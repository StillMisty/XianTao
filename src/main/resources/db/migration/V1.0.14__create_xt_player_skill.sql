-- 玩家法决表 (xt_player_skill)
CREATE TABLE xt_player_skill
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT    NOT NULL REFERENCES xt_user (id),
    skill_id        BIGINT    NOT NULL REFERENCES xt_skill (id),
    is_equipped     BOOLEAN   NOT NULL DEFAULT FALSE,
    source_sect_id  BIGINT,
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, skill_id)
);

COMMENT ON TABLE xt_player_skill IS '玩家获得的法决列表';
COMMENT ON COLUMN xt_player_skill.is_equipped IS '是否装载到技能槽位（最大3）';
COMMENT ON COLUMN xt_player_skill.source_sect_id IS '来源宗门ID，退宗时按此列删除共享功法';

CREATE INDEX idx_player_skill_source_sect ON xt_player_skill (source_sect_id);
