-- 迁移遇怪池格式
-- 旧格式: {"1": 50, "2": 30}
-- 新格式: {"1": {"weight": 50, "min": 1, "max": 3}, "2": {"weight": 30, "min": 1, "max": 2}}

-- 更新现有数据，将旧格式转换为新格式
UPDATE xt_map_node 
SET monster_encounters = (
    SELECT jsonb_object_agg(
        key,
        CASE 
            WHEN jsonb_typeof(value) = 'number' THEN 
                jsonb_build_object(
                    'weight', value::int,
                    'min', 1,
                    'max', 1
                )
            ELSE value
        END
    )
    FROM jsonb_each(monster_encounters)
)
WHERE monster_encounters IS NOT NULL 
AND monster_encounters != '{}'::jsonb;

-- 更新注释
COMMENT ON COLUMN xt_map_node.monster_encounters IS '遇怪池 JSONB: {"template_id": {"weight": 50, "min": 1, "max": 3}}';
