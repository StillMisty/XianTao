package top.stillmisty.xiantao.domain.fudi.enums;

import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public enum FudiEvent {
  WEATHER_RAIN("下雨了", "细雨绵绵，灵气充沛", List.of(Map.of("type", "ADD_EXP_PERCENT", "percent", 5))),
  WEATHER_WIND("刮风了", "微风拂过，带来远方的气息", List.of()),

  STRAY_BEAST("迷路的灵兽", "一只迷路的小灵兽闯入了福地", List.of()),
  SPIRIT_BUTTERFLY(
      "灵蝶飞舞", "一群发光的灵蝶在福地中翩翩起舞", List.of(Map.of("type", "ADD_SPIRIT_STONES", "amount", 3))),

  MYSTERIOUS_VISITOR(
      "神秘访客",
      "一位神秘的修士路过此地",
      List.of(Map.of("type", "ADD_RANDOM_ITEM", "poolId", "MATERIAL_COMMON"))),
  CROP_WITHER("灵草枯萎", "一株灵草不知为何突然枯萎了", List.of()),

  BEAST_BIRTH("灵兽诞生", "福地中诞生了一只新的小灵兽", List.of(Map.of("type", "ADD_EXP_PERCENT", "percent", 8))),
  ENERGY_RESTORE("灵气恢复", "福地的灵气自然恢复了一些", List.of(Map.of("type", "HEAL_FLAT", "amount", 30))),

  MEMORY_OLD_MASTER("回忆旧主", "地灵想起了曾经的主人", List.of()),
  MEMORY_FIRST_MEETING("初遇回忆", "地灵回忆起与你的初次相遇", List.of()),

  PRANK_SLIME("史莱姆恶作剧", "一只小史莱姆偷偷溜进了福地", List.of()),
  PRANK_THEFT("物品失踪", "你发现一件小物品不见了", List.of());

  private final String name;
  private final String description;
  private final List<Map<String, Object>> effects;

  FudiEvent(String name, String description, List<Map<String, Object>> effects) {
    this.name = name;
    this.description = description;
    this.effects = effects;
  }

  public boolean hasEffects() {
    return effects != null && !effects.isEmpty();
  }
}
