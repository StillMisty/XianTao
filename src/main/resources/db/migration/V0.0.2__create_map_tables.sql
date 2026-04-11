-- ============================================================================
-- XianTao 地图系统数据库迁移脚本
-- 版本: V0.0.2
-- 说明: 包含地图节点、地图连接表，以及用户表扩展字段
-- ============================================================================

-- ============================================================================
-- 用户表扩展
-- ============================================================================

-- 添加旅行相关字段到 xt_user 表
ALTER TABLE xt_user ADD COLUMN travel_start_time TIMESTAMP;
ALTER TABLE xt_user ADD COLUMN travel_destination_id BIGINT;

COMMENT ON COLUMN xt_user.travel_start_time IS '旅行开始时间戳 (用于计算旅行进度)';
COMMENT ON COLUMN xt_user.travel_destination_id IS '旅行目的地地图 ID';

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

-- ============================================================================
-- 初始地图数据
-- ============================================================================

-- 插入初始地图节点数据
INSERT INTO xt_map_node (name, description, map_type, level_requirement, travel_time_minutes, neighbors, specialties, travel_events) VALUES
(
    '黑金主城',
    '仙道大陆最大的城市，繁华的商业中心，各路修仙者云集之地。',
    'safe_town',
    1,
    5,
    '{"幽暗沼泽": 5}'::jsonb,
    '[]'::jsonb,
    '[]'::jsonb
),
(
    '幽暗沼泽',
    '常年弥漫着毒雾的沼泽地，盛产珍稀毒草，但也潜伏着危险的沼泽生物。',
    'training_zone',
    5,
    5,
    '{"黑金主城": 5, "枯骨林": 10}'::jsonb,
    '[{"name": "毒龙草", "weight": 30, "templateId": 1}, {"name": "史莱姆粘液", "weight": 50, "templateId": 2}]'::jsonb,
    '[{"eventType": "ambush", "weight": 40}, {"eventType": "find_treasure", "weight": 10}, {"eventType": "weather", "weight": 50}]'::jsonb
),
(
    '枯骨林',
    '一片枯萎的森林，遍地白骨，传闻曾有强大的古兽陨落于此。',
    'training_zone',
    10,
    10,
    '{"幽暗沼泽": 10, "迷雾洞窟": 15}'::jsonb,
    '[{"name": "铁矿石", "weight": 50, "templateId": 3}, {"name": "骨片", "weight": 30, "templateId": 4}]'::jsonb,
    '[{"eventType": "ambush", "weight": 50}, {"eventType": "find_treasure", "weight": 15}, {"eventType": "weather", "weight": 35}]'::jsonb
),
(
    '迷雾洞窟',
    '神秘的地下洞窟，常年被浓雾笼罩，隐藏着不为人知的古代遗物。',
    'hidden_zone',
    15,
    15,
    '{"枯骨林": 15}'::jsonb,
    '[{"name": "秘银", "weight": 30, "templateId": 5}, {"name": "古代遗物", "weight": 10, "templateId": 6}]'::jsonb,
    '[{"eventType": "ambush", "weight": 60}, {"eventType": "find_treasure", "weight": 20}, {"eventType": "weather", "weight": 20}]'::jsonb
);

-- 插入地图连接数据
INSERT INTO xt_map_connection (from_map_id, to_map_id, travel_time_minutes, bidirectional) VALUES
-- 黑金主城 -> 幽暗沼泽
((SELECT id FROM xt_map_node WHERE name = '黑金主城'), (SELECT id FROM xt_map_node WHERE name = '幽暗沼泽'), 5, true),
-- 幽暗沼泽 -> 枯骨林
((SELECT id FROM xt_map_node WHERE name = '幽暗沼泽'), (SELECT id FROM xt_map_node WHERE name = '枯骨林'), 10, true),
-- 枯骨林 -> 迷雾洞窟
((SELECT id FROM xt_map_node WHERE name = '枯骨林'), (SELECT id FROM xt_map_node WHERE name = '迷雾洞窟'), 15, true);

-- ============================================================================
-- 更新现有用户的位置
-- ============================================================================

-- 将所有用户的 location_id 设置为黑金主城的 ID
UPDATE xt_user
SET location_id = (SELECT id FROM xt_map_node WHERE name = '黑金主城')
WHERE location_id = 0;

COMMENT ON COLUMN xt_user.location_id IS '当前所在地图 ID (默认为黑金主城)';
