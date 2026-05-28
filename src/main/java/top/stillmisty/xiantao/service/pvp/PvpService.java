package top.stillmisty.xiantao.service.pvp;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.pvp.vo.SparResultVO;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.combat.CombatService;
import top.stillmisty.xiantao.service.player.UserStateService;

@Slf4j
@Service
@RequiredArgsConstructor
public class PvpService {

  private final UserStateService userStateService;
  private final UserRepository userRepository;
  private final CombatService combatService;

  @Authenticated
  @Transactional
  public ServiceResult<SparResultVO> spar(
      PlatformType platform, String openId, String targetNickname) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(spar(userId, targetNickname));
  }

  @Transactional
  public SparResultVO spar(Long userId, String targetNickname) {
    User attacker = userStateService.loadUser(userId);
    User defender =
        userRepository
            .findByNickname(targetNickname)
            .orElseThrow(() -> new BusinessException(ErrorCode.PLAYER_NOT_FOUND, targetNickname));

    if (attacker.getId().equals(defender.getId())) {
      throw new BusinessException(ErrorCode.PLAYER_CANNOT_SELF);
    }

    var teamA = combatService.buildPlayerTeam(attacker, Map.of(), "A");
    var teamB = combatService.buildPlayerTeam(defender, Map.of(), "B");

    teamA.members().forEach(m -> m.heal(m.getMaxHp()));
    teamB.members().forEach(m -> m.heal(m.getMaxHp()));

    var result = combatService.simulate(teamA, teamB, 50);

    boolean attackerWon = "A".equals(result.winner());

    return new SparResultVO(
        attacker.getNickname(),
        defender.getNickname(),
        attackerWon,
        result.combatLog(),
        collectHpStatus(teamA),
        collectHpStatus(teamB));
  }

  private static java.util.List<SparResultVO.HpStatus> collectHpStatus(CombatTeam team) {
    return team.members().stream()
        .map(m -> new SparResultVO.HpStatus(m.getName(), m.getHp(), m.getMaxHp()))
        .toList();
  }
}
