package top.stillmisty.xiantao.service.inventory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.vo.InventorySummaryVO;
import top.stillmisty.xiantao.domain.item.vo.ItemEntry;
import top.stillmisty.xiantao.domain.item.vo.StackableItemDetailVO;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.player.UserStateService;

/** 背包服务 负责：背包查看（摘要视图、分类列表、种子/装备/兽卵编号列表） */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

  private final UserStateService userStateService;
  private final StackableItemRepository stackableItemRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final ItemResolver itemResolver;

  // ===================== 公开 API（含认证） =====================

  @Authenticated
  public ServiceResult<InventorySummaryVO> getInventorySummary(
      PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getInventorySummary(userId));
  }

  @Authenticated
  public ServiceResult<List<ItemEntry>> getSeedInventory(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getSeedInventory(userId));
  }

  @Authenticated
  public ServiceResult<List<ItemEntry>> getEquipmentInventory(
      PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getEquipmentInventory(userId));
  }

  @Authenticated
  public ServiceResult<List<ItemEntry>> getEggInventory(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getEggInventory(userId));
  }

  @Authenticated
  public ServiceResult<List<ItemEntry>> getItemsByType(
      PlatformType platform, String openId, ItemType type) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getItemsByType(userId, type));
  }

  @Authenticated
  public ServiceResult<StackableItemDetailVO> getItemDetail(
      PlatformType platform, String openId, String input) {
    Long userId = UserContext.getCurrentUserId();
    var result = itemResolver.resolveStackableItem(userId, input);
    if (result instanceof ItemResolver.Found<StackableItem> found) {
      StackableItem item = found.item();
      String description =
          itemTemplateRepository
              .findById(item.getTemplateId())
              .map(ItemTemplate::getDescription)
              .orElse(null);
      return new ServiceResult.Success<>(
          new StackableItemDetailVO(
              item.getId(),
              item.getTemplateId(),
              item.getName(),
              item.getItemType().getName(),
              item.getQuantity() != null ? item.getQuantity() : 0,
              item.getQuality(),
              description,
              item.getProperties(),
              item.getTags()));
    }
    if (result instanceof ItemResolver.Ambiguous<?> ambiguous) {
      var sb = new StringBuilder("找到多个物品，请使用更精确的名称：\n");
      for (var e : ambiguous.candidates()) {
        sb.append(e.name());
        if (e.quantity() > 1) sb.append(" x").append(e.quantity());
        if (!e.metadata().isBlank()) sb.append(" [").append(e.metadata()).append("]");
        sb.append("\n");
      }
      return new ServiceResult.Failure<>(ErrorCode.ITEM_MULTIPLE_MATCH, sb.toString().strip());
    }
    return new ServiceResult.Failure<>(
        ErrorCode.ITEM_NOT_FOUND, ErrorCode.ITEM_NOT_FOUND.format(input));
  }

  // ===================== 内部 API（需预先完成认证） =====================

  /** 获取背包详情（装备列表 + 物品按类型分组 + 灵石） */
  @Cacheable(cacheNames = "player_inventory", key = "'summary:' + #userId")
  public InventorySummaryVO getInventorySummary(Long userId) {
    User user = userStateService.loadUser(userId);

    List<ItemEntry> equipment = itemResolver.listEquipment(userId);

    List<StackableItem> stackableItems = stackableItemRepository.findByUserId(userId);
    Map<ItemType, List<ItemEntry>> itemsByType = new LinkedHashMap<>();
    for (StackableItem item : stackableItems) {
      int qty = item.getQuantity() != null ? item.getQuantity() : 0;
      itemsByType
          .computeIfAbsent(item.getItemType(), k -> new ArrayList<>())
          .add(new ItemEntry(0, item.getId(), item.getName(), qty, ""));
    }

    return new InventorySummaryVO(equipment, itemsByType, user.getSpiritStones());
  }

  /** 获取种子列表（编号列表） */
  @Cacheable(cacheNames = "player_inventory", key = "'seeds:' + #userId")
  public List<ItemEntry> getSeedInventory(Long userId) {
    return itemResolver.listSeeds(userId);
  }

  /** 获取装备列表（编号列表） */
  @Cacheable(cacheNames = "player_inventory", key = "'equipment:' + #userId")
  public List<ItemEntry> getEquipmentInventory(Long userId) {
    return itemResolver.listEquipment(userId);
  }

  /** 获取兽卵列表（编号列表） */
  @Cacheable(cacheNames = "player_inventory", key = "'eggs:' + #userId")
  public List<ItemEntry> getEggInventory(Long userId) {
    return itemResolver.listEggs(userId);
  }

  /** 获取指定类型物品列表 */
  @Cacheable(cacheNames = "player_inventory", key = "'type:' + #type.code + ':' + #userId")
  public List<ItemEntry> getItemsByType(Long userId, ItemType type) {
    return switch (type) {
      case SEED -> itemResolver.listSeeds(userId);
      case BEAST_EGG -> itemResolver.listEggs(userId);
      default -> itemResolver.listItems(userId, type);
    };
  }
}
