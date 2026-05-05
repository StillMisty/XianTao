package top.stillmisty.xiantao.domain.pill.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;

/** 玩家 Buff 仓储接口 */
public interface PlayerBuffRepository {

  /** 根据ID查找Buff */
  Optional<PlayerBuff> findById(Long id);

  /** 根据用户ID查找所有未过期的Buff */
  List<PlayerBuff> findActiveByUserId(Long userId);

  /** 根据用户ID和buff类型查找未过期的Buff */
  List<PlayerBuff> findActiveByUserIdAndType(Long userId, String buffType);

  /** 保存Buff */
  PlayerBuff save(PlayerBuff buff);

  /** 根据ID删除Buff */
  void deleteById(Long id);

  /** 根据用户ID和buff类型删除所有Buff（突破后清除） */
  void deleteByUserIdAndType(Long userId, String buffType);

  /** 删除所有过期的Buff */
  void deleteExpired();

  /** 删除指定用户的所有过期Buff */
  void deleteExpiredByUserId(Long userId);
}
