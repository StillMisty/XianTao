package top.stillmisty.xiantao.service.ai;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonSpiritState;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate.AreaConfig;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate.Poi;
import top.stillmisty.xiantao.infrastructure.repository.DungeonInstanceRepository;
import top.stillmisty.xiantao.infrastructure.repository.DungeonSpiritStateRepository;

/** 秘境好感系统工具 — 调整好感、获取隐藏线索 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("NullAway")
public class DungeonFavorTools {

  private final ToolExecutor toolExecutor;
  private final DungeonInstanceRepository instanceRepository;
  private final DungeonSpiritStateRepository spiritStateRepository;
  private final DungeonStateBuilder stateBuilder;
  private final DungeonSpiritStateHelper spiritStateHelper;

  @Tool(description = "根据探索者的言行调整好感度。change 数值可正可负，reason 是变更原因简述。谨慎使用")
  @Transactional
  public AdjustFavorResponse adjustFavor(
      @ToolParam(description = "好感度变更量（正数增加，负数减少）") int change,
      @ToolParam(description = "变更原因简述") String reason) {

    return toolExecutor.execute(
        "adjustFavor",
        () -> {
          DungeonChatContext ctx = requireContext();
          DungeonInstance instance = ctx.instance();
          DungeonTemplate dungeon = ctx.dungeon();

          if (!dungeon.hasAffectionSystem()) {
            return new AdjustFavorResponse(0, "无好感系统", "此秘境无好感系统");
          }

          DungeonSpiritState spiritState = findOrCreateSpiritState(instance);
          spiritState.addFavor(change, reason);
          spiritStateRepository.save(spiritState);

          return new AdjustFavorResponse(
              spiritState.getFavor(), spiritState.favorAttitude(), "好感度已调整");
        });
  }

  @SuppressWarnings("NullAway")
  @Tool(description = "消耗好感度向秘境之灵询问隐藏线索。消耗 20~30 好感度。仅好感系统开启时可用")
  @Transactional
  public GiveHintResponse giveHint() {
    return toolExecutor.execute(
        "giveHint",
        () -> {
          DungeonChatContext ctx = requireContext();
          DungeonInstance instance = ctx.instance();
          DungeonTemplate dungeon = ctx.dungeon();
          DungeonSpiritState spiritState = ctx.spiritState();

          if (!dungeon.hasAffectionSystem()) {
            return new GiveHintResponse("", false, "此秘境无秘境之灵");
          }

          if (spiritState == null
              || spiritState.getFavor() == null
              || spiritState.getFavor() < 20) {
            return new GiveHintResponse("", false, "好感度不足（需要至少20）");
          }

          AreaConfig area = stateBuilder.findArea(dungeon, instance.getCurrentAreaKey());
          if (area == null || area.hiddenPois() == null || area.hiddenPois().isEmpty()) {
            return new GiveHintResponse("", false, "当前区域无可揭示的隐藏内容");
          }

          List<Poi> undiscovered =
              area.hiddenPois().stream().filter(p -> !instance.hasExploredPoi(p.name())).toList();

          if (undiscovered.isEmpty()) {
            return new GiveHintResponse("", false, "所有隐藏地点已发现");
          }

          int cost = 20 + ThreadLocalRandom.current().nextInt(11);
          spiritState.addFavor(-cost, "消耗好感度获取线索");
          spiritStateRepository.save(spiritState);

          Poi hintPoi = undiscovered.get(ThreadLocalRandom.current().nextInt(undiscovered.size()));
          String hint =
              hintPoi.hasClues()
                  ? hintPoi.clues().get(ThreadLocalRandom.current().nextInt(hintPoi.clues().size()))
                  : "似乎有一处不同寻常之处等待探索...";

          return new GiveHintResponse(
              hint, true, "消耗了" + cost + "好感度，获得了一条线索（剩余好感度：" + spiritState.getFavor() + "）");
        });
  }

  private DungeonSpiritState findOrCreateSpiritState(DungeonInstance instance) {
    DungeonChatContext ctx = ChatContext.require(DungeonChatContext.class);
    return spiritStateHelper.findOrCreate(
        instance.getId(), instance.getDungeonId(), ctx.user().getId());
  }

  private static DungeonChatContext requireContext() {
    return ChatContext.require(DungeonChatContext.class);
  }

  public record AdjustFavorResponse(int currentFavor, String attitude, String message) {}

  public record GiveHintResponse(String hint, boolean success, String message) {}
}
