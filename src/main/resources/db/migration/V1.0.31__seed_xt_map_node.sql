-- 地图节点种子数据 (xt_map_node)
INSERT INTO xt_map_node (id, name, description, map_type, level_requirement, neighbors, specialties, monster_encounters) VALUES
(1, '青石镇', '青山脚下的小镇，民风淳朴。镇口有座土地庙供往来修士歇脚，是大多数修仙者的起点。',
 'SAFE_TOWN', 1,
 jsonb_build_array(jsonb_build_object('targetId', 2, 'cost', 3)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='灵芝'), 'weight', 50),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='玄铁矿石'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='灵木'), 'weight', 20)
 ),
 '[]'),
(2, '翠竹林', '镇北五里的竹海，四季常青。竹林深处偶有低阶妖兽出没，是十里八乡子弟试炼的首选之地。',
 'TRAINING_ZONE', 1,
 jsonb_build_array(jsonb_build_object('targetId', 1, 'cost', 3), jsonb_build_object('targetId', 3, 'cost', 5)),
  jsonb_build_array(
    jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='灵木'), 'weight', 40),
    jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='兽骨'), 'weight', 30),
    jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='灵芝'), 'weight', 20),
    jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='玄铁矿石'), 'weight', 10),
    jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='灵芝孢子'), 'weight', 10)
  ),
  jsonb_build_array(
    jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='野狼'), 'weight', 30, 'min', 1, 'max', 3),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='毒蛇'), 'weight', 25, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='山魈'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='妖鼠'), 'weight', 15, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='食人花'), 'weight', 10, 'min', 1, 'max', 1)
 )),
(3, '青石矿洞', '翠竹林尽头的一个矿洞，洞口已被挖得七零八落。传闻深处有玄铁矿脉，但也盘踞着石灵一类的妖兽。',
 'TRAINING_ZONE', 5,
 jsonb_build_array(jsonb_build_object('targetId', 2, 'cost', 5), jsonb_build_object('targetId', 4, 'cost', 8), jsonb_build_object('targetId', 9, 'cost', 15)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='玄铁矿石'), 'weight', 45),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='寒铁'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='玄晶'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='兽骨'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='朱砂'), 'weight', 5)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='石灵'), 'weight', 30, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='妖鼠'), 'weight', 25, 'min', 2, 'max', 4),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='山魈'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='野猪妖'), 'weight', 15, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='骷髅兵'), 'weight', 10, 'min', 1, 'max', 1)
 )),
(4, '碧水潭', '青石矿洞后方山中的一汪深潭，水色碧绿如玉。潭边常有妖兽饮水，是水属性修炼者的好去处。',
 'TRAINING_ZONE', 10,
 jsonb_build_array(jsonb_build_object('targetId', 3, 'cost', 8), jsonb_build_object('targetId', 5, 'cost', 6), jsonb_build_object('targetId', 6, 'cost', 12)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='星月花种子'), 'weight', 40),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='血参'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='雪莲'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='还魂草'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='紫丹参'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='毒蛇'), 'weight', 25, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='风狼'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='树精'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='冰狼'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='石甲龟'), 'weight', 15, 'min', 1, 'max', 1)
 )),
(5, '枫叶坡', '满山遍野的枫树每到秋天便如火焰燃烧，煞是壮观。坡上有一座废弃道观，常有妖物盘踞。',
 'TRAINING_ZONE', 15,
 jsonb_build_array(jsonb_build_object('targetId', 4, 'cost', 6), jsonb_build_object('targetId', 6, 'cost', 10), jsonb_build_object('targetId', 7, 'cost', 8)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='九穗禾'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='血参'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='灵木'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='冰魄花种子'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='龙血草'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='火焰蜥'), 'weight', 25, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='树精'), 'weight', 22, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='幽魂'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='螳螂妖'), 'weight', 18, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='山贼'), 'weight', 15, 'min', 1, 'max', 1)
 )),
(6, '云来村', '山谷中的小村，因常年云雾缭绕得名。村中有间茶寮，是往来修士歇脚交换情报的好去处。',
 'SAFE_TOWN', 18,
 jsonb_build_array(jsonb_build_object('targetId', 5, 'cost', 10), jsonb_build_object('targetId', 4, 'cost', 12), jsonb_build_object('targetId', 7, 'cost', 6), jsonb_build_object('targetId', 10, 'cost', 20)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='灵芝'), 'weight', 40),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='血参'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='朱砂'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='月华露'), 'weight', 15)
 ),
 '[]'),
