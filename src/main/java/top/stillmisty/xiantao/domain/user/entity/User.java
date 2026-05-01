package top.stillmisty.xiantao.domain.user.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.stillmisty.xiantao.domain.user.enums.AttributeType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.PgJsonbTypeHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 游戏角色核心表实体
 */
@EqualsAndHashCode(callSuper = true)
@Table("xt_user")
@Accessors(chain = true)
@Data(staticConstructor = "create")
public class User extends Model<User> {

    /**
     * 内部唯一角色 ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 玩家道号
     */
    private String nickname;

    /**
     * 角色等级
     */
    private Integer level;

    /**
     * 当前经验值
     */
    private Long exp;

    /**
     * 货币 (灵石)
     */
    private Long spiritStones;

    /**
     * 力量属性 (影响破坏力/锻造)
     */
    private Integer statStr;

    /**
     * 体质属性 (影响生命值/物理防御)
     */
    private Integer statCon;

    /**
     * 敏捷属性 (影响出手顺序/杀怪效率)
     */
    private Integer statAgi;

    /**
     * 智慧属性 (影响掉宝率/经验加成/炼药)
     */
    private Integer statWis;

    /**
     * 剩余可分配属性点
     */
    private Integer freeStatPoints;

    /**
     * 当前生命值
     */
    private Integer hpCurrent;

    /**
     * 当前状态
     */
    private UserStatus status;

    /**
     * 当前所在地图/区域 ID
     */
    private Long locationId;

    /**
     * 历练开始时间戳 (用于结算收益)
     */
    private LocalDateTime trainingStartTime;

    /**
     * 旅行开始时间戳 (用于计算旅行进度)
     */
    @Column(onInsertValue = "null")
    private LocalDateTime travelStartTime;

    /**
     * 旅行目的地地图 ID
     */
    @Column(onInsertValue = "0")
    private Long travelDestinationId;

