-- 装备模板种子数据

-- ============ BLADE (刀) ============
INSERT INTO xt_equipment_template (name, description, tags, slot, weapon_type, category, equip_level, base_attack, base_defense, base_str, base_con, base_agi, base_wis, attack_speed, attack_range, drop_weight) VALUES
('砍柴刀',   '村里铁匠打的普通砍柴刀，说是法器有点勉强，但总比空手强。',       '["blade","common","starter"]',       'WEAPON', 'BLADE', 'MELEE', 1,  18, 0, 3, 0, 0, 0, 0.9, '近战', '{"BROKEN":60,"COMMON":30,"RARE":10}'),
('青锋刀',   '以玄铁淬火锻成的利刀，刀锋泛青，初具灵器之相。',              '["blade","uncommon","basic"]',       'WEAPON', 'BLADE', 'MELEE', 10, 50, 0, 8, 0, 2, 0, 1.0, '近战', '{"BROKEN":40,"COMMON":40,"RARE":15,"EPIC":5}'),
('断岳刀',   '传闻一刀可断山岳的重刀，刀身厚重但挥动时风雷隐隐。',          '["blade","rare","intermediate"]',    'WEAPON', 'BLADE', 'MELEE', 25, 110, 0, 18, 5, 0, 0, 0.8, '近战', '{"COMMON":40,"RARE":40,"EPIC":15,"LEGENDARY":5}'),
('龙雀刀',   '刀身刻有龙雀纹的宝刀，出刀时有雀鸣之声悦耳却致命。',          '["blade","epic","advanced"]',        'WEAPON', 'BLADE', 'MELEE', 50, 240, 0, 35, 10, 5, 0, 1.0, '近战', '{"RARE":40,"EPIC":40,"LEGENDARY":20}'),
('天刑刀',   '传说天庭刑天所用之刀，一刀可斩神魔，刀气冲天三千里。',         '["blade","legendary","grand"]',      'WEAPON', 'BLADE', 'MELEE', 75, 420, 0, 60, 15, 10, 5, 0.9, '近战', '{"EPIC":50,"LEGENDARY":50}');

-- ============ SWORD (剑) ============
INSERT INTO xt_equipment_template (name, description, tags, slot, weapon_type, category, equip_level, base_attack, base_defense, base_str, base_con, base_agi, base_wis, attack_speed, attack_range, drop_weight) VALUES
('桃木剑',   '桃木削成的剑，剑身贴有黄符，降妖驱鬼入门法器。',              '["sword","common","starter"]',       'WEAPON', 'SWORD', 'MELEE', 1,  15, 0, 0, 0, 3, 3, 1.2, '近战', '{"BROKEN":60,"COMMON":30,"RARE":10}'),
('青冥剑',   '剑身青黑如冥渊，轻灵而锋锐，入门剑修的首选法器。',           '["sword","uncommon","basic"]',       'WEAPON', 'SWORD', 'MELEE', 10, 45, 0, 5, 0, 5, 5, 1.2, '近战', '{"BROKEN":40,"COMMON":40,"RARE":15,"EPIC":5}'),
('纯钧剑',   '上古名剑纯钧仿品，剑身如秋水，挥动时剑鸣悠长。',             '["sword","rare","intermediate"]',    'WEAPON', 'SWORD', 'MELEE', 25, 105, 0, 12, 0, 12, 10, 1.1, '近战', '{"COMMON":40,"RARE":40,"EPIC":15,"LEGENDARY":5}'),
('七星剑',   '剑身镶嵌七颗星石的灵剑，暗合北斗七星之力，夜间威力大增。',     '["sword","epic","advanced"]',        'WEAPON', 'SWORD', 'MELEE', 45, 210, 0, 20, 5, 18, 18, 1.1, '近战', '{"RARE":30,"EPIC":50,"LEGENDARY":20}'),
('诛仙剑',   '剑身晶莹如玉，传为诛仙四剑之一的真品，剑出则天地变色。',       '["sword","legendary","grand"]',      'WEAPON', 'SWORD', 'MELEE', 65, 360, 0, 35, 10, 30, 30, 1.0, '近战', '{"EPIC":40,"LEGENDARY":60}'),
('轩辕剑',   '黄帝所铸圣道之剑，一面刻日月星辰一面刻山川草木，万剑之祖。',   '["sword","mythic","capstone"]',      'WEAPON', 'SWORD', 'MELEE', 90, 580, 0, 60, 25, 50, 50, 1.2, '近战', '{"LEGENDARY":100}');

