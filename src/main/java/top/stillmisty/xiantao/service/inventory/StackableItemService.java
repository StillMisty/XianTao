package top.stillmisty.xiantao.service.inventory;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.player.UserStateService;

/** 堆叠物品服务 负责：堆叠物品的增删查（添加/减少/检查数量）及按标签/类型搜索 仅供其他 Service 内部调用 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StackableItemService {

  private final UserStateService userStateService;
  private final StackableItemRepository stackableItemRepository;

  /** 添加堆叠物品到背包 */
  @Transactional
  public void addStackableItem(
      Long userId, Long templateId, ItemType itemType, String name, int quantity) {
    addStackableItem(userId, templateId, itemType, name, quantity, null);
  }

  /** 添加堆叠物品到背包（含属性） */
  @Transactional
  public void addStackableItem(
      Long userId,
      Long templateId,
      ItemType itemType,
      String name,
      int quantity,
      Map<String, Object> properties) {
    userStateService.loadUser(userId);

    int hash = StackableItem.computeHash(properties);
    StackableItem newItem = StackableItem.create(userId, templateId, itemType, name, quantity);
    newItem.setProperties(properties);
    newItem.setPropertiesHash(hash);

    int affected = stackableItemRepository.upsertIncrementQuantity(newItem);
    log.debug(
        "添加堆叠物品: userId={}, templateId={}, quantity={}, affected={}",
        userId,
        templateId,
        quantity,
        affected);
  }

  /** 按物品ID原子减少堆叠物品数量 */
  @Transactional
  public void reduceStackableItem(Long userId, Long itemId, int quantity) {
    if (quantity <= 0) return;
    int affected = stackableItemRepository.reduceQuantityById(itemId, userId, quantity);
    if (affected == 0) {
      var existingItem = stackableItemRepository.findById(itemId);
      if (existingItem.isEmpty()) {
        throw new BusinessException(ErrorCode.ITEM_NOT_EXISTS);
      }
      StackableItem item = existingItem.get();
      if (!item.getUserId().equals(userId)) {
        throw new BusinessException(ErrorCode.ITEM_OWNERSHIP_MISMATCH);
      }
      throw new BusinessException(
          ErrorCode.ITEM_QUANTITY_INSUFFICIENT, quantity, item.getQuantity());
    }
    log.debug("原子减少堆叠物品数量: userId={}, itemId={}, quantity={}", userId, itemId, quantity);
  }

  /** 检查堆叠物品数量是否足够 */
  public boolean hasEnoughStackableItem(Long userId, Long templateId, int quantity) {
    return stackableItemRepository
        .findByUserIdAndTemplateId(userId, templateId)
        .map(item -> item.hasEnoughQuantity(quantity))
        .orElse(false);
  }

  // ===================== 标签搜索方法（地灵AI联动） =====================

  /** 按标签搜索堆叠物品（AND关系，需包含所有标签） */
  public List<StackableItem> searchStackableItemsByTags(Long userId, List<String> tags) {
    if (tags == null || tags.isEmpty()) {
      return List.of();
    }

    List<StackableItem> allItems = stackableItemRepository.findByUserId(userId);

    return allItems.stream().filter(item -> item.hasAllTags(tags)).toList();
  }

  /** 按标签搜索堆叠物品（OR关系，包含任一标签即可） */
  public List<StackableItem> searchStackableItemsByAnyTag(Long userId, List<String> tags) {
    if (tags == null || tags.isEmpty()) {
      return List.of();
    }

    List<StackableItem> allItems = stackableItemRepository.findByUserId(userId);

    return allItems.stream().filter(item -> tags.stream().anyMatch(item::hasTag)).toList();
  }

  /** 按物品类型搜索堆叠物品 */
  public List<StackableItem> searchStackableItemsByType(Long userId, ItemType type) {
    return stackableItemRepository.findByUserIdAndType(userId, type);
  }
}
