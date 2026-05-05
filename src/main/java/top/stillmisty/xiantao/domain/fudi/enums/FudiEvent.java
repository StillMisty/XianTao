package top.stillmisty.xiantao.domain.fudi.enums;

import lombok.Getter;

/** 福地事件枚举 预定义事件池 */
@Getter
public enum FudiEvent {

  // 天气事件
  WEATHER_RAIN("下雨了", "细雨绵绵，灵气充沛"),
  WEATHER_WIND("刮风了", "微风拂过，带来远方的气息"),

  // 生物事件
  STRAY_BEAST("迷路的灵兽", "一只迷路的小灵兽闯入了福地"),
  SPIRIT_BUTTERFLY("灵蝶飞舞", "一群发光的灵蝶在福地中翩翩起舞"),

  // 访客事件
  MYSTERIOUS_VISITOR("神秘访客", "一位神秘的修士路过此地"),
  CROP_WITHER("灵草枯萎", "一株灵草不知为何突然枯萎了"),

  // 灵兽事件
  BEAST_BIRTH("灵兽诞生", "福地中诞生了一只新的小灵兽"),
  ENERGY_RESTORE("灵气恢复", "福地的灵气自然恢复了一些"),

  // 回忆事件
  MEMORY_OLD_MASTER("回忆旧主", "地灵想起了曾经的主人"),
  MEMORY_FIRST_MEETING("初遇回忆", "地灵回忆起与你的初次相遇"),

  // 恶作剧事件
  PRANK_SLIME("史莱姆恶作剧", "一只小史莱姆偷偷溜进了福地"),
  PRANK_THEFT("物品失踪", "你发现一件小物品不见了");

  private final String name;
  private final String description;

  FudiEvent(String name, String description) {
    this.name = name;
    this.description = description;
  }
}
