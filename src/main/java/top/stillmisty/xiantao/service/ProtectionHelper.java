package top.stillmisty.xiantao.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.entity.DaoProtection;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.repository.DaoProtectionRepository;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

/** 护道系统共享工具组件 */
@Component
@RequiredArgsConstructor
public class ProtectionHelper {

  static final double BASE_BONUS_PERCENTAGE = 5.0;
  static final double LEVEL_DIFF_BONUS_PERCENTAGE = 1.0;
  static final double MAX_TOTAL_BONUS_PERCENTAGE = 20.0;

  private final DaoProtectionRepository daoProtectionRepository;
  private final UserRepository userRepository;

  /** 计算单个护道者的加成 公式：5% + (护道者境界层级 - 突破者境界层级) × 1% */
  double calculateSingleProtectorBonus(User protector, User protege) {
    int levelDiff = protector.getLevel() - protege.getLevel();
    return BASE_BONUS_PERCENTAGE + (levelDiff * LEVEL_DIFF_BONUS_PERCENTAGE);
  }

  /** 计算总护道加成（仅统计同地点的护道者） */
  double calculateProtectionBonus(User protege) {
    List<DaoProtection> protections = daoProtectionRepository.findByProtegeId(protege.getId());
    double totalBonus = 0.0;

    for (DaoProtection protection : protections) {
      Optional<User> protectorOpt = userRepository.findById(protection.getProtectorId());
      if (protectorOpt.isPresent()) {
        User protector = protectorOpt.get();
        if (isInSameLocation(protector, protege)) {
          totalBonus += calculateSingleProtectorBonus(protector, protege);
        }
      }
    }

    return Math.min(MAX_TOTAL_BONUS_PERCENTAGE, totalBonus);
  }

  /** 检查两个用户是否在同一地点 */
  boolean isInSameLocation(User user1, User user2) {
    return user1.getLocationId().equals(user2.getLocationId());
  }
}
