-- 灵兽繁育配方表
CREATE
    TABLE
        breeding_recipe(
            id BIGSERIAL PRIMARY KEY,
            required_tags JSONB NOT NULL,
            result_template_id BIGINT NOT NULL,
            WEIGHT INT NOT NULL DEFAULT 100,
            CONSTRAINT fk_breeding_result_template FOREIGN KEY(result_template_id) REFERENCES item_template(id)
        );

COMMENT ON
TABLE
    breeding_recipe IS '灵兽繁育配方表：父母 tag 组合 → 后代兽卵';

COMMENT ON
COLUMN breeding_recipe.required_tags IS '匹配所需 tag 集合 JSONB，如 ["flying","water"]';

COMMENT ON
COLUMN breeding_recipe.result_template_id IS '后代兽卵 FK → item_template(id)';

COMMENT ON
COLUMN breeding_recipe.weight IS '多条匹配时的加权随机权重';

CREATE
    INDEX idx_breeding_recipe_tags ON
    breeding_recipe USING gin(required_tags);
