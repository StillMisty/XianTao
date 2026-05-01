-- 给xt_inventory_item表添加properties字段
ALTER TABLE xt_inventory_item ADD COLUMN properties JSONB DEFAULT '{}'::jsonb;

-- 更新字段注释
COMMENT ON COLUMN xt_inventory_item.properties IS '类型特有属性 JSONB：
  丹药: {"grade": 3, "quality": "superior"}
  药材: {"elements": {"wood": 3, "fire": 1, "water": 2}}
  其他: {}';