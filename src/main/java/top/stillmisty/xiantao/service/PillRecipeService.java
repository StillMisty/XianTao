package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.pill.entity.PlayerPillRecipe;
import top.stillmisty.xiantao.domain.pill.repository.PlayerPillRecipeRepository;
import top.stillmisty.xiantao.domain.pill.vo.PillRecipeVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.annotation.Authenticated;

import java.util.List;
import java.util.Objects;

/**
 * 丹方服务
 * 处理：丹方学习、已学列表、详情查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PillRecipeService {

    private final ItemTemplateRepository itemTemplateRepository;
    private final StackableItemRepository stackableItemRepository;
    private final PlayerPillRecipeRepository playerPillRecipeRepository;

    // ===================== 公开 API（含认证） =====================

    @Authenticated
    public ServiceResult<List<PillRecipeVO>> getLearnedRecipes(PlatformType platform, String openId) {
        Long userId = UserContext.getCurrentUserId();
        return new ServiceResult.Success<>(getLearnedRecipes(userId));
    }

    @Authenticated
    public ServiceResult<PillRecipeVO> getRecipeDetail(PlatformType platform, String openId, String recipeName) {
        Long userId = UserContext.getCurrentUserId();
        return new ServiceResult.Success<>(getRecipeDetail(userId, recipeName));
    }

    @Authenticated
    @Transactional
    public ServiceResult<PillRecipeVO> learnRecipe(PlatformType platform, String openId, String recipeName) {
        Long userId = UserContext.getCurrentUserId();
        return new ServiceResult.Success<>(learnRecipe(userId, recipeName));
    }

    // ===================== 内部 API =====================

    public List<PillRecipeVO> getLearnedRecipes(Long userId) {
        List<PlayerPillRecipe> recipes = playerPillRecipeRepository.findByUserId(userId);
        return recipes.stream()
                .map(recipe -> {
                    ItemTemplate recipeTemplate = itemTemplateRepository.findById(recipe.getRecipeTemplateId()).orElse(null);
                    ItemTemplate resultTemplate = itemTemplateRepository.findById(recipe.getResultItemId()).orElse(null);
                    if (recipeTemplate == null || resultTemplate == null) return null;
                    return convertToPillRecipeVO(recipe, recipeTemplate, resultTemplate);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public PillRecipeVO getRecipeDetail(Long userId, String recipeName) {
        List<PlayerPillRecipe> recipes = playerPillRecipeRepository.findByUserId(userId);
        for (PlayerPillRecipe recipe : recipes) {
            ItemTemplate recipeTemplate = itemTemplateRepository.findById(recipe.getRecipeTemplateId()).orElse(null);
            if (recipeTemplate != null && recipeTemplate.getName().contains(recipeName)) {
                ItemTemplate resultTemplate = itemTemplateRepository.findById(recipe.getResultItemId()).orElse(null);
                if (resultTemplate != null) {
                    return convertToPillRecipeVO(recipe, recipeTemplate, resultTemplate);
                }
            }
        }
        return null;
    }

    @Transactional
    public PillRecipeVO learnRecipe(Long userId, String recipeName) {
        List<StackableItem> items = stackableItemRepository.findByUserId(userId);
        StackableItem recipeItem = null;
        for (StackableItem item : items) {
            if (item.getItemType() == ItemType.RECIPE_SCROLL && item.getName().contains(recipeName)) {
                recipeItem = item;
                break;
            }
        }

        if (recipeItem == null) return null;

        ItemTemplate recipeTemplate = itemTemplateRepository.findById(recipeItem.getTemplateId()).orElse(null);
        if (recipeTemplate == null) return null;

        if (playerPillRecipeRepository.existsByUserIdAndRecipeTemplateId(userId, recipeTemplate.getId())) {
            return null;
        }

        var recipeScroll = getRecipeScroll(recipeTemplate);
        if (recipeScroll == null) return null;
        long resultItemId = recipeScroll.product().itemId();

        PlayerPillRecipe recipe = PlayerPillRecipe.create(userId, recipeTemplate.getId(), resultItemId);
        playerPillRecipeRepository.save(recipe);

        ItemTemplate resultTemplate = itemTemplateRepository.findById(resultItemId).orElse(null);
        if (resultTemplate == null) return null;

        return convertToPillRecipeVO(recipe, recipeTemplate, resultTemplate);
    }

    // ===================== 辅助方法 =====================

    private static ItemProperties.Scroll getRecipeScroll(ItemTemplate template) {
        var props = template.typedProperties();
        if (props instanceof ItemProperties.Scroll s) return s;
        return null;
    }

    private PillRecipeVO convertToPillRecipeVO(PlayerPillRecipe recipe, ItemTemplate recipeTemplate,
            ItemTemplate resultTemplate) {
        var recipeScroll = getRecipeScroll(recipeTemplate);
        if (recipeScroll == null) {
            return new PillRecipeVO(recipe.getRecipeTemplateId(), recipeTemplate.getName(), 0,
                    recipe.getResultItemId(), resultTemplate.getName(), 0, List.of());
        }
        return new PillRecipeVO(
                recipe.getRecipeTemplateId(),
                recipeTemplate.getName(),
                recipeScroll.grade(),
                recipe.getResultItemId(),
                resultTemplate.getName(),
                recipeScroll.product().quantity(),
                recipeScroll.requirements()
        );
    }
}
