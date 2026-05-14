package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.shop.entity.NpcDialogue;
import top.stillmisty.xiantao.domain.shop.enums.NpcType;
import top.stillmisty.xiantao.domain.shop.repository.NpcDialogueRepository;
import top.stillmisty.xiantao.infrastructure.mapper.NpcDialogueMapper;

@Repository
@RequiredArgsConstructor
public class NpcDialogueRepositoryImpl implements NpcDialogueRepository {

  private final NpcDialogueMapper npcDialogueMapper;

  @Override
  public NpcDialogue save(NpcDialogue dialogue) {
    npcDialogueMapper.insertOrUpdateSelective(dialogue);
    return dialogue;
  }

  @Override
  public List<NpcDialogue> findByUserIdAndNpcTypeAndNpcIdOrderByCreateTimeDesc(
      Long userId, NpcType npcType, Long npcId, int limit) {
    return npcDialogueMapper.selectListByQuery(
        new QueryWrapper()
            .eq(NpcDialogue::getUserId, userId)
            .eq(NpcDialogue::getNpcType, npcType)
            .eq(NpcDialogue::getNpcId, npcId)
            .orderBy(NpcDialogue::getCreateTime, false)
            .limit(limit));
  }

  @Override
  public void deleteOldEntries(Long userId, NpcType npcType, Long npcId, int keepCount) {
    npcDialogueMapper.deleteOldEntries(userId, npcType.getCode(), npcId, keepCount);
  }
}
