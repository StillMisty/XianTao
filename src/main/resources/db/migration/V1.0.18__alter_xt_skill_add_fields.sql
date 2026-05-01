-- 法决表添加新字段
ALTER TABLE xt_skill ADD COLUMN require_wis INTEGER;
ALTER TABLE xt_skill ADD COLUMN require_skill_id BIGINT;
ALTER TABLE xt_skill ADD COLUMN tags JSONB DEFAULT '[]';

-- 删除 power_multiplier 字段
ALTER TABLE xt_skill DROP COLUMN power_multiplier;

-- 添加注释
COMMENT ON COLUMN xt_skill.require_wis IS '智慧要求，NULL表示无要求';
COMMENT ON COLUMN xt_skill.require_skill_id IS '前置法决ID，NULL表示无前置';
COMMENT ON COLUMN xt_skill.tags IS '标签数组，JSONB格式';
