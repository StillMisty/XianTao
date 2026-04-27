INSERT INTO xt_map_node (name, description, map_type, level_requirement, travel_time_minutes, neighbors, specialties,
                         travel_events)
VALUES

-- ── 黑金主城（起始主城）──
('黑金主城',
 '仙道大陆最大的城市，繁华的商业中心，各路修仙者云集之地。',
 'safe_town',
 1,
 5,
 '{
   "幽暗沼泽": 5
 }'::jsonb,
 '[]'::jsonb,
 '[]'::jsonb),

-- ── 幽暗沼泽（5级历练区）──
('幽暗沼泽',
 '常年弥漫着毒雾的沼泽地，盛产珍稀毒草，但也潜伏着危险的沼泽生物。',
 'training_zone',
 5,
 5,
 '{
   "黑金主城": 5,
   "枯骨林": 10
 }'::jsonb,
 '[
   {
     "name": "毒龙草",
     "weight": 30,
     "templateId": 51
   },
   {
     "name": "史莱姆粘液",
     "weight": 50,
     "templateId": 74
   },
   {
     "name": "灵草",
     "weight": 20,
     "templateId": 47
   }
 ]'::jsonb,
 '[
   {
     "eventType": "ambush",
     "weight": 40
   },
   {
     "eventType": "find_treasure",
     "weight": 10
   },
   {
     "eventType": "weather",
     "weight": 50
   }
 ]'::jsonb),

-- ── 枯骨林（10级历练区）──
('枯骨林',
 '一片枯萎的森林，遍地白骨，传闻曾有强大的古兽陨落于此。',
 'training_zone',
 10,
 10,
 '{
   "幽暗沼泽": 10,
   "迷雾洞窟": 15,
   "黑风岭": 10
 }'::jsonb,
 '[
   {
     "name": "铁矿石",
     "weight": 45,
     "templateId": 80
   },
   {
     "name": "骨片",
     "weight": 30,
     "templateId": 73
   },
   {
     "name": "灵草",
     "weight": 25,
     "templateId": 47
   }
 ]'::jsonb,
 '[
   {
     "eventType": "ambush",
     "weight": 50
   },
   {
     "eventType": "find_treasure",
     "weight": 15
   },
   {
     "eventType": "weather",
     "weight": 35
   }
 ]'::jsonb),

-- ── 迷雾洞窟（15级隐藏区）──
('迷雾洞窟',
 '神秘的地下洞窟，常年被浓雾笼罩，隐藏着不为人知的古代遗物。',
 'hidden_zone',
 15,
 15,
 '{
   "枯骨林": 15,
   "青云山": 12
 }'::jsonb,
 '[
   {
     "name": "秘银",
     "weight": 30,
     "templateId": 87
   },
   {
     "name": "古代遗物",
     "weight": 10,
     "templateId": 97
   },
   {
     "name": "灵玉",
     "weight": 35,
     "templateId": 77
   },
   {
     "name": "噬魂菇",
     "weight": 25,
     "templateId": 54
   }
 ]'::jsonb,
 '[
   {
     "eventType": "ambush",
     "weight": 60
   },
   {
     "eventType": "find_treasure",
     "weight": 20
   },
   {
     "eventType": "weather",
     "weight": 20
   }
 ]'::jsonb),

-- ── 黑风岭（18级历练区）──
('黑风岭',
 '终年刮着黑色狂风的峻岭，怪石嶙峋，常有妖兽出没。山间偶有陨铁坠落，引来四方修士争夺。',
 'training_zone',
 18,
 8,
 '{
   "枯骨林": 10,
   "万妖窟": 12,
   "青云山": 8
 }'::jsonb,
 '[
   {
     "name": "龙血草",
     "weight": 15,
     "templateId": 61
   },
   {
     "name": "星辰铁",
     "weight": 25,
     "templateId": 85
   },
   {
     "name": "冰魄花",
     "weight": 20,
     "templateId": 53
   },
   {
     "name": "灵草",
     "weight": 40,
     "templateId": 47
   }
 ]'::jsonb,
 '[
   {
     "eventType": "ambush",
     "weight": 45
   },
   {
     "eventType": "find_treasure",
     "weight": 15
   },
   {
     "eventType": "weather",
     "weight": 40
   }
 ]'::jsonb),

-- ── 青云山（20级安全主城）──
('青云山',
 '云雾缭绕的仙山，青云峰伫立其间，山顶常年有仙鹤盘旋。山下有仙家集市，可补给丹药。',
 'safe_town',
 20,
 10,
 '{
   "黑风岭": 8,
   "迷雾洞窟": 12,
   "雷霆崖": 10
 }'::jsonb,
 '[]'::jsonb,
 '[]'::jsonb),

