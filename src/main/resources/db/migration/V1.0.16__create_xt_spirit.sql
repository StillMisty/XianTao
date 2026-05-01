-- 地灵实例表
CREATE TABLE xt_spirit
(
    id                  BIGSERIAL PRIMARY KEY,
    fudi_id             BIGINT      NOT NULL UNIQUE REFERENCES xt_fudi (id) ON DELETE CASCADE,
    form_id             INTEGER     NOT NULL REFERENCES xt_spirit_form (id),

    energy              INTEGER     NOT NULL DEFAULT 100,
    affection           INTEGER     NOT NULL DEFAULT 0,
    affection_max       INTEGER     NOT NULL DEFAULT 1000,
    emotion_state       VARCHAR(20) NOT NULL DEFAULT 'calm',
    mbti_type           VARCHAR(4)  NOT NULL,

    last_energy_update  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_gift_time      TIMESTAMP,
    last_event_time     TIMESTAMP,
    create_time         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_spirit_energy CHECK (energy >= 0),
    CONSTRAINT chk_spirit_affection_max CHECK (affection_max > 0),
    CONSTRAINT chk_spirit_mbti CHECK (mbti_type ~ '^[EI][SN][TF][JP]$')
);

CREATE INDEX idx_spirit_fudi_id ON xt_spirit (fudi_id);

COMMENT ON TABLE xt_spirit IS '地灵实例表（与福地1:1绑定）';
COMMENT ON COLUMN xt_spirit.fudi_id IS '所属福地ID';
COMMENT ON COLUMN xt_spirit.form_id IS '地灵形态ID（关联xt_spirit_form表）';
COMMENT ON COLUMN xt_spirit.energy IS '精力值';
COMMENT ON COLUMN xt_spirit.affection IS '好感度';
COMMENT ON COLUMN xt_spirit.affection_max IS '好感度上限（默认1000）';
COMMENT ON COLUMN xt_spirit.emotion_state IS '当前情绪状态';
COMMENT ON COLUMN xt_spirit.mbti_type IS 'MBTI人格类型（锁定，不可更改）';
COMMENT ON COLUMN xt_spirit.last_energy_update IS '精力最后更新时间（用于懒恢复计算）';
COMMENT ON COLUMN xt_spirit.last_gift_time IS '上次送礼时间（每日限送一次）';
