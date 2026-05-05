package top.stillmisty.xiantao.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.fudi.entity.CellConfig;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.domain.fudi.entity.SpiritForm;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.enums.EmotionState;
import top.stillmisty.xiantao.domain.fudi.enums.MBTIPersonality;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.fudi.repository.FudiRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritFormRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.fudi.vo.*;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.annotation.Authenticated;

@Service
@RequiredArgsConstructor
@Slf4j
public class FudiService {

  private final FudiRepository fudiRepository;
  private final FudiCellRepository fudiCellRepository;
  private final StackableItemService stackableItemService;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemRepository stackableItemRepository;
  private final SpiritRepository spiritRepository;
  private final SpiritFormRepository spiritFormRepository;
  private final BeastRepository beastRepository;
  private final BeastService beastService;
  private final FarmService farmService;
  private final FudiHelper fudiHelper;
  private final TribulationService tribulationService;

  // ===================== 公开 API（含认证） =====================

  @Authenticated
  public ServiceResult<FudiStatusVO> getFudiStatus(PlatformType platform, String openId) {
    try {
      Long userId = UserContext.getCurrentUserId();
      return new ServiceResult.Success<>(getFudiStatus(userId));
    } catch (IllegalStateException | IllegalArgumentException e) {
      return ServiceResult.businessFailure(e.getMessage());
    }
  }

  @Authenticated
  @Transactional
  public ServiceResult<CollectVO> collect(PlatformType platform, String openId, String position) {
    try {
      Long userId = UserContext.getCurrentUserId();
      return new ServiceResult.Success<>(collect(userId, position));
    } catch (IllegalStateException | IllegalArgumentException e) {
      return ServiceResult.businessFailure(e.getMessage());
    }
  }

  @Authenticated
  @Transactional
  public ServiceResult<CollectAllVO> collectAll(PlatformType platform, String openId) {
    try {
      Long userId = UserContext.getCurrentUserId();
      return new ServiceResult.Success<>(collectAll(userId));
    } catch (IllegalStateException | IllegalArgumentException e) {
      return ServiceResult.businessFailure(e.getMessage());
    }
  }

  @Authenticated
  @Transactional
  public ServiceResult<CellOperationVO> buildCell(
      PlatformType platform, String openId, String position, CellType type) {
    try {
      Long userId = UserContext.getCurrentUserId();
      return new ServiceResult.Success<>(buildCell(userId, position, type));
    } catch (IllegalStateException | IllegalArgumentException e) {
      return ServiceResult.businessFailure(e.getMessage());
    }
  }

  @Authenticated
  @Transactional
  public ServiceResult<CellOperationVO> removeCell(
      PlatformType platform, String openId, String position) {
    try {
      Long userId = UserContext.getCurrentUserId();
      return new ServiceResult.Success<>(removeCell(userId, position));
    } catch (IllegalStateException | IllegalArgumentException e) {
      return ServiceResult.businessFailure(e.getMessage());
    }
  }

  @Authenticated
  @Transactional
  public ServiceResult<UpgradeCellVO> upgradeCell(
      PlatformType platform, String openId, String position) {
    try {
      Long userId = UserContext.getCurrentUserId();
      return new ServiceResult.Success<>(upgradeCell(userId, position));
    } catch (IllegalStateException | IllegalArgumentException e) {
      return ServiceResult.businessFailure(e.getMessage());
    }
  }

  @Authenticated
  @Transactional
  public ServiceResult<GiveGiftVO> giveGift(PlatformType platform, String openId, String itemName) {
    try {
      Long userId = UserContext.getCurrentUserId();
      return new ServiceResult.Success<>(giveGift(userId, itemName));
    } catch (IllegalStateException | IllegalArgumentException e) {
      return ServiceResult.businessFailure(e.getMessage());
    }
  }

  @Authenticated
  @Transactional
  public ServiceResult<TriggerTribulationVO> triggerTribulation(
      PlatformType platform, String openId) {
    try {
      Long userId = UserContext.getCurrentUserId();
      return new ServiceResult.Success<>(triggerTribulation(userId));
    } catch (IllegalStateException | IllegalArgumentException e) {
      return ServiceResult.businessFailure(e.getMessage());
    }
  }

  // ===================== 内部 API =====================

  // ===================== 福地基础管理 =====================

