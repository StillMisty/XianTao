package top.stillmisty.xiantao.domain.beast.vo;

import java.util.List;

/** 灵兽技能池配置VO */
public record BeastSkillPoolVO(
    List<InnateSkill> innateSkills, List<AwakeningSkill> awakeningSkills) {
  /** 先天技配置 */
  public record InnateSkill(Long skillId, String unlock) {}

  /** 后天悟配置 */
  public record AwakeningSkill(Long skillId, int weight) {}
}
