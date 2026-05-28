package top.stillmisty.xiantao.service.cultivation;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.monster.vo.CombatLogEntry;
import top.stillmisty.xiantao.domain.user.enums.CultivationRealm;
import top.stillmisty.xiantao.domain.user.enums.TribulationType;

/**
 * 雷劫/突破 LLM 叙事生成器
 *
 * <p>设计决策：从 CultivationService 提取，将 LLM 文本生成与突破业务逻辑分离。 CultivationService 负责数值计算和状态变更，此类负责纯文本生成。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TribulationNarrativeGenerator {

  private final ChatClient chatClient;

  /** 生成雷劫战斗叙事（LLM 生成，失败时回退默认文案） */
  public String generateCombatNarrative(
      TribulationType tribulationType, String nickname, BattleResultVO result, boolean playerWon) {
    String combatSummary = buildCombatSummary(result);

    try {
      String prompt =
          """
              你是一位仙侠世界的说书人，擅长渲染雷劫的宏大战斗场面。
              请根据战斗记录撰写一段雷劫战斗过程的描述。

              【修士信息】
              道号：%s
              雷劫类型：%s
              结果：%s

              【战斗记录】
              %s

              【写作要求】
              字数 50-100 字，仙侠古风，气势磅礴，有画面感。
              描写雷劫的威力展现、修士的奋力抵抗、灵兽的助阵牺牲、关键转折点。
              突出"%s"雷劫的独特特征和压迫感。
              %s
              只需要输出战斗描述正文，不要任何前缀、后缀或解释。"""
              .formatted(
                  nickname,
                  tribulationType.getDisplayName(),
                  playerWon ? "修士成功渡过雷劫" : "修士未能抵挡雷劫",
                  combatSummary,
                  tribulationType.getDisplayName(),
                  playerWon ? "结尾要有劫后余生的爽感和境界突破的喜悦。" : "结尾要有雷劫的残酷和道基反噬的悲壮感。");

      String llmResult = chatClient.prompt().user(prompt).call().content();

      if (llmResult != null && !llmResult.isBlank()) {
        return llmResult.strip();
      }
    } catch (Exception e) {
      log.warn("LLM生成战斗叙事失败，使用默认文案", e);
    }

    return playerWon ? "雷劫已渡，道行精进！" : "雷劫降临，未能抵挡天劫化身……境界突破失败！";
  }

  /** 生成跨大境界突破贺词（LLM 生成，失败时回退默认文案） */
  public String generateBreakthroughMessage(CultivationRealm realm, String nickname) {
    try {
      String prompt =
          """
              你是一位仙侠世界的说书人，擅长渲染境界突破的宏大场面。
              现在有一位修士突破了境界桎梏，请撰写一段突破贺词。

              【修士信息】
              道号：%s
              新境界：%s

              【新境界介绍】
              %s

              【写作要求】
              字数 40-80 字，仙侠古风，气势磅礴。
              描写突破时的天地异象、修士感悟或命运转折。
              结合该境界的独特意境，让玩家感受到实力暴涨的爽感。
              只需要输出贺词正文，不要任何前缀、后缀或解释。"""
              .formatted(nickname, realm.getRealmName(), realm.getDescription());

      String result = chatClient.prompt().user(prompt).call().content();

      if (result != null && !result.isBlank()) {
        return result.strip();
      }
    } catch (Exception e) {
      log.warn("LLM生成突破贺词失败，使用默认文案", e);
    }
    return realm.getBreakthroughMessage();
  }

  private String buildCombatSummary(BattleResultVO result) {
    StringBuilder sb = new StringBuilder();
    sb.append("回合数：").append(result.rounds()).append("\n");

    if (result.playerHpChange() != null) {
      result
          .playerHpChange()
          .forEach(
              (name, hp) ->
                  sb.append(String.format("%s HP：%d → %d\n", name, hp.before(), hp.after())));
    }

    if (result.skillProcs() != null && !result.skillProcs().isEmpty()) {
      sb.append("技能：");
      sb.append(
          result.skillProcs().stream()
              .map(sp -> sp.key() + "×" + sp.count())
              .collect(Collectors.joining("、")));
      sb.append("\n");
    }

    if (result.combatLog() != null) {
      sb.append("关键事件：\n");
      result.combatLog().stream()
          .filter(e -> e.damageDealt() > 0 || e.isKill())
          .forEach(
              e -> {
                String action =
                    e.attackType() == CombatLogEntry.AttackType.SKILL && e.skillName() != null
                        ? "施展「" + e.skillName() + "」"
                        : "攻击";
                String line =
                    String.format(
                        "  第%d回合：%s%s，对%s造成%d伤害 (HP: %d→%d)",
                        e.round(),
                        e.attackerName(),
                        action,
                        e.defenderName(),
                        e.damageDealt(),
                        e.defenderHpBefore(),
                        e.defenderHpAfter());
                if (e.isKill()) line += " ← 击杀！";
                if (e.defenderHpBefore() > 0
                    && e.defenderHpAfter() > 0
                    && (double) e.defenderHpAfter() / e.defenderHpBefore() < 0.3) {
                  line += " ← 重创！";
                }
                sb.append(line).append("\n");
              });
    }

    return sb.toString();
  }
}
