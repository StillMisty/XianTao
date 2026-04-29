-- 物品模板配置表 (xt_item_template)
CREATE TABLE xt_item_template
(
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(128) NOT NULL,
    type            VARCHAR(32)  NOT NULL,
    rarity          VARCHAR(32)  NOT NULL DEFAULT 'common',
    slot            VARCHAR(32),
    equip_level     INT          NOT NULL DEFAULT 0,
    base_stat_bonus JSONB                 DEFAULT '{}'::jsonb,
    base_attack     INT          NOT NULL DEFAULT 0,
    base_defense    INT          NOT NULL DEFAULT 0,
    drop_weight     JSONB                 DEFAULT '{}'::jsonb,
    tags            JSONB                 DEFAULT '[]'::jsonb,
    grow_time       INT,
    yield_id        VARCHAR(64),
    survive_rate    INT,
    max_stack       INT          NOT NULL DEFAULT 1,
    description     TEXT,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE xt_item_template IS '物品模板配置表';
COMMENT ON COLUMN xt_item_template.id IS '模板ID';
COMMENT ON COLUMN xt_item_template.name IS '物品名称';
COMMENT ON COLUMN xt_item_template.type IS '物品类型 (EQUIPMENT, MATERIAL, SEED, BEAST_EGG, POTION, EVOLUTION_STONE)';
COMMENT ON COLUMN xt_item_template.rarity IS '稀有度';
COMMENT ON COLUMN xt_item_template.slot IS '装备部位 (仅装备类)';
COMMENT ON COLUMN xt_item_template.equip_level IS '装备等级（仅装备类，用于计算词条数值）';
COMMENT ON COLUMN xt_item_template.base_stat_bonus IS '基础属性加成';
COMMENT ON COLUMN xt_item_template.base_attack IS '基础攻击力';
COMMENT ON COLUMN xt_item_template.base_defense IS '基础防御力';
COMMENT ON COLUMN xt_item_template.drop_weight IS '掉落权重 JSONB（仅装备类），示例: {"BROKEN": 50, "COMMON": 30, "RARE": 15, "EPIC": 4, "LEGENDARY": 1}';
COMMENT ON COLUMN xt_item_template.tags IS '物品标签 JSONB，用于AI检索和NPC交互，示例: ["ore", "metal", "forge_base"]';
COMMENT ON COLUMN xt_item_template.grow_time IS '生长/孵化时间（小时，种子/灵蛋/灵兽卵）';
COMMENT ON COLUMN xt_item_template.yield_id IS '成熟后产出的物品模板ID（仅种子/灵蛋）';
COMMENT ON COLUMN xt_item_template.survive_rate IS '存活率百分比（仅种子/灵蛋）';
COMMENT ON COLUMN xt_item_template.max_stack IS '最大堆叠数量';
COMMENT ON COLUMN xt_item_template.description IS '物品描述';

-- 创建tags字段的GIN索引（用于标签搜索）
CREATE INDEX idx_item_template_tags ON xt_item_template USING GIN (tags);
