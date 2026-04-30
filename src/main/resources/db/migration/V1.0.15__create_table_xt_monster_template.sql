-- 怪物模板表 (xt_monster_template)
CREATE TABLE xt_monster_template
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(64)  NOT NULL,
    monster_type VARCHAR(32)  NOT NULL,
    base_level   INT          NOT NULL DEFAULT 1,
    base_hp      INT          NOT NULL DEFAULT 100,
    base_attack  INT          NOT NULL DEFAULT 10,
    base_defense INT          NOT NULL DEFAULT 5,
    base_speed   INT          NOT NULL DEFAULT 10,
    skills       JSONB        DEFAULT '[]'::jsonb,
    drop_table   JSONB        DEFAULT '{}'::jsonb,
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE xt_monster_template IS '怪物模板表';
COMMENT ON COLUMN xt_monster_template.monster_type IS 'BEAST / SPIRIT / ARMORED / WILD_BEAST / EVIL / FLYING / HUMAN';
COMMENT ON COLUMN xt_monster_template.skills IS '法决ID列表 JSONB，如 [1, 2, 3]';
COMMENT ON COLUMN xt_monster_template.drop_table IS '掉落表 JSONB: {"equipment": {"1": 50, "2": 30}, "items": {"10": 80, "20": 30}}';
