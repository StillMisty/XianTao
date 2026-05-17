package top.stillmisty.xiantao.service.sect;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.sect.entity.Sect;
import top.stillmisty.xiantao.domain.sect.entity.SectBuilding;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.domain.sect.enums.SectBuildingType;
import top.stillmisty.xiantao.domain.sect.repository.SectBuildingRepository;
import top.stillmisty.xiantao.domain.sect.repository.SectMemberRepository;
import top.stillmisty.xiantao.domain.sect.repository.SectRepository;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;

@Slf4j
@Service
@RequiredArgsConstructor
public class SectBuildingService {

  static final int DEFAULT_SCRIPTURE_SLOTS = 3;
  static final int SCRIPTURE_SLOTS_PER_LEVEL = 3;

  private final SectRepository sectRepository;
  private final SectMemberRepository sectMemberRepository;
  private final SectBuildingRepository sectBuildingRepository;

  // ===================== 公开 API =====================

  @Authenticated
  public ServiceResult<String> getBuildings(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getBuildings(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> buildStructure(
      PlatformType platform, String openId, String buildingTypeCode) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(buildStructure(userId, buildingTypeCode));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> upgradeBuilding(
      PlatformType platform, String openId, String buildingTypeCode) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(upgradeBuilding(userId, buildingTypeCode));
  }

  // ===================== 内部 API =====================

  public String getBuildings(Long userId) {
    SectMember member = requireMember(userId);
    List<SectBuilding> buildings = sectBuildingRepository.findBySectId(member.getSectId());

    StringBuilder sb = new StringBuilder();
    sb.append("=== 宗门建筑 ===\n");

    if (buildings.isEmpty()) {
      sb.append("暂无建筑，宗主可消耗宗门资金建造。\n\n");
    } else {
      for (SectBuilding b : buildings) {
        sb.append("  ")
            .append(b.getBuildingType().getName())
            .append(" Lv.")
            .append(b.getLevel())
            .append("/")
            .append(b.getBuildingType().getMaxLevel())
            .append(" | 升级: ")
            .append(b.getBuildingType().upgradeCost())
            .append(" 灵石\n");
      }
      sb.append("\n");
    }

    sb.append("可建造建筑：\n");
    for (SectBuildingType type : SectBuildingType.values()) {
      boolean built = buildings.stream().anyMatch(b -> b.getBuildingType() == type);
      if (!built) {
        sb.append("  ")
            .append(type.getName())
            .append(" | 建造: ")
            .append(type.getBuildCost())
            .append(" 灵石\n");
      }
    }

    return sb.toString();
  }

  @Transactional
  public String buildStructure(Long userId, String buildingTypeCode) {
    SectMember member = requireMember(userId);
    if (!member.getPosition().canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    SectBuildingType type;
    try {
      type = SectBuildingType.fromCode(buildingTypeCode);
    } catch (IllegalArgumentException e) {
      throw new BusinessException(ErrorCode.SECT_BUILDING_NOT_FOUND);
    }

    if (sectBuildingRepository.findBySectIdAndType(member.getSectId(), type).isPresent()) {
      throw new BusinessException(ErrorCode.SECT_BUILDING_ALREADY_EXISTS);
    }

    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    if (!sect.deductFunds(type.getBuildCost())) {
      throw new BusinessException(
          ErrorCode.SECT_FUNDS_INSUFFICIENT, type.getBuildCost(), sect.getFunds());
    }
    sectRepository.save(sect);

    SectBuilding building =
        SectBuilding.create().setSectId(member.getSectId()).setBuildingType(type).setLevel(1);
    sectBuildingRepository.save(building);

    log.info("宗门 {} 建造建筑 {} Lv.1", sect.getId(), type.getName());
    return "已建造" + type.getName() + " Lv.1，消耗资金 " + type.getBuildCost() + "。";
  }

  @Transactional
  public String upgradeBuilding(Long userId, String buildingTypeCode) {
    SectMember member = requireMember(userId);
    if (!member.getPosition().canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    SectBuildingType type;
    try {
      type = SectBuildingType.fromCode(buildingTypeCode);
    } catch (IllegalArgumentException e) {
      throw new BusinessException(ErrorCode.SECT_BUILDING_NOT_FOUND);
    }

    SectBuilding building =
        sectBuildingRepository
            .findBySectIdAndType(member.getSectId(), type)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_BUILDING_NOT_FOUND));

    if (building.isMaxLevel()) {
      throw new BusinessException(ErrorCode.SECT_BUILDING_MAX_LEVEL, type.getMaxLevel());
    }

    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    long cost = type.upgradeCost();
    if (!sect.deductFunds(cost)) {
      throw new BusinessException(ErrorCode.SECT_FUNDS_INSUFFICIENT, cost, sect.getFunds());
    }
    sectRepository.save(sect);

    int oldLevel = building.getLevel();
    building.setLevel(oldLevel + 1);
    sectBuildingRepository.save(building);

    log.info(
        "宗门 {} 升级建筑 {} Lv.{} -> Lv.{}",
        sect.getId(),
        type.getName(),
        oldLevel,
        building.getLevel());
    return "已将"
        + type.getName()
        + "从 Lv."
        + oldLevel
        + " 升级至 Lv."
        + building.getLevel()
        + "，消耗资金 "
        + cost
        + "。";
  }

  // ===================== 建筑加成查询 =====================

  public int getBuildingLevel(Long sectId, SectBuildingType type) {
    return sectBuildingRepository
        .findBySectIdAndType(sectId, type)
        .map(SectBuilding::getLevel)
        .orElse(0);
  }

  public double getTrainingBonus(Long sectId) {
    int level = getBuildingLevel(sectId, SectBuildingType.TRAINING_ROOM);
    return 1.0 + level * 0.03;
  }

  public double getAlchemyBonus(Long sectId) {
    int level = getBuildingLevel(sectId, SectBuildingType.ALCHEMY_CHAMBER);
    return 1.0 + level * 0.05;
  }

  public double getForgeDiscount(Long sectId) {
    int level = getBuildingLevel(sectId, SectBuildingType.FORGE_WORKSHOP);
    return 1.0 - level * 0.05;
  }

  public double getGuardDamageReduction(Long sectId) {
    int level = getBuildingLevel(sectId, SectBuildingType.GUARD_ARRAY);
    return 1.0 - level * 0.03;
  }

  public int getScriptureSlotCount(Long sectId) {
    int level = getBuildingLevel(sectId, SectBuildingType.SCRIPTURE_PAVILION);
    return DEFAULT_SCRIPTURE_SLOTS + level * SCRIPTURE_SLOTS_PER_LEVEL;
  }

  @Transactional
  public void settleSpiritVein(Long sectId) {
    SectBuilding vein =
        sectBuildingRepository
            .findBySectIdAndType(sectId, SectBuildingType.SPIRIT_VEIN)
            .orElse(null);
    if (vein == null) return;

    Sect sect =
        sectRepository
            .findById(sectId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime lastPayout = sect.getLastVeinPayout();
    if (lastPayout == null) {
      lastPayout = sect.getCreatedAt();
    }

    long hoursSinceLast = ChronoUnit.HOURS.between(lastPayout, now);
    if (hoursSinceLast <= 0) return;

    long income = hoursSinceLast * vein.getLevel() * 100 / 24;
    if (income > 0) {
      sect.addFunds(income);
      sect.setLastVeinPayout(now);
      sectRepository.save(sect);
    }
  }

  // ===================== 工具方法 =====================

  private SectMember requireMember(Long userId) {
    return sectMemberRepository
        .findByUserId(userId)
        .filter(m -> m.getSectId() != null)
        .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
  }
}
