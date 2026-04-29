-- 灵兽卵
INSERT INTO xt_item_template (name, type, rarity, properties, tags, description, max_stack) VALUES
-- T1 灵兽卵 (普通)
INSERT INTO xt_item_template (name, type, rarity, properties, tags, description, max_stack) VALUES
('灵狐卵', 'BEAST_EGG', 'COMMON', '{"grow_time":72,"yields":["灵狐毛皮"],"survive_rate":80}', '["beast_egg","common"]'::jsonb, '孵化后可获得灵狐，等阶T1，需72小时', 10),
('灵鹤卵', 'BEAST_EGG', 'COMMON', '{"grow_time":72,"yields":["灵鹤翎羽"],"survive_rate":80}', '["beast_egg","common"]'::jsonb, '孵化后可获得灵鹤，等阶T1，需72小时', 10),
('苍狼卵', 'BEAST_EGG', 'COMMON', '{"grow_time":72,"yields":["苍狼牙"],"survive_rate":80}', '["beast_egg","common"]'::jsonb, '孵化后可获得苍狼，等阶T1，需72小时', 10),
('蜂鸟卵', 'BEAST_EGG', 'COMMON', '{"grow_time":72,"yields":["蜂鸟晶羽"],"survive_rate":85}', '["beast_egg","wind","common"]'::jsonb, '孵化后可获得蜂鸟，等阶T1，需72小时', 10),
('灵猫卵', 'BEAST_EGG', 'COMMON', '{"grow_time":72,"yields":["灵猫爪"],"survive_rate":80}', '["beast_egg","common"]'::jsonb, '孵化后可获得灵猫，等阶T1，需72小时', 10),
('灵蛇卵', 'BEAST_EGG', 'COMMON', '{"grow_time":72,"yields":["灵蛇皮"],"survive_rate":80}', '["beast_egg","common"]'::jsonb, '孵化后可获得灵蛇，等阶T1，需72小时', 10),
('灵鹿卵', 'BEAST_EGG', 'COMMON', '{"grow_time":96,"yields":["灵鹿茸"],"survive_rate":75}', '["beast_egg","common"]'::jsonb, '孵化后可获得灵鹿，等阶T1，需96小时', 10),
('灵鼠卵', 'BEAST_EGG', 'COMMON', '{"grow_time":48,"yields":["灵狐毛皮"],"survive_rate":90}', '["beast_egg","common"]'::jsonb, '孵化后可获得灵鼠，等阶T1，需48小时', 10),
('灵蜂卵', 'BEAST_EGG', 'COMMON', '{"grow_time":60,"yields":["野蜂蜜"],"survive_rate":85}', '["beast_egg","common"]'::jsonb, '孵化后可获得灵蜂，等阶T1，需60小时', 10),
('灵雀卵', 'BEAST_EGG', 'COMMON', '{"grow_time":72,"yields":["灵鹤翎羽"],"survive_rate":80}', '["beast_egg","wind","common"]'::jsonb, '孵化后可获得灵雀，等阶T1，需72小时', 10),
('灵羊卵', 'BEAST_EGG', 'COMMON', '{"grow_time":96,"yields":["灵鹿茸"],"survive_rate":75}', '["beast_egg","common"]'::jsonb, '孵化后可获得灵羊，等阶T1，需96小时', 10);

-- T2 灵兽卵 (稀有)
INSERT INTO xt_item_template (name, type, rarity, properties, tags, description, max_stack) VALUES
('火麟卵', 'BEAST_EGG', 'RARE', '{"grow_time":96,"yields":["火麟鳞片"],"survive_rate":65}', '["beast_egg","fire","rare"]'::jsonb, '孵化后可获得火麒麟，等阶T2，需96小时', 5),
('玄龟卵', 'BEAST_EGG', 'RARE', '{"grow_time":96,"yields":["玄龟甲片"],"survive_rate":65}', '["beast_egg","water","rare"]'::jsonb, '孵化后可获得玄龟，等阶T2，需96小时', 5),
('雷鹰卵', 'BEAST_EGG', 'RARE', '{"grow_time":120,"yields":["雷鹰翎羽"],"survive_rate":60}', '["beast_egg","thunder","rare"]'::jsonb, '孵化后可获得雷鹰，等阶T2，需120小时', 5),
('金鹏卵', 'BEAST_EGG', 'RARE', '{"grow_time":96,"yields":["金鹏翎羽"],"survive_rate":60}', '["beast_egg","wind","rare"]'::jsonb, '孵化后可获得金鹏，等阶T2，需96小时', 5),
('银狐卵', 'BEAST_EGG', 'RARE', '{"grow_time":120,"yields":["银狐尾"],"survive_rate":55}', '["beast_egg","moon","rare"]'::jsonb, '孵化后可获得银狐，等阶T2，需120小时', 5),
('炎狼卵', 'BEAST_EGG', 'RARE', '{"grow_time":96,"yields":["炎狼牙"],"survive_rate":60}', '["beast_egg","fire","rare"]'::jsonb, '孵化后可获得炎狼，等阶T2，需96小时', 5),
('玄蛇卵', 'BEAST_EGG', 'RARE', '{"grow_time":120,"yields":["玄蛇鳞"],"survive_rate":55}', '["beast_egg","dark","rare"]'::jsonb, '孵化后可获得玄蛇，等阶T2，需120小时', 5),
('风貂卵', 'BEAST_EGG', 'RARE', '{"grow_time":96,"yields":["风貂尾"],"survive_rate":60}', '["beast_egg","wind","rare"]'::jsonb, '孵化后可获得风貂，等阶T2，需96小时', 5),
('金蟾卵', 'BEAST_EGG', 'RARE', '{"grow_time":120,"yields":["金蟾珠"],"survive_rate":55}', '["beast_egg","gold","rare"]'::jsonb, '孵化后可获得金蟾，等阶T2，需120小时', 5);

