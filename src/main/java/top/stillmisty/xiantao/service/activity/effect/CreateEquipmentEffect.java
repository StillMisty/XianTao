package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
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
      Long userId, User user, Map<String, Object> params, Map<String, Object> context) {
    Number templateIdNum = (Number) params.get("template_id");
    if (templateIdNum == null) return Map.of();
    long templateId = templateIdNum.longValue();
    equipmentService.createEquipment(userId, templateId);
    return Map.of();
  }
}
