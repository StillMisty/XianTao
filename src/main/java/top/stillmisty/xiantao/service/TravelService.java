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
import java.util.ArrayList;
import java.util.List;
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

    private final UserRepository userRepository;
    private final MapNodeRepository mapNodeRepository;

    public static final int STAMINA_COST_PER_MINUTE = 5;

    /**
     * 开始旅行
     *
     * @param userId       用户 ID
     * @param targetMapId  目标地图 ID
     * @param useStamina   是否使用体力模式
     * @return 旅行结果
     */
    public TravelResultVO startTravel(Long userId, Long targetMapId, boolean useStamina) {
        // 获取用户
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return TravelResultVO.builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        User user = userOpt.get();

        // 检查用户状态
        if (user.getStatus() != UserStatus.IDLE) {
            String statusName = user.getStatus() != null ? user.getStatus().getName() : "未知";
            return TravelResultVO.builder()
                    .success(false)
                    .message(String.format("您当前处于 %s 状态，无法旅行", statusName))
                    .build();
        }

        // 获取当前地图
        Optional<MapNode> currentMapOpt = mapNodeRepository.findById(user.getLocationId());
        if (currentMapOpt.isEmpty()) {
            return TravelResultVO.builder()
                    .success(false)
                    .message("当前地图不存在")
                    .build();
        }

        MapNode currentMap = currentMapOpt.get();

        // 获取目标地图
        Optional<MapNode> targetMapOpt = mapNodeRepository.findById(targetMapId);
        if (targetMapOpt.isEmpty()) {
            return TravelResultVO.builder()
                    .success(false)
                    .message("目标地图不存在")
                    .build();
        }

        MapNode targetMap = targetMapOpt.get();

        // 检查是否相邻
        if (!currentMap.isAdjacentTo(targetMap.getName())) {
            return TravelResultVO.builder()
                    .success(false)
                    .message(String.format("%s 与 %s 不相邻，无法直接前往", currentMap.getName(), targetMap.getName()))
                    .build();
        }

        // 检查等级要求
        if (!targetMap.isAccessibleBy(user.getLevel())) {
            return TravelResultVO.builder()
                    .success(false)
                    .message(String.format("您需要达到 %d 级才能前往 %s", targetMap.getLevelRequirement(), targetMap.getName()))
                    .build();
        }

        Integer travelTime = currentMap.getTravelTimeTo(targetMap.getName());

        if (useStamina) {
            // 体力模式：立即完成
            int staminaCost = travelTime * STAMINA_COST_PER_MINUTE;

            // TODO: 检查体力是否足够（需要实现体力系统）
            // 这里暂时假设体力足够

            // D20 骰点
            int d20Roll = rollD20();
            TravelEventType eventType = calculateTravelEvent(targetMap, d20Roll);

            // 处理事件
            String eventDescription = processTravelEvent(user, eventType);

            // 更新位置
            user.setLocationId(targetMapId);
            userRepository.save(user);

            log.info("玩家 {} 体力旅行到 {}, 耗时 {} 分钟, D20: {}, 事件: {}", userId, targetMap.getName(), travelTime, d20Roll, eventType);

            return TravelResultVO.builder()
                    .success(true)
                    .message(String.format("您消耗 %d 体力，已到达 %s", staminaCost, targetMap.getName()))
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
            user.setTravelDestinationId(targetMapId);
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
        // 获取用户
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return TravelResultVO.builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        User user = userOpt.get();

        // 检查用户状态
        if (user.getStatus() != UserStatus.RUNNING) {
            return TravelResultVO.builder()
                    .success(false)
                    .message("您当前不在旅行状态")
                    .build();
        }

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

        // 更新位置和状态
        user.setLocationId(targetMapId);
        user.setStatus(UserStatus.IDLE);
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
        // 获取用户
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return TravelResultVO.builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        User user = userOpt.get();

        // 检查用户状态
        if (user.getStatus() != UserStatus.RUNNING) {
            return TravelResultVO.builder()
                    .success(false)
                    .message("您当前不在旅行状态")
                    .build();
        }

        // 计算剩余时间
        long minutesElapsed = Duration.between(user.getTravelStartTime(), LocalDateTime.now()).toMinutes();

        // TODO: 获取目标地图并计算剩余旅行时间
        // 这里暂时假设需要消耗剩余时间的体力

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
     * @param d20Roll  D20 骰点结果
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
     * @param user       用户
     * @param eventType  事件类型
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
            case WEATHER -> {
                // 毒雾天气，额外消耗体力
                yield "遭遇毒雾天气，视线受阻，小心翼翼地通过了";
            }
            case SAFE_PASSAGE -> "一路平安，没有任何意外发生";
        };
    }
}
