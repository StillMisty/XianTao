package top.stillmisty.xiantao.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.service.analytics.AnalyticsService;

/** 灵石操作：原子增减、余额查询 */
@Service
public class SpiritStoneService {

  private final UserRepository userRepository;
  private final AnalyticsService analyticsService;

  public SpiritStoneService(UserRepository userRepository, AnalyticsService analyticsService) {
    this.userRepository = userRepository;
    this.analyticsService = analyticsService;
  }

  @Transactional
  public void withdraw(Long userId, long amount) {
    withdraw(userId, amount, "unknown");
  }

  /** 扣灵石并记录经济事件；origin 为消耗来源（bounty/shop/forge/sect/...）。 */
  @Transactional
  public void withdraw(Long userId, long amount, String origin) {
    if (amount <= 0) {
      throw new BusinessException(ErrorCode.PARAM_INVALID, "消耗灵石必须大于0");
    }
    int affected = userRepository.deductSpiritStonesIfEnough(userId, amount);
    if (affected == 0) {
      long balance = userRepository.findById(userId).map(Player::getSpiritStones).orElse(0L);
      throw new BusinessException(ErrorCode.SPIRIT_STONES_INSUFFICIENT, amount, balance);
    }
    analyticsService.record("stones_spend", userId, origin, amount);
  }

  @Transactional
  public void deposit(Long userId, long amount) {
    deposit(userId, amount, "unknown");
  }

  /** 加灵石并记录经济事件；origin 为产出渠道（bounty/dungeon/shop/...）。 */
  @Transactional
  public void deposit(Long userId, long amount, String origin) {
    if (amount <= 0) {
      throw new BusinessException(ErrorCode.PARAM_INVALID, "添加灵石必须大于0");
    }
    userRepository.addSpiritStonesAtomically(userId, amount);
    analyticsService.record("stones_gain", userId, origin, amount);
  }

  @Transactional(readOnly = true)
  public long getBalance(Long userId) {
    return userRepository.findById(userId).map(Player::getSpiritStones).orElse(0L);
  }
}
