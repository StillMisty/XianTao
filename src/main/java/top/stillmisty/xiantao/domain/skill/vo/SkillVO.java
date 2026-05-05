package top.stillmisty.xiantao.domain.skill.vo;

import java.util.List;
import top.stillmisty.xiantao.domain.skill.entity.SkillEffect;

public record SkillVO(
    long playerSkillId,
    long skillId,
    String name,
    String description,
    List<SkillEffect> effects,
    String bindingType,
    String bindingTypeName,
    String bindingValue,
    int cooldownSeconds,
    int levelRequirement,
    boolean equipped) {}
