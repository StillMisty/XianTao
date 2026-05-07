package top.stillmisty.xiantao.domain.shared;

import top.stillmisty.xiantao.domain.user.entity.User;

/** 跨 Service 共享的纯函数工具集 */
public final class SharedKernel {

  private SharedKernel() {}

  /** 计算单个护道者的加成 公式：5% + (护道者境界层级 - 突破者境界层级) × 1% */
  public static double calculateSingleProtectorBonus(User protector, User protege) {
    int levelDiff = protector.getLevel() - protege.getLevel();
    return 5.0 + (levelDiff * 1.0);
  }

  /** 检查两个用户是否在同一地点 */
  public static boolean isInSameLocation(User user1, User user2) {
    return user1.getLocationId().equals(user2.getLocationId());
  }

  /** 根据生长时间计算作物等阶 */
  public static int getCropTier(int growTime) {
    if (growTime <= 24) return 1;
    if (growTime <= 48) return 2;
    if (growTime <= 72) return 3;
    if (growTime <= 120) return 4;
    return 5;
  }
}