-- ============ AXE (斧) ============
INSERT INTO xt_equipment_template (name, description, tags, slot, weapon_type, category, equip_level, base_attack, base_defense, base_str, base_con, base_agi, base_wis, attack_speed, attack_range, drop_weight) VALUES
('劈柴斧',   '本是厨房劈柴用的斧头，灌注灵气后勉强算是法器了。',             '["axe","common","starter"]',         'WEAPON', 'AXE', 'MELEE', 1,  22, 0, 5, 0, 0, 0, 0.7, '近战', '{"BROKEN":60,"COMMON":30,"RARE":10}'),
('开山斧',   '斧刃厚重如门板，一斧下去开碑裂石不在话下。',                 '["axe","uncommon","basic"]',         'WEAPON', 'AXE', 'MELEE', 10, 60, 0, 12, 3, 0, 0, 0.7, '近战', '{"BROKEN":40,"COMMON":40,"RARE":15,"EPIC":5}'),
('旋风斧',   '斧柄较长可旋转挥舞，攻防一体的战斧。',                        '["axe","rare","intermediate"]',      'WEAPON', 'AXE', 'MELEE', 25, 130, 5, 25, 8, 0, 0, 0.8, '近战', '{"COMMON":40,"RARE":40,"EPIC":15,"LEGENDARY":5}'),
('刑天斧',   '传说刑天所持神斧，斧身血迹千年不干，煞气冲天。',              '["axe","epic","advanced"]',          'WEAPON', 'AXE', 'MELEE', 50, 280, 10, 50, 15, 0, 0, 0.7, '近战', '{"RARE":40,"EPIC":40,"LEGENDARY":20}'),
('盘古斧',   '创世之初盘古劈开混沌的神斧，威力大到没人能完全驾驭。',          '["axe","legendary","mythic"]',       'WEAPON', 'AXE', 'MELEE', 80, 500, 0, 80, 30, 0, 0, 0.6, '近战', '{"EPIC":50,"LEGENDARY":50}');

-- ============ SPEAR (枪) ============
INSERT INTO xt_equipment_template (name, description, tags, slot, weapon_type, category, equip_level, base_attack, base_defense, base_str, base_con, base_agi, base_wis, attack_speed, attack_range, drop_weight) VALUES
('竹枪',     '青竹削尖后经灵气加持的长枪，轻便有韧劲。',                    '["spear","common","starter"]',       'WEAPON', 'SPEAR', 'POLEARM', 1,  20, 0, 3, 0, 2, 0, 1.0, '近战', '{"BROKEN":60,"COMMON":30,"RARE":10}'),
('亮银枪',   '枪身银白锃亮，枪尖寒芒闪烁，军中猛将最爱。',                  '["spear","uncommon","basic"]',       'WEAPON', 'SPEAR', 'POLEARM', 10, 52, 0, 8, 3, 4, 0, 1.0, '近战', '{"BROKEN":40,"COMMON":40,"RARE":15,"EPIC":5}'),
('游龙枪',   '枪身盘龙浮雕，刺出时宛如游龙出水气势惊人。',                  '["spear","rare","intermediate"]',    'WEAPON', 'SPEAR', 'POLEARM', 25, 115, 5, 18, 5, 8, 0, 1.1, '近战', '{"COMMON":40,"RARE":40,"EPIC":15,"LEGENDARY":5}'),
('霸王枪',   '传说西楚霸王所用战枪，枪重三百六十斤，非天生神力不可持。',     '["spear","epic","advanced"]',        'WEAPON', 'SPEAR', 'POLEARM', 50, 250, 10, 45, 10, 5, 0, 0.8, '近战', '{"RARE":40,"EPIC":40,"LEGENDARY":20}'),
('方天追魂枪','融合方天画戟和追魂索命的特性，可刺可劈，变幻莫测。',           '["spear","legendary","grand"]',      'WEAPON', 'SPEAR', 'POLEARM', 75, 430, 0, 65, 20, 15, 0, 1.0, '近战', '{"EPIC":50,"LEGENDARY":50}');

-- ============ STAFF (棍) ============
INSERT INTO xt_equipment_template (name, description, tags, slot, weapon_type, category, equip_level, base_attack, base_defense, base_str, base_con, base_agi, base_wis, attack_speed, attack_range, drop_weight) VALUES
('木棍',     '一根普普通通的灵木棍子，但也比树枝好用。',                     '["staff","common","starter"]',       'WEAPON', 'STAFF', 'POLEARM', 1,  16, 3, 2, 0, 2, 0, 1.1, '近战', '{"BROKEN":60,"COMMON":30,"RARE":10}'),
('镇魔棍',   '经过法阵加持的伏魔之棍，对邪祟有额外镇压之力。',               '["staff","uncommon","basic"]',       'WEAPON', 'STAFF', 'POLEARM', 10, 42, 8, 5, 5, 3, 0, 1.0, '近战', '{"BROKEN":40,"COMMON":40,"RARE":15,"EPIC":5}'),
('金刚杵',   '佛门金刚手之兵，看似棍实则杵，蕴含降魔佛力。',                '["staff","rare","intermediate"]',    'WEAPON', 'STAFF', 'POLEARM', 25, 100, 15, 10, 12, 5, 5, 1.0, '近战', '{"COMMON":40,"RARE":40,"EPIC":15,"LEGENDARY":5}'),
('如意金箍棒','传说中大禹治水所用的定海神针，上有金箍两道，可随心意变化。',    '["staff","legendary","sunwukong"]',  'WEAPON', 'STAFF', 'POLEARM', 60, 330, 20, 55, 20, 20, 0, 1.2, '近战', '{"EPIC":30,"LEGENDARY":70}');

