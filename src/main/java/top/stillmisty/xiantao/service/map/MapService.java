package top.stillmisty.xiantao.service.map;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.enums.EventTypeEnum;
import top.stillmisty.xiantao.domain.event.repository.ActivityEventRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.entity.NeighborEntry;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.map.vo.MapInfoVO;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.player.UserStateService;

/** 地图服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MapService {

  private final MapNodeRepository mapNodeRepository;
  private final MonsterTemplateRepository monsterTemplateRepository;
  private final UserStateService userStateService;
  private final ActivityEventRepository activityEventRepository;

  @Lazy @Autowired private MapService self;

  // ===================== 公开 API（含认证） =====================

  @Authenticated
  public ServiceResult<List<MapInfoVO>> getAllMaps(PlatformType platform, String openId) {
    return new ServiceResult.Success<>(self.getAllMaps());
  }

  @Authenticated
  public ServiceResult<MapInfoVO> getCurrentMapInfo(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(self.getCurrentMapInfo(userId));
  }

  // ===================== 内部 API（需预先完成认证） =====================

  /** 获取所有地图 */
  @Cacheable(cacheNames = "map_data", key = "'all'")
  public List<MapInfoVO> getAllMaps() {
    var maps = mapNodeRepository.findAll();
    List<Long> mapIds = maps.stream().map(MapNode::getId).toList();
    Map<Long, List<ActivityEvent>> combatEventsByMap =
        activityEventRepository.findByOwnerIdsAndType("TRAINING", mapIds, EventTypeEnum.COMBAT);
    Map<Long, MonsterTemplate> templateMap = buildMonsterTemplateMap(combatEventsByMap);
    return maps.stream()
        .map(
            m ->
                convertToMapInfoVO(
                    m, combatEventsByMap.getOrDefault(m.getId(), List.of()), templateMap))
        .toList();
  }

  /** 获取当前所在地图详情 */
  @Cacheable(cacheNames = "map_data", key = "'current:' + #userId")
  public MapInfoVO getCurrentMapInfo(Long userId) {
    User user = userStateService.loadUser(userId);
    MapNode mapNode =
        mapNodeRepository
            .findById(user.getLocationId())
            .orElseThrow(() -> new BusinessException(ErrorCode.MAP_CURRENT_NOT_FOUND));
    List<ActivityEvent> combatEvents =
        activityEventRepository.findByType("TRAINING", mapNode.getId(), EventTypeEnum.COMBAT);
    Map<Long, MonsterTemplate> templateMap =
        buildMonsterTemplateMap(Map.of(mapNode.getId(), combatEvents));
    return convertToMapInfoVO(mapNode, combatEvents, templateMap);
  }

  /** 根据地图ID获取地图名称 */
  @Cacheable(cacheNames = "map_data", key = "'name:' + #mapId")
  public String getMapName(Long mapId) {
    if (mapId == null) return "未知";
    return mapNodeRepository.findById(mapId).map(MapNode::getName).orElse("未知");
  }

  /** 转换为地图信息 VO */
  private MapInfoVO convertToMapInfoVO(
      MapNode mapNode, List<ActivityEvent> combatEvents, Map<Long, MonsterTemplate> templateMap) {
    List<MapInfoVO.MonsterInfoVO> monsters = buildMonsterInfoList(combatEvents, templateMap);

    var neighbors = mapNode.getNeighbors();
    List<String> adjacentMapNames;
    if (neighbors != null && !neighbors.isEmpty()) {
      List<Long> neighborIds = neighbors.stream().map(NeighborEntry::targetId).toList();
      Map<Long, String> neighborNameMap =
          mapNodeRepository.findByIds(neighborIds).stream()
              .collect(Collectors.toMap(MapNode::getId, MapNode::getName));
      adjacentMapNames =
          neighborIds.stream().map(id -> neighborNameMap.getOrDefault(id, "未知")).toList();
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
        .monsters(monsters)
        .build();
  }

  private Map<Long, MonsterTemplate> buildMonsterTemplateMap(
      Map<Long, List<ActivityEvent>> combatEventsByMap) {
    List<Long> templateIds =
        combatEventsByMap.values().stream()
            .flatMap(List::stream)
            .filter(e -> e.getParams() != null && e.getParams().containsKey("monster_template_id"))
            .map(e -> ((Number) e.getParams().get("monster_template_id")).longValue())
            .distinct()
            .collect(Collectors.toList());
    if (templateIds.isEmpty()) return Map.of();
    return monsterTemplateRepository.findByIds(templateIds).stream()
        .collect(Collectors.toMap(MonsterTemplate::getId, t -> t));
  }

  /** 根据 COMBAT 事件构建怪物信息列表 */
  private List<MapInfoVO.MonsterInfoVO> buildMonsterInfoList(
      List<ActivityEvent> combatEvents, Map<Long, MonsterTemplate> templateMap) {
    if (combatEvents.isEmpty()) return List.of();
    return combatEvents.stream()
        .map(
            event -> {
              Map<String, Object> params = event.getParams();
              long templateId = ((Number) params.get("monster_template_id")).longValue();
              MonsterTemplate template = templateMap.get(templateId);
              return MapInfoVO.MonsterInfoVO.builder()
                  .templateId(templateId)
                  .name(template != null ? template.getName() : "未知怪物")
                  .typeName(
                      template != null && template.getMonsterType() != null
                          ? template.getMonsterType().getName()
                          : "未知")
                  .baseLevel(template != null ? template.getBaseLevel() : null)
                  .weight(event.getWeight())
                  .minCount(
                      params.containsKey("min_count")
                          ? ((Number) params.get("min_count")).intValue()
                          : 1)
                  .maxCount(
                      params.containsKey("max_count")
                          ? ((Number) params.get("max_count")).intValue()
                          : 1)
                  .build();
            })
        .sorted((a, b) -> Integer.compare(b.getWeight(), a.getWeight()))
        .toList();
  }
}
