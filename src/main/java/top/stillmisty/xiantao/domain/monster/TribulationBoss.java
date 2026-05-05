package top.stillmisty.xiantao.domain.monster;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import top.stillmisty.xiantao.domain.skill.entity.Skill;

/**
 * 天劫化身 — 实现 Combatant 接口，接入现有战斗引擎
 *
 * <p>属性基于防守方队伍总战力动态缩放，天然支持未来多人组队。 劫数越高，Boss 属性倍率越高。
 */
public class TribulationBoss implements Combatant {

  private static final AtomicLong ID_GENERATOR = new AtomicLong(2_000_000_000L);

  private final long instanceId;
  private final String name;
  private final int tribulationStage;
  private final boolean compassionMode;
  private int hp;
  private final int maxHp;
  private final int attack;
  private final int defense;
  private final int speed;
  private final List<Skill> skills;

  /**
   * @param defendingTeamHp 防守方队伍总最大HP
   * @param defendingTeamAtk 防守方队伍平均攻击力
   * @param defendingTeamDef 防守方队伍平均防御力
   * @param defendingTeamSpd 防守方队伍平均速度
   * @param tribulationStage 当前劫数
   * @param compassionMode 是否触发怜悯（好感度≥800时boss削弱）
   */
  public TribulationBoss(
      int defendingTeamHp,
      int defendingTeamAtk,
      int defendingTeamDef,
      int defendingTeamSpd,
      int tribulationStage,
      boolean compassionMode) {
    this.instanceId = ID_GENERATOR.getAndIncrement();
    this.tribulationStage = tribulationStage;
    this.compassionMode = compassionMode;
    this.skills = List.of();

    double stagePower = tribulationStage;

    // BOSS属性 = 防守方属性 × 倍率，劫数越高倍率越高
    double hpMultiplier = 1.0 + stagePower * 0.5;
    double atkMultiplier = 0.6 + stagePower * 0.15;
    double defMultiplier = 0.4 + stagePower * 0.10;
    double spdMultiplier = 0.7 + stagePower * 0.08;

    // 怜悯模式：全属性降低30%
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

    String prefix = compassionMode ? "天劫化身·悯" : "天劫化身";
    this.name = prefix + " (第" + tribulationStage + "劫)";
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

  public int getTribulationStage() {
    return tribulationStage;
  }

  public boolean isCompassionMode() {
    return compassionMode;
  }
}
