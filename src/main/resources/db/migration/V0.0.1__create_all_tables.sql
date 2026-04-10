-- ============================================================================
-- XianTao 数据库初始化脚本
-- 版本: V0.0.1
-- 说明: 包含用户、装备、物品等所有核心表
-- ============================================================================

-- ============================================================================
-- 用户系统
-- ============================================================================

-- 游戏角色核心表 (xt_user)
CREATE TABLE xt_user (
    id                      BIGSERIAL PRIMARY KEY,
    nickname                VARCHAR(64) NOT NULL,
    -- 游戏进度
    level                   INT         NOT NULL DEFAULT 1,
    exp                     BIGINT      NOT NULL DEFAULT 0,
    coins                   BIGINT      NOT NULL DEFAULT 0,
    spirit_stones           BIGINT      NOT NULL DEFAULT 0,
    -- 四维属性
    stat_str                INT         NOT NULL DEFAULT 5,
    stat_con                INT         NOT NULL DEFAULT 5,
    stat_agi                INT         NOT NULL DEFAULT 5,
    stat_wis                INT         NOT NULL DEFAULT 5,
    free_stat_points        INT         NOT NULL DEFAULT 0,
    -- 战斗与状态
    hp_current              INT         NOT NULL DEFAULT 200,
    status                  VARCHAR(32) NOT NULL DEFAULT 'IDLE',
    location_id             BIGINT      NOT NULL DEFAULT 0,
    afk_start_time          TIMESTAMP,
    breakthrough_fail_count INT         NOT NULL DEFAULT 0,

    -- 扩展数据
    extra_data              JSONB                DEFAULT '{}'::jsonb,
    -- 时间戳
    create_time             TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 字段备注
COMMENT ON TABLE xt_user IS '游戏角色核心表';
COMMENT ON COLUMN xt_user.id IS '内部唯一角色ID';
COMMENT ON COLUMN xt_user.nickname IS '玩家道号/昵称';
COMMENT ON COLUMN xt_user.level IS '角色等级';
COMMENT ON COLUMN xt_user.exp IS '当前经验值';
COMMENT ON COLUMN xt_user.coins IS '基础货币 (铜币)';
COMMENT ON COLUMN xt_user.spirit_stones IS '高级货币 (灵石)';
COMMENT ON COLUMN xt_user.stat_str IS '力量属性 (影响破坏力/锻造)';
COMMENT ON COLUMN xt_user.stat_con IS '体质属性 (影响生命值/物理防御)';
COMMENT ON COLUMN xt_user.stat_agi IS '敏捷属性 (影响出手顺序/杀怪效率)';
COMMENT ON COLUMN xt_user.stat_wis IS '智慧属性 (影响经验加成/炼药)';
COMMENT ON COLUMN xt_user.free_stat_points IS '剩余可分配属性点';
COMMENT ON COLUMN xt_user.hp_current IS '当前生命值';
COMMENT ON COLUMN xt_user.status IS '当前状态';
COMMENT ON COLUMN xt_user.location_id IS '当前所在地图 id';
COMMENT ON COLUMN xt_user.afk_start_time IS '挂机开始时间戳 (用于结算收益)';
COMMENT ON COLUMN xt_user.breakthrough_fail_count IS '突破失败次数 (影响下一次突破成功率)';
COMMENT ON COLUMN xt_user.extra_data IS 'JSONB 扩展字段 (存储称号、成就、小规模系统数据)';
COMMENT ON COLUMN xt_user.create_time IS '角色创建时间';
COMMENT ON COLUMN xt_user.update_time IS '最后一次数据更新时间';

-- 索引：排行榜常用
CREATE INDEX idx_xt_user_level ON xt_user (level DESC);
CREATE INDEX idx_xt_user_coins ON xt_user (coins DESC);
CREATE INDEX idx_xt_user_spirit_stones ON xt_user (spirit_stones DESC);

-- 跨平台授权绑定表 (xt_user_auth)
CREATE TABLE xt_user_auth (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    platform         VARCHAR(32)  NOT NULL,
    platform_open_id VARCHAR(128) NOT NULL,
    bind_time        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 核心约束：保证同一个平台的ID只能绑定到一个角色
    -- 而 user_id 没有唯一约束，允许一个 user_id 对应多条不同平台的记录
    CONSTRAINT uq_platform_id UNIQUE (platform, platform_open_id),
    -- 外键关联
    CONSTRAINT fk_auth_user FOREIGN KEY (user_id) REFERENCES xt_user (id) ON DELETE CASCADE
);

-- 字段备注
COMMENT ON TABLE xt_user_auth IS '跨平台授权绑定表';
COMMENT ON COLUMN xt_user_auth.id IS '绑定记录主键';
COMMENT ON COLUMN xt_user_auth.user_id IS '关联的游戏角色ID';
COMMENT ON COLUMN xt_user_auth.platform IS '平台类型';
COMMENT ON COLUMN xt_user_auth.platform_open_id IS '平台方的唯一标识ID';
COMMENT ON COLUMN xt_user_auth.bind_time IS '绑定发生的时间';

-- 索引：通过角色ID反查绑定情况
CREATE INDEX idx_xt_user_auth_user_id ON xt_user_auth (user_id);

-- ============================================================================
-- 物品系统
-- ============================================================================

-- 物品模板配置表 (xt_item_template)
CREATE TABLE xt_item_template (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(128) NOT NULL,
    type            VARCHAR(32)  NOT NULL,
    rarity          VARCHAR(32)  NOT NULL DEFAULT 'COMMON',
    slot            VARCHAR(32),
    equip_level     INT          NOT NULL DEFAULT 0,
    base_stat_bonus JSONB                 DEFAULT '{"str":0,"con":0,"agi":0,"wis":0}'::jsonb,
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
COMMENT ON COLUMN xt_item_template.type IS '物品类型';
COMMENT ON COLUMN xt_item_template.rarity IS '稀有度';
COMMENT ON COLUMN xt_item_template.slot IS '装备部位 (仅装备类)';
COMMENT ON COLUMN xt_item_template.equip_level IS '装备等级（仅装备类，用于计算词条数值）';
COMMENT ON COLUMN xt_item_template.base_stat_bonus IS '基础属性加成';
COMMENT ON COLUMN xt_item_template.base_attack IS '基础攻击力';
COMMENT ON COLUMN xt_item_template.base_defense IS '基础防御力';
COMMENT ON COLUMN xt_item_template.drop_weight IS '掉落权重 JSONB（仅装备类），示例: {"BROKEN": 50, "COMMON": 30, "RARE": 15, "EPIC": 4, "LEGENDARY": 1}';
COMMENT ON COLUMN xt_item_template.tags IS '物品标签 JSONB，用于AI检索和NPC交互，示例: ["ore", "metal", "forge_base"]';
COMMENT ON COLUMN xt_item_template.grow_time IS '生长时间（小时，仅种子/灵蛋）';
COMMENT ON COLUMN xt_item_template.yield_id IS '成熟后产出的物品模板ID（仅种子/灵蛋）';
COMMENT ON COLUMN xt_item_template.survive_rate IS '存活率百分比（仅种子/灵蛋）';
COMMENT ON COLUMN xt_item_template.max_stack IS '最大堆叠数量';
COMMENT ON COLUMN xt_item_template.description IS '物品描述';

-- 创建tags字段的GIN索引（用于标签搜索）
CREATE INDEX idx_item_template_tags ON xt_item_template USING GIN (tags);

-- 装备实例表 (xt_equipment)
CREATE TABLE xt_equipment (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT       NOT NULL,
    template_id         BIGINT       NOT NULL,
    name                VARCHAR(128) NOT NULL,
    slot                VARCHAR(32)  NOT NULL,
    rarity              VARCHAR(32)  NOT NULL DEFAULT 'COMMON',
    stat_bonus          JSONB                 DEFAULT '{"str":0,"con":0,"agi":0,"wis":0}'::jsonb,
    attack_bonus        INT          NOT NULL DEFAULT 0,
    defense_bonus       INT          NOT NULL DEFAULT 0,
    equipped            BOOLEAN      NOT NULL DEFAULT FALSE,
    quality_multiplier  DOUBLE PRECISION,
    affixes             JSONB                 DEFAULT '{}'::jsonb,
    forge_level         INT          DEFAULT 0,
    create_time         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 外键关联
    CONSTRAINT fk_equipment_user FOREIGN KEY (user_id) REFERENCES xt_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_equipment_template FOREIGN KEY (template_id) REFERENCES xt_item_template (id)
);

-- 字段备注
COMMENT ON TABLE xt_equipment IS '装备实例表';
COMMENT ON COLUMN xt_equipment.id IS '装备唯一ID';
COMMENT ON COLUMN xt_equipment.user_id IS '持有者用户ID';
COMMENT ON COLUMN xt_equipment.template_id IS '物品模板ID';
COMMENT ON COLUMN xt_equipment.name IS '装备名称';
COMMENT ON COLUMN xt_equipment.slot IS '装备部位';
COMMENT ON COLUMN xt_equipment.rarity IS '稀有度';
COMMENT ON COLUMN xt_equipment.stat_bonus IS '属性加成 JSONB: {"str":5,"con":3,"agi":2,"wis":0}';
COMMENT ON COLUMN xt_equipment.attack_bonus IS '攻击力加成';
COMMENT ON COLUMN xt_equipment.defense_bonus IS '防御力加成';
COMMENT ON COLUMN xt_equipment.equipped IS '是否已穿戴';
COMMENT ON COLUMN xt_equipment.quality_multiplier IS '品质系数（实际波动值，如1.35）';
COMMENT ON COLUMN xt_equipment.affixes IS '随机词条 JSONB，示例: {"STR": 3, "AGI": 2, "LIFE_STEAL": 5}';
COMMENT ON COLUMN xt_equipment.forge_level IS '锻造强化等级';
COMMENT ON COLUMN xt_equipment.create_time IS '创建时间';
COMMENT ON COLUMN xt_equipment.update_time IS '更新时间';

-- 索引
CREATE INDEX idx_xt_equipment_user_id ON xt_equipment (user_id);
CREATE INDEX idx_xt_equipment_user_equipped ON xt_equipment (user_id, equipped);
CREATE INDEX idx_xt_equipment_user_slot ON xt_equipment (user_id, slot);
CREATE INDEX idx_xt_equipment_stat_bonus ON xt_equipment USING GIN (stat_bonus);
CREATE INDEX idx_equipment_affixes ON xt_equipment USING GIN (affixes);

-- 物品实例表 (xt_inventory_item)
CREATE TABLE xt_inventory_item (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    template_id     BIGINT       NOT NULL,
    item_type       VARCHAR(32)  NOT NULL,
    name            VARCHAR(128) NOT NULL,
    quantity        INT          NOT NULL DEFAULT 1,
    tags            JSONB                 DEFAULT '[]'::jsonb,
    grow_time       INT,
    yield_id        VARCHAR(64),
    survive_rate    INT,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

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
COMMENT ON COLUMN xt_inventory_item.item_type IS '物品类型 (MATERIAL, SEED, SPIRIT_EGG, CONSUMABLE)';
COMMENT ON COLUMN xt_inventory_item.name IS '物品名称 (从模板复制)';
COMMENT ON COLUMN xt_inventory_item.quantity IS '数量';
COMMENT ON COLUMN xt_inventory_item.tags IS '物品标签 JSONB，用于AI检索和NPC交互';
COMMENT ON COLUMN xt_inventory_item.grow_time IS '生长时间（小时，仅种子/灵蛋）';
COMMENT ON COLUMN xt_inventory_item.yield_id IS '成熟后产出的物品模板ID（仅种子/灵蛋）';
COMMENT ON COLUMN xt_inventory_item.survive_rate IS '存活率百分比（仅种子/灵蛋）';
COMMENT ON COLUMN xt_inventory_item.create_time IS '创建时间';
COMMENT ON COLUMN xt_inventory_item.update_time IS '更新时间';

-- 索引
CREATE INDEX idx_inventory_item_user_id ON xt_inventory_item (user_id);
CREATE INDEX idx_inventory_item_user_type ON xt_inventory_item (user_id, item_type);
CREATE INDEX idx_inventory_item_tags ON xt_inventory_item USING GIN (tags);

-- ============================================================================
-- 初始数据
-- ============================================================================

-- 插入初始装备模板数据（武器类）
INSERT INTO xt_item_template (name, type, rarity, slot, equip_level, base_stat_bonus, base_attack, drop_weight, description, max_stack) VALUES
('铁剑', 'EQUIPMENT', 'BROKEN', 'WEAPON', 10, '{"str":3,"con":0,"agi":1,"wis":0}'::jsonb, 5, '{"BROKEN": 50, "COMMON": 30, "RARE": 15, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '一把普通的铁剑', 1),
('灵剑', 'EQUIPMENT', 'RARE', 'WEAPON', 20, '{"str":8,"con":0,"agi":3,"wis":2}'::jsonb, 15, '{"BROKEN": 40, "COMMON": 30, "RARE": 20, "EPIC": 8, "LEGENDARY": 2}'::jsonb, '蕴含灵气的宝剑', 1),
('仙剑', 'EQUIPMENT', 'LEGENDARY', 'WEAPON', 50, '{"str":20,"con":0,"agi":8,"wis":5}'::jsonb, 50, '{"BROKEN": 20, "COMMON": 30, "RARE": 25, "EPIC": 20, "LEGENDARY": 5}'::jsonb, '仙人遗留的神剑', 1);

-- 护甲类
INSERT INTO xt_item_template (name, type, rarity, slot, equip_level, base_stat_bonus, base_defense, drop_weight, description, max_stack) VALUES
('皮甲', 'EQUIPMENT', 'BROKEN', 'ARMOR', 10, '{"str":0,"con":3,"agi":0,"wis":0}'::jsonb, 5, '{"BROKEN": 50, "COMMON": 30, "RARE": 15, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '简单的皮革护甲', 1),
('铁甲', 'EQUIPMENT', 'COMMON', 'ARMOR', 15, '{"str":0,"con":6,"agi":-1,"wis":0}'::jsonb, 12, '{"BROKEN": 50, "COMMON": 30, "RARE": 15, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '沉重的铁制铠甲', 1),
('灵甲', 'EQUIPMENT', 'EPIC', 'ARMOR', 30, '{"str":0,"con":12,"agi":2,"wis":3}'::jsonb, 30, '{"BROKEN": 30, "COMMON": 30, "RARE": 25, "EPIC": 12, "LEGENDARY": 3}'::jsonb, '蕴含灵力的护甲', 1);

-- 饰品类
INSERT INTO xt_item_template (name, type, rarity, slot, equip_level, base_stat_bonus, base_attack, base_defense, drop_weight, description, max_stack) VALUES
('玉佩', 'EQUIPMENT', 'COMMON', 'ACCESSORY', 10, '{"str":0,"con":2,"agi":0,"wis":5}'::jsonb, 0, 0, '{"BROKEN": 40, "COMMON": 35, "RARE": 20, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '温润的玉佩，可凝神静气', 1),
('灵戒', 'EQUIPMENT', 'RARE', 'ACCESSORY', 20, '{"str":2,"con":2,"agi":2,"wis":5}'::jsonb, 0, 0, '{"BROKEN": 30, "COMMON": 30, "RARE": 25, "EPIC": 12, "LEGENDARY": 3}'::jsonb, '蕴含灵力的戒指', 1);

-- 头盔类
INSERT INTO xt_item_template (name, type, rarity, slot, equip_level, base_stat_bonus, base_defense, drop_weight, description, max_stack) VALUES
('铁盔', 'EQUIPMENT', 'BROKEN', 'HELMET', 10, '{"str":0,"con":2,"agi":0,"wis":0}'::jsonb, 3, '{"BROKEN": 50, "COMMON": 30, "RARE": 15, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '铁制的头盔', 1),
('灵冠', 'EQUIPMENT', 'RARE', 'HELMET', 20, '{"str":0,"con":3,"agi":2,"wis":5}'::jsonb, 8, '{"BROKEN": 40, "COMMON": 30, "RARE": 20, "EPIC": 8, "LEGENDARY": 2}'::jsonb, '修仙者佩戴的灵冠', 1);

-- 鞋子类
INSERT INTO xt_item_template (name, type, rarity, slot, equip_level, base_stat_bonus, base_defense, drop_weight, description, max_stack) VALUES
('皮靴', 'EQUIPMENT', 'BROKEN', 'BOOTS', 10, '{"str":0,"con":1,"agi":3,"wis":0}'::jsonb, 1, '{"BROKEN": 50, "COMMON": 30, "RARE": 15, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '轻便的皮靴', 1),
('风行靴', 'EQUIPMENT', 'RARE', 'BOOTS', 20, '{"str":0,"con":2,"agi":8,"wis":0}'::jsonb, 3, '{"BROKEN": 40, "COMMON": 30, "RARE": 20, "EPIC": 8, "LEGENDARY": 2}'::jsonb, '穿上如踏风而行', 1);

-- 消耗品/丹药类
INSERT INTO xt_item_template (name, type, tags, description, max_stack) VALUES
('疗伤丹', 'POTION', '["healing", "potion", "combat"]'::jsonb, '服用后恢复50点生命值，战斗外使用', 20),
('聚气丹', 'POTION', '["healing", "potion", "combat", "rare"]'::jsonb, '服用后恢复100点生命值，战斗外使用', 20),
('大还丹', 'POTION', '["healing", "potion", "rare", "combat"]'::jsonb, '恢复100% HP，战斗外使用', 20),
('敏捷药剂', 'POTION', '["buff", "potion", "agi", "rare"]'::jsonb, '挂机增益，持续2小时内，敏捷+50', 10),
('智慧药剂', 'POTION', '["buff", "potion", "wis", "rare"]'::jsonb, '挂机增益，持续2小时内，智慧+50', 10),
('蛮力药剂', 'POTION', '["buff", "potion", "str", "rare"]'::jsonb, '挂机增益，持续2小时内，力量+50', 10);

-- 草药类
INSERT INTO xt_item_template (name, type, base_stat_bonus, tags, description, max_stack) VALUES
('毒龙草', 'HERB', '{}'::jsonb, '["herb", "poison", "rare"]'::jsonb, '剧毒草药，用于炼制毒药或高级金创药', 99),
('灵草', 'HERB', '{}'::jsonb, '["herb", "spirit", "common"]'::jsonb, '蕴含灵气的草药，炼药常用材料', 99),
('千年人参', 'HERB', '{"str":2,"con":2,"agi":0,"wis":2}'::jsonb, '["herb", "rare", "prestige"]'::jsonb, '千年人参，极为珍贵的炼药材料', 99),
('灵芝', 'HERB', '{"str":0,"con":3,"agi":0,"wis":2}'::jsonb, '["herb", "spirit", "rare"]'::jsonb, '灵气浓郁的仙草，炼制高级丹药必备', 99);

-- 材料类
INSERT INTO xt_item_template (name, type, tags, description, max_stack) VALUES
('精铁', 'MATERIAL', '["ore", "metal", "forge_base"]'::jsonb, '锻造材料', 99),
('灵玉', 'MATERIAL', '["gem", "magic", "forge_enchant"]'::jsonb, '炼器材料', 99),
('黑铁矿石', 'MATERIAL', '["ore", "metal", "forge_base", "rare"]'::jsonb, '稀有锻造材料，用于强化装备', 99),
('秘银', 'MATERIAL', '["ore", "metal", "forge_enchant", "epic"]'::jsonb, '魔法金属，用于制作附魔装备', 50),
('龙骨', 'MATERIAL', '["bone", "dragon", "prestige", "legendary"]'::jsonb, '龙的遗骨，极为珍贵的炼器材料', 10),
('灵石', 'MATERIAL', '["gem", "spirit", "currency", "common"]'::jsonb, '蕴含灵气的宝石，可作为高级货币', 9999);

-- 种子类（福地专供）
INSERT INTO xt_item_template (name, type, tags, grow_time, yield_id, survive_rate, description, max_stack) VALUES
('灵草种子', 'SEED', '["seed", "spirit", "common"]'::jsonb, 24, 'herb_spirit_yield', 90, '灵草种子，成熟需24小时，产出灵草', 99),
('赤火莲子', 'SEED', '["seed", "fire", "rare"]'::jsonb, 48, 'herb_fire_lotus', 80, '火属性种子，成熟需48小时，产出赤火莲', 50),
('玄冰莲子', 'SEED', '["seed", "ice", "rare"]'::jsonb, 48, 'herb_ice_lotus', 80, '冰属性种子，成熟需48小时，产出玄冰莲', 50),
('金芝种子', 'SEED', '["seed", "gold", "legendary"]'::jsonb, 72, 'herb_golden_lotus', 50, '稀有金芝种子，成熟需72小时，产出金芝', 10);

-- 灵蛋类（福地专供）
INSERT INTO xt_item_template (name, type, tags, grow_time, yield_id, survive_rate, description, max_stack) VALUES
('火灵蛋', 'SPIRIT_EGG', '["egg", "fire", "rare"]'::jsonb, 72, 'pet_fire_turtle', 70, '孵化后可获得火属性灵宠，需72小时', 10),
('玄冰龟蛋', 'SPIRIT_EGG', '["egg", "water", "rare"]'::jsonb, 72, 'pet_water_turtle', 70, '孵化后可获得水属性灵宠，需72小时', 10),
('风灵蛋', 'SPIRIT_EGG', '["egg", "wind", "epic"]'::jsonb, 96, 'pet_wind_hawk', 60, '孵化后可获得风属性灵宠，需96小时', 5),
('金龙蛋', 'SPIRIT_EGG', '["egg", "gold", "legendary"]'::jsonb, 168, 'pet_gold_dragon', 30, '孵化后可获得金龙灵宠，需168小时', 1);

-- 珍礼类
INSERT INTO xt_item_template (name, type, tags, description, max_stack) VALUES
('星尘水晶', 'GIFT', '["gem", "shiny", "magic", "rare"]'::jsonb, '璀璨的星尘水晶，适合送给喜欢魔法珍宝的NPC', 5),
('古玉', 'GIFT', '["gem", "ancient", "prestige", "epic"]'::jsonb, '传承千年的古玉，适合送给有文化底蕴的NPC', 3),
('龙鳞', 'GIFT', '["scale", "dragon", "prestige", "legendary"]'::jsonb, '传说中的龙鳞，极为珍贵的礼物', 1),
('灵酒', 'GIFT', '["wine", "spirit", "consumable", "common"]'::jsonb, '灵气浓郁的酒水，适合送给喜欢饮酒的NPC', 20);

-- 草药产出物
INSERT INTO xt_item_template (name, type, base_stat_bonus, tags, description, max_stack) VALUES
('赤火莲', 'HERB', '{"str":3,"con":0,"agi":0,"wis":2}'::jsonb, '["herb", "fire", "rare", "harvest"]'::jsonb, '火属性仙草，炼制火系丹药的主材', 50),
('玄冰莲', 'HERB', '{"str":0,"con":3,"agi":0,"wis":2}'::jsonb, '["herb", "ice", "rare", "harvest"]'::jsonb, '冰属性仙草，炼制冰系丹药的主材', 50),
('金芝', 'HERB', '{"str":5,"con":5,"agi":0,"wis":5}'::jsonb, '["herb", "gold", "legendary", "harvest"]'::jsonb, '传说中的金芝，极为珍贵的仙草', 5);
