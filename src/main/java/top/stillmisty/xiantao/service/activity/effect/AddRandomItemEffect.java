package top.stillmisty.xiantao.service.activity.effect;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

@Component
public class AddRandomItemEffect implements SubEventEffect {

  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemService stackableItemService;

  public AddRandomItemEffect(
      ItemTemplateRepository itemTemplateRepository, StackableItemService stackableItemService) {
    this.itemTemplateRepository = itemTemplateRepository;
    this.stackableItemService = stackableItemService;
  }

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.ADD_RANDOM_ITEM;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, EffectParams params, EventContext context) {
    if (!(params instanceof EffectParams.AddRandomItemIdsParams p)) return Map.of();
    double chance = p.chance() != null ? p.chance() : 1.0;
    if (ThreadLocalRandom.current().nextDouble() >= chance) return Map.of();

    List<Long> templateIds = p.templateIds();
    if (templateIds == null || templateIds.isEmpty()) return Map.of();

    long templateId = templateIds.get(ThreadLocalRandom.current().nextInt(templateIds.size()));
    var template = itemTemplateRepository.findById(templateId).orElse(null);
    if (template == null) return Map.of();
    stackableItemService.addStackableItem(
        userId, template.getId(), template.getType(), template.getName(), 1);
    return Map.of("item", template.getName(), "herb", template.getName(), "count", 1);
  }
}
