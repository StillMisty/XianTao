-- 法决定义表 (skill)
CREATE
    TABLE
        skill(
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(64) NOT NULL UNIQUE,
            description VARCHAR(256),
            skill_type VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
            effects JSONB NOT NULL DEFAULT '[]',
            binding_type VARCHAR(32) NOT NULL DEFAULT 'NONE',
            binding_value VARCHAR(64),
            cooldown_seconds INT NOT NULL DEFAULT 30,
            require_wis INTEGER,
            require_skill_id BIGINT,
            tags JSONB NOT NULL DEFAULT '[]',
            level_requirement INT NOT NULL DEFAULT 1,
            create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_skill_prerequisite FOREIGN KEY(require_skill_id) REFERENCES skill(id),
            CONSTRAINT chk_skill_type CHECK(
                skill_type IN(
                    'ACTIVE',
                    'PASSIVE'
                )
            ),
            CONSTRAINT chk_skill_binding_type CHECK(
                binding_type IN(
                    'NONE',
                    'WEAPON_TYPE',
                    'WEAPON_CATEGORY',
                    'ELEMENT'
                )
            ),
            CONSTRAINT chk_skill_cooldown CHECK(
                cooldown_seconds >= 0
            ),
            CONSTRAINT chk_skill_level_requirement CHECK(
                level_requirement >= 1
            )
        );

CREATE
    INDEX idx_skill_prerequisite ON
    skill(require_skill_id);

CREATE
    INDEX idx_skill_type ON
    skill(skill_type);

CREATE
    INDEX idx_skill_learnable ON
    skill(level_requirement, require_wis, require_skill_id);

CREATE
    INDEX idx_skill_binding ON
    skill(binding_type, binding_value);

COMMENT ON
TABLE
    skill IS '法决定义表';

COMMENT ON
COLUMN skill.skill_type IS 'ACTIVE / PASSIVE';

COMMENT ON
COLUMN skill.effects IS '效果列表 JSONB，支持多效果组合';

COMMENT ON
COLUMN skill.binding_type IS 'NONE / WEAPON_TYPE / WEAPON_CATEGORY / ELEMENT';

COMMENT ON
COLUMN skill.binding_value IS 'SWORD / 刀兵 / fire 等，NONE 时为 null';

COMMENT ON
COLUMN skill.cooldown_seconds IS '冷却秒数';

COMMENT ON
COLUMN skill.require_wis IS '智慧要求，NULL表示无要求';

COMMENT ON
COLUMN skill.require_skill_id IS '前置法决ID，NULL表示无前置';

COMMENT ON
COLUMN skill.tags IS '标签数组，JSONB格式';
