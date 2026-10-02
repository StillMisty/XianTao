-- 100+ 级内容：怪物 / 地图 / 遭遇池（由 tools/balance/gen_high_level_content.py 生成）

INSERT
    INTO
        monster_template(name, description, monster_type, base_level,
            base_hp, base_attack, base_defense, base_speed, exp_reward, skills, drop_table, tags)
    VALUES
    ('太虚烛龙', '烛九阴之遗脉，睁眼为昼、闭眼为夜，盘踞太虚天河之底。', 'BEAST', 102, 17500, 500, 220, 95, 23500,
        '[18, 12, 7]'::jsonb, '[{"max": 2, "min": 1, "weight": 40, "category": "item", "templateId": 4}, {"max": 2, "min": 1, "weight": 20, "category": "item", "templateId": 55}, {"max": 4, "min": 2, "weight": 20, "category": "item", "templateId": 7}, {"max": 1, "min": 1, "weight": 10, "category": "equipment", "templateId": 10}, {"max": 1, "min": 1, "weight": 10, "category": "equipment", "templateId": 32}]'::jsonb, '["beast", "dragon", "myth", "legendary"]'::jsonb),
    ('元凤', '凤凰一族最古之血裔，振翅间星火燎原。', 'FLYING', 104, 20500, 545, 240, 100, 27500,
        '[18, 17, 15, 2]'::jsonb, '[{"max": 4, "min": 2, "weight": 35, "category": "item", "templateId": 4}, {"max": 3, "min": 1, "weight": 25, "category": "item", "templateId": 5}, {"max": 2, "min": 1, "weight": 20, "category": "item", "templateId": 61}, {"max": 1, "min": 1, "weight": 10, "category": "equipment", "templateId": 32}, {"max": 1, "min": 1, "weight": 10, "category": "equipment", "templateId": 64}]'::jsonb, '["flying", "myth", "ultimate"]'::jsonb),
    ('混元兽', '自开天辟地前的混元之气中孕育，无形无相。', 'WILD_BEAST', 106, 24000, 595, 265, 105, 32000,
        '[18, 12, 7, 1]'::jsonb, '[{"max": 2, "min": 1, "weight": 40, "category": "item", "templateId": 16}, {"max": 3, "min": 2, "weight": 25, "category": "item", "templateId": 4}, {"max": 5, "min": 3, "weight": 20, "category": "item", "templateId": 7}, {"max": 1, "min": 1, "weight": 15, "category": "equipment", "templateId": 16}]'::jsonb, '["beast", "chaos", "myth", "ultimate"]'::jsonb),
    ('大罗金仙', '陨落的大罗金仙残念，金身不灭、道韵犹存。', 'HUMAN', 108, 28000, 650, 290, 110, 37500,
        '[18, 17, 12, 6, 7]'::jsonb, '[{"max": 8, "min": 4, "weight": 40, "category": "item", "templateId": 7}, {"max": 4, "min": 2, "weight": 20, "category": "item", "templateId": 4}, {"max": 3, "min": 1, "weight": 15, "category": "item", "templateId": 5}, {"max": 1, "min": 1, "weight": 15, "category": "equipment", "templateId": 27}, {"max": 1, "min": 1, "weight": 10, "category": "equipment", "templateId": 5}]'::jsonb, '["human", "death", "divine", "ultimate"]'::jsonb),
    ('无极魔尊', '魔道极致所化，一念可令星河倒悬。', 'EVIL', 110, 32500, 710, 320, 115, 43500,
        '[18, 12, 7, 17, 1]'::jsonb, '[{"max": 10, "min": 5, "weight": 35, "category": "item", "templateId": 7}, {"max": 5, "min": 3, "weight": 20, "category": "item", "templateId": 4}, {"max": 4, "min": 2, "weight": 15, "category": "item", "templateId": 5}, {"max": 1, "min": 1, "weight": 15, "category": "equipment", "templateId": 16}, {"max": 1, "min": 1, "weight": 10, "category": "equipment", "templateId": 11}, {"max": 1, "min": 1, "weight": 5, "category": "equipment", "templateId": 32}]'::jsonb, '["evil", "primordial", "boss", "ultimate"]'::jsonb),
    ('混世魔猿', '天生地养的混世四猴之首，一怒而山河碎。', 'BEAST', 112, 38000, 775, 350, 120, 51000,
        '[18, 12, 7, 17, 1]'::jsonb, '[{"max": 10, "min": 5, "weight": 35, "category": "item", "templateId": 7}, {"max": 5, "min": 3, "weight": 20, "category": "item", "templateId": 4}, {"max": 4, "min": 2, "weight": 15, "category": "item", "templateId": 5}, {"max": 1, "min": 1, "weight": 15, "category": "equipment", "templateId": 16}, {"max": 1, "min": 1, "weight": 10, "category": "equipment", "templateId": 11}, {"max": 1, "min": 1, "weight": 5, "category": "equipment", "templateId": 32}]'::jsonb, '["evil", "primordial", "boss", "ultimate"]'::jsonb),
    ('太上道君', '太上道统的护道法相，道法自然、无懈可击。', 'HUMAN', 114, 44500, 845, 385, 125, 59500,
        '[18, 17, 12, 6, 7]'::jsonb, '[{"max": 8, "min": 4, "weight": 40, "category": "item", "templateId": 7}, {"max": 4, "min": 2, "weight": 20, "category": "item", "templateId": 4}, {"max": 3, "min": 1, "weight": 15, "category": "item", "templateId": 5}, {"max": 1, "min": 1, "weight": 15, "category": "equipment", "templateId": 27}, {"max": 1, "min": 1, "weight": 10, "category": "equipment", "templateId": 5}]'::jsonb, '["human", "death", "divine", "ultimate"]'::jsonb),
    ('无量天尊', '无量光中显化的天尊法身，镇压诸天。', 'SPIRIT', 116, 52000, 925, 420, 130, 69500,
        '[18, 12, 7, 17]'::jsonb, '[{"max": 3, "min": 1, "weight": 35, "category": "item", "templateId": 55}, {"max": 4, "min": 2, "weight": 25, "category": "item", "templateId": 4}, {"max": 2, "min": 1, "weight": 15, "category": "item", "templateId": 5}, {"max": 1, "min": 1, "weight": 15, "category": "equipment", "templateId": 10}, {"max": 1, "min": 1, "weight": 10, "category": "equipment", "templateId": 11}]'::jsonb, '["beast", "dragon", "divine", "ultimate"]'::jsonb),
    ('大罗天魔', '天魔波旬之大罗化身，蛊惑道心、吞噬气运。', 'EVIL', 118, 61000, 1010, 460, 135, 81500,
        '[18, 12, 7, 17, 1]'::jsonb, '[{"max": 10, "min": 5, "weight": 35, "category": "item", "templateId": 7}, {"max": 5, "min": 3, "weight": 20, "category": "item", "templateId": 4}, {"max": 4, "min": 2, "weight": 15, "category": "item", "templateId": 5}, {"max": 1, "min": 1, "weight": 15, "category": "equipment", "templateId": 16}, {"max": 1, "min": 1, "weight": 10, "category": "equipment", "templateId": 11}, {"max": 1, "min": 1, "weight": 5, "category": "equipment", "templateId": 32}]'::jsonb, '["evil", "primordial", "boss", "ultimate"]'::jsonb),
    ('天劫化身', '天劫本源凝聚的化身，执掌雷罚、审判万灵。', 'SPIRIT', 120, 71000, 1100, 505, 140, 95000,
        '[18, 12, 7, 1]'::jsonb, '[{"max": 2, "min": 1, "weight": 40, "category": "item", "templateId": 16}, {"max": 3, "min": 2, "weight": 25, "category": "item", "templateId": 4}, {"max": 5, "min": 3, "weight": 20, "category": "item", "templateId": 7}, {"max": 1, "min": 1, "weight": 15, "category": "equipment", "templateId": 16}]'::jsonb, '["beast", "chaos", "myth", "ultimate"]'::jsonb),
    ('洪荒祖龙', '洪荒第一头祖龙，龙威所至四海臣服。', 'BEAST', 122, 83000, 1200, 550, 145, 111000,
        '[18, 12, 7, 17]'::jsonb, '[{"max": 3, "min": 1, "weight": 35, "category": "item", "templateId": 55}, {"max": 4, "min": 2, "weight": 25, "category": "item", "templateId": 4}, {"max": 2, "min": 1, "weight": 15, "category": "item", "templateId": 5}, {"max": 1, "min": 1, "weight": 15, "category": "equipment", "templateId": 10}, {"max": 1, "min": 1, "weight": 10, "category": "equipment", "templateId": 11}]'::jsonb, '["beast", "dragon", "divine", "ultimate"]'::jsonb),
    ('鸿蒙道尊', '鸿蒙未判时便已存在的道之化身，万法之源。', 'ARMORED', 125, 105000, 1380, 625, 155, 137000,
        '[18, 12, 7, 17, 1]'::jsonb, '[{"max": 10, "min": 5, "weight": 35, "category": "item", "templateId": 7}, {"max": 5, "min": 3, "weight": 20, "category": "item", "templateId": 4}, {"max": 4, "min": 2, "weight": 15, "category": "item", "templateId": 5}, {"max": 1, "min": 1, "weight": 15, "category": "equipment", "templateId": 16}, {"max": 1, "min": 1, "weight": 10, "category": "equipment", "templateId": 11}, {"max": 1, "min": 1, "weight": 5, "category": "equipment", "templateId": 32}]'::jsonb, '["evil", "primordial", "boss", "ultimate"]'::jsonb);

