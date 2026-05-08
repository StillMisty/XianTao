-- =====================================================
-- 怪物模板种子数据 (xt_monster_template)
-- 60+ 怪物，覆盖 1-100 级
-- skills 留空 (怪物基础攻击)，drop_table 按名称子查询
-- =====================================================

-- ============ 1-10级 新手区 ============
INSERT INTO xt_monster_template (name, description, monster_type, base_level, base_hp, base_attack, base_defense, base_speed, exp_reward, skills, drop_table, tags) VALUES
('野狼',     '山林间常见的野狼，被灵气浸染后体型比凡狼大一圈。',           'WILD_BEAST', 2,  80,  12,  5,  14, 15,  '[]', '[]', '["beast","common","beginner"]'),
('毒蛇',     '草丛中潜伏的毒蛇，被咬一口虽不致命但疼得跳脚。',           'WILD_BEAST', 3,  60,  15,  3,  18, 18,  '[]', '[]', '["beast","poison","beginner"]'),
('山魈',     '山中精怪所化的独脚妖兽，身形矮小但力大无穷。',               'BEAST',    4,  100, 18,  8,  12, 22,  '[]', '[]', '["beast","spirit","beginner"]'),
('石灵',     '岩石吸收了天地灵气孕育出的石精，行动慢但皮厚。',             'ARMORED',  5,  150, 12,  15, 6,  28,  '[]', '[]', '["armored","earth","beginner"]'),
('妖鼠',     '被妖气感染的巨型老鼠，成群结队出现。',                      'BEAST',    5,  70,  16,  4,  16, 25,  '[]', '[]', '["beast","swarm","beginner"]'),
('野猪妖',   '野猪修炼成精，獠牙如同匕首，冲锋起来刹不住。',              'BEAST',    7,  180, 20,  10, 10, 35,  '[]', '[]', '["beast","charge","basic"]'),
('风狼',     '掌握了微弱风灵力的狼妖，奔跑时如一阵风。',                  'BEAST',    8,  130, 24,  8,  22, 40,  '[]', '[]', '["beast","wind","basic"]'),
('骷髅兵',   '不知哪个倒霉修士的遗骸被妖气复苏，挥舞着破刀。',            'EVIL',     8,  120, 22,  12, 10, 42,  '[]', '[]', '["evil","undead","basic"]'),
('食人花',   '看起来像是一朵美丽的花，但靠近它你就知道谁是肥料了。',        'BEAST',    9,  110, 26,  6,  8,  45,  '[]', '[]', '["beast","plant","basic"]'),
('树精',     '百年古树有了灵智，树根如触手般挥舞。',                      'SPIRIT',   10, 200, 18,  15, 5,  50,  '[]', '[]', '["spirit","plant","basic"]');

-- ============ 10-20级 初级历练区 ============
INSERT INTO xt_monster_template (name, description, monster_type, base_level, base_hp, base_attack, base_defense, base_speed, exp_reward, skills, drop_table, tags) VALUES
('冰狼',     '极寒之地出没的狼妖，吐息可冻结草木。',                      'BEAST',    12, 200, 30,  12, 18, 65,  '[]', '[]', '["beast","ice","basic"]'),
('火焰蜥',   '岩浆裂隙中生活的蜥蜴妖兽，喷吐的火舌可达一丈。',            'BEAST',    13, 180, 35,  10, 15, 70,  '[]', '[]', '["beast","fire","basic"]'),
('石甲龟',   '背上生有石头般坚硬龟甲的妖兽，缩进壳里几乎无敌。',          'ARMORED',  14, 300, 18,  25, 4,  60,  '[]', '[]', '["armored","defense","basic"]'),
('幽魂',     '不得超度的亡魂所化的灵体，物理攻击难以伤其毫分。',          'EVIL',     14, 140, 32,  5,  20, 75,  '[]', '[]', '["evil","ghost","basic"]'),
('螳螂妖',   '一人高的螳螂成妖，双臂如镰刀般锋利。',                      'BEAST',    15, 220, 38,  12, 22, 80,  '[]', '[]', '["beast","insect","basic"]'),
('山贼',     '落草为寇的修士，修炼不到家改行抢劫。揍他。',                'HUMAN',    15, 250, 30,  15, 12, 85,  '[]', '[]', '["human","bandit","basic"]'),
('蝙蝠妖',   '倒挂在洞穴顶部的吸血蝙蝠，晚上才出没。',                    'FLYING',   16, 170, 34,  8,  28, 80,  '[]', '[]', '["flying","vampire","basic"]'),
('铁甲虫',   '外壳如钢铁般坚硬的甲虫妖兽，正面强攻很难奏效。',            'ARMORED',  18, 350, 22,  30, 8,  90,  '[]', '[]', '["armored","insect","basic"]'),
('水鬼',     '淹死在河中的怨鬼，会把人拖下水。',                          'EVIL',     18, 160, 36,  6,  22, 95,  '[]', '[]', '["evil","water","ghost","intermediate"]'),
('妖狐',     '修炼了百年的狐狸精，会幻化人形迷惑路人。',                  'BEAST',    20, 240, 40,  15, 25, 110, '[]', '[]', '["beast","fox","illusion","intermediate"]');

