/* 宗门商店商品表 */
CREATE
    TABLE
        xt_sect_shop_item(
            id BIGSERIAL PRIMARY KEY,
            sect_id BIGINT NOT NULL,
            item_template_id BIGINT NOT NULL,
            price_contribution INT NOT NULL,
            stock INT NOT NULL DEFAULT - 1,
            last_refresh TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_sect_shop_item_sect FOREIGN KEY(sect_id) REFERENCES xt_sect(id) ON
            DELETE
                CASCADE,
                CONSTRAINT fk_sect_shop_item_template FOREIGN KEY(item_template_id) REFERENCES xt_item_template(id) ON
                DELETE
                    CASCADE,
                    CONSTRAINT uq_sect_shop_item UNIQUE(
                        sect_id,
                        item_template_id
                    ),
                    CONSTRAINT chk_sect_shop_item_price CHECK(
                        price_contribution > 0
                    ),
                    CONSTRAINT chk_sect_shop_item_stock CHECK(
                        stock >=- 1
                    )
        );

COMMENT ON
TABLE
    xt_sect_shop_item IS '宗门商店商品表';

COMMENT ON
COLUMN xt_sect_shop_item.id IS '商品ID';

COMMENT ON
COLUMN xt_sect_shop_item.sect_id IS '宗门ID';

COMMENT ON
COLUMN xt_sect_shop_item.item_template_id IS '物品模板ID';

COMMENT ON
COLUMN xt_sect_shop_item.price_contribution IS '贡献价格';

COMMENT ON
COLUMN xt_sect_shop_item.stock IS '库存（-1为无限）';

COMMENT ON
COLUMN xt_sect_shop_item.last_refresh IS '上次刷新时间';

CREATE
    INDEX idx_sect_shop_item_sect ON
    xt_sect_shop_item(sect_id);