(7, '落日峰', '山峰高耸，黄昏时分夕阳正好从山尖落下，景色绝美。峰腰有一条险陡小径通往峰顶道观。',
 'TRAINING_ZONE', 22,
 jsonb_build_array(jsonb_build_object('targetId', 6, 'cost', 6), jsonb_build_object('targetId', 5, 'cost', 8), jsonb_build_object('targetId', 8, 'cost', 10)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='血参'), 'weight', 35),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='九穗禾'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='何首乌苗'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='寒铁'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='还魂草'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='蝙蝠妖'), 'weight', 22, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='铁甲虫'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='水鬼'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='螳螂妖'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='妖狐'), 'weight', 18, 'min', 1, 'max', 1)
 )),
(8, '迷雾沼泽', '常年被浓雾笼罩的大沼泽，脚下泥泞不堪行走困难。沼泽深处有幽魂和毒虫潜伏，迷失者不在少数。',
 'TRAINING_ZONE', 26,
 jsonb_build_array(jsonb_build_object('targetId', 7, 'cost', 10), jsonb_build_object('targetId', 9, 'cost', 8), jsonb_build_object('targetId', 10, 'cost', 12)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='还魂草种子'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='千年灵芝'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='何首乌苗'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='龙血草'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='九天仙草'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='幽魂'), 'weight', 25, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='铁甲虫'), 'weight', 22, 'min', 2, 'max', 3),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='怨灵'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='蝙蝠妖'), 'weight', 18, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='血蝠'), 'weight', 15, 'min', 1, 'max', 1)
 )),
(9, '青云门遗址', '三百年前显赫一时的青云门，在一场突如其来的劫难中覆灭。如今只剩残垣断壁和游荡的怨灵，但也有遗宝留存的传言。',
 'HIDDEN_ZONE', 28,
 jsonb_build_array(jsonb_build_object('targetId', 3, 'cost', 15), jsonb_build_object('targetId', 8, 'cost', 8)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='还魂草'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='冰魄花'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='魂玉碎片'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='玄晶'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='壮骨丹'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='开智丹'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='石魔'), 'weight', 30, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='妖道'), 'weight', 25, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='怨灵'), 'weight', 25, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='幽魂'), 'weight', 20, 'min', 2, 'max', 4)
 )),
(10, '古道驿站', '官道旁的简陋驿站，供通往南方的修士和客商打尖过夜。旁边有个小坊市，可以买到一些基础物资。',
 'SAFE_TOWN', 30,
 jsonb_build_array(jsonb_build_object('targetId', 6, 'cost', 20), jsonb_build_object('targetId', 8, 'cost', 12), jsonb_build_object('targetId', 11, 'cost', 15)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='龙血草'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='还魂草'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='朱砂'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='月华露'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='断肠草'), 'weight', 10)
 ), '[]');
INSERT INTO xt_map_node (id, name, description, map_type, level_requirement, neighbors, specialties, monster_encounters) VALUES
(11, '飞云城', '南方第一雄城，城墙上法阵流转、灵光隐隐。城内有法宝阁、丹坊、灵兽铺等各类修士设施。',
 'SAFE_TOWN', 35,
 jsonb_build_array(jsonb_build_object('targetId', 10, 'cost', 15), jsonb_build_object('targetId', 12, 'cost', 8), jsonb_build_object('targetId', 14, 'cost', 20), jsonb_build_object('targetId', 18, 'cost', 25)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='天心兰'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='幽冥花'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='曼陀罗'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='何首乌'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='金银花'), 'weight', 15)
 ),
 '[]'),
(12, '万兽山', '妖兽密集的深山老林，据传山中盘踞着数百种不同妖兽，常年兽吼声震天。猎兽修士的天堂和地狱。',
 'TRAINING_ZONE', 38,
 jsonb_build_array(jsonb_build_object('targetId', 11, 'cost', 8), jsonb_build_object('targetId', 13, 'cost', 12), jsonb_build_object('targetId', 15, 'cost', 15)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='兽骨'), 'weight', 35),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='妖兽皮'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='血参'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='菩提树种'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='大还魂丹'), 'weight', 5)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='怨灵'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='冰蚕'), 'weight', 18, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='猿妖'), 'weight', 18, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='血蝠'), 'weight', 17, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='毒蟾'), 'weight', 15, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='蛇妖'), 'weight', 12, 'min', 1, 'max', 1)
 )),
