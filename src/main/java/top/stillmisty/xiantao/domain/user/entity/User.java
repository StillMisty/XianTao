package top.stillmisty.xiantao.domain.user.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;

import java.time.LocalDateTime;

/**
 * 游戏角色核心表实体
 */
@Data
@Table("xt_user")
public class User {

    /**
     * 内部唯一角色 ID (雪花算法)
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
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
     * 基础货币 (铜币)
     */
    private Long coins;

    /**
     * 高级货币 (灵石/用于黑市)
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
    private String locationId;

    /**
     * 挂机开始时间戳 (用于结算收益)
     */
    private LocalDateTime afkStartTime;

    /**
     * JSONB 扩展字段 (存储称号、成就、小规模系统数据)
     */
    private Object extraData;

    /**
     * 角色创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后一次数据更新时间
     */
    private LocalDateTime updateTime;
}
