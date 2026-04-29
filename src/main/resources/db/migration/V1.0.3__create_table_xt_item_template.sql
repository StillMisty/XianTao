-- 物品模板配置表 (xt_item_template)
CREATE TABLE xt_item_template
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(128) NOT NULL UNIQUE,
    type        VARCHAR(32)  NOT NULL,
    rarity      VARCHAR(32)  NOT NULL DEFAULT 'common',
    properties  JSONB        DEFAULT '{}'::jsonb,
    tags        JSONB        DEFAULT '[]'::jsonb,
    max_stack   INT          NOT NULL DEFAULT 1,
    description TEXT,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE xt_item_template IS '物品模板配置表';
COMMENT ON COLUMN xt_item_template.id IS '模板ID';
COMMENT ON COLUMN xt_item_template.name IS '物品名称（全表唯一，用作跨环境稳定的语义标识）';
COMMENT ON COLUMN xt_item_template.type IS '物品类型 (EQUIPMENT, MATERIAL, SEED, BEAST_EGG, POTION, EVOLUTION_STONE)';
COMMENT ON COLUMN xt_item_template.rarity IS '稀有度';
COMMENT ON COLUMN xt_item_template.properties IS '类型特有属性 JSONB：
  装备: {"slot":"WEAPON","equip_level":1,"base_attack":2,"base_defense":0,"base_stat_bonus":{"str":1},"drop_weight":{"BROKEN":60,...}}
  种子: {"grow_time":24,"yields":["铜矿石","铁矿石","灵石"],"survive_rate":90}
  灵兽卵: {"grow_time":72,"yields":["火灵兽"],"survive_rate":70}
  其他: {}';
COMMENT ON COLUMN xt_item_template.tags IS '物品标签 JSONB，用于AI检索和NPC交互，示例: ["ore", "metal", "forge_base"]';
COMMENT ON COLUMN xt_item_template.max_stack IS '最大堆叠数量';
COMMENT ON COLUMN xt_item_template.description IS '物品描述';

-- tags 字段 GIN 索引
CREATE INDEX idx_item_template_tags ON xt_item_template USING GIN (tags);
-- name 字段 B-tree 索引（UNIQUE 约束自带索引，显式创建仅作标注用途）
CREATE INDEX idx_item_template_name ON xt_item_template (name);
