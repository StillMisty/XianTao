package top.stillmisty.xiantao.service.combat;

import java.util.List;
import top.stillmisty.xiantao.domain.monster.vo.DropItem;

/** 一段历练的结算结果 — 已入账的基础修为、击杀修为、物品与战斗统计。 */
public record TrainingSettlement(
    long minutes,
    long baseExp,
    long killExp,
    List<DropItem> items,
    CombatSummary combatSummary,
    boolean beastDeployed,
    double efficiencyMultiplier,
    double levelDecayMultiplier) {

  public static TrainingSettlement empty() {
    return new TrainingSettlement(0, 0, 0, List.of(), CombatSummary.empty(), false, 1.0, 1.0);
  }

  public long totalExp() {
    return baseExp + killExp;
  }
}
