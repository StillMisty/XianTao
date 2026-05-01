package top.stillmisty.xiantao.domain.user.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
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
     * 体质属性值 = 4 + level
     */
    public int calculateMaxHp() {
        // 基础100 + 体质 * 20
        int statCon = 4 + level;
        return 100 + statCon * 20;
    }

    /**
     * 获取当前属性值（所有属性相同）
     * 属性值 = 4 + level
     */
    public int getStatValue() {
        return 4 + level;
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
