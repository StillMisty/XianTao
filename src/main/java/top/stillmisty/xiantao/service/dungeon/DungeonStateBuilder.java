package top.stillmisty.xiantao.service.dungeon;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonSpiritState;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate.AreaConfig;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate.Poi;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;

@Component
public class DungeonStateBuilder {

  public String buildSystemPrompt(
      DungeonTemplate dungeon, DungeonInstance instance, @Nullable DungeonSpiritState spiritState) {

    AreaConfig currentArea = findArea(dungeon, instance.getCurrentAreaKey());
    if (currentArea == null) {
      return "当前区域不存在。";
    }

    StringBuilder sb = new StringBuilder();

    sb.append("你正在扮演秘境【").append(dungeon.getName()).append("】");

    if (dungeon.hasSpirit()) {
      var sc = dungeon.getSpiritConfig();
      if (sc != null) {
        sb.append("的秘境之灵「").append(sc.spiritName()).append("」。\n\n");
        sb.append("你的形象：").append(sc.spiritAppearance()).append("\n");
        sb.append("你的性格：").append(sc.personality()).append("\n");
        sb.append("语气风格：").append(sc.toneStyle()).append("\n\n");
      }
    } else {
      sb.append("的叙事者。以旁白口吻描述探索者的所见所闻。\n\n");
    }

    sb.append("当前区域：【").append(currentArea.name()).append("】\n");
    sb.append("区域描述：").append(currentArea.description()).append("\n\n");

    sb.append("当前区域可探索的地点：\n");
    for (Poi poi : currentArea.mainPois()) {
      boolean explored =
          instance.getExploredPois() != null
              && instance.getExploredPois().stream().anyMatch(e -> e.poiName().equals(poi.name()));
      String status = explored ? "（已探索）" : "";
      sb.append("- ").append(poi.name()).append(" [").append(poi.type()).append("]").append(status);
      if (!explored && poi.hasClues()) {
        sb.append("  线索：").append(String.join("、", poi.clues()));
      }
      sb.append("\n");
    }

    if (currentArea.hiddenPois() != null && !currentArea.hiddenPois().isEmpty()) {
      sb.append("\n【隐藏线索（仅在环境描述中暗示，不要直接明说）】\n");
      for (Poi hidden : currentArea.hiddenPois()) {
        if (spiritState != null
            && spiritState.getHiddenFinds() != null
            && spiritState.getHiddenFinds().contains(hidden.name())) {
          continue;
        }
        if (hidden.hasClues()) {
          sb.append("- ").append(String.join("、", hidden.clues())).append("\n");
        }
      }
    }

    if (instance.getPassageUnlocked()) {
      sb.append("\n通往下一区域的通道已开启，玩家可以随时推进。\n");
    }

    if (dungeon.hasSpirit() && spiritState != null) {
      sb.append("\n玩家对你的好感度：")
          .append(spiritState.getFavor())
          .append("（态度：")
          .append(spiritState.favorAttitude())
          .append("）\n");
    }

    sb.append("\n【规则】\n");
    sb.append("1. 你只描述环境、场景和角色，不替玩家做决定。\n");
    sb.append("2. 当玩家描述探索某个地点的意图时，调用 resolveEncounter 工具。\n");
    sb.append("3. 当玩家表示继续前进时，先确认通道已解锁，再调用 advanceToNextArea 工具。\n");
    sb.append("4. 当玩家表示退出时，调用 retreatFromDungeon 工具。\n");
    sb.append("5. 在环境描述中自然地暗示隐藏线索（如配置了线索），但不要直接点名隐藏地点的名字。\n");
    sb.append("6. 战斗结果由工具返回具体数据，你只需要做叙事美化（3~5句），不编造数值。\n");
    sb.append("7. 如果配置了隐藏 POI 没有线索，则完全不要主动提及，除非玩家明确搜索。\n");

    if (dungeon.hasAffectionSystem()) {
      sb.append("8. 根据玩家言行调用 adjustFavor 调整好感度（谨慎、有节制的使用）。\n");
      sb.append("9. 好感度高时可以更主动地暗示隐藏内容。\n");
    }

    return sb.toString();
  }

  public String buildStatusOverview(
      DungeonTemplate dungeon, DungeonInstance instance, @Nullable DungeonSpiritState spiritState) {

    AreaConfig currentArea = findArea(dungeon, instance.getCurrentAreaKey());
    String areaName = currentArea != null ? currentArea.name() : instance.getCurrentAreaKey();
    int totalMainPois = currentArea != null ? currentArea.mainPois().size() : 0;
    int exploredCount = instance.exploredCount();
    int favor = spiritState != null && spiritState.getFavor() != null ? spiritState.getFavor() : 0;
    long elapsedMinutes =
        java.time.Duration.between(instance.getCreatedAt(), TimeUtil.now()).toMinutes();

    StringBuilder sb = new StringBuilder();
    sb.append(areaName)
        .append(" | 探索 ")
        .append(exploredCount)
        .append("/")
        .append(totalMainPois)
        .append(" | 好感 ")
        .append(favor)
        .append(" | 用时 ")
        .append(elapsedMinutes)
        .append("min");

    if (spiritState != null
        && spiritState.getHiddenFinds() != null
        && !spiritState.getHiddenFinds().isEmpty()) {
      sb.append("\n隐藏发现: ").append(String.join(", ", spiritState.getHiddenFinds()));
    }

    return sb.toString();
  }

  @Nullable
  public AreaConfig findArea(DungeonTemplate dungeon, String areaKey) {
    if (dungeon.getAreaConfigs() == null) return null;
    return dungeon.getAreaConfigs().stream()
        .filter(a -> a.key().equals(areaKey))
        .findFirst()
        .orElse(null);
  }

  @Nullable
  public AreaConfig findNextArea(DungeonTemplate dungeon, String currentAreaKey) {
    List<AreaConfig> areas = dungeon.getAreaConfigs();
    if (areas == null) return null;
    for (int i = 0; i < areas.size() - 1; i++) {
      if (areas.get(i).key().equals(currentAreaKey)) {
        return areas.get(i + 1);
      }
    }
    return null;
  }

  @Nullable
  public Poi findPoi(AreaConfig area, String poiName) {
    for (Poi poi : area.allPois()) {
      if (poi.name().equals(poiName)) return poi;
    }
    return null;
  }
}
