-- 游戏角色核心表 (xt_user)
CREATE TABLE xt_user (
                         id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         nickname          VARCHAR(64) NOT NULL,
    -- 游戏进度
                         level             INT NOT NULL DEFAULT 1,
                         exp               BIGINT NOT NULL DEFAULT 0,
                         coins             BIGINT NOT NULL DEFAULT 0,
                         spirit_stones     BIGINT NOT NULL DEFAULT 0,
    -- 四维属性
                         stat_str          INT NOT NULL DEFAULT 2,
                         stat_con          INT NOT NULL DEFAULT 2,
                         stat_agi          INT NOT NULL DEFAULT 2,
                         stat_wis          INT NOT NULL DEFAULT 2,
                         free_stat_points  INT NOT NULL DEFAULT 2,
    -- 战斗与状态
                         hp_current        INT NOT NULL DEFAULT 100,
                         status            VARCHAR(32) NOT NULL DEFAULT 'IDLE',
                         location_id       UUID NOT NULL,
                         afk_start_time    TIMESTAMP,
    -- 扩展数据
                         extra_data        JSONB DEFAULT '{}'::jsonb,
    -- 时间戳
                         create_time       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         update_time       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 字段备注
COMMENT ON TABLE xt_user IS '游戏角色核心表';
COMMENT ON COLUMN xt_user.id IS '内部唯一角色ID';
COMMENT ON COLUMN xt_user.nickname IS '玩家道号/昵称';
COMMENT ON COLUMN xt_user.level IS '角色等级';
COMMENT ON COLUMN xt_user.exp IS '当前经验值';
COMMENT ON COLUMN xt_user.coins IS '基础货币 (铜币)';
COMMENT ON COLUMN xt_user.spirit_stones IS '高级货币 (灵石)';
COMMENT ON COLUMN xt_user.stat_str IS '力量属性 (影响破坏力/锻造)';
COMMENT ON COLUMN xt_user.stat_con IS '体质属性 (影响生命值/物理防御)';
COMMENT ON COLUMN xt_user.stat_agi IS '敏捷属性 (影响出手顺序/杀怪效率)';
COMMENT ON COLUMN xt_user.stat_wis IS '智慧属性 (影响经验加成/炼药)';
COMMENT ON COLUMN xt_user.free_stat_points IS '剩余可分配属性点';
COMMENT ON COLUMN xt_user.hp_current IS '当前生命值';
COMMENT ON COLUMN xt_user.status IS '当前状态';
COMMENT ON COLUMN xt_user.location_id IS '当前所在地图id';
COMMENT ON COLUMN xt_user.afk_start_time IS '挂机开始时间戳 (用于结算收益)';
COMMENT ON COLUMN xt_user.extra_data IS 'JSONB扩展字段 (存储称号、成就、小规模系统数据)';
COMMENT ON COLUMN xt_user.create_time IS '角色创建时间';
COMMENT ON COLUMN xt_user.update_time IS '最后一次数据更新时间';

-- 索引：排行榜常用
CREATE INDEX idx_xt_user_level ON xt_user (level DESC);
CREATE INDEX idx_xt_user_coins ON xt_user (coins DESC);
CREATE INDEX idx_xt_user_spirit_stones ON xt_user (spirit_stones DESC);

-- 自动更新 update_time 的触发器
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_xt_user_time
    BEFORE UPDATE ON xt_user
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();

-- 跨平台授权绑定表 (xt_user_auth)
CREATE TABLE xt_user_auth (
                              id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              user_id           UUID NOT NULL,
                              platform          VARCHAR(32) NOT NULL,
                              platform_open_id  VARCHAR(128) NOT NULL,
                              bind_time         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 核心约束：保证同一个平台的ID只能绑定到一个角色
    -- 而 user_id 没有唯一约束，允许一个 user_id 对应多条不同平台的记录
                              CONSTRAINT uq_platform_id UNIQUE (platform, platform_open_id),
    -- 外键关联
                              CONSTRAINT fk_auth_user FOREIGN KEY (user_id) REFERENCES xt_user(id) ON DELETE CASCADE
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