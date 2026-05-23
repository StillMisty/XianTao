package top.stillmisty.xiantao.service.inventory;

import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.pill.vo.PillRecipeVO;
import top.stillmisty.xiantao.domain.skill.vo.SkillSlotResult;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.forging.ForgingService;
import top.stillmisty.xiantao.service.pill.PillConsumptionService;
import top.stillmisty.xiantao.service.pill.PillRecipeService;
import top.stillmisty.xiantao.service.skill.SkillService;

/** 物品使用服务 — 根据物品类型分发到对应领域服务 */
@Slf4j
@Service
public class ItemUseService {

  private static final Set<ItemType> INTERNAL_CONSUMPTION_TYPES = Set.of(ItemType.SKILL_JADE);

  private final StackableItemRepository stackableItemRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemService stackableItemService;
  private final PillConsumptionService pillConsumptionService;
  private final SkillService skillService;
  private final PillRecipeService pillRecipeService;
  private final ForgingService forgingService;

  public ItemUseService(
      StackableItemRepository stackableItemRepository,
      ItemTemplateRepository itemTemplateRepository,
      StackableItemService stackableItemService,
      @Lazy PillConsumptionService pillConsumptionService,
      @Lazy SkillService skillService,
      @Lazy PillRecipeService pillRecipeService,
      @Lazy ForgingService forgingService) {
    this.stackableItemRepository = stackableItemRepository;
    this.itemTemplateRepository = itemTemplateRepository;
    this.stackableItemService = stackableItemService;
    this.pillConsumptionService = pillConsumptionService;
    this.skillService = skillService;
    this.pillRecipeService = pillRecipeService;
    this.forgingService = forgingService;
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> useItem(
      PlatformType platform, String openId, String itemName, String args) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(useItem(userId, itemName, args));
  }

  @Transactional
  @CacheEvict(cacheNames = "player_inventory", allEntries = true)
  public String useItem(Long userId, String itemName, String args) {
    List<StackableItem> exactMatches =
        stackableItemRepository.findByUserIdAndName(userId, itemName);
    StackableItem matchedItem = null;
    for (StackableItem item : exactMatches) {
      ItemTemplate template = itemTemplateRepository.findById(item.getTemplateId()).orElse(null);
      if (template != null) {
        matchedItem = item;
        break;
      }
    }

    if (matchedItem == null) {
      List<StackableItem> items =
          stackableItemRepository.findByUserIdAndNameContaining(userId, itemName);
      for (StackableItem item : items) {
        ItemTemplate template = itemTemplateRepository.findById(item.getTemplateId()).orElse(null);
        if (template != null) {
          matchedItem = item;
          break;
        }
      }
    }

    if (matchedItem == null) {
      throw new BusinessException(ErrorCode.ITEM_NOT_FOUND, itemName);
    }

    ItemType type = matchedItem.getItemType();
    if (!INTERNAL_CONSUMPTION_TYPES.contains(type)) {
      stackableItemService.reduceStackableItem(userId, matchedItem.getId(), 1);
    }

    log.debug("使用物品: userId={}, item={}, type={}", userId, matchedItem.getName(), type);

    return switch (type) {
      case POTION -> pillConsumptionService.takePill(userId, matchedItem.getName());
      case SKILL_JADE -> {
        SkillSlotResult result = skillService.learnFromJade(userId, matchedItem.getName());
        StringBuilder sb = new StringBuilder();
        sb.append(result.getMessage());
        if (result.isSuccess() && result.getSkill() != null) {
          sb.append("\n习得法决：").append(result.getSkill().name());
        }
        yield sb.toString();
      }
      case RECIPE_SCROLL -> {
        PillRecipeVO recipe = pillRecipeService.learnRecipe(userId, matchedItem.getName());
        yield recipe != null ? "学习丹方成功：" + recipe.recipeName() : "学习丹方失败，请检查背包中是否有丹方卷轴";
      }
      case FORGING_BLUEPRINT -> {
        var blueprint = forgingService.learnRecipe(userId, matchedItem.getName());
        yield blueprint != null
            ? "学习锻造图纸成功：" + blueprint.blueprintName()
            : "学习锻造图纸失败，请检查背包中是否有锻造图纸";
      }
      default -> throw new BusinessException(ErrorCode.ITEM_CANNOT_USE);
    };
  }
}
