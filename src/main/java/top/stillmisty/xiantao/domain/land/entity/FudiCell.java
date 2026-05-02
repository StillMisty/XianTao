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
import java.util.List;
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

    /**
     * 获取累积产出物品列表
     * 格式：[{"template_id": 1, "name": "灵草", "quantity": 5}, ...]
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getProductionStored() {
        Object value = getConfigValue("production_stored");
        if (value instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return new java.util.ArrayList<>();
    }

    /**
     * 设置累积产出物品列表
     */
    public void setProductionStored(List<Map<String, Object>> productionStored) {
        setConfigValue("production_stored", productionStored);
    }

    /**
     * 添加物品到累积产出
     *
     * @param templateId 物品模板ID
     * @param name       物品名称
     * @param quantity   数量
     */
    public void addProductionItem(Long templateId, String name, int quantity) {
        List<Map<String, Object>> productionStored = getProductionStored();
        // 查找是否已有该物品
        boolean found = false;
        for (Map<String, Object> item : productionStored) {
            Object id = item.get("template_id");
            if (id instanceof Number n && n.longValue() == templateId) {
                // 增加数量
                Object currentQty = item.get("quantity");
                if (currentQty instanceof Number currentN) {
                    item.put("quantity", currentN.intValue() + quantity);
                }
                found = true;
                break;
            }
        }
        if (!found) {
            // 添加新物品
            Map<String, Object> newItem = new java.util.HashMap<>();
            newItem.put("template_id", templateId);
            newItem.put("name", name);
            newItem.put("quantity", quantity);
            productionStored.add(newItem);
        }
        setProductionStored(productionStored);
    }

    /**
     * 清空累积产出
     */
    public void clearProductionStored() {
        setProductionStored(new java.util.ArrayList<>());
    }

    /**
     * 获取累积产出总数量
     */
    public int getTotalProductionQuantity() {
        List<Map<String, Object>> productionStored = getProductionStored();
        int total = 0;
        for (Map<String, Object> item : productionStored) {
            Object qty = item.get("quantity");
            if (qty instanceof Number n) {
                total += n.intValue();
            }
        }
        return total;
    }
}