-- ============ BOW (弓) ============
INSERT INTO xt_equipment_template (name, description, tags, slot, weapon_type, category, equip_level, base_attack, base_defense, base_str, base_con, base_agi, base_wis, attack_speed, attack_range, drop_weight) VALUES
('猎弓',     '山中猎户用的硬木弓，拉开需要一番力气。',                       '["bow","common","starter"]',         'WEAPON', 'BOW', 'RANGED', 1,  17, 0, 0, 0, 5, 0, 1.0, '远程', '{"BROKEN":60,"COMMON":30,"RARE":10}'),
('穿云弓',   '以灵兽筋为弦的长弓，射程远超普通弓弩。',                      '["bow","uncommon","basic"]',         'WEAPON', 'BOW', 'RANGED', 10, 48, 0, 3, 0, 10, 0, 0.9, '远程', '{"BROKEN":40,"COMMON":40,"RARE":15,"EPIC":5}'),
('流星弓',   '弓身镶嵌星陨石的宝弓，射出之箭快如流星。',                     '["bow","rare","intermediate"]',      'WEAPON', 'BOW', 'RANGED', 25, 108, 0, 8, 0, 22, 3, 0.9, '远程', '{"COMMON":40,"RARE":40,"EPIC":15,"LEGENDARY":5}'),
('落日弓',   '后羿射日所用神弓的仿品，一箭射出如金乌坠落。',                 '["bow","epic","advanced"]',          'WEAPON', 'BOW', 'RANGED', 50, 230, 0, 15, 5, 40, 5, 0.8, '远程', '{"RARE":40,"EPIC":40,"LEGENDARY":20}'),
('射日神弓', '后羿真传之弓，弓弦为龙筋所制，传说拉开此弓需有逐日之志。',     '["bow","legendary","grand"]',        'WEAPON', 'BOW', 'RANGED', 75, 400, 0, 30, 10, 70, 10, 0.8, '远程', '{"EPIC":50,"LEGENDARY":50}');

-- ============ WHIP (鞭) ============
INSERT INTO xt_equipment_template (name, description, tags, slot, weapon_type, category, equip_level, base_attack, base_defense, base_str, base_con, base_agi, base_wis, attack_speed, attack_range, drop_weight) VALUES
('麻绳鞭',   '麻绳编成的软鞭，抽在身上倒也挺疼。',                           '["whip","common","starter"]',        'WEAPON', 'WHIP', 'EXOTIC', 5,  20, 0, 2, 0, 5, 0, 1.3, '近战', '{"BROKEN":60,"COMMON":30,"RARE":10}'),
('银丝软鞭', '以银丝织就的软鞭，鞭梢带倒钩，一鞭下去皮开肉绽。',              '["whip","uncommon","basic"]',        'WEAPON', 'WHIP', 'EXOTIC', 15, 55, 0, 5, 0, 12, 0, 1.3, '近战', '{"BROKEN":40,"COMMON":40,"RARE":15,"EPIC":5}'),
('九节鞭',   '九节连环的钢鞭，刚柔并济，可攻可守。',                         '["whip","rare","intermediate"]',     'WEAPON', 'WHIP', 'EXOTIC', 30, 125, 5, 15, 5, 20, 0, 1.1, '近战', '{"COMMON":40,"RARE":40,"EPIC":15,"LEGENDARY":5}'),
('缚龙索',   '传说中可缚真龙的长鞭，鞭身刻满上古禁制符文。',                 '["whip","epic","advanced"]',         'WEAPON', 'WHIP', 'EXOTIC', 50, 220, 10, 25, 10, 40, 5, 1.2, '近战', '{"RARE":40,"EPIC":40,"LEGENDARY":20}');

-- ============ HALBERD (戟) ============
INSERT INTO xt_equipment_template (name, description, tags, slot, weapon_type, category, equip_level, base_attack, base_defense, base_str, base_con, base_agi, base_wis, attack_speed, attack_range, drop_weight) VALUES
('长戟',     '军中制式长戟，可刺可劈，朴实好用。',                           '["halberd","common","starter"]',      'WEAPON', 'HALBERD', 'EXOTIC', 8,  35, 2, 8, 3, 0, 0, 0.8, '近战', '{"BROKEN":60,"COMMON":30,"RARE":10}'),
('方天画戟', '戟身刻有日月星纹，戟刃开双锋，威猛无匹的名戟。',               '["halberd","rare","intermediate"]',   'WEAPON', 'HALBERD', 'EXOTIC', 30, 140, 10, 30, 10, 0, 0, 0.8, '近战', '{"COMMON":30,"RARE":50,"EPIC":15,"LEGENDARY":5}'),
('破天戟',  '一戟破天的盖世神兵，戟尖可撕裂空间裂缝。',                       '["halberd","epic","advanced"]',       'WEAPON', 'HALBERD', 'EXOTIC', 55, 300, 10, 55, 20, 5, 5, 0.9, '近战', '{"RARE":30,"EPIC":50,"LEGENDARY":20}'),
('帝江戟',  '上古天帝所遗神戟，挥舞时如有混沌气流转环绕。',                    '["halberd","legendary","mythic"]',    'WEAPON', 'HALBERD', 'EXOTIC', 80, 480, 0, 80, 30, 10, 10, 0.9, '近战', '{"EPIC":50,"LEGENDARY":50}');

