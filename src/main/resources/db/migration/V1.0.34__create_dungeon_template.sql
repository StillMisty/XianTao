-- 秘境模板表
CREATE TABLE dungeon_template(
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(64) NOT NULL,
    description     TEXT,
    element_type    VARCHAR(16),
    min_level       INT NOT NULL,
    max_level       INT NOT NULL,
    max_team_size   INT NOT NULL DEFAULT 1,
    timeout_hours   INT NOT NULL DEFAULT 4,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    access_rules    JSONB,
    spirit_config   JSONB,
    area_configs    JSONB NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_dungeon_element CHECK(element_type IN('METAL', 'WOOD', 'WATER', 'FIRE', 'EARTH'))
);

CREATE INDEX idx_dungeon_template_active ON dungeon_template (is_active);

COMMENT ON TABLE dungeon_template IS '秘境模板表';
COMMENT ON COLUMN dungeon_template.name IS '秘境名称';
COMMENT ON COLUMN dungeon_template.element_type IS '五行属性: METAL/WOOD/WATER/FIRE/EARTH';
COMMENT ON COLUMN dungeon_template.min_level IS '最低境界要求';
COMMENT ON COLUMN dungeon_template.max_level IS '最高境界要求';
COMMENT ON COLUMN dungeon_template.max_team_size IS '最大队伍人数';
COMMENT ON COLUMN dungeon_template.timeout_hours IS '超时时间（小时）';
COMMENT ON COLUMN dungeon_template.access_rules IS '入口条件 JSONB';
COMMENT ON COLUMN dungeon_template.spirit_config IS '秘境之灵配置 JSONB（null=叙事者模式）';
COMMENT ON COLUMN dungeon_template.area_configs IS '区域+POI完整配置 JSONB';
