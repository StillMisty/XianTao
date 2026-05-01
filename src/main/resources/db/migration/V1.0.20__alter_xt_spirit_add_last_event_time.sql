-- 地灵表添加最后事件时间字段
ALTER TABLE xt_spirit ADD COLUMN last_event_time TIMESTAMP;

COMMENT ON COLUMN xt_spirit.last_event_time IS '最后事件发生时间';