-- ============ HAMMER (锤) ============
INSERT INTO xt_equipment_template (name, description, tags, slot, weapon_type, category, equip_level, base_attack, base_defense, base_str, base_con, base_agi, base_wis, attack_speed, attack_range, drop_weight) VALUES
('石锤',     '一块大石头绑在木柄上，举起需要不小的力气。',                    '["hammer","common","starter"]',       'WEAPON', 'HAMMER', 'EXOTIC', 5,  30, 0, 8, 2, 0, 0, 0.5, '近战', '{"BROKEN":60,"COMMON":30,"RARE":10}'),
('震地锤',  '锤头镌刻有震荡符文，一锤下去连地都抖三抖。',                      '["hammer","uncommon","basic"]',       'WEAPON', 'HAMMER', 'EXOTIC', 15, 70, 5, 15, 8, 0, 0, 0.5, '近战', '{"BROKEN":40,"COMMON":40,"RARE":15,"EPIC":5}'),
('碎山锤',  '锤大如斗，纯以重量伤人，一锤碎山岳不是夸张。',                    '["hammer","rare","intermediate"]',    'WEAPON', 'HAMMER', 'EXOTIC', 35, 160, 10, 35, 15, 0, 0, 0.6, '近战', '{"COMMON":40,"RARE":40,"EPIC":15,"LEGENDARY":5}'),
('昊天锤',  '九天飞来神锤，锤身萦绕雷霆，每击必带雷威。',                       '["hammer","legendary","thunder"]',    'WEAPON', 'HAMMER', 'EXOTIC', 65, 370, 15, 60, 30, 0, 0, 0.7, '近战', '{"EPIC":40,"LEGENDARY":60}');

-- ============ DAGGER (匕首) ============
INSERT INTO xt_equipment_template (name, description, tags, slot, weapon_type, category, equip_level, base_attack, base_defense, base_str, base_con, base_agi, base_wis, attack_speed, attack_range, drop_weight) VALUES
('骨匕',     '妖兽骨打磨成的匕首，轻便顺手。',                               '["dagger","common","starter"]',       'WEAPON', 'DAGGER', 'EXOTIC', 1,  14, 0, 0, 0, 5, 0, 1.5, '近战', '{"BROKEN":60,"COMMON":30,"RARE":10}'),
('暗影匕',  '淬毒的短匕，刃口发黑，见血封喉不是开玩笑的。',                   '["dagger","uncommon","basic"]',       'WEAPON', 'DAGGER', 'EXOTIC', 12, 42, 0, 2, 0, 10, 0, 1.5, '近战', '{"BROKEN":40,"COMMON":40,"RARE":15,"EPIC":5}'),
('鱼肠剑',  '上古名剑之一，据传可藏在鱼腹之中行刺，实为绝品短剑。',            '["dagger","rare","intermediate"]',    'WEAPON', 'DAGGER', 'EXOTIC', 28, 110, 0, 8, 0, 25, 3, 1.6, '近战', '{"COMMON":30,"RARE":50,"EPIC":15,"LEGENDARY":5}'),
('千幻匕',  '持之可化千百幻影，一瞬之间敌人身上多了十个窟窿。',                '["dagger","epic","advanced"]',        'WEAPON', 'DAGGER', 'EXOTIC', 50, 220, 0, 15, 0, 50, 10, 1.7, '近战', '{"RARE":30,"EPIC":50,"LEGENDARY":20}'),
('勾魂刃',  '阎罗殿流出的勾魂之刃，刃出则魂飞魄散。',                          '["dagger","legendary","hell"]',       'WEAPON', 'DAGGER', 'EXOTIC', 70, 350, 0, 20, 0, 80, 15, 1.8, '近战', '{"EPIC":40,"LEGENDARY":60}');

-- ============ FAN (扇) ============
INSERT INTO xt_equipment_template (name, description, tags, slot, weapon_type, category, equip_level, base_attack, base_defense, base_str, base_con, base_agi, base_wis, attack_speed, attack_range, drop_weight) VALUES
('竹扇',     '普通竹骨折扇，正面上书一个"雅"字，反面是山水画。',              '["fan","common","starter"]',          'WEAPON', 'FAN', 'EXOTIC', 8,  18, 0, 0, 0, 3, 5, 1.3, '近战', '{"BROKEN":60,"COMMON":30,"RARE":10}'),
('云锦扇',  '以云锦为面的灵扇，轻摇则有云气缭绕，修仙雅士首选。',              '["fan","uncommon","basic"]',          'WEAPON', 'FAN', 'EXOTIC', 18, 45, 3, 0, 0, 5, 12, 1.3, '近战', '{"BROKEN":40,"COMMON":40,"RARE":15,"EPIC":5}'),
('霓裳扇',  '扇面如霓裳羽衣，七彩流转，挥动时妙音伴生。',                     '["fan","rare","intermediate"]',       'WEAPON', 'FAN', 'EXOTIC', 35, 110, 8, 0, 3, 12, 25, 1.2, '近战', '{"COMMON":40,"RARE":40,"EPIC":15,"LEGENDARY":5}'),
('天罡扇',  '天罡三十六星之力炼化的宝扇，扇出罡风如刀。',                      '["fan","epic","advanced"]',           'WEAPON', 'FAN', 'EXOTIC', 55, 240, 12, 5, 5, 20, 45, 1.2, '近战', '{"RARE":40,"EPIC":40,"LEGENDARY":20}');

