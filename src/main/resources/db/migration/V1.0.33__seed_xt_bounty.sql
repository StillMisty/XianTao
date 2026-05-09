-- 悬赏任务种子数据
-- 青石镇 (map_id=1)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(1, '采灵芝', '到翠竹林采集3株灵芝，镇东药铺收。', 15, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 60, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵芝')), jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 10, 'max', 30)), 1, 20),
(1, '打野狼', '翠竹林最近野狼成患，去猎杀至少2头。', 20, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 50, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='兽骨')), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='妖兽皮')), jsonb_build_object('type', 'spirit_stones', 'weight', 20, 'min', 15, 'max', 40)), 1, 15),
(1, '帮铁匠', '镇口铁匠需要5块玄铁矿石，去矿洞帮个忙。', 20, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 20, 'max', 50), jsonb_build_object('type', 'rare_item', 'weight', 50, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='寒铁'))), 2, 18),
(1, '镇长的委托', '镇长家传玉佩掉进了井里，下去找回来（井底有蛇）。', 15, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 70, 'min', 30, 'max', 80), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='朱砂'))), 1, 12),
(1, '巡逻翠竹林', '近日有修士在翠竹林失踪，去巡逻查看情况。', 25, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 50, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='妖兽皮')), jsonb_build_object('type', 'spirit_stones', 'weight', 30, 'min', 25, 'max', 60), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵木'))), 1, 15),
(1, '新手试炼', '镇上的老修士想看看新人的实力，去矿洞里带一块石灵的结晶回来。', 30, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 40, 'max', 100), jsonb_build_object('type', 'equipment', 'weight', 30, 'template_id', (SELECT id FROM xt_equipment_template WHERE name='砍柴刀')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='玄铁矿石'))), 1, 10),
(1, '送信官道', '替镇口的驿卒送一封急信到云来村（虽说是急信但不急的话可以走官道）。', 40, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 60, 'min', 50, 'max', 120), jsonb_build_object('type', 'rare_item', 'weight', 25, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵木')), jsonb_build_object('type', 'rare_item', 'weight', 15, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='千年灵木'))), 3, 20),
(1, '夜巡坟岗', '镇上坟岗最近闹鬼，胆大的修士请去夜巡一番。', 30, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 60, 'max', 150), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='朱砂')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='兽骨'))), 4, 10),
(1, '帮药铺采药', '药铺缺一批基础药材：灵芝、血参各3株。', 25, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 50, 'min', 2, 'max', 3, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵芝')), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='血参')), jsonb_build_object('type', 'spirit_stones', 'weight', 20, 'min', 30, 'max', 80)), 3, 15),
(1, '清理矿洞口', '矿洞口被塌方的碎石堵了，清理出来（顺带碰碰运气能不能挖到赤铜矿）。', 35, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 80, 'max', 200), jsonb_build_object('type', 'rare_item', 'weight', 35, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='赤铜矿')), jsonb_build_object('type', 'rare_item', 'weight', 15, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='玄铁矿石'))), 5, 18);

-- 翠竹林 (map_id=2)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(2, '采药修炼', '在竹林中采集灵草修炼基础心法，顺便给青石镇药铺供货。', 15, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 50, 'min', 2, 'max', 3, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵芝')), jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 15, 'max', 40)), 1, 25),
(2, '猎杀毒蛇', '林中毒蛇太多，已有村民被咬，猎杀5条毒蛇。', 20, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 50, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='妖兽皮')), jsonb_build_object('type', 'spirit_stones', 'weight', 30, 'min', 20, 'max', 50), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='兽骨'))), 2, 20),
(2, '伐灵木', '砍伐10根灵木给镇里的木匠打造法器木胚。', 25, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 50, 'min', 2, 'max', 4, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵木')), jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 25, 'max', 60), jsonb_build_object('type', 'rare_item', 'weight', 10, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='玄铁矿石'))), 2, 18),
(2, '采集月华露', '今天正好是月圆之夜，去竹林中采集月华露——注意避开夜间出没的妖兽。', 30, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 60, 'min', 1, 'max', 3, 'template_id', (SELECT id FROM xt_item_template WHERE name='月华露')), jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 50, 'max', 120)), 3, 12),
(2, '击杀野猪妖', '最近经常有野猪妖冲到青石镇啃庄稼，猎杀3头以儆效尤。', 35, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 60, 'max', 150), jsonb_build_object('type', 'rare_item', 'weight', 35, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='兽骨')), jsonb_build_object('type', 'rare_item', 'weight', 25, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='妖兽皮'))), 5, 15),
(2, '巡逻竹林', '有传言说竹林深处出现了一只石灵，去调查看看。', 30, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 45, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵木')), jsonb_build_object('type', 'spirit_stones', 'weight', 35, 'min', 40, 'max', 100), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='玄铁矿石'))), 3, 15),
(2, '猎杀狼群', '风狼族群扩张太快，威胁到了周围安全，至少猎杀4只。', 35, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 70, 'max', 180), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 2, 'max', 3, 'template_id', (SELECT id FROM xt_item_template WHERE name='兽骨')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='妖兽皮'))), 6, 12),
(2, '寻找竹笋妖', '据说竹林中有一只竹笋成了精，把它抓回来（活的，别吃了）。', 25, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 45, 'min', 80, 'max', 200), jsonb_build_object('type', 'rare_item', 'weight', 35, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵芝')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵木'))), 4, 10),
(2, '帮猎户', '镇东猎户的猎犬走丢了，帮他在竹林里找回来。', 20, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 60, 'min', 30, 'max', 80), jsonb_build_object('type', 'rare_item', 'weight', 40, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='妖兽皮'))), 1, 15),
(2, '采集朱砂', '有人在山崖附近发现了品质不错的朱砂矿露头，去采一些回来。', 30, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 60, 'min', 2, 'max', 4, 'template_id', (SELECT id FROM xt_item_template WHERE name='朱砂')), jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 30, 'max', 70)), 3, 15);

-- 青石矿洞 (map_id=3)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(3, '挖矿', '矿洞里挖10块玄铁矿石出来。', 20, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 50, 'min', 2, 'max', 3, 'template_id', (SELECT id FROM xt_item_template WHERE name='玄铁矿石')), jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 20, 'max', 50), jsonb_build_object('type', 'rare_item', 'weight', 10, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='寒铁'))), 5, 25),
(3, '清理石灵', '矿洞深处出现了石灵阻路，清除3只以上。', 25, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 40, 'max', 100), jsonb_build_object('type', 'rare_item', 'weight', 35, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='玄铁矿石')), jsonb_build_object('type', 'rare_item', 'weight', 25, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='赤铜矿'))), 5, 20),
(3, '寻找寒铁矿脉', '传闻矿洞深处有好品质的寒铁，去探查并带回至少3块。', 30, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 50, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='寒铁')), jsonb_build_object('type', 'spirit_stones', 'weight', 30, 'min', 50, 'max', 120), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='玄铁矿石'))), 7, 18),
(3, '矿洞探秘', '矿洞据说有三层，很少有人下到第三层。去那里看看有什么。', 40, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 80, 'max', 200), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='紫金砂')), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 2, 'max', 3, 'template_id', (SELECT id FROM xt_item_template WHERE name='玄铁矿石'))), 8, 15),
(3, '灭鼠患', '妖鼠在矿洞里繁殖太快了，去清剿一批（至少8只）。', 30, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 50, 'max', 130), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 2, 'max', 3, 'template_id', (SELECT id FROM xt_item_template WHERE name='兽骨')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='妖兽皮'))), 5, 15),
(3, '探索矿洞深处', '矿洞深处据说曾有青云门的洞府遗迹——虽然可能只是传说。', 45, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 100, 'max', 250), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='紫金砂')), jsonb_build_object('type', 'equipment', 'weight', 20, 'template_id', (SELECT id FROM xt_equipment_template WHERE name='桃木剑'))), 10, 12),
(3, '采矿竞赛', '玲珑坊悬赏：谁找到品质最高的寒铁谁得奖金。', 25, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 60, 'min', 60, 'max', 150), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='寒铁')), jsonb_build_object('type', 'rare_item', 'weight', 10, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='赤铜矿'))), 6, 15),
(3, '驱赶骷髅兵', '矿洞里最近出现了一些骷髅兵，似乎是死在矿洞里的矿工尸体被妖气复苏了。', 35, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 60, 'max', 150), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='兽骨')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='魂玉碎片'))), 8, 18),
(3, '寻找紫金砂', '紫金砂是炼制法器的重要辅料，去矿洞深处寻找至少2粒。', 40, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 60, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='紫金砂')), jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 80, 'max', 200)), 10, 12),
(3, '矿工失踪事件', '几个矿工在矿洞深处失踪了，下去找找看（做好遇到了不起怪物的心理准备）。', 50, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 150, 'max', 350), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='魂玉碎片')), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='紫金砂'))), 10, 10);

