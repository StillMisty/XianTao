-- 用户悬赏记录表
CREATE TABLE xt_user_bounty
(
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    bounty_id        BIGINT       NOT NULL,
    bounty_name      VARCHAR(100) NOT NULL,
    start_time       TIMESTAMP    NOT NULL,
    duration_minutes INTEGER      NOT NULL,
    rewards          JSONB        NOT NULL DEFAULT '[]'::jsonb,
    status           VARCHAR(16)  NOT NULL DEFAULT 'active',
    create_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_bounty_user FOREIGN KEY (user_id) REFERENCES xt_user (id),
    CONSTRAINT fk_user_bounty_bounty FOREIGN KEY (bounty_id) REFERENCES xt_bounty (id)
);

COMMENT ON TABLE xt_user_bounty IS '用户悬赏记录表';
COMMENT ON COLUMN xt_user_bounty.user_id IS '用户 ID';
COMMENT ON COLUMN xt_user_bounty.bounty_id IS '悬赏 ID';
COMMENT ON COLUMN xt_user_bounty.bounty_name IS '悬赏名称（快照）';
COMMENT ON COLUMN xt_user_bounty.start_time IS '接取时间';
COMMENT ON COLUMN xt_user_bounty.duration_minutes IS '悬赏耗时（分钟）';
COMMENT ON COLUMN xt_user_bounty.rewards IS '预确定的奖励物品 JSONB，接取时由种子计算写入';
COMMENT ON COLUMN xt_user_bounty.status IS '状态: active / completed / abandoned';

CREATE INDEX idx_user_bounty_user_id ON xt_user_bounty (user_id);
CREATE INDEX idx_user_bounty_status ON xt_user_bounty (user_id, status);
