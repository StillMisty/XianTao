-- 创建玩家已学丹方表
CREATE TABLE xt_player_pill_recipe (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL,
    recipe_template_id BIGINT NOT NULL,
    result_item_id    BIGINT NOT NULL,
    learn_time        TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_recipe UNIQUE (user_id, recipe_template_id),
    CONSTRAINT fk_player_pill_recipe_user FOREIGN KEY (user_id) REFERENCES xt_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_player_pill_recipe_template FOREIGN KEY (recipe_template_id) REFERENCES xt_item_template (id),
    CONSTRAINT fk_player_pill_recipe_result FOREIGN KEY (result_item_id) REFERENCES xt_item_template (id)
);

-- 索引
CREATE INDEX idx_player_pill_recipe_user_id ON xt_player_pill_recipe (user_id);
CREATE INDEX idx_player_pill_recipe_template_id ON xt_player_pill_recipe (recipe_template_id);

-- 注释
COMMENT ON TABLE xt_player_pill_recipe IS '玩家已学丹方表';
COMMENT ON COLUMN xt_player_pill_recipe.user_id IS '用户ID';
COMMENT ON COLUMN xt_player_pill_recipe.recipe_template_id IS '丹方卷轴模板ID';
COMMENT ON COLUMN xt_player_pill_recipe.result_item_id IS '成品丹药模板ID';
COMMENT ON COLUMN xt_player_pill_recipe.learn_time IS '学习时间';