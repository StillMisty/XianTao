package top.stillmisty.xiantao.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.vo.*;
import top.stillmisty.xiantao.domain.fudi.entity.CellConfig;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.enums.BeastQuality;
import top.stillmisty.xiantao.domain.fudi.vo.CellDetailVO;
import top.stillmisty.xiantao.domain.fudi.vo.CollectVO;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.annotation.Authenticated;

/** 灵兽系统外观层 — 对外 API 含认证，内部逻辑委托至各子服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeastService {

  private final BeastBreedingService beastBreedingService;
  private final BeastCombatService beastCombatService;
  private final BeastProductionService beastProductionService;
  private final BeastSkillService beastSkillService;
  private final BeastDisplayHelper beastDisplayHelper;

  // ===================== 灵兽战斗属性公式 =====================

  public static int calculateBeastAttack(int level, BeastQuality quality) {
    return BeastCombatService.calculateBeastAttack(level, quality);
  }

  public static int calculateBeastDefense(int level, BeastQuality quality) {
    return BeastCombatService.calculateBeastDefense(level, quality);
  }

  // ===================== 公开 API（含认证） =====================

  @Authenticated
  @Transactional
  public ServiceResult<PenCellVO> hatchBeast(
      PlatformType platform, String openId, String position, String eggName) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(beastBreedingService.hatchBeast(userId, position, eggName));
  }

  @Authenticated
  @Transactional
  public ServiceResult<PenCellVO> hatchBeastByInput(
      PlatformType platform, String openId, String position, String input) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(
        beastBreedingService.hatchBeastByInput(userId, position, input));
  }

  @Authenticated
  @Transactional
  public ServiceResult<ReleaseBeastVO> releaseBeast(
      PlatformType platform, String openId, String position) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(beastBreedingService.releaseBeast(userId, position));
  }

  @Authenticated
  @Transactional
  public ServiceResult<PenCellVO> evolveBeast(
      PlatformType platform, String openId, String position, String mode) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(beastBreedingService.evolveBeast(userId, position, mode));
  }

  @Authenticated
  @Transactional
  public ServiceResult<ActionResultVO> deployBeast(
      PlatformType platform, String openId, String position) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(beastCombatService.deployBeast(userId, position));
  }

  @Authenticated
  @Transactional
  public ServiceResult<BeastUndeployResult> undeployBeast(
      PlatformType platform, String openId, String position) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(beastCombatService.undeployBeast(userId, position));
  }

  @Authenticated
  @Transactional
  public ServiceResult<BeastRecoverResult> recoverBeast(
      PlatformType platform, String openId, String position) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(beastCombatService.recoverBeast(userId, position));
  }

  @Authenticated
  public ServiceResult<List<BeastStatusVO>> getDeployedBeasts(
      PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(beastCombatService.getDeployedBeasts(userId));
  }

  // ===================== 内部 API — 孵化/放生/进化 =====================

  @Transactional
  public PenCellVO hatchBeast(Long userId, String position, String eggName) {
    return beastBreedingService.hatchBeast(userId, position, eggName);
  }

  @Transactional
  public PenCellVO hatchBeastByInput(Long userId, String position, String input) {
    return beastBreedingService.hatchBeastByInput(userId, position, input);
  }

  @Transactional
  public ReleaseBeastVO releaseBeast(Long userId, String position) {
    return beastBreedingService.releaseBeast(userId, position);
  }

  @Transactional
  public PenCellVO evolveBeast(Long userId, String position, String mode) {
    return beastBreedingService.evolveBeast(userId, position, mode);
  }

  // ===================== 内部 API — 出战/召回/恢复/查询 =====================

  @Transactional
  public ActionResultVO deployBeast(Long userId, String position) {
    return beastCombatService.deployBeast(userId, position);
  }

  @Transactional
  public BeastUndeployResult undeployBeast(Long userId, String position) {
    return beastCombatService.undeployBeast(userId, position);
  }

  @Transactional
  public BeastRecoverResult recoverBeast(Long userId, String position) {
    return beastCombatService.recoverBeast(userId, position);
  }

  List<BeastStatusVO> getDeployedBeasts(Long userId) {
    return beastCombatService.getDeployedBeasts(userId);
  }

  // ===================== 内部 API — 灵兽产出 =====================

  CollectVO collectBeastProduce(Fudi fudi, FudiCell cell, Integer cellId) {
    return beastProductionService.collectBeastProduce(fudi, cell, cellId);
  }

  void updateBeastProduction(FudiCell cell, Fudi fudi) {
    beastProductionService.updateBeastProduction(cell, fudi);
  }

  List<CellConfig.ProductionItem> getProductionStoredList(FudiCell cell) {
    return beastProductionService.getProductionStoredList(cell);
  }

  // ===================== 内部 API — 灵兽技能 =====================

  public void tryAwakeningSkill(Beast beast) {
    beastSkillService.tryAwakeningSkill(beast);
  }

  // ===================== 内部 API — 经验 =====================

  @Transactional
  public void addBeastExp(Long beastId, long expToAdd) {
    beastCombatService.addBeastExp(beastId, expToAdd);
  }

  @Transactional
  public void addExpToDeployedBeasts(Long userId, long expToAdd) {
    beastCombatService.addExpToDeployedBeasts(userId, expToAdd);
  }

  // ===================== 内部 API — 地块操作/显示辅助 =====================

  Beast findBeastByCell(FudiCell cell) {
    return beastDisplayHelper.findBeastByCell(cell);
  }

  PenCellVO buildPenCellVO(FudiCell cell) {
    return beastDisplayHelper.buildPenCellVO(cell);
  }

  void buildPenCellDetail(CellDetailVO.CellDetailVOBuilder builder, FudiCell cell) {
    beastDisplayHelper.buildPenCellDetail(builder, cell);
  }

  void clearBeastCell(FudiCell cell) {
    beastDisplayHelper.clearBeastCell(cell);
  }

  boolean isIncubating(FudiCell cell) {
    return beastDisplayHelper.isIncubating(cell);
  }
}
