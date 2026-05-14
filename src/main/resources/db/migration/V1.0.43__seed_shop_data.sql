-- 交易系统：商铺掌柜 + 商品种子数据

-- ============ 商铺掌柜 ============

INSERT INTO shop_npc (id, name, map_node_id, personality, buy_price_modifier, category_multiplier, system_prompt, created_at) VALUES
(1, '药老', 1, 'ISFJ', 0.60,
 '{"POTION": 0.85, "HERB": 0.90, "MATERIAL": 0.50}',
 '你是青石镇的药堂掌柜「药老」，经营丹药和药材生意几十年。
你说话慢声细语，带点苍老感，自称"老朽"，对修士和善但很会做生意。
你的药堂专收药材和丹药，也卖各种灵丹妙药。
当玩家想要回收时，你可以开玩笑说"老朽这药堂专收带灵气的物什，凡铁就算了"。
收购折扣根据物品种类调整，你心里有数，但不能告诉玩家具体数字。
当玩家多次讨价还价时，你可以说"老朽做了几十年生意，从未见过如此执着之人"，然后拒绝第二次砍价。
如果物品不可回收，你说"此物虽有灵性，但老朽实在找不到买主，客官见谅"。',
 '2026-01-01 00:00:00'::timestamp),

(2, '铁锤', 4, 'ESTP', 0.70,
 '{"MATERIAL": 1.10, "POTION": 0.50}',
 '你是铁山堡的铁匠铺掌柜「铁锤」，人如其名，粗犷豪爽，打铁修行。
你说话粗声粗气，自称"老子"，不讲究礼节但很实在。
你的铁匠铺专收矿石、金属材料，也卖武器和锻造材料。
收材料时出价爽快——你说"好料子老子都喜欢"；丹药收得便宜——"这玩意儿又不能打铁"。
当玩家多次讨价还价时，你一拍打铁台："老子说多少就多少，爱卖不卖！"
如果物品不可回收，你说"老子看不上这个，拿走拿走"。',
 '2026-01-01 00:00:00'::timestamp),

(3, '千机子', 11, 'INTJ', 0.55,
 '{"SKILL_JADE": 1.20, "RECIPE_SCROLL": 1.10, "MATERIAL": 0.70}',
 '你是天机阁的掌柜「千机子」，经营各类奇珍异宝、修炼秘籍。
你说话文雅，带书卷气，自称"在下"，语气礼貌但带一丝距离感。
天机阁专收法决玉简、丹方卷轴和各类珍稀材料，也卖稀有功法。
你对便宜的物品不太感兴趣——"这等凡品，天机阁不收也罢"。
当玩家多次讨价还价时，你淡然一笑："阁下若觉价高，不妨去药堂问问，那边更便宜些。"
如果物品不可回收，你说"此物与天机阁无缘，阁下请自留"。',
 '2026-01-01 00:00:00'::timestamp),

(4, '柳三娘', 17, 'ESFJ', 0.55,
 '{"SEED": 0.80, "BEAST_EGG": 0.75, "EVOLUTION_STONE": 0.90, "MATERIAL": 0.60}',
 '你是飘渺城的杂货铺老板娘「柳三娘」，店如其名什么都卖。
你嘴甜热情，自称"奴家"，说话带市井气息，对老客特别关照。
你的铺子什么都收也什么都卖，但对于冷门的东西出价压得低。
当玩家多次讨价还价时，你假装委屈："客官欺负奴家一个妇道人家，这生意没法做了～"——但不会真的降价了。
如果物品不可回收，你说"哎哟客官，这物件……奴家收了怕是卖不出去咧"。',
 '2026-01-01 00:00:00'::timestamp);


