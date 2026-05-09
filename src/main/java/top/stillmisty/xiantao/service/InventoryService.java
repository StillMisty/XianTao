package top.stillmisty.xiantao.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.item.vo.InventorySummaryVO;
import top.stillmisty.xiantao.domain.item.vo.ItemEntry;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.annotation.Authenticated;

/** 背包服务 负责：背包查看（摘要视图、分类列表、种子/装备/兽卵编号列表） */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

  private final UserStateService userStateService;
  private final EquipmentRepository equipmentRepository;
  private final StackableItemRepository stackableItemRepository;
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

  // ===================== 内部 API（需预先完成认证） =====================

  /** 获取背包摘要（按品质折叠装备，用于 #背包 命令防刷屏） */
  public InventorySummaryVO getInventorySummary(Long userId) {
    User user = userStateService.loadUser(userId);

    List<Equipment> allEquipments = equipmentRepository.findUnequippedByUserId(userId);
    List<StackableItem> stackableItems = stackableItemRepository.findByUserId(userId);

    Map<String, Integer> equipmentByQuality =
        allEquipments.stream()
            .collect(
                Collectors.groupingBy(
                    e -> e.getRarity().getName(),
                    Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

    Map<ItemType, Integer> stackableItemCount = new HashMap<>();
    for (StackableItem item : stackableItems) {
      stackableItemCount.merge(item.getItemType(), 1, Integer::sum);
    }

    return new InventorySummaryVO(equipmentByQuality, stackableItemCount, user.getSpiritStones());
  }

  /** 获取种子列表（编号列表） */
  public List<ItemEntry> getSeedInventory(Long userId) {
    return itemResolver.listSeeds(userId);
  }

  /** 获取装备列表（编号列表） */
  public List<ItemEntry> getEquipmentInventory(Long userId) {
    return itemResolver.listEquipment(userId);
  }

  /** 获取兽卵列表（编号列表） */
  public List<ItemEntry> getEggInventory(Long userId) {
    return itemResolver.listEggs(userId);
  }
}
