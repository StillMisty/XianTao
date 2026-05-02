package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.enums.MapType;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.map.vo.MapInfoVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;

import java.util.List;
import java.util.Optional;

/**
 * 地图服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MapService {

    private final MapNodeRepository mapNodeRepository;
    private final AuthenticationService authService;

    // ===================== 公开 API（含认证） =====================

    public ServiceResult<List<MapInfoVO>> getAllMaps(PlatformType platform, String openId) {
        var auth = authService.authenticate(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        return new ServiceResult.Success<>(getAllMaps());
    }

    // ===================== 内部 API（需预先完成认证） =====================

    /**
     * 获取地图信息
     *
     * @param mapId 地图 ID
     * @return 地图信息 VO
     */
    public Optional<MapInfoVO> getMapInfo(Long mapId) {
        return mapNodeRepository.findById(mapId)
                .map(this::convertToMapInfoVO);
    }

    /**
     * 根据名称获取地图信息
     *
     * @param mapName 地图名称
     * @return 地图信息 VO
     */
    public Optional<MapInfoVO> getMapInfoByName(String mapName) {
        return mapNodeRepository.findByName(mapName)
                .map(this::convertToMapInfoVO);
    }

    /**
     * 获取所有地图
     *
     * @return 地图列表
     */
    public List<MapInfoVO> getAllMaps() {
        return mapNodeRepository.findAll().stream()
                .map(this::convertToMapInfoVO)
                .toList();
    }

    /**
     * 根据类型获取地图
     *
     * @param mapType 地图类型
     * @return 地图列表
     */
    public List<MapInfoVO> getMapsByType(MapType mapType) {
        return mapNodeRepository.findByType(mapType).stream()
                .map(this::convertToMapInfoVO)
                .toList();
    }

    /**
     * 验证旅行
     *
     * @param currentMapId 当前地图 ID
     * @param targetMapId  目标地图 ID
     * @param playerLevel  玩家等级
     * @return 验证结果（错误消息，null 表示验证通过）
     */
    public String validateTravel(Long currentMapId, Long targetMapId, int playerLevel) {
        // 检查目标地图是否存在
        Optional<MapNode> targetMapOpt = mapNodeRepository.findById(targetMapId);
        if (targetMapOpt.isEmpty()) {
            return "目标地图不存在";
        }

        MapNode targetMap = targetMapOpt.get();

        // 检查玩家等级是否满足要求
        if (!targetMap.isAccessibleBy(playerLevel)) {
            return String.format("您需要达到 %d 级才能前往 %s", targetMap.getLevelRequirement(), targetMap.getName());
        }

        // 检查是否相邻
        Optional<MapNode> currentMapOpt = mapNodeRepository.findById(currentMapId);
        if (currentMapOpt.isPresent()) {
            MapNode currentMap = currentMapOpt.get();
            if (!currentMap.isAdjacentTo(targetMap.getName())) {
                return String.format("%s 与 %s 不相邻，无法直接前往", currentMap.getName(), targetMap.getName());
            }
        }

        return null; // 验证通过
    }

    /**
     * 转换为地图信息 VO
     */
    private MapInfoVO convertToMapInfoVO(MapNode mapNode) {
        return MapInfoVO.builder()
                .id(mapNode.getId())
                .name(mapNode.getName())
                .description(mapNode.getDescription())
                .mapType(mapNode.getMapType())
                .mapTypeName(mapNode.getMapType().getName())
                .levelRequirement(mapNode.getLevelRequirement())
                .neighbors(mapNode.getNeighbors())
                .adjacentMapNames(mapNode.getAdjacentMapNames())
                .specialties(mapNode.getSpecialties())
                .travelEvents(mapNode.getTravelEvents())
                .build();
    }
}
