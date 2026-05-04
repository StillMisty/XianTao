package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.entity.MonsterEncounterEntry;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.map.vo.MapInfoVO;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.service.annotation.Authenticated;

import java.util.List;
import java.util.Map;

/**
 * 地图服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MapService {

    private final MapNodeRepository mapNodeRepository;
    private final MonsterTemplateRepository monsterTemplateRepository;
    private final UserStateService userStateService;
    // ===================== 公开 API（含认证） =====================

    @Authenticated
    public ServiceResult<List<MapInfoVO>> getAllMaps(PlatformType platform, String openId) {
        Long userId = UserContext.getCurrentUserId();
        return new ServiceResult.Success<>(getAllMaps());
    }

    @Authenticated
    public ServiceResult<MapInfoVO> getCurrentMapInfo(PlatformType platform, String openId) {
        Long userId = UserContext.getCurrentUserId();
        return new ServiceResult.Success<>(getCurrentMapInfo(userId));
    }

    // ===================== 内部 API（需预先完成认证） =====================

    /**
     * 获取所有地图
     */
    public List<MapInfoVO> getAllMaps() {
        return mapNodeRepository.findAll().stream()
                .map(this::convertToMapInfoVO)
                .toList();
    }

    /**
     * 获取当前所在地图详情
     */
    public MapInfoVO getCurrentMapInfo(Long userId) {
        User user = userStateService.getUser(userId);
        MapNode mapNode = mapNodeRepository.findById(user.getLocationId())
                .orElseThrow(() -> new IllegalStateException("当前所在地图不存在"));
        return convertToMapInfoVO(mapNode);
    }

    /**
     * 根据地图ID获取地图名称
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
        List<MapInfoVO.MonsterInfoVO> monsters = buildMonsterInfoList(mapNode.getMonsterEncounters());

        var neighbors = mapNode.getNeighbors();
        List<String> adjacentMapNames;
        if (neighbors != null && !neighbors.isEmpty()) {
            adjacentMapNames = neighbors.stream()
                    .map(n -> mapNodeRepository.findById(n.targetId()).map(MapNode::getName).orElse("未知"))
                    .toList();
        } else {
            adjacentMapNames = List.of();
        }

        return MapInfoVO.builder()
                .id(mapNode.getId())
                .name(mapNode.getName())
                .description(mapNode.getDescription())
                .mapType(mapNode.getMapType())
                .mapTypeName(mapNode.getMapType().getName())
                .levelRequirement(mapNode.getLevelRequirement())
                .neighbors(mapNode.getNeighbors())
                .adjacentMapNames(adjacentMapNames)
                .specialties(mapNode.getSpecialties())
                .travelEvents(mapNode.getTravelEvents())
                .monsters(monsters)
                .build();
    }

    /**
     * 根据遇怪池构建怪物信息列表
     */
    private List<MapInfoVO.MonsterInfoVO> buildMonsterInfoList(List<MonsterEncounterEntry> monsterEncounters) {
        if (monsterEncounters == null || monsterEncounters.isEmpty()) {
            return List.of();
        }
        List<Long> templateIds = monsterEncounters.stream()
                .map(MonsterEncounterEntry::templateId)
                .toList();
        Map<Long, MonsterTemplate> templateMap = monsterTemplateRepository.findByIds(templateIds).stream()
                .collect(java.util.stream.Collectors.toMap(MonsterTemplate::getId, t -> t));

        return monsterEncounters.stream()
                .map(entry -> {
                    MonsterTemplate template = templateMap.get(entry.templateId());
                    return MapInfoVO.MonsterInfoVO.builder()
                            .templateId(entry.templateId())
                            .name(template != null ? template.getName() : "未知怪物")
                            .typeName(template != null && template.getMonsterType() != null
                                    ? template.getMonsterType().getName() : "未知")
                            .baseLevel(template != null ? template.getBaseLevel() : null)
                            .weight(entry.weight())
                            .minCount(entry.min())
                            .maxCount(entry.max())
                            .build();
                })
                .sorted((a, b) -> Integer.compare(b.getWeight(), a.getWeight()))
                .toList();
    }
}