-- ============ FLYWHISK (拂尘) ============
INSERT INTO xt_equipment_template (name, description, tags, slot, weapon_type, category, equip_level, base_attack, base_defense, base_str, base_con, base_agi, base_wis, attack_speed, attack_range, drop_weight) VALUES
('马尾拂尘','马尾巴毛扎成的拂尘，入门道士人手一把。',                          '["flywhisk","common","starter"]',      'WEAPON', 'FLYWHISK', 'EXOTIC', 8,  16, 0, 0, 3, 2, 5, 1.2, '近战', '{"BROKEN":60,"COMMON":30,"RARE":10}'),
('白鹤拂尘','白鹤翎羽制成的拂尘，轻扫时似有鹤鸣回荡。',                       '["flywhisk","uncommon","basic"]',      'WEAPON', 'FLYWHISK', 'EXOTIC', 18, 42, 5, 0, 5, 5, 10, 1.2, '近战', '{"BROKEN":40,"COMMON":40,"RARE":15,"EPIC":5}'),
('灵犀拂尘','以灵犀之尾作尘丝，拂之可消心魔、定心神。',                       '["flywhisk","rare","intermediate"]',   'WEAPON', 'FLYWHISK', 'EXOTIC', 35, 105, 10, 0, 10, 10, 22, 1.1, '近战', '{"COMMON":40,"RARE":40,"EPIC":15,"LEGENDARY":5}'),
('太虚拂尘','据说太上老君道场掉下的一根拂尘，蕴含太虚至理。',                  '["flywhisk","epic","dao"]',            'WEAPON', 'FLYWHISK', 'EXOTIC', 55, 230, 15, 0, 15, 18, 45, 1.0, '近战', '{"RARE":40,"EPIC":40,"LEGENDARY":20}');

-- ============ RING (圈) ============
INSERT INTO xt_equipment_template (name, description, tags, slot, weapon_type, category, equip_level, base_attack, base_defense, base_str, base_con, base_agi, base_wis, attack_speed, attack_range, drop_weight) VALUES
('铁环',     '普通铁环，丢出去可以砸人脑袋。',                                '["ring","common","starter"]',          'WEAPON', 'RING', 'EXOTIC', 10, 24, 0, 5, 0, 5, 0, 1.2, '近战', '{"BROKEN":60,"COMMON":30,"RARE":10}'),
('金刚圈',  '金刚琢的仿品，据说是太上老君防身之物——不过他老人家很少需要用。',   '["ring","uncommon","basic"]',          'WEAPON', 'RING', 'EXOTIC', 22, 60, 5, 12, 5, 10, 0, 1.1, '近战', '{"BROKEN":40,"COMMON":40,"RARE":15,"EPIC":5}'),
('乾坤圈',  '乾坤八卦之力凝于一环，圈内可困万物。',                            '["ring","rare","intermediate"]',       'WEAPON', 'RING', 'EXOTIC', 40, 140, 10, 25, 12, 18, 5, 1.1, '近战', '{"COMMON":40,"RARE":40,"EPIC":15,"LEGENDARY":5}'),
('日月神圈','日月交辉之圈，白日吸日精夜间纳月华，圈中有日月光影流转。',         '["ring","epic","suncircles"]',          'WEAPON', 'RING', 'EXOTIC', 60, 280, 15, 40, 20, 30, 10, 1.0, '近战', '{"RARE":40,"EPIC":40,"LEGENDARY":20}');

-- ============ BELL (钟) ============
INSERT INTO xt_equipment_template (name, description, tags, slot, weapon_type, category, equip_level, base_attack, base_defense, base_str, base_con, base_agi, base_wis, attack_speed, attack_range, drop_weight) VALUES
('铜铃',     '道观门口挂的小铜铃，摇响可驱散低级邪祟。',                       '["bell","common","starter"]',          'WEAPON', 'BELL', 'EXOTIC', 10, 22, 3, 0, 5, 0, 3, 1.1, '近战', '{"BROKEN":60,"COMMON":30,"RARE":10}'),
('镇魂铃',  '镇魂安魄之铃，铃声悠扬入耳，对灵体伤害加倍。',                    '["bell","uncommon","basic"]',          'WEAPON', 'BELL', 'EXOTIC', 22, 55, 8, 0, 10, 0, 8, 1.0, '近战', '{"BROKEN":40,"COMMON":40,"RARE":15,"EPIC":5}'),
('摄魂钟',  '钟声一响魂飞天外，对灵体有震慑奇效的法钟。',                      '["bell","rare","intermediate"]',       'WEAPON', 'BELL', 'EXOTIC', 40, 135, 12, 0, 20, 5, 18, 0.9, '近战', '{"COMMON":40,"RARE":40,"EPIC":15,"LEGENDARY":5}'),
('东皇钟',  '上古十大神器之一，据说一声钟响可让时光停滞片刻。',                '["bell","legendary","mythic"]',         'WEAPON', 'BELL', 'EXOTIC', 70, 340, 20, 10, 35, 10, 40, 0.8, '近战', '{"EPIC":40,"LEGENDARY":60}');

