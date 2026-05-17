package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.sect.SectService;

@Component
@RequiredArgsConstructor
public class SectCommandHandler implements CommandGroup {

  private final SectService sectService;

  public String handleOverview(PlatformType platform, String openId, TextFormat fmt) {
    var result = sectService.getSectOverview(platform, openId);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleCreate(PlatformType platform, String openId, String name, TextFormat fmt) {
    var result = sectService.createSect(platform, openId, name);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleInvite(
      PlatformType platform, String openId, String targetNickname, TextFormat fmt) {
    var result = sectService.inviteMember(platform, openId, targetNickname);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleApply(PlatformType platform, String openId, String sectName, TextFormat fmt) {
    var result = sectService.applyJoin(platform, openId, sectName);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleKick(
      PlatformType platform, String openId, String targetNickname, TextFormat fmt) {
    var result = sectService.kickMember(platform, openId, targetNickname);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleLeave(PlatformType platform, String openId, TextFormat fmt) {
    var result = sectService.leaveSect(platform, openId);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleAppoint(
      PlatformType platform,
      String openId,
      String targetNickname,
      String position,
      TextFormat fmt) {
    var result = sectService.appointMember(platform, openId, targetNickname, position);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleDismiss(PlatformType platform, String openId, TextFormat fmt) {
    var result = sectService.dismissSect(platform, openId);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleNotice(PlatformType platform, String openId, String content, TextFormat fmt) {
    var result = sectService.setNotice(platform, openId, content);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleDonate(PlatformType platform, String openId, long amount, TextFormat fmt) {
    var result = sectService.donateStones(platform, openId, amount);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleShop(PlatformType platform, String openId, TextFormat fmt) {
    var result = sectService.getShop(platform, openId);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleExchange(
      PlatformType platform, String openId, long shopItemId, TextFormat fmt) {
    var result = sectService.exchangeShopItem(platform, openId, shopItemId);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleSkills(PlatformType platform, String openId, TextFormat fmt) {
    var result = sectService.getSkills(platform, openId);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleUpgrade(PlatformType platform, String openId, TextFormat fmt) {
    var result = sectService.upgradeSect(platform, openId);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleExpand(PlatformType platform, String openId, TextFormat fmt) {
    var result = sectService.expandMembers(platform, openId);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleRefreshShop(PlatformType platform, String openId, TextFormat fmt) {
    var result = sectService.refreshShop(platform, openId);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  @Override
  public String groupName() {
    return "宗门";
  }

  @Override
  public String groupDescription() {
    return "宗门创建、成员管理、贡献商店、宗门功法";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("宗门", "查看宗门总览（名称、等级、成员、资金、公告）", "宗门"),
        new CommandEntry("宗门创建 「名称」", "创建宗门", "宗门创建 青云宗"),
        new CommandEntry("宗门邀请 「道号」", "邀请玩家加入宗门", "宗门邀请 张三"),
        new CommandEntry("宗门申请 「宗门名」", "申请加入宗门", "宗门申请 青云宗"),
        new CommandEntry("宗门踢出 「道号」", "踢出成员", "宗门踢出 张三"),
        new CommandEntry("宗门退出", "退出宗门（贡献清零）", "宗门退出"),
        new CommandEntry("宗门任命 「道号」 「职位」", "任命/罢免职位", "宗门任命 张三 VICE_LEADER"),
        new CommandEntry("宗门解散", "解散宗门", "宗门解散"),
        new CommandEntry("宗门公告 「内容」", "发布公告", "宗门公告 今日宗门活动"),
        new CommandEntry("宗门捐献 「灵石数量」", "捐献灵石（得等额贡献）", "宗门捐献 1000"),
        new CommandEntry("宗门商店", "查看贡献商店", "宗门商店"),
        new CommandEntry("宗门兑换 「商品编号」", "用贡献兑换物品", "宗门兑换 1"),
        new CommandEntry("宗门功法", "查看宗门功法列表", "宗门功法"),
        new CommandEntry("宗门升级", "宗主消耗资金提升宗门等级", "宗门升级"),
        new CommandEntry("宗门扩充", "宗主消耗资金扩充成员上限", "宗门扩充"),
        new CommandEntry("宗门刷新商店", "宗主刷新贡献商店", "宗门刷新商店"));
  }
}
