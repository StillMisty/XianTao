package top.stillmisty.xiantao.domain.monster.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 历练统计信息
 * 记录历练过程中的战斗统计
 */
@Getter
@Builder
public class TrainingStatistics {

    /**
     * 遇敌场次
     */
    private final int totalEncounters;

    /**
     * 击杀怪物数量
     */
    private final int totalKills;

    /**
     * 战败场次
     */
    private final int defeatCount;

    /**
     * 总战斗回合数
     */
    private final int totalRounds;

    /**
     * 灵兽战斗统计
     */
    private final List<BeastBattleStats> beastStats;

    /**
     * 法决触发统计
     */
    private final Map<String, Integer> skillProcStats;

    /**
     * 高光战斗索引（-1表示无高光战斗）
     */
    @Builder.Default
    private final int highlightBattleIndex = -1;

    /**
     * 灵兽战斗统计
     */
    @Getter
    @Builder
    public static class BeastBattleStats {
        private final String beastName;
        private final int level;
        private final int kills;
        private final long expGained;
        private final int hpBefore;
        private final int hpAfter;
    }
}
