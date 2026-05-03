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
    CONSTRAINT uq_fudi_cell UNIQUE (fudi_id, cell_id),
    CONSTRAINT fk_fudi_cell_fudi FOREIGN KEY (fudi_id) REFERENCES xt_fudi (id) ON DELETE CASCADE,
    CONSTRAINT chk_fudi_cell_type CHECK (cell_type IN ('empty', 'farm', 'pen')),
    CONSTRAINT chk_fudi_cell_level CHECK (cell_level BETWEEN 1 AND 5),
    CONSTRAINT chk_fudi_cell_id CHECK (cell_id >= 1)
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
COMMENT ON COLUMN xt_fudi_cell.config IS '建筑专有属性，反序列化时根据 cell_type 映射为 FarmConfig/PenConfig/EmptyConfig：
  farm: {"crop_id":1,"plant_time":"2026-01-01T12:00:00","mature_time":"2026-01-02T12:00:00","harvest_count":0}
  pen: {"beast_id":1,"template_id":5,"hatch_time":"2026-01-01T12:00:00","mature_time":"2026-01-02T12:00:00","production_stored":[{"template_id":1,"name":"灵草","quantity":5}],"last_production_time":"2026-01-01T18:00:00"}';
