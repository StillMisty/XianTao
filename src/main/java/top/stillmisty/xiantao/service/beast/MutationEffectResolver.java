package top.stillmisty.xiantao.service.beast;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.entity.MutationEffect;
import top.stillmisty.xiantao.domain.beast.entity.MutationTraitConfig;
import top.stillmisty.xiantao.domain.beast.enums.MutationEffectType;
import top.stillmisty.xiantao.domain.beast.enums.TriggerType;
import top.stillmisty.xiantao.infrastructure.repository.MutationTraitConfigRepository;

/** 统一处理变异效果的查询和计算 */
@Component
@RequiredArgsConstructor
public class MutationEffectResolver {

  private final MutationTraitConfigRepository traitConfigRepository;

  /** 获取灵兽所有无条件效果的总值 */
  public double sumEffectValue(Beast beast, MutationEffectType targetType) {
    return getEffects(beast).stream()
        .filter(e -> e.type() == targetType && e.condition() == null)
        .mapToDouble(MutationEffect::value)
        .sum();
  }

  /** 获取灵兽所有带指定触发条件的效果 */
  public List<MutationEffect> getConditionalEffects(Beast beast, MutationEffectType targetType) {
    return getEffects(beast).stream()
        .filter(e -> e.type() == targetType && e.condition() != null)
        .toList();
  }

  /** 获取灵兽所有带指定触发类型的效果 */
  public List<MutationEffect> getEffectsByTrigger(Beast beast, TriggerType triggerType) {
    return getEffects(beast).stream()
        .filter(e -> e.condition() != null && e.condition().trigger() == triggerType)
        .toList();
  }

  /** 检查灵兽是否有指定类型的变异效果 */
  public boolean hasEffect(Beast beast, MutationEffectType targetType) {
    return getEffects(beast).stream().anyMatch(e -> e.type() == targetType);
  }

  /** 获取灵兽的所有变异效果 */
  public List<MutationEffect> getEffects(Beast beast) {
    if (beast.getMutationTraits() == null || beast.getMutationTraits().isEmpty()) {
      return List.of();
    }
    return beast.getMutationTraits().stream()
        .map(traitConfigRepository::findById)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .flatMap(config -> config.getEffects().stream())
        .toList();
  }

  /** 获取变异配置 */
  public Optional<MutationTraitConfig> getConfig(Long id) {
    return traitConfigRepository.findById(id);
  }

  /** 获取变异配置 */
  public Optional<MutationTraitConfig> getConfigByName(String name) {
    return traitConfigRepository.findByName(name);
  }
}
