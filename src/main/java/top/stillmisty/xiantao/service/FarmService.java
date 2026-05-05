package top.stillmisty.xiantao.service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.fudi.entity.CellConfig;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.fudi.vo.CellDetailVO;
import top.stillmisty.xiantao.domain.fudi.vo.CollectVO;
import top.stillmisty.xiantao.domain.fudi.vo.FarmCellVO;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.annotation.Authenticated;

@Service
@RequiredArgsConstructor
@Slf4j
public class FarmService {

  private final FudiCellRepository fudiCellRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemRepository stackableItemRepository;
  private final StackableItemService stackableItemService;
  private final ItemResolver itemResolver;
  private final FudiHelper fudiHelper;

  // ===================== 公开 API（含认证） =====================

  @Authenticated
  @Transactional
  public ServiceResult<FarmCellVO> plantCropByName(
      PlatformType platform, String openId, String position, String cropName) {
    try {
      Long userId = UserContext.getCurrentUserId();
      return new ServiceResult.Success<>(plantCropByName(userId, position, cropName));
    } catch (IllegalStateException | IllegalArgumentException e) {
      return ServiceResult.businessFailure(e.getMessage());
    }
  }

  @Authenticated
  @Transactional
  public ServiceResult<FarmCellVO> plantCropByInput(
      PlatformType platform, String openId, String position, String input) {
    try {
      Long userId = UserContext.getCurrentUserId();
      return new ServiceResult.Success<>(plantCropByInput(userId, position, input));
    } catch (IllegalStateException | IllegalArgumentException e) {
      return ServiceResult.businessFailure(e.getMessage());
    }
  }

  // ===================== 内部 API =====================

  FarmCellVO plantCrop(Long userId, Integer cellId, Integer cropId, String cropName, int cropTier) {
    Fudi fudi =
        fudiHelper.getFudiByUserId(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));

