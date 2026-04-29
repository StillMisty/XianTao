-- 跨平台授权绑定表 (xt_user_auth)
CREATE TABLE xt_user_auth
(
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    platform         VARCHAR(32)  NOT NULL,
    platform_open_id VARCHAR(128) NOT NULL,
    bind_time        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 核心约束：保证同一个平台的ID只能绑定到一个角色
    -- 而 user_id 没有唯一约束，允许一个 user_id 对应多条不同平台的记录
    CONSTRAINT uq_platform_id UNIQUE (platform, platform_open_id),
    -- 外键关联
    CONSTRAINT fk_auth_user FOREIGN KEY (user_id) REFERENCES xt_user (id) ON DELETE CASCADE
);

-- 字段备注
COMMENT ON TABLE xt_user_auth IS '跨平台授权绑定表';
COMMENT ON COLUMN xt_user_auth.id IS '绑定记录主键';
COMMENT ON COLUMN xt_user_auth.user_id IS '关联的游戏角色ID';
COMMENT ON COLUMN xt_user_auth.platform IS '平台类型';
COMMENT ON COLUMN xt_user_auth.platform_open_id IS '平台方的唯一标识ID';
COMMENT ON COLUMN xt_user_auth.bind_time IS '绑定发生的时间';

-- 索引：通过角色ID反查绑定情况
CREATE INDEX idx_xt_user_auth_user_id ON xt_user_auth (user_id);
