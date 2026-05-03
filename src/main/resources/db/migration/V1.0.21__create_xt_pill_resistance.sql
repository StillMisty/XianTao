-- 丹药抗性表
-- 记录玩家对每种丹药的服用次数，用于计算抗性衰减
CREATE TABLE xt_pill_resistance (
    user_id     BIGINT NOT NULL REFERENCES xt_user(id),
    template_id BIGINT NOT NULL REFERENCES xt_item_template(id),
    count       INT NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, template_id),
    CONSTRAINT chk_pill_resistance_count CHECK (count >= 0)
);

COMMENT ON TABLE xt_pill_resistance IS '丹药抗性 — 玩家服用每种丹药的次数';
COMMENT ON COLUMN xt_pill_resistance.count IS '服用次数，用于计算抗性衰减：1/(1+count)';
