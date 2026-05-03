package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.beast.vo.*;
import top.stillmisty.xiantao.domain.fudi.entity.CellConfig;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.domain.fudi.enums.BeastQuality;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.enums.MutationTrait;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.fudi.vo.CellDetailVO;
import top.stillmisty.xiantao.domain.fudi.vo.CollectVO;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeastService {

    private final FudiCellRepository fudiCellRepository;
    private final BeastRepository beastRepository;
    private final ItemTemplateRepository itemTemplateRepository;
    private final StackableItemRepository stackableItemRepository;
    private final SpiritRepository spiritRepository;
    private final StackableItemService stackableItemService;
    private final ItemResolver itemResolver;
    private final FudiHelper fudiHelper;

    // ===================== 灵兽战斗属性公式 =====================

    public static int calculateBeastAttack(int level, BeastQuality quality) {
        double q = getCombatStatMultiplier(quality);
        return (int) Math.round((10 + (level - 1) * 3 * q) * q);
    }

    public static int calculateBeastDefense(int level, BeastQuality quality) {
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

    // ===================== 公开 API（含认证） =====================

    @Transactional
    public ServiceResult<PenCellVO> hatchBeast(PlatformType platform, String openId, String position, String eggName) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(hatchBeast(userId, position, eggName));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    @Transactional
    public ServiceResult<PenCellVO> hatchBeastByInput(PlatformType platform, String openId, String position, String input) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(hatchBeastByInput(userId, position, input));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    @Transactional
    public ServiceResult<ReleaseBeastVO> releaseBeast(PlatformType platform, String openId, String position) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(releaseBeast(userId, position));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    @Transactional
    public ServiceResult<PenCellVO> evolveBeast(PlatformType platform, String openId, String position, String mode) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(evolveBeast(userId, position, mode));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    @Transactional
    public ServiceResult<ActionResultVO> deployBeast(PlatformType platform, String openId, String position) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(deployBeast(userId, position));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    @Transactional
    public ServiceResult<Object> undeployBeast(PlatformType platform, String openId, String position) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(undeployBeast(userId, position));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    @Transactional
    public ServiceResult<Object> recoverBeast(PlatformType platform, String openId, String position) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(recoverBeast(userId, position));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    public ServiceResult<List<BeastStatusVO>> getDeployedBeasts(PlatformType platform, String openId) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(getDeployedBeasts(userId));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    // ===================== 内部 API =====================

    // ===================== 灵兽系统 — 孵化 =====================

    @Transactional
    public PenCellVO hatchBeast(Long userId, String position, String eggName) {
        Integer cellId = fudiHelper.parseCellId(position);

        ItemTemplate eggTemplate = itemTemplateRepository.findByType(ItemType.BEAST_EGG).stream()
                .filter(t -> t.getName().equals(eggName) || t.getName().contains(eggName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未找到兽卵: %s".formatted(eggName)));

        var stackableItem = stackableItemRepository.findByUserIdAndTemplateId(userId, eggTemplate.getId())
                .orElseThrow(() -> new IllegalStateException("背包中没有 [%s]".formatted(eggName)));
        stackableItemService.reduceStackableItem(userId, eggTemplate.getId(), 1);

        return hatchBeastWithTemplate(userId, cellId, eggTemplate);
    }

    @Transactional
    public PenCellVO hatchBeastByInput(Long userId, String position, String input) {
        Integer cellId = fudiHelper.parseCellId(position);
        var result = itemResolver.resolveEgg(userId, input);
        return switch (result) {
            case ItemResolver.Found(var template, var index) -> {
                var stackableItem = stackableItemRepository
                        .findByUserIdAndTemplateId(userId, template.getId())
                        .orElseThrow(() -> new IllegalStateException("背包中没有 [" + template.getName() + "]"));
                stackableItemService.reduceStackableItem(userId, template.getId(), 1);
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

    @Transactional
    PenCellVO hatchBeastWithTemplate(Long userId, Integer cellId, ItemTemplate eggTemplate) {
        Fudi fudi = fudiHelper.getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        fudiHelper.consumeSpiritEnergy(fudi, 10);

        FudiCell cell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId)
                .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

        if (cell.getCellType() != CellType.PEN) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }
        if (cell.getConfig() instanceof CellConfig.PenConfig pen && pen.beastId() != null) {
            throw new IllegalStateException("该兽栏已有灵兽，请先放生");
        }

        int tier = fudiHelper.getCropTier(eggTemplate.getGrowTime() != null ? eggTemplate.getGrowTime() : 72);
        int cellLevel = cell.getCellLevel();
        if (cellLevel < tier) {
            throw new IllegalStateException("灵兽等阶(T%d)需要至少Lv%d兽栏".formatted(tier, tier));
        }

        int stoneCost = tier * 200 + 200;
        fudiHelper.checkSpiritStones(userId, stoneCost);
        fudiHelper.deductSpiritStones(userId, stoneCost);

        BeastQuality quality = rollBeastQuality();
        boolean isMutant = ThreadLocalRandom.current().nextInt(100) < 5;
        List<String> mutationTraits = new ArrayList<>();
        if (isMutant) mutationTraits.add(rollRandomTrait());

        double levelSpeed = fudiHelper.getLevelSpeedMultiplier(cellLevel, tier);
        double productionInterval = 4.0 / levelSpeed;
        double baseHatchHours = 24 + tier * 8;
        double hatchHours = baseHatchHours / levelSpeed;

        String beastName = eggTemplate.getName().replace("兽卵", "").replace("蛋", "灵兽");
        LocalDateTime now = LocalDateTime.now();

        Beast beast = new Beast();
        beast.setUserId(userId);
        beast.setFudiId(fudi.getId());
        beast.setTemplateId(eggTemplate.getId());
        beast.setBeastName(beastName);
        beast.setTier(tier);
        beast.setQuality(quality);
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
        beast.setLevelCap(tier * 10 + 10);
        beastRepository.save(beast);

        unlockInnateSkills(beast, "birth");
        beastRepository.save(beast);

        cell.setConfig(new CellConfig.PenConfig(
                beast.getId(), eggTemplate.getId().intValue(), now, now.plusHours((long) hatchHours),
                new ArrayList<>(), now
        ));
        fudiCellRepository.save(cell);

        log.info("用户 {} 在地块 {} 孵化 {} (T{}, {}{})", userId, cellId, beastName, tier, quality.getChineseName(), isMutant ? ", 变异" : "");

        return buildPenCellVO(cell);
    }

    // ===================== 灵兽系统 — 放生与进化 =====================

    @Transactional
    public ReleaseBeastVO releaseBeast(Long userId, String position) {
        Integer cellId = fudiHelper.parseCellId(position);
        Fudi fudi = fudiHelper.getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        FudiCell cell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId)
                .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

        if (cell.getCellType() != CellType.PEN) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }

        Beast beast = findBeastByCell(cell);
        String beastName = beast != null ? beast.getBeastName() : "未知灵兽";
        int tier = beast != null ? beast.getTier() : 1;
        String qualityStr = beast != null ? beast.getQuality().getCode() : BeastQuality.MORTAL.getCode();

        clearBeastCell(cell);
        if (beast != null) {
            beastRepository.deleteById(beast.getId());
        }

        log.info("用户 {} 放生 {} (T{}/{})", userId, beastName, tier, qualityStr);

        return new ReleaseBeastVO(beastName, tier, qualityStr);
    }

    @Transactional
    public PenCellVO evolveBeast(Long userId, String position, String mode) {
        Integer cellId = fudiHelper.parseCellId(position);
        Fudi fudi = fudiHelper.getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        fudiHelper.consumeSpiritEnergy(fudi, 8);

        FudiCell cell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId)
                .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

        if (cell.getCellType() != CellType.PEN) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }
        if (isIncubating(cell)) {
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
        stackableItemService.reduceStackableItem(userId, stoneTemplate.getId(), stoneCount);

        if ("升品".equals(mode)) {
            return breakthroughBeastQuality(fudi, cell, userId, cellId);
        } else {
            return evolveBeastTier(fudi, cell, userId, cellId);
        }
    }

    // ===================== 灵兽系统 — 等阶进化 =====================

    @Transactional
    PenCellVO evolveBeastTier(Fudi fudi, FudiCell cell, Long userId, Integer cellId) {
        Beast beast = findBeastByCell(cell);
        if (beast == null) {
            throw new IllegalStateException("未找到灵兽");
        }

        if (beast.getTier() >= 5) {
            throw new IllegalStateException("已是最高等阶 T5");
        }

        if (beast.needsMoreLevels()) {
            throw new IllegalStateException("灵兽需要先达到等级上限才能进化");
        }

        int currentTier = beast.getTier();
        int cost = (currentTier + 1) * 200;
        fudiHelper.checkSpiritStones(userId, cost);
        fudiHelper.deductSpiritStones(userId, cost);

        Spirit spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
        int affectionBonus = spirit != null && spirit.getAffection() != null ? Math.min(15, spirit.getAffection() / 7) : 0;
        int successRate = 85 + affectionBonus;
        boolean success = ThreadLocalRandom.current().nextInt(100) < successRate;

        if (!success) {
            throw new IllegalStateException("进化失败！进化石和灵石已消耗");
        }

        beast.evolve();
        beast.setHpCurrent(beast.getMaxHp());

        boolean qualityUpgraded = false;
        if (ThreadLocalRandom.current().nextInt(100) < 10 && beast.getQuality() != BeastQuality.DIVINE) {
            beast.qualityBreak();
            qualityUpgraded = true;
        }

        boolean mutationTriggered = rollMutation(15);
        if (mutationTriggered) {
            addMutationTrait(beast);
        }

        if (beast.getTier() == 2) {
            unlockInnateSkills(beast, "tier_2");
        } else if (beast.getTier() == 3) {
            unlockInnateSkills(beast, "tier_3");
        }

        beastRepository.save(beast);

        log.info(
                "用户 {} 进化地块 {} 的灵兽 T{}->T{}{}", userId, cellId, currentTier, beast.getTier(),
                qualityUpgraded ? " (品质连带提升!)" : ""
        );

        return buildPenCellVO(cell);
    }

    // ===================== 灵兽系统 — 品质突破 =====================

    @Transactional
    PenCellVO breakthroughBeastQuality(Fudi fudi, FudiCell cell, Long userId, Integer cellId) {
        Beast beast = findBeastByCell(cell);
        if (beast == null) {
            throw new IllegalStateException("未找到灵兽");
        }

        if (beast.getQuality() == BeastQuality.DIVINE) {
            throw new IllegalStateException("已是最高品质神品");
        }

        if (beast.needsMoreLevels()) {
            throw new IllegalStateException("灵兽需要先达到等级上限才能突破");
        }

        BeastQuality currentQuality = beast.getQuality();
        BeastQuality nextQuality = currentQuality.next();

        int cost = nextQuality.getOrder() * 300;
        fudiHelper.checkSpiritStones(userId, cost);
        fudiHelper.deductSpiritStones(userId, cost);

        Spirit spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
        int affectionBonus = spirit != null && spirit.getAffection() != null ? Math.min(20, spirit.getAffection() / 5) : 0;
        int successRate = 60 + affectionBonus;

        // 灵悟变异特性：品质突破成功率+10%
        if (beast.getMutationTraits() != null && beast.getMutationTraits().contains("spiritual")) {
            successRate += 10;
        }

        boolean success = ThreadLocalRandom.current().nextInt(100) < successRate;

        if (!success) {
            throw new IllegalStateException("品质突破失败！进化石和灵石已消耗");
        }

        beast.qualityBreak();
        beast.recalculateAttributes();
        beast.setHpCurrent(beast.getMaxHp());

        boolean mutationTriggered = rollMutation(10);
        if (mutationTriggered) {
            addMutationTrait(beast);
        }

        unlockInnateSkills(beast, "quality_break");
        beastRepository.save(beast);

        log.info("用户 {} 品质突破地块 {} 的灵兽 {} -> {}", userId, cellId, currentQuality.getChineseName(), nextQuality.getChineseName());

        return buildPenCellVO(cell);
    }

    // ===================== 灵兽系统 — 出战/召回 =====================

    private record PenCellBeast(Fudi fudi, FudiCell cell, Integer cellId, Beast beast) {}

    private PenCellBeast getBeastFromPenCell(Long userId, String position, boolean checkIncubating) {
        Integer cellId = fudiHelper.parseCellId(position);
        Fudi fudi = fudiHelper.getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        FudiCell cell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId)
                .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

        if (cell.getCellType() != CellType.PEN) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }
        if (checkIncubating && isIncubating(cell)) {
            throw new IllegalStateException("灵兽尚在孵化中");
        }

        Beast beast = findBeastByCell(cell);
        if (beast == null) {
            throw new IllegalStateException("未找到灵兽");
        }
        return new PenCellBeast(fudi, cell, cellId, beast);
    }

    @Transactional
    public ActionResultVO deployBeast(Long userId, String position) {
        PenCellBeast pcb = getBeastFromPenCell(userId, position, true);
        Beast beast = pcb.beast();

        if (Boolean.TRUE.equals(beast.getIsDeployed())) {
            return new ActionResultVO(true, "灵兽已处于出战状态");
        }

        if (beast.getHpCurrent() <= 0) {
            throw new IllegalStateException("灵兽HP为0，请先恢复");
        }

        // 检查出战上限
        List<Beast> allBeasts = beastRepository.findByFudiId(pcb.fudi().getId());
        long deployedCount = allBeasts.stream().filter(b -> Boolean.TRUE.equals(b.getIsDeployed()) && b.canFight()).count();
        if (deployedCount >= 2) {
            throw new IllegalStateException("出战灵兽已达上限 (2只)，请先召回其他灵兽");
        }

        beast.setIsDeployed(true);
        beastRepository.save(beast);

        String beastName = beast.getBeastName();
        log.info("用户 {} 将灵兽 {} 设为出战", userId, beastName);
        return new ActionResultVO(true, "灵兽 [%s] 已出战".formatted(beastName));
    }

    @Transactional
    public Object undeployBeast(Long userId, String position) {
        if ("all".equalsIgnoreCase(position)) {
            return undeployAllBeasts(userId);
        }

        PenCellBeast pcb = getBeastFromPenCell(userId, position, false);
        Beast beast = pcb.beast();

        if (!Boolean.TRUE.equals(beast.getIsDeployed())) {
            return new ActionResultVO(true, "灵兽未在出战状态");
        }

        beast.setIsDeployed(false);
        beastRepository.save(beast);

        String beastName = beast.getBeastName();
        log.info("用户 {} 将灵兽 {} 召回", userId, beastName);
        return new ActionResultVO(true, "灵兽 [%s] 已召回".formatted(beastName));
    }

    BatchCountVO undeployAllBeasts(Long userId) {
        Fudi fudi = fudiHelper.getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        List<Beast> beasts = beastRepository.findByFudiId(fudi.getId());
        int count = 0;
        for (Beast b : beasts) {
            if (Boolean.TRUE.equals(b.getIsDeployed())) {
                b.setIsDeployed(false);
                beastRepository.save(b);
                count++;
            }
        }

        return new BatchCountVO(count);
    }

    // ===================== 灵兽系统 — 恢复 =====================

    @Transactional
    public Object recoverBeast(Long userId, String position) {
        if ("all".equalsIgnoreCase(position)) {
            return recoverAllBeasts(userId);
        }

        PenCellBeast pcb = getBeastFromPenCell(userId, position, true);
        Beast beast = pcb.beast();

        int hpCurrent = beast.getHpCurrent();
        int hpMax = beast.getMaxHp();
        if (hpCurrent >= hpMax) {
            return new ActionResultVO(true, "灵兽HP已满");
        }

        int missingHp = hpMax - hpCurrent;
        int stoneCost = (int) Math.ceil(missingHp * 0.1);
        fudiHelper.checkSpiritStones(userId, stoneCost);
        fudiHelper.deductSpiritStones(userId, stoneCost);

        beast.setHpCurrent(hpMax);
        beast.setRecoveryUntil(null);
        beastRepository.save(beast);

        String beastName = beast.getBeastName();
        log.info("用户 {} 恢复灵兽 {} HP (消耗{}灵石)", userId, beastName, stoneCost);
        return new RecoverResultVO(true, "灵兽 [%s] HP已恢复（消耗%d灵石）".formatted(beastName, stoneCost), stoneCost);
    }

    Object recoverAllBeasts(Long userId) {
        Fudi fudi = fudiHelper.getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        int totalCost = 0;
        int recoverCount = 0;

        List<Beast> beasts = beastRepository.findByFudiId(fudi.getId());
        for (Beast beast : beasts) {
            int hpCurrent = beast.getHpCurrent();
            int hpMax = beast.getMaxHp();
            if (hpCurrent >= hpMax) continue;

            int missingHp = hpMax - hpCurrent;
            totalCost += (int) Math.ceil(missingHp * 0.1);
            beast.setHpCurrent(hpMax);
            beast.setRecoveryUntil(null);
            beastRepository.save(beast);
            recoverCount++;
        }

        if (recoverCount == 0) {
            return new ActionResultVO(true, "没有需要恢复的灵兽");
        }

        fudiHelper.checkSpiritStones(userId, totalCost);
        fudiHelper.deductSpiritStones(userId, totalCost);

        return new BatchRecoverVO(recoverCount, totalCost);
    }

    // ===================== 灵兽系统 — 查询 =====================

    List<BeastStatusVO> getDeployedBeasts(Long userId) {
        Fudi fudi = fudiHelper.getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        List<Beast> allBeasts = beastRepository.findByFudiId(fudi.getId());
        return allBeasts.stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsDeployed()))
                .map(this::convertToBeastStatusVO)
                .toList();
    }

    // ===================== 灵兽产出系统 =====================

    CollectVO collectBeastProduce(Fudi fudi, FudiCell cell, Integer cellId) {
        if (isIncubating(cell)) {
            throw new IllegalStateException("灵兽尚在孵化中");
        }

        updateBeastProduction(cell, fudi);

        List<CellConfig.ProductionItem> productionStored = getProductionStoredList(cell);

        if (productionStored.isEmpty()) {
            throw new IllegalStateException("暂无产出可收取");
        }

        String beastName = "灵兽";
        Beast beast = findBeastByCell(cell);
        if (beast != null) {
            beastName = beast.getBeastName();
        }

        int totalItems = 0;
        for (CellConfig.ProductionItem item : productionStored) {
            if (item.quantity() > 0) {
                stackableItemService.addStackableItem(fudi.getUserId(), item.templateId(), ItemType.HERB, item.name(), item.quantity());
                totalItems += item.quantity();
                log.info("用户 {} 收取地块 {} 的灵兽产出: {} x{}", fudi.getUserId(), cellId, item.name(), item.quantity());
            }
        }

        cell.clearProductionStored();
        fudiCellRepository.save(cell);

        log.info("用户 {} 收取地块 {} 的灵兽产出 {} 件", fudi.getUserId(), cellId, totalItems);

        return new CollectVO(cellId, "pen", null, beastName, totalItems, totalItems);
    }

    void updateBeastProduction(FudiCell cell, Fudi fudi) {
        if (!(cell.getConfig() instanceof CellConfig.PenConfig pen)) return;

        LocalDateTime matureTime = pen.matureTime();
        if (matureTime == null) return;

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(matureTime)) return;

        Beast beast = findBeastByCell(cell);
        if (beast == null) return;

        if (Boolean.TRUE.equals(beast.getIsDeployed())) return;

        LocalDateTime lastProduction = pen.lastProductionTime();
        if (lastProduction == null) lastProduction = matureTime;

        double intervalHours = getProductionIntervalHours(pen.templateId());
        long intervalSeconds = (long) (intervalHours * 3600);
        if (intervalSeconds <= 0) intervalSeconds = 14400;

        long elapsed = java.time.Duration.between(lastProduction, now).getSeconds();
        int cycles = (int) (elapsed / intervalSeconds);
        if (cycles <= 0) return;

        int tier = beast.getTier();
        double outputMultiplier = beast.getQualityMultiplier();
        int perCycle = (int) Math.round(tier * outputMultiplier * (1 + ThreadLocalRandom.current().nextInt(tier + 1)) / 2.0);
        int produced = Math.max(1, perCycle * cycles);

        List<String> mutationTraits = beast.getMutationTraits();
        if (mutationTraits != null && mutationTraits.contains("high_yield")) {
            produced = (int) (produced * 1.3);
        }

        List<ItemProperties.ProductionItem> productionItems = getProductionItems(cell);
        if (productionItems.isEmpty()) {
            int currentStored = pen.totalProductionQuantity();
            int maxStorage = tier * 20;
            int newStored = Math.min(maxStorage, currentStored + produced);
            int added = newStored - currentStored;
            if (added > 0) {
                pen.addProductionItem(1L, "灵草", added);
            }
        } else {
            int maxStorage = tier * 20;
            int currentTotal = pen.totalProductionQuantity();
            int availableSpace = maxStorage - currentTotal;
            if (availableSpace <= 0) {
                cell.setConfig(pen.withLastProductionTime(now));
                fudiCellRepository.save(cell);
                return;
            }
            int toProduce = Math.min(produced, availableSpace);
            for (int i = 0; i < toProduce; i++) {
                var selectedItem = selectRandomProductionItem(productionItems);
                if (selectedItem != null) {
                    pen.addProductionItem(selectedItem.templateId(), selectedItem.name(), 1);
                }
            }
            if (mutationTraits != null && mutationTraits.contains("rare_produce")) {
                if (ThreadLocalRandom.current().nextInt(100) < 5) {
                    var higherItem = selectHigherTierItem(productionItems);
                    if (higherItem != null) {
                        pen.addProductionItem(higherItem.templateId(), higherItem.name(), 1);
                    }
                }
            }
        }

        cell.setConfig(pen.withLastProductionTime(now));
        fudiCellRepository.save(cell);
    }

    List<ItemProperties.ProductionItem> getProductionItems(FudiCell cell) {
        if (!(cell.getConfig() instanceof CellConfig.PenConfig pen) || pen.templateId() == null) {
            return List.of();
        }
        ItemTemplate template = itemTemplateRepository.findById(pen.templateId().longValue()).orElse(null);
        if (template == null) {
            return List.of();
        }
        var props = template.typedProperties();
        if (props instanceof ItemProperties.BeastEgg egg) {
            return egg.productionItems();
        }
        return List.of();
    }

    ItemProperties.ProductionItem selectRandomProductionItem(List<ItemProperties.ProductionItem> productionItems) {
        if (productionItems.isEmpty()) {
            return null;
        }
        int totalWeight = 0;
        for (var item : productionItems) {
            totalWeight += item.weight();
        }
        if (totalWeight <= 0) {
            return productionItems.getFirst();
        }
        int random = ThreadLocalRandom.current().nextInt(totalWeight);
        int current = 0;
        for (var item : productionItems) {
            current += item.weight();
            if (random < current) {
                return item;
            }
        }
        return productionItems.getFirst();
    }

    ItemProperties.ProductionItem selectHigherTierItem(List<ItemProperties.ProductionItem> productionItems) {
        if (productionItems.isEmpty()) return null;
        return productionItems.stream().min(Comparator.comparingInt(ItemProperties.ProductionItem::weight)).orElse(null);
    }

    List<CellConfig.ProductionItem> getProductionStoredList(FudiCell cell) {
        if (cell.getConfig() instanceof CellConfig.PenConfig pen) {
            return pen.productionStored();
        }
        return List.of();
    }

    // ===================== 灵兽辅助方法 =====================

    Beast findBeastByCell(FudiCell cell) {
        if (!(cell.getConfig() instanceof CellConfig.PenConfig pen) || pen.beastId() == null) return null;
        return beastRepository.findById(pen.beastId()).orElse(null);
    }

    BeastStatusVO convertToBeastStatusVO(Beast beast) {
        return BeastStatusVO.builder()
                .id(beast.getId())
                .beastName(beast.getBeastName())
                .quality(beast.getQuality().getCode())
                .isMutant(Boolean.TRUE.equals(beast.getIsMutant()))
                .mutationTraits(beast.getMutationTraits())
                .tier(beast.getTier())
                .level(beast.getLevel())
                .exp(beast.getExp())
                .attack(beast.getAttack())
                .defense(beast.getDefense())
                .maxHp(beast.getMaxHp())
                .hpCurrent(beast.getHpCurrent())
                .skills(beast.getSkills())
                .isDeployed(Boolean.TRUE.equals(beast.getIsDeployed()))
                .needsRecovery(beast.needsRecovery())
                .pennedCellId(beast.getPennedCellId() != null ? beast.getPennedCellId() : 0)
                .build();
    }

    PenCellVO buildPenCellVO(FudiCell cell) {
        Beast beast = findBeastByCell(cell);
        CellConfig.PenConfig pen = cell.getConfig() instanceof CellConfig.PenConfig p ? p : null;

        String beastName = beast != null ? beast.getBeastName() : "未知灵兽";
        int tier = beast != null ? beast.getTier() : 1;
        String qualityChinese = beast != null ? beast.getQuality().getChineseName() : "凡品";
        int qualityOrdinal = beast != null ? beast.getQuality().getOrder() : 1;
        boolean isMutant = beast != null && Boolean.TRUE.equals(beast.getIsMutant());
        List<String> mutationTraits = beast != null && beast.getMutationTraits() != null ? beast.getMutationTraits() : List.of();
        int powerScore = tier * 10;

        return PenCellVO.builder()
                .cellId(cell.getCellId())
                .cellLevel(cell.getCellLevel())
                .beastId(beast != null ? beast.getId() : null)
                .beastName(beastName)
                .tier(tier)
                .quality(qualityChinese)
                .qualityOrdinal(qualityOrdinal)
                .isMutant(isMutant)
                .mutationTraits(mutationTraits)
                .isIncubating(isIncubating(cell))
                .hatchTime(pen != null ? pen.hatchTime() : null)
                .matureTime(pen != null ? pen.matureTime() : null)
                .productionIntervalHours(pen != null ? getProductionIntervalHours(pen.templateId()) : 4.0)
                .productionStored(cell.getTotalProductionQuantity())
                .powerScore(powerScore)
                .birthTime(beast != null ? beast.getBirthTime() : null)
                .build();
    }

    void buildPenCellDetail(CellDetailVO.CellDetailVOBuilder builder, FudiCell cell) {
        Beast beast = findBeastByCell(cell);

        if (beast == null) {
            builder.name("空兽栏");
        } else {
            String qualityChinese = beast.getQuality().getChineseName();
            List<String> traits = beast.getMutationTraits() != null ? beast.getMutationTraits() : List.of();
            builder.name(beast.getBeastName())
                    .level(beast.getTier())
                    .quality(qualityChinese)
                    .mutationTraits(traits)
                    .productionStored(cell.getTotalProductionQuantity())
                    .isIncubating(isIncubating(cell));
        }
    }

    // ===================== 灵兽技能系统 =====================

    BeastSkillPoolVO getBeastSkillPool(Integer templateId) {
        if (templateId == null) {
            return null;
        }
        ItemTemplate template = itemTemplateRepository.findById(templateId.longValue()).orElse(null);
        if (template == null) {
            return null;
        }
        var props = template.typedProperties();
        if (!(props instanceof ItemProperties.BeastEgg egg)) {
            return null;
        }
        var pool = egg.skillPool();
        if (pool == null) {
            return null;
        }
        var innateSkills = pool.innateSkills().stream()
                .map(is -> new BeastSkillPoolVO.InnateSkill(is.skillId(), is.unlock()))
                .toList();
        var awakeningSkills = pool.awakeningSkills().stream()
                .map(as -> new BeastSkillPoolVO.AwakeningSkill(as.skillId(), as.weight()))
                .toList();
        return new BeastSkillPoolVO(innateSkills, awakeningSkills);
    }

    void unlockInnateSkills(Beast beast, String unlockCondition) {
        BeastSkillPoolVO skillPool = getBeastSkillPool(beast.getTemplateId().intValue());
        if (skillPool == null) {
            return;
        }
        List<Long> currentSkills = beast.getSkills();
        if (currentSkills == null) {
            currentSkills = new ArrayList<>();
        }
        for (BeastSkillPoolVO.InnateSkill innateSkill : skillPool.innateSkills()) {
            if (innateSkill.unlock().equals(unlockCondition)) {
                if (!currentSkills.contains(innateSkill.skillId())) {
                    currentSkills.add(innateSkill.skillId());
                    log.info("灵兽 {} 解锁先天技: {}", beast.getBeastName(), innateSkill.skillId());
                }
            }
        }
        beast.setSkills(currentSkills);
    }

    public void tryAwakeningSkill(Beast beast) {
        BeastSkillPoolVO skillPool = getBeastSkillPool(beast.getTemplateId().intValue());
        if (skillPool == null || skillPool.awakeningSkills().isEmpty()) {
            return;
        }
        List<Long> currentSkills = beast.getSkills();
        if (currentSkills == null) {
            currentSkills = new ArrayList<>();
        }
        if (currentSkills.size() >= 4) {
            return;
        }
        if (ThreadLocalRandom.current().nextInt(100) >= 15) {
            return;
        }
        int totalWeight = 0;
        for (BeastSkillPoolVO.AwakeningSkill awakeningSkill : skillPool.awakeningSkills()) {
            totalWeight += awakeningSkill.weight();
        }
        if (totalWeight <= 0) {
            return;
        }
        int random = ThreadLocalRandom.current().nextInt(totalWeight);
        int current = 0;
        for (BeastSkillPoolVO.AwakeningSkill awakeningSkill : skillPool.awakeningSkills()) {
            current += awakeningSkill.weight();
            if (random < current) {
                if (!currentSkills.contains(awakeningSkill.skillId())) {
                    currentSkills.add(awakeningSkill.skillId());
                    beast.setSkills(currentSkills);
                    log.info("灵兽 {} 觉醒后天悟: {}", beast.getBeastName(), awakeningSkill.skillId());
                    return;
                }
                break;
            }
        }
    }

    // ===================== 灵兽经验系统 =====================

    @Transactional
    public void addBeastExp(Long beastId, long expToAdd) {
        Beast beast = beastRepository.findById(beastId).orElse(null);
        if (beast == null) {
            return;
        }
        beast.addExp(expToAdd);
        beastRepository.save(beast);
    }

    @Transactional
    public void addExpToDeployedBeasts(Long userId, long expToAdd) {
        List<Beast> deployedBeasts = beastRepository.findByUserIdAndIsDeployed(userId, true);
        for (Beast beast : deployedBeasts) {
            addBeastExp(beast.getId(), expToAdd);
        }
    }

    // ===================== 灵兽变异/品质辅助方法 =====================

    BeastQuality rollBeastQuality() {
        int roll = ThreadLocalRandom.current().nextInt(1000);
        int cumulative = 0;
        for (BeastQuality q : BeastQuality.values()) {
            cumulative += q.getHatchWeight();
            if (roll < cumulative) return q;
        }
        return BeastQuality.MORTAL;
    }

    String rollRandomTrait() {
        return MutationTrait.values()[ThreadLocalRandom.current().nextInt(MutationTrait.values().length)].getCode();
    }

    boolean rollMutation(int chancePercent) {
        return ThreadLocalRandom.current().nextInt(100) < chancePercent;
    }

    void addMutationTrait(Beast beast) {
        List<String> traits = beast.getMutationTraits();
        if (traits == null) {
            traits = new ArrayList<>();
        }
        if (traits.size() >= 2) return;
        String newTrait = rollRandomTrait();
        if (!traits.contains(newTrait)) {
            traits.add(newTrait);
            beast.setMutationTraits(traits);
            beast.setIsMutant(true);
        }
    }

    // ===================== 地块操作辅助方法 =====================

    void clearBeastCell(FudiCell cell) {
        cell.setConfig(new CellConfig.EmptyConfig());
        fudiCellRepository.save(cell);
    }

    boolean isIncubating(FudiCell cell) {
        if (!(cell.getConfig() instanceof CellConfig.PenConfig pen)) return false;
        LocalDateTime matureTime = pen.matureTime();
        if (matureTime == null) return false;
        return LocalDateTime.now().isBefore(matureTime);
    }

    double getProductionIntervalHours(Integer templateId) {
        if (templateId == null) return 4.0;
        ItemTemplate template = itemTemplateRepository.findById(templateId.longValue()).orElse(null);
        if (template == null) return 4.0;
        double baseGrowthHours = template.getGrowTime() != null ? template.getGrowTime() : 72;
        int tier = fudiHelper.getCropTier((int) baseGrowthHours);
        double levelSpeed = fudiHelper.getLevelSpeedMultiplier(1, tier);
        return 4.0 / levelSpeed;
    }
}
