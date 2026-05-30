-- 灵兽模板种子数据 (xt_beast_template)
INSERT INTO xt_beast_template(name, grow_time, production_items, skill_pool, tags, description) VALUES
('火鼠', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'weight', 50)
    )
  ),
  '["beast", "fire"]'::jsonb,
  '火中诞生的小鼠，毛色赤红，碰触之处留下焦痕。'
),
('炎雀', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 50)
    )
  ),
  '["flying", "fire"]'::jsonb,
  '通体火红的小雀，飞翔时拖出一道焰尾。'
),
('赤蛙', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'weight', 50)
    )
  ),
  '["beast", "fire"]'::jsonb,
  '栖息在火山温泉中的蛙类，皮肤滚烫。'
),
('火蚁', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["insect", "fire"]'::jsonb,
  '群居火蚁，单只微不足道，成群则能焚山。'
),
('熔岩蜥', 18,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石')),
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'weight', 50)
    )
  ),
  '["beast", "fire", "earth"]'::jsonb,
  '皮肤如冷却的岩浆，体内流淌着地火之力。'
),
('烛蝎', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 50)
    )
  ),
  '["insect", "fire"]'::jsonb,
  '尾巴上的火焰照亮前路，也照亮了别人的修仙路。燃烧自己，温暖他人。'
),
('火鸦', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["flying", "fire"]'::jsonb,
  '传说日中三足乌的后裔，羽间有火星闪烁。'
),
('暖貂', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'weight', 50)
    )
  ),
  '["beast", "fire"]'::jsonb,
  '毛色橘红的小貂，冬天抱在怀里如同暖炉。'
),
('焰蝶', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50)
    )
  ),
  '["insect", "fire"]'::jsonb,
  '翅膀燃烧着不灭的微焰，飞过之处花香四溢。'
),
('火蟾', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '地火芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '太阳花'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '焚天诀'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '三昧真火'), 'weight', 15),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '地动术'), 'weight', 30)
    )
  ),
  '["beast", "fire"]'::jsonb,
  '腹藏地火的赤蟾，看似憨态可掬实则战力超群。'
),
('炎狼', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '地火芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '赤炼果'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '破风斩'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'weight', 60)
    )
  ),
  '["beast", "fire", "fur"]'::jsonb,
  '鬃毛如火焰般燃烧的狼，群居狩猎。'
),
('赤鬃马', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '太阳花')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '赤铜矿'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '踏波行'), 'weight', 30),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '疾风步'), 'weight', 70)
    )
  ),
  '["beast", "fire", "speed"]'::jsonb,
  '鬃毛赤红的灵马，奔跑时蹄下生火。'
),
('火蝎', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '地火芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 60)
    )
  ),
  '["insect", "fire", "poison"]'::jsonb,
  '毒钩含火毒，被蛰者先受火灼再受毒侵。'
),
('烈焰雀', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '地火芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '风刃'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 60)
    )
  ),
  '["flying", "fire", "wind"]'::jsonb,
  '振翅可引烈风，风助火势，火借风威。'
),
('熔岩龟', 42,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '赤铜矿')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金钟罩'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 60),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '铁布衫'), 'weight', 40)
    )
  ),
  '["beast", "fire", "earth", "shell"]'::jsonb,
  '壳如冷却的岩浆，体内藏有地火之心。'
),
('毕方', 72,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '赤炎花')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '千年灵木')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '焚天诀'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '三昧真火'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '莲华涅槃'), 'weight', 10),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蟠桃仙术'), 'weight', 20)
    )
  ),
  '["flying", "fire", "wood"]'::jsonb,
  '独足神鸟，木生火旺，所过之处草木自燃。'
),
('祸斗', 72,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '赤炎花')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '雷精矿石')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '烈焰铜'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '引雷诀'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '焚天诀'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '五雷正法'), 'weight', 15),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵主杀伐'), 'weight', 25)
    )
  ),
  '["beast", "fire", "thunder"]'::jsonb,
  '犬形异兽，食火焰为生，排泄物亦为火。'
),
('九尾火狐', 72,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '赤炎花')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '太阳花')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天心兰'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '焚天诀'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '三昧真火'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '太乙遁甲'), 'weight', 12),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '逆转丹行'), 'weight', 20)
    )
  ),
  '["beast", "fire", "wisdom"]'::jsonb,
  '九尾火狐，每长一尾便多悟一道天火。'
),
('朱雀', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '赤炎花')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天心兰')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '千年灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '焚天诀'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '三昧真火'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '莲华涅槃'), 'unlock', 'TIER_4'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '一气化三清'), 'unlock', 'TIER_5')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '太白斩魔'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天罡北斗阵'), 'weight', 15)
    )
  ),
  '["flying", "fire", "wood", "auspicious"]'::jsonb,
  '南方神兽，浴火而生，涅槃不灭。其羽间流火足以焚尽万邪。'
),
('灵鲤', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["beast", "water"]'::jsonb,
  '灵溪中的鲤鱼，鳞片泛着微光。'
),
('水蛙', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒露草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["beast", "water"]'::jsonb,
  '栖息在灵泉边的蛙类，叫声能聚水气。'
),
('溪蟹', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒露草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '铁布衫'), 'weight', 50)
    )
  ),
  '["beast", "water", "shell"]'::jsonb,
  '溪流中的灵蟹，壳硬如铁。'
),
('河蚌', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '月华露'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50)
    )
  ),
  '["beast", "water", "shell"]'::jsonb,
  '河底灵蚌，偶尔孕育出灵珠。'
),
('水蛇精', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒露草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["serpent", "water"]'::jsonb,
  '水中修行的小蛇，通体碧绿。'
),
('泽蛙', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50)
    )
  ),
  '["beast", "water", "poison"]'::jsonb,
  '沼泽中的毒蛙，皮肤分泌剧毒黏液。'
),
('雨燕', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 50)
    )
  ),
  '["flying", "water"]'::jsonb,
  '能预知风雨的灵燕，雨前低飞盘旋。'
),
('溪龟', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["beast", "water", "shell"]'::jsonb,
  '溪石间的小龟，壳上生满青苔。'
),
('水母妖', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '月华露'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["spirit", "water"]'::jsonb,
  '灵海中漂浮的水母，通体透明如琉璃。'
),
('碧水蛟', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '雪莲')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '水镜术'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '踏波行'), 'weight', 30),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'weight', 50)
    )
  ),
  '["serpent", "water"]'::jsonb,
  '碧色水蛟，兴风作浪的水中霸主。'
),
('灵龟', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '雪莲')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 60),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '铁布衫'), 'weight', 40)
    )
  ),
  '["beast", "water", "defense"]'::jsonb,
  '灵龟长寿，壳上纹路暗合天道。'
),
('潮蟹', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金钟罩'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '铁布衫'), 'weight', 50)
    )
  ),
  '["beast", "water", "shell"]'::jsonb,
  '随潮汐涨落修行的巨蟹。'
),
('水灵蝶', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '雪莲')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '月华露'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '万木逢春'), 'weight', 20),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["insect", "water", "heal"]'::jsonb,
  '翅膀如水般透明的灵蝶，飞过之处伤口自愈。'
),
('玄水蛇', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒露草')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天魔鞭法'), 'weight', 25),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["serpent", "water", "dark"]'::jsonb,
  '通体墨黑的水蛇，潜伏深渊不问世事。'
),
('千年老龟', 42,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '雪莲')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金钟罩'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '霸下真身'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 60)
    )
  ),
  '["beast", "water", "defense", "shell"]'::jsonb,
  '活了不知几千年，壳上长满了青苔。问它修到什么境界，它说：''忘了。'''
),
('玄武龟', 96,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '冰魄花')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '墨玉菇')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒髓晶'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金钟罩'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '霸下真身'), 'unlock', 'TIER_4')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天罡北斗阵'), 'weight', 8),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混元功'), 'weight', 20)
    )
  ),
  '["beast", "water", "earth", "defense", "shell"]'::jsonb,
  '背负龟蛇的玄武后裔，防御无双，据说万年之后可化作一方城池。'
),
('虎蛟', 54,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '冰魄花')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒髓晶'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '横刀断岳'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '破风斩'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵主杀伐'), 'weight', 18),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '穿杨箭'), 'weight', 30)
    )
  ),
  '["beast", "water", "dragon"]'::jsonb,
  '蛟虎混血的猛兽，鱼尾虎身声如婴儿，水中追风逐浪地上撕金裂石。'
),
('摸鱼鲲', 84,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '墨玉菇')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '冰魄花')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒髓晶'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '水镜术'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '冰封万里'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '袖里乾坤'), 'unlock', 'TIER_4')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蟠桃仙术'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '逆转丹行'), 'weight', 20)
    )
  ),
  '["beast", "water", "myth"]'::jsonb,
  '北冥有鱼，其志不在化鹏，只在随波逐流。修为却深不可测。'
),
('玄冥', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '冰魄花')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '万载玄冰')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄阴水'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '冰封万里'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '绝对零度'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '霸下真身'), 'unlock', 'TIER_4'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '一气化三清'), 'unlock', 'TIER_5')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '莲华涅槃'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天罡北斗阵'), 'weight', 15)
    )
  ),
  '["beast", "water", "ice", "myth", "divine"]'::jsonb,
  '北方水神玄冥的化身，执掌万水之源，寒气可冻结时空。'
),
('灵芝妖', 18,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '逆转丹行'), 'weight', 15)
    )
  ),
  '["plant", "wood", "heal"]'::jsonb,
  '灵芝成精化作的小妖，天生精通草木之道。'
),
('藤蛇', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["serpent", "wood"]'::jsonb,
  '缠绕在古藤上的青蛇，与草木共生。'
),
('花精', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["plant", "wood", "heal"]'::jsonb,
  '百花精华凝聚而成的小精，身带花香。'
),
('木灵蝶', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50)
    )
  ),
  '["insect", "wood"]'::jsonb,
  '翅膀如绿叶的灵蝶，栖息在古木之上。'
),
('翠鸟', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 50)
    )
  ),
  '["flying", "wood"]'::jsonb,
  '羽毛翠绿的小鸟，叫声能催发新芽。'
),
('荷蛙', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒露草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50)
    )
  ),
  '["beast", "wood", "water"]'::jsonb,
  '端坐莲叶之上的灵蛙，静如处子。'
),
('柳灵', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["spirit", "wood"]'::jsonb,
  '柳树化灵，随风摇曳间暗含剑意。'
),
('松鼠灵', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'weight', 50)
    )
  ),
  '["beast", "wood"]'::jsonb,
  '古松间修行的灵松鼠，囤积灵果为生。'
),
('竹节虫', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["insect", "wood"]'::jsonb,
  '以不变应万变，站着就是修行。一动不动是它的道。'
),
('花妖', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '紫丹参')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '金银花'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '霓裳扇舞'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '万木逢春'), 'weight', 20)
    )
  ),
  '["plant", "wood", "charm"]'::jsonb,
  '百花修炼成形的妖精，貌美而善惑人心神。'
),
('古藤蛇', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '何首乌')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '荆棘缠绕'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天魔鞭法'), 'weight', 25),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '缚龙索'), 'weight', 30)
    )
  ),
  '["serpent", "wood", "control"]'::jsonb,
  '千年古藤所化的巨蟒，藤蔓可缠万物。'
),
('翠玉蜂', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '紫丹参')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '金银花'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '万木逢春'), 'weight', 20)
    )
  ),
  '["insect", "wood", "heal"]'::jsonb,
  '翠绿色的灵蜂，蜂蜜可入药。'
),
('灵木猿', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '何首乌')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '千年灵木')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵主杀伐'), 'weight', 20),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混元功'), 'weight', 10)
    )
  ),
  '["beast", "wood", "strength"]'::jsonb,
  '古木间修行的灵猿，力大无穷。'
),
('青藤蟒', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '何首乌')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天魔鞭法'), 'weight', 25),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50)
    )
  ),
  '["serpent", "wood"]'::jsonb,
  '通体碧绿的巨蟒，鳞片如藤甲。'
),
('碧萝蛛', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '骨粉'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '荆棘缠绕'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '缚龙索'), 'weight', 30),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["insect", "wood", "poison"]'::jsonb,
  '以毒丝织网的灵蛛，网上挂着露珠般的毒液。'
),
('九色鹿', 84,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '千年灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '九色灵芝'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '万木逢春'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蟠桃仙术'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '莲华涅槃'), 'unlock', 'TIER_4')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '逆转丹行'), 'weight', 8),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 25)
    )
  ),
  '["beast", "wood", "auspicious", "heal"]'::jsonb,
  '九色神鹿的后裔，平和不争但生命之力浩瀚，拥有极强的恢复能力。'
),
('万年树妖', 84,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '千年灵木')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '千年灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '九天仙草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '万木逢春'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '泰山压顶'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '霸下真身'), 'unlock', 'TIER_4')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混元功'), 'weight', 8),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天罡北斗阵'), 'weight', 20)
    )
  ),
  '["plant", "wood", "defense"]'::jsonb,
  '万年古木修炼成精，根系遍布方圆百里，动则山崩地裂。'
),
('建木灵', 84,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '九天仙草')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '千年灵木')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '菩提叶'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '万木逢春'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蟠桃仙术'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '袖里乾坤'), 'unlock', 'TIER_4')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '一气化三清'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天罡北斗阵'), 'weight', 15)
    )
  ),
  '["plant", "wood", "myth"]'::jsonb,
  '通天建木残留的灵识，沟通天地人三界。'
),
('青龙', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '九天仙草')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '千年灵木')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '万木逢春'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '剑心通明'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '霸下真身'), 'unlock', 'TIER_4'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '一气化三清'), 'unlock', 'TIER_5')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '逆转丹行'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蟠桃仙术'), 'weight', 15)
    )
  ),
  '["dragon", "wood", "water", "auspicious"]'::jsonb,
  '东方神兽，掌生机之力。龙息所过草木逢春，龙威所至百兽俯首。'
),
('铁蚁', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 50)
    )
  ),
  '["insect", "metal"]'::jsonb,
  '以灵铁为食的小蚁，壳如精钢。'
),
('金蝉', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金蝉脱壳'), 'weight', 50)
    )
  ),
  '["insect", "metal"]'::jsonb,
  '金壳灵蝉，蜕下的金壳可入药。'
),
('铜蝎', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 50)
    )
  ),
  '["insect", "metal", "poison"]'::jsonb,
  '铜色毒蝎，尾钩坚逾精钢。'
),
('铁蜥', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '铁布衫'), 'weight', 50)
    )
  ),
  '["beast", "metal"]'::jsonb,
  '鳞片如铁甲的灵蜥，刀枪不入。'
),
('金蝶', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50)
    )
  ),
  '["insect", "metal"]'::jsonb,
  '翅膀如金箔的灵蝶，阳光下熠熠生辉。'
),
('铁甲虫', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '铁布衫'), 'weight', 50)
    )
  ),
  '["insect", "metal", "defense"]'::jsonb,
  '别人修仙我修壳，壳硬就是硬道理。防御力堪比法宝。'
),
('铜蛇', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50)
    )
  ),
  '["serpent", "metal"]'::jsonb,
  '鳞片呈铜色的灵蛇，硬度惊人。'
),
('铁翼雀', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["flying", "metal"]'::jsonb,
  '羽翼如铁的灵雀，振翅有金石之声。'
),
('金龟', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["beast", "metal", "shell"]'::jsonb,
  '壳如金铸的灵龟，富贵人家爱养。'
),
('铁背蜈蚣', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金钟罩'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '铁布衫'), 'weight', 60)
    )
  ),
  '["insect", "metal", "poison"]'::jsonb,
  '百足铁背，每节壳都坚如精钢。'
),
('金翎鹤', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '石斛')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵蚕丝'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '太白斩魔'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 60)
    )
  ),
  '["flying", "metal", "auspicious"]'::jsonb,
  '金色羽毛的灵鹤，展翅如金光万道。'
),
('铜角犀', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '破甲劲'), 'weight', 60)
    )
  ),
  '["beast", "metal", "strength"]'::jsonb,
  '铜角灵犀，一角之力可破万法。'
),
('铁翼蝠', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '骨粉'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '影刺'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '暗影杀'), 'weight', 30)
    )
  ),
  '["flying", "metal", "stealth"]'::jsonb,
  '铁翼夜蝠，暗中突袭无往不利。'
),
('金丝猴', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '砂金矿')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '何首乌')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '通明心法'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '太乙遁甲'), 'weight', 15),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["beast", "metal", "wisdom"]'::jsonb,
  '金毛灵猴，聪慧过人。'
),
('铁爪鹰', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '穿杨箭'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '百步穿杨'), 'weight', 25)
    )
  ),
  '["flying", "metal", "predator"]'::jsonb,
  '爪如铁钩的苍鹰，高空猎杀从不失手。'
),
('白虎', 96,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '紫金砂')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天外陨铁')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '金精草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '太白斩魔'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵主杀伐'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '血魔真经'), 'unlock', 'TIER_4')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天刀九式'), 'weight', 15)
    )
  ),
  '["beast", "metal", "wind", "auspicious"]'::jsonb,
  '西方神兽，主掌杀伐之威。一声虎啸可令千军辟易。'
),
('金翼雕', 60,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天外陨铁')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '金精草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '百步穿杨'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '太白斩魔'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '后羿射日'), 'weight', 8),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '流星箭雨'), 'weight', 20)
    )
  ),
  '["flying", "metal", "predator"]'::jsonb,
  '展翅八丈的金翼雕，高空盘旋时翼间流光如金，攻守兼备的空中霸主。'
),
('铁骨熊', 72,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天外陨铁')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '血纹钢')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金钟罩'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '霸下真身'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混元功'), 'weight', 15)
    )
  ),
  '["beast", "metal", "strength", "defense"]'::jsonb,
  '骨骼如铁的巨熊，一掌可碎山石。'
),
('太白金星兽', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '先天庚金')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '太阳真金')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '苍穹陨铁'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '太白斩魔'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '诛仙剑诀'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '血魔真经'), 'unlock', 'TIER_4'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '一气化三清'), 'unlock', 'TIER_5')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '偷天换日'), 'weight', 3),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轩辕剑法'), 'weight', 5)
    )
  ),
  '["beast", "metal", "celestial", "divine"]'::jsonb,
  '太白金星精气所化的神兽，浑身金光万丈，一吼可碎星辰。'
),
('土拨鼠', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'weight', 50)
    )
  ),
  '["beast", "earth"]'::jsonb,
  '挖洞也是悟道的一种，地下三百丈自有天地。'
),
('石蛙', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["beast", "earth"]'::jsonb,
  '石缝中的灵蛙，叫声如击石。'
),
('泥鳅精', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒露草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["beast", "earth", "water"]'::jsonb,
  '泥中修行的灵鳅，滑不溜手。'
),
('地蚁', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 50)
    )
  ),
  '["insect", "earth"]'::jsonb,
  '地下百丈的灵蚁，以灵土为食。'
),
('石蝎', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 50)
    )
  ),
  '["insect", "earth", "poison"]'::jsonb,
  '石化外壳的蝎子，毒液能石化猎物。'
),
('土蜘蛛', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '骨粉'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '缚龙索'), 'weight', 30),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50)
    )
  ),
  '["insect", "earth", "control"]'::jsonb,
  '以灵土织网的蜘蛛，网坚如铁。'
),
('石蛇精', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50)
    )
  ),
  '["serpent", "earth"]'::jsonb,
  '石化灵蛇，外表如石内蕴灵力。'
),
('泥龟', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '铁布衫'), 'weight', 50)
    )
  ),
  '["beast", "earth", "shell"]'::jsonb,
  '泥沼中的灵龟，壳上覆满灵泥。'
),
('石蝶', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50)
    )
  ),
  '["insect", "earth"]'::jsonb,
  '翅膀如薄石片的灵蝶，飞舞时沙沙作响。'
),
('岩甲犀', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄土根')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '铁布衫'), 'weight', 60)
    )
  ),
  '["beast", "earth", "defense", "strength"]'::jsonb,
  '皮如岩石的巨犀，冲锋时地动山摇。'
),
('地龙蚓', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '地髓芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '骨粉'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '地动术'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 60)
    )
  ),
  '["beast", "earth", "serpent"]'::jsonb,
  '地下修行的巨蚓，能遁地穿山。'
),
('石魔像', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '黑曜石')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄晶'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金钟罩'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '铁布衫'), 'weight', 60),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '霸下真身'), 'weight', 10)
    )
  ),
  '["spirit", "earth", "defense"]'::jsonb,
  '灵石凝聚而成的魔像，无意识地守护一方。'
),
('厚土蟾', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '地髓芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄土根')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '骨粉'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金钟罩'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 60),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混元功'), 'weight', 10)
    )
  ),
  '["beast", "earth", "defense"]'::jsonb,
  '土行灵蟾，一坐便如山岳不可动摇。'
),
('山魈', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄土根')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '影刺'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '暗影杀'), 'weight', 30),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["beast", "earth", "stealth"]'::jsonb,
  '山中精怪，来去无踪善于伏击。'
),
('穿山甲灵', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄土根')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金钟罩'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '铁布衫'), 'weight', 60),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '破甲劲'), 'weight', 40)
    )
  ),
  '["beast", "earth", "shell"]'::jsonb,
  '修行千年的穿山甲，鳞甲可破万法。'
),
('麒麟', 96,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '麒麟草')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄黄根'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '泰山压顶'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天罡北斗阵'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蟠桃仙术'), 'unlock', 'TIER_4')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '莲华涅槃'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混元功'), 'weight', 15)
    )
  ),
  '["beast", "earth", "fire", "auspicious"]'::jsonb,
  '中央神兽，祥瑞之兆。麒麟所至万物安宁，瑞气满堂。'
),
('石巨人', 72,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '黑曜石')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄黄根')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '千年灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '泰山压顶'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '霸下真身'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混元功'), 'weight', 8),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天罡北斗阵'), 'weight', 20)
    )
  ),
  '["spirit", "earth", "strength", "defense"]'::jsonb,
  '山岳精华凝聚的巨人，一步一震，力可拔山。'
),
('玄岩蟒', 72,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄黄根')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '血纹钢')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金钟罩'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '霸下真身'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天魔鞭法'), 'weight', 25)
    )
  ),
  '["serpent", "earth", "defense"]'::jsonb,
  '鳞片如玄岩的巨蟒，静卧时与山石无异。'
),
('黄龙', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '地脉精华')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '麒麟草')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄黄根'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '泰山压顶'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天罡北斗阵'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '霸下真身'), 'unlock', 'TIER_4'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '一气化三清'), 'unlock', 'TIER_5')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '莲华涅槃'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蟠桃仙术'), 'weight', 15)
    )
  ),
  '["dragon", "earth", "auspicious", "divine"]'::jsonb,
  '中央黄龙，执掌大地之力，一爪可裂山河，一息可定乾坤。'
),
('雪兔', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒露草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["beast", "ice"]'::jsonb,
  '冷到不想动，但产出很稳定。不动就是最好的修行。'
),
('冰蚕', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵蚕丝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒露草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50)
    )
  ),
  '["insect", "ice", "silk"]'::jsonb,
  '吐出的冰丝可织天衣，寒暑不侵。'
),
('霜蛾', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒露草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50)
    )
  ),
  '["insect", "ice"]'::jsonb,
  '翅膀覆满寒霜的灵蛾，飞过之处气温骤降。'
),
('雪鼠', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'weight', 50)
    )
  ),
  '["beast", "ice"]'::jsonb,
  '雪地中的小白鼠，毛色纯白如雪。'
),
('冰蝶', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒露草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50)
    )
  ),
  '["insect", "ice"]'::jsonb,
  '翅膀如冰晶的灵蝶，阳光下折射七彩光芒。'
),
('霜蛙', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒露草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["beast", "ice"]'::jsonb,
  '冰面上的灵蛙，皮肤结着薄霜。'
),
('雪雀', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 50)
    )
  ),
  '["flying", "ice"]'::jsonb,
  '雪原上的灵雀，叫声清脆如冰凌碰撞。'
),
('冰甲虫', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒露草')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '铁布衫'), 'weight', 50)
    )
  ),
  '["insect", "ice", "shell"]'::jsonb,
  '壳如冰晶的灵虫，低温下异常坚硬。'
),
('霜蛇', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒露草')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["serpent", "ice"]'::jsonb,
  '通体银白的灵蛇，鳞片如霜花。'
),
('雪狐', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 80, 'template_id', (SELECT id FROM xt_item_template WHERE name = '雪莲')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '冰魄花'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '水镜术'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '冰封万里'), 'weight', 20),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '踏波行'), 'weight', 30)
    )
  ),
  '["beast", "ice", "wisdom"]'::jsonb,
  '浑身雪白的灵狐，喜欢在雪地里打滚。'
),
('冰角鹿', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '雪莲')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '月华露'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '水镜术'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '踏波行'), 'weight', 30)
    )
  ),
  '["beast", "ice", "auspicious"]'::jsonb,
  '冰角如水晶的灵鹿，踏雪无痕。'
),
('霜狼', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '雪莲')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '破风斩'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'weight', 60)
    )
  ),
  '["beast", "ice", "pack"]'::jsonb,
  '霜原上的群狼，呼气成冰。'
),
('冰鹤', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '雪莲')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵蚕丝')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '月华露'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '水镜术'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 60)
    )
  ),
  '["flying", "ice", "auspicious"]'::jsonb,
  '通体如冰雕的灵鹤，展翅可引风雪。'
),
('寒蟾', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '雪莲')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '太阴菇')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '月华露'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '水镜术'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '通明心法'), 'weight', 30)
    )
  ),
  '["beast", "ice", "moon"]'::jsonb,
  '月中寒蟾的后裔，月圆之夜寒气最盛。'
),
('雪猿', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '雪莲')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵主杀伐'), 'weight', 20),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混元功'), 'weight', 10)
    )
  ),
  '["beast", "ice", "strength"]'::jsonb,
  '雪山之巅的白色巨猿，力大而性温。'
),
('黑水玄蛇', 72,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '冰魄花')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒髓晶')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '万载玄冰'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '冰封万里'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天魔鞭法'), 'weight', 25),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '绝对零度'), 'weight', 10)
    )
  ),
  '["beast", "ice", "water", "serpent"]'::jsonb,
  '上古黑水中的玄蛇后裔，身长百丈通体墨黑，寒气逼人冰封千里。'
),
('冰凤', 72,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '冰魄花')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒髓晶')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵蚕丝'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '水镜术'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '冰封万里'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '莲华涅槃'), 'weight', 8),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '逆转丹行'), 'weight', 20)
    )
  ),
  '["flying", "ice", "phoenix"]'::jsonb,
  '冰晶凝聚的凤凰，展翅则万里冰封。'
),
('玄冰巨蟒', 72,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '万载玄冰')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒髓晶')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金钟罩'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '霸下真身'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天魔鞭法'), 'weight', 25)
    )
  ),
  '["serpent", "ice", "defense"]'::jsonb,
  '万年玄冰中诞生的巨蟒，鳞片坚逾寒铁。'
),
('玄冰螭龙', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '万载玄冰')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '冰凤骨')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄阴水'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '冰封万里'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '绝对零度'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '霸下真身'), 'unlock', 'TIER_4'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '一气化三清'), 'unlock', 'TIER_5')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '莲华涅槃'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天罡北斗阵'), 'weight', 15)
    )
  ),
  '["dragon", "ice", "water", "divine"]'::jsonb,
  '无角螭龙中的冰属后裔，通体玄冰铸就，一怒则千里冰封。'
),
('雷蛙', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '引雷诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50)
    )
  ),
  '["beast", "thunder"]'::jsonb,
  '身上噼啪放电的小蛙，碰一下手会麻半天。'
),
('风蝶', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '风刃'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50)
    )
  ),
  '["insect", "wind"]'::jsonb,
  '翅膀能引微风的灵蝶，随风而舞。'
),
('电鳗', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒露草')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '引雷诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["beast", "thunder", "water"]'::jsonb,
  '水中放电的灵鳗，触之浑身酥麻。'
),
('雷雀', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '引雷诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["flying", "thunder"]'::jsonb,
  '飞行时羽间噼啪作响的灵雀。'
),
('风鼠', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '风刃'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '疾风步'), 'weight', 50)
    )
  ),
  '["beast", "wind", "speed"]'::jsonb,
  '快如疾风的小鼠，眨眼间便不见踪影。'
),
('电蝎', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '引雷诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 50)
    )
  ),
  '["insect", "thunder", "poison"]'::jsonb,
  '尾钩放电的毒蝎，电毒双杀。'
),
('雷蚕', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵蚕丝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '引雷诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50)
    )
  ),
  '["insect", "thunder", "silk"]'::jsonb,
  '吐出的丝带微弱电流，触之酥麻。'
),
('风蛇', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '风刃'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["serpent", "wind"]'::jsonb,
  '行动快如疾风的灵蛇，捕猎时几乎看不见身影。'
),
('电萤', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '引雷诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["insect", "thunder"]'::jsonb,
  '一闪一闪亮晶晶，看似无害实则蕴含天雷之力。'
),
('雷鹰', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '紫雷芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天雷竹')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '引雷诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '穿杨箭'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '百步穿杨'), 'weight', 25)
    )
  ),
  '["flying", "thunder", "predator"]'::jsonb,
  '振翅引雷的苍鹰，猎物常被劈晕。'
),
('风灵鹤', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '风铃花')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵蚕丝')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '月华露'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '风刃'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '踏波行'), 'weight', 30),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["flying", "wind", "auspicious"]'::jsonb,
  '御风而行的灵鹤，翱翔九天不倦。'
),
('电狼', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '紫雷芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '引雷诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '破风斩'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'weight', 60)
    )
  ),
  '["beast", "thunder", "pack"]'::jsonb,
  '鬃毛带电的灵狼，群猎时雷电交加。'
),
('雷蜥蜴', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '紫雷芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '雷纹木')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '引雷诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '铁布衫'), 'weight', 60)
    )
  ),
  '["beast", "thunder", "scale"]'::jsonb,
  '鳞片放电的灵蜥，受惊时全身电弧闪烁。'
),
('风翼蛇', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '风铃花')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '风刃'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天魔鞭法'), 'weight', 25)
    )
  ),
  '["serpent", "wind", "flying"]'::jsonb,
  '生有薄翼的灵蛇，可借风滑翔。'
),
('雷猿', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '紫雷芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '引雷诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '五雷正法'), 'weight', 15),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混元功'), 'weight', 10)
    )
  ),
  '["beast", "thunder", "strength"]'::jsonb,
  '浑身缠绕雷电的巨猿，拳出如雷殛。'
),
('夔牛', 120,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '雷精矿石')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '紫雷芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天雷竹'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '引雷诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '五雷正法'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '九天神雷'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '九天魔音'), 'unlock', 'TIER_4'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '一气化三清'), 'unlock', 'TIER_5')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混沌钟'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天罡北斗阵'), 'weight', 15)
    )
  ),
  '["beast", "thunder", "myth"]'::jsonb,
  '上古异兽，状如牛、苍身无角、一足，出入水则必有风雨，其声如雷。'
),
('飞廉', 84,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '风暴之心')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '风灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '虚空石'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '风刃'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '风卷残云'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '裂风斩'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '太乙遁甲'), 'unlock', 'TIER_4')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天风灭世'), 'weight', 8),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '偷天换日'), 'weight', 12)
    )
  ),
  '["beast", "wind", "myth"]'::jsonb,
  '风神飞廉的后裔，鹿身鸟首，振翅则狂风大作。'
),
('雷鹏', 84,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '雷精矿石')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '风暴之心')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '虚空石'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '引雷诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '风刃'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '九天神雷'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天风灭世'), 'unlock', 'TIER_4')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '后羿射日'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '一气化三清'), 'weight', 8)
    )
  ),
  '["flying", "thunder", "wind", "predator"]'::jsonb,
  '展翅遮天的雷鹏，翼间雷电交加，一飞九万里。'
),
('应龙', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天劫晶')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '雷精矿石')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '风暴之心'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '引雷诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '九天神雷'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天风灭世'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '袖里乾坤'), 'unlock', 'TIER_4'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '一气化三清'), 'unlock', 'TIER_5')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '偷天换日'), 'weight', 3),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轩辕剑法'), 'weight', 5)
    )
  ),
  '["dragon", "thunder", "wind", "flying", "myth"]'::jsonb,
  '有翼神龙，呼风唤雷，曾助黄帝斩蚩尤。一怒则天崩地裂。'
),
('灵雀', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50)
    )
  ),
  '["flying", "beast"]'::jsonb,
  '最常见的灵鸟，叽叽喳喳却有微弱灵性。'
),
('风燕', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '风刃'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '疾风步'), 'weight', 50)
    )
  ),
  '["flying", "wind"]'::jsonb,
  '春来秋去的灵燕，飞行速度极快。'
),
('云鸽', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["flying", "beast"]'::jsonb,
  '送信比修仙快，但修为也在每一次振翅中增长。'
),
('灵鹦', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '通明心法'), 'weight', 30),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50)
    )
  ),
  '["flying", "wisdom"]'::jsonb,
  '能学人言的灵鹦鹉，偶尔还会背诵心法。'
),
('雾鹭', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 50)
    )
  ),
  '["flying", "water"]'::jsonb,
  '雾中捕鱼的灵鹭，身影若隐若现。'
),
('烟鹤', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["flying", "fire"]'::jsonb,
  '羽毛如烟雾缭绕的灵鹤，仙气飘飘。'
),
('霞鸠', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50)
    )
  ),
  '["flying", "beast"]'::jsonb,
  '晚霞中归巢的灵鸠，羽毛映着霞光。'
),
('岚鸦', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["flying", "earth"]'::jsonb,
  '栖息山岚间的灵鸦，叫声回荡山谷。'
),
('霓莺', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["flying", "beast"]'::jsonb,
  '羽毛七彩的小莺，啼声婉转如天籁。'
),
('铁羽鹰', 48,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '穿杨箭'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '连珠箭'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 30),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '百步穿杨'), 'weight', 20)
    )
  ),
  '["flying", "metal", "predator"]'::jsonb,
  '铁羽如刃的苍鹰，振翅三丈便可御风而行。'
),
('比翼鸟', 42,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵蚕丝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '月华露'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '霓裳扇舞'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '莲华涅槃'), 'weight', 10),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '万木逢春'), 'weight', 25)
    )
  ),
  '["flying", "support"]'::jsonb,
  '一翼一目，比翼双飞的灵鸟。为伙伴提供源源不断的治疗之力。'
),
('云鹏', 60,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '风暴之心')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '虚空石')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵蚕丝'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '风刃'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '风卷残云'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '裂风斩'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天风灭世'), 'weight', 8),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '偷天换日'), 'weight', 12)
    )
  ),
  '["flying", "wind", "myth"]'::jsonb,
  '鲲化为鹏，扶摇直上九万里。'
),
('灵鹤', 48,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天心兰')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵蚕丝')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '月华露'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '万木逢春'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '逆转丹行'), 'weight', 15),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蟠桃仙术'), 'weight', 8)
    )
  ),
  '["flying", "auspicious", "heal"]'::jsonb,
  '仙人座骑的灵鹤，通灵性知吉凶。'
),
('风隼', 48,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '风铃花')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '风刃'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '穿杨箭'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '百步穿杨'), 'weight', 25)
    )
  ),
  '["flying", "wind", "speed"]'::jsonb,
  '最快的灵禽之一，俯冲时可破音障。'
),
('雾鹰', 48,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '月华露')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '穿杨箭'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'weight', 40),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '暗影杀'), 'weight', 30)
    )
  ),
  '["flying", "water", "stealth"]'::jsonb,
  '雾中隐匿的灵鹰，猎物到死不知从何而来。'
),
('青鸾', 72,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵蚕丝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天心兰')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '千年灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '飞仙剑法'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '剑心通明'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青莲剑歌'), 'weight', 12),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '霓裳扇舞'), 'weight', 30)
    )
  ),
  '["flying", "phoenix", "auspicious"]'::jsonb,
  '青色的凤凰后裔，羽间流转七彩霞光，所过之处百花竞放。'
),
('天马', 66,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天心兰')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵蚕丝'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '踏波行'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '穿云枪'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '游龙枪法'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '太乙遁甲'), 'weight', 12),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '霸王枪'), 'weight', 25)
    )
  ),
  '["flying", "speed", "myth"]'::jsonb,
  '背生双翼的雪白天马，御风凌虚踏空而行，速度冠绝群兽。'
),
('大鹏金翅鸟', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '凤羽')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '虚空石')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '太阳真金'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '风刃'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '太白斩魔'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '裂风斩'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '后羿射日'), 'unlock', 'TIER_4'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '一气化三清'), 'unlock', 'TIER_5')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '偷天换日'), 'weight', 3),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轩辕剑法'), 'weight', 5)
    )
  ),
  '["flying", "metal", "myth", "divine"]'::jsonb,
  '佛经所载金翅大鹏，展翅三百三十六万里，以龙为食。'
),
('灵蛇', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50)
    )
  ),
  '["serpent", "beast"]'::jsonb,
  '山间最常见的灵蛇，性温无毒。'
),
('青蛇', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'weight', 30)
    )
  ),
  '["serpent", "wood"]'::jsonb,
  '竹林间的青色灵蛇，与竹共生。'
),
('赤蛇', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'weight', 30),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["serpent", "fire"]'::jsonb,
  '通体赤红的灵蛇，体温灼人。'
),
('花蟒', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50)
    )
  ),
  '["serpent", "beast"]'::jsonb,
  '花纹斑斓的巨蟒，虽无毒但缠力惊人。'
),
('水蛟', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒露草')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'weight', 30),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["serpent", "water"]'::jsonb,
  '水中修行的小蛟，蛇身蛟首，通体碧绿。'
),
('草蛇', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'weight', 30)
    )
  ),
  '["serpent", "earth"]'::jsonb,
  '草地中的灵蛇，善于伪装。'
),
('石蛟', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'weight', 30)
    )
  ),
  '["serpent", "earth"]'::jsonb,
  '石化外表的灵蛟，外刚内柔。'
),
('金蛇', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'weight', 30)
    )
  ),
  '["serpent", "metal"]'::jsonb,
  '盘着不动省力气，偶尔吐吐信子就算修炼了。'
),
('墨蛇', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '骨粉'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '影刺'), 'weight', 30)
    )
  ),
  '["serpent", "dark"]'::jsonb,
  '通体墨黑的灵蛇，暗中行动无声无息。'
),
('碧鳞蛇', 48,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天魔鞭法'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '修罗鞭'), 'weight', 12),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '缚龙索'), 'weight', 25)
    )
  ),
  '["serpent", "poison"]'::jsonb,
  '碧鳞蛇妖产下的灵蛇，毒牙锐利。'
),
('蛟蜥', 48,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '水镜术'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '踏波行'), 'weight', 30),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'weight', 50)
    )
  ),
  '["serpent", "dragon", "water"]'::jsonb,
  '蛟龙未成形前的形态，蜥蜴身蛟龙首。'
),
('蟒精', 48,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '血纹钢'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵主杀伐'), 'weight', 20),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混元功'), 'weight', 10)
    )
  ),
  '["serpent", "strength"]'::jsonb,
  '修行千年的巨蟒，一缠可碎山石。'
),
('赤鳞蛇', 48,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '地火芝')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '焚天诀'), 'weight', 20),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天魔鞭法'), 'weight', 25)
    )
  ),
  '["serpent", "fire", "scale"]'::jsonb,
  '赤色鳞片的灵蛇，鳞片可引火。'
),
('角蛇', 48,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天魔鞭法'), 'weight', 25),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '缚龙索'), 'weight', 30)
    )
  ),
  '["serpent", "dragon"]'::jsonb,
  '头上生角的灵蛇，已有化蛟之兆。'
),
('翠蛟', 48,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '何首乌')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '万木逢春'), 'weight', 15),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天魔鞭法'), 'weight', 25)
    )
  ),
  '["serpent", "dragon", "wood"]'::jsonb,
  '翠绿色的幼蛟，木属龙族后裔。'
),
('螭龙', 78,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风剑法'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '剑心通明'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '诛仙剑诀'), 'weight', 8),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '万剑归宗'), 'weight', 25)
    )
  ),
  '["dragon", "sword", "beast"]'::jsonb,
  '无角螭龙的后裔，天生通晓剑意，吞吐之间剑气纵横天地。'
),
('内卷蛟', 72,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '血纹钢'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵主杀伐'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '血魔真经'), 'weight', 8),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天刀九式'), 'weight', 20)
    )
  ),
  '["serpent", "dragon", "diligent"]'::jsonb,
  '日夜不休修炼的蛟龙，别的蛟都在休息它还在卷，只为早一日化龙。'
),
('烛龙', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '赤炎花')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '太阳真金'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '焚天诀'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '三昧真火'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '袖里乾坤'), 'unlock', 'TIER_4'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '一气化三清'), 'unlock', 'TIER_5')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '偷天换日'), 'weight', 3),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轩辕剑法'), 'weight', 5)
    )
  ),
  '["dragon", "fire", "light", "myth", "divine"]'::jsonb,
  '钟山之神，视为昼、瞑为夜，吹为冬、呼为夏。身长千里，睁眼天下白，闭眼天下暗。'
),
('福鼠', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '通明心法'), 'weight', 30)
    )
  ),
  '["beast", "auspicious"]'::jsonb,
  '小福星，走到哪儿哪儿就有好运气。修为不高但运气极好。'
),
('瑞兔', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '月华露'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["beast", "auspicious"]'::jsonb,
  '月宫玉兔的凡间血脉，可爱又能干。'
),
('祥鸽', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["flying", "auspicious"]'::jsonb,
  '和平之鸟，祥瑞之兆。'
),
('吉蛙', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["beast", "auspicious", "earth"]'::jsonb,
  '金蟾后裔，口中含金币，招财进宝。'
),
('灵猫', 24,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '何首乌'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'weight', 50)
    )
  ),
  '["beast", "auspicious"]'::jsonb,
  '一只黏人的灵猫，会在你打坐时蹭你，偶尔叼回一株灵芝讨你欢心。'
),
('喜蛛', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '骨粉')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '缚龙索'), 'weight', 30),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50)
    )
  ),
  '["insect", "auspicious"]'::jsonb,
  '蜘蛛中的祥瑞，结网处必有好事发生。'
),
('福蝶', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'weight', 50)
    )
  ),
  '["insect", "auspicious"]'::jsonb,
  '五彩灵蝶，见之有福。'
),
('瑞蛇', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '月华露'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50)
    )
  ),
  '["serpent", "auspicious"]'::jsonb,
  '白蛇呈瑞，见之大吉。'
),
('懒猴', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵木'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '通明心法'), 'weight', 30),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混元功'), 'weight', 10)
    )
  ),
  '["beast", "auspicious", "lazy"]'::jsonb,
  '挂在树上能挂一整天，闭着眼睛也能感应天地灵气。'
),
('玉兔', 24,
  jsonb_build_array(
    jsonb_build_object('weight', 80, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝孢子')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天心兰种子'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天人感应'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '踏波行'), 'weight', 30),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 70)
    )
  ),
  '["beast", "auspicious", "moon"]'::jsonb,
  '月宫玉兔的凡间血脉，不仅可爱还能帮你打理灵田。'
),
('乘黄', 72,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '何首乌')),
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '震地锤'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轰天锤'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '碎虚锤'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '太乙遁甲'), 'weight', 10),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '踏波行'), 'weight', 30)
    )
  ),
  '["beast", "myth", "auspicious"]'::jsonb,
  '《山海经》载：乘黄状如狐，背生角，乘之寿二千。'
),
('白泽幼', 48,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天心兰')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '忘忧草')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '月华露'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '通明心法'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天人感应'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '道心通明'), 'weight', 15),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '太乙遁甲'), 'weight', 12)
    )
  ),
  '["beast", "wisdom", "auspicious"]'::jsonb,
  '白泽幼崽，虽未长成已通灵性。'
),
('瑞鹤', 48,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天心兰')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵蚕丝')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '月华露'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '万木逢春'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '逆转丹行'), 'weight', 15),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蟠桃仙术'), 'weight', 8)
    )
  ),
  '["flying", "auspicious", "heal"]'::jsonb,
  '仙鹤呈瑞，松鹤延年。'
),
('吉祥鹿', 48,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天心兰')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '何首乌')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '还魂草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '万木逢春'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '逆转丹行'), 'weight', 15),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蟠桃仙术'), 'weight', 8)
    )
  ),
  '["beast", "auspicious", "heal"]'::jsonb,
  '梅花灵鹿，鹿茸可续命。'
),
('灵芝仙', 48,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '千年灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '紫丹参')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '还魂草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '万木逢春'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '逆转丹行'), 'weight', 15),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蟠桃仙术'), 'weight', 8)
    )
  ),
  '["plant", "auspicious", "heal"]'::jsonb,
  '灵芝修成仙体，通百草之性。'
),
('白泽', 90,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄晶')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天心兰'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '通明心法'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '道心通明'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天罡北斗阵'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '袖里乾坤'), 'unlock', 'TIER_4')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '太乙遁甲'), 'weight', 8),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '八门金锁'), 'weight', 20)
    )
  ),
  '["beast", "wisdom", "auspicious"]'::jsonb,
  '通晓万物的神兽白泽后裔，智慧通天，能言人语知鬼神之事。'
),
('躺平貘', 72,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '忘忧草')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天心兰')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '月华露'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '通明心法'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '道心通明'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '袖里乾坤'), 'unlock', 'TIER_4')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混元功'), 'weight', 8),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '偷天换日'), 'weight', 12)
    )
  ),
  '["beast", "auspicious", "lazy", "dream"]'::jsonb,
  '以噩梦为食，睡着便是修行。无欲无求，反而境界飞升。'
),
('瘟鼠', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50)
    )
  ),
  '["beast", "evil"]'::jsonb,
  '走到哪儿哪儿倒霉，但它自己浑然不觉。'
),
('毒蛙', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 50)
    )
  ),
  '["beast", "evil", "poison"]'::jsonb,
  '浑身剧毒的灵蛙，碰一下就要排毒三天。'
),
('邪蛛', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '骨粉'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '缚龙索'), 'weight', 30),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50)
    )
  ),
  '["insect", "evil", "poison"]'::jsonb,
  '以怨气为食的毒蛛，网中缠满怨灵。'
),
('魔蛾', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '骨粉'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '影刺'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '暗影杀'), 'weight', 30)
    )
  ),
  '["insect", "evil", "dark"]'::jsonb,
  '扑火的魔蛾，翅膀上的鳞粉有迷幻之效。'
),
('煞蛇', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 50)
    )
  ),
  '["serpent", "evil"]'::jsonb,
  '煞气缠身的灵蛇，所过之处草木枯萎。'
),
('鬼萤', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '骨粉'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '影刺'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '暗影杀'), 'weight', 30)
    )
  ),
  '["insect", "evil", "undead"]'::jsonb,
  '墓地中飘荡的鬼火灵萤，绿光瘆人。'
),
('厄蝎', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 50)
    )
  ),
  '["insect", "evil", "poison"]'::jsonb,
  '剧毒厄蝎，被蛰者厄运缠身三日。'
),
('怨蝠', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '骨粉'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '影刺'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '轻身术'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '暗影杀'), 'weight', 30)
    )
  ),
  '["flying", "evil", "undead"]'::jsonb,
  '以怨气为食的灵蝠，夜间出没。'
),
('瘴蟾', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '茯苓')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '朱砂'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金刚体'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 50)
    )
  ),
  '["beast", "evil", "poison"]'::jsonb,
  '口吐瘴气的毒蟾，方圆百丈寸草不生。'
),
('穷奇幼', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '曼陀罗')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '断肠草')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '破风斩'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '刑天斧法'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵主杀伐'), 'weight', 20),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'weight', 50)
    )
  ),
  '["beast", "evil", "flying"]'::jsonb,
  '四凶之一穷奇的幼崽，已显凶性。'
),
('饕餮幼', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '曼陀罗')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '破风斩'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混元功'), 'weight', 10)
    )
  ),
  '["beast", "evil", "gluttony"]'::jsonb,
  '四凶之一饕餮的幼崽，食量惊人。'
),
('混沌幼', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '曼陀罗')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '断肠草')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '骨粉'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '镇魂钟声'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '逆乱阴阳'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '五雷正法'), 'weight', 15),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '九天魔音'), 'weight', 10)
    )
  ),
  '["beast", "evil", "chaos"]'::jsonb,
  '四凶之一混沌的幼崽，浑敦无面目。'
),
('梼杌幼', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '曼陀罗')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '破风斩'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '横刀断岳'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵主杀伐'), 'weight', 20),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天刀九式'), 'weight', 15)
    )
  ),
  '["beast", "evil", "stubborn"]'::jsonb,
  '四凶之一梼杌的幼崽，顽凶不化。'
),
('猰貐', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '断肠草')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '破风斩'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '刑天斧法'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵主杀伐'), 'weight', 20),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蛮牛劲'), 'weight', 50)
    )
  ),
  '["beast", "evil", "strength"]'::jsonb,
  '上古食人凶兽，力大无穷性情暴虐。'
),
('浑敦', 36,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '曼陀罗')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '幽冥花')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '骨粉'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '镇魂钟声'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '逆乱阴阳'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '九天魔音'), 'weight', 10),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '血魔真经'), 'weight', 15)
    )
  ),
  '["spirit", "evil", "chaos"]'::jsonb,
  '混沌之气凝聚的邪灵，无面目识歌舞。'
),
('穷奇', 120,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '九幽冥铁')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '幽冥花')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '魂玉碎片'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '刑天斧法'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵主杀伐'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '碎虚锤'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '血魔真经'), 'unlock', 'TIER_4'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '盘古开天'), 'unlock', 'TIER_5')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '一气化三清'), 'weight', 3),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天刀九式'), 'weight', 15)
    )
  ),
  '["beast", "evil", "flying"]'::jsonb,
  '上古四凶之一，形似虎而有翼，性情凶暴嗜战。传闻认主后便永不背弃。'
),
('饕餮', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '九幽冥铁')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '魂玉碎片')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '幽冥花'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '刑天斧法'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵主杀伐'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '碎虚锤'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '血魔真经'), 'unlock', 'TIER_4'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '盘古开天'), 'unlock', 'TIER_5')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天刀九式'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混沌钟'), 'weight', 2)
    )
  ),
  '["beast", "evil", "gluttony"]'::jsonb,
  '上古四凶之一，羊身人面，眼在腋下，虎齿人爪。贪食万物，无物不吞。'
),
('梼杌', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '九幽冥铁')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '魂玉碎片')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '破风斩'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '横刀断岳'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵主杀伐'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '血魔真经'), 'unlock', 'TIER_4'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '盘古开天'), 'unlock', 'TIER_5')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天刀九式'), 'weight', 15)
    )
  ),
  '["beast", "evil", "stubborn"]'::jsonb,
  '上古四凶之一，状如虎而犬毛，人面虎足。顽凶不化，战力滔天。'
),
('混沌', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '九幽冥铁')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '魂玉碎片')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '麒麟草')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '地脉精华'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '镇魂钟声'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '逆乱阴阳'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '五雷正法'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '九天魔音'), 'unlock', 'TIER_4'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '一气化三清'), 'unlock', 'TIER_5')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混沌钟'), 'weight', 2),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '盘古开天'), 'weight', 3)
    )
  ),
  '["beast", "evil", "chaos", "fire", "earth"]'::jsonb,
  '上古四凶之一，浑敦无面目，识歌舞。其气混沌未分，蕴含天地初开之力。'
);
