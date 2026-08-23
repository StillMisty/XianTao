-- 玩家 Buff 表
-- 存储战斗增益和突破加成等时效性 buff
-- 注：tribulation_resist 允许负值（招雷散等高风险丹药），value 下限 -100
CREATE
    TABLE
        player_buff(
            id BIGSERIAL PRIMARY KEY,
            user_id BIGINT NOT NULL REFERENCES player(id),
            buff_type VARCHAR(32) NOT NULL,
            value INT NOT NULL,
            expires_at TIMESTAMP NOT NULL,
            created_at TIMESTAMP NOT NULL DEFAULT NOW(),
            CONSTRAINT chk_player_buff_type CHECK(
                buff_type IN(
                    'attack',
                    'defense',
                    'speed',
                    'breakthrough',
                    'tribulation_resist'
                )
            ),
            CONSTRAINT chk_player_buff_value CHECK(
                value >= -100
            )
        );

CREATE
    INDEX idx_player_buff_expires ON
    player_buff(expires_at);

CREATE
    INDEX idx_player_buff_user ON
    player_buff(user_id);

CREATE INDEX idx_player_buff_user_type_expires
    ON player_buff (user_id, buff_type, expires_at);

COMMENT ON
TABLE
    player_buff IS '玩家增益/突破Buff表 — 有时效的增益效果';

COMMENT ON
COLUMN player_buff.buff_type IS 'buff类型：attack/defense/speed/breakthrough/tribulation_resist';

COMMENT ON
COLUMN player_buff.value IS '增益值：攻击/防御/速度为属性点，breakthrough为成功率百分比';

COMMENT ON
COLUMN player_buff.expires_at IS '过期时间，到期后清理';
