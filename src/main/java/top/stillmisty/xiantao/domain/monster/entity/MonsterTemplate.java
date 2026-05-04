package top.stillmisty.xiantao.domain.monster.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import top.stillmisty.xiantao.domain.monster.enums.MonsterType;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbCollectionTypeHandler;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.PgJsonbTypeHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Table("xt_monster_template")
public class MonsterTemplate {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String name;

    private MonsterType monsterType;

    private Integer baseLevel;

    private Integer baseHp;

    private Integer baseAttack;

    private Integer baseDefense;

    private Integer baseSpeed;

    @Column(typeHandler = JsonbCollectionTypeHandler.class)
    private List<Long> skills;

    @Column(typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> dropTable;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
