-- 灵兽变异特性配置表 (xt_mutation_trait_config)
CREATE
    TABLE
        xt_mutation_trait_config(
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(64) NOT NULL UNIQUE,
            chinese_name VARCHAR(32) NOT NULL,
            description VARCHAR(256) NOT NULL,
            category VARCHAR(32) NOT NULL,
            effects JSONB NOT NULL DEFAULT '[]',
            required_tags JSONB,
            required_quality VARCHAR(32),
            is_active BOOLEAN NOT NULL DEFAULT TRUE,
            sort_order INT NOT NULL DEFAULT 0,
            create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT chk_mutation_trait_category CHECK(
                category IN(
                    'ATTACK',
                    'DEFENSE',
                    'SPEED',
                    'PRODUCTION',
                    'BREAKTHROUGH',
                    'EXP',
                    'BREEDING',
                    'COMBAT'
                )
            ),
            CONSTRAINT chk_mutation_trait_quality CHECK(
                required_quality IS NULL
                OR required_quality IN(
                    'MORTAL',
                    'SPIRIT',
                    'IMMORTAL',
                    'SAINT',
                    'DIVINE'
                )
            )
        );

COMMENT ON
TABLE
    xt_mutation_trait_config IS '灵兽变异特性配置表';

COMMENT ON
COLUMN xt_mutation_trait_config.name IS '特性代码（唯一标识）';

COMMENT ON
COLUMN xt_mutation_trait_config.chinese_name IS '中文名称';

COMMENT ON
COLUMN xt_mutation_trait_config.description IS '效果描述';

COMMENT ON
COLUMN xt_mutation_trait_config.category IS '分类：ATTACK/DEFENSE/SPEED/PRODUCTION/BREAKTHROUGH/EXP/BREEDING/COMBAT';

COMMENT ON
COLUMN xt_mutation_trait_config.effects IS '效果数组 JSONB，示例: [{"type":"ATTACK_PERCENT","value":15}]';

COMMENT ON
COLUMN xt_mutation_trait_config.required_tags IS '所需tags JSONB，null=通用，需全部包含';

COMMENT ON
COLUMN xt_mutation_trait_config.required_quality IS '最低品质要求，null=无限制';

COMMENT ON
COLUMN xt_mutation_trait_config.is_active IS '是否启用';

COMMENT ON
COLUMN xt_mutation_trait_config.sort_order IS '排序顺序';

CREATE
    INDEX idx_mutation_trait_category ON
    xt_mutation_trait_config(category);

CREATE
    INDEX idx_mutation_trait_active ON
    xt_mutation_trait_config(is_active);

CREATE
    INDEX idx_mutation_trait_tags ON
    xt_mutation_trait_config
        USING GIN(required_tags);