-- 碧水潭 (map_id=4)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(4, '采冰魄花', '碧水潭底的千年冰晶上长出了冰魄花，想办法采一朵上来。', 25, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 50, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='冰魄花')), jsonb_build_object('type', 'spirit_stones', 'weight', 30, 'min', 30, 'max', 80), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='雪莲'))), 10, 20),
(4, '猎杀水鬼', '碧水潭有水鬼出没，已经有钓鱼的人被拖下水了。', 30, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 50, 'max', 120), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='魂玉碎片')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='月华露'))), 12, 18),
(4, '钓灵鱼', '碧水潭中的灵鱼肉质鲜美，据说吃了对修炼有好处。钓5条。', 30, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 40, 'max', 100), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵芝')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='血参'))), 10, 15),
(4, '采集水灵石', '潭底有沉积的水灵石，想办法采集至少5块。', 35, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 60, 'max', 150), jsonb_build_object('type', 'rare_item', 'weight', 35, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='玄晶')), jsonb_build_object('type', 'rare_item', 'weight', 25, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='冰魄花'))), 12, 15),
(4, '调查妖兽异常', '碧水潭附近的妖兽最近异常躁动，去调查原因。', 35, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 70, 'max', 180), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='妖兽皮')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='兽骨'))), 11, 12),
(4, '采集何首乌', '听说潭边的山壁上长了一株千年何首乌，去看看还在不在。', 40, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 50, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='何首乌')), jsonb_build_object('type', 'spirit_stones', 'weight', 30, 'min', 80, 'max', 200), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵芝'))), 12, 15),
(4, '清理食人花', '潭边的食人花越长越多，从枫叶坡那边蔓延过来了。', 25, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 40, 'max', 100), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='地火芝')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='太阳花'))), 10, 15),
(4, '陪钓叟钓鱼', '碧水潭有位钓叟正在寻找能陪他钓一天鱼的年轻修士（顺便交流修炼经验）。', 60, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 100, 'max', 250), jsonb_build_object('type', 'rare_item', 'weight', 40, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='月华露')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='朱砂'))), 10, 10),
(4, '捕捉妖兽幼崽', '有人出灵石收购活的妖兽幼崽，去潭边碰碰运气。', 45, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 60, 'min', 120, 'max', 300), jsonb_build_object('type', 'rare_item', 'weight', 25, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='妖兽皮')), jsonb_build_object('type', 'rare_item', 'weight', 15, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='兽骨'))), 14, 10),
(4, '潭底秘境', '有个渔夫说他曾在潭底看到过一扇石门，似乎是某个古代洞府的入口。', 60, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 200, 'max', 500), jsonb_build_object('type', 'rare_item', 'weight', 35, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='玄晶')), jsonb_build_object('type', 'equipment', 'weight', 25, 'template_id', (SELECT id FROM xt_equipment_template WHERE name='亮银枪'))), 15, 8);

-- 枫叶坡 (map_id=5)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(5, '采集地火芝', '枫叶坡的火脉附近长有地火芝，采集3朵。', 25, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 50, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='地火芝')), jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 30, 'max', 80)), 15, 20),
(5, '清理枫叶坡妖兽', '枫叶坡的妖兽最近特别多，可能是附近有什么情况。', 30, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 50, 'max', 120), jsonb_build_object('type', 'rare_item', 'weight', 35, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='妖兽皮')), jsonb_build_object('type', 'rare_item', 'weight', 25, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='血参'))), 15, 18),
(5, '探查废弃道观', '枫叶坡上那座废弃道观最近有怪声传出，去看看。', 35, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 45, 'min', 60, 'max', 150), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='朱砂')), jsonb_build_object('type', 'rare_item', 'weight', 25, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='魂玉碎片'))), 16, 15),
(5, '采集朱砂', '道观后面的山壁上有一处天然朱砂矿，采集至少4份。', 30, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 50, 'min', 2, 'max', 4, 'template_id', (SELECT id FROM xt_item_template WHERE name='朱砂')), jsonb_build_object('type', 'spirit_stones', 'weight', 35, 'min', 30, 'max', 70), jsonb_build_object('type', 'rare_item', 'weight', 15, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='月华露'))), 15, 15),
(5, '捉拿山贼', '有一伙山贼盘踞在枫叶坡往云来村的路上，去剿了他们。', 40, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 80, 'max', 200), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='赤铜矿')), jsonb_build_object('type', 'equipment', 'weight', 20, 'template_id', (SELECT id FROM xt_equipment_template WHERE name='开山斧'))), 16, 12),
(5, '寻找失踪商队', '一个商队在枫叶坡附近失踪了，去找找线索。', 40, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 100, 'max', 250), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵芝')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵蚕丝'))), 16, 15),
(5, '采集太阳花', '枫叶坡向阳的山坡上开了不少太阳花，采集5朵。', 25, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 60, 'min', 1, 'max', 3, 'template_id', (SELECT id FROM xt_item_template WHERE name='太阳花')), jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 30, 'max', 70)), 15, 18),
(5, '猎杀妖狐', '一只妖狐经常在枫叶坡出没，已迷惑了好几个路人。解决它。', 45, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 120, 'max', 300), jsonb_build_object('type', 'rare_item', 'weight', 35, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='兽骨')), jsonb_build_object('type', 'equipment', 'weight', 25, 'template_id', (SELECT id FROM xt_equipment_template WHERE name='兽皮甲'))), 18, 12),
(5, '采集灵蚕丝', '枫叶坡深处据说有灵桑树，去看看能不能采集一些灵蚕丝。', 35, jsonb_build_array(jsonb_build_object('type', 'rare_item', 'weight', 50, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵蚕丝')), jsonb_build_object('type', 'spirit_stones', 'weight', 30, 'min', 40, 'max', 100), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵木'))), 15, 15),
(5, '夜探道观', '有人声称在废弃道观中看到了青云门的信物，去确认一下。', 50, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 150, 'max', 350), jsonb_build_object('type', 'rare_item', 'weight', 35, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='魂玉碎片')), jsonb_build_object('type', 'rare_item', 'weight', 25, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='紫金砂'))), 18, 8);

