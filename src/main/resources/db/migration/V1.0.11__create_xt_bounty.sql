-- 悬赏配置表
CREATE
    TABLE
        xt_bounty(
            id BIGSERIAL PRIMARY KEY,
            map_id BIGINT NOT NULL,
            name VARCHAR(100) NOT NULL,
            description TEXT,
            duration_minutes INTEGER NOT NULL,
            rewards JSONB NOT NULL DEFAULT '[]' ::jsonb,
            require_level INTEGER NOT NULL DEFAULT 1,
            event_weight INTEGER NOT NULL DEFAULT 0,
            is_unique BOOLEAN NOT NULL DEFAULT FALSE,
            create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_bounty_map FOREIGN KEY(map_id) REFERENCES xt_map_node(id),
            CONSTRAINT chk_bounty_duration CHECK(
                duration_minutes > 0
            ),
            CONSTRAINT chk_bounty_event_weight CHECK(
                event_weight >= 0
            ),
            CONSTRAINT chk_bounty_require_level CHECK(
                require_level >= 1
            )
        );

COMMENT ON
TABLE
    xt_bounty IS '悬赏任务配置表';

COMMENT ON
COLUMN xt_bounty.map_id IS '所属地图 ID';

COMMENT ON
COLUMN xt_bounty.name IS '悬赏名称';

COMMENT ON
COLUMN xt_bounty.description IS '悬赏描述';

COMMENT ON
COLUMN xt_bounty.duration_minutes IS '悬赏耗时（分钟）';

COMMENT ON
COLUMN xt_bounty.rewards IS '奖励池 JSONB，type 区分类型: 稀有物品 {"type":"rare_item","weight":60,"min":1,"max":2,"template_id":123}, 灵石 {"type":"spirit_stones","weight":40,"min":100,"max":200}, 兽卵 {"type":"beast_egg","weight":20,"name":"灵兽蛋"}, 装备 {"type":"equipment","weight":30,"template_id":456}';

COMMENT ON
COLUMN xt_bounty.require_level IS '最低接取等级';

COMMENT ON
COLUMN xt_bounty.event_weight IS '旅行事件触发权重';

COMMENT ON
COLUMN xt_bounty.is_unique IS '是否为唯一悬赏（完成后不可再接取）';

CREATE
    INDEX idx_bounty_map_id ON
    xt_bounty(map_id);
