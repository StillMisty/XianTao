-- 玩家 Buff 表
-- 存储战斗增益和突破加成等时效性 buff
CREATE TABLE xt_player_buff (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES xt_user(id),
    buff_type   VARCHAR(32) NOT NULL,
    value       INT NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_player_buff_expires ON xt_player_buff(expires_at);
CREATE INDEX idx_player_buff_user ON xt_player_buff(user_id);

COMMENT ON TABLE xt_player_buff IS '玩家增益/突破Buff表 — 有时效的增益效果';
COMMENT ON COLUMN xt_player_buff.buff_type IS 'buff类型：attack/defense/speed/breakthrough';
COMMENT ON COLUMN xt_player_buff.value IS '增益值：攻击/防御/速度为属性点，breakthrough为成功率百分比';
COMMENT ON COLUMN xt_player_buff.expires_at IS '过期时间，到期后清理';
