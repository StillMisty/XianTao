package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.land.entity.Fudi;
import top.stillmisty.xiantao.domain.land.entity.Spirit;
import top.stillmisty.xiantao.domain.land.entity.SpiritForm;
import top.stillmisty.xiantao.domain.land.enums.*;
import top.stillmisty.xiantao.domain.land.repository.FudiRepository;
import top.stillmisty.xiantao.domain.land.vo.*;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.domain.land.repository.SpiritRepository;
import top.stillmisty.xiantao.infrastructure.mapper.SpiritFormMapper;
import top.stillmisty.xiantao.service.annotation.ConsumeSpiritEnergy;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 福地服务层
 * 提供福地管理的核心业务逻辑，包括灵气消耗、献祭、种植、收获等功能。
 * 灵气耗尽时除献祭外所有功能不可用。
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
    private final UserRepository userRepository;
    private final SpiritRepository spiritRepository;
    private final SpiritFormMapper spiritFormMapper;

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
     * 创建新福地（随机分配地灵形态）
     */
    public Fudi createFudi(Long userId, MBTIPersonality mbtiType) {
        if (fudiRepository.existsByUserId(userId)) {
            throw new IllegalStateException("用户已拥有福地");
        }

        Fudi fudi = Fudi.create();
        fudi.setUserId(userId);
        fudi.setAuraCurrent(500);
        fudi.setAuraMax(1000);
        fudi.setTribulationStage(0);
        fudi.setAutoMode(true);
        fudi.setTribulationWinStreak(0);

        Map<String, Object> gridLayout = new HashMap<>();
        gridLayout.put("cells", new ArrayList<>());
        fudi.setGridLayout(gridLayout);

        fudi.setLastAuraUpdate(LocalDateTime.now());
        fudi.setLastOnlineTime(LocalDateTime.now());

        fudi = fudiRepository.save(fudi);

        // 创建地灵实例（随机分配形态）
        Spirit spirit = createSpiritForFudi(fudi, mbtiType);
        spiritRepository.save(spirit);

        autoExpandCells(fudi);
        return fudi;
    }

    /**
     * 创建地灵实例（随机分配形态）
     */
    private Spirit createSpiritForFudi(Fudi fudi, MBTIPersonality mbtiType) {
        List<SpiritForm> allForms = spiritFormMapper.selectAll();

        Spirit spirit = Spirit.create();
        spirit.setFudiId(fudi.getId());
        spirit.setEnergy(100);
        spirit.setAffection(0);
        spirit.setAffectionMax(1000);
        spirit.setEmotionState(EmotionState.CALM);
        spirit.setMbtiType(mbtiType);
        spirit.setLastEnergyUpdate(LocalDateTime.now());

        if (!allForms.isEmpty()) {
            SpiritForm randomForm = allForms.get(new Random().nextInt(allForms.size()));
            spirit.setFormId(randomForm.getId().intValue());
        }

        return spirit;
    }

    /**
     * 获取用户福地（懒加载计算 + 自动扩建 + 精力恢复）
     */
    public Optional<Fudi> getFudiByUserId(Long userId) {
        Optional<Fudi> fudiOpt = fudiRepository.findByUserId(userId);
        fudiOpt.ifPresent(fudi -> {
            fudi.updateAura();
            spiritRepository.findByFudiId(fudi.getId()).ifPresent(spirit -> {
                spirit.restoreEnergy(fudi.getTribulationStage());
                spirit.updateEmotionState(fudi.getAuraCurrent(), fudi.getAuraMax());
                spiritRepository.save(spirit);
            });
            autoExpandCells(fudi);
            fudiRepository.save(fudi);
        });
        return fudiOpt;
    }

    /**
     * 自动扩建地块：根据劫数自动解锁新地块
     */
    private void autoExpandCells(Fudi fudi) {
        int maxCells = 3 + fudi.getTribulationStage() / 3;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().getOrDefault("cells", new ArrayList<>());
        int currentCount = cells.size();

        if (currentCount >= maxCells) return;

        int nextCellId = currentCount + 1;
        for (int i = nextCellId; i <= maxCells; i++) {
            Map<String, Object> newCell = new HashMap<>();
            newCell.put("cell_id", i);
            newCell.put("type", "empty");
            cells.add(newCell);
        }
    }

    /**
     * 获取地块总数（含空地）
     */
    private int getTotalCellCount(Fudi fudi) {
        if (fudi.getGridLayout() == null || !fudi.getGridLayout().containsKey("cells")) return 0;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");
        return cells.size();
    }

    /**
     * 检查灵气是否耗尽（除献祭外所有功能不可用）
     */
    private void checkAuraDepleted(Fudi fudi) {
        fudi.updateAura();
        if (fudi.getAuraCurrent() <= 0) {
            throw new IllegalStateException("灵气已耗尽，请先通过献祭装备补充灵气。");
        }
    }

    /**
     * 获取福地状态VO（含懒求值天劫结算）
     */
    public FudiStatusVO getFudiStatus(Long userId) {
        Fudi fudi = fudiRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        fudi.updateAura();

        var user = userRepository.findById(userId).orElseThrow();
        String tribulationResult = resolveTribulation(fudi, user.getLevel(), user.getStatStr());

        // 精力恢复与情绪更新（天劫结算后可能已修改情绪）
        spiritRepository.findByFudiId(fudi.getId()).ifPresent(spirit -> {
            spirit.restoreEnergy(fudi.getTribulationStage());
            if (tribulationResult == null) {
                spirit.updateEmotionState(fudi.getAuraCurrent(), fudi.getAuraMax());
            }
            spiritRepository.save(spirit);
        });

        fudiRepository.save(fudi);
        return buildFudiStatusVO(fudi, tribulationResult);
    }

    private FudiStatusVO buildFudiStatusVO(Fudi fudi, String tribulationResult) {
        List<CellDetailVO> cellDetails = buildCellDetails(fudi);

        int totalBeasts = (int) cellDetails.stream().filter(c -> c.getType() == CellType.PEN && c.getName() != null).count();

        Spirit spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);

        // 从 SpiritForm 表读取形态名和喜好
        String formName = null;
        List<String> likedTags = null;
        List<String> dislikedTags = null;
        if (spirit != null && spirit.getFormId() != null) {
            SpiritForm form = spiritFormMapper.selectOneById(spirit.getFormId().longValue());
            if (form != null) {
                formName = form.getName();
                likedTags = new ArrayList<>(form.getLikedTags());
                dislikedTags = new ArrayList<>(form.getDislikedTags());
            }
        }

        return FudiStatusVO.builder()
                .fudiId(fudi.getId())
                .userId(fudi.getUserId())
                .auraCurrent(fudi.getAuraCurrent())
                .auraMax(fudi.getAuraMax())
                .auraHourlyCost(fudi.calculateHourlyAuraCost())
                .tribulationStage(fudi.getTribulationStage())
                .totalCells(getTotalCellCount(fudi))
                .isAuraDepleted(fudi.getAuraCurrent() <= 0)
                .mbtiType(spirit != null ? spirit.getMbtiType() : null)
                .spiritEnergy(spirit != null ? spirit.getEnergy() : null)
                .spiritAffection(spirit != null ? spirit.getAffection() : null)
                .affectionMax(spirit != null ? spirit.getAffectionMax() : null)
                .energyMax(spirit != null ? spirit.getEnergyMax(fudi.getTribulationStage()) : null)
                .spiritForm(formName)
                .spiritFormName(formName)
                .likedTags(likedTags)
                .dislikedTags(dislikedTags)
                .emotionState(spirit != null ? spirit.getEmotionState() : null)
                .autoMode(fudi.getAutoMode())
                .occupiedCells(fudi.getOccupiedCellCount())
                .tribulationWinStreak(fudi.getTribulationWinStreak())
                .lastTribulationTime(fudi.getLastTribulationTime())
                .nextTribulationTime(fudi.calculateNextTribulationTime())
                .cellDetails(cellDetails)
                .totalBeasts(totalBeasts)
                .tribulationResult(tribulationResult)
                .build();
    }

    // ===================== 天劫系统 =====================

    private String resolveTribulation(Fudi fudi, int playerLevel, int playerStr) {
        LocalDateTime referenceTime = fudi.getLastTribulationTime() != null
                ? fudi.getLastTribulationTime()
                : fudi.getCreateTime();

        if (java.time.Duration.between(referenceTime, LocalDateTime.now()).toDays() < 7) {
            return null;
        }

        int attack = playerLevel * 80 + fudi.getTribulationStage() * 200;
        int defense = fudi.calculateTribulationDefense(playerStr);

        fudi.setLastTribulationTime(LocalDateTime.now());

        // 渡劫怜悯检查
        boolean compassionTriggered = checkTribulationCompassion(fudi, attack, defense);

        if (defense > attack) {
            return compassionTriggered ? null : applyTribulationWin(fudi, attack, defense);
        } else if (compassionTriggered) {
            return applyTribulationCompassion(fudi, attack, defense);
        } else {
            return applyTribulationLoss(fudi, attack, defense);
        }
    }

    /**
     * 检查渡劫怜悯触发条件
     * affection ≥ 800 AND defense ≥ attack × 80% AND defense < attack
     */
    private boolean checkTribulationCompassion(Fudi fudi, int attack, int defense) {
        Spirit spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
        int affection = spirit != null && spirit.getAffection() != null ? spirit.getAffection() : 0;
        return affection >= 800 && defense >= attack * 0.8 && defense < attack;
    }

    /**
     * 渡劫怜悯：视为胜利但精力归零、不加好感、触发FATIGUED
     * 返回 null 表示结果由 LLM 叙事美化（此处返回结构化文本，由调用方判断）
     */
    private String applyTribulationCompassion(Fudi fudi, int attack, int defense) {
        int oldStage = fudi.getTribulationStage();
        int newWinStreak = fudi.getTribulationWinStreak() + 1;
        int oldAuraMax = fudi.getAuraMax();
        int newStage = oldStage + 1;

        fudi.setTribulationWinStreak(newWinStreak);
        fudi.setTribulationStage(newStage);
        fudi.setAuraMax(oldAuraMax + newWinStreak * 100);

        spiritRepository.findByFudiId(fudi.getId()).ifPresent(spirit -> {
            spirit.setEnergy(0);
            spirit.setEmotionState(EmotionState.FATIGUED);
            spirit.setLastEnergyUpdate(LocalDateTime.now());
            spiritRepository.save(spirit);
        });

        return "🛡️⚡ 天劫降临！地灵燃烧灵体为你挡下了天雷……\n" +
                "   攻击力：" + attack + " ｜ 防御力：" + defense + " ｜ 怜悯庇护！\n" +
                "   劫数：" + oldStage + " → " + newStage + " ｜ 连胜×" + newWinStreak + "\n" +
                "   灵气上限：" + oldAuraMax + " → " + fudi.getAuraMax() + "\n" +
                "   精力归零，地灵陷入疲惫…";
    }

    @SuppressWarnings("unchecked")
    private String applyTribulationWin(Fudi fudi, int attack, int defense) {
        int oldStage = fudi.getTribulationStage();
        int newWinStreak = fudi.getTribulationWinStreak() + 1;
        int oldAuraMax = fudi.getAuraMax();
        int newStage = oldStage + 1;

        fudi.setTribulationWinStreak(newWinStreak);
        fudi.setTribulationStage(newStage);
        fudi.setAuraMax(oldAuraMax + newWinStreak * 100);

        Spirit spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
        int oldAffection = spirit != null ? spirit.getAffection() : 0;
        if (spirit != null) {
            spirit.addAffection(5);
            spirit.setEmotionState(EmotionState.EXCITED);
            spiritRepository.save(spirit);
        }

        return String.format(
                "⚡ 天劫降临！福地成功抵御！\n" +
                        "   攻击力：%d ｜ 防御力：%d ｜ 胜利！\n" +
                        "   劫数：%d → %d ｜ 连胜×%d\n" +
                        "   灵气上限：%d → %d ｜ 好感度：%d → %d",
                attack, defense,
                oldStage, newStage, newWinStreak,
                oldAuraMax, fudi.getAuraMax(), oldAffection, spirit != null ? spirit.getAffection() : 0
        );
    }

    @SuppressWarnings("unchecked")
    private String applyTribulationLoss(Fudi fudi, int attack, int defense) {
        int oldWinStreak = fudi.getTribulationWinStreak();

        Spirit spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
        int oldAffection = spirit != null ? spirit.getAffection() : 0;

        int diff = attack - defense;
        int occupiedCount = fudi.getOccupiedCellCount();
        double ratio = Math.min(1.0, (double) diff / attack);
        int clearCount = Math.min(occupiedCount, Math.max(1, (int) Math.ceil(ratio * occupiedCount)));

        List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");
        List<Integer> occupiedIndices = new ArrayList<>();
        for (int i = 0; i < cells.size(); i++) {
            if (!"empty".equals(cells.get(i).get("type"))) {
                occupiedIndices.add(i);
            }
        }
        Collections.shuffle(occupiedIndices);
        for (int i = 0; i < clearCount && i < occupiedIndices.size(); i++) {
            int idx = occupiedIndices.get(i);
            Map<String, Object> emptyCell = new HashMap<>();
            emptyCell.put("cell_id", cells.get(idx).get("cell_id"));
            emptyCell.put("type", "empty");
            cells.set(idx, emptyCell);
        }

        fudi.setTribulationWinStreak(0);

        if (spirit != null) {
            spirit.addAffection(-clearCount);
            spirit.setEmotionState(EmotionState.ANGRY);
            spiritRepository.save(spirit);
        }

        return String.format(
                "⚡ 天劫降临！福地未能抵御…\n" +
                        "   攻击力：%d ｜ 防御力：%d ｜ 差额：%d\n" +
                        "   连胜×%d → 中断，被毁地块：%d 个\n" +
                        "   好感度：%d → %d",
                attack, defense, diff,
                oldWinStreak, clearCount,
                oldAffection, spirit != null ? spirit.getAffection() : 0
        );
    }

    // ===================== 地块详情构建 =====================

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
            Integer cellId = (Integer) cell.get("cell_id");

            CellDetailVO.CellDetailVOBuilder builder = CellDetailVO.builder()
                    .cellId(cellId)
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

        updateGrowthProgress(cell);
        Double growthProgress = (Double) cell.getOrDefault("growth_progress", 0.0);
        int harvestCount = (Integer) cell.getOrDefault("harvest_count", 0);
        int maxHarvest = (Integer) cell.getOrDefault("max_harvest", 1);
        boolean isWilted = !Boolean.TRUE.equals(cell.get("is_perennial")) && growthProgress != null && growthProgress > 1.0 && isWilted(cell);

        builder.name(cropName)
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
        Integer level = (Integer) cell.getOrDefault("level", 1);
        Integer durability = (Integer) cell.get("durability");

        builder.name("阵眼")
                .level(level)
                .cellLevel((Integer) cell.getOrDefault("cell_level", 1))
                .durability(durability);
    }

    // ===================== 献祭系统（灵气耗尽时唯一可用功能） =====================

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

        equipmentRepository.deleteById(equipmentId);

        int newAura = Math.min(fudi.getAuraMax(), fudi.getAuraCurrent() + auraGain);
        fudi.setAuraCurrent(newAura);
        fudi.setLastAuraUpdate(LocalDateTime.now());

        fudiRepository.save(fudi);

        log.info("用户 {} 献祭装备 ID={} ({}), 获得灵气 {}", userId, equipmentId, equipment.getName(), auraGain);
        return auraGain;
    }

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

    public ServiceResult<Map<String, Integer>> sacrificeAllItems(PlatformType platform, String openId) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(sacrificeAllItems(auth.userId()));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Integer>> sacrificeItemsByQuality(PlatformType platform, String openId, String quality) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(sacrificeItemsByQuality(auth.userId(), quality));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public Map<String, Integer> sacrificeAllItems(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        List<Equipment> unequipped = equipmentRepository.findByUserId(userId).stream()
                .filter(e -> !e.getEquipped())
                .toList();

        if (unequipped.isEmpty()) {
            throw new IllegalStateException("背包中没有可献祭的装备");
        }

        int totalAura = 0;
        int count = 0;
        for (Equipment equipment : unequipped) {
            totalAura += calculateItemAura(equipment, fudi);
            equipmentRepository.deleteById(equipment.getId());
            count++;
        }

        fudi.setAuraCurrent(Math.min(fudi.getAuraMax(), fudi.getAuraCurrent() + totalAura));
        fudi.setLastAuraUpdate(LocalDateTime.now());
        fudiRepository.save(fudi);

        log.info("用户 {} 批量献祭 {} 件装备, 获得总灵气 {}", userId, count, totalAura);
        return Map.of("count", count, "totalAura", totalAura);
    }

    public Map<String, Integer> sacrificeItemsByQuality(Long userId, String quality) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        Rarity targetRarity = parseRarity(quality);

        List<Equipment> filtered = equipmentRepository.findByUserId(userId).stream()
                .filter(e -> !e.getEquipped() && e.getRarity() == targetRarity)
                .toList();

        if (filtered.isEmpty()) {
            throw new IllegalStateException("背包中没有" + targetRarity.getName() + "品质的可献祭装备");
        }

        int totalAura = 0;
        int count = 0;
        for (Equipment equipment : filtered) {
            totalAura += calculateItemAura(equipment, fudi);
            equipmentRepository.deleteById(equipment.getId());
            count++;
        }

        fudi.setAuraCurrent(Math.min(fudi.getAuraMax(), fudi.getAuraCurrent() + totalAura));
        fudi.setLastAuraUpdate(LocalDateTime.now());
        fudiRepository.save(fudi);

        log.info("用户 {} 批量献祭 {} 件{}品质装备, 获得总灵气 {}", userId, count, targetRarity.getName(), totalAura);
        return Map.of("count", count, "totalAura", totalAura);
    }

    private int calculateItemAura(Equipment equipment, Fudi fudi) {
        int itemLevel = equipment.getForgeLevel() != null ? equipment.getForgeLevel() : 1;
        double qualityMultiplier = equipment.getQualityMultiplier() != null ? equipment.getQualityMultiplier() : 1.0;
        int baseValue = equipment.getFinalAttack() + equipment.getFinalDefense();
        if (baseValue <= 0) baseValue = 10;
        return fudi.calculateSacrificeAura(baseValue, qualityMultiplier, itemLevel);
    }

    private Rarity parseRarity(String input) {
        return switch (input) {
            case "白色", "破旧" -> Rarity.BROKEN;
            case "绿色", "普通" -> Rarity.COMMON;
            case "蓝色", "稀有" -> Rarity.RARE;
            case "紫色", "史诗" -> Rarity.EPIC;
            case "金色", "传说" -> Rarity.LEGENDARY;
            default -> throw new IllegalStateException("未知品质: " + input + "，可选：白色/绿色/蓝色/紫色/金色");
        };
    }

    // ===================== 种植/收获系统 =====================

    /**
     * 统一扣除精力（懒恢复 + 好感减免 + 情绪更新 + 保存）
     */
    private void consumeSpiritEnergy(Fudi fudi, int baseCost) {
        spiritRepository.findByFudiId(fudi.getId()).ifPresent(spirit -> {
            spirit.restoreEnergy(fudi.getTribulationStage());
            int actualCost = spirit.calculateEnergyConsumption(baseCost);
            spirit.deductEnergy(actualCost);
            spirit.setLastEnergyUpdate(LocalDateTime.now());
            spiritRepository.save(spirit);
        });
    }

    /**
     * 在指定地块种植灵药
     */
    public FarmCellVO plantCrop(Long userId, Integer cellId, Integer cropId, String cropName, int cropTier) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 3);
        checkAuraDepleted(fudi);

        Map<String, Object> existingCell = getCellById(fudi, cellId);
        if (existingCell == null || !"empty".equals(existingCell.get("type"))) {
            if (existingCell != null && !CellType.FARM.getCode().equals(existingCell.get("type"))) {
                throw new IllegalStateException("地块 " + cellId + " 已有其他类型建筑");
            }
        }

        int cellLevel = existingCell != null ?
                (Integer) existingCell.getOrDefault("cell_level", 1) : 1;
        int minLevel = Math.max(1, cropTier);
        if (cellLevel < minLevel) {
            throw new IllegalStateException(
                    "作物等阶(T%d)需要至少Lv%d灵田，当前灵田Lv%d".formatted(cropTier, minLevel, cellLevel));
        }

        int auraCost = cropTier * 5;
        if (fudi.getAuraCurrent() < auraCost) {
            throw new IllegalStateException("灵气不足（需要 %d，当前 %d）".formatted(auraCost, fudi.getAuraCurrent()));
        }
        fudi.setAuraCurrent(fudi.getAuraCurrent() - auraCost);

        double levelSpeedMultiplier = getLevelSpeedMultiplier(cellLevel, minLevel);
        double baseGrowthHours = getBaseGrowthHours(cropId);
        double actualGrowthHours = baseGrowthHours / levelSpeedMultiplier;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime matureTime = now.plusHours((long) actualGrowthHours);

        int maxHarvest = 1 + cropTier / 3;
        boolean isPerennial = maxHarvest > 1;

        Map<String, Object> farmCell = existingCell != null ? existingCell : new HashMap<>();
        farmCell.put("cell_id", cellId);
        farmCell.put("type", CellType.FARM.getCode());
        farmCell.put("cell_level", cellLevel);
        farmCell.put("crop_id", cropId);
        farmCell.put("crop_name", cropName);
        farmCell.put("crop_tier", cropTier);
        farmCell.put("growth_progress", 0.0);
        farmCell.put("plant_time", now.toString());
        farmCell.put("mature_time", matureTime.toString());
        farmCell.put("base_growth_hours", baseGrowthHours);
        farmCell.put("level_speed_multiplier", levelSpeedMultiplier);
        farmCell.put("harvest_count", 0);
        farmCell.put("max_harvest", maxHarvest);
        farmCell.put("is_perennial", isPerennial);

        if (existingCell == null || "empty".equals(existingCell.get("type"))) {
            addCellToGrid(fudi, farmCell);
        }
        fudiRepository.save(fudi);

        log.info("用户 {} 在地块 {} 种植 {} (T{}, {})", userId, cellId, cropName, cropTier, isPerennial ? "多季" : "单季");

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

    public FarmCellVO plantCropByName(Long userId, String position, String cropName) {
        Integer cellId = parseCellId(position);
        ItemTemplate seedTemplate = findSeedTemplateByName(cropName);

        var stackableItem = stackableItemRepository.findByUserIdAndTemplateId(userId, seedTemplate.getId())
                .orElseThrow(() -> new IllegalStateException("背包中没有 [%s]".formatted(cropName)));

        itemService.reduceStackableItem(userId, seedTemplate.getId(), 1);

        int growTime = seedTemplate.getGrowTime() != null ? seedTemplate.getGrowTime() : 24;
        int cropTier = getCropTier(growTime);
        Integer cropId = seedTemplate.getId().intValue();

        Fudi fudi = getFudiByUserId(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));
        Map<String, Object> existingCell = getCellById(fudi, cellId);
        if (existingCell != null && !"empty".equals(existingCell.get("type")) && !CellType.FARM.getCode().equals(existingCell.get("type"))) {
            throw new IllegalStateException("地块 " + cellId + " 已有其他类型建筑");
        }

        return plantCrop(userId, cellId, cropId, cropName, cropTier);
    }

    public FarmCellVO plantCropByInput(Long userId, String position, String input) {
        Integer cellId = parseCellId(position);
        var result = itemResolver.resolveSeed(userId, input);
        return switch (result) {
            case ItemResolver.Found(var template, var index) -> {
                var stackableItem = stackableItemRepository
                        .findByUserIdAndTemplateId(userId, template.getId())
                        .orElseThrow(() -> new IllegalStateException("背包中没有 [" + template.getName() + "]"));

                itemService.reduceStackableItem(userId, template.getId(), 1);

                int growTime = template.getGrowTime() != null ? template.getGrowTime() : 24;
                int cropTier = getCropTier(growTime);
                Integer cropId = template.getId().intValue();
                String cropName = template.getName();

                Fudi fudi = getFudiByUserId(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));
                Map<String, Object> existingCell = getCellById(fudi, cellId);
                if (existingCell != null && !"empty".equals(existingCell.get("type")) && !CellType.FARM.getCode().equals(existingCell.get("type"))) {
                    throw new IllegalStateException("地块 " + cellId + " 已有其他类型建筑");
                }

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

    @ConsumeSpiritEnergy(5)
    public Map<String, Object> harvestCrop(Long userId, String position) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 5);
        checkAuraDepleted(fudi);

        Map<String, Object> cell = getCellById(fudi, cellId);
        if (cell == null || "empty".equals(cell.get("type"))) {
            throw new IllegalStateException("地块 " + cellId + " 为空");
        }

        if (!CellType.FARM.getCode().equals(cell.get("type"))) {
            throw new IllegalStateException("地块 " + cellId + " 不是灵田");
        }

        updateGrowthProgress(cell);

        Double progress = (Double) cell.get("growth_progress");
        boolean isPerennial = Boolean.TRUE.equals(cell.get("is_perennial"));

        if (!isPerennial && progress != null && progress > 1.0 && isWilted(cell)) {
            String cropName = (String) cell.get("crop_name");
            removeCellFromGrid(fudi, cellId);
            fudiRepository.save(fudi);
            throw new IllegalStateException("%s 已枯萎（超过成熟时间两倍未收获）".formatted(cropName));
        }

        if (progress == null || progress < 1.0) {
            throw new IllegalStateException("灵药尚未成熟");
        }

        String cropName = (String) cell.get("crop_name");
        Integer cropId = (Integer) cell.get("crop_id");
        int yield = calculateYield(cropId, fudi.getTribulationStage());

        int harvestCount = (Integer) cell.getOrDefault("harvest_count", 0) + 1;
        int maxHarvest = (Integer) cell.getOrDefault("max_harvest", 1);

        if (isPerennial && harvestCount < maxHarvest) {
            cell.put("harvest_count", harvestCount);
            cell.put("growth_progress", 0.0);
            cell.put("plant_time", LocalDateTime.now().toString());
            double baseGrowthHours = (Double) cell.get("base_growth_hours");
            double levelSpeed = (Double) cell.getOrDefault("level_speed_multiplier", 1.0);
            double actualHours = baseGrowthHours / levelSpeed;
            cell.put("mature_time", LocalDateTime.now().plusHours((long) actualHours).toString());
        } else {
            removeCellFromGrid(fudi, cellId);
        }

        fudiRepository.save(fudi);

        log.info("用户 {} 收获 {}，获得 {} x{}", userId, cellId, cropName, yield);

        return Map.of("cropName", cropName, "cropId", cropId, "yield", yield);
    }

    public Map<String, Object> harvestAllCrops(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 5);
        checkAuraDepleted(fudi);

        if (fudi.getGridLayout() == null || !fudi.getGridLayout().containsKey("cells")) {
            return Map.of("harvested", 0, "totalYield", 0);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");

        List<Integer> toRemove = new ArrayList<>();
        int totalYield = 0;
        int harvestedCount = 0;

        for (Map<String, Object> cell : cells) {
            if (!CellType.FARM.getCode().equals(cell.get("type"))) continue;

            updateGrowthProgress(cell);

            Double progress = (Double) cell.get("growth_progress");
            if (progress == null || progress < 1.0) continue;

            boolean isPerennial = Boolean.TRUE.equals(cell.get("is_perennial"));

            if (!isPerennial && progress > 1.0 && isWilted(cell)) {
                toRemove.add((Integer) cell.get("cell_id"));
                continue;
            }

            String cropName = (String) cell.get("crop_name");
            Integer cropId = (Integer) cell.get("crop_id");
            int yield = calculateYield(cropId, fudi.getTribulationStage());

            int harvestCount = (Integer) cell.getOrDefault("harvest_count", 0) + 1;
            int maxHarvest = (Integer) cell.getOrDefault("max_harvest", 1);

            if (isPerennial && harvestCount < maxHarvest) {
                cell.put("harvest_count", harvestCount);
                cell.put("growth_progress", 0.0);
                cell.put("plant_time", LocalDateTime.now().toString());
                double baseGrowthHours = (Double) cell.get("base_growth_hours");
                double levelSpeed = (Double) cell.getOrDefault("level_speed_multiplier", 1.0);
                double actualHours = baseGrowthHours / levelSpeed;
                cell.put("mature_time", LocalDateTime.now().plusHours((long) actualHours).toString());
            } else {
                toRemove.add((Integer) cell.get("cell_id"));
            }

            totalYield += yield;
            harvestedCount++;
            log.info("收获地块 {} 的 {}", cell.get("cell_id"), cropName);
        }

        for (Integer id : toRemove) {
            removeCellFromGrid(fudi, id);
        }

        // 批量收获额外精力扣除（第一个已由 consumeSpiritEnergy(5) 扣除）
        if (harvestedCount > 1) {
            final int extraHarvestCount = harvestedCount;
            spiritRepository.findByFudiId(fudi.getId()).ifPresent(spirit -> {
                spirit.restoreEnergy(fudi.getTribulationStage());
                int extraCost = spirit.calculateEnergyConsumption(5 * (extraHarvestCount - 1));
                spirit.deductEnergy(extraCost);
                spirit.setLastEnergyUpdate(LocalDateTime.now());
                spiritRepository.save(spirit);
            });
        }
        fudiRepository.save(fudi);

        return Map.of("harvested", harvestedCount, "totalYield", totalYield);
    }

    // ===================== 建造/拆除系统 =====================

    public Map<String, Object> buildCell(Long userId, String position, CellType type) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 3);
        checkAuraDepleted(fudi);

        if (!isCellEmpty(fudi, cellId)) {
            throw new IllegalStateException("地块 " + cellId + " 已被占用");
        }

        Map<String, Object> cell = new HashMap<>();
        cell.put("cell_id", cellId);
        cell.put("type", type.getCode());
        cell.put("cell_level", 1);

        switch (type) {
            case NODE -> {
                cell.put("level", 1);
                cell.put("durability", 100);
            }
            default -> {
            }
        }

        addCellToGrid(fudi, cell);
        fudiRepository.save(fudi);

        log.info("用户 {} 在地块 {} 建造 {}", userId, cellId, type.getChineseName());

        return Map.of("cellId", cellId, "type", type.getChineseName());
    }

    public Map<String, Object> removeCell(Long userId, String position) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 3);
        checkAuraDepleted(fudi);

        if (isCellEmpty(fudi, cellId)) {
            throw new IllegalStateException("地块 " + cellId + " 为空");
        }

        Map<String, Object> cell = getCellById(fudi, cellId);
        if (cell == null) {
            throw new IllegalStateException("地块 " + cellId + " 不存在");
        }

        CellType type = CellType.fromCode((String) cell.get("type"));
        removeCellFromGrid(fudi, cellId);
        fudiRepository.save(fudi);

        log.info("用户 {} 拆除地块 {} 的 {}", userId, cellId, type.getChineseName());

        return Map.of("cellId", cellId, "type", type.getChineseName());
    }

    // ===================== 地块升级系统 =====================

    public Map<String, Object> upgradeCell(Long userId, String position) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        checkAuraDepleted(fudi);

        Map<String, Object> cell = getCellById(fudi, cellId);
        if (cell == null || "empty".equals(cell.get("type"))) {
            throw new IllegalStateException("地块 " + cellId + " 为空");
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
        log.info("用户 {} 升级地块 {} 的 {} Lv{} -> Lv{}", userId, cellId, type.getChineseName(), currentLevel, newLevel);

        return Map.of("cellId", cellId, "type", type.getChineseName(), "oldLevel", currentLevel, "newLevel", newLevel);
    }

    // ===================== 灵兽系统 — 孵化 =====================

    public PenCellVO hatchBeast(Long userId, String position, String eggName) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 10);
        checkAuraDepleted(fudi);

        Map<String, Object> cell = getCellById(fudi, cellId);
        if (cell == null || !CellType.PEN.getCode().equals(cell.get("type"))) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }

        if (cell.containsKey("beast_id") && (Integer) cell.get("beast_id") > 0) {
            throw new IllegalStateException("该兽栏已有灵兽，请先放生");
        }

        ItemTemplate eggTemplate = itemTemplateRepository.findByType(ItemType.BEAST_EGG).stream()
                .filter(t -> t.getName().equals(eggName) || t.getName().contains(eggName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未找到兽卵: %s".formatted(eggName)));

        var stackableItem = stackableItemRepository.findByUserIdAndTemplateId(userId, eggTemplate.getId())
                .orElseThrow(() -> new IllegalStateException("背包中没有 [%s]".formatted(eggName)));
        itemService.reduceStackableItem(userId, eggTemplate.getId(), 1);

        int tier = getCropTier(eggTemplate.getGrowTime() != null ? eggTemplate.getGrowTime() : 72);

        int cellLevel = (Integer) cell.getOrDefault("cell_level", 1);
        if (cellLevel < tier) {
            throw new IllegalStateException("灵兽等阶(T%d)需要至少Lv%d兽栏".formatted(tier, tier));
        }

        int hatchAuraCost = tier * 200 + 200;
        if (fudi.getAuraCurrent() < hatchAuraCost) {
            throw new IllegalStateException("灵气不足（孵化需 %d，当前 %d）".formatted(hatchAuraCost, fudi.getAuraCurrent()));
        }
        fudi.setAuraCurrent(fudi.getAuraCurrent() - hatchAuraCost);

        BeastQuality quality = rollBeastQuality();

        boolean isMutant = new Random().nextInt(100) < 5;
        List<String> mutationTraits = new ArrayList<>();
        if (isMutant) {
            mutationTraits.add(rollRandomTrait());
        }

        double levelSpeed = getLevelSpeedMultiplier(cellLevel, tier);
        double productionInterval = 4.0 / levelSpeed;
        double baseHatchHours = 24 + tier * 8;
        double hatchHours = baseHatchHours / levelSpeed;

        String beastName = eggName.replace("兽卵", "").replace("蛋", "灵兽");
        LocalDateTime now = LocalDateTime.now();

        cell.put("beast_id", eggTemplate.getId().intValue());
        cell.put("beast_name", beastName);
        cell.put("tier", tier);
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
        log.info("用户 {} 在地块 {} 孵化 {} (T{}, {}{})", userId, cellId, beastName, tier, quality.getChineseName(), isMutant ? ", 变异" : "");

        return PenCellVO.builder()
                .cellId(cellId)
                .cellLevel(cellLevel)
                .beastId(eggTemplate.getId().intValue())
                .beastName(beastName)
                .tier(tier)
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

    public PenCellVO hatchBeastByInput(Long userId, String position, String input) {
        Integer cellId = parseCellId(position);
        var result = itemResolver.resolveEgg(userId, input);
        return switch (result) {
            case ItemResolver.Found(var template, var index) -> {
                var stackableItem = stackableItemRepository
                        .findByUserIdAndTemplateId(userId, template.getId())
                        .orElseThrow(() -> new IllegalStateException("背包中没有 [" + template.getName() + "]"));

                itemService.reduceStackableItem(userId, template.getId(), 1);

                var fudi = getFudiByUserId(userId)
                        .orElseThrow(() -> new IllegalStateException("未找到福地"));
                consumeSpiritEnergy(fudi, 10);
                checkAuraDepleted(fudi);

                var cell = getCellById(fudi, cellId);
                if (cell == null || !CellType.PEN.getCode().equals(cell.get("type"))) {
                    throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
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
                if (fudi.getAuraCurrent() < hatchAuraCost) {
                    throw new IllegalStateException("灵气不足（孵化需 %d，当前 %d）".formatted(hatchAuraCost, fudi.getAuraCurrent()));
                }
                fudi.setAuraCurrent(fudi.getAuraCurrent() - hatchAuraCost);

                BeastQuality quality = rollBeastQuality();

                boolean isMutant = new java.util.Random().nextInt(100) < 5;
                List<String> mutationTraits = new ArrayList<>();
                if (isMutant) {
                    mutationTraits.add(rollRandomTrait());
                }

                double levelSpeed = getLevelSpeedMultiplier(cellLevel, tier);
                double productionInterval = 4.0 / levelSpeed;
                double baseHatchHours = 24 + tier * 8;
                double hatchHours = baseHatchHours / levelSpeed;

                String beastName = template.getName().replace("兽卵", "").replace("蛋", "灵兽");
                LocalDateTime now = LocalDateTime.now();

                cell.put("beast_id", template.getId().intValue());
                cell.put("beast_name", beastName);
                cell.put("tier", tier);
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
                        .cellId(cellId)
                        .cellLevel(cellLevel)
                        .beastId(template.getId().intValue())
                        .beastName(beastName)
                        .tier(tier)
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

    // ===================== 送礼系统 =====================

    public ServiceResult<Map<String, Object>> giveGift(PlatformType platform, String openId, String itemName) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(giveGift(auth.userId(), itemName));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public Map<String, Object> giveGift(Long userId, String itemName) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        // 查找玩家背包中的装备
        List<Equipment> unequipped = equipmentRepository.findByUserId(userId).stream()
                .filter(e -> !e.getEquipped() && e.getName().contains(itemName))
                .toList();

        if (unequipped.isEmpty()) {
            throw new IllegalStateException("背包中未找到 [" + itemName + "]");
        }

        if (unequipped.size() > 1) {
            throw new IllegalStateException("找到多个 [" + itemName + "]，请使用更精确的名称");
        }

        Equipment gift = unequipped.getFirst();
        ItemTemplate template = itemTemplateRepository.findById(gift.getTemplateId() != null ?
                                                                        gift.getTemplateId() : (long) 1)
                .orElse(null);

        // 获取物品质量倍率
        double qualityMultiplier = gift.getQualityMultiplier() != null ? gift.getQualityMultiplier() : 1.0;

        // 从 SpiritForm 获取喜好
        Spirit spirit = spiritRepository.findByFudiId(fudi.getId())
                .orElseThrow(() -> new IllegalStateException("地灵不存在"));

        Set<String> likedTags = Set.of();
        Set<String> dislikedTags = Set.of();
        SpiritForm spiritForm = spiritFormMapper.selectOneById(spirit.getFormId().longValue());
        if (spiritForm != null) {
            likedTags = spiritForm.getLikedTags() != null ? spiritForm.getLikedTags() : Set.of();
            dislikedTags = spiritForm.getDislikedTags() != null ? spiritForm.getDislikedTags() : Set.of();
        }

        Set<String> itemTags = template != null && template.getTags() != null ?
                template.getTags() : Set.of();

        // 匹配判定
        boolean isLiked = itemTags.stream().anyMatch(likedTags::contains);
        boolean isDisliked = itemTags.stream().anyMatch(dislikedTags::contains);

        int oldAffection = spirit.getAffection();
        int change;
        String reaction;

        if (isLiked) {
            // 喜爱：+10~50，幅度取决于 qualityMultiplier
            change = (int) (10 + qualityMultiplier * 40);
            reaction = "开心";
            change = Math.min(50, change);
        } else if (isDisliked) {
            // 讨厌：-5~20
            change = -(int) (5 + qualityMultiplier * 15);
            reaction = "嫌弃";
            change = Math.max(-20, change);
        } else {
            // 中性：+1~3
            change = 1 + new Random().nextInt(3);
            reaction = "平淡";
        }

        spirit.addAffection(change);

        // 删除物品
        equipmentRepository.deleteById(gift.getId());

        spiritRepository.save(spirit);
        fudiRepository.save(fudi);

        Map<String, Object> result = new HashMap<>();
        result.put("itemName", gift.getName());
        result.put("oldAffection", oldAffection);
        result.put("newAffection", spirit.getAffection());
        result.put("change", change);
        result.put("reaction", reaction);
        result.put("isLiked", isLiked);
        result.put("isDisliked", isDisliked);

        return result;
    }

    // ===================== 灵兽系统 — 产出 =====================

    public BeastProduceVO collectProduce(Long userId, String position) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 3);
        checkAuraDepleted(fudi);

        Map<String, Object> cell = getCellById(fudi, cellId);
        if (cell == null || !CellType.PEN.getCode().equals(cell.get("type"))) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }

        if (Boolean.TRUE.equals(cell.get("is_incubating"))) {
            throw new IllegalStateException("灵兽尚在孵化中");
        }

        checkBeastAuraDepletion(fudi, cell);
        updateBeastProduction(cell, getBeastQuality(cell), fudi);
        int stored = (Integer) cell.getOrDefault("production_stored", 0);
        if (stored <= 0) {
            throw new IllegalStateException("暂无产出可收取");
        }

        String itemName = (String) cell.getOrDefault("beast_name", "灵兽") + "材料";

        cell.put("production_stored", 0);
        fudiRepository.save(fudi);

        log.info("用户 {} 收取地块 {} 的灵兽产出 {} 件", userId, cellId, stored);

        return BeastProduceVO.builder()
                .cellId(cellId)
                .beastName((String) cell.get("beast_name"))
                .totalProduced(stored)
                .itemName(itemName)
                .build();
    }

    public Map<String, Object> collectAllProduce(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 3);
        checkAuraDepleted(fudi);

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
            updateBeastProduction(cell, quality, fudi);

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

    public Map<String, Object> releaseBeast(Long userId, String position) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        checkAuraDepleted(fudi);

        Map<String, Object> cell = getCellById(fudi, cellId);
        if (cell == null || !CellType.PEN.getCode().equals(cell.get("type"))) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }

        int tier = (Integer) cell.getOrDefault("tier", 1);
        BeastQuality quality = getBeastQuality(cell);
        double qualityOutput = quality.getOutputMultiplier();
        int essenceValue = (int) (tier * 200 * qualityOutput);

        String beastName = (String) cell.get("beast_name");

        clearBeastCell(cell);

        fudiRepository.save(fudi);

        log.info("用户 {} 放生 {} (T{}/{})，获得灵兽精华(价值{}灵气)", userId, beastName, tier, quality.getChineseName(), essenceValue);

        return Map.of("beastName", beastName, "essenceValue", essenceValue);
    }

    public PenCellVO evolveBeast(Long userId, String position, String mode) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 8);
        checkAuraDepleted(fudi);

        Map<String, Object> cell = getCellById(fudi, cellId);
        if (cell == null || !CellType.PEN.getCode().equals(cell.get("type"))) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }
        if (Boolean.TRUE.equals(cell.get("is_incubating"))) {
            throw new IllegalStateException("灵兽尚在孵化中");
        }

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
            return breakthroughBeastQuality(fudi, cell, userId, cellId);
        } else {
            return evolveBeastTier(fudi, cell, userId, cellId);
        }
    }

    private PenCellVO evolveBeastTier(Fudi fudi, Map<String, Object> cell, Long userId, Integer cellId) {
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

        Spirit spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
        int affectionBonus = spirit != null && spirit.getAffection() != null ? Math.min(15, spirit.getAffection() / 7) : 0;
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
                "用户 {} 进化地块 {} 的灵兽 T{}->T{}{}", userId, cellId, currentTier, newTier,
                qualityUpgraded ? " (品质连带提升!)" : ""
        );

        return buildPenCellVO(cell);
    }

    private PenCellVO breakthroughBeastQuality(Fudi fudi, Map<String, Object> cell, Long userId, Integer cellId) {
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

        Spirit spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
        int affectionBonus = spirit != null && spirit.getAffection() != null ? Math.min(20, spirit.getAffection() / 5) : 0;
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
        log.info("用户 {} 品质突破地块 {} 的灵兽 {} -> {}", userId, cellId, currentQuality.getChineseName(), nextQuality.getChineseName());

        return buildPenCellVO(cell);
    }

    // ===================== 灵兽系统 — 灵气耗尽与寿命检查 =====================

    private void checkBeastAuraDepletion(Fudi fudi, Map<String, Object> cell) {
        fudi.updateAura();
        String beastName = (String) cell.getOrDefault("beast_name", "灵兽");

        int aura = fudi.getAuraCurrent();
        if (aura <= 0) {
            String depletionStartTime = (String) cell.get("aura_depletion_start");
            LocalDateTime now = LocalDateTime.now();
            if (depletionStartTime == null) {
                cell.put("aura_depletion_start", now.toString());
            } else {
                long depletedSeconds = java.time.Duration.between(LocalDateTime.parse(depletionStartTime), now).getSeconds();
                if (depletedSeconds >= 86400) {
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
        if (cellLevel < minRequired) return 0.5;
        int levelDiff = cellLevel - minRequired;
        return 1.0 + levelDiff * 0.15;
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

    @SuppressWarnings("unchecked")
    private void updateBeastProduction(Map<String, Object> cell, BeastQuality quality, Fudi fudi) {
        if (!cell.containsKey("last_production_time") || !cell.containsKey("mature_time")) return;

        LocalDateTime matureTime = LocalDateTime.parse((String) cell.get("mature_time"));
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(matureTime)) {
            cell.put("is_incubating", true);
            return;
        }
        cell.put("is_incubating", false);

        if (cell.containsKey("birth_time") && cell.containsKey("lifespan_days")) {
            LocalDateTime birthTime = LocalDateTime.parse((String) cell.get("birth_time"));
            double lifespanDays = (Double) cell.get("lifespan_days");
            long livedSeconds = java.time.Duration.between(birthTime, now).getSeconds();
            if (livedSeconds >= lifespanDays * 86400) {
                String beastName = (String) cell.get("beast_name");
                int tier = (Integer) cell.getOrDefault("tier", 1);
                int essenceValue = (int) (tier * 200 * quality.getOutputMultiplier());
                clearBeastCell(cell);
                log.info("灵兽 {} 寿终正寝，获得灵兽精华(价值{}灵气)", beastName, essenceValue);
                return;
            }
        }

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
        int perCycle = (int) Math.round(tier * quality.getOutputMultiplier() * auraFactor * (1 + new Random().nextInt(tier + 1)) / 2.0);
        int produced = Math.max(1, perCycle * cycles);

        int currentStored = (Integer) cell.getOrDefault("production_stored", 0);
        int maxStorage = tier * 20;
        int newStored = Math.min(maxStorage, currentStored + produced);

        cell.put("production_stored", newStored);
        cell.put("last_production_time", now.toString());
    }

    private PenCellVO buildPenCellVO(Map<String, Object> cell) {
        @SuppressWarnings("unchecked")
        List<String> traits = (List<String>) cell.getOrDefault("mutation_traits", List.of());
        BeastQuality quality = getBeastQuality(cell);

        return PenCellVO.builder()
                .cellId((Integer) cell.get("cell_id"))
                .cellLevel((Integer) cell.getOrDefault("cell_level", 1))
                .beastId((Integer) cell.getOrDefault("beast_id", 0))
                .beastName((String) cell.getOrDefault("beast_name", "未知灵兽"))
                .tier((Integer) cell.getOrDefault("tier", 1))
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
     * 获取福地网格状态（供LLM查询可用地块编号）
     */
    public Map<String, Object> getGridStatus(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        int totalCells = getTotalCellCount(fudi);
        List<Map<String, Object>> occupiedCells = new ArrayList<>();
        List<Integer> emptyCellIds = new ArrayList<>();

        if (fudi.getGridLayout() != null && fudi.getGridLayout().containsKey("cells")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");

            for (Map<String, Object> cell : cells) {
                Integer cellId = (Integer) cell.get("cell_id");
                String type = (String) cell.get("type");

                if ("empty".equals(type)) {
                    emptyCellIds.add(cellId);
                    continue;
                }

                Map<String, Object> cellInfo = new HashMap<>();
                cellInfo.put("cellId", cellId);
                cellInfo.put("type", type);

                switch (type) {
                    case "farm" -> {
                        cellInfo.put("cropName", cell.get("crop_name"));
                        updateGrowthProgress(cell);
                        Double progress = (Double) cell.get("growth_progress");
                        cellInfo.put("growthProgress", progress);
                        cellInfo.put("isMature", progress != null && progress >= 1.0);
                        cellInfo.put("cellLevel", cell.get("cell_level"));
                    }
                    case "pen" -> {
                        cellInfo.put("beastName", cell.getOrDefault("beast_name", "空兽栏"));
                        cellInfo.put("tier", cell.get("tier"));
                        cellInfo.put("quality", cell.get("quality"));
                        cellInfo.put("productionStored", cell.getOrDefault("production_stored", 0));
                        cellInfo.put("isIncubating", cell.get("is_incubating"));
                        cellInfo.put("cellLevel", cell.get("cell_level"));
                    }
                    case "node" -> {
                        cellInfo.put("level", cell.get("level"));
                        cellInfo.put("cellLevel", cell.get("cell_level"));
                    }
                }

                occupiedCells.add(cellInfo);
            }
        }

        return Map.of(
                "totalCells", totalCells,
                "occupiedCount", occupiedCells.size(),
                "emptyCount", emptyCellIds.size(),
                "emptyCellIds", emptyCellIds,
                "occupiedCells", occupiedCells
        );
    }

    // ===================== 地块操作辅助方法 =====================

    private Integer parseCellId(String position) {
        try {
            return Integer.valueOf(position);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("地块编号格式错误，请输入数字编号");
        }
    }

    private boolean isCellEmpty(Fudi fudi, Integer cellId) {
        Map<String, Object> cell = getCellById(fudi, cellId);
        return cell == null || "empty".equals(cell.get("type"));
    }

    private Map<String, Object> getCellById(Fudi fudi, Integer cellId) {
        if (fudi.getGridLayout() == null || !fudi.getGridLayout().containsKey("cells")) {
            return null;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");

        return cells.stream()
                .filter(cell -> cellId.equals(cell.get("cell_id")))
                .findFirst()
                .orElse(null);
    }

    private void addCellToGrid(Fudi fudi, Map<String, Object> cell) {
        if (fudi.getGridLayout() == null) {
            fudi.setGridLayout(new HashMap<>());
        }

        if (!fudi.getGridLayout().containsKey("cells")) {
            fudi.getGridLayout().put("cells", new ArrayList<>());
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");

        // 如果该cell_id已存在（覆盖更新），否则追加
        for (int i = 0; i < cells.size(); i++) {
            if (cell.get("cell_id").equals(cells.get(i).get("cell_id"))) {
                cells.set(i, cell);
                return;
            }
        }
        cells.add(cell);
    }

    private void removeCellFromGrid(Fudi fudi, Integer cellId) {
        if (fudi.getGridLayout() == null || !fudi.getGridLayout().containsKey("cells")) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");
        for (int i = 0; i < cells.size(); i++) {
            if (cellId.equals(cells.get(i).get("cell_id"))) {
                // 替换为empty，保留cell_id槽位
                Map<String, Object> emptyCell = new HashMap<>();
                emptyCell.put("cell_id", cellId);
                emptyCell.put("type", "empty");
                cells.set(i, emptyCell);
                return;
            }
        }
    }

    private ItemTemplate findSeedTemplateByName(String name) {
        return itemTemplateRepository.findByType(ItemType.SEED).stream()
                .filter(t -> t.getName().equals(name) || t.getName().contains(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未找到种子: %s".formatted(name)));
    }

    private int calculateYield(Integer cropId, int tribulationStage) {
        int baseYield = 1 + new Random().nextInt(3);
        int bonus = tribulationStage / 5;
        return baseYield + bonus;
    }

    private double getBaseGrowthHours(Integer cropId) {
        return itemTemplateRepository.findById((long) cropId)
                .map(template -> template.getGrowTime() != null ? template.getGrowTime().doubleValue() : 5.0)
                .orElse(5.0);
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
}
