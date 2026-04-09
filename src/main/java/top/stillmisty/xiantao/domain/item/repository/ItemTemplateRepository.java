package top.stillmisty.xiantao.domain.item.repository;

import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 物品模板仓储接口
 */
public interface ItemTemplateRepository {

    /**
     * 根据模板ID查找物品模板
     */
    Optional<ItemTemplate> findById(UUID templateId);

    /**
     * 根据模板ID列表批量查找
     */
    List<ItemTemplate> findByIds(List<UUID> templateIds);

    /**
     * 根据物品类型查找所有模板
     */
    List<ItemTemplate> findByType(ItemType type);

    /**
     * 根据物品类型列表查找
     */
    List<ItemTemplate> findByTypes(List<ItemType> types);

    /**
     * 查找所有装备模板
     */
    List<ItemTemplate> findEquipmentTemplates();

    /**
     * 查找所有福地专供物品模板（种子/灵蛋）
     */
    List<ItemTemplate> findFudiItemTemplates();

    /**
     * 根据标签查找物品模板
     */
    List<ItemTemplate> findByTag(String tag);

    /**
     * 根据多个标签查找（包含任一标签）
     */
    List<ItemTemplate> findByTags(List<String> tags);

    /**
     * 根据多个标签查找（包含所有标签）
     */
    List<ItemTemplate> findByAllTags(List<String> tags);

    /**
     * 根据装备等级范围查找装备模板
     */
    List<ItemTemplate> findEquipmentByLevelRange(int minLevel, int maxLevel);

    /**
     * 保存物品模板
     */
    ItemTemplate save(ItemTemplate template);

    /**
     * 批量保存物品模板
     */
    List<ItemTemplate> saveAll(List<ItemTemplate> templates);

    /**
     * 删除物品模板
     */
    void deleteById(UUID templateId);

    /**
     * 检查模板ID是否存在
     */
    boolean existsById(UUID templateId);
}
