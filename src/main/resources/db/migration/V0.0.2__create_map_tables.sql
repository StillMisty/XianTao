-- ============================================================================
-- 地图系统
-- ============================================================================

-- 地图节点表 (xt_map_node)
CREATE TABLE xt_map_node (
    id                      BIGSERIAL PRIMARY KEY,
    name                    VARCHAR(128) NOT NULL,
    description             TEXT,
    map_type                VARCHAR(32)  NOT NULL,
    level_requirement       INT         NOT NULL DEFAULT 1,
    travel_time_minutes     INT         NOT NULL DEFAULT 5,
    neighbors               JSONB                DEFAULT '{}'::jsonb,
    specialties             JSONB                DEFAULT '[]'::jsonb,
    travel_events           JSONB                DEFAULT '[]'::jsonb,
    create_time             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_map_node_name UNIQUE (name)
);

COMMENT ON TABLE xt_map_node IS '地图节点表';
COMMENT ON COLUMN xt_map_node.id IS '地图节点 ID';
COMMENT ON COLUMN xt_map_node.name IS '地图名称';
COMMENT ON COLUMN xt_map_node.description IS '地图描述';
COMMENT ON COLUMN xt_map_node.map_type IS '地图类型 (safe_town, training_zone, hidden_zone)';
COMMENT ON COLUMN xt_map_node.level_requirement IS '推荐等级';
COMMENT ON COLUMN xt_map_node.travel_time_minutes IS '旅行耗时（分钟）';
COMMENT ON COLUMN xt_map_node.neighbors IS '相邻地图及耗时 JSONB，格式: {"黑金主城": 5, "枯骨林": 10}';
COMMENT ON COLUMN xt_map_node.specialties IS '历练掉落池 JSONB，格式: [{"name": "毒龙草", "weight": 30}]';
COMMENT ON COLUMN xt_map_node.travel_events IS '旅行事件权重 JSONB，格式: [{"eventType": "ambush", "weight": 40}]';
COMMENT ON COLUMN xt_map_node.create_time IS '创建时间';
COMMENT ON COLUMN xt_map_node.update_time IS '更新时间';

-- 创建索引
CREATE INDEX idx_map_node_type ON xt_map_node (map_type);
CREATE INDEX idx_map_node_level ON xt_map_node (level_requirement);
CREATE INDEX idx_map_node_neighbors ON xt_map_node USING GIN (neighbors);
CREATE INDEX idx_map_node_specialties ON xt_map_node USING GIN (specialties);
CREATE INDEX idx_map_node_travel_events ON xt_map_node USING GIN (travel_events);

-- 地图连接表 (xt_map_connection)
CREATE TABLE xt_map_connection (
    id                  BIGSERIAL PRIMARY KEY,
    from_map_id         BIGINT       NOT NULL,
    to_map_id           BIGINT       NOT NULL,
    travel_time_minutes INT          NOT NULL DEFAULT 5,
    bidirectional       BOOLEAN      NOT NULL DEFAULT true,
    create_time         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_connection_from_map FOREIGN KEY (from_map_id) REFERENCES xt_map_node (id) ON DELETE CASCADE,
    CONSTRAINT fk_connection_to_map FOREIGN KEY (to_map_id) REFERENCES xt_map_node (id) ON DELETE CASCADE
);

COMMENT ON TABLE xt_map_connection IS '地图连接表';
COMMENT ON COLUMN xt_map_connection.id IS '连接 ID';
COMMENT ON COLUMN xt_map_connection.from_map_id IS '起始地图 ID';
COMMENT ON COLUMN xt_map_connection.to_map_id IS '目标地图 ID';
COMMENT ON COLUMN xt_map_connection.travel_time_minutes IS '旅行耗时（分钟）';
COMMENT ON COLUMN xt_map_connection.bidirectional IS '是否双向连接';

-- 创建索引
CREATE INDEX idx_map_connection_from ON xt_map_connection (from_map_id);
CREATE INDEX idx_map_connection_to ON xt_map_connection (to_map_id);
CREATE INDEX idx_map_connection_from_to ON xt_map_connection (from_map_id, to_map_id);
