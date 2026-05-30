package top.stillmisty.xiantao.service.inventory;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;

/**
 * 堆叠物品服务 — 增删查 + 按标签/类型搜索
 *
 * <p>设计决策：addStackableItem 不调用 UserStateService.loadUser()，调用方应自行确保用户已加载。 这避免了与
 * SubEventEffectExecutor 的循环依赖，消除了对 SimpleItemAdder 的需要。
 */
@Slf4j
@Service
public class StackableItemService {

  private final StackableItemRepository stackableItemRepository;
  private final ItemTemplateRepository itemTemplateRepository;

  public StackableItemService(
      StackableItemRepository stackableItemRepository,
      ItemTemplateRepository itemTemplateRepository) {
    this.stackableItemRepository = stackableItemRepository;
    this.itemTemplateRepository = itemTemplateRepository;
  }

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
      @Nullable Map<String, Object> properties) {

    var template = itemTemplateRepository.findById(templateId).orElse(null);
    var tags = template != null ? template.getTags() : null;
    Map<String, Object> effectiveProperties =
        (properties == null || properties.isEmpty())
            ? (template != null ? template.getProperties() : null)
            : properties;

    int hash = StackableItem.computeHash(effectiveProperties);
    StackableItem newItem = StackableItem.create(userId, templateId, itemType, name, quantity);
    newItem.setTags(tags);
    newItem.setProperties(effectiveProperties);
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
    if (quantity <= 0) {
      log.warn(
          "reduceStackableItem 请求数量无效: userId={}, itemId={}, quantity={}",
          userId,
          itemId,
          quantity);
      return;
    }
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
    var item = stackableItemRepository.findById(itemId);
    if (item.isPresent() && item.get().getQuantity() != null && item.get().getQuantity() <= 0) {
      stackableItemRepository.deleteIfZeroQuantity(itemId);
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
    return stackableItemRepository.findByUserIdAndAllTags(userId, tags);
  }

  /** 按标签搜索堆叠物品（OR关系，包含任一标签即可） */
  public List<StackableItem> searchStackableItemsByAnyTag(Long userId, List<String> tags) {
    if (tags == null || tags.isEmpty()) {
      return List.of();
    }
    return stackableItemRepository.findByUserIdAndAnyTag(userId, tags);
  }

  /** 按物品类型搜索堆叠物品 */
  public List<StackableItem> searchStackableItemsByType(Long userId, ItemType type) {
    return stackableItemRepository.findByUserIdAndType(userId, type);
  }
}