(13, '幽冥谷', '谷中终年不见天日，阴气浓重到在空中凝成雾滴。幽魂在谷中徘徊不去，偶尔能听到凄厉的哭叫声。',
 'TRAINING_ZONE', 42,
 jsonb_build_array(jsonb_build_object('targetId', 12, 'cost', 12), jsonb_build_object('targetId', 17, 'cost', 10), jsonb_build_object('targetId', 16, 'cost', 15)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='还魂草种子'), 'weight', 40),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='千年灵芝'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='魂玉碎片'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='九天仙草'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='麒麟草'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='怨灵'), 'weight', 22, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='摄魂妖'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='妖道'), 'weight', 18, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='飞头蛮'), 'weight', 18, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='熔岩巨兽'), 'weight', 12, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='雪女'), 'weight', 10, 'min', 1, 'max', 1)
 )),
(14, '灵虚洞天', '传说上古大能灵虚真人开辟的洞天福地，入口隐藏在瀑布之后。内有真人亲笔所留的道经和灵药。',
 'HIDDEN_ZONE', 40,
 jsonb_build_array(jsonb_build_object('targetId', 11, 'cost', 20), jsonb_build_object('targetId', 16, 'cost', 12)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='幽冥花种子'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='菩提树种'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='幽冥花'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='踏风丹'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='结丹丹'), 'weight', 15)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='熔岩巨兽'), 'weight', 25, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='摄魂妖'), 'weight', 25, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='夜叉'), 'weight', 25, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='金甲尸'), 'weight', 25, 'min', 1, 'max', 1)
 )),
(15, '天剑宗遗址', '以剑道闻名天下的天剑宗，在千年前一场剑劫中覆灭。残存的剑意仍在空中飘荡，进入者须以自己的剑心抗衡。',
 'TRAINING_ZONE', 45,
 jsonb_build_array(jsonb_build_object('targetId', 12, 'cost', 15), jsonb_build_object('targetId', 17, 'cost', 12), jsonb_build_object('targetId', 16, 'cost', 10)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='紫金砂'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='玄晶'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='幽冥花'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='金刚散'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='轻身散'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='化神丹'), 'weight', 5)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='蛇妖'), 'weight', 22, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='猿妖'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='狮鹫'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='摄魂妖'), 'weight', 18, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='千年树妖'), 'weight', 10, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='雷鹰'), 'weight', 10, 'min', 1, 'max', 1)
 )),
(16, '焚天岭', '活火山脉，山腰以上寸草不生，只有流动的岩浆和硫磺气。山中有火属性天材地宝，但也潜伏着火系妖兽。',
 'TRAINING_ZONE', 48,
 jsonb_build_array(jsonb_build_object('targetId', 13, 'cost', 15), jsonb_build_object('targetId', 15, 'cost', 10), jsonb_build_object('targetId', 17, 'cost', 8), jsonb_build_object('targetId', 14, 'cost', 12)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='九穗禾'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='赤铜矿'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='冰魄花种子'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='太阳花'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='九转回春丹'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='熔岩巨兽'), 'weight', 22, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='修罗'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='夜叉'), 'weight', 18, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='狮鹫'), 'weight', 17, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='飞头蛮'), 'weight', 13, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='摄魂妖'), 'weight', 10, 'min', 1, 'max', 1)
 )),
(17, '归墟海外围', '传说万物终归之处的大海边缘，海面上浮沉着远古时代的残骸。海风带着浓重的灵气和危险。',
 'TRAINING_ZONE', 50,
 jsonb_build_array(jsonb_build_object('targetId', 13, 'cost', 10), jsonb_build_object('targetId', 15, 'cost', 12), jsonb_build_object('targetId', 16, 'cost', 8), jsonb_build_object('targetId', 20, 'cost', 15), jsonb_build_object('targetId', 25, 'cost', 18)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='星月花种子'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='雪莲'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='灵蚕丝'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='冰魄花'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='大还丹'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='熔岩巨兽'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='千年树妖'), 'weight', 18, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='狮鹫'), 'weight', 18, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='雷鹰'), 'weight', 17, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='黑风老妖'), 'weight', 15, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='九尾妖狐'), 'weight', 12, 'min', 1, 'max', 1)
 )),
