package top.stillmisty.xiantao.service.sect;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.sect.entity.Sect;
import top.stillmisty.xiantao.infrastructure.repository.SectRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;

/**
 * 宗门动态事件 —— 纯氛围叙事，不产生任何资源增减。
 *
 * <p>由宗门总览与宗灵对话入口惰性触发：距上次事件超过生成间隔（20~24h，按宗门 ID 抖动）时，从模板池随机抽取一条新事件写入 {@code sect} 行；事件超过 48h
 * 有效期即视为过期，不再展示（下次触发时轮换）。并发触发由条件更新兜底，只有一个请求能写入成功。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SectEventGenerator {

  /** 事件生成的基础间隔（小时） */
  static final int EVENT_MIN_INTERVAL_HOURS = 20;

  /** 生成间隔的抖动范围（小时），实际间隔 = 20 ~ 24h */
  static final int EVENT_INTERVAL_JITTER_HOURS = 4;

  /** 事件展示有效期（小时） */
  static final int EVENT_TTL_HOURS = 48;

  /** 事件模板池：type 写入 {@code last_event_type}，renderer 按宗门名生成文案（可含随机参数） */
  private static final List<SectEventTemplate> TEMPLATES =
      List.of(
          new SectEventTemplate(
              "VEIN_SURGE", name -> "【" + name + "】后山地底灵脉微微涌动，云雾间灵机流转，弟子们打坐时只觉经脉舒泰。"),
          new SectEventTemplate("DAO_DEBATE", name -> "演武场上数名弟子论道至夜半，各执一词互不相让，最后相约明日以剑印证。"),
          new SectEventTemplate(
              "HERB_GARDEN",
              name ->
                  "药园今岁灵药长势极盛，采药弟子忙到日暮，共收得 "
                      + ThreadLocalRandom.current().nextInt(3, 10)
                      + " 筐灵药，药香顺着山风飘出数里。"),
          new SectEventTemplate("PILL_LUCK", name -> "丹房忽然异香冲天，有长老炼丹偶得佳品，炉火三日不熄，引来同门争相围观。"),
          new SectEventTemplate("GUARD_ARRAY", name -> "护山大阵夜半泛起微光，阵纹自山门一路游走后山，值守弟子屏息凝神，良久方归平静。"),
          new SectEventTemplate(
              "ROGUE_VISIT", name -> "一名散修叩响山门，自称仰慕【" + name + "】道统，愿以一则秘境传闻换取一宿借住。"),
          new SectEventTemplate(
              "CRANE_OMEN",
              name ->
                  "有白鹤自云端落下，绕主殿"
                      + ThreadLocalRandom.current().nextInt(2, 6)
                      + " 匝方去，弟子皆言此乃吉兆，纷纷猜测将有何等机缘。"),
          new SectEventTemplate("OLD_SCROLL", name -> "整理藏经阁时偶得前辈手札数页，墨迹虽旧，字里行间犹带剑鸣之意。"),
          new SectEventTemplate(
              "SPIRIT_RAIN",
              name ->
                  "夜来一场灵雨，山门外的青石阶洗得发亮，灵田中的灵植一夜拔高"
                      + ThreadLocalRandom.current().nextInt(1, 4)
                      + " 寸。"),
          new SectEventTemplate("SECT_FEAST", name -> "有弟子凑份子办了一场小宴，酒过三巡，众人争相说起自家师尊当年的风光事。"));

  private final SectRepository sectRepository;

  /** 惰性刷新事件；调用方传入的 {@code sect} 会被同步为最新事件状态。 */
  @Transactional
  public void ensureEvent(Sect sect) {
    LocalDateTime now = TimeUtil.now();

    // 过期事件视为无事件：先清空本地字段，避免调用方展示过期内容
    String previousType = sect.getLastEventType();
    if (isExpired(sect, now)) {
      clearLocalEvent(sect);
    }

    LocalDateTime staleBefore = now.minusHours(generationIntervalHours(sect.getId()));
    if (sect.getLastEventTime() != null && sect.getLastEventTime().isAfter(staleBefore)) {
      // 距上次事件不足生成间隔：沿用现有事件
      return;
    }

    SectEventTemplate template = pickTemplate(previousType);
    String eventText = template.renderer().apply(sect.getName());
    LocalDateTime expiresAt = now.plusHours(EVENT_TTL_HOURS);

    if (sectRepository.updateEventIfStale(
            sect.getId(), template.type(), eventText, now, expiresAt, staleBefore)
        > 0) {
      sect.setLastEventType(template.type());
      sect.setLastEventText(eventText);
      sect.setLastEventTime(now);
      sect.setEventExpiresAt(expiresAt);
      log.debug("宗门 {} 生成动态事件 [{}]", sect.getId(), template.type());
      return;
    }

    // 并发下未抢到写入：另一请求刚刚更新，重新读取以展示其事件
    clearLocalEvent(sect);
    sectRepository
        .findById(sect.getId())
        .ifPresent(
            fresh -> {
              sect.setLastEventType(fresh.getLastEventType());
              sect.setLastEventText(fresh.getLastEventText());
              sect.setLastEventTime(fresh.getLastEventTime());
              sect.setEventExpiresAt(fresh.getEventExpiresAt());
            });
  }

  /** 生成间隔（小时）：基础 20h + 按宗门 ID 抖动 0~4h；同一宗门多次触发结果一致，避免并发判断分歧。 */
  static int generationIntervalHours(Long sectId) {
    return EVENT_MIN_INTERVAL_HOURS + Math.floorMod(sectId, EVENT_INTERVAL_JITTER_HOURS + 1);
  }

  private boolean isExpired(Sect sect, LocalDateTime now) {
    LocalDateTime expiresAt = sect.getEventExpiresAt();
    return expiresAt != null && !expiresAt.isAfter(now);
  }

  /** 随机抽取模板，尽量避开上一次的事件类型（“去重”） */
  private SectEventTemplate pickTemplate(@Nullable String lastEventType) {
    List<SectEventTemplate> candidates =
        lastEventType == null
            ? TEMPLATES
            : TEMPLATES.stream().filter(t -> !t.type().equals(lastEventType)).toList();
    return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
  }

  private void clearLocalEvent(Sect sect) {
    sect.setLastEventType(null);
    sect.setLastEventText(null);
    sect.setLastEventTime(null);
    sect.setEventExpiresAt(null);
  }

  /** 事件模板：type 为事件类型短标识，renderer 负责按宗门名生成文案。 */
  private record SectEventTemplate(String type, Function<String, String> renderer) {}
}
