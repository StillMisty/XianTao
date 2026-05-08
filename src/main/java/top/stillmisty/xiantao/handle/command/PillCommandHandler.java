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

  /** 处理丹方列表命令 */
  public String handleRecipeList(PlatformType platform, String openId) {
    log.debug("处理丹方列表查询 - Platform: {}, OpenId: {}", platform, openId);
    return switch (pillRecipeService.getLearnedRecipes(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var recipes) -> formatRecipeList(recipes);
    };
  }

  /** 处理丹方详情命令 */
  public String handleRecipeDetail(PlatformType platform, String openId, String recipeName) {
    log.debug("处理丹方详情查询 - Platform: {}, OpenId: {}, RecipeName: {}", platform, openId, recipeName);
    return switch (pillRecipeService.getRecipeDetail(platform, openId, recipeName)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var recipe) ->
          recipe != null ? formatRecipeDetail(recipe) : "未找到丹方：" + recipeName;
    };
  }

  /** 处理自动炼丹命令 */
  public String handleRefineAuto(PlatformType platform, String openId, String recipeName) {
    log.debug("处理自动炼丹 - Platform: {}, OpenId: {}, RecipeName: {}", platform, openId, recipeName);
    return switch (pillRefiningService.refinePillAuto(platform, openId, recipeName)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> formatRefiningResult(result);
    };
  }

  /** 处理手动炼丹命令 */
  public String handleRefineManual(PlatformType platform, String openId, List<String> herbInputs) {
    log.debug("处理手动炼丹 - Platform: {}, OpenId: {}, HerbInputs: {}", platform, openId, herbInputs);
    return switch (pillRefiningService.refinePillManual(platform, openId, herbInputs)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var result) -> formatRefiningResult(result);
    };
  }

  // ===================== 文本格式化方法 =====================

  private String formatRecipeList(List<PillRecipeVO> recipes) {
    if (recipes.isEmpty()) {
      return "【丹方列表】（空）\n你还没有学会任何丹方。";
    }
    StringBuilder sb = new StringBuilder("【丹方列表】\n");
    for (int i = 0; i < recipes.size(); i++) {
      PillRecipeVO recipe = recipes.get(i);
      sb.append(String.format("%d. %s（%d品）\n", i + 1, recipe.recipeName(), recipe.grade()));
    }
    sb.append("\n输入「丹方 [名称]」查看详情");
    return sb.toString();
  }

  private String formatRecipeDetail(PillRecipeVO recipe) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("【%s】\n", recipe.recipeName()));
    sb.append(String.format("品阶：%d品\n", recipe.grade()));
    sb.append(String.format("成品：%s x%d\n", recipe.resultItemName(), recipe.resultQuantity()));
    sb.append("\n五行要求：\n");

    var requirements = recipe.requirements();
    for (Map.Entry<String, ElementRange> entry : requirements.entrySet()) {
      String elementName = getElementName(entry.getKey());
      sb.append(
          String.format(
              "  %s：%d~%d\n", elementName, entry.getValue().min(), entry.getValue().max()));
    }

    sb.append("\n输入「炼 [丹方名]」自动炼丹");
    return sb.toString();
  }

  private String formatRefiningResult(PillRefiningResultVO result) {
    StringBuilder sb = new StringBuilder();
    if (result.success()) {
      sb.append("【炼丹成功】\n");
      sb.append(String.format("成丹：%s x%d\n", result.pillName(), result.quantity()));
      sb.append(String.format("成色：%s\n", PillQuality.fromCode(result.quality()).getChineseName()));
      sb.append(
          String.format("效果倍率：%.1f\n", PillQuality.fromCode(result.quality()).getMultiplier()));

      if (result.usedHerbs() != null && !result.usedHerbs().isEmpty()) {
        sb.append("\n消耗药材：\n");
        for (Map.Entry<String, Integer> entry : result.usedHerbs().entrySet()) {
          sb.append(String.format("  %s x%d\n", entry.getKey(), entry.getValue()));
        }
      }
    } else {
      sb.append("【炼丹失败】\n");
      sb.append(result.message());

      if (result.missingElements() != null && !result.missingElements().isEmpty()) {
        sb.append("\n缺少属性：\n");
        for (String element : result.missingElements()) {
          sb.append(String.format("  %s\n", getElementName(element)));
        }
      }

      if (result.usedHerbs() != null && !result.usedHerbs().isEmpty()) {
        sb.append("\n已使用药材：\n");
        for (Map.Entry<String, Integer> entry : result.usedHerbs().entrySet()) {
          sb.append(String.format("  %s x%d\n", entry.getKey(), entry.getValue()));
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