-- ── 万妖窟（25级历练区）──
('万妖窟',
 '万妖盘踞的地下迷宫，洞窟交错如蚁穴。传闻最深处镇压着一尊太古妖帝的残魂。',
 'training_zone',
 25,
 12,
 '{
   "黑风岭": 12,
   "雷霆崖": 15,
   "太古遗迹": 30
 }'::jsonb,
 '[
   {
     "name": "噬魂菇",
     "weight": 25,
     "templateId": 54
   },
   {
     "name": "灵玉",
     "weight": 35,
     "templateId": 77
   },
   {
     "name": "黑铁矿石",
     "weight": 40,
     "templateId": 78
   }
 ]'::jsonb,
 '[
   {
     "eventType": "ambush",
     "weight": 55
   },
   {
     "eventType": "find_treasure",
     "weight": 10
   },
   {
     "eventType": "weather",
     "weight": 35
   }
 ]'::jsonb),

-- ── 雷霆崖（30级历练区）──
('雷霆崖',
 '高耸入云的悬崖绝壁，终年雷电交加，霹雳不断。雷鹰在雷云中穿梭，崖壁上随处可见雷击焦痕。',
 'training_zone',
 30,
 15,
 '{
   "青云山": 10,
   "万妖窟": 15,
   "天火山": 15
 }'::jsonb,
 '[
   {
     "name": "雷鹰翎羽",
     "weight": 20,
     "templateId": 168
   },
   {
     "name": "星辰铁",
     "weight": 25,
     "templateId": 85
   },
   {
     "name": "精铁",
     "weight": 55,
     "templateId": 76
   }
 ]'::jsonb,
 '[
   {
     "eventType": "ambush",
     "weight": 50
   },
   {
     "eventType": "find_treasure",
     "weight": 12
   },
   {
     "eventType": "weather",
     "weight": 38
   }
 ]'::jsonb),

-- ── 葬仙谷（35级隐藏区）──
('葬仙谷',
 '一片死寂的荒谷，地面上零星散落着不知年代的枯骨。谷口立着一块残碑，上书"不可说"三字。传说上古有大贤在此以身为道，镇压不可名状之物。',
 'hidden_zone',
 35,
 25,
 '{
   "天火山": 20,
   "归墟海": 25
 }'::jsonb,
 '[
   {
     "name": "天心花",
     "weight": 12,
     "templateId": 62
   },
   {
     "name": "混沌石",
     "weight": 8,
     "templateId": 95
   },
   {
     "name": "龙骨",
     "weight": 15,
     "templateId": 93
   },
   {
     "name": "噬魂菇",
     "weight": 25,
     "templateId": 54
   },
   {
     "name": "玄铁剑",
     "weight": 40,
     "templateId": 5
   }
 ]'::jsonb,
 '[
   {
     "eventType": "ambush",
     "weight": 60
   },
   {
     "eventType": "find_treasure",
     "weight": 10
   },
   {
     "eventType": "weather",
     "weight": 30
   }
 ]'::jsonb),

-- ── 天火山（40级历练区）──
('天火山',
 '永不熄灭的火焰山脉，岩浆如河流般流淌，空气中弥漫着硫磺的气味。深处有凤鸣之声回荡。',
 'training_zone',
 40,
 20,
 '{
   "雷霆崖": 15,
   "葬仙谷": 20
 }'::jsonb,
 '[
   {
     "name": "太阳真金",
     "weight": 15,
     "templateId": 91
   },
   {
     "name": "凤羽",
     "weight": 8,
     "templateId": 94
   },
   {
     "name": "龙血草",
     "weight": 20,
     "templateId": 61
   },
   {
     "name": "赤火莲",
     "weight": 57,
     "templateId": 66
   }
 ]'::jsonb,
 '[
   {
     "eventType": "ambush",
     "weight": 55
   },
   {
     "eventType": "find_treasure",
     "weight": 15
   },
   {
     "eventType": "weather",
     "weight": 30
   }
 ]'::jsonb),

