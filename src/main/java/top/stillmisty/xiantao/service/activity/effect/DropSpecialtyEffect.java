package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContextKeys;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.map.entity.SpecialtyEntry;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.util.WeightedRandom;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

@Component
@RequiredArgsConstructor
public class DropSpecialtyEffect implements SubEventEffect {

  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemService stackableItemService;

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.DROP_SPECIALTY;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, Map<String, Object> params, Map<String, Object> context) {
    var mapNode = EventContextKeys.MAP_NODE.get(context);
    if (mapNode == null) return Map.of();
    var specialties = mapNode.getSpecialties();
    if (specialties == null || specialties.isEmpty()) return Map.of();

    int count = params.containsKey("count") ? ((Number) params.get("count")).intValue() : 1;
    String firstName = null;
    int dropped = 0;
    for (int i = 0; i < count; i++) {
      SpecialtyEntry entry =
          WeightedRandom.select(specialties, SpecialtyEntry::weight, ThreadLocalRandom.current());
      if (entry == null) continue;
      var template = itemTemplateRepository.findById(entry.templateId()).orElse(null);
      if (template == null) continue;
      stackableItemService.addStackableItem(
          userId, template.getId(), template.getType(), template.getName(), 1);
      dropped++;
      if (firstName == null) {
        firstName = template.getName();
      }
    }
    if (dropped == 0) return Map.of();
    return Map.of("herb", firstName != null ? firstName : "", "count", dropped);
  }
}
