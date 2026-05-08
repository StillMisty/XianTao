-- 游戏事件表 — 存储玩家所有异步事件的叙事队列
CREATE TABLE xt_game_event
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES xt_user(id),
    category        VARCHAR(64)  NOT NULL,
    occurred_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered       BOOLEAN      NOT NULL DEFAULT FALSE,
    narrative_key   VARCHAR(128),
    narrative_args  JSONB        DEFAULT '{}'::jsonb,
    effects         JSONB        DEFAULT '{}'::jsonb
);

CREATE INDEX idx_game_event_undelivered ON xt_game_event(user_id, delivered)
    WHERE delivered = FALSE;
CREATE INDEX idx_game_event_occurred ON xt_game_event(user_id, occurred_at);

COMMENT ON TABLE xt_game_event IS '游戏事件表 — 存储所有异步事件，NotificationAppender 投递后标记已送达';
COMMENT ON COLUMN xt_game_event.user_id IS '玩家 ID';
COMMENT ON COLUMN xt_game_event.category IS '事件大类: TRAVEL_ARRIVED / TRAINING_COMPLETE / BOUNTY_COMPLETE / HP_RECOVERED / etc';
COMMENT ON COLUMN xt_game_event.occurred_at IS '事件发生时间';
COMMENT ON COLUMN xt_game_event.delivered IS '是否已投递给玩家 (FALSE=待投递)';
COMMENT ON COLUMN xt_game_event.narrative_key IS '叙事模板 key (用于格式化叙事文本)';
COMMENT ON COLUMN xt_game_event.narrative_args IS '叙事模板参数 JSONB';
COMMENT ON COLUMN xt_game_event.effects IS '事件效果 (exp/item 变动快照, 仅供调试)';
