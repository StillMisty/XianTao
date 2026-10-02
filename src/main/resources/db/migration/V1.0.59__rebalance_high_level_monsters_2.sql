-- 100+ 级怪物平衡二次调优（V1.0.58 后仍出现 9~16 回合长局与 20 回合平局）
-- 调整：血量降至原始 ×0.55、攻击 ×0.7、每只仅保留 1 个中强度技能；
-- 目标：满配队伍 2~5 回合结束、单场战损 <15%，可长时间挂机。
UPDATE monster_template SET base_hp = 9600, base_attack = 350, skills = '[12]'::jsonb WHERE name = '太虚烛龙';
UPDATE monster_template SET base_hp = 11300, base_attack = 380, skills = '[15]'::jsonb WHERE name = '元凤';
UPDATE monster_template SET base_hp = 13200, base_attack = 415, skills = '[17]'::jsonb WHERE name = '混元兽';
UPDATE monster_template SET base_hp = 15400, base_attack = 455, skills = '[12]'::jsonb WHERE name = '大罗金仙';
UPDATE monster_template SET base_hp = 17900, base_attack = 495, skills = '[15]'::jsonb WHERE name = '无极魔尊';
UPDATE monster_template SET base_hp = 20900, base_attack = 540, skills = '[17]'::jsonb WHERE name = '混世魔猿';
UPDATE monster_template SET base_hp = 24500, base_attack = 590, skills = '[12]'::jsonb WHERE name = '太上道君';
UPDATE monster_template SET base_hp = 28600, base_attack = 645, skills = '[15]'::jsonb WHERE name = '无量天尊';
UPDATE monster_template SET base_hp = 33600, base_attack = 705, skills = '[17]'::jsonb WHERE name = '大罗天魔';
UPDATE monster_template SET base_hp = 39000, base_attack = 770, skills = '[12]'::jsonb WHERE name = '天劫化身';
UPDATE monster_template SET base_hp = 45700, base_attack = 840, skills = '[15]'::jsonb WHERE name = '洪荒祖龙';
UPDATE monster_template SET base_hp = 57800, base_attack = 965, skills = '[17]'::jsonb WHERE name = '鸿蒙道尊';
