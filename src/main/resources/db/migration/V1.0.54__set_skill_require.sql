-- 法决树前置接线：静心诀 → 通明心法 → 天人感应 → 道心通明
-- 幂等：仅当 require_skill_id 为空时写入
UPDATE skill
SET require_skill_id = (SELECT id FROM skill WHERE name = '静心诀')
WHERE name = '通明心法'
  AND require_skill_id IS NULL;

UPDATE skill
SET require_skill_id = (SELECT id FROM skill WHERE name = '通明心法')
WHERE name = '天人感应'
  AND require_skill_id IS NULL;

UPDATE skill
SET require_skill_id = (SELECT id FROM skill WHERE name = '天人感应')
WHERE name = '道心通明'
  AND require_skill_id IS NULL;
