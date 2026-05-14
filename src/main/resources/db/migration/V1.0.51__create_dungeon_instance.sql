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
