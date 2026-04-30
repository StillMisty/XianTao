-- xt_map_node 新增遇怪池 + 遇怪数量范围
ALTER TABLE xt_map_node ADD COLUMN monster_encounters JSONB DEFAULT '{}'::jsonb;
ALTER TABLE xt_map_node ADD COLUMN encounter_size JSONB DEFAULT '{"min": 1, "max": 3}'::jsonb;

COMMENT ON COLUMN xt_map_node.monster_encounters IS '遇怪池 JSONB: {"template_id": weight}';
COMMENT ON COLUMN xt_map_node.encounter_size IS '遇怪数量范围 JSONB: {"min": 1, "max": 3}';
