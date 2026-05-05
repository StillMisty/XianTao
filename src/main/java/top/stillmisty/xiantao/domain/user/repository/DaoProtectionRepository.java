package top.stillmisty.xiantao.domain.user.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.user.entity.DaoProtection;

/** 护道关系仓储接口 */
public interface DaoProtectionRepository {

  /** 保存护道关系 */
  DaoProtection save(DaoProtection protection);

  /** 根据ID查找护道关系 */
  Optional<DaoProtection> findById(Long id);

  /** 查找护道者正在护道的所有关系 */
  List<DaoProtection> findByProtectorId(Long protectorId);

  /** 查找被护道者的所有护道关系 */
  List<DaoProtection> findByProtegeId(Long protegeId);

  /** 查找特定的护道关系 */
  Optional<DaoProtection> findByProtectorAndProtege(Long protectorId, Long protegeId);

  /** 删除护道关系 */
  void deleteById(Long id);

  /** 删除特定被护道者的所有护道关系 */
  void deleteByProtegeId(Long protegeId);

  /** 统计护道者当前的护道数量 */
  long countByProtectorId(Long protectorId);
}
