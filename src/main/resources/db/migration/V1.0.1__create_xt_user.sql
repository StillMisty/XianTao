CREATE TABLE xt_user
(
    id                       BIGSERIAL PRIMARY KEY,
    nickname                 VARCHAR(64) NOT NULL,
    -- 游戏进度
    level                    INT         NOT NULL DEFAULT 1,
    exp                      BIGINT      NOT NULL DEFAULT 0,
    spirit_stones            BIGINT      NOT NULL DEFAULT 0,
    -- 四维属性
    stat_str                 INT         NOT NULL DEFAULT 5,
    stat_con                 INT         NOT NULL DEFAULT 5,
    stat_agi                 INT         NOT NULL DEFAULT 5,
    stat_wis                 INT         NOT NULL DEFAULT 5,
    free_stat_points         INT         NOT NULL DEFAULT 0,
    -- 战斗与状态
    hp_current               INT         NOT NULL DEFAULT 200,
    status                   VARCHAR(32) NOT NULL DEFAULT 'idle',
    location_id              BIGINT      NOT NULL DEFAULT 1,
    training_start_time      TIMESTAMP,
    breakthrough_fail_count  INT         NOT NULL DEFAULT 0,
    last_reset_points_time   TIMESTAMP,
    travel_start_time        TIMESTAMP,
    travel_destination_id    BIGINT,

    -- 扩展数据
    extra_data               JSONB                DEFAULT '{}'::jsonb,
    -- 时间戳
    create_time              TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time              TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 唯一约束：道号必须唯一
    CONSTRAINT uq_nickname UNIQUE (nickname),
    CONSTRAINT chk_user_status CHECK (status IN ('idle', 'training', 'traveling', 'fighting', 'dead'))
);

-- 索引:排行榜常用
CREATE INDEX idx_xt_user_level ON xt_user (level DESC);
CREATE INDEX idx_xt_user_spirit_stones ON xt_user (spirit_stones DESC);

-- 字段备注
COMMENT ON TABLE xt_user IS '游戏角色核心表';
COMMENT ON COLUMN xt_user.id IS '内部唯一角色ID';
COMMENT ON COLUMN xt_user.nickname IS '玩家道号/昵称（唯一）';
COMMENT ON COLUMN xt_user.level IS '角色等级';
COMMENT ON COLUMN xt_user.exp IS '当前经验值';
COMMENT ON COLUMN xt_user.spirit_stones IS '货币 (灵石)';
COMMENT ON COLUMN xt_user.stat_str IS '力量属性 (影响破坏力/锻造)';
COMMENT ON COLUMN xt_user.stat_con IS '体质属性 (影响生命值/物理防御)';
COMMENT ON COLUMN xt_user.stat_agi IS '敏捷属性 (影响出手顺序/杀怪效率)';
COMMENT ON COLUMN xt_user.stat_wis IS '智慧属性 (影响经验加成/炼药)';
COMMENT ON COLUMN xt_user.free_stat_points IS '剩余可分配属性点';
COMMENT ON COLUMN xt_user.hp_current IS '当前生命值';
COMMENT ON COLUMN xt_user.status IS '当前状态';
COMMENT ON COLUMN xt_user.location_id IS '当前所在地图 id';
COMMENT ON COLUMN xt_user.training_start_time IS '历练开始时间戳 (用于结算收益)';
COMMENT ON COLUMN xt_user.breakthrough_fail_count IS '突破失败次数 (影响下一次突破成功率)';
COMMENT ON COLUMN xt_user.last_reset_points_time IS '上次洗点时间戳 (用于洗点冷却判定，3天冷却)';
COMMENT ON COLUMN xt_user.travel_start_time IS '旅行开始时间戳 (用于计算旅行进度)';
COMMENT ON COLUMN xt_user.travel_destination_id IS '旅行目的地地图 ID';
COMMENT ON COLUMN xt_user.extra_data IS 'JSONB 扩展字段 (存储称号、成就、小规模系统数据)';
COMMENT ON COLUMN xt_user.create_time IS '角色创建时间';
COMMENT ON COLUMN xt_user.update_time IS '最后一次数据更新时间';
