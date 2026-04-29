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

/**
 * 物品模板仓储实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemTemplateRepositoryImpl implements ItemTemplateRepository {

    private final ItemTemplateMapper mapper;

    @Override
    public Optional<ItemTemplate> findById(Long templateId) {
        return Optional.ofNullable(mapper.selectOneById(templateId));
    }

    @Override
    public Optional<ItemTemplate> findByName(String name) {
        return Optional.ofNullable(mapper.selectByName(name));
    }

    @Override
    public List<ItemTemplate> findByIds(List<Long> templateIds) {
        if (templateIds == null || templateIds.isEmpty()) {
            return List.of();
        }
        return mapper.selectListByIds(templateIds);
    }


    @Override
    public List<ItemTemplate> findByType(ItemType type) {
        return mapper.selectByType(type);
    }

    @Override
    public List<ItemTemplate> findByTypes(List<ItemType> types) {
        if (types == null || types.isEmpty()) {
            return List.of();
        }
        return mapper.selectByTypes(types);
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
    public void deleteById(Long templateId) {
        mapper.deleteById(templateId);
    }

    @Override
    public boolean existsById(Long templateId) {
        return mapper.selectOneById(templateId) != null;
    }
}
