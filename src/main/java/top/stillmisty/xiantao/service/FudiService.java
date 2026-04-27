package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.land.entity.Fudi;
import top.stillmisty.xiantao.domain.land.enums.CellType;
import top.stillmisty.xiantao.domain.land.enums.EmotionState;
import top.stillmisty.xiantao.domain.land.enums.MBTIPersonality;
import top.stillmisty.xiantao.domain.land.enums.WuxingType;
import top.stillmisty.xiantao.domain.land.repository.FudiRepository;
import top.stillmisty.xiantao.domain.land.vo.CellDetailVO;
import top.stillmisty.xiantao.domain.land.vo.FarmCellVO;
import top.stillmisty.xiantao.domain.land.vo.FudiStatusVO;
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

    public ServiceResult<Map<String, Object>> feedBeastByName(PlatformType platform, String openId, String position, String feedItemName) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(feedBeastByName(auth.userId(), position, feedItemName));
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
                    .type(type);

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

        // 懒加载计算生长进度，确保获取最新值
        updateGrowthProgress(cell);
        Double growthProgress = (Double) cell.getOrDefault("growth_progress", 0.0);

        builder.name(cropName)
                .element(element)
                .growthProgress(growthProgress)
                .isMature(growthProgress != null && growthProgress >= 1.0);

        if (cell.containsKey("plant_time")) {
            builder.createTime(LocalDateTime.parse((String) cell.get("plant_time")));
        }
    }

    private void buildPenCellDetail(CellDetailVO.CellDetailVOBuilder builder, Map<String, Object> cell) {
        String beastName = (String) cell.getOrDefault("beast_name", "未知灵兽");
        Integer tier = (Integer) cell.get("beast_tier");
        Integer hunger = (Integer) cell.getOrDefault("hunger", 100);

        builder.name(beastName)
                .level(tier)
                .hunger(hunger)
                .isMature(hunger != null && hunger >= 30);
    }

    private void buildNodeCellDetail(CellDetailVO.CellDetailVOBuilder builder, Map<String, Object> cell) {
        String elementStr = (String) cell.get("element");
        WuxingType element = elementStr != null ? WuxingType.valueOf(elementStr) : null;
        Integer level = (Integer) cell.getOrDefault("level", 1);
        Integer durability = (Integer) cell.get("durability");

        builder.name("阵眼")
                .level(level)
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
    public FarmCellVO plantCrop(Long userId, String position, Integer cropId, String cropName, WuxingType element) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        // 检查坐标是否为空
        if (!isCellEmpty(fudi, position)) {
            throw new IllegalStateException("坐标 " + position + " 已有地块");
        }

        // 计算生长速度修正（基于相邻地块五行）
        double growthModifier = calculateGrowthModifier(fudi, position, element);

        // 基础生长时间（小时）
        double baseGrowthHours = getBaseGrowthHours(cropId);
        double actualGrowthHours = baseGrowthHours / growthModifier;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime matureTime = now.plusHours((long) actualGrowthHours);

        // 创建灵田地块
        Map<String, Object> farmCell = new HashMap<>();
        farmCell.put("pos", position);
        farmCell.put("type", CellType.FARM.getCode());
        farmCell.put("element", element.name());
        farmCell.put("crop_id", cropId);
        farmCell.put("crop_name", cropName);
        farmCell.put("growth_progress", 0.0);
        farmCell.put("plant_time", now.toString());
        farmCell.put("mature_time", matureTime.toString());
        farmCell.put("base_growth_hours", baseGrowthHours);
        farmCell.put("growth_modifier", growthModifier);

        addCellToGrid(fudi, farmCell);
        fudiRepository.save(fudi);

        log.info("用户 {} 在坐标 {} 种植 {}", userId, position, cropName);

        return FarmCellVO.builder()
                .position(position)
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
                .build();
    }

    /**
     * 根据作物名称种植灵药（从背包消耗种子）
     */
    public FarmCellVO plantCropByName(Long userId, String position, String cropName) {
        ItemTemplate seedTemplate = findSeedTemplateByName(cropName);

        // 检查背包中有该种子
        var stackableItem = stackableItemRepository.findByUserIdAndTemplateId(userId, seedTemplate.getId())
                .orElseThrow(() -> new IllegalStateException("背包中没有 [%s]".formatted(cropName)));

        // 消耗 1 颗种子
        itemService.reduceStackableItem(userId, seedTemplate.getId(), 1);

        // 根据种子标签决定五行元素
        WuxingType element = resolveElement(seedTemplate);

        Integer cropId = seedTemplate.getId().intValue();
        return plantCrop(userId, position, cropId, cropName, element);
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

        // 懒加载计算生长进度
        updateGrowthProgress(cell);

        Double progress = (Double) cell.get("growth_progress");
        if (progress == null || progress < 1.0) {
            throw new IllegalStateException("灵药尚未成熟");
        }

        // 收获
        String cropName = (String) cell.get("crop_name");
        Integer cropId = (Integer) cell.get("crop_id");
        int yield = calculateYield(cropId, fudi.getCoreLevel());

        // 移除地块（变为空地）
        String pos = (String) cell.get("pos");
        removeCellFromGrid(fudi, pos);

        // 消耗精力
        fudi.setSpiritEnergy(Math.max(0, fudi.getSpiritEnergy() - 5));

        fudiRepository.save(fudi);

        // TODO: 添加到背包
        // itemService.addStackableItem(userId, cropId, yield);

        log.info("用户 {} 收获 {}，获得 {} x{}", userId, pos, cropName, yield);

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

            // 懒加载计算生长进度
            updateGrowthProgress(cell);

            Double progress = (Double) cell.get("growth_progress");
            if (progress != null && progress >= 1.0) {
                String cropName = (String) cell.get("crop_name");
                Integer cropId = (Integer) cell.get("crop_id");
                int yield = calculateYield(cropId, fudi.getCoreLevel());

                toRemove.add((String) cell.get("pos"));
                totalYield += yield;
                harvestedCount++;

                log.info("收获 {}，获得 {} x{}", cell.get("pos"), cropName, yield);
            }
        }

        // 移除已收获的地块
        for (String pos : toRemove) {
            removeCellFromGrid(fudi, pos);
        }

        // 消耗精力
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

        switch (type) {
            case FARM -> {
                cell.put("element", WuxingType.WOOD.name()); // 灵田默认木属性
            }
            case PEN -> {
                cell.put("beast_id", 0);
                cell.put("beast_tier", 1);
                cell.put("hunger", 100);
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

    // ===================== 兽栏系统 =====================

    /**
     * 喂养灵兽
     */
    public Map<String, Object> feedBeast(Long userId, String position, Integer feedItemId, String feedItemName) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        Map<String, Object> cell = getCellByPosition(fudi, position);
        if (cell == null || !CellType.PEN.getCode().equals(cell.get("type"))) {
            throw new IllegalStateException("坐标 " + position + " 不是兽栏");
        }

        Integer hunger = (Integer) cell.getOrDefault("hunger", 100);
        int feedAmount = 30; // 每次喂食恢复30饥饿值
        int newHunger = Math.min(100, hunger + feedAmount);

        cell.put("hunger", newHunger);
        cell.put("last_feed_time", LocalDateTime.now().toString());

        // 消耗精力
        fudi.setSpiritEnergy(Math.max(0, fudi.getSpiritEnergy() - 3));

        fudiRepository.save(fudi);

        // TODO: 消耗饲料物品
        // itemService.removeStackableItem(userId, feedItemId, 1);

        log.info("用户 {} 喂养坐标 {} 的灵兽，饥饿值 {} -> {}", userId, position, hunger, newHunger);

        return Map.of(
                "position", position,
                "beastName", cell.getOrDefault("beast_name", "未知灵兽"),
                "oldHunger", hunger,
                "newHunger", newHunger
        );
    }

    /**
     * 根据饲料名称喂养灵兽（从背包消耗饲料）
     */
    public Map<String, Object> feedBeastByName(Long userId, String position, String feedItemName) {
        List<StackableItem> items = stackableItemRepository.findByUserId(userId).stream()
                .filter(item -> item.getName().contains(feedItemName))
                .toList();

        if (items.isEmpty()) {
            throw new IllegalStateException("背包中没有 [%s]".formatted(feedItemName));
        }

        StackableItem feedItem = items.getFirst();

        // 消耗 1 份饲料
        itemService.reduceStackableItem(userId, feedItem.getTemplateId(), 1);

        Integer feedItemId = feedItem.getTemplateId().intValue();
        return feedBeast(userId, position, feedItemId, feedItemName);
    }

    // ===================== 辅助方法 =====================

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
                } else if (CellType.PEN.getCode().equals(cell.get("type"))) {
                    cellInfo.put("beastName", cell.get("beast_name"));
                    cellInfo.put("hunger", cell.get("hunger"));
                } else if (CellType.NODE.getCode().equals(cell.get("type"))) {
                    cellInfo.put("element", cell.get("element"));
                    cellInfo.put("level", cell.get("level"));
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
