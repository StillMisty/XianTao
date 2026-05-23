package top.stillmisty.xiantao.service.activity.effect;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.inventory.SimpleItemAdder;

@Component
public class AddRandomItemEffect implements SubEventEffect {

  private final ItemTemplateRepository itemTemplateRepository;
  private final SimpleItemAdder simpleItemAdder;

  public AddRandomItemEffect(
      ItemTemplateRepository itemTemplateRepository, SimpleItemAdder simpleItemAdder) {
    this.itemTemplateRepository = itemTemplateRepository;
    this.simpleItemAdder = simpleItemAdder;
  }

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.ADD_RANDOM_ITEM;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, Map<String, Object> params, Map<String, Object> context) {
    double chance =
        params.containsKey("chance") ? ((Number) params.get("chance")).doubleValue() : 1.0;
    if (ThreadLocalRandom.current().nextDouble() >= chance) return Map.of();

    @SuppressWarnings("unchecked")
    List<Number> templateIds = (List<Number>) params.get("template_ids");
    if (templateIds == null || templateIds.isEmpty()) return Map.of();

    long templateId =
        templateIds.get(ThreadLocalRandom.current().nextInt(templateIds.size())).longValue();
    var template = itemTemplateRepository.findById(templateId).orElse(null);
    if (template == null) return Map.of();
    simpleItemAdder.addItem(userId, template, template.getName(), 1);
    return Map.of("item", template.getName(), "herb", template.getName(), "count", 1);
  }
}
