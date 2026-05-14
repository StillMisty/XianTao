-- 队伍表
CREATE TABLE team (
    id              BIGSERIAL PRIMARY KEY,
    leader_id       BIGINT NOT NULL,
    member_count    INT NOT NULL DEFAULT 1,
    status          VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'DISBANDED')),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_team_leader FOREIGN KEY (leader_id) REFERENCES xt_user (id)
);

CREATE INDEX idx_team_leader_id ON team (leader_id);

COMMENT ON TABLE team IS '队伍表';
COMMENT ON COLUMN team.leader_id IS '队长用户ID';
COMMENT ON COLUMN team.member_count IS '队伍成员数量';
COMMENT ON COLUMN team.status IS '队伍状态: ACTIVE=活跃, DISBANDED=已解散';

-- 队伍成员表
CREATE TABLE team_member (
    id              BIGSERIAL PRIMARY KEY,
    team_id         BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    joined_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_team_member_team FOREIGN KEY (team_id) REFERENCES team (id),
    CONSTRAINT fk_team_member_user FOREIGN KEY (user_id) REFERENCES xt_user (id),
    CONSTRAINT uq_team_member_user UNIQUE (user_id)
);

CREATE INDEX idx_team_member_team_id ON team_member (team_id);

COMMENT ON TABLE team_member IS '队伍成员表';
COMMENT ON COLUMN team_member.team_id IS '队伍ID';
COMMENT ON COLUMN team_member.user_id IS '成员用户ID（同一时间只能在一支队伍中）';

-- 组队邀请表
CREATE TABLE team_invitation (
    id              BIGSERIAL PRIMARY KEY,
    team_id         BIGINT NOT NULL,
    inviter_id      BIGINT NOT NULL,
    invitee_id      BIGINT NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED')),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '5 minutes'),
    CONSTRAINT fk_team_invitation_team FOREIGN KEY (team_id) REFERENCES team (id),
    CONSTRAINT fk_team_invitation_inviter FOREIGN KEY (inviter_id) REFERENCES xt_user (id),
    CONSTRAINT fk_team_invitation_invitee FOREIGN KEY (invitee_id) REFERENCES xt_user (id)
);

CREATE INDEX idx_team_invitation_invitee ON team_invitation (invitee_id);

COMMENT ON TABLE team_invitation IS '组队邀请表';
COMMENT ON COLUMN team_invitation.team_id IS '队伍ID';
COMMENT ON COLUMN team_invitation.inviter_id IS '邀请人用户ID';
COMMENT ON COLUMN team_invitation.invitee_id IS '被邀请人用户ID';
COMMENT ON COLUMN team_invitation.status IS '邀请状态: PENDING=待处理, ACCEPTED=已接受, REJECTED=已拒绝, EXPIRED=已过期';
COMMENT ON COLUMN team_invitation.expires_at IS '过期时间（默认5分钟）';
