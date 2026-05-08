-- 怪物模板表 (xt_monster_template)
CREATE TABLE xt_monster_template
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(64)  NOT NULL,
    description  TEXT,
    monster_type VARCHAR(32)  NOT NULL,
    base_level   INT          NOT NULL DEFAULT 1,
    base_hp      INT          NOT NULL DEFAULT 100,
    base_attack  INT          NOT NULL DEFAULT 10,
    base_defense INT          NOT NULL DEFAULT 5,
    base_speed   INT          NOT NULL DEFAULT 10,
    exp_reward   INT          NOT NULL DEFAULT 0,
    skills       JSONB        DEFAULT '[]'::jsonb,
    drop_table   JSONB        DEFAULT '[]'::jsonb,
    tags         JSONB        DEFAULT '[]'::jsonb,
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_monster_template_name UNIQUE (name),
    CONSTRAINT chk_monster_template_type CHECK (monster_type IN ('BEAST', 'SPIRIT', 'ARMORED', 'WILD_BEAST', 'EVIL', 'FLYING', 'HUMAN')),
    CONSTRAINT chk_monster_template_base_level CHECK (base_level >= 1),
    CONSTRAINT chk_monster_template_base_hp CHECK (base_hp >= 0),
    CONSTRAINT chk_monster_template_base_attack CHECK (base_attack >= 0),
    CONSTRAINT chk_monster_template_base_defense CHECK (base_defense >= 0),
    CONSTRAINT chk_monster_template_base_speed CHECK (base_speed >= 0),
    CONSTRAINT chk_monster_template_exp_reward CHECK (exp_reward >= 0)
);

COMMENT ON TABLE xt_monster_template IS '怪物模板表';
COMMENT ON COLUMN xt_monster_template.description IS '怪物描述';
COMMENT ON COLUMN xt_monster_template.monster_type IS 'BEAST / SPIRIT / ARMORED / WILD_BEAST / EVIL / FLYING / HUMAN';
COMMENT ON COLUMN xt_monster_template.exp_reward IS '击杀经验';
COMMENT ON COLUMN xt_monster_template.skills IS '法决ID列表 JSONB，如 [1, 2, 3]';
COMMENT ON COLUMN xt_monster_template.drop_table IS '掉落表 JSONB: [{"category": "equipment", "templateId": 1, "weight": 50}]';
COMMENT ON COLUMN xt_monster_template.tags IS '标签列表 JSONB，如 ["beast", "fire"]';
