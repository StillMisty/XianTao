package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.map.vo.TrainingRewardVO;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 历练服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrainingService {

    private final UserRepository userRepository;
    private final MapNodeRepository mapNodeRepository;

    // 历练基础收益配置
    private static final long BASE_COINS_PER_MINUTE = 10;
    private static final long BASE_SPIRIT_STONES_PER_HOUR = 5;

    /**
     * 计算历练奖励（懒加载评估）
     *
     * @param userId 用户 ID
     * @return 历练奖励
     */
    public TrainingRewardVO calculateTrainingRewards(Long userId) {
        // 获取用户
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return TrainingRewardVO.builder()
                    .userId(userId)
                    .summary("用户不存在")
                    .build();
        }

        User user = userOpt.get();

        // 检查是否有历练时间
        if (user.getTrainingStartTime() == null) {
            return TrainingRewardVO.builder()
                    .userId(userId)
                    .mapId(user.getLocationId())
                    .summary("您还没有开始历练")
                    .build();
        }

        // 计算历练时长
        long minutesTraining = Duration.between(user.getTrainingStartTime(), LocalDateTime.now()).toMinutes();
        if (minutesTraining <= 0) {
            return TrainingRewardVO.builder()
                    .userId(userId)
                    .mapId(user.getLocationId())
                    .summary("历练时间不足")
                    .build();
        }

        // 获取当前地图
        Optional<MapNode> mapOpt = mapNodeRepository.findById(user.getLocationId());
        if (mapOpt.isEmpty()) {
            return TrainingRewardVO.builder()
                    .userId(userId)
                    .summary("当前地图不存在")
                    .build();
        }

        MapNode mapNode = mapOpt.get();

        // 计算效率倍率（基于敏捷）
        double efficiencyMultiplier = calculateEfficiencyMultiplier(user.getStatAgi());

        // 计算奖励
        long coins = calculateCoinsReward(minutesTraining, efficiencyMultiplier);
        long spiritStones = calculateSpiritStonesReward(minutesTraining);

        // 计算物品奖励
        List<Map<String, Object>> items = calculateItemsReward(minutesTraining, efficiencyMultiplier, mapNode);

        // 生成摘要
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("历练时长: %d 分钟\n", minutesTraining));
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

        log.info("用户 {} 历练奖励计算 - 时长: {} 分钟, 铜币: {}, 灵石: {}, 物品数: {}",
                userId, minutesTraining, coins, spiritStones, items.size());

        return TrainingRewardVO.builder()
                .userId(userId)
                .mapId(mapNode.getId())
                .mapName(mapNode.getName())
                .durationMinutes(minutesTraining)
                .efficiencyMultiplier(efficiencyMultiplier)
                .coins(coins)
                .spiritStones(spiritStones)
                .items(items)
                .summary(summary.toString())
                .build();
    }

    /**
     * 应用历练奖励
     *
     * @param userId  用户 ID
     * @param rewards 奖励
     * @return 是否成功
     */
    public boolean applyTrainingRewards(Long userId, TrainingRewardVO rewards) {
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

        // 重置历练时间
        user.setTrainingStartTime(LocalDateTime.now());
        userRepository.save(user);

        log.info("用户 {} 的历练奖励已应用", userId);
        return true;
    }

    /**
     * 结束历练并应用奖励
     *
     * @param userId 用户 ID
     * @return 历练结算结果
     */
    public TrainingRewardVO endTraining(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return TrainingRewardVO.builder()
                    .userId(userId)
                    .summary("用户不存在")
                    .build();
        }

        User user = userOpt.get();

        // 检查是否在历练中
        if (user.getTrainingStartTime() == null) {
            return TrainingRewardVO.builder()
                    .userId(userId)
                    .mapId(user.getLocationId())
                    .summary("您当前没有在历练")
                    .build();
        }

        // 计算奖励
        TrainingRewardVO rewards = calculateTrainingRewards(userId);

        // 应用奖励
        if (rewards.getCoins() != null && rewards.getCoins() > 0 ||
                rewards.getSpiritStones() != null && rewards.getSpiritStones() > 0 ||
                rewards.getItems() != null && !rewards.getItems().isEmpty()) {

            boolean applied = applyTrainingRewards(userId, rewards);
            if (applied) {
                log.info("用户 {} 结束历练并应用奖励", userId);
                return rewards;
            }
        }

        return rewards;
    }

    /**
     * 开始历练
     *
     * @param userId 用户 ID
     * @return 是否成功
     */
    public boolean startTraining(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        // 设置历练开始时间
        user.setTrainingStartTime(LocalDateTime.now());
        userRepository.save(user);

        log.info("用户 {} 开始历练", userId);
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
    private long calculateCoinsReward(long minutesTraining, double efficiencyMultiplier) {
        return (long) (BASE_COINS_PER_MINUTE * minutesTraining * efficiencyMultiplier);
    }

    /**
     * 计算灵石奖励
     */
    private long calculateSpiritStonesReward(long minutesTraining) {
        long hoursTraining = minutesTraining / 60;
        return hoursTraining * BASE_SPIRIT_STONES_PER_HOUR;
    }

    /**
     * 计算物品奖励
     */
    private List<Map<String, Object>> calculateItemsReward(long minutesTraining, double efficiencyMultiplier, MapNode mapNode) {
        List<Map<String, Object>> items = new ArrayList<>();

        // 根据历练时长和效率倍率计算物品掉落次数
        int dropChances = (int) (minutesTraining / 10 * efficiencyMultiplier);

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
