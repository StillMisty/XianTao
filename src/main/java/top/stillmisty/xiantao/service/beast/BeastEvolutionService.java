package top.stillmisty.xiantao.service.beast;

import static top.stillmisty.xiantao.service.ErrorCode.*;

import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.enums.MutationEffectType;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.enums.BeastQuality;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;
import top.stillmisty.xiantao.infrastructure.repository.BeastRepository;
import top.stillmisty.xiantao.infrastructure.repository.SpiritRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.SpiritStoneService;

/** 灵兽升阶 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeastEvolutionService {

  private final SpiritStoneService spiritStoneService;
  private final BeastRepository beastRepository;
  private final SpiritRepository spiritRepository;
  private final BeastSkillService beastSkillService;
  private final BeastDisplayHelper beastDisplayHelper;
  private final BeastMutationService beastMutationService;
  private final MutationEffectResolver effectResolver;

  @Transactional
  public PenCellVO evolveBeastTier(Fudi fudi, FudiCell cell, Long userId, Integer cellId) {
    Beast beast = beastDisplayHelper.findBeastByCell(cell);
    if (beast == null) {
      throw new BusinessException(BEAST_NOT_FOUND);
    }

    beast =
        beastRepository
            .findByIdForUpdate(beast.getId())
            .orElseThrow(() -> new BusinessException(BEAST_NOT_FOUND));

    if (beast.getTier() >= 5) {
      throw new BusinessException(BEAST_MAX_TIER);
    }

    if (beast.needsMoreLevels()) {
      throw new BusinessException(BEAST_NEED_MAX_LEVEL);
    }

    int currentTier = beast.getTier();
    int cost = (currentTier + 1) * 200;
    spiritStoneService.withdraw(userId, cost);

    var spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
    int affectionBonus =
        spirit != null && spirit.getAffection() != null
            ? Math.min(15, spirit.getAffection() / 7)
            : 0;
    int successRate = 85 + affectionBonus;
    boolean success = ThreadLocalRandom.current().nextInt(100) < successRate;

    if (!success) {
      throw new BusinessException(BEAST_EVOLVE_FAILED);
    }

    beast.evolve();
    beast.setHpCurrent(beast.getMaxHp());

    boolean qualityUpgraded = false;
    int qualityUpgradeChance = 10;
    qualityUpgradeChance +=
        (int) effectResolver.sumEffectValue(beast, MutationEffectType.QUALITY_UP_CHANCE);
    if (beast.getQuality() != BeastQuality.DIVINE
        && ThreadLocalRandom.current().nextInt(100) < qualityUpgradeChance) {
      int nextRank = beast.getQuality().getRank() + 1;
      for (BeastQuality q : BeastQuality.values()) {
        if (q.getRank() == nextRank) {
          beast.setQuality(q);
          break;
        }
      }
      beast.recalculateAttributes();
      beastSkillService.unlockInnateSkills(beast, "quality_break");
      qualityUpgraded = true;
    }

    beastMutationService.attemptMutation(beast, 15);

    int newTier = beast.getTier();
    if (newTier == 2) {
      beastSkillService.unlockInnateSkills(beast, "tier_2");
    } else if (newTier == 3) {
      beastSkillService.unlockInnateSkills(beast, "tier_3");
    } else if (newTier == 4) {
      beastSkillService.unlockInnateSkills(beast, "tier_4");
    } else if (newTier == 5) {
      beastSkillService.unlockInnateSkills(beast, "tier_5");
    }

    beastRepository.save(beast);

    log.info(
        "玩家 {} 升阶地块 {} 的灵兽 {}→{}{}",
        userId,
        cellId,
        Beast.getTierName(currentTier),
        Beast.getTierName(beast.getTier()),
        qualityUpgraded ? " (品质连带提升!)" : "");

    return beastDisplayHelper.buildPenCellVO(cell);
  }
}
