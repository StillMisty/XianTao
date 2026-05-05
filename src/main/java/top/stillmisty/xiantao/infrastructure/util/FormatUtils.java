package top.stillmisty.xiantao.infrastructure.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FormatUtils {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("MM-dd HH:mm");

  private FormatUtils() {}

  /** 将分钟数格式化为人类可读的字符串。 例: 30 → "30分钟", 60 → "1小时", 90 → "1小时30分钟" */
  public static String formatMinutes(long minutes) {
    if (minutes <= 0) return "0分钟";
    long hours = minutes / 60;
    long remainMinutes = minutes % 60;
    if (hours == 0) return remainMinutes + "分钟";
    if (remainMinutes == 0) return hours + "小时";
    return hours + "小时" + remainMinutes + "分钟";
  }

  /** 格式化 LocalDateTime 为 MM-dd HH:mm */
  public static String formatDateTime(LocalDateTime dateTime) {
    if (dateTime == null) return "";
    return dateTime.format(DATE_TIME_FORMATTER);
  }
}
