-- 种子数据 (xt_item_template, type=SEED)
-- production_items 中的 template_id 指向 V1.0.28.3 中按顺序插入的对应药材：
-- 灵芝(41) 血参(42) 雪莲(43) 地火芝(44) 紫丹参(46) 天心兰(48) 太阳花(50)
-- 菩提叶(51) 还魂草(52) 冰魄花(53) 龙血草(47) 幽冥花(49) 何首乌(56)
INSERT INTO xt_item_template (name, type, properties, tags, description) VALUES
('灵芝孢子',   'SEED', '{"grow_time":24,"reharvest":0,"production_items":[{"weight":1,"template_id":41}]}',  '["seed","herb","common"]', '灵芝的孢子粉，撒入灵田可长出灵芝。农桑之道，贵在耐心。'),
('血参种子',   'SEED', '{"grow_time":36,"reharvest":0,"production_items":[{"weight":1,"template_id":42}]}',  '["seed","herb","uncommon"]', '血参的种子，三年结籽一次，种出的血参年份可期。'),
('雪莲种子',   'SEED', '{"grow_time":48,"reharvest":0,"production_items":[{"weight":1,"template_id":43}]}',  '["seed","herb","ice","uncommon"]', '天山雪莲的种子，需寒性灵田方可种植，成熟后入药极佳。'),
('地火芝孢子', 'SEED', '{"grow_time":24,"reharvest":0,"production_items":[{"weight":1,"template_id":44}]}',  '["seed","herb","fire","uncommon"]', '地火芝的孢子，生于岩浆裂隙，种在火脉灵田产量翻倍。'),
('紫丹参苗',   'SEED', '{"grow_time":30,"reharvest":0,"production_items":[{"weight":1,"template_id":46}]}',  '["seed","herb","healing","uncommon"]', '紫丹参的幼苗，种下后一月可收，是炼制疗伤丹药的基础。'),
('天心兰种子', 'SEED', '{"grow_time":72,"reharvest":0,"production_items":[{"weight":1,"template_id":48}]}',  '["seed","herb","wisdom","rare"]', '天心兰的种子，花开放时能让人心旷神怡，助益悟道修行。'),
('太阳花种子', 'SEED', '{"grow_time":48,"reharvest":0,"production_items":[{"weight":1,"template_id":50}]}',  '["seed","herb","fire","uncommon"]', '向阳而生的灵花，日照越足长势越旺，花瓣可入药。'),
('星月花种子', 'SEED', '{"grow_time":48,"reharvest":0,"production_items":[]}',  '["seed","herb","night","rare"]', '只在夜晚吸收月华生长的灵花，白天会闭合休眠。'),
('九穗禾',     'SEED', '{"grow_time":24,"reharvest":1,"production_items":[]}',  '["seed","grain","common"]', '上古神农氏留下的灵谷，一茎九穗，收成后根可再生一茬。'),
('菩提树种',   'SEED', '{"grow_time":168,"reharvest":0,"production_items":[{"weight":1,"template_id":51}]}', '["seed","tree","wisdom","epic"]', '菩提古树的树种，种下后需等待整整七天才破土，叶可助人顿悟。'),
('何首乌苗',   'SEED', '{"grow_time":72,"reharvest":0,"production_items":[{"weight":1,"template_id":56}]}',  '["seed","herb","uncommon"]', '何首乌的幼苗，种植需三年，但药效随年份倍增。'),
('龙血草种子', 'SEED', '{"grow_time":96,"reharvest":0,"production_items":[{"weight":1,"template_id":47}]}',  '["seed","herb","dragon","rare"]', '龙血草的种子，据说只有龙气浸润之地才能发芽。'),
('幽冥花种子', 'SEED', '{"grow_time":96,"reharvest":0,"production_items":[{"weight":1,"template_id":49}]}',  '["seed","herb","dark","rare"]', '幽冥花的种子，需阴气充沛的灵田，只在子夜发芽。'),
('还魂草种子', 'SEED', '{"grow_time":120,"reharvest":0,"production_items":[{"weight":1,"template_id":52}]}', '["seed","herb","cure","rare"]', '还魂草的种子，生长极慢，但一株可救一命。'),
('冰魄花种子', 'SEED', '{"grow_time":96,"reharvest":0,"production_items":[{"weight":1,"template_id":53}]}',  '["seed","herb","ice","rare"]', '冰魄花的种子，离开寒潭即枯，种植条件苛刻。');
