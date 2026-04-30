package top.stillmisty.xiantao.domain.beast.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.PgJsonbTypeHandler;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("xt_beast")
public class Beast {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long userId;

    private Long fudiId;

    private Long templateId;

    private String beastName;

    private Integer tier;

    private String quality;

    private Boolean isMutant;

    @Column(typeHandler = PgJsonbTypeHandler.class)
    private List<String> mutationTraits;

    private Integer level;

    private Integer exp;

    private Integer attack;

    private Integer defense;

    private Integer maxHp;

    private Integer hpCurrent;

    @Column(typeHandler = PgJsonbTypeHandler.class)
    private List<Long> skills;

    private Boolean isDeployed;

    private LocalDateTime recoveryUntil;

    private Integer pennedCellId;

    private LocalDateTime birthTime;

    private Integer evolutionCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public boolean needsRecovery() {
        return hpCurrent != null && maxHp != null && hpCurrent < maxHp;
    }

    public boolean canFight() {
        return Boolean.TRUE.equals(isDeployed) && hpCurrent != null && hpCurrent > 0;
    }
}
