package top.stillmisty.xiantao.domain.map.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.stillmisty.xiantao.domain.map.enums.MapType;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.PgJsonbTypeHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 地图节点实体
 */
@EqualsAndHashCode(callSuper = true)
@Table("xt_map_node")
@Data(staticConstructor = "create")
public class MapNode extends Model<MapNode> {

    /**
     * 地图 ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 地图名称
     */
    private String name;

    /**
     * 地图描述
     */
    private String description;

    /**
     * 地图类型
     */
    private MapType mapType;

    /**
     * 要求等级
     */
    private Integer levelRequirement;

    /**
     * 相邻地图 (JSONB)
     * 格式: {"黑金主城": 5, "枯骨林": 10}
     */
    @Column(typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Integer> neighbors;

    /**
     * 历练掉落池/特产 (JSONB)
     * 格式: [{"name": "毒龙草", "weight": 30}, {"name": "铁矿石", "weight": 50}]
     */
    @Column(typeHandler = PgJsonbTypeHandler.class)
    private List<Map<String, Object>> specialties;

    /**
     * 旅行事件权重 (JSONB)
     * 格式: [{"eventType": "ambush", "weight": 40}, {"eventType": "find_treasure", "weight": 10}]
     */
    @Column(typeHandler = PgJsonbTypeHandler.class)
    private List<Map<String, Object>> travelEvents;

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
     * 检查玩家等级是否满足要求
     */
    public boolean isAccessibleBy(int playerLevel) {
        if (levelRequirement == null) return true;
        return playerLevel >= levelRequirement;
    }

    /**
     * 获取到指定地图的旅行时间
     */
    public Integer getTravelTimeTo(String mapName) {
        if (neighbors == null) return null;
        return neighbors.get(mapName);
    }

    /**
     * 检查是否与指定地图相邻
     */
    public boolean isAdjacentTo(String mapName) {
        if (neighbors == null) return false;
        return neighbors.containsKey(mapName);
    }

    /**
     * 获取所有相邻地图名称
     */
    public List<String> getAdjacentMapNames() {
        if (neighbors == null) return List.of();
        return List.copyOf(neighbors.keySet());
    }

    /**
     * 从特产中随机获取一个
     */
    public Map<String, Object> getRandomSpecialty() {
        if (specialties == null || specialties.isEmpty()) return null;

        int totalWeight = specialties.stream()
                .mapToInt(item -> {
                    Object weightObj = item.get("weight");
                    if (weightObj == null) return 1;
                    return ((Number) weightObj).intValue();
                })
                .sum();

        if (totalWeight == 0) return specialties.getFirst();

        int random = (int) (Math.random() * totalWeight);
        int currentWeight = 0;

        for (Map<String, Object> specialty : specialties) {
            Object weightObj = specialty.get("weight");
            int weight = (weightObj == null) ? 1 : ((Number) weightObj).intValue();

            currentWeight += weight;
            if (random < currentWeight) {
                return specialty;
            }
        }

        return specialties.getLast();
    }
}
