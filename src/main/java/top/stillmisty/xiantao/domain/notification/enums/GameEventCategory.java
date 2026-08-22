package top.stillmisty.xiantao.domain.notification.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/** 游戏事件大类 — 用于 NotificationAppender 按节分组 */
@Getter
public enum GameEventCategory {
  TRAVEL_ARRIVED("TRAVEL_ARRIVED", "旅途见闻"),
  TRAVEL_EVENT("TRAVEL_EVENT", "旅途见闻"),
  TRAVEL_HIDDEN("TRAVEL_HIDDEN", "旅途见闻"),

  TRAINING_COMPLETE("TRAINING_COMPLETE", "历练收获"),
  TRAINING_EVENT("TRAINING_EVENT", "历练收获"),
  TRAINING_HIDDEN("TRAINING_HIDDEN", "历练收获"),
  TRAINING_INTERRUPTED("TRAINING_INTERRUPTED", "历练收获"),

  BOUNTY_COMPLETE("BOUNTY_COMPLETE", "悬赏完成"),
  BOUNTY_SIDE_MODIFIER("BOUNTY_SIDE_MODIFIER", "悬赏完成"),
  BOUNTY_HIDDEN("BOUNTY_HIDDEN", "悬赏完成"),
  BOUNTY_READY("BOUNTY_READY", "悬赏完成"),

  HP_RECOVERED("HP_RECOVERED", null),
  BUFF_EXPIRED("BUFF_EXPIRED", null),
  DYING_RECOVERED("DYING_RECOVERED", null),
  LEVEL_UP("LEVEL_UP", "突破"),
  FORTUNE("FORTUNE", null),

  WORLD_EVENT("WORLD_EVENT", "世界事件"),
  WORLD_EVENT_PARTICIPATION("WORLD_EVENT_PARTICIPATION", "世界事件"),

  SECT_EVENT("SECT_EVENT", "宗门动态"),

  DUNGEON_ENTER("DUNGEON_ENTER", "秘境探索"),
  DUNGEON_EXPLORE("DUNGEON_EXPLORE", "秘境探索"),
  DUNGEON_HIDDEN("DUNGEON_HIDDEN", "秘境探索"),
  DUNGEON_COMPLETE("DUNGEON_COMPLETE", "秘境结算"),

  GUIDE("GUIDE", "初入仙途");

  @EnumValue private final String code;

  /** 节标题 (null 表示纯文本提示，无框线) */
  private final @Nullable String sectionTitle;

  GameEventCategory(String code, @Nullable String sectionTitle) {
    this.code = code;
    this.sectionTitle = sectionTitle;
  }

  public static GameEventCategory fromCode(String code) {
    for (GameEventCategory category : values()) {
      if (category.code.equals(code)) {
        return category;
      }
    }
    throw new IllegalArgumentException("Unknown GameEventCategory code: " + code);
  }
}
