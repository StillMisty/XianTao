/* 师徒关系表 */
CREATE
    TABLE
        master_apprentice(
            id BIGSERIAL PRIMARY KEY,
            master_id BIGINT NOT NULL,
            apprentice_id BIGINT NOT NULL,
            status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            graduated_at TIMESTAMP,
            cooldown_until TIMESTAMP,
            CONSTRAINT fk_ma_master FOREIGN KEY(master_id) REFERENCES xt_user(id) ON
            DELETE
                CASCADE,
                CONSTRAINT fk_ma_apprentice FOREIGN KEY(apprentice_id) REFERENCES xt_user(id) ON
                DELETE
                    CASCADE,
                    CONSTRAINT uq_ma_apprentice UNIQUE(apprentice_id),
                    CONSTRAINT uq_ma_pair UNIQUE(
                        master_id,
                        apprentice_id
                    ),
                    CONSTRAINT chk_ma_self CHECK(
                        master_id != apprentice_id
                    ),
                    CONSTRAINT chk_ma_status CHECK(
                        status IN(
                            'ACTIVE',
                            'GRADUATED',
                            'DISMISSED',
                            'RENEGED'
                        )
                    )
        );

COMMENT ON
TABLE
    master_apprentice IS '师徒关系表';

COMMENT ON
COLUMN master_apprentice.id IS '师徒关系ID';

COMMENT ON
COLUMN master_apprentice.master_id IS '师傅用户ID';

COMMENT ON
COLUMN master_apprentice.apprentice_id IS '徒弟用户ID';

COMMENT ON
COLUMN master_apprentice.status IS '状态：ACTIVE/GRADUATED/DISMISSED/RENEGED';

COMMENT ON
COLUMN master_apprentice.created_at IS '建立时间';

COMMENT ON
COLUMN master_apprentice.graduated_at IS '出师时间';

COMMENT ON
COLUMN master_apprentice.cooldown_until IS '冷却截止时间';

CREATE
    INDEX idx_ma_master ON
    master_apprentice(master_id);

CREATE
    INDEX idx_ma_apprentice ON
    master_apprentice(apprentice_id);
