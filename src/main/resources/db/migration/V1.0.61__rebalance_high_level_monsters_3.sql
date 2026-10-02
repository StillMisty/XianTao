-- 100+ 级怪物平衡三次调优：攻击再降 20%（削峰，降低偶发长局的暴毙概率）。
-- 目标：满配队伍单场净战损 ≤3%，90 分钟挂机（约 10~20 场）可持续。
UPDATE monster_template SET base_attack = 280 WHERE name = '太虚烛龙';
UPDATE monster_template SET base_attack = 304 WHERE name = '元凤';
UPDATE monster_template SET base_attack = 332 WHERE name = '混元兽';
UPDATE monster_template SET base_attack = 364 WHERE name = '大罗金仙';
UPDATE monster_template SET base_attack = 396 WHERE name = '无极魔尊';
UPDATE monster_template SET base_attack = 432 WHERE name = '混世魔猿';
UPDATE monster_template SET base_attack = 472 WHERE name = '太上道君';
UPDATE monster_template SET base_attack = 516 WHERE name = '无量天尊';
UPDATE monster_template SET base_attack = 564 WHERE name = '大罗天魔';
UPDATE monster_template SET base_attack = 616 WHERE name = '天劫化身';
UPDATE monster_template SET base_attack = 672 WHERE name = '洪荒祖龙';
UPDATE monster_template SET base_attack = 772 WHERE name = '鸿蒙道尊';
