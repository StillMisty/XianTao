-- 兽卵种子数据 (xt_item_template, type=BEAST_EGG)
INSERT INTO xt_item_template(name, type, properties, tags, description) VALUES
('火鼠卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '火鼠')
  ),
  '["beast_egg", "uncommon", "beast", "fire"]' ::jsonb,
  '蕴含火鼠血脉的灵兽卵，表面隐约可见灵光流转。'
),
('炎雀卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '炎雀')
  ),
  '["beast_egg", "uncommon", "flying", "fire"]' ::jsonb,
  '蕴含炎雀血脉的灵兽卵，表面隐约可见灵光流转。'
),
('赤蛙卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '赤蛙')
  ),
  '["beast_egg", "uncommon", "beast", "fire"]' ::jsonb,
  '蕴含赤蛙血脉的灵兽卵，表面隐约可见灵光流转。'
),
('火蚁卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '火蚁')
  ),
  '["beast_egg", "uncommon", "insect", "fire"]' ::jsonb,
  '蕴含火蚁血脉的灵兽卵，表面隐约可见灵光流转。'
),
('熔岩蜥卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '熔岩蜥')
  ),
  '["beast_egg", "rare", "beast", "fire", "earth"]' ::jsonb,
  '蕴含熔岩蜥血脉的灵兽卵，表面隐约可见灵光流转。'
),
('烛蝎卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '烛蝎')
  ),
  '["beast_egg", "uncommon", "insect", "fire"]' ::jsonb,
  '蕴含烛蝎血脉的灵兽卵，表面隐约可见灵光流转。'
),
('火鸦卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '火鸦')
  ),
  '["beast_egg", "uncommon", "flying", "fire"]' ::jsonb,
  '蕴含火鸦血脉的灵兽卵，表面隐约可见灵光流转。'
),
('暖貂卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '暖貂')
  ),
  '["beast_egg", "uncommon", "beast", "fire"]' ::jsonb,
  '蕴含暖貂血脉的灵兽卵，表面隐约可见灵光流转。'
),
('焰蝶卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '焰蝶')
  ),
  '["beast_egg", "uncommon", "insect", "fire"]' ::jsonb,
  '蕴含焰蝶血脉的灵兽卵，表面隐约可见灵光流转。'
),
('火蟾卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '火蟾')
  ),
  '["beast_egg", "uncommon", "beast", "fire"]' ::jsonb,
  '蕴含火蟾血脉的灵兽卵，表面隐约可见灵光流转。'
),
('炎狼卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '炎狼')
  ),
  '["beast_egg", "rare", "beast", "fire", "fur"]' ::jsonb,
  '蕴含炎狼血脉的灵兽卵，表面隐约可见灵光流转。'
),
('赤鬃马卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '赤鬃马')
  ),
  '["beast_egg", "rare", "beast", "fire", "speed"]' ::jsonb,
  '蕴含赤鬃马血脉的灵兽卵，表面隐约可见灵光流转。'
),
('火蝎卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '火蝎')
  ),
  '["beast_egg", "rare", "insect", "fire", "poison"]' ::jsonb,
  '蕴含火蝎血脉的灵兽卵，表面隐约可见灵光流转。'
),
('烈焰雀卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '烈焰雀')
  ),
  '["beast_egg", "rare", "flying", "fire", "wind"]' ::jsonb,
  '蕴含烈焰雀血脉的灵兽卵，表面隐约可见灵光流转。'
),
('熔岩龟卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '熔岩龟')
  ),
  '["beast_egg", "epic", "beast", "fire", "earth", "shell"]' ::jsonb,
  '蕴含熔岩龟血脉的灵兽卵，表面隐约可见灵光流转。'
),
('毕方卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '毕方')
  ),
  '["beast_egg", "rare", "flying", "fire", "wood"]' ::jsonb,
  '蕴含毕方血脉的灵兽卵，表面隐约可见灵光流转。'
),
('祸斗卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '祸斗')
  ),
  '["beast_egg", "rare", "beast", "fire", "thunder"]' ::jsonb,
  '蕴含祸斗血脉的灵兽卵，表面隐约可见灵光流转。'
),
('九尾火狐卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '九尾火狐')
  ),
  '["beast_egg", "rare", "beast", "fire", "wisdom"]' ::jsonb,
  '蕴含九尾火狐血脉的灵兽卵，表面隐约可见灵光流转。'
),
('朱雀卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '朱雀')
  ),
  '["beast_egg", "epic", "flying", "fire", "wood"]' ::jsonb,
  '蕴含朱雀血脉的灵兽卵，表面隐约可见灵光流转。'
),
('灵鲤卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '灵鲤')
  ),
  '["beast_egg", "uncommon", "beast", "water"]' ::jsonb,
  '蕴含灵鲤血脉的灵兽卵，表面隐约可见灵光流转。'
),
('水蛙卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '水蛙')
  ),
  '["beast_egg", "uncommon", "beast", "water"]' ::jsonb,
  '蕴含水蛙血脉的灵兽卵，表面隐约可见灵光流转。'
),
('溪蟹卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '溪蟹')
  ),
  '["beast_egg", "rare", "beast", "water", "shell"]' ::jsonb,
  '蕴含溪蟹血脉的灵兽卵，表面隐约可见灵光流转。'
),
('河蚌卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '河蚌')
  ),
  '["beast_egg", "rare", "beast", "water", "shell"]' ::jsonb,
  '蕴含河蚌血脉的灵兽卵，表面隐约可见灵光流转。'
),
('水蛇精卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '水蛇精')
  ),
  '["beast_egg", "uncommon", "serpent", "water"]' ::jsonb,
  '蕴含水蛇精血脉的灵兽卵，表面隐约可见灵光流转。'
),
('泽蛙卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '泽蛙')
  ),
  '["beast_egg", "rare", "beast", "water", "poison"]' ::jsonb,
  '蕴含泽蛙血脉的灵兽卵，表面隐约可见灵光流转。'
),
('雨燕卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '雨燕')
  ),
  '["beast_egg", "uncommon", "flying", "water"]' ::jsonb,
  '蕴含雨燕血脉的灵兽卵，表面隐约可见灵光流转。'
),
('溪龟卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '溪龟')
  ),
  '["beast_egg", "rare", "beast", "water", "shell"]' ::jsonb,
  '蕴含溪龟血脉的灵兽卵，表面隐约可见灵光流转。'
),
('水母妖卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '水母妖')
  ),
  '["beast_egg", "uncommon", "spirit", "water"]' ::jsonb,
  '蕴含水母妖血脉的灵兽卵，表面隐约可见灵光流转。'
),
('碧水蛟卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '碧水蛟')
  ),
  '["beast_egg", "uncommon", "serpent", "water"]' ::jsonb,
  '蕴含碧水蛟血脉的灵兽卵，表面隐约可见灵光流转。'
),
('灵龟卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '灵龟')
  ),
  '["beast_egg", "rare", "beast", "water", "defense"]' ::jsonb,
  '蕴含灵龟血脉的灵兽卵，表面隐约可见灵光流转。'
),
('潮蟹卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '潮蟹')
  ),
  '["beast_egg", "rare", "beast", "water", "shell"]' ::jsonb,
  '蕴含潮蟹血脉的灵兽卵，表面隐约可见灵光流转。'
),
('水灵蝶卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '水灵蝶')
  ),
  '["beast_egg", "rare", "insect", "water", "heal"]' ::jsonb,
  '蕴含水灵蝶血脉的灵兽卵，表面隐约可见灵光流转。'
),
('玄水蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '玄水蛇')
  ),
  '["beast_egg", "rare", "serpent", "water"]' ::jsonb,
  '蕴含玄水蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('千年老龟卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '千年老龟')
  ),
  '["beast_egg", "epic", "beast", "water", "defense", "shell"]' ::jsonb,
  '蕴含千年老龟血脉的灵兽卵，表面隐约可见灵光流转。'
),
('玄武龟卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '玄武龟')
  ),
  '["beast_egg", "epic", "beast", "water", "earth", "defense", "shell"]' ::jsonb,
  '蕴含玄武龟血脉的灵兽卵，表面隐约可见灵光流转。'
),
('虎蛟卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '虎蛟')
  ),
  '["beast_egg", "rare", "beast", "water", "dragon"]' ::jsonb,
  '蕴含虎蛟血脉的灵兽卵，表面隐约可见灵光流转。'
),
('摸鱼鲲卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '摸鱼鲲')
  ),
  '["beast_egg", "legendary", "beast", "water"]' ::jsonb,
  '蕴含摸鱼鲲血脉的灵兽卵，表面隐约可见灵光流转。'
),
('玄冥卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '玄冥')
  ),
  '["beast_egg", "legendary", "beast", "water", "ice"]' ::jsonb,
  '蕴含玄冥血脉的灵兽卵，表面隐约可见灵光流转。'
),
('灵芝妖卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '灵芝妖')
  ),
  '["beast_egg", "rare", "plant", "wood", "heal"]' ::jsonb,
  '蕴含灵芝妖血脉的灵兽卵，表面隐约可见灵光流转。'
),
('藤蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '藤蛇')
  ),
  '["beast_egg", "uncommon", "serpent", "wood"]' ::jsonb,
  '蕴含藤蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('花精卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '花精')
  ),
  '["beast_egg", "rare", "plant", "wood", "heal"]' ::jsonb,
  '蕴含花精血脉的灵兽卵，表面隐约可见灵光流转。'
),
('木灵蝶卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '木灵蝶')
  ),
  '["beast_egg", "uncommon", "insect", "wood"]' ::jsonb,
  '蕴含木灵蝶血脉的灵兽卵，表面隐约可见灵光流转。'
),
('翠鸟卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '翠鸟')
  ),
  '["beast_egg", "uncommon", "flying", "wood"]' ::jsonb,
  '蕴含翠鸟血脉的灵兽卵，表面隐约可见灵光流转。'
),
('荷蛙卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '荷蛙')
  ),
  '["beast_egg", "rare", "beast", "wood", "water"]' ::jsonb,
  '蕴含荷蛙血脉的灵兽卵，表面隐约可见灵光流转。'
),
('柳灵卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '柳灵')
  ),
  '["beast_egg", "uncommon", "spirit", "wood"]' ::jsonb,
  '蕴含柳灵血脉的灵兽卵，表面隐约可见灵光流转。'
),
('松鼠灵卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '松鼠灵')
  ),
  '["beast_egg", "uncommon", "beast", "wood"]' ::jsonb,
  '蕴含松鼠灵血脉的灵兽卵，表面隐约可见灵光流转。'
),
('竹节虫卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '竹节虫')
  ),
  '["beast_egg", "uncommon", "insect", "wood"]' ::jsonb,
  '蕴含竹节虫血脉的灵兽卵，表面隐约可见灵光流转。'
),
('花妖卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '花妖')
  ),
  '["beast_egg", "rare", "plant", "wood"]' ::jsonb,
  '蕴含花妖血脉的灵兽卵，表面隐约可见灵光流转。'
),
('古藤蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '古藤蛇')
  ),
  '["beast_egg", "rare", "serpent", "wood", "control"]' ::jsonb,
  '蕴含古藤蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('翠玉蜂卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '翠玉蜂')
  ),
  '["beast_egg", "rare", "insect", "wood", "heal"]' ::jsonb,
  '蕴含翠玉蜂血脉的灵兽卵，表面隐约可见灵光流转。'
),
('灵木猿卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '灵木猿')
  ),
  '["beast_egg", "rare", "beast", "wood", "strength"]' ::jsonb,
  '蕴含灵木猿血脉的灵兽卵，表面隐约可见灵光流转。'
),
('青藤蟒卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '青藤蟒')
  ),
  '["beast_egg", "uncommon", "serpent", "wood"]' ::jsonb,
  '蕴含青藤蟒血脉的灵兽卵，表面隐约可见灵光流转。'
),
('碧萝蛛卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '碧萝蛛')
  ),
  '["beast_egg", "rare", "insect", "wood", "poison"]' ::jsonb,
  '蕴含碧萝蛛血脉的灵兽卵，表面隐约可见灵光流转。'
),
('九色鹿卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '九色鹿')
  ),
  '["beast_egg", "epic", "beast", "wood", "heal"]' ::jsonb,
  '蕴含九色鹿血脉的灵兽卵，表面隐约可见灵光流转。'
),
('万年树妖卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '万年树妖')
  ),
  '["beast_egg", "rare", "plant", "wood", "defense"]' ::jsonb,
  '蕴含万年树妖血脉的灵兽卵，表面隐约可见灵光流转。'
),
('建木灵卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '建木灵')
  ),
  '["beast_egg", "legendary", "plant", "wood"]' ::jsonb,
  '蕴含建木灵血脉的灵兽卵，表面隐约可见灵光流转。'
),
('青龙卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '青龙')
  ),
  '["beast_egg", "epic", "dragon", "wood", "water"]' ::jsonb,
  '蕴含青龙血脉的灵兽卵，表面隐约可见灵光流转。'
),
('铁蚁卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '铁蚁')
  ),
  '["beast_egg", "uncommon", "insect", "metal"]' ::jsonb,
  '蕴含铁蚁血脉的灵兽卵，表面隐约可见灵光流转。'
),
('金蝉卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '金蝉')
  ),
  '["beast_egg", "uncommon", "insect", "metal"]' ::jsonb,
  '蕴含金蝉血脉的灵兽卵，表面隐约可见灵光流转。'
),
('铜蝎卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '铜蝎')
  ),
  '["beast_egg", "rare", "insect", "metal", "poison"]' ::jsonb,
  '蕴含铜蝎血脉的灵兽卵，表面隐约可见灵光流转。'
),
('铁蜥卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '铁蜥')
  ),
  '["beast_egg", "uncommon", "beast", "metal"]' ::jsonb,
  '蕴含铁蜥血脉的灵兽卵，表面隐约可见灵光流转。'
),
('金蝶卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '金蝶')
  ),
  '["beast_egg", "uncommon", "insect", "metal"]' ::jsonb,
  '蕴含金蝶血脉的灵兽卵，表面隐约可见灵光流转。'
),
('铁甲虫卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '铁甲虫')
  ),
  '["beast_egg", "rare", "insect", "metal", "defense"]' ::jsonb,
  '蕴含铁甲虫血脉的灵兽卵，表面隐约可见灵光流转。'
),
('铜蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '铜蛇')
  ),
  '["beast_egg", "uncommon", "serpent", "metal"]' ::jsonb,
  '蕴含铜蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('铁翼雀卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '铁翼雀')
  ),
  '["beast_egg", "uncommon", "flying", "metal"]' ::jsonb,
  '蕴含铁翼雀血脉的灵兽卵，表面隐约可见灵光流转。'
),
('金龟卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '金龟')
  ),
  '["beast_egg", "rare", "beast", "metal", "shell"]' ::jsonb,
  '蕴含金龟血脉的灵兽卵，表面隐约可见灵光流转。'
),
('铁背蜈蚣卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '铁背蜈蚣')
  ),
  '["beast_egg", "rare", "insect", "metal", "poison"]' ::jsonb,
  '蕴含铁背蜈蚣血脉的灵兽卵，表面隐约可见灵光流转。'
),
('金翎鹤卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '金翎鹤')
  ),
  '["beast_egg", "rare", "flying", "metal"]' ::jsonb,
  '蕴含金翎鹤血脉的灵兽卵，表面隐约可见灵光流转。'
),
('铜角犀卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '铜角犀')
  ),
  '["beast_egg", "rare", "beast", "metal", "strength"]' ::jsonb,
  '蕴含铜角犀血脉的灵兽卵，表面隐约可见灵光流转。'
),
('铁翼蝠卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '铁翼蝠')
  ),
  '["beast_egg", "rare", "flying", "metal", "stealth"]' ::jsonb,
  '蕴含铁翼蝠血脉的灵兽卵，表面隐约可见灵光流转。'
),
('金丝猴卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '金丝猴')
  ),
  '["beast_egg", "rare", "beast", "metal", "wisdom"]' ::jsonb,
  '蕴含金丝猴血脉的灵兽卵，表面隐约可见灵光流转。'
),
('铁爪鹰卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '铁爪鹰')
  ),
  '["beast_egg", "rare", "flying", "metal", "predator"]' ::jsonb,
  '蕴含铁爪鹰血脉的灵兽卵，表面隐约可见灵光流转。'
),
('白虎卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '白虎')
  ),
  '["beast_egg", "epic", "beast", "metal", "wind"]' ::jsonb,
  '蕴含白虎血脉的灵兽卵，表面隐约可见灵光流转。'
),
('金翼雕卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '金翼雕')
  ),
  '["beast_egg", "rare", "flying", "metal", "predator"]' ::jsonb,
  '蕴含金翼雕血脉的灵兽卵，表面隐约可见灵光流转。'
),
('铁骨熊卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '铁骨熊')
  ),
  '["beast_egg", "epic", "beast", "metal", "strength", "defense"]' ::jsonb,
  '蕴含铁骨熊血脉的灵兽卵，表面隐约可见灵光流转。'
),
('太白金星兽卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '太白金星兽')
  ),
  '["beast_egg", "legendary", "beast", "metal", "celestial"]' ::jsonb,
  '蕴含太白金星兽血脉的灵兽卵，表面隐约可见灵光流转。'
),
('土拨鼠卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '土拨鼠')
  ),
  '["beast_egg", "uncommon", "beast", "earth"]' ::jsonb,
  '蕴含土拨鼠血脉的灵兽卵，表面隐约可见灵光流转。'
),
('石蛙卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '石蛙')
  ),
  '["beast_egg", "uncommon", "beast", "earth"]' ::jsonb,
  '蕴含石蛙血脉的灵兽卵，表面隐约可见灵光流转。'
),
('泥鳅精卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '泥鳅精')
  ),
  '["beast_egg", "rare", "beast", "earth", "water"]' ::jsonb,
  '蕴含泥鳅精血脉的灵兽卵，表面隐约可见灵光流转。'
),
('地蚁卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '地蚁')
  ),
  '["beast_egg", "uncommon", "insect", "earth"]' ::jsonb,
  '蕴含地蚁血脉的灵兽卵，表面隐约可见灵光流转。'
),
('石蝎卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '石蝎')
  ),
  '["beast_egg", "rare", "insect", "earth", "poison"]' ::jsonb,
  '蕴含石蝎血脉的灵兽卵，表面隐约可见灵光流转。'
),
('土蜘蛛卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '土蜘蛛')
  ),
  '["beast_egg", "rare", "insect", "earth", "control"]' ::jsonb,
  '蕴含土蜘蛛血脉的灵兽卵，表面隐约可见灵光流转。'
),
('石蛇精卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '石蛇精')
  ),
  '["beast_egg", "uncommon", "serpent", "earth"]' ::jsonb,
  '蕴含石蛇精血脉的灵兽卵，表面隐约可见灵光流转。'
),
('泥龟卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '泥龟')
  ),
  '["beast_egg", "rare", "beast", "earth", "shell"]' ::jsonb,
  '蕴含泥龟血脉的灵兽卵，表面隐约可见灵光流转。'
),
('石蝶卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '石蝶')
  ),
  '["beast_egg", "uncommon", "insect", "earth"]' ::jsonb,
  '蕴含石蝶血脉的灵兽卵，表面隐约可见灵光流转。'
),
('岩甲犀卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '岩甲犀')
  ),
  '["beast_egg", "epic", "beast", "earth", "defense", "strength"]' ::jsonb,
  '蕴含岩甲犀血脉的灵兽卵，表面隐约可见灵光流转。'
),
('地龙蚓卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '地龙蚓')
  ),
  '["beast_egg", "rare", "beast", "earth", "serpent"]' ::jsonb,
  '蕴含地龙蚓血脉的灵兽卵，表面隐约可见灵光流转。'
),
('石魔像卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '石魔像')
  ),
  '["beast_egg", "rare", "spirit", "earth", "defense"]' ::jsonb,
  '蕴含石魔像血脉的灵兽卵，表面隐约可见灵光流转。'
),
('厚土蟾卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '厚土蟾')
  ),
  '["beast_egg", "rare", "beast", "earth", "defense"]' ::jsonb,
  '蕴含厚土蟾血脉的灵兽卵，表面隐约可见灵光流转。'
),
('山魈卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '山魈')
  ),
  '["beast_egg", "rare", "beast", "earth", "stealth"]' ::jsonb,
  '蕴含山魈血脉的灵兽卵，表面隐约可见灵光流转。'
),
('穿山甲灵卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '穿山甲灵')
  ),
  '["beast_egg", "rare", "beast", "earth", "shell"]' ::jsonb,
  '蕴含穿山甲灵血脉的灵兽卵，表面隐约可见灵光流转。'
),
('麒麟卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '麒麟')
  ),
  '["beast_egg", "epic", "beast", "earth", "fire"]' ::jsonb,
  '蕴含麒麟血脉的灵兽卵，表面隐约可见灵光流转。'
),
('石巨人卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '石巨人')
  ),
  '["beast_egg", "epic", "spirit", "earth", "strength", "defense"]' ::jsonb,
  '蕴含石巨人血脉的灵兽卵，表面隐约可见灵光流转。'
),
('玄岩蟒卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '玄岩蟒')
  ),
  '["beast_egg", "rare", "serpent", "earth", "defense"]' ::jsonb,
  '蕴含玄岩蟒血脉的灵兽卵，表面隐约可见灵光流转。'
),
('黄龙卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '黄龙')
  ),
  '["beast_egg", "legendary", "dragon", "earth"]' ::jsonb,
  '蕴含黄龙血脉的灵兽卵，表面隐约可见灵光流转。'
),
('雪兔卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '雪兔')
  ),
  '["beast_egg", "uncommon", "beast", "ice"]' ::jsonb,
  '蕴含雪兔血脉的灵兽卵，表面隐约可见灵光流转。'
),
('冰蚕卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '冰蚕')
  ),
  '["beast_egg", "rare", "insect", "ice", "silk"]' ::jsonb,
  '蕴含冰蚕血脉的灵兽卵，表面隐约可见灵光流转。'
),
('霜蛾卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '霜蛾')
  ),
  '["beast_egg", "uncommon", "insect", "ice"]' ::jsonb,
  '蕴含霜蛾血脉的灵兽卵，表面隐约可见灵光流转。'
),
('雪鼠卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '雪鼠')
  ),
  '["beast_egg", "uncommon", "beast", "ice"]' ::jsonb,
  '蕴含雪鼠血脉的灵兽卵，表面隐约可见灵光流转。'
),
('冰蝶卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '冰蝶')
  ),
  '["beast_egg", "uncommon", "insect", "ice"]' ::jsonb,
  '蕴含冰蝶血脉的灵兽卵，表面隐约可见灵光流转。'
),
('霜蛙卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '霜蛙')
  ),
  '["beast_egg", "uncommon", "beast", "ice"]' ::jsonb,
  '蕴含霜蛙血脉的灵兽卵，表面隐约可见灵光流转。'
),
('雪雀卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '雪雀')
  ),
  '["beast_egg", "uncommon", "flying", "ice"]' ::jsonb,
  '蕴含雪雀血脉的灵兽卵，表面隐约可见灵光流转。'
),
('冰甲虫卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '冰甲虫')
  ),
  '["beast_egg", "rare", "insect", "ice", "shell"]' ::jsonb,
  '蕴含冰甲虫血脉的灵兽卵，表面隐约可见灵光流转。'
),
('霜蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '霜蛇')
  ),
  '["beast_egg", "uncommon", "serpent", "ice"]' ::jsonb,
  '蕴含霜蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('雪狐卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '雪狐')
  ),
  '["beast_egg", "rare", "beast", "ice", "wisdom"]' ::jsonb,
  '蕴含雪狐血脉的灵兽卵，表面隐约可见灵光流转。'
),
('冰角鹿卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '冰角鹿')
  ),
  '["beast_egg", "rare", "beast", "ice"]' ::jsonb,
  '蕴含冰角鹿血脉的灵兽卵，表面隐约可见灵光流转。'
),
('霜狼卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '霜狼')
  ),
  '["beast_egg", "rare", "beast", "ice"]' ::jsonb,
  '蕴含霜狼血脉的灵兽卵，表面隐约可见灵光流转。'
),
('冰鹤卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '冰鹤')
  ),
  '["beast_egg", "rare", "flying", "ice"]' ::jsonb,
  '蕴含冰鹤血脉的灵兽卵，表面隐约可见灵光流转。'
),
('寒蟾卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '寒蟾')
  ),
  '["beast_egg", "rare", "beast", "ice", "moon"]' ::jsonb,
  '蕴含寒蟾血脉的灵兽卵，表面隐约可见灵光流转。'
),
('雪猿卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '雪猿')
  ),
  '["beast_egg", "rare", "beast", "ice", "strength"]' ::jsonb,
  '蕴含雪猿血脉的灵兽卵，表面隐约可见灵光流转。'
),
('黑水玄蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '黑水玄蛇')
  ),
  '["beast_egg", "epic", "beast", "ice", "water", "serpent"]' ::jsonb,
  '蕴含黑水玄蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('冰凤卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '冰凤')
  ),
  '["beast_egg", "rare", "flying", "ice", "phoenix"]' ::jsonb,
  '蕴含冰凤血脉的灵兽卵，表面隐约可见灵光流转。'
),
('玄冰巨蟒卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '玄冰巨蟒')
  ),
  '["beast_egg", "rare", "serpent", "ice", "defense"]' ::jsonb,
  '蕴含玄冰巨蟒血脉的灵兽卵，表面隐约可见灵光流转。'
),
('玄冰螭龙卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '玄冰螭龙')
  ),
  '["beast_egg", "legendary", "dragon", "ice", "water"]' ::jsonb,
  '蕴含玄冰螭龙血脉的灵兽卵，表面隐约可见灵光流转。'
),
('雷蛙卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '雷蛙')
  ),
  '["beast_egg", "uncommon", "beast", "thunder"]' ::jsonb,
  '蕴含雷蛙血脉的灵兽卵，表面隐约可见灵光流转。'
),
('风蝶卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '风蝶')
  ),
  '["beast_egg", "uncommon", "insect", "wind"]' ::jsonb,
  '蕴含风蝶血脉的灵兽卵，表面隐约可见灵光流转。'
),
('电鳗卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '电鳗')
  ),
  '["beast_egg", "rare", "beast", "thunder", "water"]' ::jsonb,
  '蕴含电鳗血脉的灵兽卵，表面隐约可见灵光流转。'
),
('雷雀卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '雷雀')
  ),
  '["beast_egg", "uncommon", "flying", "thunder"]' ::jsonb,
  '蕴含雷雀血脉的灵兽卵，表面隐约可见灵光流转。'
),
('风鼠卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '风鼠')
  ),
  '["beast_egg", "rare", "beast", "wind", "speed"]' ::jsonb,
  '蕴含风鼠血脉的灵兽卵，表面隐约可见灵光流转。'
),
('电蝎卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '电蝎')
  ),
  '["beast_egg", "rare", "insect", "thunder", "poison"]' ::jsonb,
  '蕴含电蝎血脉的灵兽卵，表面隐约可见灵光流转。'
),
('雷蚕卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '雷蚕')
  ),
  '["beast_egg", "rare", "insect", "thunder", "silk"]' ::jsonb,
  '蕴含雷蚕血脉的灵兽卵，表面隐约可见灵光流转。'
),
('风蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '风蛇')
  ),
  '["beast_egg", "uncommon", "serpent", "wind"]' ::jsonb,
  '蕴含风蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('电萤卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '电萤')
  ),
  '["beast_egg", "uncommon", "insect", "thunder"]' ::jsonb,
  '蕴含电萤血脉的灵兽卵，表面隐约可见灵光流转。'
),
('雷鹰卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '雷鹰')
  ),
  '["beast_egg", "rare", "flying", "thunder", "predator"]' ::jsonb,
  '蕴含雷鹰血脉的灵兽卵，表面隐约可见灵光流转。'
),
('风灵鹤卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '风灵鹤')
  ),
  '["beast_egg", "rare", "flying", "wind"]' ::jsonb,
  '蕴含风灵鹤血脉的灵兽卵，表面隐约可见灵光流转。'
),
('电狼卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '电狼')
  ),
  '["beast_egg", "rare", "beast", "thunder"]' ::jsonb,
  '蕴含电狼血脉的灵兽卵，表面隐约可见灵光流转。'
),
('雷蜥蜴卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '雷蜥蜴')
  ),
  '["beast_egg", "rare", "beast", "thunder", "scale"]' ::jsonb,
  '蕴含雷蜥蜴血脉的灵兽卵，表面隐约可见灵光流转。'
),
('风翼蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '风翼蛇')
  ),
  '["beast_egg", "rare", "serpent", "wind", "flying"]' ::jsonb,
  '蕴含风翼蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('雷猿卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '雷猿')
  ),
  '["beast_egg", "rare", "beast", "thunder", "strength"]' ::jsonb,
  '蕴含雷猿血脉的灵兽卵，表面隐约可见灵光流转。'
),
('夔牛卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '夔牛')
  ),
  '["beast_egg", "legendary", "beast", "thunder"]' ::jsonb,
  '蕴含夔牛血脉的灵兽卵，表面隐约可见灵光流转。'
),
('飞廉卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '飞廉')
  ),
  '["beast_egg", "legendary", "beast", "wind"]' ::jsonb,
  '蕴含飞廉血脉的灵兽卵，表面隐约可见灵光流转。'
),
('雷鹏卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '雷鹏')
  ),
  '["beast_egg", "epic", "flying", "thunder", "wind", "predator"]' ::jsonb,
  '蕴含雷鹏血脉的灵兽卵，表面隐约可见灵光流转。'
),
('应龙卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '应龙')
  ),
  '["beast_egg", "legendary", "dragon", "thunder", "wind", "flying"]' ::jsonb,
  '蕴含应龙血脉的灵兽卵，表面隐约可见灵光流转。'
),
('灵雀卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '灵雀')
  ),
  '["beast_egg", "uncommon", "flying", "beast"]' ::jsonb,
  '蕴含灵雀血脉的灵兽卵，表面隐约可见灵光流转。'
),
('风燕卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '风燕')
  ),
  '["beast_egg", "uncommon", "flying", "wind"]' ::jsonb,
  '蕴含风燕血脉的灵兽卵，表面隐约可见灵光流转。'
),
('云鸽卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '云鸽')
  ),
  '["beast_egg", "uncommon", "flying", "beast"]' ::jsonb,
  '蕴含云鸽血脉的灵兽卵，表面隐约可见灵光流转。'
),
('灵鹦卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '灵鹦')
  ),
  '["beast_egg", "uncommon", "flying", "wisdom"]' ::jsonb,
  '蕴含灵鹦血脉的灵兽卵，表面隐约可见灵光流转。'
),
('雾鹭卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '雾鹭')
  ),
  '["beast_egg", "uncommon", "flying", "water"]' ::jsonb,
  '蕴含雾鹭血脉的灵兽卵，表面隐约可见灵光流转。'
),
('烟鹤卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '烟鹤')
  ),
  '["beast_egg", "uncommon", "flying", "fire"]' ::jsonb,
  '蕴含烟鹤血脉的灵兽卵，表面隐约可见灵光流转。'
),
('霞鸠卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '霞鸠')
  ),
  '["beast_egg", "uncommon", "flying", "beast"]' ::jsonb,
  '蕴含霞鸠血脉的灵兽卵，表面隐约可见灵光流转。'
),
('岚鸦卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '岚鸦')
  ),
  '["beast_egg", "uncommon", "flying", "earth"]' ::jsonb,
  '蕴含岚鸦血脉的灵兽卵，表面隐约可见灵光流转。'
),
('霓莺卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '霓莺')
  ),
  '["beast_egg", "uncommon", "flying", "beast"]' ::jsonb,
  '蕴含霓莺血脉的灵兽卵，表面隐约可见灵光流转。'
),
('铁羽鹰卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '铁羽鹰')
  ),
  '["beast_egg", "rare", "flying", "metal", "predator"]' ::jsonb,
  '蕴含铁羽鹰血脉的灵兽卵，表面隐约可见灵光流转。'
),
('比翼鸟卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '比翼鸟')
  ),
  '["beast_egg", "uncommon", "flying", "support"]' ::jsonb,
  '蕴含比翼鸟血脉的灵兽卵，表面隐约可见灵光流转。'
),
('云鹏卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '云鹏')
  ),
  '["beast_egg", "legendary", "flying", "wind"]' ::jsonb,
  '蕴含云鹏血脉的灵兽卵，表面隐约可见灵光流转。'
),
('灵鹤卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '灵鹤')
  ),
  '["beast_egg", "rare", "flying", "heal"]' ::jsonb,
  '蕴含灵鹤血脉的灵兽卵，表面隐约可见灵光流转。'
),
('风隼卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '风隼')
  ),
  '["beast_egg", "rare", "flying", "wind", "speed"]' ::jsonb,
  '蕴含风隼血脉的灵兽卵，表面隐约可见灵光流转。'
),
('雾鹰卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '雾鹰')
  ),
  '["beast_egg", "rare", "flying", "water", "stealth"]' ::jsonb,
  '蕴含雾鹰血脉的灵兽卵，表面隐约可见灵光流转。'
),
('青鸾卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '青鸾')
  ),
  '["beast_egg", "rare", "flying", "phoenix"]' ::jsonb,
  '蕴含青鸾血脉的灵兽卵，表面隐约可见灵光流转。'
),
('天马卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '天马')
  ),
  '["beast_egg", "legendary", "flying", "speed"]' ::jsonb,
  '蕴含天马血脉的灵兽卵，表面隐约可见灵光流转。'
),
('金翼雕卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '金翼雕')
  ),
  '["beast_egg", "rare", "flying", "metal", "predator"]' ::jsonb,
  '蕴含金翼雕血脉的灵兽卵，表面隐约可见灵光流转。'
),
('大鹏金翅鸟卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '大鹏金翅鸟')
  ),
  '["beast_egg", "legendary", "flying", "metal"]' ::jsonb,
  '蕴含大鹏金翅鸟血脉的灵兽卵，表面隐约可见灵光流转。'
),
('灵蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '灵蛇')
  ),
  '["beast_egg", "uncommon", "serpent", "beast"]' ::jsonb,
  '蕴含灵蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('青蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '青蛇')
  ),
  '["beast_egg", "uncommon", "serpent", "wood"]' ::jsonb,
  '蕴含青蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('赤蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '赤蛇')
  ),
  '["beast_egg", "uncommon", "serpent", "fire"]' ::jsonb,
  '蕴含赤蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('花蟒卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '花蟒')
  ),
  '["beast_egg", "uncommon", "serpent", "beast"]' ::jsonb,
  '蕴含花蟒血脉的灵兽卵，表面隐约可见灵光流转。'
),
('水蛇精卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '水蛇精')
  ),
  '["beast_egg", "uncommon", "serpent", "water"]' ::jsonb,
  '蕴含水蛇精血脉的灵兽卵，表面隐约可见灵光流转。'
),
('草蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '草蛇')
  ),
  '["beast_egg", "uncommon", "serpent", "earth"]' ::jsonb,
  '蕴含草蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('石蛇精卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '石蛇精')
  ),
  '["beast_egg", "uncommon", "serpent", "earth"]' ::jsonb,
  '蕴含石蛇精血脉的灵兽卵，表面隐约可见灵光流转。'
),
('金蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '金蛇')
  ),
  '["beast_egg", "uncommon", "serpent", "metal"]' ::jsonb,
  '蕴含金蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('墨蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '墨蛇')
  ),
  '["beast_egg", "uncommon", "serpent"]' ::jsonb,
  '蕴含墨蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('碧鳞蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '碧鳞蛇')
  ),
  '["beast_egg", "uncommon", "serpent", "poison"]' ::jsonb,
  '蕴含碧鳞蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('蛟蜥卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '蛟蜥')
  ),
  '["beast_egg", "rare", "serpent", "dragon", "water"]' ::jsonb,
  '蕴含蛟蜥血脉的灵兽卵，表面隐约可见灵光流转。'
),
('蟒精卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '蟒精')
  ),
  '["beast_egg", "uncommon", "serpent", "strength"]' ::jsonb,
  '蕴含蟒精血脉的灵兽卵，表面隐约可见灵光流转。'
),
('赤鳞蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '赤鳞蛇')
  ),
  '["beast_egg", "rare", "serpent", "fire", "scale"]' ::jsonb,
  '蕴含赤鳞蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('角蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '角蛇')
  ),
  '["beast_egg", "uncommon", "serpent", "dragon"]' ::jsonb,
  '蕴含角蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('翠蛟卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '翠蛟')
  ),
  '["beast_egg", "rare", "serpent", "dragon", "wood"]' ::jsonb,
  '蕴含翠蛟血脉的灵兽卵，表面隐约可见灵光流转。'
),
('螭龙卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '螭龙')
  ),
  '["beast_egg", "rare", "dragon", "sword", "beast"]' ::jsonb,
  '蕴含螭龙血脉的灵兽卵，表面隐约可见灵光流转。'
),
('内卷蛟卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '内卷蛟')
  ),
  '["beast_egg", "rare", "serpent", "dragon"]' ::jsonb,
  '蕴含内卷蛟血脉的灵兽卵，表面隐约可见灵光流转。'
),
('虎蛟卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '虎蛟')
  ),
  '["beast_egg", "rare", "beast", "water", "dragon"]' ::jsonb,
  '蕴含虎蛟血脉的灵兽卵，表面隐约可见灵光流转。'
),
('烛龙卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '烛龙')
  ),
  '["beast_egg", "legendary", "dragon", "fire", "light"]' ::jsonb,
  '蕴含烛龙血脉的灵兽卵，表面隐约可见灵光流转。'
),
('福鼠卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '福鼠')
  ),
  '["beast_egg", "uncommon", "beast"]' ::jsonb,
  '蕴含福鼠血脉的灵兽卵，表面隐约可见灵光流转。'
),
('瑞兔卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '瑞兔')
  ),
  '["beast_egg", "uncommon", "beast"]' ::jsonb,
  '蕴含瑞兔血脉的灵兽卵，表面隐约可见灵光流转。'
),
('祥鸽卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '祥鸽')
  ),
  '["beast_egg", "uncommon", "flying"]' ::jsonb,
  '蕴含祥鸽血脉的灵兽卵，表面隐约可见灵光流转。'
),
('吉蛙卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '吉蛙')
  ),
  '["beast_egg", "rare", "beast", "earth"]' ::jsonb,
  '蕴含吉蛙血脉的灵兽卵，表面隐约可见灵光流转。'
),
('灵猫卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '灵猫')
  ),
  '["beast_egg", "uncommon", "beast"]' ::jsonb,
  '蕴含灵猫血脉的灵兽卵，表面隐约可见灵光流转。'
),
('喜蛛卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '喜蛛')
  ),
  '["beast_egg", "uncommon", "insect"]' ::jsonb,
  '蕴含喜蛛血脉的灵兽卵，表面隐约可见灵光流转。'
),
('福蝶卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '福蝶')
  ),
  '["beast_egg", "uncommon", "insect"]' ::jsonb,
  '蕴含福蝶血脉的灵兽卵，表面隐约可见灵光流转。'
),
('瑞蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '瑞蛇')
  ),
  '["beast_egg", "uncommon", "serpent"]' ::jsonb,
  '蕴含瑞蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('懒猴卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '懒猴')
  ),
  '["beast_egg", "rare", "beast"]' ::jsonb,
  '蕴含懒猴血脉的灵兽卵，表面隐约可见灵光流转。'
),
('玉兔卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '玉兔')
  ),
  '["beast_egg", "rare", "beast", "moon"]' ::jsonb,
  '蕴含玉兔血脉的灵兽卵，表面隐约可见灵光流转。'
),
('乘黄卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '乘黄')
  ),
  '["beast_egg", "legendary", "beast"]' ::jsonb,
  '蕴含乘黄血脉的灵兽卵，表面隐约可见灵光流转。'
),
('白泽幼卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '白泽幼')
  ),
  '["beast_egg", "rare", "beast", "wisdom"]' ::jsonb,
  '蕴含白泽幼血脉的灵兽卵，表面隐约可见灵光流转。'
),
('瑞鹤卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '瑞鹤')
  ),
  '["beast_egg", "rare", "flying", "heal"]' ::jsonb,
  '蕴含瑞鹤血脉的灵兽卵，表面隐约可见灵光流转。'
),
('吉祥鹿卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '吉祥鹿')
  ),
  '["beast_egg", "rare", "beast", "heal"]' ::jsonb,
  '蕴含吉祥鹿血脉的灵兽卵，表面隐约可见灵光流转。'
),
('灵芝仙卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '灵芝仙')
  ),
  '["beast_egg", "rare", "plant", "heal"]' ::jsonb,
  '蕴含灵芝仙血脉的灵兽卵，表面隐约可见灵光流转。'
),
('白泽卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '白泽')
  ),
  '["beast_egg", "rare", "beast", "wisdom"]' ::jsonb,
  '蕴含白泽血脉的灵兽卵，表面隐约可见灵光流转。'
),
('九色鹿卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '九色鹿')
  ),
  '["beast_egg", "epic", "beast", "heal", "wood"]' ::jsonb,
  '蕴含九色鹿血脉的灵兽卵，表面隐约可见灵光流转。'
),
('躺平貘卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '躺平貘')
  ),
  '["beast_egg", "epic", "beast"]' ::jsonb,
  '蕴含躺平貘血脉的灵兽卵，表面隐约可见灵光流转。'
),
('麒麟卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '麒麟')
  ),
  '["beast_egg", "epic", "beast", "earth", "fire"]' ::jsonb,
  '蕴含麒麟血脉的灵兽卵，表面隐约可见灵光流转。'
),
('瘟鼠卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '瘟鼠')
  ),
  '["beast_egg", "uncommon", "beast"]' ::jsonb,
  '蕴含瘟鼠血脉的灵兽卵，表面隐约可见灵光流转。'
),
('毒蛙卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '毒蛙')
  ),
  '["beast_egg", "rare", "beast", "poison"]' ::jsonb,
  '蕴含毒蛙血脉的灵兽卵，表面隐约可见灵光流转。'
),
('邪蛛卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '邪蛛')
  ),
  '["beast_egg", "rare", "insect", "poison"]' ::jsonb,
  '蕴含邪蛛血脉的灵兽卵，表面隐约可见灵光流转。'
),
('魔蛾卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '魔蛾')
  ),
  '["beast_egg", "rare", "insect"]' ::jsonb,
  '蕴含魔蛾血脉的灵兽卵，表面隐约可见灵光流转。'
),
('煞蛇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '煞蛇')
  ),
  '["beast_egg", "uncommon", "serpent"]' ::jsonb,
  '蕴含煞蛇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('鬼萤卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '鬼萤')
  ),
  '["beast_egg", "rare", "insect", "undead"]' ::jsonb,
  '蕴含鬼萤血脉的灵兽卵，表面隐约可见灵光流转。'
),
('厄蝎卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '厄蝎')
  ),
  '["beast_egg", "rare", "insect", "poison"]' ::jsonb,
  '蕴含厄蝎血脉的灵兽卵，表面隐约可见灵光流转。'
),
('怨蝠卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '怨蝠')
  ),
  '["beast_egg", "rare", "flying", "undead"]' ::jsonb,
  '蕴含怨蝠血脉的灵兽卵，表面隐约可见灵光流转。'
),
('瘴蟾卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '瘴蟾')
  ),
  '["beast_egg", "rare", "beast", "poison"]' ::jsonb,
  '蕴含瘴蟾血脉的灵兽卵，表面隐约可见灵光流转。'
),
('穷奇幼卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '穷奇幼')
  ),
  '["beast_egg", "rare", "beast", "flying"]' ::jsonb,
  '蕴含穷奇幼血脉的灵兽卵，表面隐约可见灵光流转。'
),
('饕餮幼卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '饕餮幼')
  ),
  '["beast_egg", "rare", "beast", "gluttony"]' ::jsonb,
  '蕴含饕餮幼血脉的灵兽卵，表面隐约可见灵光流转。'
),
('混沌幼卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '混沌幼')
  ),
  '["beast_egg", "rare", "beast"]' ::jsonb,
  '蕴含混沌幼血脉的灵兽卵，表面隐约可见灵光流转。'
),
('梼杌幼卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '梼杌幼')
  ),
  '["beast_egg", "rare", "beast"]' ::jsonb,
  '蕴含梼杌幼血脉的灵兽卵，表面隐约可见灵光流转。'
),
('猰貐卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '猰貐')
  ),
  '["beast_egg", "rare", "beast", "strength"]' ::jsonb,
  '蕴含猰貐血脉的灵兽卵，表面隐约可见灵光流转。'
),
('浑敦卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '浑敦')
  ),
  '["beast_egg", "rare", "spirit"]' ::jsonb,
  '蕴含浑敦血脉的灵兽卵，表面隐约可见灵光流转。'
),
('穷奇卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '穷奇')
  ),
  '["beast_egg", "rare", "beast", "flying"]' ::jsonb,
  '蕴含穷奇血脉的灵兽卵，表面隐约可见灵光流转。'
),
('饕餮卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '饕餮')
  ),
  '["beast_egg", "rare", "beast", "gluttony"]' ::jsonb,
  '蕴含饕餮血脉的灵兽卵，表面隐约可见灵光流转。'
),
('梼杌卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '梼杌')
  ),
  '["beast_egg", "rare", "beast"]' ::jsonb,
  '蕴含梼杌血脉的灵兽卵，表面隐约可见灵光流转。'
),
('混沌卵', 'BEAST_EGG',
  jsonb_build_object(
    'beast_template_id',
    (SELECT id FROM xt_beast_template WHERE name = '混沌')
  ),
  '["beast_egg", "epic", "beast", "fire", "earth"]' ::jsonb,
  '蕴含混沌血脉的灵兽卵，表面隐约可见灵光流转。'
);
