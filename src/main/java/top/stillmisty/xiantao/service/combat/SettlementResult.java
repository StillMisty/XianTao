package top.stillmisty.xiantao.service.combat;

/** 历练结算结果 — 战斗统计 + 灵兽参与信息 */
public record SettlementResult(CombatSummary combatSummary, boolean beastDeployed) {

  public static SettlementResult empty() {
    return new SettlementResult(CombatSummary.empty(), false);
  }
}
