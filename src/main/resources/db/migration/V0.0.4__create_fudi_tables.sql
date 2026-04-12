-- 福地系统核心表

-- 福地主表
CREATE TABLE xt_fudi (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE, -- 玩家ID（每个玩家只能有一个福地）
    
    -- 灵气系统
    aura_current INTEGER NOT NULL DEFAULT 0, -- 当前灵气值
    aura_max INTEGER NOT NULL DEFAULT 1000, -- 灵气上限
    core_level INTEGER NOT NULL DEFAULT 1, -- 聚灵核心等级
    
    -- 福地规模
    grid_size INTEGER NOT NULL DEFAULT 3, -- 网格大小（3/4/5）
    
    -- 地灵信息
    spirit_level INTEGER NOT NULL DEFAULT 1, -- 地灵等级
    mbti_type VARCHAR(4) NOT NULL, -- MBTI人格类型（如INTJ）
    spirit_stage INTEGER NOT NULL DEFAULT 1, -- 地灵形态阶段（1/2/3）
    spirit_energy INTEGER NOT NULL DEFAULT 100, -- 地灵精力值（0-100）
    spirit_affection INTEGER NOT NULL DEFAULT 0, -- 地灵好感度
    emotion_state VARCHAR(20) NOT NULL DEFAULT 'CALM', -- 情绪状态
    
    -- 管理模式
    auto_mode BOOLEAN NOT NULL DEFAULT TRUE, -- 是否开启自动管理
    dormant_mode BOOLEAN NOT NULL DEFAULT FALSE, -- 是否处于蛰伏模式
    
    -- 时间戳
    last_aura_update TIMESTAMP NOT NULL DEFAULT NOW(), -- 上次灵气计算时间
    last_online_time TIMESTAMP NOT NULL DEFAULT NOW(), -- 上次上线时间
    last_tribulation_time TIMESTAMP, -- 天劫最后发生时间
    tribulation_win_streak INTEGER NOT NULL DEFAULT 0, -- 天劫连续胜利次数
    
    -- JSONB字段
    grid_layout JSONB NOT NULL DEFAULT '{"grid_size": 3, "core_level": 1, "cells": []}'::jsonb, -- 福地网格布局
    spirit_config JSONB, -- 地灵配置（人格、表情、形态等）
    scorched_cells JSONB DEFAULT '[]'::jsonb, -- 焦土地块坐标列表
    
    -- 审计字段
    create_time TIMESTAMP NOT NULL DEFAULT NOW(),
    update_time TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_fudi_user FOREIGN KEY (user_id) REFERENCES xt_user(id) ON DELETE CASCADE,
    CONSTRAINT chk_grid_size CHECK (grid_size IN (3, 4, 5)),
    CONSTRAINT chk_aura_current CHECK (aura_current >= 0),
    CONSTRAINT chk_aura_max CHECK (aura_max > 0),
    CONSTRAINT chk_core_level CHECK (core_level >= 1),
    CONSTRAINT chk_spirit_energy CHECK (spirit_energy >= 0 AND spirit_energy <= 100)
);

-- 索引
CREATE INDEX idx_fudi_user_id ON xt_fudi(user_id);
CREATE INDEX idx_fudi_mbti_type ON xt_fudi(mbti_type);
CREATE INDEX idx_fudi_spirit_stage ON xt_fudi(spirit_stage);

-- JSONB字段GIN索引（支持高效查询）
CREATE INDEX idx_fudi_grid_layout ON xt_fudi USING GIN (grid_layout);
CREATE INDEX idx_fudi_spirit_config ON xt_fudi USING GIN (spirit_config);

-- 注释
COMMENT ON TABLE xt_fudi IS '福地系统核心表';
COMMENT ON COLUMN xt_fudi.id IS '福地唯一ID';
COMMENT ON COLUMN xt_fudi.user_id IS '所属玩家ID';
COMMENT ON COLUMN xt_fudi.aura_current IS '当前灵气值';
COMMENT ON COLUMN xt_fudi.aura_max IS '灵气上限（由聚灵核心等级决定）';
COMMENT ON COLUMN xt_fudi.core_level IS '聚灵核心等级';
COMMENT ON COLUMN xt_fudi.grid_size IS '福地网格大小（3/4/5）';
COMMENT ON COLUMN xt_fudi.spirit_level IS '地灵等级';
COMMENT ON COLUMN xt_fudi.mbti_type IS '地灵MBTI人格类型（锁定，不可更改）';
COMMENT ON COLUMN xt_fudi.spirit_stage IS '地灵形态阶段（1=初创之灵/2=底蕴之灵/3=化形之灵）';
COMMENT ON COLUMN xt_fudi.spirit_energy IS '地灵精力值（0-100，每天恢复100点）';
COMMENT ON COLUMN xt_fudi.spirit_affection IS '地灵好感度';
COMMENT ON COLUMN xt_fudi.emotion_state IS '地灵当前情绪状态';
COMMENT ON COLUMN xt_fudi.auto_mode IS '是否开启自动管理模式';
COMMENT ON COLUMN xt_fudi.dormant_mode IS '是否处于蛰伏模式（离线保底保护）';
COMMENT ON COLUMN xt_fudi.last_aura_update IS '上次灵气计算时间（用于懒加载）';
COMMENT ON COLUMN xt_fudi.last_online_time IS '上次上线时间（用于离线时长计算）';
COMMENT ON COLUMN xt_fudi.last_tribulation_time IS '天劫最后发生时间';
COMMENT ON COLUMN xt_fudi.tribulation_win_streak IS '天劫连续胜利次数';
COMMENT ON COLUMN xt_fudi.grid_layout IS '福地网格布局（JSONB存储）';
COMMENT ON COLUMN xt_fudi.spirit_config IS '地灵配置（JSONB存储人格、表情、形态等）';
COMMENT ON COLUMN xt_fudi.scorched_cells IS '焦土地块坐标列表（JSONB存储）';
