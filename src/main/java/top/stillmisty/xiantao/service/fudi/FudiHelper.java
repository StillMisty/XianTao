package top.stillmisty.xiantao.service.fudi;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.repository.FudiRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.shared.SharedKernel;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.player.UserStateService;

/** 福地相关的跨 Service 共享工具组件 */
@Component
@RequiredArgsConstructor
public class FudiHelper {

  private final FudiRepository fudiRepository;
  private final SpiritRepository spiritRepository;
  private final BeastRepository beastRepository;
  private final UserStateService userStateService;

  /**
   * 根据 userId 查找福地，并自动更新在线时间、更新地灵情绪状态并持久化。
   *
   * <p>注意：此方法有写入副作用（会保存 Fudi 和 Spirit），调用方需要在事务上下文中使用。
   */
  @Transactional
  public Optional<Fudi> findAndTouchFudi(Long userId) {
    Optional<Fudi> fudiOpt = fudiRepository.findByUserId(userId);
    fudiOpt.ifPresent(
        fudi -> {
          fudi.touchOnlineTime();
          spiritRepository
              .findByFudiId(fudi.getId())
              .ifPresent(spirit -> spiritRepository.save(spirit));
          fudiRepository.save(fudi);
          // 兽栏内灵兽自动回血
          List<Beast> beasts = beastRepository.findByFudiId(fudi.getId());
          boolean changed = false;
          for (Beast b : beasts) {
            if (b.tryAutoHeal()) changed = true;
          }
          if (changed) beastRepository.saveAll(beasts);
        });
    return fudiOpt;
  }

  /** 获取用户信息，不存在则抛出异常 */
  public User getUserOrThrow(Long userId) {
    return userStateService.loadUser(userId);
  }

  /** 解析地块编号（字符串 → 整数） */
  public Integer parseCellId(String position) {
    try {
      return Integer.valueOf(position);
    } catch (NumberFormatException e) {
      throw new BusinessException(ErrorCode.CELL_ID_INVALID);
    }
  }

  /** 根据生长时间计算作物等阶（委托至 SharedKernel） */
  public int getCropTier(int growTime) {
    return SharedKernel.getCropTier(growTime);
  }

  /** 根据灵田等级高于最低需求的程度计算生长速度倍率 */
  public double getLevelSpeedMultiplier(int cellLevel, int minRequired) {
    if (cellLevel < minRequired) return 0.5;
    int levelDiff = cellLevel - minRequired;
    return 1.0 + levelDiff * 0.15;
  }
}
