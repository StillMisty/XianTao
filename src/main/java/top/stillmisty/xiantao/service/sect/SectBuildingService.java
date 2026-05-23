package top.stillmisty.xiantao.service.sect;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.sect.entity.Sect;
import top.stillmisty.xiantao.domain.sect.entity.SectBuilding;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.domain.sect.enums.SectBuildingType;
import top.stillmisty.xiantao.domain.sect.repository.SectBuildingRepository;
import top.stillmisty.xiantao.domain.sect.repository.SectMemberRepository;
import top.stillmisty.xiantao.domain.sect.repository.SectRepository;
import top.stillmisty.xiantao.domain.sect.vo.BuildResultVO;
import top.stillmisty.xiantao.domain.sect.vo.BuildingsQueryVO;
import top.stillmisty.xiantao.domain.sect.vo.UpgradeBuildingResultVO;
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
    BuildingsQueryVO vo = getBuildings(userId);
    return new ServiceResult.Success<>(formatBuildingsText(vo));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> buildStructure(
      PlatformType platform, String openId, String buildingTypeCode) {
    Long userId = UserContext.getCurrentUserId();
    BuildResultVO vo = buildStructure(userId, buildingTypeCode);
    return new ServiceResult.Success<>(
        "已建造"
            + vo.buildingName()
            + " Lv."
            + vo.level()
            + "，消耗资金 "
            + vo.cost()
            + "（剩余 "
            + vo.remainingFunds()
            + "）。");
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> upgradeBuilding(
      PlatformType platform, String openId, String buildingTypeCode) {
    Long userId = UserContext.getCurrentUserId();
    UpgradeBuildingResultVO vo = upgradeBuilding(userId, buildingTypeCode);
    return new ServiceResult.Success<>(
        "已将"
            + vo.buildingName()
            + " 从 Lv."
            + vo.oldLevel()
            + " 升级至 Lv."
            + vo.newLevel()
            + "，消耗资金 "
            + vo.cost()
            + "（剩余 "
            + vo.remainingFunds()
            + "）。");
  }

  // ===================== 内部 API =====================

  @Cacheable(cacheNames = "sect_buildings", key = "#userId")
  public BuildingsQueryVO getBuildings(Long userId) {
    SectMember member = requireMember(userId);
    List<SectBuilding> buildings = sectBuildingRepository.findBySectId(member.getSectId());

    List<BuildingsQueryVO.BuildingEntry> built =
        buildings.stream()
            .map(
                b -> {
                  var type = b.getBuildingType();
                  return new BuildingsQueryVO.BuildingEntry(
                      type.getCode(),
                      type.getName(),
                      b.getLevel(),
                      type.getMaxLevel(),
                      type.upgradeCost(),
                      0);
                })
            .toList();

    List<BuildingsQueryVO.BuildingEntry> buildable =
        java.util.Arrays.stream(SectBuildingType.values())
            .filter(type -> buildings.stream().noneMatch(b -> b.getBuildingType() == type))
            .map(
                type ->
                    new BuildingsQueryVO.BuildingEntry(
                        type.getCode(), type.getName(), 0, 0, 0, type.getBuildCost()))
            .toList();

    return new BuildingsQueryVO(built, buildable);
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(cacheNames = "sect_buildings", key = "#userId"),
        @CacheEvict(cacheNames = "sect_overview", key = "#userId")
      })
  public BuildResultVO buildStructure(Long userId, String buildingTypeCode) {
    SectMember member = requireMember(userId);
    if (!member.getPosition().canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    SectBuildingType type = resolveBuildingType(buildingTypeCode);

    if (sectBuildingRepository.findBySectIdAndType(member.getSectId(), type).isPresent()) {
      throw new BusinessException(ErrorCode.SECT_BUILDING_ALREADY_EXISTS);
    }

    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    long cost = type.getBuildCost();
    if (!sect.deductFunds(cost)) {
      throw new BusinessException(ErrorCode.SECT_FUNDS_INSUFFICIENT, cost, sect.getFunds());
    }
    sectRepository.save(sect);

    SectBuilding building =
        SectBuilding.create().setSectId(member.getSectId()).setBuildingType(type).setLevel(1);
    sectBuildingRepository.save(building);

    log.info("宗门 {} 建造建筑 {} Lv.1", sect.getId(), type.getName());
    return new BuildResultVO(type.getCode(), type.getName(), 1, cost, sect.getFunds());
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(cacheNames = "sect_buildings", key = "#userId"),
        @CacheEvict(cacheNames = "sect_overview", key = "#userId")
      })
  public UpgradeBuildingResultVO upgradeBuilding(Long userId, String buildingTypeCode) {
    SectMember member = requireMember(userId);
    if (!member.getPosition().canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    SectBuildingType type = resolveBuildingType(buildingTypeCode);

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
    return new UpgradeBuildingResultVO(
        type.getCode(), type.getName(), oldLevel, building.getLevel(), cost, sect.getFunds());
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

  // ===================== 格式化 =====================

  private static String formatBuildingsText(BuildingsQueryVO vo) {
    StringBuilder sb = new StringBuilder();
    sb.append("=== 宗门建筑 ===\n");

    if (vo.built().isEmpty()) {
      sb.append("暂无建筑，宗主可消耗宗门资金建造。\n\n");
    } else {
      for (var b : vo.built()) {
        sb.append("  ")
            .append(b.name())
            .append(" Lv.")
            .append(b.level())
            .append("/")
            .append(b.maxLevel())
            .append(" | 升级: ")
            .append(b.upgradeCost())
            .append(" 灵石\n");
      }
      sb.append("\n");
    }

    sb.append("可建造建筑：\n");
    for (var b : vo.buildable()) {
      sb.append("  ").append(b.name()).append(" | 建造: ").append(b.buildCost()).append(" 灵石\n");
    }

    return sb.toString();
  }

  private SectBuildingType resolveBuildingType(String buildingTypeCode) {
    try {
      return SectBuildingType.fromCode(buildingTypeCode);
    } catch (IllegalArgumentException e) {
      throw new BusinessException(ErrorCode.SECT_BUILDING_NOT_FOUND);
    }
  }

  private SectMember requireMember(Long userId) {
    return sectMemberRepository
        .findByUserId(userId)
        .filter(m -> m.getSectId() != null)
        .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
  }
}
