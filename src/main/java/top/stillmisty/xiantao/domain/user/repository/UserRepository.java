package top.stillmisty.xiantao.domain.user.repository;

import top.stillmisty.xiantao.domain.user.entity.User;

import java.util.Optional;

public interface UserRepository {

    /**
     * 保存用户
     */
    User save(User user);

    /**
     * 根据ID查找用户
     */
    Optional<User> findById(Long id);

    /**
     * 检查昵称是否已存在
     */
    boolean existsByNickname(String nickname);
}