-- ============ 药老 · 药堂商品 ============
-- 经验丹药
INSERT INTO shop_product (shop_npc_id, product_type, template_id, base_price, min_price, max_price, min_stock, max_stock, current_price, current_stock, last_sale_time) VALUES
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '小聚灵丹'), 30, 20, 50, 0, 30, 30, 20, '2026-01-01 00:00:00'::timestamp),
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '聚灵丹'),   80, 60, 120, 0, 20, 80, 10, '2026-01-01 00:00:00'::timestamp),
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '大聚灵丹'), 200, 150, 300, 0, 15, 200, 8, '2026-01-01 00:00:00'::timestamp),
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '天元丹'),   500, 350, 800, 0, 10, 500, 5, '2026-01-01 00:00:00'::timestamp);
-- 疗伤丹药
INSERT INTO shop_product (shop_npc_id, product_type, template_id, base_price, min_price, max_price, min_stock, max_stock, current_price, current_stock, last_sale_time) VALUES
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '回春丹'),     50, 35, 75, 0, 30, 50, 18, '2026-01-01 00:00:00'::timestamp),
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '大还丹'),     160, 110, 250, 0, 20, 160, 10, '2026-01-01 00:00:00'::timestamp),
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '九转回春丹'), 600, 400, 900, 0, 8, 600, 4, '2026-01-01 00:00:00'::timestamp);
-- 属性丹药
INSERT INTO shop_product (shop_npc_id, product_type, template_id, base_price, min_price, max_price, min_stock, max_stock, current_price, current_stock, last_sale_time) VALUES
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '壮骨丹'),   120, 80, 180, 0, 15, 120, 8, '2026-01-01 00:00:00'::timestamp),
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '金刚散'),   120, 80, 180, 0, 15, 120, 8, '2026-01-01 00:00:00'::timestamp),
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '轻身散'),   120, 80, 180, 0, 15, 120, 8, '2026-01-01 00:00:00'::timestamp),
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '开智丹'),   120, 80, 180, 0, 15, 120, 8, '2026-01-01 00:00:00'::timestamp);
-- 突破/特殊丹药
INSERT INTO shop_product (shop_npc_id, product_type, template_id, base_price, min_price, max_price, min_stock, max_stock, current_price, current_stock, last_sale_time) VALUES
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '筑基丹'), 500, 350, 800, 0, 8, 500, 4, '2026-01-01 00:00:00'::timestamp),
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '结丹丹'), 1500, 1000, 2200, 0, 5, 1500, 2, '2026-01-01 00:00:00'::timestamp),
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '洗髓丹'), 300, 200, 500, 0, 12, 300, 6, '2026-01-01 00:00:00'::timestamp),
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '清心丹'), 80, 50, 130, 0, 20, 80, 12, '2026-01-01 00:00:00'::timestamp),
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '辟谷丹'), 40, 25, 65, 0, 25, 40, 15, '2026-01-01 00:00:00'::timestamp);
-- 药材
INSERT INTO shop_product (shop_npc_id, product_type, template_id, base_price, min_price, max_price, min_stock, max_stock, current_price, current_stock, last_sale_time) VALUES
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '灵芝'),     20, 10, 35, 0, 30, 20, 20, '2026-01-01 00:00:00'::timestamp),
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '血参'),     25, 12, 40, 0, 25, 25, 15, '2026-01-01 00:00:00'::timestamp),
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '茯苓'),     15, 8, 25, 0, 35, 15, 25, '2026-01-01 00:00:00'::timestamp),
(1, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '石斛'),     30, 15, 50, 0, 20, 30, 12, '2026-01-01 00:00:00'::timestamp);


