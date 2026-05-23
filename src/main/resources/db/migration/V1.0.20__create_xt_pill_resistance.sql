-- 丹药抗性表
-- 记录玩家对每种丹药(不同品质)的服用次数，用于计算抗性衰减
CREATE
    TABLE
        xt_pill_resistance(
            id BIGSERIAL PRIMARY KEY,
            user_id BIGINT NOT NULL REFERENCES xt_user(id),
            template_id BIGINT NOT NULL REFERENCES xt_item_template(id),
            quality VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
            COUNT INT NOT NULL DEFAULT 0,
            updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
            UNIQUE(
                user_id,
                template_id,
                quality
            ),
            CONSTRAINT chk_pill_resistance_count CHECK(
                COUNT >= 0
            )
        );

COMMENT ON
TABLE
    xt_pill_resistance IS '丹药抗性 — 玩家服用每种丹药各品质的次数';

COMMENT ON
COLUMN xt_pill_resistance.count IS '服用次数，用于计算抗性衰减';

COMMENT ON
COLUMN xt_pill_resistance.quality IS '丹药品质 (SUPERIOR/NORMAL/INFERIOR)，不同品质独立计算抗性';
