package top.stillmisty.xiantao.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.vo.CharacterStatusResult;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.shared.SharedKernel;
import top.stillmisty.xiantao.domain.user.entity.DaoProtection;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.DaoProtectionRepository;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.service.annotation.Authenticated;

/** 角色状态服务 负责：角色详情查询（HP、属性、装备、突破进度、护道信息） */
@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterStatusService {

  private final UserStateService userStateService;
  private final UserRepository userRepository;
  private final EquipmentRepository equipmentRepository;
  private final DaoProtectionRepository daoProtectionRepository;
  private final MapService mapService;
  private final MapNodeRepository mapNodeRepository;

  @Authenticated
  public ServiceResult<CharacterStatusResult> getCharacterStatus(
      PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getCharacterStatus(userId));
  }

  // ===================== 内部 API（需预先完成认证） =====================

  /** 查看角色状态（状态） 包含：HP、属性、装扮（已穿戴装备）、境界进度（等级经验）、当前状态 */
  public CharacterStatusResult getCharacterStatus(Long userId) {
    User user = userStateService.loadUser(userId);

    EquipData equipData = buildEquipData(userId);
    ProtectionData protData = buildProtectionData(userId, user);
    TravelData travelData = buildTravelData(user);

    int totalStr = user.getEffectiveStatStr() + equipData.equipStr;
    int totalCon = user.getEffectiveStatCon() + equipData.equipCon;
    int totalAgi = user.getEffectiveStatAgi() + equipData.equipAgi;
    int totalWis = user.getEffectiveStatWis() + equipData.equipWis;

    int attack = totalStr * 2 + equipData.equipAttack;
    int defense = totalCon + equipData.equipDefense;
    int hpMax = 100 + totalCon * 20;

    return new CharacterStatusResult(
        true,
        "",
        user.getId(),
        user.getNickname(),
        user.getLevel(),
        user.getExp(),
        user.calculateExpToNextLevel(),
        user.getExp() * 100.0 / user.calculateExpToNextLevel(),
        user.getStatus(),
        user.getStatus().getName(),
        user.getLocationId(),
        travelData.locationName,
        travelData.destinationId,
        travelData.destinationName,
        travelData.startTime,
        travelData.estimatedArrivalTime,
        travelData.timeMinutes,
        travelData.minutesElapsed,
        travelData.minutesRemaining,
        user.getHpCurrent(),
        hpMax,
        user.getHpCurrent() * 100.0 / hpMax,
        user.getEffectiveStatStr(),
        user.getEffectiveStatCon(),
        user.getEffectiveStatAgi(),
        user.getEffectiveStatWis(),
        equipData.equipStr,
        equipData.equipCon,
        equipData.equipAgi,
        equipData.equipWis,
        totalStr,
        totalCon,
        totalAgi,
        totalWis,
        attack,
        defense,
        user.getSpiritStones(),
        user.calculateBreakthroughSuccessRate(),
        user.getBreakthroughFailCount(),
        protData.protectingCount,
        3,
        protData.protectingList,
        protData.protectedByList,
        protData.totalBonus,
        equipData.summary);
  }

  // ===================== 主流程提取的辅助方法 =====================

  private record EquipData(
      CharacterStatusResult.EquipmentSummary summary,
      int equipStr,
      int equipCon,
      int equipAgi,
      int equipWis,
      int equipAttack,
      int equipDefense) {}

  private record ProtectionData(
      List<CharacterStatusResult.ProtectionInfoVO> protectingList,
      List<CharacterStatusResult.ProtectionInfoVO> protectedByList,
      int protectingCount,
      double totalBonus) {}

  private record TravelData(
      String locationName,
      Long destinationId,
      String destinationName,
      LocalDateTime startTime,
      LocalDateTime estimatedArrivalTime,
      Integer timeMinutes,
      Long minutesElapsed,
      Long minutesRemaining) {}

  private EquipData buildEquipData(Long userId) {
    List<Equipment> equippedItems = equipmentRepository.findEquippedByUserId(userId);

    int equipStr = 0, equipCon = 0, equipAgi = 0, equipWis = 0;
    int equipAttack = 0, equipDefense = 0;

    for (Equipment equipment : equippedItems) {
      equipStr += equipment.getStrBonus();
      equipCon += equipment.getConBonus();
      equipAgi += equipment.getAgiBonus();
      equipWis += equipment.getWisBonus();
      equipAttack += equipment.getAttackBonus();
      equipDefense += equipment.getDefenseBonus();
    }

    CharacterStatusResult.EquipmentSummary summary =
        new CharacterStatusResult.EquipmentSummary(
            equippedItems.size(),
            equippedItems.stream().map(this::convertToEquipmentSummaryItem).toList());

    return new EquipData(
        summary, equipStr, equipCon, equipAgi, equipWis, equipAttack, equipDefense);
  }

  private ProtectionData buildProtectionData(Long userId, User user) {
    List<DaoProtection> protectingList = daoProtectionRepository.findByProtectorId(userId);
    List<DaoProtection> protectedByList = daoProtectionRepository.findByProtegeId(userId);

    List<CharacterStatusResult.ProtectionInfoVO> protectingVOList =
        convertToProtectionInfoVO(protectingList, user);
    List<CharacterStatusResult.ProtectionInfoVO> protectedByVOList =
        convertToProtectionInfoVOWithBonus(protectedByList, user);

    double totalProtectionBonus =
        protectedByVOList.stream()
            .filter(info -> Boolean.TRUE.equals(info.isInSameLocation()))
            .mapToDouble(CharacterStatusResult.ProtectionInfoVO::bonusPercentage)
            .sum();
    totalProtectionBonus = Math.min(20.0, totalProtectionBonus);

    return new ProtectionData(
        protectingVOList, protectedByVOList, protectingList.size(), totalProtectionBonus);
  }

  private TravelData buildTravelData(User user) {
    String locationName = mapService.getMapName(user.getLocationId());
    Long travelDestinationId = null;
    String travelDestinationName = null;
    LocalDateTime travelStartTime = null;
    LocalDateTime estimatedArrivalTime = null;
    Integer travelTimeMinutes = null;
    Long travelMinutesElapsed = null;
    Long travelMinutesRemaining = null;

    if (user.getStatus() == UserStatus.RUNNING && user.getTravelDestinationId() != null) {
      travelDestinationId = user.getTravelDestinationId();
      travelDestinationName = mapService.getMapName(user.getTravelDestinationId());
      travelStartTime = user.getTravelStartTime();

      var currentMap = mapNodeRepository.findById(user.getLocationId());
      if (currentMap.isPresent()) {
        travelTimeMinutes = currentMap.get().getTravelTimeTo(user.getTravelDestinationId());
      }

      if (travelStartTime != null) {
        LocalDateTime now = LocalDateTime.now();
        if (travelTimeMinutes != null) {
          travelMinutesElapsed =
              Math.min(
                  travelTimeMinutes.longValue(),
                  Duration.between(travelStartTime, now).toMinutes());
          travelMinutesRemaining =
              Math.max(0, travelTimeMinutes.longValue() - travelMinutesElapsed);
          estimatedArrivalTime = travelStartTime.plusMinutes(travelTimeMinutes);
        }
      }
    }

    return new TravelData(
        locationName,
        travelDestinationId,
        travelDestinationName,
        travelStartTime,
        estimatedArrivalTime,
        travelTimeMinutes,
        travelMinutesElapsed,
        travelMinutesRemaining);
  }

  // ===================== 辅助转换方法 =====================

  private CharacterStatusResult.EquipmentSummaryItem convertToEquipmentSummaryItem(
      Equipment equipment) {
    return new CharacterStatusResult.EquipmentSummaryItem(
        equipment.getId(),
        equipment.getName(),
        equipment.getSlot(),
        equipment.getSlot().getName(),
        equipment.getRarity(),
        equipment.getRarity().getName(),
        equipment.getStrBonus(),
        equipment.getConBonus(),
        equipment.getAgiBonus(),
        equipment.getWisBonus(),
        equipment.getAttackBonus(),
        equipment.getDefenseBonus());
  }

  private List<CharacterStatusResult.ProtectionInfoVO> convertToProtectionInfoVO(
      List<DaoProtection> protections, User currentUser) {
    if (protections == null || protections.isEmpty()) {
      return List.of();
    }

    return protections.stream()
        .map(
            protection -> {
              Optional<User> targetUserOpt = userRepository.findById(protection.getProtegeId());
              if (targetUserOpt.isEmpty()) {
                return null;
              }

              User targetUser = targetUserOpt.get();
              boolean inSameLocation = SharedKernel.isInSameLocation(currentUser, targetUser);
              double bonus = SharedKernel.calculateSingleProtectorBonus(currentUser, targetUser);

              return new CharacterStatusResult.ProtectionInfoVO(
                  targetUser.getId(),
                  targetUser.getNickname(),
                  targetUser.getLevel(),
                  targetUser.getLocationId(),
                  mapService.getMapName(targetUser.getLocationId()),
                  inSameLocation,
                  bonus);
            })
        .filter(Objects::nonNull)
        .toList();
  }

  private List<CharacterStatusResult.ProtectionInfoVO> convertToProtectionInfoVOWithBonus(
      List<DaoProtection> protections, User currentUser) {
    if (protections == null || protections.isEmpty()) {
      return List.of();
    }

    return protections.stream()
        .map(
            protection -> {
              Optional<User> protectorOpt = userRepository.findById(protection.getProtectorId());
              if (protectorOpt.isEmpty()) {
                return null;
              }

              User protector = protectorOpt.get();
              boolean inSameLocation = SharedKernel.isInSameLocation(currentUser, protector);

              double bonus = 0.0;
              if (inSameLocation) {
                bonus = SharedKernel.calculateSingleProtectorBonus(protector, currentUser);
              }

              return new CharacterStatusResult.ProtectionInfoVO(
                  protector.getId(),
                  protector.getNickname(),
                  protector.getLevel(),
                  protector.getLocationId(),
                  mapService.getMapName(protector.getLocationId()),
                  inSameLocation,
                  bonus);
            })
        .filter(Objects::nonNull)
        .toList();
  }
}
