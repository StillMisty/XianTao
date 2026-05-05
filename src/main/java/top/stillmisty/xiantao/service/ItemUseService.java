package top.stillmisty.xiantao.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.handler.ItemUseHandler;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.annotation.Authenticated;

/** 物品使用服务 通过策略模式分发到不同的 ItemUseHandler */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemUseService {

  private final List<ItemUseHandler> handlers;
  private final StackableItemRepository stackableItemRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemService stackableItemService;

  /** 使用物品（公开API，含认证） */
  @Authenticated
  public ServiceResult<String> useItem(
      PlatformType platform, String openId, String itemName, String args) {
    try {
      Long userId = UserContext.getCurrentUserId();
      return new ServiceResult.Success<>(useItem(userId, itemName, args));
    } catch (IllegalStateException | IllegalArgumentException e) {
      return ServiceResult.businessFailure(e.getMessage());
    }
  }

  /** 使用物品（内部API） */
  public String useItem(Long userId, String itemName, String args) {
    // 1. 查找物品 - 使用数据库模糊匹配
    List<StackableItem> items =
        stackableItemRepository.findByUserIdAndNameContaining(userId, itemName);
    StackableItem matchedItem = null;
    ItemTemplate matchedTemplate = null;

    for (StackableItem item : items) {
      ItemTemplate template = itemTemplateRepository.findById(item.getTemplateId()).orElse(null);
      if (template != null) {
        matchedItem = item;
        matchedTemplate = template;
        break;
      }
    }

    if (matchedItem == null) {
      throw new IllegalStateException("背包中未找到物品：" + itemName);
    }

    final StackableItem finalItem = matchedItem;
    final ItemTemplate finalTemplate = matchedTemplate;

    // 2. 查找匹配的处理器
    ItemUseHandler handler =
        handlers.stream()
            .filter(h -> h.supports(finalItem.getItemType(), finalTemplate))
            .findFirst()
            .orElse(null);

    if (handler == null) {
      throw new IllegalStateException("该物品无法使用");
    }

    // 3. 统一扣减物品（除非 handler 自行管理消耗）
    if (!handler.consumesInternally()) {
      stackableItemService.reduceStackableItem(userId, finalItem.getTemplateId(), 1);
    }

    // 4. 执行使用
    log.info(
        "使用物品: userId={}, item={}, handler={}",
        userId,
        finalItem.getName(),
        handler.getClass().getSimpleName());
    return handler.use(userId, finalItem, finalTemplate, args);
  }
}
