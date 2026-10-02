package top.stillmisty.xiantao.service.shop;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;
import top.stillmisty.xiantao.domain.shop.entity.ShopSpecialOrder;
import top.stillmisty.xiantao.domain.shop.enums.SpecialOrderStatus;
import top.stillmisty.xiantao.domain.shop.vo.SpecialOrderVO;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.ShopProductRepository;
import top.stillmisty.xiantao.infrastructure.repository.ShopSpecialOrderRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

/**
 * 调货服务 — 本店无货/售罄物品的预定。
 *
 * <p>流程：收总价 10% 定金 → 按价格档等待 2~12 小时 → 货到（惰性判定并落库）→ 补尾款取货。取消规则：未到货（PENDING）全额退还定金；已到货（READY）定金不退。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpecialOrderService {

  /** 定金比例（总价的 10%） */
  private static final double DEPOSIT_RATIO = 0.10;

  /** 非在售物品的调货报价倍率（相对 item_template.base_value） */
  private static final double OFF_MENU_PRICE_MULTIPLIER = 2.0;

  /** 单笔调货最大件数 */
  private static final int MAX_QUANTITY = 99;

  private static final DateTimeFormatter READY_TIME_FORMAT =
      DateTimeFormatter.ofPattern("MM-dd HH:mm");

  private final ShopSpecialOrderRepository specialOrderRepository;
  private final ShopProductRepository shopProductRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final SpiritStoneService spiritStoneService;
  private final StackableItemService stackableItemService;

  /** 下单调货：收 10% 定金并生成 PENDING 订单 */
  @Transactional
  public SpecialOrderVO placeOrder(Long userId, ShopNpc npc, Long templateId, int quantity) {
    if (quantity <= 0 || quantity > MAX_QUANTITY) {
      throw new BusinessException(ErrorCode.PARAM_INVALID, "调货数量需在 1~" + MAX_QUANTITY + " 之间");
    }
    ItemTemplate template =
        itemTemplateRepository
            .findById(templateId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_EXISTS));

    long unitPrice = resolveUnitPrice(npc, template);
    long totalPrice = unitPrice * quantity;
    long deposit = Math.max(1L, (long) Math.ceil(totalPrice * DEPOSIT_RATIO));
    if (deposit >= totalPrice) {
      // 极便宜物品也留 1 灵石尾款，保证「补尾款取货」流程完整
      deposit = Math.max(1L, totalPrice - 1);
    }
    int sourcingHours = sourcingHoursFor(unitPrice);

    spiritStoneService.withdraw(userId, deposit, "special_order_deposit");

    LocalDateTime now = TimeUtil.now();
    ShopSpecialOrder order = new ShopSpecialOrder();
    order.setPlayerId(userId);
    order.setShopNpcId(npc.getId());
    order.setTemplateId(template.getId());
    order.setUnitPrice(unitPrice);
    order.setQuantity(quantity);
    order.setDeposit(deposit);
    order.setStatus(SpecialOrderStatus.PENDING);
    order.setSourcingHours(sourcingHours);
    order.setCreatedAt(now);
    specialOrderRepository.save(order);

    log.debug(
        "调货下单: userId={}, npcId={}, templateId={}, qty={}, unitPrice={}, deposit={}, hours={}",
        userId,
        npc.getId(),
        templateId,
        quantity,
        unitPrice,
        deposit,
        sourcingHours);
    return toVO(order, template.getName(), now, 0L);
  }

  /** 查询玩家全部调货订单（顺带惰性结算到货状态并落库） */
  @Transactional
  public List<SpecialOrderVO> listOrders(Long userId) {
    LocalDateTime now = TimeUtil.now();
    List<ShopSpecialOrder> orders = specialOrderRepository.findByPlayerId(userId);
    for (ShopSpecialOrder order : orders) {
      settleReadiness(order, now);
    }
    Map<Long, ItemTemplate> templates =
        itemTemplateRepository
            .findByIds(orders.stream().map(ShopSpecialOrder::getTemplateId).distinct().toList())
            .stream()
            .collect(Collectors.toMap(ItemTemplate::getId, t -> t));
    List<ShopSpecialOrder> sorted = new java.util.ArrayList<>(orders);
    sorted.sort(Comparator.comparing(ShopSpecialOrder::getId).reversed());
    return sorted.stream()
        .map(order -> toVO(order, templateName(templates, order.getTemplateId()), now, 0L))
        .toList();
  }

  /** 补尾款取货：仅 READY 订单可执行，原子扣尾款并发放物品 */
  @Transactional
  public SpecialOrderVO collectOrder(Long userId, ShopNpc npc, Long orderId) {
    ShopSpecialOrder order = requireOwnedOrder(userId, npc, orderId);
    LocalDateTime now = TimeUtil.now();
    settleReadiness(order, now);

    if (order.getStatus() == SpecialOrderStatus.COLLECTED) {
      throw new BusinessException(ErrorCode.SHOP_SPECIAL_ORDER_ALREADY_COLLECTED);
    }
    if (order.getStatus() == SpecialOrderStatus.CANCELLED) {
      throw new BusinessException(ErrorCode.SHOP_SPECIAL_ORDER_CANCELLED);
    }
    if (order.getStatus() != SpecialOrderStatus.READY) {
      throw new BusinessException(
          ErrorCode.SHOP_SPECIAL_ORDER_NOT_READY, remainingText(order, now));
    }

    ItemTemplate template =
        itemTemplateRepository
            .findById(order.getTemplateId())
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_EXISTS));

    // 条件更新防并发重复取货；失败方在下方统一报「已取货」
    int claimed =
        specialOrderRepository.updateStatusIf(
            orderId, userId, SpecialOrderStatus.READY, SpecialOrderStatus.COLLECTED);
    if (claimed == 0) {
      throw new BusinessException(ErrorCode.SHOP_SPECIAL_ORDER_ALREADY_COLLECTED);
    }

    long tailPayment = tailPayment(order);
    if (tailPayment > 0) {
      spiritStoneService.withdraw(userId, tailPayment, "special_order_tail");
    }
    stackableItemService.addStackableItem(
        userId, template.getId(), template.getType(), template.getName(), order.getQuantity());

    order.setStatus(SpecialOrderStatus.COLLECTED);
    log.debug(
        "调货取货: userId={}, orderId={}, item={}, qty={}, tail={}",
        userId,
        orderId,
        template.getName(),
        order.getQuantity(),
        tailPayment);
    return toVO(order, template.getName(), now, 0L);
  }

  /** 取消订单：未到货全额退还定金；已到货定金不退（商人已把货运回） */
  @Transactional
  public SpecialOrderVO cancelOrder(Long userId, ShopNpc npc, Long orderId) {
    ShopSpecialOrder order = requireOwnedOrder(userId, npc, orderId);
    LocalDateTime now = TimeUtil.now();
    settleReadiness(order, now);

    SpecialOrderStatus from = order.getStatus();
    if (from == SpecialOrderStatus.COLLECTED) {
      throw new BusinessException(ErrorCode.SHOP_SPECIAL_ORDER_ALREADY_COLLECTED);
    }
    if (from == SpecialOrderStatus.CANCELLED) {
      throw new BusinessException(ErrorCode.SHOP_SPECIAL_ORDER_CANCELLED);
    }

    int updated =
        specialOrderRepository.updateStatusIf(orderId, userId, from, SpecialOrderStatus.CANCELLED);
    if (updated == 0) {
      throw new BusinessException(ErrorCode.SHOP_SPECIAL_ORDER_CANCELLED);
    }

    long refundedDeposit = 0L;
    if (from == SpecialOrderStatus.PENDING) {
      refundedDeposit = order.getDeposit();
      if (refundedDeposit > 0) {
        spiritStoneService.deposit(userId, refundedDeposit, "special_order_refund");
      }
    }

    order.setStatus(SpecialOrderStatus.CANCELLED);
    ItemTemplate template = itemTemplateRepository.findById(order.getTemplateId()).orElse(null);
    String itemName = template != null ? template.getName() : "未知物品";
    log.debug(
        "调货取消: userId={}, orderId={}, from={}, refund={}", userId, orderId, from, refundedDeposit);
    return toVO(order, itemName, now, refundedDeposit);
  }

  /** 供掌柜对话 Prompt 使用的订单进度摘要；该掌柜名下无进行中订单时返回 null */
  @Transactional
  public @Nullable String describeOrdersForPrompt(Long userId, Long shopNpcId) {
    LocalDateTime now = TimeUtil.now();
    List<ShopSpecialOrder> orders =
        specialOrderRepository.findByPlayerId(userId).stream()
            .filter(order -> shopNpcId.equals(order.getShopNpcId()))
            .filter(
                order ->
                    order.getStatus() == SpecialOrderStatus.PENDING
                        || order.getStatus() == SpecialOrderStatus.READY)
            .toList();
    if (orders.isEmpty()) return null;

    Map<Long, ItemTemplate> templates =
        itemTemplateRepository
            .findByIds(orders.stream().map(ShopSpecialOrder::getTemplateId).distinct().toList())
            .stream()
            .collect(Collectors.toMap(ItemTemplate::getId, t -> t));
    StringBuilder sb = new StringBuilder("客人当前的调货订单（对话中可顺带告知进度）：\n");
    for (ShopSpecialOrder order : orders) {
      settleReadiness(order, now);
      sb.append("- [订单 ")
          .append(order.getId())
          .append("] ")
          .append(templateName(templates, order.getTemplateId()))
          .append(" x")
          .append(order.getQuantity());
      if (order.getStatus() == SpecialOrderStatus.READY) {
        sb.append("：已到货，补 ").append(tailPayment(order)).append(" 灵石尾款即可取货");
      } else {
        sb.append("：调货中，预计还需 ").append(remainingText(order, now));
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  // ===================== 内部辅助方法 =====================

  /** 报价规则：在售商品按当前售价；未售商品按 base_value × 2（调货溢价，防止与回收价套利）。 */
  private long resolveUnitPrice(ShopNpc npc, ItemTemplate template) {
    var product = shopProductRepository.findByShopNpcIdAndTemplateId(npc.getId(), template.getId());
    if (product.isPresent()) {
      return Math.max(1L, product.get().getCurrentPrice());
    }
    long baseValue = template.getBaseValue() != null ? template.getBaseValue() : 0L;
    return Math.max(1L, Math.round(baseValue * OFF_MENU_PRICE_MULTIPLIER));
  }

  /** 调货时长按单价档位：2 / 4 / 6 / 8 / 10 / 12 小时 */
  static int sourcingHoursFor(long unitPrice) {
    if (unitPrice < 100) return 2;
    if (unitPrice < 500) return 4;
    if (unitPrice < 2_000) return 6;
    if (unitPrice < 10_000) return 8;
    if (unitPrice < 50_000) return 10;
    return 12;
  }

  private ShopSpecialOrder requireOwnedOrder(Long userId, ShopNpc npc, Long orderId) {
    ShopSpecialOrder order =
        specialOrderRepository
            .findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_SPECIAL_ORDER_NOT_FOUND));
    if (!userId.equals(order.getPlayerId())) {
      throw new BusinessException(ErrorCode.SHOP_SPECIAL_ORDER_NOT_FOUND);
    }
    if (!npc.getId().equals(order.getShopNpcId())) {
      throw new BusinessException(ErrorCode.PARAM_INVALID, "请到原下单的商铺取货或取消");
    }
    return order;
  }

  /** 惰性到货：PENDING 且已过 createdAt + sourcing_hours 时置 READY 并落库 */
  private void settleReadiness(ShopSpecialOrder order, LocalDateTime now) {
    if (order.getStatus() != SpecialOrderStatus.PENDING) return;
    LocalDateTime readyAt = readyAt(order);
    if (readyAt != null && !now.isBefore(readyAt)) {
      order.setStatus(SpecialOrderStatus.READY);
      specialOrderRepository.save(order);
    }
  }

  private @Nullable LocalDateTime readyAt(ShopSpecialOrder order) {
    LocalDateTime createdAt = order.getCreatedAt();
    Integer hours = order.getSourcingHours();
    if (createdAt == null || hours == null) return null;
    return createdAt.plusHours(hours);
  }

  private long tailPayment(ShopSpecialOrder order) {
    long total = order.getUnitPrice() * order.getQuantity();
    return Math.max(0L, total - order.getDeposit());
  }

  private String remainingText(ShopSpecialOrder order, LocalDateTime now) {
    LocalDateTime readyAt = readyAt(order);
    if (readyAt == null) return "片刻";
    long minutes = Math.max(1L, Duration.between(now, readyAt).toMinutes());
    if (minutes < 60) return minutes + " 分钟";
    return ((minutes + 59) / 60) + " 小时";
  }

  private static String templateName(Map<Long, ItemTemplate> templates, Long templateId) {
    ItemTemplate template = templates.get(templateId);
    return template != null ? template.getName() : "未知物品";
  }

  private SpecialOrderVO toVO(
      ShopSpecialOrder order, String itemName, LocalDateTime now, long refundedDeposit) {
    LocalDateTime readyAt = readyAt(order);
    boolean canCollect = order.getStatus() == SpecialOrderStatus.READY;
    return new SpecialOrderVO(
        order.getId(),
        itemName,
        order.getStatus().getCode(),
        order.getStatus().getName(),
        order.getUnitPrice(),
        order.getQuantity(),
        order.getDeposit(),
        tailPayment(order),
        order.getSourcingHours(),
        readyAt != null ? READY_TIME_FORMAT.format(readyAt) : "",
        canCollect,
        refundedDeposit);
  }
}
