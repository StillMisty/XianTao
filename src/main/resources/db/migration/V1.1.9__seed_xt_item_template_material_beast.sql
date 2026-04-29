-- 材料 —— 灵兽产出 (id 165-183)
INSERT INTO xt_item_template (id, name, type, rarity, properties, tags, description, max_stack) VALUES
-- T1 灵兽产出 (165-168)
(165, '灵狐毛皮', 'MATERIAL', 'COMMON', '{}'::jsonb, '["beast_material","common"]'::jsonb, '灵狐自然脱落的毛皮，蕴含微弱灵气', 99),
(166, '灵鹤翎羽', 'MATERIAL', 'COMMON', '{}'::jsonb, '["beast_material","common"]'::jsonb, '灵鹤脱落的翎羽，轻若无物', 99),
(167, '苍狼牙', 'MATERIAL', 'COMMON', '{}'::jsonb, '["beast_material","common"]'::jsonb, '苍狼脱落的利牙', 99),
(168, '蜂鸟晶羽', 'MATERIAL', 'COMMON', '{}'::jsonb, '["beast_material","wind","common"]'::jsonb, '蜂鸟的晶羽，色彩斑斓', 99),
-- T2 灵兽产出 (169-173)
(169, '火麟鳞片', 'MATERIAL', 'RARE', '{}'::jsonb, '["beast_material","fire","rare"]'::jsonb, '火麒麟脱落的神鳞，蕴含火属灵气', 50),
(170, '玄龟甲片', 'MATERIAL', 'RARE', '{}'::jsonb, '["beast_material","water","rare"]'::jsonb, '玄龟自然脱落的甲壳碎片，极为坚韧', 50),
(171, '雷鹰翎羽', 'MATERIAL', 'RARE', '{}'::jsonb, '["beast_material","thunder","rare"]'::jsonb, '雷鹰脱落的翎羽，触及之时有酥麻之感', 50),
(172, '金鹏翎羽', 'MATERIAL', 'RARE', '{}'::jsonb, '["beast_material","wind","rare"]'::jsonb, '金鹏脱落的翎羽，金光闪烁', 50),
(173, '银狐尾', 'MATERIAL', 'RARE', '{}'::jsonb, '["beast_material","moon","rare"]'::jsonb, '银狐脱落的尾巴，月光之下熠熠生辉', 30),
-- T3 灵兽产出 (174-177)
(174, '冰凤羽毛', 'MATERIAL', 'EPIC', '{}'::jsonb, '["beast_material","ice","epic"]'::jsonb, '冰凤自然脱落的羽毛，寒气逼人', 30),
(175, '龙血草精华', 'MATERIAL', 'EPIC', '{}'::jsonb, '["beast_material","dragon","epic"]'::jsonb, '从龙血草中提炼的精华，可用于灵兽进阶', 20),
(176, '白虎皮毛', 'MATERIAL', 'EPIC', '{}'::jsonb, '["beast_material","metal","epic"]'::jsonb, '白虎自然脱落的皮毛，坚不可摧', 20),
(177, '玄武甲壳', 'MATERIAL', 'EPIC', '{}'::jsonb, '["beast_material","water","epic"]'::jsonb, '玄武的甲壳碎片，能抵御水火', 20),
-- T4 灵兽产出 (178-180)
(178, '金龙鳞', 'MATERIAL', 'LEGENDARY', '{}'::jsonb, '["beast_material","gold","legendary"]'::jsonb, '金龙脱落的鳞片，蕴含龙脉之气', 20),
(179, '朱雀翎羽', 'MATERIAL', 'LEGENDARY', '{}'::jsonb, '["beast_material","fire","legendary"]'::jsonb, '朱雀的翎羽，永不熄灭', 10),
(180, '青龙逆鳞', 'MATERIAL', 'LEGENDARY', '{}'::jsonb, '["beast_material","wood","legendary"]'::jsonb, '青龙的逆鳞，蕴含生生不息之力', 10),
-- 新增 T1 灵兽产出 (181-183)
(181, '灵猫爪', 'MATERIAL', 'COMMON', '{}'::jsonb, '["beast_material","common"]'::jsonb, '灵猫脱落的利爪，可用于炼器', 99),
(182, '灵蛇皮', 'MATERIAL', 'COMMON', '{}'::jsonb, '["beast_material","common"]'::jsonb, '灵蛇蜕下的皮膜，极为柔韧', 99),
(183, '灵鹿茸', 'MATERIAL', 'COMMON', '{}'::jsonb, '["beast_material","common"]'::jsonb, '灵鹿自然脱落的鹿茸，蕴含生机', 99);
