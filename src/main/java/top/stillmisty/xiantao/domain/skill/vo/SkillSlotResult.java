package top.stillmisty.xiantao.domain.skill.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SkillSlotResult {

  private boolean success;
  private String message;

  private SkillVO skill;

  private int equippedCount;
  private int maxSlots;
}
