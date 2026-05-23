package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.item.vo.CharacterStatusResult;
import top.stillmisty.xiantao.domain.user.enums.CultivationRealm;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.infrastructure.util.FormatUtils;
import top.stillmisty.xiantao.service.player.CharacterStatusService;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusCommandHandler implements CommandGroup {

  private final CharacterStatusService characterStatusService;

  public String handleStatus(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理状态查询 - Platform: {}, OpenId: {}", platform, openId);
    return CommandHandlerHelper.safeCall(
        () -> characterStatusService.getCharacterStatus(platform, openId),
        fmt,
        vo -> formatCharacterStatus(vo, fmt));
  }

  private String formatCharacterStatus(CharacterStatusResult status, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(fmt.subHeading(status.nickname()));

    if (status.locationName() != null) {
      sb.append(fmt.listItem("所在地：" + status.locationName()));
    }

    if (status.status() == UserStatus.TRAVELING && status.travelDestinationName() != null) {
      sb.append(
          fmt.listItem(
              "状态：赶路中 (" + status.locationName() + " → " + status.travelDestinationName() + ")"));
      if (status.travelTimeMinutes() != null
          && status.travelMinutesElapsed() != null
          && status.travelMinutesRemaining() != null) {
        String progress =
            "旅途进度："
                + FormatUtils.formatMinutes(status.travelMinutesElapsed())
                + "/"
                + FormatUtils.formatMinutes(status.travelTimeMinutes().longValue());
        if (status.travelMinutesRemaining() > 0) {
          progress += "（剩余 " + FormatUtils.formatMinutes(status.travelMinutesRemaining()) + "）";
        } else {
          progress += "（即将到达）";
        }
        sb.append(fmt.listItem(progress));
      } else if (status.estimatedArrivalTime() != null) {
        sb.append(
            fmt.listItem("预计到达：" + FormatUtils.formatDateTime(status.estimatedArrivalTime())));
      }
    } else if (status.status() != null && status.statusName() != null) {
      sb.append(fmt.listItem("状态：" + status.statusName()));
    }

    sb.append(
        fmt.listItem(
            String.format("境界：%s (%.1f%%)", status.realmDisplay(), status.expPercentage())));
    sb.append(
        fmt.listItem(
            String.format(
                "HP：%d/%d (%.1f%%)", status.hpCurrent(), status.hpMax(), status.hpPercentage())));
    sb.append("\n");
    sb.append(fmt.heading("基础属性"));
    sb.append(fmt.listItem("力道：" + formatAttrWithBonus(status.statStr(), status.equipStr())));
    sb.append(fmt.listItem("根骨：" + formatAttrWithBonus(status.statCon(), status.equipCon())));
    sb.append(fmt.listItem("身法：" + formatAttrWithBonus(status.statAgi(), status.equipAgi())));
    sb.append(fmt.listItem("悟性：" + formatAttrWithBonus(status.statWis(), status.equipWis())));
    sb.append("\n");
    sb.append(fmt.heading("战斗属性"));
    sb.append(fmt.listItem("攻击：" + status.attack()));
    sb.append(fmt.listItem("防御：" + status.defense()));
    if (status.breakthroughSuccessRate() != null) {
      sb.append("\n");
      sb.append(fmt.heading("突破信息"));
      sb.append(fmt.listItem(String.format("突破成功率：%.1f%%", status.breakthroughSuccessRate())));
      if (status.breakthroughFailCount() != null && status.breakthroughFailCount() > 0) {
        sb.append(fmt.listItem(String.format("失败次数：%d（已累积补偿）", status.breakthroughFailCount())));
      }
    }
    if (status.protectorCount() != null
        || (status.protectedByList() != null && !status.protectedByList().isEmpty())) {
      sb.append("\n");
      sb.append(fmt.heading("护道信息"));
      if (status.protectingList() != null && !status.protectingList().isEmpty()) {
        sb.append(
            fmt.listItem(
                String.format(
                    "你正在为 %d/%d 位道友护道", status.protectorCount(), status.maxProtectorCount())));
        for (var info : status.protectingList()) {
          String locationStatus = fmt.locationStatus(info.isInSameLocation());
          sb.append(
              fmt.subListItem(
                  String.format(
                      "%s（%s）- %s %s - 加成%.1f%%",
                      info.userName(),
                      CultivationRealm.realmDisplay(info.userLevel()),
                      info.locationName(),
                      locationStatus,
                      info.bonusPercentage())));
        }
      } else {
        sb.append(
            fmt.listItem(
                String.format(
                    "你正在为 %d/%d 位道友护道",
                    status.protectorCount() != null ? status.protectorCount() : 0,
                    status.maxProtectorCount() != null ? status.maxProtectorCount() : 3)));
      }
      if (status.protectedByList() != null && !status.protectedByList().isEmpty()) {
        sb.append(
            fmt.listItem(
                String.format(
                    "有 %d 位道友为你护道，总加成：%.1f%%",
                    status.protectedByList().size(),
                    status.totalProtectionBonus() != null ? status.totalProtectionBonus() : 0.0)));
        for (var info : status.protectedByList()) {
          String locationStatus = fmt.locationStatus(info.isInSameLocation());
          String bonusText =
              Boolean.TRUE.equals(info.isInSameLocation())
                  ? String.format("加成%.1f%%", info.bonusPercentage())
                  : "无法提供加成";
          sb.append(
              fmt.subListItem(
                  String.format(
                      "%s（%s）- %s %s - %s",
                      info.userName(),
                      CultivationRealm.realmDisplay(info.userLevel()),
                      info.locationName(),
                      locationStatus,
                      bonusText)));
        }
      } else {
        sb.append(fmt.listItem("无道友为你护道"));
      }
    }
    sb.append("\n").append(fmt.heading("灵石：" + status.spiritStones()));
    if (status.equipment() != null && !status.equipment().items().isEmpty()) {
      sb.append("\n");
      sb.append(fmt.heading("已穿戴装备"));
      status
          .equipment()
          .items()
          .forEach(
              item ->
                  sb.append(
                      fmt.listItem(
                          String.format(
                              "%s：%s [%s]", item.slotName(), item.name(), item.rarityName()))));
    }
    return sb.toString();
  }

  private String formatAttrWithBonus(int base, int bonus) {
    if (bonus == 0) {
      return String.valueOf(base);
    }
    String sign = bonus > 0 ? "+" : "";
    return String.format("%d（%s%d）", base, sign, bonus);
  }

  @Override
  public String groupName() {
    return "状态";
  }

  @Override
  public String groupDescription() {
    return "角色状态查询";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(new CommandEntry("状态", "查看角色完整信息", "状态"));
  }
}
