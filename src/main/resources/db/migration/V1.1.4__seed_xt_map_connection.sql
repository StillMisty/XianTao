INSERT INTO xt_map_connection (from_map_id, to_map_id, travel_time_minutes, bidirectional)
VALUES
-- 黑金主城 <-> 幽暗沼泽
((SELECT id FROM xt_map_node WHERE name = '黑金主城'), (SELECT id FROM xt_map_node WHERE name = '幽暗沼泽'), 5, true),
-- 幽暗沼泽 <-> 枯骨林
((SELECT id FROM xt_map_node WHERE name = '幽暗沼泽'), (SELECT id FROM xt_map_node WHERE name = '枯骨林'), 10, true),
-- 枯骨林 <-> 迷雾洞窟
((SELECT id FROM xt_map_node WHERE name = '枯骨林'), (SELECT id FROM xt_map_node WHERE name = '迷雾洞窟'), 15, true),
-- 枯骨林 <-> 黑风岭
((SELECT id FROM xt_map_node WHERE name = '枯骨林'), (SELECT id FROM xt_map_node WHERE name = '黑风岭'), 10, true),
-- 黑风岭 <-> 万妖窟
((SELECT id FROM xt_map_node WHERE name = '黑风岭'), (SELECT id FROM xt_map_node WHERE name = '万妖窟'), 12, true),
-- 黑风岭 <-> 青云山
((SELECT id FROM xt_map_node WHERE name = '黑风岭'), (SELECT id FROM xt_map_node WHERE name = '青云山'), 8, true),
-- 迷雾洞窟 <-> 青云山
((SELECT id FROM xt_map_node WHERE name = '迷雾洞窟'), (SELECT id FROM xt_map_node WHERE name = '青云山'), 12, true),
-- 青云山 <-> 雷霆崖
((SELECT id FROM xt_map_node WHERE name = '青云山'), (SELECT id FROM xt_map_node WHERE name = '雷霆崖'), 10, true),
-- 万妖窟 <-> 雷霆崖
((SELECT id FROM xt_map_node WHERE name = '万妖窟'), (SELECT id FROM xt_map_node WHERE name = '雷霆崖'), 15, true),
-- 雷霆崖 <-> 天火山
((SELECT id FROM xt_map_node WHERE name = '雷霆崖'), (SELECT id FROM xt_map_node WHERE name = '天火山'), 15, true),
-- 天火山 <-> 葬仙谷
((SELECT id FROM xt_map_node WHERE name = '天火山'), (SELECT id FROM xt_map_node WHERE name = '葬仙谷'), 20, true),
-- 葬仙谷 <-> 归墟海
((SELECT id FROM xt_map_node WHERE name = '葬仙谷'), (SELECT id FROM xt_map_node WHERE name = '归墟海'), 25, true),
-- 万妖窟 <-> 太古遗迹
((SELECT id FROM xt_map_node WHERE name = '万妖窟'), (SELECT id FROM xt_map_node WHERE name = '太古遗迹'), 30, true);
