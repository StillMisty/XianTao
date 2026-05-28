-- 灵兽模板表 (xt_beast_template)
-- 存储灵兽孵化配置，兽卵通过 beast_template_id 引用
CREATE
    TABLE
        xt_beast_template(
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(128) NOT NULL UNIQUE,
            grow_time INT NOT NULL,
            production_items JSONB NOT NULL DEFAULT '[]' ::jsonb,
            skill_pool JSONB NOT NULL DEFAULT '{}' ::jsonb,
            tags JSONB DEFAULT '[]' ::jsonb,
            description TEXT,
            create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT chk_beast_template_grow_time CHECK(
                grow_time > 0
            )
        );

COMMENT ON
TABLE
    xt_beast_template IS '灵兽模板表，存储孵化配置';

COMMENT ON
COLUMN xt_beast_template.name IS '灵兽名称（唯一）';

COMMENT ON
COLUMN xt_beast_template.grow_time IS '孵化时间（小时）';

COMMENT ON
COLUMN xt_beast_template.production_items IS '产出物品配置 JSONB: [{"weight":70,"template_id":1}]';

COMMENT ON
COLUMN xt_beast_template.skill_pool IS '技能池配置 JSONB: {"innate_skills":[{"skill_id":1,"unlock":"BIRTH"}],"awakening_skills":[{"skill_id":1,"weight":50}]}';

COMMENT ON
COLUMN xt_beast_template.tags IS '灵兽标签 JSONB，用于繁育配方匹配和变异特质筛选: ["beast","flying","water"]';

COMMENT ON
COLUMN xt_beast_template.description IS '灵兽描述';

CREATE
    INDEX idx_beast_template_name ON
    xt_beast_template(name);

CREATE
    INDEX idx_beast_template_tags ON
    xt_beast_template USING GIN(tags);
