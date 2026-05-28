package top.stillmisty.xiantao.service.inventory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
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
  private final Map<ItemType, ItemUseHandler> handlerMap;

  public ItemUseService(
      StackableItemRepository stackableItemRepository,
      ItemTemplateRepository itemTemplateRepository,
      StackableItemService stackableItemService,
      List<ItemUseHandler> handlers) {
    this.stackableItemRepository = stackableItemRepository;
    this.itemTemplateRepository = itemTemplateRepository;
    this.stackableItemService = stackableItemService;
    this.handlerMap =
        handlers.stream()
            .collect(Collectors.toMap(ItemUseHandler::getItemType, Function.identity()));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> useItem(
      PlatformType platform, String openId, String itemName, String args) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(useItem(userId, itemName, args));
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(cacheNames = "player_inventory", key = "'summary:' + #userId"),
        @CacheEvict(cacheNames = "player_inventory", key = "'seeds:' + #userId"),
        @CacheEvict(cacheNames = "player_inventory", key = "'eggs:' + #userId"),
        @CacheEvict(cacheNames = "player_inventory", key = "'equipment:' + #userId")
      })
  public String useItem(Long userId, String itemName, String args) {
    List<StackableItem> exactMatches =
        stackableItemRepository.findByUserIdAndName(userId, itemName);
    StackableItem matchedItem = null;
    for (StackableItem item : exactMatches) {
      ItemTemplate template = itemTemplateRepository.findById(item.getTemplateId()).orElse(null);
      if (template != null) {
        matchedItem = item;
        break;
      }
    }

    if (matchedItem == null) {
      List<StackableItem> items =
          stackableItemRepository.findByUserIdAndNameContaining(userId, itemName);
      for (StackableItem item : items) {
        ItemTemplate template = itemTemplateRepository.findById(item.getTemplateId()).orElse(null);
        if (template != null) {
          matchedItem = item;
          break;
        }
      }
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

    return handler.use(userId, matchedItem, null, args);
  }
}
