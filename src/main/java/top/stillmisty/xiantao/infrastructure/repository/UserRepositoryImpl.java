package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.infrastructure.mapper.UserMapper;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        userMapper.insertOrUpdate(user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(userMapper.selectOneById(id));
    }

    @Override
    public boolean existsByNickname(String nickname) {
        QueryWrapper query = new QueryWrapper()
                .eq(User::getNickname, nickname);
        return userMapper.selectCountByQuery(query) > 0;
    }
}