-- 云来村 (map_id=6)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(6, '采茶', '云来村以云雾茶闻名，帮忙采一筐茶送到古道驿站。', 30, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 40, 'max', 100), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵芝')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='血参'))), 18, 20),
(6, '寻找走失修士', '一个从青石镇来的年轻修士在云雾中走丢了——这种人每年都有。', 35, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 60, 'max', 150), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='妖兽皮')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='朱砂'))), 18, 15),
(6, '猎杀妖兽', '云来村周围的山林中有妖兽出没，去清理至少5只。', 30, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 50, 'max', 120), jsonb_build_object('type', 'rare_item', 'weight', 35, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='兽骨')), jsonb_build_object('type', 'rare_item', 'weight', 25, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='妖兽皮'))), 18, 18),
(6, '护送商队', '一支商队要从云来村出发去飞云城，路上不太平。', 50, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 60, 'min', 100, 'max', 250), jsonb_build_object('type', 'rare_item', 'weight', 25, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='朱砂')), jsonb_build_object('type', 'rare_item', 'weight', 15, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='月华露'))), 20, 20),
(6, '采集云雾茶', '云来村的云雾茶产量不足，去悬崖上采集野生云雾茶。', 40, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 80, 'max', 200), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='血参')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='千年灵芝'))), 20, 12),
(6, '清理落日峰的山道', '落日峰上山道有塌方，去清出一条路。', 30, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 50, 'max', 120), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='玄铁矿石')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='紫丹参'))), 20, 15),
(6, '调查失踪村民', '云来村接连失踪了三个村民，有人说是山魈干的。查个水落石出。', 45, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 40, 'min', 120, 'max', 300), jsonb_build_object('type', 'rare_item', 'weight', 35, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='妖兽皮')), jsonb_build_object('type', 'equipment', 'weight', 25, 'template_id', (SELECT id FROM xt_equipment_template WHERE name='玄铁甲'))), 22, 10),
(6, '给茶寮备货', '帮茶寮的老板收集食材：3份灵芝、3份血参、2份何首乌。', 40, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 60, 'max', 150), jsonb_build_object('type', 'rare_item', 'weight', 30, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='灵芝')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='血参'))), 20, 15),
(6, '送信古道驿站', '村中老修士有一封信要送到古道驿站，顺道采购一些朱砂回来。', 60, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 60, 'min', 120, 'max', 300), jsonb_build_object('type', 'rare_item', 'weight', 25, 'min', 1, 'max', 2, 'template_id', (SELECT id FROM xt_item_template WHERE name='朱砂')), jsonb_build_object('type', 'rare_item', 'weight', 15, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='月华露'))), 22, 20),
(6, '村中斗法', '村里来了一位外地的修士在挑衅，请一位本地修士去比试比试（友谊切磋）。', 30, jsonb_build_array(jsonb_build_object('type', 'spirit_stones', 'weight', 50, 'min', 100, 'max', 250), jsonb_build_object('type', 'equipment', 'weight', 30, 'template_id', (SELECT id FROM xt_equipment_template WHERE name='青冥剑')), jsonb_build_object('type', 'rare_item', 'weight', 20, 'min', 1, 'max', 1, 'template_id', (SELECT id FROM xt_item_template WHERE name='朱砂'))), 22, 10);

-- 落日峰 (map_id=7)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(7, '登顶落日峰', '登顶落日峰，在峰顶道观中打坐修炼一炷香，峰顶灵气浓郁对修炼大有裨益。', 30, '[]', 22, 25),
(7, '清理血蝠', '峰腰的洞穴中盘踞了大群血蝠，去清理至少10只。', 25, '[]', 22, 20),
(7, '采集地火芝', '落日峰火脉地带有地火芝，品质比枫叶坡的好，采集3朵。', 25, '[]', 22, 18),
(7, '清理妖道', '一个走上邪路的妖道盘踞在峰顶道观中祭炼邪器，尽快除去。', 40, '[]', 25, 15),
(7, '采集太阳花', '峰顶阳光充沛，太阳花开得格外灿烂，采5朵下来。', 20, '[]', 22, 18),
(7, '寻找遗失法器', '有位修士在登山时遗失了家族法器，帮忙找回来。', 35, '[]', 24, 12),
(7, '猎杀螳螂妖', '峰腰有一种螳螂妖特别凶猛，已有数人受伤，清剿5只。', 30, '[]', 24, 15),
(7, '采集紫丹参', '峰南面的山壁上有野生的紫丹参，采3株。', 25, '[]', 22, 15),
(7, '护送采药人', '一个采药老头想上峰采药但不敢一个人去，陪他一趟。', 40, '[]', 24, 18),
(7, '峰顶秘境', '据说落日峰顶有一个云洞，跳进去可以去到一个隐秘空间——当然先确保自己会飞。', 60, '[]', 28, 8);

-- 迷雾沼泽 (map_id=8)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(8, '采集幽冥花', '沼泽深处的幽冥花只在阴气浓重时开放，采两朵回来。', 30, '[]', 26, 18),
(8, '猎杀毒蟾', '沼泽里的毒蟾数量暴增，已影响到周边生态。清剿8只。', 30, '[]', 26, 20),
(8, '寻找失踪修士', '一位前往沼泽历练的修士许久未归，去把他找回来。', 40, '[]', 28, 15),
(8, '采集还魂草', '沼泽的悬崖峭壁上长有还魂草，采1株就够——这东西太难找了。', 45, '[]', 28, 12),
(8, '清理幽魂', '沼泽中幽魂越来越多，有向周边蔓延的趋势。至少消解10只。', 35, '[]', 28, 18),
(8, '沼泽深处探索', '有人说在沼泽最深处看到了远古神殿的废墟。', 50, '[]', 30, 10),
(8, '采集曼陀罗', '沼泽中生长着剧毒的曼陀罗，但炼丹用得着。采2朵，戴好手套。', 30, '[]', 28, 15),
(8, '消灭沼泽妖兽', '各种沼泽妖兽让采药队伍寸步难行，清理至少6只不同妖兽。', 35, '[]', 26, 18),
(8, '寻找失落的丹方', '传说有位金丹修士在沼泽中遇难，他的丹方可能还飘在沼泽某处。', 50, '[]', 30, 10),
(8, '采集妖兽毒囊', '沼泽毒兽的毒囊是制作高级丹药的材料，收集5个。', 30, '[]', 28, 15);

-- 青云门遗址 (map_id=9)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(9, '探索废墟', '青龙门遗址中有大量残垣断壁，每一处都可能藏着遗宝。', 40, '[]', 28, 20),
(9, '驱散怨灵', '青龙门覆灭后留下的怨念化作了无数怨灵，超度至少5个。', 35, '[]', 28, 18),
(9, '寻找青云秘典', '据说青云门有一部不传之秘典藏在遗址某处。', 50, '[]', 30, 12),
(9, '收集遗物', '收集青云门散落的遗物：法器碎片、玉简残片、兵器残骸各2件。', 30, '[]', 28, 15),
(9, '消灭九尾妖狐', '遗址深处有一只九尾妖狐盘踞，修为不浅。小心应对。', 60, '[]', 32, 10),
(9, '采集魂玉碎片', '遗址中散落着大量魂玉碎片——注意不要被怨念侵蚀心神。', 30, '[]', 28, 20),
(9, '探明月华密道', '据说月光照在遗址某处时会显现一条密道。去验证一下。', 45, '[]', 30, 12),
(9, '清除妖兽', '遗址周围妖兽横行，猎杀至少8只。', 35, '[]', 28, 18),
(9, '寻找青云令牌', '青云门的掌门令牌据说还在遗址中，虽可能无用但有收藏价值。', 50, '[]', 32, 8),
(9, '午夜探险', '午夜子时进入遗址据说能看到青云门覆灭当晚的景象——胆小的别去。', 50, '[]', 32, 8);

