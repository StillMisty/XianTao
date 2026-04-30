package top.stillmisty.xiantao.domain.skill.vo;

public record SkillVO(
        long playerSkillId,
        long skillId,
        String name,
        String description,
        String effectType,
        String effectTypeName,
        String bindingType,
        String bindingTypeName,
        String bindingValue,
        int cooldownSeconds,
        String damageFormula,
        int levelRequirement,
        boolean equipped
) {
}