-- ── 归墟海（45级隐藏区）──
('归墟海',
 '仙道大陆尽头的无尽之海，海面漆黑如墨，暗流汹涌。海底沉睡着无数上古时代的遗迹，只有大机缘者能窥其一二。',
 'hidden_zone',
 45,
 30,
 '{
   "葬仙谷": 25
 }'::jsonb,
 '[
   {
     "name": "万年玄冰",
     "weight": 20,
     "templateId": 88
   },
   {
     "name": "冰凤羽毛",
     "weight": 10,
     "templateId": 171
   },
   {
     "name": "秘银",
     "weight": 30,
     "templateId": 87
   },
   {
     "name": "玄冰莲",
     "weight": 40,
     "templateId": 67
   }
 ]'::jsonb,
 '[
   {
     "eventType": "ambush",
     "weight": 50
   },
   {
     "eventType": "find_treasure",
     "weight": 20
   },
   {
     "eventType": "weather",
     "weight": 30
   }
 ]'::jsonb),

-- ── 太古遗迹（50级隐藏区）──
('太古遗迹',
 '洪荒时代的遗存，断壁残垣间残留着远古禁制的微光。一步一险，但也一步一机缘。据说遗迹核心处的石壁上刻着无人能解的太古文字。',
 'hidden_zone',
 50,
 35,
 '{
   "万妖窟": 30
 }'::jsonb,
 '[
   {
     "name": "河图洛书",
     "weight": 3,
     "templateId": 144
   },
   {
     "name": "混沌石",
     "weight": 10,
     "templateId": 95
   },
   {
     "name": "金芝种子",
     "weight": 12,
     "templateId": 114
   },
   {
     "name": "乾坤戒",
     "weight": 5,
     "templateId": 24
   },
   {
     "name": "灵戒",
     "weight": 70,
     "templateId": 23
   }
 ]'::jsonb,
 '[
   {
     "eventType": "ambush",
     "weight": 70
   },
   {
     "eventType": "find_treasure",
     "weight": 25
   },
   {
     "eventType": "weather",
     "weight": 5
   }
 ]'::jsonb);

INSERT INTO xt_map_connection (from_map_id, to_map_id, travel_time_minutes, bidirectional)
VALUES
-- 黑金主城 <-> 幽暗沼泽
((SELECT id FROM xt_map_node WHERE name = '黑金主城'), (SELECT id FROM xt_map_node WHERE name = '幽暗沼泽'), 5, true),
-- 幽暗沼泽 <-> 枯骨林
((SELECT id FROM xt_map_node WHERE name = '幽暗沼泽'), (SELECT id FROM xt_map_node WHERE name = '枯骨林'), 10, true),
-- 枯骨林 <-> 迷雾洞窟
((SELECT id FROM xt_map_node WHERE name = '枯骨林'), (SELECT id FROM xt_map_node WHERE name = '迷雾洞窟'), 15, true),
-- 枯骨林 <-> 黑风岭
((SELECT id FROM xt_map_node WHERE name = '枯骨林'), (SELECT id FROM xt_map_node WHERE name = '黑风岭'), 10, true),
-- 黑风岭 <-> 万妖窟
((SELECT id FROM xt_map_node WHERE name = '黑风岭'), (SELECT id FROM xt_map_node WHERE name = '万妖窟'), 12, true),
-- 黑风岭 <-> 青云山
((SELECT id FROM xt_map_node WHERE name = '黑风岭'), (SELECT id FROM xt_map_node WHERE name = '青云山'), 8, true),
-- 迷雾洞窟 <-> 青云山
((SELECT id FROM xt_map_node WHERE name = '迷雾洞窟'), (SELECT id FROM xt_map_node WHERE name = '青云山'), 12, true),
-- 青云山 <-> 雷霆崖
((SELECT id FROM xt_map_node WHERE name = '青云山'), (SELECT id FROM xt_map_node WHERE name = '雷霆崖'), 10, true),
-- 万妖窟 <-> 雷霆崖
((SELECT id FROM xt_map_node WHERE name = '万妖窟'), (SELECT id FROM xt_map_node WHERE name = '雷霆崖'), 15, true),
-- 雷霆崖 <-> 天火山
((SELECT id FROM xt_map_node WHERE name = '雷霆崖'), (SELECT id FROM xt_map_node WHERE name = '天火山'), 15, true),
-- 天火山 <-> 葬仙谷
((SELECT id FROM xt_map_node WHERE name = '天火山'), (SELECT id FROM xt_map_node WHERE name = '葬仙谷'), 20, true),
-- 葬仙谷 <-> 归墟海
((SELECT id FROM xt_map_node WHERE name = '葬仙谷'), (SELECT id FROM xt_map_node WHERE name = '归墟海'), 25, true),
-- 万妖窟 <-> 太古遗迹
((SELECT id FROM xt_map_node WHERE name = '万妖窟'), (SELECT id FROM xt_map_node WHERE name = '太古遗迹'), 30, true);
