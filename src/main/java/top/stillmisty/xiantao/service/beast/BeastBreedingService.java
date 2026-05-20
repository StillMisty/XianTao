package top.stillmisty.xiantao.service.beast;

import static top.stillmisty.xiantao.service.ErrorCode.*;

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
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
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

  /** 每份灵兽精华提供的经验值 */
  public static final int ESSENCE_EXP_PER_UNIT = 50;

  private static final java.util.Map<String, Integer> ESSENCE_QUALITY_BONUS =
      java.util.Map.of(
          "MORTAL", 0,
          "SPIRIT", 2,
          "IMMORTAL", 5,
          "SAINT", 10,
          "DIVINE", 20);

  // ===================== 公开 API（含认证） =====================

  @Authenticated
  @Transactional
  public ServiceResult<PenCellVO> hatchBeast(
      PlatformType platform, String openId, String position, String eggName) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(hatchBeast(userId, position, eggName));
  }

  @Authenticated
  @Transactional
  public ServiceResult<PenCellVO> hatchBeastByInput(
      PlatformType platform, String openId, String position, String input) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(hatchBeastByInput(userId, position, input));
  }

  @Authenticated
  @Transactional
  public ServiceResult<ReleaseBeastVO> releaseBeast(
      PlatformType platform, String openId, String position) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(releaseBeast(userId, position));
  }

  @Authenticated
  @Transactional
  public ServiceResult<Integer> feedBeast(
      PlatformType platform, String openId, String position, int quantity) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(feedBeast(userId, position, quantity));
  }

  @Authenticated
  @Transactional
  public ServiceResult<PenCellVO> evolveBeast(
      PlatformType platform, String openId, String position) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(evolveBeast(userId, position));
  }

  // ===================== 内部 API =====================

  @Transactional
  public PenCellVO hatchBeast(Long userId, String position, String eggName) {
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
  public PenCellVO hatchBeastByInput(Long userId, String position, String input) {
    Integer cellId = fudiHelper.parseCellId(position);
    var result = itemResolver.resolveEgg(userId, input);
    return switch (result) {
      case ItemResolver.Found(var template, var index) -> {
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
        "玩家 {} 在地块 {} 孵化 {}（{} {}）{}",
        userId,
        cellId,
        setup.beastName,
        Beast.getTierName(setup.tier),
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

  private HatchSetup prepareHatchSetup(
      Long userId, ItemTemplate eggTemplate, int cellLevel, Fudi fudi) {
    int tier = 1;

    int stoneCost = 400;
    spiritStoneService.withdraw(userId, stoneCost);

    var spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
    int affection = spirit != null && spirit.getAffection() != null ? spirit.getAffection() : 0;
    BeastQuality quality = rollBeastQuality(affection, cellLevel);
    boolean isMutant = ThreadLocalRandom.current().nextInt(100) < 5;
    List<String> mutationTraits = new ArrayList<>();
    if (isMutant) mutationTraits.add(beastMutationService.rollRandomTrait());

    double levelSpeed = fudiHelper.getLevelSpeedMultiplier(cellLevel, tier);
    double baseHatchHours = 32;
    double hatchHours = baseHatchHours / levelSpeed;

    String beastName = eggTemplate.getName().replace("兽卵", "").replace("蛋", "灵兽");

    return new HatchSetup(tier, quality, isMutant, mutationTraits, hatchHours, beastName);
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
  public int feedBeast(Long userId, String position, int quantity) {
    if (quantity <= 0) {
      throw new BusinessException(ITEM_QUANTITY_INSUFFICIENT, quantity, 0);
    }

    var pcb = beastDisplayHelper.findPenCell(userId, position, false, true);
    var beast = pcb.beast();
    var cellId = pcb.cellId();
    if (beast.getLevel() >= beast.getLevelCap()) {
      throw new BusinessException(CELL_MAX_LEVEL);
    }

    ItemTemplate essenceTemplate =
        itemTemplateRepository
            .findByName("灵兽精华")
            .orElseThrow(() -> new BusinessException(ITEM_NOT_FOUND, "灵兽精华"));

    var essenceItem =
        stackableItemRepository
            .findByUserIdAndTemplateId(userId, essenceTemplate.getId())
            .orElseThrow(() -> new BusinessException(ITEM_NOT_EXISTS, "灵兽精华"));

    stackableItemService.reduceStackableItem(userId, essenceItem.getId(), quantity);

    int totalExp = quantity * ESSENCE_EXP_PER_UNIT;
    long consumed = beast.addExp(totalExp);
    beastRepository.save(beast);

    log.debug(
        "玩家 {} 喂 {} 份灵兽精华给地块 {} 的灵兽 {}，获得 {} 经验",
        userId,
        quantity,
        cellId,
        beast.getBeastName(),
        consumed);

    return (int) consumed;
  }

  @Transactional
  public PenCellVO evolveBeast(Long userId, String position) {
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
}
