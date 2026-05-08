-- =====================================================
-- 事件类型 + 活动事件关联 种子数据
-- xt_event_type + xt_activity_event
-- =====================================================

-- ============ xt_event_type (事件类型定义) ============
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
('TRAINING', 'training_rare_herb_found', '发现珍稀药草', '在历练时偶然发现了一株罕见的药草，运气不错！'),
('TRAINING', 'training_monster_swarm', '遭遇妖兽群', '一群妖兽突然从四面八方冒出来，数量比预想的多一倍。'),
('TRAINING', 'training_ancient_ruins', '发现远古遗迹', '在历练地发现了一个此前不为人知的远古遗迹入口。'),
('TRAINING', 'training_meditation_epiphany', '静坐顿悟', '在历练中突有所感，就地打坐悟出了些许天道碎片。'),
('TRAINING', 'training_rival_encounter', '遇到竞争对手', '另一个修士也在同一片区域历练——他好像不太友善。'),
('TRAINING', 'training_buried_treasure', '挖出宝贝', '地上有一块松土，挖开发现不知是谁埋的宝贝。'),
('TRAINING', 'training_strange_stone', '奇石异象', '一块石头在你靠近时发出了微弱的光芒——它不普通。'),
('TRAINING', 'training_evil_presence', '邪气侵体', '突然感到一阵莫名的寒意——这片区域有邪物作祟。'),
('TRAINING', 'training_spirit_guide', '灵体引路', '一个友善的灵体出现在面前，它似乎想带你去什么地方。'),
('TRAINING', 'training_qi_storm', '灵气风暴', '灵气突然狂暴起来形成了风暴——站住了别被吹走修炼就在风暴眼。'),
('TRAINING', 'training_beast_den_found', '发现妖兽巢穴', '一个妖兽的巢穴——里面可能有幼崽或蛋。'),
-- BOUNTY_SIDE 事件
('BOUNTY_SIDE', 'bounty_clue_found', '找到悬赏线索', '在执行悬赏过程中发现了额外的线索——任务可能比想象的复杂。'),
('BOUNTY_SIDE', 'bounty_betrayal', '委托人的背叛', '委托人其实是个骗子——他的真实目的另有隐情。'),
('BOUNTY_SIDE', 'bounty_extra_target', '额外目标出现', '悬赏目标之外竟然还有一个副目标，价值也不低——顺手做掉？'),
('BOUNTY_SIDE', 'bounty_hidden_cache', '发现秘密藏宝', '追踪悬赏目标时无意中发现了他们的隐秘藏宝库。'),
('BOUNTY_SIDE', 'bounty_witness_arrives', '目击者登场', '一个自称是目击者的NPC出现，带来了关于悬赏目标的新情报。'),
('BOUNTY_SIDE', 'bounty_rival_hunter', '竞争者登场', '另一个赏金猎人也盯上了你的目标——看谁先得手。'),
('BOUNTY_SIDE', 'bounty_monster_interference', '妖兽搅局', '正追着目标呢突然窜出一只妖兽打乱了局面。'),
('BOUNTY_SIDE', 'bounty_cultivation_boost', '悬赏中的顿悟', '在执行悬赏的激烈战斗中竟意外地突破了修为瓶颈。');

