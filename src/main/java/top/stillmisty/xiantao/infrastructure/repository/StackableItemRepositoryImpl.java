package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.infrastructure.mapper.StackableItemMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StackableItemRepositoryImpl implements StackableItemRepository {

    private final StackableItemMapper stackableItemMapper;

    @Override
    public StackableItem save(StackableItem item) {
        stackableItemMapper.insertOrUpdate(item);
        return item;
    }

    @Override
    public List<StackableItem> saveAll(List<StackableItem> items) {
        items.forEach(stackableItemMapper::insertOrUpdate);
        return items;
    }

    @Override
    public Optional<StackableItem> findById(Long id) {
        return Optional.ofNullable(stackableItemMapper.selectOneById(id));
    }

    @Override
    public List<StackableItem> findByUserId(Long userId) {
        QueryWrapper query = new QueryWrapper()
                .eq(StackableItem::getUserId, userId);
        return stackableItemMapper.selectListByQuery(query);
    }

    @Override
    public List<StackableItem> findByUserIdAndItemType(Long userId, ItemType itemType) {
        QueryWrapper query = new QueryWrapper()
                .eq(StackableItem::getUserId, userId)
                .eq(StackableItem::getItemType, itemType);
        return stackableItemMapper.selectListByQuery(query);
    }

    @Override
    public Optional<StackableItem> findByUserIdAndTemplateId(Long userId, Long templateId) {
        QueryWrapper query = new QueryWrapper()
                .eq(StackableItem::getUserId, userId)
                .eq(StackableItem::getTemplateId, templateId);
        return Optional.ofNullable(stackableItemMapper.selectOneByQuery(query));
    }

    @Override
    public List<StackableItem> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        QueryWrapper query = new QueryWrapper()
                .in(StackableItem::getId, ids);
        return stackableItemMapper.selectListByQuery(query);
    }

    @Override
    public void deleteById(Long id) {
        stackableItemMapper.deleteById(id);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            ids.forEach(stackableItemMapper::deleteById);
        }
    }

    @Override
    public void deleteByUserId(Long userId) {
        QueryWrapper query = new QueryWrapper()
                .eq(StackableItem::getUserId, userId);
        stackableItemMapper.deleteByQuery(query);
    }
}
