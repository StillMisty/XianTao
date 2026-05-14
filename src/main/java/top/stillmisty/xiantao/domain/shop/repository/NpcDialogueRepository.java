package top.stillmisty.xiantao.domain.shop.repository;

import java.util.List;
import top.stillmisty.xiantao.domain.shop.entity.NpcDialogue;
import top.stillmisty.xiantao.domain.shop.enums.NpcType;

public interface NpcDialogueRepository {

  NpcDialogue save(NpcDialogue dialogue);

  List<NpcDialogue> findByUserIdAndNpcTypeAndNpcIdOrderByCreateTimeDesc(
      Long userId, NpcType npcType, Long npcId, int limit);

  void deleteOldEntries(Long userId, NpcType npcType, Long npcId, int keepCount);
}
