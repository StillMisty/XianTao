package top.stillmisty.xiantao.service.beast;

import static top.stillmisty.xiantao.service.ErrorCode.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.entity.BreedingRecipe;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.beast.repository.BreedingRecipeRepository;
import top.stillmisty.xiantao.domain.fudi.enums.BeastQuality;
import top.stillmisty.xiantao.domain.fudi.enums.MutationTrait;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

/** 灵兽繁育服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeastBreedService {

  private static final List<String> RARITY_TAGS =
      List.of("beast_egg", "common", "uncommon", "rare", "epic", "legendary");

  private static final int BREED_STONE_COST_BASE = 200;
  private static final int BREED_COOLDOWN_HOURS = 24;
  private static final double TRAIT_INHERIT_CHANCE = 0.25;
  private static final double TRAIT_INHERIT_CHANCE_AWAKEN = 0.50;

  /** 繁育专用词条（不遗传给后代） */
  private static final Set<MutationTrait> BREEDING_ONLY_TRAITS =
      Set.of(MutationTrait.FERTILE, MutationTrait.PROLIFIC, MutationTrait.BLOOD_AWAKEN);

  private final BeastRepository beastRepository;
  private final BreedingRecipeRepository breedingRecipeRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final SpiritStoneService spiritStoneService;
  private final StackableItemService stackableItemService;
  private final BeastDisplayHelper beastDisplayHelper;

  @Transactional
  public BreedResult breed(Long userId, String position1, String position2) {
    Beast beast1 = findAdultBeast(userId, position1);
    Beast beast2 = findAdultBeast(userId, position2);

    validateBreedPair(beast1, beast2);

    int cost = BREED_STONE_COST_BASE + (beast1.getTier() + beast2.getTier()) * 50;
    spiritStoneService.withdraw(userId, cost);

    ItemTemplate eggTemplate = resolveOffspring(beast1, beast2);
    BeastQuality offspringQuality = rollOffspringQuality(beast1, beast2);
    List<MutationTrait> inheritedTraits = rollInheritedTraits(beast1, beast2);

    stackableItemService.addStackableItem(
        userId,
        eggTemplate.getId(),
        ItemType.BEAST_EGG,
        eggTemplate.getName(),
        1,
        java.util.Map.of());

    LocalDateTime now = LocalDateTime.now();
    boolean hasProlific =
        hasTrait(beast1, MutationTrait.PROLIFIC) || hasTrait(beast2, MutationTrait.PROLIFIC);
    long cooldownHours = hasProlific ? (long) (BREED_COOLDOWN_HOURS * 0.7) : BREED_COOLDOWN_HOURS;

    beast1.setBreedingCooldownUntil(now.plusHours(cooldownHours));
    beast2.setBreedingCooldownUntil(now.plusHours(cooldownHours));
    beastRepository.save(beast1);
    beastRepository.save(beast2);

    List<String> traitNames = inheritedTraits.stream().map(MutationTrait::getChineseName).toList();

    log.info(
        "玩家 {} 繁育 {}({}) + {}({}) → {}({})",
        userId,
        beast1.getBeastName(),
        beast1.getGender().getChineseName(),
        beast2.getBeastName(),
        beast2.getGender().getChineseName(),
        eggTemplate.getName(),
        offspringQuality.getChineseName());

    return new BreedResult(
        beast1.getBeastName(),
        beast1.getGender().getChineseName(),
        beast2.getBeastName(),
        beast2.getGender().getChineseName(),
        eggTemplate.getName(),
        offspringQuality.getChineseName(),
        traitNames,
        cooldownHours);
  }

  private Beast findAdultBeast(Long userId, String position) {
    var pcb = beastDisplayHelper.findPenCell(userId, position, false, true);
    Beast beast = pcb.beast();
    if (beast == null) throw new BusinessException(BEAST_NOT_FOUND);
    return beast;
  }

  private void validateBreedPair(Beast b1, Beast b2) {
    if (b1.getId().equals(b2.getId())) {
      throw new BusinessException(BEAST_BREED_SELF);
    }
    if (!b1.isAdult() || !b2.isAdult()) {
      throw new BusinessException(BEAST_NOT_ADULT);
    }
    if (b1.getGender() == b2.getGender()) {
      throw new BusinessException(BEAST_SAME_GENDER);
    }
    if (!b1.canBreed() || !b2.canBreed()) {
      throw new BusinessException(BEAST_BREED_COOLDOWN);
    }
    if (b1.needsRecovery() || b2.needsRecovery()) {
      throw new BusinessException(BEAST_IN_RECOVERY);
    }
  }

  private ItemTemplate resolveOffspring(Beast parent1, Beast parent2) {
    ItemTemplate egg1 = itemTemplateRepository.findById(parent1.getTemplateId()).orElse(null);
    ItemTemplate egg2 = itemTemplateRepository.findById(parent2.getTemplateId()).orElse(null);

    List<String> combinedTags = new ArrayList<>();
    if (egg1 != null && egg1.getTags() != null) {
      combinedTags.addAll(extractCategoryTags(egg1.getTags()));
    }
    if (egg2 != null && egg2.getTags() != null) {
      for (String tag : extractCategoryTags(egg2.getTags())) {
        if (!combinedTags.contains(tag)) combinedTags.add(tag);
      }
    }

    if (!combinedTags.isEmpty()) {
      List<BreedingRecipe> recipes = breedingRecipeRepository.findMatchingRecipes(combinedTags);
      if (!recipes.isEmpty()) {
        return selectByWeight(recipes);
      }
    }

    return ThreadLocalRandom.current().nextBoolean() && egg1 != null ? egg1 : egg2;
  }

  private List<String> extractCategoryTags(Set<String> tags) {
    return tags.stream()
        .filter(t -> !RARITY_TAGS.contains(t.toLowerCase()))
        .map(String::toLowerCase)
        .toList();
  }

  private ItemTemplate selectByWeight(List<BreedingRecipe> recipes) {
    int totalWeight =
        recipes.stream().mapToInt(r -> r.getWeight() != null ? r.getWeight() : 100).sum();
    int roll = ThreadLocalRandom.current().nextInt(totalWeight);
    int cumulative = 0;
    for (BreedingRecipe recipe : recipes) {
      cumulative += recipe.getWeight() != null ? recipe.getWeight() : 100;
      if (roll < cumulative) {
        return itemTemplateRepository
            .findById(recipe.getResultTemplateId())
            .orElseThrow(
                () ->
                    new BusinessException(
                        ErrorCode.ITEM_NOT_FOUND, "繁育配方结果 #" + recipe.getResultTemplateId()));
      }
    }
    return itemTemplateRepository
        .findById(recipes.get(recipes.size() - 1).getResultTemplateId())
        .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND, "繁育配方结果"));
  }

  private BeastQuality rollOffspringQuality(Beast parent1, Beast parent2) {
    double avg = (parent1.getQuality().ordinal() + parent2.getQuality().ordinal()) / 2.0;
    double roll = ThreadLocalRandom.current().nextDouble(-0.5, 1.0);

    if (hasTrait(parent1, MutationTrait.FERTILE) || hasTrait(parent2, MutationTrait.FERTILE)) {
      roll += 0.3;
    }

    int resultOrdinal = (int) Math.round(avg + roll);
    resultOrdinal = Math.clamp(resultOrdinal, 0, BeastQuality.values().length - 1);
    return BeastQuality.values()[resultOrdinal];
  }

  private List<MutationTrait> rollInheritedTraits(Beast parent1, Beast parent2) {
    List<MutationTrait> allTraits = new ArrayList<>();
    collectTraits(parent1, allTraits);
    collectTraits(parent2, allTraits);
    if (allTraits.isEmpty()) return List.of();

    boolean hasAwaken =
        hasTrait(parent1, MutationTrait.BLOOD_AWAKEN)
            || hasTrait(parent2, MutationTrait.BLOOD_AWAKEN);
    double chance = hasAwaken ? TRAIT_INHERIT_CHANCE_AWAKEN : TRAIT_INHERIT_CHANCE;

    List<MutationTrait> inherited = new ArrayList<>();
    for (MutationTrait trait : allTraits) {
      if (BREEDING_ONLY_TRAITS.contains(trait)) continue;
      if (ThreadLocalRandom.current().nextDouble() < chance) {
        inherited.add(trait);
      }
    }

    if (inherited.size() > 3) {
      java.util.Collections.shuffle(inherited);
      return inherited.subList(0, 3);
    }
    return inherited;
  }

  private void collectTraits(Beast beast, List<MutationTrait> into) {
    if (beast.getMutationTraits() == null) return;
    for (String code : beast.getMutationTraits()) {
      try {
        MutationTrait trait = MutationTrait.fromCode(code);
        if (!into.contains(trait)) into.add(trait);
      } catch (IllegalArgumentException ignored) {
        // skip unknown trait codes
      }
    }
  }

  private boolean hasTrait(Beast beast, MutationTrait trait) {
    if (beast.getMutationTraits() == null) return false;
    return beast.getMutationTraits().contains(trait.getCode());
  }

  public record BreedResult(
      String parent1Name,
      String parent1Gender,
      String parent2Name,
      String parent2Gender,
      String offspringEggName,
      String offspringQuality,
      List<String> inheritedTraits,
      long cooldownHours) {}
}
