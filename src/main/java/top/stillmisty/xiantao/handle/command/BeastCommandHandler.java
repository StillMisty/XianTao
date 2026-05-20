package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.vo.*;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.beast.BeastBreedingService;
import top.stillmisty.xiantao.service.beast.BeastCombatService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeastCommandHandler implements CommandGroup {

  private final BeastCombatService beastCombatService;
  private final BeastBreedingService beastBreedingService;

  // ===================== 统一处理方法（含 TextFormat 参数） =====================

  public String handleDeployBeast(
      PlatformType platform, String openId, String position, TextFormat fmt) {
    log.debug("处理灵兽出战 - Platform: {}, OpenId: {}, Position: {}", platform, openId, position);
    return CommandHandlerHelper.safeCall(
        () -> beastCombatService.deployBeast(platform, openId, position),
        fmt,
        result -> formatDeployResult(result, fmt));
  }

  public String handleUndeployBeast(
      PlatformType platform, String openId, String position, TextFormat fmt) {
    log.debug("处理灵兽召回 - Platform: {}, OpenId: {}, Position: {}", platform, openId, position);
    return CommandHandlerHelper.safeCall(
        () -> beastCombatService.undeployBeast(platform, openId, position),
        fmt,
        result -> formatUndeployResult(result, fmt));
  }

  public String handleRecoverBeast(
      PlatformType platform, String openId, String position, TextFormat fmt) {
    log.debug("处理灵兽恢复 - Platform: {}, OpenId: {}, Position: {}", platform, openId, position);
    return CommandHandlerHelper.safeCall(
        () -> beastCombatService.recoverBeast(platform, openId, position),
        fmt,
        result -> formatRecoverSingle(result, fmt));
  }

  public String handleEvolveBeast(
      PlatformType platform, String openId, String position, TextFormat fmt) {
    log.debug("处理灵兽进化 - Platform: {}, OpenId: {}, Position: {}", platform, openId, position);
    return CommandHandlerHelper.safeCall(
        () -> beastBreedingService.evolveBeast(platform, openId, position),
        fmt,
        result -> formatEvolveResult(result, fmt));
  }

  public String handleReleaseBeast(
      PlatformType platform, String openId, String position, TextFormat fmt) {
    log.debug("处理灵兽放生 - Platform: {}, OpenId: {}, Position: {}", platform, openId, position);
    return CommandHandlerHelper.safeCall(
        () -> beastBreedingService.releaseBeast(platform, openId, position),
        fmt,
        result -> formatReleaseResult(result, fmt));
  }

  public String handleGetDeployedBeasts(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理灵兽 - Platform: {}, OpenId: {}", platform, openId);
    return CommandHandlerHelper.safeCall(
        () -> beastCombatService.getDeployedBeasts(platform, openId),
        fmt,
        beasts -> formatDeployedBeasts(beasts, fmt));
  }

  public String handleBeastList(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理灵兽列表 - Platform: {}, OpenId: {}", platform, openId);
    return CommandHandlerHelper.safeCall(
        () -> beastCombatService.getBeastList(platform, openId),
        fmt,
        beasts -> formatBeastList(beasts, fmt));
  }

  public String handleFeedBeast(
      PlatformType platform, String openId, String position, int quantity, TextFormat fmt) {
    log.debug(
        "处理灵兽喂养 - Platform: {}, OpenId: {}, Position: {}, Quantity: {}",
        platform,
        openId,
        position,
        quantity);
    return CommandHandlerHelper.safeCall(
        () -> beastBreedingService.feedBeast(platform, openId, position, quantity),
        fmt,
        exp -> "✅ 喂养成功！灵兽获得 %d 经验。".formatted(exp));
  }

  // ===================== 格式化方法 =====================

  private String formatDeployResult(ActionResultVO result, TextFormat fmt) {
    if (result.success()) {
      return fmt.heading("灵兽出战", "🐾") + result.message();
    } else {
      return fmt.heading("出战失败", "❌") + result.message();
    }
  }

  private String formatUndeployResult(BeastUndeployResult result, TextFormat fmt) {
    if (result instanceof ActionResultVO vo) return formatUndeploySingle(vo, fmt);
    if (result instanceof BatchCountVO vo) return formatUndeployBatch(vo, fmt);
    return "召回完成";
  }

  private String formatUndeploySingle(ActionResultVO vo, TextFormat fmt) {
    if (vo.success()) {
      return fmt.heading("灵兽召回", "🐾") + vo.message();
    } else {
      return fmt.heading("召回失败", "❌") + vo.message();
    }
  }

  private String formatUndeployBatch(BatchCountVO vo, TextFormat fmt) {
    return fmt.heading("灵兽召回", "🐾") + "已召回 %d 只灵兽。".formatted(vo.count());
  }

  private String formatRecoverSingle(ActionResultVO vo, TextFormat fmt) {
    if (vo.success()) {
      return fmt.heading("灵兽恢复", "💚") + vo.message();
    } else {
      return fmt.heading("恢复失败", "❌") + vo.message();
    }
  }

  private String formatEvolveResult(PenCellVO beast, TextFormat fmt) {
    return fmt.heading("灵兽进化成功", "⬆️")
        + fmt.listItem("名称：" + beast.getBeastName())
        + fmt.listItem("等阶：" + Beast.getTierName(beast.getTier()))
        + fmt.listItem("品质：" + beast.getQuality())
        + fmt.listItem("战力：" + beast.getPowerScore());
  }

  private String formatReleaseResult(ReleaseBeastVO result, TextFormat fmt) {
    return fmt.heading("灵兽放生成功", "🕊️")
        + String.format(
            "放生了 %s（T%d %s）\n获得 %d 份灵兽精华",
            result.beastName(), result.tier(), result.quality(), result.essenceAmount());
  }

  private String formatDeployedBeasts(List<BeastStatusVO> beasts, TextFormat fmt) {
    if (beasts.isEmpty()) {
      return fmt.heading("出战灵兽（空）", "🐾") + "没有出战的灵兽。";
    }
    StringBuilder sb = new StringBuilder(fmt.heading("出战灵兽", "🐾"));
    for (int i = 0; i < beasts.size(); i++) {
      BeastStatusVO beast = beasts.get(i);
      sb.append(
          String.format(
              "%d. %s（%s %s）\n",
              i + 1, beast.beastName(), Beast.getTierName(beast.tier()), beast.quality()));
      sb.append(fmt.listItem("等级：" + beast.level()));
      sb.append(fmt.listItem("HP：" + beast.hpCurrent() + "/" + beast.maxHp()));
      sb.append(fmt.listItem("攻击：" + beast.attack() + " 防御：" + beast.defense()));
      if (beast.skills() != null && !beast.skills().isEmpty()) {
        sb.append(fmt.listItem("技能：" + beast.skills().size() + "个"));
      }
      if (i < beasts.size() - 1) sb.append(fmt.separator());
    }
    return sb.toString();
  }

  private String formatBeastList(List<BeastStatusVO> beasts, TextFormat fmt) {
    if (beasts.isEmpty()) {
      return fmt.heading("灵兽列表（空）", "📋") + "你还没有任何灵兽。";
    }
    StringBuilder sb = new StringBuilder(fmt.heading("灵兽列表", "📋"));
    sb.append(fmt.listItem("共 " + beasts.size() + " 只")).append(fmt.separator());
    for (int i = 0; i < beasts.size(); i++) {
      BeastStatusVO beast = beasts.get(i);
      String status;
      if (beast.isDeployed()) {
        status = "⚔️ 出战";
      } else if (beast.needsRecovery()) {
        status = "🛌 休养";
      } else if (beast.pennedCellId() > 0) {
        status = "🏠 在栏";
      } else {
        status = "💤 待命";
      }
      sb.append(
          "%d. %s %s %s Lv%d %s\n"
              .formatted(
                  i + 1,
                  beast.beastName(),
                  Beast.getTierName(beast.tier()),
                  beast.quality(),
                  beast.level(),
                  status));
    }
    return sb.toString();
  }

  @Override
  public String groupName() {
    return "灵兽";
  }

  @Override
  public String groupDescription() {
    return "灵兽出战、召回、恢复、进化、放生";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("灵兽", "查看当前出战的灵兽", "灵兽"),
        new CommandEntry("灵兽列表", "查看拥有的所有灵兽", "灵兽列表"),
        new CommandEntry("灵兽出战 「编号」", "派出灵兽参与战斗", "灵兽出战 1"),
        new CommandEntry("灵兽召回 「编号/all」", "召回出战的灵兽", "灵兽召回 1"),
        new CommandEntry("灵兽恢复 「编号/all」", "恢复灵兽生命值", "灵兽恢复 1"),
        new CommandEntry("灵兽进化 「编号」", "进化灵兽，提升等阶", "灵兽进化 1"),
        new CommandEntry("灵兽放生 「编号」", "放生灵兽", "灵兽放生 1"),
        new CommandEntry("灵兽喂养 「编号」 「数量」", "消耗灵兽精华喂养灵兽", "灵兽喂养 1 3"));
  }
}
