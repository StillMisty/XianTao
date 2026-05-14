-- 商铺掌柜模板
DROP TABLE IF EXISTS shop_special_order;
DROP TABLE IF EXISTS shop_product;
DROP TABLE IF EXISTS shop_npc;
CREATE TABLE shop_npc (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(64) NOT NULL,
    map_node_id         BIGINT NOT NULL REFERENCES xt_map_node(id),
    personality         VARCHAR(16),
    buy_price_modifier  NUMERIC(3,2) NOT NULL DEFAULT 0.50,
    category_multiplier JSONB,
    system_prompt       TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE shop_npc IS '商铺掌柜模板';
COMMENT ON COLUMN shop_npc.id IS '掌柜唯一 ID';
COMMENT ON COLUMN shop_npc.name IS '掌柜名称';
COMMENT ON COLUMN shop_npc.map_node_id IS '所属地图节点 ID';
COMMENT ON COLUMN shop_npc.personality IS 'MBTI 或性格标签，复用福地地灵模式';
COMMENT ON COLUMN shop_npc.buy_price_modifier IS '收购折扣系数（相对基准价的百分比）';
COMMENT ON COLUMN shop_npc.category_multiplier IS '按物品类型的浮动系数，如 {"HERB": 0.85, "ORE": 1.15}';
COMMENT ON COLUMN shop_npc.system_prompt IS 'LLM system prompt（覆盖默认）';
