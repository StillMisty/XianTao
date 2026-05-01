-- 物品实例表 (xt_inventory_item)
CREATE TABLE xt_inventory_item
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    template_id  BIGINT       NOT NULL,
    item_type    VARCHAR(32)  NOT NULL,
    name         VARCHAR(128) NOT NULL,
    quantity     INT          NOT NULL DEFAULT 1,
    tags         JSONB                 DEFAULT '[]'::jsonb,
    properties   JSONB                 DEFAULT '{}'::jsonb,
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 外键关联
    CONSTRAINT fk_inventory_item_user FOREIGN KEY (user_id) REFERENCES xt_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_inventory_item_template FOREIGN KEY (template_id) REFERENCES xt_item_template (id),

    -- 唯一约束：同一用户的同一类型物品只能有一条记录（堆叠）
    CONSTRAINT uk_user_template UNIQUE (user_id, template_id)
);

-- 字段备注
COMMENT ON TABLE xt_inventory_item IS '物品实例表 (堆叠类物品)';
COMMENT ON COLUMN xt_inventory_item.id IS '物品实例ID';
COMMENT ON COLUMN xt_inventory_item.user_id IS '持有者用户ID';
COMMENT ON COLUMN xt_inventory_item.template_id IS '物品模板ID';
COMMENT ON COLUMN xt_inventory_item.item_type IS '物品类型 (MATERIAL, SEED, BEAST_EGG, POTION, EVOLUTION_STONE)';
COMMENT ON COLUMN xt_inventory_item.name IS '物品名称 (从模板复制)';
COMMENT ON COLUMN xt_inventory_item.quantity IS '数量';
COMMENT ON COLUMN xt_inventory_item.tags IS '物品标签 JSONB，用于AI检索和NPC交互';
COMMENT ON COLUMN xt_inventory_item.properties IS '类型特有属性 JSONB：
  丹药: {"grade": 3, "quality": "superior"}
  药材: {"elements": {"wood": 3, "fire": 1, "water": 2}}
  其他: {}';
COMMENT ON COLUMN xt_inventory_item.create_time IS '创建时间';
COMMENT ON COLUMN xt_inventory_item.update_time IS '更新时间';

-- 索引
CREATE INDEX idx_inventory_item_user_id ON xt_inventory_item (user_id);
CREATE INDEX idx_inventory_item_user_type ON xt_inventory_item (user_id, item_type);
