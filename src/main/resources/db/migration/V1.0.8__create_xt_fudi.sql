-- 福地主表
CREATE TABLE xt_fudi
(
    id                     BIGSERIAL PRIMARY KEY,
    user_id                BIGINT      NOT NULL UNIQUE,

    -- 劫数系统
    tribulation_stage      INTEGER     NOT NULL DEFAULT 0,

    -- 时间戳
    last_online_time       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_tribulation_time  TIMESTAMP,
    tribulation_win_streak INTEGER     NOT NULL DEFAULT 0,

    -- JSONB字段
    cell_layout            JSONB       NOT NULL DEFAULT '{
      "cells": []
    }'::jsonb,

    -- 审计字段
    create_time            TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time            TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_fudi_user FOREIGN KEY (user_id) REFERENCES xt_user (id) ON DELETE CASCADE,
    CONSTRAINT chk_tribulation_stage CHECK (tribulation_stage >= 0)
);

-- JSONB字段GIN索引（支持高效查询）
CREATE INDEX idx_fudi_cell_layout ON xt_fudi USING GIN (cell_layout);

-- 注释
COMMENT ON TABLE xt_fudi IS '福地系统核心表';
COMMENT ON COLUMN xt_fudi.id IS '福地唯一ID';
COMMENT ON COLUMN xt_fudi.user_id IS '所属玩家ID';
COMMENT ON COLUMN xt_fudi.tribulation_stage IS '当前劫数（每渡过一次天劫+1）';
COMMENT ON COLUMN xt_fudi.last_online_time IS '上次上线时间（用于离线时长计算）';
COMMENT ON COLUMN xt_fudi.last_tribulation_time IS '天劫最后发生时间';
COMMENT ON COLUMN xt_fudi.tribulation_win_streak IS '天劫连续胜利次数';
COMMENT ON COLUMN xt_fudi.cell_layout IS '福地地块布局（JSONB存储，cell_id从1开始连续递增）';
