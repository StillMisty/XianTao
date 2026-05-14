-- 商铺出售商品
DROP TABLE IF EXISTS shop_special_order;
DROP TABLE IF EXISTS shop_product;
CREATE TABLE shop_product (
    id              BIGSERIAL PRIMARY KEY,
    shop_npc_id     BIGINT NOT NULL REFERENCES shop_npc(id),
    product_type    VARCHAR(16) NOT NULL CHECK (product_type IN ('ITEM', 'EQUIPMENT')),
    template_id     BIGINT NOT NULL,
    base_price      BIGINT NOT NULL,
    min_price       BIGINT NOT NULL,
    max_price       BIGINT NOT NULL,
    min_stock       INT NOT NULL DEFAULT 0,
    max_stock       INT NOT NULL,
    current_price   BIGINT NOT NULL,
    current_stock   INT NOT NULL,
    last_sale_time  TIMESTAMP,
    version         INT NOT NULL DEFAULT 0,

    CONSTRAINT chk_price CHECK (
        min_price <= base_price AND base_price <= max_price AND
        min_price <= current_price AND current_price <= max_price
    ),
    CONSTRAINT chk_stock CHECK (
        min_stock <= current_stock AND current_stock <= max_stock
    )
);

CREATE INDEX idx_shop_product_npc ON shop_product(shop_npc_id);

COMMENT ON TABLE shop_product IS '商铺出售的商品，控制卖什么和库存价格';
COMMENT ON COLUMN shop_product.product_type IS '商品类型：ITEM（堆叠物品）或 EQUIPMENT（装备）';
COMMENT ON COLUMN shop_product.template_id IS 'ITEM 类型指向 xt_item_template.id，EQUIPMENT 类型指向 xt_equipment_template.id';
COMMENT ON COLUMN shop_product.base_price IS '基准售价（灵石）';
COMMENT ON COLUMN shop_product.min_price IS '最低售价';
COMMENT ON COLUMN shop_product.max_price IS '最高售价';
COMMENT ON COLUMN shop_product.current_price IS '当前售价（会随供需浮动）';
COMMENT ON COLUMN shop_product.min_stock IS '最低库存';
COMMENT ON COLUMN shop_product.max_stock IS '最大库存';
COMMENT ON COLUMN shop_product.current_stock IS '当前库存（会随供需浮动）';
COMMENT ON COLUMN shop_product.last_sale_time IS '最后一次交易时间（用于懒调价/懒补货计算）';
COMMENT ON COLUMN shop_product.version IS '乐观锁版本号';
