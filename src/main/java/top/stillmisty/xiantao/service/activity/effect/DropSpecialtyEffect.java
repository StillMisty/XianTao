package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
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
    MapNode mapNode = (MapNode) context.get("mapNode");
    if (mapNode == null) return Map.of();
    var specialties = mapNode.getSpecialties();
    if (specialties == null || specialties.isEmpty()) return Map.of();

    int count = params.containsKey("count") ? ((Number) params.get("count")).intValue() : 1;
    for (int i = 0; i < count; i++) {
      SpecialtyEntry entry =
          WeightedRandom.select(specialties, SpecialtyEntry::weight, ThreadLocalRandom.current());
      if (entry == null) continue;
      itemTemplateRepository
          .findById(entry.templateId())
          .ifPresent(
              template ->
                  stackableItemService.addStackableItem(
                      userId, entry.templateId(), template.getType(), template.getName(), 1));
    }
    return Map.of();
  }
}
