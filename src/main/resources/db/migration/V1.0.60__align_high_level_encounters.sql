-- 100+ 级遭遇对齐：旧顶级图（90~100）均为单只遭遇（max_count = 1），
-- 新图原设 1~2 只导致战斗时长与战损翻倍（9~16 回合、单场 13%+ 战损）。
-- 统一为单只，使满配队伍 2~5 回合清场、可持续挂机。
UPDATE activity_event
SET params = jsonb_set(params, '{max_count}', '1'::jsonb)
WHERE event_type = 'COMBAT' AND owner_id BETWEEN 45 AND 53
  AND (params->>'max_count')::int > 1;
