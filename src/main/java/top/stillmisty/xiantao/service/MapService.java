package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
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
    // ===================== 公开 API（含认证） =====================

    public ServiceResult<List<MapInfoVO>> getAllMaps(PlatformType platform, String openId) {
        Long userId = UserContext.getCurrentUserId();
        return new ServiceResult.Success<>(getAllMaps());
    }

    // ===================== 内部 API（需预先完成认证） =====================

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
     * 根据地图ID获取地图名称
     *
     * @param mapId 地图ID
     * @return 地图名称，未找到返回"未知"
     */
    public String getMapName(Long mapId) {
        if (mapId == null) {
            return "未知";
        }
        return mapNodeRepository.findById(mapId)
                .map(MapNode::getName)
                .orElse("未知");
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
