package top.stillmisty.xiantao.service.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.sect.vo.BuildResultVO;
import top.stillmisty.xiantao.domain.sect.vo.ExpandMembersResultVO;
import top.stillmisty.xiantao.domain.sect.vo.UpgradeBuildingResultVO;
import top.stillmisty.xiantao.domain.sect.vo.UpgradeSectResultVO;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.sect.SectBuildingService;
import top.stillmisty.xiantao.service.sect.SectMemberService;

/** 宗门宗主专属工具 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SectLeaderTools {

  private final SectMemberService sectMemberService;
  private final SectBuildingService sectBuildingService;

  @Tool(description = "任命或罢免成员职位。可设为 ELDER/MEMBER，或传 MEMBER 罢免")
  @Transactional
  public AppointMemberResponse appointMember(
      @ToolParam(description = "目标成员道号") String targetNickname,
      @ToolParam(description = "职位代码：ELDER/MEMBER") String positionCode) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      sectMemberService.appointMember(userId, targetNickname, positionCode);
      return new AppointMemberResponse(targetNickname, positionCode, null);
    } catch (Exception e) {
      log.error("任命成员失败: targetNickname={}, position={}", targetNickname, positionCode, e);
      return new AppointMemberResponse(targetNickname, positionCode, e.getMessage());
    }
  }

  @Tool(description = "提升宗门等级，消耗宗门资金。等级越高消耗越大")
  @Transactional
  public UpgradeSectResponse upgradeSect() {
    try {
      Long userId = UserContext.requireCurrentUserId();
      UpgradeSectResultVO result = sectMemberService.upgradeSect(userId);
      return new UpgradeSectResponse(
          result.newLevel(), result.newMaxMembers(), result.cost(), result.remainingFunds(), null);
    } catch (Exception e) {
      log.error("升级宗门失败", e);
      return new UpgradeSectResponse(0, 0, 0, 0, e.getMessage());
    }
  }

  @Tool(description = "扩充宗门成员上限，消耗宗门资金")
  @Transactional
  public ExpandMembersResponse expandMembers() {
    try {
      Long userId = UserContext.requireCurrentUserId();
      ExpandMembersResultVO result = sectMemberService.expandMembers(userId);
      return new ExpandMembersResponse(
          result.addedSlots(),
          result.newMaxMembers(),
          result.cost(),
          result.remainingFunds(),
          null);
    } catch (Exception e) {
      log.error("扩充成员上限失败", e);
      return new ExpandMembersResponse(0, 0, 0, 0, e.getMessage());
    }
  }

  @Tool(
      description =
          "建造宗门建筑。可选类型：SCRIPTURE_PAVILION/TRAINING_HALL/ALCHEMY_ROOM/SPIRIT_VEIN/FORGE_WORKSHOP/GUARD_FORMATION/HERB_GARDEN")
  @Transactional
  public BuildStructureResponse buildStructure(
      @ToolParam(description = "建筑类型代码") String buildingTypeCode) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      BuildResultVO result = sectBuildingService.buildStructure(userId, buildingTypeCode);
      return new BuildStructureResponse(
          result.buildingTypeCode(),
          result.buildingName(),
          result.level(),
          result.cost(),
          result.remainingFunds(),
          null);
    } catch (Exception e) {
      log.error("建造建筑失败: buildingTypeCode={}", buildingTypeCode, e);
      return new BuildStructureResponse(buildingTypeCode, null, 0, 0, 0, e.getMessage());
    }
  }

  @Tool(description = "升级指定宗门建筑，消耗宗门资金")
  @Transactional
  public UpgradeBuildingResponse upgradeBuilding(
      @ToolParam(description = "建筑类型代码") String buildingTypeCode) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      UpgradeBuildingResultVO result =
          sectBuildingService.upgradeBuilding(userId, buildingTypeCode);
      return new UpgradeBuildingResponse(
          result.buildingTypeCode(),
          result.buildingName(),
          result.oldLevel(),
          result.newLevel(),
          result.cost(),
          result.remainingFunds(),
          null);
    } catch (Exception e) {
      log.error("升级建筑失败: buildingTypeCode={}", buildingTypeCode, e);
      return new UpgradeBuildingResponse(buildingTypeCode, null, 0, 0, 0, 0, e.getMessage());
    }
  }

  // ===================== 响应 Record =====================

  public record AppointMemberResponse(
      @JsonPropertyDescription("目标道号") String targetNickname,
      @JsonPropertyDescription("职位代码") String positionCode,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record UpgradeSectResponse(
      @JsonPropertyDescription("新等级") int newLevel,
      @JsonPropertyDescription("新成员上限") int newMaxMembers,
      @JsonPropertyDescription("消耗资金") long cost,
      @JsonPropertyDescription("剩余资金") long remainingFunds,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record ExpandMembersResponse(
      @JsonPropertyDescription("新增槽位") int addedSlots,
      @JsonPropertyDescription("新成员上限") int newMaxMembers,
      @JsonPropertyDescription("消耗资金") long cost,
      @JsonPropertyDescription("剩余资金") long remainingFunds,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record BuildStructureResponse(
      @JsonPropertyDescription("建筑类型代码") String buildingTypeCode,
      @JsonPropertyDescription("建筑名称") String buildingName,
      @JsonPropertyDescription("建筑等级") int level,
      @JsonPropertyDescription("消耗资金") long cost,
      @JsonPropertyDescription("剩余资金") long remainingFunds,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record UpgradeBuildingResponse(
      @JsonPropertyDescription("建筑类型代码") String buildingTypeCode,
      @JsonPropertyDescription("建筑名称") String buildingName,
      @JsonPropertyDescription("升级前等级") int oldLevel,
      @JsonPropertyDescription("升级后等级") int newLevel,
      @JsonPropertyDescription("消耗资金") long cost,
      @JsonPropertyDescription("剩余资金") long remainingFunds,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}
}
