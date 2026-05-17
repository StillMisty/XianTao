package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.service.player.UserStateService;

/** 灵石操作：原子增减、余额查询 */
@Service
@RequiredArgsConstructor
public class SpiritStoneService {

  private final UserRepository userRepository;
  private final UserStateService userStateService;

  @Transactional
  public void withdraw(Long userId, int amount) {
    int affected = userRepository.deductSpiritStonesIfEnough(userId, amount);
    if (affected == 0) {
      User user = userStateService.loadUser(userId);
      throw new BusinessException(
          ErrorCode.SPIRIT_STONES_INSUFFICIENT, amount, user.getSpiritStones());
    }
  }

  @Transactional
  public void deposit(Long userId, int amount) {
    userRepository.addSpiritStonesAtomically(userId, amount);
  }

  public int getBalance(Long userId) {
    return userStateService.loadUser(userId).getSpiritStones().intValue();
  }
}
