-- 活动事件关联表 — 子事件/隐藏事件配置
CREATE TABLE xt_activity_event
(
    id              BIGSERIAL PRIMARY KEY,
    activity_type   VARCHAR(32)  NOT NULL CHECK (activity_type IN ('TRAVEL', 'TRAINING', 'BOUNTY_SIDE')),
    owner_id        BIGINT       NOT NULL,
    code            VARCHAR(64)  NOT NULL REFERENCES xt_event_type(code),
    weight          INT          NOT NULL DEFAULT 100 CHECK (weight >= 0),
    is_hidden       BOOLEAN      NOT NULL DEFAULT FALSE,
    trigger_type    VARCHAR(32),
    trigger_params  JSONB        DEFAULT '{}'::jsonb,
    params          JSONB        DEFAULT '{}'::jsonb,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_activity_event_lookup ON xt_activity_event(activity_type, owner_id);

COMMENT ON TABLE xt_activity_event IS '活动事件关联表 — 子事件/隐藏事件的权重和触发条件配置';
COMMENT ON COLUMN xt_activity_event.activity_type IS '所属活动: TRAVEL / TRAINING / BOUNTY_SIDE';
COMMENT ON COLUMN xt_activity_event.owner_id IS '事件归属: map_id(TRAVEL/TRAINING) 或 bounty_id(BOUNTY_SIDE)';
COMMENT ON COLUMN xt_activity_event.code IS '事件 code (引用 xt_event_type)';
COMMENT ON COLUMN xt_activity_event.weight IS '权重 (加权随机使用)';
COMMENT ON COLUMN xt_activity_event.is_hidden IS '是否为隐藏事件';
COMMENT ON COLUMN xt_activity_event.trigger_type IS '隐藏事件触发条件类型: HAS_SKILL / HAS_ITEM / STAT_THRESHOLD / etc';
COMMENT ON COLUMN xt_activity_event.trigger_params IS '隐藏事件触发条件参数 JSONB';
COMMENT ON COLUMN xt_activity_event.params IS '事件通用参数 JSONB';