    FudiCell existingCell =
        fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId).orElse(null);
    if (existingCell != null
        && existingCell.getCellType() != CellType.EMPTY
        && existingCell.getCellType() != CellType.FARM) {
      throw new IllegalStateException("地块 " + cellId + " 已有其他类型建筑");
    }

    int cellLevel = existingCell != null ? existingCell.getCellLevel() : 1;
    int minLevel = Math.max(1, cropTier);
    if (cellLevel < minLevel) {
      throw new IllegalStateException(
          "作物等阶(T%d)需要至少Lv%d灵田，当前灵田Lv%d".formatted(cropTier, minLevel, cellLevel));
    }

    int stoneCost = cropTier * 5;
    fudiHelper.checkSpiritStones(userId, stoneCost);
    fudiHelper.deductSpiritStones(userId, stoneCost);

    double levelSpeedMultiplier = fudiHelper.getLevelSpeedMultiplier(cellLevel, minLevel);
    double baseGrowthHours = getBaseGrowthHours(cropId);
    double actualGrowthHours = baseGrowthHours / levelSpeedMultiplier;

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime matureTime = now.plusHours((long) actualGrowthHours);

    FudiCell farmCell = existingCell != null ? existingCell : new FudiCell();
    if (existingCell == null) {
      farmCell.setFudiId(fudi.getId());
      farmCell.setCellId(cellId);
    }
    farmCell.setCellType(CellType.FARM);
    farmCell.setCellLevel(cellLevel);
    farmCell.setConfig(new CellConfig.FarmConfig(cropId, now, matureTime, 0));
    fudiCellRepository.save(farmCell);

    log.info("用户 {} 在地块 {} 种植 {} (T{})", userId, cellId, cropName, cropTier);

    return FarmCellVO.builder()
        .cellId(cellId)
        .cellLevel(cellLevel)
        .cropId(cropId)
        .cropName(cropName)
        .plantTime(now)
        .matureTime(matureTime)
        .growthProgress(0.0)
        .isMature(false)
        .baseGrowthHours(baseGrowthHours)
        .actualGrowthHours(actualGrowthHours)
        .harvestCount(0)
        .maxHarvest(getMaxHarvest(cropId))
        .isPerennial(getMaxHarvest(cropId) > 1)
        .build();
  }

  @Transactional
  public FarmCellVO plantCropByName(Long userId, String position, String cropName) {
    Integer cellId = fudiHelper.parseCellId(position);
    ItemTemplate seedTemplate = findSeedTemplateByName(cropName);

    var stackableItem =
        stackableItemRepository
            .findByUserIdAndTemplateId(userId, seedTemplate.getId())
            .orElseThrow(() -> new IllegalStateException("背包中没有 [%s]".formatted(cropName)));

    stackableItemService.reduceStackableItem(userId, seedTemplate.getId(), 1);

    int growTime = seedTemplate.getGrowTime() != null ? seedTemplate.getGrowTime() : 24;
    int cropTier = fudiHelper.getCropTier(growTime);
    Integer cropId = seedTemplate.getId().intValue();

    return plantCrop(userId, cellId, cropId, cropName, cropTier);
  }

  @Transactional
  public FarmCellVO plantCropByInput(Long userId, String position, String input) {
    Integer cellId = fudiHelper.parseCellId(position);
    var result = itemResolver.resolveSeed(userId, input);
    return switch (result) {
      case ItemResolver.Found(var template, var index) -> {
        var stackableItem =
            stackableItemRepository
                .findByUserIdAndTemplateId(userId, template.getId())
                .orElseThrow(() -> new IllegalStateException("背包中没有 [" + template.getName() + "]"));

        stackableItemService.reduceStackableItem(userId, template.getId(), 1);

        int growTime = template.getGrowTime() != null ? template.getGrowTime() : 24;
        int cropTier = fudiHelper.getCropTier(growTime);
        Integer cropId = template.getId().intValue();
        String cropName = template.getName();

        yield plantCrop(userId, cellId, cropId, cropName, cropTier);
      }
      case ItemResolver.NotFound(var name) ->
          throw new IllegalStateException("背包中未找到种子 [" + name + "]");
      case ItemResolver.Ambiguous(var name, var candidates) -> {
        var sb = new StringBuilder("找到多个种子，请使用编号：\n");
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

  // ===================== 收获系统 =====================

  public CollectVO harvestCrop(Fudi fudi, FudiCell cell, Integer cellId) {
    updateGrowthProgress(cell);

    if (!(cell.getConfig() instanceof CellConfig.FarmConfig farm)) {
      throw new IllegalStateException("地块不是灵田");
    }

    Double progress = calculateGrowthProgress(cell);
    boolean isPerennial = farm.harvestCount() < getMaxHarvest(farm.cropId());

    if (!isPerennial && progress != null && progress > 1.0 && isWilted(cell)) {
      fudiCellRepository.deleteById(cell.getId());
      throw new IllegalStateException("%s 已枯萎（超过成熟时间两倍未收获）".formatted(getCropName(farm.cropId())));
    }

    if (progress == null || progress < 1.0) {
      throw new IllegalStateException("灵药尚未成熟");
    }

    String cropName = getCropName(farm.cropId());
    int yield = calculateYield(farm.cropId(), fudi.getTribulationStage());

    int harvestCount = farm.harvestCount() + 1;
    int maxHarvest = getMaxHarvest(farm.cropId());

    if (isPerennial && harvestCount < maxHarvest) {
      cell.setConfig(farm.withHarvestCount(harvestCount));
      replantAfterHarvest(cell);
    } else {
      fudiCellRepository.deleteById(cell.getId());
    }

    log.info("用户 {} 收获地块 {} 的 {}，获得 {}份", fudi.getUserId(), cellId, cropName, yield);

    return new CollectVO(cellId, "farm", cropName, null, yield, yield);
  }

  /** 多年生作物收获后重置生长状态 */
  void replantAfterHarvest(FudiCell cell) {
    if (!(cell.getConfig() instanceof CellConfig.FarmConfig farm)) return;

    LocalDateTime now = LocalDateTime.now();
    double baseGrowthHours = getBaseGrowthHours(farm.cropId());
    int cellLevel = cell.getCellLevel();
    int cropTier = fudiHelper.getCropTier((int) baseGrowthHours);
    double levelSpeed = fudiHelper.getLevelSpeedMultiplier(cellLevel, Math.max(1, cropTier));
    LocalDateTime matureTime = now.plusHours((long) (baseGrowthHours / levelSpeed));

    cell.setConfig(new CellConfig.FarmConfig(farm.cropId(), now, matureTime, farm.harvestCount()));
    fudiCellRepository.save(cell);
  }

  // ===================== 灵田详情构建 =====================

  void buildFarmCellDetail(CellDetailVO.CellDetailVOBuilder builder, FudiCell cell) {
    if (!(cell.getConfig() instanceof CellConfig.FarmConfig farm)) {
      builder.name("未知灵草").growthProgress(0.0).isMature(false);
      return;
    }

    String cropName = getCropName(farm.cropId());
    if (cropName == null) cropName = "未知灵草";

    updateGrowthProgress(cell);
    Double growthProgress = calculateGrowthProgress(cell);
    if (growthProgress == null) growthProgress = 0.0;
    boolean isPerennial = farm.harvestCount() < getMaxHarvest(farm.cropId());
    boolean isWilted = !isPerennial && growthProgress > 1.0 && isWilted(cell);

    builder
        .name(cropName)
        .growthProgress(growthProgress)
        .isMature(growthProgress >= 1.0 && !isWilted)
        .createTime(farm.plantTime());
  }

  // ===================== 辅助方法 =====================

  int calculateYield(Integer cropId, int tribulationStage) {
    int baseYield = 1 + ThreadLocalRandom.current().nextInt(3);
    int bonus = tribulationStage / 5;
    return baseYield + bonus;
  }

  double getBaseGrowthHours(Integer cropId) {
    return itemTemplateRepository
        .findById((long) cropId)
        .map(
            template -> template.getGrowTime() != null ? template.getGrowTime().doubleValue() : 5.0)
        .orElse(5.0);
  }

  void updateGrowthProgress(FudiCell cell) {
    // growthProgress is now computed on-the-fly, nothing to persist
  }

  boolean isWilted(FudiCell cell) {
    if (!(cell.getConfig() instanceof CellConfig.FarmConfig farm)) return false;
    if (farm.harvestCount() < getMaxHarvest(farm.cropId())) return false;

    double baseGrowthHours = getBaseGrowthHours(farm.cropId());
    LocalDateTime matureTime = farm.matureTime();
    long maxSeconds = (long) (baseGrowthHours * 3600 * 2);
    long secondsSinceMature =
        java.time.Duration.between(matureTime, LocalDateTime.now()).getSeconds();
    return secondsSinceMature > maxSeconds;
  }

  Double calculateGrowthProgress(FudiCell cell) {
    if (!(cell.getConfig() instanceof CellConfig.FarmConfig farm)) return null;

    LocalDateTime plantTime = farm.plantTime();
    LocalDateTime matureTime = farm.matureTime();
    if (plantTime == null || matureTime == null) return null;

    LocalDateTime now = LocalDateTime.now();
    if (now.isAfter(matureTime) || now.isEqual(matureTime)) return 1.0;

    long totalSeconds = java.time.Duration.between(plantTime, matureTime).getSeconds();
    if (totalSeconds <= 0) return 1.0;
    long elapsedSeconds = java.time.Duration.between(plantTime, now).getSeconds();
    return Math.min(1.0, (double) elapsedSeconds / totalSeconds);
  }

  String getCropName(Integer cropId) {
    return itemTemplateRepository.findById((long) cropId).map(ItemTemplate::getName).orElse("未知灵草");
  }

  int getMaxHarvest(Integer cropId) {
    if (cropId == null) return 1;
    var props =
        itemTemplateRepository
            .findById(cropId.longValue())
            .map(ItemTemplate::typedProperties)
            .orElse(null);
    if (props instanceof ItemProperties.Growth g) {
      return 1 + g.reharvest();
    }
    return 1;
  }

  ItemTemplate findSeedTemplateByName(String name) {
    return itemTemplateRepository.findByType(ItemType.SEED).stream()
        .filter(t -> t.getName().equals(name) || t.getName().contains(name))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("未找到种子: %s".formatted(name)));
  }
}
