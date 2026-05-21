-- 地图节点表 (xt_map_node)
CREATE TABLE xt_map_node
(
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(128) NOT NULL,
    description         TEXT,
    map_type            VARCHAR(32)  NOT NULL,
    level_requirement   INT          NOT NULL DEFAULT 1,
    neighbors           JSONB                 DEFAULT '[]'::jsonb,
    specialties         JSONB                 DEFAULT '[]'::jsonb,
    encounter_richness  INT           NOT NULL DEFAULT 5 CHECK (encounter_richness BETWEEN 1 AND 10),
    create_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE xt_map_node IS '地图节点表';
COMMENT ON COLUMN xt_map_node.name IS '地图名称';
COMMENT ON COLUMN xt_map_node.description IS '地图描述';
COMMENT ON COLUMN xt_map_node.map_type IS '地图类型: [SAFE_TOWN, TRAINING_ZONE, HIDDEN_ZONE]';
COMMENT ON COLUMN xt_map_node.level_requirement IS '推荐等级';
COMMENT ON COLUMN xt_map_node.neighbors IS '相邻地图 [{"targetId": 1, "minutes": 5}]';
COMMENT ON COLUMN xt_map_node.specialties IS '历练掉落池/特产 [{"templateId": 1, "weight": 30}]';
COMMENT ON COLUMN xt_map_node.encounter_richness IS '事件密集度 1-10, 默认5。控制遇怪与事件的触发频率。10=频发, 1=极少';
COMMENT ON COLUMN xt_map_node.create_time IS '创建时间';
COMMENT ON COLUMN xt_map_node.update_time IS '更新时间';


-- 创建索引
CREATE INDEX idx_map_node_type ON xt_map_node (map_type);
CREATE INDEX idx_map_node_level ON xt_map_node (level_requirement);
