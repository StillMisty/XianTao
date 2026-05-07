package top.stillmisty.xiantao.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.shared.SharedKernel;
import top.stillmisty.xiantao.domain.user.entity.DaoProtection;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.repository.DaoProtectionRepository;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

/** 护道系统共享工具组件 */
@Component
@RequiredArgsConstructor
public class ProtectionHelper {

  static final double MAX_TOTAL_BONUS_PERCENTAGE = 20.0;

  private final DaoProtectionRepository daoProtectionRepository;
  private final UserRepository userRepository;

  /** 计算总护道加成（仅统计同地点的护道者） */
  double calculateProtectionBonus(User protege) {
    List<DaoProtection> protections = daoProtectionRepository.findByProtegeId(protege.getId());
    double totalBonus = 0.0;

    for (DaoProtection protection : protections) {
      Optional<User> protectorOpt = userRepository.findById(protection.getProtectorId());
      if (protectorOpt.isPresent()) {
        User protector = protectorOpt.get();
        if (SharedKernel.isInSameLocation(protector, protege)) {
          totalBonus += SharedKernel.calculateSingleProtectorBonus(protector, protege);
        }
      }
    }

    return Math.min(MAX_TOTAL_BONUS_PERCENTAGE, totalBonus);
  }
}
