-- 法决种子数据 (xt_skill)

-- ============ PASSIVE SKILLS (被动 — 属性增益) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, cooldown_seconds, require_wis, level_requirement, tags) VALUES
('金刚体',   '运转灵气淬炼体魄，永久提升防御力10%。', 'PASSIVE', '[{"type":"DEFENSE_BUFF","value":10}]', 'NONE', 0, NULL, 1, '["passive","defense","beginner"]'),
('轻身术',   '灵力灌注双腿，身轻如燕，永久提升敏捷10%。', 'PASSIVE', '[{"type":"SPEED_BUFF","value":10}]', 'NONE', 0, NULL, 1, '["passive","agi","beginner"]'),
('蛮牛劲',   '模仿蛮牛之力运行气血，永久提升力道10%。', 'PASSIVE', '[{"type":"ATTACK_BUFF","value":10}]', 'NONE', 0, NULL, 1, '["passive","str","beginner"]'),
('静心诀',   '心如止水，万念归寂，永久提升悟性10%。', 'PASSIVE', '[{"type":"SPEED_BUFF","value":10}]', 'NONE', 0, NULL, 1, '["passive","wis","beginner"]'),
('铁布衫',   '以灵化罡，罩护全身，永久提升防御力15%。需要金刚体。', 'PASSIVE', '[{"type":"DEFENSE_BUFF","value":15}]', 'NONE', 0, 15, 10, '["passive","defense","basic"]'),
('疾风步',   '踏步如风，残影相随，永久提升敏捷15%。需要轻身术。', 'PASSIVE', '[{"type":"SPEED_BUFF","value":15}]', 'NONE', 0, 15, 10, '["passive","agi","basic"]'),
('龙象般若功','身具龙象之力，开碑裂石，永久提升力道15%。需要蛮牛劲。', 'PASSIVE', '[{"type":"ATTACK_BUFF","value":15}]', 'NONE', 0, 15, 10, '["passive","str","basic"]'),
('通明心法', '心通大道，慧眼如炬，永久提升悟性15%。需要静心诀。', 'PASSIVE', '[{"type":"SPEED_BUFF","value":15}]', 'NONE', 0, 15, 10, '["passive","wis","basic"]'),
('不灭金身', '灵力凝甲，金刚不坏，永久提升防御力20%。需要铁布衫。', 'PASSIVE', '[{"type":"DEFENSE_BUFF","value":20}]', 'NONE', 0, 30, 30, '["passive","defense","intermediate"]'),
('浮光掠影', '身化流光，无影无踪，永久提升敏捷20%。需要疾风步。', 'PASSIVE', '[{"type":"SPEED_BUFF","value":20}]', 'NONE', 0, 30, 30, '["passive","agi","intermediate"]'),
('九牛二虎', '九牛二虎加身，力拔山兮，永久提升力道20%。需要龙象般若功。', 'PASSIVE', '[{"type":"ATTACK_BUFF","value":20}]', 'NONE', 0, 30, 30, '["passive","str","intermediate"]'),
('天人感应', '感天悟地，道法自然，永久提升悟性20%。需要通明心法。', 'PASSIVE', '[{"type":"SPEED_BUFF","value":20}]', 'NONE', 0, 30, 30, '["passive","wis","intermediate"]'),
('万劫不磨', '千劫万磨身不损，永久提升防御力30%。需要不灭金身。', 'PASSIVE', '[{"type":"DEFENSE_BUFF","value":30}]', 'NONE', 0, 50, 50, '["passive","defense","advanced","epic"]'),
('虚空瞬步', '身融虚空，瞬移百丈，永久提升敏捷30%。需要浮光掠影。', 'PASSIVE', '[{"type":"SPEED_BUFF","value":30}]', 'NONE', 0, 50, 50, '["passive","agi","advanced","epic"]'),
('搬山倒海', '神通广大，搬山填海，永久提升力道30%。需要九牛二虎。', 'PASSIVE', '[{"type":"ATTACK_BUFF","value":30}]', 'NONE', 0, 50, 50, '["passive","str","advanced","epic"]'),
('道心通明', '道心坚定，明悟天地至理，永久提升悟性30%。需要天人感应。', 'PASSIVE', '[{"type":"SPEED_BUFF","value":30}]', 'NONE', 0, 50, 50, '["passive","wis","advanced","epic"]');

