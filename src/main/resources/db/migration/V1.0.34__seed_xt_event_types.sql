-- 事件类型 + 活动事件关联 种子数据

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
-- TRAVEL 事件
('TRAVEL', 'travel_ambush_beast', '突遇妖兽', '赶路途中突然遭遇妖兽袭击，不得不停下来应对。'),
('TRAVEL', 'travel_broken_cart', '路边破车', '路边有一辆废弃的马车，里面或许有什么值钱的东西。'),
('TRAVEL', 'travel_rainstorm', '暴雨阻路', '一场突如其来的大暴雨让你寸步难行，只能暂避。'),
('TRAVEL', 'travel_friendly_merchant', '路遇商队', '一支友善的商队邀请你同行，可能会分享一些消息和物资。'),
('TRAVEL', 'travel_injured_cultivator', '受伤修士', '路边倒着一位受伤的修士，帮他就可能在日后得报。'),
('TRAVEL', 'travel_wandering_elder', '途中遇仙', '一位白发白须的老者坐在路边石头上打盹——不，他不是凡人。'),
('TRAVEL', 'travel_treasure_map', '藏宝图碎片', '风吹来一张发黄的纸头——看起来像古董藏宝图的一角。'),
('TRAVEL', 'travel_bridge_collapse', '桥塌了', '你正要过桥结果桥塌了——修仙界也有豆腐渣工程。'),
('TRAVEL', 'travel_bandit_roadblock', '路匪拦路', '一伙不知天高地厚的路匪拦住了去路——你可是修士哎。'),
('TRAVEL', 'travel_strange_fog', '奇雾迷踪', '突然起了大雾伸手不见五指，雾中似乎还有东西在移动。'),
('TRAVEL', 'travel_fallen_star', '陨星坠地', '一颗流星在你前方不远处坠落了——赶去看看有没有陨铁。'),
('TRAVEL', 'travel_spring_of_spirit', '灵泉偶遇', '路边突然出现一汪灵气四溢的泉水，看起来可以喝。'),
-- TRAINING 事件
('TRAINING', 'training_rare_herb_found', '发现珍稀药草', '在历练时偶然发现了一株{{herb}}，采集到 ×{{count}}，运气不错！'),
('TRAINING', 'training_ancient_ruins', '发现远古遗迹', '在历练地发现了一个此前不为人知的远古遗迹入口。'),
('TRAINING', 'training_meditation_epiphany', '静坐顿悟', '在历练中突有所感，就地打坐悟出些许天道碎片，获得 {{exp}} 修为。'),
('TRAINING', 'training_rival_encounter', '遇到竞争对手', '另一个修士也在同一片区域历练——他好像不太友善。'),
('TRAINING', 'training_buried_treasure', '挖出宝贝', '挖开一块松土发现了不知谁埋的宝贝：{{item}} ×{{count}}。'),
('TRAINING', 'training_strange_stone', '奇石异象', '奇石在你靠近时发出微光，竟是{{item}} ×{{count}}。'),
('TRAINING', 'training_evil_presence', '邪气侵体', '一阵莫名阴寒袭来，邪气侵体，受到 {{damage}} 点伤害。'),
('TRAINING', 'training_spirit_guide', '灵体引路', '友善灵体引领你去了一处灵气浓郁之地，获得 +{{exp}} 修为。'),
('TRAINING', 'training_qi_storm', '灵气风暴', '灵气狂暴形成风暴！你在风暴眼稳守心神修炼，获得 +{{exp}} 修为。'),
('TRAINING', 'training_beast_den_found', '发现妖兽巢穴', '发现妖兽巢穴，翻找到妖兽藏匿的宝贝。'),
-- BOUNTY_SIDE 事件
('BOUNTY_SIDE', 'bounty_clue_found', '找到悬赏线索', '在执行悬赏过程中发现了额外的线索——任务可能比想象的复杂。'),
('BOUNTY_SIDE', 'bounty_betrayal', '委托人的背叛', '委托人其实是个骗子——他的真实目的另有隐情。'),
('BOUNTY_SIDE', 'bounty_extra_target', '额外目标出现', '悬赏目标之外竟然还有一个副目标，价值也不低——顺手做掉？'),
('BOUNTY_SIDE', 'bounty_hidden_cache', '发现秘密藏宝', '追踪悬赏目标时无意中发现了他们的隐秘藏宝库。'),
('BOUNTY_SIDE', 'bounty_witness_arrives', '目击者登场', '一个自称是目击者的NPC出现，带来了关于悬赏目标的新情报。'),
('BOUNTY_SIDE', 'bounty_rival_hunter', '竞争者登场', '另一个赏金猎人也盯上了你的目标——看谁先得手。'),
('BOUNTY_SIDE', 'bounty_monster_interference', '妖兽搅局', '正追着目标呢突然窜出一只妖兽打乱了局面。'),
('BOUNTY_SIDE', 'bounty_cultivation_boost', '悬赏中的顿悟', '在执行悬赏的激烈战斗中竟意外地突破了修为瓶颈。'),
('BOUNTY_SIDE', 'bounty_ancient_secret', '上古秘藏', '在你仔细搜查现场时，无意间触动了一个被忽略的机关——轰隆声中，一扇隐藏的石门缓缓打开，露出了一间尘封千年的密室。'),
('BOUNTY_SIDE', 'bounty_spirit_revelation', '灵机一现', '正当你准备离开时，天地间的灵气突然出现了异样的波动——冥冥之中似乎有什么在指引你走向一个意想不到的方向。'),
('BOUNTY_SIDE', 'bounty_fateful_encounter', '命运邂逅', '一位神秘修士突然出现在你面前，他似乎一直在等你——"你就是接了这个悬赏的人？很好，我有些话要单独对你说……"'),
('BOUNTY_SIDE', 'bounty_environmental_insight', '天时地利', '你敏锐地察觉到周围环境的异常——原来此地暗藏玄机，只有真正用心观察的人才能发现其中的奥妙。');

