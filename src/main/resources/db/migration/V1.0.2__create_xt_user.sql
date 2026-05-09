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
    -- 战斗与状态
    hp_current               INT         NOT NULL DEFAULT 200,
    status                   VARCHAR(32) NOT NULL DEFAULT 'IDLE',
    location_id              BIGINT      NOT NULL DEFAULT 1,
    -- 通用活动字段（旅行/历练/悬赏共享）
    activity_type            VARCHAR(16),
    activity_start_time      TIMESTAMP,
    activity_target_id       BIGINT,
    breakthrough_fail_count  INT         NOT NULL DEFAULT 0,
    last_hp_recovery_time   TIMESTAMP,
    dying_start_time        TIMESTAMP,
    -- GM
    gm                       BOOLEAN     NOT NULL DEFAULT FALSE,
    -- 时间戳
    create_time              TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time              TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 唯一约束：道号必须唯一
    CONSTRAINT uq_nickname UNIQUE (nickname),
    CONSTRAINT chk_user_status CHECK (status IN ('IDLE', 'TRAINING', 'TRAVELING', 'BOUNTY', 'DYING')),
    CONSTRAINT chk_user_activity_type CHECK (activity_type IS NULL OR activity_type IN ('TRAVEL', 'TRAINING', 'BOUNTY')),
    CONSTRAINT chk_user_level CHECK (level >= 1),
    CONSTRAINT chk_user_exp CHECK (exp >= 0),
    CONSTRAINT chk_user_spirit_stones CHECK (spirit_stones >= 0),
    CONSTRAINT chk_user_hp_current CHECK (hp_current >= 0),
    CONSTRAINT chk_user_breakthrough_fail_count CHECK (breakthrough_fail_count >= 0),
    CONSTRAINT chk_user_stat_str CHECK (stat_str >= 0),
    CONSTRAINT chk_user_stat_con CHECK (stat_con >= 0),
    CONSTRAINT chk_user_stat_agi CHECK (stat_agi >= 0),
    CONSTRAINT chk_user_stat_wis CHECK (stat_wis >= 0),
    CONSTRAINT fk_user_location FOREIGN KEY (location_id) REFERENCES xt_map_node (id)
);
