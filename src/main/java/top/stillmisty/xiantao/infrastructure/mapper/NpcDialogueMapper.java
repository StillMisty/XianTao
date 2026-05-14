package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.stillmisty.xiantao.domain.shop.entity.NpcDialogue;

@Mapper
public interface NpcDialogueMapper extends BaseMapper<NpcDialogue> {

  @Delete(
      """
      DELETE FROM xt_npc_dialogue
      WHERE user_id = #{userId} AND npc_type = #{npcType} AND npc_id = #{npcId}
      AND id NOT IN (
        SELECT id FROM xt_npc_dialogue
        WHERE user_id = #{userId} AND npc_type = #{npcType} AND npc_id = #{npcId}
        ORDER BY create_time DESC
        LIMIT #{keepCount}
      )
      """)
  int deleteOldEntries(
      @Param("userId") Long userId,
      @Param("npcType") String npcType,
      @Param("npcId") Long npcId,
      @Param("keepCount") int keepCount);
}
