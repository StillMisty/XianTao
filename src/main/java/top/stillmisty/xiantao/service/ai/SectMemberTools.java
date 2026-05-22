package top.stillmisty.xiantao.service.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.sect.vo.BuildingsQueryVO;
import top.stillmisty.xiantao.domain.sect.vo.DonateResultVO;
import top.stillmisty.xiantao.domain.sect.vo.ExchangeResultVO;
import top.stillmisty.xiantao.domain.sect.vo.LearnSkillResultVO;
import top.stillmisty.xiantao.domain.sect.vo.SectShopItemVO;
import top.stillmisty.xiantao.domain.sect.vo.SectTaskVO;
import top.stillmisty.xiantao.domain.sect.vo.SharedSkillsQueryVO;
import top.stillmisty.xiantao.domain.sect.vo.ShopQueryVO;
import top.stillmisty.xiantao.domain.sect.vo.SubmitJadeResultVO;
import top.stillmisty.xiantao.domain.sect.vo.TasksQueryVO;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.sect.SectBuildingService;
import top.stillmisty.xiantao.service.sect.SectMemberService;
import top.stillmisty.xiantao.service.sect.SectSharedSkillService;
import top.stillmisty.xiantao.service.sect.SectShopService;

/** 宗门所有成员可用的工具 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SectMemberTools {

  private final SectShopService sectShopService;
  private final SectSharedSkillService sectSharedSkillService;
  private final SectMemberService sectMemberService;
  private final SectBuildingService sectBuildingService;

  @Tool(description = "查看宗门贡献商店商品列表，含价格和库存")
  public QueryShopResponse queryShop() {
    try {
      Long userId = UserContext.requireCurrentUserId();
      ShopQueryVO vo = sectShopService.getShop(userId);
      return new QueryShopResponse(vo.myContribution(), vo.items(), null);
    } catch (Exception e) {
      log.error("查询贡献商店失败", e);
      return new QueryShopResponse(0, List.of(), e.getMessage());
    }
  }

  @Tool(description = "消耗贡献值兑换贡献商店中的商品")
  @Transactional
  public ExchangeShopItemResponse exchangeShopItem(
      @ToolParam(description = "商品编号") long shopItemId) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      ExchangeResultVO result = sectShopService.exchangeShopItem(userId, shopItemId);
      return new ExchangeShopItemResponse(
          shopItemId, result.itemName(), result.remainingContribution(), null);
    } catch (Exception e) {
      log.error("兑换商品失败: shopItemId={}", shopItemId, e);
      return new ExchangeShopItemResponse(shopItemId, null, 0, e.getMessage());
    }
  }

  @Tool(description = "查看宗门共享功法列表，含功法详情和学习消耗")
  public QuerySharedSkillsResponse querySharedSkills() {
    try {
      Long userId = UserContext.requireCurrentUserId();
      SharedSkillsQueryVO vo = sectSharedSkillService.getSharedSkills(userId);
      return new QuerySharedSkillsResponse(
          vo.myContribution(), vo.usedSlots(), vo.maxSlots(), vo.skills(), vo.pendingCount(), null);
    } catch (Exception e) {
      log.error("查询共享功法失败", e);
      return new QuerySharedSkillsResponse(0, 0, 0, List.of(), 0, e.getMessage());
    }
  }

  @Tool(description = "消耗贡献值学习指定的共享功法")
  @Transactional
  public LearnSharedSkillResponse learnSharedSkill(
      @ToolParam(description = "共享功法编号") long sharedSkillId) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      LearnSkillResultVO result = sectSharedSkillService.learnSharedSkill(userId, sharedSkillId);
      return new LearnSharedSkillResponse(
          sharedSkillId, result.skillName(), result.cost(), result.remainingContribution(), null);
    } catch (Exception e) {
      log.error("学习共享功法失败: sharedSkillId={}", sharedSkillId, e);
      return new LearnSharedSkillResponse(sharedSkillId, null, 0, 0, e.getMessage());
    }
  }

  @Tool(description = "提交功法玉简到宗门，换取贡献值")
  @Transactional
  public SubmitSkillJadeResponse submitSkillJade(@ToolParam(description = "玉简名称") String jadeName) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      SubmitJadeResultVO result = sectSharedSkillService.submitSkillJade(userId, jadeName);
      return new SubmitSkillJadeResponse(
          jadeName, result.skillName(), result.contributionGained(), null);
    } catch (Exception e) {
      log.error("提交功法玉简失败: jadeName={}", jadeName, e);
      return new SubmitSkillJadeResponse(jadeName, null, 0, e.getMessage());
    }
  }

  @Tool(description = "捐献灵石获取贡献值（兑换比例 10:1）每次捐献下限 1000 灵石")
  @Transactional
  public DonateStonesResponse donateStones(@ToolParam(description = "捐献灵石数量，最低 1000") long amount) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      DonateResultVO result = sectMemberService.donateStones(userId, amount);
      return new DonateStonesResponse(amount, result.contributionGained(), null);
    } catch (Exception e) {
      log.error("捐献灵石失败: amount={}", amount, e);
      return new DonateStonesResponse(amount, 0, e.getMessage());
    }
  }

  @Tool(description = "查看宗门所有建筑及其等级状态")
  public QueryBuildingsResponse queryBuildings() {
    try {
      Long userId = UserContext.requireCurrentUserId();
      BuildingsQueryVO vo = sectBuildingService.getBuildings(userId);
      return new QueryBuildingsResponse(vo.built(), vo.buildable(), null);
    } catch (Exception e) {
      log.error("查询建筑失败", e);
      return new QueryBuildingsResponse(List.of(), List.of(), e.getMessage());
    }
  }

  @Tool(description = "获取当前宗门事件对应的任务列表，无事件时返回空")
  public QueryTasksResponse queryTasks() {
    try {
      Long userId = UserContext.requireCurrentUserId();
      TasksQueryVO vo = sectMemberService.getTasks(userId);
      return new QueryTasksResponse(vo.tasks(), null);
    } catch (Exception e) {
      log.error("查询宗门任务失败", e);
      return new QueryTasksResponse(List.of(), e.getMessage());
    }
  }

  // ===================== 响应 Record =====================

  public record QueryShopResponse(
      @JsonPropertyDescription("当前贡献值") int myContribution,
      @JsonPropertyDescription("商品列表") List<SectShopItemVO> items,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record ExchangeShopItemResponse(
      @JsonPropertyDescription("商品编号") long shopItemId,
      @JsonPropertyDescription("商品名称") String itemName,
      @JsonPropertyDescription("剩余贡献值") int remainingContribution,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record QuerySharedSkillsResponse(
      @JsonPropertyDescription("当前贡献值") int myContribution,
      @JsonPropertyDescription("已使用功法位") int usedSlots,
      @JsonPropertyDescription("最大功法位") int maxSlots,
      @JsonPropertyDescription("功法列表")
          List<top.stillmisty.xiantao.domain.sect.vo.SectSharedSkillVO> skills,
      @JsonPropertyDescription("待上架数量") int pendingCount,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record LearnSharedSkillResponse(
      @JsonPropertyDescription("共享功法编号") long sharedSkillId,
      @JsonPropertyDescription("功法名称") String skillName,
      @JsonPropertyDescription("消耗贡献值") int cost,
      @JsonPropertyDescription("剩余贡献值") int remainingContribution,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record SubmitSkillJadeResponse(
      @JsonPropertyDescription("玉简名称") String jadeName,
      @JsonPropertyDescription("功法名称") String skillName,
      @JsonPropertyDescription("获得的贡献值") int contributionGained,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record DonateStonesResponse(
      @JsonPropertyDescription("捐献灵石数量") long amount,
      @JsonPropertyDescription("获得的贡献值") int contributionGained,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record QueryBuildingsResponse(
      @JsonPropertyDescription("已建造建筑列表") List<BuildingsQueryVO.BuildingEntry> built,
      @JsonPropertyDescription("可建造建筑列表") List<BuildingsQueryVO.BuildingEntry> buildable,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record QueryTasksResponse(
      @JsonPropertyDescription("任务列表") List<SectTaskVO> tasks,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}
}
