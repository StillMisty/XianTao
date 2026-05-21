-- 新增雷劫抗性buff类型
ALTER TABLE xt_player_buff DROP CONSTRAINT IF EXISTS chk_player_buff_type;
ALTER TABLE xt_player_buff ADD CONSTRAINT chk_player_buff_type CHECK (buff_type IN ('attack', 'defense', 'speed', 'breakthrough', 'tribulation_resist'));

COMMENT ON COLUMN xt_player_buff.buff_type IS 'buff类型：attack/defense/speed/breakthrough/tribulation_resist';
