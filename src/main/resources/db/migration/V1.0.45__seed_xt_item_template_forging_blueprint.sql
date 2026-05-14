-- 锻造图纸种子数据 (xt_item_template, type=FORGING_BLUEPRINT)
INSERT INTO xt_item_template (name, type, properties, tags, description)
VALUES
('砍柴刀图', 'FORGING_BLUEPRINT',
  jsonb_build_object(
    'equipment_template_id', (SELECT id FROM xt_equipment_template WHERE name = '砍柴刀'),
    'grade', 1,
    'requirements', '{"RIGIDITY":{"min":5,"max":15},"TOUGHNESS":{"min":3,"max":10},"SPIRIT":{"min":0,"max":5}}'::jsonb
  ),
  '["blueprint","forge","blade","entry"]'::jsonb,
  '砍柴刀的锻造图纸，入门必修，村头铁匠都会。'),

('青锋刀图', 'FORGING_BLUEPRINT',
  jsonb_build_object(
    'equipment_template_id', (SELECT id FROM xt_equipment_template WHERE name = '青锋刀'),
    'grade', 2,
    'requirements', '{"RIGIDITY":{"min":12,"max":25},"TOUGHNESS":{"min":5,"max":15},"SPIRIT":{"min":2,"max":8}}'::jsonb
  ),
  '["blueprint","forge","blade","uncommon"]'::jsonb,
  '青锋刀的锻造图纸，以玄铁淬火为关键。'),

('青冥剑图', 'FORGING_BLUEPRINT',
  jsonb_build_object(
    'equipment_template_id', (SELECT id FROM xt_equipment_template WHERE name = '青冥剑'),
    'grade', 2,
    'requirements', '{"RIGIDITY":{"min":10,"max":22},"TOUGHNESS":{"min":5,"max":14},"SPIRIT":{"min":4,"max":10}}'::jsonb
  ),
  '["blueprint","forge","sword","uncommon"]'::jsonb,
  '青冥剑的锻造图纸，剑如青冥，锋从砺出。'),

('纯钧剑图', 'FORGING_BLUEPRINT',
  jsonb_build_object(
    'equipment_template_id', (SELECT id FROM xt_equipment_template WHERE name = '纯钧剑'),
    'grade', 3,
    'requirements', '{"RIGIDITY":{"min":18,"max":32},"TOUGHNESS":{"min":8,"max":20},"SPIRIT":{"min":8,"max":16}}'::jsonb
  ),
  '["blueprint","forge","sword","rare"]'::jsonb,
  '纯钧剑的锻造图纸，古法制剑，需天时地利。'),

('开山斧图', 'FORGING_BLUEPRINT',
  jsonb_build_object(
    'equipment_template_id', (SELECT id FROM xt_equipment_template WHERE name = '开山斧'),
    'grade', 2,
    'requirements', '{"RIGIDITY":{"min":14,"max":28},"TOUGHNESS":{"min":6,"max":14},"SPIRIT":{"min":1,"max":5}}'::jsonb
  ),
  '["blueprint","forge","axe","uncommon"]'::jsonb,
  '开山斧的锻造图纸，重器天成，一斧开山。'),

('旋风斧图', 'FORGING_BLUEPRINT',
  jsonb_build_object(
    'equipment_template_id', (SELECT id FROM xt_equipment_template WHERE name = '旋风斧'),
    'grade', 3,
    'requirements', '{"RIGIDITY":{"min":20,"max":38},"TOUGHNESS":{"min":10,"max":22},"SPIRIT":{"min":3,"max":10}}'::jsonb
  ),
  '["blueprint","forge","axe","rare"]'::jsonb,
  '旋风斧的锻造图纸，攻防一体，需要精纯的锻材。'),

('亮银枪图', 'FORGING_BLUEPRINT',
  jsonb_build_object(
    'equipment_template_id', (SELECT id FROM xt_equipment_template WHERE name = '亮银枪'),
    'grade', 2,
    'requirements', '{"RIGIDITY":{"min":10,"max":22},"TOUGHNESS":{"min":6,"max":14},"SPIRIT":{"min":2,"max":8}}'::jsonb
  ),
  '["blueprint","forge","spear","uncommon"]'::jsonb,
  '亮银枪的锻造图纸，枪出如龙，寒芒所至。'),

('镇魔棍图', 'FORGING_BLUEPRINT',
  jsonb_build_object(
    'equipment_template_id', (SELECT id FROM xt_equipment_template WHERE name = '镇魔棍'),
    'grade', 2,
    'requirements', '{"RIGIDITY":{"min":8,"max":18},"TOUGHNESS":{"min":6,"max":16},"SPIRIT":{"min":5,"max":12}}'::jsonb
  ),
  '["blueprint","forge","staff","uncommon"]'::jsonb,
  '镇魔棍的锻造图纸，刻阵加持，伏魔专用。'),

