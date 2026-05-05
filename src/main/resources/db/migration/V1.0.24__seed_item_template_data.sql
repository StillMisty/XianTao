-- ============================================================
-- 仙道 · xt_item_template 完整种子数据
-- 涵盖材料/种子/灵兽卵/丹药/进化石/法决玉简/丹方卷轴/药材
-- ============================================================
INSERT INTO xt_item_template (name, type, properties, tags, description) VALUES
('玄铁','material','{}'::jsonb,'["ore","metal","forge_base","common"]'::jsonb,'最基础的炼器材料，各大宗门库存按吨计——总有弟子炸炉。'),
('星辰砂','material','{}'::jsonb,'["ore","rare","forge_mid","cosmic"]'::jsonb,'星河坠落后凝结的精粹，每粒沙都闪烁微弱星光。'),
('龙鳞','material','{}'::jsonb,'["scale","dragon","forge_top","fire"]'::jsonb,'真龙蜕下的鳞片。市面上十有八九是假的——真龙鳞谁舍得卖？'),
('凤凰翎','material','{}'::jsonb,'["feather","phoenix","forge_top","fire"]'::jsonb,'凤凰涅槃落下的翎羽，蕴含不灭之火。凤凰自己都不知道它的毛这么值钱。'),
('九天息壤','material','{}'::jsonb,'["soil","divine","forge_divine","earth"]'::jsonb,'女娲补天所用神土，一小撮重若千钧。"得九天息壤者得炼器天下"——没人真见过。'),
('万年寒铁','material','{}'::jsonb,'["ore","metal","forge_high","ice","rare"]'::jsonb,'极北冰原深处的极寒铁矿，触之如握玄冰。"用寒铁炼剑冻死的比砍死的多。"'),
('血菩提','material','{}'::jsonb,'["fruit","blood","qi_boost","rare"]'::jsonb,'古战场血土之上的异果。大胆服用可短暂暴增修为，胆小者闻其来源便已腿软。'),
('舍利子','material','{}'::jsonb,'["relic","buddha","forge_divine","holy"]'::jsonb,'佛门高僧圆寂留下的圣物。有人试图用舍利子炼丹——炸炉三天三夜。'),
('幽冥铁','material','{}'::jsonb,'["ore","metal","forge_mid","dark","rare"]'::jsonb,'产自幽冥深渊的铁矿石。锻造出的法器带有暗属性伤害加成。'),
('太阳真金','material','{}'::jsonb,'["ore","metal","forge_high","fire","sun","rare"]'::jsonb,'太阳星核中诞生的金属。打造的兵刃对邪祟有额外伤害加成。'),
('火灵石','material','{}'::jsonb,'["crystal","fire","forge_mid","common"]'::jsonb,'烈焰荒漠特产。采石工自嘲："在火坑里捡石头——字面意义。"'),
('星核碎片','material','{}'::jsonb,'["ore","cosmic","forge_top","light","rare"]'::jsonb,'流星坠落后的内核碎片，蕴含星辰本源之力。有人专门收藏当文玩。'),
('万年玄冰','material','{}'::jsonb,'["crystal","ice","forge_high","legendary"]'::jsonb,'极北冰原深处玄冰。历经万年寒气淬炼不化——但锻造时会在炉子里化。'),
('魔血石','material','{}'::jsonb,'["ore","dark","forge_mid","blood","rare"]'::jsonb,'魔血浸染的矿石。魔修奉至宝正修避之不及。散修态度："管它正邪好用就行。"'),
('虚空结晶','material','{}'::jsonb,'["crystal","void","forge_top","chaos","epic"]'::jsonb,'虚空裂缝中凝结的晶石。锻造时总是莫名其妙消失——虚空嘛。'),
('神血石','material','{}'::jsonb,'["ore","holy","forge_divine","blood","legendary"]'::jsonb,'上古神祇之血凝结的宝石。碰到而不被震飞的修士没几个。'),
('混沌元晶','material','{}'::jsonb,'["crystal","chaos","forge_divine","void","legendary"]'::jsonb,'混沌本源凝聚的晶体。价值无法以灵石衡量——它本身就是灵石的终极形态。'),
('天道碎片','material','{}'::jsonb,'["shard","divine","forge_divine","dao","divine"]'::jsonb,'大道碎裂后的碎片。拿到剑道悟逆天剑招，拿到摸鱼道修炼反而快三倍。'),
('聚灵草种子','seed','{"grow_time":2,"reharvest":0}'::jsonb,'["seed","herb","common","qi"]'::jsonb,'最基础灵植种子。两天收获。外门弟子几乎都从种这个开始修仙。'),
('凝气花种子','seed','{"grow_time":6,"reharvest":0}'::jsonb,'["seed","flower","common","qi"]'::jsonb,'花朵盛开时自然凝聚灵气。野外种的被路过的妖怪顺手薅走。'),
('朱果种子','seed','{"grow_time":12,"reharvest":1}'::jsonb,'["seed","fruit","fire","rare"]'::jsonb,'火属性灵植种子。梗："种朱果的时间够你从筑基修到金丹了。"'),
('冰莲种子','seed','{"grow_time":18,"reharvest":1}'::jsonb,'["seed","flower","ice","rare"]'::jsonb,'极寒莲花种子。某火灵根修士偏要种这个——福地整块田冻住了。'),
('千年灵芝孢子','seed','{"grow_time":24,"reharvest":0}'::jsonb,'["seed","fungus","epic","wood"]'::jsonb,'极品灵芝种子。名言："没什么丹是不能加灵芝的，如果有就加两棵。"'),
('九转金莲种子','seed','{"grow_time":48,"reharvest":0}'::jsonb,'["seed","flower","legendary","holy","gold"]'::jsonb,'佛祖座下金莲种子——没人种到过九转，不是被抢就是枯了。'),
('火灵芝孢子','seed','{"grow_time":10,"reharvest":0}'::jsonb,'["seed","fungus","fire","rare"]'::jsonb,'火山口灵植。某炼丹师种了一片福地温度升到五十度——改名"福地毁灭者"。'),
('星辰花种子','seed','{"grow_time":16,"reharvest":1}'::jsonb,'["seed","flower","cosmic","epic"]'::jsonb,'只在星光下开放的花。美到让人忘记摘——第二天谢了。'),
('血莲种子','seed','{"grow_time":20,"reharvest":0}'::jsonb,'["seed","flower","blood","dark","epic"]'::jsonb,'精血浇灌的莲花。每天一滴血坚持二十天——花开了你也贫血了。'),
('混沌藤种子','seed','{"grow_time":36,"reharvest":0}'::jsonb,'["seed","vine","chaos","legendary"]'::jsonb,'混沌藤蔓的种子。开花时爆发混沌之气随机变异福地所有作物。'),
('天道花种子','seed','{"grow_time":72,"reharvest":0}'::jsonb,'["seed","flower","dao","divine"]'::jsonb,'每颗种子都藏着一条大道。开花时种花者顿悟——至于悟了什么说不准。'),
('灵茶树苗','seed','{"grow_time":30,"reharvest":1}'::jsonb,'["seed","tree","wood","qi","epic"]'::jsonb,'产出灵茶的仙树幼苗。修仙界社交神器——"来我福地喝杯灵茶"抵十万灵石。'),
('不死草种子','seed','{"grow_time":48,"reharvest":0}'::jsonb,'["seed","herb","holy","divine"]'::jsonb,'吃一片能起死回生的仙草——但种不出来条件苛刻到离谱。'),
('培元丹','potion','{"effects":[{"type":"exp","amount":100}]}'::jsonb,'["pill","exp","common","beginner"]'::jsonb,'最基础修炼丹药100点经验。连炸炉都不够资格。'),
('凝气丹','potion','{"effects":[{"type":"exp","amount":300}]}'::jsonb,'["pill","exp","common"]'::jsonb,'培元丹三倍效果。外门弟子当糖豆吃。"根基虚浮得跟豆腐渣一样。"'),
('筑基丹','potion','{"effects":[{"type":"exp","amount":800},{"type":"breakthrough","rate":0.1}]}'::jsonb,'["pill","exp","breakthrough","rare","qi"]'::jsonb,'突破筑基境必备。有经验的修士都准备三颗以上——第一颗总是莫名其妙炸。'),
('洗髓丹','potion','{"effects":[{"type":"stat","stat_attr":"reset","amount":1}]}'::jsonb,'["pill","stat_reset","rare","body"]'::jsonb,'洗经伐髓重置四维属性。"加错点不要紧一颗洗髓丹重新开始——你确定那几点值得？"'),
('回春丹','potion','{"effects":[{"type":"hp","amount":500}]}'::jsonb,'["pill","heal","common","wood"]'::jsonb,'恢复500点HP。冷笑话："最贵的丹药不是最好的是你快死的时候刚好有那么一颗。"'),
('大还丹','potion','{"effects":[{"type":"hp","amount":1500}]}'::jsonb,'["pill","heal","rare","wood"]'::jsonb,'1500点HP——修士的第二条命。各大宗门长老必备——活得久的人最怕死。'),
('破障丹','potion','{"effects":[{"type":"breakthrough","rate":0.3}]}'::jsonb,'["pill","breakthrough","epic","mind"]'::jsonb,'30%额外突破成功率。潜规则：没有三颗破障丹不配渡天劫。'),
('九转还魂丹','potion','{"effects":[{"type":"hp","amount":9999},{"type":"breakthrough","rate":0.5},{"type":"exp","amount":5000}]}'::jsonb,'["pill","heal","breakthrough","legendary","divine"]'::jsonb,'传说中的圣药能起死回生。"能炼出九转还魂丹的人根本不需要吃它。"'),
('风行丹','potion','{"effects":[{"type":"buff","attribute":"speed","amount":10,"duration_seconds":1800}]}'::jsonb,'["pill","buff","speed","common"]'::jsonb,'服用后身轻如燕敏捷提升30分钟。打不过就跑的最佳辅助。'),
('金刚丹','potion','{"effects":[{"type":"buff","attribute":"defense","amount":20,"duration_seconds":1800}]}'::jsonb,'["pill","buff","defense","rare"]'::jsonb,'服用后皮糙肉厚。"金刚丹+运气好=苟住一命。"'),
('聚元丹','potion','{"effects":[{"type":"exp","amount":2000}]}'::jsonb,'["pill","exp","rare","qi"]'::jsonb,'高阶修炼丹药2000点经验。元婴期修士当零食。金丹期以下吃一颗白练三天。'),
('破境丹','potion','{"effects":[{"type":"breakthrough","rate":0.2}]}'::jsonb,'["pill","breakthrough","epic","mind"]'::jsonb,'大境界突破专用20%加成。"不是给修为吃的是给心脏吃的——吃了不紧张。"'),
('生生造化丹','potion','{"effects":[{"type":"hp","amount":3000}]}'::jsonb,'["pill","heal","epic","wood"]'::jsonb,'回血终极选择3000点HP——这已经不是救命丹药是"第二条命"。'),
('九转金丹','potion','{"effects":[{"type":"exp","amount":10000},{"type":"breakthrough","rate":0.5}]}'::jsonb,'["pill","exp","breakthrough","divine","legendary"]'::jsonb,'太上老君亲炼。张三吃了暴涨三个大境界——然后被灵力撑炸。'),
('避毒丹','potion','{"effects":[{"type":"cure","status":"poisoned"},{"type":"buff","attribute":"poison_resist","amount":50,"duration_seconds":1800}]}'::jsonb,'["pill","cure","defense","rare"]'::jsonb,'解毒+抗毒30分钟。沼泽冒险不带的都是在赌命。'),
('定颜丹','potion','{"effects":[{"type":"buff","attribute":"charisma","amount":80,"duration_seconds":7200}]}'::jsonb,'["pill","buff","cosmetic","rare"]'::jsonb,'两小时最佳容颜。女性修士最爱——价格被炒到同品级十倍。'),
('狂暴丹','potion','{"effects":[{"type":"buff","attribute":"attack","amount":40,"duration_seconds":900}]}'::jsonb,'["pill","buff","attack","epic"]'::jsonb,'十五分钟攻击力大幅提升——药效过后虚脱。赌徒之选。'),
('涅槃丹','potion','{"effects":[{"type":"hp","amount":0,"percentage":1.0},{"type":"cure","status":"all"}]}'::jsonb,'["pill","heal","revive","legendary","phoenix"]'::jsonb,'回满血+清除所有负面状态。终极保险。"这颗涅槃丹我留给下辈子——哦吃了就没下辈子了。"'),
('神行丹','potion','{"effects":[{"type":"buff","attribute":"speed","amount":30,"duration_seconds":3600}]}'::jsonb,'["pill","buff","speed","rare"]'::jsonb,'风行丹升级版——加速+减速免疫。跑路专用且姿势潇洒。'),
('破魔丹','potion','{"effects":[{"type":"buff","attribute":"magic_resist","amount":60,"duration_seconds":1800}]}'::jsonb,'["pill","buff","defense","epic"]'::jsonb,'半小时大幅提升术法抗性。打法系BOSS必需——不吃就被一套秒。'),
('悟道丹','potion','{"effects":[{"type":"stat","stat_attr":"wis","amount":5}]}'::jsonb,'["pill","stat_boost","wis","divine"]'::jsonb,'直接提升5点悟性。先吃再修百年比修了再吃划算——但得先有悟道丹。'),
('破劫丹','potion','{"effects":[{"type":"breakthrough","rate":0.4}]}'::jsonb,'["pill","breakthrough","divine","legendary"]'::jsonb,'40%突破成功率叠加破障丹到70%！但原料含天道碎片——比破劫丹还稀有。第一颗从哪来？没人知道。'),
('进化石·凡','evolution_stone','{"evolution_tier":1,"success_rate":100,"max_quality":"spirit"}'::jsonb,'["evolution_stone","common","beast"]'::jsonb,'最基础100%成功率仅用于凡品灵品。虽说是100%——总有倒霉蛋。'),
('进化石·灵','evolution_stone','{"evolution_tier":2,"success_rate":80,"max_quality":"immortal"}'::jsonb,'["evolution_stone","rare","beast"]'::jsonb,'80%成功率适用于仙品以下。散修统计一半失败过——幸存者偏差。'),
('进化石·仙','evolution_stone','{"evolution_tier":3,"success_rate":60,"max_quality":"saint"}'::jsonb,'["evolution_stone","epic","beast"]'::jsonb,'60%成功率圣品以下。失败可能倒退——赌狗慎入。'),
('进化石·圣','evolution_stone','{"evolution_tier":4,"success_rate":30,"max_quality":"divine"}'::jsonb,'["evolution_stone","legendary","beast","divine"]'::jsonb,'30%成功传说级。失败退化到凡品——主人心态随之崩了。'),
('进化石·神','evolution_stone','{"evolution_tier":5,"success_rate":15,"max_quality":"divine"}'::jsonb,'["evolution_stone","divine","beast"]'::jsonb,'15%成功率——成功神品失败凡品。"一次天堂一次地狱——灵兽变回蛋。"'),
('血脉进化石','evolution_stone','{"evolution_tier":3,"success_rate":75,"max_quality":"immortal"}'::jsonb,'["evolution_stone","rare","beast","blood"]'::jsonb,'远古灵兽精血炼制成功率略高但消耗灵兽寿命。"灵兽的命也是命"vs"我的灵石呢？"'),
('混沌进化石','evolution_stone','{"evolution_tier":4,"success_rate":50,"max_quality":"divine"}'::jsonb,'["evolution_stone","legendary","beast","chaos"]'::jsonb,'50%成功但技能池随机洗牌。确定性爱好者不用惊喜追求者天天用。'),
('聚灵草','herb','{"grade":1,"elements":{"qi":1}}'::jsonb,'["herb","qi","common","material_herb"]'::jsonb,'最基础低阶药草。"炼丹不从聚灵草开始你的灵石就不是你的。"'),
('凝气花','herb','{"grade":1,"elements":{"qi":2}}'::jsonb,'["herb","qi","common","flower"]'::jsonb,'不知道加什么又不想用灵芝就用凝气花——"凝气花式摆烂"。'),
('朱果','herb','{"grade":2,"elements":{"fire":3,"qi":1}}'::jsonb,'["herb","fire","rare","fruit"]'::jsonb,'火山口火属性灵果。采摘需守三天三夜——最难受的不是热是无聊。'),
('千年灵芝','herb','{"grade":3,"elements":{"wood":5,"qi":2}}'::jsonb,'["herb","wood","epic","fungus"]'::jsonb,'理论千年——大多催熟效果打八折。良心标注"百年催熟"。'),
('冰莲花','herb','{"grade":3,"elements":{"ice":5,"qi":2}}'::jsonb,'["herb","ice","epic","flower"]'::jsonb,'极北冰原特产。采摘时需心如止水——所以最高境界是"心如死灰"。'),
('龙血草','herb','{"grade":4,"elements":{"fire":5,"qi":3}}'::jsonb,'["herb","fire","legendary","dragon"]'::jsonb,'被龙血浇灌的草。真正的龙血草一只手数得过来——其他都是染红的。'),
('九幽花','herb','{"grade":4,"elements":{"dark":5,"qi":3}}'::jsonb,'["herb","dark","legendary","flower"]'::jsonb,'黑色花瓣散发幽光。触摸久会涌现负面情绪——花在考验道心。'),
('菩提子','herb','{"grade":5,"elements":{"holy":5,"qi":5}}'::jsonb,'["herb","holy","legendary","buddha"]'::jsonb,'佛门圣菩提树果实一颗抵百万修炼。市面上八成假两成陷阱——小心。'),
('火灵芝','herb','{"grade":2,"elements":{"fire":3,"qi":2}}'::jsonb,'["herb","fire","rare","fungus"]'::jsonb,'火山口催生灵芝——大多百年催熟药效打八折。'),
('星辰花瓣','herb','{"grade":3,"elements":{"qi":4,"light":3}}'::jsonb,'["herb","cosmic","epic","flower"]'::jsonb,'星辰花谢落后花瓣须瞬间收集——差一秒化星光。最可怕：守六时辰——花要谢了——打个喷嚏——花没了。'),
('血莲花','herb','{"grade":4,"elements":{"dark":5,"qi":3,"blood":2}}'::jsonb,'["herb","dark","legendary","flower"]'::jsonb,'精血浇灌的血莲。采摘时会发出叹息——大多数修士产生"我在杀生"的负担。'),
('灵茶叶','herb','{"grade":3,"elements":{"wood":3,"qi":3}}'::jsonb,'["herb","tea","wood","epic"]'::jsonb,'灵茶树嫩叶。泡出的茶让方圆十里修士闻香而来——发现没自己份。"十大社交尴尬制造者"。'),
('虚空草','herb','{"grade":5,"elements":{"void":5,"qi":4}}'::jsonb,'["herb","void","legendary"]'::jsonb,'触碰时会短暂传送——采一颗平均被传送七次。最惨：被传到BOSS面前。'),
('天道花瓣','herb','{"grade":5,"elements":{"holy":5,"qi":5,"dao":3}}'::jsonb,'["herb","holy","divine","flower"]'::jsonb,'一片可替代任意同属性药材——炼丹界万能替代品。但没人用过——种出天道花的修士比用过它的还少。');

