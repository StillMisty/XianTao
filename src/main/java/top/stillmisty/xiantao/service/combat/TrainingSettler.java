package top.stillmisty.xiantao.service.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContextKeys;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.event.repository.ActivityEventRepository;
import top.stillmisty.xiantao.domain.event.vo.FortuneVO;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.infrastructure.util.WeightedRandom;
import top.stillmisty.xiantao.service.FortuneService;
import top.stillmisty.xiantao.service.GameEventService;
import top.stillmisty.xiantao.service.activity.TrainingCompleter;

/** 历练结算器 — 统一的历练战斗事件循环，供 TrainingService 和 UserStateService 共享 */
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
  private final FortuneService fortuneService;
  private final GameEventService gameEventService;

  /** 对一段历练时间执行统一事件循环（COMBAT + NUMERIC）并返回战斗统计 */
  public CombatSummary settleChunk(
      Long userId, User user, MapNode mapNode, long fromMinute, long toMinute) {
    int durationMinutes = (int) (toMinute - fromMinute);
    if (durationMinutes <= 0) return CombatSummary.empty();

    var fortune = fortuneService.calculate(userId);
    return runUnifiedEventLoop(userId, user, mapNode, durationMinutes, fortune);
  }

  /** 触发一个 CHOICE 事件，将选项写入 xt_game_event.effects */
  public void fireChoiceEvent(Long userId, String eventCode, Map<String, Object> params) {
    Map<String, Object> choiceData = new HashMap<>();
    choiceData.put("choice", params);
    gameEventService.createEvent(userId, GameEventCategory.TRAINING_EVENT, choiceData);
  }

  private CombatSummary runUnifiedEventLoop(
      Long userId, User user, MapNode mapNode, int minutesTraining, FortuneVO fortune) {
    List<ActivityEvent> pool = activityEventRepository.findSubEvents("TRAINING", mapNode.getId());
    if (pool.isEmpty()) return CombatSummary.empty();

    var params = encounterCalculator.compute(userId, user, mapNode, minutesTraining);
    CombatSummary combatSummary = CombatSummary.empty();

    List<Long> combatTemplateIds =
        pool.stream()
            .filter(e -> "COMBAT".equals(e.getEventType()))
            .map(e -> ((Number) e.getParams().get("monster_template_id")).longValue())
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

    Map<String, Object> context = new HashMap<>();
    EventContextKeys.MAP_NODE.put(context, mapNode);
    EventContextKeys.MAP_NAME.put(context, mapNode.getName());
    EventContextKeys.FORTUNE.put(context, fortune);

    double fateMultiplier = fortuneService.getFateMultiplier(fortune.fate());
    double adjustedPerRollChance = Math.min(1.0, params.perRollChance() * fateMultiplier);

    for (int i = 0; i < params.slots(); i++) {
      if (ThreadLocalRandom.current().nextDouble() >= adjustedPerRollChance) continue;

      ActivityEvent event =
          WeightedRandom.select(pool, ActivityEvent::getWeight, ThreadLocalRandom.current());
      if (event == null) continue;

      if ("COMBAT".equals(event.getEventType())) {
        EncounterResult result =
            combatEventHandler.handle(event, userId, user, templateMap, skillMap, i);
        combatSummary = combatSummary.merge(result);
        if (user.getStatus() == UserStatus.DYING) break;
      } else if ("CHOICE".equals(event.getEventType())) {
        fireChoiceEvent(userId, event.getCode(), event.getParams());
      } else {
        trainingCompleter.handleNumericEvent(userId, user, event, context);
      }
    }
    return combatSummary;
  }
}
