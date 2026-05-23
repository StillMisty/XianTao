package top.stillmisty.xiantao.service.inventory;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;

/** 轻量物品添加器 — 仅供 SubEventEffect 使用，不依赖 UserStateService 避免循环依赖 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleItemAdder {

  private final StackableItemRepository stackableItemRepository;

  @Transactional
  public void addItem(Long userId, ItemTemplate template, String name, int quantity) {
    long templateId = template.getId();

    Map<String, Object> effectiveProperties = template.getProperties();

    int hash = StackableItem.computeHash(effectiveProperties);
    StackableItem newItem =
        StackableItem.create(userId, templateId, template.getType(), name, quantity);
    newItem.setTags(template.getTags());
    newItem.setProperties(effectiveProperties);
    newItem.setPropertiesHash(hash);

    stackableItemRepository.upsertIncrementQuantity(newItem);
    log.debug(
        "SimpleItemAdder: userId={}, templateId={}, quantity={}", userId, templateId, quantity);
  }
}
