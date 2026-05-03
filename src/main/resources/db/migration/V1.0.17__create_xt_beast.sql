-- 灵兽实体表 (xt_beast)
CREATE TABLE xt_beast
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES xt_user (id),
    fudi_id         BIGINT       NOT NULL REFERENCES xt_fudi (id),
    template_id     BIGINT       NOT NULL REFERENCES xt_item_template (id),
    beast_name      VARCHAR(128),
    tier            INT          NOT NULL DEFAULT 1,
    quality         VARCHAR(32)  NOT NULL DEFAULT 'mortal',
    is_mutant       BOOLEAN      NOT NULL DEFAULT FALSE,
    mutation_traits JSONB        DEFAULT '[]'::jsonb,
    level           INT          NOT NULL DEFAULT 1,
    exp             INT          NOT NULL DEFAULT 0,
    attack          INT          NOT NULL DEFAULT 10,
    defense         INT          NOT NULL DEFAULT 8,
    max_hp          INT          NOT NULL DEFAULT 100,
    hp_current      INT          NOT NULL DEFAULT 100,
    skills          JSONB        DEFAULT '[]'::jsonb,
    is_deployed     BOOLEAN      NOT NULL DEFAULT FALSE,
    recovery_until  TIMESTAMP,
    penned_cell_id  INT,
    birth_time      TIMESTAMP,
    evolution_count INT          NOT NULL DEFAULT 0,
    level_cap       INT          NOT NULL DEFAULT 20,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_beast_tier CHECK (tier > 0),
    CONSTRAINT chk_beast_level CHECK (level > 0),
    CONSTRAINT chk_beast_hp CHECK (hp_current >= 0),
    CONSTRAINT chk_beast_quality CHECK (quality IN ('mortal', 'spirit', 'immortal', 'saint', 'divine')),
    CONSTRAINT chk_beast_exp CHECK (exp >= 0),
    CONSTRAINT chk_beast_attack CHECK (attack >= 0),
    CONSTRAINT chk_beast_defense CHECK (defense >= 0),
    CONSTRAINT chk_beast_max_hp CHECK (max_hp > 0),
    CONSTRAINT chk_beast_evolution_count CHECK (evolution_count >= 0),
    CONSTRAINT chk_beast_level_cap CHECK (level_cap >= 1),
    CONSTRAINT chk_beast_penned_cell_id CHECK (penned_cell_id IS NULL OR penned_cell_id >= 1)
);

COMMENT ON TABLE xt_beast IS '灵兽实体表（战斗化）';
COMMENT ON COLUMN xt_beast.user_id IS 'FK → xt_user(id)';
COMMENT ON COLUMN xt_beast.fudi_id IS 'FK → xt_fudi(id)，所属福地';
COMMENT ON COLUMN xt_beast.template_id IS 'FK → xt_item_template(id)，孵化源卵模板';
COMMENT ON COLUMN xt_beast.quality IS '品质：mortal/spirit/immortal/saint/divine';
COMMENT ON COLUMN xt_beast.mutation_traits IS '变异特质列表 JSONB';
COMMENT ON COLUMN xt_beast.skills IS '技能ID列表 JSONB，引用 xt_skill';
COMMENT ON COLUMN xt_beast.is_deployed IS '是否出战（福地选择）';
COMMENT ON COLUMN xt_beast.recovery_until IS '休养截止时间（阵亡后设置）';
COMMENT ON COLUMN xt_beast.penned_cell_id IS '所属栏位编号，null = 栏外休憩';
COMMENT ON COLUMN xt_beast.level_cap IS '等级上限（tier × 10 + 10）';

CREATE INDEX idx_beast_user_id ON xt_beast (user_id);
CREATE INDEX idx_beast_fudi_id ON xt_beast (fudi_id);
CREATE INDEX idx_beast_deployed ON xt_beast (is_deployed);
