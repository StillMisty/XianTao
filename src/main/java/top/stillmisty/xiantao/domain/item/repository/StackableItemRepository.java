package top.stillmisty.xiantao.domain.item.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;

/** 堆叠物品仓储接口 */
public interface StackableItemRepository {

  /** 保存物品 */
  StackableItem save(StackableItem item);

  /** 根据ID查找物品 */
  Optional<StackableItem> findById(Long id);

  /** 根据用户ID查找所有物品 */
  List<StackableItem> findByUserId(Long userId);

  /** 根据用户ID和物品类型查找物品 */
  List<StackableItem> findByUserIdAndType(Long userId, ItemType type);

  /** 根据用户ID和模板ID查找物品 */
  Optional<StackableItem> findByUserIdAndTemplateId(Long userId, Long templateId);

  /** 根据用户ID和名称模糊匹配查找物品 */
  List<StackableItem> findByUserIdAndNameContaining(Long userId, String name);

  /** 删除物品 */
  void deleteById(Long id);
}
