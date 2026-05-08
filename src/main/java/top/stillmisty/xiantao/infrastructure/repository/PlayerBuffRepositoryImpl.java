package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.pill.enums.PlayerBuffType;
import top.stillmisty.xiantao.domain.pill.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.infrastructure.mapper.PlayerBuffMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PlayerBuffRepositoryImpl implements PlayerBuffRepository {

  private final PlayerBuffMapper mapper;

  @Override
  public Optional<PlayerBuff> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  @Override
  public List<PlayerBuff> findActiveByUserId(Long userId) {
    return mapper.selectActiveByUserId(userId);
  }

  @Override
  public List<PlayerBuff> findActiveByUserIdAndType(Long userId, PlayerBuffType buffType) {
    return mapper.selectActiveByUserIdAndType(userId, buffType.getCode());
  }

  @Override
  public PlayerBuff save(PlayerBuff buff) {
    mapper.insertOrUpdateSelective(buff);
    return buff;
  }

  @Override
  public void deleteById(Long id) {
    mapper.deleteById(id);
  }

  @Override
  public void deleteByUserIdAndType(Long userId, PlayerBuffType buffType) {
    mapper.deleteByUserIdAndType(userId, buffType.getCode());
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
