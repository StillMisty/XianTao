-- 进化石种子数据 (xt_item_template, type=EVOLUTION_STONE)
-- properties: tierCap=适用最高等阶, successBonus=进化成功率加成(%)
INSERT INTO xt_item_template (name, type, properties, tags, description) VALUES
('初阶进化石', 'EVOLUTION_STONE', '{"tierCap":3,"successBonus":0}', '["evolution_stone","low"]', '微光闪烁的土黄色石头，蕴含微弱灵力，仅能让灵兽升到T3等阶。'),
('中阶进化石', 'EVOLUTION_STONE', '{"tierCap":5,"successBonus":5}', '["evolution_stone","mid"]', '碧绿莹润的灵石，灵力充沛，T5以前都用得着。'),
('高阶进化石', 'EVOLUTION_STONE', '{"tierCap":7,"successBonus":10}', '["evolution_stone","high"]', '紫气缭绕的进化石，不仅支持高阶进化，还能略微提高成功率。'),
('极品进化石', 'EVOLUTION_STONE', '{"tierCap":9,"successBonus":15}', '["evolution_stone","epic"]', '金光璀璨，T9进化专用，天道成功率加成让灵兽突破更容易。'),
('神阶进化石', 'EVOLUTION_STONE', '{"tierCap":99,"successBonus":20}', '["evolution_stone","legendary"]', '传说中的混沌级进化石，无视等阶限制且大幅提升成功率，天下仅寥寥数块。');
