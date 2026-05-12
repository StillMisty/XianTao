package top.stillmisty.xiantao.service.combat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import top.stillmisty.xiantao.domain.monster.BattleContext;
import top.stillmisty.xiantao.domain.monster.Buff;
import top.stillmisty.xiantao.domain.monster.BuffManager;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.Team;
import top.stillmisty.xiantao.domain.monster.enums.BuffType;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.entity.SkillEffect;
import top.stillmisty.xiantao.domain.skill.enums.BindingType;
import top.stillmisty.xiantao.domain.skill.enums.EffectType;
import top.stillmisty.xiantao.domain.skill.enums.SkillType;

@DisplayName("DefaultCombatEngine")
class DefaultCombatEngineTest {

  private final DamageCalculator damageCalculator = new DamageCalculator();
  private final DefaultCombatEngine engine = new DefaultCombatEngine(damageCalculator);

  @Nested
  @DisplayName("buildTurnOrder")
  class BuildTurnOrder {

    @Test
    @DisplayName("sorts by effective speed descending")
    void sortsByEffectiveSpeed() {
      var fast = combatant(1, "fast", 100);
      var slow = combatant(2, "slow", 50);
      var mid = combatant(3, "mid", 75);
      var teamA = team("A", fast);
      var teamB = team("B", slow, mid);

      var order = engine.buildTurnOrder(teamA, teamB, new BuffManager());

      assertThat(order).hasSize(3);
      assertThat(order.get(0).getName()).isEqualTo("fast");
      assertThat(order.get(1).getName()).isEqualTo("mid");
      assertThat(order.get(2).getName()).isEqualTo("slow");
    }

    @Test
    @DisplayName("excludes dead combatants")
    void excludesDead() {
      var alive = combatant(1, "alive", 100);
      var dead = deadCombatant(2, "dead");
      var teamA = team("A", alive);
      var teamB = team("B", dead);

      var order = engine.buildTurnOrder(teamA, teamB, new BuffManager());

      assertThat(order).hasSize(1);
      assertThat(order.getFirst().getName()).isEqualTo("alive");
    }

    @Test
    @DisplayName("slow debuff reduces speed in ordering")
    void respectsSlowDebuff() {
      var normal = combatant(1, "normal", 100);
      var slowed = combatant(2, "slowed", 100);
      var teamA = team("A", normal, slowed);
      var teamB = team("B");

      var bm = new BuffManager();
      bm.addBuff(
          2L,
          Buff.builder().type(BuffType.SLOW).value(0.5).remainingTurns(3).source("test").build());

      var order = engine.buildTurnOrder(teamA, teamB, bm);

      assertThat(order.get(0).getName()).isEqualTo("normal");
      assertThat(order.get(1).getName()).isEqualTo("slowed");
    }
  }

  @Nested
  @DisplayName("selectSkill")
  class SelectSkill {

    @Test
    @DisplayName("selects an available skill")
    void selectsAvailableSkill() {
      var attacker = combatant(1, "attacker", 100);
      var skill = skill(1, "火球术", 3, List.of(skillEffect(EffectType.DAMAGE)));
      attacker.skills.add(skill);

      var result = engine.selectSkill(attacker, Map.of(), new BuffManager());

      assertThat(result).isNotNull();
      assertThat(result.getName()).isEqualTo("火球术");
    }

    @Test
    @DisplayName("returns null when no skills")
    void nullWhenNoSkills() {
      var attacker = combatant(1, "attacker", 100);
      assertThat(engine.selectSkill(attacker, Map.of(), new BuffManager())).isNull();
    }

    @Test
    @DisplayName("returns null when all skills on cooldown")
    void nullWhenAllOnCooldown() {
      var attacker = combatant(1, "attacker", 100);
      attacker.skills.add(skill(1, "火球术", 3, List.of()));

      var result = engine.selectSkill(attacker, Map.of("1:1", 2), new BuffManager());

      assertThat(result).isNull();
    }

    @Test
    @DisplayName("returns null when silenced")
    void nullWhenSilenced() {
      var attacker = combatant(1, "attacker", 100);
      attacker.skills.add(skill(1, "火球术", 3, List.of()));
      var bm = new BuffManager();
      bm.addBuff(
          1L,
          Buff.builder()
              .type(BuffType.SILENCE)
              .value(1.0)
              .remainingTurns(2)
              .source("test")
              .build());

      assertThat(engine.selectSkill(attacker, Map.of(), bm)).isNull();
    }
  }

  @Nested
  @DisplayName("tickCooldowns")
  class TickCooldowns {

    @Test
    @DisplayName("decrements cooldowns by 1")
    void decrements() {
      Map<String, Integer> cds = new LinkedHashMap<>();
      cds.put("a", 3);
      engine.tickCooldowns(cds);
      assertThat(cds).containsEntry("a", 2);
    }

    @Test
    @DisplayName("removes expired entries")
    void removesExpired() {
      Map<String, Integer> cds = new LinkedHashMap<>();
      cds.put("a", 1);
      engine.tickCooldowns(cds);
      assertThat(cds).isEmpty();
    }
  }

  @Nested
  @DisplayName("captureHp")
  class CaptureHp {

    @Test
    @DisplayName("captures HP of all team members")
    void capturesHp() {
      var c1 = combatant(1, "a", 50);
      c1.setHp(100);
      var c2 = combatant(2, "b", 50);
      c2.setHp(200);
      var t = team("T", c1, c2);

      Map<String, Integer> hp = engine.captureHp(t);

      assertThat(hp).containsEntry("member_1", 100).containsEntry("member_2", 200);
    }
  }

