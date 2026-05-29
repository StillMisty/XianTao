package top.stillmisty.xiantao.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

public final class MaterialParser {

  private MaterialParser() {}

  private static final Pattern MATERIAL_PATTERN = Pattern.compile("^(\\D+)(\\d+)$");

  /**
   * Parse an input string like "灵草3" into name and quantity.
   *
   * @param input the raw input string
   * @return the parsed material, or {@code null} if the input is malformed
   */
  @Nullable
  public static ParsedMaterial parse(String input) {
    Matcher matcher = MATERIAL_PATTERN.matcher(input.trim());
    if (!matcher.matches()) return null;
    String name = matcher.group(1).trim();
    int quantity;
    try {
      quantity = Integer.parseInt(matcher.group(2));
    } catch (NumberFormatException e) {
      return null;
    }
    if (quantity <= 0 || name.isEmpty()) return null;
    return new ParsedMaterial(name, quantity);
  }

  /** Check if the input looks like a material input (name + quantity, e.g. "灵草3"). */
  public static boolean isMaterialInput(String input) {
    return MATERIAL_PATTERN.matcher(input.trim()).matches();
  }

  public record ParsedMaterial(String name, int quantity) {}
}
