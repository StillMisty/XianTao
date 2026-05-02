package top.stillmisty.xiantao.domain.fudi.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.PgJsonbTypeHandler;

import java.util.Set;

/**
 * 地灵形态定义实体
 */
@EqualsAndHashCode(callSuper = true)
@Table("xt_spirit_form")
@Accessors(chain = true)
@Data(staticConstructor = "create")
public class SpiritForm extends Model<SpiritForm> {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String name;

    private String description;

    @Column(typeHandler = PgJsonbTypeHandler.class)
    private Set<String> likedTags;

    @Column(typeHandler = PgJsonbTypeHandler.class)
    private Set<String> dislikedTags;
}
