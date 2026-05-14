-- 创建玩家已学锻造图纸表
CREATE TABLE xt_player_forging_recipe (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL,
    blueprint_template_id   BIGINT NOT NULL,
    equipment_template_id   BIGINT NOT NULL,
    learn_time              TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_forging_recipe UNIQUE (user_id, blueprint_template_id),
    CONSTRAINT fk_player_forging_recipe_user FOREIGN KEY (user_id) REFERENCES xt_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_player_forging_recipe_blueprint FOREIGN KEY (blueprint_template_id) REFERENCES xt_item_template (id),
    CONSTRAINT fk_player_forging_recipe_equipment FOREIGN KEY (equipment_template_id) REFERENCES xt_equipment_template (id)
);

CREATE INDEX idx_player_forging_recipe_user_id ON xt_player_forging_recipe (user_id);
CREATE INDEX idx_player_forging_recipe_blueprint_id ON xt_player_forging_recipe (blueprint_template_id);

COMMENT ON TABLE xt_player_forging_recipe IS '玩家已学锻造图纸表';
COMMENT ON COLUMN xt_player_forging_recipe.user_id IS '用户ID';
COMMENT ON COLUMN xt_player_forging_recipe.blueprint_template_id IS '锻造图纸模板ID';
COMMENT ON COLUMN xt_player_forging_recipe.equipment_template_id IS '产出装备模板ID';
COMMENT ON COLUMN xt_player_forging_recipe.learn_time IS '学习时间';
