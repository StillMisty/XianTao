package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.inventory.EquipmentService;

@Component
@RequiredArgsConstructor
public class CreateEquipmentEffect implements SubEventEffect {

  private final EquipmentService equipmentService;

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.CREATE_EQUIPMENT;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, EffectParams params, EventContext context) {
    if (!(params instanceof EffectParams.CreateEquipmentParams p)) return Map.of();
    if (p.templateId() == null) return Map.of();
    equipmentService.createEquipment(userId, p.templateId());
    return Map.of();
  }
}
