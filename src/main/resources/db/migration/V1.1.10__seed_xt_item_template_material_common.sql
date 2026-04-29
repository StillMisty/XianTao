-- 材料 —— 新增普通材料 (id 184-189)
INSERT INTO xt_item_template (id, name, type, rarity, properties, tags, description, max_stack) VALUES
(184, '青石', 'MATERIAL', 'COMMON', '{}'::jsonb, '["ore","common"]'::jsonb, '常见的青灰色石材，可用于建造', 99),
(185, '木精', 'MATERIAL', 'COMMON', '{}'::jsonb, '["wood","common"]'::jsonb, '灵木之精华，蕴含微弱生机', 99),
(186, '露水', 'MATERIAL', 'COMMON', '{}'::jsonb, '["water","common"]'::jsonb, '清晨灵草叶尖采集的甘露', 99),
(187, '野蜂蜜', 'MATERIAL', 'COMMON', '{}'::jsonb, '["food","common"]'::jsonb, '灵蜂采集百花酿制的蜂蜜', 99),
(188, '蚕丝', 'MATERIAL', 'COMMON', '{}'::jsonb, '["cloth","common"]'::jsonb, '灵蚕吐出的丝线，轻盈坚韧', 99),
(189, '灵泉水', 'MATERIAL', 'COMMON', '{}'::jsonb, '["water","spirit","common"]'::jsonb, '灵脉中涌出的清泉，蕴含灵气', 99);

-- 重置序列确保后续自增从 190 开始
SELECT setval('xt_item_template_id_seq', 189);
