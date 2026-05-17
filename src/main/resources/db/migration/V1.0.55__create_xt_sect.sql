/* 宗门表 */
CREATE TABLE xt_sect
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(32) NOT NULL,
    leader_id    BIGINT      NOT NULL,
    level        INT         NOT NULL DEFAULT 1,
    funds        BIGINT      NOT NULL DEFAULT 0,
    max_members  INT         NOT NULL DEFAULT 10,
    description  TEXT,
    notice       TEXT,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sect_leader FOREIGN KEY (leader_id) REFERENCES xt_user (id) ON DELETE CASCADE,
    CONSTRAINT uq_sect_name UNIQUE (name),
    CONSTRAINT chk_sect_level CHECK (level BETWEEN 1 AND 5),
    CONSTRAINT chk_sect_max_members CHECK (max_members >= 10),
    CONSTRAINT chk_sect_funds CHECK (funds >= 0)
);

COMMENT ON TABLE xt_sect IS '宗门表';
COMMENT ON COLUMN xt_sect.id IS '宗门ID';
COMMENT ON COLUMN xt_sect.name IS '宗门名';
COMMENT ON COLUMN xt_sect.leader_id IS '宗主用户ID';
COMMENT ON COLUMN xt_sect.level IS '宗门等级（1~5）';
COMMENT ON COLUMN xt_sect.funds IS '宗门资金池';
COMMENT ON COLUMN xt_sect.max_members IS '成员上限';
COMMENT ON COLUMN xt_sect.description IS '宗门简介';
COMMENT ON COLUMN xt_sect.notice IS '宗门公告';
COMMENT ON COLUMN xt_sect.created_at IS '创建时间';
COMMENT ON COLUMN xt_sect.updated_at IS '更新时间';
