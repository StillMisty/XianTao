package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.user.entity.table.PlayerTableDef.PLAYER;

import com.mybatisflex.core.query.QueryWrapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.mapper.UserMapper;

@Repository
@RequiredArgsConstructor
public class UserRepository {
  private final UserMapper userMapper;

  public Player save(Player user) {
    userMapper.insertOrUpdateSelective(user);
    return user;
  }

  public Optional<Player> findById(Long id) {
    return Optional.ofNullable(userMapper.selectOneById(id));
  }

  public Optional<Player> findByIdForUpdate(Long id) {
    return Optional.ofNullable(userMapper.selectByIdForUpdate(id));
  }

  public List<Player> findByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return List.of();
    return userMapper.selectListByIds(ids);
  }

  public boolean existsByNickname(String nickname) {
    QueryWrapper query = QueryWrapper.create().where(PLAYER.NICKNAME.eq(nickname));
    return userMapper.selectCountByQuery(query) > 0;
  }

  public Optional<Player> findByNickname(String nickname) {
    QueryWrapper query = QueryWrapper.create().where(PLAYER.NICKNAME.eq(nickname));
    return Optional.ofNullable(userMapper.selectOneByQuery(query));
  }

  public List<Player> findTopByLevel(int limit) {
    QueryWrapper query =
        QueryWrapper.create().orderBy(PLAYER.LEVEL.asc()).orderBy(PLAYER.EXP.asc()).limit(limit);
    return userMapper.selectListByQuery(query);
  }

  public List<Player> findTopBySpiritStones(int limit) {
    QueryWrapper query =
        QueryWrapper.create()
            .orderBy(PLAYER.SPIRIT_STONES.asc())
            .orderBy(PLAYER.LEVEL.asc())
            .limit(limit);
    return userMapper.selectListByQuery(query);
  }

  public int deductSpiritStonesIfEnough(Long userId, long cost) {
    return userMapper.deductSpiritStonesIfEnough(userId, cost);
  }

  public int addSpiritStonesAtomically(Long userId, long amount) {
    return userMapper.addSpiritStonesAtomically(userId, amount);
  }

  public void clearActivity(Long userId) {
    userMapper.clearActivity(userId);
  }

  public void startActivity(
      Long userId,
      String status,
      String activityType,
      @Nullable LocalDateTime activityStartTime,
      @Nullable Long activityTargetId) {
    userMapper.startActivity(userId, status, activityType, activityStartTime, activityTargetId);
  }

  public void updateHpStatus(
      Long userId, int hpCurrent, String status, @Nullable LocalDateTime dyingStartTime) {
    userMapper.updateHpStatus(userId, hpCurrent, status, dyingStartTime);
  }

  public void completeTraining(
      Long userId,
      int hpCurrent,
      long exp,
      String status,
      @Nullable LocalDateTime dyingStartTime,
      @Nullable String activityType,
      @Nullable LocalDateTime activityStartTime,
      @Nullable Long activityTargetId) {
    userMapper.completeTraining(
        userId,
        hpCurrent,
        exp,
        status,
        dyingStartTime,
        activityType,
        activityStartTime,
        activityTargetId);
  }

  public void updateTrainingSettlement(
      Long userId, int hpCurrent, long exp, long lastSettlementMinute) {
    userMapper.updateTrainingSettlement(userId, hpCurrent, exp, lastSettlementMinute);
  }
}
