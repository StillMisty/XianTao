-- 队伍成员表
CREATE
    TABLE
        team_member(
            id BIGSERIAL PRIMARY KEY,
            team_id BIGINT NOT NULL,
            user_id BIGINT NOT NULL,
            joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_team_member_team FOREIGN KEY(team_id) REFERENCES team(id),
            CONSTRAINT fk_team_member_user FOREIGN KEY(user_id) REFERENCES xt_user(id),
            CONSTRAINT uq_team_member_user UNIQUE(user_id)
        );

CREATE
    INDEX idx_team_member_team_id ON
    team_member(team_id);

COMMENT ON
TABLE
    team_member IS '队伍成员表';

COMMENT ON
COLUMN team_member.team_id IS '队伍ID';

COMMENT ON
COLUMN team_member.user_id IS '成员用户ID（同一时间只能在一支队伍中）';
