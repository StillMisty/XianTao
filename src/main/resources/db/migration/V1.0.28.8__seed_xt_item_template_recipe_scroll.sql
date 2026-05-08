-- 丹方卷轴种子数据 (xt_item_template, type=RECIPE_SCROLL)
-- product.item_id 通过子查询从 xt_item_template 获取
INSERT INTO xt_item_template (name, type, properties, tags, description)
SELECT v.name, 'RECIPE_SCROLL',
  jsonb_build_object(
    'grade', v.grade,
    'product', jsonb_build_object(
      'item_id', (SELECT id FROM xt_item_template WHERE name = v.product_name),
      'quantity', v.product_quantity
    ),
    'requirements', v.requirements
  ),
  v.tags, v.description
FROM (VALUES
  ('小聚灵丹方',  1, '小聚灵丹',   2, '[{"element":"metal","min":1,"max":2}]'::jsonb,                                                                       '["recipe","pill","entry"]'::jsonb,        '小聚灵丹的丹方，简单易学，成丹率高，适合练手。'),
  ('聚灵丹方',    1, '聚灵丹',     1, '[{"element":"metal","min":1,"max":3},{"element":"wood","min":1,"max":3}]'::jsonb,                                     '["recipe","pill","basic"]'::jsonb,        '标准聚灵丹的丹方，所有丹师入门必学。'),
  ('大聚灵丹方',  2, '大聚灵丹',   1, '[{"element":"metal","min":2,"max":4},{"element":"fire","min":1,"max":2}]'::jsonb,                                     '["recipe","pill","intermediate"]'::jsonb, '大聚灵丹的丹方，稍有难度，成丹率随丹师熟练度提高。'),
  ('天元丹方',    2, '天元丹',     1, '[{"element":"metal","min":3,"max":5},{"element":"fire","min":2,"max":3}]'::jsonb,                                     '["recipe","pill","advanced"]'::jsonb,     '天元丹的丹方，需要精纯的药材和熟练的控火技巧。'),
  ('太乙金丹方',  3, '太乙金丹',   1, '[{"element":"metal","min":4,"max":5},{"element":"fire","min":3,"max":5},{"element":"earth","min":2,"max":3}]'::jsonb, '["recipe","pill","epic"]'::jsonb,          '太乙金丹的古方，炼化难度极高，非大丹师不可尝试。'),
  ('九转仙灵丹方',4, '九转仙灵丹', 1, '[{"element":"metal","min":5,"max":5},{"element":"fire","min":4,"max":5},{"element":"water","min":4,"max":5},{"element":"earth","min":4,"max":5},{"element":"wood","min":4,"max":5}]'::jsonb, '["recipe","pill","legendary"]'::jsonb, '传说中的九转仙灵丹古方，一丹可抵百年苦修。'),
  ('回春丹方',    1, '回春丹',     1, '[{"element":"wood","min":1,"max":3}]'::jsonb,                                                                         '["recipe","pill","heal"]'::jsonb,          '回春丹方，以草木精华入药，简单实用。'),
  ('大还丹方',    2, '大还丹',     1, '[{"element":"wood","min":2,"max":4},{"element":"earth","min":1,"max":2}]'::jsonb,                                     '["recipe","pill","heal","intermediate"]'::jsonb, '大还丹的丹方，疗伤圣品，丹师随身常备。'),
  ('九转回春丹方',3, '九转回春丹', 1, '[{"element":"wood","min":3,"max":5},{"element":"water","min":3,"max":5},{"element":"earth","min":2,"max":4}]'::jsonb, '["recipe","pill","heal","advanced"]'::jsonb, '九转炼制的回春丹方，一丹回春天地惊。'),
  ('小还魂丹方',  3, '小还魂丹',   1, '[{"element":"wood","min":3,"max":5},{"element":"water","min":2,"max":4}]'::jsonb,                                     '["recipe","pill","cure","rare"]'::jsonb,  '以还魂草为主材的丹方，一颗可救濒死之命。'),
  ('大还魂丹方',  4, '大还魂丹',   1, '[{"element":"wood","min":4,"max":5},{"element":"water","min":3,"max":5},{"element":"earth","min":3,"max":5}]'::jsonb, '["recipe","pill","heal","epic"]'::jsonb,   '大还魂丹的丹方，传言此方可活死人。'),
  ('洗髓丹方',    1, '洗髓丹',     1, '[{"element":"metal","min":1,"max":3},{"element":"wood","min":1,"max":3},{"element":"water","min":1,"max":3}]'::jsonb,  '["recipe","pill","special","entry"]'::jsonb, '洗髓丹的古老丹方，每个入门者的第一份礼物。'),
  ('壮骨丹方',    1, '壮骨丹',     1, '[{"element":"metal","min":1,"max":2},{"element":"earth","min":1,"max":3}]'::jsonb,                                     '["recipe","pill","stat"]'::jsonb,         '壮骨丹的丹方，想打人更痛就炼它。'),
  ('金刚散方',    1, '金刚散',     1, '[{"element":"metal","min":1,"max":2},{"element":"earth","min":1,"max":3}]'::jsonb,                                     '["recipe","pill","stat"]'::jsonb,         '金刚散的丹方，锤炼皮膜内脏，固本培元。'),
  ('轻身散方',    1, '轻身散',     1, '[{"element":"wood","min":1,"max":2},{"element":"water","min":1,"max":3}]'::jsonb,                                     '["recipe","pill","stat"]'::jsonb,         '轻身散的丹方，服后走路如飞。'),
  ('开智丹方',    1, '开智丹',     1, '[{"element":"wood","min":1,"max":2},{"element":"water","min":1,"max":3}]'::jsonb,                                     '["recipe","pill","stat"]'::jsonb,         '开智丹的丹方，开启灵台，悟性大增。'),
  ('龙力丹方',    3, '龙力丹',     1, '[{"element":"metal","min":3,"max":5},{"element":"earth","min":3,"max":5},{"element":"fire","min":2,"max":4}]'::jsonb,  '["recipe","pill","stat","rare"]'::jsonb,  '以龙血草入药的秘方，力道暴涨的捷径。'),
  ('不动明王丹方',3, '不动明王丹', 1, '[{"element":"metal","min":3,"max":5},{"element":"earth","min":3,"max":5}]'::jsonb,                                     '["recipe","pill","stat","rare"]'::jsonb,  '不动明王丹的秘方，筋骨如金刚不坏。'),
  ('踏风丹方',    3, '踏风丹',     1, '[{"element":"wood","min":3,"max":5},{"element":"water","min":2,"max":4},{"element":"fire","min":1,"max":2}]'::jsonb,   '["recipe","pill","stat","rare"]'::jsonb,  '踏风丹的秘方，御风而行，踏虚凌空。'),
  ('大悟道丹方',  3, '大悟道丹',   1, '[{"element":"wood","min":3,"max":5},{"element":"water","min":3,"max":5}]'::jsonb,                                     '["recipe","pill","stat","rare"]'::jsonb,  '以菩提叶入药的秘方，顿悟破境不是梦。'),
  ('筑基丹方',    2, '筑基丹',     1, '[{"element":"metal","min":2,"max":4},{"element":"wood","min":2,"max":4}]'::jsonb,                                     '["recipe","pill","breakthrough","critical"]'::jsonb, '筑基丹的丹方，每个散修做梦都想得到。'),
  ('结丹丹方',    3, '结丹丹',     1, '[{"element":"metal","min":3,"max":5},{"element":"fire","min":2,"max":4},{"element":"earth","min":2,"max":3}]'::jsonb,  '["recipe","pill","breakthrough","critical"]'::jsonb, '结丹丹的古方，各大宗门视为不外传之秘。'),
  ('化婴丹方',    4, '化婴丹',     1, '[{"element":"metal","min":4,"max":5},{"element":"fire","min":3,"max":5},{"element":"water","min":3,"max":5},{"element":"earth","min":3,"max":5}]'::jsonb, '["recipe","pill","breakthrough","epic"]'::jsonb, '化婴丹的丹方，此方一出，天下丹师无不垂涎。'),
  ('化神丹方',    5, '化神丹',     1, '[{"element":"metal","min":5,"max":5},{"element":"fire","min":4,"max":5},{"element":"water","min":4,"max":5},{"element":"earth","min":4,"max":5},{"element":"wood","min":4,"max":5}]'::jsonb, '["recipe","pill","breakthrough","legendary"]'::jsonb, '化神丹的丹方，天下已失传大半，仅存残卷。'),
  ('瞬回丹方',    2, '瞬回丹',     1, '[{"element":"wood","min":2,"max":4},{"element":"water","min":2,"max":4}]'::jsonb,                                     '["recipe","pill","heal","emergency"]'::jsonb, '瞬回丹的丹方，打架必备，但材料不太好找。'),
  ('抗性丹方',    2, '抗性丹',     1, '[{"element":"metal","min":1,"max":3},{"element":"earth","min":1,"max":3}]'::jsonb,                                    '["recipe","pill","buff"]'::jsonb,          '抗性丹的丹方，提前服用可抵御部分负面状态。'),
  ('避毒丹方',    1, '避毒丹',     1, '[{"element":"wood","min":1,"max":3},{"element":"water","min":1,"max":2}]'::jsonb,                                    '["recipe","pill","buff"]'::jsonb,          '避毒丹的丹方，深入毒瘴之地前务必炼制。'),
  ('清心丹方',    2, '清心丹',     1, '[{"element":"wood","min":2,"max":4},{"element":"water","min":2,"max":4}]'::jsonb,                                    '["recipe","pill","buff","mind"]'::jsonb,   '清心丹的丹方，渡劫前服之可定心神。'),
  ('凝神丹方',    2, '凝神丹',     1, '[{"element":"wood","min":2,"max":3},{"element":"fire","min":1,"max":2}]'::jsonb,                                     '["recipe","pill","buff","mind"]'::jsonb,   '凝神丹的丹方，延长修炼专注时间。'),
  ('破甲丹方',    2, '破甲丹',     1, '[{"element":"metal","min":2,"max":4},{"element":"fire","min":1,"max":3}]'::jsonb,                                    '["recipe","pill","buff","offensive"]'::jsonb, '破甲丹的丹方，服后攻击附带破甲效果。'),
  ('延寿丹方',    4, '延寿丹',     1, '[{"element":"wood","min":4,"max":5},{"element":"water","min":4,"max":5},{"element":"earth","min":3,"max":5}]'::jsonb,  '["recipe","pill","special","rare"]'::jsonb, '延寿丹的丹方，一颗延寿十年，凡人视若仙丹。'),
  ('天劫丹方',    4, '天劫丹',     1, '[{"element":"metal","min":4,"max":5},{"element":"earth","min":4,"max":5},{"element":"fire","min":2,"max":4}]'::jsonb,   '["recipe","pill","tribulation","epic"]'::jsonb, '天劫丹的丹方，渡劫时服下可抵挡三成天雷之力。'),
  ('易容丹方',    1, '易容丹',     1, '[{"element":"wood","min":1,"max":2},{"element":"water","min":1,"max":2}]'::jsonb,                                    '["recipe","pill","utility"]'::jsonb,       '易容丹的丹方，改变面容一个时辰。'),
  ('定颜丹方',    2, '定颜丹',     1, '[{"element":"wood","min":2,"max":4},{"element":"water","min":2,"max":3}]'::jsonb,                                    '["recipe","pill","cosmetic"]'::jsonb,      '定颜丹的丹方，永葆青春，女修界最爱。'),
  ('辟谷丹方',    1, '辟谷丹',     1, '[{"element":"earth","min":1,"max":2}]'::jsonb,                                                                       '["recipe","pill","utility"]'::jsonb,       '辟谷丹的丹方，闭关修炼必备，一粒可一日不食。')
) AS v(name, grade, product_name, product_quantity, requirements, tags, description);
