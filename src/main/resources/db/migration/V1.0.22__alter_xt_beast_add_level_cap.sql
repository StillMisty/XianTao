-- 给xt_beast表添加level_cap字段
ALTER TABLE xt_beast ADD COLUMN level_cap INT NOT NULL DEFAULT 20;

-- 更新字段注释
COMMENT ON COLUMN xt_beast.level_cap IS '等级上限（tier × 10 + 10）';

-- 更新现有数据的level_cap
UPDATE xt_beast SET level_cap = tier * 10 + 10;