package top.stillmisty.xiantao.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
  public SectToolResponse appointMember(
      @ToolParam(description = "目标成员道号") String targetNickname,
      @ToolParam(description = "职位代码：ELDER/MEMBER") String positionCode) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectMemberService.appointMember(userId, targetNickname, positionCode);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("任命成员失败: targetNickname={}, position={}", targetNickname, positionCode, e);
      return new SectToolResponse(false, e.getMessage());
    }
  }

  @Tool(description = "提升宗门等级，消耗宗门资金。等级越高消耗越大")
  @Transactional
  public SectToolResponse upgradeSect() {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectMemberService.upgradeSect(userId);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("升级宗门失败", e);
      return new SectToolResponse(false, e.getMessage());
    }
  }

  @Tool(description = "扩充宗门成员上限，消耗宗门资金")
  @Transactional
  public SectToolResponse expandMembers() {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectMemberService.expandMembers(userId);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("扩充成员上限失败", e);
      return new SectToolResponse(false, e.getMessage());
    }
  }

  @Tool(description = "查看宗门所有建筑及其等级状态")
  public SectToolResponse queryBuildings() {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectBuildingService.getBuildings(userId);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("查询建筑失败", e);
      return new SectToolResponse(false, e.getMessage());
    }
  }

  @Tool(
      description =
          "建造宗门建筑。可选类型：SCRIPTURE_PAVILION/TRAINING_HALL/ALCHEMY_ROOM/SPIRIT_VEIN/FORGE_WORKSHOP/GUARD_FORMATION/HERB_GARDEN")
  @Transactional
  public SectToolResponse buildStructure(
      @ToolParam(description = "建筑类型代码") String buildingTypeCode) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectBuildingService.buildStructure(userId, buildingTypeCode);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("建造建筑失败: buildingTypeCode={}", buildingTypeCode, e);
      return new SectToolResponse(false, e.getMessage());
    }
  }

  @Tool(description = "升级指定宗门建筑，消耗宗门资金")
  @Transactional
  public SectToolResponse upgradeBuilding(
      @ToolParam(description = "建筑类型代码") String buildingTypeCode) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String result = sectBuildingService.upgradeBuilding(userId, buildingTypeCode);
      return new SectToolResponse(true, result);
    } catch (Exception e) {
      log.error("升级建筑失败: buildingTypeCode={}", buildingTypeCode, e);
      return new SectToolResponse(false, e.getMessage());
    }
  }
}
