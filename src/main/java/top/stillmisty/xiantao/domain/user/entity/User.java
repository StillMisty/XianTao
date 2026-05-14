package top.stillmisty.xiantao.domain.user.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.activerecord.Model;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.user.enums.CultivationRealm;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;

/** 游戏角色核心表实体 */
@EqualsAndHashCode(callSuper = true)
@Table("xt_user")
@Accessors(chain = true)
@Data
@NoArgsConstructor
public class User extends Model<User> {

  public static User create() {
    return new User();
  }

  /** 内部唯一角色 ID */
  @Id(keyType = KeyType.Auto)
  private Long id;

  /** 玩家道号 */
  private String nickname;

  /** 角色等级 */
  private Integer level;

  /** 当前经验值 */
  private Long exp;

  /** 货币 (灵石) */
  private Long spiritStones;

  /** 力道 */
  private Integer statStr;

  /** 根骨 */
  private Integer statCon;

  /** 身法 */
  private Integer statAgi;

  /** 悟性 */
  private Integer statWis;

  /** 当前生命值 */
  private Integer hpCurrent;

  /** 当前状态 */
  private UserStatus status;

  /** 当前所在地图/区域 ID */
  private Long locationId;

  /** 通用活动类型 (TRAVEL/TRAINING/BOUNTY) */
  private ActivityType activityType;

  /** 活动开始时间戳 */
  private LocalDateTime activityStartTime;

  /** 活动目标 ID (根据 activity_type 引用不同表: TRAVEL/TRAINING→map_id, BOUNTY→bounty_record_id) */
  private Long activityTargetId;

  /** 上次 HP 自然恢复时间 */
  private LocalDateTime lastHpRecoveryTime;

  /** 濒死开始时间 */
  private LocalDateTime dyingStartTime;

  /** 突破失败次数 (影响下一次突破成功率) */
  private Integer breakthroughFailCount;

  // ===================== 境界显示方法 =====================

  /** 获取境界显示名（如 "筑基 · 融合"） */
  public String realmDisplay() {
    return CultivationRealm.realmDisplay(level);
  }

  /** 获取当前大境界 */
  public CultivationRealm currentRealm() {
    return CultivationRealm.fromLevel(level);
  }

  /** 是否为GM（游戏管理员） */
  private Boolean gm;

  /** 创建时间 */
  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  /** 更新时间 */
  @Column(onUpdateValue = "now()", onInsertValue = "now()")
  private LocalDateTime updateTime;

  // ===================== 业务逻辑方法 =====================

  /** 计算最大生命值（基于体质） */
  public int calculateMaxHp() {
    int statCon = getStatCon();
    return 100 + statCon * 20;
  }

  /** 获取当前属性值（四维平均） */
  public int getStatValue() {
    return (getEffectiveStatStr()
            + getEffectiveStatCon()
            + getEffectiveStatAgi()
            + getEffectiveStatWis())
        / 4;
  }

  public int getEffectiveStatStr() {
    return (statStr != null ? statStr : 0) + (4 + (level != null ? level : 0));
  }

  public int getEffectiveStatCon() {
    return (statCon != null ? statCon : 0) + (4 + (level != null ? level : 0));
  }

  public int getEffectiveStatAgi() {
    return (statAgi != null ? statAgi : 0) + (4 + (level != null ? level : 0));
  }

  public int getEffectiveStatWis() {
    return (statWis != null ? statWis : 0) + (4 + (level != null ? level : 0));
  }

  /** 直接加属性值（丹药等来源，不加基础值） */
  public void addStatStr(int amount) {
    this.statStr = (this.statStr != null ? this.statStr : 0) + amount;
  }

  public void addStatCon(int amount) {
    this.statCon = (this.statCon != null ? this.statCon : 0) + amount;
  }

  public void addStatAgi(int amount) {
    this.statAgi = (this.statAgi != null ? this.statAgi : 0) + amount;
  }

  public void addStatWis(int amount) {
    this.statWis = (this.statWis != null ? this.statWis : 0) + amount;
  }

  /** 计算升级到下一级所需经验 */
  public long calculateExpToNextLevel() {
    return 100L * level * level;
  }

  /** 计算当前等级可存储的最大经验值 */
  public long calculateMaxExpStorage() {
    return calculateExpToNextLevel() * 5;
  }

  /** 计算突破成功率 范围：[0, 100] */
  public double calculateBreakthroughSuccessRate() {
    double baseMidpoint = 50;
    double baseSteepness = 4.5;
    double minPity = 3;
    double maxPityOffset = 17;

    double rawBase = 100.0 / (1.0 + Math.pow((double) level / baseMidpoint, baseSteepness));
    double rawPityGain = minPity + (maxPityOffset / (1.0 + Math.pow(level / 50.0, 2)));
    double totalProb = Math.clamp(rawBase + (breakthroughFailCount * rawPityGain), 0.0, 100.0);
    return BigDecimal.valueOf(totalProb).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  /** 消耗生命值（受伤） */
  public void takeDamage(int amount) {
    hpCurrent = Math.max(0, hpCurrent - amount);
  }

  /** 添加经验值（考虑存储上限） */
  public void addExp(long expToAdd) {
    long maxStorage = calculateMaxExpStorage();
    long currentStorage = exp - (level > 1 ? calculateExpToPrevLevel() : 0);
    long availableSpace = maxStorage - currentStorage;
    long actualAdd = Math.min(expToAdd, availableSpace);
    this.exp += actualAdd;
  }

  /** 计算从上一级到当前级所需经验 */
  private long calculateExpToPrevLevel() {
    if (level <= 1) return 0;
    return 100L * (level - 1) * (level - 1);
  }

  /** HP 自然恢复 空闲状态每5分钟恢复1%最大HP */
  public void naturalHpRecovery() {
    if (status != UserStatus.IDLE) return;
    int maxHp = calculateMaxHp();
    if (hpCurrent >= maxHp) return;
    int recoveryPerTick = Math.max(1, maxHp / 100);
    hpCurrent = Math.min(maxHp, hpCurrent + recoveryPerTick);
  }

  /** 设置为濒死状态 */
  public void setDying() {
    this.status = UserStatus.DYING;
    this.hpCurrent = 1;
  }

  /** 清除活动状态，回到空闲 */
  public void clearActivity() {
    this.activityType = null;
    this.activityStartTime = null;
    this.activityTargetId = null;
  }
}
