-- 秘境奖励记录表
CREATE TABLE dungeon_progress (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    dungeon_id      BIGINT NOT NULL,
    last_reward_date DATE NOT NULL,
    reward_count    INT NOT NULL DEFAULT 0,
    daily_limit     INT NOT NULL,
    first_clear     BOOLEAN NOT NULL DEFAULT FALSE,
    best_area       VARCHAR(16),
    CONSTRAINT fk_dungeon_progress_user FOREIGN KEY (user_id) REFERENCES xt_user (id),
    CONSTRAINT fk_dungeon_progress_dungeon FOREIGN KEY (dungeon_id) REFERENCES dungeon_template (id),
    CONSTRAINT chk_progress_area CHECK (best_area IN ('OUTER', 'INNER', 'CORE')),
    CONSTRAINT uq_dungeon_progress UNIQUE (user_id, dungeon_id)
);

CREATE INDEX idx_dungeon_progress_user ON dungeon_progress (user_id);

COMMENT ON TABLE dungeon_progress IS '秘境奖励记录表';
COMMENT ON COLUMN dungeon_progress.last_reward_date IS '最近领奖日期';
COMMENT ON COLUMN dungeon_progress.reward_count IS '今日已领奖次数';
COMMENT ON COLUMN dungeon_progress.daily_limit IS '当日通关奖励上限';
COMMENT ON COLUMN dungeon_progress.first_clear IS '是否首通';
COMMENT ON COLUMN dungeon_progress.best_area IS '历史最高到达区域';
