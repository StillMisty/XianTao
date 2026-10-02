-- 数据采集基础设施：原始行为事件（append-only）+ 每日玩家快照
-- 方案与分析口径见 tools/analytics/README.md；事件字典见同文件。

CREATE TABLE analytics_event (
    id          BIGSERIAL PRIMARY KEY,
    occurred_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id     BIGINT,
    kind        VARCHAR(48) NOT NULL,
    subject     VARCHAR(96),
    value       BIGINT,
    payload     JSONB       NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX idx_analytics_event_kind_time ON analytics_event (kind, occurred_at);
CREATE INDEX idx_analytics_event_user_time ON analytics_event (user_id, occurred_at);

COMMENT ON TABLE analytics_event IS '分析用原始行为事件（运行时依赖为零，可清空重建）';

CREATE TABLE player_daily_snapshot (
    snapshot_date DATE   NOT NULL,
    user_id       BIGINT NOT NULL,
    level         INT    NOT NULL,
    exp           BIGINT NOT NULL,
    spirit_stones BIGINT NOT NULL,
    hp_current    INT,
    status        VARCHAR(16),
    location_id   BIGINT,
    PRIMARY KEY (snapshot_date, user_id)
);

CREATE INDEX idx_player_daily_snapshot_user ON player_daily_snapshot (user_id, snapshot_date);

COMMENT ON TABLE player_daily_snapshot IS '每日玩家快照（每小时 upsert，当日最后一次写入为该日终态）';
