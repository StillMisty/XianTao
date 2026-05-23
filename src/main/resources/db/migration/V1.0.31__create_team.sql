-- 队伍表
CREATE
    TABLE
        team(
            id BIGSERIAL PRIMARY KEY,
            leader_id BIGINT NOT NULL,
            member_count INT NOT NULL DEFAULT 1,
            status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' CHECK(
                status IN(
                    'ACTIVE',
                    'DISBANDED'
                )
            ),
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_team_leader FOREIGN KEY(leader_id) REFERENCES xt_user(id)
        );

CREATE
    INDEX idx_team_leader_id ON
    team(leader_id);

CREATE INDEX idx_team_leader_status
    ON team (leader_id, status);

COMMENT ON
TABLE
    team IS '队伍表';

COMMENT ON
COLUMN team.leader_id IS '队长用户ID';

COMMENT ON
COLUMN team.member_count IS '队伍成员数量';

COMMENT ON
COLUMN team.status IS '队伍状态: ACTIVE=活跃, DISBANDED=已解散';
