-- T1 灵兽卵 (普通)
INSERT INTO xt_item_template (name, type, properties, tags, description, max_stack) VALUES
('灵狐卵', 'BEAST_EGG', '{"grow_time":72,"yields":["灵狐毛皮"],"survive_rate":80}', '["beast_egg","common"]'::jsonb, '灵狐可产出毛皮，毛茸茸的适合当抱枕', 999),
('灵鹤卵', 'BEAST_EGG', '{"grow_time":72,"yields":["灵鹤翎羽"],"survive_rate":80}', '["beast_egg","common"]'::jsonb, '灵鹤姿态优雅，可惜孵出来之前不知道它会不会跳舞', 999),
('苍狼卵', 'BEAST_EGG', '{"grow_time":72,"yields":["苍狼牙"],"survive_rate":80}', '["beast_egg","common"]'::jsonb, '苍狼利牙可用于炼器——狼本身也挺能看门的', 999),
('蜂鸟卵', 'BEAST_EGG', '{"grow_time":72,"yields":["蜂鸟晶羽"],"survive_rate":85}', '["beast_egg","wind","common"]'::jsonb, '巴掌大的灵鸟，晶羽色彩斑斓——福地里的颜值担当', 999),
('灵猫卵', 'BEAST_EGG', '{"grow_time":72,"yields":["灵猫爪"],"survive_rate":80}', '["beast_egg","common"]'::jsonb, '灵猫高冷且娇贵，但产出爪子还算勤快——猫奴修士必备', 999),
('灵蛇卵', 'BEAST_EGG', '{"grow_time":72,"yields":["灵蛇皮"],"survive_rate":80}', '["beast_egg","common"]'::jsonb, '灵蛇蜕皮即产出，稳定高效——不怕蛇的再考虑', 999),
('灵鹿卵', 'BEAST_EGG', '{"grow_time":96,"yields":["灵鹿茸"],"survive_rate":75}', '["beast_egg","common"]'::jsonb, '灵鹿温顺亲人，鹿茸更是好东西——就是孵化慢了点', 999),
('灵鼠卵', 'BEAST_EGG', '{"grow_time":48,"yields":["灵狐毛皮"],"survive_rate":90}', '["beast_egg","common"]'::jsonb, '48小时速成——修真界的小白鼠，产出效率惊人', 999),
('灵蜂卵', 'BEAST_EGG', '{"grow_time":60,"yields":["野蜂蜜"],"survive_rate":85}', '["beast_egg","common"]'::jsonb, '灵蜂勤劳能干，从此蜂蜜自由——不建议靠太近', 999),
('灵雀卵', 'BEAST_EGG', '{"grow_time":72,"yields":["灵鹤翎羽"],"survive_rate":80}', '["beast_egg","wind","common"]'::jsonb, '灵雀小巧活泼，叫声清脆——福地的天然闹钟', 999),
('灵羊卵', 'BEAST_EGG', '{"grow_time":96,"yields":["灵鹿茸"],"survive_rate":75}', '["beast_egg","common"]'::jsonb, '灵羊性情温和，闷头产出从不抱怨——模范员工', 999);

-- T2 灵兽卵 (稀有)
INSERT INTO xt_item_template (name, type, properties, tags, description, max_stack) VALUES
('火麟卵', 'BEAST_EGG', '{"grow_time":96,"yields":["火麟鳞片"],"survive_rate":65}', '["beast_egg","fire","rare"]'::jsonb, '火麒麟的后裔，鳞片蕴含火属灵气——就是脾气跟火一样大', 999),
('玄龟卵', 'BEAST_EGG', '{"grow_time":96,"yields":["玄龟甲片"],"survive_rate":65}', '["beast_egg","water","rare"]'::jsonb, '玄龟甲壳坚不可摧——但这家伙的行动速度也跟甲壳一样', 999),
('雷鹰卵', 'BEAST_EGG', '{"grow_time":120,"yields":["雷鹰翎羽"],"survive_rate":60}', '["beast_egg","thunder","rare"]'::jsonb, '雷鹰翱翔九天，翎羽带电——孵它之前先做好防雷措施', 999),
('金鹏卵', 'BEAST_EGG', '{"grow_time":96,"yields":["金鹏翎羽"],"survive_rate":60}', '["beast_egg","wind","rare"]'::jsonb, '金鹏展翅九万里——当然养在福地里它飞不了那么远', 999),
('银狐卵', 'BEAST_EGG', '{"grow_time":120,"yields":["银狐尾"],"survive_rate":55}', '["beast_egg","moon","rare"]'::jsonb, '银狐尾巴月光下闪闪发光——修仙界自带特效', 999),
('炎狼卵', 'BEAST_EGG', '{"grow_time":96,"yields":["炎狼牙"],"survive_rate":60}', '["beast_egg","fire","rare"]'::jsonb, '炎狼牙残留火焰之气——咬人之前会先预热', 999),
('玄蛇卵', 'BEAST_EGG', '{"grow_time":120,"yields":["玄蛇鳞"],"survive_rate":55}', '["beast_egg","dark","rare"]'::jsonb, '玄蛇鳞片色如深渊——适合喜欢暗色系风格的修士', 999),
('风貂卵', 'BEAST_EGG', '{"grow_time":96,"yields":["风貂尾"],"survive_rate":60}', '["beast_egg","wind","rare"]'::jsonb, '风貂速度极快，尾巴生风——抓它的时候就知道厉害了', 999),
('金蟾卵', 'BEAST_EGG', '{"grow_time":120,"yields":["金蟾珠"],"survive_rate":55}', '["beast_egg","gold","rare"]'::jsonb, '金蟾吐珠，招财进宝——就是长得有点磕碜', 999);

