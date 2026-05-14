-- 世界事件表
DROP TABLE IF EXISTS world_event;
CREATE TABLE world_event (
    id                BIGSERIAL PRIMARY KEY,
    title             VARCHAR(128) NOT NULL,
    description       TEXT NOT NULL,
    affected_tags     JSONB NOT NULL,
    global_multiplier NUMERIC(4,2) NOT NULL DEFAULT 1.03,
    affected_min      NUMERIC(4,2) NOT NULL DEFAULT 0.50,
    affected_max      NUMERIC(4,2) NOT NULL DEFAULT 2.00,
    start_time        TIMESTAMP NOT NULL,
    end_time          TIMESTAMP NOT NULL,
    created_by        VARCHAR(32) DEFAULT 'SYSTEM'
);

CREATE INDEX idx_world_event_active ON world_event(start_time, end_time);

COMMENT ON TABLE world_event IS 'AI 驱动的世界事件，影响物品价格和库存';
COMMENT ON COLUMN world_event.affected_tags IS '受影响的物品标签列表 JSONB，如 ["ore", "herb"]';
COMMENT ON COLUMN world_event.global_multiplier IS '全局物价系数，默认 1.03（轻微通胀）';
COMMENT ON COLUMN world_event.affected_min IS '受影响的物品最低价格浮动';
COMMENT ON COLUMN world_event.affected_max IS '受影响的物品最高价格浮动';
