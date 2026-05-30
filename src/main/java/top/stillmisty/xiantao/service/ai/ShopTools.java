package top.stillmisty.xiantao.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;
import top.stillmisty.xiantao.domain.shop.vo.AppraisalResult;
import top.stillmisty.xiantao.domain.shop.vo.EquipmentPurchaseResult;
import top.stillmisty.xiantao.domain.shop.vo.HaggleResult;
import top.stillmisty.xiantao.domain.shop.vo.PlayerItemsVO;
import top.stillmisty.xiantao.domain.shop.vo.ProductListVO;
import top.stillmisty.xiantao.domain.shop.vo.PurchaseResult;
import top.stillmisty.xiantao.domain.shop.vo.SellResult;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.player.UserStateService;
import top.stillmisty.xiantao.service.shop.ShopService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopTools {

  private final ToolExecutor toolExecutor;
  private final ShopService shopService;
  private final ItemTemplateRepository itemTemplateRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;
  private final UserStateService userStateService;

  private UserAndNpc resolveUserAndNpc() {
    ShopChatContext ctx = ShopChatContext.current();
    if (ctx != null) {
      return new UserAndNpc(ctx.user(), ctx.npc());
    }
    Long userId = UserContext.requireCurrentUserId();
    User user = userStateService.loadUser(userId);
    ShopNpc npc = shopService.findByLocation(user.getLocationId());
    return new UserAndNpc(user, npc);
  }

  private record UserAndNpc(User user, ShopNpc npc) {}

  /**
   * 查看商品列表：当前所在店铺的所有可购买商品及其价格和库存。
   *
   * <p>在客人询问"有什么卖的"时调用。返回店铺名称和商品清单。 每个商品含编号(id)、类型、名称、价格(灵石)和库存。
   */
  @Tool(description = "展示店铺所有可购商品清单，含商品编号、名称、价格(灵石)和库存")
  public ProductListVO showGoods() {
    return toolExecutor.execute(
        "showGoods",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          return shopService.listProductsInternal(userId);
        });
  }

  /**
   * 查看客人背包中可供出售的物品。
   *
   * <p>返回两类物品：
   *
   * <ul>
   *   <li>装备列表（未装备的装备，含编号、名称、稀有度、锻造等级）
   *   <li>堆叠物品列表（含编号、名称、数量、类型、是否可交易）
   * </ul>
   *
   * <p>tradable=false 的物品不可回收，掌柜应拒绝收购。
   */
  @Tool(description = "查看客人背包中可出售/可回收的物品清单，含编号和是否可交易")
  public PlayerItemsVO checkPlayerItems() {
    return toolExecutor.execute(
        "checkPlayerItems",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          return shopService.queryPlayerItems(userId);
        });
  }

  /**
   * 估价物品：查看客人出售物品能卖多少灵石。
   *
   * <p>两阶段查找逻辑（无需客人参与，由代码自动处理）：
   *
   * <ol>
   *   <li>若 itemId 为空或 blank，按名称搜索：找到唯一匹配则直接估价；找到多个则列出所有匹配让客人指定编号
   *   <li>若 itemId 不为空，直接用编号估价
   * </ol>
   *
   * <p>返回结果：
   *
   * <ul>
   *   <li>tradable=false：物品不可交易，拒绝收购
   *   <li>tradable=true：basePrice=掌柜首次报价（必须以此报价开始），minPrice/maxPrice=砍价范围
   * </ul>
   *
   * <p>重要：报价必须等于 basePrice，不可自行编造价格。
   *
   * @param itemName 物品名称（客人表述的物品名）
   * @param itemId 物品编号（若有多个重名物品时客人指定；首次留空即可）
   */
  @Tool(description = "估价客人要出售的物品，返回收购价范围。首次报价必须等于 basePrice，不可自行编造")
  public AppraisalResult appraiseItem(
      @ToolParam(description = "物品名称") String itemName,
      @ToolParam(description = "物品编号，首次留空，重名时由客人指定") String itemId) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      UserAndNpc resolved = resolveUserAndNpc();

      if (itemId != null && !itemId.isBlank()) {
        try {
          long id = Long.parseLong(itemId);
          var equipResult = tryAppraiseEquipment(userId, resolved.npc(), id);
          if (equipResult != null) return equipResult;
          var stackResult = tryAppraiseStackable(userId, resolved.npc(), id);
          if (stackResult != null) return stackResult;
        } catch (NumberFormatException ignored) {
          log.trace("估价: itemId \"{}\" 不是数字，尝试按名称查找", itemId);
        }
      }

      var matchingEquipment = shopService.findEquipmentByName(userId, itemName);
      var matchingItems = shopService.findStackableItemsByName(userId, itemName);

      if (matchingEquipment.isEmpty() && matchingItems.isEmpty()) {
        return new AppraisalResult(false, 0, 0, 0, itemName, "背包中未找到名为「" + itemName + "」的物品");
      }

      int totalMatches = matchingEquipment.size() + matchingItems.size();
      if (totalMatches > 1) {
        var sb = new StringBuilder("找到多个匹配物品，请客人确认具体是哪一个：\n");
        for (Equipment e : matchingEquipment) {
          sb.append("  [装备] ID:").append(e.getId()).append(" ").append(e.getName());
          if (e.getRarity() != null) sb.append(" (").append(e.getRarity().getName()).append(")");
          sb.append("\n");
        }
        for (StackableItem s : matchingItems) {
          sb.append("  [物品] ID:")
              .append(s.getId())
              .append(" ")
              .append(s.getName())
              .append(" x")
              .append(s.getQuantity())
              .append("\n");
        }
        sb.append("请告知具体编号。");
        return new AppraisalResult(false, 0, 0, 0, itemName, sb.toString());
      }

      if (!matchingEquipment.isEmpty()) {
        return shopService.appraiseEquipment(
            userId, resolved.npc(), matchingEquipment.getFirst().getId());
      }
      return shopService.appraiseStackableItem(
          userId, resolved.npc(), matchingItems.getFirst().getId());
    } catch (BusinessException e) {
      return new AppraisalResult(
          false, 0, 0, 0, itemName, e.getMessage() != null ? e.getMessage() : "估价失败");
    } catch (Exception e) {
      log.error("估价失败: itemName={}, itemId={}", itemName, itemId, e);
      return new AppraisalResult(false, 0, 0, 0, itemName, "估价失败：" + e.getMessage());
    }
  }

  /**
   * 讨价还价：客人要求更有利的价格。
   *
   * <p>每轮对话仅允许讨价还价一次——若已讨价过，直接拒绝。 买家砍价（isBuying=true）：尝试降价约 3%~15%；卖家抬价（isBuying=false）：尝试提价约
   * 3%~15%。
   *
   * @param currentPrice 当前的灵石报价
   * @param basePrice 商品的基准价格（从 showGoods 或 appraiseItem 获取，用于校验价格范围）
   * @param isBuying true=客人在买东西想降价，false=客人在卖东西想提价
   */
  @Tool(
      description =
          "与客人讨价还价，每轮对话限一次。isBuying=true=砍价(降价)，false=抬价(提价)。currentPrice 为当前灵石报价，basePrice 为基准价")
  @Transactional
  public HaggleResult negotiatePrice(
      @ToolParam(description = "当前报价（灵石）") long currentPrice,
      @ToolParam(description = "基准价格（灵石），从 showGoods 或 appraiseItem 结果中获取") long basePrice,
      @ToolParam(description = "客人是否在买东西(ture=买家砍价，false=卖家抬价)") boolean isBuying) {
    return toolExecutor.execute(
        "negotiatePrice",
        () -> {
          ShopChatContext chatCtx = ShopChatContext.current();
          if (chatCtx != null && chatCtx.isHaggleUsed()) {
            return new HaggleResult(
                false, currentPrice, 0, isBuying ? "客官，方才已让过利了，这价不能再降了" : "客官，方才已加过价了，这价不能再升了");
          }

          long minValid = shopService.getMinPrice(basePrice);
          long maxValid = shopService.getMaxPrice(basePrice);
          if (currentPrice < minValid || currentPrice > maxValid) {
            return new HaggleResult(
                false,
                currentPrice,
                0,
                "客官，你报的 " + currentPrice + " 灵石不在合理范围（" + minValid + "~" + maxValid + "），请重新确认价格");
          }

          Long userId = UserContext.requireCurrentUserId();
          UserAndNpc resolved = resolveUserAndNpc();
          HaggleResult result =
              shopService.haggleItem(userId, resolved.npc(), currentPrice, basePrice, isBuying);
          if (chatCtx != null) {
            chatCtx.markHaggled();
          }
          return result;
        });
  }

  /**
   * 收购客人出售的物品，付灵石给对方。
   *
   * <p>成交价格必须在估价范围（appraiseItem 的 minPrice~maxPrice）内。 若经过砍价（negotiatePrice），以砍价后的价格为准。
   *
   * @param itemName 物品名称
   * @param itemId 物品编号（从 checkPlayerItems 或 appraiseItem 的结果中获取）
   * @param confirmedPrice 成交价格（灵石），必须在估价/砍价范围内的价格
   */
  @Tool(description = "从客人手里收购物品。itemId 是物品编号，confirmedPrice 必须均在估价/砍价的价格范围内")
  @Transactional
  public SellResult buyItem(
      @ToolParam(description = "物品名称") String itemName,
      @ToolParam(description = "物品编号") String itemId,
      @ToolParam(description = "成交价格（灵石），必须在对估价/砍价得到的价格") long confirmedPrice) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      UserAndNpc resolved = resolveUserAndNpc();

      if (itemId == null || itemId.isBlank()) {
        throw new IllegalArgumentException("需要提供物品编号");
      }

      try {
        long id = Long.parseLong(itemId);
        try {
          return shopService.sellEquipment(userId, resolved.npc(), id, confirmedPrice);
        } catch (BusinessException e) {
          if (e.getErrorCode() == ErrorCode.EQUIPMENT_NOT_FOUND) {
            return shopService.sellStackableItem(userId, resolved.npc(), id, confirmedPrice);
          }
          throw e;
        }
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("物品编号格式错误");
      }
    } catch (BusinessException e) {
      log.error("收购失败: itemName={}, itemId={}", itemName, itemId, e);
      throw e;
    }
  }

  /**
   * 出售堆叠物品（丹药、材料等）给客人，扣灵石后将物品放入客人背包。
   *
   * <p>前提：客人有足够灵石、店铺有足够库存。
   *
   * @param templateName 商品名称（必须与 showGoods 结果中的 name 完全一致）
   * @param quantity 购买数量
   */
  @Tool(description = "卖给客人丹药/材料等堆叠物品。templateName 必须与 showGoods 商品名称一致")
  @Transactional
  public PurchaseResult sellGoods(
      @ToolParam(description = "商品名称") String templateName,
      @ToolParam(description = "购买数量") int quantity) {
    return toolExecutor.execute(
        "sellGoods",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          UserAndNpc resolved = resolveUserAndNpc();

          var template =
              itemTemplateRepository
                  .findByName(templateName)
                  .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_PRODUCT_NOT_FOUND));

          return shopService.purchaseItemInternal(
              userId, resolved.npc(), template.getId(), quantity);
        });
  }

  /**
   * 出售装备给客人。装备品质和词缀随机生成，扣灵石后分配给客人。
   *
   * <p>品质范围：COMMON/UNCOMMON/RARE/EPIC/LEGENDARY，不可人为指定品质。
   *
   * @param templateName 装备名称（必须与 showGoods 商品名称一致）
   */
  @Tool(description = "卖给客人装备，品质随机。templateName 必须与 showGoods 商品名称一致")
  @Transactional
  public EquipmentPurchaseResult sellEquipment(
      @ToolParam(description = "装备名称") String templateName) {
    return toolExecutor.execute(
        "sellEquipment",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          UserAndNpc resolved = resolveUserAndNpc();

          var template = equipmentTemplateRepository.findByName(templateName).orElse(null);
          if (template == null) {
            throw new IllegalArgumentException("未找到装备：" + templateName);
          }

          return shopService.purchaseEquipmentInternal(userId, resolved.npc(), template.getId());
        });
  }

  // ===================== 私有辅助方法 =====================

  @Nullable
  private AppraisalResult tryAppraiseEquipment(Long userId, ShopNpc npc, Long equipmentId) {
    try {
      return shopService.appraiseEquipment(userId, npc, equipmentId);
    } catch (Exception e) {
      log.debug("估价装备失败, fallback: equipmentId={}", equipmentId, e);
      return null;
    }
  }

  @Nullable
  private AppraisalResult tryAppraiseStackable(Long userId, ShopNpc npc, Long itemId) {
    try {
      return shopService.appraiseStackableItem(userId, npc, itemId);
    } catch (Exception e) {
      log.debug("估价堆叠物品失败, fallback: itemId={}", itemId, e);
      return null;
    }
  }
}
