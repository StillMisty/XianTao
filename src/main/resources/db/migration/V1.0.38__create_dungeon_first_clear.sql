-- 全服首通表
CREATE
    TABLE
        dungeon_first_clear(
            dungeon_id BIGINT NOT NULL,
            team_members JSONB NOT NULL,
            clear_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            duration_minutes INT,
            CONSTRAINT pk_dungeon_first_clear PRIMARY KEY(dungeon_id),
            CONSTRAINT fk_first_clear_dungeon FOREIGN KEY(dungeon_id) REFERENCES dungeon_template(id)
        );

COMMENT ON
TABLE
    dungeon_first_clear IS '全服首通记录表';

COMMENT ON
COLUMN dungeon_first_clear.team_members IS '首通队伍成员 JSONB: [userId1, userId2, ...]';

COMMENT ON
COLUMN dungeon_first_clear.duration_minutes IS '通关耗时（分钟）';
