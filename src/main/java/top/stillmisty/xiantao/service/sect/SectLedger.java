package top.stillmisty.xiantao.service.sect;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.infrastructure.repository.SectMemberRepository;
import top.stillmisty.xiantao.infrastructure.repository.SectRepository;

/** 宗门账本 — 宗门资金与成员贡献变动的唯一入口。所有变动均为原子 SQL，防并发双花。 */
@Component
@RequiredArgsConstructor
public class SectLedger {

  /** 扩容成员上限的档位数量 */
  static final int EXPAND_SLOTS = 5;

  /** 每档扩容所需资金 */
  static final long EXPAND_COST_PER_SLOT = 500L;

  private final SectRepository sectRepository;
  private final SectMemberRepository sectMemberRepository;

  /** 原子累加宗门资金（捐献） */
  public void addFunds(Long sectId, long amount) {
    sectRepository.addFunds(sectId, amount);
  }

  /** 原子条件扣减宗门资金，余额不足时返回 false */
  public boolean deductFundsIfEnough(Long sectId, long amount) {
    return sectRepository.deductFundsIfEnough(sectId, amount) > 0;
  }

  /** 原子累加成员贡献 */
  public void addContribution(Long userId, int gain) {
    sectMemberRepository.addContribution(userId, gain);
  }

  /** 原子条件扣减成员贡献，余额不足时返回 false */
  public boolean deductContributionIfEnough(Long userId, int cost) {
    return sectMemberRepository.deductContributionIfEnough(userId, cost) > 0;
  }

  /** 宗门升级价目表：当前等级 → 升级至下一级所需资金 */
  public static long upgradeCost(int level) {
    return switch (level) {
      case 1 -> 5000;
      case 2 -> 15000;
      case 3 -> 30000;
      case 4 -> 50000;
      default -> Long.MAX_VALUE;
    };
  }
}
