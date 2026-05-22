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

@Configuration
@EnableCaching
public class CacheConfig {

  private static final long DEFAULT_TTL_MINUTES = 5;
  private static final long STATIC_TTL_MINUTES = 30;
  private static final long VOLATILE_TTL_MINUTES = 1;

  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager manager = new SimpleCacheManager();
    manager.setCaches(
        Arrays.asList(
            cache("player_inventory", VOLATILE_TTL_MINUTES, 200),
            cache("player_equipment", VOLATILE_TTL_MINUTES, 200),
            cache("player_skills", VOLATILE_TTL_MINUTES, 200),
            cache("player_status", VOLATILE_TTL_MINUTES, 200),
            cache("player_view", DEFAULT_TTL_MINUTES, 200),
            cache("fortunes", 1440, 200),
            cache("map_data", STATIC_TTL_MINUTES, 50),
            cache("dungeon_list", STATIC_TTL_MINUTES, 50),
            cache("bounties", DEFAULT_TTL_MINUTES, 100),
            cache("shop_products", DEFAULT_TTL_MINUTES, 100),
            cache("shop_locations", STATIC_TTL_MINUTES, 50),
            cache("shop_player_items", VOLATILE_TTL_MINUTES, 200),
            cache("sect_overview", DEFAULT_TTL_MINUTES, 100),
            cache("sect_buildings", DEFAULT_TTL_MINUTES, 100),
            cache("sect_shared_skills", DEFAULT_TTL_MINUTES, 100),
            cache("sect_shop", DEFAULT_TTL_MINUTES, 100),
            cache("team_status", VOLATILE_TTL_MINUTES, 200),
            cache("leaderboard", DEFAULT_TTL_MINUTES, 10),
            cache("dao_protection", DEFAULT_TTL_MINUTES, 100),
            cache("userAuth", DEFAULT_TTL_MINUTES, 5000),
            cache("itemTemplate", STATIC_TTL_MINUTES, 2000),
            cache("mapNodes", STATIC_TTL_MINUTES, 200)));
    return manager;
  }

  private static CaffeineCache cache(String name, long ttlMinutes, int maxSize) {
    return new CaffeineCache(
        name,
        Caffeine.newBuilder()
            .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
            .maximumSize(maxSize)
            .build());
  }
}
