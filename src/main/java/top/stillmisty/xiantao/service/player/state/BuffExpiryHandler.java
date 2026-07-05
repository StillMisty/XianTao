package top.stillmisty.xiantao.service.player.state;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.PlayerBuffRepository;

@Component
@RequiredArgsConstructor
@Order(4)
class BuffExpiryHandler implements StateHandler {

  private final PlayerBuffRepository playerBuffRepository;

  @Override
  public boolean tryResolve(Player user) {
    playerBuffRepository.deleteExpiredByUserId(user.getId());
    return false;
  }
}
