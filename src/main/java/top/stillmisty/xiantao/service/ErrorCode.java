package top.stillmisty.xiantao.service;

/** 业务异常枚举，携带结构化的错误码和格式化消息 */
public enum ErrorCode {

  // ===== User / Fudi / System =====
  USER_NOT_FOUND("用户不存在"),
  FUDI_NOT_FOUND("未找到福地"),
  FUDI_ALREADY_EXISTS("用户已拥有福地"),
  SPIRIT_NOT_FOUND("地灵不存在"),
  SPIRIT_STONES_INSUFFICIENT("灵石不足（需要 %d，当前 %d）"),

  // ===== Cell =====
  CELL_NOT_FOUND("地块 %s 不存在"),
  CELL_EMPTY("地块 %s 为空"),
  CELL_NOT_PEN("地块 %s 不是兽栏"),
  CELL_NOT_FARM("地块不是灵田"),
  CELL_OCCUPIED("地块 %s 已被占用"),
  CELL_MAX_LEVEL("已是最高等级 Lv5"),
  CELL_ID_INVALID("地块编号格式错误，请输入数字编号"),
  CELL_OUT_OF_RANGE("地块编号超出范围（1-%d），当前劫数仅开放到 %d 号地块"),
  CELL_NO_COLLECTIBLE("地块 %s 无可收取内容"),
  CELL_TYPE_MISMATCH("地块 %s 已有其他类型建筑"),

  // ===== Beast =====
  BEAST_NOT_FOUND("未找到灵兽"),
  BEAST_HATCHING("灵兽尚在孵化中"),
  BEAST_DEAD("灵兽HP为0，请先恢复"),
  BEAST_DEPLOY_FULL("出战灵兽已达上限 (2只)，请先召回其他灵兽"),
  BEAST_MAX_TIER("已是最高等阶 T5"),
  BEAST_MAX_QUALITY("已是最高品质神品"),
  BEAST_NEED_MAX_LEVEL("灵兽需要先达到等级上限才能进化"),
  BEAST_NEED_MAX_LEVEL_BREAK("灵兽需要先达到等级上限才能突破"),
  BEAST_EVOLVE_FAILED("进化失败！进化石和灵石已消耗"),
  BEAST_QUALITY_FAILED("品质突破失败！进化石和灵石已消耗"),
  BEAST_TIER_REQUIRES_PEN("灵兽等阶(T%d)需要至少Lv%d兽栏"),
  BEAST_PEN_OCCUPIED("该兽栏已有灵兽，请先放生"),
  BEAST_EGG_NOT_FOUND("未找到兽卵: %s"),
  BEAST_EGG_NOT_IN_INVENTORY("背包中没有 [%s]"),
  BEAST_EVOLVE_STONE_NOT_FOUND("进化石模板未找到"),
  BEAST_EVOLVE_STONE_INVENTORY_EMPTY("背包中没有进化石"),
  BEAST_EVOLVE_STONE_INSUFFICIENT("需要 %d 个进化石（当前%d）"),
  BEAST_PRODUCE_NOTHING("暂无产出可收取"),

  // ===== Farm =====
  CROP_TIER_REQUIRES_FARM("作物等阶(T%d)需要至少Lv%d灵田，当前灵田Lv%d"),
  SEED_NOT_IN_INVENTORY("背包中没有 [%s]"),
  SEED_TEMPLATE_NOT_FOUND("未找到种子: %s"),
  CROP_WITHERED("%s 已枯萎（超过成熟时间两倍未收获）"),
  CROP_NOT_READY("灵药尚未成熟"),

  // ===== Item / Inventory / Gift =====
  ITEM_NOT_FOUND("背包中未找到物品：%s"),
  ITEM_CANNOT_USE("该物品无法使用"),
  ITEM_NOT_EXISTS("物品不存在"),
  ITEM_OWNERSHIP_MISMATCH("物品所有权不匹配"),
  ITEM_QUANTITY_INSUFFICIENT("物品数量不足: 需要 %d，当前 %d"),
  ITEM_EQUIPPED("【%s】已装备，请先卸下再丢弃"),
  ITEM_MULTIPLE_MATCH("找到多个 [%s]，请使用更精确的名称"),
  GIFT_ALREADY_TODAY("今日已送过礼物，明天再来吧"),

  // ===== Pill / Recipe =====
  RECIPE_SCROLL_NOT_FOUND("未找到名为「%s」的丹方卷轴"),
  RECIPE_SCROLL_DATA_ERROR("丹方卷轴数据异常"),
  RECIPE_ALREADY_LEARNED("已学会该丹方"),
  RECIPE_SCROLL_WRONG_TYPE("该物品不是丹方卷轴"),
  RECIPE_PILL_DATA_ERROR("丹药产物数据异常"),

