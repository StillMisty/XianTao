-- 福地事件模板表
CREATE TABLE fudi_event_template (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(64) NOT NULL,
    description       TEXT NOT NULL,
    effects           JSONB NOT NULL DEFAULT '[]',
    selection_weight  INT NOT NULL DEFAULT 100,
    created_by        VARCHAR(64) DEFAULT 'SYSTEM',
    created_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE fudi_event_template IS '福地事件模板池，会话时随机生成1-6个事件';
COMMENT ON COLUMN fudi_event_template.name IS '事件名称，如「下雨了」「灵蝶飞舞」';
COMMENT ON COLUMN fudi_event_template.description IS '事件描述，用于 LLM 提示和通知文本';
COMMENT ON COLUMN fudi_event_template.effects IS '事件效果配置 JSONB，空数组表示纯叙事无效果';
COMMENT ON COLUMN fudi_event_template.selection_weight IS '选取权重，越大越容易被选中';
