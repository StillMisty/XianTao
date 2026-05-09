package top.stillmisty.xiantao.handle.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.GmService;
import top.stillmisty.xiantao.service.ServiceResult;

@Component
@RequiredArgsConstructor
public class GmCommandHandler {

  private final GmService gmService;

  public String handleGmHelp(PlatformType platform, String openId) {
    return switch (gmService.gmHelp(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var text) -> text;
    };
  }

  public String handleGiveSpiritStones(
      PlatformType platform, String openId, String targetNickname, String amountStr) {
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
    return switch (gmService.healUser(platform, openId, targetNickname)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var text) -> text;
    };
  }

  public String handleReviveUser(PlatformType platform, String openId, String targetNickname) {
    return switch (gmService.reviveUser(platform, openId, targetNickname)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var text) -> text;
    };
  }

  public String handleSetLevel(
      PlatformType platform, String openId, String targetNickname, String levelStr) {
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

  // ===================== Markdown 格式化方法（QQ平台） =====================

  public String handleGmHelpMarkdown(PlatformType platform, String openId) {
    return switch (gmService.gmHelp(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var text) -> text;
    };
  }

  public String handleGiveSpiritStonesMarkdown(
      PlatformType platform, String openId, String targetNickname, String amountStr) {
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

  public String handleGiveExpMarkdown(
      PlatformType platform, String openId, String targetNickname, String amountStr) {
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

  public String handleHealUserMarkdown(
      PlatformType platform, String openId, String targetNickname) {
    return switch (gmService.healUser(platform, openId, targetNickname)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var text) -> text;
    };
  }

  public String handleReviveUserMarkdown(
      PlatformType platform, String openId, String targetNickname) {
    return switch (gmService.reviveUser(platform, openId, targetNickname)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var text) -> text;
    };
  }

  public String handleSetLevelMarkdown(
      PlatformType platform, String openId, String targetNickname, String levelStr) {
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

  public String handleSetLocationMarkdown(
      PlatformType platform, String openId, String targetNickname, String locationName) {
    return switch (gmService.setLocation(platform, openId, targetNickname, locationName)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var text) -> text;
    };
  }

  public String handleGiveItemMarkdown(
      PlatformType platform,
      String openId,
      String targetNickname,
      String itemName,
      String quantityStr) {
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
