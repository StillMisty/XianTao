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
import top.stillmisty.xiantao.domain.land.entity.FudiCell;
import top.stillmisty.xiantao.domain.land.entity.Spirit;
import top.stillmisty.xiantao.domain.land.entity.SpiritForm;
import top.stillmisty.xiantao.domain.land.enums.*;
import top.stillmisty.xiantao.domain.land.repository.FudiCellRepository;
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
    private final FudiCellRepository fudiCellRepository;
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
        fudi.setLastOnlineTime(LocalDateTime.now());

        fudi = fudiRepository.save(fudi);

        Spirit spirit = createSpiritForFudi(fudi, mbtiType);
        spiritRepository.save(spirit);

        // 创建初始空地块
        autoExpandCells(fudi);
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

        List<Map<String, Object>> cells = (List<Map<String, Object>>) fudi.getCellLayout().get("cells");
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
        List<FudiCell> cells = fudiCellRepository.findByFudiId(fudi.getId());
        List<CellDetailVO> details = new ArrayList<>();

        for (FudiCell cell : cells) {
            CellDetailVO.CellDetailVOBuilder builder = CellDetailVO.builder()
                    .cellId(cell.getCellId())
                    .type(cell.getCellType())
                    .cellLevel(cell.getCellLevel());

            switch (cell.getCellType()) {
                case FARM -> buildFarmCellDetail(builder, cell);
                case PEN -> buildPenCellDetail(builder, cell);
                default -> {
                }
            }

            details.add(builder.build());
        }

        return details;
    }

    private void buildFarmCellDetail(CellDetailVO.CellDetailVOBuilder builder, FudiCell cell) {
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

    private void buildPenCellDetail(CellDetailVO.CellDetailVOBuilder builder, FudiCell cell) {
        String beastName = cell.getStringConfig("beast_name");
        if (beastName == null) beastName = "空兽栏";
        Integer tier = cell.getIntConfig("tier");

        if (beastName.equals("空兽栏")) {
            builder.name("空兽栏");
        } else {
            BeastQuality quality = getBeastQuality(cell);
            List<String> traits = (List<String>) cell.getConfigValue("mutation_traits", List.of());
            builder.name(beastName)
                    .level(tier)
                    .quality(quality.getChineseName())
                    .mutationTraits(traits)
                    .productionStored(cell.getIntConfig("production_stored") != null ? cell.getIntConfig("production_stored") : 0)
                    .isIncubating(Boolean.TRUE.equals(cell.getBoolConfig("is_incubating")));
        }
    }

    // ===================== 统一收取系统（种植收获 + 灵兽产出） =====================

    @ConsumeSpiritEnergy(5)
    public Map<String, Object> collect(Long userId, String position) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        FudiCell cell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId)
                .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

        if (cell.isEmpty()) {
            throw new IllegalStateException("地块 " + cellId + " 为空");
        }

        if (cell.getCellType() == CellType.FARM) {
            return harvestCrop(fudi, cell, cellId);
        } else if (cell.getCellType() == CellType.PEN) {
            return collectBeastProduce(fudi, cell, cellId);
        } else {
            throw new IllegalStateException("地块 " + cellId + " 无可收取内容");
        }
    }

    public Map<String, Object> collectAll(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        List<FudiCell> cells = fudiCellRepository.findByFudiId(fudi.getId());
        if (cells.isEmpty()) {
            return Map.of("harvested", 0, "collected", 0, "totalItems", 0);
        }

        int harvestCount = 0;
        int collectedCount = 0;
        int totalItems = 0;

        // 收取灵田
        List<Long> toRemove = new ArrayList<>();
        for (FudiCell cell : cells) {
            if (cell.getCellType() != CellType.FARM) continue;

            updateGrowthProgress(cell);
            Double progress = cell.getDoubleConfig("growth_progress");
            if (progress == null || progress < 1.0) continue;

            boolean isPerennial = Boolean.TRUE.equals(cell.getBoolConfig("is_perennial"));
            if (!isPerennial && progress > 1.0 && isWilted(cell)) {
                toRemove.add(cell.getId());
                continue;
            }

            Integer cropId = cell.getIntConfig("crop_id");
            int yield = calculateYield(cropId, fudi.getTribulationStage());
            Integer harvestCountVal = cell.getIntConfig("harvest_count");
            int hCount = (harvestCountVal != null ? harvestCountVal : 0) + 1;
            Integer maxHarvest = cell.getIntConfig("max_harvest");
            if (maxHarvest == null) maxHarvest = 1;

            if (isPerennial && hCount < maxHarvest) {
                cell.setConfigValue("harvest_count", hCount);
                cell.setConfigValue("growth_progress", 0.0);
                cell.setConfigValue("plant_time", LocalDateTime.now().toString());
                Double baseGrowthHours = cell.getDoubleConfig("base_growth_hours");
                Double levelSpeed = cell.getDoubleConfig("level_speed_multiplier");
                if (levelSpeed == null) levelSpeed = 1.0;
                cell.setConfigValue("mature_time", LocalDateTime.now().plusHours((long) (baseGrowthHours / levelSpeed)).toString());
                fudiCellRepository.save(cell);
            } else {
                toRemove.add(cell.getId());
            }

            totalItems += yield;
            harvestCount++;
        }

        for (Long id : toRemove) {
            fudiCellRepository.deleteById(id);
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
        for (FudiCell cell : cells) {
            if (cell.getCellType() != CellType.PEN) continue;
            if (Boolean.TRUE.equals(cell.getBoolConfig("is_incubating"))) continue;

            updateBeastProduction(cell, fudi);
            Integer stored = cell.getIntConfig("production_stored");
            if (stored == null) stored = 0;
            if (stored > 0) {
                totalItems += stored;
                collectedCount++;
                cell.setConfigValue("production_stored", 0);
                fudiCellRepository.save(cell);
            }
        }

        return Map.of("harvested", harvestCount, "collected", collectedCount, "totalItems", totalItems);
    }

    private Map<String, Object> harvestCrop(Fudi fudi, FudiCell cell, Integer cellId) {
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

        return Map.of("cellId", cellId, "cropName", cropName, "yield", yield, "type", "farm");
    }

    private Map<String, Object> collectBeastProduce(Fudi fudi, FudiCell cell, Integer cellId) {
        if (Boolean.TRUE.equals(cell.getBoolConfig("is_incubating"))) {
            throw new IllegalStateException("灵兽尚在孵化中");
        }

        updateBeastProduction(cell, fudi);
        Integer stored = cell.getIntConfig("production_stored");
        if (stored == null) stored = 0;
        if (stored <= 0) {
            throw new IllegalStateException("暂无产出可收取");
        }

        String beastName = cell.getStringConfig("beast_name");
        if (beastName == null) beastName = "灵兽";
        cell.setConfigValue("production_stored", 0);
        fudiCellRepository.save(cell);

        log.info("用户 {} 收取地块 {} 的灵兽产出 {} 件", fudi.getUserId(), cellId, stored);

        return Map.of("cellId", cellId, "beastName", beastName, "totalItems", stored, "type", "pen");
    }

    // ===================== 种植系统 =====================

    public FarmCellVO plantCrop(Long userId, Integer cellId, Integer cropId, String cropName, int cropTier) {
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

        // 创建或更新地块
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

    public FarmCellVO plantCropByName(Long userId, String position, String cropName) {
        Integer cellId = parseCellId(position);
        ItemTemplate seedTemplate = findSeedTemplateByName(cropName);

        var stackableItem = stackableItemRepository.findByUserIdAndTemplateId(userId, seedTemplate.getId())
                .orElseThrow(() -> new IllegalStateException("背包中没有 [%s]".formatted(cropName)));

        itemService.reduceStackableItem(userId, seedTemplate.getId(), 1);

        int growTime = seedTemplate.getGrowTime() != null ? seedTemplate.getGrowTime() : 24;
        int cropTier = getCropTier(growTime);
        Integer cropId = seedTemplate.getId().intValue();

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

        FudiCell existingCell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId).orElse(null);
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

        return Map.of("cellId", cellId, "type", type.getChineseName());
    }

    public Map<String, Object> removeCell(Long userId, String position) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 3);

        FudiCell cell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId)
                .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

        if (cell.isEmpty()) {
            throw new IllegalStateException("地块 " + cellId + " 为空");
        }

        // 拆除兽栏：灵兽退回栏外休憩（penned_cell_id = null）
        if (cell.getCellType() == CellType.PEN) {
            Integer beastId = cell.getIntConfig("beast_id");
            if (beastId != null && beastId > 0) {
                var beasts = beastRepository.findByFudiId(fudi.getId());
                for (Beast b : beasts) {
                    if (b.getPennedCellId() != null && b.getPennedCellId().equals(cellId)) {
                        b.setPennedCellId(null);
                        beastRepository.save(b);
                        break;
                    }
                }
            }
        }

        fudiCellRepository.deleteById(cell.getId());

        log.info("用户 {} 拆除地块 {} 的 {}", userId, cellId, cell.getCellType().getChineseName());

        return Map.of("cellId", cellId, "type", cell.getCellType().getChineseName());
    }

    // ===================== 地块升级系统 =====================

    public Map<String, Object> upgradeCell(Long userId, String position) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        FudiCell cell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId)
                .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

        if (cell.isEmpty()) {
            throw new IllegalStateException("地块 " + cellId + " 为空");
        }

        int currentLevel = cell.getCellLevel();
        if (currentLevel >= 5) {
            throw new IllegalStateException("已是最高等级 Lv5");
        }

        int cost = currentLevel == 1 ? 200 : currentLevel == 2 ? 400 : currentLevel == 3 ? 800 : 1600;
        checkSpiritStones(userId, cost);
        deductSpiritStones(userId, cost);

        int newLevel = currentLevel + 1;
        cell.setCellLevel(newLevel);
        fudiCellRepository.save(cell);

        log.info("用户 {} 升级地块 {} 的 {} Lv{} -> Lv{}", userId, cellId, cell.getCellType().getChineseName(), currentLevel, newLevel);

        return Map.of("cellId", cellId, "type", cell.getCellType().getChineseName(), "oldLevel", currentLevel, "newLevel", newLevel);
    }

    // ===================== 灵兽系统 — 孵化 =====================

    public PenCellVO hatchBeast(Long userId, String position, String eggName) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 10);

        FudiCell cell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId)
                .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

        if (cell.getCellType() != CellType.PEN) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }

        Integer beastId = cell.getIntConfig("beast_id");
        if (beastId != null && beastId > 0) {
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

        int cellLevel = cell.getCellLevel();
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

        // 更新地块配置
        cell.setConfigValue("beast_id", eggTemplate.getId().intValue());
        cell.setConfigValue("beast_name", beastName);
        cell.setConfigValue("tier", tier);
        cell.setConfigValue("quality", quality.getCode());
        cell.setConfigValue("quality_ordinal", quality.getOrder());
        cell.setConfigValue("is_mutant", isMutant);
        cell.setConfigValue("mutation_traits", mutationTraits);
        cell.setConfigValue("is_incubating", true);
        cell.setConfigValue("hatch_time", now.toString());
        cell.setConfigValue("mature_time", now.plusHours((long) hatchHours).toString());
        cell.setConfigValue("production_stored", 0);
        cell.setConfigValue("last_production_time", now.toString());
        cell.setConfigValue("production_interval_hours", productionInterval);
        cell.setConfigValue("birth_time", now.toString());
        cell.setConfigValue("power_score", tier * 10);
        cell.setConfigValue("evolution_count", 0);
        cell.setConfigValue("hp_current", tier * 200);
        cell.setConfigValue("hp_max", tier * 200);
        cell.setConfigValue("is_deployed", false);
        fudiCellRepository.save(cell);

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

        FudiCell cell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId)
                .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

        if (cell.getCellType() != CellType.PEN) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }
        Integer beastId = cell.getIntConfig("beast_id");
        if (beastId != null && beastId > 0) {
            throw new IllegalStateException("该兽栏已有灵兽，请先放生");
        }

        int tier = getCropTier(eggTemplate.getGrowTime() != null ? eggTemplate.getGrowTime() : 72);
        int cellLevel = cell.getCellLevel();
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

        cell.setConfigValue("beast_id", eggTemplate.getId().intValue());
        cell.setConfigValue("beast_name", beastName);
        cell.setConfigValue("tier", tier);
        cell.setConfigValue("quality", quality.getCode());
        cell.setConfigValue("quality_ordinal", quality.getOrder());
        cell.setConfigValue("is_mutant", isMutant);
        cell.setConfigValue("mutation_traits", mutationTraits);
        cell.setConfigValue("is_incubating", true);
        cell.setConfigValue("hatch_time", now.toString());
        cell.setConfigValue("mature_time", now.plusHours((long) hatchHours).toString());
        cell.setConfigValue("production_stored", 0);
        cell.setConfigValue("last_production_time", now.toString());
        cell.setConfigValue("production_interval_hours", productionInterval);
        cell.setConfigValue("birth_time", now.toString());
        cell.setConfigValue("power_score", tier * 10);
        cell.setConfigValue("evolution_count", 0);
        cell.setConfigValue("hp_current", tier * 200);
        cell.setConfigValue("hp_max", tier * 200);
        cell.setConfigValue("is_deployed", false);
        fudiCellRepository.save(cell);

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

        FudiCell cell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId)
                .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

        if (cell.getCellType() != CellType.PEN) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }

        String beastName = cell.getStringConfig("beast_name");
        Integer tier = cell.getIntConfig("tier");
        if (tier == null) tier = 1;
        BeastQuality quality = getBeastQuality(cell);

        clearBeastCell(cell);

        var beasts = beastRepository.findByFudiId(fudi.getId());
        for (Beast b : beasts) {
            if (b.getPennedCellId() != null && b.getPennedCellId().equals(cellId)) {
                beastRepository.deleteById(b.getId());
                break;
            }
        }

        log.info("用户 {} 放生 {} (T{}/{})", userId, beastName, tier, quality.getChineseName());

        return Map.of("beastName", beastName, "tier", tier, "quality", quality.getChineseName());
    }

    public PenCellVO evolveBeast(Long userId, String position, String mode) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 8);

        FudiCell cell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId)
                .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

        if (cell.getCellType() != CellType.PEN) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }
        if (Boolean.TRUE.equals(cell.getBoolConfig("is_incubating"))) {
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

    private PenCellVO evolveBeastTier(Fudi fudi, FudiCell cell, Long userId, Integer cellId) {
        Integer currentTier = cell.getIntConfig("tier");
        if (currentTier == null) currentTier = 1;
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
            throw new IllegalStateException("进化失败！进化石和灵石已消耗");
        }

        int newTier = currentTier + 1;
        cell.setConfigValue("tier", newTier);
        cell.setConfigValue("power_score", newTier * 10);
        cell.setConfigValue("birth_time", LocalDateTime.now().toString());
        Integer evoCount = cell.getIntConfig("evolution_count");
        cell.setConfigValue("evolution_count", (evoCount != null ? evoCount : 0) + 1);
        cell.setConfigValue("hp_max", newTier * 200);
        cell.setConfigValue("hp_current", newTier * 200);

        boolean qualityUpgraded = false;
        BeastQuality currentQuality = getBeastQuality(cell);
        if (new Random().nextInt(100) < 10 && currentQuality != BeastQuality.DIVINE) {
            BeastQuality newQuality = currentQuality.next();
            cell.setConfigValue("quality", newQuality.getCode());
            cell.setConfigValue("quality_ordinal", newQuality.getOrder());
            qualityUpgraded = true;
        }

        boolean mutationTriggered = rollMutation(15);
        if (mutationTriggered) {
            addMutationTrait(cell);
        }

        fudiCellRepository.save(cell);
        log.info("用户 {} 进化地块 {} 的灵兽 T{}->T{}{}", userId, cellId, currentTier, newTier,
                qualityUpgraded ? " (品质连带提升!)" : "");

        return buildPenCellVO(cell);
    }

    private PenCellVO breakthroughBeastQuality(Fudi fudi, FudiCell cell, Long userId, Integer cellId) {
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
            throw new IllegalStateException("品质突破失败！进化石和灵石已消耗");
        }

        cell.setConfigValue("quality", nextQuality.getCode());
        cell.setConfigValue("quality_ordinal", nextQuality.getOrder());

        boolean mutationTriggered = rollMutation(10);
        if (mutationTriggered) {
            addMutationTrait(cell);
        }

        fudiCellRepository.save(cell);
        log.info("用户 {} 品质突破地块 {} 的灵兽 {} -> {}", userId, cellId, currentQuality.getChineseName(), nextQuality.getChineseName());

        return buildPenCellVO(cell);
    }

    // ===================== 灵兽系统 — 出战/召回/恢复 =====================

    public Map<String, Object> deployBeast(Long userId, String position) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        FudiCell cell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId)
                .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

        if (cell.getCellType() != CellType.PEN) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }
        if (Boolean.TRUE.equals(cell.getBoolConfig("is_incubating"))) {
            throw new IllegalStateException("灵兽尚在孵化中");
        }

        if (Boolean.TRUE.equals(cell.getBoolConfig("is_deployed"))) {
            return Map.of("success", true, "message", "灵兽已处于出战状态");
        }

        Integer hpCurrent = cell.getIntConfig("hp_current");
        if (hpCurrent == null) hpCurrent = 0;
        if (hpCurrent <= 0) {
            throw new IllegalStateException("灵兽HP为0，请先恢复");
        }

        // 出战上限 2
        long deployedCount = getDeployedCellCount(fudi);
        if (deployedCount >= 2) {
            throw new IllegalStateException("出战灵兽已达上限 (2只)，请先召回其他灵兽");
        }

        cell.setConfigValue("is_deployed", true);
        fudiCellRepository.save(cell);

        // 同步 Beast 实体
        var beasts = beastRepository.findByFudiId(fudi.getId());
        for (Beast b : beasts) {
            if (b.getPennedCellId() != null && b.getPennedCellId().equals(cellId)) {
                b.setIsDeployed(true);
                beastRepository.save(b);
                break;
            }
        }

        String beastName = cell.getStringConfig("beast_name");
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

        FudiCell cell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId)
                .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

        if (cell.getCellType() != CellType.PEN) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }

        if (!Boolean.TRUE.equals(cell.getBoolConfig("is_deployed"))) {
            return Map.of("success", true, "message", "灵兽未在出战状态");
        }

        cell.setConfigValue("is_deployed", false);
        fudiCellRepository.save(cell);

        var beasts = beastRepository.findByFudiId(fudi.getId());
        for (Beast b : beasts) {
            if (b.getPennedCellId() != null && b.getPennedCellId().equals(cellId)) {
                b.setIsDeployed(false);
                beastRepository.save(b);
                break;
            }
        }

        String beastName = cell.getStringConfig("beast_name");
        log.info("用户 {} 将灵兽 {} 召回", userId, beastName);
        return Map.of("success", true, "message", "灵兽 [%s] 已召回".formatted(beastName));
    }

    private Map<String, Object> undeployAllBeasts(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        List<FudiCell> penCells = fudiCellRepository.findByFudiIdAndCellType(fudi.getId(), CellType.PEN);
        int count = 0;
        for (FudiCell cell : penCells) {
            if (Boolean.TRUE.equals(cell.getBoolConfig("is_deployed"))) {
                cell.setConfigValue("is_deployed", false);
                fudiCellRepository.save(cell);
                count++;
            }
        }

        var beasts = beastRepository.findByFudiId(fudi.getId());
        for (Beast b : beasts) {
            if (Boolean.TRUE.equals(b.getIsDeployed())) {
                b.setIsDeployed(false);
                beastRepository.save(b);
            }
        }

        return Map.of("count", count);
    }

    public Map<String, Object> recoverBeast(Long userId, String position) {
        if ("all".equalsIgnoreCase(position)) {
            return recoverAllBeasts(userId);
        }

        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        FudiCell cell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId)
                .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

        if (cell.getCellType() != CellType.PEN) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }

        if (Boolean.TRUE.equals(cell.getBoolConfig("is_incubating"))) {
            throw new IllegalStateException("灵兽尚在孵化中");
        }

        Integer hpCurrent = cell.getIntConfig("hp_current");
        if (hpCurrent == null) hpCurrent = 0;
        Integer hpMax = cell.getIntConfig("hp_max");
        if (hpMax == null) hpMax = 1;
        if (hpCurrent >= hpMax) {
            return Map.of("success", true, "message", "灵兽HP已满");
        }

        int missingHp = hpMax - hpCurrent;
        int stoneCost = (int) Math.ceil(missingHp * 0.1);
        checkSpiritStones(userId, stoneCost);
        deductSpiritStones(userId, stoneCost);

        cell.setConfigValue("hp_current", hpMax);
        fudiCellRepository.save(cell);

        // 同步 Beast 实体
        var beasts = beastRepository.findByFudiId(fudi.getId());
        for (Beast b : beasts) {
            if (b.getPennedCellId() != null && b.getPennedCellId().equals(cellId)) {
                b.setHpCurrent(b.getMaxHp());
                beastRepository.save(b);
                break;
            }
        }

        String beastName = cell.getStringConfig("beast_name");
        log.info("用户 {} 恢复灵兽 {} HP (消耗{}灵石)", userId, beastName, stoneCost);
        return Map.of("success", true, "message", "灵兽 [%s] HP已恢复（消耗%d灵石）".formatted(beastName, stoneCost), "cost", stoneCost);
    }

    private Map<String, Object> recoverAllBeasts(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        int totalCost = 0;
        int recoverCount = 0;

        List<FudiCell> penCells = fudiCellRepository.findByFudiIdAndCellType(fudi.getId(), CellType.PEN);
        for (FudiCell cell : penCells) {
            if (Boolean.TRUE.equals(cell.getBoolConfig("is_incubating"))) continue;

            Integer hpCurrent = cell.getIntConfig("hp_current");
            if (hpCurrent == null) hpCurrent = 0;
            Integer hpMax = cell.getIntConfig("hp_max");
            if (hpMax == null) hpMax = 1;
            if (hpCurrent >= hpMax) continue;

            int missingHp = hpMax - hpCurrent;
            totalCost += (int) Math.ceil(missingHp * 0.1);
            cell.setConfigValue("hp_current", hpMax);
            fudiCellRepository.save(cell);
            recoverCount++;
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
        List<FudiCell> penCells = fudiCellRepository.findByFudiIdAndCellType(fudi.getId(), CellType.PEN);
        return penCells.stream()
                .filter(c -> Boolean.TRUE.equals(c.getBoolConfig("is_deployed")))
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

    private boolean isWilted(FudiCell cell) {
        if (Boolean.TRUE.equals(cell.getBoolConfig("is_perennial"))) return false;
        String matureTimeStr = cell.getStringConfig("mature_time");
        Double baseGrowthHours = cell.getDoubleConfig("base_growth_hours");
        if (matureTimeStr == null || baseGrowthHours == null) return false;
        LocalDateTime matureTime = LocalDateTime.parse(matureTimeStr);
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

    private void addMutationTrait(FudiCell cell) {
        @SuppressWarnings("unchecked")
        List<String> traits = (List<String>) cell.getConfigValue("mutation_traits", new ArrayList<>());
        if (traits.size() >= 2) return;
        String newTrait = rollRandomTrait();
        if (!traits.contains(newTrait)) {
            traits.add(newTrait);
            cell.setConfigValue("mutation_traits", traits);
            cell.setConfigValue("is_mutant", true);
        }
    }

    private BeastQuality getBeastQuality(FudiCell cell) {
        try {
            String qualityCode = cell.getStringConfig("quality");
            if (qualityCode == null) qualityCode = "mortal";
            return BeastQuality.fromCode(qualityCode);
        } catch (IllegalArgumentException e) {
            return BeastQuality.MORTAL;
        }
    }

    private void updateBeastProduction(FudiCell cell, Fudi fudi) {
        String lastProductionTimeStr = cell.getStringConfig("last_production_time");
        String matureTimeStr = cell.getStringConfig("mature_time");
        if (lastProductionTimeStr == null || matureTimeStr == null) return;

        LocalDateTime matureTime = LocalDateTime.parse(matureTimeStr);
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(matureTime)) {
            cell.setConfigValue("is_incubating", true);
            return;
        }
        cell.setConfigValue("is_incubating", false);

        // 出战不产出
        if (Boolean.TRUE.equals(cell.getBoolConfig("is_deployed"))) return;

        LocalDateTime lastProduction = LocalDateTime.parse(lastProductionTimeStr);
        Double intervalHours = cell.getDoubleConfig("production_interval_hours");
        if (intervalHours == null) intervalHours = 4.0;
        long intervalSeconds = (long) (intervalHours * 3600);
        if (intervalSeconds <= 0) intervalSeconds = 14400;

        long elapsed = java.time.Duration.between(lastProduction, now).getSeconds();
        int cycles = (int) (elapsed / intervalSeconds);
        if (cycles <= 0) return;

        Integer tier = cell.getIntConfig("tier");
        if (tier == null) tier = 1;
        BeastQuality quality = getBeastQuality(cell);
        int perCycle = (int) Math.round(tier * quality.getOutputMultiplier() * (1 + new Random().nextInt(tier + 1)) / 2.0);
        int produced = Math.max(1, perCycle * cycles);

        Integer currentStored = cell.getIntConfig("production_stored");
        if (currentStored == null) currentStored = 0;
        int maxStorage = tier * 20;
        int newStored = Math.min(maxStorage, currentStored + produced);

        cell.setConfigValue("production_stored", newStored);
        cell.setConfigValue("last_production_time", now.toString());
        fudiCellRepository.save(cell);
    }

    private PenCellVO buildPenCellVO(FudiCell cell) {
        @SuppressWarnings("unchecked")
        List<String> traits = (List<String>) cell.getConfigValue("mutation_traits", List.of());
        BeastQuality quality = getBeastQuality(cell);

        String hatchTimeStr = cell.getStringConfig("hatch_time");
        String matureTimeStr = cell.getStringConfig("mature_time");
        String birthTimeStr = cell.getStringConfig("birth_time");

        return PenCellVO.builder()
                .cellId(cell.getCellId())
                .cellLevel(cell.getCellLevel())
                .beastId(cell.getIntConfig("beast_id") != null ? cell.getIntConfig("beast_id") : 0)
                .beastName(cell.getStringConfig("beast_name") != null ? cell.getStringConfig("beast_name") : "未知灵兽")
                .tier(cell.getIntConfig("tier") != null ? cell.getIntConfig("tier") : 1)
                .quality(quality.getChineseName())
                .qualityOrdinal(quality.getOrder())
                .isMutant(Boolean.TRUE.equals(cell.getBoolConfig("is_mutant")))
                .mutationTraits(traits)
                .isIncubating(Boolean.TRUE.equals(cell.getBoolConfig("is_incubating")))
                .hatchTime(hatchTimeStr != null ? LocalDateTime.parse(hatchTimeStr) : null)
                .matureTime(matureTimeStr != null ? LocalDateTime.parse(matureTimeStr) : null)
                .productionIntervalHours(cell.getDoubleConfig("production_interval_hours") != null ? cell.getDoubleConfig("production_interval_hours") : 4.0)
                .productionStored(cell.getIntConfig("production_stored") != null ? cell.getIntConfig("production_stored") : 0)
                .powerScore(cell.getIntConfig("power_score") != null ? cell.getIntConfig("power_score") : 10)
                .birthTime(birthTimeStr != null ? LocalDateTime.parse(birthTimeStr) : null)
                .build();
    }

    public Map<String, Object> getCellStatus(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        int totalCells = getTotalCellCount(fudi);
        List<Map<String, Object>> occupiedCells = new ArrayList<>();
        List<Integer> emptyCellIds = new ArrayList<>();

        List<FudiCell> cells = fudiCellRepository.findByFudiId(fudi.getId());
        for (FudiCell cell : cells) {
            if (cell.isEmpty()) {
                emptyCellIds.add(cell.getCellId());
                continue;
            }

            Map<String, Object> cellInfo = new HashMap<>();
            cellInfo.put("cellId", cell.getCellId());
            cellInfo.put("type", cell.getCellType().getCode());
            cellInfo.put("cellLevel", cell.getCellLevel());

            switch (cell.getCellType()) {
                case FARM -> {
                    cellInfo.put("cropName", cell.getStringConfig("crop_name"));
                    updateGrowthProgress(cell);
                    Double progress = cell.getDoubleConfig("growth_progress");
                    cellInfo.put("growthProgress", progress);
                    cellInfo.put("isMature", progress != null && progress >= 1.0);
                }
                case PEN -> {
                    cellInfo.put("beastName", cell.getStringConfig("beast_name") != null ? cell.getStringConfig("beast_name") : "空兽栏");
                    cellInfo.put("tier", cell.getIntConfig("tier"));
                    cellInfo.put("quality", cell.getStringConfig("quality"));
                    Integer stored = cell.getIntConfig("production_stored");
                    cellInfo.put("productionStored", stored != null ? stored : 0);
                    cellInfo.put("isIncubating", cell.getBoolConfig("is_incubating"));
                }
            }

            occupiedCells.add(cellInfo);
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

    private void clearBeastCell(FudiCell cell) {
        cell.setConfigValue("beast_id", null);
        cell.setConfigValue("beast_name", null);
        cell.setConfigValue("tier", null);
        cell.setConfigValue("quality", null);
        cell.setConfigValue("quality_ordinal", null);
        cell.setConfigValue("is_mutant", null);
        cell.setConfigValue("mutation_traits", null);
        cell.setConfigValue("is_incubating", null);
        cell.setConfigValue("hatch_time", null);
        cell.setConfigValue("mature_time", null);
        cell.setConfigValue("production_stored", null);
        cell.setConfigValue("last_production_time", null);
        cell.setConfigValue("production_interval_hours", null);
        cell.setConfigValue("birth_time", null);
        cell.setConfigValue("power_score", null);
        cell.setConfigValue("evolution_count", null);
        cell.setConfigValue("hp_current", null);
        cell.setConfigValue("hp_max", null);
        cell.setConfigValue("is_deployed", null);
        fudiCellRepository.save(cell);
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

    private void updateGrowthProgress(FudiCell cell) {
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
