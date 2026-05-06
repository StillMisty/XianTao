package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.monster.vo.CombatLogEntry;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.service.annotation.Authenticated;

@Slf4j
@Service
@RequiredArgsConstructor
public class PvpService {

  private final UserStateService userStateService;
  private final UserRepository userRepository;
  private final CombatService combatService;

  @Authenticated
  @Transactional
  public ServiceResult<String> spar(PlatformType platform, String openId, String targetNickname) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(spar(userId, targetNickname));
  }

  @Transactional
  public String spar(Long userId, String targetNickname) {
    User attacker = userStateService.loadUser(userId);
    User defender =
        userRepository
            .findByNickname(targetNickname)
            .orElseThrow(() -> new IllegalStateException("未找到玩家【" + targetNickname + "】"));

    if (attacker.getId().equals(defender.getId())) {
      throw new IllegalStateException("不能和自己切磋！");
    }

    var teamA = combatService.buildPlayerTeam(attacker);
    var teamB = combatService.buildPlayerTeam(defender);

    teamA.members().forEach(m -> m.heal(m.getMaxHp()));
    teamB.members().forEach(m -> m.heal(m.getMaxHp()));

    var result = combatService.simulate(teamA, teamB, 50);

    boolean playerAWon = "Player".equals(result.winner());

    StringBuilder sb = new StringBuilder();
    sb.append("⚔️ ")
        .append(attacker.getNickname())
        .append(" 与 ")
        .append(defender.getNickname())
        .append(" 切磋！\n");

    String winnerName = playerAWon ? attacker.getNickname() : defender.getNickname();
    sb.append("🏆 ").append(winnerName).append(" 获胜！\n\n");

    sb.append("【战斗记录】\n");
    var logs = result.combatLog();
    int showCount = Math.min(5, logs.size());
    for (int i = logs.size() - showCount; i < logs.size(); i++) {
      var entry = logs.get(i);
      sb.append("  ").append(entry.attackerName()).append(" ");
      sb.append(entry.attackType() == CombatLogEntry.AttackType.SKILL ? "释放" : "攻击");
      if (entry.skillName() != null) {
        sb.append("【").append(entry.skillName()).append("】");
      }
      sb.append(" → ").append(entry.defenderName());
      if (entry.damageDealt() > 0) {
        sb.append(" (").append(entry.damageDealt()).append("点伤害)");
      }
      if (entry.isKill()) {
        sb.append(" 💀击杀");
      }
      sb.append("\n");
    }

    sb.append("\n").append("切磋为模拟战，不实际消耗 HP，双方状态不变");

    return sb.toString();
  }
}
