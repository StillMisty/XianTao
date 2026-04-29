-- 福地主表
CREATE TABLE xt_fudi
(
    id                     BIGSERIAL PRIMARY KEY,
    user_id                BIGINT      NOT NULL UNIQUE,

    -- 灵气系统
    aura_current           INTEGER     NOT NULL DEFAULT 0,
    aura_max               INTEGER     NOT NULL DEFAULT 1000,
    tribulation_stage      INTEGER     NOT NULL DEFAULT 0,

    -- 管理模式
    auto_mode              BOOLEAN     NOT NULL DEFAULT TRUE,

    -- 时间戳
    last_aura_update       TIMESTAMP   NOT NULL DEFAULT NOW(),
    last_online_time       TIMESTAMP   NOT NULL DEFAULT NOW(),
    last_tribulation_time  TIMESTAMP,
    tribulation_win_streak INTEGER     NOT NULL DEFAULT 0,

    -- JSONB字段
    grid_layout            JSONB       NOT NULL DEFAULT '{
      "cells": []
    }'::jsonb,

    -- 审计字段
    create_time            TIMESTAMP   NOT NULL DEFAULT NOW(),
    update_time            TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_fudi_user FOREIGN KEY (user_id) REFERENCES xt_user (id) ON DELETE CASCADE,
    CONSTRAINT chk_aura_current CHECK (aura_current >= 0),
    CONSTRAINT chk_aura_max CHECK (aura_max > 0),
    CONSTRAINT chk_tribulation_stage CHECK (tribulation_stage >= 0)
);

-- 索引
CREATE INDEX idx_fudi_user_id ON xt_fudi (user_id);

-- JSONB字段GIN索引（支持高效查询）
CREATE INDEX idx_fudi_grid_layout ON xt_fudi USING GIN (grid_layout);

-- 注释
COMMENT ON TABLE xt_fudi IS '福地系统核心表';
COMMENT ON COLUMN xt_fudi.id IS '福地唯一ID';
COMMENT ON COLUMN xt_fudi.user_id IS '所属玩家ID';
COMMENT ON COLUMN xt_fudi.aura_current IS '当前灵气值';
COMMENT ON COLUMN xt_fudi.aura_max IS '灵气上限（由劫数和天劫胜利积累）';
COMMENT ON COLUMN xt_fudi.tribulation_stage IS '当前劫数（每渡过一次天劫+1）';
COMMENT ON COLUMN xt_fudi.auto_mode IS '是否开启自动管理模式';
COMMENT ON COLUMN xt_fudi.last_aura_update IS '上次灵气计算时间（用于懒加载）';
COMMENT ON COLUMN xt_fudi.last_online_time IS '上次上线时间（用于离线时长计算）';
COMMENT ON COLUMN xt_fudi.last_tribulation_time IS '天劫最后发生时间';
COMMENT ON COLUMN xt_fudi.tribulation_win_streak IS '天劫连续胜利次数';
COMMENT ON COLUMN xt_fudi.grid_layout IS '福地布局（JSONB存储，cell_id从1开始连续递增，empty保留槽位）';