('玄铁甲图', 'FORGING_BLUEPRINT',
  jsonb_build_object(
    'equipment_template_id', (SELECT id FROM xt_equipment_template WHERE name = '玄铁甲'),
    'grade', 2,
    'requirements', '{"RIGIDITY":{"min":14,"max":28},"TOUGHNESS":{"min":8,"max":18},"SPIRIT":{"min":1,"max":5}}'::jsonb
  ),
  '["blueprint","forge","armor","plate"]'::jsonb,
  '玄铁甲的锻造图纸，硬甲铸成哐哐响，但绝对安全。'),

('寒铁重甲图', 'FORGING_BLUEPRINT',
  jsonb_build_object(
    'equipment_template_id', (SELECT id FROM xt_equipment_template WHERE name = '寒铁重甲'),
    'grade', 3,
    'requirements', '{"RIGIDITY":{"min":20,"max":38},"TOUGHNESS":{"min":12,"max":25},"SPIRIT":{"min":3,"max":10}}'::jsonb
  ),
  '["blueprint","forge","armor","plate","rare"]'::jsonb,
  '寒铁重甲的锻造图纸，表面凝霜，穿上霸气凛然。'),

('灵蚕法袍图', 'FORGING_BLUEPRINT',
  jsonb_build_object(
    'equipment_template_id', (SELECT id FROM xt_equipment_template WHERE name = '灵蚕法袍'),
    'grade', 3,
    'requirements', '{"RIGIDITY":{"min":3,"max":12},"TOUGHNESS":{"min":12,"max":28},"SPIRIT":{"min":6,"max":15}}'::jsonb
  ),
  '["blueprint","forge","armor","cloth","rare"]'::jsonb,
  '灵蚕法袍的锻造图纸，轻盈通透，仙气飘飘。'),

('陨铁战甲图', 'FORGING_BLUEPRINT',
  jsonb_build_object(
    'equipment_template_id', (SELECT id FROM xt_equipment_template WHERE name = '陨铁战甲'),
    'grade', 4,
    'requirements', '{"RIGIDITY":{"min":25,"max":45},"TOUGHNESS":{"min":15,"max":30},"SPIRIT":{"min":8,"max":18}}'::jsonb
  ),
  '["blueprint","forge","armor","plate","epic"]'::jsonb,
  '陨铁战甲的锻造图纸，天外陨铁铸就，星辰护体。'),

('穿云弓图', 'FORGING_BLUEPRINT',
  jsonb_build_object(
    'equipment_template_id', (SELECT id FROM xt_equipment_template WHERE name = '穿云弓'),
    'grade', 2,
    'requirements', '{"RIGIDITY":{"min":6,"max":16},"TOUGHNESS":{"min":8,"max":20},"SPIRIT":{"min":3,"max":8}}'::jsonb
  ),
  '["blueprint","forge","bow","uncommon"]'::jsonb,
  '穿云弓的锻造图纸，灵兽筋为弦，射程超远。'),

('灵玉戒指图', 'FORGING_BLUEPRINT',
  jsonb_build_object(
    'equipment_template_id', (SELECT id FROM xt_equipment_template WHERE name = '灵玉戒指'),
    'grade', 1,
    'requirements', '{"RIGIDITY":{"min":2,"max":10},"TOUGHNESS":{"min":3,"max":12},"SPIRIT":{"min":5,"max":12}}'::jsonb
  ),
  '["blueprint","forge","accessory","ring"]'::jsonb,
  '灵玉戒指的锻造图纸，嵌入灵石，灵气流转。'),

('灵石吊坠图', 'FORGING_BLUEPRINT',
  jsonb_build_object(
    'equipment_template_id', (SELECT id FROM xt_equipment_template WHERE name = '灵石吊坠'),
    'grade', 1,
    'requirements', '{"RIGIDITY":{"min":1,"max":8},"TOUGHNESS":{"min":3,"max":12},"SPIRIT":{"min":6,"max":14}}'::jsonb
  ),
  '["blueprint","forge","accessory","necklace"]'::jsonb,
  '灵石吊坠的锻造图纸，水滴灵玉，胸前暖阳。'),

('玉镯图', 'FORGING_BLUEPRINT',
  jsonb_build_object(
    'equipment_template_id', (SELECT id FROM xt_equipment_template WHERE name = '玉镯'),
    'grade', 1,
    'requirements', '{"RIGIDITY":{"min":2,"max":10},"TOUGHNESS":{"min":5,"max":15},"SPIRIT":{"min":4,"max":10}}'::jsonb
  ),
  '["blueprint","forge","accessory","bracelet"]'::jsonb,
  '玉镯的锻造图纸，翡翠手镯，温润养人。'),

('龙鳞软甲图', 'FORGING_BLUEPRINT',
  jsonb_build_object(
    'equipment_template_id', (SELECT id FROM xt_equipment_template WHERE name = '龙鳞软甲'),
    'grade', 5,
    'requirements', '{"RIGIDITY":{"min":30,"max":50},"TOUGHNESS":{"min":20,"max":40},"SPIRIT":{"min":12,"max":25}}'::jsonb
  ),
  '["blueprint","forge","armor","leather","legendary"]'::jsonb,
  '龙鳞软甲的锻造图纸，龙鳞为甲，轻而坚不可摧。');
