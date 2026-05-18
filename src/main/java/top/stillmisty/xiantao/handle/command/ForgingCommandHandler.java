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
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.enhance.EnhancementService;
import top.stillmisty.xiantao.service.forging.ForgingService;

/** 锻造/强化命令处理器（纯 View 层） */
@Slf4j
@Component
@RequiredArgsConstructor
public class ForgingCommandHandler implements CommandGroup {

  private final ForgingService forgingService;
  private final EnhancementService enhancementService;

  // ===================== 统一处理方法（含 TextFormat 参数） =====================

  public String handleForgingRecipeList(PlatformType platform, String openId, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> forgingService.getForgingRecipes(platform, openId),
        fmt,
        recipes -> formatRecipeList(recipes, fmt),
        msg -> msg);
  }

  public String handleForgeAuto(
      PlatformType platform, String openId, String blueprintName, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> forgingService.forgeAuto(platform, openId, blueprintName),
        fmt,
        result -> formatForgingResult(result, fmt),
        msg -> msg);
  }

  public String handleForgeManual(
      PlatformType platform, String openId, List<String> materialInputs, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> forgingService.forgeManual(platform, openId, materialInputs),
        fmt,
        result -> formatForgingResult(result, fmt),
        msg -> msg);
  }

  public String handleEnhanceAuto(
      PlatformType platform, String openId, String equipmentInput, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> enhancementService.enhanceAuto(platform, openId, equipmentInput),
        fmt,
        result -> formatEnhanceResult(result, fmt),
        msg -> msg);
  }

  public String handleEnhanceManual(
      PlatformType platform,
      String openId,
      String equipmentInput,
      List<String> materialInputs,
      TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> enhancementService.enhanceManual(platform, openId, equipmentInput, materialInputs),
        fmt,
        result -> formatEnhanceResult(result, fmt),
        msg -> msg);
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
    sb.append(fmt.heading("锻造成功", "🔨✨"));
    sb.append(fmt.listItem("装备：" + result.equipmentName()));
    if (result.rarity() != null) {
      sb.append(
          fmt.listItem("稀有度：" + result.rarity().getColor().getEmoji() + result.rarity().getName()));
    }
    sb.append(fmt.listItem(String.format("品质分：%.1f%%", result.qualityScore() * 100)));

    appendUsedMaterials(sb, result.usedMaterials(), fmt);
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
    appendUsedMaterials(sb, result.usedMaterials(), fmt);
    return sb.toString();
  }

  private void appendUsedMaterials(
      StringBuilder sb, Map<String, Integer> usedMaterials, TextFormat fmt) {
    if (usedMaterials != null && !usedMaterials.isEmpty()) {
      sb.append("\n消耗锻材：\n");
      for (Map.Entry<String, Integer> entry : usedMaterials.entrySet()) {
        sb.append(fmt.listItem(String.format("%s x%d", entry.getKey(), entry.getValue())));
      }
    }
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
        new CommandEntry("锻造 「图纸名」", "自动锻造（自动选材）", "锻造 寒冰剑图"),
        new CommandEntry("锻造 「锻材1×N 锻材2×M ...」", "手动锻造（指定锻材配比）", "锻造 玄铁矿石×3 紫金砂×1"),
        new CommandEntry("强化 「装备名」", "自动强化（自动选材）", "强化 寒冰剑"),
        new CommandEntry("强化 「装备名 锻材1×N ...」", "手动强化（精确控制配比）", "强化 寒冰剑 玄铁矿石×3"));
  }
}
