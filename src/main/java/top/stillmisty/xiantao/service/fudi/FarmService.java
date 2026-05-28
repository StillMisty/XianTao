package top.stillmisty.xiantao.service.fudi;

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
import top.stillmisty.xiantao.domain.fudi.vo.CellDetailVO;
import top.stillmisty.xiantao.domain.fudi.vo.CollectVO;
import top.stillmisty.xiantao.domain.fudi.vo.FarmCellVO;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.infrastructure.repository.FudiCellRepository;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.inventory.ItemResolver;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

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
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(plantCropByName(userId, position, cropName));
  }

  @Authenticated
  @Transactional
  public ServiceResult<FarmCellVO> plantCropByInput(
      PlatformType platform, String openId, String position, String input) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(plantCropByInput(userId, position, input));
  }

  // ===================== 内部 API =====================

  FarmCellVO plantCrop(Long userId, Integer cellId, Integer cropId, String cropName, int cropTier) {
    Fudi fudi =
        fudiHelper
            .findAndTouchFudi(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FUDI_NOT_FOUND));

    FudiCell existingCell =
        fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId).orElse(null);
    if (existingCell != null
        && existingCell.getCellType() != CellType.EMPTY
        && existingCell.getCellType() != CellType.FARM) {
      throw new BusinessException(ErrorCode.CELL_TYPE_MISMATCH, cellId);
    }

    int cellLevel = existingCell != null ? existingCell.getCellLevel() : 1;
    int minLevel = Math.max(1, cropTier);
    if (cellLevel < minLevel) {
      throw new BusinessException(ErrorCode.CROP_TIER_REQUIRES_FARM, cropTier, minLevel, cellLevel);
    }

    double levelSpeedMultiplier = fudiHelper.getLevelSpeedMultiplier(cellLevel, minLevel);
    double baseGrowthHours = getBaseGrowthHours(cropId);
    double actualGrowthHours = Math.max(0.1, baseGrowthHours / levelSpeedMultiplier);

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

    log.info("玩家 {} 在地块 {} 种植 {} (T{})", userId, cellId, cropName, cropTier);

    return new FarmCellVO(
        cellId,
        cellLevel,
        cropId,
        cropName,
        now,
        matureTime,
        0.0,
        false,
        baseGrowthHours,
        actualGrowthHours,
        0,
        getMaxHarvest(cropId),
        getMaxHarvest(cropId) > 1);
  }

  @Transactional
  public FarmCellVO plantCropByName(Long userId, String position, String cropName) {
    Integer cellId = fudiHelper.parseCellId(position);
    ItemTemplate seedTemplate = findSeedTemplateByName(cropName);

    var stackableItem =
        stackableItemRepository
            .findByUserIdAndTemplateId(userId, seedTemplate.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SEED_NOT_IN_INVENTORY, cropName));

    stackableItemService.reduceStackableItem(userId, stackableItem.getId(), 1);

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
      case ItemResolver.Found(var template, var _) -> {
        var stackableItem =
            stackableItemRepository
                .findByUserIdAndTemplateId(userId, template.getId())
                .orElseThrow(
                    () ->
                        new BusinessException(ErrorCode.SEED_NOT_IN_INVENTORY, template.getName()));

        stackableItemService.reduceStackableItem(userId, stackableItem.getId(), 1);

        int growTime = template.getGrowTime() != null ? template.getGrowTime() : 24;
        int cropTier = fudiHelper.getCropTier(growTime);
        Integer cropId = template.getId().intValue();
        String cropName = template.getName();

        yield plantCrop(userId, cellId, cropId, cropName, cropTier);
      }
      case ItemResolver.NotFound(var name) ->
          throw new BusinessException(ErrorCode.SEED_NOT_IN_INVENTORY, name);
      case ItemResolver.Ambiguous(var name, var _) ->
          throw new BusinessException(ErrorCode.ITEM_MULTIPLE_MATCH, name);
    };
  }

  // ===================== 收获系统 =====================

  public CollectVO harvestCrop(Fudi fudi, FudiCell cell, Integer cellId) {
    if (!(cell.getConfig() instanceof CellConfig.FarmConfig farm)) {
      throw new BusinessException(ErrorCode.CELL_NOT_FARM);
    }

    Double progress = calculateGrowthProgress(cell);
    boolean isPerennial = farm.harvestCount() < getMaxHarvest(farm.cropId());

    if (!isPerennial && progress != null && progress > 1.0 && isWilted(cell)) {
      fudiCellRepository.deleteById(cell.getId());
      throw new BusinessException(ErrorCode.CROP_WITHERED, getCropName(farm.cropId()));
    }

    if (progress == null || progress < 1.0) {
      throw new BusinessException(ErrorCode.CROP_NOT_READY);
    }

    String cropName = getCropName(farm.cropId());
    int yield = calculateYield(farm.cropId());
    grantHarvestItems(fudi.getUserId(), farm.cropId(), yield);

    int harvestCount = farm.harvestCount() + 1;
    int maxHarvest = getMaxHarvest(farm.cropId());

    if (isPerennial && harvestCount < maxHarvest) {
      cell.setConfig(farm.withHarvestCount(harvestCount));
      replantAfterHarvest(cell);
    } else {
      fudiCellRepository.deleteById(cell.getId());
    }

    log.info("玩家 {} 收获地块 {} 的 {}，获得 {}份", fudi.getUserId(), cellId, cropName, yield);

    return new CollectVO(cellId, "FARM", cropName, null, yield, yield);
  }

  void grantHarvestItems(Long userId, Integer cropId, int yield) {
    ItemTemplate seedTemplate = itemTemplateRepository.findById(cropId.longValue()).orElse(null);
    if (seedTemplate == null) {
      log.warn("收获作物时种子模板缺失: cropId={}, userId={}", cropId, userId);
      return;
    }
    var props = seedTemplate.typedProperties();
    if (!(props instanceof ItemProperties.Growth g)) {
      log.warn("收获作物时模板缺少Growth属性: cropId={}, userId={}", cropId, userId);
      return;
    }

    double mutationChance = g.mutation() != null ? g.mutation().chance() : 0;
    Long mutationTemplateId = g.mutation() != null ? g.mutation().templateId() : null;

    for (var item : g.productionItems()) {
      int normalCount = 0;
      int mutationCount = 0;
      for (int i = 0; i < yield; i++) {
        if (mutationTemplateId != null
            && mutationChance > 0
            && ThreadLocalRandom.current().nextDouble() < mutationChance) {
          mutationCount++;
        } else {
          normalCount++;
        }
      }

      if (normalCount > 0) {
        var prodTemplate = itemTemplateRepository.findById(item.templateId()).orElse(null);
        String name = prodTemplate != null ? prodTemplate.getName() : "未知灵药";
        ItemType prodType = prodTemplate != null ? prodTemplate.getType() : ItemType.HERB;
        stackableItemService.addStackableItem(
            userId, item.templateId(), prodType, name, normalCount);
      }
      if (mutationCount > 0) {
        var mutationTemplate = itemTemplateRepository.findById(mutationTemplateId).orElse(null);
        if (mutationTemplate != null) {
          stackableItemService.addStackableItem(
              userId,
              mutationTemplateId,
              mutationTemplate.getType(),
              mutationTemplate.getName(),
              mutationCount);
        }
      }
    }
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

  public int calculateYield(Integer cropId) {
    var props =
        itemTemplateRepository
            .findById((long) cropId)
            .map(ItemTemplate::typedProperties)
            .orElse(null);
    if (props instanceof ItemProperties.Growth g) {
      return (g.yieldMin() + ThreadLocalRandom.current().nextInt(g.yieldMax() - g.yieldMin() + 1));
    }
    return 1 + ThreadLocalRandom.current().nextInt(3);
  }

  public double getBaseGrowthHours(Integer cropId) {
    return itemTemplateRepository
        .findById((long) cropId)
        .map(
            template -> template.getGrowTime() != null ? template.getGrowTime().doubleValue() : 5.0)
        .orElse(5.0);
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

  public Double calculateGrowthProgress(FudiCell cell) {
    if (!(cell.getConfig() instanceof CellConfig.FarmConfig farm)) return null;

    LocalDateTime plantTime = farm.plantTime();
    LocalDateTime matureTime = farm.matureTime();
    if (plantTime == null || matureTime == null) return null;

    LocalDateTime now = LocalDateTime.now();
    long totalSeconds = java.time.Duration.between(plantTime, matureTime).getSeconds();
    if (totalSeconds <= 0) return 1.0;
    long elapsedSeconds = java.time.Duration.between(plantTime, now).getSeconds();
    return (double) elapsedSeconds / totalSeconds;
  }

  public String getCropName(Integer cropId) {
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
      return g.maxHarvest();
    }
    return 1;
  }

  public ItemTemplate findSeedTemplateByName(String name) {
    return itemTemplateRepository.findByType(ItemType.SEED).stream()
        .filter(t -> t.getName().equals(name) || t.getName().contains(name))
        .min(java.util.Comparator.comparing(ItemTemplate::getName))
        .orElseThrow(() -> new BusinessException(ErrorCode.SEED_TEMPLATE_NOT_FOUND, name));
  }
}
