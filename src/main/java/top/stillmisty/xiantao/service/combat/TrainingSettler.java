package top.stillmisty.xiantao.service.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.event.EffectData;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.enums.EventTypeEnum;
import top.stillmisty.xiantao.domain.event.vo.FortuneVO;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.entity.SpecialtyEntry;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.vo.DropItem;
import top.stillmisty.xiantao.domain.notification.entity.GameEvent;
import top.stillmisty.xiantao.domain.notification.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.infrastructure.repository.ActivityEventRepository;
import top.stillmisty.xiantao.infrastructure.repository.BeastRepository;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.SkillRepository;
import top.stillmisty.xiantao.infrastructure.util.TypeUtils;
import top.stillmisty.xiantao.infrastructure.util.WeightedRandom;
import top.stillmisty.xiantao.service.FortuneService;
import top.stillmisty.xiantao.service.GameEventService;
import top.stillmisty.xiantao.service.RewardGrant;
import top.stillmisty.xiantao.service.activity.TrainingCompleter;
import top.stillmisty.xiantao.service.beast.BeastCombatService;
import top.stillmisty.xiantao.service.masterapprentice.MasterApprenticeService;
import top.stillmisty.xiantao.service.sect.SectBuildingService;

/** 历练结算器 — 结算一段未结算时长的全部收益（基础修为、物品、事件循环、灵兽经验），中途与最终结算共用。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrainingSettler {

  private final ActivityEventRepository activityEventRepository;
  private final EncounterCalculator encounterCalculator;
  private final CombatEventHandler combatEventHandler;
  private final TrainingCompleter trainingCompleter;
  private final MonsterTemplateRepository monsterTemplateRepository;
  private final SkillRepository skillRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final FortuneService fortuneService;
  private final GameEventService gameEventService;
  private final BeastRepository beastRepository;
  private final BeastCombatService beastCombatService;
  private final RewardGrant rewardGrant;
  private final MasterApprenticeService masterApprenticeService;
  private final SectBuildingService sectBuildingService;

  /** 结算 [fromMinute, toMinute) 的历练：基础修为、物品、统一事件循环与灵兽经验， 并写入角色修为、推进 {@code lastSettlementMinute}。 */
  public TrainingSettlement settle(
      Long userId, Player user, MapNode mapNode, long fromMinute, long toMinute) {
    long minutes = toMinute - fromMinute;
    if (minutes <= 0) return TrainingSettlement.empty();

    double efficiencyMultiplier = TrainingRates.efficiencyMultiplier(user.getEffectiveStatAgi());
    double levelDecayMultiplier =
        TrainingRates.levelDecayMultiplier(user.getLevel(), mapNode.getLevelRequirement());
    long baseExpPerMinute =
        TrainingRates.baseExpPerMinute(mapNode.getLevelRequirement(), user.getEffectiveStatWis());

    // 修炼速度加成：师徒被动（1 + 等级差 × 0.002，上限 1.5）与宗门练功房（+3%/级）
    double masterBonus = masterApprenticeService.calculateTrainingBonus(userId);
    double sectBonus = sectBuildingService.getTrainingBonusForUser(userId);

    var fortune = fortuneService.calculate(userId);
    // 所有乘数用 double 连乘后统一取整，避免中间截断损失精度
    double expMultiplier =
        efficiencyMultiplier
            * levelDecayMultiplier
            * masterBonus
            * (1.0 + sectBonus)
            * fortuneService.getLuckMultiplier(fortune.luck());
    long baseExp = Math.round(baseExpPerMinute * minutes * expMultiplier);
    List<DropItem> items = calculateItemsReward(minutes, efficiencyMultiplier, mapNode);

    SettlementResult loop = runUnifiedEventLoop(userId, user, mapNode, (int) minutes, fortune);
    long killExp = loop.combatSummary().expGained();

    // 灵兽独立修为：历练分钟 × 2 + 本段战斗所得（怪物等级 × 10）。
    // 必须在事件循环之后发放：战斗过程会回写灵兽实体，先发会被旧实体覆盖。
    long beastExpGained = minutes * 2L + loop.combatSummary().beastExpGained();
    beastCombatService.addExpToDeployedBeasts(userId, beastExpGained);

    long totalExp = baseExp + killExp;
    if (totalExp > 0) {
      user.addExp(totalExp);
    }
    rewardGrant.grant(userId, items);
    user.setLastSettlementMinute(toMinute);

    return new TrainingSettlement(
        minutes,
        baseExp,
        killExp,
        items,
        loop.combatSummary(),
        loop.beastDeployed(),
        efficiencyMultiplier,
        levelDecayMultiplier);
  }

  /** 触发一个 CHOICE 事件，将选项写入 game_event.effects */
  @Transactional
  public void fireChoiceEvent(Long userId, String eventCode, Map<String, Object> params) {
    var choiceData = EffectData.ChoiceOptions.fromParamsMap(params);
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.TRAINING_EVENT)
            .withSourceEventCode(eventCode)
            .withEffectData(choiceData));
  }

  private SettlementResult runUnifiedEventLoop(
      Long userId, Player user, MapNode mapNode, int minutesTraining, FortuneVO fortune) {
    List<ActivityEvent> pool = activityEventRepository.findSubEvents("TRAINING", mapNode.getId());
    if (pool.isEmpty()) return SettlementResult.empty();

    var params = encounterCalculator.compute(userId, user, mapNode, minutesTraining);
    CombatSummary combatSummary = CombatSummary.empty();

    List<Long> combatTemplateIds =
        pool.stream()
            .filter(e -> e.getEventType() == EventTypeEnum.COMBAT)
            .map(e -> TypeUtils.getLongOrDefault(e.getParams(), "monster_template_id", 0L))
            .distinct()
            .toList();
    Map<Long, MonsterTemplate> templateMap =
        combatTemplateIds.isEmpty()
            ? Map.of()
            : monsterTemplateRepository.findByIds(combatTemplateIds).stream()
                .collect(Collectors.toMap(MonsterTemplate::getId, t -> t));
    Set<Long> skillIds =
        templateMap.values().stream()
            .flatMap(
                t -> t.getSkills() != null ? t.getSkills().stream() : java.util.stream.Stream.of())
            .collect(Collectors.toSet());
    Map<Long, Skill> skillMap =
        skillIds.isEmpty()
            ? Map.of()
            : skillRepository.findByIds(new ArrayList<>(skillIds)).stream()
                .collect(Collectors.toMap(Skill::getId, s -> s));

    EventContext context = EventContext.withMapAndFortune(mapNode, fortune);

    double fateMultiplier = fortuneService.getFateMultiplier(fortune.fate());
    double adjustedPerRollChance = Math.min(1.0, params.perRollChance() * fateMultiplier);

    // 预查灵兽，避免每场战斗都重复查询
    Map<Long, Beast> beastCache = new HashMap<>();
    for (Beast beast : beastRepository.findDeployedByUserId(userId)) {
      beastCache.put(beast.getId(), beast);
    }

    for (int i = 0; i < params.slots(); i++) {
      if (ThreadLocalRandom.current().nextDouble() >= adjustedPerRollChance) continue;

      ActivityEvent event =
          WeightedRandom.select(pool, ActivityEvent::getWeight, ThreadLocalRandom.current());
      if (event == null) continue;

      if (event.getEventType() == EventTypeEnum.COMBAT) {
        EncounterResult result =
            combatEventHandler.handle(event, userId, user, templateMap, skillMap, i, beastCache);
        combatSummary = combatSummary.merge(result);
        if (user.getStatus() == UserStatus.DYING) break;
      } else if (event.getEventType() == EventTypeEnum.CHOICE) {
        fireChoiceEvent(userId, event.getCode(), event.getParams());
      } else {
        trainingCompleter.handleNumericEvent(userId, user, event, context);
      }
    }
    return new SettlementResult(combatSummary, !beastCache.isEmpty());
  }

  /** 按未结算分钟数判定地图特产掉落：每 10 分钟一次判定，次数本身受身法效率与 12 小时封顶影响。 */
  private List<DropItem> calculateItemsReward(
      long minutesTraining, double efficiencyMultiplier, MapNode mapNode) {
    var specialties = mapNode.getSpecialties();
    if (specialties == null || specialties.isEmpty()) return List.of();
    Map<Long, ItemTemplate> templateMap =
        itemTemplateRepository
            .findByIds(specialties.stream().map(SpecialtyEntry::templateId).toList())
            .stream()
            .collect(Collectors.toMap(ItemTemplate::getId, t -> t));
    long effectiveMinutes = TrainingRates.settlementMinutes(minutesTraining);
    int dropChances = Math.max(1, (int) ((effectiveMinutes / 10.0) * efficiencyMultiplier));
    Map<Long, DropItem> merged = new LinkedHashMap<>();
    for (int i = 0; i < dropChances; i++) {
      SpecialtyEntry selectedEntry =
          WeightedRandom.select(specialties, SpecialtyEntry::weight, ThreadLocalRandom.current());
      if (selectedEntry == null) continue;
      Long selectedTemplateId = selectedEntry.templateId();
      if (selectedTemplateId == null) continue;
      int quantity = ThreadLocalRandom.current().nextInt(3) + 1;
      DropItem existing = merged.get(selectedTemplateId);
      if (existing != null) {
        merged.put(
            selectedTemplateId,
            new DropItem(
                DropItem.DropType.ITEM,
                existing.templateId(),
                existing.name(),
                existing.quantity() + quantity));
      } else {
        ItemTemplate template = templateMap.get(selectedTemplateId);
        String name = template != null ? template.getName() : "未知物品";
        merged.put(
            selectedTemplateId,
            new DropItem(DropItem.DropType.ITEM, selectedTemplateId, name, quantity));
      }
    }
    return new ArrayList<>(merged.values());
  }
}
