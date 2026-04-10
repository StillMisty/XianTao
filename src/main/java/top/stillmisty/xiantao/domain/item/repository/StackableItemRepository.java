package top.stillmisty.xiantao.domain.item.repository;

import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;

import java.util.List;
import java.util.Optional;

/**
 * 堆叠物品仓储接口
 */
public interface StackableItemRepository {

    /**
     * 保存物品
     */
    StackableItem save(StackableItem item);

    /**
     * 批量保存物品
     */
    List<StackableItem> saveAll(List<StackableItem> items);

    /**
     * 根据ID查找物品
     */
    Optional<StackableItem> findById(Long id);

    /**
     * 根据用户ID查找所有物品
     */
    List<StackableItem> findByUserId(Long userId);

    /**
     * 根据用户ID和物品类型查找物品
     */
    List<StackableItem> findByUserIdAndItemType(Long userId, ItemType itemType);

    /**
     * 根据用户ID和模板ID查找物品
     */
    Optional<StackableItem> findByUserIdAndTemplateId(Long userId, Long templateId);

    /**
     * 根据ID列表批量查找物品
     */
    List<StackableItem> findByIds(List<Long> ids);

    /**
     * 删除物品
     */
    void deleteById(Long id);

    /**
     * 批量删除物品
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据用户ID删除所有物品
     */
    void deleteByUserId(Long userId);
}