(18, '玲珑坊', '以炼器闻名的城池，满街都是法宝铺和锻造炉的叮当声。城内最大的玲珑阁，据说收藏着数件仙器。',
 'SAFE_TOWN', 50,
 jsonb_build_array(jsonb_build_object('targetId', 11, 'cost', 25), jsonb_build_object('targetId', 20, 'cost', 10), jsonb_build_object('targetId', 23, 'cost', 18)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='曼陀罗'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='七星草'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='何首乌'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='三生花'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='元阳草'), 'weight', 20)
 ),
 '[]'),
(19, '魔王岭', '据说是上古一位魔王的陨落之地，魔气至今未散。山岭上的草木都呈诡异的黑紫色，妖兽也变得异常凶悍。',
 'TRAINING_ZONE', 53,
 jsonb_build_array(jsonb_build_object('targetId', 20, 'cost', 10), jsonb_build_object('targetId', 25, 'cost', 12), jsonb_build_object('targetId', 21, 'cost', 5)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='还魂草种子'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='龙血草种子'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='太阳花'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='壮骨丹'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='瞬回丹'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='小还魂丹'), 'weight', 5)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='黑风老妖'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='雷鹰'), 'weight', 18, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='火凤雏'), 'weight', 18, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='山鬼'), 'weight', 17, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='狮鹫'), 'weight', 15, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='蜚廉'), 'weight', 12, 'min', 1, 'max', 1)
 )),
(20, '天机阁外山', '天机阁的入口山脉，整座山布满了机关和阵法。天机阁以推演天机和炼制奇宝闻名。',
 'TRAINING_ZONE', 55,
 jsonb_build_array(jsonb_build_object('targetId', 17, 'cost', 15), jsonb_build_object('targetId', 18, 'cost', 10), jsonb_build_object('targetId', 19, 'cost', 10), jsonb_build_object('targetId', 23, 'cost', 12), jsonb_build_object('targetId', 21, 'cost', 8)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='玄晶'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='紫金砂'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='菩提叶'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='金刚散'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='大悟道丹'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='九转回春丹'), 'weight', 5)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='熔岩巨兽'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='修罗'), 'weight', 18, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='黑风老妖'), 'weight', 17, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='雷鹰'), 'weight', 17, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='山鬼'), 'weight', 15, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='九尾妖狐'), 'weight', 13, 'min', 1, 'max', 1)
 )),
(21, '九幽魔窟·一层', '九幽魔窟的入口层，虽然只是最上层但已让人感觉窒息。洞壁流淌着诡异的暗红色浆体，地底传来隐隐的咆哮声。',
 'HIDDEN_ZONE', 55,
 jsonb_build_array(jsonb_build_object('targetId', 19, 'cost', 5), jsonb_build_object('targetId', 20, 'cost', 8)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='魂玉碎片'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='太阳花'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='太阴菇'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='化神丹'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='小还魂丹'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='火凤雏'), 'weight', 25, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='山鬼'), 'weight', 25, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='蜚廉'), 'weight', 25, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='幽冥骑士'), 'weight', 25, 'min', 1, 'max', 1)
 )),
(22, '冰霜冻原', '一望无际的冰原，寒风如刀刮过。冰层之下有无数冰属性妖兽和天材地宝。',
 'TRAINING_ZONE', 55,
 jsonb_build_array(jsonb_build_object('targetId', 23, 'cost', 15), jsonb_build_object('targetId', 24, 'cost', 12), jsonb_build_object('targetId', 28, 'cost', 20)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='星月花种子'), 'weight', 35),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='雪莲'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='万载玄冰'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='大还丹'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='筑基丹'), 'weight', 5)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='雪女'), 'weight', 22, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='九尾妖狐'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='修罗'), 'weight', 18, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='山鬼'), 'weight', 15, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='幽冥骑士'), 'weight', 13, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='蜚廉'), 'weight', 12, 'min', 1, 'max', 1)
 )),
