-- 进化石
INSERT INTO xt_item_template (name, type, rarity, properties, tags, description, max_stack) VALUES
('进化石', 'EVOLUTION_STONE', 'RARE', '{}'::jsonb, '["evolution","rare"]'::jsonb, '蕴含天地精华的灵石，可用于灵兽进化与品质突破', 999),
('初级进化石', 'EVOLUTION_STONE', 'COMMON', '{}'::jsonb, '["evolution","common"]'::jsonb, '蕴含微弱灵气的进化石，仅可用于T1→T2的突破', 999),
('高级进化石', 'EVOLUTION_STONE', 'EPIC', '{}'::jsonb, '["evolution","epic"]'::jsonb, '蕴含磅礴灵气的进化石，提升T3→T4突破成功率20%', 999),
('传说进化石', 'EVOLUTION_STONE', 'LEGENDARY', '{}'::jsonb, '["evolution","legendary"]'::jsonb, '天地初开时便存在的灵石，必定成功突破', 999),
('属性进化石', 'EVOLUTION_STONE', 'EPIC', '{}'::jsonb, '["evolution","epic"]'::jsonb, '蕴含特定属性灵力的进化石，可为灵兽附加新属性', 999),
