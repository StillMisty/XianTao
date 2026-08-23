package top.stillmisty.xiantao.service.combat;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.monster.vo.HpChange;
import top.stillmisty.xiantao.domain.monster.vo.SkillProc;

/** 高光战斗检测器 识别势均力敌、回合数多、稀有触发的战斗 */
@Slf4j
@Component
public class HighlightBattleDetector {

  /** 高光战斗回合数阈值 */
  private static final int HIGHLIGHT_ROUND_THRESHOLD = 10;

  /** 高光战斗血量阈值（双方血量都低于此比例） */
  private static final double HIGHLIGHT_HP_THRESHOLD = 0.3;

  /**
   * 检测是否为高光战斗
   *
   * @param battleResult 战斗结果
   * @param battleIndex 战斗序号
   * @return 高光战斗信息，如果不是高光战斗返回null
   */
  public @Nullable HighlightInfo detectHighlight(BattleResultVO battleResult, int battleIndex) {
    if (battleResult == null) {
      return null;
    }

    int rounds = battleResult.rounds();
    Map<String, HpChange> playerHpChange = battleResult.playerHpChange();

    // 检查回合数
    boolean isLongBattle = rounds >= HIGHLIGHT_ROUND_THRESHOLD;

    // 检查血量变化：任一队员被打到残血即视为势均力敌（原仅取首个成员，多人队伍失真）
    boolean isCloseBattle = false;
    if (playerHpChange != null && !playerHpChange.isEmpty()) {
      for (HpChange hpChange : playerHpChange.values()) {
        if (hpChange.before() > 0) {
          double hpRatio = (double) hpChange.after() / hpChange.before();
          if (hpRatio <= HIGHLIGHT_HP_THRESHOLD) {
            isCloseBattle = true;
            break;
          }
        }
      }
    }

    // 检查技能多样性：≥3 个不同技能登场视为精彩战斗
    // （原判定「同一技能触发≥3 次」与「稀有」语义相反——常用技能每场都满足）
    boolean hasSkillVariety = false;
    List<SkillProc> skillProcs = battleResult.skillProcs();
    if (skillProcs != null && skillProcs.size() >= 3) {
      hasSkillVariety = true;
    }

    // 判断是否为高光战斗
    if (isLongBattle || isCloseBattle || hasSkillVariety) {
      String reason = buildHighlightReason(isLongBattle, isCloseBattle, hasSkillVariety, rounds);
      log.info("检测到高光战斗 - 序号: {}, 原因: {}", battleIndex, reason);
      return HighlightInfo.builder().battleIndex(battleIndex).reason(reason).rounds(rounds).build();
    }

    return null;
  }

  private String buildHighlightReason(
      boolean isLongBattle, boolean isCloseBattle, boolean hasSkillVariety, int rounds) {
    StringBuilder reason = new StringBuilder();
    if (isLongBattle) {
      reason.append(String.format("战斗持续%d回合", rounds));
    }
    if (isCloseBattle) {
      if (!reason.isEmpty()) reason.append("，");
      reason.append("势均力敌");
    }
    if (hasSkillVariety) {
      if (!reason.isEmpty()) reason.append("，");
      reason.append("技能纷呈");
    }
    return reason.toString();
  }

  /** 高光战斗信息 */
  @lombok.Builder
  @lombok.Getter
  public static class HighlightInfo {
    private final int battleIndex;
    private final String reason;
    private final int rounds;
  }
}
