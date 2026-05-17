package top.stillmisty.xiantao.service.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.sect.SectBuildingService;
import top.stillmisty.xiantao.service.sect.SectMemberService;
import top.stillmisty.xiantao.service.sect.SectSharedSkillService;
import top.stillmisty.xiantao.service.sect.SectShopService;

/** 宗灵可用的工具函数（Function Calling） 这些工具会被 LLM 调用，以执行宗门相关的操作 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SectSpiritTools {

  private final SectMemberService sectMemberService;
  private final SectShopService sectShopService;
  private final SectSharedSkillService sectSharedSkillService;
  private final SectBuildingService sectBuildingService;

  /** 获取当前用户ID */
  private Long getCurrentUserId() {
    Long userId = UserContext.getCurrentUserId();
    if (userId == null) {
      throw new BusinessException(ErrorCode.USER_CONTEXT_MISSING);
    }
    return userId;
  }

  /** 查看贡献商店 */
  @Tool(description = "查看宗门贡献商店的商品列表，包含编号、名称、价格和库存")
  public ShopQueryResponse queryShop() {
    try {
      Long userId = getCurrentUserId();
      String result = sectShopService.getShop(userId);
      return new ShopQueryResponse(true, result);
    } catch (Exception e) {
      log.error("查询贡献商店失败", e);
      return new ShopQueryResponse(false, e.getMessage());
    }
  }

  /** 兑换商店商品 */
  @Tool(description = "使用贡献值兑换宗门贡献商店的商品")
  public ExchangeResponse exchangeShopItem(
      @ToolParam(description = "商品编号，从queryShop结果中获取") long shopItemId) {
    try {
      Long userId = getCurrentUserId();
      String result = sectShopService.exchangeShopItem(userId, shopItemId);
      return new ExchangeResponse(true, result);
    } catch (Exception e) {
      log.error("兑换商品失败: shopItemId={}", shopItemId, e);
      return new ExchangeResponse(false, e.getMessage());
    }
  }

  /** 查看共享功法 */
  @Tool(description = "查看宗门共享功法列表，包含功法名称、等级要求和学习消耗")
  public SharedSkillQueryResponse querySharedSkills() {
    try {
      Long userId = getCurrentUserId();
      String result = sectSharedSkillService.getSharedSkills(userId);
      return new SharedSkillQueryResponse(true, result);
    } catch (Exception e) {
      log.error("查询共享功法失败", e);
      return new SharedSkillQueryResponse(false, e.getMessage());
    }
  }

  /** 学习共享功法 */
  @Tool(description = "消耗贡献值学习宗门共享功法")
  public LearnSkillResponse learnSharedSkill(
      @ToolParam(description = "共享功法编号，从querySharedSkills结果中获取") long sharedSkillId) {
    try {
      Long userId = getCurrentUserId();
      String result = sectSharedSkillService.learnSharedSkill(userId, sharedSkillId);
      return new LearnSkillResponse(true, result);
    } catch (Exception e) {
      log.error("学习共享功法失败: sharedSkillId={}", sharedSkillId, e);
      return new LearnSkillResponse(false, e.getMessage());
    }
  }

  /** 提交功法玉简 */
  @Tool(description = "将背包中的功法玉简提交到宗门，获得300贡献值。一次性奖励，消耗玉简。")
  public SubmitJadeResponse submitSkillJade(
      @ToolParam(description = "功法玉简名称，如'烈焰诀玉简'") String jadeName) {
    try {
      Long userId = getCurrentUserId();
      String result = sectSharedSkillService.submitSkillJade(userId, jadeName);
      return new SubmitJadeResponse(true, result);
    } catch (Exception e) {
      log.error("提交功法玉简失败: jadeName={}", jadeName, e);
      return new SubmitJadeResponse(false, e.getMessage());
    }
  }

  /** 捐献灵石 */
  @Tool(description = "向宗门捐献灵石，获得贡献（捐献量×0.1），宗门资金等额增加")
  public DonateResponse donateStones(@ToolParam(description = "捐献灵石数量") long amount) {
    try {
      Long userId = getCurrentUserId();
      String result = sectMemberService.donateStones(userId, amount);
      return new DonateResponse(true, result);
    } catch (Exception e) {
      log.error("捐献灵石失败: amount={}", amount, e);
      return new DonateResponse(false, e.getMessage());
    }
  }

  /** 邀请成员（长老+） */
  @Tool(description = "邀请散修玩家加入宗门（需要长老或宗主权限）")
  public InviteMemberResponse inviteMember(
      @ToolParam(description = "目标玩家的道号") String targetNickname) {
    try {
      Long userId = getCurrentUserId();
      String result = sectMemberService.inviteMember(userId, targetNickname);
      return new InviteMemberResponse(true, result);
    } catch (Exception e) {
      log.error("邀请成员失败: targetNickname={}", targetNickname, e);
      return new InviteMemberResponse(false, e.getMessage());
    }
  }

  /** 踢出成员（长老+） */
  @Tool(description = "踢出宗门成员（需要长老或宗主权限，只能踢出比自己职位低的成员）")
  public KickMemberResponse kickMember(@ToolParam(description = "要踢出的成员道号") String targetNickname) {
    try {
      Long userId = getCurrentUserId();
      String result = sectMemberService.kickMember(userId, targetNickname);
      return new KickMemberResponse(true, result);
    } catch (Exception e) {
      log.error("踢出成员失败: targetNickname={}", targetNickname, e);
      return new KickMemberResponse(false, e.getMessage());
    }
  }

  /** 发布/更新公告（长老+） */
  @Tool(description = "发布或更新宗门公告（需要长老或宗主权限）")
  public PostNoticeResponse postNotice(@ToolParam(description = "公告内容") String content) {
    try {
      Long userId = getCurrentUserId();
      String result = sectMemberService.setNotice(userId, content);
      return new PostNoticeResponse(true, result);
    } catch (Exception e) {
      log.error("发布公告失败", e);
      return new PostNoticeResponse(false, e.getMessage());
    }
  }

  /** 下架共享功法（长老+） */
  @Tool(description = "下架共享功法（设置为PENDING状态，已学习的成员不受影响）（需要长老或宗主权限）")
  public RemoveSkillResponse removeSharedSkill(
      @ToolParam(description = "共享功法编号，从querySharedSkills结果中获取") long sharedSkillId) {
    try {
      Long userId = getCurrentUserId();
      String result = sectSharedSkillService.removeSharedSkill(userId, sharedSkillId);
      return new RemoveSkillResponse(true, result);
    } catch (Exception e) {
      log.error("下架共享功法失败: sharedSkillId={}", sharedSkillId, e);
      return new RemoveSkillResponse(false, e.getMessage());
    }
  }

  /** 上架共享功法（长老+） */
  @Tool(description = "上架共享功法（PENDING → LISTED，需要长老或宗主权限）")
  public ListSkillResponse listSharedSkill(
      @ToolParam(description = "共享功法编号，从待上架列表获取") long sharedSkillId) {
    try {
      Long userId = getCurrentUserId();
      String result = sectSharedSkillService.listSharedSkill(userId, sharedSkillId);
      return new ListSkillResponse(true, result);
    } catch (Exception e) {
      log.error("上架共享功法失败: sharedSkillId={}", sharedSkillId, e);
      return new ListSkillResponse(false, e.getMessage());
    }
  }

  /** 查看宗门任务 */
  @Tool(description = "查看当前宗门事件任务列表")
  public TaskQueryResponse queryTasks() {
    try {
      Long userId = getCurrentUserId();
      String result = sectMemberService.getTasks(userId);
      return new TaskQueryResponse(true, result);
    } catch (Exception e) {
      log.error("查询宗门任务失败", e);
      return new TaskQueryResponse(false, e.getMessage());
    }
  }

  /** 任命/罢免（宗主） */
  @Tool(description = "任命或罢免宗门成员职位（仅宗主可用）。可任命为ELDER（长老），或传承宗主之位（LEADER）。")
  public AppointMemberResponse appointMember(
      @ToolParam(description = "目标成员道号") String targetNickname,
      @ToolParam(description = "新职位代码：LEADER（宗主）/ ELDER（长老）/ MEMBER（弟子）") String positionCode) {
    try {
      Long userId = getCurrentUserId();
      String result = sectMemberService.appointMember(userId, targetNickname, positionCode);
      return new AppointMemberResponse(true, result);
    } catch (Exception e) {
      log.error("任命成员失败: targetNickname={}, position={}", targetNickname, positionCode, e);
      return new AppointMemberResponse(false, e.getMessage());
    }
  }

  /** 升级宗门（宗主） */
  @Tool(description = "消耗宗门资金提升宗门等级（仅宗主可用）")
  public UpgradeSectResponse upgradeSect() {
    try {
      Long userId = getCurrentUserId();
      String result = sectMemberService.upgradeSect(userId);
      return new UpgradeSectResponse(true, result);
    } catch (Exception e) {
      log.error("升级宗门失败", e);
      return new UpgradeSectResponse(false, e.getMessage());
    }
  }

  /** 扩充成员上限（宗主） */
  @Tool(description = "消耗宗门资金扩充成员上限+5（仅宗主可用）")
  public ExpandMembersResponse expandMembers() {
    try {
      Long userId = getCurrentUserId();
      String result = sectMemberService.expandMembers(userId);
      return new ExpandMembersResponse(true, result);
    } catch (Exception e) {
      log.error("扩充成员上限失败", e);
      return new ExpandMembersResponse(false, e.getMessage());
    }
  }

  /** 查看建筑（宗主） */
  @Tool(description = "查看宗门建筑状态（仅宗主可用）")
  public BuildingQueryResponse queryBuildings() {
    try {
      Long userId = getCurrentUserId();
      String result = sectBuildingService.getBuildings(userId);
      return new BuildingQueryResponse(true, result);
    } catch (Exception e) {
      log.error("查询建筑失败", e);
      return new BuildingQueryResponse(false, e.getMessage());
    }
  }

  /** 建造建筑（宗主） */
  @Tool(
      description =
          "消耗宗门资金建造建筑（仅宗主可用）。建筑类型：SCRIPTURE_PAVILION（藏经阁）/ TRAINING_ROOM（练功房）/ ALCHEMY_CHAMBER（炼丹房）/ SPIRIT_VEIN（灵脉）/ FORGE_WORKSHOP（锻造坊）/ GUARD_ARRAY（护阵）/ HERB_GARDEN（药园）")
  public BuildStructureResponse buildStructure(
      @ToolParam(description = "建筑类型代码") String buildingTypeCode) {
    try {
      Long userId = getCurrentUserId();
      String result = sectBuildingService.buildStructure(userId, buildingTypeCode);
      return new BuildStructureResponse(true, result);
    } catch (Exception e) {
      log.error("建造建筑失败: buildingTypeCode={}", buildingTypeCode, e);
      return new BuildStructureResponse(false, e.getMessage());
    }
  }

  /** 升级建筑（宗主） */
  @Tool(description = "消耗宗门资金升级建筑（仅宗主可用）")
  public UpgradeBuildingResponse upgradeBuilding(
      @ToolParam(description = "建筑类型代码") String buildingTypeCode) {
    try {
      Long userId = getCurrentUserId();
      String result = sectBuildingService.upgradeBuilding(userId, buildingTypeCode);
      return new UpgradeBuildingResponse(true, result);
    } catch (Exception e) {
      log.error("升级建筑失败: buildingTypeCode={}", buildingTypeCode, e);
      return new UpgradeBuildingResponse(false, e.getMessage());
    }
  }

  // ===================== 响应 Record 定义 =====================

  public record ShopQueryResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}

  public record ExchangeResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}

  public record SharedSkillQueryResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}

  public record LearnSkillResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}

  public record SubmitJadeResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}

  public record DonateResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}

  public record InviteMemberResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}

  public record KickMemberResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}

  public record PostNoticeResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}

  public record RemoveSkillResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}

  public record ListSkillResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}

  public record TaskQueryResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}

  public record AppointMemberResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}

  public record UpgradeSectResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}

  public record ExpandMembersResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}

  public record BuildingQueryResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}

  public record BuildStructureResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}

  public record UpgradeBuildingResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}
}
