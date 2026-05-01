-- 法决定义表 (xt_skill)
CREATE TABLE xt_skill
(
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(64)   NOT NULL,
    description      VARCHAR(256),
    skill_type       VARCHAR(16)   NOT NULL DEFAULT 'ACTIVE',
    effects          JSONB         NOT NULL DEFAULT '[]',
    binding_type     VARCHAR(32)   NOT NULL DEFAULT 'NONE',
    binding_value    VARCHAR(64),
    cooldown_seconds INT           NOT NULL DEFAULT 30,
    require_wis INTEGER,
    require_skill_id BIGINT,
    tags JSONB DEFAULT '[]',
    level_requirement INT          NOT NULL DEFAULT 1,
    create_time      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE xt_skill IS '法决定义表';
COMMENT ON COLUMN xt_skill.skill_type IS 'ACTIVE / PASSIVE';
COMMENT ON COLUMN xt_skill.effects IS '效果列表 JSONB，支持多效果组合';
COMMENT ON COLUMN xt_skill.binding_type IS 'NONE / WEAPON_TYPE / WEAPON_CATEGORY / ELEMENT';
COMMENT ON COLUMN xt_skill.binding_value IS 'SWORD / 刀兵 / fire 等，NONE 时为 null';
COMMENT ON COLUMN xt_skill.cooldown_seconds IS '冷却秒数';
COMMENT ON COLUMN xt_skill.require_wis IS '智慧要求，NULL表示无要求';
COMMENT ON COLUMN xt_skill.require_skill_id IS '前置法决ID，NULL表示无前置';
COMMENT ON COLUMN xt_skill.tags IS '标签数组，JSONB格式';
