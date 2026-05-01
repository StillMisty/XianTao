package top.stillmisty.xiantao.domain.skill.vo;

import top.stillmisty.xiantao.domain.skill.entity.SkillEffect;

import java.util.List;

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
        boolean equipped
) {
}
