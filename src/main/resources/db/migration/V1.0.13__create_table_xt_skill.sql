-- 法决定义表 (xt_skill)
CREATE TABLE xt_skill
(
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(64)   NOT NULL,
    description      VARCHAR(256),
    skill_type       VARCHAR(16)   NOT NULL DEFAULT 'ACTIVE',
    effect_type      VARCHAR(32)   NOT NULL,
    binding_type     VARCHAR(32)   NOT NULL DEFAULT 'NONE',
    binding_value    VARCHAR(64),
    cooldown_seconds INT           NOT NULL DEFAULT 30,
    damage_formula   VARCHAR(128),
    power_multiplier DOUBLE PRECISION        DEFAULT 1.0,
    level_requirement INT          NOT NULL DEFAULT 1,
    create_time      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE xt_skill IS '法决定义表';
COMMENT ON COLUMN xt_skill.skill_type IS 'ACTIVE / PASSIVE';
COMMENT ON COLUMN xt_skill.effect_type IS 'DAMAGE / ARMOR_BREAK / SLOW / EXECUTE / LIFESTEAL / MULTI_HIT / DOT';
COMMENT ON COLUMN xt_skill.binding_type IS 'NONE / WEAPON_TYPE / WEAPON_CATEGORY / ELEMENT';
COMMENT ON COLUMN xt_skill.binding_value IS 'SWORD / 刀兵 / fire 等，NONE 时为 null';
COMMENT ON COLUMN xt_skill.cooldown_seconds IS '冷却秒数';
COMMENT ON COLUMN xt_skill.damage_formula IS '伤害公式字符串，如 "wis*3 + 20"，非 DAMAGE 类型为 null';
