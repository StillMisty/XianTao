-- 秘境模板表
CREATE TABLE dungeon_template (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(64) NOT NULL,
    description     TEXT,
    map_node_id     BIGINT NOT NULL,
    element_type    VARCHAR(16),
    min_level       INT NOT NULL,
    max_level       INT NOT NULL,
    max_team_size   INT NOT NULL DEFAULT 3,
    timeout_hours   INT NOT NULL DEFAULT 4,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dungeon_template_map_node FOREIGN KEY (map_node_id) REFERENCES xt_map_node (id),
    CONSTRAINT chk_dungeon_element_type CHECK (element_type IN ('METAL', 'WOOD', 'WATER', 'FIRE', 'EARTH'))
);

COMMENT ON TABLE dungeon_template IS '秘境模板表';
COMMENT ON COLUMN dungeon_template.name IS '秘境名称，如"紫府秘境"';
COMMENT ON COLUMN dungeon_template.map_node_id IS '入口地图节点ID（类型为HIDDEN_ZONE）';
COMMENT ON COLUMN dungeon_template.element_type IS '秘境属性: METAL/WOOD/WATER/FIRE/EARTH';
COMMENT ON COLUMN dungeon_template.min_level IS '最低境界要求';
COMMENT ON COLUMN dungeon_template.max_level IS '最高境界要求';
COMMENT ON COLUMN dungeon_template.max_team_size IS '最大队伍人数';
COMMENT ON COLUMN dungeon_template.timeout_hours IS '超时时间（小时）';
COMMENT ON COLUMN dungeon_template.is_active IS '是否启用';

-- 秘境建筑配置表
CREATE TABLE dungeon_poi_config (
    id                  BIGSERIAL PRIMARY KEY,
    dungeon_id          BIGINT NOT NULL,
    area                VARCHAR(16) NOT NULL,
    name                VARCHAR(64) NOT NULL,
    poi_type            VARCHAR(16) NOT NULL
                        CHECK (poi_type IN ('GATHER', 'COMBAT', 'SEARCH', 'BOSS')),
    monster_pool        JSONB,
    loot_pool           JSONB NOT NULL,
    affinity_tags       JSONB,
    is_passage          BOOLEAN NOT NULL DEFAULT FALSE,
    unlock_condition    VARCHAR(64),
    is_one_time         BOOLEAN NOT NULL DEFAULT TRUE,
    exhausted_hours     INT,
    CONSTRAINT fk_dungeon_poi_dungeon FOREIGN KEY (dungeon_id) REFERENCES dungeon_template (id),
    CONSTRAINT chk_poi_area CHECK (area IN ('OUTER', 'INNER', 'CORE')),
    CONSTRAINT chk_poi_type CHECK (poi_type IN ('GATHER', 'COMBAT', 'SEARCH', 'BOSS')),
    CONSTRAINT uq_dungeon_poi UNIQUE (dungeon_id, area, name)
);

CREATE INDEX idx_poi_config_dungeon_id ON dungeon_poi_config (dungeon_id);

COMMENT ON TABLE dungeon_poi_config IS '秘境建筑配置表';
COMMENT ON COLUMN dungeon_poi_config.area IS '所在区域: OUTER=外围, INNER=内围, CORE=核心';
COMMENT ON COLUMN dungeon_poi_config.poi_type IS '建筑类型: GATHER=采集, COMBAT=战斗, SEARCH=搜索, BOSS=BOSS战';
COMMENT ON COLUMN dungeon_poi_config.monster_pool IS '怪物池 JSONB: [{monsterTemplateId, weight}]';
COMMENT ON COLUMN dungeon_poi_config.loot_pool IS '掉落池 JSONB: [{templateId, weight, minQty, maxQty}]';
COMMENT ON COLUMN dungeon_poi_config.affinity_tags IS '产出标签 JSONB: ["HERB", "WOOD"]';
COMMENT ON COLUMN dungeon_poi_config.is_passage IS '是否为通往下一区域的通道';
COMMENT ON COLUMN dungeon_poi_config.unlock_condition IS '解锁此POI的条件';
COMMENT ON COLUMN dungeon_poi_config.is_one_time IS '队伍中是否只能探索一次';
COMMENT ON COLUMN dungeon_poi_config.exhausted_hours IS '个人探索后冷却时间（NULL=不可重复）';

