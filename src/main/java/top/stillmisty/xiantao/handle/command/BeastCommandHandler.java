package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.beast.vo.*;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.BeastService;
import top.stillmisty.xiantao.service.ServiceResult;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeastCommandHandler implements CommandGroup {

  private final BeastService beastService;

  public String handleDeployBeast(PlatformType platform, String openId, String position) {
    return handleDeployBeast(platform, openId, position, TextFormat.PLAIN);
  }

  public String handleUndeployBeast(PlatformType platform, String openId, String position) {
    return handleUndeployBeast(platform, openId, position, TextFormat.PLAIN);
  }

  public String handleRecoverBeast(PlatformType platform, String openId, String position) {
    return handleRecoverBeast(platform, openId, position, TextFormat.PLAIN);
  }

  public String handleEvolveBeast(
      PlatformType platform, String openId, String position, String mode) {
    return handleEvolveBeast(platform, openId, position, mode, TextFormat.PLAIN);
  }

  public String handleReleaseBeast(PlatformType platform, String openId, String position) {
    return handleReleaseBeast(platform, openId, position, TextFormat.PLAIN);
  }

  public String handleGetDeployedBeasts(PlatformType platform, String openId) {
    return handleGetDeployedBeasts(platform, openId, TextFormat.PLAIN);
  }

  public String handleDeployBeastMarkdown(PlatformType platform, String openId, String position) {
    return handleDeployBeast(platform, openId, position, TextFormat.MARKDOWN);
  }

  public String handleUndeployBeastMarkdown(PlatformType platform, String openId, String position) {
    return handleUndeployBeast(platform, openId, position, TextFormat.MARKDOWN);
  }

  public String handleRecoverBeastMarkdown(PlatformType platform, String openId, String position) {
    return handleRecoverBeast(platform, openId, position, TextFormat.MARKDOWN);
  }

  public String handleEvolveBeastMarkdown(
      PlatformType platform, String openId, String position, String mode) {
    return handleEvolveBeast(platform, openId, position, mode, TextFormat.MARKDOWN);
  }

  public String handleReleaseBeastMarkdown(PlatformType platform, String openId, String position) {
    return handleReleaseBeast(platform, openId, position, TextFormat.MARKDOWN);
  }

  public String handleGetDeployedBeastsMarkdown(PlatformType platform, String openId) {
    return handleGetDeployedBeasts(platform, openId, TextFormat.MARKDOWN);
  }

  // ===================== 统一处理方法（含 TextFormat 参数） =====================

  public String handleDeployBeast(
      PlatformType platform, String openId, String position, TextFormat fmt) {
    log.debug("处理灵兽出战 - Platform: {}, OpenId: {}, Position: {}", platform, openId, position);
    return switch (beastService.deployBeast(platform, openId, position)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> formatDeployResult(result, fmt);
    };
  }

  public String handleUndeployBeast(
      PlatformType platform, String openId, String position, TextFormat fmt) {
    log.debug("处理灵兽召回 - Platform: {}, OpenId: {}, Position: {}", platform, openId, position);
    return switch (beastService.undeployBeast(platform, openId, position)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> formatUndeployResult(result, fmt);
    };
  }

  public String handleRecoverBeast(
      PlatformType platform, String openId, String position, TextFormat fmt) {
    log.debug("处理灵兽恢复 - Platform: {}, OpenId: {}, Position: {}", platform, openId, position);
    return switch (beastService.recoverBeast(platform, openId, position)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> formatRecoverResult(result, fmt);
    };
  }

  public String handleEvolveBeast(
      PlatformType platform, String openId, String position, String mode, TextFormat fmt) {
    log.debug(
        "处理灵兽进化 - Platform: {}, OpenId: {}, Position: {}, Mode: {}",
        platform,
        openId,
        position,
        mode);
    return switch (beastService.evolveBeast(platform, openId, position, mode)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> formatEvolveResult(result, fmt);
    };
  }

  public String handleReleaseBeast(
      PlatformType platform, String openId, String position, TextFormat fmt) {
    log.debug("处理灵兽放生 - Platform: {}, OpenId: {}, Position: {}", platform, openId, position);
    return switch (beastService.releaseBeast(platform, openId, position)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> formatReleaseResult(result, fmt);
    };
  }

  public String handleGetDeployedBeasts(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理查看出战灵兽 - Platform: {}, OpenId: {}", platform, openId);
    return switch (beastService.getDeployedBeasts(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var beasts) -> formatDeployedBeasts(beasts, fmt);
    };
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

  private String formatRecoverResult(BeastRecoverResult result, TextFormat fmt) {
    if (result instanceof ActionResultVO vo) return formatRecoverSingle(vo, fmt);
    if (result instanceof RecoverResultVO vo) return formatRecoverDetailed(vo, fmt);
    if (result instanceof BatchRecoverVO vo) return formatRecoverBatch(vo, fmt);
    return "恢复完成";
  }

  private String formatRecoverSingle(ActionResultVO vo, TextFormat fmt) {
    if (vo.success()) {
      return fmt.heading("灵兽恢复", "💚") + vo.message();
    } else {
      return fmt.heading("恢复失败", "❌") + vo.message();
    }
  }

  private String formatRecoverDetailed(RecoverResultVO vo, TextFormat fmt) {
    if (vo.success()) {
      return fmt.heading("灵兽恢复", "💚") + vo.message();
    } else {
      return fmt.heading("恢复失败", "❌") + vo.message();
    }
  }

  private String formatRecoverBatch(BatchRecoverVO vo, TextFormat fmt) {
    return fmt.heading("灵兽恢复", "💚") + "已恢复 %d 只灵兽，消耗 %d 灵石。".formatted(vo.count(), vo.cost());
  }

  private String formatEvolveResult(PenCellVO beast, TextFormat fmt) {
    return fmt.heading("灵兽进化成功", "⬆️")
        + fmt.listItem("名称：" + beast.getBeastName())
        + fmt.listItem("等阶：T" + beast.getTier())
        + fmt.listItem("品质：" + beast.getQuality())
        + fmt.listItem("战力：" + beast.getPowerScore());
  }

  private String formatReleaseResult(ReleaseBeastVO result, TextFormat fmt) {
    return fmt.heading("灵兽放生成功", "🕊️")
        + String.format(
            "放生了 %s（T%d %s）\n获得灵兽精华", result.beastName(), result.tier(), result.quality());
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
              "%d. %s（T%d %s）\n", i + 1, beast.beastName(), beast.tier(), beast.quality()));
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
        new CommandEntry("灵兽出战 {{编号}}", "派出灵兽参与战斗", "灵兽出战 1"),
        new CommandEntry("灵兽召回 {{编号/all}}", "召回出战的灵兽", "灵兽召回 1"),
        new CommandEntry("灵兽恢复 {{编号/all}}", "恢复灵兽生命值", "灵兽恢复 1"),
        new CommandEntry("灵兽进化 {{编号}} {{升阶/升品}}", "进化灵兽", "灵兽进化 1 升阶"),
        new CommandEntry("灵兽放生 {{编号}}", "放生灵兽", "灵兽放生 1"),
        new CommandEntry("出战灵兽", "查看当前出战的灵兽", "出战灵兽"));
  }
}
