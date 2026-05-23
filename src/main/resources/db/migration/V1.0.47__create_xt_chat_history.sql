/* 统一对话历史表（地灵/商铺/宗灵/旅行商人 共用） */
CREATE
    TABLE
        xt_chat_history(
            id BIGSERIAL PRIMARY KEY,
            chat_type VARCHAR(16) NOT NULL,
            conversation_id BIGINT,
            user_id BIGINT NOT NULL,
            ROLE VARCHAR(16) NOT NULL,
            content TEXT NOT NULL,
            extra_data JSONB DEFAULT '{}' ::jsonb,
            create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_chat_history_user FOREIGN KEY(user_id) REFERENCES xt_user(id) ON
            DELETE
                CASCADE,
                CONSTRAINT chk_chat_history_type CHECK(
                    chat_type IN(
                        'SPIRIT',
                        'SHOP',
                        'SECT',
                        'TRAVELER'
                    )
                ),
                CONSTRAINT chk_chat_history_role CHECK(
                    ROLE IN(
                        'user',
                        'assistant',
                        'system',
                        'tool'
                    )
                )
        );

COMMENT ON
TABLE
    xt_chat_history IS '统一对话历史表';

COMMENT ON
COLUMN xt_chat_history.id IS '消息ID';

COMMENT ON
COLUMN xt_chat_history.chat_type IS '对话类型：SPIRIT/SHOP/SECT/TRAVELER';

COMMENT ON
COLUMN xt_chat_history.conversation_id IS '对话对象ID（fudi_id / npc_id / sect_id）';

COMMENT ON
COLUMN xt_chat_history.user_id IS '用户ID';

COMMENT ON
COLUMN xt_chat_history.role IS '角色：user/assistant/system/tool';

COMMENT ON
COLUMN xt_chat_history.content IS '消息内容';

COMMENT ON
COLUMN xt_chat_history.extra_data IS '扩展数据（JSONB）';

COMMENT ON
COLUMN xt_chat_history.create_time IS '创建时间';

CREATE
    INDEX idx_chat_history_lookup ON
    xt_chat_history(
        chat_type,
        conversation_id,
        user_id,
        create_time DESC
    );
