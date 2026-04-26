package top.stillmisty.xiantao.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.mapper.ItemTemplateMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    public Optional<ItemTemplate> findById(Long templateId) {
        return Optional.ofNullable(mapper.selectOneById(templateId));
    }


    @Override
    public List<ItemTemplate> findByType(ItemType type) {
        // 先查询所有，然后在内存中过滤
        // 后续可以使用@Select注解写自定义SQL优化
        return Objects.requireNonNull(mapper.selectAll()).stream()
                .filter(t -> t.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemTemplate> findByTypes(List<ItemType> types) {
        if (types == null || types.isEmpty()) {
            return List.of();
        }
        return Objects.requireNonNull(mapper.selectAll()).stream()
                .filter(t -> types.contains(t.getType()))
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
    public void deleteById(Long templateId) {
        mapper.deleteById(templateId);
    }

    @Override
    public boolean existsById(Long templateId) {
        return mapper.selectOneById(templateId) != null;
    }
}
