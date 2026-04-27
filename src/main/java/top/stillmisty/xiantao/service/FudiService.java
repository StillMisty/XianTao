package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.land.entity.Fudi;
import top.stillmisty.xiantao.domain.land.enums.*;
import top.stillmisty.xiantao.domain.land.repository.FudiRepository;
import top.stillmisty.xiantao.domain.land.vo.*;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 福地服务层
 * 提供福地管理的核心业务逻辑，包括灵气消耗、献祭、种植、收获等功能。
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FudiService {

    private final FudiRepository fudiRepository;
    private final ItemService itemService;
    private final AuthenticationService authService;
    private final ItemTemplateRepository itemTemplateRepository;
    private final EquipmentRepository equipmentRepository;
    private final StackableItemRepository stackableItemRepository;
    private final ItemResolver itemResolver;

    // ===================== 公开 API（含认证） =====================

    public ServiceResult<FudiStatusVO> getFudiStatus(PlatformType platform, String openId) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(getFudiStatus(auth.userId()));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<FarmCellVO> plantCropByName(PlatformType platform, String openId, String position, String cropName) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(plantCropByName(auth.userId(), position, cropName));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<FarmCellVO> plantCropByInput(PlatformType platform, String openId, String position, String input) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(plantCropByInput(auth.userId(), position, input));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> harvestCrop(PlatformType platform, String openId, String position) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(harvestCrop(auth.userId(), position));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> harvestAllCrops(PlatformType platform, String openId) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(harvestAllCrops(auth.userId()));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> buildCell(PlatformType platform, String openId, String position, CellType type) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(buildCell(auth.userId(), position, type));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> removeCell(PlatformType platform, String openId, String position) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(removeCell(auth.userId(), position));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<Integer> sacrificeItemByName(PlatformType platform, String openId, String itemName) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(sacrificeItemByName(auth.userId(), itemName));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<Integer> sacrificeItemByInput(PlatformType platform, String openId, String input) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(sacrificeItemByInput(auth.userId(), input));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> upgradeCell(PlatformType platform, String openId, String position) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(upgradeCell(auth.userId(), position));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<PenCellVO> hatchBeast(PlatformType platform, String openId, String position, String eggName) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(hatchBeast(auth.userId(), position, eggName));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<PenCellVO> hatchBeastByInput(PlatformType platform, String openId, String position, String input) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(hatchBeastByInput(auth.userId(), position, input));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<BeastProduceVO> collectProduce(PlatformType platform, String openId, String position) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(collectProduce(auth.userId(), position));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> collectAllProduce(PlatformType platform, String openId) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(collectAllProduce(auth.userId()));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> releaseBeast(PlatformType platform, String openId, String position) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(releaseBeast(auth.userId(), position));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<PenCellVO> evolveBeast(PlatformType platform, String openId, String position, String mode) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(evolveBeast(auth.userId(), position, mode));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    // ===================== 内部 API（需预先完成认证） =====================

    // ===================== 福地基础管理 =====================

    /**
     * 创建新福地
     */
    public Fudi createFudi(Long userId, MBTIPersonality mbtiType) {
        if (fudiRepository.existsByUserId(userId)) {
            throw new IllegalStateException("用户已拥有福地");
        }

        // 初始化福地
        Fudi fudi = Fudi.create();
        fudi.setUserId(userId);
        fudi.setAuraCurrent(500); // 初始灵气
        fudi.setAuraMax(1000); // 初始灵气上限
        fudi.setCoreLevel(1);
        fudi.setGridSize(3);
        fudi.setMbtiType(mbtiType);
        fudi.setSpiritEnergy(100);
        fudi.setSpiritAffection(0);
        fudi.setEmotionState(EmotionState.CALM);
        fudi.setAutoMode(true);
        fudi.setDormantMode(false);
        fudi.setTribulationWinStreak(0);

        // 初始化网格布局
        Map<String, Object> gridLayout = new HashMap<>();
        gridLayout.put("grid_size", 3);
        gridLayout.put("core_level", 1);
        gridLayout.put("cells", new ArrayList<>());
        fudi.setGridLayout(gridLayout);

        // 初始化地灵配置
        Map<String, Object> spiritConfig = new HashMap<>();
        spiritConfig.put("mbti_type", mbtiType.getCode());
        spiritConfig.put("tone_style", mbtiType.getToneStyle());
        spiritConfig.put("emotion_state", "calm");
        fudi.setSpiritConfig(spiritConfig);

        fudi.setLastAuraUpdate(LocalDateTime.now());
        fudi.setLastOnlineTime(LocalDateTime.now());

        return fudiRepository.save(fudi);
    }

    /**
     * 获取用户福地（懒加载计算）
     */
    public Optional<Fudi> getFudiByUserId(Long userId) {
        Optional<Fudi> fudiOpt = fudiRepository.findByUserId(userId);
        fudiOpt.ifPresent(fudi -> {
            // 懒加载更新灵气
            fudi.updateAura();
            // 更新情绪状态
            fudi.updateEmotionState();
            fudiRepository.save(fudi);
        });
        return fudiOpt;
    }

    /**
     * 获取福地状态VO
     */
    public FudiStatusVO getFudiStatus(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        return buildFudiStatusVO(fudi);
    }

    /**
     * 构建福地状态VO
     */
    private FudiStatusVO buildFudiStatusVO(Fudi fudi) {
        List<CellDetailVO> cellDetails = buildCellDetails(fudi);

        int totalBeasts = (int) cellDetails.stream().filter(c -> c.getType() == CellType.PEN && c.getName() != null).count();
        long depletionCountdown = 0;
        if (fudi.getAuraCurrent() <= 0 && fudi.getGridLayout() != null && fudi.getGridLayout().containsKey("cells")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");
            for (Map<String, Object> cell : cells) {
                if (CellType.PEN.getCode().equals(cell.get("type")) && cell.containsKey("beast_id") && cell.containsKey("aura_depletion_start")) {
                    long elapsed = java.time.Duration.between(LocalDateTime.parse((String) cell.get("aura_depletion_start")), LocalDateTime.now()).getSeconds();
                    long remaining = Math.max(0, 86400 - elapsed);
                    depletionCountdown = Math.max(depletionCountdown, remaining);
                }
            }
        }

        return FudiStatusVO.builder()
                .fudiId(fudi.getId())
                .userId(fudi.getUserId())
                .auraCurrent(fudi.getAuraCurrent())
                .auraMax(fudi.getAuraMax())
                .auraHourlyCost(fudi.calculateHourlyAuraCost())
                .coreLevel(fudi.getCoreLevel())
                .gridSize(fudi.getGridSize())
                .mbtiType(fudi.getMbtiType())
                .spiritEnergy(fudi.getSpiritEnergy())
                .spiritAffection(fudi.getSpiritAffection())
                .emotionState(fudi.getEmotionState())
                .autoMode(fudi.getAutoMode())
                .dormantMode(fudi.getDormantMode())
                .occupiedCells(fudi.getOccupiedCellCount())
                .scorchedCells(fudi.getScorchedCells() != null ? fudi.getScorchedCells().size() : 0)
                .lastTribulationTime(fudi.getLastTribulationTime())
                .nextTribulationTime(calculateNextTribulationTime(fudi))
                .cellDetails(cellDetails)
                .totalBeasts(totalBeasts)
                .auraDepleteCountdownSeconds(depletionCountdown > 0 ? depletionCountdown : null)
                .build();
    }

    /**
     * 计算下次天劫时间
     */
    private LocalDateTime calculateNextTribulationTime(Fudi fudi) {
        if (fudi.getLastTribulationTime() == null) {
            return fudi.getCreateTime().plusDays(7);
        }
        return fudi.getLastTribulationTime().plusDays(7);
    }

    /**
     * 构建地块详情列表
     */
    private List<CellDetailVO> buildCellDetails(Fudi fudi) {
        if (fudi.getGridLayout() == null || !fudi.getGridLayout().containsKey("cells")) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");
        List<CellDetailVO> details = new ArrayList<>();

        for (Map<String, Object> cell : cells) {
            String typeStr = (String) cell.get("type");
            CellType type = CellType.fromCode(typeStr);
            String position = (String) cell.get("pos");

            CellDetailVO.CellDetailVOBuilder builder = CellDetailVO.builder()
                    .position(position)
                    .type(type)
                    .cellLevel((Integer) cell.getOrDefault("cell_level", 1));

            switch (type) {
                case FARM -> buildFarmCellDetail(builder, cell);
                case PEN -> buildPenCellDetail(builder, cell);
                case NODE -> buildNodeCellDetail(builder, cell);
                default -> {
                }
            }

            details.add(builder.build());
        }

        return details;
    }

    private void buildFarmCellDetail(CellDetailVO.CellDetailVOBuilder builder, Map<String, Object> cell) {
        String cropName = (String) cell.getOrDefault("crop_name", "未知灵草");
        String elementStr = (String) cell.get("element");
        WuxingType element = elementStr != null ? WuxingType.valueOf(elementStr) : null;

        updateGrowthProgress(cell);
        Double growthProgress = (Double) cell.getOrDefault("growth_progress", 0.0);
        int harvestCount = (Integer) cell.getOrDefault("harvest_count", 0);
        int maxHarvest = (Integer) cell.getOrDefault("max_harvest", 1);
        boolean isWilted = !Boolean.TRUE.equals(cell.get("is_perennial")) && growthProgress != null && growthProgress > 1.0 && isWilted(cell);

        builder.name(cropName)
                .element(element)
                .growthProgress(growthProgress)
                .isMature(growthProgress != null && growthProgress >= 1.0 && !isWilted);

        if (cell.containsKey("plant_time")) {
            builder.createTime(LocalDateTime.parse((String) cell.get("plant_time")));
        }
    }

    private void buildPenCellDetail(CellDetailVO.CellDetailVOBuilder builder, Map<String, Object> cell) {
        String beastName = (String) cell.getOrDefault("beast_name", "空兽栏");
        Integer tier = (Integer) cell.get("tier");

        if (beastName.equals("空兽栏")) {
            builder.name("空兽栏");
        } else {
            BeastQuality quality = getBeastQuality(cell);
            @SuppressWarnings("unchecked")
            List<String> traits = (List<String>) cell.getOrDefault("mutation_traits", List.of());
            builder.name(beastName)
                    .level(tier)
                    .quality(quality.getChineseName())
                    .mutationTraits(traits)
                    .productionStored((Integer) cell.getOrDefault("production_stored", 0))
                    .isIncubating(Boolean.TRUE.equals(cell.get("is_incubating")));
        }
    }

    private void buildNodeCellDetail(CellDetailVO.CellDetailVOBuilder builder, Map<String, Object> cell) {
        String elementStr = (String) cell.get("element");
        WuxingType element = elementStr != null ? WuxingType.valueOf(elementStr) : null;
        Integer level = (Integer) cell.getOrDefault("level", 1);
        Integer durability = (Integer) cell.get("durability");

        builder.name("阵眼")
                .level(level)
                .cellLevel((Integer) cell.getOrDefault("cell_level", 1))
                .element(element)
                .durability(durability);
    }

    // ===================== 献祭系统 =====================

    /**
     * 献祭装备获得灵气
     */
    public int sacrificeItem(Long userId, Long equipmentId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalStateException("未找到装备"));

        if (!equipment.getUserId().equals(userId)) {
            throw new IllegalStateException("该装备不属于你");
        }

        if (equipment.getEquipped()) {
            throw new IllegalStateException("不能献祭已穿戴的装备，请先卸下");
        }

        int itemLevel = equipment.getForgeLevel() != null ? equipment.getForgeLevel() : 1;
        double qualityMultiplier = equipment.getQualityMultiplier() != null ? equipment.getQualityMultiplier() : 1.0;
        int baseValue = equipment.getFinalAttack() + equipment.getFinalDefense();

        if (baseValue <= 0) {
            baseValue = 10;
        }

        int auraGain = fudi.calculateSacrificeAura(baseValue, qualityMultiplier, itemLevel);

        // 删除装备
        equipmentRepository.deleteById(equipmentId);

        // 添加灵气（不能超过上限）
        int newAura = Math.min(fudi.getAuraMax(), fudi.getAuraCurrent() + auraGain);
        fudi.setAuraCurrent(newAura);
        fudi.setLastAuraUpdate(LocalDateTime.now());

        fudiRepository.save(fudi);

        log.info("用户 {} 献祭装备 ID={} ({}), 获得灵气 {}", userId, equipmentId, equipment.getName(), auraGain);
        return auraGain;
    }

    /**
     * 根据物品名称献祭装备
     */
    public int sacrificeItemByName(Long userId, String itemName) {
        List<Equipment> equipments = equipmentRepository.findByUserId(userId).stream()
                .filter(e -> !e.getEquipped() && e.getName().contains(itemName))
                .toList();

        if (equipments.isEmpty()) {
            throw new IllegalStateException("背包中未找到 [%s]".formatted(itemName));
        }

        if (equipments.size() > 1) {
            throw new IllegalStateException("找到多个 [%s]，请使用更精确的名称".formatted(itemName));
        }

        return sacrificeItem(userId, equipments.getFirst().getId());
    }

    /**
     * 按输入（编号或名称）献祭装备
     */
    public int sacrificeItemByInput(Long userId, String input) {
        var result = itemResolver.resolveEquipment(userId, input);
        return switch (result) {
            case ItemResolver.Found(var equipment, var index) -> sacrificeItem(userId, equipment.getId());
            case ItemResolver.NotFound(var name) ->
                    throw new IllegalStateException("背包中未找到 [" + name + "] 相关的装备");
            case ItemResolver.Ambiguous(var name, var candidates) -> {
                var sb = new StringBuilder("找到多个装备，请使用编号：\n");
                for (var e : candidates) {
                    sb.append(e.index()).append(". ").append(e.name()).append(" [").append(e.metadata()).append("]\n");
                }
                throw new IllegalStateException(sb.toString().strip());
            }
        };
    }

    /**
     * 批量献祭指定品质的装备
     */
    public Map<String, Integer> sacrificeItemsByQuality(Long userId, String quality) {
        // TODO: 实现批量献祭逻辑
        // 1. 查询背包中所有指定品质的装备
        // 2. 逐个献祭
        // 3. 返回统计信息

        return Map.of(
                "count", 0,
                "totalAura", 0
        );
    }

    // ===================== 种植/收获系统 =====================

    /**
     * 在指定坐标种植灵药
     */
    public FarmCellVO plantCrop(Long userId, String position, Integer cropId, String cropName, WuxingType element, int cropTier) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        Map<String, Object> existingCell = getCellByPosition(fudi, position);
        if (existingCell != null && !CellType.FARM.getCode().equals(existingCell.get("type"))) {
            throw new IllegalStateException("坐标 " + position + " 已有其他类型地块");
        }

        int cellLevel = existingCell != null ?
                (Integer) existingCell.getOrDefault("cell_level", 1) : 1;
        int minLevel = Math.max(1, cropTier);
        if (cellLevel < minLevel) {
            throw new IllegalStateException(
                    "作物等阶(T%d)需要至少Lv%d灵田，当前灵田Lv%d".formatted(cropTier, minLevel, cellLevel));
        }

        // 消耗灵气
        int auraCost = cropTier * 5;
        fudi.updateAura();
        if (fudi.getAuraCurrent() < auraCost) {
            throw new IllegalStateException("灵气不足（需要 %d，当前 %d）".formatted(auraCost, fudi.getAuraCurrent()));
        }
        fudi.setAuraCurrent(fudi.getAuraCurrent() - auraCost);

        double levelSpeedMultiplier = getLevelSpeedMultiplier(cellLevel, minLevel);
        double growthModifier = calculateGrowthModifier(fudi, position, element);
        double baseGrowthHours = getBaseGrowthHours(cropId);
        double actualGrowthHours = baseGrowthHours / (growthModifier * levelSpeedMultiplier);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime matureTime = now.plusHours((long) actualGrowthHours);

        int maxHarvest = 1 + cropTier / 3;
        boolean isPerennial = maxHarvest > 1;

        Map<String, Object> farmCell = existingCell != null ? existingCell : new HashMap<>();
        farmCell.put("pos", position);
        farmCell.put("type", CellType.FARM.getCode());
        farmCell.put("cell_level", cellLevel);
        farmCell.put("element", element.name());
        farmCell.put("crop_id", cropId);
        farmCell.put("crop_name", cropName);
        farmCell.put("crop_tier", cropTier);
        farmCell.put("growth_progress", 0.0);
        farmCell.put("plant_time", now.toString());
        farmCell.put("mature_time", matureTime.toString());
        farmCell.put("base_growth_hours", baseGrowthHours);
        farmCell.put("growth_modifier", growthModifier);
        farmCell.put("level_speed_multiplier", levelSpeedMultiplier);
        farmCell.put("harvest_count", 0);
        farmCell.put("max_harvest", maxHarvest);
        farmCell.put("is_perennial", isPerennial);

        if (existingCell == null) {
            addCellToGrid(fudi, farmCell);
        }
        fudiRepository.save(fudi);

        log.info("用户 {} 在坐标 {} 种植 {} (T%d, %s)".formatted(userId, position, cropName, cropTier, isPerennial ? "多季" : "单季"), cropTier, isPerennial ? "多季" : "单季");

        return FarmCellVO.builder()
                .position(position)
                .cellLevel(cellLevel)
                .cropId(cropId)
                .cropName(cropName)
                .element(element)
                .plantTime(now)
                .matureTime(matureTime)
                .growthProgress(0.0)
                .isMature(false)
                .baseGrowthHours(baseGrowthHours)
                .growthModifier(growthModifier)
                .actualGrowthHours(actualGrowthHours)
                .harvestCount(0)
                .maxHarvest(maxHarvest)
                .isPerennial(isPerennial)
                .build();
    }

    /**
     * 根据作物名称种植灵药（从背包消耗种子）
     */
    public FarmCellVO plantCropByName(Long userId, String position, String cropName) {
        ItemTemplate seedTemplate = findSeedTemplateByName(cropName);

        var stackableItem = stackableItemRepository.findByUserIdAndTemplateId(userId, seedTemplate.getId())
                .orElseThrow(() -> new IllegalStateException("背包中没有 [%s]".formatted(cropName)));

        itemService.reduceStackableItem(userId, seedTemplate.getId(), 1);

        WuxingType element = resolveElement(seedTemplate);

        int growTime = seedTemplate.getGrowTime() != null ? seedTemplate.getGrowTime() : 24;
        int cropTier = getCropTier(growTime);

        Integer cropId = seedTemplate.getId().intValue();

        // 检查坐标已有灵田时是否需要铲掉重种
        Fudi fudi = getFudiByUserId(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));
        Map<String, Object> existingCell = getCellByPosition(fudi, position);
        if (existingCell != null && !CellType.FARM.getCode().equals(existingCell.get("type"))) {
            throw new IllegalStateException("坐标 " + position + " 已有其他类型地块");
        }

        return plantCrop(userId, position, cropId, cropName, element, cropTier);
    }

    /**
     * 按输入（编号或名称）种植灵药
     */
    public FarmCellVO plantCropByInput(Long userId, String position, String input) {
        var result = itemResolver.resolveSeed(userId, input);
        return switch (result) {
            case ItemResolver.Found(var template, var index) -> {
                var stackableItem = stackableItemRepository
                        .findByUserIdAndTemplateId(userId, template.getId())
                        .orElseThrow(() -> new IllegalStateException("背包中没有 [" + template.getName() + "]"));

                itemService.reduceStackableItem(userId, template.getId(), 1);

                WuxingType element = resolveElement(template);
                int growTime = template.getGrowTime() != null ? template.getGrowTime() : 24;
                int cropTier = getCropTier(growTime);
                Integer cropId = template.getId().intValue();
                String cropName = template.getName();

                Fudi fudi = getFudiByUserId(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));
                Map<String, Object> existingCell = getCellByPosition(fudi, position);
                if (existingCell != null && !CellType.FARM.getCode().equals(existingCell.get("type"))) {
                    throw new IllegalStateException("坐标 " + position + " 已有其他类型地块");
                }

                yield plantCrop(userId, position, cropId, cropName, element, cropTier);
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

    /**
     * 收获指定坐标的成熟灵药
     */
    public Map<String, Object> harvestCrop(Long userId, String position) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        Map<String, Object> cell = getCellByPosition(fudi, position);
        if (cell == null) {
            throw new IllegalStateException("坐标 " + position + " 不存在地块");
        }

        if (!CellType.FARM.getCode().equals(cell.get("type"))) {
            throw new IllegalStateException("坐标 " + position + " 不是灵田");
        }

        updateGrowthProgress(cell);

        Double progress = (Double) cell.get("growth_progress");
        boolean isPerennial = Boolean.TRUE.equals(cell.get("is_perennial"));

        // 枯萎检查（非多季作物）
        if (!isPerennial && progress != null && progress > 1.0 && isWilted(cell)) {
            String cropName = (String) cell.get("crop_name");
            String pos = (String) cell.get("pos");
            removeCellFromGrid(fudi, pos);
            fudiRepository.save(fudi);
            throw new IllegalStateException("%s 已枯萎（超过成熟时间两倍未收获）".formatted(cropName));
        }

        if (progress == null || progress < 1.0) {
            throw new IllegalStateException("灵药尚未成熟");
        }

        String cropName = (String) cell.get("crop_name");
        Integer cropId = (Integer) cell.get("crop_id");
        int yield = calculateYield(cropId, fudi.getCoreLevel());

        // 跨类型五行加成：相邻PEN相生 +20%产量
        WuxingType element = cell.containsKey("element") ? WuxingType.valueOf((String) cell.get("element")) : null;
        if (element != null) {
            double crossBonus = calculateCrossTypeBonus(fudi, position, element, "FARM");
            yield = (int) Math.round(yield * crossBonus);
        }

        int harvestCount = (Integer) cell.getOrDefault("harvest_count", 0) + 1;
        int maxHarvest = (Integer) cell.getOrDefault("max_harvest", 1);

        if (isPerennial && harvestCount < maxHarvest) {
            // 多季：保留地块，重置进度
            cell.put("harvest_count", harvestCount);
            cell.put("growth_progress", 0.0);
            cell.put("plant_time", LocalDateTime.now().toString());
            double baseGrowthHours = (Double) cell.get("base_growth_hours");
            double growthModifier = (Double) cell.getOrDefault("growth_modifier", 1.0);
            double levelSpeed = (Double) cell.getOrDefault("level_speed_multiplier", 1.0);
            double actualHours = baseGrowthHours / (growthModifier * levelSpeed);
            cell.put("mature_time", LocalDateTime.now().plusHours((long) actualHours).toString());
        } else {
            removeCellFromGrid(fudi, position);
        }

        fudi.setSpiritEnergy(Math.max(0, fudi.getSpiritEnergy() - 5));
        fudiRepository.save(fudi);

        // TODO: 添加到背包
        log.info("用户 {} 收获 {}，获得 {} x%d".formatted(userId, position, cropName, yield));

        return Map.of(
                "cropName", cropName,
                "cropId", cropId,
                "yield", yield
        );
    }

    /**
     * 收获所有成熟灵田
     */
    public Map<String, Object> harvestAllCrops(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        if (fudi.getGridLayout() == null || !fudi.getGridLayout().containsKey("cells")) {
            return Map.of("harvested", 0, "totalYield", 0);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");

        List<String> toRemove = new ArrayList<>();
        int totalYield = 0;
        int harvestedCount = 0;

        for (Map<String, Object> cell : cells) {
            if (!CellType.FARM.getCode().equals(cell.get("type"))) {
                continue;
            }

            updateGrowthProgress(cell);

            Double progress = (Double) cell.get("growth_progress");
            if (progress == null || progress < 1.0) continue;

            boolean isPerennial = Boolean.TRUE.equals(cell.get("is_perennial"));

            // 枯萎检查（非多季）
            if (!isPerennial && progress > 1.0 && isWilted(cell)) {
                toRemove.add((String) cell.get("pos"));
                continue;
            }

            String cropName = (String) cell.get("crop_name");
            Integer cropId = (Integer) cell.get("crop_id");
            int yield = calculateYield(cropId, fudi.getCoreLevel());

            WuxingType element = cell.containsKey("element") ? WuxingType.valueOf((String) cell.get("element")) : null;
            if (element != null) {
                double crossBonus = calculateCrossTypeBonus(fudi, (String) cell.get("pos"), element, "FARM");
                yield = (int) Math.round(yield * crossBonus);
            }

            int harvestCount = (Integer) cell.getOrDefault("harvest_count", 0) + 1;
            int maxHarvest = (Integer) cell.getOrDefault("max_harvest", 1);

            if (isPerennial && harvestCount < maxHarvest) {
                cell.put("harvest_count", harvestCount);
                cell.put("growth_progress", 0.0);
                cell.put("plant_time", LocalDateTime.now().toString());
                double baseGrowthHours = (Double) cell.get("base_growth_hours");
                double growthModifier = (Double) cell.getOrDefault("growth_modifier", 1.0);
                double levelSpeed = (Double) cell.getOrDefault("level_speed_multiplier", 1.0);
                double actualHours = baseGrowthHours / (growthModifier * levelSpeed);
                cell.put("mature_time", LocalDateTime.now().plusHours((long) actualHours).toString());
            } else {
                toRemove.add((String) cell.get("pos"));
            }

            totalYield += yield;
            harvestedCount++;
            log.info("收获 {}，获得 {} x{}", cell.get("pos"), cropName, yield);
        }

        for (String pos : toRemove) {
            removeCellFromGrid(fudi, pos);
        }

        fudi.setSpiritEnergy(Math.max(0, fudi.getSpiritEnergy() - harvestedCount * 5));
        fudiRepository.save(fudi);

        return Map.of(
                "harvested", harvestedCount,
                "totalYield", totalYield
        );
    }

    /**
     * 更新生长进度（懒加载）
     */
    private void updateGrowthProgress(Map<String, Object> cell) {
        if (cell.containsKey("mature_time")) {
            LocalDateTime matureTime = LocalDateTime.parse((String) cell.get("mature_time"));
            LocalDateTime now = LocalDateTime.now();

            if (now.isAfter(matureTime) || now.isEqual(matureTime)) {
                cell.put("growth_progress", 1.0);
            } else {
                LocalDateTime plantTime = LocalDateTime.parse((String) cell.get("plant_time"));
                long totalSeconds = java.time.Duration.between(plantTime, matureTime).getSeconds();
                long elapsedSeconds = java.time.Duration.between(plantTime, now).getSeconds();

                double progress = Math.min(1.0, (double) elapsedSeconds / totalSeconds);
                cell.put("growth_progress", progress);
            }
        }
    }

    /**
     * 计算产量
     */
    private int calculateYield(Integer cropId, int coreLevel) {
        // 基础产量 1-3，聚灵核心等级提供加成
        int baseYield = 1 + new Random().nextInt(3);
        int bonus = coreLevel / 5; // 每5级核心等级+1产量
        return baseYield + bonus;
    }

    /**
     * 获取基础生长时间（小时），从物品模板表查询
     */
    private double getBaseGrowthHours(Integer cropId) {
        return itemTemplateRepository.findById((long) cropId)
                .map(template -> template.getGrowTime() != null ? template.getGrowTime().doubleValue() : 5.0)
                .orElse(5.0);
    }

    // ===================== 建造/拆除系统 =====================

    /**
     * 建造地块
     */
    public Map<String, Object> buildCell(Long userId, String position, CellType type) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        if (!isCellEmpty(fudi, position)) {
            throw new IllegalStateException("坐标 " + position + " 已有地块");
        }

        Map<String, Object> cell = new HashMap<>();
        cell.put("pos", position);
        cell.put("type", type.getCode());
        cell.put("cell_level", 1);

        switch (type) {
            case FARM -> {
                cell.put("element", WuxingType.WOOD.name());
            }
            case PEN -> {
                cell.put("element", WuxingType.WOOD.name());
            }
            case NODE -> {
                cell.put("level", 1);
                cell.put("element", WuxingType.METAL.name()); // 阵眼默认金属性
                cell.put("durability", 100);
            }
            default -> throw new IllegalStateException("不支持的地块类型");
        }

        addCellToGrid(fudi, cell);
        fudiRepository.save(fudi);

        log.info("用户 {} 在坐标 {} 建造 {}", userId, position, type.getChineseName());

        return Map.of(
                "position", position,
                "type", type.getChineseName()
        );
    }

    /**
     * 拆除地块
     */
    public Map<String, Object> removeCell(Long userId, String position) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        if (isCellEmpty(fudi, position)) {
            throw new IllegalStateException("坐标 " + position + " 为空");
        }

        Map<String, Object> cell = getCellByPosition(fudi, position);
        if (cell == null) {
            throw new IllegalStateException("坐标 " + position + " 不存在");
        }

        CellType type = CellType.fromCode((String) cell.get("type"));
        removeCellFromGrid(fudi, position);
        fudiRepository.save(fudi);

        log.info("用户 {} 拆除坐标 {} 的 {}", userId, position, type.getChineseName());

        return Map.of(
                "position", position,
                "type", type.getChineseName()
        );
    }

    // ===================== 地块升级系统 =====================

    /**
     * 升级地块等级
     */
    public Map<String, Object> upgradeCell(Long userId, String position) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        Map<String, Object> cell = getCellByPosition(fudi, position);
        if (cell == null) {
            throw new IllegalStateException("坐标 " + position + " 不存在地块");
        }

        int currentLevel = (Integer) cell.getOrDefault("cell_level", 1);
        if (currentLevel >= 5) {
            throw new IllegalStateException("已是最高等级 Lv5");
        }

        int cost = currentLevel == 1 ? 200 : currentLevel == 2 ? 400 : currentLevel == 3 ? 800 : 1600;
        fudi.updateAura();
        if (fudi.getAuraCurrent() < cost) {
            throw new IllegalStateException("灵气不足（需要 %d，当前 %d）".formatted(cost, fudi.getAuraCurrent()));
        }
        fudi.setAuraCurrent(fudi.getAuraCurrent() - cost);

        int newLevel = currentLevel + 1;
        cell.put("cell_level", newLevel);
        fudiRepository.save(fudi);

        CellType type = CellType.fromCode((String) cell.get("type"));
        log.info("用户 {} 升级坐标 {} 的 {} Lv{} -> Lv{}", userId, position, type.getChineseName(), currentLevel, newLevel);

        return Map.of("position", position, "type", type.getChineseName(), "oldLevel", currentLevel, "newLevel", newLevel);
    }

    // ===================== 灵兽系统 — 孵化 =====================

    /**
     * 孵化灵兽
     */
    public PenCellVO hatchBeast(Long userId, String position, String eggName) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        Map<String, Object> cell = getCellByPosition(fudi, position);
        if (cell == null || !CellType.PEN.getCode().equals(cell.get("type"))) {
            throw new IllegalStateException("坐标 " + position + " 不是兽栏");
        }

        if (cell.containsKey("beast_id") && (Integer) cell.get("beast_id") > 0) {
            throw new IllegalStateException("该兽栏已有灵兽，请先放生");
        }

        ItemTemplate eggTemplate = itemTemplateRepository.findByType(ItemType.BEAST_EGG).stream()
                .filter(t -> t.getName().equals(eggName) || t.getName().contains(eggName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未找到兽卵: %s".formatted(eggName)));

        // 消耗兽卵
        var stackableItem = stackableItemRepository.findByUserIdAndTemplateId(userId, eggTemplate.getId())
                .orElseThrow(() -> new IllegalStateException("背包中没有 [%s]".formatted(eggName)));
        itemService.reduceStackableItem(userId, eggTemplate.getId(), 1);

        int tier = getCropTier(eggTemplate.getGrowTime() != null ? eggTemplate.getGrowTime() : 72);

        // 等级检查
        int cellLevel = (Integer) cell.getOrDefault("cell_level", 1);
        if (cellLevel < tier) {
            throw new IllegalStateException("灵兽等阶(T%d)需要至少Lv%d兽栏".formatted(tier, tier));
        }

        // 消耗灵气
        int hatchAuraCost = tier * 200 + 200;
        fudi.updateAura();
        if (fudi.getAuraCurrent() < hatchAuraCost) {
            throw new IllegalStateException("灵气不足（孵化需 %d，当前 %d）".formatted(hatchAuraCost, fudi.getAuraCurrent()));
        }
        fudi.setAuraCurrent(fudi.getAuraCurrent() - hatchAuraCost);

        // 随机品质
        BeastQuality quality = rollBeastQuality();
        WuxingType element = resolveElement(eggTemplate);

        // 变异概率 5%
        boolean isMutant = new Random().nextInt(100) < 5;
        List<String> mutationTraits = new ArrayList<>();
        if (isMutant) {
            mutationTraits.add(rollRandomTrait());
        }

        double levelSpeed = getLevelSpeedMultiplier(cellLevel, tier);
        double wuxingModifier = calculateGrowthModifier(fudi, position, element);
        double productionInterval = 4.0 / (levelSpeed * (wuxingModifier > 0 ? wuxingModifier : 1.0));
        double baseHatchHours = 24 + tier * 8;
        double hatchHours = baseHatchHours / (levelSpeed * (wuxingModifier > 0 ? wuxingModifier : 1.0));

        String beastName = eggName.replace("兽卵", "").replace("蛋", "灵兽");
        LocalDateTime now = LocalDateTime.now();

        cell.put("beast_id", eggTemplate.getId().intValue());
        cell.put("beast_name", beastName);
        cell.put("tier", tier);
        cell.put("element", element.name());
        cell.put("quality", quality.getCode());
        cell.put("quality_ordinal", quality.getOrder());
        cell.put("is_mutant", isMutant);
        cell.put("mutation_traits", mutationTraits);
        cell.put("is_incubating", true);
        cell.put("hatch_time", now.toString());
        cell.put("mature_time", now.plusHours((long) hatchHours).toString());
        cell.put("production_stored", 0);
        cell.put("last_production_time", now.toString());
        cell.put("production_interval_hours", productionInterval);
        cell.put("lifespan_days", tier * 7.0 * quality.getLifespanMultiplier());
        cell.put("birth_time", now.toString());
        cell.put("power_score", tier * 10);
        cell.put("evolution_count", 0);

        fudiRepository.save(fudi);
        log.info("用户 {} 在坐标 {} 孵化 {} (T{}, {}{})", userId, position, beastName, tier, quality.getChineseName(), isMutant ? ", 变异" : "");

        return PenCellVO.builder()
                .position(position)
                .cellLevel(cellLevel)
                .beastId(eggTemplate.getId().intValue())
                .beastName(beastName)
                .tier(tier)
                .element(element)
                .quality(quality.getChineseName())
                .qualityOrdinal(quality.getOrder())
                .isMutant(isMutant)
                .mutationTraits(mutationTraits)
                .isIncubating(true)
                .hatchTime(now)
                .matureTime(now.plusHours((long) hatchHours))
                .productionIntervalHours(productionInterval)
                .productionStored(0)
                .powerScore(tier * 10)
                .lifespanDays(tier * 7.0 * quality.getLifespanMultiplier())
                .birthTime(now)
                .build();
    }

    /**
     * 按输入（编号或名称）孵化灵兽
     */
    public PenCellVO hatchBeastByInput(Long userId, String position, String input) {
        var result = itemResolver.resolveEgg(userId, input);
        return switch (result) {
            case ItemResolver.Found(var template, var index) -> {
                var stackableItem = stackableItemRepository
                        .findByUserIdAndTemplateId(userId, template.getId())
                        .orElseThrow(() -> new IllegalStateException("背包中没有 [" + template.getName() + "]"));

                itemService.reduceStackableItem(userId, template.getId(), 1);

                var fudi = getFudiByUserId(userId)
                        .orElseThrow(() -> new IllegalStateException("未找到福地"));

                var cell = getCellByPosition(fudi, position);
                if (cell == null || !CellType.PEN.getCode().equals(cell.get("type"))) {
                    throw new IllegalStateException("坐标 " + position + " 不是兽栏");
                }
                if (cell.containsKey("beast_id") && (Integer) cell.get("beast_id") > 0) {
                    throw new IllegalStateException("该兽栏已有灵兽，请先放生");
                }

                int tier = getCropTier(template.getGrowTime() != null ? template.getGrowTime() : 72);

                int cellLevel = (Integer) cell.getOrDefault("cell_level", 1);
                if (cellLevel < tier) {
                    throw new IllegalStateException("灵兽等阶(T%d)需要至少Lv%d兽栏".formatted(tier, tier));
                }

                int hatchAuraCost = tier * 200 + 200;
                fudi.updateAura();
                if (fudi.getAuraCurrent() < hatchAuraCost) {
                    throw new IllegalStateException("灵气不足（孵化需 %d，当前 %d）".formatted(hatchAuraCost, fudi.getAuraCurrent()));
                }
                fudi.setAuraCurrent(fudi.getAuraCurrent() - hatchAuraCost);

                BeastQuality quality = rollBeastQuality();
                WuxingType element = resolveElement(template);

                boolean isMutant = new java.util.Random().nextInt(100) < 5;
                List<String> mutationTraits = new ArrayList<>();
                if (isMutant) {
                    mutationTraits.add(rollRandomTrait());
                }

                double levelSpeed = getLevelSpeedMultiplier(cellLevel, tier);
                double wuxingModifier = calculateGrowthModifier(fudi, position, element);
                double productionInterval = 4.0 / (levelSpeed * (wuxingModifier > 0 ? wuxingModifier : 1.0));
                double baseHatchHours = 24 + tier * 8;
                double hatchHours = baseHatchHours / (levelSpeed * (wuxingModifier > 0 ? wuxingModifier : 1.0));

                String beastName = template.getName().replace("兽卵", "").replace("蛋", "灵兽");
                LocalDateTime now = LocalDateTime.now();

                cell.put("beast_id", template.getId().intValue());
                cell.put("beast_name", beastName);
                cell.put("tier", tier);
                cell.put("element", element.name());
                cell.put("quality", quality.getCode());
                cell.put("quality_ordinal", quality.getOrder());
                cell.put("is_mutant", isMutant);
                cell.put("mutation_traits", mutationTraits);
                cell.put("is_incubating", true);
                cell.put("hatch_time", now.toString());
                cell.put("mature_time", now.plusHours((long) hatchHours).toString());
                cell.put("production_stored", 0);
                cell.put("last_production_time", now.toString());
                cell.put("production_interval_hours", productionInterval);
                cell.put("lifespan_days", tier * 7.0 * quality.getLifespanMultiplier());
                cell.put("birth_time", now.toString());
                cell.put("power_score", tier * 10);
                cell.put("evolution_count", 0);

                fudiRepository.save(fudi);

                yield PenCellVO.builder()
                        .position(position)
                        .cellLevel(cellLevel)
                        .beastId(template.getId().intValue())
                        .beastName(beastName)
                        .tier(tier)
                        .element(element)
                        .quality(quality.getChineseName())
                        .qualityOrdinal(quality.getOrder())
                        .isMutant(isMutant)
                        .mutationTraits(mutationTraits)
                        .isIncubating(true)
                        .hatchTime(now)
                        .matureTime(now.plusHours((long) hatchHours))
                        .productionIntervalHours(productionInterval)
                        .productionStored(0)
                        .powerScore(tier * 10)
                        .lifespanDays(tier * 7.0 * quality.getLifespanMultiplier())
                        .birthTime(now)
                        .build();
            }
            case ItemResolver.NotFound(var name) -> throw new IllegalStateException("背包中未找到兽卵 [" + name + "]");
            case ItemResolver.Ambiguous(var name, var candidates) -> {
                var sb = new StringBuilder("找到多个兽卵，请使用编号：\n");
                for (var e : candidates) {
                    sb.append(e.index()).append(". ").append(e.name()).append(" x").append(e.quantity()).append(" (").append(e.metadata()).append(")\n");
                }
                throw new IllegalStateException(sb.toString().strip());
            }
        };
    }

    // ===================== 灵兽系统 — 产出 =====================

    /**
     * 收取指定兽栏产出
     */
    public BeastProduceVO collectProduce(Long userId, String position) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        Map<String, Object> cell = getCellByPosition(fudi, position);
        if (cell == null || !CellType.PEN.getCode().equals(cell.get("type"))) {
            throw new IllegalStateException("坐标 " + position + " 不是兽栏");
        }

        if (Boolean.TRUE.equals(cell.get("is_incubating"))) {
            throw new IllegalStateException("灵兽尚在孵化中");
        }

        // 灵气耗尽检查
        checkBeastAuraDepletion(fudi, cell);

        // 懒加载计算产出
        updateBeastProduction(cell, getBeastQuality(cell), getBeastWuxingModifier(fudi, position, cell), fudi);
        int stored = (Integer) cell.getOrDefault("production_stored", 0);
        if (stored <= 0) {
            throw new IllegalStateException("暂无产出可收取");
        }

        String itemName = (String) cell.getOrDefault("beast_name", "灵兽") + "材料";
        // TODO: 添加到背包
        // itemService.addStackableItem(userId, itemTemplateId, stored);

        cell.put("production_stored", 0);
        fudiRepository.save(fudi);

        log.info("用户 {} 收取坐标 {} 的灵兽产出 {} 件", userId, position, stored);

        return BeastProduceVO.builder()
                .position(position)
                .beastName((String) cell.get("beast_name"))
                .totalProduced(stored)
                .itemName(itemName)
                .build();
    }

    /**
     * 收取所有兽栏产出
     */
    public Map<String, Object> collectAllProduce(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        if (fudi.getGridLayout() == null || !fudi.getGridLayout().containsKey("cells")) {
            return Map.of("totalPositions", 0, "totalItems", 0);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");
        int totalItems = 0;
        int totalPositions = 0;

        for (Map<String, Object> cell : cells) {
            if (!CellType.PEN.getCode().equals(cell.get("type"))) continue;
            if (Boolean.TRUE.equals(cell.get("is_incubating"))) continue;

            checkBeastAuraDepletion(fudi, cell);
            BeastQuality quality = getBeastQuality(cell);
            double wuxingMod = getBeastWuxingModifier(fudi, (String) cell.get("pos"), cell);
            updateBeastProduction(cell, quality, wuxingMod, fudi);

            int stored = (Integer) cell.getOrDefault("production_stored", 0);
            if (stored > 0) {
                totalItems += stored;
                totalPositions++;
                cell.put("production_stored", 0);
            }
        }

        fudiRepository.save(fudi);
        return Map.of("totalPositions", totalPositions, "totalItems", totalItems);
    }

    // ===================== 灵兽系统 — 放生与进化 =====================

    /**
     * 放生灵兽，获得灵兽精华
     */
    public Map<String, Object> releaseBeast(Long userId, String position) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        Map<String, Object> cell = getCellByPosition(fudi, position);
        if (cell == null || !CellType.PEN.getCode().equals(cell.get("type"))) {
            throw new IllegalStateException("坐标 " + position + " 不是兽栏");
        }

        int tier = (Integer) cell.getOrDefault("tier", 1);
        BeastQuality quality = getBeastQuality(cell);
        double qualityOutput = quality.getOutputMultiplier();
        int essenceValue = (int) (tier * 200 * qualityOutput);

        String beastName = (String) cell.get("beast_name");

        // 清空兽栏
        cell.remove("beast_id");
        cell.remove("beast_name");
        cell.remove("tier");
        cell.remove("element");
        cell.remove("quality");
        cell.remove("quality_ordinal");
        cell.remove("is_mutant");
        cell.remove("mutation_traits");
        cell.remove("is_incubating");
        cell.remove("hatch_time");
        cell.remove("mature_time");
        cell.remove("production_stored");
        cell.remove("last_production_time");
        cell.remove("production_interval_hours");
        cell.remove("lifespan_days");
        cell.remove("birth_time");
        cell.remove("power_score");
        cell.remove("evolution_count");

        fudiRepository.save(fudi);

        // TODO: 添加灵兽精华到背包
        log.info("用户 {} 放生 {} (T{}/{})，获得灵兽精华(价值{}灵气)", userId, beastName, tier, quality.getChineseName(), essenceValue);

        return Map.of("beastName", beastName, "essenceValue", essenceValue);
    }

    /**
     * 进化灵兽（升阶）或品质突破（升品）
     */
    public PenCellVO evolveBeast(Long userId, String position, String mode) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        Map<String, Object> cell = getCellByPosition(fudi, position);
        if (cell == null || !CellType.PEN.getCode().equals(cell.get("type"))) {
            throw new IllegalStateException("坐标 " + position + " 不是兽栏");
        }
        if (Boolean.TRUE.equals(cell.get("is_incubating"))) {
            throw new IllegalStateException("灵兽尚在孵化中");
        }

        // 消耗进化石
        int stoneCount = "升品".equals(mode) ? 2 : 1;
        ItemTemplate stoneTemplate = itemTemplateRepository.findByType(ItemType.EVOLUTION_STONE).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("进化石模板未找到"));
        var stoneItem = stackableItemRepository.findByUserIdAndTemplateId(userId, stoneTemplate.getId())
                .orElseThrow(() -> new IllegalStateException("背包中没有进化石"));
        if (stoneItem.getQuantity() < stoneCount) {
            throw new IllegalStateException("需要 %d 个进化石（当前%d）".formatted(stoneCount, stoneItem.getQuantity()));
        }
        itemService.reduceStackableItem(userId, stoneTemplate.getId(), stoneCount);

        if ("升品".equals(mode)) {
            return breakthroughBeastQuality(fudi, cell, userId, position);
        } else {
            return evolveBeastTier(fudi, cell, userId, position);
        }
    }

    private PenCellVO evolveBeastTier(Fudi fudi, Map<String, Object> cell, Long userId, String position) {
        int currentTier = (Integer) cell.getOrDefault("tier", 1);
        if (currentTier >= 5) {
            throw new IllegalStateException("已是最高等阶 T5");
        }

        int cost = (currentTier + 1) * 200;
        fudi.updateAura();
        if (fudi.getAuraCurrent() < cost) {
            throw new IllegalStateException("灵气不足（进化需 %d，当前 %d）".formatted(cost, fudi.getAuraCurrent()));
        }
        fudi.setAuraCurrent(fudi.getAuraCurrent() - cost);

        int affectionBonus = fudi.getSpiritAffection() != null ? Math.min(15, fudi.getSpiritAffection() / 7) : 0;
        int successRate = 85 + affectionBonus;
        boolean success = new Random().nextInt(100) < successRate;

        if (!success) {
            double currentLifespan = (Double) cell.getOrDefault("lifespan_days", 7.0);
            cell.put("lifespan_days", currentLifespan * 0.5);
            fudiRepository.save(fudi);
            throw new IllegalStateException("进化失败！进化石和灵气已消耗，灵兽寿命减半");
        }

        int newTier = currentTier + 1;
        cell.put("tier", newTier);
        cell.put("power_score", newTier * 10);
        cell.put("birth_time", LocalDateTime.now().toString());
        cell.put("evolution_count", (Integer) cell.getOrDefault("evolution_count", 0) + 1);

        BeastQuality currentQuality = getBeastQuality(cell);
        double lifeMult = currentQuality.getLifespanMultiplier();
        cell.put("lifespan_days", newTier * 7.0 * lifeMult);

        // 10%概率连带品质升级
        boolean qualityUpgraded = false;
        if (new Random().nextInt(100) < 10 && currentQuality != BeastQuality.DIVINE) {
            BeastQuality newQuality = currentQuality.next();
            cell.put("quality", newQuality.getCode());
            cell.put("quality_ordinal", newQuality.getOrder());
            cell.put("lifespan_days", newTier * 7.0 * newQuality.getLifespanMultiplier());
            qualityUpgraded = true;
        }

        boolean mutationTriggered = rollMutation(15);
        if (mutationTriggered) {
            addMutationTrait(cell);
        }

        fudiRepository.save(fudi);
        log.info(
                "用户 {} 进化坐标 {} 的灵兽 T{}->T{}{}", userId, position, currentTier, newTier,
                qualityUpgraded ? " (品质连带提升!)" : ""
        );

        return buildPenCellVO(cell);
    }

    private PenCellVO breakthroughBeastQuality(Fudi fudi, Map<String, Object> cell, Long userId, String position) {
        BeastQuality currentQuality = getBeastQuality(cell);
        if (currentQuality == BeastQuality.DIVINE) {
            throw new IllegalStateException("已是最高品质神品");
        }

        BeastQuality nextQuality = currentQuality.next();
        int cost = nextQuality.getOrder() * 300;
        fudi.updateAura();
        if (fudi.getAuraCurrent() < cost) {
            throw new IllegalStateException("灵气不足（突破需 %d，当前 %d）".formatted(cost, fudi.getAuraCurrent()));
        }
        fudi.setAuraCurrent(fudi.getAuraCurrent() - cost);

        int affectionBonus = fudi.getSpiritAffection() != null ? Math.min(20, fudi.getSpiritAffection() / 5) : 0;
        int successRate = 60 + affectionBonus;
        boolean success = new Random().nextInt(100) < successRate;

        if (!success) {
            double currentLifespan = (Double) cell.getOrDefault("lifespan_days", 7.0);
            cell.put("lifespan_days", currentLifespan * 0.5);
            fudiRepository.save(fudi);
            throw new IllegalStateException("品质突破失败！进化石和灵气已消耗，灵兽寿命减半");
        }

        cell.put("quality", nextQuality.getCode());
        cell.put("quality_ordinal", nextQuality.getOrder());
        int tier = (Integer) cell.getOrDefault("tier", 1);
        cell.put("lifespan_days", tier * 7.0 * nextQuality.getLifespanMultiplier());

        boolean mutationTriggered = rollMutation(10);
        if (mutationTriggered) {
            addMutationTrait(cell);
        }

        fudiRepository.save(fudi);
        log.info("用户 {} 品质突破坐标 {} 的灵兽 {} -> {}", userId, position, currentQuality.getChineseName(), nextQuality.getChineseName());

        return buildPenCellVO(cell);
    }

    // ===================== 灵兽系统 — 灵气耗尽与寿命检查 =====================

    private void checkBeastAuraDepletion(Fudi fudi, Map<String, Object> cell) {
        fudi.updateAura();
        String beastName = (String) cell.getOrDefault("beast_name", "灵兽");

        int aura = fudi.getAuraCurrent();
        int auraMax = fudi.getAuraMax();
        if (aura <= 0) {
            // 开始蛰伏倒计时
            String depletionStartTime = (String) cell.get("aura_depletion_start");
            LocalDateTime now = LocalDateTime.now();
            if (depletionStartTime == null) {
                cell.put("aura_depletion_start", now.toString());
            } else {
                long depletedSeconds = java.time.Duration.between(LocalDateTime.parse(depletionStartTime), now).getSeconds();
                if (depletedSeconds >= 86400) { // 24h
                    // 灵兽逃逸
                    log.info("灵气持续耗尽24h，灵兽 {} 逃逸", beastName);
                    clearBeastCell(cell);
                    throw new IllegalStateException("灵气耗尽超过24小时，%s 已逃逸！".formatted(beastName));
                }
            }
        } else if (cell.containsKey("aura_depletion_start")) {
            cell.remove("aura_depletion_start");
        }
    }

    private void clearBeastCell(Map<String, Object> cell) {
        cell.remove("beast_id");
        cell.remove("beast_name");
        cell.remove("tier");
        cell.remove("element");
        cell.remove("quality");
        cell.remove("quality_ordinal");
        cell.remove("is_mutant");
        cell.remove("mutation_traits");
        cell.remove("is_incubating");
        cell.remove("hatch_time");
        cell.remove("mature_time");
        cell.remove("production_stored");
        cell.remove("last_production_time");
        cell.remove("production_interval_hours");
        cell.remove("lifespan_days");
        cell.remove("birth_time");
        cell.remove("power_score");
        cell.remove("evolution_count");
        cell.remove("aura_depletion_start");
    }

    // ===================== 辅助方法 =====================

    private int getCropTier(int growTime) {
        if (growTime <= 24) return 1;
        if (growTime <= 48) return 2;
        if (growTime <= 72) return 3;
        if (growTime <= 120) return 4;
        return 5;
    }

    private double getLevelSpeedMultiplier(int cellLevel, int minRequired) {
        if (cellLevel < minRequired) return 0.5; // 不应触发，前置检查已拦截
        int levelDiff = cellLevel - minRequired;
        return 1.0 + levelDiff * 0.15;
    }

    private double calculateCrossTypeBonus(Fudi fudi, String position, WuxingType element, String selfType) {
        String[] coords = position.split(",");
        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);
        String[] adjPositions = {(x - 1) + "," + y, (x + 1) + "," + y, x + "," + (y - 1), x + "," + (y + 1)};

        double bonus = 1.0;
        for (String adjPos : adjPositions) {
            Map<String, Object> adjCell = getCellByPosition(fudi, adjPos);
            if (adjCell == null || !adjCell.containsKey("element")) continue;
            String adjType = (String) adjCell.get("type");
            WuxingType adjElement = WuxingType.valueOf((String) adjCell.get("element"));

            if ("FARM".equals(selfType) && CellType.PEN.getCode().equals(adjType) && adjElement.generates(element)) {
                bonus *= 1.2;
            } else if ("PEN".equals(selfType) && CellType.FARM.getCode().equals(adjType) && adjElement.generates(element)) {
                bonus *= 1.2;
            }
        }
        return bonus;
    }

    private boolean isWilted(Map<String, Object> cell) {
        if (Boolean.TRUE.equals(cell.get("is_perennial"))) return false;
        if (!cell.containsKey("mature_time") || !cell.containsKey("base_growth_hours")) return false;
        LocalDateTime matureTime = LocalDateTime.parse((String) cell.get("mature_time"));
        double baseGrowthHours = (Double) cell.get("base_growth_hours");
        long maxSeconds = (long) (baseGrowthHours * 3600 * 2);
        long secondsSinceMature = java.time.Duration.between(matureTime, LocalDateTime.now()).getSeconds();
        return secondsSinceMature > maxSeconds;
    }

    private BeastQuality rollBeastQuality() {
        int roll = new Random().nextInt(1000);
        int cumulative = 0;
        for (BeastQuality q : BeastQuality.values()) {
            cumulative += q.getHatchWeight();
            if (roll < cumulative) return q;
        }
        return BeastQuality.MORTAL;
    }

    private String rollRandomTrait() {
        return MutationTrait.values()[new Random().nextInt(MutationTrait.values().length)].getCode();
    }

    private boolean rollMutation(int chancePercent) {
        return new Random().nextInt(100) < chancePercent;
    }

    private void addMutationTrait(Map<String, Object> cell) {
        @SuppressWarnings("unchecked")
        List<String> traits = (List<String>) cell.getOrDefault("mutation_traits", new ArrayList<>());
        if (traits.size() >= 2) return;
        String newTrait = rollRandomTrait();
        if (!traits.contains(newTrait)) {
            traits.add(newTrait);
            cell.put("mutation_traits", traits);
            cell.put("is_mutant", true);
        }
    }

    private BeastQuality getBeastQuality(Map<String, Object> cell) {
        try {
            return BeastQuality.fromCode((String) cell.getOrDefault("quality", "mortal"));
        } catch (IllegalArgumentException e) {
            return BeastQuality.MORTAL;
        }
    }

    private double getBeastWuxingModifier(Fudi fudi, String position, Map<String, Object> cell) {
        if (cell.containsKey("element")) {
            WuxingType element = WuxingType.valueOf((String) cell.get("element"));
            double wuxMod = calculateGrowthModifier(fudi, position, element);
            double crossBonus = calculateCrossTypeBonus(fudi, position, element, "PEN");
            return (wuxMod > 0 ? wuxMod : 1.0) * crossBonus;
        }
        return 1.0;
    }

    @SuppressWarnings("unchecked")
    private void updateBeastProduction(Map<String, Object> cell, BeastQuality quality, double wuxingModifier, Fudi fudi) {
        if (!cell.containsKey("last_production_time") || !cell.containsKey("mature_time")) return;

        LocalDateTime matureTime = LocalDateTime.parse((String) cell.get("mature_time"));
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(matureTime)) {
            // 还未孵化完成
            cell.put("is_incubating", true);
            return;
        }
        cell.put("is_incubating", false);

        // 寿命检查
        if (cell.containsKey("birth_time") && cell.containsKey("lifespan_days")) {
            LocalDateTime birthTime = LocalDateTime.parse((String) cell.get("birth_time"));
            double lifespanDays = (Double) cell.get("lifespan_days");
            long livedSeconds = java.time.Duration.between(birthTime, now).getSeconds();
            if (livedSeconds >= lifespanDays * 86400) {
                // 善终
                String beastName = (String) cell.get("beast_name");
                int tier = (Integer) cell.getOrDefault("tier", 1);
                int essenceValue = (int) (tier * 200 * quality.getOutputMultiplier());
                clearBeastCell(cell);
                log.info("灵兽 {} 寿终正寝，获得灵兽精华(价值{}灵气)", beastName, essenceValue);
                return;
            }
        }

        // 灵气耗尽则产出减半
        double auraRatio = fudi.getAuraCurrent().doubleValue() / fudi.getAuraMax().doubleValue();
        double auraFactor = auraRatio <= 0 ? 0 : auraRatio <= 0.1 ? 0.5 : 1.0;

        LocalDateTime lastProduction = LocalDateTime.parse((String) cell.get("last_production_time"));
        double intervalHours = (Double) cell.getOrDefault("production_interval_hours", 4.0);
        long intervalSeconds = (long) (intervalHours * 3600);
        if (intervalSeconds <= 0) intervalSeconds = 14400;

        long elapsed = java.time.Duration.between(lastProduction, now).getSeconds();
        int cycles = (int) (elapsed / intervalSeconds);
        if (cycles <= 0) return;

        int tier = (Integer) cell.getOrDefault("tier", 1);
        int perCycle = (int) Math.round(tier * quality.getOutputMultiplier() * wuxingModifier * auraFactor * (1 + new Random().nextInt(tier + 1)) / 2.0);
        int produced = Math.max(1, perCycle * cycles);

        int currentStored = (Integer) cell.getOrDefault("production_stored", 0);

        // 产出上限检查
        int maxStorage = tier * 20;
        int newStored = Math.min(maxStorage, currentStored + produced);

        cell.put("production_stored", newStored);
        cell.put("last_production_time", now.toString());
    }

    private PenCellVO buildPenCellVO(Map<String, Object> cell) {
        @SuppressWarnings("unchecked")
        List<String> traits = (List<String>) cell.getOrDefault("mutation_traits", List.of());
        WuxingType element = cell.containsKey("element") ? WuxingType.valueOf((String) cell.get("element")) : null;
        BeastQuality quality = getBeastQuality(cell);

        return PenCellVO.builder()
                .position((String) cell.get("pos"))
                .cellLevel((Integer) cell.getOrDefault("cell_level", 1))
                .beastId((Integer) cell.getOrDefault("beast_id", 0))
                .beastName((String) cell.getOrDefault("beast_name", "未知灵兽"))
                .tier((Integer) cell.getOrDefault("tier", 1))
                .element(element)
                .quality(quality.getChineseName())
                .qualityOrdinal(quality.getOrder())
                .isMutant(Boolean.TRUE.equals(cell.get("is_mutant")))
                .mutationTraits(traits)
                .isIncubating(Boolean.TRUE.equals(cell.get("is_incubating")))
                .hatchTime(cell.containsKey("hatch_time") ? LocalDateTime.parse((String) cell.get("hatch_time")) : null)
                .matureTime(cell.containsKey("mature_time") ? LocalDateTime.parse((String) cell.get("mature_time")) : null)
                .productionIntervalHours((Double) cell.getOrDefault("production_interval_hours", 4.0))
                .productionStored((Integer) cell.getOrDefault("production_stored", 0))
                .powerScore((Integer) cell.getOrDefault("power_score", 10))
                .lifespanDays((Double) cell.getOrDefault("lifespan_days", 7.0))
                .birthTime(cell.containsKey("birth_time") ? LocalDateTime.parse((String) cell.get("birth_time")) : null)
                .build();
    }

    /**
     * 获取福地网格状态（供LLM查询可用坐标）
     */
    public Map<String, Object> getGridStatus(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        int gridSize = fudi.getGridSize();
        List<Map<String, Object>> occupiedCells = new ArrayList<>();
        List<String> emptyPositions = new ArrayList<>();

        // 收集已占地块信息
        if (fudi.getGridLayout() != null && fudi.getGridLayout().containsKey("cells")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");

            for (Map<String, Object> cell : cells) {
                Map<String, Object> cellInfo = new HashMap<>();
                cellInfo.put("position", cell.get("pos"));
                cellInfo.put("type", cell.get("type"));

                if (CellType.FARM.getCode().equals(cell.get("type"))) {
                    cellInfo.put("cropName", cell.get("crop_name"));
                    cellInfo.put("element", cell.get("element"));

                    // 懒加载计算生长进度
                    updateGrowthProgress(cell);
                    Double progress = (Double) cell.get("growth_progress");
                    cellInfo.put("growthProgress", progress);
                    cellInfo.put("isMature", progress != null && progress >= 1.0);
                    cellInfo.put("cellLevel", cell.get("cell_level"));
                } else if (CellType.PEN.getCode().equals(cell.get("type"))) {
                    cellInfo.put("beastName", cell.getOrDefault("beast_name", "空兽栏"));
                    cellInfo.put("tier", cell.get("tier"));
                    cellInfo.put("quality", cell.get("quality"));
                    cellInfo.put("productionStored", cell.getOrDefault("production_stored", 0));
                    cellInfo.put("isIncubating", cell.get("is_incubating"));
                    cellInfo.put("cellLevel", cell.get("cell_level"));
                } else if (CellType.NODE.getCode().equals(cell.get("type"))) {
                    cellInfo.put("element", cell.get("element"));
                    cellInfo.put("level", cell.get("level"));
                    cellInfo.put("cellLevel", cell.get("cell_level"));
                }

                occupiedCells.add(cellInfo);
            }
        }

        // 计算所有空位
        Set<String> occupiedPositions = occupiedCells.stream()
                .map(cell -> (String) cell.get("position"))
                .collect(Collectors.toSet());

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                String pos = x + "," + y;
                if (!occupiedPositions.contains(pos)) {
                    emptyPositions.add(pos);
                }
            }
        }

        return Map.of(
                "gridSize", gridSize,
                "totalCells", gridSize * gridSize,
                "occupiedCount", occupiedCells.size(),
                "emptyCount", emptyPositions.size(),
                "emptyPositions", emptyPositions,
                "occupiedCells", occupiedCells
        );
    }

    /**
     * 检查坐标是否为空
     */
    private boolean isCellEmpty(Fudi fudi, String position) {
        return getCellByPosition(fudi, position) == null;
    }

    /**
     * 根据坐标获取地块
     */
    private Map<String, Object> getCellByPosition(Fudi fudi, String position) {
        if (fudi.getGridLayout() == null || !fudi.getGridLayout().containsKey("cells")) {
            return null;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");

        return cells.stream()
                .filter(cell -> position.equals(cell.get("pos")))
                .findFirst()
                .orElse(null);
    }

    /**
     * 添加地块到网格
     */
    private void addCellToGrid(Fudi fudi, Map<String, Object> cell) {
        if (fudi.getGridLayout() == null) {
            fudi.setGridLayout(new HashMap<>());
        }

        if (!fudi.getGridLayout().containsKey("cells")) {
            fudi.getGridLayout().put("cells", new ArrayList<>());
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");
        cells.add(cell);
    }

    /**
     * 从网格移除地块
     */
    private void removeCellFromGrid(Fudi fudi, String position) {
        if (fudi.getGridLayout() == null || !fudi.getGridLayout().containsKey("cells")) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");
        cells.removeIf(cell -> position.equals(cell.get("pos")));
    }

    /**
     * 计算生长速度修正（基于相邻地块五行）
     */
    private double calculateGrowthModifier(Fudi fudi, String position, WuxingType element) {
        String[] coords = position.split(",");
        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);

        // 获取上下左右四个相邻坐标
        String[] adjacentPositions = {
                (x - 1) + "," + y, // 左
                (x + 1) + "," + y, // 右
                x + "," + (y - 1), // 上
                x + "," + (y + 1)  // 下
        };

        double modifier = 1.0;
        for (String adjPos : adjacentPositions) {
            Map<String, Object> adjCell = getCellByPosition(fudi, adjPos);
            if (adjCell != null && adjCell.containsKey("element")) {
                String adjElementStr = (String) adjCell.get("element");
                WuxingType adjElement = WuxingType.valueOf(adjElementStr);
                modifier *= element.calculateGrowthModifier(adjElement);
            }
        }

        return modifier;
    }

    /**
     * 根据名称查找种子模板
     */
    private ItemTemplate findSeedTemplateByName(String name) {
        return itemTemplateRepository.findByType(ItemType.SEED).stream()
                .filter(t -> t.getName().equals(name) || t.getName().contains(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未找到种子: %s".formatted(name)));
    }

    /**
     * 根据物品模板标签解析五行元素
     */
    private WuxingType resolveElement(ItemTemplate template) {
        if (template.getTags() != null) {
            for (String tag : template.getTags()) {
                switch (tag.toLowerCase()) {
                    case "fire" -> {
                        return WuxingType.FIRE;
                    }
                    case "water", "ice" -> {
                        return WuxingType.WATER;
                    }
                    case "wood" -> {
                        return WuxingType.WOOD;
                    }
                    case "gold", "metal" -> {
                        return WuxingType.METAL;
                    }
                    case "earth" -> {
                        return WuxingType.EARTH;
                    }
                }
            }
        }
        return WuxingType.WOOD;
    }
}
