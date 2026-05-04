-- 装备模板表 (xt_equipment_template)
CREATE TABLE xt_equipment_template
(
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(128) NOT NULL,
    description    TEXT,
    tags           JSONB        DEFAULT '[]'::jsonb,
    slot           VARCHAR(32)  NOT NULL,
    weapon_type    VARCHAR(32),
    category       VARCHAR(16),
    equip_level    INT          NOT NULL DEFAULT 1,
    base_attack    INT          NOT NULL DEFAULT 0,
    base_defense   INT          NOT NULL DEFAULT 0,
    base_str       INT          NOT NULL DEFAULT 0,
    base_con       INT          NOT NULL DEFAULT 0,
    base_agi       INT          NOT NULL DEFAULT 0,
    base_wis       INT          NOT NULL DEFAULT 0,
    attack_speed   DECIMAL(3,1),
    attack_range   VARCHAR(16),
    drop_weight    JSONB        DEFAULT '{}'::jsonb,
    create_time    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_equip_tmpl_slot CHECK (slot IN ('weapon', 'armor', 'accessory')),
    CONSTRAINT chk_equip_tmpl_weapon_type CHECK (weapon_type IS NULL OR weapon_type IN ('blade', 'sword', 'axe', 'spear', 'staff', 'bow', 'whip', 'halberd', 'hammer', 'dagger', 'fan', 'flywhisk', 'ring', 'bell')),
    CONSTRAINT chk_equip_tmpl_category CHECK (category IS NULL OR category IN ('刀兵', '长兵', '远兵', '奇兵')),
    CONSTRAINT chk_equip_tmpl_equip_level CHECK (equip_level >= 1),
    CONSTRAINT chk_equip_tmpl_base_attack CHECK (base_attack >= 0),
    CONSTRAINT chk_equip_tmpl_base_defense CHECK (base_defense >= 0),
    CONSTRAINT chk_equip_tmpl_base_str CHECK (base_str >= 0),
    CONSTRAINT chk_equip_tmpl_base_con CHECK (base_con >= 0),
    CONSTRAINT chk_equip_tmpl_base_agi CHECK (base_agi >= 0),
    CONSTRAINT chk_equip_tmpl_base_wis CHECK (base_wis >= 0),
    CONSTRAINT chk_equip_tmpl_attack_speed CHECK (attack_speed IS NULL OR attack_speed > 0)
);

COMMENT ON TABLE xt_equipment_template IS '装备模板表（法器/护甲/饰品专用属性）';
COMMENT ON COLUMN xt_equipment_template.id IS '装备模板ID，xt_equipment.template_id 引用此列';
COMMENT ON COLUMN xt_equipment_template.name IS '装备名称';
COMMENT ON COLUMN xt_equipment_template.description IS '装备描述';
COMMENT ON COLUMN xt_equipment_template.tags IS '装备标签 JSONB，用于AI检索';
COMMENT ON COLUMN xt_equipment_template.slot IS '装备部位 WEAPON/ARMOR/ACCESSORY';
COMMENT ON COLUMN xt_equipment_template.weapon_type IS '法器子类型 BLADE/SWORD/AXE/SPEAR/STAFF/BOW...（法器专属，护甲/饰品为 null）';
COMMENT ON COLUMN xt_equipment_template.category IS '法器大类 刀兵/长兵/远兵/奇兵（法器专属）';
COMMENT ON COLUMN xt_equipment_template.equip_level IS '装备等级';
COMMENT ON COLUMN xt_equipment_template.base_attack IS '基础攻击力';
COMMENT ON COLUMN xt_equipment_template.base_defense IS '基础防御力';
COMMENT ON COLUMN xt_equipment_template.base_str IS '基础力量加成';
COMMENT ON COLUMN xt_equipment_template.base_con IS '基础体质加成';
COMMENT ON COLUMN xt_equipment_template.base_agi IS '基础敏捷加成';
COMMENT ON COLUMN xt_equipment_template.base_wis IS '基础智慧加成';
COMMENT ON COLUMN xt_equipment_template.attack_speed IS '攻速，如 1.2 快 / 0.7 慢（法器专属）';
COMMENT ON COLUMN xt_equipment_template.attack_range IS '近战/远程（法器专属）';
COMMENT ON COLUMN xt_equipment_template.drop_weight IS '稀有度掉落权重 JSONB';

-- 索引
CREATE INDEX idx_equipment_template_slot ON xt_equipment_template (slot);
CREATE INDEX idx_equipment_template_weapon_type ON xt_equipment_template (weapon_type);
CREATE INDEX idx_equipment_template_category ON xt_equipment_template (category);
CREATE INDEX idx_equipment_template_tags ON xt_equipment_template USING GIN (tags);
