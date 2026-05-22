package top.stillmisty.xiantao.service.player.state;

import top.stillmisty.xiantao.domain.user.entity.User;

/** 用户状态处理器 — 每个实现负责一种需要自动结算的运行时状态（旅行、HP恢复、历练、运势等）。 */
public interface StateHandler {

  /** 尝试结算用户状态。返回 true 表示修改了用户对象。 */
  boolean tryResolve(User user);
}
