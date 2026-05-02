package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.fudi.repository.FudiRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.fudi.vo.CellDetailVO;
import top.stillmisty.xiantao.domain.fudi.vo.CollectVO;
import top.stillmisty.xiantao.domain.fudi.vo.FarmCellVO;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.service.annotation.ConsumeSpiritEnergy;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class FarmService {

    private final FudiRepository fudiRepository;
    private final FudiCellRepository fudiCellRepository;
    private final ItemTemplateRepository itemTemplateRepository;
    private final StackableItemRepository stackableItemRepository;
    private final SpiritRepository spiritRepository;
    private final StackableItemService stackableItemService;
    private final ItemResolver itemResolver;
    private final UserRepository userRepository;

    // ===================== 公开 API（含认证） =====================

    public ServiceResult<FarmCellVO> plantCropByName(PlatformType platform, String openId, String position, String cropName) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(plantCropByName(userId, position, cropName));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    public ServiceResult<FarmCellVO> plantCropByInput(PlatformType platform, String openId, String position, String input) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(plantCropByInput(userId, position, input));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    // ===================== 内部 API =====================

    FarmCellVO plantCrop(Long userId, Integer cellId, Integer cropId, String cropName, int cropTier) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 3);

        FudiCell existingCell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId).orElse(null);
        if (existingCell != null && existingCell.getCellType() != CellType.EMPTY && existingCell.getCellType() != CellType.FARM) {
            throw new IllegalStateException("地块 " + cellId + " 已有其他类型建筑");
        }

        int cellLevel = existingCell != null ? existingCell.getCellLevel() : 1;
        int minLevel = Math.max(1, cropTier);
        if (cellLevel < minLevel) {
            throw new IllegalStateException(
                    "作物等阶(T%d)需要至少Lv%d灵田，当前灵田Lv%d".formatted(cropTier, minLevel, cellLevel));
        }

        int stoneCost = cropTier * 5;
        checkSpiritStones(userId, stoneCost);
        deductSpiritStones(userId, stoneCost);

        double levelSpeedMultiplier = getLevelSpeedMultiplier(cellLevel, minLevel);
        double baseGrowthHours = getBaseGrowthHours(cropId);
        double actualGrowthHours = baseGrowthHours / levelSpeedMultiplier;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime matureTime = now.plusHours((long) actualGrowthHours);

        int maxHarvest = 1 + cropTier / 3;
        boolean isPerennial = maxHarvest > 1;

        FudiCell farmCell = existingCell != null ? existingCell : new FudiCell();
        if (existingCell == null) {
            farmCell.setFudiId(fudi.getId());
            farmCell.setCellId(cellId);
        }
        farmCell.setCellType(CellType.FARM);
        farmCell.setCellLevel(cellLevel);
        farmCell.setConfigValue("crop_id", cropId);
        farmCell.setConfigValue("crop_name", cropName);
        farmCell.setConfigValue("crop_tier", cropTier);
        farmCell.setConfigValue("growth_progress", 0.0);
        farmCell.setConfigValue("plant_time", now.toString());
        farmCell.setConfigValue("mature_time", matureTime.toString());
        farmCell.setConfigValue("base_growth_hours", baseGrowthHours);
        farmCell.setConfigValue("level_speed_multiplier", levelSpeedMultiplier);
        farmCell.setConfigValue("harvest_count", 0);
        farmCell.setConfigValue("max_harvest", maxHarvest);
        farmCell.setConfigValue("is_perennial", isPerennial);
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
                .maxHarvest(maxHarvest)
                .isPerennial(isPerennial)
                .build();
    }

    @Transactional
    public FarmCellVO plantCropByName(Long userId, String position, String cropName) {
        Integer cellId = parseCellId(position);
        ItemTemplate seedTemplate = findSeedTemplateByName(cropName);

        var stackableItem = stackableItemRepository.findByUserIdAndTemplateId(userId, seedTemplate.getId())
                .orElseThrow(() -> new IllegalStateException("背包中没有 [%s]".formatted(cropName)));

        stackableItemService.reduceStackableItem(userId, seedTemplate.getId(), 1);

        int growTime = seedTemplate.getGrowTime() != null ? seedTemplate.getGrowTime() : 24;
        int cropTier = getCropTier(growTime);
        Integer cropId = seedTemplate.getId().intValue();

        return plantCrop(userId, cellId, cropId, cropName, cropTier);
    }

    @Transactional
    public FarmCellVO plantCropByInput(Long userId, String position, String input) {
        Integer cellId = parseCellId(position);
        var result = itemResolver.resolveSeed(userId, input);
        return switch (result) {
            case ItemResolver.Found(var template, var index) -> {
                var stackableItem = stackableItemRepository
                        .findByUserIdAndTemplateId(userId, template.getId())
                        .orElseThrow(() -> new IllegalStateException("背包中没有 [" + template.getName() + "]"));

                stackableItemService.reduceStackableItem(userId, template.getId(), 1);

                int growTime = template.getGrowTime() != null ? template.getGrowTime() : 24;
                int cropTier = getCropTier(growTime);
                Integer cropId = template.getId().intValue();
                String cropName = template.getName();

                yield plantCrop(userId, cellId, cropId, cropName, cropTier);
            }
            case ItemResolver.NotFound(var name) -> throw new IllegalStateException("背包中未找到种子 [" + name + "]");
            case ItemResolver.Ambiguous(var name, var candidates) -> {
                var sb = new StringBuilder("找到多个种子，请使用编号：\n");
                for (var e : candidates) {
                    sb.append(e.index()).append(". ").append(e.name()).append(" x").append(e.quantity()).append(" (").append(e.metadata()).append(")\n");
                }
                throw new IllegalStateException(sb.toString().strip());
            }
        };
    }

    // ===================== 收获系统 =====================

    @ConsumeSpiritEnergy(5)
    public CollectVO harvestCrop(Fudi fudi, FudiCell cell, Integer cellId) {
        updateGrowthProgress(cell);

        Double progress = cell.getDoubleConfig("growth_progress");
        Boolean isPerennial = cell.getBoolConfig("is_perennial");

        if (!Boolean.TRUE.equals(isPerennial) && progress != null && progress > 1.0 && isWilted(cell)) {
            String cropName = cell.getStringConfig("crop_name");
            fudiCellRepository.deleteById(cell.getId());
            throw new IllegalStateException("%s 已枯萎（超过成熟时间两倍未收获）".formatted(cropName));
        }

        if (progress == null || progress < 1.0) {
            throw new IllegalStateException("灵药尚未成熟");
        }

        String cropName = cell.getStringConfig("crop_name");
        Integer cropId = cell.getIntConfig("crop_id");
        int yield = calculateYield(cropId, fudi.getTribulationStage());

        Integer harvestCountVal = cell.getIntConfig("harvest_count");
        int harvestCount = (harvestCountVal != null ? harvestCountVal : 0) + 1;
        Integer maxHarvest = cell.getIntConfig("max_harvest");
        if (maxHarvest == null) maxHarvest = 1;

        if (Boolean.TRUE.equals(isPerennial) && harvestCount < maxHarvest) {
            cell.setConfigValue("harvest_count", harvestCount);
            cell.setConfigValue("growth_progress", 0.0);
            cell.setConfigValue("plant_time", LocalDateTime.now().toString());
            Double baseGrowthHours = cell.getDoubleConfig("base_growth_hours");
            Double levelSpeed = cell.getDoubleConfig("level_speed_multiplier");
            if (levelSpeed == null) levelSpeed = 1.0;
            cell.setConfigValue("mature_time", LocalDateTime.now().plusHours((long) (baseGrowthHours / levelSpeed)).toString());
            fudiCellRepository.save(cell);
        } else {
            fudiCellRepository.deleteById(cell.getId());
        }

        log.info("用户 {} 收获地块 {} 的 {}，获得 {}份", fudi.getUserId(), cellId, cropName, yield);

        return new CollectVO(cellId, "farm", cropName, null, yield, yield);
    }

    // ===================== 灵田详情构建 =====================

    void buildFarmCellDetail(CellDetailVO.CellDetailVOBuilder builder, FudiCell cell) {
        String cropName = cell.getStringConfig("crop_name");
        if (cropName == null) cropName = "未知灵草";

        updateGrowthProgress(cell);
        Double growthProgress = cell.getDoubleConfig("growth_progress");
        if (growthProgress == null) growthProgress = 0.0;
        Integer harvestCount = cell.getIntConfig("harvest_count");
        if (harvestCount == null) harvestCount = 0;
        Integer maxHarvest = cell.getIntConfig("max_harvest");
        if (maxHarvest == null) maxHarvest = 1;
        Boolean isPerennial = cell.getBoolConfig("is_perennial");
        boolean isWilted = !Boolean.TRUE.equals(isPerennial) && growthProgress > 1.0 && isWilted(cell);

        builder.name(cropName)
                .growthProgress(growthProgress)
                .isMature(growthProgress >= 1.0 && !isWilted);

        if (cell.getConfigValue("plant_time") != null) {
            builder.createTime(LocalDateTime.parse(cell.getStringConfig("plant_time")));
        }
    }

    // ===================== 辅助方法 =====================

    int calculateYield(Integer cropId, int tribulationStage) {
        int baseYield = 1 + ThreadLocalRandom.current().nextInt(3);
        int bonus = tribulationStage / 5;
        return baseYield + bonus;
    }

    double getBaseGrowthHours(Integer cropId) {
        return itemTemplateRepository.findById((long) cropId)
                .map(template -> template.getGrowTime() != null ? template.getGrowTime().doubleValue() : 5.0)
                .orElse(5.0);
    }

    void updateGrowthProgress(FudiCell cell) {
        String matureTimeStr = cell.getStringConfig("mature_time");
        if (matureTimeStr != null) {
            LocalDateTime matureTime = LocalDateTime.parse(matureTimeStr);
            LocalDateTime now = LocalDateTime.now();

            if (now.isAfter(matureTime) || now.isEqual(matureTime)) {
                cell.setConfigValue("growth_progress", 1.0);
            } else {
                String plantTimeStr = cell.getStringConfig("plant_time");
                LocalDateTime plantTime = LocalDateTime.parse(plantTimeStr);
                long totalSeconds = java.time.Duration.between(plantTime, matureTime).getSeconds();
                long elapsedSeconds = java.time.Duration.between(plantTime, now).getSeconds();
                double progress = Math.min(1.0, (double) elapsedSeconds / totalSeconds);
                cell.setConfigValue("growth_progress", progress);
            }
            fudiCellRepository.save(cell);
        }
    }

    boolean isWilted(FudiCell cell) {
        if (Boolean.TRUE.equals(cell.getBoolConfig("is_perennial"))) return false;
        String matureTimeStr = cell.getStringConfig("mature_time");
        Double baseGrowthHours = cell.getDoubleConfig("base_growth_hours");
        if (matureTimeStr == null || baseGrowthHours == null) return false;
        LocalDateTime matureTime = LocalDateTime.parse(matureTimeStr);
        long maxSeconds = (long) (baseGrowthHours * 3600 * 2);
        long secondsSinceMature = java.time.Duration.between(matureTime, LocalDateTime.now()).getSeconds();
        return secondsSinceMature > maxSeconds;
    }

    ItemTemplate findSeedTemplateByName(String name) {
        return itemTemplateRepository.findByType(ItemType.SEED).stream()
                .filter(t -> t.getName().equals(name) || t.getName().contains(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未找到种子: %s".formatted(name)));
    }

    // ===================== 通用辅助方法 =====================

    void consumeSpiritEnergy(Fudi fudi, int baseCost) {
        spiritRepository.findByFudiId(fudi.getId()).ifPresent(spirit -> {
            spirit.restoreEnergy(fudi.getTribulationStage());
            int actualCost = spirit.calculateEnergyConsumption(baseCost);
            spirit.deductEnergy(actualCost);
            spirit.setLastEnergyUpdate(LocalDateTime.now());
            spiritRepository.save(spirit);
        });
    }

    Optional<Fudi> getFudiByUserId(Long userId) {
        Optional<Fudi> fudiOpt = fudiRepository.findByUserId(userId);
        fudiOpt.ifPresent(fudi -> {
            fudi.touchOnlineTime();
            spiritRepository.findByFudiId(fudi.getId()).ifPresent(spirit -> {
                spirit.restoreEnergy(fudi.getTribulationStage());
                spirit.updateEmotionState();
                spiritRepository.save(spirit);
            });
            fudiRepository.save(fudi);
        });
        return fudiOpt;
    }

    void checkSpiritStones(Long userId, int cost) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getSpiritStones() == null || user.getSpiritStones() < cost) {
            throw new IllegalStateException("灵石不足（需要 %d，当前 %d）".formatted(cost, user.getSpiritStones() != null ? user.getSpiritStones() : 0));
        }
    }

    void deductSpiritStones(Long userId, int cost) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setSpiritStones(user.getSpiritStones() - cost);
        userRepository.save(user);
    }

    Integer parseCellId(String position) {
        try {
            return Integer.valueOf(position);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("地块编号格式错误，请输入数字编号");
        }
    }

    int getCropTier(int growTime) {
        if (growTime <= 24) return 1;
        if (growTime <= 48) return 2;
        if (growTime <= 72) return 3;
        if (growTime <= 120) return 4;
        return 5;
    }

    double getLevelSpeedMultiplier(int cellLevel, int minRequired) {
        if (cellLevel < minRequired) return 0.5;
        int levelDiff = cellLevel - minRequired;
        return 1.0 + levelDiff * 0.15;
    }
}
