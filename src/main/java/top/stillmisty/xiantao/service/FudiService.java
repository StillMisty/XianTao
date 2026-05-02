package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.domain.fudi.entity.SpiritForm;
import top.stillmisty.xiantao.domain.fudi.enums.*;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.fudi.repository.FudiRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.fudi.vo.CellDetailVO;
import top.stillmisty.xiantao.domain.fudi.vo.FudiStatusVO;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.infrastructure.mapper.SpiritFormMapper;
import top.stillmisty.xiantao.service.annotation.ConsumeSpiritEnergy;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class FudiService {

    private final FudiRepository fudiRepository;
    private final FudiCellRepository fudiCellRepository;
    private final StackableItemService stackableItemService;
    private final AuthenticationService authService;
    private final ItemTemplateRepository itemTemplateRepository;
    private final StackableItemRepository stackableItemRepository;
    private final UserRepository userRepository;
    private final SpiritRepository spiritRepository;
    private final SpiritFormMapper spiritFormMapper;
    private final BeastRepository beastRepository;
    private final BeastService beastService;
    private final FarmService farmService;

    // ===================== 公开 API（含认证） =====================

    public ServiceResult<FudiStatusVO> getFudiStatus(PlatformType platform, String openId) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return ServiceResult.authFailure(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(getFudiStatus(auth.userId()));
        } catch (IllegalStateException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> collect(PlatformType platform, String openId, String position) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return ServiceResult.authFailure(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(collect(auth.userId(), position));
        } catch (IllegalStateException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> collectAll(PlatformType platform, String openId) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return ServiceResult.authFailure(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(collectAll(auth.userId()));
        } catch (IllegalStateException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> buildCell(PlatformType platform, String openId, String position, CellType type) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return ServiceResult.authFailure(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(buildCell(auth.userId(), position, type));
        } catch (IllegalStateException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> removeCell(PlatformType platform, String openId, String position) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return ServiceResult.authFailure(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(removeCell(auth.userId(), position));
        } catch (IllegalStateException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> upgradeCell(PlatformType platform, String openId, String position) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return ServiceResult.authFailure(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(upgradeCell(auth.userId(), position));
        } catch (IllegalStateException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> giveGift(PlatformType platform, String openId, String itemName) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return ServiceResult.authFailure(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(giveGift(auth.userId(), itemName));
        } catch (IllegalStateException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    public ServiceResult<Map<String, Object>> triggerTribulation(PlatformType platform, String openId) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return ServiceResult.authFailure(auth.errorMessage());
        try {
            return new ServiceResult.Success<>(triggerTribulation(auth.userId()));
        } catch (IllegalStateException e) {
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
            SpiritForm randomForm = allForms.get(ThreadLocalRandom.current().nextInt(allForms.size()));
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

    void consumeSpiritEnergy(Fudi fudi, int baseCost) {
        spiritRepository.findByFudiId(fudi.getId()).ifPresent(spirit -> {
            spirit.restoreEnergy(fudi.getTribulationStage());
            int actualCost = spirit.calculateEnergyConsumption(baseCost);
            spirit.deductEnergy(actualCost);
            spirit.setLastEnergyUpdate(LocalDateTime.now());
            spiritRepository.save(spirit);
        });
    }

    // ===================== 天劫系统 =====================

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
        return (int) fudiCellRepository.findByFudiId(fudi.getId()).stream()
                .filter(cell -> cell.getCellType() != CellType.EMPTY)
                .count();
    }

    private int calculateTribulationDefense(Fudi fudi, int playerStr) {
        int defense = playerStr * 10 + (fudi.getTribulationStage() != null ? fudi.getTribulationStage() : 0) * 50;

        List<Beast> beasts = beastRepository.findByFudiId(fudi.getId());
        for (Beast beast : beasts) {
            if (!Boolean.TRUE.equals(beast.getIsDeployed())) continue;
            if (beast.getHpCurrent() == null || beast.getHpCurrent() <= 0) continue;

            int tier = beast.getTier() != null ? beast.getTier() : 1;
            double power = tier * 10.0;
            List<String> traits = beast.getMutationTraits();
            if (traits != null && traits.contains(MutationTrait.GUARDIAN.getCode())) {
                power *= 1.5;
            }
            defense += (int) power;
        }

        return defense;
    }

    @Transactional
    public Map<String, Object> triggerTribulation(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        User user = userRepository.findById(userId).orElseThrow();

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

        if (!forceTrigger && java.time.Duration.between(referenceTime, LocalDateTime.now()).toDays() < 7) {
            return null;
        }

        int attack = playerLevel * 80 + fudi.getTribulationStage() * 200;
        int defense = calculateTribulationDefense(fudi, playerStr);

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

    private String applyTribulationCompassion(Fudi fudi, int attack, int defense) {
        int oldStage = fudi.getTribulationStage();
        int newWinStreak = fudi.getTribulationWinStreak() + 1;
        int newStage = oldStage + 1;

        fudi.setTribulationWinStreak(newWinStreak);
        fudi.setTribulationStage(newStage);

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

    private String applyTribulationLoss(Fudi fudi, int attack, int defense) {
        int oldWinStreak = fudi.getTribulationWinStreak();

        Spirit spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
        int oldAffection = spirit != null ? spirit.getAffection() : 0;

        int diff = attack - defense;
        int occupiedCount = getOccupiedCellCount(fudi);
        double ratio = Math.min(1.0, (double) diff / attack);
        int clearCount = Math.min(occupiedCount, Math.max(1, (int) Math.ceil(ratio * occupiedCount)));

        List<FudiCell> occupiedCells = fudiCellRepository.findByFudiId(fudi.getId()).stream()
                .filter(cell -> cell.getCellType() != CellType.EMPTY)
                .toList();
        List<FudiCell> cellsToDestroy = new ArrayList<>(occupiedCells);
        Collections.shuffle(cellsToDestroy);
        cellsToDestroy = cellsToDestroy.subList(0, Math.min(clearCount, cellsToDestroy.size()));

        for (FudiCell cell : cellsToDestroy) {
            cell.setCellType(CellType.EMPTY);
            cell.setConfig(new HashMap<>());
            fudiCellRepository.save(cell);
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
                case FARM -> farmService.buildFarmCellDetail(builder, cell);
                case PEN -> beastService.buildPenCellDetail(builder, cell);
                default -> {
                }
            }

            details.add(builder.build());
        }

        return details;
    }

    // ===================== 统一收取系统（种植收获 + 灵兽产出） =====================

    @ConsumeSpiritEnergy(5)
    @Transactional
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
            return farmService.harvestCrop(fudi, cell, cellId);
        } else if (cell.getCellType() == CellType.PEN) {
            return beastService.collectBeastProduce(fudi, cell, cellId);
        } else {
            throw new IllegalStateException("地块 " + cellId + " 无可收取内容");
        }
    }

    // ===================== 种植系统 =====================

    @Transactional
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

            farmService.updateGrowthProgress(cell);
            Double progress = cell.getDoubleConfig("growth_progress");
            if (progress == null || progress < 1.0) continue;

            boolean isPerennial = Boolean.TRUE.equals(cell.getBoolConfig("is_perennial"));
            if (!isPerennial && progress > 1.0 && farmService.isWilted(cell)) {
                toRemove.add(cell.getId());
                continue;
            }

            Integer cropId = cell.getIntConfig("crop_id");
            int yield = farmService.calculateYield(cropId, fudi.getTribulationStage());
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

            beastService.updateBeastProduction(cell, fudi);

            List<Map<String, Object>> productionStored = cell.getProductionStored();
            if (productionStored.isEmpty()) {
                Integer stored = cell.getIntConfig("production_stored");
                if (stored == null || stored <= 0) continue;
                var productionItems = beastService.getProductionItems(cell);
                if (productionItems.isEmpty()) {
                    productionStored = new ArrayList<>();
                    Map<String, Object> defaultItem = new HashMap<>();
                    defaultItem.put("template_id", 1L);
                    defaultItem.put("name", "灵草");
                    defaultItem.put("quantity", stored);
                    productionStored.add(defaultItem);
                } else {
                    productionStored = new ArrayList<>();
                    int remaining = stored;
                    while (remaining > 0) {
                        var selectedItem = beastService.selectRandomProductionItem(productionItems);
                        if (selectedItem != null) {
                            long templateId = selectedItem.templateId();
                            String name = selectedItem.name();
                            boolean found = false;
                            for (Map<String, Object> item : productionStored) {
                                Object id = item.get("template_id");
                                if (id instanceof Number n && n.longValue() == templateId) {
                                    Object currentQty = item.get("quantity");
                                    if (currentQty instanceof Number currentN) {
                                        item.put("quantity", currentN.intValue() + 1);
                                    }
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                Map<String, Object> newItem = new HashMap<>();
                                newItem.put("template_id", templateId);
                                newItem.put("name", name);
                                newItem.put("quantity", 1);
                                productionStored.add(newItem);
                            }
                        }
                        remaining--;
                    }
                }
            }

            if (productionStored.isEmpty()) continue;

            String beastName = "灵兽";
            Beast collectBeast = beastService.findBeastByCell(cell);
            if (collectBeast != null) {
                beastName = collectBeast.getBeastName();
            }

            int cellTotalItems = 0;
            for (Map<String, Object> item : productionStored) {
                Long templateId = ((Number) item.get("template_id")).longValue();
                String name = (String) item.get("name");
                Integer quantity = ((Number) item.get("quantity")).intValue();
                if (quantity > 0) {
                    stackableItemService.addStackableItem(fudi.getUserId(), templateId, ItemType.HERB, name, quantity);
                    cellTotalItems += quantity;
                }
            }

            cell.clearProductionStored();
            cell.setConfigValue("production_stored", 0);
            fudiCellRepository.save(cell);

            totalItems += cellTotalItems;
            collectedCount++;
            log.info("用户 {} 收取地块 {} 的灵兽产出 {} 件", fudi.getUserId(), cell.getCellId(), cellTotalItems);
        }

        return Map.of("harvested", harvestCount, "collected", collectedCount, "totalItems", totalItems);
    }

    // ===================== 建造/拆除/升级系统 =====================

    @Transactional
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

    @Transactional
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

        if (cell.getCellType() == CellType.PEN) {
            Beast beast = beastService.findBeastByCell(cell);
            if (beast != null) {
                beast.setPennedCellId(null);
                beastRepository.save(beast);
            }
        }

        fudiCellRepository.deleteById(cell.getId());

        log.info("用户 {} 拆除地块 {} 的 {}", userId, cellId, cell.getCellType().getChineseName());

        return Map.of("cellId", cellId, "type", cell.getCellType().getChineseName());
    }

    @Transactional
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

    // ===================== 送礼系统 =====================

    @Transactional
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

    // ===================== 地块状态查询 =====================

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
                    farmService.updateGrowthProgress(cell);
                    Double progress = cell.getDoubleConfig("growth_progress");
                    cellInfo.put("growthProgress", progress);
                    cellInfo.put("isMature", progress != null && progress >= 1.0);
                }
                case PEN -> {
                    Beast beast = beastService.findBeastByCell(cell);
                    cellInfo.put("beastName", beast != null ? beast.getBeastName() : "空兽栏");
                    cellInfo.put("tier", beast != null && beast.getTier() != null ? beast.getTier() : 0);
                    cellInfo.put("quality", beast != null ? beast.getQuality() : null);
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

    private Integer parseCellId(String position) {
        try {
            return Integer.valueOf(position);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("地块编号格式错误，请输入数字编号");
        }
    }
}
