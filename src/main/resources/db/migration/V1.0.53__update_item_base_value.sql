-- 为无基准价的堆叠类物品补定价（此前 base_value 默认 0，回收仅得 1 灵石）
-- 定价锚点：悬赏单次收入 30~150 灵石；小丹 30~120；筑基丹 350~800
-- 已有基准价的物品（如灵兽精华）不受影响
UPDATE item_template
SET base_value =
    CASE
        WHEN tags @> '["mythic"]'       THEN 2500
        WHEN tags @> '["legendary"]'    THEN 1000
        WHEN tags @> '["epic"]'         THEN 300
        WHEN tags @> '["rare"]'         THEN 80
        WHEN tags @> '["uncommon"]'     THEN 25
        ELSE 8
        END
WHERE type IN ('MATERIAL', 'HERB', 'SEED', 'POTION', 'BEAST_EGG')
  AND base_value = 0;