-- ============ xt_activity_event (活动事件关联) ============
-- TRAVEL events (owner_id = map_id)
INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, is_hidden, trigger_type, trigger_params, params) VALUES
('TRAVEL', 1, 'travel_ambush_beast', 'NUMERIC', 20, false, NULL, '{}', '{}'),
('TRAVEL', 1, 'travel_broken_cart', 'NUMERIC', 15, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_RANDOM_ITEM', 'template_ids', jsonb_build_array((SELECT id FROM xt_item_template WHERE name='灵芝'), (SELECT id FROM xt_item_template WHERE name='玄铁矿石')), 'chance', 0.3)))),
('TRAVEL', 1, 'travel_friendly_merchant', 'NUMERIC', 10, false, NULL, '{}', '{}'),
('TRAVEL', 2, 'travel_strange_fog', 'NUMERIC', 15, false, NULL, '{}', '{}'),
('TRAVEL', 2, 'travel_injured_cultivator', 'NUMERIC', 10, false, NULL, '{}', '{"effects": [{"type": "ADD_EXP", "amount": 50}]}'),
('TRAVEL', 3, 'travel_fallen_star', 'NUMERIC', 10, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='玄铁矿石'), 'count', 1)))),
('TRAVEL', 4, 'travel_spring_of_spirit', 'NUMERIC', 12, false, NULL, '{}', '{"effects": [{"type": "HEAL_FLAT", "amount": 100}]}'),
('TRAVEL', 4, 'travel_broken_cart', 'NUMERIC', 8, false, NULL, '{}', '{}'),
('TRAVEL', 5, 'travel_rainstorm', 'NUMERIC', 15, false, NULL, '{}', '{}'),
('TRAVEL', 6, 'travel_friendly_merchant', 'NUMERIC', 15, false, NULL, '{}', '{}'),
('TRAVEL', 6, 'travel_wandering_elder', 'NUMERIC', 5, false, 'STAT_THRESHOLD', '{"stat":"WIS","min":20}', '{}'),
('TRAVEL', 7, 'travel_treasure_map', 'NUMERIC', 8, false, NULL, '{}', '{}'),
('TRAVEL', 8, 'travel_strange_fog', 'NUMERIC', 15, false, NULL, '{}', '{}'),
('TRAVEL', 10, 'travel_bandit_roadblock', 'NUMERIC', 15, false, NULL, '{}', '{}'),
('TRAVEL', 11, 'travel_friendly_merchant', 'NUMERIC', 12, false, NULL, '{}', '{}'),
('TRAVEL', 11, 'travel_wandering_elder', 'NUMERIC', 6, false, 'STAT_THRESHOLD', '{"stat":"WIS","min":45}', '{}'),
('TRAVEL', 17, 'travel_strange_fog', 'NUMERIC', 20, false, NULL, '{}', '{}'),
('TRAVEL', 24, 'travel_spring_of_spirit', 'NUMERIC', 10, false, NULL, '{}', '{"effects": [{"type": "HEAL_FLAT", "amount": 500}]}'),
('TRAVEL', 27, 'travel_fallen_star', 'NUMERIC', 12, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='天外陨铁'), 'count', 1)))),
('TRAVEL', 30, 'travel_fallen_star', 'NUMERIC', 15, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='天外陨铁'), 'count', 1)))),
('TRAVEL', 35, 'travel_strange_fog', 'NUMERIC', 15, false, 'HAS_ITEM', jsonb_build_object('item_template_id', (SELECT id FROM xt_item_template WHERE name='魂玉碎片')), '{}'),
('TRAVEL', 39, 'travel_fallen_star', 'NUMERIC', 8, false, NULL, '{}', '{}'),
('TRAVEL', 42, 'travel_wandering_elder', 'NUMERIC', 10, false, NULL, '{}', '{"effects": [{"type": "ADD_EXP", "amount": 50}]}');