    /**
     * JSONB 扩展字段 (存储称号、成就、小规模系统数据)
     */
    @Column(typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> extraData;

    /**
     * 突破失败次数 (影响下一次突破成功率)
     */
    private Integer breakthroughFailCount;

    /**
     * 上次洗点时间戳 (用于洗点冷却判定，3天冷却)
     */
    private LocalDateTime lastResetPointsTime;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(onUpdateValue = "now()", onInsertValue = "now()")
    private LocalDateTime updateTime;

    // ===================== 业务逻辑方法 =====================

    /**
     * 计算最大生命值（基于体质）
     */
    public int calculateMaxHp() {
        // 基础100 + 体质 * 20
        return 100 + (statCon != null ? statCon : 5) * 20;
    }

    /**
     * 计算升级到下一级所需经验
     */
    public long calculateExpToNextLevel() {
        // 公式：100 * level^2
        return 100L * level * level;
    }

    /**
     * 计算当前等级可存储的最大经验值
     */
    public long calculateMaxExpStorage() {
        // 最多存储为当前等级升级经验的5倍
        return calculateExpToNextLevel() * 5;
    }

    /**
     * 计算突破成功率
     * 范围：[0, 100]
     */
    public double calculateBreakthroughSuccessRate() {
        double baseMidpoint = 50;   // 基础概率减半的等级 (50)
        double baseSteepness = 4.5;  // 基础概率下降的陡峭度
        double minPity = 3;        // 最小失败补偿 (3%)
        double maxPityOffset = 17;   // 额外最大补偿 (17% -> 总计20%)

        // 计算基础概率 (0-100)
        double rawBase = 100.0 / (1.0 + Math.pow((double) level / baseMidpoint, baseSteepness));

        // 计算单次补偿增量
        double rawPityGain = minPity + (maxPityOffset / (1.0 + Math.pow(level / 50.0, 2)));

        // 计算当前总概率并应用范围限制 [0, 100]
        double totalProb = Math.clamp(rawBase + (breakthroughFailCount * rawPityGain), 0.0, 100.0);

        // 舍入到两位小数
        return BigDecimal.valueOf(totalProb).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 消耗生命值（受伤）
     *
     * @param amount 伤害量
     * @return 实际伤害量
     */
    public int takeDamage(int amount) {
        int oldHp = hpCurrent;
        hpCurrent = Math.max(0, hpCurrent - amount);
        return oldHp - hpCurrent;
    }

    /**
     * 添加经验值（考虑存储上限）
     *
     * @param expToAdd 要添加的经验值
     * @return 实际添加的经验值
     */
    public long addExp(long expToAdd) {
        long maxStorage = calculateMaxExpStorage();
        long currentStorage = exp - (level > 1 ? calculateExpToPrevLevel() : 0);
        long availableSpace = maxStorage - currentStorage;

        long actualAdd = Math.min(expToAdd, availableSpace);
        this.exp += actualAdd;
        return actualAdd;
    }

    /**
     * 计算从上一级到当前级所需经验
     */
    private long calculateExpToPrevLevel() {
        if (level <= 1) return 0;
        return 100L * (level - 1) * (level - 1);
    }

    /**
     * 分配属性点
     *
     * @param statType 属性类型 (str, con, agi, wis)
     * @param points   点数
     * @return 是否分配成功
     */
    public boolean allocateStatPoints(AttributeType statType, int points) {
        if (freeStatPoints < points || points <= 0) {
            return false;
        }

        switch (statType) {
            case AttributeType.STR:
                statStr += points;
                break;
            case AttributeType.CON:
                statCon += points;
                break;
            case AttributeType.AGI:
                statAgi += points;
                break;
            case AttributeType.WIS:
                statWis += points;
                break;
        }

        freeStatPoints -= points;
        return true;
    }

    /**
     * 重置所有属性点（洗点）
     * 将所有已分配的属性点全部返还为可用属性点
     */
    public void resetAllStatPoints() {
        // 计算总已分配点数
        int totalAllocated = (statStr != null ? statStr : 5) +
                (statCon != null ? statCon : 5) +
                (statAgi != null ? statAgi : 5) +
                (statWis != null ? statWis : 5);

        // 扣除基础值（每个属性基础值为5）
        int baseStats = 5 * 4; // 四个属性，每个基础5点
        int allocatedPoints = totalAllocated - baseStats;

        // 返还到可用属性点
        freeStatPoints += allocatedPoints;

        // 重置为基础值
        statStr = 5;
        statCon = 5;
        statAgi = 5;
        statWis = 5;

        // 更新洗点时间
        lastResetPointsTime = LocalDateTime.now();
    }

    /**
     * 检查是否可以洗点（3天冷却）
     *
     * @return 是否可以洗点
     */
    public boolean canResetPoints() {
        if (lastResetPointsTime == null) {
            return true; // 从未洗过点，可以洗
        }

        LocalDateTime now = LocalDateTime.now();
        long hoursSinceLastReset = java.time.Duration.between(lastResetPointsTime, now).toHours();
        return hoursSinceLastReset >= 72; // 3天 = 72小时
    }

    /**
     * 获取下次可洗点的时间
     *
     * @return 下次可洗点时间
     */
    public LocalDateTime getNextResetTime() {
        if (lastResetPointsTime == null) {
            return LocalDateTime.now();
        }
        return lastResetPointsTime.plusHours(72);
    }

    /**
     * 获取洗点冷却剩余小时数
     *
     * @return 剩余小时数
     */
    public long getResetCooldownHoursRemaining() {
        if (lastResetPointsTime == null) {
            return 0;
        }

        LocalDateTime nextResetTime = getNextResetTime();
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(nextResetTime)) {
            return 0;
        }

        return java.time.Duration.between(now, nextResetTime).toHours();
    }

    /**
     * HP 自然恢复
     * 空闲状态每5分钟恢复1%最大HP
     */
    public void naturalHpRecovery() {
        if (status != UserStatus.IDLE) return;
        int maxHp = calculateMaxHp();
        if (hpCurrent >= maxHp) return;
        int recoveryPerTick = Math.max(1, maxHp / 100);
        hpCurrent = Math.min(maxHp, hpCurrent + recoveryPerTick);
    }

    /**
     * 设置为濒死状态
     */
    public void setDying() {
        this.status = UserStatus.DYING;
        this.hpCurrent = 1;
    }
}