  @Transactional
  public void createFudi(Long userId, MBTIPersonality mbtiType) {
    if (fudiRepository.existsByUserId(userId)) {
      throw new IllegalStateException("用户已拥有福地");
    }

    Fudi fudi = Fudi.create();
    fudi.setUserId(userId);
    fudi.setTribulationStage(0);
    fudi.setTribulationWinStreak(0);
    fudi.setLastOnlineTime(LocalDateTime.now());

    fudi = fudiRepository.save(fudi);

    Spirit spirit = createSpiritForFudi(fudi, mbtiType);
    spiritRepository.save(spirit);

    autoExpandCells(fudi);
  }

  private Spirit createSpiritForFudi(Fudi fudi, MBTIPersonality mbtiType) {
    List<SpiritForm> allForms = spiritFormRepository.findAll();

    Spirit spirit = Spirit.create();
    spirit.setFudiId(fudi.getId());
    spirit.setAffection(0);
    spirit.setAffectionMax(1000);
    spirit.setEmotionState(EmotionState.NEUTRAL);
    spirit.setMbtiType(mbtiType);

    if (allForms != null && !allForms.isEmpty()) {
      SpiritForm randomForm = allForms.get(ThreadLocalRandom.current().nextInt(allForms.size()));
      spirit.setFormId(randomForm.getId().intValue());
    }

    return spirit;
  }

  // 保留此内部方法供 SpiritTools 使用
  public Optional<Fudi> getFudiByUserId(Long userId) {
    return fudiHelper.getFudiByUserId(userId);
  }

