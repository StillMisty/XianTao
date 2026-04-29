package top.stillmisty.xiantao.domain.bounty.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.PgJsonbTypeHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 悬赏任务实体
 */
@Data
@Table("xt_bounty")
public class Bounty {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long mapId;

    private String name;

    private String description;

    private Integer durationMinutes;

    @Column(typeHandler = PgJsonbTypeHandler.class)
    private List<Map<String, Object>> rewards;

    private Integer requireLevel;

    private Integer eventWeight;

    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "now()", onInsertValue = "now()")
    private LocalDateTime updateTime;
}