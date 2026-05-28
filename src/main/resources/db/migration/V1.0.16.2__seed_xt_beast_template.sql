-- 灵兽模板种子数据 (xt_beast_template)
INSERT INTO xt_beast_template(name, grow_time, production_items, skill_pool, tags, description) VALUES
-- ==================== 通用灵兽 ====================
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
  '["beast"]'::jsonb,
  '一只黏人的灵猫，会在你打坐时蹭你，偶尔叼回一株灵芝讨你欢心。'
),
('灵蚕', 12,
  jsonb_build_array(
    jsonb_build_object('weight', 100, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵蚕丝'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '静心诀'), 'unlock', 'BIRTH')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 50),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '拂尘净心'), 'weight', 50)
    )
  ),
  '["silk"]'::jsonb,
  '小小的灵蚕，不打架不惹事，安安静静吐丝便是它对主人最大的贡献。'
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
  '["beast","moon"]'::jsonb,
  '月宫玉兔的凡间血脉，不仅可爱还能帮你打理灵田，但别让它偷吃你的灵药。'
),
('灵芝妖', 18,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '何首乌'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '青木诀'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '万木逢春'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '逆转丹行'), 'weight', 15),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '清风拂柳'), 'weight', 35)
    )
  ),
  '["plant","heal"]'::jsonb,
  '灵芝成精化作的小妖，天生精通草木之道，是炼丹修士最爱的福地伙伴。'
),
-- ==================== 稀有灵兽 ====================
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
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '穿心箭'), 'weight', 30)
    )
  ),
  '["flying"]'::jsonb,
  '铁羽如刃的苍鹰，振翅三丈便可御风而行。驯服后可作坐骑，亦是凌厉的战斗伙伴。'
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
  '["ice"]'::jsonb,
  '浑身雪白的灵狐，喜欢在雪地里打滚，产出寒属性药材。'
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
  '["fire"]'::jsonb,
  '腹藏地火的赤蟾，看似憨态可掬实则战力超群，口吐烈焰足以焚石熔金。'
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
  '["beast","poison"]'::jsonb,
  '碧鳞蛇妖产下的灵蛇，毒牙锐利，擅长持续中毒战术。'
),
('虎蛟', 54,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '破风斩'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '横刀断岳'), 'unlock', 'TIER_2')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵主杀伐'), 'weight', 18),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '穿杨箭'), 'weight', 30)
    )
  ),
  '["beast","water"]'::jsonb,
  '蛟虎混血的猛兽，鱼尾虎身声如婴儿，水中追风逐浪地上撕金裂石。'
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
  '["flying","support"]'::jsonb,
  '一翼一目，比翼双飞的灵鸟。虽不能独自翱翔，但可为伙伴提供源源不断的治疗之力。'
),
-- ==================== 史诗灵兽 ====================
('青鸾', 72,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵蚕丝')),
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天心兰种子'))
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
  '["flying","phoenix"]'::jsonb,
  '青色的凤凰后裔，羽间流转七彩霞光，所过之处百花竞放、百鸟朝凤。'
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
  '["beast","myth"]'::jsonb,
  '《山海经》载：乘黄状如狐，背生角，乘之寿二千。其气息温润如春，令人心安。'
),
('金翼雕', 60,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝孢子'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '鹰眼术'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '穿心箭'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '百步穿杨'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '后羿射日'), 'weight', 8),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '流星箭雨'), 'weight', 20)
    )
  ),
  '["flying","predator"]'::jsonb,
  '展翅八丈的金翼雕，高空盘旋时翼间流光如金，攻守兼备的空中霸主。'
),
('螭龙', 78,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石'))
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
  '["dragon","sword"]'::jsonb,
  '无角螭龙的后裔，天生通晓剑意，吞吐之间剑气纵横天地。'
),
('天马', 66,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天心兰种子')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝孢子'))
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
  '["flying","speed"]'::jsonb,
  '背生双翼的雪白天马，御风凌虚踏空而行，速度冠绝群兽。'
),
('黑水玄蛇', 72,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '冰魄花')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '灵蛇鞭'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '寒冰掌'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '水镜术'), 'unlock', 'TIER_3')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '冰封万里'), 'weight', 10),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天魔鞭法'), 'weight', 25)
    )
  ),
  '["beast","ice","water"]'::jsonb,
  '上古黑水中的玄蛇后裔，身长百丈通体墨黑，寒气逼人冰封千里。'
),
('玄武龟', 96,
  jsonb_build_array(
    jsonb_build_object('weight', 60, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄晶'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金钟罩'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '泰山压顶'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '霸下真身'), 'unlock', 'TIER_4')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天罡北斗阵'), 'weight', 8),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混元功'), 'weight', 20)
    )
  ),
  '["beast","defense","water"]'::jsonb,
  '背负龟蛇的玄武后裔，防御无双，据说万年之后可化作一方城池。'
),
('九色鹿', 84,
  jsonb_build_array(
    jsonb_build_object('weight', 50, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵芝孢子'))
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
  '["beast","auspicious","heal"]'::jsonb,
  '九色神鹿的后裔，平和不争但生命之力浩瀚，拥有极强的恢复能力。'
),
('白泽', 90,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄晶')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '天心兰种子'))
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
  '["beast","auspicious","wis"]'::jsonb,
  '通晓万物的神兽白泽后裔，智慧通天，能言人语知鬼神之事。'
),
-- ==================== 传说灵兽 ====================
('夔牛', 120,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄晶')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁'))
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
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混沌钟'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '五雷正法'), 'weight', 20)
    )
  ),
  '["beast","thunder"]'::jsonb,
  '上古异兽夔牛，状如牛、苍身无角、一足，出入水则必有风雨，其声如雷。'
),
('穷奇', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄晶'))
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
  '["beast","evil"]'::jsonb,
  '上古四凶之一，形似虎而有翼，性情凶暴嗜战。传闻认主后便永不背弃。'
),
('朱雀', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '灵蚕丝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '地火芝')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '赤铜矿'))
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
  '["phoenix","fire"]'::jsonb,
  '南方朱雀的后裔，浴火而生，涅槃不灭。其羽间流火足以焚尽万邪。'
),
('青龙', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '蛇涎果')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草'))
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
  '["dragon","wood"]'::jsonb,
  '东方青龙的后裔，掌生机之力。龙息所过草木逢春，龙威所至百兽俯首。'
),
('白虎', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄晶'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵主杀伐'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金灵剑气'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '太白斩魔'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '血魔真经'), 'unlock', 'TIER_4'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '盘古开天'), 'unlock', 'TIER_5')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '兵锋诀'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天刀九式'), 'weight', 15)
    )
  ),
  '["beast","metal"]'::jsonb,
  '西方白虎的后裔，主掌杀伐之威。一声虎啸可令千军辟易，冲锋陷阵天下无敌。'
),
('麒麟', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '麒麟草')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄晶'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '厚土盾'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '泰山压顶'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '天罡北斗阵'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '蟠桃仙术'), 'unlock', 'TIER_4'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '一气化三清'), 'unlock', 'TIER_5')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '莲华涅槃'), 'weight', 5),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混元功'), 'weight', 15)
    )
  ),
  '["auspicious","earth"]'::jsonb,
  '中央麒麟的后裔，祥瑞之兽。麒麟所至万物安宁，瑞气满堂。'
),
('狻猊', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '赤铜矿')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草'))
  ),
  jsonb_build_object(
    'innate_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '烈火掌'), 'unlock', 'BIRTH'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '焚天诀'), 'unlock', 'TIER_2'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '三昧真火'), 'unlock', 'TIER_3'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '金钟罩'), 'unlock', 'TIER_4'),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '一气化三清'), 'unlock', 'TIER_5')
    ),
    'awakening_skills', jsonb_build_array(
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '混元功'), 'weight', 3),
      jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name = '五雷正法'), 'weight', 10)
    )
  ),
  '["beast","fire"]'::jsonb,
  '龙生九子之一，形如狮，喜烟火。其威压可震慑群兽，烈焰焚尽一切邪祟。'
),
('饕餮', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '妖兽皮')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '兽骨')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄晶'))
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
  '["beast","gluttony"]'::jsonb,
  '上古四凶之一，羊身人面，眼在腋下，虎齿人爪。贪食万物，无物不吞。'
),
('混沌', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄晶')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '龙血草')),
    jsonb_build_object('weight', 20, 'template_id', (SELECT id FROM xt_item_template WHERE name = '麒麟草'))
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
  '["beast","chaos"]'::jsonb,
  '上古四凶之一，浑敦无面目，识歌舞。其气混沌未分，蕴含天地初开之力。'
),
('梼杌', 168,
  jsonb_build_array(
    jsonb_build_object('weight', 40, 'template_id', (SELECT id FROM xt_item_template WHERE name = '寒铁')),
    jsonb_build_object('weight', 30, 'template_id', (SELECT id FROM xt_item_template WHERE name = '玄晶')),
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
  '["beast","stubborn"]'::jsonb,
  '上古四凶之一，状如虎而犬毛，人面虎足。顽凶不化，战力滔天。'
);
