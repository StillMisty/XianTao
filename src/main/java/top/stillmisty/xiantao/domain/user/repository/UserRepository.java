package top.stillmisty.xiantao.domain.user.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.user.entity.User;

public interface UserRepository {
  User save(User user);

  Optional<User> findById(Long id);

  /** 使用行锁加载用户，用于并发敏感操作（突破、状态切换等） */
  Optional<User> findByIdForUpdate(Long id);

  List<User> findByIds(List<Long> ids);

  /** 检查昵称是否已存在 */
  boolean existsByNickname(String nickname);

  /** 根据昵称查找用户 */
  Optional<User> findByNickname(String nickname);

  /** 获取修为排行榜（按等级降序） */
  List<User> findTopByLevel(int limit);

  /** 获取财富排行榜（按灵石降序） */
  List<User> findTopBySpiritStones(int limit);

  /** 原子扣除灵石（灵石不足时返回0，成功返回1） */
  int deductSpiritStonesIfEnough(Long userId, long cost);

  /** 原子增加灵石 */
  int addSpiritStonesAtomically(Long userId, long amount);
}