-- ============================================================
-- ARMOR (护甲)
-- ============================================================
INSERT INTO xt_equipment_template (name, description, tags, slot, equip_level, base_attack, base_defense, base_str, base_con, base_agi, base_wis, drop_weight) VALUES
-- 布甲/法袍系列
('麻布道袍',   '粗麻布缝制的道袍，虽然朴素但好歹有层布。',                  '["armor","cloth","common","starter"]',        'ARMOR', 1,   0, 8,   0, 3,  0, 0, '{"BROKEN":60,"COMMON":30,"RARE":10}'),
('青布道袍',   '染青的棉布道袍，袖口绣有微弱的防御符文。',                  '["armor","cloth","uncommon","basic"]',        'ARMOR', 8,   0, 18,  0, 5,  0, 2, '{"BROKEN":50,"COMMON":35,"RARE":12,"EPIC":3}'),
('灵蚕法袍',   '以灵蚕丝织成的法袍，轻盈透亮如水波流转。',                   '["armor","cloth","rare","intermediate"]',     'ARMOR', 20,  0, 40,  0, 10, 5,  8, '{"COMMON":30,"RARE":50,"EPIC":15,"LEGENDARY":5}'),
('云锦仙袍',   '云中织成的仙锦所制，穿着如披云霞，御风而行。',               '["armor","cloth","epic","advanced"]',          'ARMOR', 40,  0, 80,  0, 18, 12, 18, '{"RARE":40,"EPIC":40,"LEGENDARY":20}'),
('太极仙衣',   '太极八卦纹的仙衣，暗合天道，穿上后心魔不侵。',                '["armor","cloth","legendary","dao"]',          'ARMOR', 65,  0, 150, 0, 30, 20, 40, '{"EPIC":40,"LEGENDARY":60}'),
-- 皮甲系列
('兽皮甲',     '妖兽皮缝制的皮甲，普通刀剑砍不透。',                        '["armor","leather","common","starter"]',      'ARMOR', 5,   0, 14,  3, 5,  2,  0, '{"BROKEN":60,"COMMON":30,"RARE":10}'),
('犀皮铠',     '以犀妖兽皮鞣制的重皮甲，防御力不逊于铁甲而更轻。',           '["armor","leather","uncommon","basic"]',      'ARMOR', 15,  0, 30,  5, 12, 3,  0, '{"BROKEN":40,"COMMON":40,"RARE":15,"EPIC":5}'),
('龙鳞软甲',   '龙鳞嵌入的软甲，轻如无物却坚不可摧。',                       '["armor","leather","dragon","rare"]',          'ARMOR', 35,  0, 65,  10, 25, 8,  0, '{"COMMON":30,"RARE":50,"EPIC":15,"LEGENDARY":5}'),
('玄武甲',     '玄武神兽的甲壳所制，站立不动时防御翻倍。',                    '["armor","leather","defense","epic"]',         'ARMOR', 55,  0, 120, 15, 45, 5,  5, '{"RARE":40,"EPIC":40,"LEGENDARY":20}'),
-- 铁/重甲系列
('玄铁甲',     '以玄铁打制的硬甲，穿上去哐哐响，但安全。',                   '["armor","plate","common","starter"]',        'ARMOR', 8,   0, 25,  5, 8,  0,  0, '{"BROKEN":50,"COMMON":40,"RARE":10}'),
('寒铁重甲',   '寒铁铸造的重型战甲，表面凝霜，穿上走路都掉冰碴。',            '["armor","plate","uncommon","basic"]',         'ARMOR', 18,  0, 50,  10, 18, 0,  0, '{"BROKEN":30,"COMMON":45,"RARE":20,"EPIC":5}'),
('陨铁战甲',   '天外陨铁打造的稀世战甲，陨石中蕴含的星辰之力加持防御。',     '["armor","plate","rare","intermediate"]',     'ARMOR', 38,  0, 100, 20, 35, 0,  0, '{"COMMON":30,"RARE":50,"EPIC":15,"LEGENDARY":5}'),
('混元战甲',   '以五行之力淬炼的重甲，五行相生循环不断，生生不息。',          '["armor","plate","epic","advanced"]',          'ARMOR', 58,  0, 180, 35, 55, 0,  0, '{"RARE":30,"EPIC":50,"LEGENDARY":20}'),
-- 轻甲系列
('丝绢软甲',   '蚕丝与妖兽皮交织的软甲，穿在衣服里防身用。',                 '["armor","light","uncommon","basic"]',         'ARMOR', 10,  0, 22,  3, 7,  5,  2, '{"BROKEN":40,"COMMON":40,"RARE":15,"EPIC":5}'),
('天蚕宝甲',   '天山灵蚕丝编织的宝甲，轻若无物却能卸力八成。',               '["armor","light","rare","intermediate"]',     'ARMOR', 28,  0, 48,  5, 15, 12, 5,  '{"COMMON":30,"RARE":50,"EPIC":15,"LEGENDARY":5}'),
('青鸾羽衣',   '青鸾仙禽的绒羽织就，穿上如披鸾羽，轻灵到感觉自己会飞。',     '["armor","light","phoenix","epic"]',           'ARMOR', 48,  0, 95,  8, 22, 22, 12, '{"RARE":40,"EPIC":40,"LEGENDARY":20}'),
('九霄轻甲',   '九霄云外采来的清气所凝，穿上之后敌人经常打不中你。',           '["armor","light","legendary"]',                'ARMOR', 72,  0, 160, 12, 35, 40, 25, '{"EPIC":40,"LEGENDARY":60}'),
-- 灵甲
('五行灵甲',   '五色灵力交织而生的灵甲，无需穿戴自动护主。',                 '["armor","spirit","rare","intermediate"]',    'ARMOR', 30,  0, 55,  8, 18, 8,  8, '{"COMMON":30,"RARE":50,"EPIC":15,"LEGENDARY":5}'),
('元磁灵衣',   '地心元磁之力凝成的灵衣，对金行法器有特别的吸引力（防御）。',   '["armor","spirit","epic","advanced"]',         'ARMOR', 52,  0, 110, 18, 30, 12, 12, '{"RARE":40,"EPIC":40,"LEGENDARY":20}'),
('不灭金身',   '以不灭金身神通铸就的甲衣，穿上之后真的很难打死。',             '["armor","spirit","legendary","defense"]',    'ARMOR', 78,  0, 200, 30, 60, 10, 20, '{"EPIC":40,"LEGENDARY":60}'),
-- 新手回廊专属
('新手布衣',   '刚踏入仙途的新手标配，虽然弱但也别无他求。',                  '["armor","cloth","common","tutorial"]',        'ARMOR', 1,   0, 5,   1, 1,  1,  1, '{"BROKEN":100}'),
('见习道袍',   '宗门见习弟子配发的道袍，穿了就是自己人了。',                  '["armor","cloth","common","tutorial"]',        'ARMOR', 1,   0, 10,  2, 2,  2,  2, '{"BROKEN":80,"COMMON":20}');

