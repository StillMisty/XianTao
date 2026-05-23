-- 组队邀请表
CREATE
    TABLE
        team_invitation(
            id BIGSERIAL PRIMARY KEY,
            team_id BIGINT NOT NULL,
            inviter_id BIGINT NOT NULL,
            invitee_id BIGINT NOT NULL,
            status VARCHAR(16) NOT NULL DEFAULT 'PENDING' CHECK(
                status IN(
                    'PENDING',
                    'ACCEPTED',
                    'REJECTED',
                    'EXPIRED'
                )
            ),
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            expires_at TIMESTAMP NOT NULL DEFAULT(
                CURRENT_TIMESTAMP + INTERVAL '5 minutes'
            ),
            CONSTRAINT fk_team_invitation_team FOREIGN KEY(team_id) REFERENCES team(id),
            CONSTRAINT fk_team_invitation_inviter FOREIGN KEY(inviter_id) REFERENCES xt_user(id),
            CONSTRAINT fk_team_invitation_invitee FOREIGN KEY(invitee_id) REFERENCES xt_user(id)
        );

CREATE
    INDEX idx_team_invitation_invitee ON
    team_invitation(invitee_id);

COMMENT ON
TABLE
    team_invitation IS '组队邀请表';

COMMENT ON
COLUMN team_invitation.team_id IS '队伍ID';

COMMENT ON
COLUMN team_invitation.inviter_id IS '邀请人用户ID';

COMMENT ON
COLUMN team_invitation.invitee_id IS '被邀请人用户ID';

COMMENT ON
COLUMN team_invitation.status IS '邀请状态: PENDING=待处理, ACCEPTED=已接受, REJECTED=已拒绝, EXPIRED=已过期';

COMMENT ON
COLUMN team_invitation.expires_at IS '过期时间（默认5分钟）';