-- 古道驿站 (map_id=10)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(10, '修缮驿道', '古道的路面年久失修，帮忙填坑铺石。', 25, '[]', 30, 20),
(10, '护送车队到飞云城', '一支商队要出发去飞云城，聘请修士护驾。', 60, '[]', 30, 25),
(10, '清理驿道旁妖兽', '驿道附近的妖兽经常袭击过往旅人，清理一下。', 35, '[]', 30, 18),
(10, '采购物资', '驿站需要一批丹药补给，跑腿去飞云城采购回来。', 90, '[]', 30, 20),
(10, '寻人启事', '有人在驿站贴了寻人启事：他兄弟在迷雾沼泽失踪了。', 50, '[]', 32, 15),
(10, '采集灵木', '驿站后的山林中有百年灵木，伐几根回来修缮驿站。', 30, '[]', 30, 18),
(10, '驱赶盗贼', '最近有伙盗贼专门在驿道上打劫，给他们点教训。', 40, '[]', 32, 15),
(10, '送急报', '驿站收到一封急报要送到玲珑坊，快马加鞭（御剑更快）。', 120, '[]', 30, 20),
(10, '寻找灵兽坐骑', '有人重金悬赏活的飞行妖兽幼崽当坐骑。', 90, '[]', 34, 10),
(10, '打扫战场', '之前有修士在驿道附近大战妖兽，去清理战场——顺便捡捡漏。', 35, '[]', 30, 18);

-- 飞云城 (map_id=11)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(11, '送货上门', '飞云城某店铺需要一批玄铁矿石，从青石矿洞运来。', 60, '[]', 35, 20),
(11, '城中巡查', '飞云城的守卫人手不足，临时雇修士巡查街坊。', 30, '[]', 35, 18),
(11, '帮法宝阁收材料', '法宝阁急需一批紫金砂和魂玉碎片。', 45, '[]', 35, 15),
(11, '斗法擂台', '飞云城每月一次的斗法擂台开赛了，去打一场（不论输赢都有奖励）。', 30, '[]', 38, 20),
(11, '护送炼丹材料', '丹坊有一批珍贵药材要运到天剑宗遗址附近交接。', 75, '[]', 38, 18),
(11, '抓捕小偷', '一个小偷专偷法宝阁的矿石材料，把他揪出来。', 35, '[]', 36, 12),
(11, '研究万兽山地图', '有位老修士需要一份最新绘制的万兽山地图，跑一趟。', 120, '[]', 38, 15),
(11, '帮灵兽铺找蛋', '灵兽铺的灵兽最近不下蛋了，去万兽山找几个新鲜的妖兽卵回来。', 60, '[]', 40, 12),
(11, '收集情报', '最近北方似乎有什么异动，去万兽山那边打探一下消息。', 80, '[]', 40, 15),
(11, '捐赠灵石建庙', '飞云城要建一座新的土地庙，出钱出力都可以。', 20, '[]', 35, 10);

-- 万兽山 (map_id=12)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(12, '猎杀猿妖', '万兽山中的猿妖霸占了一条山路，猎杀3只清理通道。', 35, '[]', 38, 20),
(12, '采集兽骨', '满山的妖兽尸骨，收集10份质量好的兽骨回来。', 30, '[]', 38, 25),
(12, '猎杀狮鹫', '空中盘旋的狮鹫让巡山弟子头疼不已，射下来2只。', 45, '[]', 42, 15),
(12, '寻找妖兽巢穴', '深入万兽山绘制最新的妖兽巢穴分布图。', 60, '[]', 42, 15),
(12, '采集龙血草', '有传言说万兽山深处有龙气浸润之地，可能长着龙血草。', 50, '[]', 44, 10),
(12, '护送猎人', '一群经验丰富的猎人要进山狩猎，但缺一个修士压阵。', 60, '[]', 38, 18),
(12, '捕捉妖兽幼崽', '飞云城灵兽铺要补货了，抓5只活的妖兽幼崽。', 45, '[]', 40, 12),
(12, '清理妖兽巢穴', '发现了一处妖兽巢穴，清理干净避免繁殖扩散。', 40, '[]', 40, 18);

-- 幽冥谷 (map_id=13)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(13, '采集幽冥花', '幽冥谷中的幽冥花比沼泽的品质高得多，采3朵。', 35, '[]', 42, 20),
(13, '超度亡魂', '幽冥谷中亡魂多到影响阳间秩序了，超度至少10个。', 40, '[]', 42, 18),
(13, '采集太阴菇', '谷中阴气浓重处生有太阴菇，是炼制阴丹的好材料。', 30, '[]', 42, 15),
(13, '寻找失落的魂玉', '据说一位合体期大修士在幽冥谷陨落，他的魂玉还在谷中。', 60, '[]', 45, 10),
(13, '调查结界裂缝', '幽冥谷深处有一处通向阳间的结界裂缝似乎在扩大。', 50, '[]', 45, 12),
(13, '消灭夜叉', '从冥界跑出来的夜叉盘踞在谷中，清除之。', 50, '[]', 46, 15),
(13, '采集曼陀罗', '幽冥谷中的曼陀罗是极品种，炼丹师出价很高。', 35, '[]', 42, 15),
(13, '寻找失踪同门', '一个宗门弟子在幽冥谷历练时失联了，找回来。', 45, '[]', 44, 12);

-- 灵虚洞天 (map_id=14)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(14, '探索洞天一层', '灵虚洞天共三层，第一层是药圃，探索并采集灵药。', 40, '[]', 40, 20),
(14, '寻找道经残卷', '洞天中散落着灵虚真人手书的道经，共九卷。找到至少一卷。', 50, '[]', 42, 15),
(14, '消灭守护灵兽', '洞天中的守护灵兽被邪气污染了，不去掉探索不了深处。', 50, '[]', 44, 12),
(14, '收集天心兰', '洞天药圃中的天心兰已经开花了，采5朵。', 30, '[]', 40, 18),
(14, '探索洞天二层', '据说二层有道藏阁，去看看还有没有功法玉简幸存。', 60, '[]', 45, 10),
(14, '采集千年灵芝', '洞天千年药圃中的灵芝年份比外面的好太多。', 35, '[]', 40, 15),
(14, '研究洞天阵法', '洞天入口的禁制阵法似乎可以解析，记录下来对研究有助。', 45, '[]', 44, 10),
(14, '探索洞天三层', '第三层是灵虚真人闭关修炼之所，可能还有他留下的法宝。', 90, '[]', 48, 5);

-- 天剑宗遗址 (map_id=15)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(15, '感受剑意', '天剑宗遗址中残存的剑意对剑修来说是最好的修炼地。', 30, '[]', 45, 20),
(15, '收集断剑', '遗址中散落着无数断剑，收集10把品相好的。', 35, '[]', 45, 18),
(15, '寻找剑谱', '天剑宗的几套不传剑谱据说埋在废墟下。', 50, '[]', 48, 10),
(15, '降服剑魂', '一把古剑中的剑魂失控伤人，将其降服或毁掉。', 60, '[]', 48, 12),
(15, '调查剑劫之秘', '千年前天剑宗覆灭的真正原因至今成谜，去寻找线索。', 70, '[]', 50, 8),
(15, '收集剑石', '遗址周围有一种吸收了剑气的灵石——剑石，对锻造法器极有助。', 30, '[]', 45, 18),
(15, '清理妖兽', '妖兽把遗址当做了巢穴，赶走它们。', 35, '[]', 45, 15),
(15, '寻找天剑令', '天剑宗掌门的天剑令据说能开启剑冢，但不知何在。', 90, '[]', 50, 5);

-- 焚天岭 (map_id=16)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(16, '采集赤铜矿', '焚天岭以出产优质赤铜矿闻名，挖10块。', 30, '[]', 48, 20),
(16, '寻找天外陨铁', '有传言说焚天岭的山体中有几块天外陨铁嵌着。', 60, '[]', 50, 12),
(16, '猎杀熔岩巨兽', '熔岩巨兽在岭上肆虐，猎杀之可得珍贵的火属性妖丹。', 50, '[]', 50, 15),
(16, '采集地火芝', '焚天岭的地火芝是火属性药材中的上品。', 25, '[]', 48, 20),
(16, '寻找火凤幼雏', '有人声称在岭顶看到过火凤的幼雏，去看看。', 70, '[]', 52, 8),
(16, '采集火灵石', '岭上岩浆冷却后会形成火灵石，收集一些。', 35, '[]', 48, 18),
(16, '压制火山爆发', '焚天岭有再度喷发的迹象，用法力压制一下。', 45, '[]', 50, 15),
(16, '探索岩浆河底', '有人说在岩浆河底看到过一个上古锻造炉。', 90, '[]', 52, 5);

