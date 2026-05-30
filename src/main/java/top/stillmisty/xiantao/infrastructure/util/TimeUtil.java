package top.stillmisty.xiantao.infrastructure.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class TimeUtil {

  private static final ZoneId ZONE = ZoneId.systemDefault();

  private TimeUtil() {}

  public static LocalDateTime now() {
    return LocalDateTime.now(ZONE);
  }

  public static LocalDate today() {
    return LocalDate.now(ZONE);
  }
}
