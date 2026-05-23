package top.stillmisty.xiantao.service.combat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.skill.entity.PlayerSkill;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.repository.PlayerSkillRepository;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.GameEventService;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnlightenmentProcessor {

  private final SkillRepository skillRepository;
  private final PlayerSkillRepository playerSkillRepository;
  private final GameEventService gameEventService;

  public boolean process(Long userId, User user) {
    int wis = user.getEffectiveStatWis();
    double chance = 0.02 + wis * 0.0005;
    if (ThreadLocalRandom.current().nextDouble() >= chance) return false;

    long expToNextLevel = user.calculateExpToNextLevel();
    double roll = ThreadLocalRandom.current().nextDouble();
    Map<String, Object> args = new HashMap<>();

    if (roll < 0.50) {
      long expBonus =
          (long) (expToNextLevel * (0.03 + ThreadLocalRandom.current().nextDouble() * 0.05));
      user.addExp(expBonus);
      args.put("exp", expBonus);
      gameEventService.save(
          GameEvent.create(userId, GameEventCategory.TRAINING_EVENT)
              .withNarrative("历练中灵光一闪，顿悟天道至理，修为增进 +{{exp}}。", args));
    } else if (roll < 0.80) {
      Skill learned = tryLearnRandomSkill(userId, user);
      if (learned != null) {
        args.put("skillName", learned.getName());
        gameEventService.save(
            GameEvent.create(userId, GameEventCategory.TRAINING_EVENT)
                .withNarrative("心有所感，悟得绝学「{{skillName}}」！", args));
      } else {
        long expBonus =
            (long) (expToNextLevel * (0.01 + ThreadLocalRandom.current().nextDouble() * 0.02));
        user.addExp(expBonus);
        args.put("exp", expBonus);
        gameEventService.save(
            GameEvent.create(userId, GameEventCategory.TRAINING_EVENT)
                .withNarrative("似有所悟，但未得要领，仅获 +{{exp}} 修为。", args));
      }
    } else if (roll < 0.95) {
      long expBonus =
          (long) (expToNextLevel * (0.08 + ThreadLocalRandom.current().nextDouble() * 0.12));
      user.addExp(expBonus);
      Skill learned = tryLearnRandomSkill(userId, user);
      args.put("exp", expBonus);
      if (learned != null) {
        args.put("skillName", learned.getName());
        gameEventService.save(
            GameEvent.create(userId, GameEventCategory.TRAINING_EVENT)
                .withNarrative("天机乍现，大彻大悟！修为增进 +{{exp}}，并悟得「{{skillName}}」！", args));
      } else {
        gameEventService.save(
            GameEvent.create(userId, GameEventCategory.TRAINING_EVENT)
                .withNarrative("天机乍现，大彻大悟！修为增进 +{{exp}}。", args));
      }
    } else {
      long maxStorage = user.calculateMaxExpStorage();
      long current = user.getExp();
      long currentInLevel =
          current
              - (user.getLevel() > 1 ? 100L * (user.getLevel() - 1) * (user.getLevel() - 1) : 0);
      long expNeededForCap = maxStorage - currentInLevel;
      long expGiven = Math.max(expToNextLevel, expNeededForCap);
      user.addExp(expGiven);
      Skill learned = tryLearnRandomSkill(userId, user);
      args.put("exp", expGiven);
      if (learned != null) {
        args.put("skillName", learned.getName());
        gameEventService.save(
            GameEvent.create(userId, GameEventCategory.TRAINING_EVENT)
                .withNarrative("天人交感，道心通明！修为暴涨 +{{exp}}，悟得「{{skillName}}」！", args));
      } else {
        gameEventService.save(
            GameEvent.create(userId, GameEventCategory.TRAINING_EVENT)
                .withNarrative("天人交感，道心通明！修为暴涨 +{{exp}}！", args));
      }
    }
    return true;
  }

  private Skill tryLearnRandomSkill(Long userId, User user) {
    Set<Long> learnedSkillIds =
        playerSkillRepository.findByUserId(userId).stream()
            .map(PlayerSkill::getSkillId)
            .collect(Collectors.toSet());

    int wis = user.getEffectiveStatWis();
    int level = user.getLevel();

    List<Skill> learnable = skillRepository.findLearnable(wis, level, learnedSkillIds);

    if (learnable.isEmpty()) return null;

    Skill chosen = learnable.get(ThreadLocalRandom.current().nextInt(learnable.size()));
    PlayerSkill playerSkill = PlayerSkill.create(userId, chosen.getId(), false);
    playerSkillRepository.save(playerSkill);
    return chosen;
  }
}
