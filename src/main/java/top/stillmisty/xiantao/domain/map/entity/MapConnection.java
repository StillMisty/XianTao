package top.stillmisty.xiantao.domain.map.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 地图连接实体
 */
@EqualsAndHashCode(callSuper = true)
@Table("xt_map_connection")
@Data(staticConstructor = "create")
public class MapConnection extends Model<MapConnection> {

    /**
     * 连接 ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 起始地图 ID
     */
    private Long fromMapId;

    /**
     * 目标地图 ID
     */
    private Long toMapId;

    /**
     * 旅行耗时（分钟）
     */
    private Integer travelTimeMinutes;

    /**
     * 是否双向连接
     */
    private Boolean bidirectional;

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
}
