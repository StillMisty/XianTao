package top.stillmisty.xiantao.domain.land.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import top.stillmisty.xiantao.domain.land.enums.CellType;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.PgJsonbTypeHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 福地地块实体
 */
@Data
@Table("xt_fudi_cell")
public class FudiCell {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 关联福地ID
     */
    private Long fudiId;

    /**
     * 地块编号（从1开始）
     */
    private Integer cellId;

    /**
     * 地块类型：empty/farm/pen
     */
    private CellType cellType;

    /**
     * 地块等级（1-5）
     */
    private Integer cellLevel;

    /**
     * 建筑专有属性（JSONB）
     */
    @Column(typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> config;

    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "now()", onInsertValue = "now()")
    private LocalDateTime updateTime;

    // ===================== 业务方法 =====================

    /**
     * 创建空地块
     */
    public static FudiCell createEmpty(Long fudiId, Integer cellId) {
        FudiCell cell = new FudiCell();
        cell.setFudiId(fudiId);
        cell.setCellId(cellId);
        cell.setCellType(CellType.EMPTY);
        cell.setCellLevel(1);
        cell.setConfig(new HashMap<>());
        return cell;
    }

    /**
     * 是否为空地块
     */
    public boolean isEmpty() {
        return cellType == CellType.EMPTY;
    }

    /**
     * 获取配置值
     */
    public Object getConfigValue(String key) {
        return config != null ? config.get(key) : null;
    }

    /**
     * 获取配置值（带默认值）
     */
    public Object getConfigValue(String key, Object defaultValue) {
        if (config == null) return defaultValue;
        return config.getOrDefault(key, defaultValue);
    }

    /**
     * 设置配置值
     */
    public void setConfigValue(String key, Object value) {
        if (config == null) {
            config = new HashMap<>();
        }
        config.put(key, value);
    }

    /**
     * 获取整数配置
     */
    public Integer getIntConfig(String key) {
        Object value = getConfigValue(key);
        if (value instanceof Number n) return n.intValue();
        return null;
    }

    /**
     * 获取字符串配置
     */
    public String getStringConfig(String key) {
        Object value = getConfigValue(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取布尔配置
     */
    public Boolean getBoolConfig(String key) {
        Object value = getConfigValue(key);
        if (value instanceof Boolean b) return b;
        return null;
    }

    /**
     * 获取双精度配置
     */
    public Double getDoubleConfig(String key) {
        Object value = getConfigValue(key);
        if (value instanceof Number n) return n.doubleValue();
        return null;
    }
}
