-- 特殊调货订单
DROP TABLE IF EXISTS shop_special_order;
CREATE TABLE shop_special_order (
    id              BIGSERIAL PRIMARY KEY,
    player_id       BIGINT NOT NULL REFERENCES xt_user(id),
    shop_npc_id     BIGINT NOT NULL REFERENCES shop_npc(id),
    template_id     BIGINT NOT NULL REFERENCES xt_item_template(id),
    unit_price      BIGINT NOT NULL,
    quantity        INT NOT NULL DEFAULT 1,
    deposit         BIGINT NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'READY', 'COLLECTED', 'CANCELLED')),
    sourcing_hours  INT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_special_order_player ON shop_special_order(player_id);

COMMENT ON TABLE shop_special_order IS '特殊调货订单：本店无货时 LLM 可提议调货';
COMMENT ON COLUMN shop_special_order.unit_price IS '单价（灵石）';
COMMENT ON COLUMN shop_special_order.deposit IS '定金（灵石），一般为总价的 10%';
COMMENT ON COLUMN shop_special_order.status IS '状态：PENDING-等待调货 READY-已到货 COLLECTED-已取货 CANCELLED-已取消';
COMMENT ON COLUMN shop_special_order.sourcing_hours IS '调货所需时长（小时）';
