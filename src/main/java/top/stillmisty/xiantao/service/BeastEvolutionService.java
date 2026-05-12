package top.stillmisty.xiantao.service;

import static top.stillmisty.xiantao.service.ErrorCode.*;

import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.enums.BeastQuality;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;

/** 灵兽进化 — 等阶提升、品质突破 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeastEvolutionService {

  private final FudiHelper fudiHelper;
  private final BeastRepository beastRepository;
  private final SpiritRepository spiritRepository;
  private final BeastSkillService beastSkillService;
  private final BeastDisplayHelper beastDisplayHelper;
  private final BeastMutationService beastMutationService;

  @Transactional
  public PenCellVO evolveBeastTier(Fudi fudi, FudiCell cell, Long userId, Integer cellId) {
    Beast beast = beastDisplayHelper.findBeastByCell(cell);
    if (beast == null) {
      throw new BusinessException(BEAST_NOT_FOUND);
    }

    if (beast.getTier() >= 5) {
      throw new BusinessException(BEAST_MAX_TIER);
    }

    if (beast.needsMoreLevels()) {
      throw new BusinessException(BEAST_NEED_MAX_LEVEL);
    }

    int currentTier = beast.getTier();
    int cost = (currentTier + 1) * 200;
    fudiHelper.deductSpiritStones(userId, cost);

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
    if (ThreadLocalRandom.current().nextInt(100) < 10
        && beast.getQuality() != BeastQuality.DIVINE) {
      beast.qualityBreak();
      qualityUpgraded = true;
    }

    beastMutationService.attemptMutation(beast, 15);

    if (beast.getTier() == 2) {
      beastSkillService.unlockInnateSkills(beast, "tier_2");
    } else if (beast.getTier() == 3) {
      beastSkillService.unlockInnateSkills(beast, "tier_3");
    }

    beastRepository.save(beast);

    log.info(
        "用户 {} 进化地块 {} 的灵兽 T{}->T{}{}",
        userId,
        cellId,
        currentTier,
        beast.getTier(),
        qualityUpgraded ? " (品质连带提升!)" : "");

    return beastDisplayHelper.buildPenCellVO(cell);
  }

  @Transactional
  public PenCellVO breakthroughBeastQuality(Fudi fudi, FudiCell cell, Long userId, Integer cellId) {
    Beast beast = beastDisplayHelper.findBeastByCell(cell);
    if (beast == null) {
      throw new BusinessException(BEAST_NOT_FOUND);
    }

    if (beast.getQuality() == BeastQuality.DIVINE) {
      throw new BusinessException(BEAST_MAX_QUALITY);
    }

    if (beast.needsMoreLevels()) {
      throw new BusinessException(BEAST_NEED_MAX_LEVEL_BREAK);
    }

    BeastQuality currentQuality = beast.getQuality();
    BeastQuality nextQuality = currentQuality.next();

    int cost = nextQuality.getOrder() * 300;
    fudiHelper.deductSpiritStones(userId, cost);

    var spirit = spiritRepository.findByFudiId(fudi.getId()).orElse(null);
    int affectionBonus =
        spirit != null && spirit.getAffection() != null
            ? Math.min(20, spirit.getAffection() / 5)
            : 0;
    int successRate = 60 + affectionBonus;

    if (beast.getMutationTraits() != null && beast.getMutationTraits().contains("SPIRITUAL")) {
      successRate += 10;
    }

    boolean success = ThreadLocalRandom.current().nextInt(100) < successRate;

    if (!success) {
      throw new BusinessException(BEAST_QUALITY_FAILED);
    }

    beast.qualityBreak();
    beast.recalculateAttributes();
    beast.setHpCurrent(beast.getMaxHp());

    beastMutationService.attemptMutation(beast, 10);

    beastSkillService.unlockInnateSkills(beast, "quality_break");
    beastRepository.save(beast);

    log.info(
        "用户 {} 品质突破地块 {} 的灵兽 {} -> {}",
        userId,
        cellId,
        currentQuality.getChineseName(),
        nextQuality.getChineseName());

    return beastDisplayHelper.buildPenCellVO(cell);
  }
}
