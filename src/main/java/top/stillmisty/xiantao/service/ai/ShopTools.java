package top.stillmisty.xiantao.service.ai;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;
import top.stillmisty.xiantao.domain.shop.vo.AppraisalResult;
import top.stillmisty.xiantao.domain.shop.vo.EquipmentListVO;
import top.stillmisty.xiantao.domain.shop.vo.EquipmentPurchaseResult;
import top.stillmisty.xiantao.domain.shop.vo.HaggleResult;
import top.stillmisty.xiantao.domain.shop.vo.ProductListVO;
import top.stillmisty.xiantao.domain.shop.vo.PurchaseResult;
import top.stillmisty.xiantao.domain.shop.vo.SellResult;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ShopService;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.UserStateService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopTools {

  private final ShopService shopService;
  private final ItemTemplateRepository itemTemplateRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;
  private final EquipmentRepository equipmentRepository;
  private final UserStateService userStateService;

  @Tool(
      description =
          """
      估价玩家想要出售的物品，返回系统计算的基准收购价。
      首次报价必须等于返回的 basePrice。
      如果物品不可回收，tradable 为 false，直接拒绝回收。
      """)
  public AppraisalResult appraiseItem(
      @ToolParam(description = "物品名称，如'玄铁剑'、'聚气丹'") String itemName,
      @ToolParam(description = "具体实例ID（装备需要）。如果不确定，先不传此参数，首次调用会返回提示") String itemId) {
    try {
      Long userId = getCurrentUserId();
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
      if (!matchingItems.isEmpty()) {
        return shopService.appraiseStackableItem(userId, npc, matchingItems.getFirst().getId());
      }

      return new AppraisalResult(false, 0, 0, 0, itemName, "未找到可鉴定的物品：" + itemName);
    } catch (BusinessException e) {
      return new AppraisalResult(false, 0, 0, 0, itemName, e.getMessage());
    } catch (Exception e) {
      log.error("估价失败: itemName={}, itemId={}", itemName, itemId, e);
      return new AppraisalResult(false, 0, 0, 0, itemName, "估价失败：" + e.getMessage());
    }
  }

  @Tool(
      description =
          """
      玩家要求砍价时调用。每次独立计算概率。
      通过对话历史自行判断是否已砍过——同一笔交易最多砍价一次。
      砍价成功则降价约5%。
      """)
  public HaggleResult haggle(
      @ToolParam(description = "物品名称") String itemName,
      @ToolParam(description = "当前报价（灵石），从 appraiseItem 或上一次 haggle 的结果中获取") long currentPrice) {
    try {
      Long userId = getCurrentUserId();
      User user = userStateService.loadUser(userId);
      ShopNpc npc = shopService.findByLocation(user.getLocationId());
      return shopService.haggleItem(userId, npc, currentPrice);
    } catch (BusinessException e) {
      return new HaggleResult(false, currentPrice, 0, e.getMessage());
    } catch (Exception e) {
      log.error("砍价失败: itemName={}, currentPrice={}", itemName, currentPrice, e);
      return new HaggleResult(false, currentPrice, 0, "砍价失败：" + e.getMessage());
    }
  }

  @Tool(
      description =
          """
      执行收购，扣除玩家物品并增加灵石。
      成交价格应在 minPrice 到 maxPrice 之间，
      且在 appraiseItem 或 haggle 得到的价格范围内。
      """)
  public SellResult sellItem(
      @ToolParam(description = "物品名称") String itemName,
      @ToolParam(description = "物品ID（从 appraiseItem 返回的匹配列表或对话中获取）") String itemId,
      @ToolParam(description = "确认的成交价格（灵石）") long confirmedPrice) {
    try {
      Long userId = getCurrentUserId();
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

  @Tool(description = "列出当前商铺所有在售商品及其价格和库存")
  public ProductListVO listProducts() {
    try {
      Long userId = getCurrentUserId();
      return shopService.listProducts(userId);
    } catch (BusinessException e) {
      return new ProductListVO("未知", List.of());
    } catch (Exception e) {
      log.error("列出商品失败", e);
      return new ProductListVO("未知", List.of());
    }
  }

  @Tool(description = "购买堆叠物品（丹药、材料等），扣灵石并添加物品到背包")
  public PurchaseResult purchaseItem(
      @ToolParam(description = "商品名称，如'聚气丹'、'玄铁矿石'") String templateName,
      @ToolParam(description = "购买数量") int quantity) {
    try {
      Long userId = getCurrentUserId();
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

  @Tool(description = "购买装备，随机生成品质和词缀。扣灵石并分配装备给玩家")
  public EquipmentPurchaseResult purchaseEquipment(
      @ToolParam(description = "装备名称，如'精铁剑'、'生铁甲'") String templateName) {
    try {
      Long userId = getCurrentUserId();
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

  @Tool(description = "查看玩家背包中未装备的装备列表，用于确认玩家想卖的是哪一件")
  public EquipmentListVO queryPlayerEquipment() {
    try {
      Long userId = getCurrentUserId();
      return shopService.queryPlayerEquipment(userId);
    } catch (Exception e) {
      log.error("查询装备列表失败", e);
      return new EquipmentListVO(List.of());
    }
  }

  private AppraisalResult tryAppraiseEquipment(Long userId, ShopNpc npc, Long equipmentId) {
    try {
      return shopService.appraiseEquipment(userId, npc, equipmentId);
    } catch (Exception e) {
      return null;
    }
  }

  private AppraisalResult tryAppraiseStackable(Long userId, ShopNpc npc, Long itemId) {
    try {
      return shopService.appraiseStackableItem(userId, npc, itemId);
    } catch (Exception e) {
      return null;
    }
  }

  private Long getCurrentUserId() {
    Long userId = UserContext.getCurrentUserId();
    if (userId == null) {
      throw new BusinessException(ErrorCode.USER_CONTEXT_MISSING);
    }
    return userId;
  }
}
