-- 地灵形态定义表
CREATE TABLE xt_spirit_form
(
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(50) NOT NULL UNIQUE,
    description   TEXT        NOT NULL,
    liked_tags    JSONB       NOT NULL DEFAULT '[]'::jsonb,
    disliked_tags JSONB       NOT NULL DEFAULT '[]'::jsonb
);

-- JSONB字段GIN索引
CREATE INDEX idx_spirit_form_liked_tags ON xt_spirit_form USING GIN (liked_tags);
CREATE INDEX idx_spirit_form_disliked_tags ON xt_spirit_form USING GIN (disliked_tags);

-- 注释
COMMENT ON TABLE xt_spirit_form IS '地灵形态定义表';
COMMENT ON COLUMN xt_spirit_form.name IS '形态名（如小狐妖、春秋蝉）';
COMMENT ON COLUMN xt_spirit_form.description IS '梗向描述';
COMMENT ON COLUMN xt_spirit_form.liked_tags IS '该形态喜欢的物品tag候选池';
COMMENT ON COLUMN xt_spirit_form.disliked_tags IS '该形态讨厌的物品tag候选池';
