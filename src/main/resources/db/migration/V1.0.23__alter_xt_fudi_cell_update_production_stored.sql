-- 修改xt_fudi_cell表的config字段中的production_stored格式
-- 从简单计数改为物品列表格式
-- 注意：这是一个数据迁移，需要更新现有数据

-- 首先，更新现有production_stored为整数的数据，将其转换为物品列表格式
-- 假设现有数据中production_stored是整数，表示累积的产出数量
-- 我们需要将其转换为物品列表格式

-- 更新farm类型地块的production_stored
UPDATE xt_fudi_cell 
SET config = jsonb_set(
    config, 
    '{production_stored}', 
    CASE 
        WHEN config->>'production_stored' IS NOT NULL AND config->>'production_stored' ~ '^[0-9]+$' THEN
            -- 如果production_stored是数字，转换为空数组（因为不知道具体物品）
            '[]'::jsonb
        ELSE
            -- 如果已经是数组或为空，保持不变
            COALESCE(config->'production_stored', '[]'::jsonb)
    END
)
WHERE cell_type = 'farm' AND config ? 'production_stored';

-- 更新pen类型地块的production_stored
UPDATE xt_fudi_cell 
SET config = jsonb_set(
    config, 
    '{production_stored}', 
    CASE 
        WHEN config->>'production_stored' IS NOT NULL AND config->>'production_stored' ~ '^[0-9]+$' THEN
            -- 如果production_stored是数字，转换为空数组（因为不知道具体物品）
            '[]'::jsonb
        ELSE
            -- 如果已经是数组或为空，保持不变
            COALESCE(config->'production_stored', '[]'::jsonb)
    END
)
WHERE cell_type = 'pen' AND config ? 'production_stored';

-- 添加注释说明新的production_stored格式
COMMENT ON COLUMN xt_fudi_cell.config IS '建筑专有属性（JSONB）：
  farm: {"crop_name":"灵草","planted_at":"...","growth_hours":24,"production_stored":[{"template_id":1,"name":"灵草","quantity":5}]}
  pen: {"beast_id":1,"production_stored":[{"template_id":1,"name":"灵草","quantity":5}]}';