-- TRAINING events (owner_id = map_id)
INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, is_hidden, trigger_type, trigger_params, params) VALUES
('TRAINING', 2, 'training_rare_herb_found', 'NUMERIC', 15, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='灵芝'), 'min', 2, 'max', 4)))),
('TRAINING', 3, 'training_buried_treasure', 'NUMERIC', 10, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='玄铁矿石'), 'count', 2)))),
('TRAINING', 3, 'training_strange_stone', 'NUMERIC', 8, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='赤铜矿'), 'count', 1)))),
('TRAINING', 4, 'training_qi_storm', 'NUMERIC', 10, false, NULL, '{}', '{"effects": [{"type": "ADD_EXP", "amount": 50}]}'),
('TRAINING', 5, 'training_rival_encounter', 'NUMERIC', 10, false, NULL, '{}', '{}'),
('TRAINING', 7, 'training_rare_herb_found', 'NUMERIC', 12, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='地火芝'), 'min', 1, 'max', 3)))),
('TRAINING', 8, 'training_evil_presence', 'NUMERIC', 12, false, NULL, '{}', '{"effects": [{"type": "TAKE_DAMAGE_FLAT", "amount": 30}]}'),
('TRAINING', 9, 'training_ancient_ruins', 'NUMERIC', 15, false, NULL, '{}', '{}'),
('TRAINING', 12, 'training_beast_den_found', 'NUMERIC', 10, false, NULL, '{}', '{}'),
('TRAINING', 14, 'training_meditation_epiphany', 'NUMERIC', 15, false, NULL, '{}', '{"effects": [{"type": "ADD_EXP", "amount": 200}]}'),
('TRAINING', 15, 'training_ancient_ruins', 'NUMERIC', 12, false, 'HAS_SKILL', jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name='青莲剑歌')), '{}'),
('TRAINING', 16, 'training_rare_herb_found', 'NUMERIC', 15, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='地火芝'), 'min', 2, 'max', 5)))),
('TRAINING', 20, 'training_strange_stone', 'NUMERIC', 10, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='紫金砂'), 'count', 2)))),
('TRAINING', 23, 'training_spirit_guide', 'NUMERIC', 10, false, NULL, '{}', '{"effects": [{"type": "ADD_EXP", "amount": 150}]}'),
('TRAINING', 27, 'training_evil_presence', 'NUMERIC', 15, false, NULL, '{}', '{"effects": [{"type": "TAKE_DAMAGE_FLAT", "amount": 80}]}'),
('TRAINING', 29, 'training_meditation_epiphany', 'NUMERIC', 15, false, NULL, '{}', '{"effects": [{"type": "ADD_EXP", "amount": 500}]}'),
('TRAINING', 32, 'training_qi_storm', 'NUMERIC', 20, false, NULL, '{}', '{"effects": [{"type": "ADD_EXP", "amount": 300}]}'),
('TRAINING', 34, 'training_meditation_epiphany', 'NUMERIC', 20, false, NULL, '{}', '{"effects": [{"type": "ADD_EXP", "amount": 800}]}'),
('TRAINING', 38, 'training_spirit_guide', 'NUMERIC', 12, false, NULL, '{}', '{"effects": [{"type": "ADD_EXP", "amount": 500}]}'),
('TRAINING', 39, 'training_meditation_epiphany', 'NUMERIC', 15, false, NULL, '{}', '{"effects": [{"type": "ADD_EXP", "amount": 1000}]}'),
('TRAINING', 41, 'training_qi_storm', 'NUMERIC', 15, false, NULL, '{}', '{"effects": [{"type": "ADD_EXP", "amount": 500}]}');

