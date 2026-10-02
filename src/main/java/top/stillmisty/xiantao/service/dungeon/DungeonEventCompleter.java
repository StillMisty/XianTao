package top.stillmisty.xiantao.service.dungeon;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.notification.entity.GameEvent;
import top.stillmisty.xiantao.domain.notification.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.service.GameEventService;
import top.stillmisty.xiantao.service.activity.ActivitySubEventPipeline;
import top.stillmisty.xiantao.service.player.PlayerLoader;

/** 秘境事件完成器 — 进入/推进/通关固定叙事、探索子事件与隐藏事件 */
@Component
@RequiredArgsConstructor
public class DungeonEventCompleter {

  /** 探索每个 POI 后触发子事件的概率（设计 §5.4：20%） */
  private static final double EXPLORE_SUB_EVENT_CHANCE = 0.20;

  private final GameEventService gameEventService;
  private final ActivitySubEventPipeline subEventPipeline;
  private final PlayerLoader playerLoader;
  private final UserRepository userRepository;

  /** 进入秘境固定叙事 */
  @Transactional
  public void produceEnterEvent(Long userId, String dungeonName, String areaName) {
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.DUNGEON_ENTER)
            .withNarrative(
                "你踏入了秘境【{{dungeon}}】，置身{{area}}，四周灵气扑面而来。",
                Map.of("dungeon", dungeonName, "area", areaName)));
  }

  /** 推进区域固定叙事 */
  @Transactional
  public void produceAreaAdvanceEvent(Long userId, String dungeonName, String areaName) {
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.DUNGEON_ENTER)
            .withNarrative(
                "你深入秘境【{{dungeon}}】——踏入{{area}}，眼前的景象与外围截然不同，灵气浓度明显提升。",
                Map.of("dungeon", dungeonName, "area", areaName)));
  }

  /**
   * 探索 POI 后的结算：20% 概率触发概率子事件，并检查该秘境的隐藏事件。
   *
   * <p>隐藏事件以 dungeon_template.id 为 owner_id，命中后写入 hidden_completion 保证每人一次；
   * 子事件/隐藏事件可能改变气血与修为，结算后落库。
   */
  @Transactional
  public void onPoiExplored(Long userId, Long dungeonId) {
    Player user = playerLoader.load(userId);
    subEventPipeline.rollSubEvent(
        ActivityType.DUNGEON.getCode(),
        dungeonId,
        EXPLORE_SUB_EVENT_CHANCE,
        userId,
        user,
        GameEventCategory.DUNGEON_EXPLORE,
        EventContext::withFortune);
    subEventPipeline.checkHiddenEvents(
        ActivityType.DUNGEON.getCode(),
        dungeonId,
        userId,
        user,
        GameEventCategory.DUNGEON_HIDDEN,
        EventContext::withFortune);
    userRepository.save(user);
  }

  /** 发现隐藏地点固定叙事 */
  @Transactional
  public void produceHiddenPoiEvent(Long userId, String poiName, @Nullable String poiDescription) {
    StringBuilder narrative = new StringBuilder("你察觉到了一处被禁制遮蔽的隐秘之地——「{{poi}}」。");
    if (poiDescription != null && !poiDescription.isBlank()) {
      narrative.append("\n").append(poiDescription);
    }
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.DUNGEON_HIDDEN)
            .withNarrative(narrative.toString(), Map.of("poi", poiName)));
  }

  /** 通关固定叙事 */
  @Transactional
  public void produceCompleteEvent(Long userId, String dungeonName) {
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.DUNGEON_COMPLETE)
            .withNarrative(
                "秘境探索圆满完成！你在【{{dungeon}}】中历练归来，修为大有精进。", Map.of("dungeon", dungeonName)));
  }
}
