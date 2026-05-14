-- 物品模板配置表 (xt_item_template)
CREATE TABLE xt_item_template
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(128) NOT NULL UNIQUE,
    type        VARCHAR(32)  NOT NULL,
    properties  JSONB        DEFAULT '{}'::jsonb,
    tags        JSONB        DEFAULT '[]'::jsonb,
    base_value  BIGINT       NOT NULL DEFAULT 0,
    description TEXT,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_item_template_type CHECK (type IN ('MATERIAL', 'SEED', 'BEAST_EGG', 'POTION', 'EVOLUTION_STONE', 'SKILL_JADE', 'RECIPE_SCROLL', 'FORGING_BLUEPRINT', 'HERB'))
);

COMMENT ON TABLE xt_item_template IS '物品模板配置表';
COMMENT ON COLUMN xt_item_template.id IS '模板ID';
COMMENT ON COLUMN xt_item_template.name IS '物品名称（全表唯一，用作跨环境稳定的语义标识）';
COMMENT ON COLUMN xt_item_template.type IS '物品类型 (MATERIAL, SEED, BEAST_EGG, POTION, EVOLUTION_STONE, SKILL_JADE, RECIPE_SCROLL, HERB)';
COMMENT ON COLUMN xt_item_template.properties IS '类型特有属性 JSONB：
    种子: {"grow_time":24,"reharvest":0,"production_items":[{"weight":1,"template_id":1}]}
    兽卵: {"grow_time":72,"production_items":[...],"skill_pool":{...}}
   法决玉简: {"skill_id":1}
   丹方卷轴: {"grade":3,"product":{"item_id":1,"quantity":1},"requirements":[{"element":"metal","min":1,"max":5}]}
   丹药: {"effects":[{"type":"exp","amount":100}]}';
COMMENT ON COLUMN xt_item_template.tags IS '物品标签 JSONB，用于AI检索和NPC交互，示例: ["ore", "metal", "forge_base"]';
COMMENT ON COLUMN xt_item_template.base_value IS '物品基准价（灵石），系统配置，用于计算收购/售价';
COMMENT ON COLUMN xt_item_template.description IS '物品描述';

-- tags 字段 GIN 索引
CREATE INDEX idx_item_template_tags ON xt_item_template USING GIN (tags);
-- type 字段 B-tree 索引
CREATE INDEX idx_item_template_type ON xt_item_template (type);