(23, '神木林', '一片古老无比的灵木森林，树冠高耸如天。林中的木灵气浓郁到形成了绿色光雾。',
 'TRAINING_ZONE', 56,
 jsonb_build_array(jsonb_build_object('targetId', 18, 'cost', 18), jsonb_build_object('targetId', 20, 'cost', 12), jsonb_build_object('targetId', 22, 'cost', 15), jsonb_build_object('targetId', 24, 'cost', 10)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='千年灵木'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='幽冥花种子'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='灵芝'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='结丹丹'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='大还丹'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='金刚散'), 'weight', 5)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='千年树妖'), 'weight', 22, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='狮鹫'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='黑风老妖'), 'weight', 18, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='蜚廉'), 'weight', 17, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='幽冥骑士'), 'weight', 13, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='雷鹰'), 'weight', 10, 'min', 1, 'max', 1)
 )),
(24, '金沙荒漠', '一片无边无际的金色沙海，烈日当空毫不留情。沙漠深处有上古遗迹埋藏，偶尔被风吹出地表一角。',
 'TRAINING_ZONE', 58,
 jsonb_build_array(jsonb_build_object('targetId', 22, 'cost', 12), jsonb_build_object('targetId', 23, 'cost', 10), jsonb_build_object('targetId', 25, 'cost', 8), jsonb_build_object('targetId', 30, 'cost', 20)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='紫金砂'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='九穗禾'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='太阳花'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='大还丹'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='冰魄花'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='幽冥骑士'), 'weight', 22, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='蜚廉'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='蛟龙'), 'weight', 18, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='山鬼'), 'weight', 17, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='九尾妖狐'), 'weight', 13, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='天罗蛛'), 'weight', 10, 'min', 1, 'max', 1)
 )),
(25, '暗影沼泽', '比迷雾沼泽更深邃百倍的巨大沼泽，沼泽中暗流涌动，天空永远被黑云遮蔽。',
 'TRAINING_ZONE', 60,
 jsonb_build_array(jsonb_build_object('targetId', 17, 'cost', 18), jsonb_build_object('targetId', 19, 'cost', 12), jsonb_build_object('targetId', 24, 'cost', 8), jsonb_build_object('targetId', 27, 'cost', 20)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='千年灵芝'), 'weight', 35),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='还魂草种子'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='九天仙草'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='太阴菇'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='九转回春丹'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='天罗蛛'), 'weight', 22, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='蛟龙'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='火凤雏'), 'weight', 18, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='山鬼'), 'weight', 17, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='蜚廉'), 'weight', 13, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='梼杌'), 'weight', 10, 'min', 1, 'max', 1)
 ));
INSERT INTO xt_map_node (id, name, description, map_type, level_requirement, neighbors, specialties, monster_encounters) VALUES
(26, '太虚城', '修仙界的中心城之一，城中悬浮着无数仙岛，需乘坐仙鹤或御剑才能往来。各大宗门在此设有分舵。',
 'SAFE_TOWN', 65,
 jsonb_build_array(jsonb_build_object('targetId', 27, 'cost', 10), jsonb_build_object('targetId', 28, 'cost', 12), jsonb_build_object('targetId', 42, 'cost', 30)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='曼陀罗'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='七星草'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='何首乌'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='三生花'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='断肠草'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='茯苓'), 'weight', 10)
 ),
 '[]'),
(27, '不周山', '传说中支撑天地的神山，山体断裂处倾泻出混沌气息。山中危机四伏，但也充满机缘。',
 'TRAINING_ZONE', 68,
 jsonb_build_array(jsonb_build_object('targetId', 26, 'cost', 10), jsonb_build_object('targetId', 28, 'cost', 15), jsonb_build_object('targetId', 31, 'cost', 18), jsonb_build_object('targetId', 29, 'cost', 12)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='玄晶'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='混沌石'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='菩提叶'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='小还魂丹'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='避毒丹'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='易容丹'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='夔牛'), 'weight', 18, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='梼杌'), 'weight', 17, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='金乌'), 'weight', 17, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='旱魃'), 'weight', 16, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='白泽'), 'weight', 16, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='烛龙'), 'weight', 16, 'min', 1, 'max', 1)
 )),
(28, '昆仑墟', '昆仑山深处的远古废墟，相传是仙人居住过的宫殿。废墟中阵法仍在此运转，稍有不慎就会触发毁灭性的禁制。',
 'TRAINING_ZONE', 70,
 jsonb_build_array(jsonb_build_object('targetId', 26, 'cost', 12), jsonb_build_object('targetId', 27, 'cost', 15), jsonb_build_object('targetId', 29, 'cost', 10), jsonb_build_object('targetId', 30, 'cost', 15)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='紫金砂'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='魂玉碎片'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='菩提叶'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='大还魂丹'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='避毒丹'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='抗性丹'), 'weight', 5)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='夔牛'), 'weight', 18, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='金乌'), 'weight', 17, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='烛龙'), 'weight', 17, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='梼杌'), 'weight', 16, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='天魔王'), 'weight', 16, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='麒麟'), 'weight', 16, 'min', 1, 'max', 1)
 )),
