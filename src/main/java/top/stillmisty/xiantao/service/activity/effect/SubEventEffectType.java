package top.stillmisty.xiantao.service.activity.effect;

import java.util.Arrays;

public enum SubEventEffectType {
  ADD_EXP,
  ADD_EXP_PERCENT,
  TAKE_DAMAGE_PERCENT,
  TAKE_DAMAGE_FLAT,
  HEAL_FLAT,
  ADD_ITEM,
  ADD_RANDOM_ITEM,
  CREATE_EQUIPMENT,
  DROP_SPECIALTY,
  ADD_SPIRIT_STONES,
  TAKE_SPIRIT_STONES,
  MULTIPLY_BOUNTY_REWARD,
  PURE_NARRATIVE;

  public static SubEventEffectType fromCode(String code) {
    return Arrays.stream(values()).filter(t -> t.name().equals(code)).findFirst().orElse(null);
  }
}
