package top.stillmisty.xiantao.handle.command;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.forge.vo.EnhanceResultVO;
import top.stillmisty.xiantao.domain.forge.vo.ForgingRecipeVO;
import top.stillmisty.xiantao.domain.forge.vo.ForgingResultVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.EnhancementService;
import top.stillmisty.xiantao.service.ForgingService;
import top.stillmisty.xiantao.service.ServiceResult;

/** 锻造/强化命令处理器（纯 View 层） */
@Slf4j
@Component
@RequiredArgsConstructor
public class ForgingCommandHandler implements CommandGroup {

  private final ForgingService forgingService;
  private final EnhancementService enhancementService;

  // ===================== 委托方法（纯文本） =====================

  public String handleForgingRecipeList(PlatformType platform, String openId) {
    return handleForgingRecipeList(platform, openId, TextFormat.PLAIN);
  }

  public String handleForgeAuto(PlatformType platform, String openId, String blueprintName) {
    return handleForgeAuto(platform, openId, blueprintName, TextFormat.PLAIN);
  }

  public String handleForgeManual(
      PlatformType platform, String openId, List<String> materialInputs) {
    return handleForgeManual(platform, openId, materialInputs, TextFormat.PLAIN);
  }

  public String handleEnhanceAuto(PlatformType platform, String openId, String equipmentInput) {
    return handleEnhanceAuto(platform, openId, equipmentInput, TextFormat.PLAIN);
  }

  public String handleEnhanceManual(
      PlatformType platform, String openId, String equipmentInput, List<String> materialInputs) {
    return handleEnhanceManual(platform, openId, equipmentInput, materialInputs, TextFormat.PLAIN);
  }

  // ===================== 委托方法（Markdown） =====================

  public String handleForgingRecipeListMarkdown(PlatformType platform, String openId) {
    return handleForgingRecipeList(platform, openId, TextFormat.MARKDOWN);
  }

  public String handleForgeAutoMarkdown(
      PlatformType platform, String openId, String blueprintName) {
    return handleForgeAuto(platform, openId, blueprintName, TextFormat.MARKDOWN);
  }

  public String handleForgeManualMarkdown(
      PlatformType platform, String openId, List<String> materialInputs) {
    return handleForgeManual(platform, openId, materialInputs, TextFormat.MARKDOWN);
  }

  public String handleEnhanceAutoMarkdown(
      PlatformType platform, String openId, String equipmentInput) {
    return handleEnhanceAuto(platform, openId, equipmentInput, TextFormat.MARKDOWN);
  }

  public String handleEnhanceManualMarkdown(
      PlatformType platform, String openId, String equipmentInput, List<String> materialInputs) {
    return handleEnhanceManual(
        platform, openId, equipmentInput, materialInputs, TextFormat.MARKDOWN);
  }

  // ===================== 统一处理方法（含 TextFormat 参数） =====================

  public String handleForgingRecipeList(PlatformType platform, String openId, TextFormat fmt) {
    return switch (forgingService.getForgingRecipes(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var recipes) -> formatRecipeList(recipes, fmt);
    };
  }