-- ============ xt_activity_event (活动事件关联) ============
-- TRAVEL events (owner_id = map_id)
INSERT INTO xt_activity_event (activity_type, owner_id, code, weight, is_hidden, trigger_type, trigger_params, params) VALUES
('TRAVEL', 1, 'travel_ambush_beast', 20, false, NULL, '{}', '{}'),
('TRAVEL', 1, 'travel_broken_cart', 15, false, NULL, '{}', '{"item_chance":0.3,"items":["灵芝","玄铁矿石"]}'),
('TRAVEL', 1, 'travel_friendly_merchant', 10, false, NULL, '{}', '{}'),
('TRAVEL', 2, 'travel_strange_fog', 15, false, NULL, '{}', '{}'),
('TRAVEL', 2, 'travel_injured_cultivator', 10, false, NULL, '{}', '{"reward_exp":50}'),
('TRAVEL', 3, 'travel_fallen_star', 10, false, NULL, '{}', '{"item":"玄铁矿石","count":1}'),
('TRAVEL', 4, 'travel_spring_of_spirit', 12, false, NULL, '{}', '{"buff_type":"heal","amount":100}'),
('TRAVEL', 4, 'travel_broken_cart', 8, false, NULL, '{}', '{}'),
('TRAVEL', 5, 'travel_rainstorm', 15, false, NULL, '{}', '{}'),
('TRAVEL', 6, 'travel_friendly_merchant', 15, false, NULL, '{}', '{}'),
('TRAVEL', 6, 'travel_wandering_elder', 5, false, 'STAT_THRESHOLD', '{"stat":"WIS","min":20}', '{}'),
('TRAVEL', 7, 'travel_treasure_map', 8, false, NULL, '{}', '{}'),
('TRAVEL', 8, 'travel_strange_fog', 15, false, NULL, '{}', '{}'),
('TRAVEL', 10, 'travel_bandit_roadblock', 15, false, NULL, '{}', '{}'),
('TRAVEL', 11, 'travel_friendly_merchant', 12, false, NULL, '{}', '{}'),
('TRAVEL', 11, 'travel_wandering_elder', 6, false, 'STAT_THRESHOLD', '{"stat":"WIS","min":45}', '{}'),
('TRAVEL', 17, 'travel_strange_fog', 20, false, NULL, '{}', '{}'),
('TRAVEL', 24, 'travel_spring_of_spirit', 10, false, NULL, '{}', '{"buff_type":"heal","amount":500}'),
('TRAVEL', 27, 'travel_fallen_star', 12, false, NULL, '{}', '{"item":"天外陨铁","count":1}'),
('TRAVEL', 30, 'travel_fallen_star', 15, false, NULL, '{}', '{"item":"星辰石","count":1}'),
('TRAVEL', 35, 'travel_strange_fog', 15, false, 'HAS_ITEM', '{"item_name":"魂玉碎片"}', '{}'),
('TRAVEL', 39, 'travel_fallen_star', 8, false, NULL, '{}', '{}'),
('TRAVEL', 42, 'travel_wandering_elder', 10, false, NULL, '{}', '{"wisdom_buff":50}');

-- TRAINING events (owner_id = map_id)
INSERT INTO xt_activity_event (activity_type, owner_id, code, weight, is_hidden, trigger_type, trigger_params, params) VALUES
('TRAINING', 2, 'training_rare_herb_found', 15, false, NULL, '{}', '{"herb":"灵芝","min":2,"max":4}'),
('TRAINING', 2, 'training_monster_swarm', 15, false, NULL, '{}', '{"extra_monster_count":2}'),
('TRAINING', 3, 'training_buried_treasure', 10, false, NULL, '{}', '{"item":"玄铁矿石","count":2}'),
('TRAINING', 3, 'training_strange_stone', 8, false, NULL, '{}', '{"item":"赤铜矿","count":1}'),
('TRAINING', 4, 'training_qi_storm', 10, false, NULL, '{}', '{"exp_boost":50}'),
('TRAINING', 5, 'training_rival_encounter', 10, false, NULL, '{}', '{}'),
('TRAINING', 7, 'training_rare_herb_found', 12, false, NULL, '{}', '{"herb":"地火芝","min":1,"max":3}'),
('TRAINING', 8, 'training_evil_presence', 12, false, NULL, '{}', '{}'),
('TRAINING', 9, 'training_ancient_ruins', 15, false, NULL, '{}', '{}'),
('TRAINING', 12, 'training_monster_swarm', 15, false, NULL, '{}', '{"extra_monster_count":3}'),
('TRAINING', 12, 'training_beast_den_found', 10, false, NULL, '{}', '{}'),
('TRAINING', 14, 'training_meditation_epiphany', 15, false, NULL, '{}', '{"exp_boost":200}'),
('TRAINING', 15, 'training_ancient_ruins', 12, false, 'HAS_SKILL', '{"skill_name":"青莲剑歌"}', '{"extra_loot":true}'),
('TRAINING', 16, 'training_rare_herb_found', 15, false, NULL, '{}', '{"herb":"地火芝","min":2,"max":5}'),
('TRAINING', 20, 'training_strange_stone', 10, false, NULL, '{}', '{"item":"紫金砂","count":2}'),
('TRAINING', 23, 'training_spirit_guide', 10, false, NULL, '{}', '{}'),
('TRAINING', 27, 'training_evil_presence', 15, false, NULL, '{}', '{}'),
('TRAINING', 29, 'training_meditation_epiphany', 15, false, NULL, '{}', '{"exp_boost":500}'),
('TRAINING', 32, 'training_qi_storm', 20, false, NULL, '{}', '{"exp_boost":300}'),
('TRAINING', 34, 'training_meditation_epiphany', 20, false, NULL, '{}', '{"exp_boost":800}'),
('TRAINING', 38, 'training_spirit_guide', 12, false, NULL, '{}', '{}'),
('TRAINING', 39, 'training_meditation_epiphany', 15, false, NULL, '{}', '{"exp_boost":1000}'),
('TRAINING', 41, 'training_qi_storm', 15, false, NULL, '{}', '{"exp_boost":500}');

