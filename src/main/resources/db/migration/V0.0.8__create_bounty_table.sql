-- 悬赏配置表
CREATE TABLE xt_bounty
(
    id               BIGSERIAL PRIMARY KEY,
    map_id           BIGINT       NOT NULL,
    name             VARCHAR(100) NOT NULL,
    description      TEXT,
    duration_minutes INTEGER      NOT NULL,
    rewards          JSONB        NOT NULL DEFAULT '[]'::jsonb,
    require_level    INTEGER      NOT NULL DEFAULT 1,
    event_weight     INTEGER      NOT NULL DEFAULT 0,
    create_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bounty_map FOREIGN KEY (map_id) REFERENCES xt_map_node (id)
);

COMMENT ON TABLE xt_bounty IS '悬赏任务配置表';
COMMENT ON COLUMN xt_bounty.map_id IS '所属地图 ID';
COMMENT ON COLUMN xt_bounty.name IS '悬赏名称';
COMMENT ON COLUMN xt_bounty.description IS '悬赏描述';
COMMENT ON COLUMN xt_bounty.duration_minutes IS '悬赏耗时（分钟）';
COMMENT ON COLUMN xt_bounty.rewards IS '奖励池 JSONB，格式: [{"type":"rare_item","weight":50,"count":3},{"type":"spirit_stones","weight":30,"amount":100},{"type":"beast_egg","weight":20}]';
COMMENT ON COLUMN xt_bounty.require_level IS '最低接取等级';
COMMENT ON COLUMN xt_bounty.event_weight IS '旅行事件触发权重';

CREATE INDEX idx_bounty_map_id ON xt_bounty (map_id);

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
