-- 隐藏事件完成记录表 — 每人每个隐藏事件只触发一次
CREATE
    TABLE
        hidden_completion(
            id BIGSERIAL PRIMARY KEY,
            user_id BIGINT NOT NULL REFERENCES player(id),
            activity_type VARCHAR(32) NOT NULL,
            owner_id BIGINT NOT NULL,
            code VARCHAR(64) NOT NULL,
            completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            UNIQUE(
                user_id,
                activity_type,
                owner_id,
                code
            )
        );

CREATE
    INDEX idx_hidden_completion_user ON
    hidden_completion(user_id);

CREATE INDEX idx_hidden_completion_user_code
    ON hidden_completion (user_id, code);

CREATE INDEX idx_hidden_completion_lookup
    ON hidden_completion(activity_type, owner_id);

COMMENT ON
TABLE
    hidden_completion IS '隐藏事件完成记录表 — 保证每人每个隐藏事件只触发一次';

COMMENT ON
COLUMN hidden_completion.user_id IS '玩家 ID';

COMMENT ON
COLUMN hidden_completion.activity_type IS '活动类型';

COMMENT ON
COLUMN hidden_completion.owner_id IS '事件归属 ID';

COMMENT ON
COLUMN hidden_completion.code IS '事件 code';

COMMENT ON
COLUMN hidden_completion.completed_at IS '完成时间';
