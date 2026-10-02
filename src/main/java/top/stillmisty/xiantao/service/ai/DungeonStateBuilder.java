package top.stillmisty.xiantao.service.ai;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonSpiritState;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate.AreaConfig;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate.Poi;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;

@Component
public class DungeonStateBuilder {

  public String buildSystemPrompt(
      DungeonTemplate dungeon, DungeonInstance instance, @Nullable DungeonSpiritState spiritState) {

    AreaConfig currentArea = findArea(dungeon, instance.getCurrentAreaKey());
    if (currentArea == null) {
      return "当前区域不存在。";
    }

    StringBuilder sb = new StringBuilder();

    // ── 身份定义 ──
    sb.append("你正在扮演秘境【").append(dungeon.getName()).append("】");

    if (dungeon.hasSpirit()) {
      var sc = dungeon.getSpiritConfig();
      if (sc != null) {
        sb.append("的秘境之灵「").append(sc.spiritName()).append("」。\n\n");
        sb.append("你的形象：").append(sc.spiritAppearance()).append("\n");
        sb.append("你的性格：").append(sc.personality()).append("\n");
        sb.append("语气风格：").append(sc.toneStyle()).append("\n\n");
      }
    } else {
      sb.append("的叙事者。以旁白口吻描述探索者的所见所闻。\n\n");
    }

    // ── 秘境状态 ──
    sb.append("【秘境状态】\n");
    sb.append("当前区域：").append(currentArea.name()).append("\n");
    sb.append("区域描述：").append(currentArea.description()).append("\n\n");

    sb.append("可探索地点：\n");
    for (Poi poi : currentArea.mainPois()) {
      boolean explored =
          instance.getExploredPois() != null
              && instance.getExploredPois().stream().anyMatch(e -> e.poiName().equals(poi.name()));
      String status = explored ? "（已探索）" : "";
      sb.append("- ").append(poi.name()).append(" [").append(poi.type()).append("]").append(status);
      if (!explored && poi.hasClues()) {
        sb.append("  线索：").append(String.join("、", poi.clues()));
      }
      sb.append("\n");
    }

    if (currentArea.hiddenPois() != null && !currentArea.hiddenPois().isEmpty()) {
      sb.append("\n【隐藏线索（仅在环境描述中暗示，不要直接明说）】\n");
      for (Poi hidden : currentArea.hiddenPois()) {
        if (spiritState != null
            && spiritState.getHiddenFinds() != null
            && spiritState.getHiddenFinds().contains(hidden.name())) {
          continue;
        }
        if (hidden.hasClues()) {
          sb.append("- ").append(String.join("、", hidden.clues())).append("\n");
        }
      }
    }

    if (instance.getPassageUnlocked()) {
      sb.append("\n通往下一区域的通道已开启。\n");
    }

    if (dungeon.hasSpirit() && spiritState != null) {
      sb.append("\n好感度：")
          .append(spiritState.getFavor())
          .append("（态度：")
          .append(spiritState.favorAttitude())
          .append("）\n");
    }

    // ── 流程指引 ──
    sb.append(
        """

            【流程指引】
            你拥有影响秘境状态的秘境之力，你的秘境之力会告诉你它具体能做什么。
            - 先等探索者说出意图，再思考调用哪个秘境之力
            - 如果探索者只是与环境互动（观察、走动、触摸等），用叙事描述即可，无需动用秘境之力
            - 如果拿不准探索者想做什么，先描述环境让他选择，不要替玩家做决定
            - 战斗结果由工具返回具体数据，你只需要做叙事美化（3~5句），不编造数值

            【叙事规则】""");
    sb.append(
        """
            - 你只描述环境、场景和角色，不替玩家做决定
            - 在环境描述中自然地暗示隐藏线索（如配置了线索），但不要直接点名隐藏地点的名字
            - 如果配置了隐藏 POI 没有线索，则完全不要主动提及，除非玩家明确搜索
            """);

    if (dungeon.hasAffectionSystem()) {
      sb.append("- 好感度高时可以更主动地暗示隐藏内容\n");
    }

    return sb.toString();
  }

