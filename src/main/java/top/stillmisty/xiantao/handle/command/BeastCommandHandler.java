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
import top.stillmisty.xiantao.service.BeastService;
import top.stillmisty.xiantao.service.ServiceResult;

/** 灵兽命令处理器（纯 View 层） 调用 Service 层获取结构化数据，格式化为纯文本返回 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BeastCommandHandler implements CommandGroup {

  private final BeastService beastService;

  /** 处理灵兽出战命令 */
  public String handleDeployBeast(PlatformType platform, String openId, String position) {
    log.debug("处理灵兽出战 - Platform: {}, OpenId: {}, Position: {}", platform, openId, position);
    return switch (beastService.deployBeast(platform, openId, position)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> formatDeployResult(result);
    };
  }

  /** 处理灵兽召回命令 */
  public String handleUndeployBeast(PlatformType platform, String openId, String position) {
    log.debug("处理灵兽召回 - Platform: {}, OpenId: {}, Position: {}", platform, openId, position);
    return switch (beastService.undeployBeast(platform, openId, position)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> formatUndeployResult(result);
    };
  }

  /** 处理灵兽恢复命令 */
  public String handleRecoverBeast(PlatformType platform, String openId, String position) {
    log.debug("处理灵兽恢复 - Platform: {}, OpenId: {}, Position: {}", platform, openId, position);
    return switch (beastService.recoverBeast(platform, openId, position)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> formatRecoverResult(result);
    };
  }

  /** 处理灵兽进化命令 */
  public String handleEvolveBeast(
      PlatformType platform, String openId, String position, String mode) {
    log.debug(
        "处理灵兽进化 - Platform: {}, OpenId: {}, Position: {}, Mode: {}",
        platform,
        openId,
        position,
        mode);
    return switch (beastService.evolveBeast(platform, openId, position, mode)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> formatEvolveResult(result);
    };
  }

  /** 处理灵兽放生命令 */
  public String handleReleaseBeast(PlatformType platform, String openId, String position) {
    log.debug("处理灵兽放生 - Platform: {}, OpenId: {}, Position: {}", platform, openId, position);
    return switch (beastService.releaseBeast(platform, openId, position)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> formatReleaseResult(result);
    };
  }

  /** 处理查看出战灵兽命令 */
  public String handleGetDeployedBeasts(PlatformType platform, String openId) {
    log.debug("处理查看出战灵兽 - Platform: {}, OpenId: {}", platform, openId);
    return switch (beastService.getDeployedBeasts(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var beasts) -> formatDeployedBeasts(beasts);
    };
  }

  // ===================== 文本格式化方法 =====================

  private String formatDeployResult(ActionResultVO result) {
    if (result.success()) {
      return "【灵兽出战】\n" + result.message();
    } else {
      return "【出战失败】\n" + result.message();
    }
  }

  private String formatUndeployResult(BeastUndeployResult result) {
    if (result instanceof ActionResultVO vo) return formatUndeploySingle(vo);
    if (result instanceof BatchCountVO vo) return formatUndeployBatch(vo);
    return "召回完成";
  }

  private String formatUndeploySingle(ActionResultVO vo) {
    if (vo.success()) {
      return "【灵兽召回】\n" + vo.message();
    } else {
      return "【召回失败】\n" + vo.message();
    }
  }

  private String formatUndeployBatch(BatchCountVO vo) {
    return "【灵兽召回】\n已召回 %d 只灵兽。".formatted(vo.count());
  }

  private String formatRecoverResult(BeastRecoverResult result) {
    if (result instanceof ActionResultVO vo) return formatRecoverSingle(vo);
    if (result instanceof RecoverResultVO vo) return formatRecoverDetailed(vo);
    if (result instanceof BatchRecoverVO vo) return formatRecoverBatch(vo);
    return "恢复完成";
  }

  private String formatRecoverSingle(ActionResultVO vo) {
    if (vo.success()) {
      return "【灵兽恢复】\n" + vo.message();
    } else {
      return "【恢复失败】\n" + vo.message();
    }
  }

  private String formatRecoverDetailed(RecoverResultVO vo) {
    if (vo.success()) {
      return "【灵兽恢复】\n" + vo.message();
    } else {
      return "【恢复失败】\n" + vo.message();
    }
  }

  private String formatRecoverBatch(BatchRecoverVO vo) {
    return "【灵兽恢复】\n已恢复 %d 只灵兽，消耗 %d 灵石。".formatted(vo.count(), vo.cost());
  }

  private String formatEvolveResult(PenCellVO beast) {
    return "【灵兽进化成功】\n"
        + String.format("名称：%s\n", beast.getBeastName())
        + String.format("等阶：T%d\n", beast.getTier())
        + String.format("品质：%s\n", beast.getQuality())
        + String.format("战力：%d\n", beast.getPowerScore());
  }

  private String formatReleaseResult(ReleaseBeastVO result) {
    return String.format(
        "【灵兽放生成功】\n放生了 %s（T%d %s）\n获得灵兽精华", result.beastName(), result.tier(), result.quality());
  }

  private String formatDeployedBeasts(List<BeastStatusVO> beasts) {
    if (beasts.isEmpty()) {
      return "【出战灵兽】（空）\n没有出战的灵兽。";
    }
    StringBuilder sb = new StringBuilder("【出战灵兽】\n");
    for (int i = 0; i < beasts.size(); i++) {
      BeastStatusVO beast = beasts.get(i);
      sb.append(
          String.format(
              "%d. %s（T%d %s）\n", i + 1, beast.beastName(), beast.tier(), beast.quality()));
      sb.append(String.format("   等级：%d\n", beast.level()));
      sb.append(String.format("   HP：%d/%d\n", beast.hpCurrent(), beast.maxHp()));
      sb.append(String.format("   攻击：%d 防御：%d\n", beast.attack(), beast.defense()));
      if (beast.skills() != null && !beast.skills().isEmpty()) {
        sb.append(String.format("   技能：%d个\n", beast.skills().size()));
      }
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
