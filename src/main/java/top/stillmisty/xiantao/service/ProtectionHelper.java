package top.stillmisty.xiantao.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.entity.DaoProtection;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.repository.DaoProtectionRepository;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;

/** 护道系统共享工具组件 */
@Component
@RequiredArgsConstructor
public class ProtectionHelper {

  public static final double MAX_TOTAL_BONUS_PERCENTAGE = 20.0;

  private final DaoProtectionRepository daoProtectionRepository;
  private final UserRepository userRepository;

  /** 计算总护道加成（仅统计同地点的护道者） */
  public double calculateProtectionBonus(User protege) {
    List<DaoProtection> protections = daoProtectionRepository.findByProtegeId(protege.getId());
    if (protections.isEmpty()) return 0.0;

    // 批量查询所有护道者，消除 N+1 问题
    List<Long> protectorIds =
        protections.stream().map(DaoProtection::getProtectorId).collect(Collectors.toList());
    Map<Long, User> protectorMap =
        userRepository.findByIds(protectorIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u));

    double totalBonus = 0.0;
    for (DaoProtection protection : protections) {
      User protector = protectorMap.get(protection.getProtectorId());
      if (protector != null && isInSameLocation(protector, protege)) {
        totalBonus += calculateSingleProtectorBonus(protector, protege);
      }
    }

    return Math.min(MAX_TOTAL_BONUS_PERCENTAGE, totalBonus);
  }

  /** 计算单个护道者的加成 公式：5% + (护道者境界层级 - 突破者境界层级) × 1% */
  public static double calculateSingleProtectorBonus(User protector, User protege) {
    int levelDiff = protector.getLevel() - protege.getLevel();
    return 5.0 + (levelDiff * 1.0);
  }

  /** 检查两个用户是否在同一地点 */
  public static boolean isInSameLocation(User user1, User user2) {
    return user1.getLocationId().equals(user2.getLocationId());
  }
}
