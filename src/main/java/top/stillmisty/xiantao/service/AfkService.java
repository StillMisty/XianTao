package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.map.vo.AfkRewardVO;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AFK 服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AfkService {

    private final UserRepository userRepository;
    private final MapNodeRepository mapNodeRepository;

    // 挂机基础收益配置
    private static final long BASE_COINS_PER_MINUTE = 10;
    private static final long BASE_SPIRIT_STONES_PER_HOUR = 5;

    /**
     * 计算 AFK 奖励（懒加载评估）
     *
     * @param userId 用户 ID
     * @return AFK 奖励
     */
    public AfkRewardVO calculateAfkRewards(Long userId) {
        // 获取用户
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return AfkRewardVO.builder()
                    .userId(userId)
                    .summary("用户不存在")
                    .build();
        }

        User user = userOpt.get();

        // 检查是否有挂机时间
        if (user.getAfkStartTime() == null) {
            return AfkRewardVO.builder()
                    .userId(userId)
                    .mapId(user.getLocationId())
                    .summary("您还没有开始挂机")
                    .build();
        }

        // 计算挂机时长
        long minutesAfk = Duration.between(user.getAfkStartTime(), LocalDateTime.now()).toMinutes();
        if (minutesAfk <= 0) {
            return AfkRewardVO.builder()
                    .userId(userId)
                    .mapId(user.getLocationId())
                    .summary("挂机时间不足")
                    .build();
        }

        // 获取当前地图
        Optional<MapNode> mapOpt = mapNodeRepository.findById(user.getLocationId());
        if (mapOpt.isEmpty()) {
            return AfkRewardVO.builder()
                    .userId(userId)
                    .summary("当前地图不存在")
                    .build();
        }

        MapNode mapNode = mapOpt.get();

        // 计算效率倍率（基于敏捷）
        double efficiencyMultiplier = calculateEfficiencyMultiplier(user.getStatAgi());

        // 计算奖励
        long coins = calculateCoinsReward(minutesAfk, efficiencyMultiplier);
        long spiritStones = calculateSpiritStonesReward(minutesAfk);

        // 计算物品奖励
        List<Map<String, Object>> items = calculateItemsReward(minutesAfk, efficiencyMultiplier, mapNode);

        // 生成摘要
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("挂机时长: %d 分钟\n", minutesAfk));
        if (coins > 0) {
            summary.append(String.format("铜币: +%d\n", coins));
        }
        if (spiritStones > 0) {
            summary.append(String.format("灵石: +%d\n", spiritStones));
        }
        if (!items.isEmpty()) {
            summary.append("物品: ");
            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> item = items.get(i);
                String name = (String) item.get("name");
                Integer quantity = (Integer) item.get("quantity");
                summary.append(String.format("%s x%d", name, quantity));
                if (i < items.size() - 1) {
                    summary.append(", ");
                }
            }
        }

        log.info("用户 {} 挂机奖励计算 - 时长: {} 分钟, 铜币: {}, 灵石: {}, 物品数: {}",
                userId, minutesAfk, coins, spiritStones, items.size());

        return AfkRewardVO.builder()
                .userId(userId)
                .mapId(mapNode.getId())
                .mapName(mapNode.getName())
                .durationMinutes(minutesAfk)
                .efficiencyMultiplier(efficiencyMultiplier)
                .coins(coins)
                .spiritStones(spiritStones)
                .items(items)
                .summary(summary.toString())
                .build();
    }

    /**
     * 应用 AFK 奖励
     *
     * @param userId  用户 ID
     * @param rewards 奖励
     * @return 是否成功
     */
    public boolean applyAfkRewards(Long userId, AfkRewardVO rewards) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        // 添加货币
        if (rewards.getCoins() != null && rewards.getCoins() > 0) {
            user.setCoins(user.getCoins() + rewards.getCoins());
        }
        if (rewards.getSpiritStones() != null && rewards.getSpiritStones() > 0) {
            user.setSpiritStones(user.getSpiritStones() + rewards.getSpiritStones());
        }

        // TODO: 添加物品到背包（需要调用 ItemService）
        // 这里暂时只记录日志
        if (rewards.getItems() != null && !rewards.getItems().isEmpty()) {
            for (Map<String, Object> item : rewards.getItems()) {
                log.info("添加物品到用户 {} 的背包: {} x{}", userId, item.get("name"), item.get("quantity"));
            }
        }

        // 重置挂机时间
        user.setAfkStartTime(LocalDateTime.now());
        userRepository.save(user);

        log.info("用户 {} 的挂机奖励已应用", userId);
        return true;
    }

    /**
     * 计算效率倍率（基于敏捷）
     */
    private double calculateEfficiencyMultiplier(int agility) {
        // 基础倍率 1.0，每 10 点敏捷增加 0.1 倍率
        return 1.0 + (agility * 0.01);
    }

    /**
     * 计算铜币奖励
     */
    private long calculateCoinsReward(long minutesAfk, double efficiencyMultiplier) {
        return (long) (BASE_COINS_PER_MINUTE * minutesAfk * efficiencyMultiplier);
    }

    /**
     * 计算灵石奖励
     */
    private long calculateSpiritStonesReward(long minutesAfk) {
        long hoursAfk = minutesAfk / 60;
        return hoursAfk * BASE_SPIRIT_STONES_PER_HOUR;
    }

    /**
     * 计算物品奖励
     */
    private List<Map<String, Object>> calculateItemsReward(long minutesAfk, double efficiencyMultiplier, MapNode mapNode) {
        List<Map<String, Object>> items = new ArrayList<>();

        // 根据挂机时长和效率倍率计算物品掉落次数
        int dropChances = (int) (minutesAfk / 10 * efficiencyMultiplier);

        for (int i = 0; i < dropChances; i++) {
            Map<String, Object> specialty = mapNode.getRandomSpecialty();
            if (specialty != null) {
                // 随机数量 1-3
                int quantity = (int) (Math.random() * 3) + 1;

                // 检查是否已存在该物品
                boolean exists = false;
                for (Map<String, Object> existing : items) {
                    if (existing.get("name").equals(specialty.get("name"))) {
                        existing.put("quantity", (Integer) existing.get("quantity") + quantity);
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("name", specialty.get("name"));
                    item.put("templateId", specialty.get("templateId"));
                    item.put("quantity", quantity);
                    items.add(item);
                }
            }
        }

        return items;
    }
}
