package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.infrastructure.mapper.ItemTemplateMapper;

/** 物品模板仓储实现 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemTemplateRepository {

  private final ItemTemplateMapper mapper;

  public Optional<ItemTemplate> findById(Long templateId) {
    return Optional.ofNullable(mapper.selectOneById(templateId));
  }

  @Cacheable(value = "itemTemplate", key = "'name:' + #name")
  public Optional<ItemTemplate> findByName(String name) {
    return Optional.ofNullable(mapper.selectByName(name));
  }

  public List<ItemTemplate> findByIds(List<Long> templateIds) {
    if (templateIds == null || templateIds.isEmpty()) {
      return List.of();
    }
    return mapper.selectListByIds(templateIds);
  }

  public List<ItemTemplate> findByType(ItemType type) {
    return mapper.selectByType(type);
  }

  public List<ItemTemplate> findByTypes(List<ItemType> types) {
    if (types == null || types.isEmpty()) {
      return List.of();
    }
    return mapper.selectByTypes(types);
  }

  public ItemTemplate save(ItemTemplate template) {
    mapper.insertOrUpdateSelective(template);
    return template;
  }

  public void deleteById(Long templateId) {
    mapper.deleteById(templateId);
  }

  public boolean existsById(Long templateId) {
    return mapper.selectCountByQuery(
            com.mybatisflex.core.query.QueryWrapper.create()
                .select()
                .from(
                    top.stillmisty.xiantao.domain.item.entity.table.ItemTemplateTableDef
                        .ITEM_TEMPLATE)
                .where(
                    top.stillmisty.xiantao.domain.item.entity.table.ItemTemplateTableDef
                        .ITEM_TEMPLATE
                        .ID.eq(templateId)))
        > 0;
  }
}
