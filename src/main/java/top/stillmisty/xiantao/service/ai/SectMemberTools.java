package top.stillmisty.xiantao.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.sect.vo.*;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.ai.sect.*;
import top.stillmisty.xiantao.service.sect.SectBuildingService;
import top.stillmisty.xiantao.service.sect.SectMemberService;
import top.stillmisty.xiantao.service.sect.SectSharedSkillService;
import top.stillmisty.xiantao.service.sect.SectShopService;

@Service
@RequiredArgsConstructor
@Slf4j
public class SectMemberTools {

  private final ToolExecutor toolExecutor;
  private final SectShopService sectShopService;
  private final SectSharedSkillService sectSharedSkillService;
  private final SectMemberService sectMemberService;
  private final SectBuildingService sectBuildingService;

  /**
   * 查看宗门贡献商店的货品清单。
   *
   * <p>返回当前弟子的贡献值余额和可兑换的商品列表。 每件商品含编号(id)、名称、消耗贡献值和库存。 兑换前请确认贡献值充足。
   */
  @Tool(description = "查看宗门贡献商店的货品清单，含价格(贡献值)和库存。返回当前弟子的贡献值余额")
  public CheckSectShopResponse checkSectShop() {
    return toolExecutor.execute(
        "checkSectShop",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          ShopQueryVO vo = sectShopService.getShopInternal(userId);
          return new CheckSectShopResponse(vo.myContribution(), vo.items());
        });
  }

  /**
   * 消耗贡献值兑换宗门商店商品。
   *
   * <p>商品编号（shopItemId）来自 checkSectShop 返回的商品列表。 兑换成功后商品进入背包，贡献值扣除。
   *
   * @param shopItemId 商品编号（从 checkSectShop 返回的 items 列表中选取）
   */
  @Tool(description = "消耗贡献值兑换宗门商店商品。shopItemId 从 checkSectShop 返回的商品编号中选择")
  @Transactional
  public ExchangeShopItemResponse exchangeShopItem(
      @ToolParam(description = "商品编号") long shopItemId) {
    return toolExecutor.execute(
        "exchangeShopItem",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          ExchangeResultVO r = sectShopService.exchangeShopItemInternal(userId, shopItemId);
          return new ExchangeShopItemResponse(shopItemId, r.itemName(), r.remainingContribution());
        });
  }

  /**
   * 查看宗门共享功法库。
   *
   * <p>返回宗门所有可学习的共享功法列表。关键信息：
   *
   * <ul>
   *   <li>myContribution：当前贡献值
   *   <li>usedSlots/maxSlots：已用/最大功法槽位
   *   <li>skills：可学习功法（含 sharedSkillId、名称、消耗、效果描述）
   *   <li>pendingCount：已提交玉简但待上架的功法数
   * </ul>
   *
   * <p>若槽位已满（usedSlots >= maxSlots），需先遗忘旧功法。
   */
  @Tool(description = "查看宗门共享功法库，含功法详情、学习消耗和槽位状态")
  public CheckSharedSkillsResponse checkSharedSkills() {
    return toolExecutor.execute(
        "checkSharedSkills",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          SharedSkillsQueryVO vo = sectSharedSkillService.getSharedSkillsInternal(userId);
          return new CheckSharedSkillsResponse(
              vo.myContribution(), vo.usedSlots(), vo.maxSlots(), vo.skills(), vo.pendingCount());
        });
  }

  /**
   * 消耗贡献值学习共享功法。
   *
   * <p>共享功法编号（sharedSkillId）来自 checkSharedSkills 返回的 skills 列表。 学习前确保：贡献值 >= cost、槽位未满（usedSlots <
   * maxSlots）。
   *
   * @param sharedSkillId 共享功法编号（从 checkSharedSkills 返回的 skills 列表中选取）
   */
  @Tool(description = "消耗贡献值学习共享功法。sharedSkillId 从 checkSharedSkills 返回的功法编号中选择")
  @Transactional
  public LearnSharedSkillResponse learnSharedSkill(
      @ToolParam(description = "共享功法编号") long sharedSkillId) {
    return toolExecutor.execute(
        "learnSharedSkill",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          LearnSkillResultVO r =
              sectSharedSkillService.learnSharedSkillInternal(userId, sharedSkillId);
          return new LearnSharedSkillResponse(
              sharedSkillId, r.skillName(), r.cost(), r.remainingContribution());
        });
  }

  /**
   * 将背包中的功法玉简献给宗门。
   *
   * <p>玉简中的功法进入共享功法库（待上架状态），需长老审核后弟子方可学习。 献上后获得对应贡献值奖励。
   *
   * @param jadeName 玉简名称（背包中的功法玉简物品名）
   */
  @Tool(description = "将背包中的功法玉简献给宗门，获得贡献值。功法进入待上架状态，需长老审核")
  @Transactional
  public OfferSkillJadeResponse offerSkillJade(@ToolParam(description = "玉简名称") String jadeName) {
    return toolExecutor.execute(
        "offerSkillJade",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          SubmitJadeResultVO r = sectSharedSkillService.submitSkillJadeInternal(userId, jadeName);
          return new OfferSkillJadeResponse(jadeName, r.skillName(), r.contributionGained());
        });
  }

  /**
   * 向宗门捐献灵石换取贡献值。
   *
   * <p>兑换比例 10:1（10 灵石 = 1 贡献值），最低捐献额 1000 灵石。 不足最低额的操作会被拒绝。
   *
   * @param amount 捐献灵石数量（最低 1000）
   */
  @Tool(description = "向宗门捐献灵石换取贡献值。比例 10:1，最低 1000 灵石")
  @Transactional
  public OfferSpiritStonesResponse offerSpiritStones(
      @ToolParam(description = "捐献灵石数量，最低 1000") long amount) {
    return toolExecutor.execute(
        "offerSpiritStones",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          DonateResultVO r = sectMemberService.donateStonesInternal(userId, amount);
          return new OfferSpiritStonesResponse(amount, r.contributionGained());
        });
  }

  /**
   * 查看宗门所有建筑状态。
   *
   * <p>返回已建成建筑（含等级和效果）和可建造建筑模板（含建造成本）。 建造新建筑需要宗门资金，仅宗主可执行建造操作。
   */
  @Tool(description = "查看宗门建筑列表，含已建成建筑的等级和可建造建筑的成本")
  public CheckSectBuildingsResponse checkSectBuildings() {
    return toolExecutor.execute(
        "checkSectBuildings",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          BuildingsQueryVO vo = sectBuildingService.getBuildingsInternal(userId);
          return new CheckSectBuildingsResponse(vo.built(), vo.buildable());
        });
  }

  /**
   * 查看当前宗门事件对应的宗门任务。
   *
   * <p>宗门无事件时返回空列表。任务含名称、需求描述、奖励描述和进度。
   */
  @Tool(description = "查看宗门当前事件对应的任务列表。无事件时列表为空")
  public CheckSectTasksResponse checkSectTasks() {
    return toolExecutor.execute(
        "checkSectTasks",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          TasksQueryVO vo = sectMemberService.getTasksInternal(userId);
          return new CheckSectTasksResponse(vo.tasks());
        });
  }
}
