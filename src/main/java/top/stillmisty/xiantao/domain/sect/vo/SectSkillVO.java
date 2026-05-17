package top.stillmisty.xiantao.domain.sect.vo;

/** 宗门功法 VO */
public record SectSkillVO(
    Long skillId, String skillName, String effectDesc, Integer requiredSectLevel) {}
