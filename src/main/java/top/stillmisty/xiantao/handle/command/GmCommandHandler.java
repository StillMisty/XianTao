package top.stillmisty.xiantao.handle.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.GmService;

@Component
@RequiredArgsConstructor
public class GmCommandHandler {

  private final GmService gmService;

  public String handleGmHelp(PlatformType platform, String openId, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> gmService.gmHelp(platform, openId), fmt, text -> text);
  }

  public String handleGiveSpiritStones(
      PlatformType platform,
      String openId,
      String targetNickname,
      String amountStr,
      TextFormat fmt) {
    long amount;
    try {
      amount = Long.parseLong(amountStr);
    } catch (NumberFormatException e) {
      return "❌ 数量格式错误：" + amountStr;
    }
    return CommandHandlerHelper.safeCall(
        () -> gmService.giveSpiritStones(platform, openId, targetNickname, amount),
        fmt,
        text -> text);
  }

  public String handleGiveExp(
      PlatformType platform,
      String openId,
      String targetNickname,
      String amountStr,
      TextFormat fmt) {
    long amount;
    try {
      amount = Long.parseLong(amountStr);
    } catch (NumberFormatException e) {
      return "❌ 数量格式错误：" + amountStr;
    }
    return CommandHandlerHelper.safeCall(
        () -> gmService.giveExp(platform, openId, targetNickname, amount), fmt, text -> text);
  }

  public String handleHealUser(
      PlatformType platform, String openId, String targetNickname, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> gmService.healUser(platform, openId, targetNickname), fmt, text -> text);
  }

  public String handleReviveUser(
      PlatformType platform, String openId, String targetNickname, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> gmService.reviveUser(platform, openId, targetNickname), fmt, text -> text);
  }

  public String handleSetLevel(
      PlatformType platform,
      String openId,
      String targetNickname,
      String levelStr,
      TextFormat fmt) {
    int level;
    try {
      level = Integer.parseInt(levelStr);
    } catch (NumberFormatException e) {
      return "❌ 等级格式错误：" + levelStr;
    }
    return CommandHandlerHelper.safeCall(
        () -> gmService.setLevel(platform, openId, targetNickname, level), fmt, text -> text);
  }

  public String handleSetLocation(
      PlatformType platform,
      String openId,
      String targetNickname,
      String locationName,
      TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> gmService.setLocation(platform, openId, targetNickname, locationName),
        fmt,
        text -> text);
  }

  public String handleGiveItem(
      PlatformType platform,
      String openId,
      String targetNickname,
      String itemName,
      String quantityStr,
      TextFormat fmt) {
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
        () -> gmService.giveItem(platform, openId, targetNickname, itemName, finalQuantity),
        fmt,
        text -> text);
  }
}
