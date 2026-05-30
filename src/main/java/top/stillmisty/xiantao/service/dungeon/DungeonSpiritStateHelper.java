package top.stillmisty.xiantao.service.dungeon;

import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonSpiritState;
import top.stillmisty.xiantao.infrastructure.repository.DungeonSpiritStateRepository;

@Component
@RequiredArgsConstructor
public class DungeonSpiritStateHelper {

  private final DungeonSpiritStateRepository spiritStateRepository;

  public DungeonSpiritState findOrCreate(Long instanceId, Long dungeonId, Long userId) {
    return spiritStateRepository
        .findByInstanceIdAndUserId(instanceId, userId)
        .orElseGet(
            () -> {
              DungeonSpiritState state = new DungeonSpiritState();
              state.setInstanceId(instanceId);
              state.setDungeonId(dungeonId);
              state.setUserId(userId);
              state.setFavor(0);
              state.setFavorLog(new ArrayList<>());
              state.setHiddenFinds(new ArrayList<>());
              state.setTriggeredEvents(new ArrayList<>());
              spiritStateRepository.save(state);
              return state;
            });
  }
}
