-- 世界事件模板表
CREATE
    TABLE
        world_event_template(
            id BIGSERIAL PRIMARY KEY,
            category VARCHAR(32) NOT NULL CHECK(
                category IN(
                    'ECONOMIC',
                    'ENVIRONMENTAL',
                    'NARRATIVE',
                    'PARTICIPATORY'
                )
            ),
            SCOPE VARCHAR(16) NOT NULL DEFAULT 'GLOBAL' CHECK(
                SCOPE IN(
                    'GLOBAL',
                    'REGIONAL'
                )
            ),
            title VARCHAR(128) NOT NULL,
            description TEXT NOT NULL,
            cooldown_hours INT NOT NULL DEFAULT 24,
            selection_weight INT NOT NULL DEFAULT 100,
            duration_hours INT NOT NULL DEFAULT 6,
            affected_tags JSONB,
            global_multiplier NUMERIC(
                4,
                2
            ) DEFAULT 1.00,
            effects JSONB NOT NULL DEFAULT '[]',
            participation_enabled BOOLEAN NOT NULL DEFAULT FALSE,
            participation_limit INT,
            participation_effects JSONB,
            valid_region_tags JSONB,
            chained_template_id BIGINT REFERENCES world_event_template(id),
            created_by VARCHAR(64) DEFAULT 'SYSTEM',
            created_at TIMESTAMP NOT NULL DEFAULT NOW()
        );

CREATE
    INDEX idx_world_event_template_category ON
    world_event_template(category);

COMMENT ON
TABLE
    world_event_template IS '世界事件模板池，用于 AI 或定时任务随机选取生成事件';

COMMENT ON
COLUMN world_event_template.cooldown_hours IS '冷却时间（小时），同一模板再次可用前需等待的时间';

COMMENT ON
COLUMN world_event_template.selection_weight IS '选取权重，越大越容易被选中';

COMMENT ON
COLUMN world_event_template.duration_hours IS '事件持续时长（小时）';

COMMENT ON
COLUMN world_event_template.valid_region_tags IS '区域标签过滤，仅匹配有此标签的地图节点。如 ["forest", "mountain"]';

COMMENT ON
COLUMN world_event_template.chained_template_id IS '事件链：此模板事件结束后自动触发的下一个模板';

COMMENT ON
COLUMN world_event_template.created_by IS '创建来源：SYSTEM / AI';
