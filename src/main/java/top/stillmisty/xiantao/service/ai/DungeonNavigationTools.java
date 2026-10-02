package top.stillmisty.xiantao.service.ai;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonSpiritState;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate.AreaConfig;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate.Poi;
import top.stillmisty.xiantao.infrastructure.repository.DungeonInstanceRepository;
import top.stillmisty.xiantao.infrastructure.repository.DungeonSpiritStateRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.dungeon.DungeonProgressHelper;
import top.stillmisty.xiantao.service.player.UserStateService;

/** 秘境导航工具 — 推进区域、撤退、查看区域/玩家状态 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("NullAway")
public class DungeonNavigationTools {

  private final ToolExecutor toolExecutor;
  private final DungeonInstanceRepository instanceRepository;
  private final DungeonSpiritStateRepository spiritStateRepository;
  private final DungeonStateBuilder stateBuilder;
  private final SpiritStoneService spiritStoneService;
  private final DungeonProgressHelper progressHelper;
  private final UserStateService userStateService;

  /** 在工具事务内重读最新实例，避免陈旧快照覆盖并发写入 */
  private DungeonInstance reloadInstance(DungeonChatContext ctx) {
    return instanceRepository
        .findById(ctx.instance().getId())
        .filter(DungeonInstance::isActive)
        .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_NO_ACTIVE_INSTANCE));
  }

  @Tool(description = "推进到下一区域。仅在当前区域所有主线 POI 已探索、通道已解锁后调用")
  @Transactional
  public AdvanceAreaResponse advanceToNextArea() {
    return toolExecutor.execute(
        "advanceToNextArea",
        () -> {
          DungeonChatContext ctx = requireContext();
          // 工具事务内重读最新实例，防止同一秘境并发对话时用陈旧快照互相覆盖
          DungeonInstance instance = reloadInstance(ctx);
          DungeonTemplate dungeon = ctx.dungeon();

          if (!instance.getPassageUnlocked()) {
            return new AdvanceAreaResponse(false, null, null, "通道尚未解锁，请先探索完当前区域的所有主线地点");
          }

          AreaConfig nextArea = stateBuilder.findNextAccessibleArea(dungeon, instance);
          if (nextArea == null) {
            progressHelper.completeDungeon(ctx.user().getId(), instance);
            return new AdvanceAreaResponse(true, null, null, "你已经通关了秘境！");
          }

          instance.advanceArea(nextArea.key());
          instanceRepository.save(instance);

          List<String> newPoiNames = nextArea.mainPois().stream().map(Poi::name).toList();

          return new AdvanceAreaResponse(
              true,
              nextArea.name(),
              nextArea.description(),
              "进入了【"
                  + nextArea.name()
                  + "】。"
                  + nextArea.description()
                  + "\n可探索地点："
                  + String.join(", ", newPoiNames));
        });
  }

  @Tool(description = "撤退离开秘境。保留已获得的奖励")
  @Transactional
  public RetreatResponse retreatFromDungeon() {
    return toolExecutor.execute(
        "retreatFromDungeon",
        () -> {
          DungeonChatContext ctx = requireContext();
          DungeonInstance instance = reloadInstance(ctx);

          instance.markAbandoned();
          instanceRepository.save(instance);

          ctx.user().clearActivity();
          userStateService.saveActivity(ctx.user());

          return new RetreatResponse(true, "你已经离开了秘境，已获奖励保留。");
        });
  }

  @Tool(description = "查看探索者的自身属性、好感度等信息")
  public PlayerStatusResponse checkPlayerStatus() {
    return toolExecutor.execute(
        "checkPlayerStatus",
        () -> {
          DungeonChatContext ctx = requireContext();
          DungeonSpiritState spiritState = ctx.spiritState();
          var user = ctx.user();

          return new PlayerStatusResponse(
              user.getNickname(),
              user.getLevel() != null ? user.getLevel() : 0,
              user.getHpCurrent() != null ? user.getHpCurrent() : 0,
              spiritState != null ? spiritState.getFavor() : 0,
              spiritState != null ? spiritState.favorAttitude() : "未知",
              spiritStoneService.getBalance(user.getId()));
        });
  }

  @Tool(description = "查看当前区域已探索和剩余的 POI")
  public CurrentAreaResponse checkCurrentArea() {
    return toolExecutor.execute(
        "checkCurrentArea",
        () -> {
          DungeonChatContext ctx = requireContext();
          // 工具事务内重读最新实例，防止同一秘境并发对话时用陈旧快照互相覆盖
          DungeonInstance instance = reloadInstance(ctx);
          DungeonTemplate dungeon = ctx.dungeon();

          AreaConfig area = stateBuilder.findArea(dungeon, instance.getCurrentAreaKey());
          if (area == null) {
            return new CurrentAreaResponse(
                instance.getCurrentAreaKey(), List.of(), List.of(), false);
          }

          Set<String> areaPoiNames =
              area.allPois().stream().map(Poi::name).collect(Collectors.toSet());
          List<String> explored =
              instance.getExploredPois() != null
                  ? instance.getExploredPois().stream()
                      .map(DungeonInstance.ExploredPoiRecord::poiName)
                      // 探索记录跨区域保留（隐藏区域解锁需要），此处按当前区域过滤展示
                      .filter(areaPoiNames::contains)
                      .toList()
                  : List.of();

          List<String> remaining =
              area.mainPois().stream()
                  .filter(p -> !p.isPassage())
                  .map(Poi::name)
                  .filter(n -> !explored.contains(n))
                  .toList();

          return new CurrentAreaResponse(
              area.name(), explored, remaining, instance.getPassageUnlocked());
        });
  }

  private static DungeonChatContext requireContext() {
    DungeonChatContext ctx = DungeonChatContext.current();
    if (ctx == null) {
      throw new BusinessException(ErrorCode.DUNGEON_NO_ACTIVE_INSTANCE);
    }
    return ctx;
  }

  public record AdvanceAreaResponse(
      boolean success, String newAreaName, String newAreaDescription, String message) {}

  public record RetreatResponse(boolean success, String message) {}

  public record PlayerStatusResponse(
      String nickname,
      int level,
      int hpCurrent,
      int favor,
      String favorAttitude,
      long spiritStoneBalance) {}

  public record CurrentAreaResponse(
      String areaName,
      List<String> exploredPois,
      List<String> remainingPois,
      boolean passageUnlocked) {}
}
