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

@Data
@Table("xt_user_bounty")
public class UserBounty {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long userId;

    private Long bountyId;

    private String bountyName;

    private LocalDateTime startTime;

    private Integer durationMinutes;

    @Column(typeHandler = PgJsonbTypeHandler.class)
    private List<Map<String, Object>> rewards;

    private String status;

    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "now()", onInsertValue = "now()")
    private LocalDateTime updateTime;

    public boolean isActive() {
        return "active".equals(status);
    }

    public boolean isCompleted() {
        return "completed".equals(status);
    }
}