-- T3 灵兽卵 (史诗)
INSERT INTO xt_item_template (name, type, rarity, properties, tags, description, max_stack) VALUES
('冰凤卵', 'BEAST_EGG', 'EPIC', '{"grow_time":192,"yields":["冰凤羽毛"],"survive_rate":35}', '["beast_egg","ice","epic"]'::jsonb, '孵化后可获得冰凤，等阶T3，需192小时', 3),
('白虎卵', 'BEAST_EGG', 'EPIC', '{"grow_time":168,"yields":["白虎皮毛"],"survive_rate":35}', '["beast_egg","metal","epic"]'::jsonb, '孵化后可获得白虎，等阶T3，需168小时', 3),
('玄武卵', 'BEAST_EGG', 'EPIC', '{"grow_time":168,"yields":["玄武甲壳"],"survive_rate":35}', '["beast_egg","water","epic"]'::jsonb, '孵化后可获得玄武，等阶T3，需168小时', 3),
('火凤卵', 'BEAST_EGG', 'EPIC', '{"grow_time":192,"yields":["冰凤泪"],"survive_rate":35}', '["beast_egg","fire","epic"]'::jsonb, '孵化后可获得火凤，等阶T3，需192小时', 3),
('青鸾卵', 'BEAST_EGG', 'EPIC', '{"grow_time":168,"yields":["青鸾羽"],"survive_rate":35}', '["beast_egg","wind","epic"]'::jsonb, '孵化后可获得青鸾，等阶T3，需168小时', 3),
('麒麟卵', 'BEAST_EGG', 'EPIC', '{"grow_time":192,"yields":["麒麟角"],"survive_rate":30}', '["beast_egg","five_elements","epic"]'::jsonb, '孵化后可获得麒麟，等阶T3，需192小时', 3);

-- T4 灵兽卵 (传说)
INSERT INTO xt_item_template (name, type, rarity, properties, tags, description, max_stack) VALUES
('金龙卵', 'BEAST_EGG', 'LEGENDARY', '{"grow_time":168,"yields":["金龙鳞"],"survive_rate":30}', '["beast_egg","gold","legendary"]'::jsonb, '孵化后可获得金龙，等阶T4，需168小时', 1),
('朱雀卵', 'BEAST_EGG', 'LEGENDARY', '{"grow_time":240,"yields":["朱雀翎羽"],"survive_rate":20}', '["beast_egg","fire","legendary"]'::jsonb, '孵化后可获得朱雀，等阶T4，需240小时', 1),
('青龙卵', 'BEAST_EGG', 'LEGENDARY', '{"grow_time":240,"yields":["青龙逆鳞"],"survive_rate":20}', '["beast_egg","wood","legendary"]'::jsonb, '孵化后可获得青龙，等阶T4，需240小时', 1),
('九尾灵狐卵', 'BEAST_EGG', 'LEGENDARY', '{"grow_time":240,"yields":["九尾灵狐尾"],"survive_rate":20}', '["beast_egg","beast","legendary"]'::jsonb, '孵化后可获得九尾灵狐，等阶T4，需240小时', 1),
('应龙卵', 'BEAST_EGG', 'LEGENDARY', '{"grow_time":240,"yields":["应龙鳞"],"survive_rate":15}', '["beast_egg","dragon","legendary"]'::jsonb, '孵化后可获得应龙，等阶T4，需240小时', 1);
