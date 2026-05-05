package top.stillmisty.xiantao.domain.fudi.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 地块类型 */
@Getter
public enum CellType {
  EMPTY("empty", "空地"),
  FARM("farm", "灵田"),
  PEN("pen", "兽栏");

  @EnumValue private final String code;
  private final String chineseName;

  CellType(String code, String chineseName) {
    this.code = code;
    this.chineseName = chineseName;
  }

  public static CellType fromCode(String code) {
    for (CellType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown CellType code: " + code);
  }

  public static CellType fromChineseName(String name) {
    for (CellType type : values()) {
      if (type.chineseName.equals(name)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown CellType name: " + name);
  }
}