(29, '封神台', '远古封神一战遗留的破碎神台，神血洒落之处长出了奇异的血红色花朵。封神榜碎片据说在此飘荡。',
 'HIDDEN_ZONE', 72,
 jsonb_build_array(jsonb_build_object('targetId', 27, 'cost', 12), jsonb_build_object('targetId', 28, 'cost', 10), jsonb_build_object('targetId', 34, 'cost', 15)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='菩提叶'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='茯苓'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='魂玉碎片'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='抗性丹'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='清心丹'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='相柳'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='刑天'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='毕方'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='应龙'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='守鹤'), 'weight', 20, 'min', 1, 'max', 1)
 )),
(30, '星河古道', '一条悬浮于星河之中的古路，两侧是璀璨的星海深渊。古道上遍布星兽和星辰碎片。',
 'TRAINING_ZONE', 73,
 jsonb_build_array(jsonb_build_object('targetId', 24, 'cost', 20), jsonb_build_object('targetId', 28, 'cost', 15), jsonb_build_object('targetId', 31, 'cost', 12), jsonb_build_object('targetId', 33, 'cost', 18)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='玄晶'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='混沌石'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='菩提叶'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='小还魂丹'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='凝神丹'), 'weight', 15)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='旱魃'), 'weight', 18, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='白泽'), 'weight', 17, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='梼杌'), 'weight', 17, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='烛龙'), 'weight', 16, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='相柳'), 'weight', 16, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='毕方'), 'weight', 16, 'min', 1, 'max', 1)
 )),
(31, '紫府秘境', '传说紫府真人飞升前留下的秘境，紫色灵气凝聚为液，汇成小溪在秘境中流淌。',
 'HIDDEN_ZONE', 75,
 jsonb_build_array(jsonb_build_object('targetId', 27, 'cost', 18), jsonb_build_object('targetId', 30, 'cost', 12), jsonb_build_object('targetId', 32, 'cost', 10)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='太阳花'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='赤炼果'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='茯苓'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='清心丹'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='避毒丹'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='应龙'), 'weight', 22, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='刑天'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='相柳'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='守鹤'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'weight', 18, 'min', 1, 'max', 1)
 )),
(32, '九天雷池', '九天之巅的一座巨大雷池，亿万道雷霆在池中翻滚跳跃。修炼雷属性法决的修士在此可一日千里——也可能一命呜呼。',
 'TRAINING_ZONE', 76,
 jsonb_build_array(jsonb_build_object('targetId', 31, 'cost', 10), jsonb_build_object('targetId', 30, 'cost', 15), jsonb_build_object('targetId', 34, 'cost', 12), jsonb_build_object('targetId', 36, 'cost', 18)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='魂玉碎片'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='混沌石'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='菩提叶'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='大还魂丹'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='破甲丹'), 'weight', 15)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='白泽'), 'weight', 18, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='天魔王'), 'weight', 17, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='麒麟'), 'weight', 17, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='应龙'), 'weight', 16, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='刑天'), 'weight', 16, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'weight', 16, 'min', 1, 'max', 1)
 )),
(33, '蚩尤血渊', '蚩尤被斩杀之处，鲜血深渗地层千万丈形成了整座血渊。渊底有蚩尤魔兵残片的传说。',
 'HIDDEN_ZONE', 78,
 jsonb_build_array(jsonb_build_object('targetId', 30, 'cost', 18), jsonb_build_object('targetId', 35, 'cost', 10), jsonb_build_object('targetId', 38, 'cost', 15)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='茯苓'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='赤炼果'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='菩提叶'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='抗性丹'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='易容丹'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='毕方'), 'weight', 22, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='应龙'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='守鹤'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='神龙'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='阎罗天子'), 'weight', 18, 'min', 1, 'max', 1)
 )),