INSERT
    INTO
        event_type(activity_type, code, name, description)
    VALUES
    ('TRAINING', 'combat_monster_太虚烛龙', '太虚烛龙', '遭遇太虚烛龙'),
    ('TRAINING', 'combat_monster_元凤', '元凤', '遭遇元凤'),
    ('TRAINING', 'combat_monster_混元兽', '混元兽', '遭遇混元兽'),
    ('TRAINING', 'combat_monster_大罗金仙', '大罗金仙', '遭遇大罗金仙'),
    ('TRAINING', 'combat_monster_无极魔尊', '无极魔尊', '遭遇无极魔尊'),
    ('TRAINING', 'combat_monster_混世魔猿', '混世魔猿', '遭遇混世魔猿'),
    ('TRAINING', 'combat_monster_太上道君', '太上道君', '遭遇太上道君'),
    ('TRAINING', 'combat_monster_无量天尊', '无量天尊', '遭遇无量天尊'),
    ('TRAINING', 'combat_monster_大罗天魔', '大罗天魔', '遭遇大罗天魔'),
    ('TRAINING', 'combat_monster_天劫化身', '天劫化身', '遭遇天劫化身'),
    ('TRAINING', 'combat_monster_洪荒祖龙', '洪荒祖龙', '遭遇洪荒祖龙'),
    ('TRAINING', 'combat_monster_鸿蒙道尊', '鸿蒙道尊', '遭遇鸿蒙道尊');