-- ============================================================
-- 仙道 · 第三批：灵兽卵 (BEAST_EGG)
-- ============================================================
INSERT INTO xt_item_template (name, type, properties, tags, description) VALUES
('火狐兽卵','beast_egg',jsonb_build_object('grow_time',36,'production_items',jsonb_build_array(jsonb_build_object('weight',50,'template_id',(SELECT id FROM xt_item_template WHERE name='火灵芝'),'name','火灵芝'),jsonb_build_object('weight',30,'template_id',(SELECT id FROM xt_item_template WHERE name='朱果'),'name','朱果'),jsonb_build_object('weight',20,'template_id',(SELECT id FROM xt_item_template WHERE name='凝气花'),'name','凝气花')),'skill_pool',jsonb_build_object('innate_skills',jsonb_build_array(jsonb_build_object('skill_id',1,'unlock','birth'),jsonb_build_object('skill_id',6,'unlock','tier_2'),jsonb_build_object('skill_id',32,'unlock','tier_3')),'awakening_skills',jsonb_build_array(jsonb_build_object('skill_id',18,'weight',30),jsonb_build_object('skill_id',33,'weight',70)))),'["beast_egg","fire","fox","rare","tier2"]'::jsonb,'火属性灵兽之卵，孵化可得火狐。幼兽形态为三尾火狐，成年后烈焰缠身——但得先熬过它把福地当砂盆的时期。'),
('冰凤兽卵','beast_egg',jsonb_build_object('grow_time',60,'production_items',jsonb_build_array(jsonb_build_object('weight',50,'template_id',(SELECT id FROM xt_item_template WHERE name='冰莲花'),'name','冰莲花'),jsonb_build_object('weight',30,'template_id',(SELECT id FROM xt_item_template WHERE name='星辰花瓣'),'name','星辰花瓣'),jsonb_build_object('weight',20,'template_id',(SELECT id FROM xt_item_template WHERE name='灵茶叶'),'name','灵茶叶')),'skill_pool',jsonb_build_object('innate_skills',jsonb_build_array(jsonb_build_object('skill_id',6,'unlock','birth'),jsonb_build_object('skill_id',35,'unlock','tier_2')),'awakening_skills',jsonb_build_array(jsonb_build_object('skill_id',36,'weight',40),jsonb_build_object('skill_id',3,'weight',60)))),'["beast_egg","ice","phoenix","epic","tier3"]'::jsonb,'冰凤血脉的灵兽卵。冰凤虽属凤凰旁支，但脾气一点不小——主人需做好被冻床的心理准备。'),
('石龟兽卵','beast_egg',jsonb_build_object('grow_time',18,'production_items',jsonb_build_array(jsonb_build_object('weight',60,'template_id',(SELECT id FROM xt_item_template WHERE name='聚灵草'),'name','聚灵草'),jsonb_build_object('weight',40,'template_id',(SELECT id FROM xt_item_template WHERE name='凝气花'),'name','凝气花')),'skill_pool',jsonb_build_object('innate_skills',jsonb_build_array(jsonb_build_object('skill_id',3,'unlock','birth'),jsonb_build_object('skill_id',14,'unlock','tier_2')),'awakening_skills',jsonb_build_array(jsonb_build_object('skill_id',37,'weight',50),jsonb_build_object('skill_id',9,'weight',50)))),'["beast_egg","earth","turtle","common","tier1"]'::jsonb,'石龟兽卵，入门灵兽不二之选。优点是皮糙肉厚能挡伤害——缺点是走得慢，战斗结束它还在路上。'),
('雷鹰兽卵','beast_egg',jsonb_build_object('grow_time',96,'production_items',jsonb_build_array(jsonb_build_object('weight',50,'template_id',(SELECT id FROM xt_item_template WHERE name='星辰花瓣'),'name','星辰花瓣'),jsonb_build_object('weight',30,'template_id',(SELECT id FROM xt_item_template WHERE name='虚空草'),'name','虚空草'),jsonb_build_object('weight',20,'template_id',(SELECT id FROM xt_item_template WHERE name='灵茶叶'),'name','灵茶叶')),'skill_pool',jsonb_build_object('innate_skills',jsonb_build_array(jsonb_build_object('skill_id',2,'unlock','birth'),jsonb_build_object('skill_id',36,'unlock','tier_2'),jsonb_build_object('skill_id',38,'unlock','quality_break')),'awakening_skills',jsonb_build_array(jsonb_build_object('skill_id',25,'weight',50),jsonb_build_object('skill_id',22,'weight',50)))),'["beast_egg","lightning","hawk","epic","tier4"]'::jsonb,'雷鹰乃九天之上的雷兽血脉。卵壳有微弱电光——不小心碰到头发竖三天。孵化后雷鹰飞行速度冠绝灵兽，缺点是掉毛掉得满福地都是。'),
('幽影猫兽卵','beast_egg',jsonb_build_object('grow_time',30,'production_items',jsonb_build_array(jsonb_build_object('weight',50,'template_id',(SELECT id FROM xt_item_template WHERE name='聚灵草'),'name','聚灵草'),jsonb_build_object('weight',30,'template_id',(SELECT id FROM xt_item_template WHERE name='火灵芝'),'name','火灵芝'),jsonb_build_object('weight',20,'template_id',(SELECT id FROM xt_item_template WHERE name='虚空草'),'name','虚空草')),'skill_pool',jsonb_build_object('innate_skills',jsonb_build_array(jsonb_build_object('skill_id',25,'unlock','birth'),jsonb_build_object('skill_id',33,'unlock','tier_2')),'awakening_skills',jsonb_build_array(jsonb_build_object('skill_id',8,'weight',30),jsonb_build_object('skill_id',16,'weight',70)))),'["beast_egg","dark","cat","rare","tier2"]'::jsonb,'幽影猫通体漆黑，两耳发紫光。白天躲进影子，晚上才愿意活动——像个猫形态的夜班保安。某散修："自从养了它，再也没有妖兽半夜来偷田里的灵芝。"'),
('金麟兽卵','beast_egg',jsonb_build_object('grow_time',130,'production_items',jsonb_build_array(jsonb_build_object('weight',40,'template_id',(SELECT id FROM xt_item_template WHERE name='龙血草'),'name','龙血草'),jsonb_build_object('weight',30,'template_id',(SELECT id FROM xt_item_template WHERE name='菩提子'),'name','菩提子'),jsonb_build_object('weight',30,'template_id',(SELECT id FROM xt_item_template WHERE name='天道花瓣'),'name','天道花瓣')),'skill_pool',jsonb_build_object('innate_skills',jsonb_build_array(jsonb_build_object('skill_id',19,'unlock','birth'),jsonb_build_object('skill_id',23,'unlock','tier_2'),jsonb_build_object('skill_id',37,'unlock','tier_3'),jsonb_build_object('skill_id',40,'unlock','quality_break')),'awakening_skills',jsonb_build_array(jsonb_build_object('skill_id',21,'weight',40),jsonb_build_object('skill_id',28,'weight',60)))),'["beast_egg","metal","qilin","legendary","tier5"]'::jsonb,'传说中麒麟血脉的纯正灵兽——卵壳泛金光，入手温热。修真界最终极灵兽之选。当然，能不能孵出来是另一回事——到目前为止成功的传闻还没得到证实。');

