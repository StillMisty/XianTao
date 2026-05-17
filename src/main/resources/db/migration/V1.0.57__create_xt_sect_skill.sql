/* 宗门功法表（已解锁） */
CREATE TABLE xt_sect_skill
(
    sect_id     BIGINT    NOT NULL,
    skill_id    BIGINT    NOT NULL,
    unlocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sect_skill_sect FOREIGN KEY (sect_id) REFERENCES xt_sect (id) ON DELETE CASCADE,
    CONSTRAINT fk_sect_skill_skill FOREIGN KEY (skill_id) REFERENCES xt_skill (id) ON DELETE CASCADE,
    CONSTRAINT uq_sect_skill UNIQUE (sect_id, skill_id)
);

COMMENT ON TABLE xt_sect_skill IS '宗门功法表（已解锁）';
COMMENT ON COLUMN xt_sect_skill.sect_id IS '宗门ID';
COMMENT ON COLUMN xt_sect_skill.skill_id IS '功法ID';
COMMENT ON COLUMN xt_sect_skill.unlocked_at IS '解锁时间';
