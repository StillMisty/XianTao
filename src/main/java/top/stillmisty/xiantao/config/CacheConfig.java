package top.stillmisty.xiantao.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

  private static final long DEFAULT_TTL_MINUTES = 5;
  private static final long STATIC_TTL_MINUTES = 30;
  private static final long VOLATILE_TTL_MINUTES = 1;
  private static final long DAY_TTL_MINUTES = 1440;

  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager manager = new SimpleCacheManager();
    manager.setCaches(
        Arrays.asList(
            // expireAfterWrite — 固定 TTL，适合有明确刷新周期的数据
            writeCache("fortunes", DAY_TTL_MINUTES, 200),
            writeCache("map_data", STATIC_TTL_MINUTES, 50),
            writeCache("dungeon_list", VOLATILE_TTL_MINUTES, 50),
            writeCache("team_status", VOLATILE_TTL_MINUTES, 200),
            writeCache("leaderboard", DEFAULT_TTL_MINUTES, 50),
            writeCache("itemTemplate", STATIC_TTL_MINUTES, 2000),
            writeCache("mapNodes", STATIC_TTL_MINUTES, 200),
            writeCache("shop_locations", STATIC_TTL_MINUTES, 50),
            writeCache("bounties", DEFAULT_TTL_MINUTES, 100),
            // expireAfterAccess — 读取延长 TTL，适合读多写少、冷热分明的数据
            accessCache("userAuth", STATIC_TTL_MINUTES, 5000),
            accessCache("player_inventory", DEFAULT_TTL_MINUTES, 200),
            accessCache("player_equipment", DEFAULT_TTL_MINUTES, 200),
            accessCache("player_skills", DEFAULT_TTL_MINUTES, 200),
            accessCache("player_view", DEFAULT_TTL_MINUTES, 200),
            accessCache("shop_products", DEFAULT_TTL_MINUTES, 100),
            accessCache("shop_player_items", DEFAULT_TTL_MINUTES, 200),
            accessCache("sect_overview", DEFAULT_TTL_MINUTES, 100),
            accessCache("sect_buildings", DEFAULT_TTL_MINUTES, 100),
            accessCache("sect_shared_skills", DEFAULT_TTL_MINUTES, 100),
            accessCache("sect_shop", DEFAULT_TTL_MINUTES, 100),
            accessCache("dao_protection", DEFAULT_TTL_MINUTES, 100)));
    return manager;
  }

  private static CaffeineCache writeCache(String name, long ttlMinutes, int maxSize) {
    return new CaffeineCache(
        name,
        Caffeine.newBuilder()
            .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
            .maximumSize(maxSize)
            .recordStats()
            .build());
  }

  private static CaffeineCache accessCache(String name, long ttlMinutes, int maxSize) {
    return new CaffeineCache(
        name,
        Caffeine.newBuilder()
            .expireAfterAccess(ttlMinutes, TimeUnit.MINUTES)
            .maximumSize(maxSize)
            .recordStats()
            .build());
  }
}
