-- Phase 4: 福地系统重设计 — 删除灵气系统字段
ALTER TABLE xt_fudi
    DROP COLUMN IF EXISTS aura_current,
    DROP COLUMN IF EXISTS aura_max,
    DROP COLUMN IF EXISTS last_aura_update;

-- 删除相关约束
ALTER TABLE xt_fudi
    DROP CONSTRAINT IF EXISTS chk_aura_current,
    DROP CONSTRAINT IF EXISTS chk_aura_max;