-- ============ 20-35级 中级历练区 ============
INSERT INTO xt_monster_template (name, description, monster_type, base_level, base_hp, base_attack, base_defense, base_speed, exp_reward, skills, drop_table, tags) VALUES
('石魔',     '矿洞中矿石成精所化，挥舞石拳砸得地板发抖。',                'ARMORED',  22, 450, 32,  35, 6,  130, '[]', '[]', '["armored","earth","intermediate"]'),
('怨灵',     '含冤而死的修士魂魄，怨念深重化为厉鬼。',                    'EVIL',     24, 220, 48,  8,  30, 150, '[]', '[]', '["evil","ghost","intermediate"]'),
('血蝠',     '吸食了妖兽血液后变异的大蝙蝠，双目赤红如血。',              'FLYING',   25, 280, 44,  12, 32, 160, '[]', '[]', '["flying","vampire","intermediate"]'),
('冰蚕',     '万载玄冰中孕育的寒蚕，吐出的冰丝可瞬间冻结人。',            'BEAST',    26, 300, 36,  10, 16, 155, '[]', '[]', '["beast","ice","silk","intermediate"]'),
('妖道',     '走上邪路的修士，用活人祭炼法器，人人得而诛之。',            'HUMAN',    28, 400, 55,  20, 18, 200, '[]', '[]', '["human","evil","cultivator","intermediate"]'),
('毒蟾',     '浑身分泌剧毒黏液的大蟾蜍，近身的敌人都会中毒。',            'BEAST',    29, 380, 42,  15, 12, 190, '[]', '[]', '["beast","poison","intermediate"]'),
('飞头蛮',   '被诅咒的头颅脱离身体飞行，张口喷出恶臭妖风。',               'EVIL',     30, 260, 52,  8,  35, 210, '[]', '[]', '["evil","flying","horror","intermediate"]'),
('猿妖',     '修炼了三百年的大猿，力可搬山，脾气暴躁。',                   'BEAST',    32, 500, 60,  22, 20, 240, '[]', '[]', '["beast","ape","strength","intermediate"]'),
('蛇妖',     '一条大蛇将化蛟而未成，头生肉角行将化龙。',                  'BEAST',    34, 450, 65,  25, 24, 280, '[]', '[]', '["beast","snake","dragon_kin","intermediate"]');