(34, '太上道场', '传说太上老君曾在凡间讲道的道场，如今已是一片废墟但道韵犹存。在此修炼可获道祖悟道加成。',
 'TRAINING_ZONE', 80,
 jsonb_build_array(jsonb_build_object('targetId', 29, 'cost', 15), jsonb_build_object('targetId', 32, 'cost', 12), jsonb_build_object('targetId', 35, 'cost', 10), jsonb_build_object('targetId', 38, 'cost', 15), jsonb_build_object('targetId', 39, 'cost', 8)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='菩提叶'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='赤炼果'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='茯苓'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='清心丹'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='易容丹'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='抗性丹'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='刑天'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='应龙'), 'weight', 18, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='毕方'), 'weight', 18, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='相柳'), 'weight', 17, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'weight', 15, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='神龙'), 'weight', 12, 'min', 1, 'max', 1)
 )),
(35, '仙魔战场', '远古仙魔大战的主战场，空间被打得支离破碎至今未愈合。战场上游荡着仙魔残魂和至宝碎片。',
 'TRAINING_ZONE', 82,
 jsonb_build_array(jsonb_build_object('targetId', 33, 'cost', 10), jsonb_build_object('targetId', 34, 'cost', 10), jsonb_build_object('targetId', 36, 'cost', 12), jsonb_build_object('targetId', 37, 'cost', 15), jsonb_build_object('targetId', 41, 'cost', 20)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='魂玉碎片'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='混沌石'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='赤炼果'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='九转回春丹'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='避毒丹'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='易容丹'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='刑天'), 'weight', 18, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'weight', 18, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='阎罗天子'), 'weight', 17, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='相柳'), 'weight', 17, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='守鹤'), 'weight', 15, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='神龙'), 'weight', 15, 'min', 1, 'max', 1)
 )),
(36, '万妖谷', '无数大妖盘踞的巨型山谷，是妖族的核心领地之一。非强者不敢靠近。',
 'TRAINING_ZONE', 84,
 jsonb_build_array(jsonb_build_object('targetId', 32, 'cost', 18), jsonb_build_object('targetId', 35, 'cost', 12), jsonb_build_object('targetId', 37, 'cost', 10), jsonb_build_object('targetId', 38, 'cost', 12)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='龙血草种子'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='地火芝'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='赤炼果'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='小还魂丹'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='破甲丹'), 'weight', 10),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='避毒丹'), 'weight', 5)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='毕方'), 'weight', 18, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='应龙'), 'weight', 18, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='刑天'), 'weight', 17, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'weight', 17, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='神龙'), 'weight', 15, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='原始天魔'), 'weight', 15, 'min', 1, 'max', 1)
 )),
(37, '死神沼泽', '连死神都要绕道走的沼泽，据说沼泽最深处有通向冥界的裂缝。沼泽里的水是黑色的，碰一下就会吞噬生命力。',
 'TRAINING_ZONE', 86,
 jsonb_build_array(jsonb_build_object('targetId', 35, 'cost', 15), jsonb_build_object('targetId', 36, 'cost', 10), jsonb_build_object('targetId', 41, 'cost', 12), jsonb_build_object('targetId', 40, 'cost', 8)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='千年灵芝'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='还魂草种子'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='九天仙草'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='茯苓'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='清心丹'), 'weight', 15)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='阎罗天子'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'weight', 18, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='神龙'), 'weight', 18, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='守鹤'), 'weight', 17, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='应龙'), 'weight', 15, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='原始天魔'), 'weight', 12, 'min', 1, 'max', 1)
 )),
(38, '悟道山', '一座充满道韵的神山，山上的每一块石头上都有天然而成的道纹。在此打坐修炼事半功倍。',
 'TRAINING_ZONE', 88,
 jsonb_build_array(jsonb_build_object('targetId', 33, 'cost', 15), jsonb_build_object('targetId', 34, 'cost', 15), jsonb_build_object('targetId', 36, 'cost', 12), jsonb_build_object('targetId', 39, 'cost', 10), jsonb_build_object('targetId', 42, 'cost', 20)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='菩提叶'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='赤炼果'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='灵芝'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='清心丹'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='避毒丹'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='应龙'), 'weight', 22, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='毕方'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='阎罗天子'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='相柳'), 'weight', 18, 'min', 1, 'max', 1)
 )),