-- ============ BLADE (刀法) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, level_requirement, tags) VALUES
('破风斩',   '刀如疾风破空而出，造成150%攻击力的伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*1.5","target":"single"}]', 'WEAPON_TYPE', 'BLADE', 10, NULL, 1, '["blade","damage","beginner"]'),
('横刀断岳', '一记横斩力道万钧，造成220%攻击力的伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.2","target":"single"}]', 'WEAPON_TYPE', 'BLADE', 15, 10, 10, '["blade","damage","basic"]'),
('刀光如练', '刀势连绵不绝，连续三刀每刀80%攻击力的伤害。', 'ACTIVE', '[{"type":"MULTI_HIT","formula":"attack*0.8","value":3,"target":"single"}]', 'WEAPON_TYPE', 'BLADE', 20, 15, 20, '["blade","multi_hit","intermediate"]'),
('霸刀诀',   '霸道无双，无视30%防御且造成180%攻击力的伤害。', 'ACTIVE', '[{"type":"ARMOR_BREAK","value":30,"duration":1,"target":"single"},{"type":"DAMAGE","formula":"attack*1.8","target":"single"}]', 'WEAPON_TYPE', 'BLADE', 30, 25, 30, '["blade","armor_break","advanced"]'),
('天刀九式', '九式合一，刀意纵横。连斩4次（60%攻击力），且对血量低于25%的目标造成250%斩杀伤害。', 'ACTIVE', '[{"type":"MULTI_HIT","formula":"attack*0.6","value":4,"target":"single"},{"type":"EXECUTE","formula":"attack*2.5","chance":0.25,"target":"single"}]', 'WEAPON_TYPE', 'BLADE', 60, 40, 50, '["blade","execute","multi_hit","epic"]');

-- ============ SWORD (剑法) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, level_requirement, tags) VALUES
('清风剑法', '剑如清风拂柳，造成160%攻击力的伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*1.6","target":"single"}]', 'WEAPON_TYPE', 'SWORD', 10, NULL, 1, '["sword","damage","beginner"]'),
('飞仙剑法', '剑气如飞仙凌空，造成210%攻击力的伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.1","target":"single"}]', 'WEAPON_TYPE', 'SWORD', 12, 10, 10, '["sword","damage","basic"]'),
('万剑归宗', '万剑齐发，对全体敌人造成120%攻击力的伤害。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.2","target":"aoe"}]', 'WEAPON_TYPE', 'SWORD', 25, 20, 20, '["sword","aoe","intermediate"]'),
('剑心通明', '剑心如镜，照破万法。造成180%攻击力伤害，同时提升自己15%防御2回合。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*1.8","target":"single"},{"type":"DEFENSE_BUFF","value":15,"duration":2,"target":"single"}]', 'WEAPON_TYPE', 'SWORD', 35, 30, 30, '["sword","damage","defense_buff","advanced"]'),
('青莲剑歌', '剑舞如莲华绽放，连续5次每击55%攻击力的伤害，附带10%吸血。', 'ACTIVE', '[{"type":"MULTI_HIT","formula":"attack*0.55","value":5,"target":"single"},{"type":"LIFESTEAL","value":10,"target":"single"}]', 'WEAPON_TYPE', 'SWORD', 60, 45, 50, '["sword","multi_hit","lifesteal","epic"]'),
('诛仙剑诀', '传说可屠仙之剑，造成300%攻击力的伤害，且对残血目标（HP<30%）造成450%斩杀伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*3.0","target":"single"},{"type":"EXECUTE","formula":"attack*4.5","chance":0.3,"target":"single"}]', 'WEAPON_TYPE', 'SWORD', 90, 60, 70, '["sword","execute","legendary"]');

-- ============ AXE (斧法) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, level_requirement, tags) VALUES
('开山斧',   '势如破竹，一斧劈山，造成200%攻击力的伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.0","target":"single"}]', 'WEAPON_TYPE', 'AXE', 12, NULL, 1, '["axe","damage","beginner"]'),
('旋风斧',   '旋转横扫，对全体造成130%攻击力的伤害。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.3","target":"aoe"}]', 'WEAPON_TYPE', 'AXE', 20, 10, 10, '["axe","aoe","basic"]'),
('碎星斧',   '一斧碎星辰，造成240%攻击力伤害并附带40%破甲持续2回合。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.4","target":"single"},{"type":"ARMOR_BREAK","value":40,"duration":2,"target":"single"}]', 'WEAPON_TYPE', 'AXE', 45, 25, 25, '["axe","armor_break","intermediate"]'),
('刑天斧法', '仿上古刑天舞干戚之势，造成260%伤害并对半血以下敌人造成380%斩杀。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.6","target":"single"},{"type":"EXECUTE","formula":"attack*3.8","chance":0.5,"target":"single"}]', 'WEAPON_TYPE', 'AXE', 75, 40, 45, '["axe","execute","advanced"]'),
('盘古开天', '传说盘古开天之力，造成400%攻击力的伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*4.0","target":"single"}]', 'WEAPON_TYPE', 'AXE', 120, 60, 65, '["axe","damage","legendary"]');

