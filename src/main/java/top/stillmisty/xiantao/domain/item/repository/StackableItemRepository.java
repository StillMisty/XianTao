package top.stillmisty.xiantao.domain.item.repository;

import top.stillmisty.xiantao.domain.item.entity.StackableItem;

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
     * 根据ID查找物品
     */
    Optional<StackableItem> findById(Long id);

    /**
     * 根据用户ID查找所有物品
     */
    List<StackableItem> findByUserId(Long userId);

    /**
     * 根据用户ID和模板ID查找物品
     */
    Optional<StackableItem> findByUserIdAndTemplateId(Long userId, Long templateId);

    /**
     * 删除物品
     */
    void deleteById(Long id);
}
