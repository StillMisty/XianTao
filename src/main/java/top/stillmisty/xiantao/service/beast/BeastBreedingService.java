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
import top.stillmisty.xiantao.domain.beast.entity.MutationTraitConfig;
import top.stillmisty.xiantao.domain.beast.enums.BeastGender;
import top.stillmisty.xiantao.domain.beast.enums.MutationEffectType;
import top.stillmisty.xiantao.domain.beast.vo.ReleaseBeastVO;
import top.stillmisty.xiantao.domain.fudi.entity.CellConfig;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.enums.BeastQuality;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.infrastructure.repository.BeastRepository;
import top.stillmisty.xiantao.infrastructure.repository.BeastTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.BreedingRecipeRepository;
import top.stillmisty.xiantao.infrastructure.repository.FudiCellRepository;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.MutationTraitConfigRepository;
import top.stillmisty.xiantao.infrastructure.repository.SpiritRepository;
import top.stillmisty.xiantao.infrastructure.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.fudi.FudiHelper;
import top.stillmisty.xiantao.service.inventory.ItemResolver;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

/** 灵兽孵化、放生、进化、变异 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeastBreedingService {

  private final FudiCellRepository fudiCellRepository;
  private final BeastRepository beastRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final BeastTemplateRepository beastTemplateRepository;
  private final StackableItemRepository stackableItemRepository;
  private final StackableItemService stackableItemService;
  private final ItemResolver itemResolver;
  private final FudiHelper fudiHelper;
  private final SpiritStoneService spiritStoneService;
  private final SpiritRepository spiritRepository;
  private final BeastSkillService beastSkillService;
  private final BeastDisplayHelper beastDisplayHelper;
  private final BeastEvolutionService beastEvolutionService;
  private final BeastMutationService beastMutationService;
  private final BreedingRecipeRepository breedingRecipeRepository;
  private final MutationTraitConfigRepository traitConfigRepository;
  private final MutationEffectResolver effectResolver;

  /** 每份灵兽精华提供的修为 */
  public static final int ESSENCE_EXP_PER_UNIT = 50;

  private static final java.util.Map<String, Integer> ESSENCE_QUALITY_BONUS =
      java.util.Map.of("MORTAL", 0, "SPIRIT", 2, "IMMORTAL", 5, "SAINT", 10, "DIVINE", 20);

  // ===================== 公开 API =====================

  @Transactional
  public ServiceResult<PenCellVO> hatchBeast(Long userId, String position, String eggName) {
    return new ServiceResult.Success<>(hatchBeastInternal(userId, position, eggName));
  }

  @Transactional
  public ServiceResult<PenCellVO> hatchBeastByInput(Long userId, String position, String input) {
    return new ServiceResult.Success<>(hatchBeastByInputInternal(userId, position, input));
  }

  @Transactional
  public ServiceResult<ReleaseBeastVO> releaseBeast(Long userId, String position) {
    return new ServiceResult.Success<>(releaseBeastInternal(userId, position));
  }

  @Transactional
  public ServiceResult<PenCellVO> evolveBeast(Long userId, String position) {
    return new ServiceResult.Success<>(evolveBeastInternal(userId, position));
  }

  // ===================== 内部 API =====================

  @Transactional
  public PenCellVO hatchBeastInternal(Long userId, String position, String eggName) {
    Integer cellId = fudiHelper.parseCellId(position);

    ItemTemplate eggTemplate =
        itemTemplateRepository.findByType(ItemType.BEAST_EGG).stream()
            .filter(t -> t.getName().equals(eggName) || t.getName().contains(eggName))
            .min(java.util.Comparator.comparing(ItemTemplate::getName))
            .orElseThrow(() -> new BusinessException(BEAST_EGG_NOT_FOUND, eggName));

    var stackableItem =
        stackableItemRepository
            .findByUserIdAndTemplateId(userId, eggTemplate.getId())
            .orElseThrow(() -> new BusinessException(BEAST_EGG_NOT_IN_INVENTORY, eggName));
    stackableItemService.reduceStackableItem(userId, stackableItem.getId(), 1);

    return hatchBeastWithTemplate(userId, cellId, eggTemplate);
  }

  @Transactional
  public PenCellVO hatchBeastByInputInternal(Long userId, String position, String input) {
    Integer cellId = fudiHelper.parseCellId(position);
    var result = itemResolver.resolveEgg(userId, input);
    return switch (result) {
      case ItemResolver.Found(var template, var _) -> {
        var stackableItem =
            stackableItemRepository
                .findByUserIdAndTemplateId(userId, template.getId())
                .orElseThrow(
                    () -> new BusinessException(BEAST_EGG_NOT_IN_INVENTORY, template.getName()));
        stackableItemService.reduceStackableItem(userId, stackableItem.getId(), 1);
        yield hatchBeastWithTemplate(userId, cellId, template);
      }
      case ItemResolver.NotFound(var name) ->
          throw new BusinessException(BEAST_EGG_NOT_IN_INVENTORY, name);
      case ItemResolver.Ambiguous(var name, _) ->
          throw new BusinessException(ITEM_MULTIPLE_MATCH, name);
    };
  }

  @Transactional
  PenCellVO hatchBeastWithTemplate(Long userId, Integer cellId, ItemTemplate eggTemplate) {
    Fudi fudi =
        fudiHelper
            .findAndTouchFudi(userId)
            .orElseThrow(() -> new BusinessException(FUDI_NOT_FOUND));

    FudiCell cell =
        fudiCellRepository
            .findByFudiIdAndCellId(fudi.getId(), cellId)
            .orElseThrow(() -> new BusinessException(CELL_NOT_FOUND, cellId));

    validateCellForHatch(cell, cellId);

    HatchSetup setup = prepareHatchSetup(userId, eggTemplate, cell.getCellLevel(), fudi);
    LocalDateTime now = LocalDateTime.now();

    Beast beast =
        createBeastFromTemplate(
            userId,
            fudi.getId(),
            eggTemplate,
            setup.tier,
            setup.quality,
            setup.beastName,
            cellId,
            now);
    beastMutationService.attemptMutation(beast, 5);
    beastSkillService.unlockInnateSkills(beast, "birth");
    beastRepository.save(beast);

    configurePenCellForHatch(cell, beast, eggTemplate, setup.hatchHours, now);
    fudiCellRepository.save(cell);

    log.info(
        "玩家 {} 在地块 {} 孵化 {}（{} {}）{}",
        userId,
        cellId,
        setup.beastName,
        Beast.getTierName(setup.tier),
        setup.quality.getChineseName(),
        beast.isMutant() ? ", 变异" : "");

    return beastDisplayHelper.buildPenCellVO(cell);
  }

  private record HatchSetup(int tier, BeastQuality quality, double hatchHours, String beastName) {}

  private HatchSetup prepareHatchSetup(
      Long userId, ItemTemplate eggTemplate, int cellLevel, Fudi fudi) {
    int tier = 1;

    int stoneCost = 400;
    spiritStoneService.withdraw(userId, stoneCost);

    var spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
    int affection = spirit != null && spirit.getAffection() != null ? spirit.getAffection() : 0;
    BeastQuality quality = rollBeastQuality(affection, cellLevel);

    double levelSpeed = fudiHelper.getLevelSpeedMultiplier(cellLevel, tier);
    double baseHatchHours = 32;
    double hatchHours = baseHatchHours / levelSpeed;

    String beastName = eggTemplate.getName().replace("兽卵", "").replace("蛋", "灵兽");

    return new HatchSetup(tier, quality, hatchHours, beastName);
  }

  private void validateCellForHatch(FudiCell cell, Integer cellId) {
    if (cell.getCellType() != CellType.PEN) {
      throw new BusinessException(CELL_NOT_PEN, cellId);
    }
    if (cell.getConfig() instanceof CellConfig.PenConfig pen && pen.beastId() != null) {
      throw new BusinessException(BEAST_PEN_OCCUPIED);
    }
  }

  private Beast createBeastFromTemplate(
      Long userId,
      Long fudiId,
      ItemTemplate eggTemplate,
      int tier,
      BeastQuality quality,
      String beastName,
      int cellId,
      LocalDateTime now) {
    int maxHp = tier * 200;
    Beast beast = new Beast();
    beast.setUserId(userId);
    beast.setFudiId(fudiId);
    beast.setTemplateId(eggTemplate.getId());
    beast.setBeastName(beastName);
    beast.setGender(ThreadLocalRandom.current().nextBoolean() ? BeastGender.YIN : BeastGender.YANG);
    beast.setTier(tier);
    beast.setQuality(quality);
    beast.setMutationTraits(new java.util.LinkedHashSet<>());
    beast.setLevel(1);
    beast.setExp(0);
    beast.setAttack(BeastCombatService.calculateBeastAttack(1, quality));
    beast.setDefense(BeastCombatService.calculateBeastDefense(1, quality));
    beast.setMaxHp(maxHp);
    beast.setHpCurrent(maxHp);
    beast.setSkills(List.of());
    beast.setIsDeployed(false);
    beast.setRecoveryUntil(null);
    beast.setPennedCellId(cellId);
    beast.setBirthTime(now);
    return beast;
  }

  private void configurePenCellForHatch(
      FudiCell cell, Beast beast, ItemTemplate eggTemplate, double hatchHours, LocalDateTime now) {
    cell.setConfig(
        new CellConfig.PenConfig(
            beast.getId(),
            eggTemplate.getId().intValue(),
            now,
            now.plusHours((long) hatchHours),
            new ArrayList<>(),
            now));
  }

  @Transactional
  public ReleaseBeastVO releaseBeastInternal(Long userId, String position) {
    var pcb = beastDisplayHelper.findPenCell(userId, position, false, false);
    var cell = pcb.cell();
    var beast = pcb.beast();
    String beastName = beast != null ? beast.getBeastName() : "未知灵兽";
    int tier = beast != null ? beast.getTier() : 1;
    String qualityStr =
        beast != null ? beast.getQuality().getCode() : BeastQuality.MORTAL.getCode();

    beastDisplayHelper.clearBeastCell(cell);
    if (beast != null) {
      beastRepository.deleteById(beast.getId());
    }

    int essenceAmount = tier * 5 + ESSENCE_QUALITY_BONUS.getOrDefault(qualityStr, 0);
    if (essenceAmount > 0) {
      itemTemplateRepository
          .findByName("灵兽精华")
          .ifPresent(
              template ->
                  stackableItemService.addStackableItem(
                      userId,
                      template.getId(),
                      ItemType.BEAST_ESSENCE,
                      "灵兽精华",
                      essenceAmount,
                      java.util.Map.of()));
    }

    log.info(
        "玩家 {} 放生 {}（{} {}），获得 {} 份灵兽精华",
        userId,
        beastName,
        Beast.getTierName(tier),
        qualityStr,
        essenceAmount);

    return new ReleaseBeastVO(beastName, tier, qualityStr, essenceAmount);
  }

  @Transactional
  public PenCellVO evolveBeastInternal(Long userId, String position) {
    var pcb = beastDisplayHelper.findPenCell(userId, position, true, false);
    var fudi = pcb.fudi();
    var cell = pcb.cell();
    var cellId = pcb.cellId();

    return beastEvolutionService.evolveBeastTier(fudi, cell, userId, cellId);
  }

  public BeastQuality rollBeastQuality(int affection, int cellLevel) {
    int[] weights = new int[BeastQuality.values().length];
    for (int i = 0; i < weights.length; i++) {
      weights[i] = BeastQuality.values()[i].getHatchWeight();
    }

    int cellBonus = cellLevel * 10;
    int transfer = Math.min(cellBonus, weights[0]);
    weights[0] -= transfer;
    weights[4] += transfer;

    int floor = affection / 100;
    for (int i = 0; i < floor && i < weights.length - 1; i++) {
      weights[i + 1] += weights[i];
      weights[i] = 0;
    }

    int total = 0;
    for (int w : weights) total += w;
    int roll = ThreadLocalRandom.current().nextInt(total);
    int cumulative = 0;
    BeastQuality[] values = BeastQuality.values();
    for (int i = 0; i < values.length; i++) {
      cumulative += weights[i];
      if (roll < cumulative) return values[i];
    }
    return BeastQuality.MORTAL;
  }

  // ===================== 灵兽繁育 =====================

  private static final List<String> RARITY_TAGS =
      List.of("beast_egg", "common", "uncommon", "rare", "epic", "legendary");
  private static final int BREED_STONE_COST_BASE = 200;
  private static final int BREED_COOLDOWN_HOURS = 24;
  private static final long MIN_BREED_COOLDOWN_HOURS = 1;
  private static final double TRAIT_INHERIT_CHANCE = 0.25;
  private static final double TRAIT_INHERIT_CHANCE_AWAKEN = 0.50;
  private static final int MAX_INHERITED_TRAITS = 3;

  @Transactional
  public BreedResult breed(Long userId, String position1, String position2) {
    Beast beast1 = findAdultBeastForBreed(userId, position1);
    Beast beast2 = findAdultBeastForBreed(userId, position2);

    validateBreedPair(beast1, beast2);

    int cost = BREED_STONE_COST_BASE + (beast1.getTier() + beast2.getTier()) * 50;
    spiritStoneService.withdraw(userId, cost);

    ItemTemplate eggTemplate = resolveOffspring(beast1, beast2);
    BeastQuality offspringQuality = rollOffspringQuality(beast1, beast2);
    List<Long> inheritedTraits = rollInheritedTraits(beast1, beast2);

    stackableItemService.addStackableItem(
        userId,
        eggTemplate.getId(),
        ItemType.BEAST_EGG,
        eggTemplate.getName(),
        1,
        java.util.Map.of());

    LocalDateTime now = LocalDateTime.now();
    double cooldownReduce =
        effectResolver.sumEffectValue(beast1, MutationEffectType.BREED_COOLDOWN_REDUCE)
            + effectResolver.sumEffectValue(beast2, MutationEffectType.BREED_COOLDOWN_REDUCE);
    long cooldownHours = (long) (BREED_COOLDOWN_HOURS * (1 - Math.min(cooldownReduce, 100) / 100));
    cooldownHours = Math.max(cooldownHours, MIN_BREED_COOLDOWN_HOURS);

    beast1.setBreedingCooldownUntil(now.plusHours(cooldownHours));
    beast2.setBreedingCooldownUntil(now.plusHours(cooldownHours));
    beastRepository.save(beast1);
    beastRepository.save(beast2);

    List<String> traitNames =
        inheritedTraits.stream()
            .map(
                id ->
                    traitConfigRepository
                        .findById(id)
                        .map(MutationTraitConfig::getChineseName)
                        .orElse("未知"))
            .toList();

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

  private Beast findAdultBeastForBreed(Long userId, String position) {
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
    var template1 = beastTemplateRepository.findById(parent1.getTemplateId()).orElse(null);
    var template2 = beastTemplateRepository.findById(parent2.getTemplateId()).orElse(null);

    List<String> combinedTags = new ArrayList<>();
    if (template1 != null && template1.getTags() != null) {
      combinedTags.addAll(extractCategoryTags(template1.getTags()));
    }
    if (template2 != null && template2.getTags() != null) {
      for (String tag : extractCategoryTags(template2.getTags())) {
        if (!combinedTags.contains(tag)) combinedTags.add(tag);
      }
    }

    if (!combinedTags.isEmpty()) {
      List<BreedingRecipe> recipes = breedingRecipeRepository.findMatchingRecipes(combinedTags);
      if (!recipes.isEmpty()) {
        return selectByWeight(recipes);
      }
    }

    // 无匹配配方时，随机返回父母之一的兽卵
    ItemTemplate egg1 = template1 != null ? findEggTemplate(template1.getName()) : null;
    ItemTemplate egg2 = template2 != null ? findEggTemplate(template2.getName()) : null;

    if (egg1 != null && egg2 != null) {
      return ThreadLocalRandom.current().nextBoolean() ? egg1 : egg2;
    }
    return egg1 != null ? egg1 : egg2;
  }

  private ItemTemplate findEggTemplate(String beastName) {
    return itemTemplateRepository.findByType(ItemType.BEAST_EGG).stream()
        .filter(t -> t.getName().equals(beastName + "卵"))
        .findFirst()
        .orElse(null);
  }

  private List<String> extractCategoryTags(java.util.Set<String> tags) {
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
                        ITEM_NOT_FOUND, "繁育配方结果 #" + recipe.getResultTemplateId()));
      }
    }
    return itemTemplateRepository
        .findById(recipes.get(recipes.size() - 1).getResultTemplateId())
        .orElseThrow(() -> new BusinessException(ITEM_NOT_FOUND, "繁育配方结果"));
  }

  private BeastQuality rollOffspringQuality(Beast parent1, Beast parent2) {
    double avg = (parent1.getQuality().ordinal() + parent2.getQuality().ordinal()) / 2.0;
    double roll = ThreadLocalRandom.current().nextDouble(-0.5, 1.0);

    double qualityBoost =
        effectResolver.sumEffectValue(parent1, MutationEffectType.BREED_QUALITY_BOOST)
            + effectResolver.sumEffectValue(parent2, MutationEffectType.BREED_QUALITY_BOOST);
    roll += qualityBoost / 100;

    int resultOrdinal = (int) Math.round(avg + roll);
    resultOrdinal = Math.clamp(resultOrdinal, 0, BeastQuality.values().length - 1);
    return BeastQuality.values()[resultOrdinal];
  }

  private List<Long> rollInheritedTraits(Beast parent1, Beast parent2) {
    Set<Long> allTraits = new java.util.LinkedHashSet<>();
    if (parent1.getMutationTraits() != null) allTraits.addAll(parent1.getMutationTraits());
    if (parent2.getMutationTraits() != null) allTraits.addAll(parent2.getMutationTraits());
    if (allTraits.isEmpty()) return List.of();

    double inheritBoost =
        effectResolver.sumEffectValue(parent1, MutationEffectType.INHERIT_RATE_BOOST)
            + effectResolver.sumEffectValue(parent2, MutationEffectType.INHERIT_RATE_BOOST);
    double chance =
        Math.min(TRAIT_INHERIT_CHANCE + inheritBoost / 100, TRAIT_INHERIT_CHANCE_AWAKEN);

    List<Long> inherited = new ArrayList<>();
    for (Long traitId : allTraits) {
      if (ThreadLocalRandom.current().nextDouble() < chance) {
        inherited.add(traitId);
      }
    }

    if (inherited.size() > MAX_INHERITED_TRAITS) {
      java.util.Collections.shuffle(inherited);
      return inherited.subList(0, MAX_INHERITED_TRAITS);
    }
    return inherited;
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
