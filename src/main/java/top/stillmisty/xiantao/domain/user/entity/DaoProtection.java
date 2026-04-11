package top.stillmisty.xiantao.domain.user.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 护道关系实体
 */
@EqualsAndHashCode(callSuper = true)
@Table("xt_dao_protection")
@Accessors(chain = true)
@Data(staticConstructor = "create")
public class DaoProtection extends Model<DaoProtection> {

    /**
     * 护道关系ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 护道者ID (提供加成的一方)
     */
    private Long protectorId;

    /**
     * 被护道者ID (突破的一方)
     */
    private Long protegeId;

    /**
     * 建立护道关系的时间
     */
    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(onUpdateValue = "now()", onInsertValue = "now()")
    private LocalDateTime updateTime;
}
