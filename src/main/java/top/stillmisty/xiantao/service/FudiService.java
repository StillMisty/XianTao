package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.beast.vo.BeastStatusVO;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.item.vo.ItemEntry;
import top.stillmisty.xiantao.domain.land.entity.Fudi;
import top.stillmisty.xiantao.domain.land.entity.Spirit;
import top.stillmisty.xiantao.domain.land.entity.SpiritForm;
import top.stillmisty.xiantao.domain.land.enums.*;
import top.stillmisty.xiantao.domain.land.repository.FudiRepository;
import top.stillmisty.xiantao.domain.land.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.land.vo.*;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.infrastructure.mapper.SpiritFormMapper;
import top.stillmisty.xiantao.service.annotation.ConsumeSpiritEnergy;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 福地服务层
 * 移除灵气系统、献祭系统、阵眼，替换为灵石经济。
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
    private final StackableItemRepository stackableItemRepository;
    private final ItemResolver itemResolver;
    private final UserRepository userRepository;
    private final SpiritRepository spiritRepository;
    private final SpiritFormMapper spiritFormMapper;
    private final BeastRepository beastRepository;

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

    public ServiceResult<Map<String, Object>> collect(PlatformType platform, String openId, String position) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(collect(auth.userId(), position));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> collectAll(PlatformType platform, String openId) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(collectAll(auth.userId()));
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

    public ServiceResult<Map<String, Object>> deployBeast(PlatformType platform, String openId, String position) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(deployBeast(auth.userId(), position));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> undeployBeast(PlatformType platform, String openId, String position) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(undeployBeast(auth.userId(), position));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> recoverBeast(PlatformType platform, String openId, String position) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(recoverBeast(auth.userId(), position));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<List<BeastStatusVO>> getDeployedBeasts(PlatformType platform, String openId) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(getDeployedBeasts(auth.userId()));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> giveGift(PlatformType platform, String openId, String itemName) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(giveGift(auth.userId(), itemName));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> triggerTribulation(PlatformType platform, String openId) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(triggerTribulation(auth.userId()));
        } catch (IllegalStateException e) {
            return new ServiceResult.Failure<>(e.getMessage());
        }
    }

    // ===================== 内部 API =====================

    // ===================== 福地基础管理 =====================

    public void createFudi(Long userId, MBTIPersonality mbtiType) {
        if (fudiRepository.existsByUserId(userId)) {
            throw new IllegalStateException("用户已拥有福地");
        }

        Fudi fudi = Fudi.create();
        fudi.setUserId(userId);
        fudi.setTribulationStage(0);
        fudi.setTribulationWinStreak(0);

        Map<String, Object> gridLayout = new HashMap<>();
        gridLayout.put("cells", new ArrayList<>());
        fudi.setGridLayout(gridLayout);
        fudi.setLastOnlineTime(LocalDateTime.now());

        fudi = fudiRepository.save(fudi);

        Spirit spirit = createSpiritForFudi(fudi, mbtiType);
        spiritRepository.save(spirit);

        autoExpandCells(fudi);
        fudiRepository.save(fudi);
    }

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

    public Optional<Fudi> getFudiByUserId(Long userId) {
        Optional<Fudi> fudiOpt = fudiRepository.findByUserId(userId);
        fudiOpt.ifPresent(fudi -> {
            fudi.touchOnlineTime();
            spiritRepository.findByFudiId(fudi.getId()).ifPresent(spirit -> {
                spirit.restoreEnergy(fudi.getTribulationStage());
                spirit.updateEmotionState();
                spiritRepository.save(spirit);
            });
            autoExpandCells(fudi);
            fudiRepository.save(fudi);
        });
        return fudiOpt;
    }

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

    private int getTotalCellCount(Fudi fudi) {
        if (fudi.getGridLayout() == null || !fudi.getGridLayout().containsKey("cells")) return 0;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");
        return cells.size();
    }

    /**
     * 检查灵石是否足够
     */
    private void checkSpiritStones(Long userId, int cost) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getSpiritStones() == null || user.getSpiritStones() < cost) {
            throw new IllegalStateException("灵石不足（需要 %d，当前 %d）".formatted(cost, user.getSpiritStones() != null ? user.getSpiritStones() : 0));
        }
    }

    /**
     * 扣除灵石
     */
    private void deductSpiritStones(Long userId, int cost) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setSpiritStones(user.getSpiritStones() - cost);
        userRepository.save(user);
    }

    public FudiStatusVO getFudiStatus(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        var user = userRepository.findById(userId).orElseThrow();
        String tribulationResult = resolveTribulation(fudi, user.getLevel(), user.getStatValue(), false);

        spiritRepository.findByFudiId(fudi.getId()).ifPresent(spirit -> {
            spirit.restoreEnergy(fudi.getTribulationStage());
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

        int totalBeasts = (int) cellDetails.stream().filter(c -> c.getType() == CellType.PEN && c.getName() != null).count();

        Spirit spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);

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
                .tribulationStage(fudi.getTribulationStage())
                .totalCells(getTotalCellCount(fudi))
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

    /**
     * 主动触发天劫
     */
    public Map<String, Object> triggerTribulation(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        User user = userRepository.findById(userId).orElseThrow();

        // 主动渡劫不需要检查7天限制
        String result = resolveTribulation(fudi, user.getLevel(), user.getStatValue(), true);
        fudiRepository.save(fudi);

        return Map.of(
                "tribulationResult", result != null ? result : "天劫未触发",
                "tribulationStage", fudi.getTribulationStage(),
                "winStreak", fudi.getTribulationWinStreak()
        );
    }

    private String resolveTribulation(Fudi fudi, int playerLevel, int playerStr) {
        return resolveTribulation(fudi, playerLevel, playerStr, false);
    }

    private String resolveTribulation(Fudi fudi, int playerLevel, int playerStr, boolean forceTrigger) {
        LocalDateTime referenceTime = fudi.getLastTribulationTime() != null
                ? fudi.getLastTribulationTime()
                : fudi.getCreateTime();

        // 如果不是主动触发，检查7天限制
        if (!forceTrigger && java.time.Duration.between(referenceTime, LocalDateTime.now()).toDays() < 7) {
            return null;
        }

        int attack = playerLevel * 80 + fudi.getTribulationStage() * 200;
        int defense = fudi.calculateTribulationDefense(playerStr);

        fudi.setLastTribulationTime(LocalDateTime.now());

        boolean compassionTriggered = checkTribulationCompassion(fudi, attack, defense);

        if (defense > attack) {
            return compassionTriggered ? null : applyTribulationWin(fudi, attack, defense);
        } else if (compassionTriggered) {
            return applyTribulationCompassion(fudi, attack, defense);
        } else {
            return applyTribulationLoss(fudi, attack, defense);
        }
    }

    private boolean checkTribulationCompassion(Fudi fudi, int attack, int defense) {
        Spirit spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
        int affection = spirit != null && spirit.getAffection() != null ? spirit.getAffection() : 0;
        return affection >= 800 && defense >= attack * 0.8 && defense < attack;
    }

    /**
     * 天劫怜悯：视为胜利但精力归零，触发FATIGUED
     */
    private String applyTribulationCompassion(Fudi fudi, int attack, int defense) {
        int oldStage = fudi.getTribulationStage();
        int newWinStreak = fudi.getTribulationWinStreak() + 1;
        int newStage = oldStage + 1;

        fudi.setTribulationWinStreak(newWinStreak);
        fudi.setTribulationStage(newStage);

        // 奖励灵石：连胜×100
        int stoneReward = newWinStreak * 100;
        User user = userRepository.findById(fudi.getUserId()).orElseThrow();
        user.setSpiritStones((user.getSpiritStones() != null ? user.getSpiritStones() : 0) + stoneReward);
        userRepository.save(user);

        spiritRepository.findByFudiId(fudi.getId()).ifPresent(spirit -> {
            spirit.setEnergy(0);
            spirit.setEmotionState(EmotionState.FATIGUED);
            spirit.setLastEnergyUpdate(LocalDateTime.now());
            spiritRepository.save(spirit);
        });

        return "🛡️⚡ 天劫降临！地灵燃烧灵体为你挡下了天雷……\n" +
                "   攻击力：" + attack + " ｜ 防御力：" + defense + " ｜ 怜悯庇护！\n" +
                "   劫数：" + oldStage + " → " + newStage + " ｜ 连胜×" + newWinStreak + "\n" +
                "   灵石奖励：+" + stoneReward + "\n" +
                "   精力归零，地灵陷入疲惫…";
    }

    @SuppressWarnings("unchecked")
    private String applyTribulationWin(Fudi fudi, int attack, int defense) {
        int oldStage = fudi.getTribulationStage();
        int newWinStreak = fudi.getTribulationWinStreak() + 1;
        int newStage = oldStage + 1;

        fudi.setTribulationWinStreak(newWinStreak);
        fudi.setTribulationStage(newStage);

        int stoneReward = newWinStreak * 100;
        User user = userRepository.findById(fudi.getUserId()).orElseThrow();
        user.setSpiritStones((user.getSpiritStones() != null ? user.getSpiritStones() : 0) + stoneReward);
        userRepository.save(user);

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
                        "   灵石奖励：+%d ｜ 好感度：%d → %d",
                attack, defense,
                oldStage, newStage, newWinStreak,
                stoneReward, oldAffection, spirit != null ? spirit.getAffection() : 0
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

    // ===================== 统一收取系统（种植收获 + 灵兽产出） =====================

    @ConsumeSpiritEnergy(5)
    public Map<String, Object> collect(Long userId, String position) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        Map<String, Object> cell = getCellById(fudi, cellId);
        if (cell == null || "empty".equals(cell.get("type"))) {
            throw new IllegalStateException("地块 " + cellId + " 为空");
        }

        String type = (String) cell.get("type");

        if (CellType.FARM.getCode().equals(type)) {
            return harvestCrop(fudi, cell, cellId);
        } else if (CellType.PEN.getCode().equals(type)) {
            return collectBeastProduce(fudi, cell, cellId);
        } else {
            throw new IllegalStateException("地块 " + cellId + " 无可收取内容");
        }
    }

    public Map<String, Object> collectAll(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        if (fudi.getGridLayout() == null || !fudi.getGridLayout().containsKey("cells")) {
            return Map.of("harvested", 0, "collected", 0, "totalItems", 0);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");

        int harvestCount = 0;
        int collectedCount = 0;
        int totalItems = 0;

        // 收取灵田
        List<Integer> toRemove = new ArrayList<>();
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

            int yield = calculateYield((Integer) cell.get("crop_id"), fudi.getTribulationStage());
            int hCount = (Integer) cell.getOrDefault("harvest_count", 0) + 1;
            int maxHarvest = (Integer) cell.getOrDefault("max_harvest", 1);

            if (isPerennial && hCount < maxHarvest) {
                cell.put("harvest_count", hCount);
                cell.put("growth_progress", 0.0);
                cell.put("plant_time", LocalDateTime.now().toString());
                double baseGrowthHours = (Double) cell.get("base_growth_hours");
                double levelSpeed = (Double) cell.getOrDefault("level_speed_multiplier", 1.0);
                cell.put("mature_time", LocalDateTime.now().plusHours((long) (baseGrowthHours / levelSpeed)).toString());
            } else {
                toRemove.add((Integer) cell.get("cell_id"));
            }

            totalItems += yield;
            harvestCount++;
        }

        for (Integer id : toRemove) {
            removeCellFromGrid(fudi, id);
        }

        // 精力扣除：第一个收获已由 ConsumeSpiritEnergy(5) 扣除
        final int extraCount = harvestCount;
        if (extraCount > 1) {
            spiritRepository.findByFudiId(fudi.getId()).ifPresent(spirit -> {
                spirit.restoreEnergy(fudi.getTribulationStage());
                int extraCost = spirit.calculateEnergyConsumption(5 * (extraCount - 1));
                spirit.deductEnergy(extraCost);
                spirit.setLastEnergyUpdate(LocalDateTime.now());
                spiritRepository.save(spirit);
            });
        }

        // 收取兽栏产出
        for (Map<String, Object> cell : cells) {
            if (!CellType.PEN.getCode().equals(cell.get("type"))) continue;
            if (Boolean.TRUE.equals(cell.get("is_incubating"))) continue;

            updateBeastProduction(cell, fudi);
            int stored = (Integer) cell.getOrDefault("production_stored", 0);
            if (stored > 0) {
                totalItems += stored;
                collectedCount++;
                cell.put("production_stored", 0);
            }
        }

        fudiRepository.save(fudi);

        return Map.of("harvested", harvestCount, "collected", collectedCount, "totalItems", totalItems);
    }

    private Map<String, Object> harvestCrop(Fudi fudi, Map<String, Object> cell, Integer cellId) {
        updateGrowthProgress(cell);

        Double progress = (Double) cell.get("growth_progress");
        boolean isPerennial = Boolean.TRUE.equals(cell.get("is_perennial"));

        if (!isPerennial && progress != null && progress > 1.0 && isWilted(cell)) {
            String cropName = (String) cell.get("crop_name");
            removeCellFromGrid(fudi, cellId);
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
            cell.put("mature_time", LocalDateTime.now().plusHours((long) (baseGrowthHours / levelSpeed)).toString());
        } else {
            removeCellFromGrid(fudi, cellId);
        }

        fudiRepository.save(fudi);

        log.info("用户 {} 收获地块 {} 的 {}，获得 {}份", fudi.getUserId(), cellId, cropName, yield);

        return Map.of("cellId", cellId, "cropName", cropName, "yield", yield, "type", "farm");
    }

    private Map<String, Object> collectBeastProduce(Fudi fudi, Map<String, Object> cell, Integer cellId) {
        if (Boolean.TRUE.equals(cell.get("is_incubating"))) {
            throw new IllegalStateException("灵兽尚在孵化中");
        }

        updateBeastProduction(cell, fudi);
        int stored = (Integer) cell.getOrDefault("production_stored", 0);
        if (stored <= 0) {
            throw new IllegalStateException("暂无产出可收取");
        }

        String beastName = (String) cell.getOrDefault("beast_name", "灵兽");
        cell.put("production_stored", 0);
        fudiRepository.save(fudi);

        log.info("用户 {} 收取地块 {} 的灵兽产出 {} 件", fudi.getUserId(), cellId, stored);

        return Map.of("cellId", cellId, "beastName", beastName, "totalItems", stored, "type", "pen");
    }

    // ===================== 种植系统 =====================

    public FarmCellVO plantCrop(Long userId, Integer cellId, Integer cropId, String cropName, int cropTier) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 3);

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

        // 灵石消耗：tier × 5
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

    // ===================== 建造/拆除系统 =====================

    public Map<String, Object> buildCell(Long userId, String position, CellType type) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 3);

        if (!isCellEmpty(fudi, cellId)) {
            throw new IllegalStateException("地块 " + cellId + " 已被占用");
        }

        Map<String, Object> cell = new HashMap<>();
        cell.put("cell_id", cellId);
        cell.put("type", type.getCode());
        cell.put("cell_level", 1);

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

        if (isCellEmpty(fudi, cellId)) {
            throw new IllegalStateException("地块 " + cellId + " 为空");
        }

        Map<String, Object> cell = getCellById(fudi, cellId);
        if (cell == null) {
            throw new IllegalStateException("地块 " + cellId + " 不存在");
        }

        CellType type = CellType.fromCode((String) cell.get("type"));

        // 拆除兽栏：灵兽退回栏外休憩（penned_cell_id = null）
        if (type == CellType.PEN && cell.containsKey("beast_id") && (Integer) cell.get("beast_id") > 0) {
            var beasts = beastRepository.findByFudiId(fudi.getId());
            for (Beast b : beasts) {
                if (b.getPennedCellId() != null && b.getPennedCellId().equals(cellId)) {
                    b.setPennedCellId(null);
                    beastRepository.save(b);
                    break;
                }
            }
        }

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

        Map<String, Object> cell = getCellById(fudi, cellId);
        if (cell == null || "empty".equals(cell.get("type"))) {
            throw new IllegalStateException("地块 " + cellId + " 为空");
        }

        int currentLevel = (Integer) cell.getOrDefault("cell_level", 1);
        if (currentLevel >= 5) {
            throw new IllegalStateException("已是最高等级 Lv5");
        }

        int cost = currentLevel == 1 ? 200 : currentLevel == 2 ? 400 : currentLevel == 3 ? 800 : 1600;
        checkSpiritStones(userId, cost);
        deductSpiritStones(userId, cost);

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

        // 孵化灵石消耗：tier × 200 + 200
        int stoneCost = tier * 200 + 200;
        checkSpiritStones(userId, stoneCost);
        deductSpiritStones(userId, stoneCost);

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
        cell.put("birth_time", now.toString());
        cell.put("power_score", tier * 10);
        cell.put("evolution_count", 0);
        cell.put("hp_current", tier * 200);
        cell.put("hp_max", tier * 200);
        cell.put("is_deployed", false);

        fudiRepository.save(fudi);

        Beast beast = new Beast();
        beast.setUserId(userId);
        beast.setFudiId(fudi.getId());
        beast.setTemplateId(eggTemplate.getId());
        beast.setBeastName(beastName);
        beast.setTier(tier);
        beast.setQuality(quality.getCode());
        beast.setIsMutant(isMutant);
        beast.setMutationTraits(mutationTraits);
        beast.setLevel(1);
        beast.setExp(0);
        beast.setAttack(calculateBeastAttack(1, quality));
        beast.setDefense(calculateBeastDefense(1, quality));
        int maxHp = tier * 200;
        beast.setMaxHp(maxHp);
        beast.setHpCurrent(maxHp);
        beast.setSkills(List.of());
        beast.setIsDeployed(false);
        beast.setRecoveryUntil(null);
        beast.setPennedCellId(cellId);
        beast.setBirthTime(now);
        beast.setEvolutionCount(0);
        beastRepository.save(beast);

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

                yield hatchBeastWithTemplate(userId, cellId, template);
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

    private PenCellVO hatchBeastWithTemplate(Long userId, Integer cellId, ItemTemplate eggTemplate) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 10);

        Map<String, Object> cell = getCellById(fudi, cellId);
        if (cell == null || !CellType.PEN.getCode().equals(cell.get("type"))) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }
        if (cell.containsKey("beast_id") && (Integer) cell.get("beast_id") > 0) {
            throw new IllegalStateException("该兽栏已有灵兽，请先放生");
        }

        int tier = getCropTier(eggTemplate.getGrowTime() != null ? eggTemplate.getGrowTime() : 72);
        int cellLevel = (Integer) cell.getOrDefault("cell_level", 1);
        if (cellLevel < tier) {
            throw new IllegalStateException("灵兽等阶(T%d)需要至少Lv%d兽栏".formatted(tier, tier));
        }

        int stoneCost = tier * 200 + 200;
        checkSpiritStones(userId, stoneCost);
        deductSpiritStones(userId, stoneCost);

        BeastQuality quality = rollBeastQuality();
        boolean isMutant = new Random().nextInt(100) < 5;
        List<String> mutationTraits = new ArrayList<>();
        if (isMutant) mutationTraits.add(rollRandomTrait());

        double levelSpeed = getLevelSpeedMultiplier(cellLevel, tier);
        double productionInterval = 4.0 / levelSpeed;
        double baseHatchHours = 24 + tier * 8;
        double hatchHours = baseHatchHours / levelSpeed;

        String beastName = eggTemplate.getName().replace("兽卵", "").replace("蛋", "灵兽");
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
        cell.put("birth_time", now.toString());
        cell.put("power_score", tier * 10);
        cell.put("evolution_count", 0);
        cell.put("hp_current", tier * 200);
        cell.put("hp_max", tier * 200);
        cell.put("is_deployed", false);

        fudiRepository.save(fudi);

        Beast beast = new Beast();
        beast.setUserId(userId);
        beast.setFudiId(fudi.getId());
        beast.setTemplateId(eggTemplate.getId());
        beast.setBeastName(beastName);
        beast.setTier(tier);
        beast.setQuality(quality.getCode());
        beast.setIsMutant(isMutant);
        beast.setMutationTraits(mutationTraits);
        beast.setLevel(1);
        beast.setExp(0);
        beast.setAttack(calculateBeastAttack(1, quality));
        beast.setDefense(calculateBeastDefense(1, quality));
        int maxHp = tier * 200;
        beast.setMaxHp(maxHp);
        beast.setHpCurrent(maxHp);
        beast.setSkills(List.of());
        beast.setIsDeployed(false);
        beast.setRecoveryUntil(null);
        beast.setPennedCellId(cellId);
        beast.setBirthTime(now);
        beast.setEvolutionCount(0);
        beastRepository.save(beast);

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
                .birthTime(now)
                .build();
    }

    // ===================== 送礼系统 =====================

    public Map<String, Object> giveGift(Long userId, String itemName) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        List<StackableItem> items = stackableItemRepository.findByUserId(userId).stream()
                .filter(e -> e.getName().contains(itemName))
                .toList();

        if (items.isEmpty()) {
            throw new IllegalStateException("背包中未找到 [" + itemName + "]");
        }

        if (items.size() > 1) {
            throw new IllegalStateException("找到多个 [" + itemName + "]，请使用更精确的名称");
        }

        StackableItem gift = items.getFirst();
        ItemTemplate template = itemTemplateRepository.findById(gift.getTemplateId() != null ?
                        gift.getTemplateId() : (long) 1)
                .orElse(null);

        Spirit spirit = spiritRepository.findByFudiId(fudi.getId())
                .orElseThrow(() -> new IllegalStateException("地灵不存在"));

        if (spirit.getLastGiftTime() != null &&
                spirit.getLastGiftTime().toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
            throw new IllegalStateException("今日已送过礼物，明天再来吧");
        }

        Set<String> likedTags = Set.of();
        Set<String> dislikedTags = Set.of();
        SpiritForm spiritForm = spiritFormMapper.selectOneById(spirit.getFormId().longValue());
        if (spiritForm != null) {
            likedTags = spiritForm.getLikedTags() != null ? spiritForm.getLikedTags() : Set.of();
            dislikedTags = spiritForm.getDislikedTags() != null ? spiritForm.getDislikedTags() : Set.of();
        }

        Set<String> itemTags = gift.getTags() != null && !gift.getTags().isEmpty() ?
                gift.getTags() :
                (template != null && template.getTags() != null ? template.getTags() : Set.of());

        boolean isLiked = itemTags.stream().anyMatch(likedTags::contains);
        boolean isDisliked = itemTags.stream().anyMatch(dislikedTags::contains);

        int oldAffection = spirit.getAffection();
        int change;
        String reaction;

        if (isLiked) {
            change = 10 + new Random().nextInt(41);
            reaction = "开心";
        } else if (isDisliked) {
            change = -(5 + new Random().nextInt(16));
            reaction = "嫌弃";
        } else {
            change = 1 + new Random().nextInt(3);
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

    // ===================== 灵兽系统 — 放生与进化 =====================

    public Map<String, Object> releaseBeast(Long userId, String position) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        Map<String, Object> cell = getCellById(fudi, cellId);
        if (cell == null || !CellType.PEN.getCode().equals(cell.get("type"))) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }

        String beastName = (String) cell.get("beast_name");
        int tier = (Integer) cell.getOrDefault("tier", 1);
        BeastQuality quality = getBeastQuality(cell);

        clearBeastCell(cell);

        var beasts = beastRepository.findByFudiId(fudi.getId());
        for (Beast b : beasts) {
            if (b.getPennedCellId() != null && b.getPennedCellId().equals(cellId)) {
                beastRepository.deleteById(b.getId());
                break;
            }
        }

        fudiRepository.save(fudi);

        log.info("用户 {} 放生 {} (T{}/{})", userId, beastName, tier, quality.getChineseName());

        return Map.of("beastName", beastName, "tier", tier, "quality", quality.getChineseName());
    }

    public PenCellVO evolveBeast(Long userId, String position, String mode) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 8);

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
        checkSpiritStones(userId, cost);
        deductSpiritStones(userId, cost);

        Spirit spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
        int affectionBonus = spirit != null && spirit.getAffection() != null ? Math.min(15, spirit.getAffection() / 7) : 0;
        int successRate = 85 + affectionBonus;
        boolean success = new Random().nextInt(100) < successRate;

        if (!success) {
            fudiRepository.save(fudi);
            throw new IllegalStateException("进化失败！进化石和灵石已消耗");
        }

        int newTier = currentTier + 1;
        cell.put("tier", newTier);
        cell.put("power_score", newTier * 10);
        cell.put("birth_time", LocalDateTime.now().toString());
        cell.put("evolution_count", (Integer) cell.getOrDefault("evolution_count", 0) + 1);
        cell.put("hp_max", newTier * 200);
        cell.put("hp_current", newTier * 200);

        boolean qualityUpgraded = false;
        if (new Random().nextInt(100) < 10 && getBeastQuality(cell) != BeastQuality.DIVINE) {
            BeastQuality newQuality = getBeastQuality(cell).next();
            cell.put("quality", newQuality.getCode());
            cell.put("quality_ordinal", newQuality.getOrder());
            qualityUpgraded = true;
        }

        boolean mutationTriggered = rollMutation(15);
        if (mutationTriggered) {
            addMutationTrait(cell);
        }

        fudiRepository.save(fudi);
        log.info("用户 {} 进化地块 {} 的灵兽 T{}->T{}{}", userId, cellId, currentTier, newTier,
                qualityUpgraded ? " (品质连带提升!)" : "");

        return buildPenCellVO(cell);
    }

    private PenCellVO breakthroughBeastQuality(Fudi fudi, Map<String, Object> cell, Long userId, Integer cellId) {
        BeastQuality currentQuality = getBeastQuality(cell);
        if (currentQuality == BeastQuality.DIVINE) {
            throw new IllegalStateException("已是最高品质神品");
        }

        BeastQuality nextQuality = currentQuality.next();
        int cost = nextQuality.getOrder() * 300;
        checkSpiritStones(userId, cost);
        deductSpiritStones(userId, cost);

        Spirit spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
        int affectionBonus = spirit != null && spirit.getAffection() != null ? Math.min(20, spirit.getAffection() / 5) : 0;
        int successRate = 60 + affectionBonus;
        boolean success = new Random().nextInt(100) < successRate;

        if (!success) {
            fudiRepository.save(fudi);
            throw new IllegalStateException("品质突破失败！进化石和灵石已消耗");
        }

        cell.put("quality", nextQuality.getCode());
        cell.put("quality_ordinal", nextQuality.getOrder());

        boolean mutationTriggered = rollMutation(10);
        if (mutationTriggered) {
            addMutationTrait(cell);
        }

        fudiRepository.save(fudi);
        log.info("用户 {} 品质突破地块 {} 的灵兽 {} -> {}", userId, cellId, currentQuality.getChineseName(), nextQuality.getChineseName());

        return buildPenCellVO(cell);
    }

    // ===================== 灵兽系统 — 出战/召回/恢复 =====================

    public Map<String, Object> deployBeast(Long userId, String position) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        Map<String, Object> cell = getCellById(fudi, cellId);
        if (cell == null || !CellType.PEN.getCode().equals(cell.get("type"))) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }
        if (Boolean.TRUE.equals(cell.get("is_incubating"))) {
            throw new IllegalStateException("灵兽尚在孵化中");
        }

        if (Boolean.TRUE.equals(cell.get("is_deployed"))) {
            return Map.of("success", true, "message", "灵兽已处于出战状态");
        }

        int hpCurrent = (Integer) cell.getOrDefault("hp_current", 0);
        if (hpCurrent <= 0) {
            throw new IllegalStateException("灵兽HP为0，请先恢复");
        }

        // 出战上限 2
        long deployedCount = getDeployedCellCount(fudi);
        if (deployedCount >= 2) {
            throw new IllegalStateException("出战灵兽已达上限 (2只)，请先召回其他灵兽");
        }

        cell.put("is_deployed", true);
        fudiRepository.save(fudi);

        // 同步 Beast 实体
        var beasts = beastRepository.findByFudiId(fudi.getId());
        for (Beast b : beasts) {
            if (b.getPennedCellId() != null && b.getPennedCellId().equals(cellId)) {
                b.setIsDeployed(true);
                beastRepository.save(b);
                break;
            }
        }

        String beastName = (String) cell.get("beast_name");
        log.info("用户 {} 将灵兽 {} 设为出战", userId, beastName);
        return Map.of("success", true, "message", "灵兽 [%s] 已出战".formatted(beastName));
    }

    public Map<String, Object> undeployBeast(Long userId, String position) {
        if ("all".equalsIgnoreCase(position)) {
            return undeployAllBeasts(userId);
        }

        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        Map<String, Object> cell = getCellById(fudi, cellId);
        if (cell == null || !CellType.PEN.getCode().equals(cell.get("type"))) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }

        if (!Boolean.TRUE.equals(cell.get("is_deployed"))) {
            return Map.of("success", true, "message", "灵兽未在出战状态");
        }

        cell.put("is_deployed", false);
        fudiRepository.save(fudi);

        var beasts = beastRepository.findByFudiId(fudi.getId());
        for (Beast b : beasts) {
            if (b.getPennedCellId() != null && b.getPennedCellId().equals(cellId)) {
                b.setIsDeployed(false);
                beastRepository.save(b);
                break;
            }
        }

        String beastName = (String) cell.get("beast_name");
        log.info("用户 {} 将灵兽 {} 召回", userId, beastName);
        return Map.of("success", true, "message", "灵兽 [%s] 已召回".formatted(beastName));
    }

    private Map<String, Object> undeployAllBeasts(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        int count = 0;
        if (fudi.getGridLayout() != null && fudi.getGridLayout().containsKey("cells")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");
            for (Map<String, Object> cell : cells) {
                if (CellType.PEN.getCode().equals(cell.get("type")) && Boolean.TRUE.equals(cell.get("is_deployed"))) {
                    cell.put("is_deployed", false);
                    count++;
                }
            }
        }

        var beasts = beastRepository.findByFudiId(fudi.getId());
        for (Beast b : beasts) {
            if (Boolean.TRUE.equals(b.getIsDeployed())) {
                b.setIsDeployed(false);
                beastRepository.save(b);
            }
        }

        fudiRepository.save(fudi);
        return Map.of("count", count);
    }

    public Map<String, Object> recoverBeast(Long userId, String position) {
        if ("all".equalsIgnoreCase(position)) {
            return recoverAllBeasts(userId);
        }

        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        Map<String, Object> cell = getCellById(fudi, cellId);
        if (cell == null || !CellType.PEN.getCode().equals(cell.get("type"))) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }

        if (Boolean.TRUE.equals(cell.get("is_incubating"))) {
            throw new IllegalStateException("灵兽尚在孵化中");
        }

        int hpCurrent = (Integer) cell.getOrDefault("hp_current", 0);
        int hpMax = (Integer) cell.getOrDefault("hp_max", 1);
        if (hpCurrent >= hpMax) {
            return Map.of("success", true, "message", "灵兽HP已满");
        }

        int missingHp = hpMax - hpCurrent;
        int stoneCost = (int) Math.ceil(missingHp * 0.1);
        checkSpiritStones(userId, stoneCost);
        deductSpiritStones(userId, stoneCost);

        cell.put("hp_current", hpMax);
        fudiRepository.save(fudi);

        // 同步 Beast 实体
        var beasts = beastRepository.findByFudiId(fudi.getId());
        for (Beast b : beasts) {
            if (b.getPennedCellId() != null && b.getPennedCellId().equals(cellId)) {
                b.setHpCurrent(b.getMaxHp());
                beastRepository.save(b);
                break;
            }
        }

        String beastName = (String) cell.get("beast_name");
        log.info("用户 {} 恢复灵兽 {} HP (消耗{}灵石)", userId, beastName, stoneCost);
        return Map.of("success", true, "message", "灵兽 [%s] HP已恢复（消耗%d灵石）".formatted(beastName, stoneCost), "cost", stoneCost);
    }

    private Map<String, Object> recoverAllBeasts(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        int totalCost = 0;
        int recoverCount = 0;

        if (fudi.getGridLayout() != null && fudi.getGridLayout().containsKey("cells")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");
            for (Map<String, Object> cell : cells) {
                if (!CellType.PEN.getCode().equals(cell.get("type"))) continue;
                if (Boolean.TRUE.equals(cell.get("is_incubating"))) continue;

                int hpCurrent = (Integer) cell.getOrDefault("hp_current", 0);
                int hpMax = (Integer) cell.getOrDefault("hp_max", 1);
                if (hpCurrent >= hpMax) continue;

                int missingHp = hpMax - hpCurrent;
                totalCost += (int) Math.ceil(missingHp * 0.1);
                cell.put("hp_current", hpMax);
                recoverCount++;
            }
        }

        if (recoverCount == 0) {
            return Map.of("success", true, "message", "没有需要恢复的灵兽");
        }

        checkSpiritStones(userId, totalCost);
        deductSpiritStones(userId, totalCost);

        var beasts = beastRepository.findByFudiId(fudi.getId());
        for (Beast b : beasts) {
            if (b.getHpCurrent() != null && b.getMaxHp() != null && b.getHpCurrent() < b.getMaxHp()) {
                b.setHpCurrent(b.getMaxHp());
                beastRepository.save(b);
            }
        }

        fudiRepository.save(fudi);
        return Map.of("count", recoverCount, "cost", totalCost);
    }

    public List<BeastStatusVO> getDeployedBeasts(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        List<Beast> allBeasts = beastRepository.findByFudiId(fudi.getId());
        return allBeasts.stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsDeployed()))
                .map(this::convertToBeastStatusVO)
                .toList();
    }

    private long getDeployedCellCount(Fudi fudi) {
        if (fudi.getGridLayout() == null || !fudi.getGridLayout().containsKey("cells")) return 0;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getGridLayout().get("cells");
        return cells.stream()
                .filter(c -> CellType.PEN.getCode().equals(c.get("type")) && Boolean.TRUE.equals(c.get("is_deployed")))
                .count();
    }

    private BeastStatusVO convertToBeastStatusVO(Beast beast) {
        return BeastStatusVO.builder()
                .id(beast.getId())
                .beastName(beast.getBeastName())
                .quality(beast.getQuality())
                .isMutant(Boolean.TRUE.equals(beast.getIsMutant()))
                .mutationTraits(beast.getMutationTraits())
                .tier(beast.getTier() != null ? beast.getTier() : 1)
                .level(beast.getLevel() != null ? beast.getLevel() : 1)
                .exp(beast.getExp() != null ? beast.getExp() : 0)
                .attack(beast.getAttack() != null ? beast.getAttack() : 10)
                .defense(beast.getDefense() != null ? beast.getDefense() : 8)
                .maxHp(beast.getMaxHp() != null ? beast.getMaxHp() : 100)
                .hpCurrent(beast.getHpCurrent() != null ? beast.getHpCurrent() : 100)
                .skills(beast.getSkills())
                .isDeployed(Boolean.TRUE.equals(beast.getIsDeployed()))
                .needsRecovery(beast.needsRecovery())
                .pennedCellId(beast.getPennedCellId() != null ? beast.getPennedCellId() : 0)
                .build();
    }

    // ===================== 辅助方法 =====================

    private void consumeSpiritEnergy(Fudi fudi, int baseCost) {
        spiritRepository.findByFudiId(fudi.getId()).ifPresent(spirit -> {
            spirit.restoreEnergy(fudi.getTribulationStage());
            int actualCost = spirit.calculateEnergyConsumption(baseCost);
            spirit.deductEnergy(actualCost);
            spirit.setLastEnergyUpdate(LocalDateTime.now());
            spiritRepository.save(spirit);
        });
    }

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
    private void updateBeastProduction(Map<String, Object> cell, Fudi fudi) {
        if (!cell.containsKey("last_production_time") || !cell.containsKey("mature_time")) return;

        LocalDateTime matureTime = LocalDateTime.parse((String) cell.get("mature_time"));
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(matureTime)) {
            cell.put("is_incubating", true);
            return;
        }
        cell.put("is_incubating", false);

        // 出战不产出
        if (Boolean.TRUE.equals(cell.get("is_deployed"))) return;

        LocalDateTime lastProduction = LocalDateTime.parse((String) cell.get("last_production_time"));
        double intervalHours = (Double) cell.getOrDefault("production_interval_hours", 4.0);
        long intervalSeconds = (long) (intervalHours * 3600);
        if (intervalSeconds <= 0) intervalSeconds = 14400;

        long elapsed = java.time.Duration.between(lastProduction, now).getSeconds();
        int cycles = (int) (elapsed / intervalSeconds);
        if (cycles <= 0) return;

        int tier = (Integer) cell.getOrDefault("tier", 1);
        BeastQuality quality = getBeastQuality(cell);
        int perCycle = (int) Math.round(tier * quality.getOutputMultiplier() * (1 + new Random().nextInt(tier + 1)) / 2.0);
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
                .birthTime(cell.containsKey("birth_time") ? LocalDateTime.parse((String) cell.get("birth_time")) : null)
                .build();
    }

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
                .filter(c -> cellId.equals(c.get("cell_id")))
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
                Map<String, Object> emptyCell = new HashMap<>();
                emptyCell.put("cell_id", cellId);
                emptyCell.put("type", "empty");
                cells.set(i, emptyCell);
                return;
            }
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
        cell.remove("birth_time");
        cell.remove("power_score");
        cell.remove("evolution_count");
        cell.remove("hp_current");
        cell.remove("hp_max");
        cell.remove("is_deployed");
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

    static int calculateBeastAttack(int level, BeastQuality quality) {
        double q = getCombatStatMultiplier(quality);
        return (int) Math.round((10 + (level - 1) * 3 * q) * q);
    }

    static int calculateBeastDefense(int level, BeastQuality quality) {
        double q = getCombatStatMultiplier(quality);
        return (int) Math.round((8 + (level - 1) * 2 * q) * q);
    }

    private static double getCombatStatMultiplier(BeastQuality quality) {
        return switch (quality) {
            case MORTAL -> 0.8;
            case SPIRIT -> 1.0;
            case IMMORTAL -> 1.3;
            case SAINT -> 1.6;
            case DIVINE -> 2.0;
        };
    }
}
