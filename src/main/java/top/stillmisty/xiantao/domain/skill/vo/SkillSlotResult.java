package top.stillmisty.xiantao.domain.skill.vo;

import lombok.Builder;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@Builder
public class SkillSlotResult {

  private boolean success;
  private String message;

  @Nullable private SkillVO skill;

  private int equippedCount;
  private int maxSlots;
}