  public String buildStatusOverview(
      DungeonTemplate dungeon, DungeonInstance instance, @Nullable DungeonSpiritState spiritState) {

    AreaConfig currentArea = findArea(dungeon, instance.getCurrentAreaKey());
    String areaName = currentArea != null ? currentArea.name() : instance.getCurrentAreaKey();
    List<Poi> explorableMainPois =
        currentArea != null
            ? currentArea.mainPois().stream().filter(poi -> !poi.isPassage()).toList()
            : List.of();
    int totalMainPois = explorableMainPois.size();
    Set<String> explorableNames =
        explorableMainPois.stream().map(Poi::name).collect(Collectors.toSet());
    List<DungeonInstance.ExploredPoiRecord> exploredPois =
        instance.getExploredPois() != null ? instance.getExploredPois() : List.of();
    int exploredCount = 0;
    for (DungeonInstance.ExploredPoiRecord record : exploredPois) {
      if (explorableNames.contains(record.poiName())) {
        exploredCount++;
      }
    }
    int favor = spiritState != null && spiritState.getFavor() != null ? spiritState.getFavor() : 0;
    long elapsedMinutes =
        java.time.Duration.between(instance.getCreatedAt(), TimeUtil.now()).toMinutes();

    StringBuilder sb = new StringBuilder();
    sb.append(areaName)
        .append(" | 探索 ")
        .append(exploredCount)
        .append("/")
        .append(totalMainPois)
        .append(" | 好感 ")
        .append(favor)
        .append(" | 用时 ")
        .append(elapsedMinutes)
        .append("min");

    if (spiritState != null
        && spiritState.getHiddenFinds() != null
        && !spiritState.getHiddenFinds().isEmpty()) {
      sb.append("\n隐藏发现: ").append(String.join(", ", spiritState.getHiddenFinds()));
    }

    return sb.toString();
  }

  @Nullable
  public AreaConfig findArea(DungeonTemplate dungeon, String areaKey) {
    if (dungeon.getAreaConfigs() == null) return null;
    return dungeon.getAreaConfigs().stream()
        .filter(a -> a.key().equals(areaKey))
        .findFirst()
        .orElse(null);
  }

  /**
   * 查找下一个可进入的区域：跳过未解锁的隐藏区域。
   *
   * <p>隐藏区域需先完成 trigger_after_resolve 指定的 POI 才解锁，否则推进时跳过。
   */
  @Nullable
  public AreaConfig findNextAccessibleArea(DungeonTemplate dungeon, DungeonInstance instance) {
    List<AreaConfig> areas = dungeon.getAreaConfigs();
    if (areas == null) return null;

    int currentIndex = -1;
    for (int i = 0; i < areas.size(); i++) {
      if (areas.get(i).key().equals(instance.getCurrentAreaKey())) {
        currentIndex = i;
        break;
      }
    }
    if (currentIndex < 0) return null;

    for (int i = currentIndex + 1; i < areas.size(); i++) {
      AreaConfig candidate = areas.get(i);
      if (isHiddenArea(candidate) && !isHiddenAreaUnlocked(dungeon, instance, candidate.key())) {
        continue;
      }
      return candidate;
    }
    return null;
  }

  /** 隐藏区域解锁判定：未配置触发条件的隐藏区域不设门槛（兼容无 trigger_after_resolve 的数据）。 */
  public boolean isHiddenAreaUnlocked(
      DungeonTemplate dungeon, DungeonInstance instance, String areaKey) {
    List<String> triggerPois = findHiddenAreaTriggers(dungeon, areaKey);
    if (triggerPois == null || triggerPois.isEmpty()) {
      return true;
    }
    return triggerPois.stream().allMatch(instance::hasExploredPoi);
  }

  @Nullable
  private List<String> findHiddenAreaTriggers(DungeonTemplate dungeon, String areaKey) {
    List<AreaConfig> areas = dungeon.getAreaConfigs();
    if (areas == null) return null;
    for (AreaConfig area : areas) {
      if (area.hiddenAreas() == null) continue;
      for (DungeonTemplate.HiddenArea hiddenArea : area.hiddenAreas()) {
        if (hiddenArea.key().equals(areaKey)) {
          return hiddenArea.triggerAfterResolve();
        }
      }
    }
    return null;
  }

  private static boolean isHiddenArea(AreaConfig area) {
    return "HIDDEN".equals(area.type());
  }

  @Nullable
  public Poi findPoi(AreaConfig area, String poiName) {
    for (Poi poi : area.allPois()) {
      if (poi.name().equals(poiName)) return poi;
    }
    return null;
  }
}