INSERT
    INTO
        map_node(id, name, description, map_type, level_requirement,
            neighbors, specialties, encounter_richness)
    VALUES
    (45, '太虚天河', '横贯太虚的星河之水，河底沉浮着上古神兽的骸骨。', 'TRAINING_ZONE', 102,
                jsonb_build_array(
            jsonb_build_object(
                    'targetId',
                    44,
                    'minutes',
                    10
            ),
            jsonb_build_object(
                    'targetId',
                    43,
                    'minutes',
                    15
            ),
            jsonb_build_object(
                    'targetId',
                    42,
                    'minutes',
                    18
            ),
            jsonb_build_object(
                    'targetId',
                    46,
                    'minutes',
                    14
            )
        ),
                jsonb_build_array(
            jsonb_build_object(
                    'templateId',
                    16,
                    'weight',
                    15
            ),
            jsonb_build_object(
                    'templateId',
                    14,
                    'weight',
                    15
            )
        ),
        7),
    (46, '混元道场', '开天辟地前的混元之气在此流转，道韵化为有形的道场。', 'TRAINING_ZONE', 104,
                jsonb_build_array(
            jsonb_build_object(
                    'targetId',
                    45,
                    'minutes',
                    14
            ),
            jsonb_build_object(
                    'targetId',
                    47,
                    'minutes',
                    12
            )
        ),
                jsonb_build_array(
            jsonb_build_object(
                    'templateId',
                    16,
                    'weight',
                    15
            ),
            jsonb_build_object(
                    'templateId',
                    24,
                    'weight',
                    15
            )
        ),
        7),
    (47, '大罗云海', '大罗天外的云海，每一缕云雾都是一道残破的道法。', 'TRAINING_ZONE', 106,
                jsonb_build_array(
            jsonb_build_object(
                    'targetId',
                    46,
                    'minutes',
                    12
            ),
            jsonb_build_object(
                    'targetId',
                    48,
                    'minutes',
                    12
            )
        ),
                jsonb_build_array(
            jsonb_build_object(
                    'templateId',
                    40,
                    'weight',
                    15
            ),
            jsonb_build_object(
                    'templateId',
                    15,
                    'weight',
                    15
            )
        ),
        7),
    (48, '无极魔渊', '魔气自渊底翻涌而上，坠入者皆成魔。', 'TRAINING_ZONE', 108,
                jsonb_build_array(
            jsonb_build_object(
                    'targetId',
                    47,
                    'minutes',
                    12
            ),
            jsonb_build_object(
                    'targetId',
                    49,
                    'minutes',
                    12
            )
        ),
                jsonb_build_array(
            jsonb_build_object(
                    'templateId',
                    39,
                    'weight',
                    15
            ),
            jsonb_build_object(
                    'templateId',
                    14,
                    'weight',
                    15
            )
        ),
        7),
    (49, '周天星斗', '三百六十五颗主星列阵于此，星光如剑、周而复始。', 'TRAINING_ZONE', 110,
                jsonb_build_array(
            jsonb_build_object(
                    'targetId',
                    48,
                    'minutes',
                    12
            ),
            jsonb_build_object(
                    'targetId',
                    50,
                    'minutes',
                    14
            )
        ),
                jsonb_build_array(
            jsonb_build_object(
                    'templateId',
                    40,
                    'weight',
                    15
            ),
            jsonb_build_object(
                    'templateId',
                    16,
                    'weight',
                    15
            )
        ),
        7),
    (50, '天罚雷池', '雷霆如雨、万古不歇的雷池，雷劫本源在此显化。', 'TRAINING_ZONE', 112,
                jsonb_build_array(
            jsonb_build_object(
                    'targetId',
                    49,
                    'minutes',
                    14
            ),
            jsonb_build_object(
                    'targetId',
                    51,
                    'minutes',
                    12
            )
        ),
                jsonb_build_array(
            jsonb_build_object(
                    'templateId',
                    40,
                    'weight',
                    15
            ),
            jsonb_build_object(
                    'templateId',
                    175,
                    'weight',
                    15
            )
        ),
        7),
    (51, '无量天宫', '无量天尊遗留的天宫，仙乐缭绕，是三界外最后的栖身之所。', 'SAFE_TOWN', 116,
                jsonb_build_array(
            jsonb_build_object(
                    'targetId',
                    50,
                    'minutes',
                    12
            ),
            jsonb_build_object(
                    'targetId',
                    52,
                    'minutes',
                    14
            )
        ),
        jsonb_build_array(),
        3),
    (52, '界外天', '三界之外的虚空夹层，天道法则在此残缺不全。', 'TRAINING_ZONE', 118,
                jsonb_build_array(
            jsonb_build_object(
                    'targetId',
                    51,
                    'minutes',
                    14
            ),
            jsonb_build_object(
                    'targetId',
                    53,
                    'minutes',
                    16
            )
        ),
                jsonb_build_array(
            jsonb_build_object(
                    'templateId',
                    16,
                    'weight',
                    15
            ),
            jsonb_build_object(
                    'templateId',
                    39,
                    'weight',
                    15
            )
        ),
        7),
    (53, '鸿蒙秘境', '鸿蒙初判时被剥离的一角秘境，蕴藏着道之本源。', 'HIDDEN_ZONE', 122,
                jsonb_build_array(
            jsonb_build_object(
                    'targetId',
                    52,
                    'minutes',
                    16
            )
        ),
                jsonb_build_array(
            jsonb_build_object(
                    'templateId',
                    16,
                    'weight',
                    15
            ),
            jsonb_build_object(
                    'templateId',
                    40,
                    'weight',
                    15
            )
        ),
        5);

