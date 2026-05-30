package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

@Component
public class AddItemEffect implements SubEventEffect {

  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemService stackableItemService;

  public AddItemEffect(
      ItemTemplateRepository itemTemplateRepository, StackableItemService stackableItemService) {
    this.itemTemplateRepository = itemTemplateRepository;
    this.stackableItemService = stackableItemService;
  }

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.ADD_ITEM;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, EffectParams params, EventContext context) {
    if (!(params instanceof EffectParams.AddItemParams p)) return Map.of();
    if (p.templateId() == null) return Map.of();
    int count = p.resolveCount();
    if (count <= 0) return Map.of();
    var template = itemTemplateRepository.findById(p.templateId()).orElse(null);
    if (template == null) return Map.of();
    stackableItemService.addStackableItem(
        userId, template.getId(), template.getType(), template.getName(), count);
    return Map.of("item", template.getName(), "herb", template.getName(), "count", count);
  }
}