-- 秘境运行时实例表
CREATE TABLE dungeon_instance (
    id              BIGSERIAL PRIMARY KEY,
    dungeon_id      BIGINT NOT NULL,
    leader_id       BIGINT NOT NULL,
    team_id         BIGINT,
    current_area    VARCHAR(16) NOT NULL DEFAULT 'OUTER'
                    CHECK (current_area IN ('OUTER', 'INNER', 'CORE', 'COMPLETED')),
    passage_unlocked BOOLEAN NOT NULL DEFAULT FALSE,
    explored_pois   JSONB NOT NULL DEFAULT '[]'::jsonb,
    status          VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'COMPLETED', 'FAILED', 'ABANDONED')),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP NOT NULL,
    completed_at    TIMESTAMP,
    CONSTRAINT fk_dungeon_instance_dungeon FOREIGN KEY (dungeon_id) REFERENCES dungeon_template (id),
    CONSTRAINT fk_dungeon_instance_leader FOREIGN KEY (leader_id) REFERENCES xt_user (id),
    CONSTRAINT fk_dungeon_instance_team FOREIGN KEY (team_id) REFERENCES team (id)
);

CREATE INDEX idx_dungeon_instance_leader ON dungeon_instance (leader_id);
CREATE INDEX idx_dungeon_instance_team ON dungeon_instance (team_id);

COMMENT ON TABLE dungeon_instance IS '秘境运行时实例表';
COMMENT ON COLUMN dungeon_instance.leader_id IS '队长用户ID（单人时为自己）';
COMMENT ON COLUMN dungeon_instance.team_id IS '关联队伍ID（单人时为NULL）';
COMMENT ON COLUMN dungeon_instance.current_area IS '当前区域: OUTER/INNER/CORE/COMPLETED';
COMMENT ON COLUMN dungeon_instance.passage_unlocked IS '通往下一区域的通道是否已开启';
COMMENT ON COLUMN dungeon_instance.explored_pois IS '已探索的 poi_config_id 列表 JSONB';
COMMENT ON COLUMN dungeon_instance.status IS '实例状态: ACTIVE/COMPLETED/FAILED/ABANDONED';
COMMENT ON COLUMN dungeon_instance.expires_at IS '超时时间';

-- 秘境奖励记录表
CREATE TABLE dungeon_progress (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    dungeon_id      BIGINT NOT NULL,
    last_reward_date DATE NOT NULL,
    reward_count    INT NOT NULL DEFAULT 0,
    daily_limit     INT NOT NULL,
    first_clear     BOOLEAN NOT NULL DEFAULT FALSE,
    best_area       VARCHAR(16),
    CONSTRAINT fk_dungeon_progress_user FOREIGN KEY (user_id) REFERENCES xt_user (id),
    CONSTRAINT fk_dungeon_progress_dungeon FOREIGN KEY (dungeon_id) REFERENCES dungeon_template (id),
    CONSTRAINT chk_progress_area CHECK (best_area IN ('OUTER', 'INNER', 'CORE')),
    CONSTRAINT uq_dungeon_progress UNIQUE (user_id, dungeon_id)
);

CREATE INDEX idx_dungeon_progress_user ON dungeon_progress (user_id);

COMMENT ON TABLE dungeon_progress IS '秘境奖励记录表';
COMMENT ON COLUMN dungeon_progress.last_reward_date IS '最近领奖日期';
COMMENT ON COLUMN dungeon_progress.reward_count IS '今日已领奖次数';
COMMENT ON COLUMN dungeon_progress.daily_limit IS '当日通关奖励上限';
COMMENT ON COLUMN dungeon_progress.first_clear IS '是否首通';
COMMENT ON COLUMN dungeon_progress.best_area IS '历史最高到达区域';

-- 全服首通表
CREATE TABLE dungeon_first_clear (
    dungeon_id      BIGINT NOT NULL,
    team_members    JSONB NOT NULL,
    clear_time      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    duration_minutes INT,
    CONSTRAINT pk_dungeon_first_clear PRIMARY KEY (dungeon_id),
    CONSTRAINT fk_first_clear_dungeon FOREIGN KEY (dungeon_id) REFERENCES dungeon_template (id)
);

COMMENT ON TABLE dungeon_first_clear IS '全服首通记录表';
COMMENT ON COLUMN dungeon_first_clear.team_members IS '首通队伍成员 JSONB: [userId1, userId2, ...]';
COMMENT ON COLUMN dungeon_first_clear.duration_minutes IS '通关耗时（分钟）';