-- Hidden TRAVEL events
INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, is_hidden, trigger_type, trigger_params, params) VALUES
('TRAVEL', 14, 'travel_wandering_elder', 'NUMERIC', 8, true, 'STAT_THRESHOLD', '{"stat":"WIS","min":50}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='大悟道丹'), 'count', 1)))),
('TRAVEL', 28, 'travel_spring_of_spirit', 'NUMERIC', 10, true, 'HAS_ITEM', jsonb_build_object('item_template_id', (SELECT id FROM xt_item_template WHERE name='九转金莲')), '{}'),
('TRAVEL', 33, 'travel_bandit_roadblock', 'NUMERIC', 10, true, 'HAS_SKILL', jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name='刑天斧法')), '{}');

-- Hidden TRAINING events
INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, is_hidden, trigger_type, trigger_params, params) VALUES
('TRAINING', 29, 'training_spirit_guide', 'NUMERIC', 8, true, 'HAS_SKILL', jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name='一气化三清')), jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='魂玉碎片'), 'count', 1)))),
('TRAINING', 40, 'training_meditation_epiphany', 'NUMERIC', 8, true, 'STAT_THRESHOLD', '{"stat":"WIS","min":85}', '{"effects": [{"type": "ADD_EXP", "amount": 5000}]}'),
('TRAINING', 44, 'training_ancient_ruins', 'NUMERIC', 5, true, 'HAS_SKILL', jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name='轩辕剑法')), jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'CREATE_EQUIPMENT', 'template_id', (SELECT id FROM xt_equipment_template WHERE name='轩辕剑')))));

-- BOUNTY_SIDE events (owner_id = bounty_id, using codes from event_type)
INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, is_hidden, trigger_type, trigger_params, params) VALUES
('BOUNTY_SIDE', 1, 'bounty_clue_found', 'NUMERIC', 20, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 80), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 30)))),
('BOUNTY_SIDE', 5, 'bounty_monster_interference', 'NUMERIC', 15, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 50)))),
('BOUNTY_SIDE', 10, 'bounty_extra_target', 'NUMERIC', 12, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 150), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 60)))),
('BOUNTY_SIDE', 15, 'bounty_hidden_cache', 'NUMERIC', 10, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='聚灵丹'), 'count', 3), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 100)))),
('BOUNTY_SIDE', 20, 'bounty_rival_hunter', 'NUMERIC', 12, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 200), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 80)))),
('BOUNTY_SIDE', 25, 'bounty_witness_arrives', 'NUMERIC', 10, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 100), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='兽骨'), 'count', 1)))),
('BOUNTY_SIDE', 40, 'bounty_clue_found', 'NUMERIC', 15, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 150), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='玄铁矿石'), 'count', 2)))),
('BOUNTY_SIDE', 50, 'bounty_monster_interference', 'NUMERIC', 15, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 300), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 150)))),
('BOUNTY_SIDE', 60, 'bounty_cultivation_boost', 'NUMERIC', 10, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 800), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 200)))),
('BOUNTY_SIDE', 70, 'bounty_extra_target', 'NUMERIC', 12, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 400), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 200)))),
('BOUNTY_SIDE', 80, 'bounty_hidden_cache', 'NUMERIC', 10, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 500), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='灵芝'), 'count', 2)))),
('BOUNTY_SIDE', 120, 'bounty_rival_hunter', 'NUMERIC', 12, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 600), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 300)))),
('BOUNTY_SIDE', 150, 'bounty_witness_arrives', 'NUMERIC', 10, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 1000), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 500)))),
('BOUNTY_SIDE', 200, 'bounty_cultivation_boost', 'NUMERIC', 8, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 2000), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 800)))),
('BOUNTY_SIDE', 250, 'bounty_clue_found', 'NUMERIC', 15, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 800), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 400)))),
('BOUNTY_SIDE', 300, 'bounty_monster_interference', 'NUMERIC', 12, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 800), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 400)))),
('BOUNTY_SIDE', 350, 'bounty_extra_target', 'NUMERIC', 10, false, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 1200), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 600)))),
('BOUNTY_SIDE', 400, 'bounty_cultivation_boost', 'NUMERIC', 5, true, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 10000), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 5000), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='大悟道丹'), 'count', 1), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='青鸾卵'), 'count', 1), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='天人感应玉简'), 'count', 1)))),
('BOUNTY_SIDE', 6, 'bounty_hidden_cache', 'NUMERIC', 6, true, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 1500), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 500), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='小聚灵丹'), 'count', 3)))),
('BOUNTY_SIDE', 20, 'bounty_extra_target', 'NUMERIC', 6, true, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 2000), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 800), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='灵芝妖卵'), 'count', 1), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='青木诀玉简'), 'count', 1)))),
('BOUNTY_SIDE', 51, 'bounty_ancient_secret', 'NUMERIC', 5, true, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 2000), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 800), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='冰魄花'), 'count', 1), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='雪狐卵'), 'count', 1), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='聚灵丹'), 'count', 1)))),
('BOUNTY_SIDE', 91, 'bounty_fateful_encounter', 'NUMERIC', 5, true, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 3000), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 1500), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='朱砂'), 'count', 3), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='火蟾卵'), 'count', 1), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='大聚灵丹'), 'count', 1)))),
('BOUNTY_SIDE', 101, 'bounty_cultivation_boost', 'NUMERIC', 5, true, 'STAT_THRESHOLD', jsonb_build_object('stat', 'WIS', 'min', 45), jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 5000), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 2000), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='碧鳞蛇卵'), 'count', 1), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='水镜术玉简'), 'count', 1)))),
('BOUNTY_SIDE', 165, 'bounty_spirit_revelation', 'NUMERIC', 5, true, 'STAT_THRESHOLD', jsonb_build_object('stat', 'WIS', 'min', 55), jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 8000), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 3000), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='白泽卵'), 'count', 1), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='太乙金丹'), 'count', 1)))),
('BOUNTY_SIDE', 191, 'bounty_ancient_secret', 'NUMERIC', 5, true, 'HAS_SKILL', jsonb_build_object('skill_id', (SELECT id FROM xt_skill WHERE name='轩辕剑法')), jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 10000), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 5000), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='轩辕剑法玉简'), 'count', 1), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='九转仙灵丹'), 'count', 1)))),
('BOUNTY_SIDE', 247, 'bounty_extra_target', 'NUMERIC', 5, true, NULL, '{}', jsonb_build_object('effects', jsonb_build_array(jsonb_build_object('type', 'ADD_EXP', 'amount', 4000), jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 2000), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='金翼雕卵'), 'count', 1), jsonb_build_object('type', 'ADD_ITEM', 'template_id', (SELECT id FROM xt_item_template WHERE name='龙力丹'), 'count', 1))));

