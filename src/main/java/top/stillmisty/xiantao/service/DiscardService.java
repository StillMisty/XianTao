package top.stillmisty.xiantao.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.annotation.Authenticated;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscardService {

  private final UserStateService userStateService;
  private final ItemResolver itemResolver;
  private final EquipmentRepository equipmentRepository;
  private final StackableItemRepository stackableItemRepository;
  private final StackableItemService stackableItemService;

  @Authenticated
  @Transactional
  public ServiceResult<String> discardItem(PlatformType platform, String openId, String itemName) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(discardItem(userId, itemName));
  }

  @Transactional
  public String discardItem(Long userId, String input) {
    userStateService.getUser(userId);

    var equipResult = itemResolver.resolveEquipment(userId, input);
    if (equipResult instanceof ItemResolver.Found<Equipment> found) {
      Equipment equipment = found.item();
      if (equipment.getEquipped()) {
        throw new IllegalStateException("【" + equipment.getName() + "】已装备，请先卸下再丢弃");
      }
      equipmentRepository.deleteById(equipment.getId());
      log.info(
          "丢弃装备: userId={}, equipmentId={}, name={}",
          userId,
          equipment.getId(),
          equipment.getName());
      return "已丢弃装备【" + equipment.getName() + "】";
    }

    List<StackableItem> items =
        stackableItemRepository.findByUserIdAndNameContaining(userId, input);
    if (items.isEmpty()) {
      throw new IllegalStateException("背包中未找到【" + input + "】");
    }
    StackableItem item = items.getFirst();
    stackableItemService.reduceStackableItem(userId, item.getTemplateId(), 1);
    log.info(
        "丢弃物品: userId={}, item={}, templateId={}", userId, item.getName(), item.getTemplateId());
    return "已丢弃【" + item.getName() + "】";
  }
}