-- ============================================================
-- 仙道 · 第四批：法决玉简 (SKILL_JADE)
-- ============================================================
INSERT INTO xt_item_template (name, type, properties, tags, description) VALUES
('火球术玉简','skill_jade','{"skill_id":1}'::jsonb,'["skill_jade","fire","basic"]'::jsonb,'记载火球术的法决玉简。贴在额头即可感悟——前提是你的神识够强不会头晕。修真界最畅销的玉简便携版。'),
('金刚不坏玉简','skill_jade','{"skill_id":3}'::jsonb,'["skill_jade","defense","holy","rare"]'::jsonb,'佛门护体神功残篇玉简。"挨打不是修行——但金钟罩级别的挨打是。先学这个。"'),
('遁地术玉简','skill_jade','{"skill_id":4}'::jsonb,'["skill_jade","earth","movement","rare"]'::jsonb,'逃命三大法宝之首——记载遁地术。温馨提示：在地下遇到石头绕路，别硬钻——已经有三个修士卡在石头里喊了一整天。'),
('轻身术玉简','skill_jade','{"skill_id":5}'::jsonb,'["skill_jade","movement","basic"]'::jsonb,'轻身术——修真界共识：可以不会攻击法决，但不能不会轻身术。打不过就跑是修仙的基本素养。'),
('冰封咒玉简','skill_jade','{"skill_id":6}'::jsonb,'["skill_jade","ice","control","rare"]'::jsonb,'冰封咒——伤害加冰冻控制。某散修评价："冻住一个敌人三秒，够你跑十丈——不是逃跑是我在找更好的施法角度。"'),
('回春术玉简','skill_jade','{"skill_id":9}'::jsonb,'["skill_jade","wood","heal","rare"]'::jsonb,'回春术——治疗法术之根基。名言："活得久才有输出。会治疗的修士打群架从来不缺人请。"'),
('大摧山掌玉简','skill_jade','{"skill_id":13}'::jsonb,'["skill_jade","earth","str","physical"]'::jsonb,'大摧山掌——体修最爱。不需要悟性不需要法阵，一只手就够了。一位体修的临终遗言："帮我把这掌劈出去……不劈完不闭眼……"'),
('金钟罩玉简','skill_jade','{"skill_id":14}'::jsonb,'["skill_jade","defense","holy","body"]'::jsonb,'金钟罩完整版——佛门护体第一神功。修炼要点：站着挨打就行。建议搭配回春术使用效果翻倍。'),
('清风诀玉简','skill_jade','{"skill_id":18}'::jsonb,'["skill_jade","wind","support","speed"]'::jsonb,'清风诀——加速加防双光环。某玩家号称三速Buff叠满（轻身+幽影+清风）天劫都追不上他——然后发现跑太快撞进更高阶怪物区了。'),
('御剑术玉简','skill_jade','{"skill_id":28}'::jsonb,'["skill_jade","sword","attack","basic"]'::jsonb,'御剑术——剑修入门必修。学会后可以御剑飞行——虽然速度还不如走路但帅就完了。名场面："道友你的剑……它自己飞走了。"');

