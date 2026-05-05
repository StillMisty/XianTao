package top.stillmisty.xiantao.domain.item.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;

/** 物品模板仓储接口 */
public interface ItemTemplateRepository {

  /** 根据模板ID查找物品模板 */
  Optional<ItemTemplate> findById(Long templateId);

  /** 根据名称查找物品模板 */
  Optional<ItemTemplate> findByName(String name);

  /** 根据模板ID列表批量查找物品模板 */
  List<ItemTemplate> findByIds(List<Long> templateIds);

  /** 根据物品类型查找所有模板 */
  List<ItemTemplate> findByType(ItemType type);

  /** 根据物品类型列表查找 */
  List<ItemTemplate> findByTypes(List<ItemType> types);

  /** 保存物品模板 */
  ItemTemplate save(ItemTemplate template);

  /** 删除物品模板 */
  void deleteById(Long templateId);

  /** 检查模板ID是否存在 */
  boolean existsById(Long templateId);
}
