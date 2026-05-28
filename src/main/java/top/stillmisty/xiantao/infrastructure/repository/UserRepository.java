package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.mapper.UserMapper;

@Repository
@RequiredArgsConstructor
public class UserRepository {
  private final UserMapper userMapper;

  public User save(User user) {
    userMapper.insertOrUpdateSelective(user);
    return user;
  }

  public Optional<User> findById(Long id) {
    return Optional.ofNullable(userMapper.selectOneById(id));
  }

  public Optional<User> findByIdForUpdate(Long id) {
    return Optional.ofNullable(userMapper.selectByIdForUpdate(id));
  }

  public List<User> findByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return List.of();
    return userMapper.selectListByIds(ids);
  }

  public boolean existsByNickname(String nickname) {
    QueryWrapper query = new QueryWrapper().eq(User::getNickname, nickname);
    return userMapper.selectCountByQuery(query) > 0;
  }

  public Optional<User> findByNickname(String nickname) {
    QueryWrapper query = new QueryWrapper().eq(User::getNickname, nickname);
    return Optional.ofNullable(userMapper.selectOneByQuery(query));
  }

  public List<User> findTopByLevel(int limit) {
    QueryWrapper query =
        new QueryWrapper().orderBy(User::getLevel, true).orderBy(User::getExp, true).limit(limit);
    return userMapper.selectListByQuery(query);
  }

  public List<User> findTopBySpiritStones(int limit) {
    QueryWrapper query =
        new QueryWrapper()
            .orderBy(User::getSpiritStones, true)
            .orderBy(User::getLevel, true)
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
      LocalDateTime activityStartTime,
      Long activityTargetId) {
    userMapper.startActivity(userId, status, activityType, activityStartTime, activityTargetId);
  }

  public void updateHpStatus(
      Long userId, int hpCurrent, String status, LocalDateTime dyingStartTime) {
    userMapper.updateHpStatus(userId, hpCurrent, status, dyingStartTime);
  }

  public void completeTraining(
      Long userId,
      int hpCurrent,
      long exp,
      String status,
      LocalDateTime dyingStartTime,
      String activityType,
      LocalDateTime activityStartTime,
      Long activityTargetId) {
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
