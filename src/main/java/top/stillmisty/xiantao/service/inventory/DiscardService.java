package top.stillmisty.xiantao.service.inventory;

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
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.player.UserStateService;

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
    userStateService.loadUser(userId);

    var equipResult = itemResolver.resolveEquipment(userId, input);
    if (equipResult instanceof ItemResolver.Found<Equipment> found) {
      Equipment equipment = found.item();
      if (equipment.getEquipped()) {
        throw new BusinessException(ErrorCode.ITEM_EQUIPPED, equipment.getName());
      }
      equipmentRepository.deleteById(equipment.getId());
      log.debug(
          "丢弃装备: userId={}, equipmentId={}, name={}",
          userId,
          equipment.getId(),
          equipment.getName());
      return "已丢弃装备【" + equipment.getName() + "】";
    }

    int quantity = 1;
    String itemName = input;
    int spaceIdx = input.indexOf(' ');
    if (spaceIdx > 0) {
      try {
        quantity = Integer.parseInt(input.substring(0, spaceIdx));
        if (quantity > 0) {
          itemName = input.substring(spaceIdx + 1).trim();
        }
      } catch (NumberFormatException ignored) {
        log.trace("丢弃: 无法从 \"{}\" 解析数量，按物品名处理", input);
      }
    }

    List<StackableItem> items =
        stackableItemRepository.findByUserIdAndNameContaining(userId, itemName);
    if (items.isEmpty()) {
      throw new BusinessException(ErrorCode.ITEM_NOT_FOUND, itemName);
    }
    StackableItem item = items.getFirst();
    int actualDiscard = Math.min(quantity, item.getQuantity());
    stackableItemService.reduceStackableItem(userId, item.getId(), actualDiscard);
    log.debug(
        "丢弃物品: userId={}, item={}, quantity={}, templateId={}",
        userId,
        item.getName(),
        actualDiscard,
        item.getTemplateId());
    return "已丢弃【" + item.getName() + "】 x" + actualDiscard;
  }
}