-- 归墟海外围 (map_id=17)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(17, '捕捉海兽', '归墟海中有许多妖兽，捕捉5只——活的比死的有价值。', 40, '[]', 50, 20),
(17, '采集冰魄花', '海底冰晶上盛开的冰魄花，品质远超碧水潭的。', 35, '[]', 50, 18),
(17, '寻找沉船宝藏', '归墟海底有不少沉没的古船，下去寻宝。', 60, '[]', 52, 12),
(17, '调查海妖异常', '归墟海的海妖最近特别反常，似乎在向什么朝拜。', 50, '[]', 52, 15),
(17, '采集深海雪莲', '海底冰脉上的深水雪莲，炼丹界的稀缺货。', 30, '[]', 50, 18),
(17, '猎杀蛟龙', '一条即将化龙的蛟在海底兴风作浪，猎杀之。', 80, '[]', 55, 10),
(17, '收集灵蚕丝', '归墟海有一种海蚕，吐出的丝在深海压力下异常坚韧。', 35, '[]', 50, 15),
(17, '探索海底遗迹', '海底有远古大能留下的海底洞府遗迹。', 90, '[]', 55, 8);

-- 玲珑坊 (map_id=18)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(18, '帮铁匠打铁', '玲珑坊的铁匠缺一个拉风箱的助手——顺便学学炼器。', 30, '[]', 50, 20),
(18, '收集炼器材料', '玲珑阁最近在收各种稀有矿石，带来换灵石。', 45, '[]', 50, 18),
(18, '护送法宝到天机阁', '一件刚炼好的法宝要送到天机阁，路不太平。', 90, '[]', 52, 15),
(18, '拍卖会', '玲珑坊每月一次的拍卖会开始了，去现场见识见识。', 60, '[]', 50, 20),
(18, '鉴定古宝', '有人在废墟中发现了一件古宝，需要懂行的人鉴定。', 45, '[]', 52, 12),
(18, '捕捉炼器辅料', '玲珑坊需要一批新鲜的妖兽材料，去万兽山搜集。', 60, '[]', 50, 15),
(18, '学习锻造术', '玲珑坊的大师公开课——教你如何锻造一把法器。', 90, '[]', 50, 10),
(18, '收集进化石', '玲珑阁在悬赏收购进化石，不管品阶来者不拒。', 120, '[]', 52, 8);

-- 魔王岭 (map_id=19)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(19, '采集龙血草', '魔王岭的魔气滋养了特殊的龙血草品种。', 35, '[]', 53, 18),
(19, '探索魔王洞穴', '魔王岭的魔王洞穴据说通九幽魔窟的一层。', 60, '[]', 55, 12),
(19, '消灭山鬼', '魔王岭上有一只山鬼经常迷惑修士，除掉它。', 50, '[]', 55, 15),
(19, '收集魔晶', '魔王岭的岩壁上有魔气凝结成的魔晶，虽邪但有研究价值。', 40, '[]', 53, 18),
(19, '调查魔气扩散', '魔王岭的魔气似乎在缓慢向外扩散，调查原因。', 60, '[]', 56, 12),
(19, '猎杀妖兽', '变异妖兽在魔王岭上横行，清剿一批。', 45, '[]', 54, 15),
(19, '探明月华井', '魔王岭中有一口月华井，井水能洗净魔气。', 50, '[]', 55, 10),
(19, '寻找魔修遗物', '一个魔修在魔王岭修炼时走火入魔陨落了，他的遗物还在。', 70, '[]', 56, 8);

-- 天机阁外山 (map_id=20)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(20, '破解机关阵', '天机阁外山的入口有一道机关阵，尝试破解进入。', 45, '[]', 55, 18),
(20, '收集赤铜矿', '外山矿脉有优质赤铜矿，采10块。', 30, '[]', 55, 20),
(20, '拜访天机阁', '带上礼物去天机阁拜访——进门本身就考验修为。', 60, '[]', 55, 15),
(20, '寻找星辰石', '天机阁曾外泄了一份星辰石的所在地图。', 50, '[]', 58, 10),
(20, '消灭守山傀儡', '天机阁的守山机关傀儡出bug了，到处攻击人。', 50, '[]', 56, 15),
(20, '采集紫金砂', '外山溪流中有天然的紫金砂沉积。', 35, '[]', 55, 18),
(20, '送信给阁主', '玲珑坊有封信要交给天机阁主——但阁主不见外客。', 90, '[]', 58, 10),
(20, '研究外山阵法', '外山阵法据说是上古先贤所设，研究一番受益匪浅。', 70, '[]', 56, 8);

-- 九幽魔窟·一层 (map_id=21)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(21, '探索魔窟', '九幽魔窟传闻有好几层，从第一层开始探索。', 45, '[]', 55, 18),
(21, '采集魔气结晶', '魔窟壁上的魔气结晶有研究价值。', 30, '[]', 55, 15),
(21, '消灭魔化妖兽', '被魔气污染的妖兽更加凶残，清剿5只。', 40, '[]', 56, 18),
(21, '寻找冥界之花', '魔窟深处据说开有一种冥界之花，见之则死——先找找看。', 60, '[]', 58, 10),
(21, '收集魂玉碎片', '魔窟中充满了未散的魂魄碎片。', 35, '[]', 55, 15),
(21, '调查魔窟入口', '魔窟入口的封印似乎出现了松动，加强一下封印。', 50, '[]', 58, 12),
(21, '猎杀夜叉', '从魔窟深处爬上来的夜叉越来越多。', 50, '[]', 60, 12),
(21, '寻找远古修士遗物', '曾有至少三位大修士入窟探索未归……', 80, '[]', 60, 5);

-- 冰霜冻原 (map_id=22)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(22, '采集冰魄花', '冻原的冰魄花比碧水潭的品质高了几个档次。', 30, '[]', 55, 20),
(22, '猎杀冰蚕', '冰蚕吐的丝是织造顶级法袍的材料。', 40, '[]', 56, 15),
(22, '探索冰窟', '冻原地下的冰窟据说通向一个失落的冰雪王国。', 60, '[]', 58, 12),
(22, '采集雪莲', '冻原雪峰上的雪莲是所有雪莲中最纯净的。', 35, '[]', 55, 18),
(22, '消灭雪女', '一个雪女经常迷惑路过的修士，将他们带入冰窟冻死。', 55, '[]', 58, 12),
(22, '寻找万年玄冰', '万载不化的玄冰核心是锻造寒属性法器的最佳材料。', 70, '[]', 60, 8),
(22, '救助被困队伍', '一支修炼队伍在冻原遭遇暴风雪被困了。', 50, '[]', 56, 15),
(22, '调查暴雪之源', '冻原的暴风雪越来越频繁，似有异宝出世或妖王作乱。', 80, '[]', 60, 10);

-- 神木林 (map_id=23)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(23, '采集千年灵木', '神木林中的灵木动不动就是千年以上的，砍一根就够用好几年。', 35, '[]', 56, 20),
(23, '寻找菩提树', '传说神木林中有一棵菩提古树，找到它打坐一夜比苦修十年。', 60, '[]', 58, 12),
(23, '采集菩提叶', '如果你找到了菩提树——采几片叶子回来。', 45, '[]', 58, 15),
(23, '猎杀千年树妖', '一棵千年树妖挡住了进入神木林深处的路。', 60, '[]', 60, 12),
(23, '收集木灵精华', '林中木灵气浓郁处会自然凝结木灵精华。', 30, '[]', 56, 18),
(23, '寻找灵蚕', '神木林中有一种神品灵蚕，吐的丝价值连城。', 50, '[]', 58, 12),
(23, '探索林心湖', '神木林中心有一个碧绿色的湖泊，据说湖水就是液化的木灵气。', 55, '[]', 58, 15),
(23, '消灭树妖巢穴', '树妖们聚集在神木林某处，形成了一个巨大巢穴。', 70, '[]', 60, 10);

