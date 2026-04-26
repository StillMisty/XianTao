package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.enums.TravelEventType;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.map.vo.TravelResultVO;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * 旅行服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TravelService {

    private static final int STAMINA_COST_PER_TRAVEL_MINUTE = 5;
    private final UserRepository userRepository;
    private final MapNodeRepository mapNodeRepository;
    private final StaminaService staminaService;

    /**
     * 开始旅行
     *
     * @param userId        用户 ID
     * @param mapName       目标地图名称
     * @param forceRealTime 是否强制使用真实时间模式
     * @return 旅行结果
     */
    public TravelResultVO startTravel(Long userId, String mapName, boolean forceRealTime) {
        User user = userRepository.findById(userId).orElseThrow();

        // 获取当前地图
        Optional<MapNode> currentMapOpt = mapNodeRepository.findById(user.getLocationId());
        if (currentMapOpt.isEmpty()) {
            return TravelResultVO.builder()
                    .success(false)
                    .message("当前地图不存在")
                    .build();
        }

        MapNode currentMap = currentMapOpt.get();

        // 根据名称查找目标地图
        Optional<MapNode> targetMapOpt = mapNodeRepository.findByName(mapName);
        if (targetMapOpt.isEmpty()) {
            return TravelResultVO.builder()
                    .success(false)
                    .message(String.format("未找到地图: %s", mapName))
                    .build();
        }

        MapNode targetMap = targetMapOpt.get();

        // 检查是否相邻
        if (currentMap.isAdjacentTo(targetMap.getName())) {
            return TravelResultVO.builder()
                    .success(false)
                    .message(String.format("%s 与 %s 不相邻，无法直接前往", currentMap.getName(), targetMap.getName()))
                    .build();
        }

        // 检查等级要求
        if (targetMap.isAccessibleBy(user.getLevel())) {
            return TravelResultVO.builder()
                    .success(false)
                    .message(String.format("您需要达到 %d 级才能前往 %s", targetMap.getLevelRequirement(), targetMap.getName()))
                    .build();
        }

        Integer travelTime = currentMap.getTravelTimeTo(targetMap.getName());

        // 决定是否使用体力模式
        boolean useStamina = !forceRealTime && hasEnoughStamina(user, travelTime);

        return executeTravel(userId, user, currentMap, targetMap, travelTime, useStamina);
    }

    /**
     * 执行旅行逻辑
     */
    private TravelResultVO executeTravel(
            Long userId, User user, MapNode currentMap, MapNode targetMap,
            Integer travelTime, boolean useStamina
    ) {

        if (useStamina) {
            // 体力模式：检查并消耗体力
            var staminaResult = staminaService.checkAndConsumeTravelStamina(userId, travelTime);
            if (!staminaResult.success()) {
                // 体力不足，降级为真实时间模式
                user.setStatus(UserStatus.RUNNING);
                user.setTravelStartTime(LocalDateTime.now());
                user.setTravelDestinationId(targetMap.getId());
                userRepository.save(user);

                LocalDateTime estimatedArrival = LocalDateTime.now().plusMinutes(travelTime);

                log.info("玩家 {} 体力不足，降级为真实时间旅行到 {}, 耗时 {} 分钟", userId, targetMap.getName(), travelTime);

                return TravelResultVO.builder()
                        .success(true)
                        .message(String.format("体力不足！已自动切换为真实时间模式\n您开始前往 %s，预计 %d 分钟后到达", targetMap.getName(), travelTime))
                        .fromMapId(currentMap.getId())
                        .fromMapName(currentMap.getName())
                        .toMapId(targetMap.getId())
                        .toMapName(targetMap.getName())
                        .useStamina(false)
                        .travelTimeMinutes(travelTime)
                        .arrived(false)
                        .estimatedArrivalTime(estimatedArrival)
                        .build();
            }

            int staminaCost = staminaResult.consumedStamina();
            int remainingStamina = staminaResult.remainingStamina();

            // D20 骰点
            int d20Roll = rollD20();
            TravelEventType eventType = calculateTravelEvent(targetMap, d20Roll);

            // 处理事件
            String eventDescription = processTravelEvent(user, eventType);

            // 更新位置
            user.setLocationId(targetMap.getId());
            userRepository.save(user);

            log.info("玩家 {} 体力旅行到 {}, 耗时 {} 分钟, 消耗体力: {}, D20: {}, 事件: {}", userId, targetMap.getName(), travelTime, staminaCost, d20Roll, eventType);

            return TravelResultVO.builder()
                    .success(true)
                    .message(String.format("您消耗 %d 体力（剩余 %d），已到达 %s", staminaCost, remainingStamina, targetMap.getName()))
                    .fromMapId(currentMap.getId())
                    .fromMapName(currentMap.getName())
                    .toMapId(targetMap.getId())
                    .toMapName(targetMap.getName())
                    .useStamina(true)
                    .staminaCost(staminaCost)
                    .travelTimeMinutes(travelTime)
                    .d20Roll(d20Roll)
                    .eventType(eventType)
                    .eventDescription(eventDescription)
                    .arrived(true)
                    .build();
        } else {
            // 真实时间模式
            user.setStatus(UserStatus.RUNNING);
            user.setTravelStartTime(LocalDateTime.now());
            user.setTravelDestinationId(targetMap.getId());
            userRepository.save(user);

            LocalDateTime estimatedArrival = LocalDateTime.now().plusMinutes(travelTime);

            log.info("玩家 {} 开始旅行到 {}, 耗时 {} 分钟", userId, targetMap.getName(), travelTime);

            return TravelResultVO.builder()
                    .success(true)
                    .message(String.format("您开始前往 %s，预计 %d 分钟后到达", targetMap.getName(), travelTime))
                    .fromMapId(currentMap.getId())
                    .fromMapName(currentMap.getName())
                    .toMapId(targetMap.getId())
                    .toMapName(targetMap.getName())
                    .useStamina(false)
                    .travelTimeMinutes(travelTime)
                    .arrived(false)
                    .estimatedArrivalTime(estimatedArrival)
                    .build();
        }
    }

    /**
     * 完成旅行（真实时间模式）
     *
     * @param userId 用户 ID
     * @return 旅行结果
     */
    public TravelResultVO completeTravel(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        // 检查是否到达时间
        if (user.getTravelStartTime() == null) {
            return TravelResultVO.builder()
                    .success(false)
                    .message("旅行开始时间未设置")
                    .build();
        }

        long minutesElapsed = Duration.between(user.getTravelStartTime(), LocalDateTime.now()).toMinutes();

        // 获取目标地图 ID
        Long targetMapId = user.getTravelDestinationId();
        if (targetMapId == null) {
            return TravelResultVO.builder()
                    .success(false)
                    .message("旅行目的地未设置")
                    .build();
        }

        // 获取目标地图
        Optional<MapNode> targetMapOpt = mapNodeRepository.findById(targetMapId);
        if (targetMapOpt.isEmpty()) {
            return TravelResultVO.builder()
                    .success(false)
                    .message("目标地图不存在")
                    .build();
        }

        MapNode targetMap = targetMapOpt.get();

        // D20 骰点
        int d20Roll = rollD20();
        TravelEventType eventType = calculateTravelEvent(targetMap, d20Roll);

        // 处理事件
        String eventDescription = processTravelEvent(user, eventType);

        // 移动玩家到目标地图
        user.setStatus(UserStatus.IDLE);
        user.setLocationId(targetMap.getId());
        user.setTravelStartTime(null);
        user.setTravelDestinationId(null);
        userRepository.save(user);

        log.info("玩家 {} 完成旅行到 {}, 耗时 {} 分钟, D20: {}, 事件: {}", userId, targetMap.getName(), minutesElapsed, d20Roll, eventType);

        return TravelResultVO.builder()
                .success(true)
                .message(String.format("您已到达 %s", targetMap.getName()))
                .toMapId(targetMap.getId())
                .toMapName(targetMap.getName())
                .travelTimeMinutes((int) minutesElapsed)
                .d20Roll(d20Roll)
                .eventType(eventType)
                .eventDescription(eventDescription)
                .arrived(true)
                .build();
    }

    /**
     * 使用体力跳过等待
     *
     * @param userId 用户 ID
     * @return 旅行结果
     */
    public TravelResultVO useStaminaToSkip(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        Long targetMapId = user.getTravelDestinationId();
        if (targetMapId == null || targetMapId == 0) {
            return TravelResultVO.builder()
                    .success(false)
                    .message("旅行目的地未设置")
                    .build();
        }

        // 计算剩余旅行时间
        long minutesElapsed = Duration.between(user.getTravelStartTime(), LocalDateTime.now()).toMinutes();

        // 获取目标地图以计算总旅行时间
        Optional<MapNode> targetMapOpt = mapNodeRepository.findById(targetMapId);
        if (targetMapOpt.isEmpty()) {
            return TravelResultVO.builder()
                    .success(false)
                    .message("目标地图不存在")
                    .build();
        }

        // 获取当前地图以计算旅行时间
        Optional<MapNode> currentMapOpt = mapNodeRepository.findById(user.getLocationId());
        if (currentMapOpt.isEmpty()) {
            return TravelResultVO.builder()
                    .success(false)
                    .message("当前地图不存在")
                    .build();
        }

        Integer totalTravelTime = currentMapOpt.get().getTravelTimeTo(targetMapOpt.get().getName());
        if (totalTravelTime == null) {
            return TravelResultVO.builder()
                    .success(false)
                    .message("无法计算旅行时间")
                    .build();
        }

        int remainingMinutes = Math.max(1, totalTravelTime - (int) minutesElapsed);

        // 检查并消耗体力
        var staminaResult = staminaService.checkAndConsumeTravelStamina(userId, remainingMinutes);
        if (!staminaResult.success()) {
            return TravelResultVO.builder()
                    .success(false)
                    .message(staminaResult.message())
                    .build();
        }

        log.info("玩家 {} 使用体力跳过旅行，消耗 {} 体力", userId, staminaResult.consumedStamina());

        // 完成旅行
        return completeTravel(userId);
    }

    /**
     * D20 骰点
     */
    private int rollD20() {
        return (int) (Math.random() * 20) + 1;
    }

    /**
     * 计算旅行事件
     *
     * @param mapNode 地图节点
     * @param d20Roll D20 骰点结果
     * @return 事件类型
     */
    private TravelEventType calculateTravelEvent(MapNode mapNode, int d20Roll) {
        // 1-10: 触发事件, 11-20: 安全通行
        if (d20Roll <= 10 && mapNode.getTravelEvents() != null && !mapNode.getTravelEvents().isEmpty()) {
            // 根据权重随机选择事件
            Map<String, Integer> eventWeights = new java.util.HashMap<>();
            for (Map<String, Object> event : mapNode.getTravelEvents()) {
                String eventType = (String) event.get("eventType");
                Integer weight = ((Number) event.getOrDefault("weight", 1)).intValue();
                eventWeights.put(eventType, weight);
            }
            return TravelEventType.randomEvent(eventWeights);
        }
        return TravelEventType.SAFE_PASSAGE;
    }

    /**
     * 处理旅行事件
     *
     * @param user      用户
     * @param eventType 事件类型
     * @return 事件描述
     */
    private String processTravelEvent(User user, TravelEventType eventType) {
        return switch (eventType) {
            case AMBUSH -> {
                // 遭遇敌人，扣除 HP
                int damage = 10 + (int) (Math.random() * 20);
                user.takeDamage(damage);
                yield String.format("途中遭遇敌人袭击！受到了 %d 点伤害，剩余 HP: %d", damage, user.getHpCurrent());
            }
            case FIND_TREASURE -> {
                // 发现宝箱，获得奖励
                long coins = 50 + (long) (Math.random() * 100);
                user.setCoins(user.getCoins() + coins);
                yield String.format("运气真好！您在路边发现了一个宝箱，获得了 %d 铜币", coins);
            }
            case WEATHER -> // 毒雾天气，额外消耗体力
                    "遭遇毒雾天气，视线受阻，小心翼翼地通过了";
            case SAFE_PASSAGE -> "一路平安，没有任何意外发生";
        };
    }

    /**
     * 检查用户是否有足够体力进行旅行
     */
    private boolean hasEnoughStamina(User user, int travelTime) {
        int staminaCost = travelTime * STAMINA_COST_PER_TRAVEL_MINUTE;
        user.calculateOfflineStaminaRecovery();
        return user.hasEnoughStamina(staminaCost);
    }
}