  @Nested
  @DisplayName("simulate — full combat")
  class Simulate {

    @Test
    @DisplayName("stronger combatant defeats weaker one")
    void strongerDefeatsWeaker() {
      var a = combatant(1, "attacker", 100, 100, 20, 500);
      var d = combatant(2, "defender", 30, 10, 10, 200);
      var teamA = team("A", a);
      var teamB = team("B", d);

      var result =
          engine.simulate(BattleContext.builder().teamA(teamA).teamB(teamB).maxRounds(20).build());

      assertThat(result.winner()).isEqualTo("A");
      assertThat(result.combatLog()).isNotEmpty();
    }

    @Test
    @DisplayName("draws after max rounds expires")
    void drawAtMaxRounds() {
      var a = combatant(1, "a", 50, 10, 10, 999);
      var b = combatant(2, "b", 50, 10, 10, 999);
      var teamA = team("A", a);
      var teamB = team("B", b);

      var result =
          engine.simulate(BattleContext.builder().teamA(teamA).teamB(teamB).maxRounds(3).build());

      assertThat(result.winner()).isEqualTo("DRAW");
      assertThat(result.rounds()).isEqualTo(3);
    }

    @Test
    @DisplayName("combat log contains entries for each action")
    void producesCombatLog() {
      var a = combatant(1, "attacker", 100, 30, 10, 500);
      var d = combatant(2, "defender", 30, 10, 10, 200);
      var teamA = team("A", a);
      var teamB = team("B", d);

      var result =
          engine.simulate(BattleContext.builder().teamA(teamA).teamB(teamB).maxRounds(10).build());

      assertThat(result.combatLog()).isNotEmpty();
      assertThat(result.damageDealt()).isNotEmpty();
    }

    @Test
    @DisplayName("captures HP changes in result")
    void capturesHpChange() {
      var a = combatant(1, "attacker", 100, 50, 20, 500);
      var d = combatant(2, "defender", 30, 10, 10, 300);
      var teamA = team("A", a);
      var teamB = team("B", d);

      var result =
          engine.simulate(BattleContext.builder().teamA(teamA).teamB(teamB).maxRounds(15).build());

      assertThat(result.playerHpChange()).containsKey("attacker");
      @SuppressWarnings("unchecked")
      Map<String, Integer> hp = (Map<String, Integer>) result.playerHpChange().get("attacker");
      assertThat(hp).containsKeys("before", "after");
    }

    @Test
    @DisplayName("damage skill produces skill log entries")
    void damageSkillLogged() {
      var skill = skill(1, "重击", 0, List.of(skillEffect(EffectType.DAMAGE)));
      var a = combatant(1, "attacker", 100, 100, 20, 500);
      a.skills.add(skill);
      var d = combatant(2, "defender", 30, 10, 10, 300);
      var teamA = team("A", a);
      var teamB = team("B", d);

      var result =
          engine.simulate(BattleContext.builder().teamA(teamA).teamB(teamB).maxRounds(10).build());

      assertThat(result.skillProcs()).isNotEmpty();
    }
  }

  // ===================== 测试辅助方法 =====================

  static TestCombatant combatant(long id, String name, int speed) {
    return new TestCombatant(id, name, speed, 10, 10, 200);
  }

  static TestCombatant combatant(long id, String name, int speed, int attack, int defense, int hp) {
    return new TestCombatant(id, name, speed, attack, defense, hp);
  }

  static TestCombatant deadCombatant(long id, String name) {
    var c = combatant(id, name, 50);
    c.setHp(0);
    return c;
  }

  static Team team(String name, Combatant... members) {
    var t = new Team(1L, name);
    for (var m : members) t.addMember(m);
    return t;
  }

  static Skill skill(long id, String name, int cooldown, List<SkillEffect> effects) {
    var s = new Skill();
    s.setId(id);
    s.setName(name);
    s.setCooldownSeconds(cooldown);
    s.setEffects(effects);
    s.setSkillType(SkillType.ACTIVE);
    s.setBindingType(BindingType.NONE);
    return s;
  }

  static SkillEffect skillEffect(EffectType type) {
    return new SkillEffect(type, "atk", null, null, null, 1.0, null, null);
  }

  static class TestCombatant implements Combatant {
    private final long id;
    private final String name;
    private final int speed;
    private final int attack;
    private final int defense;
    private int hp;
    private final int maxHp;
    final List<Skill> skills = new ArrayList<>();

    TestCombatant(long id, String name, int speed, int attack, int defense, int hp) {
      this.id = id;
      this.name = name;
      this.speed = speed;
      this.attack = attack;
      this.defense = defense;
      this.hp = hp;
      this.maxHp = hp;
    }

    void setHp(int hp) {
      this.hp = hp;
    }

    @Override
    public Long getId() {
      return id;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public int getSpeed() {
      return speed;
    }

    @Override
    public int getAttack() {
      return attack;
    }

    @Override
    public int getDefense() {
      return defense;
    }

    @Override
    public int getHp() {
      return hp;
    }

    @Override
    public int getMaxHp() {
      return maxHp;
    }

    @Override
    public void takeDamage(int amount) {
      hp = Math.max(0, hp - amount);
    }

    @Override
    public void heal(int amount) {
      hp = Math.min(maxHp, hp + Math.max(0, amount));
    }

    @Override
    public boolean isAlive() {
      return hp > 0;
    }

    @Override
    public List<Skill> getSkills() {
      return skills;
    }
  }
}
