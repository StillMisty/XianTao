/* 宗门成员表 */
CREATE TABLE xt_sect_member
(
    id             BIGSERIAL PRIMARY KEY,
    sect_id        BIGINT,
    user_id        BIGINT      NOT NULL,
    position       VARCHAR(16) NOT NULL DEFAULT 'MEMBER',
    contribution   INT         NOT NULL DEFAULT 0,
    joined_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cooldown_until TIMESTAMP,
    CONSTRAINT fk_sect_member_sect FOREIGN KEY (sect_id) REFERENCES xt_sect (id) ON DELETE CASCADE,
    CONSTRAINT fk_sect_member_user FOREIGN KEY (user_id) REFERENCES xt_user (id) ON DELETE CASCADE,
    CONSTRAINT uq_sect_member_user UNIQUE (user_id),
    CONSTRAINT chk_sect_member_position CHECK (position IN ('LEADER', 'ELDER', 'MEMBER')),
    CONSTRAINT chk_sect_member_contribution CHECK (contribution >= 0)
);

COMMENT ON TABLE xt_sect_member IS '宗门成员表';
COMMENT ON COLUMN xt_sect_member.id IS '成员关系ID';
COMMENT ON COLUMN xt_sect_member.sect_id IS '宗门ID';
COMMENT ON COLUMN xt_sect_member.user_id IS '用户ID';
COMMENT ON COLUMN xt_sect_member.position IS '职位：LEADER/ELDER/MEMBER';
COMMENT ON COLUMN xt_sect_member.contribution IS '个人贡献值';
COMMENT ON COLUMN xt_sect_member.joined_at IS '加入时间';
COMMENT ON COLUMN xt_sect_member.cooldown_until IS '退宗冷却截止时间';

CREATE INDEX idx_sect_member_sect ON xt_sect_member (sect_id);
CREATE INDEX idx_sect_member_user ON xt_sect_member (user_id);
