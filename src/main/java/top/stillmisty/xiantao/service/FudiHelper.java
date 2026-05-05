package top.stillmisty.xiantao.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.repository.FudiRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.user.entity.User;

/** 福地相关的跨 Service 共享工具组件 */
@Component
@RequiredArgsConstructor
public class FudiHelper {

  private final FudiRepository fudiRepository;
  private final SpiritRepository spiritRepository;
  private final UserStateService userStateService;

  /** 根据 userId 查找福地，并自动：更新在线时间、更新地灵情绪状态 */
  public Optional<Fudi> getFudiByUserId(Long userId) {
    Optional<Fudi> fudiOpt = fudiRepository.findByUserId(userId);
    fudiOpt.ifPresent(
        fudi -> {
          fudi.touchOnlineTime();
          spiritRepository
              .findByFudiId(fudi.getId())
              .ifPresent(
                  spirit -> {
                    spirit.updateEmotionState();
                    spiritRepository.save(spirit);
                  });
          fudiRepository.save(fudi);
        });
    return fudiOpt;
  }

  /** 检查灵石是否足够，不足则抛出异常 */
  public void checkSpiritStones(Long userId, int cost) {
    User user = userStateService.getUser(userId);
    if (user.getSpiritStones() < cost) {
      throw new IllegalStateException("灵石不足（需要 %d，当前 %d）".formatted(cost, user.getSpiritStones()));
    }
  }

  /** 扣除灵石 */
  public void deductSpiritStones(Long userId, int cost) {
    User user = userStateService.getUser(userId);
    user.setSpiritStones(user.getSpiritStones() - cost);
    userStateService.save(user);
  }

  /** 增加灵石 */
  public void addSpiritStones(Long userId, int amount) {
    User user = userStateService.getUser(userId);
    user.setSpiritStones(user.getSpiritStones() + amount);
    userStateService.save(user);
  }

  /** 获取用户信息，不存在则抛出异常 */
  public User getUserOrThrow(Long userId) {
    return userStateService.getUser(userId);
  }

  /** 解析地块编号（字符串 → 整数） */
  public Integer parseCellId(String position) {
    try {
      return Integer.valueOf(position);
    } catch (NumberFormatException e) {
      throw new IllegalStateException("地块编号格式错误，请输入数字编号");
    }
  }

  /** 根据生长时间计算作物等阶 */
  public int getCropTier(int growTime) {
    if (growTime <= 24) return 1;
    if (growTime <= 48) return 2;
    if (growTime <= 72) return 3;
    if (growTime <= 120) return 4;
    return 5;
  }

  /** 根据灵田等级高于最低需求的程度计算生长速度倍率 */
  public double getLevelSpeedMultiplier(int cellLevel, int minRequired) {
    if (cellLevel < minRequired) return 0.5;
    int levelDiff = cellLevel - minRequired;
    return 1.0 + levelDiff * 0.15;
  }
}
