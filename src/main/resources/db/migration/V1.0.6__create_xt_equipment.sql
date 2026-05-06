-- 装备实例表 (xt_equipment)
CREATE TABLE xt_equipment
(
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT       NOT NULL,
    template_id        BIGINT       NOT NULL,
    name               VARCHAR(128) NOT NULL,
    slot               VARCHAR(32)  NOT NULL,
    weapon_type        VARCHAR(32),
    rarity             VARCHAR(32)  NOT NULL DEFAULT 'COMMON',
    stat_bonus         JSONB                 DEFAULT '{}'::jsonb,
    attack_bonus       INT          NOT NULL DEFAULT 0,
    defense_bonus      INT          NOT NULL DEFAULT 0,
    equipped           BOOLEAN      NOT NULL DEFAULT FALSE,
    quality_multiplier DOUBLE PRECISION,
    affixes            JSONB                 DEFAULT '{}'::jsonb,
    forge_level        INT          NOT NULL DEFAULT 0,
    create_time        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 外键关联
    CONSTRAINT fk_equipment_user FOREIGN KEY (user_id) REFERENCES xt_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_equipment_template FOREIGN KEY (template_id) REFERENCES xt_equipment_template (id),
    CONSTRAINT chk_equipment_slot CHECK (slot IN ('WEAPON', 'ARMOR', 'ACCESSORY')),
    CONSTRAINT chk_equipment_rarity CHECK (rarity IN ('BROKEN', 'COMMON', 'RARE', 'EPIC', 'LEGENDARY')),
    CONSTRAINT chk_equipment_weapon_type CHECK (weapon_type IS NULL OR weapon_type IN ('BLADE', 'SWORD', 'AXE', 'SPEAR', 'STAFF', 'BOW', 'WHIP', 'HALBERD', 'HAMMER', 'DAGGER', 'FAN', 'FLYWHISK', 'RING', 'BELL')),
    CONSTRAINT chk_equipment_forge_level CHECK (forge_level >= 0),
    CONSTRAINT chk_equipment_attack_bonus CHECK (attack_bonus >= 0),
    CONSTRAINT chk_equipment_defense_bonus CHECK (defense_bonus >= 0)
);

-- 字段备注
COMMENT ON TABLE xt_equipment IS '装备实例表';
COMMENT ON COLUMN xt_equipment.id IS '装备唯一ID';
COMMENT ON COLUMN xt_equipment.user_id IS '持有者用户ID';
COMMENT ON COLUMN xt_equipment.template_id IS '装备模板ID → xt_equipment_template(id)';
COMMENT ON COLUMN xt_equipment.name IS '装备名称';
COMMENT ON COLUMN xt_equipment.slot IS '装备部位';
COMMENT ON COLUMN xt_equipment.weapon_type IS '法器子类型 BLADE/SWORD/AXE/...（护甲/饰品为 null）';
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