-- ============ 35-50级 高级历练区 ============
INSERT INTO xt_monster_template (name, description, monster_type, base_level, base_hp, base_attack, base_defense, base_speed, exp_reward, skills, drop_table, tags) VALUES
('熔岩巨兽', '火山口中诞生的岩石巨兽，通体流淌岩浆。',                    'BEAST',    36, 600, 70,  35, 8,  320, '[]', '[]', '["beast","fire","giant","advanced"]'),
('摄魂妖',   '专门吸食修士魂魄的邪妖，形如黑雾变幻不定。',                'EVIL',     38, 350, 75,  12, 35, 350, '[]', '[]', '["evil","soul","dark","advanced"]'),
('夜叉',     '地狱跑出来的恶鬼夜叉，面目狰狞手持钢叉。',                  'EVIL',     40, 550, 80,  30, 28, 400, '[]', '[]', '["evil","demon","hell","advanced"]'),
('雪女',     '雪山深处出没的美艳女妖，其实冷血无情。',                    'EVIL',     42, 400, 72,  18, 40, 420, '[]', '[]', '["evil","ice","female","advanced"]'),
('金甲尸',   '前朝将军尸体在养尸地孕育千年而成的尸王，刀枪不入。',        'ARMORED',  44, 800, 65,  50, 10, 460, '[]', '[]', '["evil","zombie","armored","advanced"]'),
('狮鹫',     '狮身鹰首的飞行妖兽，爪牙锋利可撕裂钢铁。',                   'FLYING',   45, 550, 85,  25, 38, 480, '[]', '[]', '["flying","beast","predator","advanced"]'),
('修罗',     '阿修罗道下凡的战士，抱着战斗至死的信念。',                   'HUMAN',    46, 700, 95,  35, 30, 520, '[]', '[]', '["human","asura","warrior","advanced"]'),
('千年树妖', '千年古树成妖，树冠遮天蔽日，根系绵延数里。',                 'SPIRIT',   48, 900, 70,  45, 12, 550, '[]', '[]', '["spirit","plant","giant","advanced"]'),
('雷鹰',     '在九天雷云中翱翔的巨鹰，浑身萦绕奔雷。',                     'FLYING',   50, 500, 90,  20, 50, 600, '[]', '[]', '["flying","thunder","bird","advanced"]');

-- ============ 50-65级 大师区 ============
INSERT INTO xt_monster_template (name, description, monster_type, base_level, base_hp, base_attack, base_defense, base_speed, exp_reward, skills, drop_table, tags) VALUES
('黑风老妖', '黑风洞中修炼千年的熊妖，凶悍无比。',                        'BEAST',    52, 1000, 95,  45, 22, 700,   '[]', '[]', '["beast","boss","master"]'),
('火凤雏',   '火凤的幼雏，虽未成年但火翼一展可焚百里。',                  'FLYING',   53, 650, 110, 25, 55, 720,   '[]', '[]', '["flying","phoenix","fire","master"]'),
('九尾妖狐', '修炼到九尾境界的妖狐，一尾一神通，幻化无穷。',              'BEAST',    55, 800, 105, 30, 50, 800,   '[]', '[]', '["beast","fox","illusion","master"]'),
('山鬼',     '屈原笔下的山鬼，貌美而危险，山林之主。',                    'SPIRIT',   56, 750, 100, 35, 45, 780,   '[]', '[]', '["spirit","forest","master"]'),
('蜚廉',     '上古风伯之后裔，鹿身雀首，御风而行。',                       'BEAST',    58, 900, 115, 40, 60, 880,   '[]', '[]', '["beast","wind","myth","master"]'),
('幽冥骑士', '幽冥之地的亡魂骑士，骑着骷髅战马巡狩。',                     'EVIL',     60, 1200, 120, 50, 35, 1000,  '[]', '[]', '["evil","knight","death","master"]'),
('蛟龙',     '即将化龙的大蛟，翻江倒海易如反掌。',                         'BEAST',    62, 1500, 130, 55, 40, 1200,  '[]', '[]', '["beast","dragon_kin","water","master"]'),
('天罗蛛',   '编织天罗地网的巨型蛛妖，落入网中就别想跑了。',              'BEAST',    64, 1100, 125, 35, 48, 1100,  '[]', '[]', '["beast","spider","trap","master"]');

