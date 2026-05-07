package top.stillmisty.xiantao.domain.item.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** 灵兽技能池，从 BeastEgg 提取以降低嵌套深度 */
public record BeastSkillPool(
    @JsonProperty("innate_skills") List<InnateSkill> innateSkills,
    @JsonProperty("awakening_skills") List<AwakeningSkill> awakeningSkills) {

  public record InnateSkill(
      @JsonProperty("skill_id") long skillId, @JsonProperty("unlock") String unlock) {}

  public record AwakeningSkill(
      @JsonProperty("skill_id") long skillId, @JsonProperty("weight") int weight) {}
}
