package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.user.enums.CultivationRealm;
import top.stillmisty.xiantao.domain.user.vo.BreakthroughResult;
import top.stillmisty.xiantao.domain.user.vo.DaoProtectionQueryResult;
import top.stillmisty.xiantao.domain.user.vo.DaoProtectionResult;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.cultivation.CultivationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class CultivationCommandHandler implements CommandGroup {

  private final CultivationService cultivationService;

  public String handleBreakthrough(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    log.debug("处理突破 - UserId: {}", userId);
    return CommandHandlerHelper.safeCall(
        () -> cultivationService.attemptBreakthrough(userId),
        fmt,
        vo -> formatBreakthroughResult(vo, fmt));
  }

  public String handleEstablishProtection(String protegeNickname, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    log.debug("处理护道 - UserId: {}, Protege: {}", userId, protegeNickname);
    return CommandHandlerHelper.safeCall(
        () -> cultivationService.establishProtection(userId, protegeNickname),
        fmt,
        vo -> vo.success() ? formatProtectionResult(vo, fmt) : vo.message());
  }

  public String handleRemoveProtection(String protegeNickname, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    log.debug("处理护道解除 - UserId: {}, Protege: {}", userId, protegeNickname);
    return CommandHandlerHelper.safeCall(
        () -> cultivationService.removeProtection(userId, protegeNickname),
        fmt,
        vo -> vo.message());
  }

  public String handleQueryProtection(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    log.debug("处理护道查询 - UserId: {}", userId);
    return CommandHandlerHelper.safeCall(
        () -> cultivationService.queryProtectionInfo(userId),
        fmt,
        vo -> vo.isSuccess() ? formatProtectionQueryResult(vo, fmt) : vo.getMessage());
  }

  // ===================== 格式化方法 =====================

  private String formatBreakthroughResult(BreakthroughResult result, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(result.message()).append("\n\n");

    if (result.battleResult() != null) {
      sb.append(fmt.heading("雷劫详情"));
      sb.append(fmt.listItem("雷劫类型：" + result.tribulationTypeName()));
      sb.append(fmt.listItem("战斗回合：" + result.battleResult().rounds()));
      sb.append(fmt.listItem("胜负：" + result.battleResult().winner()));
      if (result.newLevel() != null) sb.append(fmt.listItem("当前境界：" + result.realmDisplay()));

      if (result.battleResult().playerHpChange() != null) {
        result
            .battleResult()
            .playerHpChange()
            .forEach(
                (name, hp) ->
                    sb.append(
                        fmt.listItem(
                            String.format("%s HP：%d → %d", name, hp.before(), hp.after()))));
      }
    } else {
      if (result.successRate() != null)
        sb.append(fmt.listItem(String.format("突破成功率：%.1f%%", result.successRate())));
      if (result.newLevel() != null)
        sb.append(fmt.listItem(String.format("当前境界：%s", result.realmDisplay())));
      if (result.nextBreakthroughRate() != null)
        sb.append(fmt.listItem(String.format("下次突破成功率：%.1f%%", result.nextBreakthroughRate())));
    }
    return sb.toString();
  }

  private String formatProtectionResult(DaoProtectionResult result, TextFormat fmt) {
    return (result.message()
        + "\n\n"
        + fmt.heading("护道详情")
        + fmt.listItem(
            "护道者："
                + result.protectorName()
                + "（"
                + CultivationRealm.realmDisplay(result.protectorLevel())
                + "）")
        + fmt.listItem(
            "被护道者："
                + result.protegeName()
                + "（"
                + CultivationRealm.realmDisplay(result.protegeLevel())
                + "）")
        + fmt.listItem(String.format("单人加成：%.1f%%", result.singleProtectorBonus()))
        + fmt.listItem("是否同地点：" + (result.isInSameLocation() ? "是" : "否")));
  }

  private String formatProtectionQueryResult(DaoProtectionQueryResult result, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(result.getMessage()).append("\n");
    if (result.getProtectingList() != null && !result.getProtectingList().isEmpty()) {
      sb.append("\n");
      sb.append(
          fmt.heading(
              String.format(
                  "你正在为以下道友护道 (%d/%d)",
                  result.getProtectingCount(), result.getMaxProtectingCount())));
      for (var info : result.getProtectingList()) {
        String locationStatus = fmt.locationStatus(info.getIsInSameLocation());
        sb.append(
            fmt.subListItem(
                String.format(
                    "%s（%s）- %s %s - 加成%.1f%%",
                    info.getUserName(),
                    CultivationRealm.realmDisplay(info.getUserLevel()),
                    info.getLocationName(),
                    locationStatus,
                    info.getBonusPercentage())));
      }
    } else {
      sb.append("\n");
      sb.append(fmt.heading("你正在为以下道友护道"));
      sb.append("无\n");
    }
    if (result.getProtectedByList() != null && !result.getProtectedByList().isEmpty()) {
      sb.append("\n");
      sb.append(
          fmt.heading(String.format("以下道友正在为你护道（总加成：%.1f%%）", result.getTotalBonusPercentage())));
      for (var info : result.getProtectedByList()) {
        String locationStatus = fmt.locationStatus(info.getIsInSameLocation());
        String bonusText =
            info.getIsInSameLocation()
                ? String.format("加成%.1f%%", info.getBonusPercentage())
                : "无法提供加成";
        sb.append(
            fmt.subListItem(
                String.format(
                    "%s（%s）- %s %s - %s",
                    info.getUserName(),
                    CultivationRealm.realmDisplay(info.getUserLevel()),
                    info.getLocationName(),
                    locationStatus,
                    bonusText)));
      }
    } else {
      sb.append("\n");
      sb.append(fmt.heading("以下道友正在为你护道"));
      sb.append("无\n");
    }
    return sb.toString();
  }

  @Override
  public String groupName() {
    return "修炼";
  }

  @Override
  public String groupSummary() {
    return "境界突破、护道系统";
  }

  @Override
  public String groupDescription() {
    return "突破、护道";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("突破", "尝试境界突破", "突破"),
        new CommandEntry("护道 「道号」", "建立护道关系", "护道 李四"),
        new CommandEntry("护道解除 「道号」", "解除护道关系", "护道解除 李四"),
        new CommandEntry("护道查询", "查看护道信息", "护道查询"));
  }
}
