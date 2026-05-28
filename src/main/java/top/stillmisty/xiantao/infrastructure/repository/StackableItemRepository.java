package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.infrastructure.mapper.StackableItemMapper;

@Repository
@RequiredArgsConstructor
public class StackableItemRepository {

  private final StackableItemMapper stackableItemMapper;

  public StackableItem save(StackableItem item) {
    stackableItemMapper.insertOrUpdateSelective(item);
    return item;
  }

  public Optional<StackableItem> findById(Long id) {
    return Optional.ofNullable(stackableItemMapper.selectOneById(id));
  }

  public List<StackableItem> findByUserId(Long userId) {
    QueryWrapper query = new QueryWrapper().eq(StackableItem::getUserId, userId);
    return stackableItemMapper.selectListByQuery(query);
  }

  public Optional<StackableItem> findByUserIdAndTemplateId(Long userId, Long templateId) {
    QueryWrapper query =
        new QueryWrapper()
            .eq(StackableItem::getUserId, userId)
            .eq(StackableItem::getTemplateId, templateId);
    return Optional.ofNullable(stackableItemMapper.selectOneByQuery(query));
  }

  public Optional<StackableItem> findByUserIdAndTemplateIdAndPropertiesHash(
      Long userId, Long templateId, int propertiesHash) {
    QueryWrapper query =
        new QueryWrapper()
            .eq(StackableItem::getUserId, userId)
            .eq(StackableItem::getTemplateId, templateId)
            .eq(StackableItem::getPropertiesHash, propertiesHash);
    return Optional.ofNullable(stackableItemMapper.selectOneByQuery(query));
  }

  public List<StackableItem> findByUserIdAndType(Long userId, ItemType type) {
    QueryWrapper query =
        new QueryWrapper()
            .eq(StackableItem::getUserId, userId)
            .eq(StackableItem::getItemType, type);
    return stackableItemMapper.selectListByQuery(query);
  }

  public List<StackableItem> findByUserIdAndName(Long userId, String name) {
    return stackableItemMapper.selectByUserIdAndName(userId, name);
  }

  public List<StackableItem> findByUserIdAndNameContaining(Long userId, String name) {
    QueryWrapper query =
        new QueryWrapper().eq(StackableItem::getUserId, userId).like(StackableItem::getName, name);
    return stackableItemMapper.selectListByQuery(query);
  }

  public void deleteById(Long id) {
    stackableItemMapper.deleteById(id);
  }

  public int reduceQuantityById(Long id, Long userId, int qty) {
    return stackableItemMapper.reduceQuantityById(id, userId, qty);
  }

  public int upsertIncrementQuantity(StackableItem item) {
    return stackableItemMapper.upsertIncrementQuantity(item);
  }

  public int deleteIfZeroQuantity(Long id) {
    return stackableItemMapper.deleteIfZeroQuantity(id);
  }

  public List<StackableItem> findByUserIdAndAllTags(Long userId, List<String> tags) {
    String tagsJson =
        "["
            + tags.stream()
                .map(t -> "\"" + t + "\"")
                .collect(java.util.stream.Collectors.joining(","))
            + "]";
    return stackableItemMapper.selectByUserIdAndAllTags(userId, tagsJson);
  }

  public List<StackableItem> findByUserIdAndAnyTag(Long userId, List<String> tags) {
    return stackableItemMapper.selectByUserIdAndAnyTag(userId, tags.toArray(new String[0]));
  }
}
