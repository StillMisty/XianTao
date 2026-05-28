-- 灵兽繁育配方表
CREATE
    TABLE
        xt_breeding_recipe(
            id SERIAL PRIMARY KEY,
            required_tags JSONB NOT NULL,
            result_template_id BIGINT NOT NULL,
            WEIGHT INT NOT NULL DEFAULT 100,
            CONSTRAINT fk_breeding_result_template FOREIGN KEY(result_template_id) REFERENCES xt_item_template(id)
        );

COMMENT ON
TABLE
    xt_breeding_recipe IS '灵兽繁育配方表：父母 tag 组合 → 后代兽卵';

COMMENT ON
COLUMN xt_breeding_recipe.required_tags IS '匹配所需 tag 集合 JSONB，如 ["flying","water"]';

COMMENT ON
COLUMN xt_breeding_recipe.result_template_id IS '后代兽卵 FK → xt_item_template(id)';

COMMENT ON
COLUMN xt_breeding_recipe.weight IS '多条匹配时的加权随机权重';

CREATE
    INDEX idx_breeding_recipe_tags ON
    xt_breeding_recipe USING gin(required_tags);
