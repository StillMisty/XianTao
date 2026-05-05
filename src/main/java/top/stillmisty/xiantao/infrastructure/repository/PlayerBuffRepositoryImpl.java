package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.pill.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.infrastructure.mapper.PlayerBuffMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PlayerBuffRepositoryImpl implements PlayerBuffRepository {

  private final PlayerBuffMapper mapper;

  @Override
  public PlayerBuff findById(Long id) {
    return mapper.selectOneById(id);
  }

  @Override
  public List<PlayerBuff> findActiveByUserId(Long userId) {
    return mapper.selectActiveByUserId(userId);
  }

  @Override
  public List<PlayerBuff> findActiveByUserIdAndType(Long userId, String buffType) {
    return mapper.selectActiveByUserIdAndType(userId, buffType);
  }

  @Override
  public PlayerBuff save(PlayerBuff buff) {
    mapper.insert(buff);
    return buff;
  }

  @Override
  public void deleteById(Long id) {
    mapper.deleteById(id);
  }

  @Override
  public void deleteByUserIdAndType(Long userId, String buffType) {
    mapper.deleteByUserIdAndType(userId, buffType);
  }

  @Override
  public void deleteExpired() {
    mapper.deleteExpired();
  }

  @Override
  public void deleteExpiredByUserId(Long userId) {
    mapper.deleteExpiredByUserId(userId);
  }
}