-- ============ SPEAR (枪法) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, level_requirement, tags) VALUES
('穿云枪',   '枪出如龙穿云而出，造成160%攻击力的伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*1.6","target":"single"}]', 'WEAPON_TYPE', 'SPEAR', 10, NULL, 1, '["spear","damage","beginner"]'),
('游龙枪法', '枪势如游龙盘旋，造成230%攻击力的伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.3","target":"single"}]', 'WEAPON_TYPE', 'SPEAR', 15, 10, 10, '["spear","damage","basic"]'),
('寒芒点星', '枪尖寒芒如寒星点点，连刺3次每击70%攻击力伤害。', 'ACTIVE', '[{"type":"MULTI_HIT","formula":"attack*0.7","value":3,"target":"single"}]', 'WEAPON_TYPE', 'SPEAR', 20, 15, 20, '["spear","multi_hit","intermediate"]'),
('回马枪',   '佯退诱敌，回身致命一枪造成250%伤害，对残血（<30%）造成400%斩杀。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.5","target":"single"},{"type":"EXECUTE","formula":"attack*4.0","chance":0.3,"target":"single"}]', 'WEAPON_TYPE', 'SPEAR', 40, 25, 30, '["spear","execute","advanced"]'),
('霸王枪',   '横扫千军，对全体造成140%攻击力伤害。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.4","target":"aoe"}]', 'WEAPON_TYPE', 'SPEAR', 50, 35, 45, '["spear","aoe","advanced"]'),
('破阵枪诀', '专破阵法，造成280%攻击力伤害附带50%破甲持续2回合。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.8","target":"single"},{"type":"ARMOR_BREAK","value":50,"duration":2,"target":"single"}]', 'WEAPON_TYPE', 'SPEAR', 80, 50, 60, '["spear","armor_break","epic"]');

-- ============ STAFF (棍法) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, level_requirement, tags) VALUES
('伏魔棍',   '降妖伏魔基础棍法，造成150%攻击力伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*1.5","target":"single"}]', 'WEAPON_TYPE', 'STAFF', 10, NULL, 1, '["staff","damage","beginner"]'),
('横扫千军', '棍扫一大片，对全体造成120%攻击力伤害。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.2","target":"aoe"}]', 'WEAPON_TYPE', 'STAFF', 20, 10, 10, '["staff","aoe","basic"]'),
('定海神针', '重击如定海神针镇压，造成200%攻击力伤害并有40%概率眩晕1回合。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.0","target":"single"},{"type":"STUN","chance":0.4,"duration":1,"target":"single"}]', 'WEAPON_TYPE', 'STAFF', 40, 20, 25, '["staff","stun","intermediate"]'),
('翻天棍法', '一套翻天覆地之棍，对全体造成150%伤害附带30%破甲。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.5","target":"aoe"},{"type":"ARMOR_BREAK","value":30,"duration":2,"target":"aoe"}]', 'WEAPON_TYPE', 'STAFF', 60, 35, 40, '["staff","aoe","armor_break","advanced"]'),
('菩提棍意', '以菩提之心运棍，造成230%攻击力伤害并吸取25%生命。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.3","target":"single"},{"type":"LIFESTEAL","value":25,"target":"single"}]', 'WEAPON_TYPE', 'STAFF', 70, 45, 55, '["staff","lifesteal","epic"]');

-- ============ BOW (弓法) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, level_requirement, tags) VALUES
('穿杨箭',   '百步穿杨，精准一射造成170%攻击力伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*1.7","target":"single"}]', 'WEAPON_TYPE', 'BOW', 8, NULL, 1, '["bow","damage","beginner"]'),
('连珠箭',   '连珠三箭，每箭65%攻击力伤害。', 'ACTIVE', '[{"type":"MULTI_HIT","formula":"attack*0.65","value":3,"target":"single"}]', 'WEAPON_TYPE', 'BOW', 12, 10, 10, '["bow","multi_hit","basic"]'),
('穿心箭',   '一箭穿心，造成210%攻击力伤害附带35%破甲。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.1","target":"single"},{"type":"ARMOR_BREAK","value":35,"duration":2,"target":"single"}]', 'WEAPON_TYPE', 'BOW', 25, 20, 20, '["bow","armor_break","intermediate"]'),
('流星箭雨', '箭如流星雨般落下，对全体造成125%攻击力伤害。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.25","target":"aoe"}]', 'WEAPON_TYPE', 'BOW', 45, 30, 35, '["bow","aoe","advanced"]'),
('追魂箭',   '一箭追魂索命，造成250%伤害并对低血（<25%）敌人造成430%斩杀。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.5","target":"single"},{"type":"EXECUTE","formula":"attack*4.3","chance":0.25,"target":"single"}]', 'WEAPON_TYPE', 'BOW', 75, 40, 50, '["bow","execute","epic"]'),
('射日弓',   '后羿射日之术，造成350%攻击力的伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*3.5","target":"single"}]', 'WEAPON_TYPE', 'BOW', 120, 60, 70, '["bow","damage","legendary"]');

