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
