-- 地图节点表 (xt_map_node)
CREATE TABLE xt_map_node
(
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(128) NOT NULL,
    description         TEXT,
    map_type            VARCHAR(32)  NOT NULL,
    level_requirement   INT          NOT NULL DEFAULT 1,
    neighbors           JSONB                 DEFAULT '{}'::jsonb,
    specialties         JSONB                 DEFAULT '{}'::jsonb,
    travel_events       JSONB                 DEFAULT '{}'::jsonb,
    monster_encounters  JSONB                 DEFAULT '{}'::jsonb,
    create_time         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_map_node_name UNIQUE (name)
);

COMMENT ON TABLE xt_map_node IS '地图节点表';
COMMENT ON COLUMN xt_map_node.id IS '地图节点 ID';
COMMENT ON COLUMN xt_map_node.name IS '地图名称';
COMMENT ON COLUMN xt_map_node.description IS '地图描述';
COMMENT ON COLUMN xt_map_node.map_type IS '地图类型 (safe_town, training_zone, hidden_zone)';
COMMENT ON COLUMN xt_map_node.level_requirement IS '要求等级';
COMMENT ON COLUMN xt_map_node.neighbors IS '相邻地图及耗时 JSONB，格式: {"黑金主城": 5, "枯骨林": 10}';
COMMENT ON COLUMN xt_map_node.specialties IS '历练掉落池 JSONB，格式: {"1": 30, "2": 50} (templateId → weight)';
COMMENT ON COLUMN xt_map_node.travel_events IS '旅行事件权重 JSONB，格式: {"ambush": 40, "find_treasure": 10} (eventType → weight)';
COMMENT ON COLUMN xt_map_node.monster_encounters IS '遇怪池 JSONB: {"template_id": weight}';
COMMENT ON COLUMN xt_map_node.create_time IS '创建时间';
COMMENT ON COLUMN xt_map_node.update_time IS '更新时间';

-- 创建索引
CREATE INDEX idx_map_node_type ON xt_map_node (map_type);
CREATE INDEX idx_map_node_level ON xt_map_node (level_requirement);
