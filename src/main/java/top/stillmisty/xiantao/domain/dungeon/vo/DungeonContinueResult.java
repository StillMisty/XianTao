package top.stillmisty.xiantao.domain.dungeon.vo;

/** 秘境推进的结果 — 可能是进入新区域，也可能是通关结算 */
public sealed interface DungeonContinueResult
    permits DungeonContinueResult.AreaView, DungeonContinueResult.Completed {

  /** 进入新区域 */
  record AreaView(DungeonEnterResult enterResult) implements DungeonContinueResult {}

  /** 通关结算完成 */
  record Completed(String settlementMessage) implements DungeonContinueResult {}
}
