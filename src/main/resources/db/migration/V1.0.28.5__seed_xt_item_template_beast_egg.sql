-- 灵兽卵种子数据 (xt_item_template, type=BEAST_EGG)
INSERT INTO xt_item_template (name, type, properties, tags, description)
SELECT v.name, 'BEAST_EGG',
  jsonb_build_object(
    'grow_time', v.grow_time,
    'production_items', v.production_items,
    'skill_pool', v.skill_pool
  ),
  v.tags, v.description
FROM (VALUES
  ('灵猫卵', 24,
    jsonb_build_array(
      jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵芝')),
      jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name='何首乌'))
    ),
    jsonb_build_object(
      'innate_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='金刚体'), 0), 'unlock', 'tier_1')
      ),
      'awakening_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='轻身术'), 0), 'weight', 50)
      )
    ),
    '["beast_egg","common","beast"]'::jsonb,
    '一只黏人的灵猫，会找药草和在你打坐时蹭你。'),
  ('铁羽鹰卵', 48,
    jsonb_build_array(
      jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name='妖兽皮')),
      jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name='兽骨'))
    ),
    jsonb_build_object(
      'innate_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='穿杨箭'), 0), 'unlock', 'tier_1')
      ),
      'awakening_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='连珠箭'), 0), 'weight', 30)
      )
    ),
    '["beast_egg","uncommon","flying"]'::jsonb,
    '展翅三丈的铁羽鹰，不仅是战斗伙伴更是坐骑。'),
  ('雪狐卵', 36,
    jsonb_build_array(
      jsonb_build_object('weight', 80, 'template_id', (SELECT id FROM xt_item_template WHERE name='雪莲')),
      jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name='冰魄花'))
    ),
    jsonb_build_object(
      'innate_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='寒冰掌'), 0), 'unlock', 'tier_1')
      ),
      'awakening_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='冰封万里'), 0), 'weight', 20)
      )
    ),
    '["beast_egg","uncommon","ice"]'::jsonb,
    '浑身雪白的灵狐，喜欢在雪地里打滚，产出寒属性药材。'),
  ('火蟾卵', 36,
    jsonb_build_array(
      jsonb_build_object('weight', 70, 'template_id', (SELECT id FROM xt_item_template WHERE name='地火芝')),
      jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name='太阳花'))
    ),
    jsonb_build_object(
      'innate_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='烈火掌'), 0), 'unlock', 'tier_1')
      ),
      'awakening_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='焚天诀'), 0), 'weight', 25)
      )
    ),
    '["beast_egg","uncommon","fire"]'::jsonb,
    '腹部火红的大蟾蜍，看似憨态可掬实则战力超群，专产火属性材料。'),
  ('青鸾卵', 72,
    jsonb_build_array(
      jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name='凤羽')),
      jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name='天心兰'))
    ),
    jsonb_build_object(
      'innate_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='清风拂柳'), 0), 'unlock', 'tier_1'),
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='鹰眼术'), 0), 'unlock', 'tier_2')
      ),
      'awakening_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='霓裳扇舞'), 0), 'weight', 15)
      )
    ),
    '["beast_egg","rare","flying","phoenix"]'::jsonb,
    '青色的凤凰后裔，优雅而高傲，凤凰血脉使其自带祥瑞之气。'),
  ('玄武龟卵', 96,
    jsonb_build_array(
      jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name='龙鳞')),
      jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name='玄晶'))
    ),
    jsonb_build_object(
      'innate_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='铁布衫'), 0), 'unlock', 'tier_1'),
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='金钟罩'), 0), 'unlock', 'tier_1')
      ),
      'awakening_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='万劫不磨'), 0), 'weight', 10)
      )
    ),
    '["beast_egg","epic","beast","defense"]'::jsonb,
    '背负龟蛇的玄武后裔，防御无双，据说万年之后可化作一方城池。'),
  ('乘黄卵', 72,
    jsonb_build_array(
      jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name='龙血草')),
      jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name='九天仙草'))
    ),
    jsonb_build_object(
      'innate_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='震地锤'), 0), 'unlock', 'tier_1')
      ),
      'awakening_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='轰天锤'), 0), 'weight', 10)
      )
    ),
    '["beast_egg","rare","beast","myth"]'::jsonb,
    '《山海经》中的神兽，其状如狐，背生角，乘之寿二千。'),
  ('灵蚕卵', 12,
    jsonb_build_array(
      jsonb_build_object('weight', 100, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵蚕丝'))
    ),
    jsonb_build_object(
      'innate_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='静心诀'), 0), 'unlock', 'tier_1')
      ),
      'awakening_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='清风拂柳'), 0), 'weight', 5)
      )
    ),
    '["beast_egg","common","silk"]'::jsonb,
    '小小的灵蚕，不打架不惹事，安安静静吐丝就是它最大的贡献。'),
  ('夔牛卵', 120,
    jsonb_build_array(
      jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name='魂玉碎片')),
      jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name='龙鳞')),
      jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name='混沌石'))
    ),
    jsonb_build_object(
      'innate_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='镇魂钟声'), 0), 'unlock', 'tier_1'),
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='逆乱阴阳'), 0), 'unlock', 'tier_2')
      ),
      'awakening_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='混沌钟'), 0), 'weight', 5)
      )
    ),
    '["beast_egg","legendary","beast","thunder"]'::jsonb,
    '上古异兽夔牛，状如牛、苍身无角、一足，出入水则必有风雨，其声如雷。'),
  ('玉兔卵', 24,
    jsonb_build_array(
      jsonb_build_object('weight', 80, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵芝孢子')),
      jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name='天心兰种子'))
    ),
    jsonb_build_object(
      'innate_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='龙象般若功'), 0), 'unlock', 'tier_1')
      ),
      'awakening_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='天人感应'), 0), 'weight', 30)
      )
    ),
    '["beast_egg","common","beast","moon"]'::jsonb,
    '月宫玉兔的凡间血脉，不仅可爱还能帮你打理灵田，但别让它偷吃你的灵药。'),
  ('穷奇卵', 168,
    jsonb_build_array(
      jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name='白虎骨')),
      jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name='先天庚金')),
      jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name='混沌石'))
    ),
    jsonb_build_object(
      'innate_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='刑天斧法'), 0), 'unlock', 'tier_1'),
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='兵主杀伐'), 0), 'unlock', 'tier_2')
      ),
      'awakening_skills', jsonb_build_array(
        jsonb_build_object('skill_id', COALESCE((SELECT id FROM xt_skill WHERE name='盘古开天'), 0), 'weight', 3)
      )
    ),
    '["beast_egg","legendary","beast","evil"]'::jsonb,
    '上古四凶之一，形似虎而有翼，性情凶暴但认主后忠心不二。')
) AS v(name, grow_time, production_items, skill_pool, tags, description);
