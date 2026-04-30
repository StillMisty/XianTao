-- 首批法决数据
INSERT INTO xt_skill (name, description, skill_type, effect_type, binding_type, binding_value, cooldown_seconds, damage_formula, power_multiplier, level_requirement)
VALUES
    ('御剑术', '以气御剑，灵动穿刺', 'ACTIVE', 'DAMAGE', 'WEAPON_TYPE', 'SWORD', 30, 'wis*3 + 20', 1.0, 1),
    ('破空斩', '刚猛斩击，破碎虚空', 'ACTIVE', 'DAMAGE', 'WEAPON_TYPE', 'BLADE', 40, 'wis*4 + 15', 1.0, 1),
    ('雷霆一击', '沉重破甲，雷击碎魂', 'ACTIVE', 'ARMOR_BREAK', 'WEAPON_TYPE', 'AXE', 60, null, 1.0, 1),
    ('穿云箭', '远程精准，一箭穿云', 'ACTIVE', 'DAMAGE', 'WEAPON_TYPE', 'BOW', 35, 'wis*3 + 25', 1.0, 1),
    ('伏魔棍法', '降妖伏魔，棍扫乾坤', 'ACTIVE', 'DAMAGE', 'WEAPON_TYPE', 'STAFF', 30, 'wis*3 + 15', 1.0, 1),
    ('烈焰诀', '烈焰焚天，灼烧万物', 'ACTIVE', 'DAMAGE', 'NONE', null, 45, 'wis*2 + 15', 1.0, 1),
    ('寒冰诀', '冰封千里，迟缓敌人', 'ACTIVE', 'SLOW', 'NONE', null, 60, null, 1.0, 1),
    ('万剑诀', '万剑归宗，连击四起', 'ACTIVE', 'MULTI_HIT', 'WEAPON_TYPE', 'SWORD', 120, 'wis*4 + 30', 1.0, 5);
