-- NPC 对话历史统一表
DROP TABLE IF EXISTS xt_npc_dialogue;
CREATE TABLE xt_npc_dialogue (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES xt_user(id) ON DELETE CASCADE,
    npc_type      VARCHAR(32) NOT NULL,
    npc_id        BIGINT,
    role          VARCHAR(16) NOT NULL CHECK (role IN ('user', 'assistant', 'system')),
    content       TEXT NOT NULL,
    extra_data    JSONB DEFAULT '{}'::jsonb,
    create_time   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_npc_dialogue_lookup
    ON xt_npc_dialogue(user_id, npc_type, npc_id, create_time DESC);

COMMENT ON TABLE xt_npc_dialogue IS 'NPC 对话历史统一表';
COMMENT ON COLUMN xt_npc_dialogue.npc_type IS 'NPC 类型：FUDI_SPIRIT / SHOP_KEEPER / TRAVELER 等';
COMMENT ON COLUMN xt_npc_dialogue.npc_id IS 'NPC 实例 ID（如 shop_npc.id）';
COMMENT ON COLUMN xt_npc_dialogue.role IS '消息角色：user / assistant / system';
COMMENT ON COLUMN xt_npc_dialogue.extra_data IS '额外元数据，预留扩展';
