package top.stillmisty.xiantao.domain.event;

import top.stillmisty.xiantao.domain.event.vo.FortuneVO;
import top.stillmisty.xiantao.domain.map.entity.MapNode;

/** 事件上下文键常量 — 所有进出 SubEventEffectExecutor 的 context Map 键均在此定义 */
public final class EventContextKeys {

  private EventContextKeys() {}

  /** 当前所在地图实体 */
  public static final ContextKey<MapNode> MAP_NODE = new ContextKey<>("mapNode");

  /** 当前地图名称（由 MapNode.name 取值） */
  public static final ContextKey<String> MAP_NAME = new ContextKey<>("mapName");

  /** 悬赏名称 */
  public static final ContextKey<String> BOUNTY_NAME = new ContextKey<>("bountyName");

  /** 悬赏灵石奖励（可变数组，MultiplyBountyRewardEffect 通过此键原地修改奖励） */
  public static final ContextKey<long[]> BOUNTY_REWARD = new ContextKey<>("bountyReward");

  /** 当日运势（财运/机缘/气运 + 点评） */
  public static final ContextKey<FortuneVO> FORTUNE = new ContextKey<>("fortune");
}
