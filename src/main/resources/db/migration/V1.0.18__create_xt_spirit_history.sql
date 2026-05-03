-- 地灵对话历史表
CREATE TABLE xt_spirit_history (
    id            BIGSERIAL PRIMARY KEY,
    fudi_id       BIGINT NOT NULL,
    role          VARCHAR(16) NOT NULL,
    content       TEXT NOT NULL,
    emotion_state VARCHAR(20),
    create_time   TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_spirit_history_fudi FOREIGN KEY (fudi_id) REFERENCES xt_fudi (id) ON DELETE CASCADE,
    CONSTRAINT chk_spirit_history_role CHECK (role IN ('user', 'assistant', 'system')),
    CONSTRAINT chk_spirit_history_emotion_state CHECK (emotion_state IS NULL OR emotion_state IN ('happy', 'calm', 'anxious', 'fatigued', 'angry', 'excited'))
);

CREATE INDEX idx_spirit_history_fudi_time ON xt_spirit_history (fudi_id, create_time DESC);

COMMENT ON TABLE xt_spirit_history IS '地灵对话历史表';
COMMENT ON COLUMN xt_spirit_history.fudi_id IS '关联福地ID';
COMMENT ON COLUMN xt_spirit_history.role IS '角色：user/assistant/system';
COMMENT ON COLUMN xt_spirit_history.content IS '对话内容';
COMMENT ON COLUMN xt_spirit_history.emotion_state IS '情绪状态';