-- 既有地图追加通往新区域的边（双向连通）
UPDATE map_node SET neighbors = neighbors || jsonb_build_array(jsonb_build_object('targetId', 45, 'minutes', 10)) WHERE id = 44;
UPDATE map_node SET neighbors = neighbors || jsonb_build_array(jsonb_build_object('targetId', 45, 'minutes', 15)) WHERE id = 43;
UPDATE map_node SET neighbors = neighbors || jsonb_build_array(jsonb_build_object('targetId', 45, 'minutes', 18)) WHERE id = 42;

INSERT
    INTO
        activity_event(activity_type, owner_id, code, event_type, weight, params)
    VALUES
    ('TRAINING', 45, 'combat_monster_太虚烛龙', 'COMBAT', 40,
        jsonb_build_object(
            'monster_template_id', (SELECT id FROM monster_template WHERE name = '太虚烛龙'),
            'min_count', 1,
            'max_count', 2
        )),
    ('TRAINING', 45, 'combat_monster_元凤', 'COMBAT', 40,
        jsonb_build_object(
            'monster_template_id', (SELECT id FROM monster_template WHERE name = '元凤'),
            'min_count', 1,
            'max_count', 2
        )),
    ('TRAINING', 46, 'combat_monster_元凤', 'COMBAT', 40,
        jsonb_build_object(
            'monster_template_id', (SELECT id FROM monster_template WHERE name = '元凤'),
            'min_count', 1,
            'max_count', 2
        )),
    ('TRAINING', 46, 'combat_monster_混元兽', 'COMBAT', 40,
        jsonb_build_object(
            'monster_template_id', (SELECT id FROM monster_template WHERE name = '混元兽'),
            'min_count', 1,
            'max_count', 2
        )),
    ('TRAINING', 47, 'combat_monster_混元兽', 'COMBAT', 40,
        jsonb_build_object(
            'monster_template_id', (SELECT id FROM monster_template WHERE name = '混元兽'),
            'min_count', 1,
            'max_count', 2
        )),
    ('TRAINING', 47, 'combat_monster_大罗金仙', 'COMBAT', 40,
        jsonb_build_object(
            'monster_template_id', (SELECT id FROM monster_template WHERE name = '大罗金仙'),
            'min_count', 1,
            'max_count', 2
        )),
    ('TRAINING', 48, 'combat_monster_大罗金仙', 'COMBAT', 40,
        jsonb_build_object(
            'monster_template_id', (SELECT id FROM monster_template WHERE name = '大罗金仙'),
            'min_count', 1,
            'max_count', 2
        )),
    ('TRAINING', 48, 'combat_monster_无极魔尊', 'COMBAT', 40,
        jsonb_build_object(
            'monster_template_id', (SELECT id FROM monster_template WHERE name = '无极魔尊'),
            'min_count', 1,
            'max_count', 2
        )),
    ('TRAINING', 49, 'combat_monster_无极魔尊', 'COMBAT', 40,
        jsonb_build_object(
            'monster_template_id', (SELECT id FROM monster_template WHERE name = '无极魔尊'),
            'min_count', 1,
            'max_count', 2
        )),
    ('TRAINING', 49, 'combat_monster_混世魔猿', 'COMBAT', 40,
        jsonb_build_object(
            'monster_template_id', (SELECT id FROM monster_template WHERE name = '混世魔猿'),
            'min_count', 1,
            'max_count', 2
        )),
    ('TRAINING', 50, 'combat_monster_混世魔猿', 'COMBAT', 40,
        jsonb_build_object(
            'monster_template_id', (SELECT id FROM monster_template WHERE name = '混世魔猿'),
            'min_count', 1,
            'max_count', 2
        )),
    ('TRAINING', 50, 'combat_monster_太上道君', 'COMBAT', 40,
        jsonb_build_object(
            'monster_template_id', (SELECT id FROM monster_template WHERE name = '太上道君'),
            'min_count', 1,
            'max_count', 2
        )),
    ('TRAINING', 52, 'combat_monster_无量天尊', 'COMBAT', 40,
        jsonb_build_object(
            'monster_template_id', (SELECT id FROM monster_template WHERE name = '无量天尊'),
            'min_count', 1,
            'max_count', 2
        )),
    ('TRAINING', 52, 'combat_monster_大罗天魔', 'COMBAT', 40,
        jsonb_build_object(
            'monster_template_id', (SELECT id FROM monster_template WHERE name = '大罗天魔'),
            'min_count', 1,
            'max_count', 2
        )),
    ('TRAINING', 53, 'combat_monster_天劫化身', 'COMBAT', 40,
        jsonb_build_object(
            'monster_template_id', (SELECT id FROM monster_template WHERE name = '天劫化身'),
            'min_count', 1,
            'max_count', 1
        )),
    ('TRAINING', 53, 'combat_monster_洪荒祖龙', 'COMBAT', 40,
        jsonb_build_object(
            'monster_template_id', (SELECT id FROM monster_template WHERE name = '洪荒祖龙'),
            'min_count', 1,
            'max_count', 1
        )),
    ('TRAINING', 53, 'combat_monster_鸿蒙道尊', 'COMBAT', 40,
        jsonb_build_object(
            'monster_template_id', (SELECT id FROM monster_template WHERE name = '鸿蒙道尊'),
            'min_count', 1,
            'max_count', 1
        ));
