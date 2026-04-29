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
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TravelService {

    private final UserRepository userRepository;
    private final MapNodeRepository mapNodeRepository;
    private final AuthenticationService authService;

    // ===================== 公开 API =====================

    public ServiceResult<TravelResultVO> startTravel(PlatformType platform, String openId, String mapName) {
        var auth = authService.authenticateAndValidateStatus(platform, openId, UserStatus.IDLE);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        return new ServiceResult.Success<>(startTravel(auth.userId(), mapName));
    }

    // ===================== 内部 API =====================

    public TravelResultVO startTravel(Long userId, String mapName) {
        User user = userRepository.findById(userId).orElseThrow();

        Optional<MapNode> currentMapOpt = mapNodeRepository.findById(user.getLocationId());
        if (currentMapOpt.isEmpty()) {
            return TravelResultVO.builder().success(false).message("当前地图不存在").build();
        }
        MapNode currentMap = currentMapOpt.get();

        Optional<MapNode> targetMapOpt = mapNodeRepository.findByName(mapName);
        if (targetMapOpt.isEmpty()) {
            return TravelResultVO.builder().success(false).message("未找到地图: " + mapName).build();
        }
        MapNode targetMap = targetMapOpt.get();

        if (!currentMap.isAdjacentTo(targetMap.getName())) {
            return TravelResultVO.builder()
                    .success(false)
                    .message(currentMap.getName() + " 与 " + targetMap.getName() + " 不相邻，无法直接前往")
                    .build();
        }

        if (!targetMap.isAccessibleBy(user.getLevel())) {
            return TravelResultVO.builder()
                    .success(false)
                    .message("需要达到 " + targetMap.getLevelRequirement() + " 级才能前往 " + targetMap.getName())
                    .build();
        }

        Integer travelTime = currentMap.getTravelTimeTo(targetMap.getName());

        user.setStatus(UserStatus.RUNNING);
        user.setTravelStartTime(LocalDateTime.now());
        user.setTravelDestinationId(targetMap.getId());
        userRepository.save(user);

        LocalDateTime estimatedArrival = LocalDateTime.now().plusMinutes(travelTime);

        log.info("玩家 {} 开始旅行到 {}, 耗时 {} 分钟", userId, targetMap.getName(), travelTime);

        return TravelResultVO.builder()
                .success(true)
                .message("开始前往 " + targetMap.getName() + "，预计 " + travelTime + " 分钟后到达")
                .fromMapId(currentMap.getId())
                .fromMapName(currentMap.getName())
                .toMapId(targetMap.getId())
                .toMapName(targetMap.getName())
                .travelTimeMinutes(travelTime)
                .arrived(false)
                .estimatedArrivalTime(estimatedArrival)
                .build();
    }

    public TravelResultVO completeTravel(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        if (user.getTravelStartTime() == null) {
            return TravelResultVO.builder().success(false).message("旅行开始时间未设置").build();
        }

        long minutesElapsed = Duration.between(user.getTravelStartTime(), LocalDateTime.now()).toMinutes();

        Long targetMapId = user.getTravelDestinationId();
        if (targetMapId == null) {
            return TravelResultVO.builder().success(false).message("旅行目的地未设置").build();
        }

        Optional<MapNode> targetMapOpt = mapNodeRepository.findById(targetMapId);
        if (targetMapOpt.isEmpty()) {
            return TravelResultVO.builder().success(false).message("目标地图不存在").build();
        }
        MapNode targetMap = targetMapOpt.get();

        // D20 骰点 + 事件
        int d20Roll = rollD20();
        TravelEventType eventType = calculateTravelEvent(targetMap, d20Roll);
        String eventDescription = processTravelEvent(user, eventType);

        user.setStatus(UserStatus.IDLE);
        user.setLocationId(targetMap.getId());
        user.setTravelStartTime(null);
        user.setTravelDestinationId(null);
        userRepository.save(user);

        log.info("玩家 {} 完成旅行到 {}, 耗时 {} 分钟, D20: {}, 事件: {}", userId, targetMap.getName(), minutesElapsed, d20Roll, eventType);

        return TravelResultVO.builder()
                .success(true)
                .message("已到达 " + targetMap.getName())
                .toMapId(targetMap.getId())
                .toMapName(targetMap.getName())
                .travelTimeMinutes((int) minutesElapsed)
                .d20Roll(d20Roll)
                .eventType(eventType)
                .eventDescription(eventDescription)
                .arrived(true)
                .build();
    }

    // ===================== 事件 =====================

    private int rollD20() {
        return (int) (Math.random() * 20) + 1;
    }

    private TravelEventType calculateTravelEvent(MapNode mapNode, int d20Roll) {
        if (d20Roll <= 10 && mapNode.getTravelEvents() != null && !mapNode.getTravelEvents().isEmpty()) {
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

    private String processTravelEvent(User user, TravelEventType eventType) {
        return switch (eventType) {
            case AMBUSH -> {
                int damage = 10 + (int) (Math.random() * 20);
                user.takeDamage(damage);
                yield String.format("途中遭遇敌人袭击，受到 %d 点伤害（剩余 HP: %d）", damage, user.getHpCurrent());
            }
            case FIND_TREASURE -> {
                long coins = 50 + (long) (Math.random() * 100);
                user.setCoins(user.getCoins() + coins);
                yield String.format("在路边发现了一个宝箱，获得 %d 铜币", coins);
            }
            case WEATHER -> "遭遇毒雾天气，艰难穿过才得以继续前行";
            case SAFE_PASSAGE -> "一路平安，没有意外发生";
        };
    }
}