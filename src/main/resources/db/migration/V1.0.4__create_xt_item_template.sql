-- 物品模板配置表 (xt_item_template)
CREATE
    TABLE
        xt_item_template(
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(128) NOT NULL UNIQUE,
            TYPE VARCHAR(32) NOT NULL,
            properties JSONB DEFAULT '{}' ::jsonb,
            tags JSONB DEFAULT '[]' ::jsonb,
            base_value BIGINT NOT NULL DEFAULT 0,
            description TEXT,
            create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT chk_item_template_type CHECK(
                TYPE IN(
                    'MATERIAL',
                    'SEED',
                    'BEAST_EGG',
                    'POTION',
                    'SKILL_JADE',
                    'RECIPE_SCROLL',
                    'FORGING_BLUEPRINT',
                    'HERB',
                    'BEAST_ESSENCE'
                )
            )
        );

COMMENT ON
TABLE
    xt_item_template IS '物品模板配置表';

COMMENT ON
COLUMN xt_item_template.id IS '模板ID';

COMMENT ON
COLUMN xt_item_template.name IS '物品名称（全表唯一，用作跨环境稳定的语义标识）';

COMMENT ON
COLUMN xt_item_template.type IS '物品类型 (MATERIAL, SEED, BEAST_EGG, POTION, SKILL_JADE, RECIPE_SCROLL, HERB)';

COMMENT ON
COLUMN xt_item_template.properties IS '类型特有属性 JSONB：
    种子: {"grow_time":24,"max_harvest":1,"yield_min":2,"yield_max":4,"mutation":{"chance":0.05,"template_id":N},"production_items":[{"template_id":1}]}
    兽卵: {"beast_template_id":1}
   法决玉简: {"skill_id":1}
    丹方卷轴: {"recipe":{"grade":3,"result_item_id":1,"result_quantity":1,"requirements":{"metal":{"min":1,"max":5}}}}
    丹药: {"effects":[{"type":"exp","amount":100}]}
    药材: {"elements":{"wood":1,"water":1}}
    锻材: {"RIGIDITY":5,"TOUGHNESS":3,"SPIRIT":1}';

COMMENT ON
COLUMN xt_item_template.tags IS '物品标签 JSONB，用于AI检索和NPC交互，示例: ["ore", "metal", "forge_base"]';

COMMENT ON
COLUMN xt_item_template.base_value IS '物品基准价（灵石），系统配置，用于计算收购/售价';

COMMENT ON
COLUMN xt_item_template.description IS '物品描述';

-- tags 字段 GIN 索引
CREATE
    INDEX idx_item_template_tags ON
    xt_item_template
        USING GIN(tags);

-- type 字段 B-tree 索引
CREATE
    INDEX idx_item_template_type ON
    xt_item_template(TYPE);
