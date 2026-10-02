package top.stillmisty.xiantao.service.shop;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.shop.vo.TravelerGoodsVO;
import top.stillmisty.xiantao.domain.shop.vo.TravelerPurchaseResult;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

/**
 * 旅行商人临时商铺 — 事件触发的无 shop_npc 数据商铺。
 *
 * <p>玩家在「旅行商人」选择事件中做出选择后，商人就地开摊：从物品模板随机 4~6 件，单价 = base_value × 随机 0.5~3.0，库存 1~3，摊位 30
 * 分钟后消失（内存会话，玩家维度）。交易经 LLM 对话调用工具完成，价格与库存全部由程序裁决。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TravelerShopService {

  /** 触发旅行商人开摊的 CHOICE 活动事件 code */
  public static final String TRAVEL_MERCHANT_EVENT_CODE = "travel_curious_merchant";

  /** 摊位存活时长（分钟） */
  static final int SESSION_TTL_MINUTES = 30;

  private static final int MIN_GOODS = 4;
  private static final int MAX_GOODS = 6;
  private static final double MIN_PRICE_MULTIPLIER = 0.5;
  private static final double MAX_PRICE_MULTIPLIER = 3.0;
  private static final int MAX_STOCK = 3;
  private static final List<String> MERCHANT_NAMES =
      List.of("云游客商", "行脚货郎", "江湖商贾", "游方老丈", "异乡商贩");

  private final ItemTemplateRepository itemTemplateRepository;
  private final SpiritStoneService spiritStoneService;
  private final StackableItemService stackableItemService;

  private final Map<Long, TravelerSession> sessions = new ConcurrentHashMap<>();
  private final AtomicLong sessionSequence = new AtomicLong();

  /** 开摊（或替换旧摊）：随机货物 4~6 件。模板池为空时返回 null（调用方静默跳过） */
  public @Nullable TravelerSession openSession(Long userId) {
    LocalDateTime now = TimeUtil.now();
    pruneExpired(now);
    List<ItemTemplate> pool =
        itemTemplateRepository.findByTypes(Arrays.asList(ItemType.values())).stream()
            .filter(template -> template.getBaseValue() != null && template.getBaseValue() > 0)
            .collect(Collectors.toCollection(ArrayList::new));
    if (pool.isEmpty()) {
      log.warn("旅行商人开摊失败：没有带基准价的物品模板");
      return null;
    }
    Collections.shuffle(pool, ThreadLocalRandom.current());
    int count =
        Math.min(
            pool.size(),
            MIN_GOODS + ThreadLocalRandom.current().nextInt(MAX_GOODS - MIN_GOODS + 1));
    List<TravelerGood> goods = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      goods.add(rollGood(pool.get(i)));
    }
    TravelerSession session =
        new TravelerSession(
            sessionSequence.incrementAndGet(),
            pickMerchantName(),
            now.plusMinutes(SESSION_TTL_MINUTES),
            userId,
            goods);
    sessions.put(userId, session);
    log.debug("旅行商人开摊: userId={}, sessionId={}, goods={}", userId, session.sessionId(), count);
    return session;
  }

  /** 取当前有效会话；不存在或已过期时抛 TRAVELER_NOT_FOUND */
  public TravelerSession requireSession(Long userId) {
    LocalDateTime now = TimeUtil.now();
    TravelerSession session = sessions.get(userId);
    if (session == null || session.isExpired(now)) {
      if (session != null) sessions.remove(userId, session);
      throw new BusinessException(ErrorCode.TRAVELER_NOT_FOUND);
    }
    return session;
  }

  /** 货摊在，但已无货时返回空清单（会话仍在有效期内） */
  public TravelerGoodsVO listGoods(Long userId) {
    TravelerSession session = requireSession(userId);
    long remainingMinutes =
        Math.max(1L, Duration.between(TimeUtil.now(), session.expiresAt()).toMinutes());
    List<TravelerGoodsVO.TravelerGood> goods =
        session.goods().stream()
            .filter(good -> good.getStock() > 0)
            .map(
                good ->
                    new TravelerGoodsVO.TravelerGood(
                        good.getTemplateId(),
                        good.getName(),
                        good.getItemType().getName(),
                        good.getUnitPrice(),
                        good.getStock()))
            .toList();
    return new TravelerGoodsVO(session.merchantName(), remainingMinutes, goods);
  }

  /** 购买：原子扣灵石 + 入背包 + 扣摊位库存 */
  @Transactional
  public TravelerPurchaseResult buy(Long userId, String goodsName, int quantity) {
    if (quantity <= 0) {
      throw new BusinessException(ErrorCode.PARAM_INVALID, "购买数量必须大于0");
    }
    TravelerSession session = requireSession(userId);
    TravelerGood good = findGood(session, goodsName);

    long totalPrice;
    synchronized (session) {
      if (good.getStock() < quantity) {
        throw new BusinessException(
            ErrorCode.TRAVELER_STOCK_INSUFFICIENT, quantity, good.getStock());
      }
      totalPrice = good.getUnitPrice() * quantity;
      spiritStoneService.withdraw(userId, totalPrice, "traveler");
      stackableItemService.addStackableItem(
          userId, good.getTemplateId(), good.getItemType(), good.getName(), quantity);
      good.reduceStock(quantity);
    }
    log.debug(
        "旅行商人成交: userId={}, item={}, qty={}, total={}, remainingStock={}",
        userId,
        good.getName(),
        quantity,
        totalPrice,
        good.getStock());
    return new TravelerPurchaseResult(good.getName(), quantity, totalPrice, good.getStock());
  }

  /** 选择事件后附加给玩家的摊位提示 */
  public static String sessionHint() {
    return "旅行商人就地支起了货摊（限时 " + SESSION_TTL_MINUTES + " 分钟）：可用「游商 看货」挑拣货物。";
  }

  // ===================== 内部辅助 =====================

  private TravelerGood rollGood(ItemTemplate template) {
    double multiplier =
        MIN_PRICE_MULTIPLIER
            + ThreadLocalRandom.current().nextDouble()
                * (MAX_PRICE_MULTIPLIER - MIN_PRICE_MULTIPLIER);
    long baseValue = template.getBaseValue() != null ? template.getBaseValue() : 0L;
    long unitPrice = Math.max(1L, Math.round(baseValue * multiplier));
    int stock = 1 + ThreadLocalRandom.current().nextInt(MAX_STOCK);
    return new TravelerGood(
        template.getId(), template.getType(), template.getName(), unitPrice, stock);
  }

  private TravelerGood findGood(TravelerSession session, String goodsName) {
    String query = goodsName == null ? "" : goodsName.trim();
    List<TravelerGood> exact =
        session.goods().stream().filter(good -> good.getName().equals(query)).toList();
    if (!exact.isEmpty()) return exact.getFirst();
    List<TravelerGood> fuzzy =
        query.isEmpty()
            ? List.of()
            : session.goods().stream().filter(good -> good.getName().contains(query)).toList();
    if (fuzzy.size() == 1) return fuzzy.getFirst();
    if (fuzzy.size() > 1) {
      throw new BusinessException(
          ErrorCode.TRAVELER_GOODS_AMBIGUOUS,
          fuzzy.stream().map(TravelerGood::getName).collect(Collectors.joining("、")));
    }
    throw new BusinessException(ErrorCode.TRAVELER_GOODS_NOT_FOUND, query);
  }

  private void pruneExpired(LocalDateTime now) {
    sessions.values().removeIf(session -> session.isExpired(now));
  }

  private static String pickMerchantName() {
    return MERCHANT_NAMES.get(ThreadLocalRandom.current().nextInt(MERCHANT_NAMES.size()));
  }

  /** 玩家维度的临时摊位会话（内存态，重启即失效） */
  public record TravelerSession(
      long sessionId,
      String merchantName,
      LocalDateTime expiresAt,
      Long userId,
      List<TravelerGood> goods) {

    public boolean isExpired(LocalDateTime now) {
      return !now.isBefore(expiresAt);
    }
  }

  /** 摊位上的一件货：价格在开摊时随机定格，库存可被购买消耗 */
  public static final class TravelerGood {
    private final long templateId;
    private final ItemType itemType;
    private final String name;
    private final long unitPrice;
    private int stock;

    TravelerGood(long templateId, ItemType itemType, String name, long unitPrice, int stock) {
      this.templateId = templateId;
      this.itemType = itemType;
      this.name = name;
      this.unitPrice = unitPrice;
      this.stock = stock;
    }

    public void reduceStock(int quantity) {
      this.stock -= quantity;
    }

    public long getTemplateId() {
      return templateId;
    }

    public ItemType getItemType() {
      return itemType;
    }

    public String getName() {
      return name;
    }

    public long getUnitPrice() {
      return unitPrice;
    }

    public int getStock() {
      return stock;
    }
  }
}