-- Hidden TRAVEL events
INSERT INTO xt_activity_event (activity_type, owner_id, code, weight, is_hidden, trigger_type, trigger_params, params) VALUES
('TRAVEL', 9, 'travel_treasure_map', 10, true, 'HAS_ITEM', '{"item_name":"青云令牌"}', '{"hidden":"青云密道已现"}'),
('TRAVEL', 14, 'travel_wandering_elder', 8, true, 'STAT_THRESHOLD', '{"stat":"WIS","min":50}', '{"item":"大悟道丹","count":1}'),
('TRAVEL', 28, 'travel_spring_of_spirit', 10, true, 'HAS_ITEM', '{"item_name":"九转金莲"}', '{}'),
('TRAVEL', 33, 'travel_bandit_roadblock', 10, true, 'HAS_SKILL', '{"skill_name":"刑天斧法"}', '{}'),
('TRAVEL', 42, 'travel_fallen_star', 8, true, 'STAT_THRESHOLD', '{"stat":"STR","min":90}', '{"item":"神阶进化石","count":1}');

-- Hidden TRAINING events
INSERT INTO xt_activity_event (activity_type, owner_id, code, weight, is_hidden, trigger_type, trigger_params, params) VALUES
('TRAINING', 29, 'training_spirit_guide', 8, true, 'HAS_SKILL', '{"skill_name":"一气化三清"}', '{"item":"封神榜碎片","count":1}'),
('TRAINING', 31, 'training_rare_herb_found', 10, true, 'HAS_ITEM', '{"item_name":"紫府秘匙"}', '{"item":"九天仙草","count":1}'),
('TRAINING', 40, 'training_meditation_epiphany', 8, true, 'STAT_THRESHOLD', '{"stat":"WIS","min":85}', '{"exp_boost":5000}'),
('TRAINING', 44, 'training_ancient_ruins', 5, true, 'HAS_SKILL', '{"skill_name":"轩辕剑法"}', '{"item":"轩辕剑","count":1}');

-- BOUNTY_SIDE events (owner_id = bounty_id, using codes from event_type)
INSERT INTO xt_activity_event (activity_type, owner_id, code, weight, is_hidden, trigger_type, trigger_params, params) VALUES
('BOUNTY_SIDE', 1, 'bounty_clue_found', 20, false, NULL, '{}', '{}'),
('BOUNTY_SIDE', 5, 'bounty_monster_interference', 15, false, NULL, '{}', '{}'),
('BOUNTY_SIDE', 10, 'bounty_extra_target', 12, false, NULL, '{}', '{}'),
('BOUNTY_SIDE', 15, 'bounty_hidden_cache', 10, false, NULL, '{}', '{"item":"聚灵丹","count":3}'),
('BOUNTY_SIDE', 20, 'bounty_rival_hunter', 12, false, NULL, '{}', '{}'),
('BOUNTY_SIDE', 25, 'bounty_witness_arrives', 10, false, NULL, '{}', '{}'),
('BOUNTY_SIDE', 30, 'bounty_betrayal', 8, true, NULL, '{}', '{}'),
('BOUNTY_SIDE', 40, 'bounty_clue_found', 15, false, NULL, '{}', '{}'),
('BOUNTY_SIDE', 50, 'bounty_monster_interference', 15, false, NULL, '{}', '{}'),
('BOUNTY_SIDE', 60, 'bounty_cultivation_boost', 10, false, NULL, '{}', '{"exp_boost":100}'),
('BOUNTY_SIDE', 70, 'bounty_extra_target', 12, false, NULL, '{}', '{}'),
('BOUNTY_SIDE', 80, 'bounty_hidden_cache', 10, false, NULL, '{}', '{}'),
('BOUNTY_SIDE', 100, 'bounty_betrayal', 8, true, NULL, '{}', '{"reward_boost":2}'),
('BOUNTY_SIDE', 120, 'bounty_rival_hunter', 12, false, NULL, '{}', '{}'),
('BOUNTY_SIDE', 150, 'bounty_witness_arrives', 10, false, NULL, '{}', '{}'),
('BOUNTY_SIDE', 200, 'bounty_cultivation_boost', 8, false, NULL, '{}', '{"exp_boost":300}'),
('BOUNTY_SIDE', 250, 'bounty_clue_found', 15, false, NULL, '{}', '{}'),
('BOUNTY_SIDE', 300, 'bounty_monster_interference', 12, false, NULL, '{}', '{}'),
('BOUNTY_SIDE', 350, 'bounty_extra_target', 10, false, NULL, '{}', '{}'),
('BOUNTY_SIDE', 400, 'bounty_cultivation_boost', 5, true, NULL, '{}', '{"exp_boost":1000}');
