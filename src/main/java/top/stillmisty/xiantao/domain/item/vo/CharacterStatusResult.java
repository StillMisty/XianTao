package top.stillmisty.xiantao.domain.item.vo;

import java.util.List;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;

/** 角色状态查看结果 VO 包含：HP、属性、装扮、境界进度、当前状态 */
public record CharacterStatusResult(
    boolean success,
    String message,
    Long userId,
    String nickname,
    Integer level,
    Long exp,
    Long expToNextLevel,
    Double expPercentage,
    UserStatus status,
    String statusName,
    Long locationId,
    String locationName,
    Long travelDestinationId,
    String travelDestinationName,
    java.time.LocalDateTime travelStartTime,
    java.time.LocalDateTime estimatedArrivalTime,
    Integer travelTimeMinutes,
    Long travelMinutesElapsed,
    Long travelMinutesRemaining,
    Integer hpCurrent,
    Integer hpMax,
    Double hpPercentage,
    Integer statStr,
    Integer statCon,
    Integer statAgi,
    Integer statWis,
    Integer equipStr,
    Integer equipCon,
    Integer equipAgi,
    Integer equipWis,
    Integer totalStr,
    Integer totalCon,
    Integer totalAgi,
    Integer totalWis,
    Integer attack,
    Integer defense,
    Long spiritStones,
    Double breakthroughSuccessRate,
    Integer breakthroughFailCount,
    Integer protectorCount,
    Integer maxProtectorCount,
    List<ProtectionInfoVO> protectingList,
    List<ProtectionInfoVO> protectedByList,
    Double totalProtectionBonus,
    EquipmentSummary equipment) {
  public static record EquipmentSummary(Integer totalEquipped, List<EquipmentSummaryItem> items) {}

  public static record EquipmentSummaryItem(
      Long equipmentId,
      String name,
      EquipmentSlot slot,
      String slotName,
      Rarity rarity,
      String rarityName,
      Integer strBonus,
      Integer conBonus,
      Integer agiBonus,
      Integer wisBonus,
      Integer attackBonus,
      Integer defenseBonus) {}

  /** 护道信息 VO */
  public static record ProtectionInfoVO(
      Long userId,
      String userName,
      Integer userLevel,
      Long locationId,
      String locationName,
      Boolean isInSameLocation,
      Double bonusPercentage) {}
}
