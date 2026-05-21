package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.monster.vo.CombatLogEntry;
import top.stillmisty.xiantao.domain.pvp.vo.SparResultVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.pvp.PvpService;

@Component
@RequiredArgsConstructor
public class PvpCommandHandler implements CommandGroup {

  private final PvpService pvpService;

  public String handleSpar(
      PlatformType platform, String openId, String targetNickname, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> pvpService.spar(platform, openId, targetNickname),
        fmt,
        vo -> formatSparResult(vo, fmt));
  }

  private String formatSparResult(SparResultVO vo, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(fmt.heading(vo.attackerNickname() + " 与 " + vo.defenderNickname() + " 切磋！", "⚔️"));

    String winnerName = vo.attackerWon() ? vo.attackerNickname() : vo.defenderNickname();
    sb.append(fmt.bold(winnerName + " 获胜！"));
    sb.append("\n\n");

    sb.append(fmt.bold("战斗记录")).append("\n");
    var logs = vo.combatLog();
    int showCount = Math.min(5, logs.size());
    for (int i = logs.size() - showCount; i < logs.size(); i++) {
      var entry = logs.get(i);
      StringBuilder line = new StringBuilder();
      line.append(String.format("[R%d] ", entry.round()));
      line.append(entry.attackerName()).append(" ");
      if (entry.attackType() == CombatLogEntry.AttackType.SKILL) {
        line.append("施展");
        if (entry.skillName() != null) {
          line.append(fmt.italic(entry.skillName()));
        }
      } else {
        line.append("攻击");
      }
      line.append(" → ").append(entry.defenderName());
      if (entry.damageDealt() > 0) {
        line.append(
            String.format(
                " (-%d, HP %d→%d)",
                entry.damageDealt(), entry.defenderHpBefore(), entry.defenderHpAfter()));
      }
      if (entry.isKill()) {
        line.append(" 💀击杀");
      }
      sb.append(fmt.listItem(line.toString()));
    }

    sb.append("\n").append(fmt.bold("剩余状态")).append("\n");
    sb.append(formatHpSide(vo.attackerNickname(), vo.attackerHpStatus(), fmt));
    sb.append(formatHpSide(vo.defenderNickname(), vo.defenderHpStatus(), fmt));

    sb.append("\n").append(fmt.tip("切磋为模拟战，不实际消耗 HP，双方状态不变"));

    return sb.toString();
  }

  private String formatHpSide(String nickname, List<SparResultVO.HpStatus> hpList, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    boolean allDead = hpList.stream().allMatch(hp -> hp.hp() <= 0);
    if (hpList.size() == 1 && hpList.getFirst().name().equals(nickname)) {
      String hpText =
          String.format("%s  %d/%d", nickname, hpList.getFirst().hp(), hpList.getFirst().maxHp());
      sb.append(fmt.listItem(allDead ? fmt.strikethrough(hpText) : hpText));
    } else {
      sb.append(fmt.listItem(allDead ? fmt.strikethrough(nickname) : nickname));
      for (var hp : hpList) {
        String hpText = String.format("%s  %d/%d", hp.name(), hp.hp(), hp.maxHp());
        sb.append(fmt.subListItem(hp.hp() <= 0 ? fmt.strikethrough(hpText) : hpText));
      }
    }
    return sb.toString();
  }

  @Override
  public String groupName() {
    return "切磋";
  }

  @Override
  public String groupDescription() {
    return "玩家对战";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(new CommandEntry("切磋 「道号」", "与其他玩家切磋对战", "切磋 张三"));
  }
}
