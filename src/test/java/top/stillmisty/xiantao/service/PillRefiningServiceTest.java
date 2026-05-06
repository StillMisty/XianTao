package top.stillmisty.xiantao.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.pill.entity.PlayerPillRecipe;
import top.stillmisty.xiantao.domain.pill.repository.PlayerPillRecipeRepository;
import top.stillmisty.xiantao.domain.pill.vo.PillRefiningResultVO;

@DisplayName("PillRefiningService 测试")
@ExtendWith(MockitoExtension.class)
class PillRefiningServiceTest {

  @Mock private ItemTemplateRepository itemTemplateRepository;
  @Mock private StackableItemRepository stackableItemRepository;
  @Mock private PlayerPillRecipeRepository playerPillRecipeRepository;
  @Mock private StackableItemService stackableItemService;

  private PillCombinationFinder combinationFinder;
  private PillRefiningService pillRefiningService;

  private final Long userId = 1L;

  @BeforeEach
  void setUp() {
    combinationFinder =
        new PillCombinationFinder(
            stackableItemService, itemTemplateRepository, stackableItemRepository);
    pillRefiningService =
        new PillRefiningService(
            itemTemplateRepository,
            stackableItemRepository,
            playerPillRecipeRepository,
            stackableItemService,
            combinationFinder);
  }

  // ===================== refinePillAuto =====================

  @Test
  @DisplayName("refinePillAuto — 未找到丹方返回失败")
  void refinePillAuto_whenRecipeNotFound_shouldReturnFailure() {
    when(playerPillRecipeRepository.findByUserId(userId)).thenReturn(List.of());

    PillRefiningResultVO result = pillRefiningService.refinePillAuto(userId, "养气丹");

    assertFalse(result.success());
    assertTrue(result.message().contains("未找到丹方"));
  }

  @Test
  @DisplayName("refinePillAuto — 丹方匹配成功但无药材")
  void refinePillAuto_whenNoHerbs_shouldReturnFailure() {
    PlayerPillRecipe recipe = new PlayerPillRecipe();
    recipe.setRecipeTemplateId(1L);

    ItemTemplate recipeTemplate = new ItemTemplate();
    recipeTemplate.setId(1L);
    recipeTemplate.setName("养气丹方");
    recipeTemplate.setType(ItemType.RECIPE_SCROLL);
    recipeTemplate.setProperties(
        Map.of(
            "grade", 0,
            "product", Map.of("item_id", 100L, "quantity", 1),
            "requirements", List.of(Map.of("element", "FIRE", "min", 3, "max", 6))));

    when(playerPillRecipeRepository.findByUserId(userId)).thenReturn(List.of(recipe));
    when(itemTemplateRepository.findById(1L)).thenReturn(Optional.of(recipeTemplate));
    when(stackableItemRepository.findByUserId(userId)).thenReturn(List.of());

    PillRefiningResultVO result = pillRefiningService.refinePillAuto(userId, "养气丹");

    assertFalse(result.success());
    assertTrue(result.message().contains("没有药材"));
  }

  @Test
  @DisplayName("refinePillAuto — 药材不足五行返回失败")
  void refinePillAuto_whenElementsInsufficient_shouldReturnFailure() {
    PlayerPillRecipe recipe = new PlayerPillRecipe();
    recipe.setRecipeTemplateId(1L);

    ItemTemplate recipeTemplate = new ItemTemplate();
    recipeTemplate.setId(1L);
    recipeTemplate.setName("养气丹方");
    recipeTemplate.setType(ItemType.RECIPE_SCROLL);
    recipeTemplate.setProperties(
        Map.of(
            "grade", 1,
            "product", Map.of("item_id", 100L, "quantity", 1),
            "requirements",
                List.of(
                    Map.of("element", "FIRE", "min", 3, "max", 6),
                    Map.of("element", "WATER", "min", 2, "max", 4))));

    StackableItem herb = new StackableItem();
    herb.setId(1L);
    herb.setName("火焰草");
    herb.setItemType(ItemType.HERB);
    herb.setQuantity(10);
    herb.setTemplateId(50L);
    herb.setProperties(Map.of("elements", Map.of("FIRE", 3)));

    when(playerPillRecipeRepository.findByUserId(userId)).thenReturn(List.of(recipe));
    when(itemTemplateRepository.findById(1L)).thenReturn(Optional.of(recipeTemplate));
    when(stackableItemRepository.findByUserId(userId)).thenReturn(List.of(herb));

    PillRefiningResultVO result = pillRefiningService.refinePillAuto(userId, "养气丹");

    assertFalse(result.success());
    assertTrue(result.message().contains("缺少药材属性"));
  }

  // ===================== refinePillManual =====================

  @Test
  @DisplayName("refinePillManual — 格式错误返回失败")
  void refinePillManual_whenInvalidFormat_shouldReturnFailure() {
    PillRefiningResultVO result = pillRefiningService.refinePillManual(userId, List.of("invalid"));

    assertFalse(result.success());
    assertTrue(result.message().contains("格式错误"));
  }

  @Test
  @DisplayName("refinePillManual — 药材不存在返回空解析")
  void refinePillManual_whenHerbNotFound_shouldReturnEmptyParsed() {
    when(stackableItemRepository.findByUserId(userId)).thenReturn(List.of());

    PillRefiningResultVO result = pillRefiningService.refinePillManual(userId, List.of("不存在的药材x5"));

    assertFalse(result.success());
    assertTrue(result.message().contains("格式错误"));
  }

  // ===================== quality calculation =====================

  @Test
  @DisplayName("refinePillAuto — 完美匹配不抛异常")
  void refinePillAuto_withPerfectMatch_shouldNotThrow() {
    PlayerPillRecipe recipe = new PlayerPillRecipe();
    recipe.setRecipeTemplateId(1L);

    ItemTemplate recipeTemplate = new ItemTemplate();
    recipeTemplate.setId(1L);
    recipeTemplate.setName("完美丹方");
    recipeTemplate.setType(ItemType.RECIPE_SCROLL);
    recipeTemplate.setProperties(
        Map.of(
            "grade", 1,
            "product", Map.of("item_id", 100L, "quantity", 1),
            "requirements", List.of(Map.of("element", "FIRE", "min", 3, "max", 3))));

    StackableItem herb = new StackableItem();
    herb.setId(1L);
    herb.setName("火焰草");
    herb.setItemType(ItemType.HERB);
    herb.setQuantity(1);
    herb.setTemplateId(50L);
    herb.setProperties(Map.of("elements", Map.of("FIRE", 3)));

    ItemTemplate resultTemplate = new ItemTemplate();
    resultTemplate.setId(100L);
    resultTemplate.setName("完美丹药");
    resultTemplate.setType(ItemType.HERB);

    when(playerPillRecipeRepository.findByUserId(userId)).thenReturn(List.of(recipe));
    when(itemTemplateRepository.findById(1L)).thenReturn(Optional.of(recipeTemplate));
    when(stackableItemRepository.findByUserId(userId)).thenReturn(List.of(herb));
    when(itemTemplateRepository.findById(100L)).thenReturn(Optional.of(resultTemplate));

    PillRefiningResultVO result = pillRefiningService.refinePillAuto(userId, "完美丹方");

    assertNotNull(result);
  }
}
