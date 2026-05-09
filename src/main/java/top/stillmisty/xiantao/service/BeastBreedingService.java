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
import top.stillmisty.xiantao.domain.fudi.enums.BeastQuality;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
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
  private final StackableItemService stackableItemService;
  private final ItemResolver itemResolver;
  private final FudiHelper fudiHelper;
  private final BeastSkillService beastSkillService;
  private final BeastDisplayHelper beastDisplayHelper;
  private final BeastEvolutionService beastEvolutionService;
  private final BeastMutationService beastMutationService;

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
    stackableItemService.reduceStackableItem(userId, stackableItem.getId(), 1);

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
        stackableItemService.reduceStackableItem(userId, stackableItem.getId(), 1);
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
        fudiHelper.findAndTouchFudi(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));

    FudiCell cell =
        fudiCellRepository
            .findByFudiIdAndCellId(fudi.getId(), cellId)
            .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

    validateCellForHatch(cell, cellId);

    HatchSetup setup = prepareHatchSetup(userId, eggTemplate, cell.getCellLevel());
    LocalDateTime now = LocalDateTime.now();

    Beast beast =
        createBeastFromTemplate(
            userId,
            fudi.getId(),
            eggTemplate,
            setup.tier,
            setup.quality,
            setup.isMutant,
            setup.mutationTraits,
            setup.beastName,
            cellId,
            now);
    beastSkillService.unlockInnateSkills(beast, "birth");
    beastRepository.save(beast);

    configurePenCellForHatch(cell, beast, eggTemplate, setup.hatchHours, now);
    fudiCellRepository.save(cell);

    log.info(
        "用户 {} 在地块 {} 孵化 {} (T{}, {}{})",
        userId,
        cellId,
        setup.beastName,
        setup.tier,
        setup.quality.getChineseName(),
        setup.isMutant ? ", 变异" : "");

    return beastDisplayHelper.buildPenCellVO(cell);
  }

  private record HatchSetup(
      int tier,
      BeastQuality quality,
      boolean isMutant,
      List<String> mutationTraits,
      double hatchHours,
      String beastName) {}

  private HatchSetup prepareHatchSetup(Long userId, ItemTemplate eggTemplate, int cellLevel) {
    int tier =
        fudiHelper.getCropTier(eggTemplate.getGrowTime() != null ? eggTemplate.getGrowTime() : 72);
    if (cellLevel < tier) {
      throw new IllegalStateException("灵兽等阶(T%d)需要至少Lv%d兽栏".formatted(tier, tier));
    }

    int stoneCost = tier * 200 + 200;
    fudiHelper.deductSpiritStones(userId, stoneCost);

    BeastQuality quality = rollBeastQuality();
    boolean isMutant = ThreadLocalRandom.current().nextInt(100) < 5;
    List<String> mutationTraits = new ArrayList<>();
    if (isMutant) mutationTraits.add(beastMutationService.rollRandomTrait());

    double levelSpeed = fudiHelper.getLevelSpeedMultiplier(cellLevel, tier);
    double baseHatchHours = 24 + tier * 8;
    double hatchHours = baseHatchHours / levelSpeed;

    String beastName = eggTemplate.getName().replace("兽卵", "").replace("蛋", "灵兽");

    return new HatchSetup(tier, quality, isMutant, mutationTraits, hatchHours, beastName);
  }

  private void validateCellForHatch(FudiCell cell, Integer cellId) {
    if (cell.getCellType() != CellType.PEN) {
      throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
    }
    if (cell.getConfig() instanceof CellConfig.PenConfig pen && pen.beastId() != null) {
      throw new IllegalStateException("该兽栏已有灵兽，请先放生");
    }
  }

  private Beast createBeastFromTemplate(
      Long userId,
      Long fudiId,
      ItemTemplate eggTemplate,
      int tier,
      BeastQuality quality,
      boolean isMutant,
      List<String> mutationTraits,
      String beastName,
      int cellId,
      LocalDateTime now) {
    int maxHp = tier * 200;
    Beast beast = new Beast();
    beast.setUserId(userId);
    beast.setFudiId(fudiId);
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
  public ReleaseBeastVO releaseBeast(Long userId, String position) {
    Integer cellId = fudiHelper.parseCellId(position);
    Fudi fudi =
        fudiHelper.findAndTouchFudi(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));

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
        fudiHelper.findAndTouchFudi(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));

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
    stackableItemService.reduceStackableItem(userId, stoneItem.getId(), stoneCount);

    if ("升品".equals(mode)) {
      return beastEvolutionService.breakthroughBeastQuality(fudi, cell, userId, cellId);
    } else {
      return beastEvolutionService.evolveBeastTier(fudi, cell, userId, cellId);
    }
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
}
