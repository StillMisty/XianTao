-- 删除地灵精力系统：移除 energy、last_energy_update 列及相关约束
ALTER TABLE xt_spirit
    DROP CONSTRAINT IF EXISTS chk_spirit_energy,
    DROP COLUMN IF EXISTS energy,
    DROP COLUMN IF EXISTS last_energy_update;
