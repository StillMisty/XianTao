INSERT INTO xt_map_node (name, description, map_type, level_requirement, neighbors, specialties, travel_events) VALUES
('黑金主城', '仙道大陆最大的城市，繁华的商业中心，各路修仙者云集之地。', 'safe_town', 1, '{"幽暗沼泽":5}'::jsonb, '[]'::jsonb, '[]'::jsonb),
('幽暗沼泽', '常年弥漫着毒雾的沼泽地，盛产珍稀毒草，但也潜伏着危险的沼泽生物。', 'training_zone', 5,
    '{"黑金主城":5,"枯骨林":10}'::jsonb,
    jsonb_build_array(
        jsonb_build_object('weight', 30, 'templateId', (SELECT id FROM xt_item_template WHERE name = '毒龙草')),
        jsonb_build_object('weight', 50, 'templateId', (SELECT id FROM xt_item_template WHERE name = '史莱姆粘液')),
        jsonb_build_object('weight', 20, 'templateId', (SELECT id FROM xt_item_template WHERE name = '灵草'))
    ),
    '[{"eventType":"ambush","weight":40},{"eventType":"find_treasure","weight":10},{"eventType":"weather","weight":50}]'::jsonb),
('枯骨林', '一片枯萎的森林，遍地白骨，传闻曾有强大的古兽陨落于此。', 'training_zone', 10,
    '{"幽暗沼泽":10,"迷雾洞窟":15,"黑风岭":10}'::jsonb,
    jsonb_build_array(
        jsonb_build_object('weight', 45, 'templateId', (SELECT id FROM xt_item_template WHERE name = '铁矿石')),
        jsonb_build_object('weight', 30, 'templateId', (SELECT id FROM xt_item_template WHERE name = '骨片')),
        jsonb_build_object('weight', 25, 'templateId', (SELECT id FROM xt_item_template WHERE name = '灵草'))
    ),
    '[{"eventType":"ambush","weight":50},{"eventType":"find_treasure","weight":15},{"eventType":"weather","weight":35}]'::jsonb),
('迷雾洞窟', '神秘的地下洞窟，常年被浓雾笼罩，隐藏着不为人知的古代遗物。', 'hidden_zone', 15,
    '{"枯骨林":15,"青云山":12}'::jsonb,
    jsonb_build_array(
        jsonb_build_object('weight', 30, 'templateId', (SELECT id FROM xt_item_template WHERE name = '秘银')),
        jsonb_build_object('weight', 10, 'templateId', (SELECT id FROM xt_item_template WHERE name = '古代遗物')),
        jsonb_build_object('weight', 35, 'templateId', (SELECT id FROM xt_item_template WHERE name = '灵玉')),
        jsonb_build_object('weight', 25, 'templateId', (SELECT id FROM xt_item_template WHERE name = '噬魂菇'))
    ),
    '[{"eventType":"ambush","weight":60},{"eventType":"find_treasure","weight":20},{"eventType":"weather","weight":20}]'::jsonb),
('黑风岭', '终年刮着黑色狂风的峻岭，怪石嶙峋，常有妖兽出没。山间偶有陨铁坠落，引来四方修士争夺。', 'training_zone', 18,
    '{"枯骨林":10,"万妖窟":12,"青云山":8}'::jsonb,
    jsonb_build_array(
        jsonb_build_object('weight', 15, 'templateId', (SELECT id FROM xt_item_template WHERE name = '龙血草')),
        jsonb_build_object('weight', 25, 'templateId', (SELECT id FROM xt_item_template WHERE name = '星辰铁')),
        jsonb_build_object('weight', 20, 'templateId', (SELECT id FROM xt_item_template WHERE name = '冰魄花')),
        jsonb_build_object('weight', 40, 'templateId', (SELECT id FROM xt_item_template WHERE name = '灵草'))
    ),
    '[{"eventType":"ambush","weight":45},{"eventType":"find_treasure","weight":15},{"eventType":"weather","weight":40}]'::jsonb),
('青云山', '云雾缭绕的仙山，青云峰伫立其间，山顶常年有仙鹤盘旋。山下有仙家集市，可补给丹药。', 'safe_town', 20,
    '{"黑风岭":8,"迷雾洞窟":12,"雷霆崖":10}'::jsonb,
    '[]'::jsonb,
    '[]'::jsonb),
('万妖窟', '万妖盘踞的地下迷宫，洞窟交错如蚁穴。传闻最深处镇压着一尊太古妖帝的残魂。', 'training_zone', 25,
    '{"黑风岭":12,"雷霆崖":15,"太古遗迹":30}'::jsonb,
    jsonb_build_array(
        jsonb_build_object('weight', 25, 'templateId', (SELECT id FROM xt_item_template WHERE name = '噬魂菇')),
        jsonb_build_object('weight', 35, 'templateId', (SELECT id FROM xt_item_template WHERE name = '灵玉')),
        jsonb_build_object('weight', 40, 'templateId', (SELECT id FROM xt_item_template WHERE name = '黑铁矿石'))
    ),
    '[{"eventType":"ambush","weight":55},{"eventType":"find_treasure","weight":10},{"eventType":"weather","weight":35}]'::jsonb),
