package top.stillmisty.xiantao.domain.user.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.user.entity.User;

public interface UserRepository {

  /** 保存用户 */
  User save(User user);

  /** 根据ID查找用户 */
  Optional<User> findById(Long id);

  /** 根据ID列表批量查找用户 */
  List<User> findByIds(List<Long> ids);

  /** 检查昵称是否已存在 */
  boolean existsByNickname(String nickname);

  /** 根据昵称查找用户 */
  Optional<User> findByNickname(String nickname);

  /** 获取修为排行榜（按等级降序） */
  List<User> findTopByLevel(int limit);

  /** 获取财富排行榜（按灵石降序） */
  List<User> findTopBySpiritStones(int limit);
}
