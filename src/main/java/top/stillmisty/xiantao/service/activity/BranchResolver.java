package top.stillmisty.xiantao.service.activity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.jspecify.annotations.Nullable;

/**
 * 分支效果解析器 — 按概率从 branches 列表中随机选择一组 effects。
 *
 * <p>消除 SubEventEffectExecutor 和 WorldEventEffectApplier 之间的分支逻辑重复。
 */
public final class BranchResolver {

  private BranchResolver() {}

  /**
   * 从分支列表中按概率选择一组效果。
   *
   * @param branches 分支列表，每项包含 {@code chance} (double) 和 {@code effects} (List&lt;Map&gt;)
   * @return 选中的 effects 列表，或 null（所有分支概率为 0 或列表为空）
   */
  @SuppressWarnings("unchecked")
  public static @Nullable List<Map<String, Object>> resolve(
      @Nullable List<Map<String, Object>> branches) {
    if (branches == null || branches.isEmpty()) return null;

    double roll = ThreadLocalRandom.current().nextDouble();
    double cumulative = 0;
    for (Map<String, Object> branch : branches) {
      Object chanceObj = branch.get("chance");
      double chance = chanceObj instanceof Number n ? n.doubleValue() : 0;
      cumulative += chance;
      if (roll < cumulative || Math.abs(cumulative - 1.0) < 1e-9) {
        return (List<Map<String, Object>>) branch.get("effects");
      }
    }
    return null;
  }
}
