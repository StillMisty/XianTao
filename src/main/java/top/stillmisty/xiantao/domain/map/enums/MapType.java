package top.stillmisty.xiantao.domain.map.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 地图类型枚举 */
@Getter
public enum MapType {

  /** 主城/安全区 */
  SAFE_TOWN("SAFE_TOWN", "安全区"),

  /** 历练区 */
  TRAINING_ZONE("TRAINING_ZONE", "历练区"),

  /** 秘境 */
  HIDDEN_ZONE("HIDDEN_ZONE", "秘境");

  @EnumValue private final String code;
  private final String name;

  MapType(String code, String name) {
    this.code = code;
    this.name = name;
  }
}
