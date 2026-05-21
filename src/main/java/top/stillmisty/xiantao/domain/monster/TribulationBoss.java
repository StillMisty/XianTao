package top.stillmisty.xiantao.domain.monster;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.user.enums.TribulationType;

/**
 * 天劫化身 — 实现 Combatant 接口，接入现有战斗引擎
 *
 * <p>两种构造路径：
 *
 * <ul>
 *   <li>福地渡劫（现有）— 基于 tribulationStage 缩放
 *   <li>玩家突破雷劫（新增）— 基于目标境界序号 + 雷劫类型缩放，支持丹药/护道/保底削弱
 * </ul>
 */
public class TribulationBoss implements Combatant {

  private static final AtomicLong ID_GENERATOR = new AtomicLong(2_000_000_000L);

  private final long instanceId;
  private final String name;
  private final int maxHp;
  private final int attack;
  private final int defense;
  private final int speed;
  private final List<Skill> skills;
  private int hp;

  /** 福地渡劫构造（保持现有逻辑不变） */
  public TribulationBoss(
      int defendingTeamHp,
      int defendingTeamAtk,
      int defendingTeamDef,
      int defendingTeamSpd,
      int tribulationStage,
      boolean compassionMode) {
    this.instanceId = ID_GENERATOR.getAndIncrement();

    double hpMultiplier = 1.0 + (double) tribulationStage * 0.5;
    double atkMultiplier = 0.6 + (double) tribulationStage * 0.15;
    double defMultiplier = 0.4 + (double) tribulationStage * 0.10;
    double spdMultiplier = 0.7 + (double) tribulationStage * 0.08;

    if (compassionMode) {
      hpMultiplier *= 0.7;
      atkMultiplier *= 0.7;
      defMultiplier *= 0.7;
      spdMultiplier *= 0.7;
    }

    this.maxHp = (int) (defendingTeamHp * hpMultiplier);
    this.attack = (int) (defendingTeamAtk * atkMultiplier);
    this.defense = (int) (defendingTeamDef * defMultiplier);
    this.speed = (int) (defendingTeamSpd * spdMultiplier);
    this.hp = this.maxHp;
    this.skills = List.of();

    String prefix = compassionMode ? "天劫化身·悯" : "天劫化身";
    this.name = prefix + " (第" + tribulationStage + "劫)";
  }

  private TribulationBoss(
      String name, int maxHp, int attack, int defense, int speed, List<Skill> skills) {
    this.instanceId = ID_GENERATOR.getAndIncrement();
    this.name = name;
    this.maxHp = maxHp;
    this.hp = maxHp;
    this.attack = attack;
    this.defense = defense;
    this.speed = speed;
    this.skills = skills;
  }

  /**
   * 玩家突破雷劫工厂方法
   *
   * @param defendingTeamHp 防守方队伍总最大HP
   * @param defendingTeamAtk 防守方队伍平均攻击力
   * @param defendingTeamDef 防守方队伍平均防御力
   * @param defendingTeamSpd 防守方队伍平均速度
   * @param targetRealmOrdinal 目标大境界序号 (1=FOUNDATION, ..., 8=TRIBULATION)
   * @param type 雷劫类型
   * @param bossReduction 丹药+护道削弱系数 (0.0~0.5)
   * @param pityReduction 保底削弱系数 (0.0~0.5)
   * @param tribulationResist 雷劫抗性削弱系数 (0.0~1.0)
   * @param tribulationLevel 渡劫期内部层数（非渡劫期突破时传0）
   */
  public static TribulationBoss forPlayerBreakthrough(
      int defendingTeamHp,
      int defendingTeamAtk,
      int defendingTeamDef,
      int defendingTeamSpd,
      int targetRealmOrdinal,
      TribulationType type,
      double bossReduction,
      double pityReduction,
      double tribulationResist,
      int tribulationLevel) {
    double hpMultiplier = (1.5 + targetRealmOrdinal * 0.5) * type.getDifficultyMultiplier();
    double atkMultiplier = (0.8 + targetRealmOrdinal * 0.3) * type.getDifficultyMultiplier();
    double defMultiplier = (0.6 + targetRealmOrdinal * 0.25) * type.getDifficultyMultiplier();
    double spdMultiplier = (0.7 + targetRealmOrdinal * 0.2) * type.getDifficultyMultiplier();

    // 渡劫期每级额外难度
    if (tribulationLevel > 0) {
      double levelMultiplier = 0.5 + tribulationLevel * 0.5;
      hpMultiplier *= levelMultiplier;
      atkMultiplier *= levelMultiplier;
      defMultiplier *= levelMultiplier * 0.7;
      spdMultiplier *= levelMultiplier * 0.7;
    }

    // 应用削弱
    double combinedReduction = 1.0 - bossReduction;
    combinedReduction *= (1.0 - pityReduction);
    combinedReduction *= Math.max(0.1, 1.0 - tribulationResist);
    hpMultiplier *= combinedReduction;
    atkMultiplier *= combinedReduction;
    defMultiplier *= combinedReduction;
    spdMultiplier *= combinedReduction;

    int maxHp = (int) (defendingTeamHp * hpMultiplier);
    int attack = (int) (defendingTeamAtk * atkMultiplier);
    int defense = (int) (defendingTeamDef * defMultiplier);
    int speed = (int) (defendingTeamSpd * spdMultiplier);

    String name = "天劫化身 · " + type.getDisplayName();
    if (tribulationLevel > 0) {
      String levelName =
          tribulationLevel <= 10
              ? List.of("一", "二", "三", "四", "五", "六", "七", "八", "九", "飞升").get(tribulationLevel - 1)
                  + "劫"
              : "第" + tribulationLevel + "劫";
      name += " (" + levelName + ")";
    } else {
      name += " (" + targetRealmOrdinal + "阶劫)";
    }

    return new TribulationBoss(name, maxHp, attack, defense, speed, type.buildSkills());
  }

  @Override
  public Long getId() {
    return instanceId;
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
    hp = Math.min(maxHp, hp + amount);
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
