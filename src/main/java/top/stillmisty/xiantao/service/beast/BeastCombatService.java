package top.stillmisty.xiantao.service.beast;

import static top.stillmisty.xiantao.service.ErrorCode.*;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.enums.MutationEffectType;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.beast.vo.BeastStatusVO;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.enums.BeastQuality;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.fudi.FudiHelper;

/** 灵兽出战/召回、修为 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeastCombatService {

  private final BeastRepository beastRepository;
  private final FudiHelper fudiHelper;
  private final BeastDisplayHelper beastDisplayHelper;
  private final MutationEffectResolver effectResolver;

  @Authenticated
  public ServiceResult<List<BeastStatusVO>> getDeployedBeasts(
      PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getDeployedBeasts(userId));
  }

  @Authenticated
  public ServiceResult<List<BeastStatusVO>> getBeastList(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getBeastList(userId));
  }

  public static int calculateBeastAttack(int level, BeastQuality quality) {
    double q = getCombatStatMultiplier(quality);
    return (int) Math.round((10 + (level - 1) * 3 * q) * q);
  }

  public static int calculateBeastDefense(int level, BeastQuality quality) {
    double q = getCombatStatMultiplier(quality);
    return (int) Math.round((8 + (level - 1) * 2 * q) * q);
  }

  private static double getCombatStatMultiplier(BeastQuality quality) {
    return switch (quality) {
      case MORTAL -> 0.8;
      case SPIRIT -> 1.0;
      case IMMORTAL -> 1.3;
      case SAINT -> 1.6;
      case DIVINE -> 2.0;
    };
  }

  List<BeastStatusVO> getDeployedBeasts(Long userId) {
    Fudi fudi =
        fudiHelper
            .findAndTouchFudi(userId)
            .orElseThrow(() -> new BusinessException(FUDI_NOT_FOUND));
    List<Beast> allBeasts = beastRepository.findByFudiId(fudi.getId());
    return allBeasts.stream()
        .filter(b -> Boolean.TRUE.equals(b.getIsDeployed()))
        .map(beastDisplayHelper::convertToBeastStatusVO)
        .toList();
  }

  List<BeastStatusVO> getBeastList(Long userId) {
    return beastRepository.findByUserId(userId).stream()
        .map(beastDisplayHelper::convertToBeastStatusVO)
        .toList();
  }

  @Transactional
  public String toggleDeploy(Long userId, String position) {
    BeastDisplayHelper.PenCellBeast pcb =
        beastDisplayHelper.getBeastFromPenCell(userId, position, false);
    Beast beast = pcb.beast();

    if (Boolean.TRUE.equals(beast.getIsDeployed())) {
      beast.setIsDeployed(false);
      beastRepository.save(beast);
      return "灵兽 [%s] 已召回".formatted(beast.getBeastName());
    }

    if (beast.getHpCurrent() <= 0) {
      throw new BusinessException(BEAST_DEAD);
    }

    if (beast.getRecoveryUntil() != null && beast.getRecoveryUntil().isAfter(LocalDateTime.now())) {
      throw new BusinessException(BEAST_IN_RECOVERY);
    }

    List<Beast> allBeasts = beastRepository.findByFudiId(pcb.fudi().getId());
    long deployedCount =
        allBeasts.stream().filter(b -> Boolean.TRUE.equals(b.getIsDeployed())).count();
    if (deployedCount >= 2) {
      throw new BusinessException(BEAST_DEPLOY_FULL);
    }

    beast.setIsDeployed(true);
    beastRepository.save(beast);
    return "灵兽 [%s] 已出战".formatted(beast.getBeastName());
  }

  @Transactional
  public void addBeastExp(Long beastId, long expToAdd) {
    beastRepository
        .findById(beastId)
        .ifPresentOrElse(
            beast -> {
              double expBonus =
                  effectResolver.sumEffectValue(beast, MutationEffectType.EXP_PERCENT);
              long actualExp = (long) (expToAdd * (1 + expBonus / 100));
              long consumed = beast.addExp(actualExp);
              beastRepository.save(beast);
              log.debug("灵兽 {} 获得 {} 修为", beastId, consumed);
            },
            () -> log.warn("灵兽 {} 不存在，修为 {} 无法添加", beastId, expToAdd));
  }

  @Transactional
  public void addExpToDeployedBeasts(Long userId, long expToAdd) {
    List<Beast> deployedBeasts = beastRepository.findByUserIdAndIsDeployed(userId, true);
    for (Beast beast : deployedBeasts) {
      double expBonus = effectResolver.sumEffectValue(beast, MutationEffectType.EXP_PERCENT);
      long actualExp = (long) (expToAdd * (1 + expBonus / 100));
      long consumed = beast.addExp(actualExp);
      beastRepository.save(beast);
      log.debug("灵兽 {} 获得 {} 修为", beast.getId(), consumed);
    }
  }
}