-- T3 灵兽卵 (史诗)
INSERT INTO xt_item_template (name, type, properties, tags, description, max_stack) VALUES
('冰凤卵', 'BEAST_EGG', '{"grow_time":192,"yields":["冰凤羽毛"],"survive_rate":35}', '["beast_egg","ice","epic"]'::jsonb, '冰凤乃上古神禽，羽毛寒气逼人——孵它的时候记得多穿点', 999),
('白虎卵', 'BEAST_EGG', '{"grow_time":168,"yields":["白虎皮毛"],"survive_rate":35}', '["beast_egg","metal","epic"]'::jsonb, '白虎皮毛坚不可摧——但灵兽本身的性格也没那么温柔', 999),
('玄武卵', 'BEAST_EGG', '{"grow_time":168,"yields":["玄武甲壳"],"survive_rate":35}', '["beast_egg","water","epic"]'::jsonb, '玄武为四象之一，能抵御水火——孵化时需要极大的耐心', 999),
('火凤卵', 'BEAST_EGG', '{"grow_time":192,"yields":["冰凤泪"],"survive_rate":35}', '["beast_egg","fire","epic"]'::jsonb, '火凤涅槃而生——建议养它之前先把福地的防火做好', 999),
('青鸾卵', 'BEAST_EGG', '{"grow_time":168,"yields":["青鸾羽"],"survive_rate":35}', '["beast_egg","wind","epic"]'::jsonb, '青鸾乃西王母信使，羽毛蕴含清风之力——孵出来倍有面子', 999),
('麒麟卵', 'BEAST_EGG', '{"grow_time":192,"yields":["麒麟角"],"survive_rate":30}', '["beast_egg","five_elements","epic"]'::jsonb, '麒麟现世，祥瑞之兆——但能不能孵出来得看缘分', 999);

-- T4 灵兽卵 (传说)
INSERT INTO xt_item_template (name, type, properties, tags, description, max_stack) VALUES
('金龙卵', 'BEAST_EGG', '{"grow_time":168,"yields":["金龙鳞"],"survive_rate":30}', '["beast_egg","gold","legendary"]'::jsonb, '龙族之后，鳞片蕴含龙脉之气——孵出来就是福地里的镇宅之宝', 999),
('朱雀卵', 'BEAST_EGG', '{"grow_time":240,"yields":["朱雀翎羽"],"survive_rate":20}', '["beast_egg","fire","legendary"]'::jsonb, '天之四灵之一，翎羽永不熄灭——孵化需十日，请设好闹钟', 999),
('青龙卵', 'BEAST_EGG', '{"grow_time":240,"yields":["青龙逆鳞"],"survive_rate":20}', '["beast_egg","wood","legendary"]'::jsonb, '东方青龙，生生不息——逆鳞触之即怒，建议与其保持友好距离', 999),
('九尾灵狐卵', 'BEAST_EGG', '{"grow_time":240,"yields":["九尾灵狐尾"],"survive_rate":20}', '["beast_egg","beast","legendary"]'::jsonb, '九尾之狐，每尾一命——孵出来就是一整个福地的排面', 999),
('应龙卵', 'BEAST_EGG', '{"grow_time":240,"yields":["应龙鳞"],"survive_rate":15}', '["beast_egg","dragon","legendary"]'::jsonb, '有翼之龙，呼风唤雨——孵化需十日，成活率最低，值不值得自己判断', 999);
