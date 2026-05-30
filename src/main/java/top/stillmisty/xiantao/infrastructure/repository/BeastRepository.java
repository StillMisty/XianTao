package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.beast.entity.table.BeastTableDef.BEAST;

import com.mybatisflex.core.query.QueryWrapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.infrastructure.mapper.BeastMapper;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BeastRepository {

  private final BeastMapper mapper;

  public Optional<Beast> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  public Optional<Beast> findByIdForUpdate(Long id) {
    return Optional.ofNullable(
        mapper.selectOneByQuery(QueryWrapper.create().where(BEAST.ID.eq(id)).forUpdate()));
  }

  public List<Beast> findByUserId(Long userId) {
    return mapper.selectListByQuery(
        QueryWrapper.create().select().from(BEAST).where(BEAST.USER_ID.eq(userId)));
  }

  public List<Beast> findByUserId(Long userId, int limit, int offset) {
    return mapper.selectListByQuery(
        QueryWrapper.create()
            .select()
            .from(BEAST)
            .where(BEAST.USER_ID.eq(userId))
            .limit(limit)
            .offset(offset));
  }

  public List<Beast> findByFudiId(Long fudiId) {
    return mapper.selectListByQuery(
        QueryWrapper.create().select().from(BEAST).where(BEAST.FUDI_ID.eq(fudiId)));
  }

  public List<Beast> findDeployedByUserId(Long userId) {
    LocalDateTime now = TimeUtil.now();
    return mapper.selectListByQuery(
        QueryWrapper.create()
            .select()
            .from(BEAST)
            .where(BEAST.USER_ID.eq(userId))
            .and(BEAST.IS_DEPLOYED.eq(true))
            .and(BEAST.HP_CURRENT.gt(0))
            .and(BEAST.RECOVERY_UNTIL.lt(now).or(BEAST.RECOVERY_UNTIL.isNull())));
  }

  public List<Beast> findByUserIdAndIsDeployed(Long userId, boolean isDeployed) {
    return mapper.selectListByQuery(
        QueryWrapper.create()
            .select()
            .from(BEAST)
            .where(BEAST.USER_ID.eq(userId))
            .and(BEAST.IS_DEPLOYED.eq(isDeployed)));
  }

  public Beast save(Beast beast) {
    mapper.insertOrUpdateSelective(beast);
    return beast;
  }

  public List<Beast> saveAll(List<Beast> beasts) {
    if (beasts == null || beasts.isEmpty()) return List.of();

    // 分离新记录和已存在的记录
    List<Beast> toInsert = new java.util.ArrayList<>();
    List<Beast> toUpdate = new java.util.ArrayList<>();

    for (Beast beast : beasts) {
      if (beast.getId() == null) {
        toInsert.add(beast);
      } else {
        toUpdate.add(beast);
      }
    }

    // 批量插入新记录
    if (!toInsert.isEmpty()) {
      mapper.insertBatch(toInsert);
    }

    // 逐个更新已存在的记录（MyBatis-Flex 没有 updateBatch）
    for (Beast beast : toUpdate) {
      mapper.insertOrUpdateSelective(beast);
    }

    return beasts;
  }

  public void deleteById(Long id) {
    mapper.deleteById(id);
  }

  public long countByUserId(Long userId) {
    return mapper.selectCountByQuery(
        QueryWrapper.create().select().from(BEAST).where(BEAST.USER_ID.eq(userId)));
  }
}
