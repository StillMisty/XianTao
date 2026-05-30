package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.dungeon.vo.DropItemVO;
import top.stillmisty.xiantao.domain.dungeon.vo.DungeonContinueResult;
import top.stillmisty.xiantao.domain.dungeon.vo.DungeonEnterResult;
import top.stillmisty.xiantao.domain.dungeon.vo.DungeonListVO;
import top.stillmisty.xiantao.domain.dungeon.vo.ExploreResultVO;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.dungeon.DungeonService;

@Slf4j
@Component
@RequiredArgsConstructor
public class DungeonCommandHandler implements CommandGroup {

  private final DungeonService dungeonService;

  public String handleDungeon(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    log.debug("处理秘境列表 - UserId: {}", userId);
    return CommandHandlerHelper.safeCall(
        () -> dungeonService.listDungeons(userId), fmt, vo -> formatDungeonList(vo, fmt));
  }

  public String handleDungeonEnter(String dungeonName, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    log.debug("处理进入秘境 - UserId: {}, Dungeon: {}", userId, dungeonName);
    return CommandHandlerHelper.safeCall(
        () -> dungeonService.enterDungeon(userId, dungeonName),
        fmt,
        vo -> formatDungeonEnterResult(vo, "紫气弥漫，天地间充斥着锋锐的金行道韵。", fmt));
  }

  public String handleDungeonExplore(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    log.debug("处理秘境探索 - UserId: {}", userId);
    return CommandHandlerHelper.safeCall(
        () -> dungeonService.exploreDungeon(userId), fmt, vo -> formatExploreResult(vo, fmt));
  }

  public String handleDungeonContinue(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    log.debug("处理秘境继续 - UserId: {}", userId);
    return CommandHandlerHelper.safeCall(
        () -> dungeonService.continueDungeon(userId),
        fmt,
        result ->
            switch (result) {
              case DungeonContinueResult.AreaView av ->
                  formatDungeonEnterResult(av.enterResult(), "紫气弥漫，天地间充斥着锋锐的金行道韵。", fmt);
              case DungeonContinueResult.Completed c -> c.settlementMessage();
            });
  }

  public String handleDungeonRetreat(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    log.debug("处理秘境撤退 - UserId: {}", userId);
    return CommandHandlerHelper.safeCall(
        () -> dungeonService.retreatDungeon(userId), fmt, msg -> msg);
  }

  // ===================== 格式化方法 =====================

  private String formatDungeonEnterResult(
      DungeonEnterResult result, String flavorText, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(fmt.bold(result.dungeonName()))
        .append(" · ")
        .append(result.areaName())
        .append("\n\n");
    if (result.memberCount() > 1) {
      sb.append(fmt.listItem("队伍人数：" + result.memberCount() + "人"));
    }
    sb.append(flavorText).append("\n\n");
    sb.append(fmt.heading("可探索的建筑"));
    for (var poi : result.pois()) {
      String locked = poi.locked() ? fmt.italic("已探索") : "";
      String suffix = locked.isEmpty() ? "" : " " + locked;
      sb.append(fmt.listItem(poi.name() + " [" + poi.typeName() + "]" + suffix));
    }
    sb.append(fmt.separator()).append(fmt.tip("输入「秘境探索」开始探索"));
    return sb.toString();
  }

  private String formatDungeonList(List<DungeonListVO> dungeons, TextFormat fmt) {
    if (dungeons.isEmpty()) {
      return "暂无开放的秘境。";
    }

    StringBuilder sb = new StringBuilder();
    sb.append(fmt.heading("秘境列表"));

    for (DungeonListVO d : dungeons) {
      sb.append(fmt.bold(d.name())).append("\n");
      sb.append(fmt.listItem("等级：" + d.minLevel() + "-" + d.maxLevel()));
      sb.append(fmt.listItem("队伍上限：" + d.maxTeamSize() + "人"));

      if (d.hasActiveInstance()) {
        sb.append(
            fmt.listItem(
                "状态：" + (d.activeArea() != null ? d.activeArea().getName() : "未知") + " · 进行中"));
      } else if (d.firstClear()) {
        sb.append(fmt.listItem("奖励：" + d.rewardCount() + "/" + d.dailyLimit() + " · 已首通"));
      } else {
        sb.append(fmt.listItem("奖励：" + d.rewardCount() + "/" + d.dailyLimit()));
      }
      sb.append("\n");
    }

    sb.append(fmt.tip("「秘境 秘境名」进入秘境  「秘境探索」探索  「秘境继续」推进  「秘境撤退」退出"));
    return sb.toString();
  }

  private String formatExploreResult(ExploreResultVO result, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();

    String typeLabel = result.poiType();
    sb.append(fmt.bold(typeLabel)).append(" ").append(result.poiName()).append("\n");

    if (result.combatSummary() != null && !result.combatSummary().isEmpty()) {
      sb.append(result.combatSummary()).append("\n");
    }

    sb.append(result.message()).append("\n");

    if (result.expGained() > 0) {
      sb.append(fmt.listItem("修为 +" + result.expGained()));
    }
    if (result.spiritStonesGained() > 0) {
      sb.append(fmt.listItem("灵石 +" + result.spiritStonesGained()));
    }
    if (result.items() != null && !result.items().isEmpty()) {
      StringBuilder itemsStr = new StringBuilder();
      for (DropItemVO item : result.items()) {
        if (!itemsStr.isEmpty()) itemsStr.append(" ");
        itemsStr.append(item.name()).append("×").append(item.quantity());
      }
      sb.append(fmt.listItem("获得: " + itemsStr));
    }

    if (result.passageUnlocked()) {
      sb.append("\n").append(fmt.bold("通道已开启！输入「秘境继续」进入下一区域。"));
    } else if (!result.combatOccurred()
        || result.combatSummary() == null
        || result.combatSummary().contains("击败")) {
      sb.append("\n输入「秘境探索」继续探索。");
    }

    return sb.toString();
  }

  @Override
  public String groupName() {
    return "秘境";
  }

  @Override
  public String groupSummary() {
    return "秘境探索与通关";
  }

  @Override
  public String groupDescription() {
    return "秘境探索、通关、奖励";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("秘境", "查看秘境列表与当前进度", "秘境"),
        new CommandEntry("秘境 「秘境名」", "进入指定秘境", "秘境 紫府秘境"),
        new CommandEntry("秘境探索", "探索当前区域的下一个建筑", "秘境探索"),
        new CommandEntry("秘境继续", "推进到下一区域", "秘境继续"),
        new CommandEntry("秘境撤退", "退出秘境并结算", "秘境撤退"));
  }
}
