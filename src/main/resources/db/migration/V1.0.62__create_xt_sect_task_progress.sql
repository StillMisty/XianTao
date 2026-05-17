/* 宗门任务进度表 */
CREATE TABLE xt_sect_task_progress
(
    id           BIGSERIAL PRIMARY KEY,
    task_id      BIGINT    NOT NULL,
    user_id      BIGINT    NOT NULL,
    progress     INT       NOT NULL DEFAULT 0,
    completed    BOOLEAN   NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    CONSTRAINT fk_sect_task_progress_task FOREIGN KEY (task_id) REFERENCES xt_sect_task (id) ON DELETE CASCADE,
    CONSTRAINT fk_sect_task_progress_user FOREIGN KEY (user_id) REFERENCES xt_user (id) ON DELETE CASCADE,
    CONSTRAINT uq_sect_task_progress UNIQUE (task_id, user_id),
    CONSTRAINT chk_sect_task_progress_count CHECK (progress >= 0)
);

COMMENT ON TABLE xt_sect_task_progress IS '宗门任务进度表';
COMMENT ON COLUMN xt_sect_task_progress.id IS '进度记录ID';
COMMENT ON COLUMN xt_sect_task_progress.task_id IS '任务ID';
COMMENT ON COLUMN xt_sect_task_progress.user_id IS '用户ID';
COMMENT ON COLUMN xt_sect_task_progress.progress IS '已完成数量';
COMMENT ON COLUMN xt_sect_task_progress.completed IS '是否已领奖';
COMMENT ON COLUMN xt_sect_task_progress.completed_at IS '完成时间';

CREATE INDEX idx_sect_task_progress_task ON xt_sect_task_progress (task_id);
CREATE INDEX idx_sect_task_progress_user ON xt_sect_task_progress (user_id);
