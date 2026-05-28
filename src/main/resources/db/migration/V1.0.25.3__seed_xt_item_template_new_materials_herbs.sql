-- 新增药材种子数据
INSERT INTO xt_item_template(name, type, properties, tags, description) VALUES
('雷击木', 'HERB', '{"elements":{"thunder":2}}', '["herb","thunder","common"]', '被雷劈过的枯木，残存雷气，可入药引雷。'),
('风信草', 'HERB', '{"elements":{"wind":2}}', '["herb","wind","common"]', '随风摇曳的细草，风行修士常用。'),
('冰晶花', 'HERB', '{"elements":{"ice":2}}', '["herb","ice","common"]', '冰面上的小花，触之冰凉。'),
('火绒草', 'HERB', '{"elements":{"fire":2}}', '["herb","fire","common"]', '干燥易燃的灵草，火行入门药材。'),
('金盏花', 'HERB', '{"elements":{"metal":2}}', '["herb","metal","common"]', '金色小花，金气内敛。'),
('地骨皮', 'HERB', '{"elements":{"earth":2}}', '["herb","earth","common"]', '灵树根皮，土行平和之材。'),
('星辰草', 'HERB', '{"elements":{"metal":2,"water":1}}', '["herb","star","uncommon"]', '叶面有星点的灵草，夜间发光。'),
('雷藤', 'HERB', '{"elements":{"thunder":3}}', '["herb","thunder","uncommon"]', '缠绕雷气的藤蔓，触之微麻。'),
('风铃花', 'HERB', '{"elements":{"wind":3}}', '["herb","wind","uncommon"]', '风吹时发出铃声的灵花，安神定志。'),
('霜降果', 'HERB', '{"elements":{"ice":3}}', '["herb","ice","uncommon"]', '霜降时节采摘的灵果，寒气凝结。'),
('龙须草', 'HERB', '{"elements":{"fire":3,"wood":2}}', '["herb","dragon","rare"]', '形如龙须的灵草，龙气所化。'),
('雷公藤', 'HERB', '{"elements":{"thunder":4,"metal":1}}', '["herb","thunder","rare"]', '雷暴后生长的异藤，蕴含天威。'),
('风灵芝', 'HERB', '{"elements":{"wind":4,"wood":1}}', '["herb","wind","rare"]', '生于风口的灵芝，轻若无物。'),
('天霜莲', 'HERB', '{"elements":{"ice":4,"water":2}}', '["herb","ice","rare"]', '天降霜华凝结的冰莲，可炼极品冰丹。');-- 新增材料种子数据
INSERT INTO xt_item_template(name, type, properties, tags, description) VALUES
('雷击铁', 'MATERIAL', '{"RIGIDITY":7,"TOUGHNESS":3,"SPIRIT":3}', '["ore","thunder","craft","common"]', '被雷劈过的铁矿，带微弱雷气。'),
('风磨石', 'MATERIAL', '{"RIGIDITY":6,"TOUGHNESS":2,"SPIRIT":2}', '["ore","wind","craft","common"]', '风蚀而成的灵石，质地轻盈。'),
('冰凌晶', 'MATERIAL', '{"RIGIDITY":8,"TOUGHNESS":4,"SPIRIT":5}', '["ore","ice","craft","uncommon"]', '冰川深处的结晶体，寒气凝而不散。'),
('雷纹木', 'MATERIAL', '{"RIGIDITY":4,"TOUGHNESS":7,"SPIRIT":6}', '["wood","thunder","craft","uncommon"]', '带雷纹的灵木，韧性极佳。'),
('风灵石', 'MATERIAL', '{"RIGIDITY":3,"TOUGHNESS":3,"SPIRIT":8}', '["ore","wind","craft","uncommon"]', '含风行灵气的轻石，可制飞行法器。'),
('玄冰铁', 'MATERIAL', '{"RIGIDITY":14,"TOUGHNESS":6,"SPIRIT":7}', '["ore","ice","craft","rare"]', '极寒之地的玄铁变种，硬度惊人。'),
('雷魂石', 'MATERIAL', '{"RIGIDITY":8,"TOUGHNESS":5,"SPIRIT":14}', '["ore","thunder","craft","rare"]', '雷暴区凝聚的灵魂结晶，内蕴天雷之力。'),
('风暴之心', 'MATERIAL', '{"RIGIDITY":2,"TOUGHNESS":2,"SPIRIT":16}', '["essence","wind","craft","rare"]', '风暴核心凝结的精华，蕴含狂风之力。');
