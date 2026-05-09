package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.infrastructure.mapper.StackableItemMapper;

@Repository
@RequiredArgsConstructor
public class StackableItemRepositoryImpl implements StackableItemRepository {

  private final StackableItemMapper stackableItemMapper;

  @Override
  public StackableItem save(StackableItem item) {
    stackableItemMapper.insertOrUpdateSelective(item);
    return item;
  }

  @Override
  public Optional<StackableItem> findById(Long id) {
    return Optional.ofNullable(stackableItemMapper.selectOneById(id));
  }

  @Override
  public List<StackableItem> findByUserId(Long userId) {
    QueryWrapper query = new QueryWrapper().eq(StackableItem::getUserId, userId);
    return stackableItemMapper.selectListByQuery(query);
  }

  @Override
  public Optional<StackableItem> findByUserIdAndTemplateId(Long userId, Long templateId) {
    QueryWrapper query =
        new QueryWrapper()
            .eq(StackableItem::getUserId, userId)
            .eq(StackableItem::getTemplateId, templateId);
    return Optional.ofNullable(stackableItemMapper.selectOneByQuery(query));
  }

  @Override
  public Optional<StackableItem> findByUserIdAndTemplateIdAndPropertiesHash(
      Long userId, Long templateId, int propertiesHash) {
    QueryWrapper query =
        new QueryWrapper()
            .eq(StackableItem::getUserId, userId)
            .eq(StackableItem::getTemplateId, templateId)
            .eq(StackableItem::getPropertiesHash, propertiesHash);
    return Optional.ofNullable(stackableItemMapper.selectOneByQuery(query));
  }

  @Override
  public List<StackableItem> findByUserIdAndType(Long userId, ItemType type) {
    QueryWrapper query =
        new QueryWrapper()
            .eq(StackableItem::getUserId, userId)
            .eq(StackableItem::getItemType, type);
    return stackableItemMapper.selectListByQuery(query);
  }

  @Override
  public List<StackableItem> findByUserIdAndNameContaining(Long userId, String name) {
    QueryWrapper query =
        new QueryWrapper().eq(StackableItem::getUserId, userId).like(StackableItem::getName, name);
    return stackableItemMapper.selectListByQuery(query);
  }

  @Override
  public void deleteById(Long id) {
    stackableItemMapper.deleteById(id);
  }
}
