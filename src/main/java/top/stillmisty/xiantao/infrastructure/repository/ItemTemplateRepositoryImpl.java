package top.stillmisty.xiantao.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.mapper.ItemTemplateMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 物品模板仓储实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemTemplateRepositoryImpl implements ItemTemplateRepository {

    private final ItemTemplateMapper mapper;

    @Override
    public Optional<ItemTemplate> findById(UUID templateId) {
        return Optional.ofNullable(mapper.selectOneById(templateId));
    }

    @Override
    public List<ItemTemplate> findByIds(List<UUID> templateIds) {
        return mapper.selectListByIds(templateIds);
    }

    @Override
    public List<ItemTemplate> findByType(ItemType type) {
        // 先查询所有，然后在内存中过滤
        // 后续可以使用@Select注解写自定义SQL优化
        return mapper.selectAll().stream()
                .filter(t -> t.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemTemplate> findByTypes(List<ItemType> types) {
        if (types == null || types.isEmpty()) {
            return List.of();
        }
        return mapper.selectAll().stream()
                .filter(t -> types.contains(t.getType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemTemplate> findEquipmentTemplates() {
        return findByType(ItemType.EQUIPMENT);
    }

    @Override
    public List<ItemTemplate> findFudiItemTemplates() {
        return findByTypes(List.of(ItemType.SEED, ItemType.SPIRIT_EGG));
    }

    @Override
    public List<ItemTemplate> findByTag(String tag) {
        // TODO: 实现PostgreSQL JSONB标签搜索
        // 目前先返回空列表，后续使用自定义SQL实现
        return List.of();
    }

    @Override
    public List<ItemTemplate> findByTags(List<String> tags) {
        // TODO: 实现PostgreSQL JSONB标签搜索
        // 目前先返回空列表，后续使用自定义SQL实现
        return List.of();
    }

    @Override
    public List<ItemTemplate> findByAllTags(List<String> tags) {
        // TODO: 实现PostgreSQL JSONB标签搜索
        // 目前先返回空列表，后续使用自定义SQL实现
        return List.of();
    }

    @Override
    public List<ItemTemplate> findEquipmentByLevelRange(int minLevel, int maxLevel) {
        // 先查询所有装备，然后在内存中过滤
        return mapper.selectAll().stream()
                .filter(t -> t.getType() == ItemType.EQUIPMENT)
                .filter(t -> {
                    Integer level = t.getEquipLevel();
                    return level != null && level >= minLevel && level <= maxLevel;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemTemplate save(ItemTemplate template) {
        if (template.getId() == null || !existsById(template.getId())) {
            mapper.insert(template);
        } else {
            mapper.update(template);
        }
        return template;
    }

    @Override
    public List<ItemTemplate> saveAll(List<ItemTemplate> templates) {
        templates.forEach(this::save);
        return templates;
    }

    @Override
    public void deleteById(UUID templateId) {
        mapper.deleteById(templateId);
    }

    @Override
    public boolean existsById(UUID templateId) {
        return mapper.selectOneById(templateId) != null;
    }
}
