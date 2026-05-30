package top.stillmisty.xiantao.domain.user.enums;

import java.util.List;
import lombok.Getter;

/** 修仙境界枚举 每个大境界定义：名称、等级区间、每层独立名称、背景描述、突破贺词 */
@Getter
public enum CultivationRealm {
  QI_REFINING(
      "炼气期",
      1,
      10,
      List.of("启灵", "引气", "炼息", "凝旋", "聚海", "通玄", "化液", "淬液", "冲关", "圆满"),
      """
                     修仙之始，凡人之躯引天地灵气入体，
                     于丹田凝聚第一缕气旋。寿元可延至一百五十岁。
                     虽只是大道之始，却已踏上了逆天改命的第一步。""",
      """
                     天地灵气如潮涌灌入四肢百骸，丹田之中气旋乍现！
                     凡人桎梏就此崩裂，从此命不由天，踏入炼气期。
                     仙途漫漫，此为一始。"""),
  FOUNDATION(
      "筑基期",
      11,
      20,
      List.of("开光", "辟府", "筑台", "炼神", "通脉", "融合", "化罡", "洗髓", "问心", "圆满"),
      """
                     气旋凝实化为道基，如筑高台于虚空中。
                     可御器飞行、神念外放，灵台清明洞察八方。
                     寿元增至三百岁，从此凡俗难伤。""",
      """
                     丹田气海轰然开辟，道基如柱拔地而起！
                     灵台之中仿佛点燃一盏不灭明灯，四方八荒尽在神念笼罩之中。
                     凡胎脱去，仙骨初成——恭喜踏入筑基期！"""),
  GOLDEN_CORE(
      "金丹期",
      21,
      30,
      List.of("凝丹", "养丹", "固丹", "淬丹", "通灵", "孕神", "丹纹", "丹火", "碎丹", "圆满"),
      """
                     以身为炉，以道为火，于丹田凝结一颗不朽金丹。
                     金丹既成可引动天地异象，鬼神辟易。
                     寿元骤增至八百岁，可称真人。""",
      """
                     丹田深处金光迸射，一颗金丹缓缓凝聚成形！
                     天地风云为之变色，四周灵气如万川归海涌入体内。
                     一粒金丹吞入腹，始知我命由我不由天——恭喜踏入金丹期！"""),
  NASCENT_SOUL(
      "元婴期",
      31,
      40,
      List.of("孕婴", "育婴", "凝婴", "开窍", "固婴", "化形", "出窍", "分神", "感道", "圆满"),
      """
                     金丹碎裂化为元婴，如婴儿新生纯净无瑕。
                     元婴可离体遨游瞬息千里，一念可察千里山河。
                     寿元可达三千岁，已非凡人可揣度。""",
      """
                     金丹之上裂痕密布，轰然间碎作漫天金光！
                     金光之中一个澄澈如水的元婴破壳而出，与天地共鸣。
                     金丹碎裂元婴出，从此天大地大何处不可去——恭喜踏入元婴期！"""),
  DEITY_TRANSFORMATION(
      "化神期",
      41,
      55,
      List.of(
          "化神", "凝神", "锻神", "化念", "御神", "合神", "通幽", "洞玄", "知命", "化道", "悟真", "证道", "执道", "感虚", "圆满"),
      """
                     元婴化神，神念通彻天地。可分神万千，一念可察一界。
                     举手投足皆有天地法则相随，言出法随。
                     寿元万年，已近仙道。""",
      """
                     元婴陡然睁开双目，万千光芒自其中迸射而出，与天地万道交相共鸣！
                     神念如水银泻地般铺展开来，方圆千里纤毫毕现。
                     元神出窍化万千，一念通天地——恭喜踏入化神期！"""),
  VOID_TRAINING(
      "炼虚期",
      56,
      70,
      List.of(
          "初虚", "入虚", "窥虚", "破虚", "游虚", "辟界", "养界", "定界", "演界", "掌界", "破界", "融界", "归墟", "化墟", "圆满"),
      """
                     炼神返虚，窥见天地本源。可开辟一方小世界，自成一界之主。
                     虚空中见真我，万象归寂道心明。
                     天地法则已可借为己用。""",
      """
                     神念触及虚空深处，天地本源的脉动在意识中轰然炸开！
                     一方虚无世界在识海中缓缓开辟，从此自成一界之主。
                     虚空中见真我，万象归寂道心明——恭喜踏入炼虚期！"""),
  UNITY(
      "合体期",
      71,
      90,
      List.of(
          "合天", "合地", "合人", "合道", "融天", "融地", "融道", "天心", "地脉", "人道", "三才", "归一", "无我", "有我", "化身",
          "执天", "万象", "超脱", "望劫", "圆满"),
      """
                     天地人三才合为一体，道法自然。
                     举手投足皆有天道相随，一念可令山河变色。
                     自身即天道，天道即自身。""",
      """
                     天光地脉与人魂轰然合一，三才归位道法自成！
                     周身仿佛与整个天地融为一体，举手投足皆带天道威压。
                     天地人三才合一，道法自然——恭喜踏入合体期！"""),
  MAHAYANA(
      "大乘期",
      91,
      110,
      List.of(
          "渡己", "渡人", "渡世", "慈悲", "般若", "菩提", "涅槃", "因果", "轮回", "无相", "空明", "圆觉", "法相", "金身", "天眼",
          "宿命", "漏尽", "飞升", "劫临", "圆满"),
      """
                     大乘圆满，大道在望。可感知天道运转，预知祸福。
                     半只脚已踏入仙门，世间万象皆洞明于胸。
                     只差最后一步便可飞升仙界。""",
      """
                     道心通明大圆满，天地万道尽收眼底。一道仙门虚影在头顶缓缓浮现，
                     仙光洒落涤荡周身凡尘。万事万物在眼中皆有了因果脉络。
                     大乘圆满道心成，仙门已在眼前——恭喜踏入大乘期！"""),
  TRIBULATION(
      "渡劫期",
      111,
      Integer.MAX_VALUE,
      /* 渡劫期前10层有独立命名，之后按"第N劫"生成。 layerNames仅存储前10层，internalLayerForName()超出部分走公式。 */
      List.of("一劫", "二劫", "三劫", "四劫", "五劫", "六劫", "七劫", "八劫", "九劫", "飞升劫"),
      """
                     天道降下雷劫考验。每一劫都是生死之间的大恐怖。
                     渡得过则飞升成仙，渡不过则魂飞魄散万劫不复。
                     九劫过后即为真仙，从此跳出三界外不在五行中。""",
      """
                     九天之上雷霆咆哮，第一道天劫撕裂苍穹劈落而下！
                     周身灵气如沸腾般涌动，每一寸血肉都在雷光中淬炼重生。
                     天劫降临，渡过即仙——恭喜踏入渡劫期！""");

