package top.stillmisty.xiantao.service.dungeon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonFirstClear;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonProgress;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonArea;
import top.stillmisty.xiantao.domain.dungeon.repository.DungeonFirstClearRepository;
import top.stillmisty.xiantao.domain.dungeon.repository.DungeonProgressRepository;
import top.stillmisty.xiantao.domain.dungeon.repository.DungeonTemplateRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.player.UserStateService;

@Component
@RequiredArgsConstructor
public class DungeonProgressHelper {

  private final DungeonTemplateRepository dungeonTemplateRepository;
  private final DungeonProgressRepository progressRepository;
  private final DungeonFirstClearRepository firstClearRepository;
  private final UserStateService userStateService;
  private final SpiritStoneService spiritStoneService;

  @Transactional
  public String completeDungeon(Long userId, DungeonInstance instance) {
    DungeonTemplate dungeon =
        dungeonTemplateRepository
            .findById(instance.getDungeonId())
            .orElseThrow(
                () ->
                    new BusinessException(
                        ErrorCode.DUNGEON_NOT_FOUND, String.valueOf(instance.getDungeonId())));
    instance.markCompleted();

    User user = userStateService.loadUser(userId);
    user.clearActivity();
    userStateService.saveActivity(user);

    DungeonProgress progress =
        progressRepository
            .findByUserIdAndDungeonId(userId, dungeon.getId())
            .orElseGet(
                () -> {
                  DungeonProgress p = new DungeonProgress();
                  p.setUserId(userId);
                  p.setDungeonId(dungeon.getId());
                  p.setRewardCount(0);
                  p.setDailyLimit(DungeonProgress.calculateDailyLimit(user.getLevel()));
                  p.setFirstClear(false);
                  p.setLastRewardDate(java.time.LocalDate.now());
                  return p;
                });

    boolean isFirstClear = progress.getFirstClear() == null || !progress.getFirstClear();
    if (isFirstClear) {
      DungeonFirstClear globalFirst =
          firstClearRepository.findByDungeonId(dungeon.getId()).orElse(null);
      if (globalFirst == null) {
        globalFirst = new DungeonFirstClear();
        globalFirst.setDungeonId(dungeon.getId());
        globalFirst.setTeamMembers(List.of(userId));
        globalFirst.setDurationMinutes(
            (int)
                java.time.Duration.between(instance.getCreatedAt(), LocalDateTime.now())
                    .toMinutes());
        firstClearRepository.save(globalFirst);
      }
      progress.setFirstClear(true);
    }

    if (progress.getBestArea() == null
        || progress.getBestArea().ordinal() < DungeonArea.CORE.ordinal()) {
      progress.setBestArea(DungeonArea.CORE);
    }

    long spiritStonesReward = ThreadLocalRandom.current().nextInt(500, 2001);
    spiritStoneService.deposit(userId, spiritStonesReward);

    boolean rewardGiven = false;
    if (progress.canGetReward()) {
      progress.recordReward();
      rewardGiven = true;
    }
    progressRepository.save(progress);

    StringBuilder sb = new StringBuilder();
    sb.append("恭喜！你成功通关了【").append(dungeon.getName()).append("】！\n");
    sb.append("获得灵石 ×").append(spiritStonesReward).append("\n");
    if (isFirstClear) {
      sb.append("★ 首次通关记录！\n");
    }
    if (!rewardGiven) {
      sb.append("今日通关奖励次数已达上限。\n");
    }
    return sb.toString();
  }
}
