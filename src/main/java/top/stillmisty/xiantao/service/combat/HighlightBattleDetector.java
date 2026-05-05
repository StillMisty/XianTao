package top.stillmisty.xiantao.service.combat;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;

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
  public HighlightInfo detectHighlight(BattleResultVO battleResult, int battleIndex) {
    if (battleResult == null) {
      return null;
    }

    int rounds = battleResult.rounds();
    Map<String, Object> playerHpChange = battleResult.playerHpChange();

    // 检查回合数
    boolean isLongBattle = rounds >= HIGHLIGHT_ROUND_THRESHOLD;

    // 检查血量变化
    boolean isCloseBattle = false;
    if (playerHpChange != null && playerHpChange.containsKey("Player")) {
      @SuppressWarnings("unchecked")
      Map<String, Object> hpChange = (Map<String, Object>) playerHpChange.get("Player");
      if (hpChange != null) {
        int hpBefore = (int) hpChange.get("before");
        int hpAfter = (int) hpChange.get("after");
        double hpRatio = (double) hpAfter / hpBefore;
        isCloseBattle = hpRatio <= HIGHLIGHT_HP_THRESHOLD;
      }
    }

    // 检查是否有稀有技能触发
    boolean hasRareSkillProc = false;
    List<Map<String, Object>> skillProcs = battleResult.skillProcs();
    if (skillProcs != null) {
      for (Map<String, Object> proc : skillProcs) {
        String key = (String) proc.get("key");
        int count = (int) proc.get("count");
        // 如果某个技能触发了多次，认为是高光
        if (count >= 3) {
          hasRareSkillProc = true;
          break;
        }
      }
    }

    // 判断是否为高光战斗
    if (isLongBattle || isCloseBattle || hasRareSkillProc) {
      String reason = buildHighlightReason(isLongBattle, isCloseBattle, hasRareSkillProc, rounds);
      log.info("检测到高光战斗 - 序号: {}, 原因: {}", battleIndex, reason);
      return HighlightInfo.builder().battleIndex(battleIndex).reason(reason).rounds(rounds).build();
    }

    return null;
  }

  private String buildHighlightReason(
      boolean isLongBattle, boolean isCloseBattle, boolean hasRareSkillProc, int rounds) {
    StringBuilder reason = new StringBuilder();
    if (isLongBattle) {
      reason.append(String.format("战斗持续%d回合", rounds));
    }
    if (isCloseBattle) {
      if (!reason.isEmpty()) reason.append("，");
      reason.append("势均力敌");
    }
    if (hasRareSkillProc) {
      if (!reason.isEmpty()) reason.append("，");
      reason.append("触发稀有技能");
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
