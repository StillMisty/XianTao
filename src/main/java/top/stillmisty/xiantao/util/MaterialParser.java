package top.stillmisty.xiantao.util;

import org.jspecify.annotations.Nullable;

public final class MaterialParser {

  private MaterialParser() {}

  /**
   * Parse an input string like "物品×5" into name and quantity. Supports ×, x, or X as the separator.
   *
   * @param input the raw input string
   * @return the parsed material, or {@code null} if the input is malformed
   */
  @Nullable
  public static ParsedMaterial parse(String input) {
    String[] parts = input.split("[×xX]");
    if (parts.length != 2) return null;
    String name = parts[0].trim();
    int quantity;
    try {
      quantity = Integer.parseInt(parts[1].trim());
    } catch (NumberFormatException e) {
      return null;
    }
    if (quantity <= 0) return null;
    return new ParsedMaterial(name, quantity);
  }

  public record ParsedMaterial(String name, int quantity) {}
}
