package top.stillmisty.xiantao.domain.land.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.stillmisty.xiantao.domain.land.enums.MutationTrait;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.PgJsonbTypeHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 福地核心实体
 */
@EqualsAndHashCode(callSuper = true)
@Table("xt_fudi")
@Accessors(chain = true)
@Data(staticConstructor = "create")
public class Fudi extends Model<Fudi> {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long userId;

    /**
     * 当前劫数（每渡过一次天劫+1，无上限）
     */
    private Integer tribulationStage;

    /**
     * 上次上线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 福地地块布局（JSONB存储）
     */
    @Column(typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> gridLayout;

    /**
     * 天劫最后发生时间
     */
    private LocalDateTime lastTribulationTime;

    /**
     * 天劫连续胜利次数
     */
    private Integer tribulationWinStreak;

    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "now()", onInsertValue = "now()")
    private LocalDateTime updateTime;

    // ===================== 业务逻辑方法 =====================

    /**
     * 获取已占地块数
     */
    public int getOccupiedCellCount() {
        if (gridLayout == null || !gridLayout.containsKey("cells")) {
            return 0;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cells = (List<Map<String, Object>>) gridLayout.get("cells");
        return (int) cells.stream()
                .filter(cell -> !"empty".equals(cell.get("type")))
                .count();
    }

    /**
     * 更新在线时间
     */
    public void touchOnlineTime() {
        lastOnlineTime = LocalDateTime.now();
    }

    /**
     * 计算福地防御力（用于天劫结算）
     * 公式：Σ(出战灵兽战力 × 护主乘数) + 玩家STR × 10 + 劫数 × 50
     *
     * @param playerStr 玩家力量值
     * @return 福地总防御力
     */
    @SuppressWarnings("unchecked")
    public int calculateTribulationDefense(int playerStr) {
        int defense = playerStr * 10 + (tribulationStage != null ? tribulationStage : 0) * 50;

        if (gridLayout == null || !gridLayout.containsKey("cells")) {
            return defense;
        }

        List<Map<String, Object>> cells = (List<Map<String, Object>>) gridLayout.get("cells");
        for (Map<String, Object> cell : cells) {
            String type = (String) cell.get("type");
            if ("pen".equals(type) && cell.containsKey("power_score")
                    && Boolean.TRUE.equals(cell.get("is_deployed"))) {
                double power = ((Number) cell.get("power_score")).doubleValue();
                // 护主特性：灵兽携带GUARDIAN变异时战力×1.5
                if (cell.containsKey("mutation_traits")) {
                    List<String> traits = (List<String>) cell.get("mutation_traits");
                    if (traits.contains(MutationTrait.GUARDIAN.getCode())) {
                        power *= 1.5;
                    }
                }
                defense += (int) power;
            }
        }

        return defense;
    }

    /**
     * 计算下次天劫触发时间
     */
    public LocalDateTime calculateNextTribulationTime() {
        if (lastTribulationTime == null) {
            return createTime.plusDays(7);
        }
        return lastTribulationTime.plusDays(7);
    }
}
