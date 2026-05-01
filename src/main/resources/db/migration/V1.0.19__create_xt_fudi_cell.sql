-- 创建福地地块独立表
CREATE TABLE xt_fudi_cell (
    id          BIGSERIAL PRIMARY KEY,
    fudi_id     BIGINT NOT NULL,
    cell_id     INT NOT NULL,
    cell_type   VARCHAR(16) NOT NULL DEFAULT 'empty',
    cell_level  INT NOT NULL DEFAULT 1,
    config      JSONB NOT NULL DEFAULT '{}'::jsonb,
    create_time TIMESTAMP NOT NULL DEFAULT NOW(),
    update_time TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_fudi_cell UNIQUE (fudi_id, cell_id)
);

-- 索引
CREATE INDEX idx_fudi_cell_fudi_id ON xt_fudi_cell (fudi_id);
CREATE INDEX idx_fudi_cell_type ON xt_fudi_cell (fudi_id, cell_type);

-- 注释
COMMENT ON TABLE xt_fudi_cell IS '福地地块表';
COMMENT ON COLUMN xt_fudi_cell.fudi_id IS '关联福地ID';
COMMENT ON COLUMN xt_fudi_cell.cell_id IS '地块编号（从1开始）';
COMMENT ON COLUMN xt_fudi_cell.cell_type IS '地块类型：empty/farm/pen';
COMMENT ON COLUMN xt_fudi_cell.cell_level IS '地块等级（1-5）';
COMMENT ON COLUMN xt_fudi_cell.config IS '建筑专有属性（JSONB）';