-- ============ WHIP (鞭法) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, level_requirement, tags) VALUES
('灵蛇鞭',   '鞭如灵蛇吐信，造成140%攻击力伤害并减速20%持续2回合。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*1.4","target":"single"},{"type":"SLOW","value":20,"duration":2,"target":"single"}]', 'WEAPON_TYPE', 'WHIP', 12, NULL, 5, '["whip","damage","slow","beginner"]'),
('缚龙索',   '鞭化缚龙之索，50%概率使目标眩晕1回合。', 'ACTIVE', '[{"type":"STUN","chance":0.5,"duration":1,"target":"single"}]', 'WEAPON_TYPE', 'WHIP', 35, 15, 20, '["whip","stun","control","intermediate"]'),
('天魔鞭法', '天魔乱舞之鞭，对全体造成120%伤害附带持续伤害（攻击力15%×3回合）。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.2","target":"aoe"},{"type":"DOT","value":15,"duration":3,"target":"aoe"}]', 'WEAPON_TYPE', 'WHIP', 60, 35, 40, '["whip","aoe","dot","advanced"]'),
('修罗鞭',   '修罗道的狂鞭，造成220%攻击力伤害并吸取20%生命。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.2","target":"single"},{"type":"LIFESTEAL","value":20,"target":"single"}]', 'WEAPON_TYPE', 'WHIP', 70, 45, 55, '["whip","lifesteal","epic"]');

-- ============ HALBERD (戟法) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, level_requirement, tags) VALUES
('方天画戟', '基础戟法，造成180%攻击力的伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*1.8","target":"single"}]', 'WEAPON_TYPE', 'HALBERD', 12, NULL, 10, '["halberd","damage","beginner"]'),
('戟破苍穹', '一戟破空，造成230%伤害附带40%破甲。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.3","target":"single"},{"type":"ARMOR_BREAK","value":40,"duration":2,"target":"single"}]', 'WEAPON_TYPE', 'HALBERD', 45, 25, 30, '["halberd","armor_break","intermediate"]'),
('裂天戟',   '一戟分裂天地，对全体造成140%伤害，对残血（<25%）造成380%斩杀。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.4","target":"aoe"},{"type":"EXECUTE","formula":"attack*3.8","chance":0.25,"target":"aoe"}]', 'WEAPON_TYPE', 'HALBERD', 90, 50, 50, '["halberd","aoe","execute","epic"]');

-- ============ HAMMER (锤法) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, level_requirement, tags) VALUES
('震地锤',   '一锤震地，造成190%攻击力伤害，40%概率眩晕1回合。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*1.9","target":"single"},{"type":"STUN","chance":0.4,"duration":1,"target":"single"}]', 'WEAPON_TYPE', 'HAMMER', 15, NULL, 10, '["hammer","damage","stun","beginner"]'),
('轰天锤',   '一锤轰天，对全体造成130%攻击力伤害。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.3","target":"aoe"}]', 'WEAPON_TYPE', 'HAMMER', 45, 25, 30, '["hammer","aoe","intermediate"]'),
('碎虚锤',   '一锤碎虚空，造成300%伤害附带60%破甲和对残血（<25%）450%斩杀。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*3.0","target":"single"},{"type":"ARMOR_BREAK","value":60,"duration":2,"target":"single"},{"type":"EXECUTE","formula":"attack*4.5","chance":0.25,"target":"single"}]', 'WEAPON_TYPE', 'HAMMER', 100, 60, 55, '["hammer","armor_break","execute","legendary"]');

-- ============ DAGGER (匕首) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, level_requirement, tags) VALUES
('影刺',     '从暗影中发起的一击，造成180%攻击力伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*1.8","target":"single"}]', 'WEAPON_TYPE', 'DAGGER', 8, NULL, 5, '["dagger","damage","beginner"]'),
('暗影杀',   '暗影中的致命一击，对血量低于40%的目标造成300%斩杀伤害。', 'ACTIVE', '[{"type":"EXECUTE","formula":"attack*3.0","chance":0.4,"target":"single"}]', 'WEAPON_TYPE', 'DAGGER', 30, 15, 15, '["dagger","execute","basic"]'),
('千幻刺杀', '幻化千影连续刺杀，连击4次每击55%攻击力伤害，附带持续毒伤（10%×3回合）。', 'ACTIVE', '[{"type":"MULTI_HIT","formula":"attack*0.55","value":4,"target":"single"},{"type":"DOT","value":10,"duration":3,"target":"single"}]', 'WEAPON_TYPE', 'DAGGER', 50, 30, 30, '["dagger","multi_hit","dot","advanced"]'),
('瞬狱杀',   '瞬移至敌后发动致命一击，造成260%伤害附带眩晕（50%）和对残血（<20%）480%斩杀。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.6","target":"single"},{"type":"STUN","chance":0.5,"duration":1,"target":"single"},{"type":"EXECUTE","formula":"attack*4.8","chance":0.2,"target":"single"}]', 'WEAPON_TYPE', 'DAGGER', 80, 50, 50, '["dagger","execute","stun","epic"]');

