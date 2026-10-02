package top.stillmisty.xiantao.service.dungeon;

import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonProgress;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.DungeonProgressRepository;
import top.stillmisty.xiantao.infrastructure.repository.DungeonTemplateRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.player.UserStateService;

@Component
@RequiredArgsConstructor
public class DungeonProgressHelper {

  private final DungeonTemplateRepository dungeonTemplateRepository;
  private final DungeonProgressRepository progressRepository;
  private final UserStateService userStateService;
  private final SpiritStoneService spiritStoneService;
  private final DungeonInstanceManager instanceManager;
  private final DungeonEventCompleter dungeonEventCompleter;

  @Transactional
  public String completeDungeon(Long userId, DungeonInstance instance) {
    // 关闭秘境实例，否则部分唯一索引会阻止玩家再次进入
    instanceManager.markCompleted(instance);

    DungeonTemplate dungeon =
        dungeonTemplateRepository
            .findById(instance.getDungeonId())
            .orElseThrow(
                () ->
                    new BusinessException(
                        ErrorCode.DUNGEON_NOT_FOUND, String.valueOf(instance.getDungeonId())));

    Player user = userStateService.loadUser(userId);
    user.clearActivity();
    userStateService.saveActivity(user);

    DungeonProgress progress =
        progressRepository
            .findByUserIdAndDungeonIdForUpdate(userId, dungeon.getId())
            .orElseGet(
                () -> {
                  DungeonProgress p = new DungeonProgress();
                  p.setUserId(userId);
                  p.setDungeonId(dungeon.getId());
                  p.setRewardCount(0);
                  p.setDailyLimit(DungeonProgress.calculateDailyLimit(user.getLevel()));
                  p.setFirstClear(false);
                  p.setLastRewardDate(TimeUtil.today());
                  p.setInteractionCount(0);
                  return p;
                });

    boolean isFirstClear = progress.getFirstClear() == null || !progress.getFirstClear();
    if (isFirstClear) {
      progress.setFirstClear(true);
    }

    progress.setBestArea(instance.getCurrentAreaKey());

    // 灵石奖励必须在每日限额判定之内发放，否则上限形同虚设
    long spiritStonesReward = 0;
    boolean rewardGiven = false;
    if (progress.canGetReward()) {
      spiritStonesReward = ThreadLocalRandom.current().nextInt(500, 2001);
      spiritStoneService.deposit(userId, spiritStonesReward);
      progress.recordReward();
      rewardGiven = true;
    }
    progressRepository.save(progress);

    dungeonEventCompleter.produceCompleteEvent(userId, dungeon.getName());

    StringBuilder sb = new StringBuilder();
    sb.append("恭喜！你成功通关了【").append(dungeon.getName()).append("】！\n");
    if (rewardGiven) {
      sb.append("获得灵石 ×").append(spiritStonesReward).append("\n");
    }
    if (isFirstClear) {
      sb.append("★ 首次通关记录！\n");
    }
    if (!rewardGiven) {
      sb.append("今日通关奖励次数已达上限，未获得灵石。\n");
    }
    return sb.toString();
  }
}
