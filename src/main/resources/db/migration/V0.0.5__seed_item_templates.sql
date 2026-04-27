-- 装备 —— 武器
INSERT INTO xt_item_template (name, type, rarity, slot, equip_level, base_stat_bonus, base_attack, drop_weight, description, max_stack) VALUES
('木剑',         'EQUIPMENT', 'BROKEN',    'WEAPON',  1, '{"str":1,"con":0,"agi":0,"wis":0}'::jsonb,  2, '{"BROKEN": 60, "COMMON": 25, "RARE": 10, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '削木而成的简陋短剑', 1),
('青铜剑',      'EQUIPMENT', 'COMMON',    'WEAPON',  5, '{"str":3,"con":0,"agi":1,"wis":0}'::jsonb,  5, '{"BROKEN": 55, "COMMON": 30, "RARE": 12, "EPIC": 3, "LEGENDARY": 0}'::jsonb, '普通青铜铸剑', 1),
('铁剑',        'EQUIPMENT', 'BROKEN',    'WEAPON', 10, '{"str":3,"con":0,"agi":1,"wis":0}'::jsonb,  5, '{"BROKEN": 50, "COMMON": 30, "RARE": 15, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '一把普通的铁剑', 1),
('灵剑',        'EQUIPMENT', 'RARE',      'WEAPON', 20, '{"str":8,"con":0,"agi":3,"wis":2}'::jsonb, 15, '{"BROKEN": 40, "COMMON": 30, "RARE": 20, "EPIC": 8, "LEGENDARY": 2}'::jsonb, '蕴含灵气的宝剑', 1),
('玄铁剑',      'EQUIPMENT', 'RARE',      'WEAPON', 25, '{"str":10,"con":2,"agi":2,"wis":0}'::jsonb, 20, '{"BROKEN": 40, "COMMON": 30, "RARE": 20, "EPIC": 8, "LEGENDARY": 2}'::jsonb, '以千年玄铁铸成，挥动间寒光凛冽', 1),
('破军刀',      'EQUIPMENT', 'RARE',      'WEAPON', 30, '{"str":13,"con":5,"agi":-2,"wis":0}'::jsonb, 28, '{"BROKEN": 40, "COMMON": 30, "RARE": 20, "EPIC": 8, "LEGENDARY": 2}'::jsonb, '沉重霸道的战刀，一刀可破千军', 1),
('碧水剑',      'EQUIPMENT', 'EPIC',      'WEAPON', 40, '{"str":12,"con":0,"agi":10,"wis":6}'::jsonb, 38, '{"BROKEN": 30, "COMMON": 30, "RARE": 25, "EPIC": 12, "LEGENDARY": 3}'::jsonb, '剑身碧绿如水，轻灵异常', 1),
('青云剑',      'EQUIPMENT', 'EPIC',      'WEAPON', 35, '{"str":15,"con":0,"agi":8,"wis":5}'::jsonb, 35, '{"BROKEN": 30, "COMMON": 30, "RARE": 25, "EPIC": 12, "LEGENDARY": 3}'::jsonb, '青云峰镇山之宝，剑气如虹直冲云霄', 1),
('碎星刀',      'EQUIPMENT', 'EPIC',      'WEAPON', 38, '{"str":17,"con":4,"agi":4,"wis":0}'::jsonb, 40, '{"BROKEN": 30, "COMMON": 30, "RARE": 25, "EPIC": 12, "LEGENDARY": 3}'::jsonb, '刀芒可碎星辰', 1),
('仙剑',        'EQUIPMENT', 'LEGENDARY',  'WEAPON', 50, '{"str":20,"con":0,"agi":8,"wis":5}'::jsonb, 50, '{"BROKEN": 20, "COMMON": 30, "RARE": 25, "EPIC": 20, "LEGENDARY": 5}'::jsonb, '仙人遗留的神剑', 1);

-- 装备 —— 护甲
INSERT INTO xt_item_template (name, type, rarity, slot, equip_level, base_stat_bonus, base_defense, drop_weight, description, max_stack) VALUES
('竹甲',        'EQUIPMENT', 'BROKEN',    'ARMOR',  1, '{"str":0,"con":1,"agi":0,"wis":0}'::jsonb,  2, '{"BROKEN": 60, "COMMON": 25, "RARE": 10, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '竹片编成的简陋护甲', 1),
('皮甲',        'EQUIPMENT', 'BROKEN',    'ARMOR', 10, '{"str":0,"con":3,"agi":0,"wis":0}'::jsonb,  5, '{"BROKEN": 50, "COMMON": 30, "RARE": 15, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '简单的皮革护甲', 1),
('铜甲',        'EQUIPMENT', 'COMMON',    'ARMOR',  5, '{"str":0,"con":3,"agi":-1,"wis":0}'::jsonb,  7, '{"BROKEN": 55, "COMMON": 30, "RARE": 12, "EPIC": 3, "LEGENDARY": 0}'::jsonb, '铜制的轻甲', 1),
('铁甲',        'EQUIPMENT', 'COMMON',    'ARMOR', 15, '{"str":0,"con":6,"agi":-1,"wis":0}'::jsonb, 12, '{"BROKEN": 50, "COMMON": 30, "RARE": 15, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '沉重的铁制铠甲', 1),
('寒铁甲',      'EQUIPMENT', 'RARE',      'ARMOR', 25, '{"str":0,"con":10,"agi":0,"wis":0}'::jsonb, 20, '{"BROKEN": 40, "COMMON": 30, "RARE": 20, "EPIC": 8, "LEGENDARY": 2}'::jsonb, '寒铁铸就的铠甲，触之冰凉如雪', 1),
('灵甲',        'EQUIPMENT', 'EPIC',      'ARMOR', 30, '{"str":0,"con":12,"agi":2,"wis":3}'::jsonb, 30, '{"BROKEN": 30, "COMMON": 30, "RARE": 25, "EPIC": 12, "LEGENDARY": 3}'::jsonb, '蕴含灵力的护甲', 1),
('炎帝甲',      'EQUIPMENT', 'LEGENDARY',  'ARMOR', 50, '{"str":3,"con":20,"agi":2,"wis":5}'::jsonb, 55, '{"BROKEN": 20, "COMMON": 30, "RARE": 25, "EPIC": 20, "LEGENDARY": 5}'::jsonb, '炎帝遗留的神甲，烈火环绕', 1),
('龙鳞甲',      'EQUIPMENT', 'LEGENDARY',  'ARMOR', 45, '{"str":2,"con":18,"agi":3,"wis":5}'::jsonb, 50, '{"BROKEN": 20, "COMMON": 30, "RARE": 25, "EPIC": 20, "LEGENDARY": 5}'::jsonb, '以真龙之鳞缝制，寻常法宝难伤分毫', 1);

-- 装备 —— 饰品
INSERT INTO xt_item_template (name, type, rarity, slot, equip_level, base_stat_bonus, base_attack, base_defense, drop_weight, description, max_stack) VALUES
('铜环',        'EQUIPMENT', 'BROKEN',    'ACCESSORY',  5, '{"str":0,"con":0,"agi":1,"wis":1}'::jsonb, 0, 0, '{"BROKEN": 55, "COMMON": 30, "RARE": 12, "EPIC": 3, "LEGENDARY": 0}'::jsonb, '朴素的铜指环', 1),
('玉佩',        'EQUIPMENT', 'COMMON',    'ACCESSORY', 10, '{"str":0,"con":2,"agi":0,"wis":5}'::jsonb, 0, 0, '{"BROKEN": 40, "COMMON": 35, "RARE": 20, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '温润的玉佩，可凝神静气', 1),
('银坠',        'EQUIPMENT', 'COMMON',    'ACCESSORY', 15, '{"str":0,"con":1,"agi":2,"wis":3}'::jsonb, 0, 0, '{"BROKEN": 40, "COMMON": 35, "RARE": 20, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '银制吊坠', 1),
('定颜珠',      'EQUIPMENT', 'RARE',      'ACCESSORY', 25, '{"str":0,"con":5,"agi":2,"wis":5}'::jsonb, 0, 0, '{"BROKEN": 30, "COMMON": 30, "RARE": 25, "EPIC": 12, "LEGENDARY": 3}'::jsonb, '传说佩戴者容颜不老', 1),
('灵戒',        'EQUIPMENT', 'RARE',      'ACCESSORY', 20, '{"str":2,"con":2,"agi":2,"wis":5}'::jsonb, 0, 0, '{"BROKEN": 30, "COMMON": 30, "RARE": 25, "EPIC": 12, "LEGENDARY": 3}'::jsonb, '蕴含灵力的戒指', 1),
('乾坤戒',      'EQUIPMENT', 'LEGENDARY',  'ACCESSORY', 40, '{"str":5,"con":5,"agi":5,"wis":8}'::jsonb, 0, 0, '{"BROKEN": 20, "COMMON": 30, "RARE": 25, "EPIC": 20, "LEGENDARY": 5}'::jsonb, '内含一方小世界，可纳百川', 1);

-- 装备 —— 头盔
INSERT INTO xt_item_template (name, type, rarity, slot, equip_level, base_stat_bonus, base_defense, drop_weight, description, max_stack) VALUES
('布帽',        'EQUIPMENT', 'BROKEN',    'HELMET',  1, '{"str":0,"con":0,"agi":0,"wis":0}'::jsonb,  1, '{"BROKEN": 60, "COMMON": 25, "RARE": 10, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '普通的布帽', 1),
('铁盔',        'EQUIPMENT', 'BROKEN',    'HELMET', 10, '{"str":0,"con":2,"agi":0,"wis":0}'::jsonb,  3, '{"BROKEN": 50, "COMMON": 30, "RARE": 15, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '铁制的头盔', 1),
('银冠',        'EQUIPMENT', 'COMMON',    'HELMET', 15, '{"str":0,"con":2,"agi":1,"wis":2}'::jsonb,  5, '{"BROKEN": 40, "COMMON": 35, "RARE": 20, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '银质的修士冠', 1),
('灵冠',        'EQUIPMENT', 'RARE',      'HELMET', 20, '{"str":0,"con":3,"agi":2,"wis":5}'::jsonb,  8, '{"BROKEN": 40, "COMMON": 30, "RARE": 20, "EPIC": 8, "LEGENDARY": 2}'::jsonb, '修仙者佩戴的灵冠', 1),
('星辰冠',      'EQUIPMENT', 'EPIC',      'HELMET', 30, '{"str":2,"con":3,"agi":3,"wis":8}'::jsonb, 12, '{"BROKEN": 30, "COMMON": 30, "RARE": 25, "EPIC": 12, "LEGENDARY": 3}'::jsonb, '冠上镶嵌星辰碎片，可借周天星斗之力', 1);

-- 装备 —— 鞋子
INSERT INTO xt_item_template (name, type, rarity, slot, equip_level, base_stat_bonus, base_defense, drop_weight, description, max_stack) VALUES
('草鞋',        'EQUIPMENT', 'BROKEN',    'BOOTS',  1, '{"str":0,"con":0,"agi":1,"wis":0}'::jsonb,  0, '{"BROKEN": 60, "COMMON": 25, "RARE": 10, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '草编的简陋鞋子', 1),
('皮靴',        'EQUIPMENT', 'BROKEN',    'BOOTS', 10, '{"str":0,"con":1,"agi":3,"wis":0}'::jsonb,  1, '{"BROKEN": 50, "COMMON": 30, "RARE": 15, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '轻便的皮靴', 1),
('铁履',        'EQUIPMENT', 'COMMON',    'BOOTS', 15, '{"str":0,"con":2,"agi":2,"wis":0}'::jsonb,  2, '{"BROKEN": 40, "COMMON": 35, "RARE": 20, "EPIC": 4, "LEGENDARY": 1}'::jsonb, '铁底布履', 1),
('踏云靴',      'EQUIPMENT', 'RARE',      'BOOTS', 28, '{"str":0,"con":3,"agi":10,"wis":0}'::jsonb,  3, '{"BROKEN": 30, "COMMON": 30, "RARE": 25, "EPIC": 12, "LEGENDARY": 3}'::jsonb, '可踏空而行', 1),
('风行靴',      'EQUIPMENT', 'RARE',      'BOOTS', 20, '{"str":0,"con":2,"agi":8,"wis":0}'::jsonb,  3, '{"BROKEN": 40, "COMMON": 30, "RARE": 20, "EPIC": 8, "LEGENDARY": 2}'::jsonb, '穿上如踏风而行', 1),
('云履',        'EQUIPMENT', 'EPIC',      'BOOTS', 30, '{"str":0,"con":2,"agi":12,"wis":0}'::jsonb,  5, '{"BROKEN": 30, "COMMON": 30, "RARE": 25, "EPIC": 12, "LEGENDARY": 3}'::jsonb, '踏云而行的仙履，穿戴者身轻如燕', 1);

-- 丹药 / 消耗品
INSERT INTO xt_item_template (name, type, tags, description, max_stack) VALUES
-- 恢复类
('气血散',      'POTION', '["healing", "potion", "common"]'::jsonb,                                    '服用后恢复25点生命值', 30),
('疗伤丹',      'POTION', '["healing", "potion", "combat"]'::jsonb,                                     '服用后恢复50点生命值', 20),
('聚气丹',      'POTION', '["healing", "potion", "combat", "rare"]'::jsonb,                              '服用后恢复100点生命值', 20),
('回春丹',      'POTION', '["healing", "potion", "regen", "rare"]'::jsonb,                               '服用后每秒恢复5%生命值，持续10秒', 20),
('大还丹',      'POTION', '["healing", "potion", "rare", "combat"]'::jsonb,                              '恢复100% HP', 20),
('续命丹',      'POTION', '["revive", "potion", "rare"]'::jsonb,                                        '历练战败后自动使用，避免死亡惩罚，限一次', 3),
-- 属性增益类
('清心丹',      'POTION', '["curative", "potion", "common"]'::jsonb,                                    '服用后清除一个负面状态', 20),
('辟谷丹',      'POTION', '["stamina", "potion", "common"]'::jsonb,                                     '服用后立即恢复50点体力', 20),
('敏捷药剂',    'POTION', '["buff", "potion", "agi", "rare"]'::jsonb,                                   '历练增益，持续2小时内，敏捷+50', 10),
('智慧药剂',    'POTION', '["buff", "potion", "wis", "rare"]'::jsonb,                                   '历练增益，持续2小时内，智慧+50', 10),
('蛮力药剂',    'POTION', '["buff", "potion", "str", "rare"]'::jsonb,                                   '历练增益，持续2小时内，力量+50', 10),
('疾风丹',      'POTION', '["buff", "potion", "agi", "rare"]'::jsonb,                                   '历练增益，持续2小时内，敏捷+80', 10),
('铁骨丹',      'POTION', '["buff", "potion", "con", "rare"]'::jsonb,                                   '历练增益，持续2小时内，体质+80', 10),
('凝神丹',      'POTION', '["buff", "potion", "wis", "rare"]'::jsonb,                                   '历练增益，持续2小时内，智慧+80', 10),
('金刚丹',      'POTION', '["buff", "potion", "str", "con", "epic"]'::jsonb,                             '历练增益，持续2小时内，力量+40 体质+40', 10),
-- 特殊类
('龟息丹',      'POTION', '["stamina", "potion", "rare"]'::jsonb,                                       '服用后体力恢复速度翻倍，持续4小时', 10),
('聚灵丹',      'POTION', '["spirit", "potion", "epic"]'::jsonb,                                        '历练中获得灵石概率翻倍，持续1小时', 5),
('筑基丹',      'POTION', '["level_up", "potion", "rare"]'::jsonb,                                      '服用后直升一级，每次突破后仅可使用一次', 5),
('破境丹',      'POTION', '["breakthrough", "potion", "epic"]'::jsonb,                                  '破而后立之丹，突破时使用可提升20%成功率', 3),
('培元丹',      'POTION', '["permanent", "potion", "rare"]'::jsonb,                                     '服用后永久增加全属性+1，每人最多服用5次', 5),
('铸魂丹',      'POTION', '["breakthrough", "potion", "epic"]'::jsonb,                                  '突破时使用，失败后保留50%经验值', 3),
('九转金丹',    'POTION', '["full_restore", "potion", "legendary"]'::jsonb,                              '瞬间恢复全部HP与体力，并清除所有负面状态', 1);

-- 草药
INSERT INTO xt_item_template (name, type, base_stat_bonus, tags, description, max_stack) VALUES
-- 普通
('灵草',        'HERB', '{}'::jsonb,                                             '["herb", "spirit", "common"]'::jsonb,               '蕴含灵气的草药，炼药常用材料', 99),
('铜骨草',      'HERB', '{}'::jsonb,                                             '["herb", "common"]'::jsonb,                          '最常见的低级灵草', 99),
('清心草',      'HERB', '{"str":0,"con":0,"agi":0,"wis":1}'::jsonb,              '["herb", "common", "curative"]'::jsonb,               '可清心安神的草药', 99),
('银叶草',      'HERB', '{}'::jsonb,                                             '["herb", "common"]'::jsonb,                          '叶片呈银白色的低级灵草', 99),
-- 稀有
('毒龙草',      'HERB', '{}'::jsonb,                                             '["herb", "poison", "rare"]'::jsonb,                  '剧毒草药，用于炼制毒药或高级金创药', 99),
('灵芝',        'HERB', '{"str":0,"con":3,"agi":0,"wis":2}'::jsonb,              '["herb", "spirit", "rare"]'::jsonb,                  '灵气浓郁的仙草，炼制高级丹药必备', 99),
('冰魄花',      'HERB', '{"str":0,"con":3,"agi":0,"wis":2}'::jsonb,              '["herb", "ice", "rare", "alchemy"]'::jsonb,           '只生长在极寒之地的奇花，花瓣如冰晶般剔透', 99),
('噬魂菇',      'HERB', '{"str":0,"con":0,"agi":0,"wis":0}'::jsonb,              '["herb", "poison", "rare", "dark"]'::jsonb,           '生长于阴暗洞窟的诡异蘑菇', 50),
('七叶莲',      'HERB', '{"str":0,"con":2,"agi":0,"wis":2}'::jsonb,              '["herb", "rare", "curative"]'::jsonb,                 '七片叶子各呈一色的珍奇莲花', 50),
('九幽兰',      'HERB', '{"str":0,"con":0,"agi":0,"wis":0}'::jsonb,              '["herb", "dark", "rare", "poison"]'::jsonb,           '只生长于九幽之地的暗色兰花', 30),
('紫金藤',      'HERB', '{"str":2,"con":2,"agi":0,"wis":0}'::jsonb,              '["herb", "rare"]'::jsonb,                            '紫金色的藤蔓，坚韧异常', 50),
('月光菇',      'HERB', '{"str":0,"con":0,"agi":0,"wis":3}'::jsonb,              '["herb", "spirit", "rare"]'::jsonb,                   '只在月光下生长的灵菇', 50),
-- 史诗
('朱雀血藤',    'HERB', '{"str":4,"con":0,"agi":0,"wis":0}'::jsonb,              '["herb", "fire", "epic"]'::jsonb,                    '据说是朱雀血洒落凡间所生', 30),
('千年人参',    'HERB', '{"str":2,"con":2,"agi":0,"wis":2}'::jsonb,              '["herb", "rare", "prestige"]'::jsonb,                 '极为珍贵的炼药材料', 50),
('龙血草',      'HERB', '{"str":5,"con":3,"agi":0,"wis":0}'::jsonb,              '["herb", "dragon", "epic", "forge_potion"]'::jsonb,   '传说以真龙之血浇灌而生', 50),
('天心花',      'HERB', '{"str":0,"con":2,"agi":0,"wis":6}'::jsonb,              '["herb", "spirit", "epic", "wisdom"]'::jsonb,         '只在月圆之夜绽放', 30),
('天山雪莲',    'HERB', '{"str":0,"con":4,"agi":0,"wis":3}'::jsonb,              '["herb", "ice", "epic", "curative"]'::jsonb,          '百年一绽的天山雪莲', 20),
('长生果',      'HERB', '{"str":3,"con":5,"agi":0,"wis":3}'::jsonb,              '["herb", "epic", "longevity"]'::jsonb,                '传说中的长生果，极为稀有', 10),
('万年玄参',    'HERB', '{"str":3,"con":5,"agi":0,"wis":3}'::jsonb,              '["herb", "rare", "prestige", "longevity"]'::jsonb,    '在雪山深处生长万年的玄参', 20),
-- 传说
('赤火莲',      'HERB', '{"str":3,"con":0,"agi":0,"wis":2}'::jsonb,              '["herb", "fire", "rare", "harvest"]'::jsonb,          '火属性仙草，炼制火系丹药的主材', 50),
('玄冰莲',      'HERB', '{"str":0,"con":3,"agi":0,"wis":2}'::jsonb,              '["herb", "ice", "rare", "harvest"]'::jsonb,           '冰属性仙草，炼制冰系丹药的主材', 50),
('幽冥花',      'HERB', '{"str":0,"con":0,"agi":0,"wis":0}'::jsonb,              '["herb", "dark", "rare", "harvest", "poison"]'::jsonb,'暗属性奇花，深黑花瓣如夜', 20),
('金芝',        'HERB', '{"str":5,"con":5,"agi":0,"wis":5}'::jsonb,              '["herb", "gold", "legendary", "harvest"]'::jsonb,      '传说中的金芝，极为珍贵的仙草', 5),
('菩提叶',      'HERB', '{"str":0,"con":0,"agi":0,"wis":8}'::jsonb,              '["herb", "wisdom", "legendary"]'::jsonb,               '菩提树之叶，蕴含开悟之力', 1);

-- 材料
INSERT INTO xt_item_template (name, type, tags, description, max_stack) VALUES
-- 普通
('铜矿石',      'MATERIAL', '["ore", "metal", "common"]'::jsonb,                                         '常见铜矿', 99),
('银矿石',      'MATERIAL', '["ore", "metal", "common"]'::jsonb,                                         '普通银矿', 99),
('骨片',        'MATERIAL', '["bone", "common"]'::jsonb,                                                 '兽骨碎片', 99),
('史莱姆粘液',  'MATERIAL', '["slime", "common"]'::jsonb,                                                 '低级魔物留下的粘液', 99),
('灵石',        'MATERIAL', '["gem", "spirit", "currency", "common"]'::jsonb,                             '蕴含灵气的宝石，可作为高级货币', 9999),
-- 稀有
('精铁',        'MATERIAL', '["ore", "metal", "forge_base"]'::jsonb,                                     '锻造材料', 99),
('灵玉',        'MATERIAL', '["gem", "magic", "forge_enchant"]'::jsonb,                                  '炼器材料', 99),
('黑铁矿石',    'MATERIAL', '["ore", "metal", "forge_base", "rare"]'::jsonb,                               '稀有锻造材料，用于强化装备', 99),
('玄铁',        'MATERIAL', '["ore", "metal", "rare", "cold"]'::jsonb,                                   '寒气森森的玄铁', 50),
('铁矿石',      'MATERIAL', '["ore", "metal", "common", "forge_base"]'::jsonb,                             '可冶炼为精铁的铁矿', 99),
('寒玉',        'MATERIAL', '["gem", "ice", "rare", "magic"]'::jsonb,                                    '寒冷刺骨的玉石', 50),
('炎晶',        'MATERIAL', '["gem", "fire", "rare", "magic"]'::jsonb,                                   '蕴含火属灵气的晶石', 50),
('雷击木',      'MATERIAL', '["wood", "thunder", "rare"]'::jsonb,                                        '被天雷劈中而未燃的灵木', 30),
('玄武岩',      'MATERIAL', '["ore", "earth", "rare"]'::jsonb,                                           '大地深处最坚硬的岩石', 50),
('星辰铁',      'MATERIAL', '["ore", "metal", "star", "rare"]'::jsonb,                                   '天外陨铁，蕴含星辰之力', 50),
('琉璃土',      'MATERIAL', '["clay", "rare", "ceramic"]'::jsonb,                                        '可烧制琉璃的稀有土壤', 50),
-- 史诗
('秘银',        'MATERIAL', '["ore", "metal", "forge_enchant", "epic"]'::jsonb,                           '魔法金属，用于制作附魔装备', 50),
('万年玄冰',    'MATERIAL', '["ice", "gem", "magic", "epic"]'::jsonb,                                    '极北冰原深处万年不化的玄冰', 30),
('天外陨铁',    'MATERIAL', '["ore", "metal", "epic", "star"]'::jsonb,                                   '来自天外的神秘陨铁', 20),
('五行石',      'MATERIAL', '["gem", "epic", "five_elements"]'::jsonb,                                   '蕴含五行之力的奇石', 15),
('太阳真金',    'MATERIAL', '["ore", "metal", "sun", "epic"]'::jsonb,                                    '生于火山深处的太阳之金', 20),
('云中丝',      'MATERIAL', '["cloth", "epic", "wind"]'::jsonb,                                          '九天之上的云丝，轻薄如无物', 20),
-- 传说
('龙骨',        'MATERIAL', '["bone", "dragon", "prestige", "legendary"]'::jsonb,                         '龙的遗骨，极为珍贵的炼器材料', 10),
('凤羽',        'MATERIAL', '["feather", "phoenix", "prestige", "legendary"]'::jsonb,                     '神凤涅槃时脱落的尾羽，蕴含火之本源', 10),
('混沌石',      'MATERIAL', '["gem", "chaos", "prestige", "legendary"]'::jsonb,                           '混沌初开时形成的原初之石', 5),
('天道石',      'MATERIAL', '["gem", "legendary", "dao"]'::jsonb,                                        '蕴含天道碎片的奇石', 3),
('古代遗物',    'MATERIAL', '["ancient", "relic", "rare"]'::jsonb,                                        '不知年代的远古遗物', 10);

-- 种子 (SEED，福地专供)
INSERT INTO xt_item_template (name, type, tags, grow_time, yield_id, survive_rate, description, max_stack) VALUES
-- 普通
('灵草种子',    'SEED', '["seed", "spirit", "common"]'::jsonb,        24,  'herb_spirit_yield',       90, '灵草种子，成熟需24小时，产出灵草', 99),
('铜骨草种子',  'SEED', '["seed", "common"]'::jsonb,                 18,  'herb_copper_bone',        90, '铜骨草种子，成熟需18小时', 99),
('清心草种子',  'SEED', '["seed", "common"]'::jsonb,                 24,  'herb_purifying',          85, '清心草种子，成熟需24小时', 99),
('银叶草种子',  'SEED', '["seed", "common"]'::jsonb,                 18,  'herb_silver_leaf',        90, '银叶草种子，成熟需18小时', 99),
-- 稀有
('赤火莲子',    'SEED', '["seed", "fire", "rare"]'::jsonb,           48,  'herb_fire_lotus',         80, '火属性种子，成熟需48小时，产出赤火莲', 50),
('玄冰莲子',    'SEED', '["seed", "ice", "rare"]'::jsonb,            48,  'herb_ice_lotus',          80, '冰属性种子，成熟需48小时，产出玄冰莲', 50),
('冰魄花种子',  'SEED', '["seed", "ice", "rare"]'::jsonb,            36,  'herb_ice_bloom',          80, '冰魄花种子，成熟需36小时，产出冰魄花', 50),
('幽冥花种子',  'SEED', '["seed", "dark", "rare"]'::jsonb,           60,  'herb_nether_bloom',       65, '幽冥花种子，成熟需60小时', 30),
('紫金藤种子',  'SEED', '["seed", "rare"]'::jsonb,                   36,  'herb_purple_gold_vine',   75, '紫金藤种子，成熟需36小时', 30),
('九幽兰种子',  'SEED', '["seed", "dark", "rare"]'::jsonb,           72,  'herb_nine_abyss_orchid',  55, '九幽兰种子，成熟需72小时', 20),
('万年玄参种子','SEED', '["seed", "prestige", "rare"]'::jsonb,       96,  'herb_longevity_ginseng',  50, '万年玄参种子，成熟需96小时', 10),
-- 史诗
('朱雀血藤种子','SEED', '["seed", "fire", "epic"]'::jsonb,           72,  'herb_vermilion_vine',     55, '朱雀血藤种子，成熟需72小时', 20),
('天山雪莲种子','SEED', '["seed", "ice", "epic"]'::jsonb,            96,  'herb_snow_lotus',         45, '天山雪莲种子，成熟需96小时', 10),
('龙血草种子',  'SEED', '["seed", "dragon", "epic"]'::jsonb,         48,  'herb_dragon_blood',       70, '龙血草种子，成熟需48小时', 30),
('天心花种子',  'SEED', '["seed", "spirit", "epic"]'::jsonb,         60,  'herb_heaven_heart',       60, '天心花种子，成熟需60小时，仅在月圆之夜可播种', 20),
('长生果种子',  'SEED', '["seed", "epic", "longevity"]'::jsonb,      120, 'herb_longevity_fruit',    30, '长生果种子，成熟需120小时', 5),
-- 传说
('金芝种子',    'SEED', '["seed", "gold", "legendary"]'::jsonb,       72,  'herb_golden_lotus',       50, '稀有金芝种子，成熟需72小时，产出金芝', 10),
('菩提叶种子',  'SEED', '["seed", "wisdom", "legendary"]'::jsonb,    120, 'herb_bodhi_leaf',         25, '菩提叶种子，成熟需120小时', 1);

-- 灵蛋 (SPIRIT_EGG，福地专供)
INSERT INTO xt_item_template (name, type, tags, grow_time, yield_id, survive_rate, description, max_stack) VALUES
('火灵蛋',      'SPIRIT_EGG', '["egg", "fire", "rare"]'::jsonb,           72,  'pet_fire_turtle',      70, '孵化后可获得火属性灵宠，需72小时', 10),
('玄冰龟蛋',    'SPIRIT_EGG', '["egg", "water", "rare"]'::jsonb,          72,  'pet_water_turtle',     70, '孵化后可获得水属性灵宠，需72小时', 10),
('土灵蛋',      'SPIRIT_EGG', '["egg", "earth", "rare"]'::jsonb,          72,  'pet_earth_bear',       70, '孵化后可获得土属性灵宠，需72小时', 10),
('玉兔蛋',      'SPIRIT_EGG', '["egg", "moon", "rare"]'::jsonb,           96,  'pet_jade_rabbit',      60, '孵化后可获得玉兔灵宠，需96小时', 5),
('风灵蛋',      'SPIRIT_EGG', '["egg", "wind", "epic"]'::jsonb,           96,  'pet_wind_hawk',        60, '孵化后可获得风属性灵宠，需96小时', 5),
('雷鹰蛋',      'SPIRIT_EGG', '["egg", "thunder", "epic"]'::jsonb,       120,  'pet_thunder_eagle',    50, '孵化后可获得雷属性灵宠雷鹰，需120小时', 3),
('暗影蛋',      'SPIRIT_EGG', '["egg", "dark", "epic"]'::jsonb,          120,  'pet_shadow_serpent',   45, '孵化后可获得暗属性灵宠，需120小时', 3),
('光灵蛋',      'SPIRIT_EGG', '["egg", "light", "epic"]'::jsonb,         120,  'pet_light_deer',       45, '孵化后可获得光属性灵宠，需120小时', 3),
('炎狮蛋',      'SPIRIT_EGG', '["egg", "fire", "epic"]'::jsonb,          144,  'pet_fire_lion',        40, '孵化后可获得炎狮灵宠，需144小时', 3),
('金龙蛋',      'SPIRIT_EGG', '["egg", "gold", "legendary"]'::jsonb,     168,  'pet_gold_dragon',      30, '孵化后可获得金龙灵宠，需168小时', 1),
('冰凤蛋',      'SPIRIT_EGG', '["egg", "ice", "legendary"]'::jsonb,      192,  'pet_ice_phoenix',      25, '孵化后可获得冰凤灵宠，需192小时', 1),
('毒龙蛋',      'SPIRIT_EGG', '["egg", "poison", "legendary"]'::jsonb,   192,  'pet_poison_dragon',    20, '孵化后可获得毒龙灵宠，需192小时', 1);

-- 珍礼
INSERT INTO xt_item_template (name, type, tags, description, max_stack) VALUES
-- 普通
('月光石',      'GIFT', '["gem", "moon", "common"]'::jsonb,                                                '在月光下散发幽光的石头', 10),
('灵酒',        'GIFT', '["wine", "spirit", "consumable", "common"]'::jsonb,                                '灵气浓郁的酒水，适合送给喜欢饮酒的NPC', 20),
('长寿面',      'GIFT', '["food", "common", "warm"]'::jsonb,                                               '一碗简单的长寿面', 20),
-- 稀有
('星尘水晶',    'GIFT', '["gem", "shiny", "magic", "rare"]'::jsonb,                                         '璀璨的星尘水晶', 5),
('暖玉',        'GIFT', '["gem", "rare", "warm"]'::jsonb,                                                  '握在手中温暖如春的宝玉', 5),
('镇魂铃',      'GIFT', '["bell", "spirit", "rare"]'::jsonb,                                               '轻轻摇动可安魂定魄', 3),
('定风珠',      'GIFT', '["gem", "wind", "rare"]'::jsonb,                                                  '佩戴者不为狂风所动', 3),
('琉璃心',      'GIFT', '["gem", "rare", "art"]'::jsonb,                                                   '琉璃制成的精致心形', 5),
('玲珑棋局',    'GIFT', '["game", "wisdom", "rare"]'::jsonb,                                               '上古大能留下的残局', 3),
-- 史诗
('鲛人泪',      'GIFT', '["gem", "sea", "epic"]'::jsonb,                                                   '深海鲛人的眼泪化作的珍珠', 3),
('金乌羽',      'GIFT', '["feather", "sun", "epic"]'::jsonb,                                               '金乌的羽毛，触之温热', 3),
('玄黄尺',      'GIFT', '["ruler", "ancient", "epic"]'::jsonb,                                             '玄黄二气铸造的古老法器', 1),
('古玉',        'GIFT', '["gem", "ancient", "prestige", "epic"]'::jsonb,                                    '传承千年的古玉', 3),
('紫金葫芦',    'GIFT', '["vessel", "magic", "prestige", "epic"]'::jsonb,                                  '紫金炼制的葫芦，可收万物', 1),
('万兽丹',      'GIFT', '["beast", "epic", "utility"]'::jsonb,                                             '可吸引万兽的奇丹', 5),
-- 传说
('龙鳞',        'GIFT', '["scale", "dragon", "prestige", "legendary"]'::jsonb,                               '传说中的龙鳞，极为珍贵的礼物', 1),
('河图洛书',    'GIFT', '["book", "ancient", "prestige", "legendary", "unique"]'::jsonb,                     '河出图洛出书，记载天地至理的太古奇书', 1),
('先天八卦',    'GIFT', '["diagram", "ancient", "legendary"]'::jsonb,                                       '伏羲先天八卦图', 1);

-- 进化石 (EVOLUTION_STONE)
INSERT INTO xt_item_template (name, type, tags, description, max_stack) VALUES
('进化石',      'EVOLUTION_STONE', '["evolution", "rare"]'::jsonb,                                         '蕴含天地精华的灵石，可用于灵兽进化与品质突破', 50);

-- 灵兽卵
INSERT INTO xt_item_template (name, type, tags, grow_time, yield_id, survive_rate, description, max_stack) VALUES
-- T1 普通
('灵狐卵',      'BEAST_EGG', '["beast_egg", "common"]'::jsonb,             72,  'beast_fox',             80, '孵化后可获得灵狐，等阶T1，需72小时', 10),
('灵鹤卵',      'BEAST_EGG', '["beast_egg", "common"]'::jsonb,             72,  'beast_crane',           80, '孵化后可获得灵鹤，等阶T1，需72小时', 10),
('苍狼卵',      'BEAST_EGG', '["beast_egg", "common"]'::jsonb,             72,  'beast_wolf',            80, '孵化后可获得苍狼，等阶T1，需72小时', 10),
('蜂鸟卵',      'BEAST_EGG', '["beast_egg", "wind", "common"]'::jsonb,     72,  'beast_hummingbird',     85, '孵化后可获得蜂鸟，等阶T1，需72小时', 10),
-- T2 稀有
('火麟卵',      'BEAST_EGG', '["beast_egg", "fire", "rare"]'::jsonb,       96,  'beast_kirin',           65, '孵化后可获得火麒麟，等阶T2，需96小时', 5),
('玄龟卵',      'BEAST_EGG', '["beast_egg", "water", "rare"]'::jsonb,      96,  'beast_turtle',          65, '孵化后可获得玄龟，等阶T2，需96小时', 5),
('雷鹰卵',      'BEAST_EGG', '["beast_egg", "thunder", "rare"]'::jsonb,   120,  'beast_thunder_eagle',   60, '孵化后可获得雷鹰，等阶T2，需120小时', 5),
('金鹏卵',      'BEAST_EGG', '["beast_egg", "wind", "rare"]'::jsonb,       96,  'beast_golden_roc',      60, '孵化后可获得金鹏，等阶T2，需96小时', 5),
('银狐卵',      'BEAST_EGG', '["beast_egg", "moon", "rare"]'::jsonb,      120,  'beast_silver_fox',      55, '孵化后可获得银狐，等阶T2，需120小时', 5),
-- T3 史诗
('冰凤卵',      'BEAST_EGG', '["beast_egg", "ice", "epic"]'::jsonb,       192,  'beast_ice_phoenix',     35, '孵化后可获得冰凤，等阶T3，需192小时', 3),
('白虎卵',      'BEAST_EGG', '["beast_egg", "metal", "epic"]'::jsonb,     168,  'beast_white_tiger',     35, '孵化后可获得白虎，等阶T3，需168小时', 3),
('玄武卵',      'BEAST_EGG', '["beast_egg", "water", "epic"]'::jsonb,     168,  'beast_black_tortoise',  35, '孵化后可获得玄武，等阶T3，需168小时', 3),
-- T4 传说
('金龙卵',      'BEAST_EGG', '["beast_egg", "gold", "legendary"]'::jsonb, 168,  'beast_gold_dragon',     30, '孵化后可获得金龙，等阶T4，需168小时', 1),
('朱雀卵',      'BEAST_EGG', '["beast_egg", "fire", "legendary"]'::jsonb, 240,  'beast_vermilion_bird',  20, '孵化后可获得朱雀，等阶T4，需240小时', 1),
('青龙卵',      'BEAST_EGG', '["beast_egg", "wood", "legendary"]'::jsonb, 240,  'beast_azure_dragon',    20, '孵化后可获得青龙，等阶T4，需240小时', 1);

-- 灵兽材料 (BEAST_MATERIAL，兽栏产出物)
INSERT INTO xt_item_template (name, type, tags, description, max_stack) VALUES
-- 普通
('灵狐毛皮',    'BEAST_MATERIAL', '["beast_material", "common"]'::jsonb,                                  '灵狐自然脱落的毛皮，蕴含微弱灵气', 99),
('灵鹤翎羽',    'BEAST_MATERIAL', '["beast_material", "common"]'::jsonb,                                  '灵鹤脱落的翎羽，轻若无物', 99),
('苍狼牙',      'BEAST_MATERIAL', '["beast_material", "common"]'::jsonb,                                  '苍狼脱落的利牙', 99),
('蜂鸟晶羽',    'BEAST_MATERIAL', '["beast_material", "wind", "common"]'::jsonb,                           '蜂鸟的晶羽，色彩斑斓', 99),
-- 稀有
('火麟鳞片',    'BEAST_MATERIAL', '["beast_material", "fire", "rare"]'::jsonb,                             '火麒麟脱落的神鳞，蕴含火属灵气', 50),
('玄龟甲片',    'BEAST_MATERIAL', '["beast_material", "water", "rare"]'::jsonb,                             '玄龟自然脱落的甲壳碎片，极为坚韧', 50),
('雷鹰翎羽',    'BEAST_MATERIAL', '["beast_material", "thunder", "rare"]'::jsonb,                           '雷鹰脱落的翎羽，触及之时有酥麻之感', 50),
('金鹏翎羽',    'BEAST_MATERIAL', '["beast_material", "wind", "rare"]'::jsonb,                             '金鹏脱落的翎羽，金光闪烁', 50),
('银狐尾',      'BEAST_MATERIAL', '["beast_material", "moon", "rare"]'::jsonb,                             '银狐脱落的尾巴，月光之下熠熠生辉', 30),
-- 史诗
('冰凤羽毛',    'BEAST_MATERIAL', '["beast_material", "ice", "epic"]'::jsonb,                              '冰凤自然脱落的羽毛，寒气逼人', 30),
('龙血草精华',  'BEAST_MATERIAL', '["beast_material", "dragon", "epic"]'::jsonb,                            '从龙血草中提炼的精华，可用于灵兽进阶', 20),
('白虎皮毛',    'BEAST_MATERIAL', '["beast_material", "metal", "epic"]'::jsonb,                             '白虎自然脱落的皮毛，坚不可摧', 20),
('玄武甲壳',    'BEAST_MATERIAL', '["beast_material", "water", "epic"]'::jsonb,                             '玄武的甲壳碎片，能抵御水火', 20),
-- 传说
('金龙鳞',      'BEAST_MATERIAL', '["beast_material", "gold", "legendary"]'::jsonb,                         '金龙脱落的鳞片，蕴含龙脉之气', 20),
('朱雀翎羽',    'BEAST_MATERIAL', '["beast_material", "fire", "legendary"]'::jsonb,                         '朱雀的翎羽，永不熄灭', 10),
('青龙逆鳞',    'BEAST_MATERIAL', '["beast_material", "wood", "legendary"]'::jsonb,                         '青龙的逆鳞，蕴含生生不息之力', 10);