-- ============ FAN (扇法) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, level_requirement, tags) VALUES
('清风拂柳', '扇引清风，治疗自身悟性×80%+100的生命值。', 'ACTIVE', '[{"type":"HEAL","formula":"wis*0.8+100","value":80,"target":"single"}]', 'WEAPON_TYPE', 'FAN', 15, 15, 10, '["fan","heal","beginner"]'),
('霓裳扇舞', '扇舞如霓裳，对全体造成110%攻击力伤害并减速15%持续2回合。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.1","target":"aoe"},{"type":"SLOW","value":15,"duration":2,"target":"aoe"}]', 'WEAPON_TYPE', 'FAN', 40, 25, 25, '["fan","aoe","slow","intermediate"]'),
('天罡扇',   '天罡之力入扇，造成200%攻击力伤害并提升自己攻击力20%持续3回合。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.0","target":"single"},{"type":"ATTACK_BUFF","value":20,"duration":3,"target":"single"}]', 'WEAPON_TYPE', 'FAN', 55, 35, 40, '["fan","attack_buff","advanced"]');

-- ============ FLYWHISK (拂尘) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, level_requirement, tags) VALUES
('拂尘净心', '拂尘轻扫净心田，治疗悟性×70%+80血量并提升速度15%持续2回合。', 'ACTIVE', '[{"type":"HEAL","formula":"wis*0.7+80","value":70,"target":"single"},{"type":"SPEED_BUFF","value":15,"duration":2,"target":"single"}]', 'WEAPON_TYPE', 'FLYWHISK', 20, 15, 10, '["flywhisk","heal","speed_buff","beginner"]'),
('三千烦恼', '尘丝化三千，对全体造成120%攻击力伤害并50%概率沉默2回合。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.2","target":"aoe"},{"type":"SILENCE","chance":0.5,"duration":2,"target":"aoe"}]', 'WEAPON_TYPE', 'FLYWHISK', 45, 30, 25, '["flywhisk","aoe","silence","intermediate"]'),
('太虚拂尘', '太虚之境化拂尘，造成210%攻击力伤害并提升防御25%持续3回合。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.1","target":"single"},{"type":"DEFENSE_BUFF","value":25,"duration":3,"target":"single"}]', 'WEAPON_TYPE', 'FLYWHISK', 60, 40, 40, '["flywhisk","defense_buff","advanced"]');

-- ============ RING (圈法) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, level_requirement, tags) VALUES
('金刚圈',   '金刚圈脱手而出，造成170%攻击力伤害，40%概率眩晕1回合。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*1.7","target":"single"},{"type":"STUN","chance":0.4,"duration":1,"target":"single"}]', 'WEAPON_TYPE', 'RING', 12, NULL, 15, '["ring","damage","stun","beginner"]'),
('乾坤圈',   '乾坤一圈，对全体造成115%攻击力伤害。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.15","target":"aoe"}]', 'WEAPON_TYPE', 'RING', 40, 25, 30, '["ring","aoe","intermediate"]'),
('日月双环', '日月齐出，连环双击每击90%攻击力伤害，第二次必暴击（180%伤害）。', 'ACTIVE', '[{"type":"MULTI_HIT","formula":"attack*0.9","value":2,"target":"single"}]', 'WEAPON_TYPE', 'RING', 50, 35, 45, '["ring","multi_hit","advanced"]');

-- ============ BELL (钟法) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, level_requirement, tags) VALUES
('伏魔钟',   '钟声震荡伏妖邪，造成160%攻击力伤害并50%概率沉默2回合。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*1.6","target":"single"},{"type":"SILENCE","chance":0.5,"duration":2,"target":"single"}]', 'WEAPON_TYPE', 'BELL', 15, 10, 15, '["bell","damage","silence","beginner"]'),
('镇魂钟声', '钟声镇魂，对全体造成125%攻击力伤害并40%概率眩晕1回合。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.25","target":"aoe"},{"type":"STUN","chance":0.4,"duration":1,"target":"aoe"}]', 'WEAPON_TYPE', 'BELL', 45, 30, 30, '["bell","aoe","stun","intermediate"]'),
('混沌钟',   '混沌初开的钟声回荡，造成280%攻击力伤害并对残血（<30%）造成420%斩杀。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.8","target":"single"},{"type":"EXECUTE","formula":"attack*4.2","chance":0.3,"target":"single"}]', 'WEAPON_TYPE', 'BELL', 90, 50, 50, '["bell","execute","epic"]');

