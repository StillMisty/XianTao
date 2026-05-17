/* 宗门共享功法表（玩家提交） */
CREATE TABLE xt_sect_shared_skill
(
    id                 BIGSERIAL PRIMARY KEY,
    sect_id            BIGINT      NOT NULL,
    skill_id           BIGINT      NOT NULL,
    submitter_user_id  BIGINT      NOT NULL,
    status             VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sect_shared_skill_sect FOREIGN KEY (sect_id) REFERENCES xt_sect (id) ON DELETE CASCADE,
    CONSTRAINT fk_sect_shared_skill_skill FOREIGN KEY (skill_id) REFERENCES xt_skill (id) ON DELETE CASCADE,
    CONSTRAINT fk_sect_shared_skill_user FOREIGN KEY (submitter_user_id) REFERENCES xt_user (id) ON DELETE CASCADE,
    CONSTRAINT uq_sect_shared_skill UNIQUE (sect_id, skill_id),
    CONSTRAINT chk_sect_shared_skill_status CHECK (status IN ('PENDING', 'LISTED'))
);

COMMENT ON TABLE xt_sect_shared_skill IS '宗门共享功法表（玩家提交）';
COMMENT ON COLUMN xt_sect_shared_skill.id IS '记录ID';
COMMENT ON COLUMN xt_sect_shared_skill.sect_id IS '宗门ID';
COMMENT ON COLUMN xt_sect_shared_skill.skill_id IS '功法ID';
COMMENT ON COLUMN xt_sect_shared_skill.submitter_user_id IS '提交者用户ID';
COMMENT ON COLUMN xt_sect_shared_skill.status IS '状态：PENDING/​LISTED';
COMMENT ON COLUMN xt_sect_shared_skill.created_at IS '提交时间';

CREATE INDEX idx_sect_shared_skill_sect ON xt_sect_shared_skill (sect_id);
