-- 秘境模板表
CREATE
    TABLE
        dungeon_template(
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(64) NOT NULL,
            description TEXT,
            map_node_id BIGINT NOT NULL,
            min_level INT NOT NULL,
            max_level INT NOT NULL,
            max_team_size INT NOT NULL DEFAULT 3,
            timeout_hours INT NOT NULL DEFAULT 4,
            is_active BOOLEAN NOT NULL DEFAULT TRUE,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_dungeon_template_map_node FOREIGN KEY(map_node_id) REFERENCES xt_map_node(id)
        );

CREATE INDEX idx_dungeon_template_active
    ON dungeon_template (is_active);

CREATE INDEX idx_dungeon_template_map_node
    ON dungeon_template (map_node_id);

COMMENT ON
TABLE
    dungeon_template IS '秘境模板表';

COMMENT ON
COLUMN dungeon_template.name IS '秘境名称，如"紫府秘境"';

COMMENT ON
COLUMN dungeon_template.map_node_id IS '入口地图节点ID（类型为HIDDEN_ZONE）';

COMMENT ON
COLUMN dungeon_template.min_level IS '最低境界要求';

COMMENT ON
COLUMN dungeon_template.max_level IS '最高境界要求';

COMMENT ON
COLUMN dungeon_template.max_team_size IS '最大队伍人数';

COMMENT ON
COLUMN dungeon_template.timeout_hours IS '超时时间（小时）';

COMMENT ON
COLUMN dungeon_template.is_active IS '是否启用';
