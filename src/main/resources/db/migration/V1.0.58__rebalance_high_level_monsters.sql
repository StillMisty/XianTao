-- 100+ 级怪物平衡调优（满配实测：原数值下防御 buff 叠加 + 大招导致 20 回合平局频发）
-- 调整：血量 ×0.8、攻击 ×0.85、技能组去除 0 冷却防御 buff（1/2/7 中的防御类）与诛仙剑诀（18），
-- 改为中强度主动（12 天刀九式 / 15 万剑归宗 / 17 青莲剑歌）与轻量速度 buff（2）。
UPDATE monster_template SET base_hp = 14000, base_attack = 425, skills = '[12]'::jsonb WHERE name = '太虚烛龙';
UPDATE monster_template SET base_hp = 16400, base_attack = 465, skills = '[15]'::jsonb WHERE name = '元凤';
UPDATE monster_template SET base_hp = 19200, base_attack = 505, skills = '[12, 2]'::jsonb WHERE name = '混元兽';
UPDATE monster_template SET base_hp = 22400, base_attack = 550, skills = '[17]'::jsonb WHERE name = '大罗金仙';
UPDATE monster_template SET base_hp = 26000, base_attack = 605, skills = '[12, 15]'::jsonb WHERE name = '无极魔尊';
UPDATE monster_template SET base_hp = 30400, base_attack = 660, skills = '[12]'::jsonb WHERE name = '混世魔猿';
UPDATE monster_template SET base_hp = 35600, base_attack = 720, skills = '[17]'::jsonb WHERE name = '太上道君';
UPDATE monster_template SET base_hp = 41600, base_attack = 785, skills = '[15, 2]'::jsonb WHERE name = '无量天尊';
UPDATE monster_template SET base_hp = 48800, base_attack = 860, skills = '[12, 17]'::jsonb WHERE name = '大罗天魔';
UPDATE monster_template SET base_hp = 56800, base_attack = 935, skills = '[15]'::jsonb WHERE name = '天劫化身';
UPDATE monster_template SET base_hp = 66400, base_attack = 1020, skills = '[12, 15]'::jsonb WHERE name = '洪荒祖龙';
UPDATE monster_template SET base_hp = 84000, base_attack = 1175, skills = '[17, 12]'::jsonb WHERE name = '鸿蒙道尊';
