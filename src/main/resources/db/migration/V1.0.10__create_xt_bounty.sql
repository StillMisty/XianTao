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

    CONSTRAINT fk_bounty_map FOREIGN KEY (map_id) REFERENCES xt_map_node (id),
    CONSTRAINT chk_bounty_duration CHECK (duration_minutes > 0),
    CONSTRAINT chk_bounty_event_weight CHECK (event_weight >= 0)
);

COMMENT ON TABLE xt_bounty IS '悬赏任务配置表';
COMMENT ON COLUMN xt_bounty.map_id IS '所属地图 ID';
COMMENT ON COLUMN xt_bounty.name IS '悬赏名称';
COMMENT ON COLUMN xt_bounty.description IS '悬赏描述';
COMMENT ON COLUMN xt_bounty.duration_minutes IS '悬赏耗时（分钟）';
COMMENT ON COLUMN xt_bounty.rewards IS '奖励池 JSONB，格式: [{"name":"月光石","weight":50,"count":3},{"name":"竹剑","weight":30,"amount":1}]';
COMMENT ON COLUMN xt_bounty.require_level IS '最低接取等级';
COMMENT ON COLUMN xt_bounty.event_weight IS '旅行事件触发权重';

CREATE INDEX idx_bounty_map_id ON xt_bounty (map_id);