-- ============ WEAPON_CATEGORY SKILLS (兵器大类) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, level_requirement, tags) VALUES
-- MELEE
('兵锋诀',   '运灵力强化兵刃，提升攻击力15%持续3回合。', 'ACTIVE', '[{"type":"ATTACK_BUFF","value":15,"duration":3,"target":"single"}]', 'WEAPON_CATEGORY', 'MELEE', 30, 10, 5, '["melee","attack_buff","beginner"]'),
('刀剑无双', '刀剑齐出，对全体造成140%攻击力伤害。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.4","target":"aoe"}]', 'WEAPON_CATEGORY', 'MELEE', 50, 25, 25, '["melee","aoe","intermediate"]'),
('兵主杀伐', '杀伐之气纵横，造成240%伤害并吸取20%生命。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.4","target":"single"},{"type":"LIFESTEAL","value":20,"target":"single"}]', 'WEAPON_CATEGORY', 'MELEE', 80, 40, 45, '["melee","lifesteal","advanced"]'),
-- POLEARM
('长驱直入', '以长兵之势直捣黄龙，造成200%攻击力伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.0","target":"single"}]', 'WEAPON_CATEGORY', 'POLEARM', 20, NULL, 5, '["polearm","damage","beginner"]'),
('横扫八荒', '长兵横扫八荒，对全体造成130%攻击力伤害。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.3","target":"aoe"}]', 'WEAPON_CATEGORY', 'POLEARM', 50, 25, 25, '["polearm","aoe","intermediate"]'),
('枪出如龙', '全力一击如苍龙出海，对残血（<30%）造成400%斩杀。', 'ACTIVE', '[{"type":"EXECUTE","formula":"attack*4.0","chance":0.3,"target":"single"}]', 'WEAPON_CATEGORY', 'POLEARM', 90, 45, 45, '["polearm","execute","advanced"]'),
-- RANGED
('鹰眼术',   '凝神聚目，提升远程攻击力20%持续3回合。', 'ACTIVE', '[{"type":"ATTACK_BUFF","value":20,"duration":3,"target":"single"}]', 'WEAPON_CATEGORY', 'RANGED', 25, 10, 10, '["ranged","attack_buff","beginner"]'),
('百步穿杨', '远距离精准一击，造成240%攻击力伤害附带30%破甲。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.4","target":"single"},{"type":"ARMOR_BREAK","value":30,"duration":2,"target":"single"}]', 'WEAPON_CATEGORY', 'RANGED', 60, 30, 30, '["ranged","armor_break","intermediate"]'),
('后羿射日', '远古箭神之力，造成380%攻击力的伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*3.8","target":"single"}]', 'WEAPON_CATEGORY', 'RANGED', 120, 55, 55, '["ranged","damage","legendary"]'),
-- EXOTIC
('奇门遁甲', '奇兵八门变化，造成170%攻击力伤害并减速20%持续2回合。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*1.7","target":"single"},{"type":"SLOW","value":20,"duration":2,"target":"single"}]', 'WEAPON_CATEGORY', 'EXOTIC', 20, 10, 10, '["exotic","slow","beginner"]'),
('八门金锁', '八门金锁阵展开，对全体造成120%伤害并50%概率沉默2回合。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.2","target":"aoe"},{"type":"SILENCE","chance":0.5,"duration":2,"target":"aoe"}]', 'WEAPON_CATEGORY', 'EXOTIC', 55, 30, 30, '["exotic","aoe","silence","intermediate"]'),
('逆乱阴阳', '逆转阴阳，造成250%伤害，对残血（<25%）造成430%斩杀。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.5","target":"single"},{"type":"EXECUTE","formula":"attack*4.3","chance":0.25,"target":"single"}]', 'WEAPON_CATEGORY', 'EXOTIC', 90, 50, 50, '["exotic","execute","epic"]');

