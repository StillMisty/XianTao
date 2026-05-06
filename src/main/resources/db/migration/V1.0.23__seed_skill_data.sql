-- ============================================================
-- 仙道 · xt_skill 种子数据
-- ============================================================
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, require_skill_id, tags, level_requirement) VALUES
('火球术','凝聚灵力在掌心搓出一个火球砸向敌人。修真界最朴实无华。化神期大能感叹:"修炼千年最常用的还是火球术——简单不容易失误。"','ACTIVE','[{"type":"DAMAGE","formula":"wis*1.5","element":"fire","target":"single"}]'::jsonb,'NONE',NULL,5,1,NULL,'["fire","attack","basic","beginner"]'::jsonb,1),
('天雷诀','引天地之雷为己用。威力巨大但精准度感人——劈中谁全看天意。谚语:"天雷诀练到极致你就是雷劫。"','ACTIVE','[{"type":"DAMAGE","formula":"wis*2.5","element":"lightning","target":"single"}]'::jsonb,'NONE',NULL,20,15,NULL,'["lightning","attack","rare"]'::jsonb,15),
('金刚不坏','佛门护体神功残篇。运转时全身金光闪烁——修炼法则:挨打使劲挨打挨到金刚不坏。','PASSIVE','[{"type":"DEFENSE_BUFF","value":0.3}]'::jsonb,'NONE',NULL,0,10,NULL,'["defense","body","holy","passive"]'::jsonb,10),
('遁地术','脚踏大地瞬间钻入地下。逃跑偷袭躲避天劫之必备神技。逃命三大法宝之首——另外两个是"装死"和"喊长辈"。','ACTIVE','[{"type":"SPEED_BUFF","value":1.0,"duration":2}]'::jsonb,'NONE',NULL,30,20,NULL,'["earth","movement","escape","rare"]'::jsonb,20),
('轻身术','灵力注入双腿身轻如燕。共识:"你可以不会攻击法决但不能不会轻身术——因为你不确定什么时候需要跑路。"','ACTIVE','[{"type":"SPEED_BUFF","value":1.0,"duration":3}]'::jsonb,'NONE',NULL,15,5,NULL,'["movement","speed","basic"]'::jsonb,5),
('冰封咒','寒冰之力冻结敌人。冻住时间取决于灵力差距——遇到比自己强的可能只冻住眉毛。','ACTIVE','[{"type":"DAMAGE","formula":"wis*1.8","element":"ice","target":"single"},{"type":"SLOW","value":0.4,"duration":2}]'::jsonb,'NONE',NULL,15,20,NULL,'["ice","attack","control"]'::jsonb,15),
('吸星大法','吸取对手灵气化为己用。邪功被正道联盟明令禁止——但每个大修士都偷偷练过。潜规则:"只要没被人发现就不是魔道。"','ACTIVE','[{"type":"LIFESTEAL","value":0.4}]'::jsonb,'NONE',NULL,45,30,NULL,'["dark","attack","drain","forbidden"]'::jsonb,30),
('分身术','凝聚灵力制造分身迷惑敌人。不造成伤害——本质高级障眼法。最高境界连自己都分不清哪个是真身。','ACTIVE','[{"type":"STUN","value":1.0,"duration":1}]'::jsonb,'NONE',NULL,25,25,NULL,'["trick","defense","rare"]'::jsonb,25),
('回春术','木属性灵力转化治疗自身。珍惜治疗法术——大多修士只学攻击不学治疗死亡率因此居高不下。','ACTIVE','[{"type":"HEAL","value":0.5}]'::jsonb,'NONE',NULL,10,10,NULL,'["wood","heal","support","basic"]'::jsonb,5),
('九阳真经','至阳至刚绝世内功。周身太阳真火缭绕万邪不侵。评价:"练九阳真经的修士冬天不用穿衣服。"','PASSIVE','[{"type":"ATTACK_BUFF","value":0.6}]'::jsonb,'NONE',NULL,0,60,NULL,'["fire","yang","passive","legendary"]'::jsonb,60),
('九阴真经','至阴至寒绝世功法。与九阳一阴一阳相生相克。练成者盛夏自带冷气。','PASSIVE','[{"type":"DEFENSE_BUFF","value":0.4}]'::jsonb,'NONE',NULL,0,60,NULL,'["water","yin","passive","legendary"]'::jsonb,60),
('幻术·镜花水月','制造幻觉攻击心神无视物理防御。名言:"你以为你看穿了幻术?那正是幻术的第二层。"','ACTIVE','[{"type":"DAMAGE","formula":"wis*2.0","element":"dark","target":"single"}]'::jsonb,'NONE',NULL,20,30,NULL,'["mind","attack","control"]'::jsonb,25),
('大摧山掌','肉身之力凝聚掌心一掌劈出山崩地裂。体修名言:"体修的浪漫是——你念咒三秒我劈你一掌你人没了。"','ACTIVE','[{"type":"DAMAGE","formula":"str*3.0","element":"earth","target":"single"}]'::jsonb,'NONE',NULL,10,15,NULL,'["earth","attack","str","physical"]'::jsonb,15),
('金钟罩','佛门护体神功完整版。周身浮现金钟虚影——致命缺陷是不能移动因为你不是真的钟。','PASSIVE','[{"type":"DEFENSE_BUFF","value":0.5}]'::jsonb,'NONE',NULL,0,20,NULL,'["defense","holy","passive","body"]'::jsonb,20),
('血魔功','以自身精血激发潜力。攻击暴涨且每击吸血——但反噬也够受。评价:"血魔功是经济学——算清吸的血值不值得吐的血。"','ACTIVE','[{"type":"LIFESTEAL","value":0.3},{"type":"ATTACK_BUFF","value":0.4,"duration":3}]'::jsonb,'NONE',NULL,60,40,NULL,'["dark","attack","drain","forbidden"]'::jsonb,35),
('毒雾术','释放毒雾持续伤害可叠加。阴招之王——下完毒就跑等对方毒差不多回来补刀。','ACTIVE','[{"type":"DOT","value":0.15,"duration":3,"maxStacks":5,"element":"dark"}]'::jsonb,'NONE',NULL,25,35,NULL,'["dark","dot","poison","control"]'::jsonb,30),
('星辰坠落','牵引天外星辰砸向敌人——趁你病要你命型。"血量低于一半的敌人都是死人"——前提你放出这招时还活着。','ACTIVE','[{"type":"DAMAGE","formula":"wis*2.5","element":"metal","target":"single"},{"type":"EXECUTE","formula":"atk*5.0","value":0.3}]'::jsonb,'NONE',NULL,45,50,NULL,'["metal","attack","execute","cosmic"]'::jsonb,40),
('清风诀','借助风力加速+防御双提升。跑路三件套之首——轻身术+幽影步+清风诀叠加敌人连衣角都碰不到。','ACTIVE','[{"type":"SPEED_BUFF","value":0.5,"duration":3},{"type":"DEFENSE_BUFF","value":0.2,"duration":3}]'::jsonb,'NONE',NULL,15,15,NULL,'["wind","support","speed","defense"]'::jsonb,20),
('道心通明','心境澄澈映照天地大道常驻被动。"真正道心通明的大佬不会到处炫耀——只有半瓶水才说。"','PASSIVE','[{"type":"ATTACK_BUFF","value":0.3},{"type":"DEFENSE_BUFF","value":0.2}]'::jsonb,'NONE',NULL,0,50,NULL,'["mind","passive","legendary","balance"]'::jsonb,45),
('太极剑意','太极之理融入剑法——练到极致能一边打一边喝茶。水灵根绑定。','ACTIVE','[{"type":"DAMAGE","formula":"wis*2.0","element":"water","target":"single"},{"type":"SLOW","value":0.3,"duration":2}]'::jsonb,'ELEMENT''WATER',20,40,NULL,'["water","sword","attack","control"]'::jsonb,35),
('破军枪决','枪中霸王——一枪刺出面前一排全遭殃。传说某枪修捅破了自家护山大阵。标注:"使用时确认枪头朝向。"','ACTIVE','[{"type":"AOE_DAMAGE","formula":"str*2.5","element":"metal","target":"aoe"}]'::jsonb,'WEAPON_TYPE''SPEAR',35,35,NULL,'["spear","attack","aoe"]'::jsonb,35),
('万箭齐发','一箭射出化为万箭。远程修士终极梦想——被近战贴脸前放出此招就是压制之王。','ACTIVE','[{"type":"AOE_DAMAGE","formula":"agi*3.0","element":"metal","target":"aoe"}]'::jsonb,'WEAPON_TYPE''BOW',40,45,NULL,'["bow","attack","aoe"]'::jsonb,40),
('刀剑双绝','左手刀右手剑——刀刚猛剑灵动攻速双升。必须装备刀或剑类法器。"本质是贪——但贪得有理有据。"','PASSIVE','[{"type":"ATTACK_BUFF","value":0.35},{"type":"SPEED_BUFF","value":0.25}]'::jsonb,'WEAPON_CATEGORY','刀兵',0,35,NULL,'["sword","blade","passive","attack","speed"]'::jsonb,30),
('碎魂击','致命一击——低血量敌人有概率秒杀。赌博之王:"对方剩一丝血——碎魂击——没触发——回血——心态碎了。"','ACTIVE','[{"type":"EXECUTE","formula":"atk*10.0","value":0.2,"chance":0.3}]'::jsonb,'NONE',NULL,60,60,NULL,'["dark","execute","gamble"]'::jsonb,55),
('幽影步','身形如鬼魅流转大幅提升闪避和速度。"打不过就跑"流必修。','ACTIVE','[{"type":"SPEED_BUFF","value":0.8,"duration":3}]'::jsonb,'NONE',NULL,20,25,NULL,'["movement","speed","escape"]'::jsonb,25),
('五行轮转','五行相生之理化为己用五系伤害均有抗性。万金油被动——五行宗立派之本。','PASSIVE','[{"type":"DEFENSE_BUFF","value":0.25}]'::jsonb,'NONE',NULL,0,40,NULL,'["element","defense","passive","balance"]'::jsonb,40),
('天眼通','佛门六神通之一——洞悉弱点攻击更易命中要害。都市传说:"某和尚学成后发现师兄弟半夜偷去凡间吃宵夜。"','PASSIVE','[{"type":"ATTACK_BUFF","value":0.35}]'::jsonb,'NONE',NULL,0,45,NULL,'["holy","passive","attack"]'::jsonb,40);

