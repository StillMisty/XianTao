CREATE TABLE xt_dao_protection
(
    id           BIGSERIAL PRIMARY KEY,
    protector_id BIGINT    NOT NULL,
    protege_id   BIGINT    NOT NULL,
    create_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_protector FOREIGN KEY (protector_id) REFERENCES xt_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_protege FOREIGN KEY (protege_id) REFERENCES xt_user (id) ON DELETE CASCADE,
    CONSTRAINT uq_protection UNIQUE (protector_id, protege_id)
);

COMMENT ON TABLE xt_dao_protection IS '护道关系表';
COMMENT ON COLUMN xt_dao_protection.id IS '护道关系ID';
COMMENT ON COLUMN xt_dao_protection.protector_id IS '护道者ID (提供加成的一方)';
COMMENT ON COLUMN xt_dao_protection.protege_id IS '被护道者ID (突破的一方)';
COMMENT ON COLUMN xt_dao_protection.create_time IS '建立护道关系的时间';
COMMENT ON COLUMN xt_dao_protection.update_time IS '更新时间';

-- 创建索引
CREATE INDEX idx_dao_protection_protector ON xt_dao_protection (protector_id);
CREATE INDEX idx_dao_protection_protege ON xt_dao_protection (protege_id);