('雷霆崖', '高耸入云的悬崖绝壁，终年雷电交加，霹雳不断。雷鹰在雷云中穿梭，崖壁上随处可见雷击焦痕。', 'training_zone', 30,
    '{"青云山":10,"万妖窟":15,"天火山":15}'::jsonb,
    jsonb_build_array(
        jsonb_build_object('weight', 20, 'templateId', (SELECT id FROM xt_item_template WHERE name = '蜂鸟晶羽')),
        jsonb_build_object('weight', 25, 'templateId', (SELECT id FROM xt_item_template WHERE name = '星辰铁')),
        jsonb_build_object('weight', 55, 'templateId', (SELECT id FROM xt_item_template WHERE name = '精铁'))
    ),
    '[{"eventType":"ambush","weight":50},{"eventType":"find_treasure","weight":12},{"eventType":"weather","weight":38}]'::jsonb),
('葬仙谷', '一片死寂的荒谷，地面上零星散落着不知年代的枯骨。谷口立着一块残碑，上书"不可说"三字。传说上古有大贤在此以身为道，镇压不可名状之物。', 'hidden_zone', 35,
    '{"天火山":20,"归墟海":25}'::jsonb,
    jsonb_build_array(
        jsonb_build_object('weight', 12, 'templateId', (SELECT id FROM xt_item_template WHERE name = '天心花')),
        jsonb_build_object('weight', 8, 'templateId', (SELECT id FROM xt_item_template WHERE name = '混沌石')),
        jsonb_build_object('weight', 15, 'templateId', (SELECT id FROM xt_item_template WHERE name = '龙骨')),
        jsonb_build_object('weight', 25, 'templateId', (SELECT id FROM xt_item_template WHERE name = '噬魂菇')),
        jsonb_build_object('weight', 40, 'templateId', (SELECT id FROM xt_item_template WHERE name = '玄铁剑'))
    ),
    '[{"eventType":"ambush","weight":60},{"eventType":"find_treasure","weight":10},{"eventType":"weather","weight":30}]'::jsonb),
('天火山', '永不熄灭的火焰山脉，岩浆如河流般流淌，空气中弥漫着硫磺的气味。深处有凤鸣之声回荡。', 'training_zone', 40,
    '{"雷霆崖":15,"葬仙谷":20}'::jsonb,
    jsonb_build_array(
        jsonb_build_object('weight', 15, 'templateId', (SELECT id FROM xt_item_template WHERE name = '太阳真金')),
        jsonb_build_object('weight', 8, 'templateId', (SELECT id FROM xt_item_template WHERE name = '凤羽')),
        jsonb_build_object('weight', 20, 'templateId', (SELECT id FROM xt_item_template WHERE name = '龙血草')),
        jsonb_build_object('weight', 57, 'templateId', (SELECT id FROM xt_item_template WHERE name = '赤火莲'))
    ),
    '[{"eventType":"ambush","weight":55},{"eventType":"find_treasure","weight":15},{"eventType":"weather","weight":30}]'::jsonb),
('归墟海', '仙道大陆尽头的无尽之海，海面漆黑如墨，暗流汹涌。海底沉睡着无数上古时代的遗迹，只有大机缘者能窥其一二。', 'hidden_zone', 45,
    '{"葬仙谷":25}'::jsonb,
    jsonb_build_array(
        jsonb_build_object('weight', 20, 'templateId', (SELECT id FROM xt_item_template WHERE name = '万年玄冰')),
        jsonb_build_object('weight', 10, 'templateId', (SELECT id FROM xt_item_template WHERE name = '冰凤羽毛')),
        jsonb_build_object('weight', 30, 'templateId', (SELECT id FROM xt_item_template WHERE name = '秘银')),
        jsonb_build_object('weight', 40, 'templateId', (SELECT id FROM xt_item_template WHERE name = '玄冰莲'))
    ),
    '[{"eventType":"ambush","weight":50},{"eventType":"find_treasure","weight":20},{"eventType":"weather","weight":30}]'::jsonb),
('太古遗迹', '洪荒时代的遗存，断壁残垣间残留着远古禁制的微光。一步一险，但也一步一机缘。据说遗迹核心处的石壁上刻着无人能解的太古文字。', 'hidden_zone', 50,
    '{"万妖窟":30}'::jsonb,
    jsonb_build_array(
        jsonb_build_object('weight', 3, 'templateId', (SELECT id FROM xt_item_template WHERE name = '河图洛书')),
        jsonb_build_object('weight', 10, 'templateId', (SELECT id FROM xt_item_template WHERE name = '混沌石')),
        jsonb_build_object('weight', 12, 'templateId', (SELECT id FROM xt_item_template WHERE name = '金芝种子')),
        jsonb_build_object('weight', 5, 'templateId', (SELECT id FROM xt_item_template WHERE name = '乾坤戒')),
        jsonb_build_object('weight', 70, 'templateId', (SELECT id FROM xt_item_template WHERE name = '灵戒'))
    ),
    '[{"eventType":"ambush","weight":70},{"eventType":"find_treasure","weight":25},{"eventType":"weather","weight":5}]'::jsonb);
