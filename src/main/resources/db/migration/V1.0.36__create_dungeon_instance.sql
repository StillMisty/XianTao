-- 秘境运行时实例表
CREATE TABLE dungeon_instance(
    id                BIGSERIAL PRIMARY KEY,
    dungeon_id        BIGINT NOT NULL REFERENCES dungeon_template(id),
    leader_id         BIGINT NOT NULL REFERENCES xt_user(id),
    current_area_key  VARCHAR(32) NOT NULL,
    passage_unlocked  BOOLEAN NOT NULL DEFAULT FALSE,
    explored_pois     JSONB NOT NULL DEFAULT '[]'::jsonb,
    status            VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
                      CHECK(status IN('ACTIVE', 'COMPLETED', 'FAILED', 'ABANDONED')),
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at        TIMESTAMP NOT NULL,
    completed_at      TIMESTAMP
);

CREATE UNIQUE INDEX uq_dungeon_instance_active ON dungeon_instance(leader_id, dungeon_id)
WHERE status = 'ACTIVE';

CREATE INDEX idx_dungeon_instance_leader ON dungeon_instance(leader_id);
CREATE INDEX idx_dungeon_instance_dungeon ON dungeon_instance(dungeon_id);

COMMENT ON TABLE dungeon_instance IS '秘境运行时实例表';
COMMENT ON COLUMN dungeon_instance.leader_id IS '队长用户ID';
COMMENT ON COLUMN dungeon_instance.current_area_key IS '当前区域 key，对应 area_configs[].key';
COMMENT ON COLUMN dungeon_instance.passage_unlocked IS '通道是否已解锁';
COMMENT ON COLUMN dungeon_instance.explored_pois IS '已探索POI名称列表 JSONB: [{poiName}]';
COMMENT ON COLUMN dungeon_instance.status IS '实例状态: ACTIVE/COMPLETED/FAILED/ABANDONED';
COMMENT ON COLUMN dungeon_instance.expires_at IS '超时时间';
