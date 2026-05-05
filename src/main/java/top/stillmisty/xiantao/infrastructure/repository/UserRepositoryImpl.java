package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.infrastructure.mapper.UserMapper;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
  private final UserMapper userMapper;

  @Override
  public User save(User user) {
    userMapper.insertOrUpdateSelective(user);
    return user;
  }

  @Override
  public Optional<User> findById(Long id) {
    return Optional.ofNullable(userMapper.selectOneById(id));
  }

  @Override
  public List<User> findByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return List.of();
    return userMapper.selectListByIds(ids);
  }

  @Override
  public boolean existsByNickname(String nickname) {
    QueryWrapper query = new QueryWrapper().eq(User::getNickname, nickname);
    return userMapper.selectCountByQuery(query) > 0;
  }

  @Override
  public Optional<User> findByNickname(String nickname) {
    QueryWrapper query = new QueryWrapper().eq(User::getNickname, nickname);
    return Optional.ofNullable(userMapper.selectOneByQuery(query));
  }

  @Override
  public List<User> findTopByLevel(int limit) {
    QueryWrapper query =
        new QueryWrapper().orderBy(User::getLevel, true).orderBy(User::getExp, true).limit(limit);
    return userMapper.selectListByQuery(query);
  }

  @Override
  public List<User> findTopBySpiritStones(int limit) {
    QueryWrapper query =
        new QueryWrapper()
            .orderBy(User::getSpiritStones, true)
            .orderBy(User::getLevel, true)
            .limit(limit);
    return userMapper.selectListByQuery(query);
  }
}
