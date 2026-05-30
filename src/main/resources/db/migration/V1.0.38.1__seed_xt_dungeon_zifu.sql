-- ============================================================
-- 紫府秘境 (紫府真人飞升前留下的秘境, 金属性)
-- ============================================================
INSERT INTO dungeon_template(
    name, description, element_type, min_level, max_level, max_team_size, timeout_hours,
    access_rules, spirit_config, area_configs
) VALUES (
    '紫府秘境',
    '传说紫府真人飞升前留下的秘境，紫色灵气凝聚为液，汇成小溪在秘境中流淌。紫府真人一生精研金系功法，秘境中处处弥漫着锋锐的金气。',
    'METAL',
    5,
    30,
     1,
    4,
    jsonb_build_array(
        jsonb_build_object('type', 'MAP_NODE', 'node_ids', jsonb_build_array(31)),
        jsonb_build_object('type', 'LEVEL', 'min', 5, 'max', 30)
    ),
    jsonb_build_object(
        'spirit_name', '紫府剑灵',
        'spirit_appearance', '一柄插在古碑前的残剑轻颤，剑身映出模糊人影，剑意凝而不散',
        'personality', '威严寡言，欣赏勇气与坚持，厌恶怯懦与谄媚。视剑道为至高追求。',
        'tone_style', '文言为主，短促有力',
        'greeting', '又见求道者…能走到紫府深处，算你有几分本事。此间机缘，凭本事取之。',
        'affection_system', true
    ),
    jsonb_build_array(
        -- ============ 外围 ============
        jsonb_build_object(
            'key', 'outer',
            'name', '外围',
            'description', '灵雾缭绕的山门区域，紫色灵气弥漫，隐约可见残破的建筑群',
            'type', 'MAIN',
            'main_pois', jsonb_build_array(
                jsonb_build_object(
                    'name', '青石牌坊',
                    'type', 'SEARCH',
                    'description', '布满剑痕的古朴牌坊，似乎记载着什么',
                    'loot_pool', jsonb_build_array(
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝'), 'min_qty', 1, 'max_qty', 3, 'weight', 40),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '血参'), 'min_qty', 1, 'max_qty', 2, 'weight', 30),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'), 'min_qty', 1, 'max_qty', 1, 'weight', 30)
                    )
                ),
                jsonb_build_object(
                    'name', '试炼碑',
                    'type', 'COMBAT',
                    'description', '三丈高的黑色石碑散发着压迫感，碑上灵纹流动',
                    'loot_pool', jsonb_build_array(
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石'), 'min_qty', 1, 'max_qty', 2, 'weight', 40),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '小聚灵丹'), 'min_qty', 1, 'max_qty', 1, 'weight', 30),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'), 'min_qty', 1, 'max_qty', 2, 'weight', 30)
                    ),
                    'monster_pool', jsonb_build_array(
                        jsonb_build_object('template_id', (SELECT id FROM xt_monster_template WHERE name = '铁甲虫'), 'weight', 50),
                        jsonb_build_object('template_id', (SELECT id FROM xt_monster_template WHERE name = '山贼'), 'weight', 50)
                    )
                ),
                jsonb_build_object(
                    'name', '回廊',
                    'type', 'PASSAGE',
                    'description', '通往后殿的漫长回廊，两侧壁画记载着紫府真人的生平',
                    'loot_pool', null,
                    'monster_pool', null
                )
            ),
            'hidden_pois', jsonb_build_array(
                jsonb_build_object(
                    'name', '坍塌丹室',
                    'type', 'SEARCH',
                    'description', '藏在回廊下的秘密丹房，丹炉虽已熄灭，但炉灰中仍有丹药存在',
                    'loot_pool', jsonb_build_array(
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '聚灵丹'), 'min_qty', 1, 'max_qty', 2, 'weight', 50),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '筑基丹'), 'min_qty', 1, 'max_qty', 1, 'weight', 50)
                    ),
                    'clues', jsonb_build_array('回廊地砖有空鼓声', '砖缝有丹香逸出')
                )
            )
        ),
        -- ============ 内围 ============
        jsonb_build_object(
            'key', 'inner',
            'name', '内围',
            'description', '灵气浓度骤然提升，空气中弥漫着锋锐的金行道韵。可见散落的炼器残骸和早已荒废的建筑群。',
            'type', 'MAIN',
            'main_pois', jsonb_build_array(
                jsonb_build_object(
                    'name', '灵兽巢穴',
                    'type', 'COMBAT',
                    'description', '废弃巢穴中盘踞着变异的灵兽，散发着暴戾的气息',
                    'loot_pool', jsonb_build_array(
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'), 'min_qty', 2, 'max_qty', 4, 'weight', 35),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'), 'min_qty', 1, 'max_qty', 3, 'weight', 30),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '玄晶'), 'min_qty', 1, 'max_qty', 1, 'weight', 20),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '大聚灵丹'), 'min_qty', 1, 'max_qty', 1, 'weight', 15)
                    ),
                    'monster_pool', jsonb_build_array(
                        jsonb_build_object('template_id', (SELECT id FROM xt_monster_template WHERE name = '猿妖'), 'weight', 50),
                        jsonb_build_object('template_id', (SELECT id FROM xt_monster_template WHERE name = '冰狼'), 'weight', 50)
                    )
                ),
                jsonb_build_object(
                    'name', '残破丹房',
                    'type', 'SEARCH',
                    'description', '丹房虽已残破，但药架上的玉瓶仍有灵力波动',
                    'loot_pool', jsonb_build_array(
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '小聚灵丹'), 'min_qty', 1, 'max_qty', 3, 'weight', 25),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '聚灵丹'), 'min_qty', 1, 'max_qty', 2, 'weight', 25),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '筑基丹'), 'min_qty', 1, 'max_qty', 1, 'weight', 20),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '壮骨丹'), 'min_qty', 1, 'max_qty', 1, 'weight', 15),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '开智丹'), 'min_qty', 1, 'max_qty', 1, 'weight', 15)
                    )
                ),
                jsonb_build_object(
                    'name', '守护者·金甲将',
                    'type', 'COMBAT',
                    'description', '身披金色重甲的守护者拦在内围通往核心的必经之路上，战意凛然',
                    'loot_pool', jsonb_build_array(
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '玄晶'), 'min_qty', 2, 'max_qty', 5, 'weight', 40),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '魂玉碎片'), 'min_qty', 2, 'max_qty', 4, 'weight', 35),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '大聚灵丹'), 'min_qty', 1, 'max_qty', 3, 'weight', 25)
                    ),
                    'monster_pool', jsonb_build_array(
                        jsonb_build_object('template_id', (SELECT id FROM xt_monster_template WHERE name = '金甲尸'), 'weight', 100)
                    )
                ),
                jsonb_build_object(
                    'name', '剑道石碑',
                    'type', 'PASSAGE',
                    'description', '通往核心区域的石碑，碑上刻着"剑心通明"四字，散发着柔和的金光',
                    'loot_pool', null,
                    'monster_pool', null
                )
            ),
            'hidden_pois', jsonb_build_array(
                jsonb_build_object(
                    'name', '藏经阁',
                    'type', 'SEARCH',
                    'description', '藏于峭壁后的秘密藏经阁，书架上的玉简仍有灵力流转',
                    'loot_pool', jsonb_build_array(
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '金刚体玉简'), 'min_qty', 1, 'max_qty', 1, 'weight', 35),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '金灵剑气玉简'), 'min_qty', 1, 'max_qty', 1, 'weight', 35),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '霸刀诀玉简'), 'min_qty', 1, 'max_qty', 1, 'weight', 30)
                    ),
                    'clues', jsonb_build_array('峭壁之后偶有金光一闪', '能感应到微弱的功法波动')
                )
            ),
            'hidden_areas', jsonb_build_array(
                jsonb_build_object(
                    'key', 'secret_cavern',
                    'trigger_after_resolve', jsonb_build_array('藏经阁')
                )
            )
        ),
        -- ============ 核心 ============
        jsonb_build_object(
            'key', 'core',
            'name', '核心',
            'description', '紫气浓郁如实质，空气中弥漫着恐怖的剑意。地面布满被剑气切割的深痕，这里已经是秘境的最深处。',
            'type', 'MAIN',
            'main_pois', jsonb_build_array(
                jsonb_build_object(
                    'name', '宗门宝库',
                    'type', 'SEARCH',
                    'description', '紫府真人遗留的宝库，虽已被时间侵蚀，但仍藏有大量珍稀宝物',
                    'loot_pool', jsonb_build_array(
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '玄晶'), 'min_qty', 3, 'max_qty', 8, 'weight', 25),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '魂玉碎片'), 'min_qty', 3, 'max_qty', 8, 'weight', 25),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '天外陨铁'), 'min_qty', 1, 'max_qty', 3, 'weight', 20),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '大聚灵丹'), 'min_qty', 2, 'max_qty', 5, 'weight', 15),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '筑基丹'), 'min_qty', 1, 'max_qty', 3, 'weight', 15)
                    )
                ),
                jsonb_build_object(
                    'name', '镇守BOSS·紫府真魂',
                    'type', 'COMBAT',
                    'description', '紫府真人留下的一缕神魂，镇守于此，考验每一位求道者',
                    'loot_pool', jsonb_build_array(
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '魂玉碎片'), 'min_qty', 5, 'max_qty', 10, 'weight', 30),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '玄晶'), 'min_qty', 5, 'max_qty', 10, 'weight', 25),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '天外陨铁'), 'min_qty', 1, 'max_qty', 5, 'weight', 25),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '大聚灵丹'), 'min_qty', 3, 'max_qty', 8, 'weight', 20)
                    ),
                    'monster_pool', jsonb_build_array(
                        jsonb_build_object('template_id', (SELECT id FROM xt_monster_template WHERE name = '修罗'), 'weight', 100)
                    )
                )
            ),
            'hidden_pois', jsonb_build_array(
                jsonb_build_object(
                    'name', '真人之墓',
                    'type', 'SEARCH',
                    'description', '紫府真人埋骨之地，他静静地坐化于此，遗蜕散发着柔和的金光',
                    'loot_pool', jsonb_build_array(
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '天外陨铁'), 'min_qty', 2, 'max_qty', 4, 'weight', 30),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '魂玉碎片'), 'min_qty', 5, 'max_qty', 10, 'weight', 40),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '霸刀诀玉简'), 'min_qty', 1, 'max_qty', 1, 'weight', 30)
                    ),
                    'clues', jsonb_build_array('剑气最浓之处似乎有一缕柔和之意', '核心深处隐约能感应到一股悲悯的气息')
                )
            )
        ),
        -- ============ 隐藏区域 ============
        jsonb_build_object(
            'key', 'secret_cavern',
            'name', '紫府秘境洞天',
            'type', 'HIDDEN',
            'description', '穿过藏经阁后的密道，眼前豁然开朗。这里的灵气浓郁得几乎化为实质，是紫府真人真正的传承所在。',
            'main_pois', jsonb_build_array(
                jsonb_build_object(
                    'name', '紫府试炼',
                    'type', 'COMBAT',
                    'description', '紫府真人留下的最终试炼，剑意凝聚为实体。通过者可得真传。',
                    'loot_pool', jsonb_build_array(
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '魂玉碎片'), 'min_qty', 8, 'max_qty', 15, 'weight', 30),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '天外陨铁'), 'min_qty', 3, 'max_qty', 8, 'weight', 25),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '玄晶'), 'min_qty', 5, 'max_qty', 15, 'weight', 25),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '白泽幼卵'), 'min_qty', 1, 'max_qty', 1, 'weight', 10),
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '九色鹿卵'), 'min_qty', 1, 'max_qty', 1, 'weight', 10)
                    ),
                    'monster_pool', jsonb_build_array(
                        jsonb_build_object('template_id', (SELECT id FROM xt_monster_template WHERE name = '修罗'), 'weight', 100)
                    )
                )
            ),
            'hidden_pois', jsonb_build_array(
                jsonb_build_object(
                    'name', '紫府传承',
                    'type', 'SEARCH',
                    'description', '紫府真人的传承之晶，记载了他一生的剑道感悟',
                    'loot_pool', jsonb_build_array(
                        jsonb_build_object('template_id', (SELECT id FROM xt_item_template WHERE name = '魂玉碎片'), 'min_qty', 10, 'max_qty', 20, 'weight', 100)
                    )
                )
            )
        )
    )
);