-- 金沙荒漠 (map_id=24)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(24, '采集赤铜矿', '金沙荒漠下的矿脉以赤铜矿品质极高闻名。', 30, '[]', 58, 20),
(24, '寻找紫金砂', '干涸的古河床中偶尔能淘到极品的紫金砂。', 40, '[]', 58, 15),
(24, '探索远古遗迹', '被风沙掩盖的远古建筑偶尔会露出地表。', 60, '[]', 60, 12),
(24, '猎杀沙漠妖兽', '沙漠中的妖兽耐旱耐热，基因相当强大。', 40, '[]', 58, 18),
(24, '寻找绿洲秘境', '传说在无尽沙海中有一个绿洲，里面有上古仙人栽种的仙果。', 90, '[]', 60, 8),
(24, '收集太阳花', '沙漠中的太阳花开得格外旺盛，花瓣蕴含纯阳之力。', 30, '[]', 58, 18),
(24, '护送商队', '一支商队要穿过沙漠前往太虚城，需要修士保护。', 120, '[]', 58, 20),
(24, '调查沙漠扩张', '沙漠正在以异常速度向外扩张，去调查原因。', 80, '[]', 62, 10);

-- 暗影沼泽 (map_id=25)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(25, '采集幽冥花', '暗影沼泽是所有幽冥花产区中最阴气重的。', 30, '[]', 60, 18),
(25, '猎杀夜叉', '从冥界裂缝中跑出的夜叉在沼泽中作乱。', 50, '[]', 62, 15),
(25, '寻找还魂草', '有修士在暗影沼泽见到过一株巨大的还魂草。', 50, '[]', 61, 12),
(25, '采集魔法湖水', '沼泽中部有一个黑色的湖，湖水有强烈的魔气。', 35, '[]', 60, 15),
(25, '超度万千亡魂', '暗影沼泽中的亡魂数量——数不清就尽量多超度。', 60, '[]', 62, 15),
(25, '探索死神之门', '有人说沼泽最深处有一扇刻着骷髅的门……', 90, '[]', 65, 5),
(25, '收集毒囊', '沼泽中的毒兽毒囊质量最好。', 30, '[]', 60, 18),
(25, '寻找失踪队伍', '之前有三个元婴期修士组队进去再也没出来。', 80, '[]', 64, 8);

-- 太虚城 (map_id=26)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(26, '城中巡逻', '太虚城的城防阵法需要定期检查灵力节点。', 30, '[]', 65, 20),
(26, '参加仙术比试', '太虚城每季度举办一次仙术比试大会。', 45, '[]', 68, 20),
(26, '帮法宝阁鉴宝', '法宝阁最近收了一批古宝，需要人帮忙鉴别真伪。', 60, '[]', 65, 15),
(26, '采购丹药', '太虚城的丹坊是全修仙界品种最全的。', 40, '[]', 65, 18),
(26, '拜访宗门分舵', '各大宗门在太虚城都有分舵，去拜访结交人脉。', 50, '[]', 66, 18),
(26, '听道讲坛', '本月的道讲坛请到了一位化神大能，去听听。', 90, '[]', 68, 15),
(26, '寻找失散的道友', '一位道友在不周山和你走散了——虽然你们还没去过不周山。', 80, '[]', 68, 10),
(26, '挑战城主', '太虚城主据说修为深不可测，但要先通过层层考验才能见到他。', 120, '[]', 70, 5);

-- 不周山 (map_id=27)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(27, '采集混沌石', '不周山的山体断裂处不时飞出混沌石碎片。', 45, '[]', 68, 18),
(27, '寻找天梯碎片', '传说中通天的天梯在远古被打碎了，碎片散落在不周山。', 60, '[]', 70, 12),
(27, '消灭石魔', '不周山上的石魔体型巨大，是普通石魔的十倍。', 50, '[]', 69, 15),
(27, '探索山腹秘境', '不周山山腹是中空的，里面据说是远古神族的实验室。', 90, '[]', 72, 8),
(27, '收集玄晶', '不周山的玄晶矿脉是已知最好的。', 40, '[]', 68, 18),
(27, '调查天雷', '不周山常年遭天雷劈打，据说和某件被封印的神器有关。', 70, '[]', 72, 10),
(27, '猎杀夔牛', '一头野生的夔牛把山道踩塌了，赶走或者猎杀。', 60, '[]', 72, 12),
(27, '寻找夸父之杖', '夸父追日倒下后，他的桃木杖化为了不周山的一部分。', 100, '[]', 75, 5);

-- 昆仑墟 (map_id=28)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(28, '探索仙宫废墟', '昆仑墟曾是西王母的行宫废墟，每寸土地都可能藏着仙界遗物。', 60, '[]', 70, 20),
(28, '收集远古法宝碎片', '仙宫废墟中散落着远古大战的法宝碎片。', 45, '[]', 70, 18),
(28, '解开上古禁制', '废墟中有一处被强力禁制封印的房间，尝试破解。', 80, '[]', 73, 10),
(28, '猎杀金乌', '一只受伤的金乌落在了昆仑墟，趁它虚弱的时候……', 70, '[]', 74, 12),
(28, '寻找九转金莲', '昆仑墟的金池中据说有九转金莲，摘一朵。', 50, '[]', 72, 15),
(28, '研究仙文碑刻', '废墟中有大量上古仙文，破译一些会对道法有新的理解。', 90, '[]', 72, 12),
(28, '消灭梼杌', '上古四凶之一的梼杌在昆仑墟徘徊。', 80, '[]', 75, 10),
(28, '寻找蟠桃树', '传说昆仑墟还有一棵蟠桃树存活——虽然多半是假的。', 120, '[]', 75, 5);

-- 封神台 (map_id=29)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(29, '寻找封神榜碎片', '封神台的核心吸引力——封神榜的碎片据说还飘荡在此。', 80, '[]', 72, 15),
(29, '收集神血结晶', '众神之血洒落后化成的血红色结晶。', 40, '[]', 72, 18),
(29, '感应神战痕迹', '在封神台静坐可感应到远古封神之战的余波，对领悟道法大有益处。', 60, '[]', 72, 20),
(29, '消灭神战斗魂', '封神台上残留的远古战魂依然在互相厮杀。', 50, '[]', 74, 15),
(29, '寻找神兵碎片', '封神之战中被打碎的神兵碎片散落在台上。', 70, '[]', 75, 12),
(29, '研究封神阵法', '封神台的核心是一个巨大无比的阵法。解析它。', 100, '[]', 75, 8),
(29, '收集天心兰', '封神台因神血浇灌而变异的天心兰品种。', 35, '[]', 72, 18),
(29, '寻找妲己遗物', '传说妲己被斩后，她的九尾狐内丹化为了封神台上的一颗星石。', 90, '[]', 76, 5);

-- 星河古道 (map_id=30)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(30, '采集星辰石', '星河古道上漂浮的星辰碎片——捡几块回来研究。', 35, '[]', 73, 20),
(30, '猎杀星兽', '生活在星河中的星兽，形态怪异但掉落星石。', 50, '[]', 74, 15),
(30, '探索星门', '星河古道有通向未知世界的星门。', 80, '[]', 76, 8),
(30, '收集九天仙草', '在星河无重力的地方，仙草以奇特形态生长。', 40, '[]', 73, 18),
(30, '寻找陨落星辰', '一颗大星在附近陨落了——通常意味着天外陨铁和玄晶。', 60, '[]', 74, 15),
(30, '护送采集队伍', '一支采集星辰石的队伍需要保护。', 60, '[]', 73, 18),
(30, '调查星河异象', '星河古道的星辰排列突然变了，可能有大能在此推演天机。', 70, '[]', 76, 10),
(30, '寻找紫府秘匙', '紫府秘境的入钥匙据说藏在了星河古道某处。', 90, '[]', 78, 5);

