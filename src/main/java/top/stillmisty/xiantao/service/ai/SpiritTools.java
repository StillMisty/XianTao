package top.stillmisty.xiantao.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.vo.CellStatusVO;
import top.stillmisty.xiantao.domain.fudi.vo.CollectAllVO;
import top.stillmisty.xiantao.domain.fudi.vo.CollectVO;
import top.stillmisty.xiantao.domain.fudi.vo.FarmCellVO;
import top.stillmisty.xiantao.domain.fudi.vo.GiveGiftVO;
import top.stillmisty.xiantao.domain.fudi.vo.TriggerTribulationVO;
import top.stillmisty.xiantao.domain.fudi.vo.UpgradeCellVO;
import top.stillmisty.xiantao.domain.item.enums.InventoryCategory;
import top.stillmisty.xiantao.domain.item.vo.ItemEntry;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.ai.spirit.*;
import top.stillmisty.xiantao.service.beast.BeastBreedingService;
import top.stillmisty.xiantao.service.fudi.FarmService;
import top.stillmisty.xiantao.service.fudi.FudiService;
import top.stillmisty.xiantao.service.inventory.InventoryService;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpiritTools {

  private final ToolExecutor toolExecutor;
  private final FudiService fudiService;
  private final FarmService farmService;
  private final BeastBreedingService beastBreedingService;
  private final InventoryService inventoryService;

  // ===================== 地块查询 =====================

  /**
   * 查询福地地块状态：总地块数、已占用数、空地块数和空地块编号列表。
   *
   * <p>在种植、建造或孵化前必须调用，确认有空地块可用。 若无空地块（emptyCount==0），告诉主人需要先拆除不需要的地块。
   */
  @Tool(description = "查询福地土地使用情况：有多少块地、哪些空着可用。返回空地块编号列表供种植/建造选择")
  public CheckFudiCellsResponse checkFudiCells() {
    return toolExecutor.execute(
        "checkFudiCells",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          CellStatusVO r = fudiService.getCellStatus(userId);
          return new CheckFudiCellsResponse(
              r.totalCells(), r.occupiedCount(), r.emptyCount(), r.emptyCellIds());
        });
  }

  // ===================== 背包查询 =====================

  /**
   * 按类别查看背包物品。每次仅返回一个类别，避免一次性输出全部物品。
   *
   * <p>使用场景：
   *
   * <ul>
   *   <li>种植前用 SEED 类别查种子
   *   <li>孵化前用 BEAST_EGG 类别查兽卵
   *   <li>送礼前用其他类别查可送物品
   * </ul>
   *
   * <p>每个物品包含 id（用于后续操作）、name、quantity、description。 若列表为空，该类别无物品。
   *
   * @param category 物品类别：SEED=种子, BEAST_EGG=兽卵, EQUIPMENT=装备, MATERIAL=锻材, POTION=丹药, HERB=药材,
   *     SKILL_JADE=法决玉简, RECIPE_SCROLL=丹方卷轴, FORGING_BLUEPRINT=锻造图纸, BEAST_ESSENCE=灵兽精华
   */
  @Tool(
      description =
          "按类别查看背包物品：SEED/BEAST_EGG/EQUIPMENT/MATERIAL/POTION/HERB/SKILL_JADE/RECIPE_SCROLL/FORGING_BLUEPRINT/BEAST_ESSENCE。返回物品列表含编号、名称和数量")
  public CheckPlayerBagResponse checkPlayerBag(
      @ToolParam(
              description =
                  "物品类别：SEED/BEAST_EGG/EQUIPMENT/MATERIAL/POTION/HERB/SKILL_JADE/RECIPE_SCROLL/FORGING_BLUEPRINT/BEAST_ESSENCE")
          InventoryCategory category) {
    return toolExecutor.execute(
        "checkPlayerBag",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          var items =
              switch (category) {
                case SEED -> inventoryService.getSeedInventory(userId);
                case EQUIPMENT -> inventoryService.getEquipmentInventory(userId);
                case BEAST_EGG -> inventoryService.getEggInventory(userId);
                default -> {
                  var type = category.toItemType();
                  yield type != null
                      ? inventoryService.getItemsByType(userId, type)
                      : java.util.List.<ItemEntry>of();
                }
              };
          return new CheckPlayerBagResponse(category.getChineseName(), items);
        });
  }

  // ===================== 种植 =====================

  /**
   * 在指定地块播种灵药/作物。
   *
   * <p>前置条件：目标地块必须是灵田（FARM），且当前未种植。
   *
   * <p>作物名称（cropName）可以是：
   *
   * <ul>
   *   <li>完整名称（如"聚灵草"、"血灵芝"）
   *   <li>种子编号（如 item id）
   * </ul>
   *
   * <p>返回的基础生长时间受地块等级和世界事件影响会缩短。
   *
   * @param position 地块编号（从 checkFudiCells 的 emptyCellIds 中选择或已有 FARM 地块）
   * @param cropName 作物名称或种子编号（从 checkPlayerBag(SEED) 的物品列表中选取）
   */
  @Tool(description = "在指定灵田地块播种。cropName 是作物名称或种子编号（需先从 checkPlayerBag(SEED) 获取）")
  @Transactional
  public PlantCropResponse plantCrop(
      @ToolParam(description = "地块编号") String position,
      @ToolParam(description = "作物名称或种子编号") String cropName) {
    return toolExecutor.execute(
        "plantCrop",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          FarmCellVO r = farmService.plantCropByInput(userId, position, cropName);
          return new PlantCropResponse(position, cropName, r.getBaseGrowthHours());
        });
  }

  // ===================== 建造/拆除/升级 =====================

  /**
   * 在空地块上开辟灵田或建造兽栏。
   *
   * <p>地块必须为空（EMPTY），建议先调用 checkFudiCells 确认 emptyCellIds。 无法在已有建筑的地块上建造——需先 removeCell。
   *
   * @param position 空地块编号
   * @param cellType FARM=灵田（种植作物）, PEN=兽栏（孵化灵兽）
   */
  @Tool(description = "在空地块上建造灵田或兽栏。FARM=灵田用于种植，PEN=兽栏用于孵化灵兽")
  @Transactional
  public BuildCellResponse buildCell(
      @ToolParam(description = "空地块编号") String position,
      @ToolParam(description = "地块类型：FARM 或 PEN") CellType cellType) {
    return toolExecutor.execute(
        "buildCell",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          fudiService.buildCell(userId, position, cellType);
          return new BuildCellResponse(position, cellType.getChineseName());
        });
  }

  /**
   * 拆除地块上的建筑，恢复为空地。
   *
   * <p>拆除不可撤销，资源不返还。不可拆除有作物或灵兽的地块。 拆除后该地块可供重新建造。
   *
   * @param position 要拆除的地块编号
   */
  @Tool(description = "拆除指定地块上的灵田或兽栏，返还为空地。不可拆除已种植或已孵化的地块")
  @Transactional
  public RemoveCellResponse removeCell(@ToolParam(description = "要拆除的地块编号") String position) {
    return toolExecutor.execute(
        "removeCell",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          var r = fudiService.removeCell(userId, position);
          return new RemoveCellResponse(position, r.type());
        });
  }

  /**
   * 消耗灵石升级地块，每次提升 1 级。
   *
   * <p>等级越高产量越大、成熟时间越短。消耗灵石随等级递增。
   *
   * @param position 要升级的地块编号
   */
  @Tool(description = "消耗灵石给地块升级。等级越高产量越大、成熟越快")
  @Transactional
  public UpgradeCellResponse upgradeCell(@ToolParam(description = "要升级的地块编号") String position) {
    return toolExecutor.execute(
        "upgradeCell",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          UpgradeCellVO r = fudiService.upgradeCell(userId, position);
          return new UpgradeCellResponse(position, r.oldLevel(), r.newLevel());
        });
  }

  // ===================== 灵兽 =====================

  /**
   * 地灵对主人的无礼言行表示不满。
   *
   * <p>降低好感度（幅度 = severity）。仅在对话中出现明确冒犯性内容时调用。 不必要的频繁调用会破坏游戏体验，请审慎使用。
   *
   * @param reason 冒犯原因（简短的描述）
   * @param severity 冒犯程度 1-5（1=轻微不满，5=严重冒犯）
   */
  @Tool(description = "表达对主人言行的不满，降低好感度。severity 1-5。仅当主人确有冒犯言行时调用")
  @Transactional
  public FeelOffendedResponse feelOffended(
      @ToolParam(description = "冒犯原因简述") String reason,
      @ToolParam(description = "冒犯程度 1-5") int severity) {
    return toolExecutor.execute(
        "feelOffended",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          int clamped = Math.clamp(severity, 1, 5);
          fudiService.adjustSpiritAffection(userId, -clamped);
          return new FeelOffendedResponse(reason, clamped);
        });
  }

  // ===================== 收取产出 =====================

  /**
   * 收获灵田成熟作物或收集灵兽产出。
   *
   * <p>两种使用方式：
   *
   * <ul>
   *   <li>传 'all'：一次性收取所有可收获的地块（推荐在 checkFudiCells 发现有成熟后使用）
   *   <li>传地块编号：仅收取该地块
   * </ul>
   *
   * <p>只有成熟（生长进度 100%）的灵田和孵化完成的兽栏才能收取。
   *
   * @param position 地块编号或 'all'
   */
  @Tool(description = "收取成熟作物和灵兽产出。传 'all' 批量收取所有成熟地块")
  @Transactional
  public CollectProduceResponse collectProduce(
      @ToolParam(description = "地块编号，或 'all' 全部收取") String position) {
    return toolExecutor.execute(
        "collectProduce",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          if ("all".equalsIgnoreCase(position)) {
            CollectAllVO r = fudiService.collectAll(userId);
            return new CollectProduceResponse("all", r.harvested(), r.collected(), r.totalItems());
          }
          CollectVO r = fudiService.collect(userId, position);
          boolean isFarm = "FARM".equals(r.type());
          int harvested = isFarm ? 1 : 0;
          int collected = isFarm ? 0 : 1;
          int items = isFarm ? r.yield() : r.totalItems();
          return new CollectProduceResponse(position, harvested, collected, items);
        });
  }

  // ===================== 地灵互动 =====================

  /**
   * 接受主人赠送的礼物。
   *
   * <p>喜欢的礼物会提升好感度/情绪状态，不喜欢的礼物亦可能降低。 送礼有冷却时间，频繁送礼效果递减。
   *
   * @param itemName 赠送的物品名称
   */
  @Tool(description = "接受主人赠送的礼物。itemName 是背包中的物品名称。好感度可能上升也可能下降")
  @Transactional
  public AcceptGiftResponse acceptGift(@ToolParam(description = "赠送的物品名称") String itemName) {
    return toolExecutor.execute(
        "acceptGift",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          GiveGiftVO r = fudiService.giveGift(userId, itemName);
          return new AcceptGiftResponse(r.itemName(), r.change(), r.reaction());
        });
  }

  // ===================== 天劫 =====================

  /**
   * 为主人触发天劫渡劫考验。
   *
   * <p>天劫是境界突破的必要仪式。仅在主人明确要求渡劫/突破/迎接天劫时调用。 不建议在主人什么都没说时主动触发。
   *
   * <p>天劫结果：
   *
   * <ul>
   *   <li>胜利：劫数+1，连胜+1，好感度+5
   *   <li>怜悯：好感度 >= 800 时地灵替主人扛雷，劫数+1，连胜+1
   *   <li>失败：劫数不变，连胜中断，地块可能被毁，好感度下降
   * </ul>
   */
  @Tool(description = "为主人触发天劫渡劫考验。仅在主人明确要求渡劫、突破或迎接天劫时调用")
  @Transactional
  public TriggerTribulationVO triggerTribulation() {
    return toolExecutor.execute(
        "triggerTribulation",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          return fudiService.triggerTribulation(userId);
        });
  }
}
