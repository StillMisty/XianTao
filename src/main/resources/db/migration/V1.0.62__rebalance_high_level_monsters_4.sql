-- 100+ 级怪物平衡四次调优：攻击再降 25%。
-- 原因：满配下战斗已全胜且 1~6 回合，但玩家自身气血在 15+ 场连续战斗中先耗尽；
-- 降低怪物攻击以缩小玩家单场掉血，使 90 分钟挂机可持续。
UPDATE monster_template SET base_attack = 210 WHERE name = '太虚烛龙';
UPDATE monster_template SET base_attack = 228 WHERE name = '元凤';
UPDATE monster_template SET base_attack = 249 WHERE name = '混元兽';
UPDATE monster_template SET base_attack = 273 WHERE name = '大罗金仙';
UPDATE monster_template SET base_attack = 297 WHERE name = '无极魔尊';
UPDATE monster_template SET base_attack = 324 WHERE name = '混世魔猿';
UPDATE monster_template SET base_attack = 354 WHERE name = '太上道君';
UPDATE monster_template SET base_attack = 387 WHERE name = '无量天尊';
UPDATE monster_template SET base_attack = 423 WHERE name = '大罗天魔';
UPDATE monster_template SET base_attack = 462 WHERE name = '天劫化身';
UPDATE monster_template SET base_attack = 504 WHERE name = '洪荒祖龙';
UPDATE monster_template SET base_attack = 579 WHERE name = '鸿蒙道尊';
