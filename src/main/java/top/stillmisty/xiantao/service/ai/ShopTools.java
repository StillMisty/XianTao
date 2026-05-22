package top.stillmisty.xiantao.service.ai;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;
import top.stillmisty.xiantao.domain.shop.vo.AppraisalResult;
import top.stillmisty.xiantao.domain.shop.vo.EquipmentPurchaseResult;
import top.stillmisty.xiantao.domain.shop.vo.HaggleResult;
import top.stillmisty.xiantao.domain.shop.vo.PlayerItemsVO;
import top.stillmisty.xiantao.domain.shop.vo.ProductListVO;
import top.stillmisty.xiantao.domain.shop.vo.PurchaseResult;
import top.stillmisty.xiantao.domain.shop.vo.SellResult;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.player.UserStateService;
import top.stillmisty.xiantao.service.shop.ShopService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopTools {

  private final ShopService shopService;
  private final ItemTemplateRepository itemTemplateRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;
  private final UserStateService userStateService;

  @Tool(description = "估价物品，返回基准收购价。不可回收则拒绝。")
  public AppraisalResult appraiseItem(
      @ToolParam(description = "物品名称") String itemName,
      @ToolParam(description = "物品ID（可选，首次留空）") String itemId) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      User user = userStateService.loadUser(userId);
      ShopNpc npc = shopService.findByLocation(user.getLocationId());

      if (itemId != null && !itemId.isBlank()) {
        try {
          long id = Long.parseLong(itemId);
          var equipResult = tryAppraiseEquipment(userId, npc, id);
          if (equipResult != null) return equipResult;
          var stackResult = tryAppraiseStackable(userId, npc, id);
          if (stackResult != null) return stackResult;
        } catch (NumberFormatException ignored) {
          log.trace("鉴定: itemId \"{}\" 不是数字，尝试按名称查找", itemId);
        }
      }

      List<Equipment> matchingEquipment = shopService.findEquipmentByName(userId, itemName);
      List<StackableItem> matchingItems = shopService.findStackableItemsByName(userId, itemName);

      if (matchingEquipment.isEmpty() && matchingItems.isEmpty()) {
        return new AppraisalResult(false, 0, 0, 0, itemName, "背包中未找到名为「" + itemName + "」的物品");
      }

      int totalMatches = matchingEquipment.size() + matchingItems.size();
      if (totalMatches > 1) {
        StringBuilder sb = new StringBuilder("找到多个匹配物品：\n");
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
        sb.append("请告知具体的编号（ID）");
        return new AppraisalResult(false, 0, 0, 0, itemName, sb.toString());
      }

      if (!matchingEquipment.isEmpty()) {
        return shopService.appraiseEquipment(userId, npc, matchingEquipment.getFirst().getId());
      }
      return shopService.appraiseStackableItem(userId, npc, matchingItems.getFirst().getId());
    } catch (BusinessException e) {
      return new AppraisalResult(false, 0, 0, 0, itemName, e.getMessage());
    } catch (Exception e) {
      log.error("估价失败: itemName={}, itemId={}", itemName, itemId, e);
      return new AppraisalResult(false, 0, 0, 0, itemName, "估价失败：" + e.getMessage());
    }
  }

  @Tool(description = "砍价。每笔交易限一次，成功约降5%。")
  @Transactional
  public HaggleResult haggle(@ToolParam(description = "当前报价（灵石）") long currentPrice) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      User user = userStateService.loadUser(userId);
      ShopNpc npc = shopService.findByLocation(user.getLocationId());
      return shopService.haggleItem(userId, npc, currentPrice);
    } catch (BusinessException e) {
      return new HaggleResult(false, currentPrice, 0, e.getMessage());
    } catch (Exception e) {
      log.error("砍价失败: currentPrice={}", currentPrice, e);
      return new HaggleResult(false, currentPrice, 0, "砍价失败：" + e.getMessage());
    }
  }

  @Tool(description = "执行收购，扣除物品加灵石。价格限 appraisal/haggle 范围内。")
  @Transactional
  public SellResult sellItem(
      @ToolParam(description = "物品名称") String itemName,
      @ToolParam(description = "物品ID") String itemId,
      @ToolParam(description = "成交价格（灵石）") long confirmedPrice) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      User user = userStateService.loadUser(userId);
      ShopNpc npc = shopService.findByLocation(user.getLocationId());

      if (itemId == null || itemId.isBlank()) {
        return new SellResult(false, 0, itemName, "需要提供物品ID");
      }

      try {
        long id = Long.parseLong(itemId);
        try {
          return shopService.sellEquipment(userId, npc, id, confirmedPrice);
        } catch (BusinessException e) {
          try {
            return shopService.sellStackableItem(userId, npc, id, confirmedPrice);
          } catch (BusinessException e2) {
            return new SellResult(false, 0, itemName, e2.getMessage());
          }
        }
      } catch (NumberFormatException e) {
        return new SellResult(false, 0, itemName, "物品ID格式错误");
      }
    } catch (BusinessException e) {
      return new SellResult(false, 0, itemName, e.getMessage());
    } catch (Exception e) {
      log.error("出售失败: itemName={}, itemId={}", itemName, itemId, e);
      return new SellResult(false, 0, itemName, "出售失败：" + e.getMessage());
    }
  }

  @Tool(description = "列出在售商品、价格、库存")
  public ProductListVO listProducts() {
    try {
      Long userId = UserContext.requireCurrentUserId();
      return shopService.listProducts(userId);
    } catch (BusinessException e) {
      return new ProductListVO("未知", List.of());
    } catch (Exception e) {
      log.error("列出商品失败", e);
      return new ProductListVO("未知", List.of());
    }
  }

  @Tool(description = "购买堆叠物品（丹药/材料等）。扣灵石，添物品到背包")
  @Transactional
  public PurchaseResult purchaseItem(
      @ToolParam(description = "商品名称") String templateName,
      @ToolParam(description = "数量") int quantity) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      User user = userStateService.loadUser(userId);
      ShopNpc npc = shopService.findByLocation(user.getLocationId());

      var template =
          itemTemplateRepository
              .findByName(templateName)
              .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_PRODUCT_NOT_FOUND));

      return shopService.purchaseItem(userId, npc, template.getId(), quantity);
    } catch (BusinessException e) {
      return new PurchaseResult(false, templateName, quantity, 0, e.getMessage());
    } catch (Exception e) {
      log.error("购买物品失败: templateName={}, quantity={}", templateName, quantity, e);
      return new PurchaseResult(false, templateName, quantity, 0, "购买失败：" + e.getMessage());
    }
  }

  @Tool(description = "购买装备，随机品质和词缀。扣灵石，分配装备给玩家")
  @Transactional
  public EquipmentPurchaseResult purchaseEquipment(
      @ToolParam(description = "装备名称") String templateName) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      User user = userStateService.loadUser(userId);
      ShopNpc npc = shopService.findByLocation(user.getLocationId());

      var template = equipmentTemplateRepository.findByName(templateName).orElse(null);
      if (template == null) {
        return new EquipmentPurchaseResult(
            false, templateName, "COMMON", 0, "未找到装备：" + templateName);
      }

      return shopService.purchaseEquipment(userId, npc, template.getId());
    } catch (BusinessException e) {
      return new EquipmentPurchaseResult(false, templateName, "COMMON", 0, e.getMessage());
    } catch (Exception e) {
      log.error("购买装备失败: templateName={}", templateName, e);
      return new EquipmentPurchaseResult(
          false, templateName, "COMMON", 0, "购买失败：" + e.getMessage());
    }
  }

  @Tool(description = "查看背包所有可出售物品")
  public PlayerItemsVO queryPlayerItems() {
    try {
      Long userId = UserContext.requireCurrentUserId();
      return shopService.queryPlayerItems(userId);
    } catch (Exception e) {
      log.error("批量查询玩家物品失败", e);
      return new PlayerItemsVO(List.of(), List.of());
    }
  }

  private AppraisalResult tryAppraiseEquipment(Long userId, ShopNpc npc, Long equipmentId) {
    try {
      return shopService.appraiseEquipment(userId, npc, equipmentId);
    } catch (Exception e) {
      log.debug("鉴定装备失败, fallback: equipmentId={}", equipmentId, e);
      return null;
    }
  }

  private AppraisalResult tryAppraiseStackable(Long userId, ShopNpc npc, Long itemId) {
    try {
      return shopService.appraiseStackableItem(userId, npc, itemId);
    } catch (Exception e) {
      log.debug("鉴定堆叠物品失败, fallback: itemId={}", itemId, e);
      return null;
    }
  }
}
