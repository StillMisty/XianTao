package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.beast.vo.*;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.domain.fudi.enums.BeastQuality;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.enums.MutationTrait;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.fudi.repository.FudiRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.fudi.vo.CellDetailVO;
import top.stillmisty.xiantao.domain.fudi.vo.CollectVO;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeastService {

    private final FudiRepository fudiRepository;
    private final FudiCellRepository fudiCellRepository;
    private final BeastRepository beastRepository;
    private final ItemTemplateRepository itemTemplateRepository;
    private final StackableItemRepository stackableItemRepository;
    private final SpiritRepository spiritRepository;
    private final StackableItemService stackableItemService;
    private final UserRepository userRepository;
    private final ItemResolver itemResolver;

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

    public ServiceResult<PenCellVO> hatchBeast(PlatformType platform, String openId, String position, String eggName) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(hatchBeast(userId, position, eggName));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    public ServiceResult<PenCellVO> hatchBeastByInput(PlatformType platform, String openId, String position, String input) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(hatchBeastByInput(userId, position, input));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    public ServiceResult<ReleaseBeastVO> releaseBeast(PlatformType platform, String openId, String position) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(releaseBeast(userId, position));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    public ServiceResult<PenCellVO> evolveBeast(PlatformType platform, String openId, String position, String mode) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(evolveBeast(userId, position, mode));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    public ServiceResult<ActionResultVO> deployBeast(PlatformType platform, String openId, String position) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(deployBeast(userId, position));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    public ServiceResult<Object> undeployBeast(PlatformType platform, String openId, String position) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(undeployBeast(userId, position));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

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
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 10);

        FudiCell cell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId)
                .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

        if (cell.getCellType() != CellType.PEN) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }

        if (cell.getIntConfig("beast_id") != null) {
            throw new IllegalStateException("该兽栏已有灵兽，请先放生");
        }

        ItemTemplate eggTemplate = itemTemplateRepository.findByType(ItemType.BEAST_EGG).stream()
                .filter(t -> t.getName().equals(eggName) || t.getName().contains(eggName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未找到兽卵: %s".formatted(eggName)));

        var stackableItem = stackableItemRepository.findByUserIdAndTemplateId(userId, eggTemplate.getId())
                .orElseThrow(() -> new IllegalStateException("背包中没有 [%s]".formatted(eggName)));
        stackableItemService.reduceStackableItem(userId, eggTemplate.getId(), 1);

        int tier = getCropTier(eggTemplate.getGrowTime() != null ? eggTemplate.getGrowTime() : 72);

        int cellLevel = cell.getCellLevel();
        if (cellLevel < tier) {
            throw new IllegalStateException("灵兽等阶(T%d)需要至少Lv%d兽栏".formatted(tier, tier));
        }

        int stoneCost = tier * 200 + 200;
        checkSpiritStones(userId, stoneCost);
        deductSpiritStones(userId, stoneCost);

        BeastQuality quality = rollBeastQuality();

        boolean isMutant = ThreadLocalRandom.current().nextInt(100) < 5;
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

        cell.setConfigValue("beast_id", beast.getId());
        cell.setConfigValue("template_id", eggTemplate.getId());
        cell.setConfigValue("is_incubating", true);
        cell.setConfigValue("hatch_time", now.toString());
        cell.setConfigValue("mature_time", now.plusHours((long) hatchHours).toString());
        cell.setConfigValue("production_stored", 0);
        cell.setConfigValue("last_production_time", now.toString());
        cell.setConfigValue("production_interval_hours", productionInterval);
        fudiCellRepository.save(cell);

        log.info("用户 {} 在地块 {} 孵化 {} (T{}, {}{})", userId, cellId, beastName, tier, quality.getChineseName(), isMutant ? ", 变异" : "");

        return buildPenCellVO(cell);
    }

    @Transactional
    public PenCellVO hatchBeastByInput(Long userId, String position, String input) {
        Integer cellId = parseCellId(position);
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
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        consumeSpiritEnergy(fudi, 10);

        FudiCell cell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId)
                .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

        if (cell.getCellType() != CellType.PEN) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }
        if (cell.getIntConfig("beast_id") != null) {
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
        boolean isMutant = ThreadLocalRandom.current().nextInt(100) < 5;
        List<String> mutationTraits = new ArrayList<>();
        if (isMutant) mutationTraits.add(rollRandomTrait());

        double levelSpeed = getLevelSpeedMultiplier(cellLevel, tier);
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

        cell.setConfigValue("beast_id", beast.getId());
        cell.setConfigValue("template_id", eggTemplate.getId());
        cell.setConfigValue("is_incubating", true);
        cell.setConfigValue("hatch_time", now.toString());
        cell.setConfigValue("mature_time", now.plusHours((long) hatchHours).toString());
        cell.setConfigValue("production_stored", 0);
        cell.setConfigValue("last_production_time", now.toString());
        cell.setConfigValue("production_interval_hours", productionInterval);
        fudiCellRepository.save(cell);

        log.info("用户 {} 在地块 {} 孵化 {} (T{}, {}{})", userId, cellId, beastName, tier, quality.getChineseName(), isMutant ? ", 变异" : "");

        return buildPenCellVO(cell);
    }

    // ===================== 灵兽系统 — 放生与进化 =====================

    @Transactional
    public ReleaseBeastVO releaseBeast(Long userId, String position) {
        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
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

        if (!beast.canEvolve()) {
            throw new IllegalStateException("灵兽需要先达到等级上限才能进化");
        }

        int currentTier = beast.getTier();
        int cost = (currentTier + 1) * 200;
        checkSpiritStones(userId, cost);
        deductSpiritStones(userId, cost);

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

        if (!beast.canEvolve()) {
            throw new IllegalStateException("灵兽需要先达到等级上限才能突破");
        }

        BeastQuality currentQuality = beast.getQuality();
        BeastQuality nextQuality = currentQuality.next();

        int cost = nextQuality.getOrder() * 300;
        checkSpiritStones(userId, cost);
        deductSpiritStones(userId, cost);

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

    @Transactional
    public ActionResultVO deployBeast(Long userId, String position) {
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

        Beast beast = findBeastByCell(cell);
        if (beast == null) {
            throw new IllegalStateException("未找到灵兽");
        }

        if (Boolean.TRUE.equals(beast.getIsDeployed())) {
            return new ActionResultVO(true, "灵兽已处于出战状态");
        }

        if (beast.getHpCurrent() <= 0) {
            throw new IllegalStateException("灵兽HP为0，请先恢复");
        }

        // 检查出战上限
        List<Beast> allBeasts = beastRepository.findByFudiId(fudi.getId());
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

        Integer cellId = parseCellId(position);
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        FudiCell cell = fudiCellRepository.findByFudiIdAndCellId(fudi.getId(), cellId)
                .orElseThrow(() -> new IllegalStateException("地块 " + cellId + " 不存在"));

        if (cell.getCellType() != CellType.PEN) {
            throw new IllegalStateException("地块 " + cellId + " 不是兽栏");
        }

        Beast beast = findBeastByCell(cell);
        if (beast == null) {
            throw new IllegalStateException("未找到灵兽");
        }

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
        Fudi fudi = getFudiByUserId(userId)
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

        Beast beast = findBeastByCell(cell);
        if (beast == null) {
            throw new IllegalStateException("未找到灵兽");
        }

        int hpCurrent = beast.getHpCurrent();
        int hpMax = beast.getMaxHp();
        if (hpCurrent >= hpMax) {
            return new ActionResultVO(true, "灵兽HP已满");
        }

        int missingHp = hpMax - hpCurrent;
        int stoneCost = (int) Math.ceil(missingHp * 0.1);
        checkSpiritStones(userId, stoneCost);
        deductSpiritStones(userId, stoneCost);

        beast.setHpCurrent(hpMax);
        beast.setRecoveryUntil(null);
        beastRepository.save(beast);

        String beastName = beast.getBeastName();
        log.info("用户 {} 恢复灵兽 {} HP (消耗{}灵石)", userId, beastName, stoneCost);
        return new RecoverResultVO(true, "灵兽 [%s] HP已恢复（消耗%d灵石）".formatted(beastName, stoneCost), stoneCost);
    }

    Object recoverAllBeasts(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
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

        checkSpiritStones(userId, totalCost);
        deductSpiritStones(userId, totalCost);

        return new BatchRecoverVO(recoverCount, totalCost);
    }

    // ===================== 灵兽系统 — 查询 =====================

    List<BeastStatusVO> getDeployedBeasts(Long userId) {
        Fudi fudi = getFudiByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));
        List<Beast> allBeasts = beastRepository.findByFudiId(fudi.getId());
        return allBeasts.stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsDeployed()))
                .map(this::convertToBeastStatusVO)
                .toList();
    }

    // ===================== 灵兽产出系统 =====================

    CollectVO collectBeastProduce(Fudi fudi, FudiCell cell, Integer cellId) {
        if (Boolean.TRUE.equals(cell.getBoolConfig("is_incubating"))) {
            throw new IllegalStateException("灵兽尚在孵化中");
        }

        updateBeastProduction(cell, fudi);

        List<Map<String, Object>> productionStored = cell.getProductionStored();
        if (productionStored.isEmpty()) {
            Integer stored = cell.getIntConfig("production_stored");
            if (stored == null || stored <= 0) {
                throw new IllegalStateException("暂无产出可收取");
            }
            var productionItems = getProductionItems(cell);
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
                    var selectedItem = selectRandomProductionItem(productionItems);
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

        if (productionStored.isEmpty()) {
            throw new IllegalStateException("暂无产出可收取");
        }

        String beastName = "灵兽";
        Beast beast = findBeastByCell(cell);
        if (beast != null) {
            beastName = beast.getBeastName();
        }

        int totalItems = 0;
        for (Map<String, Object> item : productionStored) {
            Long templateId = ((Number) item.get("template_id")).longValue();
            String name = (String) item.get("name");
            int quantity = ((Number) item.get("quantity")).intValue();
            if (quantity > 0) {
                stackableItemService.addStackableItem(fudi.getUserId(), templateId, ItemType.HERB, name, quantity);
                totalItems += quantity;
                log.info("用户 {} 收取地块 {} 的灵兽产出: {} x{}", fudi.getUserId(), cellId, name, quantity);
            }
        }

        cell.clearProductionStored();
        cell.setConfigValue("production_stored", 0);
        fudiCellRepository.save(cell);

        log.info("用户 {} 收取地块 {} 的灵兽产出 {} 件", fudi.getUserId(), cellId, totalItems);

        return new CollectVO(cellId, "pen", null, beastName, totalItems, totalItems);
    }

    void updateBeastProduction(FudiCell cell, Fudi fudi) {
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

        Beast beast = findBeastByCell(cell);
        if (beast == null) return;

        if (Boolean.TRUE.equals(beast.getIsDeployed())) return;

        LocalDateTime lastProduction = LocalDateTime.parse(lastProductionTimeStr);
        Double intervalHours = cell.getDoubleConfig("production_interval_hours");
        if (intervalHours == null) intervalHours = 4.0;
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
            Integer currentStored = cell.getIntConfig("production_stored");
            if (currentStored == null) currentStored = 0;
            int maxStorage = tier * 20;
            int newStored = Math.min(maxStorage, currentStored + produced);
            cell.setConfigValue("production_stored", newStored);
        } else {
            int maxStorage = tier * 20;
            int currentTotal = cell.getTotalProductionQuantity();
            int availableSpace = maxStorage - currentTotal;
            if (availableSpace <= 0) {
                cell.setConfigValue("last_production_time", now.toString());
                fudiCellRepository.save(cell);
                return;
            }
            int toProduce = Math.min(produced, availableSpace);
            for (int i = 0; i < toProduce; i++) {
                var selectedItem = selectRandomProductionItem(productionItems);
                if (selectedItem != null) {
                    cell.addProductionItem(selectedItem.templateId(), selectedItem.name(), 1);
                }
            }
            if (mutationTraits != null && mutationTraits.contains("rare_produce")) {
                if (ThreadLocalRandom.current().nextInt(100) < 5) {
                    var higherItem = selectHigherTierItem(productionItems);
                    if (higherItem != null) {
                        cell.addProductionItem(higherItem.templateId(), higherItem.name(), 1);
                    }
                }
            }
        }

        cell.setConfigValue("last_production_time", now.toString());
        fudiCellRepository.save(cell);
    }

    List<ItemProperties.ProductionItem> getProductionItems(FudiCell cell) {
        Integer templateId = cell.getIntConfig("template_id");
        if (templateId == null) {
            return List.of();
        }
        ItemTemplate template = itemTemplateRepository.findById(templateId.longValue()).orElse(null);
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

    // ===================== 灵兽辅助方法 =====================

    Beast findBeastByCell(FudiCell cell) {
        Object beastIdObj = cell.getConfigValue("beast_id");
        if (beastIdObj == null) return null;
        long beastId;
        if (beastIdObj instanceof Long l) {
            beastId = l;
        } else if (beastIdObj instanceof Number n) {
            beastId = n.longValue();
        } else {
            return null;
        }
        return beastRepository.findById(beastId).orElse(null);
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
        String hatchTimeStr = cell.getStringConfig("hatch_time");
        String matureTimeStr = cell.getStringConfig("mature_time");

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
                .isIncubating(Boolean.TRUE.equals(cell.getBoolConfig("is_incubating")))
                .hatchTime(hatchTimeStr != null ? LocalDateTime.parse(hatchTimeStr) : null)
                .matureTime(matureTimeStr != null ? LocalDateTime.parse(matureTimeStr) : null)
                .productionIntervalHours(cell.getDoubleConfig("production_interval_hours") != null ? cell.getDoubleConfig("production_interval_hours") : 4.0)
                .productionStored(cell.getIntConfig("production_stored") != null ? cell.getIntConfig("production_stored") : 0)
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
                    .productionStored(cell.getIntConfig("production_stored") != null ? cell.getIntConfig("production_stored") : 0)
                    .isIncubating(Boolean.TRUE.equals(cell.getBoolConfig("is_incubating")));
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

    public boolean tryAwakeningSkill(Beast beast) {
        BeastSkillPoolVO skillPool = getBeastSkillPool(beast.getTemplateId().intValue());
        if (skillPool == null || skillPool.awakeningSkills().isEmpty()) {
            return false;
        }
        List<Long> currentSkills = beast.getSkills();
        if (currentSkills == null) {
            currentSkills = new ArrayList<>();
        }
        if (currentSkills.size() >= 4) {
            return false;
        }
        if (ThreadLocalRandom.current().nextInt(100) >= 15) {
            return false;
        }
        int totalWeight = 0;
        for (BeastSkillPoolVO.AwakeningSkill awakeningSkill : skillPool.awakeningSkills()) {
            totalWeight += awakeningSkill.weight();
        }
        if (totalWeight <= 0) {
            return false;
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
                    return true;
                }
                break;
            }
        }
        return false;
    }

    // ===================== 灵兽经验系统 =====================

    @Transactional
    public long addBeastExp(Long beastId, long expToAdd) {
        Beast beast = beastRepository.findById(beastId).orElse(null);
        if (beast == null) {
            return 0;
        }
        long actualAdd = beast.addExp(expToAdd);
        beastRepository.save(beast);
        return actualAdd;
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
        cell.setConfigValue("beast_id", null);
        cell.setConfigValue("template_id", null);
        cell.setConfigValue("is_incubating", null);
        cell.setConfigValue("hatch_time", null);
        cell.setConfigValue("mature_time", null);
        cell.setConfigValue("production_stored", null);
        cell.setConfigValue("last_production_time", null);
        cell.setConfigValue("production_interval_hours", null);
        fudiCellRepository.save(cell);
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
        if (user.getSpiritStones() < cost) {
            throw new IllegalStateException("灵石不足（需要 %d，当前 %d）".formatted(cost, user.getSpiritStones()));
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
