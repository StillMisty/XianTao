-- 世界事件表
DROP TABLE IF EXISTS world_event;
CREATE TABLE world_event (
    id                      BIGSERIAL PRIMARY KEY,
    category                VARCHAR(32) NOT NULL CHECK (category IN ('ECONOMIC', 'ENVIRONMENTAL', 'NARRATIVE', 'PARTICIPATORY')),
    scope                   VARCHAR(16) NOT NULL DEFAULT 'GLOBAL' CHECK (scope IN ('GLOBAL', 'REGIONAL')),
    region_map_node_id      BIGINT,
    title                   VARCHAR(128) NOT NULL,
    description             TEXT NOT NULL,
    status                  VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('UPCOMING', 'ACTIVE', 'ENDING', 'EXPIRED')),
    start_time              TIMESTAMP NOT NULL,
    end_time                TIMESTAMP NOT NULL,
    affected_tags           JSONB,
    global_multiplier       NUMERIC(4,2) NOT NULL DEFAULT 1.00,
    effects                 JSONB NOT NULL DEFAULT '[]',
    participation_enabled   BOOLEAN NOT NULL DEFAULT false,
    participation_limit     INT,
    participation_count     INT NOT NULL DEFAULT 0,
    participation_effects   JSONB,
    parent_event_id         BIGINT,
    chain_order             INT,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by              VARCHAR(64) DEFAULT 'SYSTEM',

    CONSTRAINT chk_world_event_count CHECK (participation_count >= 0),
    CONSTRAINT chk_world_event_chain_order CHECK (chain_order IS NULL OR chain_order > 0)
);

CREATE INDEX idx_world_event_active  ON world_event(start_time, end_time) WHERE status = 'ACTIVE';
CREATE INDEX idx_world_event_region  ON world_event(scope, region_map_node_id) WHERE scope = 'REGIONAL';
CREATE INDEX idx_world_event_chain   ON world_event(parent_event_id) WHERE parent_event_id IS NOT NULL;

COMMENT ON TABLE world_event IS '世界事件：影响经济、环境、叙事和玩家参与';
COMMENT ON COLUMN world_event.category IS '事件类别：ECONOMIC(经济) / ENVIRONMENTAL(环境) / NARRATIVE(叙事) / PARTICIPATORY(参与)';
COMMENT ON COLUMN world_event.scope IS '作用域：GLOBAL(全体) / REGIONAL(区域)';
COMMENT ON COLUMN world_event.region_map_node_id IS '区域事件绑定的地图节点';
COMMENT ON COLUMN world_event.status IS '状态：UPCOMING(预告) / ACTIVE(进行中) / ENDING(收尾) / EXPIRED(已过期)';
COMMENT ON COLUMN world_event.effects IS '效果配置 JSONB，与 xt_activity_event.params 同格式，由 SubEventEffectExecutor 执行';
COMMENT ON COLUMN world_event.participation_count IS '当前参与人数（原子更新）';
COMMENT ON COLUMN world_event.participation_effects IS '参与奖励效果 JSONB';
COMMENT ON COLUMN world_event.parent_event_id IS '父事件ID，用于事件链';
COMMENT ON COLUMN world_event.chain_order IS '事件链中的顺序';
