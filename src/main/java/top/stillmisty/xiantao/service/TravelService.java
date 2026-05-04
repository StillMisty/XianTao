package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.map.vo.TravelResultVO;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.service.annotation.Authenticated;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TravelService {

    private final UserStateService userStateService;
    private final MapNodeRepository mapNodeRepository;

    // ===================== 公开 API =====================

    @Authenticated
    @Transactional
    public ServiceResult<TravelResultVO> startTravel(PlatformType platform, String openId, String mapName) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(startTravel(userId, mapName));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    // ===================== 内部 API =====================

    @Transactional
    public TravelResultVO startTravel(Long userId, String mapName) {
        User user = userStateService.getUser(userId);

        if (user.getStatus() != UserStatus.IDLE) {
            throw new IllegalStateException("您当前处于 " + user.getStatus().getName() + " 状态，无法旅行（需要 空闲 状态）");
        }

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

        if (!currentMap.isAdjacentTo(targetMap.getId())) {
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

        Integer travelTime = currentMap.getTravelTimeTo(targetMap.getId());

        user.setStatus(UserStatus.RUNNING);
        user.setTravelStartTime(LocalDateTime.now());
        user.setTravelDestinationId(targetMap.getId());
        userStateService.save(user);

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
}