-- 第二批：需引用前驱技能
INSERT INTO xt_skill (name, description, skill_type, effects, binding_type, binding_value, cooldown_seconds, require_wis, require_skill_id, tags, level_requirement) VALUES
('御剑术','以气御剑。学会能踩着剑飞——但新手大多摔下来。名场面:"道友你的剑……它自己跑了。"','ACTIVE','[{"type":"DAMAGE","formula":"wis*1.8","element":"metal","target":"single"}]'::jsonb,'WEAPON_TYPE''SWORD',10,5,NULL,'["sword","attack","movement","basic"]'::jsonb,5),
('万剑归宗','御剑术至高境界——万剑齐发遮天蔽日消耗极其恐怖。段子:"万剑归宗是一种气势至于真有没有一万把——别问问就是艺术夸张。"','ACTIVE','[{"type":"DAMAGE","formula":"atk*3.0","element":"metal","target":"single"}]'::jsonb,'WEAPON_TYPE''SWORD',60,50,(SELECT id FROM xt_skill WHERE name='御剑术'),'["sword","attack","ultimate","epic"]'::jsonb,40),
('辟邪剑谱','令人又爱又恨的剑法。精妙绝伦但修炼前提极其苛刻——练成的修士都闭口不谈。修真界最大未解之谜。','ACTIVE','[{"type":"DAMAGE","formula":"str*3.0","element":"metal","target":"single"}]'::jsonb,'WEAPON_TYPE''SWORD',30,60,(SELECT id FROM xt_skill WHERE name='御剑术'),'["sword","attack","legendary"]'::jsonb,60),
('斩天剑诀','名字响亮效果惊人。据说练到极致一剑斩开天空——当然只是寓意。','ACTIVE','[{"type":"DAMAGE","formula":"str*2.5","element":"metal","target":"single"}]'::jsonb,'WEAPON_TYPE''SWORD',40,40,(SELECT id FROM xt_skill WHERE name='万剑归宗'),'["sword","attack","epic"]'::jsonb,35),
('焚天诀','引天地真火焚尽一切。不要在森林/福地/室内使用——打架除外。','ACTIVE','[{"type":"DAMAGE","formula":"wis*2.8","element":"fire","target":"single"}]'::jsonb,'NONE',NULL,35,35,(SELECT id FROM xt_skill WHERE name='火球术'),'["fire","attack","epic"]'::jsonb,35),
('夺灵术','强行吸收残余灵力续命——"打得越久我越强"——前提你活着。','ACTIVE','[{"type":"LIFESTEAL","value":0.3},{"type":"HEAL","value":0.2}]'::jsonb,'NONE',NULL,25,35,(SELECT id FROM xt_skill WHERE name='吸星大法'),'["dark","drain","heal"]'::jsonb,40),
('千锋斩','灵力化作千道剑刃瞬间斩出。段子:"千锋斩——九百九十九道斩空气剩下一道看缘分。"','ACTIVE','[{"type":"MULTI_HIT","formula":"atk*0.8","element":"metal","target":"single"}]'::jsonb,'WEAPON_TYPE''SWORD',25,30,(SELECT id FROM xt_skill WHERE name='御剑术'),'["sword","attack","multi_hit"]'::jsonb,25),
('寒冰掌','掌心凝聚极寒之力——一掌拍出冻天冻地。冰修名言:"你跑跑得过我的冰封吗?"冻住概率看人品。','ACTIVE','[{"type":"DAMAGE","formula":"wis*1.8","element":"ice","target":"single"},{"type":"FREEZE","value":0.4,"duration":2,"element":"ice"}]'::jsonb,'NONE',NULL,18,25,(SELECT id FROM xt_skill WHERE name='冰封咒'),'["ice","attack","control","freeze"]'::jsonb,20),
('九天雷动','引九天之雷降世群攻版——但雷电敌我不分。事故:"某散修团战放出九天雷动全队团灭——包括敌人。"','ACTIVE','[{"type":"AOE_DAMAGE","formula":"wis*2.0","element":"lightning","target":"aoe"}]'::jsonb,'NONE',NULL,30,40,(SELECT id FROM xt_skill WHERE name='天雷诀'),'["lightning","attack","aoe","epic"]'::jsonb,35),
('不灭金身','体表凝聚金色护甲防御倍增+微量回血。某高僧被打了三天三夜悟出。"练到第九层天劫都懒得劈——反正打不死省电。"','PASSIVE','[{"type":"DEFENSE_BUFF","value":0.6},{"type":"HEAL","value":0.05}]'::jsonb,'NONE',NULL,0,60,(SELECT id FROM xt_skill WHERE name='金刚不坏'),'["defense","holy","heal","passive","legendary"]'::jsonb,55),
('寂灭雷音','雷电+沉默神技。法修对轰时放出直接终结战斗——对面哑了。','ACTIVE','[{"type":"DAMAGE","formula":"wis*2.2","element":"lightning","target":"single"},{"type":"SILENCE","value":1.0,"duration":2}]'::jsonb,'NONE',NULL,35,50,(SELECT id FROM xt_skill WHERE name='幻术·镜花水月'),'["lightning","attack","silence","control"]'::jsonb,50),
('诛仙剑意','万剑归宗进阶心法——招式易学境界难悟。万剑归宗六层以上才可能顿悟。争论千年:"要万剑归宗六层?告辞。"','ACTIVE','[{"type":"DAMAGE","formula":"str*4.0","element":"metal","target":"single"},{"type":"ARMOR_BREAK","value":0.5,"duration":2}]'::jsonb,'WEAPON_TYPE''SWORD',90,80,(SELECT id FROM xt_skill WHERE name='万剑归宗'),'["sword","attack","armor_break","legendary"]'::jsonb,70),
('一气化三清','道门至高绝学——巨量灵力连发三击:第一剑破防第二剑重伤第三剑人没。"第一清别人第二清自己第三清全队——但看着帅。"','ACTIVE','[{"type":"MULTI_HIT","formula":"atk*1.5","element":"qi","target":"single"}]'::jsonb,'WEAPON_CATEGORY','刀兵',120,100,NULL,'["qi","attack","multi_hit","legendary"]'::jsonb,85);

SELECT setval('xt_skill_id_seq', (SELECT COALESCE(MAX(id), 0) FROM xt_skill));