-- COMBAT event type definitions (migrated from xt_map_node.monster_encounters)

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_野狼', '野狼', '遭遇野狼');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_毒蛇', '毒蛇', '遭遇毒蛇');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_山魈', '山魈', '遭遇山魈');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_妖鼠', '妖鼠', '遭遇妖鼠');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_食人花', '食人花', '遭遇食人花');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_石灵', '石灵', '遭遇石灵');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_野猪妖', '野猪妖', '遭遇野猪妖');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_骷髅兵', '骷髅兵', '遭遇骷髅兵');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_风狼', '风狼', '遭遇风狼');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_树精', '树精', '遭遇树精');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_冰狼', '冰狼', '遭遇冰狼');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_石甲龟', '石甲龟', '遭遇石甲龟');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_火焰蜥', '火焰蜥', '遭遇火焰蜥');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_幽魂', '幽魂', '遭遇幽魂');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_螳螂妖', '螳螂妖', '遭遇螳螂妖');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_山贼', '山贼', '遭遇山贼');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_蝙蝠妖', '蝙蝠妖', '遭遇蝙蝠妖');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_铁甲虫', '铁甲虫', '遭遇铁甲虫');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_水鬼', '水鬼', '遭遇水鬼');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_妖狐', '妖狐', '遭遇妖狐');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_怨灵', '怨灵', '遭遇怨灵');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_血蝠', '血蝠', '遭遇血蝠');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_石魔', '石魔', '遭遇石魔');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_妖道', '妖道', '遭遇妖道');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_冰蚕', '冰蚕', '遭遇冰蚕');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_猿妖', '猿妖', '遭遇猿妖');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_毒蟾', '毒蟾', '遭遇毒蟾');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_蛇妖', '蛇妖', '遭遇蛇妖');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_摄魂妖', '摄魂妖', '遭遇摄魂妖');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_飞头蛮', '飞头蛮', '遭遇飞头蛮');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_熔岩巨兽', '熔岩巨兽', '遭遇熔岩巨兽');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_雪女', '雪女', '遭遇雪女');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_夜叉', '夜叉', '遭遇夜叉');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_金甲尸', '金甲尸', '遭遇金甲尸');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_狮鹫', '狮鹫', '遭遇狮鹫');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_千年树妖', '千年树妖', '遭遇千年树妖');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_雷鹰', '雷鹰', '遭遇雷鹰');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_修罗', '修罗', '遭遇修罗');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_黑风老妖', '黑风老妖', '遭遇黑风老妖');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_九尾妖狐', '九尾妖狐', '遭遇九尾妖狐');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_火凤雏', '火凤雏', '遭遇火凤雏');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_山鬼', '山鬼', '遭遇山鬼');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_蜚廉', '蜚廉', '遭遇蜚廉');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_幽冥骑士', '幽冥骑士', '遭遇幽冥骑士');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_蛟龙', '蛟龙', '遭遇蛟龙');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_天罗蛛', '天罗蛛', '遭遇天罗蛛');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_梼杌', '梼杌', '遭遇梼杌');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_夔牛', '夔牛', '遭遇夔牛');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_金乌', '金乌', '遭遇金乌');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_旱魃', '旱魃', '遭遇旱魃');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_白泽', '白泽', '遭遇白泽');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_烛龙', '烛龙', '遭遇烛龙');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_天魔王', '天魔王', '遭遇天魔王');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_麒麟', '麒麟', '遭遇麒麟');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_相柳', '相柳', '遭遇相柳');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_刑天', '刑天', '遭遇刑天');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_毕方', '毕方', '遭遇毕方');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_应龙', '应龙', '遭遇应龙');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_守鹤', '守鹤', '遭遇守鹤');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_鲲鹏', '鲲鹏', '遭遇鲲鹏');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_神龙', '神龙', '遭遇神龙');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_阎罗天子', '阎罗天子', '遭遇阎罗天子');

