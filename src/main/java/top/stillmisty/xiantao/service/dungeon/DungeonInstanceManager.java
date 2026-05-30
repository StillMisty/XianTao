package top.stillmisty.xiantao.service.dungeon;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonStatus;
import top.stillmisty.xiantao.infrastructure.repository.DungeonInstanceRepository;

/**
 * 秘境实例状态管理
 *
 * <p>提取自DungeonService，消除自代理模式。所有实例状态变更必须通过此类进行， 以确保事务边界正确。
 */
@Component
@RequiredArgsConstructor
public class DungeonInstanceManager {

  private final DungeonInstanceRepository instanceRepository;

  /** 标记实例为失败状态。必须在事务中调用（MANDATORY传播）。 */
  @Transactional(propagation = Propagation.MANDATORY)
  public void markFailed(DungeonInstance instance) {
    instance.markFailed();
    instanceRepository.save(instance);
  }

  /** 标记实例为放弃状态。必须在事务中调用（MANDATORY传播）。 */
  @Transactional(propagation = Propagation.MANDATORY)
  public void markAbandoned(DungeonInstance instance) {
    instance.setStatus(DungeonStatus.ABANDONED);
    instanceRepository.save(instance);
  }

  /** 标记实例为完成状态。必须在事务中调用（MANDATORY传播）。 */
  @Transactional(propagation = Propagation.MANDATORY)
  public void markCompleted(DungeonInstance instance) {
    instance.markCompleted();
    instanceRepository.save(instance);
  }
}