-- ============================================================
-- ACCESSORY (饰品)
-- ============================================================
INSERT INTO xt_equipment_template (name, description, tags, slot, equip_level, base_attack, base_defense, base_str, base_con, base_agi, base_wis, drop_weight) VALUES
-- 戒指
('铁指环',     '铁打的指环，除了戴在手上没别的用——其实稍微加一点力道。',     '["accessory","ring","common","starter"]',       'ACCESSORY', 3,   5, 0, 3, 0, 0, 0, '{"BROKEN":70,"COMMON":25,"RARE":5}'),
('灵玉戒指',   '嵌入小块灵石的白玉戒指，能微量提升灵力流转速度。',            '["accessory","ring","uncommon","basic"]',       'ACCESSORY', 12,  15, 0, 6, 3, 3, 0, '{"BROKEN":50,"COMMON":35,"RARE":12,"EPIC":3}'),
('乾坤戒',     '戒中有芥子空间，同时增强空间感知，加力道和悟性。',            '["accessory","ring","rare","intermediate"]',    'ACCESSORY', 32,  35, 5, 15, 5, 5, 8, '{"COMMON":30,"RARE":50,"EPIC":15,"LEGENDARY":5}'),
('龙凤对戒',   '龙戒和凤戒本是一对，分开戴增加力道/悟性，同时戴效果翻倍。',   '["accessory","ring","dragon","phoenix","epic"]', 'ACCESSORY', 55,  60, 10, 25, 10, 10, 20, '{"RARE":30,"EPIC":50,"LEGENDARY":20}'),
-- 项链
('狼牙项链',   '妖兽狼王的犬齿穿成的项链，戴上后感觉自己更凶了。',            '["accessory","necklace","common","starter"]',   'ACCESSORY', 2,   8, 0, 5, 0, 0, 0, '{"BROKEN":70,"COMMON":25,"RARE":5}'),
('灵石吊坠',   '整块灵石打磨成的水滴吊坠，挂在胸前暖洋洋的。',                '["accessory","necklace","uncommon","basic"]',   'ACCESSORY', 10,  12, 3, 0, 5, 5, 3, '{"BROKEN":50,"COMMON":35,"RARE":12,"EPIC":3}'),
('舍利子',     '佛门高僧坐化后留下的舍利子，蕴含其毕生佛力。',                 '["accessory","necklace","rare","buddhist"]',    'ACCESSORY', 30,  30, 10, 5, 15, 0, 15, '{"COMMON":30,"RARE":50,"EPIC":15,"LEGENDARY":5}'),
('日月珠',     '似从日月精华中凝结的宝珠，日夜交替间转换阳/阴之力。',          '["accessory","necklace","epic","suncircles"]',  'ACCESSORY', 50,  50, 15, 10, 22, 10, 22, '{"RARE":30,"EPIC":50,"LEGENDARY":20}'),
-- 手镯
('铜手镯',     '普普通通的铜手镯，但注入灵气后能微幅提力。',                   '["accessory","bracelet","common","starter"]',   'ACCESSORY', 3,   3, 3, 0, 5, 0, 0, '{"BROKEN":70,"COMMON":25,"RARE":5}'),
('玉镯',       '翡翠手镯，温润典雅，养人养颜还增加一点体质。',                 '["accessory","bracelet","uncommon","basic"]',   'ACCESSORY', 12,  8, 8, 0, 10, 3, 3, '{"BROKEN":50,"COMMON":35,"RARE":12,"EPIC":3}'),
('玄铁手环',   '一对玄铁铸造的厚重手环，戴着像是手上绑了沙袋但效果出奇。',    '["accessory","bracelet","rare","intermediate"]', 'ACCESSORY', 33,  20, 15, 12, 18, 0, 0, '{"COMMON":30,"RARE":50,"EPIC":15,"LEGENDARY":5}'),
('金刚琢',     '太上老君的法宝（是的又是一个仿品），戴在手腕上犹如戴了防线。', '["accessory","bracelet","epic","dao"]',          'ACCESSORY', 58,  40, 25, 20, 35, 5, 10, '{"RARE":30,"EPIC":50,"LEGENDARY":20}'),
-- 腰带
('麻绳腰带',   '就一根麻绳而已，但修仙之人腰间还是得有东西。',                 '["accessory","belt","common","starter"]',        'ACCESSORY', 1,   0, 5, 0, 5, 0, 0, '{"BROKEN":80,"COMMON":20}'),
('灵蛇腰带',   '以灵蛇蜕皮编成的腰带，柔软有韧性。',                          '["accessory","belt","uncommon","basic"]',        'ACCESSORY', 10,  0, 12, 0, 10, 5, 0, '{"BROKEN":50,"COMMON":35,"RARE":12,"EPIC":3}'),
('盘龙金带',   '金线织就盘龙纹腰带，腰间一束威风凛凛。',                      '["accessory","belt","rare","intermediate"]',     'ACCESSORY', 30,  5, 25, 10, 20, 5, 5, '{"COMMON":30,"RARE":50,"EPIC":15,"LEGENDARY":5}'),
('混天绫',     '哪吒的混天绫碎片制成的腰带——别想了哪吒送的，其实是捡的。',   '["accessory","belt","myth","epic"]',              'ACCESSORY', 55,  15, 45, 15, 35, 15, 10, '{"RARE":30,"EPIC":50,"LEGENDARY":20}'),
-- 护符
('平安符',     '庙里求来的平安符，蕴含微弱灵力，聊胜于无。',                   '["accessory","charm","common","starter"]',       'ACCESSORY', 1,   0, 3, 0, 3, 0, 2, '{"BROKEN":80,"COMMON":20}'),
('镇妖符',     '朱砂黄纸画成的镇妖符，贴在身上可以震慑低级妖兽。',              '["accessory","charm","uncommon","basic"]',       'ACCESSORY', 8,   8, 5, 0, 5, 0, 8, '{"BROKEN":50,"COMMON":35,"RARE":12,"EPIC":3}'),
('五行符',     '五张分别代表金木水火土的灵符合一，均衡提升各项属性。',          '["accessory","charm","rare","intermediate"]',    'ACCESSORY', 28,  15, 10, 8, 8, 8, 8,  '{"COMMON":30,"RARE":50,"EPIC":15,"LEGENDARY":5}'),
('天命符',     '刻有"天"字的紫金符箓，传说得此符者天命所归。',                 '["accessory","charm","epic","destiny"]',         'ACCESSORY', 52,  25, 20, 15, 15, 15, 18, '{"RARE":30,"EPIC":50,"LEGENDARY":20}'),
-- 特殊饰品
('玉净瓶',     '观音菩萨的玉净瓶（当然是仿品），插柳枝甘露水可恢复生命力。',   '["accessory","special","rare","buddhist"]',      'ACCESSORY', 45,  20, 20, 5, 25, 5, 25, '{"COMMON":20,"RARE":50,"EPIC":25,"LEGENDARY":5}'),
('阴阳玉佩',   '一阴一阳两块玉佩，合二为一方显全效。',                          '["accessory","special","rare","intermediate"]',  'ACCESSORY', 35,  28, 15, 10, 10, 12, 12, '{"COMMON":30,"RARE":50,"EPIC":15,"LEGENDARY":5}'),
('万魂幡',     '万千魂灵汇聚的幡旗，阴气森森但增加道力惊人（慎用）。',          '["accessory","special","evil","legendary"]',     'ACCESSORY', 65,  45, 30, 30, 20, 5, 35, '{"EPIC":40,"LEGENDARY":60}'),
('山河社稷图','传说女娲的山河社稷图碎片，内含一方小世界。',                     '["accessory","special","mythic","legendary"]',   'ACCESSORY', 85,  60, 40, 35, 35, 20, 45, '{"LEGENDARY":100}');
