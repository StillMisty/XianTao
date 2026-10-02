package top.stillmisty.xiantao.service.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate.AreaConfig;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate.HiddenArea;

class DungeonStateBuilderTest {

  private final DungeonStateBuilder builder = new DungeonStateBuilder();

  @Test
  void hiddenAreaSkippedUntilTriggerPoiResolved() {
    DungeonTemplate dungeon = dungeonWithHiddenArea();

    assertNull(
        builder.findNextAccessibleArea(dungeon, instance("core")),
        "trigger_after_resolve 指定的 POI 未完成时应跳过隐藏区域");
    assertEquals(
        "secret",
        Objects.requireNonNull(builder.findNextAccessibleArea(dungeon, instance("core", "藏经阁")))
            .key(),
        "完成指定 POI 后解锁隐藏区域入口");
  }

  @Test
  void hiddenAreaIsNotNextBeforeItsPosition() {
    DungeonTemplate dungeon = dungeonWithHiddenArea();

    assertEquals(
        "core",
        Objects.requireNonNull(builder.findNextAccessibleArea(dungeon, instance("inner"))).key());
  }

  @Test
  void hiddenAreaWithoutTriggerConfigHasNoGate() {
    DungeonTemplate dungeon = new DungeonTemplate();
    dungeon.setAreaConfigs(
        new ArrayList<>(List.of(area("main", "MAIN", null), area("secret", "HIDDEN", null))));

    assertEquals(
        "secret",
        Objects.requireNonNull(builder.findNextAccessibleArea(dungeon, instance("main"))).key());
  }

  @Test
  void advanceAreaKeepsExploredRecordsForTriggerChecks() {
    DungeonInstance instance = instance("inner", "藏经阁");
    instance.advanceArea("core");

    assertTrue(instance.hasExploredPoi("藏经阁"), "跨区域保留探索记录");
    assertEquals(
        "secret",
        Objects.requireNonNull(builder.findNextAccessibleArea(dungeonWithHiddenArea(), instance))
            .key());
  }

  private static DungeonTemplate dungeonWithHiddenArea() {
    DungeonTemplate dungeon = new DungeonTemplate();
    HiddenArea hiddenArea = new HiddenArea("secret", List.of("藏经阁"));
    dungeon.setAreaConfigs(
        new ArrayList<>(
            List.of(
                area("inner", "MAIN", List.of(hiddenArea)),
                area("core", "MAIN", null),
                area("secret", "HIDDEN", null))));
    return dungeon;
  }

  private static AreaConfig area(String key, String type, @Nullable List<HiddenArea> hiddenAreas) {
    return new AreaConfig(key, key, "描述", type, List.of(), List.of(), hiddenAreas);
  }

  private static DungeonInstance instance(String areaKey, String... exploredPois) {
    DungeonInstance instance = new DungeonInstance();
    instance.setCurrentAreaKey(areaKey);
    instance.setExploredPois(new ArrayList<>());
    for (String poiName : exploredPois) {
      instance.addExploredPoi(poiName);
    }
    return instance;
  }
}
