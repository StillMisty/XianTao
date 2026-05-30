package top.stillmisty.xiantao.service.dungeon;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonStatus;
import top.stillmisty.xiantao.infrastructure.repository.DungeonInstanceRepository;

@Component
@RequiredArgsConstructor
public class DungeonInstanceManager {

  private final DungeonInstanceRepository instanceRepository;

  @Transactional(propagation = Propagation.MANDATORY)
  public void markFailed(DungeonInstance instance) {
    instance.markFailed();
    instanceRepository.save(instance);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void markAbandoned(DungeonInstance instance) {
    instance.setStatus(DungeonStatus.ABANDONED);
    instanceRepository.save(instance);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void markCompleted(DungeonInstance instance) {
    instance.markCompleted();
    instanceRepository.save(instance);
  }
}
