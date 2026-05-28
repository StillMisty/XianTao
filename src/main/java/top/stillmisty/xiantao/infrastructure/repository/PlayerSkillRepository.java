package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.skill.entity.table.PlayerSkillTableDef.PLAYER_SKILL;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.skill.entity.PlayerSkill;
import top.stillmisty.xiantao.infrastructure.mapper.PlayerSkillMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PlayerSkillRepository {

  private final PlayerSkillMapper mapper;

  public List<PlayerSkill> findByUserId(Long userId) {
    return mapper.selectListByQuery(
        QueryWrapper.create().select().from(PLAYER_SKILL).where(PLAYER_SKILL.USER_ID.eq(userId)));
  }

  public List<PlayerSkill> findEquippedByUserId(Long userId) {
    return mapper.selectListByQuery(
        QueryWrapper.create()
            .select()
            .from(PLAYER_SKILL)
            .where(PLAYER_SKILL.USER_ID.eq(userId))
            .and(PLAYER_SKILL.IS_EQUIPPED.eq(true)));
  }

  public Optional<PlayerSkill> findByUserIdAndSkillId(Long userId, Long skillId) {
    return Optional.ofNullable(
        mapper.selectOneByQuery(
            QueryWrapper.create()
                .select()
                .from(PLAYER_SKILL)
                .where(PLAYER_SKILL.USER_ID.eq(userId))
                .and(PLAYER_SKILL.SKILL_ID.eq(skillId))));
  }

  public PlayerSkill save(PlayerSkill playerSkill) {
    mapper.insertOrUpdateSelective(playerSkill);
    return playerSkill;
  }

  public void deleteById(Long id) {
    mapper.deleteById(id);
  }

  public void deleteByUserIdAndSourceSectId(Long userId, Long sourceSectId) {
    mapper.deleteByQuery(
        QueryWrapper.create()
            .where(PLAYER_SKILL.USER_ID.eq(userId))
            .and(PLAYER_SKILL.SOURCE_SECT_ID.eq(sourceSectId)));
  }
}
