-- 秘境之灵好感度表
CREATE TABLE dungeon_spirit_state(
    id                BIGSERIAL PRIMARY KEY,
    instance_id       BIGINT NOT NULL REFERENCES dungeon_instance(id),
    dungeon_id        BIGINT NOT NULL REFERENCES dungeon_template(id),
    user_id           BIGINT NOT NULL REFERENCES xt_user(id),
    favor             INT NOT NULL DEFAULT 0,
    favor_log         JSONB,
    hidden_finds      JSONB NOT NULL DEFAULT '[]'::jsonb,
    triggered_events  JSONB NOT NULL DEFAULT '[]'::jsonb,
    CONSTRAINT uq_spirit_state_instance_user UNIQUE(instance_id, user_id)
);

CREATE INDEX idx_spirit_state_instance ON dungeon_spirit_state(instance_id);
CREATE INDEX idx_spirit_state_user ON dungeon_spirit_state(user_id);

COMMENT ON TABLE dungeon_spirit_state IS '秘境之灵好感度表';
COMMENT ON COLUMN dungeon_spirit_state.instance_id IS '所属秘境实例';
COMMENT ON COLUMN dungeon_spirit_state.favor IS '好感度数值';
COMMENT ON COLUMN dungeon_spirit_state.favor_log IS '好感度变更日志 JSONB: [string]';
COMMENT ON COLUMN dungeon_spirit_state.hidden_finds IS '已发现的隐藏 POI 名称列表 JSONB: [string]';
COMMENT ON COLUMN dungeon_spirit_state.triggered_events IS '已触发事件列表 JSONB: [string]';
