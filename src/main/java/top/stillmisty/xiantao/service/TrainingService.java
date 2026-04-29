package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.map.vo.TrainingRewardVO;
import top.stillmisty.xiantao.domain.map.vo.TrainingStartResult;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 历练服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrainingService {

    // 历练基础收益配置
    private static final long BASE_EXP_PER_MINUTE = 2;
    private final UserRepository userRepository;
    private final MapNodeRepository mapNodeRepository;
    private final AuthenticationService authService;
    private final ItemService itemService;
    private final ItemTemplateRepository itemTemplateRepository;

    // ===================== 公开 API（含认证） =====================

    public ServiceResult<TrainingStartResult> startTraining(PlatformType platform, String openId) {
        var auth = authService.authenticateAndValidateStatus(platform, openId, UserStatus.IDLE);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        return new ServiceResult.Success<>(startTraining(auth.userId()));
    }

    public ServiceResult<TrainingRewardVO> endTraining(PlatformType platform, String openId) {
        var auth = authService.authenticateAndValidateStatus(platform, openId, UserStatus.EXERCISING);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        return new ServiceResult.Success<>(endTraining(auth.userId()));
    }

    // ===================== 内部 API（需预先完成认证） =====================

    /**
     * 计算历练奖励（懒加载评估）
     *
     * @param userId 用户 ID
     * @return 历练奖励
     */
    public TrainingRewardVO calculateTrainingRewards(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

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

        // 计算经验
        long expGained = (long) (BASE_EXP_PER_MINUTE * minutesTraining * efficiencyMultiplier);

        // 计算物品奖励（仅基础材料）
        List<Map<String, Object>> items = calculateItemsReward(minutesTraining, efficiencyMultiplier, mapNode);

        // 生成摘要
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("历练时长: %d 分钟\n", minutesTraining));
        if (expGained > 0) {
            summary.append(String.format("经验: +%d\n", expGained));
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

        log.info(
                "用户 {} 历练奖励计算 - 时长: {} 分钟, 经验: {}, 物品数: {}",
                userId, minutesTraining, expGained, items.size()
        );

        return TrainingRewardVO.builder()
                .userId(userId)
                .mapId(mapNode.getId())
                .mapName(mapNode.getName())
                .durationMinutes(minutesTraining)
                .efficiencyMultiplier(efficiencyMultiplier)
                .exp(expGained)
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
        User user = userRepository.findById(userId).orElseThrow();

        // 添加经验
        if (rewards.getExp() != null && rewards.getExp() > 0) {
            user.addExp(rewards.getExp());
        }

        // 添加物品到背包
        if (rewards.getItems() != null && !rewards.getItems().isEmpty()) {
            for (Map<String, Object> item : rewards.getItems()) {
                String name = (String) item.get("name");
                Long templateId = toLong(item.get("templateId"));
                int quantity = ((Number) item.get("quantity")).intValue();
                ItemType itemType = itemTemplateRepository.findById(templateId)
                        .map(ItemTemplate::getType)
                        .orElse(ItemType.MATERIAL);
                itemService.addStackableItem(userId, templateId, itemType, name, quantity);
            }
        }

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
        User user = userRepository.findById(userId).orElseThrow();

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

        // 应用经验
        if (rewards.getExp() != null && rewards.getExp() > 0) {
            user.addExp(rewards.getExp());
        }

        // 添加物品到背包
        if (rewards.getItems() != null && !rewards.getItems().isEmpty()) {
            for (Map<String, Object> item : rewards.getItems()) {
                String name = (String) item.get("name");
                Long templateId = toLong(item.get("templateId"));
                int quantity = ((Number) item.get("quantity")).intValue();
                ItemType itemType = itemTemplateRepository.findById(templateId)
                        .map(ItemTemplate::getType)
                        .orElse(ItemType.MATERIAL);
                itemService.addStackableItem(userId, templateId, itemType, name, quantity);
            }
        }

        // 结束历练：恢复空闲状态，清除历练时间
        user.setStatus(UserStatus.IDLE);
        user.setTrainingStartTime(null);
        userRepository.save(user);

        log.info("用户 {} 结束历练并应用奖励", userId);
        return rewards;
    }

    /**
     * 开始历练
     *
     * @param userId 用户 ID
     * @return 开始结果
     */
    public TrainingStartResult startTraining(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        // 获取地图名称
        if (user.getLocationId() == null) {
            return TrainingStartResult.builder()
                    .success(false)
                    .message("当前位置无效，无法开始历练")
                    .build();
        }

        var mapName = mapNodeRepository.findById(user.getLocationId())
                .map(MapNode::getName)
                .orElse(null);

        if (mapName == null) {
            return TrainingStartResult.builder()
                    .success(false)
                    .message("当前地图不存在，无法开始历练")
                    .build();
        }

        // 设置历练开始时间和状态
        user.setTrainingStartTime(LocalDateTime.now());
        user.setStatus(UserStatus.EXERCISING);
        userRepository.save(user);

        log.info("用户 {} 开始在 {} 历练", userId, mapName);
        return TrainingStartResult.builder()
                .success(true)
                .mapName(mapName)
                .build();
    }

    /**
     * 计算效率倍率（基于敏捷）
     */
    private double calculateEfficiencyMultiplier(int agility) {
        // 基础倍率 1.0，每 10 点敏捷增加 0.1 倍率
        return 1.0 + (agility * 0.01);
    }

    /**
     * 计算物品奖励（仅基础材料：rarity=common）
     */
    private List<Map<String, Object>> calculateItemsReward(long minutesTraining, double efficiencyMultiplier, MapNode mapNode) {
        List<Map<String, Object>> items = new ArrayList<>();
        var specialties = mapNode.getSpecialties();
        if (specialties == null || specialties.isEmpty()) return items;

        // Batch lookup item templates for name resolution
        Map<Long, ItemTemplate> templateMap = itemTemplateRepository.findByIds(new ArrayList<>(specialties.keySet()))
                .stream()
                .collect(Collectors.toMap(ItemTemplate::getId, t -> t));

        int dropChances = (int) (minutesTraining / 10 * efficiencyMultiplier);

        for (int i = 0; i < dropChances; i++) {
            int totalWeight = specialties.values().stream().mapToInt(Integer::intValue).sum();
            if (totalWeight == 0) continue;
            int roll = (int) (Math.random() * totalWeight);
            int cumulative = 0;
            Long selectedTemplateId = null;
            for (Map.Entry<Long, Integer> entry : specialties.entrySet()) {
                cumulative += entry.getValue();
                if (roll < cumulative) {
                    selectedTemplateId = entry.getKey();
                    break;
                }
            }
            if (selectedTemplateId == null) continue;

            int quantity = (int) (Math.random() * 3) + 1;

            boolean exists = false;
            for (Map<String, Object> existing : items) {
                if (Objects.equals(existing.get("templateId"), selectedTemplateId)) {
                    existing.put("quantity", (Integer) existing.get("quantity") + quantity);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                ItemTemplate template = templateMap.get(selectedTemplateId);
                String name = template != null ? template.getName() : "未知物品";
                Map<String, Object> item = new HashMap<>();
                item.put("templateId", selectedTemplateId);
                item.put("name", name);
                item.put("quantity", quantity);
                items.add(item);
            }
        }

        return items;
    }

    /**
     * 安全转换为 Long
     */
    private Long toLong(Object value) {
        if (value instanceof Long longVal) return longVal;
        if (value instanceof Number number) return number.longValue();
        throw new IllegalArgumentException("无法转换为 Long: " + value);
    }
}
