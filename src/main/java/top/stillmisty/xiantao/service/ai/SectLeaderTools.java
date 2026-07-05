package top.stillmisty.xiantao.service.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.sect.enums.SectBuildingType;
import top.stillmisty.xiantao.domain.sect.enums.SectPosition;
import top.stillmisty.xiantao.domain.sect.vo.BuildResultVO;
import top.stillmisty.xiantao.domain.sect.vo.ExpandMembersResultVO;
import top.stillmisty.xiantao.domain.sect.vo.UpgradeBuildingResultVO;
import top.stillmisty.xiantao.domain.sect.vo.UpgradeSectResultVO;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.sect.SectBuildingService;
import top.stillmisty.xiantao.service.sect.SectMemberService;

/**
 * 宗主专属工具（最高权限）。
 *
 * <p>仅有 LEADER 职位时才会注册这些工具。 与 {@link SectMemberTools} 和 {@link SectElderTools} 组合使用。
 *
 * <p>关键区分：宗门资金与灵石是不同的货币体系，不可互兑。 宗门建筑建造/升级消耗宗门资金，成员捐献灵石获得贡献值。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SectLeaderTools {

  private final ToolExecutor toolExecutor;
  private final SectMemberService sectMemberService;
  private final SectBuildingService sectBuildingService;

  /**
   * 任免成员职位。
   *
   * <p>仅宗主可操作。可将成员设为长老（ELDER）或降为弟子（MEMBER）。 不可任免职位高于自己的成员（即不可任免其他宗主）。
   *
   * @param targetNickname 目标道号
   * @param position 职位：ELDER=长老/执事, MEMBER=弟子
   */
  @Tool(description = "任免成员职位。不可任免职位高于自己的成员")
  @Transactional
  public AppointMemberResponse appointMember(
      @ToolParam(description = "目标成员道号") String targetNickname,
      @ToolParam(description = "职位") SectPosition position) {
    return toolExecutor.execute(
        "appointMember",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          sectMemberService.appointMemberInternal(userId, targetNickname, position.getCode());
          return new AppointMemberResponse(targetNickname, position.getCode());
        });
  }

  /**
   * 提升宗门等级。
   *
   * <p>仅宗主可操作。消耗宗门资金提升等级。 等级提升可能解锁新建筑、增加成员上限等。
   */
  @Tool(description = "消耗宗门资金提升宗门等级。等级越高消耗越多")
  @Transactional
  public UpgradeSectResponse upgradeSect() {
    return toolExecutor.execute(
        "upgradeSect",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          UpgradeSectResultVO r = sectMemberService.upgradeSectInternal(userId);
          return new UpgradeSectResponse(
              r.newLevel(), r.newMaxMembers(), r.cost(), r.remainingFunds());
        });
  }

  /**
   * 扩充宗门成员上限。
   *
   * <p>仅宗主可操作。消耗宗门资金增加成员名额。 不能超过宗门等级允许的最大成员数。
   */
  @Tool(description = "消耗宗门资金扩充成员名额上限")
  @Transactional
  public ExpandMembersResponse expandMembers() {
    return toolExecutor.execute(
        "expandMembers",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          ExpandMembersResultVO r = sectMemberService.expandMembersInternal(userId);
          return new ExpandMembersResponse(
              r.addedSlots(), r.newMaxMembers(), r.cost(), r.remainingFunds());
        });
  }

  /**
   * 建造宗门建筑。
   *
   * <p>仅宗主可操作。消耗宗门资金建造指定类型建筑。每类建筑只能建一座。 建筑类型各有独特效果：灵脉产出宗门资金、藏经阁解锁功法槽、炼丹房提升丹药效果等。
   *
   * @param buildingType 建筑类型枚举
   */
  @Tool(description = "消耗宗门资金建造宗门建筑。仅宗主操作")
  @Transactional
  public BuildStructureResponse buildStructure(
      @ToolParam(description = "建筑类型") SectBuildingType buildingType) {
    return toolExecutor.execute(
        "buildStructure",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          BuildResultVO r =
              sectBuildingService.buildStructureInternal(userId, buildingType.getCode());
          return new BuildStructureResponse(
              r.buildingTypeCode(), r.buildingName(), r.level(), r.cost(), r.remainingFunds());
        });
  }

  /**
   * 升级宗门建筑。
   *
   * <p>仅宗主可操作。消耗宗门资金升级指定建筑。每类建筑有最大等级限制， 达到上限后无法继续升级。升级效果取决于建筑类型。
   *
   * @param buildingType 建筑类型枚举
   */
  @Tool(description = "消耗宗门资金升级宗门建筑。有最大等级限制")
  @Transactional
  public UpgradeBuildingResponse upgradeBuilding(
      @ToolParam(description = "建筑类型") SectBuildingType buildingType) {
    return toolExecutor.execute(
        "upgradeBuilding",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          UpgradeBuildingResultVO r =
              sectBuildingService.upgradeBuildingInternal(userId, buildingType.getCode());
          return new UpgradeBuildingResponse(
              r.buildingTypeCode(),
              r.buildingName(),
              r.oldLevel(),
              r.newLevel(),
              r.cost(),
              r.remainingFunds());
        });
  }

  // === Inlined response records ===

  public record AppointMemberResponse(
      @JsonPropertyDescription("被任免的成员道号") String targetNickname,
      @JsonPropertyDescription("新职位代码") String positionCode) {}

  public record UpgradeSectResponse(
      @JsonPropertyDescription("宗门新等级") int newLevel,
      @JsonPropertyDescription("新的成员上限") int newMaxMembers,
      @JsonPropertyDescription("本次消耗的宗门资金") long cost,
      @JsonPropertyDescription("升级后剩余的宗门资金") long remainingFunds) {}

  public record ExpandMembersResponse(
      @JsonPropertyDescription("本次扩充增加的槽位数") int addedSlots,
      @JsonPropertyDescription("新的成员上限") int newMaxMembers,
      @JsonPropertyDescription("本次消耗的宗门资金") long cost,
      @JsonPropertyDescription("扩充后剩余的宗门资金") long remainingFunds) {}

  public record BuildStructureResponse(
      @JsonPropertyDescription("建筑类型代码") String buildingTypeCode,
      @JsonPropertyDescription("建造的建筑中文名称") String buildingName,
      @JsonPropertyDescription("建筑当前等级（新建为1）") int level,
      @JsonPropertyDescription("本次消耗的宗门资金") long cost,
      @JsonPropertyDescription("建造后剩余的宗门资金") long remainingFunds) {}

  public record UpgradeBuildingResponse(
      @JsonPropertyDescription("被升级的建筑类型代码") String buildingTypeCode,
      @JsonPropertyDescription("建筑中文名称") String buildingName,
      @JsonPropertyDescription("升级前等级") int oldLevel,
      @JsonPropertyDescription("升级后等级") int newLevel,
      @JsonPropertyDescription("本次消耗的宗门资金") long cost,
      @JsonPropertyDescription("升级后剩余的宗门资金") long remainingFunds) {}
}