-- ============ ELEMENT SKILLS (五行元素) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, level_requirement, tags) VALUES
-- METAL
('金灵剑气', '以金行之力化剑气，造成180%攻击力伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*1.8","element":"metal","target":"single"}]', 'ELEMENT', 'metal', 10, 10, 10, '["metal","damage","beginner"]'),
('金钟罩',   '以金行灵力罩体，提升防御力25%持续3回合。', 'ACTIVE', '[{"type":"DEFENSE_BUFF","value":25,"duration":3,"target":"single"}]', 'ELEMENT', 'metal', 30, 15, 20, '["metal","defense_buff","basic"]'),
('太白斩魔', '太白金星之力斩妖除魔，造成260%攻击力伤害附带40%破甲。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*2.6","element":"metal","target":"single"},{"type":"ARMOR_BREAK","value":40,"duration":2,"target":"single"}]', 'ELEMENT', 'metal', 60, 35, 40, '["metal","armor_break","advanced"]'),
-- WOOD
('青木诀',   '引木行生机滋养自身，治疗悟性×90%+120的生命值。', 'ACTIVE', '[{"type":"HEAL","formula":"wis*0.9+120","value":90,"target":"single"}]', 'ELEMENT', 'wood', 12, 10, 10, '["wood","heal","beginner"]'),
('荆棘缠绕', '荆棘藤蔓缠绕敌身，造成120%攻击力伤害附带持续伤害（12%×3回合）和减速20%。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*1.2","element":"wood","target":"single"},{"type":"DOT","value":12,"duration":3,"target":"single"},{"type":"SLOW","value":20,"duration":3,"target":"single"}]', 'ELEMENT', 'wood', 35, 20, 20, '["wood","dot","slow","basic"]'),
('万木逢春', '百花齐放万木逢春，治疗全体悟性×60%+100的生命值。', 'ACTIVE', '[{"type":"HEAL","formula":"wis*0.6+100","value":60,"target":"aoe"}]', 'ELEMENT', 'wood', 60, 40, 40, '["wood","heal","aoe","advanced"]'),
-- WATER
('寒冰掌',   '寒气凝于掌心，造成170%攻击力伤害附带冰冻（30%概率冻结1回合）。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*1.7","element":"ice","target":"single"},{"type":"FREEZE","chance":0.3,"duration":1,"target":"single"}]', 'ELEMENT', 'water', 12, 10, 10, '["water","ice","freeze","beginner"]'),
('水镜术',   '水镜映万物反弹三成伤害，提升防御20%持续3回合。', 'ACTIVE', '[{"type":"DEFENSE_BUFF","value":20,"duration":3,"target":"single"}]', 'ELEMENT', 'water', 30, 15, 20, '["water","defense_buff","basic"]'),
('冰封万里', '万里冰封，对全体造成130%攻击力伤害并40%概率冰冻1回合。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.3","element":"ice","target":"aoe"},{"type":"FREEZE","chance":0.4,"duration":1,"target":"aoe"}]', 'ELEMENT', 'water', 70, 45, 40, '["water","ice","aoe","freeze","advanced"]'),
-- FIRE
('烈火掌',   '聚火气于掌，造成190%攻击力的火焰伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*1.9","element":"fire","target":"single"}]', 'ELEMENT', 'fire', 10, NULL, 10, '["fire","damage","beginner"]'),
('焚天诀',   '焚天之火燎原势，对全体造成135%攻击力伤害附带持续灼烧（15%×3回合）。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.35","element":"fire","target":"aoe"},{"type":"DOT","value":15,"duration":3,"target":"aoe"}]', 'ELEMENT', 'fire', 55, 30, 25, '["fire","aoe","dot","intermediate"]'),
('三昧真火', '三昧真火焚万物，造成320%攻击力伤害并附带50%破甲。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*3.2","element":"fire","target":"single"},{"type":"ARMOR_BREAK","value":50,"duration":2,"target":"single"}]', 'ELEMENT', 'fire', 90, 55, 60, '["fire","armor_break","epic"]'),
-- EARTH
('厚土盾',   '以大地之力凝聚盾甲，提升防御30%持续3回合。', 'ACTIVE', '[{"type":"DEFENSE_BUFF","value":30,"duration":3,"target":"single"}]', 'ELEMENT', 'earth', 30, 10, 10, '["earth","defense_buff","beginner"]'),
('地动术',   '震动大地，对全体造成125%攻击力伤害并减速25%持续2回合。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.25","element":"earth","target":"aoe"},{"type":"SLOW","value":25,"duration":2,"target":"aoe"}]', 'ELEMENT', 'earth', 45, 25, 25, '["earth","aoe","slow","intermediate"]'),
('泰山压顶', '泰山之力从天而降，造成300%攻击力伤害并50%概率眩晕1回合。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*3.0","element":"earth","target":"single"},{"type":"STUN","chance":0.5,"duration":1,"target":"single"}]', 'ELEMENT', 'earth', 80, 50, 55, '["earth","stun","epic"]');

