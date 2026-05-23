/* 宗门事件任务表 */
CREATE
    TABLE
        xt_sect_task(
            id BIGSERIAL PRIMARY KEY,
            sect_id BIGINT NOT NULL,
            task_type VARCHAR(16) NOT NULL,
            target_id BIGINT NOT NULL,
            required_count INT NOT NULL,
            contribution_reward INT NOT NULL,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_sect_task_sect FOREIGN KEY(sect_id) REFERENCES xt_sect(id) ON
            DELETE
                CASCADE,
                CONSTRAINT chk_sect_task_type CHECK(
                    task_type IN(
                        'HUNT',
                        'DONATE'
                    )
                ),
                CONSTRAINT chk_sect_task_required_count CHECK(
                    required_count > 0
                ),
                CONSTRAINT chk_sect_task_reward CHECK(
                    contribution_reward > 0
                )
        );

COMMENT ON
TABLE
    xt_sect_task IS '宗门事件任务表';

COMMENT ON
COLUMN xt_sect_task.id IS '任务ID';

COMMENT ON
COLUMN xt_sect_task.sect_id IS '宗门ID';

COMMENT ON
COLUMN xt_sect_task.task_type IS '任务类型：HUNT/DONATE';

COMMENT ON
COLUMN xt_sect_task.target_id IS '目标怪物ID或物品ID';

COMMENT ON
COLUMN xt_sect_task.required_count IS '需要数量';

COMMENT ON
COLUMN xt_sect_task.contribution_reward IS '完成奖励贡献值';

COMMENT ON
COLUMN xt_sect_task.created_at IS '创建时间';

CREATE
    INDEX idx_sect_task_sect ON
    xt_sect_task(sect_id);

CREATE
    INDEX idx_sect_task_target ON
    xt_sect_task(target_id);

CREATE
    INDEX idx_sect_task_sect_type ON
    xt_sect_task(sect_id, task_type);
