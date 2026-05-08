-- 事件类型定义表 — 子事件 / 隐藏事件的 code 枚举
CREATE TABLE xt_event_type
(
    id              BIGSERIAL PRIMARY KEY,
    activity_type   VARCHAR(32)  NOT NULL,
    code            VARCHAR(64)  NOT NULL UNIQUE,
    name            VARCHAR(128) NOT NULL,
    description     TEXT,
    UNIQUE (activity_type, code)
);

COMMENT ON TABLE xt_event_type IS '事件类型定义表 — 所有子事件/隐藏事件的 code 注册表';
COMMENT ON COLUMN xt_event_type.activity_type IS '所属活动类型: TRAVEL / TRAINING / BOUNTY_SIDE';
COMMENT ON COLUMN xt_event_type.code IS '事件 code (唯一)';
COMMENT ON COLUMN xt_event_type.name IS '事件名称';
COMMENT ON COLUMN xt_event_type.description IS '事件描述';