-- ============ 65-80级 宗师区 ============
INSERT INTO xt_monster_template (name, description, monster_type, base_level, base_hp, base_attack, base_defense, base_speed, exp_reward, skills, drop_table, tags) VALUES
('夔牛',     '上古异兽夔牛，状如牛、苍身无角、一足，其声如雷。',           'BEAST',    66, 1800, 140, 60, 30, 1500,  '[]', '[]', '["beast","thunder","myth","grandmaster"]'),
('梼杌',     '上古四凶之一，虎身人面，獠牙外露。',                          'BEAST',    68, 2000, 155, 65, 35, 1700,  '[]', '[]', '["beast","evil","myth","grandmaster"]'),
('金乌',     '太阳中的三足金乌，浑身金光灼目，凡人直视即瞎。',             'FLYING',   70, 1500, 170, 40, 65, 2000,  '[]', '[]', '["flying","sun","myth","grandmaster"]'),
('旱魃',     '僵尸之祖，所到之处赤地千里。',                                'EVIL',     72, 2500, 145, 70, 25, 1900,  '[]', '[]', '["evil","zombie","disaster","grandmaster"]'),
('白泽',     '知晓天下万物的神兽，虽然不主动攻击但逼急了也很凶。',         'BEAST',    74, 1800, 160, 50, 55, 2200,  '[]', '[]', '["beast","wisdom","myth","grandmaster"]'),
('烛龙',     '人面蛇身而赤，睁眼为昼闭眼为夜，烛九阴之龙。',               'BEAST',    76, 3000, 180, 75, 45, 2800,  '[]', '[]', '["beast","dragon","myth","grandmaster"]'),
('天魔王',   '域外天魔之王，降临此界只为收割修士元神。',                   'EVIL',     78, 2200, 200, 55, 60, 3200,  '[]', '[]', '["evil","demon_king","alien","grandmaster"]');

-- ============ 80-95级 传说区 ============
INSERT INTO xt_monster_template (name, description, monster_type, base_level, base_hp, base_attack, base_defense, base_speed, exp_reward, skills, drop_table, tags) VALUES
('麒麟',     '瑞兽麒麟，头生一角身披鳞甲，踏火不焚。',                     'BEAST',    82, 3500, 200, 80, 55, 3800,  '[]', '[]', '["beast","myth","auspicious","legendary"]'),
('相柳',     '九头蛇身的上古凶神，一次吃九座山。',                           'BEAST',    84, 4000, 220, 90, 45, 4200,  '[]', '[]', '["beast","snake","myth","legendary"]'),
('刑天',     '被砍了头仍以乳为目、以脐为口继续战斗的无头巨人。',           'HUMAN',    86, 5000, 250, 100, 30, 5000, '[]', '[]', '["human","myth","warrior","legendary"]'),
('毕方',     '白喙单足的神鸟，所到之处必有火灾。',                           'FLYING',   88, 3800, 240, 70, 70, 4800, '[]', '[]', '["flying","fire","myth","legendary"]'),
('应龙',     '上古大神应龙，生有双翼，曾助黄帝斩杀蚩尤。',                 'BEAST',    90, 6000, 280, 110, 60, 6500, '[]', '[]', '["beast","dragon","myth","legendary"]'),
('守鹤',     '尾兽守鹤（串场了？），形如貉而身披紫纹狸猫。',               'BEAST',    93, 5500, 260, 95, 50, 6000,  '[]', '[]', '["beast","sand","myth","legendary"]');

-- ============ 95-100级 巅峰区 ============
INSERT INTO xt_monster_template (name, description, monster_type, base_level, base_hp, base_attack, base_defense, base_speed, exp_reward, skills, drop_table, tags) VALUES
('鲲鹏',     '北冥有鱼其名为鲲，化而为鸟其名为鹏，展翅遮天。',              'FLYING',   95, 8000, 300, 120, 70, 8000,  '[]', '[]', '["flying","myth","ultimate"]'),
('混沌',     '上古四凶之混沌，形如犬而四翼，善恶不分。',                     'BEAST',    96, 7500, 320, 130, 60, 8500,  '[]', '[]', '["beast","chaos","myth","ultimate"]'),
('神龙',     '真正的五爪金龙，中华神兽之首，见之即是大机缘。',              'BEAST',    98, 10000,350, 150, 80, 12000, '[]', '[]', '["beast","dragon","divine","ultimate"]'),
('阎罗天子', '十殿阎罗之主，生死薄在手，判你阳寿几何。',                    'HUMAN',    99, 12000,380, 160, 75, 15000, '[]', '[]', '["human","death","divine","ultimate"]'),
('原始天魔', '混沌未开之时便存在的原始天魔，仙魔大战的元凶。',              'EVIL',     100,15000,450, 200, 90, 20000, '[]', '[]', '["evil","primordial","boss","ultimate"]');
