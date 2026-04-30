-- 首批怪物模板 + 遇怪池数据
INSERT INTO xt_monster_template (name, monster_type, base_level, base_hp, base_attack, base_defense, base_speed, skills, drop_table)
VALUES
    ('青狼', 'BEAST', 3, 80, 12, 6, 14, '[]', '{"equipment": {}, "items": {"1": 30}}'),
    ('木灵', 'SPIRIT', 5, 100, 15, 5, 12, '[]', '{"equipment": {}, "items": {"2": 25}}'),
    ('铁甲虫', 'ARMORED', 4, 120, 10, 15, 8, '[]', '{"equipment": {}, "items": {"3": 30}}'),
    ('石魔', 'ARMORED', 8, 200, 20, 20, 6, '[]', '{"equipment": {}, "items": {"4": 25}}'),
    ('疾风鹰', 'FLYING', 10, 150, 25, 5, 20, '[]', '{"equipment": {}, "items": {"5": 30}}'),
    ('怨灵', 'EVIL', 15, 180, 30, 8, 16, '[]', '{"equipment": {}, "items": {"6": 25}}'),
    ('幽魂', 'SPIRIT', 12, 140, 22, 3, 18, '[]', '{"equipment": {}, "items": {"7": 30}}');

-- 设置遇怪池：幽暗沼泽 (id=2)
UPDATE xt_map_node SET monster_encounters = '{"1": 40, "2": 30, "3": 30}', encounter_size = '{"min": 1, "max": 2}' WHERE id = 2;
-- 枯骨林 (id=3)
UPDATE xt_map_node SET monster_encounters = '{"4": 50, "5": 50}', encounter_size = '{"min": 1, "max": 3}' WHERE id = 3;
-- 迷雾洞窟 (id=4)
UPDATE xt_map_node SET monster_encounters = '{"6": 40, "7": 60}', encounter_size = '{"min": 1, "max": 2}' WHERE id = 4;