-- ============ LEGENDARY / UNIVERSAL SKILLS (传说/通用) ============
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, cooldown_seconds, require_wis, level_requirement, tags) VALUES
('轩辕剑法', '黄帝轩辕氏所创战剑之法，造成350%攻击力伤害并对全体波及150%伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*3.5","target":"single"},{"type":"AOE_DAMAGE","formula":"attack*1.5","target":"aoe"}]', 'NONE', 120, 70, 80, '["sword","legendary","damage","aoe"]'),
('霸下真身', '龙九子之首霸下的不灭真身奥义，永久提升防御力40%。', 'PASSIVE', '[{"type":"DEFENSE_BUFF","value":40}]', 'NONE', 0, 80, 90, '["passive","con","legendary"]'),
('蟠桃仙术', '西王母蟠桃仙根所化疗愈术，治疗全体悟性×150%+500生命值。', 'ACTIVE', '[{"type":"HEAL","formula":"wis*1.5+500","value":150,"target":"aoe"}]', 'NONE', 180, 80, 85, '["heal","legendary","aoe"]'),
('莲华涅槃', '濒死之际绽莲华涅槃重生，免疫死亡一次并回复50%生命值（被动触发）。', 'PASSIVE', '[{"type":"HEAL","value":50,"target":"single"},{"type":"DEFENSE_BUFF","value":50,"duration":1,"target":"single"}]', 'NONE', 600, 90, 95, '["passive","survival","legendary"]'),
('一气化三清','道祖神通，分身化三，对单一目标造成3次各180%攻击力的伤害。', 'ACTIVE', '[{"type":"MULTI_HIT","formula":"attack*1.8","value":3,"target":"single"}]', 'NONE', 120, 80, 90, '["multi_hit","legendary","dao"]'),
('天罡北斗阵','天罡北斗七星之力灌注，提升全体攻击力25%和防御力25%持续4回合。', 'ACTIVE', '[{"type":"ATTACK_BUFF","value":25,"duration":4,"target":"aoe"},{"type":"DEFENSE_BUFF","value":25,"duration":4,"target":"aoe"}]', 'NONE', 150, 70, 75, '["aoe","buff","legendary"]'),
('袖里乾坤', '五庄观镇元大仙秘术，禁锢敌方全体1回合（沉默+减速50%）。', 'ACTIVE', '[{"type":"SILENCE","chance":1.0,"duration":1,"target":"aoe"},{"type":"SLOW","value":50,"duration":1,"target":"aoe"}]', 'NONE', 180, 85, 85, '["control","legendary","aoe"]'),
('五雷正法', '召唤五方神雷，对全体造成200%攻击力的雷电伤害，并有25%概率眩晕1回合。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*2.0","element":"lightning","target":"aoe"},{"type":"STUN","chance":0.25,"duration":1,"target":"aoe"}]', 'NONE', 100, 60, 70, '["thunder","aoe","stun","epic"]'),
('血魔真经', '燃烧10%最大生命值，换取攻击力提升40%持续5回合，此间吸血30%。', 'ACTIVE', '[{"type":"ATTACK_BUFF","value":40,"duration":5,"target":"single"},{"type":"LIFESTEAL","value":30,"duration":5,"target":"single"}]', 'NONE', 90, 55, 60, '["lifesteal","attack_buff","evil","epic"]'),
('太乙遁甲', '以先天演算预判敌方攻势，闪避下一回合所有攻击并使自身速度提升30%持续3回合。', 'ACTIVE', '[{"type":"SPEED_BUFF","value":30,"duration":3,"target":"single"},{"type":"DEFENSE_BUFF","value":30,"duration":1,"target":"single"}]', 'NONE', 75, 50, 50, '["speed_buff","defense","dodge","epic"]'),
('九天魔音', '九天之外降临的魔音贯脑，对全体造成150%伤害并附带50%概率眩晕+50%概率沉默。', 'ACTIVE', '[{"type":"AOE_DAMAGE","formula":"attack*1.5","target":"aoe"},{"type":"STUN","chance":0.5,"duration":1,"target":"aoe"},{"type":"SILENCE","chance":0.5,"duration":2,"target":"aoe"}]', 'NONE', 110, 65, 65, '["aoe","control","evil","epic"]'),
('养剑术',   '养一口先天剑意在丹田，每回合自动累积一层剑意（最多10层），释放时造成总剑意×50%攻击力的真实伤害。', 'ACTIVE', '[{"type":"DAMAGE","formula":"attack*stackCount*0.5","maxStacks":10,"target":"single"}]', 'NONE', 30, 40, 45, '["sword","stack","unique"]'),
('混元功',   '混元一中包容万象，将受到的溢出治疗转化为等额护盾持续2回合。', 'PASSIVE', '[{"type":"DEFENSE_BUFF","value":15,"duration":2,"target":"single"}]', 'NONE', 0, 45, 40, '["passive","heal","shield","unique"]'),
('踏波行',   '轻功水上漂的进阶版——水上起飞，战斗中速度提升20%且无视减速效果。', 'PASSIVE', '[{"type":"SPEED_BUFF","value":20,"target":"single"}]', 'NONE', 0, 30, 25, '["passive","speed","mobility"]'),
('逆转丹行', '逆转丹道药性，将下一次治疗效果翻倍。', 'ACTIVE', '[{"type":"HEAL","value":100,"target":"single"}]', 'NONE', 60, 35, 35, '["heal","boost","unique"]'),
('偷天换日', '偷天之力加持，敌方下一次释放法决效果减半。', 'ACTIVE', '[{"type":"SILENCE","chance":0.8,"duration":1,"target":"single"}]', 'NONE', 150, 75, 80, '["control","legendary"]');
