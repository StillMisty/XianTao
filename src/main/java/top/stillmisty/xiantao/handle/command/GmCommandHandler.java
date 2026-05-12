package top.stillmisty.xiantao.handle.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.GmService;
import top.stillmisty.xiantao.service.ServiceResult;

@Component
@RequiredArgsConstructor
public class GmCommandHandler {

  private final GmService gmService;

  public String handleGmHelp(PlatformType platform, String openId) {
    return handleGmHelp(platform, openId, TextFormat.PLAIN);
  }

  public String handleGmHelpMarkdown(PlatformType platform, String openId) {
    return handleGmHelp(platform, openId, TextFormat.MARKDOWN);
  }

  public String handleGmHelp(PlatformType platform, String openId, TextFormat fmt) {
    return switch (gmService.gmHelp(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var text) -> text;
    };
  }

  public String handleGiveSpiritStones(
      PlatformType platform, String openId, String targetNickname, String amountStr) {
    return handleGiveSpiritStones(platform, openId, targetNickname, amountStr, TextFormat.PLAIN);
  }

  public String handleGiveSpiritStonesMarkdown(
      PlatformType platform, String openId, String targetNickname, String amountStr) {
    return handleGiveSpiritStones(platform, openId, targetNickname, amountStr, TextFormat.MARKDOWN);
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
    return switch (gmService.giveSpiritStones(platform, openId, targetNickname, amount)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var text) -> text;
    };
  }

  public String handleGiveExp(
      PlatformType platform, String openId, String targetNickname, String amountStr) {
    return handleGiveExp(platform, openId, targetNickname, amountStr, TextFormat.PLAIN);
  }

  public String handleGiveExpMarkdown(
      PlatformType platform, String openId, String targetNickname, String amountStr) {
    return handleGiveExp(platform, openId, targetNickname, amountStr, TextFormat.MARKDOWN);
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
    return switch (gmService.giveExp(platform, openId, targetNickname, amount)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var text) -> text;
    };
  }

  public String handleHealUser(PlatformType platform, String openId, String targetNickname) {
    return handleHealUser(platform, openId, targetNickname, TextFormat.PLAIN);
  }

  public String handleHealUserMarkdown(
      PlatformType platform, String openId, String targetNickname) {
    return handleHealUser(platform, openId, targetNickname, TextFormat.MARKDOWN);
  }

  public String handleHealUser(
      PlatformType platform, String openId, String targetNickname, TextFormat fmt) {
    return switch (gmService.healUser(platform, openId, targetNickname)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var text) -> text;
    };
  }

  public String handleReviveUser(PlatformType platform, String openId, String targetNickname) {
    return handleReviveUser(platform, openId, targetNickname, TextFormat.PLAIN);
  }

  public String handleReviveUserMarkdown(
      PlatformType platform, String openId, String targetNickname) {
    return handleReviveUser(platform, openId, targetNickname, TextFormat.MARKDOWN);
  }

  public String handleReviveUser(
      PlatformType platform, String openId, String targetNickname, TextFormat fmt) {
    return switch (gmService.reviveUser(platform, openId, targetNickname)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var text) -> text;
    };
  }

  public String handleSetLevel(
      PlatformType platform, String openId, String targetNickname, String levelStr) {
    return handleSetLevel(platform, openId, targetNickname, levelStr, TextFormat.PLAIN);
  }

  public String handleSetLevelMarkdown(
      PlatformType platform, String openId, String targetNickname, String levelStr) {
    return handleSetLevel(platform, openId, targetNickname, levelStr, TextFormat.MARKDOWN);
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
    return switch (gmService.setLevel(platform, openId, targetNickname, level)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var text) -> text;
    };
  }

  public String handleSetLocation(
      PlatformType platform, String openId, String targetNickname, String locationName) {
    return handleSetLocation(platform, openId, targetNickname, locationName, TextFormat.PLAIN);
  }

  public String handleSetLocationMarkdown(
      PlatformType platform, String openId, String targetNickname, String locationName) {
    return handleSetLocation(platform, openId, targetNickname, locationName, TextFormat.MARKDOWN);
  }

  public String handleSetLocation(
      PlatformType platform,
      String openId,
      String targetNickname,
      String locationName,
      TextFormat fmt) {
    return switch (gmService.setLocation(platform, openId, targetNickname, locationName)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var text) -> text;
    };
  }

  public String handleGiveItem(
      PlatformType platform,
      String openId,
      String targetNickname,
      String itemName,
      String quantityStr) {
    return handleGiveItem(
        platform, openId, targetNickname, itemName, quantityStr, TextFormat.PLAIN);
  }

  public String handleGiveItemMarkdown(
      PlatformType platform,
      String openId,
      String targetNickname,
      String itemName,
      String quantityStr) {
    return handleGiveItem(
        platform, openId, targetNickname, itemName, quantityStr, TextFormat.MARKDOWN);
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
    return switch (gmService.giveItem(platform, openId, targetNickname, itemName, quantity)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var text) -> text;
    };
  }
}
