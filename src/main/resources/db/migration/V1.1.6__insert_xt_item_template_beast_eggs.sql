-- 灵兽卵（id 116-164）
-- 旧版灵蛋 (116-127)
INSERT INTO xt_item_template (id, name, type, rarity, properties, tags, description, max_stack) VALUES
(116, '火灵蛋', 'BEAST_EGG', 'RARE', '{"grow_time":72,"yields":["火灵兽"],"survive_rate":70}', '["egg","fire","rare"]'::jsonb, '孵化后可获得火属性灵宠，需72小时', 10),
(117, '玄冰龟蛋', 'BEAST_EGG', 'RARE', '{"grow_time":72,"yields":["玄冰龟"],"survive_rate":70}', '["egg","water","rare"]'::jsonb, '孵化后可获得水属性灵宠，需72小时', 10),
(118, '土灵蛋', 'BEAST_EGG', 'RARE', '{"grow_time":72,"yields":["土灵熊"],"survive_rate":70}', '["egg","earth","rare"]'::jsonb, '孵化后可获得土属性灵宠，需72小时', 10),
(119, '玉兔蛋', 'BEAST_EGG', 'RARE', '{"grow_time":96,"yields":["玉兔"],"survive_rate":60}', '["egg","moon","rare"]'::jsonb, '孵化后可获得玉兔灵宠，需96小时', 5),
(120, '风灵蛋', 'BEAST_EGG', 'EPIC', '{"grow_time":96,"yields":["风鹰"],"survive_rate":60}', '["egg","wind","epic"]'::jsonb, '孵化后可获得风属性灵宠，需96小时', 5),
(121, '雷鹰蛋', 'BEAST_EGG', 'EPIC', '{"grow_time":120,"yields":["雷鹰"],"survive_rate":50}', '["egg","thunder","epic"]'::jsonb, '孵化后可获得雷属性灵宠雷鹰，需120小时', 3),
(122, '暗影蛋', 'BEAST_EGG', 'EPIC', '{"grow_time":120,"yields":["暗影蛇"],"survive_rate":45}', '["egg","dark","epic"]'::jsonb, '孵化后可获得暗属性灵宠，需120小时', 3),
(123, '光灵蛋', 'BEAST_EGG', 'EPIC', '{"grow_time":120,"yields":["光鹿"],"survive_rate":45}', '["egg","light","epic"]'::jsonb, '孵化后可获得光属性灵宠，需120小时', 3),
(124, '炎狮蛋', 'BEAST_EGG', 'EPIC', '{"grow_time":144,"yields":["炎狮"],"survive_rate":40}', '["egg","fire","epic"]'::jsonb, '孵化后可获得炎狮灵宠，需144小时', 3),
(125, '金龙蛋', 'BEAST_EGG', 'LEGENDARY', '{"grow_time":168,"yields":["金龙"],"survive_rate":30}', '["egg","gold","legendary"]'::jsonb, '孵化后可获得金龙灵宠，需168小时', 1),
(126, '冰凤蛋', 'BEAST_EGG', 'LEGENDARY', '{"grow_time":192,"yields":["冰凤"],"survive_rate":25}', '["egg","ice","legendary"]'::jsonb, '孵化后可获得冰凤灵宠，需192小时', 1),
(127, '毒龙蛋', 'BEAST_EGG', 'LEGENDARY', '{"grow_time":192,"yields":["毒龙"],"survive_rate":20}', '["egg","poison","legendary"]'::jsonb, '孵化后可获得毒龙灵宠，需192小时', 1);

