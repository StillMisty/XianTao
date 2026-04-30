ALTER TABLE xt_spirit ADD COLUMN last_gift_time TIMESTAMP;
COMMENT ON COLUMN xt_spirit.last_gift_time IS '上次送礼时间（每日限送一次）';
