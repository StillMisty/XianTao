package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import top.stillmisty.xiantao.domain.analytics.entity.AnalyticsEvent;

@Mapper
public interface AnalyticsEventMapper extends BaseMapper<AnalyticsEvent> {

  /** 玩家每日快照：每小时 upsert，当日最后一次写入为该日终态。 */
  @Insert(
      """
      INSERT INTO player_daily_snapshot(
          snapshot_date, user_id, level, exp, spirit_stones, hp_current, status, location_id)
      SELECT CURRENT_DATE, id, level, exp, spirit_stones, hp_current, status, location_id
      FROM player
      ON CONFLICT (snapshot_date, user_id) DO UPDATE SET
          level = EXCLUDED.level,
          exp = EXCLUDED.exp,
          spirit_stones = EXCLUDED.spirit_stones,
          hp_current = EXCLUDED.hp_current,
          status = EXCLUDED.status,
          location_id = EXCLUDED.location_id
      """)
  int snapshotAllPlayers();
}