  private Fudi getFudiOrThrow(Long userId) {
    return getFudiByUserId(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));
  }

  private record CellContext(Fudi fudi, FudiCell cell, Integer cellId) {}

  private CellContext getCellContext(Long userId, String position) {
    Integer cellId = fudiHelper.parseCellId(position);
    Fudi fudi = getFudiOrThrow(userId);
    FudiCell cell =
        fudiCellRepository
            .findByFudiIdAndCellId(fudi.getId(), cellId)
            .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));
    return new CellContext(fudi, cell, cellId);
  }

  private FudiCell getCellOrNull(Long userId, String position) {
    Integer cellId = fudiHelper.parseCellId(position);
    Fudi fudi = getFudiOrThrow(userId);
    return fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId).orElse(null);
  }

  private void autoExpandCells(Fudi fudi) {
    int maxCells = 3 + fudi.getTribulationStage() / 3;
    int currentCount = fudiCellRepository.countByFudiId(fudi.getId());

    if (currentCount >= maxCells) return;

    for (int i = currentCount + 1; i <= maxCells; i++) {
      FudiCell cell = FudiCell.createEmpty(fudi.getId(), i);
      fudiCellRepository.save(cell);
    }
  }

  private int getTotalCellCount(Fudi fudi) {
    return fudiCellRepository.countByFudiId(fudi.getId());
  }

  // ===================== 福地状态查询 =====================

  public FudiStatusVO getFudiStatus(Long userId) {
    Fudi fudi = getFudiOrThrow(userId);
    autoExpandCells(fudi);

    var user = fudiHelper.getUserOrThrow(userId);
    String tribulationResult = tribulationService.resolveTribulation(fudi, user, false);

    spiritRepository
        .findByFudiId(fudi.getId())
        .ifPresent(
            spirit -> {
              if (tribulationResult == null) {
                spirit.updateEmotionState();
              }
              spiritRepository.save(spirit);
            });

    fudiRepository.save(fudi);
    return buildFudiStatusVO(fudi, tribulationResult);
  }

  private FudiStatusVO buildFudiStatusVO(Fudi fudi, String tribulationResult) {
    List<CellDetailVO> cellDetails = buildCellDetails(fudi);

    int totalBeasts =
        (int)
            cellDetails.stream()
                .filter(c -> c.getType() == CellType.PEN && c.getName() != null)
                .count();

    Spirit spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);

    String formName = null;
    List<String> likedTags = null;
    List<String> dislikedTags = null;
    if (spirit != null && spirit.getFormId() != null) {
      SpiritForm form = spiritFormRepository.findById(spirit.getFormId().longValue()).orElse(null);
      if (form != null) {
        formName = form.getName();
        likedTags = new ArrayList<>(form.getLikedTags());
        dislikedTags = new ArrayList<>(form.getDislikedTags());
      }
    }

    return FudiStatusVO.builder()
        .fudiId(fudi.getId())
        .userId(fudi.getUserId())
        .tribulationStage(fudi.getTribulationStage())
        .totalCells(getTotalCellCount(fudi))
        .mbtiType(spirit != null ? spirit.getMbtiType() : null)
        .spiritAffection(spirit != null ? spirit.getAffection() : null)
        .affectionMax(spirit != null ? spirit.getAffectionMax() : null)
        .spiritForm(formName)
        .spiritFormName(formName)
        .likedTags(likedTags)
        .dislikedTags(dislikedTags)
        .emotionState(spirit != null ? spirit.getEmotionState() : null)
        .occupiedCells(getOccupiedCellCount(fudi))
        .tribulationWinStreak(fudi.getTribulationWinStreak())
        .lastTribulationTime(fudi.getLastTribulationTime())
        .nextTribulationTime(fudi.calculateNextTribulationTime())
        .cellDetails(cellDetails)
        .totalBeasts(totalBeasts)
        .tribulationResult(tribulationResult)
        .build();
  }

  private int getOccupiedCellCount(Fudi fudi) {
    return (int)
        fudiCellRepository.findByFudiId(fudi.getId()).stream()
            .filter(cell -> cell.getCellType() != CellType.EMPTY)
            .count();
  }

  // ===================== 天劫触发 =====================

  @Transactional
  public TriggerTribulationVO triggerTribulation(Long userId) {
    Fudi fudi = getFudiOrThrow(userId);

    User user = fudiHelper.getUserOrThrow(userId);

    String result = tribulationService.resolveTribulation(fudi, user, true);
    fudiRepository.save(fudi);

    return new TriggerTribulationVO(
        result != null ? result : "天劫未触发",
        fudi.getTribulationStage(),
        fudi.getTribulationWinStreak());
  }

  // ===================== 地块详情构建 =====================

  private List<CellDetailVO> buildCellDetails(Fudi fudi) {
    List<FudiCell> cells = fudiCellRepository.findByFudiId(fudi.getId());
    List<CellDetailVO> details = new ArrayList<>();

    for (FudiCell cell : cells) {
      CellDetailVO.CellDetailVOBuilder builder =
          CellDetailVO.builder()
              .cellId(cell.getCellId())
              .type(cell.getCellType())
              .cellLevel(cell.getCellLevel());

      switch (cell.getCellType()) {
        case FARM -> farmService.buildFarmCellDetail(builder, cell);
        case PEN -> beastService.buildPenCellDetail(builder, cell);
        default -> {}
      }

      details.add(builder.build());
    }

    return details;
  }

  // ===================== 统一收取系统（种植收获 + 灵兽产出） =====================

  @Transactional
  public CollectVO collect(Long userId, String position) {
    CellContext ctx = getCellContext(userId, position);
    FudiCell cell = ctx.cell();
    Integer cellId = ctx.cellId();
    Fudi fudi = ctx.fudi();

    if (cell.isEmpty()) {
      throw new IllegalStateException("地块 " + cellId + " 为空");
    }

    if (cell.getCellType() == CellType.FARM) {
      return farmService.harvestCrop(fudi, cell, cellId);
    } else if (cell.getCellType() == CellType.PEN) {
      return beastService.collectBeastProduce(fudi, cell, cellId);
    } else {
      throw new IllegalStateException("地块 " + cellId + " 无可收取内容");
    }
  }

  // ===================== 种植系统 =====================

  @Transactional
  public CollectAllVO collectAll(Long userId) {
    Fudi fudi = getFudiOrThrow(userId);

    List<FudiCell> cells = fudiCellRepository.findByFudiId(fudi.getId());
    if (cells.isEmpty()) {
      return new CollectAllVO(0, 0, 0);
    }

    int harvestCount = 0;
    int collectedCount = 0;
    int totalItems = 0;

    // 收取灵田
    List<Long> toRemove = new ArrayList<>();
    for (FudiCell cell : cells) {
      if (cell.getCellType() != CellType.FARM) continue;
      if (!(cell.getConfig() instanceof CellConfig.FarmConfig farm)) continue;

      farmService.updateGrowthProgress(cell);
      Double progress = farmService.calculateGrowthProgress(cell);
      if (progress == null || progress < 1.0) continue;

      boolean isPerennial = farm.harvestCount() < farmService.getMaxHarvest(farm.cropId());
      if (!isPerennial && progress > 1.0 && farmService.isWilted(cell)) {
        toRemove.add(cell.getId());
        continue;
      }

      int yield = farmService.calculateYield(farm.cropId(), fudi.getTribulationStage());
      int hCount = farm.harvestCount() + 1;
      int maxHarvest = farmService.getMaxHarvest(farm.cropId());

      if (isPerennial && hCount < maxHarvest) {
        cell.setConfig(farm.withHarvestCount(hCount));
        farmService.replantAfterHarvest(cell);
      } else {
        toRemove.add(cell.getId());
      }

      totalItems += yield;
      harvestCount++;
    }

    for (Long id : toRemove) {
      fudiCellRepository.deleteById(id);
    }

    // 收取兽栏产出
    for (FudiCell cell : cells) {
      if (cell.getCellType() != CellType.PEN) continue;
      if (beastService.isIncubating(cell)) continue;

      beastService.updateBeastProduction(cell, fudi);

      List<CellConfig.ProductionItem> productionStored = beastService.getProductionStoredList(cell);
      if (productionStored.isEmpty()) continue;

      int cellTotalItems = 0;
      for (CellConfig.ProductionItem item : productionStored) {
        if (item.quantity() > 0) {
          stackableItemService.addStackableItem(
              fudi.getUserId(), item.templateId(), ItemType.HERB, item.name(), item.quantity());
          cellTotalItems += item.quantity();
        }
      }

      cell.clearProductionStored();
      fudiCellRepository.save(cell);

      totalItems += cellTotalItems;
      collectedCount++;
      log.info("用户 {} 收取地块 {} 的灵兽产出 {} 件", fudi.getUserId(), cell.getCellId(), cellTotalItems);
    }

    return new CollectAllVO(harvestCount, collectedCount, totalItems);
  }

  // ===================== 建造/拆除/升级系统 =====================

  @Transactional
  public CellOperationVO buildCell(Long userId, String position, CellType type) {
    Integer cellId = fudiHelper.parseCellId(position);
    Fudi fudi = getFudiOrThrow(userId);

    FudiCell existingCell =
        fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId).orElse(null);
    if (existingCell != null && existingCell.getCellType() != CellType.EMPTY) {
      throw new IllegalStateException("地块 " + cellId + " 已被占用");
    }

    FudiCell cell = existingCell != null ? existingCell : new FudiCell();
    if (existingCell == null) {
      cell.setFudiId(fudi.getId());
      cell.setCellId(cellId);
    }
    cell.setCellType(type);
    cell.setCellLevel(1);
    fudiCellRepository.save(cell);

    log.info("用户 {} 在地块 {} 建造 {}", userId, cellId, type.getChineseName());

    return new CellOperationVO(cellId, type.getChineseName());
  }

  @Transactional
  public CellOperationVO removeCell(Long userId, String position) {
    Integer cellId = fudiHelper.parseCellId(position);
    Fudi fudi = getFudiOrThrow(userId);

    FudiCell cell =
        fudiCellRepository
            .findByFudiIdAndCellId(fudi.getId(), cellId)
            .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

    if (cell.isEmpty()) {
      throw new IllegalStateException("地块 " + cellId + " 为空");
    }

    if (cell.getCellType() == CellType.PEN) {
      Beast beast = beastService.findBeastByCell(cell);
      if (beast != null) {
        beast.setPennedCellId(null);
        beastRepository.save(beast);
      }
    }

    fudiCellRepository.deleteById(cell.getId());

    log.info("用户 {} 拆除地块 {} 的 {}", userId, cellId, cell.getCellType().getChineseName());

    return new CellOperationVO(cellId, cell.getCellType().getChineseName());
  }

  @Transactional
  public UpgradeCellVO upgradeCell(Long userId, String position) {
    CellContext ctx = getCellContext(userId, position);
    FudiCell cell = ctx.cell();
    Integer cellId = ctx.cellId();

    if (cell.isEmpty()) {
      throw new IllegalStateException("地块 " + cellId + " 为空");
    }

    int currentLevel = cell.getCellLevel();
    if (currentLevel >= 5) {
      throw new IllegalStateException("已是最高等级 Lv5");
    }

    int cost = currentLevel == 1 ? 200 : currentLevel == 2 ? 400 : currentLevel == 3 ? 800 : 1600;
    fudiHelper.checkSpiritStones(userId, cost);
    fudiHelper.deductSpiritStones(userId, cost);

    int newLevel = currentLevel + 1;
    cell.setCellLevel(newLevel);
    fudiCellRepository.save(cell);

    log.info(
        "用户 {} 升级地块 {} 的 {} Lv{} -> Lv{}",
        userId,
        cellId,
        cell.getCellType().getChineseName(),
        currentLevel,
        newLevel);

    return new UpgradeCellVO(cellId, cell.getCellType().getChineseName(), currentLevel, newLevel);
  }

  // ===================== 送礼系统 =====================

  @Transactional
  public GiveGiftVO giveGift(Long userId, String itemName) {
    Fudi fudi = getFudiOrThrow(userId);

    List<StackableItem> items =
        stackableItemRepository.findByUserId(userId).stream()
            .filter(e -> e.getName().contains(itemName))
            .toList();

    if (items.isEmpty()) {
      throw new IllegalStateException("背包中未找到 [" + itemName + "]");
    }

    if (items.size() > 1) {
      throw new IllegalStateException("找到多个 [" + itemName + "]，请使用更精确的名称");
    }

    StackableItem gift = items.getFirst();
    ItemTemplate template =
        itemTemplateRepository
            .findById(gift.getTemplateId() != null ? gift.getTemplateId() : (long) 1)
            .orElse(null);

    Spirit spirit =
        spiritRepository
            .findByFudiId(fudi.getId())
            .orElseThrow(() -> new IllegalStateException("地灵不存在"));

    if (spirit.getLastGiftTime() != null
        && spirit.getLastGiftTime().toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
      throw new IllegalStateException("今日已送过礼物，明天再来吧");
    }

    Set<String> likedTags = Set.of();
    Set<String> dislikedTags = Set.of();
    if (spirit.getFormId() != null) {
      SpiritForm spiritForm =
          spiritFormRepository.findById(spirit.getFormId().longValue()).orElse(null);
      if (spiritForm != null) {
        likedTags = spiritForm.getLikedTags() != null ? spiritForm.getLikedTags() : Set.of();
        dislikedTags =
            spiritForm.getDislikedTags() != null ? spiritForm.getDislikedTags() : Set.of();
      }
    }

    Set<String> itemTags =
        gift.getTags() != null && !gift.getTags().isEmpty()
            ? gift.getTags()
            : (template != null && template.getTags() != null ? template.getTags() : Set.of());

    boolean isLiked = itemTags.stream().anyMatch(likedTags::contains);
    boolean isDisliked = itemTags.stream().anyMatch(dislikedTags::contains);

    int oldAffection = spirit.getAffection();
    int change;
    String reaction;

    if (isLiked) {
      change = 10 + ThreadLocalRandom.current().nextInt(41);
      reaction = "开心";
    } else if (isDisliked) {
      change = -(5 + ThreadLocalRandom.current().nextInt(16));
      reaction = "嫌弃";
    } else {
      change = 1 + ThreadLocalRandom.current().nextInt(3);
      reaction = "平淡";
    }

    spirit.addAffection(change);
    spirit.setLastGiftTime(LocalDateTime.now());

    if (gift.getQuantity() != null && gift.getQuantity() > 1) {
      gift.setQuantity(gift.getQuantity() - 1);
      stackableItemRepository.save(gift);
    } else {
      stackableItemRepository.deleteById(gift.getId());
    }

    spiritRepository.save(spirit);
    fudiRepository.save(fudi);

    return new GiveGiftVO(
        gift.getName(), oldAffection, spirit.getAffection(), change, reaction, isLiked, isDisliked);
  }

  // ===================== 地块状态查询 =====================

  public CellStatusVO getCellStatus(Long userId) {
    Fudi fudi = getFudiOrThrow(userId);

    int totalCells = getTotalCellCount(fudi);
    List<CellDetailVO> occupiedCells = new ArrayList<>();
    List<Integer> emptyCellIds = new ArrayList<>();

    List<FudiCell> cells = fudiCellRepository.findByFudiId(fudi.getId());
    for (FudiCell cell : cells) {
      if (cell.isEmpty()) {
        emptyCellIds.add(cell.getCellId());
        continue;
      }

      var builder =
          CellDetailVO.builder()
              .cellId(cell.getCellId())
              .type(cell.getCellType())
              .cellLevel(cell.getCellLevel());

      switch (cell.getCellType()) {
        case FARM -> {
          if (cell.getConfig() instanceof CellConfig.FarmConfig farm) {
            builder.name(farmService.getCropName(farm.cropId()));
          } else {
            builder.name("未知灵草");
          }
          farmService.updateGrowthProgress(cell);
          Double progress = farmService.calculateGrowthProgress(cell);
          builder.growthProgress(progress);
          builder.isMature(progress != null && progress >= 1.0);
        }
        case PEN -> {
          Beast beast = beastService.findBeastByCell(cell);
          builder.name(beast != null ? beast.getBeastName() : "空兽栏");
          builder.level(beast != null ? beast.getTier() : 0);
          builder.quality(beast != null ? beast.getQuality().getCode() : null);
          builder.productionStored(cell.getTotalProductionQuantity());
          builder.isIncubating(beastService.isIncubating(cell));
        }
      }

      occupiedCells.add(builder.build());
    }

    return new CellStatusVO(
        totalCells, occupiedCells.size(), emptyCellIds.size(), emptyCellIds, occupiedCells);
  }
}