  /** 突破大境界时的全属性加成百分比（基于有效属性） */
  public static final int MAJOR_BREAKTHROUGH_STAT_PERCENT = 20;

  /** 突破大境界时的灵石奖励基数 */
  public static final int MAJOR_BREAKTHROUGH_SPIRIT_STONES_BASE = 2000;

  private final String realmName;
  private final int startLevel;
  private final int endLevel;

  @SuppressWarnings("ImmutableEnumChecker") // List.copyOf() in constructor
  private final List<String> layerNames;

  private final String description;
  private final String breakthroughMessage;

  CultivationRealm(
      String realmName,
      int startLevel,
      int endLevel,
      List<String> layerNames,
      String description,
      String breakthroughMessage) {
    this.realmName = realmName;
    this.startLevel = startLevel;
    this.endLevel = endLevel;
    this.layerNames = List.copyOf(layerNames);
    this.description = description.strip().replace("\n", "");
    this.breakthroughMessage = breakthroughMessage.strip().replace("\n", " ");
  }

  /** 根据总层数推断所属大境界 */
  public static CultivationRealm fromLevel(int level) {
    for (CultivationRealm realm : values()) {
      if (level >= realm.startLevel && level <= realm.endLevel) {
        return realm;
      }
    }
    return TRIBULATION;
  }

  /** 获取完整境界显示名（如 "炼气期 · 启灵"、"渡劫期 · 三劫"） */
  public static String realmDisplay(int level) {
    CultivationRealm realm = fromLevel(level);
    int innerLayer = level - realm.startLevel + 1;
    return realm.realmName + " · " + realm.layerNameForInnerLayer(innerLayer);
  }

  /** 根据境界内层数获取该层独立名称 */
  private String layerNameForInnerLayer(int innerLayer) {
    if (innerLayer <= layerNames.size()) {
      return layerNames.get(innerLayer - 1);
    }
    // 渡劫期超出已定义名称的部分，用"第N劫"
    return "第" + innerLayer + "劫";
  }

  @SuppressWarnings("EnumOrdinal")
  public int getRank() {
    return ordinal();
  }

  public static CultivationRealm fromRank(int rank) {
    CultivationRealm[] values = values();
    if (rank < 0 || rank >= values.length) {
      throw new IllegalArgumentException("Unknown CultivationRealm rank: " + rank);
    }
    return values[rank];
  }

  /** 判断是否跨大境界突破 */
  public static boolean isMajorBreakthrough(int oldLevel, int newLevel) {
    return fromLevel(oldLevel) != fromLevel(newLevel);
  }

  /** 获取突破该境界时的灵石奖励 */
  public static long breakthroughSpiritStonesReward(CultivationRealm realm) {
    return (long) (realm.getRank() + 1) * MAJOR_BREAKTHROUGH_SPIRIT_STONES_BASE;
  }
}