  // ===== Forging =====
  BLUEPRINT_SCROLL_NOT_FOUND("未找到名为「%s」的锻造图纸"),
  BLUEPRINT_ALREADY_LEARNED("已学会该锻造图纸"),
  BLUEPRINT_SCROLL_WRONG_TYPE("该物品不是锻造图纸"),
  BLUEPRINT_DATA_ERROR("锻造图纸数据异常"),
  BLUEPRINT_NOT_LEARNED("未学会该锻造图纸：%s"),
  FORGING_MATERIAL_INSUFFICIENT("锻材不足，无法锻造"),
  FORGING_ATTRIBUTE_MISSING("缺少锻材属性：%s"),
  FORGING_ATTRIBUTE_EXCEED("锻材属性超过上限：%s"),
  FORGING_NO_MATCHING_BLUEPRINT("锻材三性不匹配任何锻造图纸"),
  EQUIPMENT_NOT_FORGEABLE("该装备不可锻造"),
  EQUIPMENT_FORGE_LEVEL_MAX("装备强化等级已达上限"),
  EQUIPMENT_BLUEPRINT_REQUIRED("强化+10及以上需要持有对应锻造图纸"),
  EQUIPMENT_FORGE_LEVEL_CAP("掉落装备强化上限为+9"),
  ENHANCE_MATERIAL_NOT_MATCH("锻材不满足强化约束"),

  // ===== Bounty =====
  BOUNTY_NO_ACTIVE("当前没有进行中的悬赏"),
  BOUNTY_NOT_FOUND("悬赏不存在"),
  BOUNTY_WRONG_MAP("该悬赏不属于当前地图"),
  BOUNTY_LEVEL_INSUFFICIENT("等级不足，无法接取该悬赏"),
  BOUNTY_TIME_REMAINING("悬赏「%s」还需 %d 分钟（共需 %d 分）"),

  // ===== Status =====
  STATUS_BLOCKED("您当前处于 %s 状态，无法进行此操作（需要 %s 状态）"),

  // ===== Player / Auth =====
  PLAYER_NOT_FOUND("未找到玩家【%s】"),
  PLAYER_CANNOT_SELF("不能和自己切磋！"),
  NICKNAME_TAKEN("此道号已被他人使用，请另择佳名~"),
  CHARACTER_NOT_FOUND("角色不存在"),

  // ===== Map =====
  MAP_CURRENT_NOT_FOUND("当前所在地图不存在"),

  // ===== Shop / Trade =====
  SHOP_NOT_FOUND("此地没有商铺掌柜"),
  SHOP_PRODUCT_NOT_FOUND("商铺中没有该商品"),
  SHOP_PRODUCT_OUT_OF_STOCK("商品已售罄"),
  SHOP_PRODUCT_PRICE_CHANGED("商品价格已变动，请重新查看"),
  SHOP_SPECIAL_ORDER_NOT_FOUND("未找到调货订单"),
  SHOP_SPECIAL_ORDER_NOT_READY("调货尚未到货（还需等待）"),
  SHOP_SPECIAL_ORDER_ALREADY_COLLECTED("该调货订单已取货"),
  SHOP_SPIRIT_STONES_INSUFFICIENT("灵石不足（需要 %d，当前 %d）"),
  EQUIPMENT_NOT_FOUND("未找到该装备"),
  EQUIPMENT_NOT_OWNED("该装备不属于您"),
  EQUIPMENT_ALREADY_EQUIPPED("请先卸下装备再出售"),
  EQUIPMENT_NOT_TRADABLE("此装备已绑定，不可出售"),
  ITEM_NOT_TRADABLE("此物品不可出售"),
  ITEM_NOT_IN_BAG("背包中没有该物品"),
  APPRAISE_ITEM_NOT_FOUND("未找到物品：%s"),
  HAGGLE_LIMIT_REACHED("此交易已经砍过一次价了，老朽不能再让了"),
  SELL_PRICE_MISMATCH("价格不匹配，请重新报价"),
  WORLD_EVENT_NOT_FOUND("世界事件不存在"),

  // ===== Context =====
  USER_CONTEXT_MISSING("未找到用户上下文，请先设置 UserContext");

  private final String template;

  ErrorCode(String template) {
    this.template = template;
  }

  public String getTemplate() {
    return template;
  }

  public String format(Object... args) {
    return args.length == 0 ? template : String.format(template, args);
  }
}
