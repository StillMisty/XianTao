package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.infrastructure.mapper.UserMapper;

import java.util.Optional;

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
    public boolean existsByNickname(String nickname) {
        QueryWrapper query = new QueryWrapper()
                .eq(User::getNickname, nickname);
        return userMapper.selectCountByQuery(query) > 0;
    }
}
