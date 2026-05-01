-- 玩家法决表 (xt_player_skill)
CREATE TABLE xt_player_skill
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    NOT NULL REFERENCES xt_user (id),
    skill_id    BIGINT    NOT NULL REFERENCES xt_skill (id),
    is_equipped BOOLEAN   NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, skill_id)
);

COMMENT ON TABLE xt_player_skill IS '玩家获得的法决列表';
COMMENT ON COLUMN xt_player_skill.is_equipped IS '是否装载到技能槽位（最大3）';
