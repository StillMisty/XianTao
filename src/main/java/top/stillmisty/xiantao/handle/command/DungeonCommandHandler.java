package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.dungeon.vo.DropItemVO;
import top.stillmisty.xiantao.domain.dungeon.vo.DungeonListVO;
import top.stillmisty.xiantao.domain.dungeon.vo.ExploreResultVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.dungeon.DungeonService;

@Slf4j
@Component
@RequiredArgsConstructor
public class DungeonCommandHandler implements CommandGroup {

  private final DungeonService dungeonService;

  // ===================== 统一处理方法 =====================

  public String handleDungeon(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理秘境列表 - Platform: {}, OpenId: {}", platform, openId);
    return switch (dungeonService.listDungeons(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var vo) -> formatDungeonList(vo, fmt);
    };
  }

  public String handleDungeonEnter(
      PlatformType platform, String openId, String dungeonName, TextFormat fmt) {
    log.debug("处理进入秘境 - Platform: {}, OpenId: {}, Dungeon: {}", platform, openId, dungeonName);
    return switch (dungeonService.enterDungeon(platform, openId, dungeonName)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var msg) -> msg;
    };
  }

  public String handleDungeonExplore(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理秘境探索 - Platform: {}, OpenId: {}", platform, openId);
    return switch (dungeonService.exploreDungeon(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var vo) -> formatExploreResult(vo, fmt);
    };
  }

  public String handleDungeonContinue(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理秘境继续 - Platform: {}, OpenId: {}", platform, openId);
    return switch (dungeonService.continueDungeon(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var msg) -> msg;
    };
  }

  public String handleDungeonRetreat(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理秘境撤退 - Platform: {}, OpenId: {}", platform, openId);
    return switch (dungeonService.retreatDungeon(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var msg) -> msg;
    };
  }

  // ===================== 格式化方法 =====================

  private String formatDungeonList(List<DungeonListVO> dungeons, TextFormat fmt) {
    if (dungeons.isEmpty()) {
      return "暂无开放的秘境。";
    }

    StringBuilder sb = new StringBuilder();
    sb.append(fmt.heading("秘境列表"));

    for (DungeonListVO d : dungeons) {
      sb.append(fmt.bold(d.name()));
      sb.append("\n");
      sb.append(fmt.listItem("等级: " + d.minLevel() + "-" + d.maxLevel()));
      sb.append(fmt.listItem("队伍上限: " + d.maxTeamSize() + "人"));

      if (d.hasActiveInstance()) {
        sb.append(fmt.listItem("状态: " + d.activeArea().getName() + " · 进行中"));
      } else if (d.firstClear()) {
        sb.append(fmt.listItem("奖励: " + d.rewardCount() + "/" + d.dailyLimit() + " · 已首通"));
      } else {
        sb.append(fmt.listItem("奖励: " + d.rewardCount() + "/" + d.dailyLimit()));
      }
      sb.append("\n");
    }

    sb.append("「秘境 秘境名」进入秘境  「秘境探索」探索  「秘境继续」推进  「秘境撤退」退出");
    return sb.toString();
  }

  private String formatExploreResult(ExploreResultVO result, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();

    String typeLabel = result.poiType();
    sb.append(fmt.bold("【")).append(typeLabel).append("】").append(result.poiName()).append("\n");

    if (result.combatSummary() != null && !result.combatSummary().isEmpty()) {
      sb.append(result.combatSummary()).append("\n");
    }

    sb.append(result.message()).append("\n");

    if (result.expGained() > 0) {
      sb.append(fmt.listItem("经验 +" + result.expGained()));
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

  // ===================== CommandGroup =====================

  @Override
  public String groupName() {
    return "秘境";
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