-- ============================================================
-- 仙道 · 第五批：丹方卷轴 (RECIPE_SCROLL)
-- ============================================================
INSERT INTO xt_item_template (name, type, properties, tags, description) VALUES
('培元丹方','recipe_scroll',jsonb_build_object('grade',1,'product',jsonb_build_object('item_id',(SELECT id FROM xt_item_template WHERE name='培元丹'),'quantity',1),'requirements',jsonb_build_array(jsonb_build_object('element','qi','min',3,'max',20))),'["recipe_scroll","pill","basic"]'::jsonb,'培元丹炼制丹方。最低门槛的炼丹入门——3份聚灵草或2份凝气花即可开炉。新手入门必备，炸炉率30%（其实挺低的了）。'),
('凝气丹方','recipe_scroll',jsonb_build_object('grade',1,'product',jsonb_build_object('item_id',(SELECT id FROM xt_item_template WHERE name='凝气丹'),'quantity',1),'requirements',jsonb_build_array(jsonb_build_object('element','qi','min',6,'max',30))),'["recipe_scroll","pill","common"]'::jsonb,'凝气丹炼制丹方。培元丹的进阶版——三倍药效同样的药材只需要多放一倍。"药量翻倍不代表效果翻倍——但至少看起来有诚意。"'),
('回春丹方','recipe_scroll',jsonb_build_object('grade',2,'product',jsonb_build_object('item_id',(SELECT id FROM xt_item_template WHERE name='回春丹'),'quantity',1),'requirements',jsonb_build_array(jsonb_build_object('element','wood','min',3,'max',20),jsonb_build_object('element','qi','min',1,'max',15))),'["recipe_scroll","pill","heal","wood"]'::jsonb,'回春丹炼制丹方。木属性药材为主——一棵千年灵芝就够做一堆。据说丹方作者是个经常被打得只剩半条命的修士——"炼丹不是为了变强是为了活到下药发挥作用。"'),
('筑基丹方','recipe_scroll',jsonb_build_object('grade',3,'product',jsonb_build_object('item_id',(SELECT id FROM xt_item_template WHERE name='筑基丹'),'quantity',1),'requirements',jsonb_build_array(jsonb_build_object('element','qi','min',5,'max',25),jsonb_build_object('element','fire','min',2,'max',12))),'["recipe_scroll","pill","breakthrough","rare"]'::jsonb,'筑基丹炼制丹方。突破筑基境的必备辅助——火属性药材激发药力，气属性稳固根基。大多数散修的第一颗筑基丹都炼失败了——第二颗也是。'),
('破障丹方','recipe_scroll',jsonb_build_object('grade',4,'product',jsonb_build_object('item_id',(SELECT id FROM xt_item_template WHERE name='破障丹'),'quantity',1),'requirements',jsonb_build_array(jsonb_build_object('element','dark','min',3,'max',20),jsonb_build_object('element','qi','min',3,'max',15))),'["recipe_scroll","pill","breakthrough","epic"]'::jsonb,'破障丹炼制丹方——修真界最珍贵的配方之一。需暗属性药材打破心魔桎梏，配以灵气稳固道心。配方来源成谜——传说第一个写出丹方的人被正邪两道同时追杀。'),
('风行丹方','recipe_scroll',jsonb_build_object('grade',2,'product',jsonb_build_object('item_id',(SELECT id FROM xt_item_template WHERE name='风行丹'),'quantity',1),'requirements',jsonb_build_array(jsonb_build_object('element','qi','min',4,'max',20))),'["recipe_scroll","pill","speed","common"]'::jsonb,'风行丹炼制丹方。最基本的增益类丹药——跑路流入门丹。某散修靠这个丹方垄断了整个青云山外门的逃跑市场。"跑得快就是生产力。"');

SELECT setval('xt_item_template_id_seq', (SELECT COALESCE(MAX(id), 0) FROM xt_item_template));
