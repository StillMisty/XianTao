-- 玩家法决表 (player_skill)
CREATE
    TABLE
        player_skill(
            id BIGSERIAL PRIMARY KEY,
            user_id BIGINT NOT NULL REFERENCES player(id),
            skill_id BIGINT NOT NULL REFERENCES skill(id),
            is_equipped BOOLEAN NOT NULL DEFAULT FALSE,
            source_sect_id BIGINT,
            create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            UNIQUE(
                user_id,
                skill_id
            )
        );

COMMENT ON
TABLE
    player_skill IS '玩家获得的法决列表';

COMMENT ON
COLUMN player_skill.is_equipped IS '是否装载到技能槽位（最大3）';

COMMENT ON
COLUMN player_skill.source_sect_id IS '来源宗门ID，退宗时按此列删除共享功法';

CREATE
    INDEX idx_player_skill_source_sect ON
    player_skill(source_sect_id);

CREATE INDEX idx_player_skill_user_sect
    ON player_skill (user_id, source_sect_id);