-- ============ 铁锤 · 铁匠铺商品 ============
-- 法器
INSERT INTO shop_product (shop_npc_id, product_type, template_id, base_price, min_price, max_price, min_stock, max_stock, current_price, current_stock, last_sale_time) VALUES
(2, 'EQUIPMENT', (SELECT id FROM xt_equipment_template WHERE name = '砍柴刀'), 40, 25, 65, 0, 15, 40, 8, '2026-01-01 00:00:00'::timestamp),
(2, 'EQUIPMENT', (SELECT id FROM xt_equipment_template WHERE name = '青锋刀'), 180, 120, 270, 0, 10, 180, 5, '2026-01-01 00:00:00'::timestamp),
(2, 'EQUIPMENT', (SELECT id FROM xt_equipment_template WHERE name = '桃木剑'), 35, 20, 55, 0, 15, 35, 8, '2026-01-01 00:00:00'::timestamp),
(2, 'EQUIPMENT', (SELECT id FROM xt_equipment_template WHERE name = '青冥剑'), 200, 140, 300, 0, 10, 200, 5, '2026-01-01 00:00:00'::timestamp),
(2, 'EQUIPMENT', (SELECT id FROM xt_equipment_template WHERE name = '开山斧'), 200, 140, 300, 0, 8, 200, 4, '2026-01-01 00:00:00'::timestamp),
(2, 'EQUIPMENT', (SELECT id FROM xt_equipment_template WHERE name = '亮银枪'), 180, 120, 270, 0, 10, 180, 5, '2026-01-01 00:00:00'::timestamp),
(2, 'EQUIPMENT', (SELECT id FROM xt_equipment_template WHERE name = '猎弓'),   140, 90, 210, 0, 10, 140, 6, '2026-01-01 00:00:00'::timestamp);
-- 护甲
INSERT INTO shop_product (shop_npc_id, product_type, template_id, base_price, min_price, max_price, min_stock, max_stock, current_price, current_stock, last_sale_time) VALUES
(2, 'EQUIPMENT', (SELECT id FROM xt_equipment_template WHERE name = '兽皮甲'),   80, 50, 130, 0, 12, 80, 8, '2026-01-01 00:00:00'::timestamp),
(2, 'EQUIPMENT', (SELECT id FROM xt_equipment_template WHERE name = '玄铁甲'),   150, 100, 220, 0, 8, 150, 5, '2026-01-01 00:00:00'::timestamp),
(2, 'EQUIPMENT', (SELECT id FROM xt_equipment_template WHERE name = '犀皮铠'),   100, 65, 160, 0, 10, 100, 6, '2026-01-01 00:00:00'::timestamp),
(2, 'EQUIPMENT', (SELECT id FROM xt_equipment_template WHERE name = '寒铁重甲'), 280, 180, 420, 0, 5, 280, 3, '2026-01-01 00:00:00'::timestamp);
-- 材料
INSERT INTO shop_product (shop_npc_id, product_type, template_id, base_price, min_price, max_price, min_stock, max_stock, current_price, current_stock, last_sale_time) VALUES
(2, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '玄铁矿石'), 30, 20, 50, 0, 30, 30, 18, '2026-01-01 00:00:00'::timestamp),
(2, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '寒铁'),     50, 30, 80, 0, 20, 50, 12, '2026-01-01 00:00:00'::timestamp),
(2, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '赤铜矿'),   25, 15, 40, 0, 25, 25, 15, '2026-01-01 00:00:00'::timestamp),
(2, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '紫金砂'),   60, 40, 90, 0, 15, 60, 8, '2026-01-01 00:00:00'::timestamp),
(2, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '灵木'),     35, 20, 55, 0, 20, 35, 12, '2026-01-01 00:00:00'::timestamp);


-- ============ 千机子 · 天机阁商品 ============
-- 法决玉简
INSERT INTO shop_product (shop_npc_id, product_type, template_id, base_price, min_price, max_price, min_stock, max_stock, current_price, current_stock, last_sale_time) VALUES
(3, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '破风斩玉简'),   500, 350, 750, 0, 8, 500, 4, '2026-01-01 00:00:00'::timestamp),
(3, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '清风剑法玉简'), 800, 550, 1200, 0, 8, 800, 4, '2026-01-01 00:00:00'::timestamp),
(3, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '金刚体玉简'),   600, 400, 900, 0, 8, 600, 4, '2026-01-01 00:00:00'::timestamp),
(3, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '轻身术玉简'),   500, 350, 750, 0, 8, 500, 4, '2026-01-01 00:00:00'::timestamp),
(3, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '烈火掌玉简'),   700, 480, 1050, 0, 6, 700, 3, '2026-01-01 00:00:00'::timestamp),
(3, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '寒冰掌玉简'),   700, 480, 1050, 0, 6, 700, 3, '2026-01-01 00:00:00'::timestamp),
(3, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '静心诀玉简'),   550, 380, 830, 0, 8, 550, 4, '2026-01-01 00:00:00'::timestamp);
-- 丹方卷轴
INSERT INTO shop_product (shop_npc_id, product_type, template_id, base_price, min_price, max_price, min_stock, max_stock, current_price, current_stock, last_sale_time) VALUES
(3, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '聚灵丹方'),   300, 200, 450, 0, 6, 300, 3, '2026-01-01 00:00:00'::timestamp),
(3, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '回春丹方'),   250, 160, 380, 0, 6, 250, 3, '2026-01-01 00:00:00'::timestamp),
(3, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '筑基丹方'),   500, 350, 750, 0, 5, 500, 2, '2026-01-01 00:00:00'::timestamp),
(3, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '结丹丹方'),   1200, 800, 1800, 0, 3, 1200, 1, '2026-01-01 00:00:00'::timestamp),
(3, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '壮骨丹方'),   350, 240, 530, 0, 6, 350, 3, '2026-01-01 00:00:00'::timestamp),
(3, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '洗髓丹方'),   400, 280, 600, 0, 5, 400, 2, '2026-01-01 00:00:00'::timestamp);
-- 稀有材料
INSERT INTO shop_product (shop_npc_id, product_type, template_id, base_price, min_price, max_price, min_stock, max_stock, current_price, current_stock, last_sale_time) VALUES
(3, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '天外陨铁'), 100, 65, 150, 0, 10, 100, 5, '2026-01-01 00:00:00'::timestamp),
(3, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '玄晶'),     80, 50, 120, 0, 10, 80, 6, '2026-01-01 00:00:00'::timestamp),
(3, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '魂玉碎片'), 120, 80, 180, 0, 8, 120, 4, '2026-01-01 00:00:00'::timestamp);


