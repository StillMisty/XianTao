INSERT INTO xt_spirit_form (id, name, description, liked_tags, disliked_tags) VALUES
(1, '小狐妖', '一只毛茸茸的九尾幼狐，喜欢蹭人但傲娇不承认，九条尾巴是她的骄傲。', '["wine","art","book","prestige","shiny","jadeite","peach"]'::jsonb, '["common","ore","forge_base","bone","slime","insect"]'::jsonb),
(2, '春秋蝉', '晶莹剔透的金蝉，趴在耳畔低语时间之道。看尽时光流转，对凡间事物淡然处之。', '["time","medicinal","art","book","jadeite","sacred","prestige"]'::jsonb, '["common","forge_base","ore","bone","beast","meat"]'::jsonb),
(3, '石中鲤', '在一块灵石中游弋的小锦鲤，据说是远古大能的宠物遗种。能带来好运。', '["gem","ore","shiny","jadeite","water","sacred","light"]'::jsonb, '["common","weed","slime","bone","insect","rotten"]'::jsonb),
(4, '剑灵残片', '上古神剑的碎片所化剑灵，身负剑意，性格冷傲。对弱者不屑一顾。', '["weapon","forge","forge_base","metal","sharp","sacred","blood"]'::jsonb, '["common","weed","flower","food","sweet","toy"]'::jsonb),
(5, '千年何首乌', '一只成了精的何首乌，长着人形的根须，走路一蹦一跳。说话喜欢引经据典像个老学究。', '["medicinal","herb","book","wood","time","sacred","art"]'::jsonb, '["metal","forge","forge_base","beast","blood","ore"]'::jsonb),
(6, '乌云踏雪', '一只通体乌黑的麒麟幼崽，四蹄踏雪，性格活泼好动。最喜欢在灵田里打滚。', '["beast","sacred","shiny","prestige","food","meat","sweet"]'::jsonb, '["common","weed","slime","insect","rotten","bone"]'::jsonb),
(7, '酒葫芦', '一个成了精的紫金酒葫芦，据说里面装着仙人酿的琼浆。性格豪爽，说话带三分醉意。', '["wine","food","sweet","fruit","art","book","jadeite"]'::jsonb, '["common","weed","ore","forge_base","slime","bone"]'::jsonb);

SELECT setval('xt_spirit_form_id_seq', 7);