  public String handleForgeAuto(
      PlatformType platform, String openId, String blueprintName, TextFormat fmt) {
    return switch (forgingService.forgeAuto(platform, openId, blueprintName)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var result) -> formatForgingResult(result, fmt);
    };
  }

  public String handleForgeManual(
      PlatformType platform, String openId, List<String> materialInputs, TextFormat fmt) {
    return switch (forgingService.forgeManual(platform, openId, materialInputs)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var result) -> formatForgingResult(result, fmt);
    };
  }

  public String handleEnhanceAuto(
      PlatformType platform, String openId, String equipmentInput, TextFormat fmt) {
    return switch (enhancementService.enhanceAuto(platform, openId, equipmentInput)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var result) -> formatEnhanceResult(result, fmt);
    };
  }

  public String handleEnhanceManual(
      PlatformType platform,
      String openId,
      String equipmentInput,
      List<String> materialInputs,
      TextFormat fmt) {
    return switch (enhancementService.enhanceManual(
        platform, openId, equipmentInput, materialInputs)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var result) -> formatEnhanceResult(result, fmt);
    };
  }

  // ===================== 统一格式化方法 =====================

  private String formatRecipeList(List<ForgingRecipeVO> recipes, TextFormat fmt) {
    if (recipes.isEmpty()) {
      return fmt.heading("锻造图纸（空）", "📜") + "你还没有学会任何锻造图纸。";
    }
    StringBuilder sb = new StringBuilder();
    sb.append(fmt.heading("锻造图纸", "🔨"));
    for (int i = 0; i < recipes.size(); i++) {
      ForgingRecipeVO recipe = recipes.get(i);
      sb.append(
          String.format(
              "%d. %s（%d品）→ %s\n",
              i + 1, recipe.blueprintName(), recipe.grade(), recipe.equipmentTemplateName()));
    }
    sb.append("\n输入「锻造 [图纸名]」自动锻造");
    return sb.toString();
  }

  private String formatForgingResult(ForgingResultVO result, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    if (result.success()) {
      sb.append(fmt.heading("锻造成功", "🔨✨"));
      sb.append(fmt.listItem("装备：" + result.equipmentName()));
      if (result.rarity() != null) {
        sb.append(
            fmt.listItem(
                "稀有度：" + result.rarity().getColor().getEmoji() + result.rarity().getName()));
      }
      sb.append(fmt.listItem(String.format("品质分：%.1f%%", result.qualityScore() * 100)));

      if (result.usedMaterials() != null && !result.usedMaterials().isEmpty()) {
        sb.append("\n消耗锻材：\n");
        for (Map.Entry<String, Integer> entry : result.usedMaterials().entrySet()) {
          sb.append(fmt.listItem(String.format("%s x%d", entry.getKey(), entry.getValue())));
        }
      }
    } else {
      sb.append(fmt.heading("锻造失败", "💥"));
      sb.append(result.message());
    }
    return sb.toString();
  }

  private String formatEnhanceResult(EnhanceResultVO result, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    if (result.success()) {
      sb.append(fmt.heading("强化成功", "⚡✨"));
      sb.append(fmt.listItem(result.message()));
      if (result.milestoneReward() != null) {
        sb.append(fmt.listItem("里程碑：" + result.milestoneReward()));
      }
    } else {
      sb.append(fmt.heading("强化失败", "💥"));
      sb.append(result.message());
    }

    if (result.spiritStoneCost() > 0) {
      sb.append(fmt.listItem("消耗灵石：" + result.spiritStoneCost()));
    }
    if (result.usedMaterials() != null && !result.usedMaterials().isEmpty()) {
      sb.append("\n消耗锻材：\n");
      for (Map.Entry<String, Integer> entry : result.usedMaterials().entrySet()) {
        sb.append(fmt.listItem(String.format("%s x%d", entry.getKey(), entry.getValue())));
      }
    }
    return sb.toString();
  }

  @Override
  public String groupName() {
    return "锻造";
  }

  @Override
  public String groupDescription() {
    return "锻造图纸查询、锻造、装备强化";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("锻造列表", "查看已学锻造图纸", "锻造列表"),
        new CommandEntry("锻造 {{图纸名}}", "自动锻造（自动选材）", "锻造 寒冰剑图"),
        new CommandEntry("锻造 {{锻材1×N 锻材2×M ...}}", "手动锻造（指定锻材配比）", "锻造 玄铁矿石×3 紫金砂×1"),
        new CommandEntry("强化 {{装备名}}", "自动强化（自动选材）", "强化 寒冰剑"),
        new CommandEntry("强化 {{装备名 锻材1×N ...}}", "手动强化（精确控制配比）", "强化 寒冰剑 玄铁矿石×3"));
  }
}