-- ============ 柳三娘 · 杂货铺商品 ============
-- 种子
INSERT INTO shop_product (shop_npc_id, product_type, template_id, base_price, min_price, max_price, min_stock, max_stock, current_price, current_stock, last_sale_time) VALUES
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '灵芝孢子'),   20, 10, 35, 0, 50, 20, 30, '2026-01-01 00:00:00'::timestamp),
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '血参种子'),   40, 20, 65, 0, 40, 40, 25, '2026-01-01 00:00:00'::timestamp),
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '雪莲种子'),   50, 25, 80, 0, 30, 50, 18, '2026-01-01 00:00:00'::timestamp),
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '地火芝孢子'), 60, 30, 95, 0, 25, 60, 15, '2026-01-01 00:00:00'::timestamp),
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '天心兰种子'), 45, 22, 70, 0, 35, 45, 22, '2026-01-01 00:00:00'::timestamp),
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '太阳花种子'), 35, 18, 55, 0, 35, 35, 20, '2026-01-01 00:00:00'::timestamp),
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '菩提树种'),   80, 40, 120, 0, 15, 80, 8, '2026-01-01 00:00:00'::timestamp);
-- 兽卵
INSERT INTO shop_product (shop_npc_id, product_type, template_id, base_price, min_price, max_price, min_stock, max_stock, current_price, current_stock, last_sale_time) VALUES
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '灵猫卵'),   120, 80, 180, 0, 12, 120, 6, '2026-01-01 00:00:00'::timestamp),
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '铁羽鹰卵'), 180, 120, 270, 0, 10, 180, 5, '2026-01-01 00:00:00'::timestamp),
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '雪狐卵'),   200, 130, 300, 0, 8, 200, 4, '2026-01-01 00:00:00'::timestamp),
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '火蟾卵'),   150, 100, 220, 0, 10, 150, 5, '2026-01-01 00:00:00'::timestamp),
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '玉兔卵'),   80, 50, 120, 0, 15, 80, 8, '2026-01-01 00:00:00'::timestamp);
-- 进化石
INSERT INTO shop_product (shop_npc_id, product_type, template_id, base_price, min_price, max_price, min_stock, max_stock, current_price, current_stock, last_sale_time) VALUES
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '初阶进化石'), 100, 70, 150, 0, 20, 100, 12, '2026-01-01 00:00:00'::timestamp),
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '中阶进化石'), 300, 200, 450, 0, 12, 300, 6, '2026-01-01 00:00:00'::timestamp),
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '高阶进化石'), 800, 550, 1200, 0, 8, 800, 3, '2026-01-01 00:00:00'::timestamp);
-- 杂货
INSERT INTO shop_product (shop_npc_id, product_type, template_id, base_price, min_price, max_price, min_stock, max_stock, current_price, current_stock, last_sale_time) VALUES
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '妖兽皮'), 20, 10, 35, 0, 30, 20, 18, '2026-01-01 00:00:00'::timestamp),
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '灵蚕丝'), 25, 12, 40, 0, 25, 25, 15, '2026-01-01 00:00:00'::timestamp),
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '兽骨'),   15, 8, 25, 0, 30, 15, 20, '2026-01-01 00:00:00'::timestamp),
(4, 'ITEM', (SELECT id FROM xt_item_template WHERE name = '朱砂'),   40, 25, 60, 0, 20, 40, 10, '2026-01-01 00:00:00'::timestamp);
