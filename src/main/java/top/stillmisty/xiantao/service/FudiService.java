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
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.annotation.Authenticated;

@Service
@RequiredArgsConstructor
@Slf4j
public class FudiService {

  private final FudiRepository fudiRepository;
  private final FudiCellRepository fudiCellRepository;
  private final SpiritRepository spiritRepository;
  private final SpiritFormRepository spiritFormRepository;
  private final BeastRepository beastRepository;
  private final BeastService beastService;
  private final FarmService farmService;
  private final FudiHelper fudiHelper;
  private final TribulationService tribulationService;
  private final FudiGiftService fudiGiftService;
  private final FudiCollectService fudiCollectService;

  // ===================== 公开 API（含认证） =====================

  @Authenticated
  public ServiceResult<FudiStatusVO> getFudiStatus(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getFudiStatus(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<CollectVO> collect(PlatformType platform, String openId, String position) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(collect(userId, position));
  }

  @Authenticated
  @Transactional
  public ServiceResult<CollectAllVO> collectAll(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(collectAll(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<CellOperationVO> buildCell(
      PlatformType platform, String openId, String position, String cellTypeName) {
    Long userId = UserContext.getCurrentUserId();
    CellType type;
    try {
      type = CellType.fromChineseName(cellTypeName);
    } catch (IllegalArgumentException e) {
      return new ServiceResult.Failure<>(
          "INVALID_INPUT", "不支持的地块类型：" + cellTypeName + "（可选：灵田、兽栏）");
    }
    return new ServiceResult.Success<>(buildCell(userId, position, type));
  }

  @Authenticated
  @Transactional
  public ServiceResult<CellOperationVO> removeCell(
      PlatformType platform, String openId, String position) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(removeCell(userId, position));
  }

  @Authenticated
  @Transactional
  public ServiceResult<UpgradeCellVO> upgradeCell(
      PlatformType platform, String openId, String position) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(upgradeCell(userId, position));
  }

  @Authenticated
  @Transactional
  public ServiceResult<GiveGiftVO> giveGift(PlatformType platform, String openId, String itemName) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(giveGift(userId, itemName));
  }

  @Authenticated
  @Transactional
  public ServiceResult<TriggerTribulationVO> triggerTribulation(
      PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(triggerTribulation(userId));
  }

  // ===================== 内部 API =====================

  // ===================== 福地基础管理 =====================

  @Transactional
  public void createFudi(Long userId, MBTIPersonality mbtiType) {
    Fudi fudi = Fudi.create();
    fudi.setUserId(userId);
    fudi.setTribulationStage(0);
    fudi.setTribulationWinStreak(0);
    fudi.setLastOnlineTime(LocalDateTime.now());

    try {
      fudi = fudiRepository.save(fudi);
    } catch (org.springframework.dao.DuplicateKeyException e) {
      throw new IllegalStateException("用户已拥有福地");
    }

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
      spirit.setFormId(randomForm.getId());
    }

    return spirit;
  }

  // 保留此内部方法供 SpiritTools 使用
  public Optional<Fudi> findAndTouchFudi(Long userId) {
    return fudiHelper.findAndTouchFudi(userId);
  }

  private Fudi getFudiOrThrow(Long userId) {
    return findAndTouchFudi(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));
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
    List<FudiCell> existingCells = fudiCellRepository.findByFudiId(fudi.getId());
    var existingCellIds =
        existingCells.stream()
            .map(FudiCell::getCellId)
            .collect(java.util.stream.Collectors.toSet());

    for (int i = 1; i <= maxCells; i++) {
      if (!existingCellIds.contains(i)) {
        FudiCell cell = FudiCell.createEmpty(fudi.getId(), i);
        fudiCellRepository.save(cell);
      }
    }
  }

  private int getTotalCellCount(Fudi fudi) {
    return fudiCellRepository.countByFudiId(fudi.getId());
  }

  // ===================== 福地状态查询 =====================

  @Transactional
  public FudiStatusVO getFudiStatus(Long userId) {
    Fudi fudi = getFudiOrThrow(userId);
    autoExpandCells(fudi);

    String tribulationResult = null;
    if (tribulationService.isTribulationDue(fudi)) {
      var user = fudiHelper.getUserOrThrow(userId);
      tribulationResult = tribulationService.resolveTribulation(fudi, user, false);
    }

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
      SpiritForm form = spiritFormRepository.findById(spirit.getFormId()).orElse(null);
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

  public CollectAllVO collectAll(Long userId) {
    return fudiCollectService.collectAll(userId);
  }

  // ===================== 建造/拆除/升级系统 =====================

  @Transactional
  public CellOperationVO buildCell(Long userId, String position, CellType type) {
    Integer cellId = fudiHelper.parseCellId(position);
    Fudi fudi = getFudiOrThrow(userId);

    int maxCells = 3 + fudi.getTribulationStage() / 3;
    if (cellId < 1 || cellId > maxCells) {
      throw new IllegalStateException(
          "地块编号超出范围（1-%d），当前劫数仅开放到 %d 号地块".formatted(maxCells, maxCells));
    }

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

  public GiveGiftVO giveGift(Long userId, String itemName) {
    return fudiGiftService.giveGift(userId, itemName);
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
