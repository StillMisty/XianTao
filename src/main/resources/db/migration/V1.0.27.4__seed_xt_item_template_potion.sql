-- 丹药种子数据 (xt_item_template, type=POTION)
INSERT INTO xt_item_template (name, type, properties, tags, description) VALUES
-- 经验丹药
('小聚灵丹',     'POTION', '{"effects":[{"type":"exp","amount":100}]}',    '["pill","exp","entry"]', '入门修士服用的基础丹药，蕴含少量灵气，可快速转化为修为。'),
('聚灵丹',       'POTION', '{"effects":[{"type":"exp","amount":300}]}',    '["pill","exp","basic"]', '标准灵气丹，筑基修士日服十粒不皱眉，练气修士谨慎服用。'),
('大聚灵丹',     'POTION', '{"effects":[{"type":"exp","amount":800}]}',    '["pill","exp","intermediate"]', '灵气浓郁的丹药，金丹修士方敢直接服用，低阶修士需分次化开。'),
('天元丹',       'POTION', '{"effects":[{"type":"exp","amount":2000}]}',   '["pill","exp","advanced"]', '以天地元气炼成，一颗蕴含百倍于聚灵丹的灵气，元婴修士的最爱。'),
('太乙金丹',     'POTION', '{"effects":[{"type":"exp","amount":5000}]}',   '["pill","exp","epic"]', '传说太乙真人传下的丹方，金光流转，化神之下磕此丹犹如开挂。'),
('九转仙灵丹',   'POTION', '{"effects":[{"type":"exp","amount":15000}]}',  '["pill","exp","legendary"]', '九转方得一颗，据说化神大圆满修士也只见过丹方。'),
-- 疗伤丹药
('回春丹',       'POTION', '{"effects":[{"type":"hp","amount":200}]}',     '["pill","heal","basic"]', '草木精华炼制的疗伤丹，服下外伤即刻愈合，内伤仍需静养。'),
('大还丹',       'POTION', '{"effects":[{"type":"hp","amount":600}]}',     '["pill","heal","intermediate"]', '可续断骨、接经脉的疗伤圣品，光头上仙秘传丹方。'),
('九转回春丹',   'POTION', '{"effects":[{"type":"hp","amount":2000}]}',    '["pill","heal","advanced"]', '九转炼制的回春丹，只要还有一口气就能拉回来。'),
('小还魂丹',     'POTION', '{"effects":[{"type":"cure","amount":1}]}',      '["pill","cure","rare"]', '以还魂草为主材炼成，可让濒死之人回转一口气，保命神丹。'),
('大还魂丹',     'POTION', '{"effects":[{"type":"hp","amount":5000}]}',    '["pill","heal","epic"]', '据说能生死人肉白骨，元婴修士也要备一颗防身。'),
-- 属性丹药
('壮骨丹',       'POTION', '{"effects":[{"type":"stat","stat":"STR","amount":10,"permanent":true}]}',  '["pill","stat","str"]', '淬炼筋骨，增力十钧，服后感觉能一拳打死一头牛。'),
('金刚散',       'POTION', '{"effects":[{"type":"stat","stat":"CON","amount":10,"permanent":true}]}',  '["pill","stat","con"]', '锤炼皮膜内脏，固本培元，挨打时没那么疼了。'),
('轻身散',       'POTION', '{"effects":[{"type":"stat","stat":"AGI","amount":10,"permanent":true}]}',  '["pill","stat","agi"]', '身轻如燕草上飞，服后走路自带残影。'),
('开智丹',       'POTION', '{"effects":[{"type":"stat","stat":"WIS","amount":10,"permanent":true}]}',  '["pill","stat","wis"]', '开启灵台，悟性大增，以前看不懂的法决突然就通了。'),
('龙力丹',       'POTION', '{"effects":[{"type":"stat","stat":"STR","amount":30,"permanent":true}]}',  '["pill","stat","str","rare"]', '以龙血草为主材，服后力道暴涨。'),
('不动明王丹',   'POTION', '{"effects":[{"type":"stat","stat":"CON","amount":30,"permanent":true}]}', '["pill","stat","con","rare"]', '如明王端坐不动，筋骨如金刚，打不死的是我。'),
('踏风丹',       'POTION', '{"effects":[{"type":"stat","stat":"AGI","amount":30,"permanent":true}]}', '["pill","stat","agi","rare"]', '御风而行，踏虚凌空，金丹以下都看不清你的影子。'),
('大悟道丹',     'POTION', '{"effects":[{"type":"stat","stat":"WIS","amount":30,"permanent":true}]}', '["pill","stat","wis","rare"]', '以菩提叶炼成，有人吃了直接顿悟破境——但那是他的机缘。'),
-- 突破丹药
('筑基丹',       'POTION', '{"effects":[{"type":"breakthrough","stage":"foundation"}]}',  '["pill","breakthrough","critical"]', '练气修士冲击筑基的关键丹药，一粒难求，黑市炒到天价。'),
('结丹丹',       'POTION', '{"effects":[{"type":"breakthrough","stage":"core_formation"}]}', '["pill","breakthrough","critical"]', '辅助筑基圆满修士凝结金丹，失败率降低三成。'),
('化婴丹',       'POTION', '{"effects":[{"type":"breakthrough","stage":"nascent_soul"}]}', '["pill","breakthrough","epic"]', '金丹圆满破碎化婴，从此踏上真正仙途。丹成九转方可得。'),
('化神丹',       'POTION', '{"effects":[{"type":"breakthrough","stage":"deity_transformation"}]}', '["pill","breakthrough","legendary"]', '元婴圆满冲击化神之用，此丹已成天下绝品，可遇不可求。'),
-- 特殊丹药
('洗髓丹',       'POTION', '{"effects":[{"type":"stat","stat":"STR","amount":5,"permanent":true},{"type":"stat","stat":"CON","amount":5,"permanent":true}]}', '["pill","special","entry"]', '伐毛洗髓，脱胎换骨，入门修士必服的第一粒丹药。'),
('瞬回丹',       'POTION', '{"effects":[{"type":"hp","amount":1000}]}',    '["pill","heal","emergency"]', '危急时刻服下即可大幅回血，丹瓶上写着"打架必备"。'),
('抗性丹',       'POTION', '{"effects":[{"type":"buff","effect":"resist","duration":180}]}', '["pill","buff","resist"]', '提前服下可抵御部分负面状态，持续三炷香。'),
('避毒丹',       'POTION', '{"effects":[{"type":"buff","effect":"poison_immune","duration":600}]}', '["pill","buff","immune"]', '服用后万毒不侵一炷香，深入毒沼之前务必嗑一粒。'),
('清心丹',       'POTION', '{"effects":[{"type":"buff","effect":"calm","duration":900}]}', '["pill","buff","mind"]', '祛除心魔、明心见性，渡劫前服之可定心神。'),
('凝神丹',       'POTION', '{"effects":[{"type":"buff","effect":"focus","duration":600}]}', '["pill","buff","mind"]', '凝神聚气，一刻钟内法决冷却缩短一半。'),
('破甲丹',       'POTION', '{"effects":[{"type":"buff","effect":"armor_break","duration":180}]}', '["pill","buff","offensive"]', '服后攻击附带破甲效果，啃不动的硬茬也变得酥脆。'),
('易容丹',       'POTION', '{"effects":[{"type":"buff","effect":"disguise","duration":3600}]}', '["pill","buff","utility"]', '改变面容一个时辰，但熟人还是认得你的灵气气息。'),
('定颜丹',       'POTION', '{"effects":[{"type":"buff","effect":"youth","permanent":true}]}', '["pill","special","cosmetic"]', '永葆青春的驻颜丹，女修界最畅销丹药没有之一。'),
('辟谷丹',       'POTION', '{"effects":[{"type":"buff","effect":"no_hunger","duration":86400}]}', '["pill","buff","utility"]', '服一粒可一日不食，闭关修炼必备，但味道不如真饭吃。'),
('延寿丹',       'POTION', '{"effects":[{"type":"buff","effect":"lifespan","amount":10}]}', '["pill","special","lifespan","rare"]', '一颗延寿十年，凡人视若仙丹，修士视为保底续命之物。'),
('天劫丹',       'POTION', '{"effects":[{"type":"buff","effect":"tribulation_resist","duration":3600}]}', '["pill","buff","tribulation","epic"]', '渡劫专用，可抵御三成天雷之力，但别指望它能扛第九道。');
