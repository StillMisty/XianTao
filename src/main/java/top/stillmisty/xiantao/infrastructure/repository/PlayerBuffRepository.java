package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.pill.enums.PlayerBuffType;
import top.stillmisty.xiantao.infrastructure.mapper.PlayerBuffMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PlayerBuffRepository {

  private final PlayerBuffMapper mapper;

  public Optional<PlayerBuff> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  public List<PlayerBuff> findActiveByUserId(Long userId) {
    return mapper.selectActiveByUserId(userId);
  }

  public List<PlayerBuff> findActiveByUserIdAndType(Long userId, PlayerBuffType buffType) {
    return mapper.selectActiveByUserIdAndType(userId, buffType.getCode());
  }

  public int countActiveByUserIdAndType(Long userId, PlayerBuffType buffType) {
    return mapper.countActiveByUserIdAndType(userId, buffType.getCode());
  }

  /** 原子条件插入：仅当同类活跃 buff 数低于上限时写入。返回 0 表示已达堆叠上限。 */
  public int insertIfBelowStackLimit(
      Long userId,
      PlayerBuffType buffType,
      int value,
      java.time.LocalDateTime expiresAt,
      int maxStack) {
    return mapper.insertIfBelowStackLimit(userId, buffType.getCode(), value, expiresAt, maxStack);
  }

  public PlayerBuff save(PlayerBuff buff) {
    mapper.insertOrUpdateSelective(buff);
    return buff;
  }

  public void deleteById(Long id) {
    mapper.deleteById(id);
  }

  public void deleteByUserIdAndType(Long userId, PlayerBuffType buffType) {
    mapper.deleteByUserIdAndType(userId, buffType.getCode());
  }

  public void deleteExpired() {
    mapper.deleteExpired();
  }

  public void deleteExpiredByUserId(Long userId) {
    mapper.deleteExpiredByUserId(userId);
  }
}
