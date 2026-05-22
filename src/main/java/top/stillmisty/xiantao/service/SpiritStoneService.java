package top.stillmisty.xiantao.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

/** 灵石操作：原子增减、余额查询 */
@Service
public class SpiritStoneService {

  private final UserRepository userRepository;

  public SpiritStoneService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public void withdraw(Long userId, long amount) {
    if (amount <= 0) {
      throw new BusinessException(ErrorCode.PARAM_INVALID, "消耗灵石必须大于0");
    }
    int affected = userRepository.deductSpiritStonesIfEnough(userId, amount);
    if (affected == 0) {
      long balance = userRepository.findById(userId).map(User::getSpiritStones).orElse(0L);
      throw new BusinessException(ErrorCode.SPIRIT_STONES_INSUFFICIENT, amount, balance);
    }
  }

  @Transactional
  public void deposit(Long userId, long amount) {
    if (amount <= 0) {
      throw new BusinessException(ErrorCode.PARAM_INVALID, "添加灵石必须大于0");
    }
    userRepository.addSpiritStonesAtomically(userId, amount);
  }

  public long getBalance(Long userId) {
    return userRepository.findById(userId).map(User::getSpiritStones).orElse(0L);
  }
}
