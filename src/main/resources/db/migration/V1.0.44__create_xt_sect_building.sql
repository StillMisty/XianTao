/* 宗门建筑表 */
CREATE
    TABLE
        xt_sect_building(
            id BIGSERIAL PRIMARY KEY,
            sect_id BIGINT NOT NULL,
            building_type VARCHAR(24) NOT NULL,
            LEVEL INT NOT NULL DEFAULT 1,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_sect_building_sect FOREIGN KEY(sect_id) REFERENCES xt_sect(id) ON
            DELETE
                CASCADE,
                CONSTRAINT uq_sect_building UNIQUE(
                    sect_id,
                    building_type
                ),
                CONSTRAINT chk_sect_building_type CHECK(
                    building_type IN(
                        'SCRIPTURE_PAVILION',
                        'TRAINING_ROOM',
                        'ALCHEMY_CHAMBER',
                        'SPIRIT_VEIN',
                        'FORGE_WORKSHOP',
                        'GUARD_ARRAY',
                        'HERB_GARDEN'
                    )
                ),
                CONSTRAINT chk_sect_building_level CHECK(
                    LEVEL >= 1
                )
        );

COMMENT ON
TABLE
    xt_sect_building IS '宗门建筑表';

COMMENT ON
COLUMN xt_sect_building.id IS '建筑记录ID';

COMMENT ON
COLUMN xt_sect_building.sect_id IS '宗门ID';

COMMENT ON
COLUMN xt_sect_building.building_type IS '建筑类型';

COMMENT ON
COLUMN xt_sect_building.level IS '建筑等级';

COMMENT ON
COLUMN xt_sect_building.created_at IS '建造时间';

COMMENT ON
COLUMN xt_sect_building.updated_at IS '更新时间';

CREATE
    INDEX idx_sect_building_sect ON
    xt_sect_building(sect_id);