-- 紫府秘境 (map_id=31)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(31, '探索紫府', '紫府秘境中的紫气对修炼大有裨益。', 50, '[]', 75, 20),
(31, '采集紫气精华', '秘境中的紫气浓度高到可以采集了——用玉瓶装几瓶。', 30, '[]', 75, 18),
(31, '寻找紫府真人遗物', '紫府真人飞升后，有些他看不上的东西留在了秘境里。', 60, '[]', 78, 12),
(31, '消灭紫府守卫', '紫府真人留下的护法傀儡已经失控。', 55, '[]', 77, 15),
(31, '研究紫府功法', '紫府中留有一些紫府真人的修炼心得。', 70, '[]', 78, 12),
(31, '收集菩提叶', '秘境中有紫府真人亲手种的菩提树。', 35, '[]', 75, 18),
(31, '探索紫府内殿', '紫府秘境的内殿被一层封印封住，需先破解。', 90, '[]', 80, 8),
(31, '寻找紫府丹方', '紫府真人炼丹一辈子的成果——他的丹方集。', 80, '[]', 78, 10);

-- 九天雷池 (map_id=32)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(32, '吸收雷霆之力', '在雷池边修炼可淬炼肉身，顺便挨几下雷劈——修行就是要皮实。', 40, '[]', 76, 20),
(32, '收集雷晶', '雷霆结晶化成的矿石，蕴含精纯的雷电之力。', 35, '[]', 76, 18),
(32, '猎杀雷鹰', '在雷池上空盘旋的雷鹰，浑身带电。', 55, '[]', 78, 15),
(32, '调查天雷暴动', '最近雷池的暴动频率异常增高，去查原因。', 60, '[]', 80, 12),
(32, '寻找天雷竹', '被天雷劈中后幸存的天雷竹仅在此处有产。', 50, '[]', 78, 15),
(32, '帮助渡劫修士', '一位修士要在雷池附近渡劫——帮他护法（挨雷劈）。', 80, '[]', 80, 10),
(32, '采集混沌石', '雷池最核心处有混沌石被雷霆劈开后暴露。', 70, '[]', 82, 8),
(32, '探索雷池中心', '据说雷池最中心有一片雷液大海——从未有人亲眼见过。', 120, '[]', 82, 5);

-- 蚩尤血渊 (map_id=33)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(33, '采集龙血草', '蚩尤之血浇灌出的龙血草品种最纯。', 35, '[]', 78, 18),
(33, '寻找魔兵碎片', '蚩尤的魔兵碎片据说就在血渊底。', 80, '[]', 80, 12),
(33, '猎杀旱魃', '一只旱魃在血渊中滋养，必须除掉以免赤地千里。', 70, '[]', 82, 10),
(33, '收集血晶', '蚩尤之血凝结而成的血色晶石。', 30, '[]', 78, 18),
(33, '探索血渊底层', '血渊共有九层——是的，又是九这个吉利数字。', 120, '[]', 82, 5),
(33, '镇压魔气', '血渊的魔气似乎在向上扩散。用法力镇压。', 60, '[]', 80, 15),
(33, '收集魂玉碎片', '蚩尤部落战死的将士魂魄碎片散落。', 35, '[]', 78, 18),
(33, '寻找刑天战斧', '刑天被斩后他的战斧据说失落在了血渊。', 100, '[]', 82, 8);

-- 太上道场 (map_id=34)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(34, '悟道修炼', '在太上道场打坐修炼效果是非同一般的。', 60, '[]', 80, 25),
(34, '收集道韵碎片', '道场中的道韵浓郁到可以凝结成碎片。', 40, '[]', 80, 20),
(34, '寻找太上丹方', '太上老君曾在此讲道炼丹，丹方可能有副本留下。', 70, '[]', 82, 12),
(34, '研究道纹', '道场上石头的天然道纹——临摹下来对参悟大道有帮助。', 80, '[]', 82, 15),
(34, '消灭来犯魔族', '有魔族想占领此地道场，打回去。', 60, '[]', 83, 15),
(34, '寻找龙骨', '据说一条真龙在太上道场听道后寿终正寝了。', 90, '[]', 84, 8),
(34, '收集天心兰', '道场的天心兰吸收道韵后形状变成了太极图样。', 35, '[]', 80, 18),
(34, '太上演道', '据说特定时间在道场中心坐定，可得太上投影讲道十五分钟。', 120, '[]', 85, 5);

-- 仙魔战场 (map_id=35)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(35, '收集法宝碎片', '仙魔大战遗落的法宝碎片漫山遍野都是。', 40, '[]', 82, 20),
(35, '猎杀修罗', '阿修罗道的余孽仍在战场上横行。', 60, '[]', 84, 15),
(35, '寻找仙器碎片', '至少有三件仙器被打碎在了这片战场上。', 80, '[]', 85, 10),
(35, '采集魂玉碎片', '战场上死去的仙魔之魂凝结的魂玉极品。', 50, '[]', 82, 18),
(35, '探索空间裂缝', '战场上的空间被撕碎了无数处，有些裂缝通向了未知。', 100, '[]', 86, 8),
(35, '收集混沌石', '仙魔之力对轰的地方诞生了天然的混沌石。', 60, '[]', 84, 12),
(35, '调查远古传送阵', '据说仙魔双方都曾在此布设传送阵。', 70, '[]', 85, 10),
(35, '寻找天都大帝印', '幽冥之主曾在战场上遗落了帝印的一角。', 120, '[]', 88, 5);

-- 万妖谷 (map_id=36)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(36, '猎杀妖兽', '万妖谷的妖兽基数比万兽山多十倍。', 50, '[]', 84, 20),
(36, '采集龙血草', '谷中有真龙曾经盘踞的痕迹，龙气仍在滋养龙血草。', 40, '[]', 84, 18),
(36, '寻找白泽', '白泽据说隐居在万妖谷——去问问它天下大势。', 90, '[]', 86, 8),
(36, '收集兽骨', '谷中遍地妖兽骨架，品相好的随便捡。', 35, '[]', 84, 20),
(36, '探索妖王洞', '万妖谷的最深处是当代妖王的领地。', 100, '[]', 88, 5),
(36, '采集九天仙草', '传说万妖谷顶部有一片与世隔绝的草地——仙草可能就在那。', 60, '[]', 86, 12),
(36, '拯救被抓修士', '妖王抓了一批修士关在谷中做苦力。', 80, '[]', 86, 15),
(36, '寻找麒麟', '真的有麒麟来过万妖谷吗？去找找证据。', 90, '[]', 87, 8);

-- 死神沼泽 (map_id=37)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(37, '采集还魂草', '死神沼泽的还魂草据说是死神亲自加持过的。', 40, '[]', 86, 18),
(37, '猎杀阎罗天子', '冥界之主竟然在沼泽里闲逛——假的，但那个夜叉王是真的。', 80, '[]', 88, 10),
(37, '寻找冥界入口', '死神沼泽深处真的有一个冥界入口。', 100, '[]', 90, 5),
(37, '收集冥晶', '冥气凝结的晶石，对阴属性修炼者来说比灵石还好。', 35, '[]', 86, 18),
(37, '超度战场残魂', '不知什么原因很多仙魔战场的残魂飘到了这里。', 50, '[]', 87, 15),
(37, '采集曼陀罗', '死神沼泽的曼陀罗是黑色的，比普通的毒十倍。', 30, '[]', 86, 18),
(37, '调查死神之影', '有人声称在沼泽中真的看到了死神的影子。', 90, '[]', 88, 8),
(37, '寻找九幽魔窟入口', '据说九幽魔窟最深处的出口就开在了死神沼泽。', 120, '[]', 90, 5);

