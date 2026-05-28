-- 灵兽繁育配方种子数据
-- 同种 tag 组合可产出多种兽卵（按 weight 加权随机）

INSERT INTO xt_breeding_recipe(required_tags, result_template_id, weight, description) VALUES

('["flying","water"]',
 (SELECT id FROM xt_item_template WHERE name = '比翼鸟卵'),
 60,
 '飞禽与水族繁育，可能产出比翼鸟'),

('["flying","water"]',
 (SELECT id FROM xt_item_template WHERE name = '天马卵'),
 40,
 '飞禽与水族繁育，可能产出天马'),

('["flying","beast"]',
 (SELECT id FROM xt_item_template WHERE name = '铁羽鹰卵'),
 60,
 '飞禽与走兽繁育，可能产出铁羽鹰'),

('["flying","beast"]',
 (SELECT id FROM xt_item_template WHERE name = '金翼雕卵'),
 40,
 '飞禽与走兽繁育，可能产出金翼雕'),

('["dragon","flying"]',
 (SELECT id FROM xt_item_template WHERE name = '青鸾卵'),
 100,
 '龙族与飞禽繁育，产出青鸾'),

('["beast","water"]',
 (SELECT id FROM xt_item_template WHERE name = '虎蛟卵'),
 50,
 '走兽与水族繁育，可能产出虎蛟'),

('["beast","water"]',
 (SELECT id FROM xt_item_template WHERE name = '黑水玄蛇卵'),
 50,
 '走兽与水族繁育，可能产出黑水玄蛇'),

('["fire","beast"]',
 (SELECT id FROM xt_item_template WHERE name = '火蟾卵'),
 100,
 '火属与走兽繁育，产出火蟾'),

('["ice","beast"]',
 (SELECT id FROM xt_item_template WHERE name = '雪狐卵'),
 100,
 '冰属与走兽繁育，产出雪狐'),

('["poison","flying"]',
 (SELECT id FROM xt_item_template WHERE name = '碧鳞蛇卵'),
 100,
 '毒属与飞禽繁育，产出碧鳞蛇'),

('["myth","beast"]',
 (SELECT id FROM xt_item_template WHERE name = '乘黄卵'),
 100,
 '神话与走兽繁育，产出乘黄'),

('["defense","water"]',
 (SELECT id FROM xt_item_template WHERE name = '玄武龟卵'),
 100,
 '防御与水族繁育，产出玄武龟'),

('["phoenix","beast"]',
 (SELECT id FROM xt_item_template WHERE name = '九色鹿卵'),
 100,
 '凤凰与走兽繁育，产出九色鹿'),

('["sword","beast"]',
 (SELECT id FROM xt_item_template WHERE name = '螭龙卵'),
 100,
 '剑意与走兽繁育，产出螭龙'),

('["silk","plant"]',
 (SELECT id FROM xt_item_template WHERE name = '灵芝妖卵'),
 100,
 '灵蚕与草木繁育，产出灵芝妖'),

('["moon","flying"]',
 (SELECT id FROM xt_item_template WHERE name = '比翼鸟卵'),
 100,
 '月属与飞禽繁育，产出比翼鸟'),

('["speed","flying"]',
 (SELECT id FROM xt_item_template WHERE name = '天马卵'),
 100,
 '速属与飞禽繁育，产出天马'),

('["support","beast"]',
 (SELECT id FROM xt_item_template WHERE name = '玉兔卵'),
 100,
 '辅助与走兽繁育，产出玉兔'),

('["predator","ice"]',
 (SELECT id FROM xt_item_template WHERE name = '金翼雕卵'),
 100,
 '猛禽与冰属繁育，产出金翼雕'),

('["dragon","fire"]',
 (SELECT id FROM xt_item_template WHERE name = '火蟾卵'),
 100,
 '龙族与火属繁育，产出火蟾'),

('["ice","water"]',
 (SELECT id FROM xt_item_template WHERE name = '雪狐卵'),
 100,
 '冰属与水族繁育，产出雪狐'),

('["beast","beast"]',
 (SELECT id FROM xt_item_template WHERE name = '灵猫卵'),
 100,
 '走兽自繁，产出灵猫'),

('["fire","dragon"]',
 (SELECT id FROM xt_item_template WHERE name = '狻猊卵'),
 100,
 '火属与龙族繁育，产出狻猊'),

('["earth","auspicious"]',
 (SELECT id FROM xt_item_template WHERE name = '麒麟卵'),
 100,
 '土属与祥瑞繁育，产出麒麟'),

('["evil","beast"]',
 (SELECT id FROM xt_item_template WHERE name = '穷奇卵'),
 100,
 '凶属与走兽繁育，产出穷奇'),

('["thunder","beast"]',
 (SELECT id FROM xt_item_template WHERE name = '夔牛卵'),
 100,
 '雷属与走兽繁育，产出夔牛'),

('["phoenix","fire"]',
 (SELECT id FROM xt_item_template WHERE name = '朱雀卵'),
 100,
 '凤凰与火属繁育，产出朱雀'),

('["dragon","earth"]',
 (SELECT id FROM xt_item_template WHERE name = '白虎卵'),
 100,
 '龙族与土属繁育，产出白虎'),

('["dragon","beast"]',
 (SELECT id FROM xt_item_template WHERE name = '青龙卵'),
 100,
 '龙族与走兽繁育，产出青龙'),

('["wis","auspicious"]',
 (SELECT id FROM xt_item_template WHERE name = '白泽卵'),
 100,
 '智慧与祥瑞繁育，产出白泽'),

('["beast","poison"]',
 (SELECT id FROM xt_item_template WHERE name = '碧鳞蛇卵'),
 100,
 '走兽与毒属繁育，产出碧鳞蛇');
