-- 地图连接表 (xt_map_connection)
CREATE TABLE xt_map_connection
(
    id                  BIGSERIAL PRIMARY KEY,
    from_map_id         BIGINT    NOT NULL,
    to_map_id           BIGINT    NOT NULL,
    travel_time_minutes INT       NOT NULL DEFAULT 5,
    bidirectional       BOOLEAN   NOT NULL DEFAULT true,
    create_time         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_connection_from_map FOREIGN KEY (from_map_id) REFERENCES xt_map_node (id) ON DELETE CASCADE,
    CONSTRAINT fk_connection_to_map FOREIGN KEY (to_map_id) REFERENCES xt_map_node (id) ON DELETE CASCADE
);

COMMENT ON TABLE xt_map_connection IS '地图连接表';
COMMENT ON COLUMN xt_map_connection.id IS '连接 ID';
COMMENT ON COLUMN xt_map_connection.from_map_id IS '起始地图 ID';
COMMENT ON COLUMN xt_map_connection.to_map_id IS '目标地图 ID';
COMMENT ON COLUMN xt_map_connection.travel_time_minutes IS '旅行耗时（分钟）';
COMMENT ON COLUMN xt_map_connection.bidirectional IS '是否双向连接';

-- 创建索引
CREATE INDEX idx_map_connection_from ON xt_map_connection (from_map_id);
CREATE INDEX idx_map_connection_to ON xt_map_connection (to_map_id);
CREATE INDEX idx_map_connection_from_to ON xt_map_connection (from_map_id, to_map_id);