-- T1-T4 灵兽卵 (147-164); T1=普通, T2=稀有, T3=史诗, T4=传说
INSERT INTO xt_item_template (id, name, type, rarity, properties, tags, description, max_stack) VALUES
-- T1 (147-150, 162-164)
(147, '灵狐卵', 'BEAST_EGG', 'COMMON', '{"grow_time":72,"yields":["灵狐毛皮"],"survive_rate":80}', '["beast_egg","common"]'::jsonb, '孵化后可获得灵狐，等阶T1，需72小时', 10),
(148, '灵鹤卵', 'BEAST_EGG', 'COMMON', '{"grow_time":72,"yields":["灵鹤翎羽"],"survive_rate":80}', '["beast_egg","common"]'::jsonb, '孵化后可获得灵鹤，等阶T1，需72小时', 10),
(149, '苍狼卵', 'BEAST_EGG', 'COMMON', '{"grow_time":72,"yields":["苍狼牙"],"survive_rate":80}', '["beast_egg","common"]'::jsonb, '孵化后可获得苍狼，等阶T1，需72小时', 10),
(150, '蜂鸟卵', 'BEAST_EGG', 'COMMON', '{"grow_time":72,"yields":["蜂鸟晶羽"],"survive_rate":85}', '["beast_egg","wind","common"]'::jsonb, '孵化后可获得蜂鸟，等阶T1，需72小时', 10),
-- T2 (151-155)
(151, '火麟卵', 'BEAST_EGG', 'RARE', '{"grow_time":96,"yields":["火麟鳞片"],"survive_rate":65}', '["beast_egg","fire","rare"]'::jsonb, '孵化后可获得火麒麟，等阶T2，需96小时', 5),
(152, '玄龟卵', 'BEAST_EGG', 'RARE', '{"grow_time":96,"yields":["玄龟甲片"],"survive_rate":65}', '["beast_egg","water","rare"]'::jsonb, '孵化后可获得玄龟，等阶T2，需96小时', 5),
(153, '雷鹰卵', 'BEAST_EGG', 'RARE', '{"grow_time":120,"yields":["雷鹰翎羽"],"survive_rate":60}', '["beast_egg","thunder","rare"]'::jsonb, '孵化后可获得雷鹰，等阶T2，需120小时', 5),
(154, '金鹏卵', 'BEAST_EGG', 'RARE', '{"grow_time":96,"yields":["金鹏翎羽"],"survive_rate":60}', '["beast_egg","wind","rare"]'::jsonb, '孵化后可获得金鹏，等阶T2，需96小时', 5),
(155, '银狐卵', 'BEAST_EGG', 'RARE', '{"grow_time":120,"yields":["银狐尾"],"survive_rate":55}', '["beast_egg","moon","rare"]'::jsonb, '孵化后可获得银狐，等阶T2，需120小时', 5),
-- T3 (156-158)
(156, '冰凤卵', 'BEAST_EGG', 'EPIC', '{"grow_time":192,"yields":["冰凤羽毛"],"survive_rate":35}', '["beast_egg","ice","epic"]'::jsonb, '孵化后可获得冰凤，等阶T3，需192小时', 3),
(157, '白虎卵', 'BEAST_EGG', 'EPIC', '{"grow_time":168,"yields":["白虎皮毛"],"survive_rate":35}', '["beast_egg","metal","epic"]'::jsonb, '孵化后可获得白虎，等阶T3，需168小时', 3),
(158, '玄武卵', 'BEAST_EGG', 'EPIC', '{"grow_time":168,"yields":["玄武甲壳"],"survive_rate":35}', '["beast_egg","water","epic"]'::jsonb, '孵化后可获得玄武，等阶T3，需168小时', 3),
-- T4 (159-161)
(159, '金龙卵', 'BEAST_EGG', 'LEGENDARY', '{"grow_time":168,"yields":["金龙鳞"],"survive_rate":30}', '["beast_egg","gold","legendary"]'::jsonb, '孵化后可获得金龙，等阶T4，需168小时', 1),
(160, '朱雀卵', 'BEAST_EGG', 'LEGENDARY', '{"grow_time":240,"yields":["朱雀翎羽"],"survive_rate":20}', '["beast_egg","fire","legendary"]'::jsonb, '孵化后可获得朱雀，等阶T4，需240小时', 1),
(161, '青龙卵', 'BEAST_EGG', 'LEGENDARY', '{"grow_time":240,"yields":["青龙逆鳞"],"survive_rate":20}', '["beast_egg","wood","legendary"]'::jsonb, '孵化后可获得青龙，等阶T4，需240小时', 1),
-- 新增 T1 (162-164)
(162, '灵猫卵', 'BEAST_EGG', 'COMMON', '{"grow_time":72,"yields":["灵猫爪"],"survive_rate":80}', '["beast_egg","common"]'::jsonb, '孵化后可获得灵猫，等阶T1，需72小时', 10),
(163, '灵蛇卵', 'BEAST_EGG', 'COMMON', '{"grow_time":72,"yields":["灵蛇皮"],"survive_rate":80}', '["beast_egg","common"]'::jsonb, '孵化后可获得灵蛇，等阶T1，需72小时', 10),
(164, '灵鹿卵', 'BEAST_EGG', 'COMMON', '{"grow_time":96,"yields":["灵鹿茸"],"survive_rate":75}', '["beast_egg","common"]'::jsonb, '孵化后可获得灵鹿，等阶T1，需96小时', 10);
