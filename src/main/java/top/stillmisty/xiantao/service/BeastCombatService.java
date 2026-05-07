package top.stillmisty.xiantao.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.beast.vo.*;
import top.stillmisty.xiantao.domain.beast.vo.ActionResultVO;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.enums.BeastQuality;

/** 灵兽出战/召回、恢复、经验 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeastCombatService {

  private final BeastRepository beastRepository;
  private final FudiHelper fudiHelper;
  private final BeastDisplayHelper beastDisplayHelper;

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

  @Transactional
  public ActionResultVO deployBeast(Long userId, String position) {
    BeastDisplayHelper.PenCellBeast pcb =
        beastDisplayHelper.getBeastFromPenCell(userId, position, true);
    Beast beast = pcb.beast();

    if (Boolean.TRUE.equals(beast.getIsDeployed())) {
      return new ActionResultVO(true, "灵兽已处于出战状态");
    }

    if (beast.getHpCurrent() <= 0) {
      throw new IllegalStateException("灵兽HP为0，请先恢复");
    }

    List<Beast> allBeasts = beastRepository.findByFudiId(pcb.fudi().getId());
    long deployedCount =
        allBeasts.stream()
            .filter(b -> Boolean.TRUE.equals(b.getIsDeployed()) && b.canFight())
            .count();
    if (deployedCount >= 2) {
      throw new IllegalStateException("出战灵兽已达上限 (2只)，请先召回其他灵兽");
    }

    beast.setIsDeployed(true);
    beastRepository.save(beast);

    String beastName = beast.getBeastName();
    log.info("用户 {} 将灵兽 {} 设为出战", userId, beastName);
    return new ActionResultVO(true, "灵兽 [%s] 已出战".formatted(beastName));
  }

  @Transactional
  public BeastUndeployResult undeployBeast(Long userId, String position) {
    if ("all".equalsIgnoreCase(position)) {
      return undeployAllBeasts(userId);
    }

    BeastDisplayHelper.PenCellBeast pcb =
        beastDisplayHelper.getBeastFromPenCell(userId, position, false);
    Beast beast = pcb.beast();

    if (!Boolean.TRUE.equals(beast.getIsDeployed())) {
      return new ActionResultVO(true, "灵兽未在出战状态");
    }

    beast.setIsDeployed(false);
    beastRepository.save(beast);

    String beastName = beast.getBeastName();
    log.info("用户 {} 将灵兽 {} 召回", userId, beastName);
    return new ActionResultVO(true, "灵兽 [%s] 已召回".formatted(beastName));
  }

  BatchCountVO undeployAllBeasts(Long userId) {
    Fudi fudi =
        fudiHelper.findAndTouchFudi(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));

    List<Beast> beasts = beastRepository.findByFudiId(fudi.getId());
    int count = 0;
    for (Beast b : beasts) {
      if (Boolean.TRUE.equals(b.getIsDeployed())) {
        b.setIsDeployed(false);
        beastRepository.save(b);
        count++;
      }
    }

    return new BatchCountVO(count);
  }

  @Transactional
  public BeastRecoverResult recoverBeast(Long userId, String position) {
    if ("all".equalsIgnoreCase(position)) {
      return recoverAllBeasts(userId);
    }

    BeastDisplayHelper.PenCellBeast pcb =
        beastDisplayHelper.getBeastFromPenCell(userId, position, true);
    Beast beast = pcb.beast();

    int hpCurrent = beast.getHpCurrent();
    int hpMax = beast.getMaxHp();
    if (hpCurrent >= hpMax) {
      return new ActionResultVO(true, "灵兽HP已满");
    }

    int missingHp = hpMax - hpCurrent;
    int stoneCost = (int) Math.ceil(missingHp * 0.1);
    fudiHelper.deductSpiritStones(userId, stoneCost);

    beast.setHpCurrent(hpMax);
    beast.setRecoveryUntil(null);
    beastRepository.save(beast);

    String beastName = beast.getBeastName();
    log.info("用户 {} 恢复灵兽 {} HP (消耗{}灵石)", userId, beastName, stoneCost);
    return new RecoverResultVO(
        true, "灵兽 [%s] HP已恢复（消耗%d灵石）".formatted(beastName, stoneCost), stoneCost);
  }

  BeastRecoverResult recoverAllBeasts(Long userId) {
    Fudi fudi =
        fudiHelper.findAndTouchFudi(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));

    int totalCost = 0;
    int recoverCount = 0;

    List<Beast> beasts = beastRepository.findByFudiId(fudi.getId());
    for (Beast beast : beasts) {
      int hpCurrent = beast.getHpCurrent();
      int hpMax = beast.getMaxHp();
      if (hpCurrent >= hpMax) continue;

      int missingHp = hpMax - hpCurrent;
      totalCost += (int) Math.ceil(missingHp * 0.1);
      beast.setHpCurrent(hpMax);
      beast.setRecoveryUntil(null);
      beastRepository.save(beast);
      recoverCount++;
    }

    if (recoverCount == 0) {
      return new ActionResultVO(true, "没有需要恢复的灵兽");
    }

    fudiHelper.deductSpiritStones(userId, totalCost);

    return new BatchRecoverVO(recoverCount, totalCost);
  }

  List<BeastStatusVO> getDeployedBeasts(Long userId) {
    Fudi fudi =
        fudiHelper.findAndTouchFudi(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));
    List<Beast> allBeasts = beastRepository.findByFudiId(fudi.getId());
    return allBeasts.stream()
        .filter(b -> Boolean.TRUE.equals(b.getIsDeployed()))
        .map(beastDisplayHelper::convertToBeastStatusVO)
        .toList();
  }

  @Transactional
  public void addBeastExp(Long beastId, long expToAdd) {
    Beast beast = beastRepository.findById(beastId).orElse(null);
    if (beast == null) {
      return;
    }
    beast.addExp(expToAdd);
    beastRepository.save(beast);
  }

  @Transactional
  public void addExpToDeployedBeasts(Long userId, long expToAdd) {
    List<Beast> deployedBeasts = beastRepository.findByUserIdAndIsDeployed(userId, true);
    for (Beast beast : deployedBeasts) {
      addBeastExp(beast.getId(), expToAdd);
    }
  }
}
