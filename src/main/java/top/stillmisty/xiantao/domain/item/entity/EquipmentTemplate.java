package top.stillmisty.xiantao.domain.item.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.WeaponType;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbCollectionTypeHandler;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.PgJsonbTypeHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * 装备模板实体
 * 独立于 xt_item_template，存储装备专属属性
 */
@Data
@Table("xt_equipment_template")
public class EquipmentTemplate {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String name;

    private String description;

    @Column(typeHandler = JsonbCollectionTypeHandler.class)
    private Set<String> tags;

    private EquipmentSlot slot;

    private WeaponType weaponType;

    private String category;

    private Integer equipLevel;

    private Integer baseAttack;

    private Integer baseDefense;

    private Integer baseStr;

    private Integer baseCon;

    private Integer baseAgi;

    private Integer baseWis;

    private Double attackSpeed;

    private String attackRange;

    @Column(typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Integer> dropWeight;

    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "now()", onInsertValue = "now()")
    private LocalDateTime updateTime;

    public int getTotalDropWeight() {
        if (dropWeight == null || dropWeight.isEmpty()) return 0;
        return dropWeight.values().stream().mapToInt(Integer::intValue).sum();
    }
}