INSERT INTO xt_event_type (activity_type, code, name, description) VALUES
('TRAINING', 'combat_monster_原始天魔', '原始天魔', '遭遇原始天魔'),
('TRAINING', 'combat_monster_混沌', '混沌', '遭遇混沌');

-- COMBAT activity events
INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 2, 'combat_monster_野狼', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='野狼'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 2, 'combat_monster_毒蛇', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='毒蛇'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 2, 'combat_monster_山魈', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='山魈'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 2, 'combat_monster_妖鼠', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='妖鼠'), 'min_count', 1, 'max_count', 1));

-- 食人花已从翠竹林移除，移至青石矿洞 (map 3) 作为稀有遭遇
INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 3, 'combat_monster_食人花', 'COMBAT', 30,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='食人花'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 3, 'combat_monster_石灵', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='石灵'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 3, 'combat_monster_妖鼠', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='妖鼠'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 3, 'combat_monster_山魈', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='山魈'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 3, 'combat_monster_野猪妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='野猪妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 3, 'combat_monster_骷髅兵', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='骷髅兵'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 4, 'combat_monster_毒蛇', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='毒蛇'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 4, 'combat_monster_风狼', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='风狼'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 4, 'combat_monster_树精', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='树精'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 4, 'combat_monster_冰狼', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='冰狼'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 4, 'combat_monster_石甲龟', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='石甲龟'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 5, 'combat_monster_火焰蜥', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='火焰蜥'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 5, 'combat_monster_树精', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='树精'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 5, 'combat_monster_幽魂', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='幽魂'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 5, 'combat_monster_螳螂妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='螳螂妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 5, 'combat_monster_山贼', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='山贼'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 7, 'combat_monster_蝙蝠妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='蝙蝠妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 7, 'combat_monster_铁甲虫', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='铁甲虫'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 7, 'combat_monster_水鬼', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='水鬼'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 7, 'combat_monster_螳螂妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='螳螂妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 7, 'combat_monster_妖狐', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='妖狐'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 8, 'combat_monster_幽魂', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='幽魂'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 8, 'combat_monster_铁甲虫', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='铁甲虫'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 8, 'combat_monster_怨灵', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='怨灵'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 8, 'combat_monster_蝙蝠妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='蝙蝠妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 8, 'combat_monster_血蝠', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='血蝠'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 9, 'combat_monster_石魔', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='石魔'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 9, 'combat_monster_妖道', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='妖道'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 9, 'combat_monster_怨灵', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='怨灵'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 9, 'combat_monster_幽魂', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='幽魂'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 12, 'combat_monster_怨灵', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='怨灵'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 12, 'combat_monster_冰蚕', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='冰蚕'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 12, 'combat_monster_猿妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='猿妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 12, 'combat_monster_血蝠', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='血蝠'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 12, 'combat_monster_毒蟾', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='毒蟾'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 12, 'combat_monster_蛇妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='蛇妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 13, 'combat_monster_怨灵', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='怨灵'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 13, 'combat_monster_摄魂妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='摄魂妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 13, 'combat_monster_妖道', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='妖道'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 13, 'combat_monster_飞头蛮', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='飞头蛮'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 13, 'combat_monster_熔岩巨兽', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='熔岩巨兽'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 13, 'combat_monster_雪女', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='雪女'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 14, 'combat_monster_熔岩巨兽', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='熔岩巨兽'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 14, 'combat_monster_摄魂妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='摄魂妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 14, 'combat_monster_夜叉', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='夜叉'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 14, 'combat_monster_金甲尸', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='金甲尸'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 15, 'combat_monster_蛇妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='蛇妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 15, 'combat_monster_猿妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='猿妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 15, 'combat_monster_狮鹫', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='狮鹫'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 15, 'combat_monster_摄魂妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='摄魂妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 15, 'combat_monster_千年树妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='千年树妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 15, 'combat_monster_雷鹰', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='雷鹰'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 16, 'combat_monster_熔岩巨兽', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='熔岩巨兽'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 16, 'combat_monster_修罗', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='修罗'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 16, 'combat_monster_夜叉', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='夜叉'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 16, 'combat_monster_狮鹫', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='狮鹫'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 16, 'combat_monster_飞头蛮', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='飞头蛮'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 16, 'combat_monster_摄魂妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='摄魂妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 17, 'combat_monster_熔岩巨兽', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='熔岩巨兽'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 17, 'combat_monster_千年树妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='千年树妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 17, 'combat_monster_狮鹫', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='狮鹫'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 17, 'combat_monster_雷鹰', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='雷鹰'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 17, 'combat_monster_黑风老妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='黑风老妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 17, 'combat_monster_九尾妖狐', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='九尾妖狐'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 19, 'combat_monster_黑风老妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='黑风老妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 19, 'combat_monster_雷鹰', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='雷鹰'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 19, 'combat_monster_火凤雏', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='火凤雏'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 19, 'combat_monster_山鬼', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='山鬼'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 19, 'combat_monster_狮鹫', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='狮鹫'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 19, 'combat_monster_蜚廉', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='蜚廉'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 20, 'combat_monster_熔岩巨兽', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='熔岩巨兽'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 20, 'combat_monster_修罗', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='修罗'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 20, 'combat_monster_黑风老妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='黑风老妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 20, 'combat_monster_雷鹰', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='雷鹰'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 20, 'combat_monster_山鬼', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='山鬼'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 20, 'combat_monster_九尾妖狐', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='九尾妖狐'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 21, 'combat_monster_火凤雏', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='火凤雏'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 21, 'combat_monster_山鬼', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='山鬼'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 21, 'combat_monster_蜚廉', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='蜚廉'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 21, 'combat_monster_幽冥骑士', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='幽冥骑士'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 22, 'combat_monster_雪女', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='雪女'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 22, 'combat_monster_九尾妖狐', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='九尾妖狐'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 22, 'combat_monster_修罗', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='修罗'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 22, 'combat_monster_山鬼', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='山鬼'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 22, 'combat_monster_幽冥骑士', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='幽冥骑士'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 22, 'combat_monster_蜚廉', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='蜚廉'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 23, 'combat_monster_千年树妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='千年树妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 23, 'combat_monster_狮鹫', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='狮鹫'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 23, 'combat_monster_黑风老妖', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='黑风老妖'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 23, 'combat_monster_蜚廉', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='蜚廉'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 23, 'combat_monster_幽冥骑士', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='幽冥骑士'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 23, 'combat_monster_雷鹰', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='雷鹰'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 24, 'combat_monster_幽冥骑士', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='幽冥骑士'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 24, 'combat_monster_蜚廉', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='蜚廉'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 24, 'combat_monster_蛟龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='蛟龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 24, 'combat_monster_山鬼', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='山鬼'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 24, 'combat_monster_九尾妖狐', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='九尾妖狐'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 24, 'combat_monster_天罗蛛', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='天罗蛛'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 25, 'combat_monster_天罗蛛', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='天罗蛛'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 25, 'combat_monster_蛟龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='蛟龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 25, 'combat_monster_火凤雏', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='火凤雏'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 25, 'combat_monster_山鬼', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='山鬼'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 25, 'combat_monster_蜚廉', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='蜚廉'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 25, 'combat_monster_梼杌', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='梼杌'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 27, 'combat_monster_夔牛', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='夔牛'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 27, 'combat_monster_梼杌', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='梼杌'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 27, 'combat_monster_金乌', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='金乌'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 27, 'combat_monster_旱魃', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='旱魃'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 27, 'combat_monster_白泽', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='白泽'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 27, 'combat_monster_烛龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='烛龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 28, 'combat_monster_夔牛', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='夔牛'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 28, 'combat_monster_金乌', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='金乌'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 28, 'combat_monster_烛龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='烛龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 28, 'combat_monster_梼杌', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='梼杌'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 28, 'combat_monster_天魔王', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='天魔王'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 28, 'combat_monster_麒麟', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='麒麟'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 29, 'combat_monster_相柳', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='相柳'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 29, 'combat_monster_刑天', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='刑天'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 29, 'combat_monster_毕方', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='毕方'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 29, 'combat_monster_应龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='应龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 29, 'combat_monster_守鹤', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='守鹤'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 30, 'combat_monster_旱魃', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='旱魃'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 30, 'combat_monster_白泽', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='白泽'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 30, 'combat_monster_梼杌', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='梼杌'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 30, 'combat_monster_烛龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='烛龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 30, 'combat_monster_相柳', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='相柳'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 30, 'combat_monster_毕方', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='毕方'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 31, 'combat_monster_应龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='应龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 31, 'combat_monster_刑天', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='刑天'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 31, 'combat_monster_相柳', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='相柳'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 31, 'combat_monster_守鹤', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='守鹤'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 31, 'combat_monster_鲲鹏', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 32, 'combat_monster_白泽', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='白泽'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 32, 'combat_monster_天魔王', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='天魔王'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 32, 'combat_monster_麒麟', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='麒麟'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 32, 'combat_monster_应龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='应龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 32, 'combat_monster_刑天', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='刑天'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 32, 'combat_monster_鲲鹏', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 33, 'combat_monster_毕方', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='毕方'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 33, 'combat_monster_应龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='应龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 33, 'combat_monster_守鹤', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='守鹤'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 33, 'combat_monster_神龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='神龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 33, 'combat_monster_阎罗天子', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='阎罗天子'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 34, 'combat_monster_刑天', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='刑天'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 34, 'combat_monster_应龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='应龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 34, 'combat_monster_毕方', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='毕方'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 34, 'combat_monster_相柳', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='相柳'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 34, 'combat_monster_鲲鹏', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 34, 'combat_monster_神龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='神龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 35, 'combat_monster_刑天', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='刑天'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 35, 'combat_monster_鲲鹏', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 35, 'combat_monster_阎罗天子', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='阎罗天子'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 35, 'combat_monster_相柳', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='相柳'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 35, 'combat_monster_守鹤', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='守鹤'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 35, 'combat_monster_神龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='神龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 36, 'combat_monster_毕方', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='毕方'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 36, 'combat_monster_应龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='应龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 36, 'combat_monster_刑天', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='刑天'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 36, 'combat_monster_鲲鹏', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 36, 'combat_monster_神龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='神龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 36, 'combat_monster_原始天魔', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='原始天魔'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 37, 'combat_monster_阎罗天子', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='阎罗天子'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 37, 'combat_monster_鲲鹏', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 37, 'combat_monster_神龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='神龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 37, 'combat_monster_守鹤', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='守鹤'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 37, 'combat_monster_应龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='应龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 37, 'combat_monster_原始天魔', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='原始天魔'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 38, 'combat_monster_应龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='应龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 38, 'combat_monster_毕方', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='毕方'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 38, 'combat_monster_鲲鹏', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 38, 'combat_monster_阎罗天子', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='阎罗天子'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 38, 'combat_monster_相柳', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='相柳'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 39, 'combat_monster_刑天', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='刑天'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 39, 'combat_monster_鲲鹏', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 39, 'combat_monster_守鹤', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='守鹤'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 39, 'combat_monster_阎罗天子', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='阎罗天子'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 39, 'combat_monster_原始天魔', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='原始天魔'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 40, 'combat_monster_鲲鹏', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 40, 'combat_monster_阎罗天子', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='阎罗天子'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 40, 'combat_monster_神龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='神龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 40, 'combat_monster_原始天魔', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='原始天魔'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 41, 'combat_monster_阎罗天子', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='阎罗天子'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 41, 'combat_monster_原始天魔', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='原始天魔'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 43, 'combat_monster_守鹤', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='守鹤'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 43, 'combat_monster_阎罗天子', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='阎罗天子'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 43, 'combat_monster_神龙', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='神龙'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 43, 'combat_monster_原始天魔', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='原始天魔'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 43, 'combat_monster_鲲鹏', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='鲲鹏'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 44, 'combat_monster_原始天魔', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='原始天魔'), 'min_count', 1, 'max_count', 1));

-- 混沌 COMBAT events (level 96, 上古四凶)
INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 41, 'combat_monster_混沌', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='混沌'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 43, 'combat_monster_混沌', 'COMBAT', 40,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='混沌'), 'min_count', 1, 'max_count', 1));

INSERT INTO xt_activity_event (activity_type, owner_id, code, event_type, weight, params) VALUES
('TRAINING', 44, 'combat_monster_混沌', 'COMBAT', 80,
 jsonb_build_object('monster_template_id', (SELECT id FROM xt_monster_template WHERE name='混沌'), 'min_count', 1, 'max_count', 1));
