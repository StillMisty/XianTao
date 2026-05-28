package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import org.springframework.stereotype.Component;
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
      Long userId, User user, Map<String, Object> params, Map<String, Object> context) {
    long templateId = ((Number) params.get("template_id")).longValue();
    int count =
        params.containsKey("count")
            ? ((Number) params.get("count")).intValue()
            : resolveCount(params);
    if (count <= 0) return Map.of();
    var template = itemTemplateRepository.findById(templateId).orElse(null);
    if (template == null) return Map.of();
    stackableItemService.addStackableItem(
        userId, template.getId(), template.getType(), template.getName(), count);
    return Map.of("item", template.getName(), "herb", template.getName(), "count", count);
  }

  static int resolveCount(Map<String, Object> params) {
    int min = params.containsKey("min") ? ((Number) params.get("min")).intValue() : 1;
    int max = params.containsKey("max") ? ((Number) params.get("max")).intValue() : 1;
    return java.util.concurrent.ThreadLocalRandom.current().nextInt(min, max + 1);
  }
}
