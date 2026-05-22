package top.stillmisty.xiantao.domain.event.enums;

import lombok.Getter;

@Getter
public enum FortuneLevel {
  GREAT_MISFORTUNE("大凶"),
  BAD_FORTUNE("凶"),
  NEUTRAL("平"),
  GOOD_FORTUNE("吉"),
  GREAT_FORTUNE("大吉");

  private final String display;

  FortuneLevel(String display) {
    this.display = display;
  }
}
