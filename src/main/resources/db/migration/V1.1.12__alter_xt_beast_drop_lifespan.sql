-- Phase 4: 福地系统重设计 — 删除灵兽寿命字段
ALTER TABLE xt_beast
    DROP COLUMN IF EXISTS lifespan_days;