(39, '大道河', '一条横贯天地的灵河，河水不是水而是液化的大道之力。溯流而上可得道之始，顺流而下可见道之终。',
 'TRAINING_ZONE', 90,
 jsonb_build_array(jsonb_build_object('targetId', 34, 'cost', 8), jsonb_build_object('targetId', 38, 'cost', 10), jsonb_build_object('targetId', 40, 'cost', 12), jsonb_build_object('targetId', 43, 'cost', 15)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='茯苓'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='菩提叶'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='赤炼果'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='易容丹'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='避毒丹'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='刑天'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='守鹤'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='阎罗天子'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='原始天魔'), 'weight', 20, 'min', 1, 'max', 1)
 )),
(40, '浮世幻境', '一个巨大无比的幻境入口，进去之后面对的是自己内心的破绽和恐惧——但也有突破了它们的机会。',
 'HIDDEN_ZONE', 90,
 jsonb_build_array(jsonb_build_object('targetId', 37, 'cost', 8), jsonb_build_object('targetId', 39, 'cost', 12), jsonb_build_object('targetId', 42, 'cost', 10)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='赤炼果'), 'weight', 35),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='茯苓'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='清心丹'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='易容丹'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'weight', 25, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='阎罗天子'), 'weight', 25, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='神龙'), 'weight', 25, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='原始天魔'), 'weight', 25, 'min', 1, 'max', 1)
 )),
(41, '混沌海', '天地未开时混沌原海的残留，进入者需要抵御混沌之气的侵蚀才能存活。但同时混沌中孕育着无限可能。',
 'HIDDEN_ZONE', 95,
 jsonb_build_array(jsonb_build_object('targetId', 35, 'cost', 20), jsonb_build_object('targetId', 37, 'cost', 12), jsonb_build_object('targetId', 44, 'cost', 8), jsonb_build_object('targetId', 43, 'cost', 12)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='混沌石'), 'weight', 40),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='大还魂丹'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='茯苓'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='易容丹'), 'weight', 15)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='阎罗天子'), 'weight', 50, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='原始天魔'), 'weight', 50, 'min', 1, 'max', 1)
 )),
(42, '飞升台', '修仙界最高的山峰之巅，据说登顶者可感天应道，飞升在望。这里是每个修士的终极目的地。',
 'SAFE_TOWN', 98,
 jsonb_build_array(jsonb_build_object('targetId', 26, 'cost', 30), jsonb_build_object('targetId', 38, 'cost', 20), jsonb_build_object('targetId', 40, 'cost', 10), jsonb_build_object('targetId', 43, 'cost', 8), jsonb_build_object('targetId', 44, 'cost', 5)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='茯苓'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='玄黄根'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='大还魂丹'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='易容丹'), 'weight', 20)
 ),
 '[]'),
(43, '归墟海底', '归墟深处沉没的海底世界，古老的海底城市中封印着被遗忘的远古生物。',
 'TRAINING_ZONE', 98,
 jsonb_build_array(jsonb_build_object('targetId', 39, 'cost', 15), jsonb_build_object('targetId', 41, 'cost', 12), jsonb_build_object('targetId', 42, 'cost', 8), jsonb_build_object('targetId', 44, 'cost', 10)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='地火芝'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='雪莲'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='茯苓'), 'weight', 20),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='大还魂丹'), 'weight', 15),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='易容丹'), 'weight', 10)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='守鹤'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='阎罗天子'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='神龙'), 'weight', 20, 'min', 1, 'max', 2),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='原始天魔'), 'weight', 20, 'min', 1, 'max', 1),
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'weight', 20, 'min', 1, 'max', 1)
 )),
(44, '仙堺裂缝', '仙界和人界之间的裂缝，凡人误入其中可能瞬间被仙气粉碎——但也可能直接飞升成仙。最高等级的隐藏地图。',
 'HIDDEN_ZONE', 100,
 jsonb_build_array(jsonb_build_object('targetId', 41, 'cost', 8), jsonb_build_object('targetId', 42, 'cost', 5), jsonb_build_object('targetId', 43, 'cost', 10)),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='混沌石'), 'weight', 30),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='大还魂丹'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='易容丹'), 'weight', 25),
   jsonb_build_object('templateId', (SELECT id FROM xt_item_template WHERE name='赤炼果'), 'weight', 20)
 ),
 jsonb_build_array(
   jsonb_build_object('templateId', (SELECT id FROM xt_monster_template WHERE name='原始天魔'), 'weight', 100, 'min', 1, 'max', 1)
 ));

-- Reset sequence
SELECT setval('xt_map_node_id_seq', (SELECT MAX(id) FROM xt_map_node));
