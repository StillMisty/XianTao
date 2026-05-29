package top.stillmisty.xiantao.handle.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.GmService;
import top.stillmisty.xiantao.service.UserContext;

@Component
@RequiredArgsConstructor
public class GmCommandHandler {

  private final GmService gmService;

  public String handleGmHelp(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(() -> gmService.gmHelp(userId), fmt, text -> text);
  }

  public String handleGiveSpiritStones(String targetNickname, String amountStr, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    long amount;
    try {
      amount = Long.parseLong(amountStr);
    } catch (NumberFormatException e) {
      return "❌ 数量格式错误：" + amountStr;
    }
    return CommandHandlerHelper.safeCall(
        () -> gmService.giveSpiritStones(userId, targetNickname, amount), fmt, text -> text);
  }

  public String handleGiveExp(String targetNickname, String amountStr, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    long amount;
    try {
      amount = Long.parseLong(amountStr);
    } catch (NumberFormatException e) {
      return "❌ 数量格式错误：" + amountStr;
    }
    return CommandHandlerHelper.safeCall(
        () -> gmService.giveExp(userId, targetNickname, amount), fmt, text -> text);
  }

  public String handleHealUser(String targetNickname, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> gmService.healUser(userId, targetNickname), fmt, text -> text);
  }

  public String handleReviveUser(String targetNickname, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> gmService.reviveUser(userId, targetNickname), fmt, text -> text);
  }

  public String handleSetLevel(String targetNickname, String levelStr, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    int level;
    try {
      level = Integer.parseInt(levelStr);
    } catch (NumberFormatException e) {
      return "❌ 等级格式错误：" + levelStr;
    }
    return CommandHandlerHelper.safeCall(
        () -> gmService.setLevel(userId, targetNickname, level), fmt, text -> text);
  }

  public String handleSetLocation(String targetNickname, String locationName, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> gmService.setLocation(userId, targetNickname, locationName), fmt, text -> text);
  }

  public String handleGiveItem(
      String targetNickname, String itemName, String quantityStr, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    int quantity = 1;
    if (quantityStr != null && !quantityStr.isBlank()) {
      try {
        quantity = Integer.parseInt(quantityStr);
      } catch (NumberFormatException e) {
        return "❌ 数量格式错误：" + quantityStr;
      }
    }
    int finalQuantity = quantity;
    return CommandHandlerHelper.safeCall(
        () -> gmService.giveItem(userId, targetNickname, itemName, finalQuantity),
        fmt,
        text -> text);
  }
}
