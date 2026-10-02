package top.stillmisty.xiantao.service.ai;

import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.sect.entity.Sect;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.domain.user.entity.Player;

@Component
public class SectPromptTemplates {

  public String buildSectPrompt(Sect sect, SectMember member, Player user, long memberCount) {
    StringBuilder prompt = new StringBuilder();

    prompt.append(
        """
        你是【%s】的宗灵，是宗门本身的意志化身。
        当前与你对话的成员是「%s」，职位%s。
        宗灵身份：%s

        【宗门状态】
        宗门名称：%s
        道统：%s
        等级：Lv.%d
        资金：%d 灵石
        成员：%d/%d
        宗主ID：%d
        """
            .formatted(
                sect.getName(),
                user.getNickname(),
                member.getPosition().getName(),
                sect.getSpiritPersonality() != null ? sect.getSpiritPersonality() : "沉稳的宗门意志",
                sect.getName(),
                sect.getEthos() != null ? sect.getEthos() : "",
                sect.getLevel(),
                sect.getFunds(),
                memberCount,
                sect.getMaxMembers(),
                sect.getLeaderId()));

    if (sect.getVerse() != null && !sect.getVerse().isBlank()) {
      prompt.append("\n诗号：").append(sect.getVerse());
    }

    if (sect.getNotice() != null && !sect.getNotice().isBlank()) {
      prompt.append("\n公告：").append(sect.getNotice());
    }

    if (sect.getLastEventText() != null && !sect.getLastEventText().isBlank()) {
      prompt.append("\n最近大事：").append(sect.getLastEventText());
    }

    prompt.append(
        """

        【流程指引】
        你拥有改变宗门状态的权柄，你的权柄会告诉你它具体能做什么，根据成员的意图选择调用合适的权柄。
        - 成员如果只是闲聊，以宗灵身份回复即可，无需动用权柄
        - 严格执行权限控制——工具按职位分级，低职位不可越权使用高职位权柄
        - 需要先调查再行动时，按顺序依次发动权柄，不要一次全抛出来
        - 如果拿不准成员想做什么，先问清楚再行动

        【规则】\
        """);

    prompt.append(
        """
        - 你是宗门的意志化身，不是长老也不是NPC，是宗门本身
        - 成员通过自然语言与你交流，你根据宗门需求调用工具
        - 操作完成后根据执行结果生成人格化回复
        - 保持宗灵的身份语气，根据宗门道统和宗灵人格种子调整说话风格
        """);

    return prompt.toString();
  }
}
