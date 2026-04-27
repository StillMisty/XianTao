CREATE TABLE xt_user (
    id                      BIGSERIAL PRIMARY KEY,
    nickname                VARCHAR(64) NOT NULL UNIQUE ,
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
    stamina_current         INT         NOT NULL DEFAULT 100,
    status                  VARCHAR(32) NOT NULL DEFAULT 'idle',
    location_id             BIGINT      NOT NULL DEFAULT 0,
    training_start_time     TIMESTAMP,
    breakthrough_fail_count INT         NOT NULL DEFAULT 0,
    last_reset_points_time  TIMESTAMP,
    last_stamina_update_time TIMESTAMP,
    travel_start_time       TIMESTAMP,
    travel_destination_id   BIGINT,

    -- 扩展数据
    extra_data              JSONB                DEFAULT '{}'::jsonb,
    -- 时间戳
    create_time             TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 唯一约束：道号必须唯一
    CONSTRAINT uq_nickname UNIQUE (nickname)
);

-- 索引:排行榜常用
CREATE INDEX idx_xt_user_level ON xt_user (level DESC);
CREATE INDEX idx_xt_user_coins ON xt_user (coins DESC);
CREATE INDEX idx_xt_user_spirit_stones ON xt_user (spirit_stones DESC);

-- 字段备注
COMMENT ON TABLE xt_user IS '游戏角色核心表';
COMMENT ON COLUMN xt_user.id IS '内部唯一角色ID';
COMMENT ON COLUMN xt_user.nickname IS '玩家道号/昵称（唯一）';
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
COMMENT ON COLUMN xt_user.stamina_current IS '当前体力值';
COMMENT ON COLUMN xt_user.status IS '当前状态';
COMMENT ON COLUMN xt_user.location_id IS '当前所在地图 id';
COMMENT ON COLUMN xt_user.training_start_time IS '历练开始时间戳 (用于结算收益)';
COMMENT ON COLUMN xt_user.breakthrough_fail_count IS '突破失败次数 (影响下一次突破成功率)';
COMMENT ON COLUMN xt_user.last_reset_points_time IS '上次洗点时间戳 (用于洗点冷却判定，3天冷却)';
COMMENT ON COLUMN xt_user.last_stamina_update_time IS '上次体力更新时间戳（用于离线恢复计算）';
COMMENT ON COLUMN xt_user.travel_start_time IS '旅行开始时间戳 (用于计算旅行进度)';
COMMENT ON COLUMN xt_user.travel_destination_id IS '旅行目的地地图 ID';
COMMENT ON COLUMN xt_user.extra_data IS 'JSONB 扩展字段 (存储称号、成就、小规模系统数据)';
COMMENT ON COLUMN xt_user.create_time IS '角色创建时间';
COMMENT ON COLUMN xt_user.update_time IS '最后一次数据更新时间';

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
    rarity          VARCHAR(32)  NOT NULL DEFAULT 'common',
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
COMMENT ON COLUMN xt_item_template.type IS '物品类型 (EQUIPMENT, MATERIAL, SEED, SPIRIT_EGG, CONSUMABLE, HERB, POTION, GIFT, EVOLUTION_STONE, BEAST_EGG, BEAST_MATERIAL)';
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

-- 装备实例表 (xt_equipment)
CREATE TABLE xt_equipment (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT       NOT NULL,
    template_id         BIGINT       NOT NULL,
    name                VARCHAR(128) NOT NULL,
    slot                VARCHAR(32)  NOT NULL,
    rarity              VARCHAR(32)  NOT NULL DEFAULT 'common',
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
COMMENT ON COLUMN xt_inventory_item.item_type IS '物品类型 (MATERIAL, SEED, SPIRIT_EGG, CONSUMABLE, HERB, POTION, GIFT, EVOLUTION_STONE, BEAST_EGG, BEAST_MATERIAL)';
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
