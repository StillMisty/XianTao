package top.stillmisty.xiantao.service.dungeon;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.monster.BattleContext;
import top.stillmisty.xiantao.domain.monster.CombatEngine;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.Monster;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.SkillRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.infrastructure.util.WeightedRandom;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.combat.CombatService;
import top.stillmisty.xiantao.service.combat.PostCombatProcessor;
import top.stillmisty.xiantao.service.player.UserStateService;

@Component
@RequiredArgsConstructor
public class DungeonCombatHelper {

  private final MonsterTemplateRepository monsterTemplateRepository;
  private final SkillRepository skillRepository;
  private final CombatEngine combatEngine;
  private final CombatService combatService;
  private final PostCombatProcessor postCombatProcessor;
  private final UserStateService userStateService;

  public record SimpleCombatOutcome(
      boolean playerWon,
      long expGained,
      String monsterName,
      String summary,
      List<String> lootDescriptions) {}

  @Transactional
  public SimpleCombatOutcome executeCombat(
      Long userId,
      Player user,
      DungeonTemplate.Poi poi,
      DungeonTemplate.MonsterEntry monsterEntry) {

    MonsterTemplate monsterTmpl =
        monsterTemplateRepository
            .findById(monsterEntry.templateId())
            .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_POI_NOT_FOUND));

    // 按模板等级加载技能并尊重 min/max 数量（原硬编码 1 级无技能导致高阶模板属性为负、开局即胜）
    List<Skill> monsterSkills = loadMonsterSkills(monsterTmpl);
    int minCount =
        monsterEntry.minCount() != null && monsterEntry.minCount() > 0
            ? monsterEntry.minCount()
            : 1;
    int maxCount =
        monsterEntry.maxCount() != null && monsterEntry.maxCount() >= minCount
            ? monsterEntry.maxCount()
            : minCount;
    int count = ThreadLocalRandom.current().nextInt(minCount, maxCount + 1);

    CombatTeam monsterTeam = new CombatTeam(-1L, monsterTmpl.getName());
    for (int i = 0; i < count; i++) {
      monsterTeam.addMember(new Monster(monsterTmpl, monsterTmpl.getBaseLevel(), monsterSkills));
    }

    CombatTeam playerTeam = combatService.buildPlayerTeam(user);

    BattleContext context =
        BattleContext.builder()
            .teamA(playerTeam)
            .teamB(monsterTeam)
            .maxRounds(20)
            .scene(BattleContext.BattleScene.DUNGEON)
            .build();
    BattleResultVO battleResult = combatEngine.simulate(context);

    postCombatProcessor.applyHpToUser(user, playerTeam);
    userStateService.saveHpStatus(user);

    boolean playerWon = "Player".equals(battleResult.winner());

    if (!playerWon && playerTeam.aliveMembers().isEmpty()) {
      user.setDying(TimeUtil.now());
      userStateService.saveHpStatus(user);
      return new SimpleCombatOutcome(false, 0, monsterTmpl.getName(), "你被击败了，陷入了濒死状态。", List.of());
    }

    return new SimpleCombatOutcome(
        playerWon,
        0,
        monsterTmpl.getName(),
        playerWon ? ("击败了" + monsterTmpl.getName() + "！") : "被" + monsterTmpl.getName() + "击退了...",
        List.of());
  }

  /** 批量加载怪物模板配置的技能 */
  private List<Skill> loadMonsterSkills(MonsterTemplate tmpl) {
    if (tmpl.getSkills() == null || tmpl.getSkills().isEmpty()) return List.of();
    Map<Long, Skill> skillMap =
        skillRepository.findByIds(tmpl.getSkills()).stream()
            .collect(Collectors.toMap(Skill::getId, Function.identity()));
    return tmpl.getSkills().stream().map(skillMap::get).filter(Objects::nonNull).toList();
  }

  @SuppressWarnings("NullAway")
  public DungeonTemplate.MonsterEntry selectMonster(DungeonTemplate.Poi poi) {
    if (poi.monsterPool() == null || poi.monsterPool().isEmpty()) return null;
    return WeightedRandom.select(
        poi.monsterPool(), DungeonTemplate.MonsterEntry::weight, ThreadLocalRandom.current());
  }
}
