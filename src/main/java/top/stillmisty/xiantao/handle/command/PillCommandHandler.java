package top.stillmisty.xiantao.handle.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.pill.enums.ElementType;
import top.stillmisty.xiantao.domain.pill.vo.PillRecipeVO;
import top.stillmisty.xiantao.domain.pill.vo.PillRefiningResultVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.PillRecipeService;
import top.stillmisty.xiantao.service.PillRefiningService;
import top.stillmisty.xiantao.service.ServiceResult;

import java.util.List;
import java.util.Map;

/**
 * 炼丹命令处理器（纯 View 层）
 * 调用 Service 层获取结构化数据，格式化为纯文本返回
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PillCommandHandler {

    private final PillRecipeService pillRecipeService;
    private final PillRefiningService pillRefiningService;

    /**
     * 处理丹方列表命令
     */
    public String handleRecipeList(PlatformType platform, String openId) {
        log.debug("处理丹方列表查询 - Platform: {}, OpenId: {}", platform, openId);
        return switch (pillRecipeService.getLearnedRecipes(platform, openId)) {
            case ServiceResult.Failure(var code, var msg) -> msg;
            case ServiceResult.Success(var recipes) -> formatRecipeList(recipes);
        };
    }

    /**
     * 处理丹方详情命令
     */
    public String handleRecipeDetail(PlatformType platform, String openId, String recipeName) {
        log.debug("处理丹方详情查询 - Platform: {}, OpenId: {}, RecipeName: {}", platform, openId, recipeName);
        return switch (pillRecipeService.getRecipeDetail(platform, openId, recipeName)) {
            case ServiceResult.Failure(var code, var msg) -> msg;
            case ServiceResult.Success(var recipe) -> recipe != null ? formatRecipeDetail(recipe) : "未找到丹方：" + recipeName;
        };
    }

    /**
     * 处理自动炼丹命令
     */
    public String handleRefineAuto(PlatformType platform, String openId, String recipeName) {
        log.debug("处理自动炼丹 - Platform: {}, OpenId: {}, RecipeName: {}", platform, openId, recipeName);
        return switch (pillRefiningService.refinePillAuto(platform, openId, recipeName)) {
            case ServiceResult.Failure(var code, var msg) -> msg;
            case ServiceResult.Success(var result) -> formatRefiningResult(result);
        };
    }

    /**
     * 处理手动炼丹命令
     */
    public String handleRefineManual(PlatformType platform, String openId, List<String> herbInputs) {
        log.debug("处理手动炼丹 - Platform: {}, OpenId: {}, HerbInputs: {}", platform, openId, herbInputs);
        return switch (pillRefiningService.refinePillManual(platform, openId, herbInputs)) {
            case ServiceResult.Failure(var code, var msg) -> msg;
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
        for (var req : requirements) {
            String elementName = getElementName(req.element());
            sb.append(String.format("  %s：%d~%d\n", elementName, req.min(), req.max()));
        }

        sb.append("\n输入「炼 [丹方名]」自动炼丹");
        return sb.toString();
    }

    private String formatRefiningResult(PillRefiningResultVO result) {
        StringBuilder sb = new StringBuilder();
        if (result.success()) {
            sb.append("【炼丹成功】\n");
            sb.append(String.format("成丹：%s x%d\n", result.pillName(), result.quantity()));
            sb.append(String.format("成色：%s\n", getQualityName(result.quality())));
            sb.append(String.format("效果倍率：%.1f\n", getQualityMultiplier(result.quality())));

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
        ElementType type = ElementType.fromCode(element);
        return type != null ? type.getName() : element;
    }

    private String getQualityName(String quality) {
        if (quality == null) return "未知";
        return switch (quality) {
            case "superior" -> "上成";
            case "normal" -> "中成";
            case "inferior" -> "下成";
            default -> "未知";
        };
    }

    private double getQualityMultiplier(String quality) {
        if (quality == null) return 1.0;
        return switch (quality) {
            case "superior" -> 1.5;
            case "inferior" -> 0.7;
            default -> 1.0;
        };
    }
}
