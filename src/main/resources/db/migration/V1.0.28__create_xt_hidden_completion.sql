-- 隐藏事件完成记录表 — 每人每个隐藏事件只触发一次
CREATE TABLE xt_hidden_completion
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES xt_user(id),
    activity_type   VARCHAR(32)  NOT NULL,
    owner_id        BIGINT       NOT NULL,
    code            VARCHAR(64)  NOT NULL,
    completed_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, activity_type, owner_id, code)
);

CREATE INDEX idx_hidden_completion_user ON xt_hidden_completion(user_id);

COMMENT ON TABLE xt_hidden_completion IS '隐藏事件完成记录表 — 保证每人每个隐藏事件只触发一次';
COMMENT ON COLUMN xt_hidden_completion.user_id IS '玩家 ID';
COMMENT ON COLUMN xt_hidden_completion.activity_type IS '活动类型';
COMMENT ON COLUMN xt_hidden_completion.owner_id IS '事件归属 ID';
COMMENT ON COLUMN xt_hidden_completion.code IS '事件 code';
COMMENT ON COLUMN xt_hidden_completion.completed_at IS '完成时间';