-- 悟道山 (map_id=38)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(38, '悟道修炼', '在悟道山打坐一天抵得上在别处打坐一个月。', 60, '[]', 88, 25),
(38, '临摹道纹', '山上石头的天然道纹层出不穷，带纸笔来临摹几十幅。', 50, '[]', 88, 20),
(38, '寻找先天庚金', '悟道山上有先天五行本源的残留。', 70, '[]', 90, 12),
(38, '收集道石', '浸透了道韵的石头本身就是修行的好材料。', 40, '[]', 88, 18),
(38, '参加论道大会', '每年一次全修仙界各大门派齐聚悟道山论道。', 90, '[]', 90, 20),
(38, '消灭偷道的妖王', '一只妖王躲在山中要偷悟道山的道韵。', 80, '[]', 92, 10),
(38, '收集菩提叶', '悟道山上的菩提树叶子居然自带道纹。', 35, '[]', 88, 18),
(38, '感应飞升台', '在悟道山的最高处盘坐，可以隐隐感应到飞升台的气息。', 120, '[]', 92, 8);

-- 大道河 (map_id=39)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(39, '在河中修炼', '大道河水中修炼对悟道的加成无与伦比。', 60, '[]', 90, 25),
(39, '收集大道之水', '大道河的水就是液化的大道之力——带几瓶回去感悟。', 40, '[]', 90, 20),
(39, '寻找河源', '溯流而上找到大道河的源头——据说那才是道的起点。', 120, '[]', 93, 8),
(39, '猎杀河中妖兽', '大道河中孕育的水生妖兽也自带道韵。', 55, '[]', 92, 15),
(39, '采集冰魄花', '大道河底的千年冰晶上长有变异冰魄花。', 35, '[]', 90, 18),
(39, '研究河底道纹', '河底岩床上刻满了天地自然生成的道纹。', 80, '[]', 92, 15),
(39, '护送悟道者', '一个破境在即的修士想在大道河里泡一泡。', 70, '[]', 91, 18),
(39, '寻找道种', '在大道河中偶尔会结出一颗道种——服之即可顿悟。', 150, '[]', 95, 5);

-- 浮世幻境 (map_id=40)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(40, '经历幻境试炼', '进入幻境面对自己内心的最后一关。', 50, '[]', 90, 20),
(40, '收集幻境之晶', '幻境中打败心魔后会掉落幻境之晶。', 40, '[]', 90, 18),
(40, '帮助他人突破幻境', '有修士困在了幻境中出不来，去把他拉出来。', 60, '[]', 92, 15),
(40, '探索幻境深处', '据说幻境一层比一层深，最深处连着真实的另一个世界。', 100, '[]', 94, 8),
(40, '猎杀心魔', '幻境中心魔以实体形态出现时方可彻底斩杀。', 70, '[]', 92, 15),
(40, '收集菩提叶', '幻境中有一棵超大的菩提树——但不是真实的。', 35, '[]', 90, 18),
(40, '研究幻境阵法', '幻境的运行机制据说是上古大能布下的一个巨型阵法。', 90, '[]', 93, 10),
(40, '寻找镜花水月', '幻境中有一面镜子，照之可见前世——小心别陷进去。', 120, '[]', 95, 5);

-- 混沌海 (map_id=41)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(41, '采集混沌石', '混沌海是混沌石的原产地，品质最高的。', 45, '[]', 95, 18),
(41, '吸收混沌之气', '在混沌海中修炼可以容纳混沌之气为己用——虽然很危险。', 60, '[]', 96, 15),
(41, '探索混沌裂隙', '混沌海中偶尔会打开虚空中投射来的裂隙。', 90, '[]', 98, 8),
(41, '猎杀混沌', '上古四凶之一的混沌必然在这些海中出没。', 80, '[]', 98, 10),
(41, '收集先天庚金', '混沌初开之时才有先天五行本源。', 60, '[]', 96, 12),
(41, '寻找原初之种', '据说混沌海中存在一颗原初之种——一切可能性的起点。', 150, '[]', 100, 3),
(41, '抵御混沌侵蚀', '混沌之气会侵蚀修士的道心，修炼抵御它的方法。', 70, '[]', 95, 20),
(41, '探访混沌生灵', '混沌中竟然有活物——从宇宙之初就存在的活物。', 120, '[]', 98, 5);

-- 飞升台 (map_id=42)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(42, '挑战飞升台', '飞升台上有一套试炼，需要展现你这一路走来的所有道法。', 90, '[]', 98, 25),
(42, '感应飞升之机', '坐在飞升台最高的石头上，体会那种天人合一的感觉。', 120, '[]', 98, 20),
(42, '收集飞升台道石', '飞升台的道石是天下第一的——因为它是所有修士终极目标的见证。', 50, '[]', 98, 18),
(42, '寻找仙堺裂缝', '飞升台附近据说有一条通向仙界的裂缝。', 100, '[]', 100, 10),
(42, '帮助他人飞升', '有一位修士已经万事俱备了——帮忙护法见证他飞升。', 60, '[]', 99, 15),
(42, '研究飞升阵法', '飞升台本身就是一座巨型的飞升阵法。', 80, '[]', 98, 15),
(42, '寻找遗失的飞升录', '飞升台上曾有前辈刻下飞升的心得——被风雨磨灭了。找拓本。', 70, '[]', 98, 12),
(42, '成就飞升', '你已经做好了准备。飞升吧。', 180, '[]', 100, 2);

-- 归墟海底 (map_id=43)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(43, '探索海底城市', '一个远古的海底文明遗迹。', 80, '[]', 98, 18),
(43, '采集海底奇珍', '海底城市中有无数奇珍异宝。', 60, '[]', 98, 20),
(43, '猎杀深海巨兽', '体长百丈的深海妖兽是海底的霸主。', 100, '[]', 99, 12),
(43, '寻找远古封印', '海底城市最深处有一个远古封印——里面是什么？', 120, '[]', 100, 5),
(43, '收集海底玄晶', '海底压力下形成的玄晶非同一般。', 45, '[]', 98, 18),
(43, '研究海底文明', '这个远古文明可能比仙族还要早。', 90, '[]', 99, 15),
(43, '采集混沌石', '混沌石竟在海底也有分布——因为归墟是万物的终点。', 50, '[]', 98, 15),
(43, '寻找应龙遗蜕', '应龙最终归墟于深海——他的遗蜕可能还在。', 150, '[]', 100, 3);

-- 仙堺裂缝 (map_id=44)
INSERT INTO xt_bounty (map_id, name, description, duration_minutes, rewards, require_level, event_weight) VALUES
(44, '吸取仙气', '仙堺裂缝中泄露的仙气对修士来说是十全大补。', 60, '[]', 100, 20),
(44, '收取仙器碎片', '仙界大战中被打飞的仙器碎片偶尔会掉进裂缝。', 90, '[]', 100, 15),
(44, '探索裂缝通道', '裂缝通向仙界的某一个角落——但凡人越界的代价是什么？', 150, '[]', 100, 5),
(44, '收集仙晶', '仙气过浓处会凝结成仙晶。', 50, '[]', 100, 18),
(44, '阻止魔界偷渡', '有魔物试图通过仙堺裂缝从魔界偷渡到仙界。', 100, '[]', 100, 10),
(44, '寻找飞升捷径', '有人说通过裂缝可以直接飞升——不经过天劫的那种。', 120, '[]', 100, 8),
(44, '研究仙凡结界', '裂缝所在是仙凡结界最薄弱的一处。', 80, '[]', 100, 15),
(44, '终极试炼', '在仙堺裂缝前接受仙界之光和凡界之暗的双重洗练。', 180, '[]', 100, 2);

-- Reset sequence
SELECT setval('xt_bounty_id_seq', (SELECT MAX(id) FROM xt_bounty));
