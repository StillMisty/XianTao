package top.stillmisty.xiantao.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.beast.vo.ReleaseBeastVO;
import top.stillmisty.xiantao.domain.fudi.entity.CellConfig;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.domain.fudi.enums.BeastQuality;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.enums.MutationTrait;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;

/** 灵兽孵化、放生、进化、品质突破、变异 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeastBreedingService {

  private final FudiCellRepository fudiCellRepository;
  private final BeastRepository beastRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemRepository stackableItemRepository;
  private final SpiritRepository spiritRepository;
  private final StackableItemService stackableItemService;
  private final ItemResolver itemResolver;
  private final FudiHelper fudiHelper;
  private final BeastSkillService beastSkillService;
  private final BeastDisplayHelper beastDisplayHelper;

  @Transactional
  public PenCellVO hatchBeast(Long userId, String position, String eggName) {
    Integer cellId = fudiHelper.parseCellId(position);

    ItemTemplate eggTemplate =
        itemTemplateRepository.findByType(ItemType.BEAST_EGG).stream()
            .filter(t -> t.getName().equals(eggName) || t.getName().contains(eggName))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("未找到兽卵: %s".formatted(eggName)));

    var stackableItem =
        stackableItemRepository
            .findByUserIdAndTemplateId(userId, eggTemplate.getId())
            .orElseThrow(() -> new IllegalStateException("背包中没有 [%s]".formatted(eggName)));
    stackableItemService.reduceStackableItem(userId, eggTemplate.getId(), 1);

    return hatchBeastWithTemplate(userId, cellId, eggTemplate);
  }

  @Transactional
  public PenCellVO hatchBeastByInput(Long userId, String position, String input) {
    Integer cellId = fudiHelper.parseCellId(position);
    var result = itemResolver.resolveEgg(userId, input);
    return switch (result) {
      case ItemResolver.Found(var template, var index) -> {
        var stackableItem =
            stackableItemRepository
                .findByUserIdAndTemplateId(userId, template.getId())
                .orElseThrow(() -> new IllegalStateException("背包中没有 [" + template.getName() + "]"));
        stackableItemService.reduceStackableItem(userId, template.getId(), 1);
        yield hatchBeastWithTemplate(userId, cellId, template);
      }
      case ItemResolver.NotFound(var name) ->
          throw new IllegalStateException("背包中未找到兽卵 [" + name + "]");
      case ItemResolver.Ambiguous(var name, var candidates) -> {
        var sb = new StringBuilder("找到多个兽卵，请使用编号：\n");
        for (var e : candidates) {
          sb.append(e.index())
              .append(". ")
              .append(e.name())
              .append(" x")
              .append(e.quantity())
              .append(" (")
              .append(e.metadata())
              .append(")\n");
        }
        throw new IllegalStateException(sb.toString().strip());
      }
    };
  }

  @Transactional
  PenCellVO hatchBeastWithTemplate(Long userId, Integer cellId, ItemTemplate eggTemplate) {
    Fudi fudi =
        fudiHelper.getFudiByUserId(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));

    FudiCell cell =
        fudiCellRepository
            .findByFudiIdAndCellId(fudi.getId(), cellId)
            .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

    if (cell.getCellType() != CellType.PEN) {
      throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
    }
    if (cell.getConfig() instanceof CellConfig.PenConfig pen && pen.beastId() != null) {
      throw new IllegalStateException("该兽栏已有灵兽，请先放生");
    }

    int tier =
        fudiHelper.getCropTier(eggTemplate.getGrowTime() != null ? eggTemplate.getGrowTime() : 72);
    int cellLevel = cell.getCellLevel();
    if (cellLevel < tier) {
      throw new IllegalStateException("灵兽等阶(T%d)需要至少Lv%d兽栏".formatted(tier, tier));
    }

    int stoneCost = tier * 200 + 200;
    fudiHelper.checkSpiritStones(userId, stoneCost);
    fudiHelper.deductSpiritStones(userId, stoneCost);

    BeastQuality quality = rollBeastQuality();
    boolean isMutant = ThreadLocalRandom.current().nextInt(100) < 5;
    List<String> mutationTraits = new ArrayList<>();
    if (isMutant) mutationTraits.add(rollRandomTrait());

    double levelSpeed = fudiHelper.getLevelSpeedMultiplier(cellLevel, tier);
    double baseHatchHours = 24 + tier * 8;
    double hatchHours = baseHatchHours / levelSpeed;

    String beastName = eggTemplate.getName().replace("兽卵", "").replace("蛋", "灵兽");
    LocalDateTime now = LocalDateTime.now();

    Beast beast = new Beast();
    beast.setUserId(userId);
    beast.setFudiId(fudi.getId());
    beast.setTemplateId(eggTemplate.getId());
    beast.setBeastName(beastName);
    beast.setTier(tier);
    beast.setQuality(quality);
    beast.setIsMutant(isMutant);
    beast.setMutationTraits(mutationTraits);
    beast.setLevel(1);
    beast.setExp(0);
    beast.setAttack(BeastCombatService.calculateBeastAttack(1, quality));
    beast.setDefense(BeastCombatService.calculateBeastDefense(1, quality));
    int maxHp = tier * 200;
    beast.setMaxHp(maxHp);
    beast.setHpCurrent(maxHp);
    beast.setSkills(List.of());
    beast.setIsDeployed(false);
    beast.setRecoveryUntil(null);
    beast.setPennedCellId(cellId);
    beast.setBirthTime(now);
    beast.setEvolutionCount(0);
    beast.setLevelCap(tier * 10 + 10);
    beastRepository.save(beast);

    beastSkillService.unlockInnateSkills(beast, "birth");
    beastRepository.save(beast);

    cell.setConfig(
        new CellConfig.PenConfig(
            beast.getId(),
            eggTemplate.getId().intValue(),
            now,
            now.plusHours((long) hatchHours),
            new ArrayList<>(),
            now));
    fudiCellRepository.save(cell);

    log.info(
        "用户 {} 在地块 {} 孵化 {} (T{}, {}{})",
        userId,
        cellId,
        beastName,
        tier,
        quality.getChineseName(),
        isMutant ? ", 变异" : "");

    return beastDisplayHelper.buildPenCellVO(cell);
  }

  @Transactional
  public ReleaseBeastVO releaseBeast(Long userId, String position) {
    Integer cellId = fudiHelper.parseCellId(position);
    Fudi fudi =
        fudiHelper.getFudiByUserId(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));

    FudiCell cell =
        fudiCellRepository
            .findByFudiIdAndCellId(fudi.getId(), cellId)
            .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

    if (cell.getCellType() != CellType.PEN) {
      throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
    }

    Beast beast = beastDisplayHelper.findBeastByCell(cell);
    String beastName = beast != null ? beast.getBeastName() : "未知灵兽";
    int tier = beast != null ? beast.getTier() : 1;
    String qualityStr =
        beast != null ? beast.getQuality().getCode() : BeastQuality.MORTAL.getCode();

    beastDisplayHelper.clearBeastCell(cell);
    if (beast != null) {
      beastRepository.deleteById(beast.getId());
    }

    log.info("用户 {} 放生 {} (T{}/{})", userId, beastName, tier, qualityStr);

    return new ReleaseBeastVO(beastName, tier, qualityStr);
  }

  @Transactional
  public PenCellVO evolveBeast(Long userId, String position, String mode) {
    Integer cellId = fudiHelper.parseCellId(position);
    Fudi fudi =
        fudiHelper.getFudiByUserId(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));

    FudiCell cell =
        fudiCellRepository
            .findByFudiIdAndCellId(fudi.getId(), cellId)
            .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

    if (cell.getCellType() != CellType.PEN) {
      throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
    }
    if (beastDisplayHelper.isIncubating(cell)) {
      throw new IllegalStateException("灵兽尚在孵化中");
    }

    int stoneCount = "升品".equals(mode) ? 2 : 1;
    ItemTemplate stoneTemplate =
        itemTemplateRepository.findByType(ItemType.EVOLUTION_STONE).stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("进化石模板未找到"));
    var stoneItem =
        stackableItemRepository
            .findByUserIdAndTemplateId(userId, stoneTemplate.getId())
            .orElseThrow(() -> new IllegalStateException("背包中没有进化石"));
    if (stoneItem.getQuantity() < stoneCount) {
      throw new IllegalStateException(
          "需要 %d 个进化石（当前%d）".formatted(stoneCount, stoneItem.getQuantity()));
    }
    stackableItemService.reduceStackableItem(userId, stoneTemplate.getId(), stoneCount);

    if ("升品".equals(mode)) {
      return breakthroughBeastQuality(fudi, cell, userId, cellId);
    } else {
      return evolveBeastTier(fudi, cell, userId, cellId);
    }
  }

  @Transactional
  PenCellVO evolveBeastTier(Fudi fudi, FudiCell cell, Long userId, Integer cellId) {
    Beast beast = beastDisplayHelper.findBeastByCell(cell);
    if (beast == null) {
      throw new IllegalStateException("未找到灵兽");
    }

    if (beast.getTier() >= 5) {
      throw new IllegalStateException("已是最高等阶 T5");
    }

    if (beast.needsMoreLevels()) {
      throw new IllegalStateException("灵兽需要先达到等级上限才能进化");
    }

    int currentTier = beast.getTier();
    int cost = (currentTier + 1) * 200;
    fudiHelper.checkSpiritStones(userId, cost);
    fudiHelper.deductSpiritStones(userId, cost);

    Spirit spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
    int affectionBonus =
        spirit != null && spirit.getAffection() != null
            ? Math.min(15, spirit.getAffection() / 7)
            : 0;
    int successRate = 85 + affectionBonus;
    boolean success = ThreadLocalRandom.current().nextInt(100) < successRate;

    if (!success) {
      throw new IllegalStateException("进化失败！进化石和灵石已消耗");
    }

    beast.evolve();
    beast.setHpCurrent(beast.getMaxHp());

    boolean qualityUpgraded = false;
    if (ThreadLocalRandom.current().nextInt(100) < 10
        && beast.getQuality() != BeastQuality.DIVINE) {
      beast.qualityBreak();
      qualityUpgraded = true;
    }

    boolean mutationTriggered = rollMutation(15);
    if (mutationTriggered) {
      addMutationTrait(beast);
    }

    if (beast.getTier() == 2) {
      beastSkillService.unlockInnateSkills(beast, "tier_2");
    } else if (beast.getTier() == 3) {
      beastSkillService.unlockInnateSkills(beast, "tier_3");
    }

    beastRepository.save(beast);

    log.info(
        "用户 {} 进化地块 {} 的灵兽 T{}->T{}{}",
        userId,
        cellId,
        currentTier,
        beast.getTier(),
        qualityUpgraded ? " (品质连带提升!)" : "");

    return beastDisplayHelper.buildPenCellVO(cell);
  }

  @Transactional
  PenCellVO breakthroughBeastQuality(Fudi fudi, FudiCell cell, Long userId, Integer cellId) {
    Beast beast = beastDisplayHelper.findBeastByCell(cell);
    if (beast == null) {
      throw new IllegalStateException("未找到灵兽");
    }

    if (beast.getQuality() == BeastQuality.DIVINE) {
      throw new IllegalStateException("已是最高品质神品");
    }

    if (beast.needsMoreLevels()) {
      throw new IllegalStateException("灵兽需要先达到等级上限才能突破");
    }

    BeastQuality currentQuality = beast.getQuality();
    BeastQuality nextQuality = currentQuality.next();

    int cost = nextQuality.getOrder() * 300;
    fudiHelper.checkSpiritStones(userId, cost);
    fudiHelper.deductSpiritStones(userId, cost);

    Spirit spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
    int affectionBonus =
        spirit != null && spirit.getAffection() != null
            ? Math.min(20, spirit.getAffection() / 5)
            : 0;
    int successRate = 60 + affectionBonus;

    if (beast.getMutationTraits() != null && beast.getMutationTraits().contains("spiritual")) {
      successRate += 10;
    }

    boolean success = ThreadLocalRandom.current().nextInt(100) < successRate;

    if (!success) {
      throw new IllegalStateException("品质突破失败！进化石和灵石已消耗");
    }

    beast.qualityBreak();
    beast.recalculateAttributes();
    beast.setHpCurrent(beast.getMaxHp());

    boolean mutationTriggered = rollMutation(10);
    if (mutationTriggered) {
      addMutationTrait(beast);
    }

    beastSkillService.unlockInnateSkills(beast, "quality_break");
    beastRepository.save(beast);

    log.info(
        "用户 {} 品质突破地块 {} 的灵兽 {} -> {}",
        userId,
        cellId,
        currentQuality.getChineseName(),
        nextQuality.getChineseName());

    return beastDisplayHelper.buildPenCellVO(cell);
  }

  BeastQuality rollBeastQuality() {
    int roll = ThreadLocalRandom.current().nextInt(1000);
    int cumulative = 0;
    for (BeastQuality q : BeastQuality.values()) {
      cumulative += q.getHatchWeight();
      if (roll < cumulative) return q;
    }
    return BeastQuality.MORTAL;
  }

  String rollRandomTrait() {
    return MutationTrait.values()[
        ThreadLocalRandom.current().nextInt(MutationTrait.values().length)]
        .getCode();
  }

  boolean rollMutation(int chancePercent) {
    return ThreadLocalRandom.current().nextInt(100) < chancePercent;
  }

  void addMutationTrait(Beast beast) {
    List<String> traits = beast.getMutationTraits();
    if (traits == null) {
      traits = new ArrayList<>();
    }
    if (traits.size() >= 2) return;
    String newTrait = rollRandomTrait();
    if (!traits.contains(newTrait)) {
      traits.add(newTrait);
      beast.setMutationTraits(traits);
      beast.setIsMutant(true);
    }
  }
}
