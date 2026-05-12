package top.stillmisty.xiantao.handle.command;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.item.entity.ElementRange;
import top.stillmisty.xiantao.domain.pill.enums.ElementType;
import top.stillmisty.xiantao.domain.pill.enums.PillQuality;
import top.stillmisty.xiantao.domain.pill.vo.PillRecipeVO;
import top.stillmisty.xiantao.domain.pill.vo.PillRefiningResultVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.PillRecipeService;
import top.stillmisty.xiantao.service.PillRefiningService;
import top.stillmisty.xiantao.service.ServiceResult;

/** 炼丹命令处理器（纯 View 层） 调用 Service 层获取结构化数据，格式化为纯文本返回 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PillCommandHandler implements CommandGroup {

  private final PillRecipeService pillRecipeService;
  private final PillRefiningService pillRefiningService;

  // ===================== 委托方法（纯文本） =====================

  public String handleRecipeList(PlatformType platform, String openId) {
    return handleRecipeList(platform, openId, TextFormat.PLAIN);
  }

  public String handleRecipeDetail(PlatformType platform, String openId, String recipeName) {
    return handleRecipeDetail(platform, openId, recipeName, TextFormat.PLAIN);
  }

  public String handleRefineAuto(PlatformType platform, String openId, String recipeName) {
    return handleRefineAuto(platform, openId, recipeName, TextFormat.PLAIN);
  }

  public String handleRefineManual(PlatformType platform, String openId, List<String> herbInputs) {
    return handleRefineManual(platform, openId, herbInputs, TextFormat.PLAIN);
  }

  // ===================== 委托方法（Markdown） =====================

  public String handleRecipeListMarkdown(PlatformType platform, String openId) {
    return handleRecipeList(platform, openId, TextFormat.MARKDOWN);
  }

  public String handleRecipeDetailMarkdown(
      PlatformType platform, String openId, String recipeName) {
    return handleRecipeDetail(platform, openId, recipeName, TextFormat.MARKDOWN);
  }

  public String handleRefineAutoMarkdown(PlatformType platform, String openId, String recipeName) {
    return handleRefineAuto(platform, openId, recipeName, TextFormat.MARKDOWN);
  }

  public String handleRefineManualMarkdown(
      PlatformType platform, String openId, List<String> herbInputs) {
    return handleRefineManual(platform, openId, herbInputs, TextFormat.MARKDOWN);
  }

  // ===================== 统一处理方法（含 TextFormat 参数） =====================

  public String handleRecipeList(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理丹方列表查询 - Platform: {}, OpenId: {}", platform, openId);
    return switch (pillRecipeService.getLearnedRecipes(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var recipes) -> formatRecipeList(recipes, fmt);
    };
  }

  public String handleRecipeDetail(
      PlatformType platform, String openId, String recipeName, TextFormat fmt) {
    log.debug("处理丹方详情查询 - Platform: {}, OpenId: {}, RecipeName: {}", platform, openId, recipeName);
    return switch (pillRecipeService.getRecipeDetail(platform, openId, recipeName)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var recipe) ->
          recipe != null ? formatRecipeDetail(recipe, fmt) : "未找到丹方：" + recipeName;
    };
  }

  public String handleRefineAuto(
      PlatformType platform, String openId, String recipeName, TextFormat fmt) {
    log.debug("处理自动炼丹 - Platform: {}, OpenId: {}, RecipeName: {}", platform, openId, recipeName);
    return switch (pillRefiningService.refinePillAuto(platform, openId, recipeName)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> formatRefiningResult(result, fmt);
    };
  }

  public String handleRefineManual(
      PlatformType platform, String openId, List<String> herbInputs, TextFormat fmt) {
    log.debug("处理手动炼丹 - Platform: {}, OpenId: {}, HerbInputs: {}", platform, openId, herbInputs);
    return switch (pillRefiningService.refinePillManual(platform, openId, herbInputs)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> formatRefiningResult(result, fmt);
    };
  }

  // ===================== 统一格式化方法 =====================

  private String formatRecipeList(List<PillRecipeVO> recipes, TextFormat fmt) {
    if (recipes.isEmpty()) {
      return fmt.heading("丹方列表（空）", "📜") + "你还没有学会任何丹方。";
    }
    StringBuilder sb = new StringBuilder();
    sb.append(fmt.heading("丹方列表", "📜"));
    for (int i = 0; i < recipes.size(); i++) {
      PillRecipeVO recipe = recipes.get(i);
      sb.append(String.format("%d. %s（%d品）\n", i + 1, recipe.recipeName(), recipe.grade()));
    }
    sb.append("\n输入「丹方 [名称]」查看详情");
    return sb.toString();
  }

  private String formatRecipeDetail(PillRecipeVO recipe, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(fmt.heading(recipe.recipeName()));
    sb.append(fmt.listItem(String.format("品阶：%d品", recipe.grade())));
    sb.append(
        fmt.listItem(String.format("成品：%s x%d", recipe.resultItemName(), recipe.resultQuantity())));
    sb.append("\n五行要求：\n");

    var requirements = recipe.requirements();
    for (Map.Entry<String, ElementRange> entry : requirements.entrySet()) {
      String elementName = getElementName(entry.getKey());
      sb.append(
          fmt.listItem(
              String.format(
                  "%s：%d~%d", elementName, entry.getValue().min(), entry.getValue().max())));
    }

    sb.append("\n输入「炼 [丹方名]」自动炼丹");
    return sb.toString();
  }

  private String formatRefiningResult(PillRefiningResultVO result, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    if (result.success()) {
      sb.append(fmt.heading("炼丹成功", "⚗️✨"));
      sb.append(fmt.listItem(String.format("成丹：%s x%d", result.pillName(), result.quantity())));
      sb.append(
          fmt.listItem(
              String.format("成色：%s", PillQuality.fromCode(result.quality()).getChineseName())));
      sb.append(
          fmt.listItem(
              String.format("效果倍率：%.1f", PillQuality.fromCode(result.quality()).getMultiplier())));

      if (result.usedHerbs() != null && !result.usedHerbs().isEmpty()) {
        sb.append("\n消耗药材：\n");
        for (Map.Entry<String, Integer> entry : result.usedHerbs().entrySet()) {
          sb.append(fmt.listItem(String.format("%s x%d", entry.getKey(), entry.getValue())));
        }
      }
    } else {
      sb.append(fmt.heading("炼丹失败", "💥"));
      sb.append(result.message());

      if (result.missingElements() != null && !result.missingElements().isEmpty()) {
        sb.append("\n缺少属性：\n");
        for (String element : result.missingElements()) {
          sb.append(fmt.listItem(getElementName(element)));
        }
      }

      if (result.usedHerbs() != null && !result.usedHerbs().isEmpty()) {
        sb.append("\n已使用药材：\n");
        for (Map.Entry<String, Integer> entry : result.usedHerbs().entrySet()) {
          sb.append(fmt.listItem(String.format("%s x%d", entry.getKey(), entry.getValue())));
        }
      }
    }
    return sb.toString();
  }

  private String getElementName(String element) {
    try {
      return ElementType.fromCode(element).getName();
    } catch (IllegalArgumentException e) {
      return element;
    }
  }

  @Override
  public String groupName() {
    return "炼丹";
  }

  @Override
  public String groupDescription() {
    return "丹方查询、炼丹";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("丹方", "查看已学会的丹方列表", "丹方"),
        new CommandEntry("丹方 {{名称}}", "查看丹方详情", "丹方 天元丹"),
        new CommandEntry("炼 {{药材输入}}", "手动炼丹（指定药材配比）", "炼 灵草×3 火灵花×2"),
        new CommandEntry("炼方 {{丹方名}}", "根据丹方自动炼丹", "炼方 天元丹"));
  }
}
