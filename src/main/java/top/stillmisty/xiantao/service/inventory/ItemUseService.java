package top.stillmisty.xiantao.service.inventory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.inventory.handler.ItemUseHandler;

/**
 * 物品使用服务
 *
 * <p>设计决策：使用 Map<ItemType, ItemUseHandler> 而非 List + 遍历 supports()， 因为每个 handler 绑定唯一 ItemType，Map
 * 查找 O(1) 且消除 supports() 方法。
 */
@Slf4j
@Service
public class ItemUseService {

  private final StackableItemRepository stackableItemRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemService stackableItemService;
  private final CacheManager cacheManager;
  private final Map<ItemType, ItemUseHandler> handlerMap;

  public ItemUseService(
      StackableItemRepository stackableItemRepository,
      ItemTemplateRepository itemTemplateRepository,
      StackableItemService stackableItemService,
      CacheManager cacheManager,
      List<ItemUseHandler> handlers) {
    this.stackableItemRepository = stackableItemRepository;
    this.itemTemplateRepository = itemTemplateRepository;
    this.stackableItemService = stackableItemService;
    this.cacheManager = cacheManager;
    this.handlerMap =
        handlers.stream()
            .collect(Collectors.toMap(ItemUseHandler::getItemType, Function.identity()));
  }

  @Transactional
  public ServiceResult<String> useItem(Long userId, String itemName, String args) {
    return new ServiceResult.Success<>(useItemInternal(userId, itemName, args));
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(cacheNames = "player_inventory", key = "'summary:' + #userId"),
        @CacheEvict(cacheNames = "player_inventory", key = "'seeds:' + #userId"),
        @CacheEvict(cacheNames = "player_inventory", key = "'eggs:' + #userId"),
        @CacheEvict(cacheNames = "player_inventory", key = "'equipment:' + #userId")
      })
  public String useItemInternal(Long userId, String itemName, String args) {
    List<StackableItem> exactMatches =
        stackableItemRepository.findByUserIdAndName(userId, itemName);
    StackableItem matchedItem = findFirstWithValidTemplate(exactMatches);

    if (matchedItem == null) {
      List<StackableItem> items =
          stackableItemRepository.findByUserIdAndNameContaining(userId, itemName);
      matchedItem = findFirstWithValidTemplate(items);
    }

    if (matchedItem == null) {
      throw new BusinessException(ErrorCode.ITEM_NOT_FOUND, itemName);
    }

    ItemType type = matchedItem.getItemType();
    ItemUseHandler handler = handlerMap.get(type);
    if (handler == null) {
      throw new BusinessException(ErrorCode.ITEM_CANNOT_USE);
    }

    if (!handler.consumesInternally()) {
      stackableItemService.reduceStackableItem(userId, matchedItem.getId(), 1);
    }

    log.debug("使用物品: userId={}, item={}, type={}", userId, matchedItem.getName(), type);

    String result = handler.use(userId, matchedItem, null, args);

    // 手动清除该用户的type缓存
    clearTypeCacheForUser(userId);

    return result;
  }

  private void clearTypeCacheForUser(Long userId) {
    var cache = cacheManager.getCache("player_inventory");
    if (cache != null) {
      // 清除该用户的所有type缓存
      for (ItemType type : ItemType.values()) {
        String key = "type:" + type.getCode() + ":" + userId;
        cache.evict(key);
      }
    }
  }

  @Nullable
  private StackableItem findFirstWithValidTemplate(List<StackableItem> items) {
    if (items.isEmpty()) {
      return null;
    }
    List<Long> templateIds = items.stream().map(StackableItem::getTemplateId).distinct().toList();
    java.util.Set<Long> existingIds =
        itemTemplateRepository.findByIds(templateIds).stream()
            .map(ItemTemplate::getId)
            .collect(Collectors.toSet());
    return items.stream()
        .filter(item -> existingIds.contains(item.getTemplateId()))
        .findFirst()
        .orElse(null);
  }
}
