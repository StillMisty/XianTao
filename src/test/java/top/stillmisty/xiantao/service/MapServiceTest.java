package top.stillmisty.xiantao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.entity.MonsterEncounterEntry;
import top.stillmisty.xiantao.domain.map.enums.MapType;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.map.vo.MapInfoVO;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.enums.MonsterType;
import top.stillmisty.xiantao.domain.monster.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("MapService 测试")
@ExtendWith(MockitoExtension.class)
class MapServiceTest {

    @Mock
    private MapNodeRepository mapNodeRepository;
    @Mock
    private MonsterTemplateRepository monsterTemplateRepository;
    @Mock
    private UserStateService userStateService;

    @InjectMocks
    private MapService mapService;

    private final Long userId = 1L;
    private final Long mapId = 1L;

    // ===================== fixtures =====================

    private User createUser(Long locationId) {
        return User.create().setId(userId).setLocationId(locationId).setNickname("测试修士");
    }

    private MapNode createMapNode() {
        MapNode node = MapNode.create();
        node.setId(mapId);
        node.setName("测试地图");
        node.setDescription("测试描述");
        node.setMapType(MapType.TRAINING_ZONE);
        node.setLevelRequirement(5);
        return node;
    }

    private MonsterTemplate createTemplate(long id, String name, int baseLevel) {
        MonsterTemplate t = new MonsterTemplate();
        t.setId(id);
        t.setName(name);
        t.setMonsterType(MonsterType.BEAST);
        t.setBaseLevel(baseLevel);
        return t;
    }

    private MonsterEncounterEntry encounter(long templateId, int weight, int min, int max) {
        return new MonsterEncounterEntry(templateId, weight, min, max);
    }

    // ===================== getCurrentMapInfo =====================

    @Nested
    @DisplayName("getCurrentMapInfo")
    class GetCurrentMapInfoTests {

        @Test
        @DisplayName("地图有怪物时返回完整信息")
        void withMonsters_shouldReturnFullMapInfo() {
            User user = createUser(mapId);
            MapNode mapNode = createMapNode();
            mapNode.setMonsterEncounters(List.of(
                    encounter(1L, 50, 1, 3),
                    encounter(2L, 30, 2, 5)));

            when(userStateService.getUser(userId)).thenReturn(user);
            when(mapNodeRepository.findById(mapId)).thenReturn(Optional.of(mapNode));
            when(monsterTemplateRepository.findByIds(anyList()))
                    .thenReturn(List.of(createTemplate(1L, "妖兽A", 5), createTemplate(2L, "妖兽B", 8)));

            MapInfoVO result = mapService.getCurrentMapInfo(userId);

            assertNotNull(result);
            assertEquals("测试地图", result.getName());
            assertEquals(2, result.getMonsters().size());
            assertEquals(50, result.getMonsters().get(0).getWeight());
            assertEquals("妖兽A", result.getMonsters().get(0).getName());
        }

        @Test
        @DisplayName("地图无怪物时返回空列表")
        void withoutMonsters_shouldReturnEmptyList() {
            User user = createUser(mapId);
            MapNode mapNode = createMapNode();

            when(userStateService.getUser(userId)).thenReturn(user);
            when(mapNodeRepository.findById(mapId)).thenReturn(Optional.of(mapNode));

            MapInfoVO result = mapService.getCurrentMapInfo(userId);

            assertTrue(result.getMonsters().isEmpty());
            verify(monsterTemplateRepository, never()).findByIds(any());
        }

        @Test
        @DisplayName("地图不存在时抛出异常")
        void whenMapNotFound_shouldThrow() {
            User user = createUser(mapId);

            when(userStateService.getUser(userId)).thenReturn(user);
            when(mapNodeRepository.findById(mapId)).thenReturn(Optional.empty());

            assertThrows(IllegalStateException.class, () -> mapService.getCurrentMapInfo(userId));
        }

        @Test
        @DisplayName("怪物按权重降序排列")
        void monstersSortedByWeightDescending() {
            User user = createUser(mapId);
            MapNode mapNode = createMapNode();
            mapNode.setMonsterEncounters(List.of(
                    encounter(1L, 10, 1, 1),
                    encounter(2L, 90, 1, 1),
                    encounter(3L, 50, 1, 1)));

            when(userStateService.getUser(userId)).thenReturn(user);
            when(mapNodeRepository.findById(mapId)).thenReturn(Optional.of(mapNode));
            when(monsterTemplateRepository.findByIds(anyList()))
                    .thenReturn(List.of(
                            createTemplate(1L, "A", 5),
                            createTemplate(2L, "B", 5),
                            createTemplate(3L, "C", 5)));

            MapInfoVO result = mapService.getCurrentMapInfo(userId);

            List<MapInfoVO.MonsterInfoVO> monsters = result.getMonsters();
            assertEquals(3, monsters.size());
            assertEquals(90, monsters.get(0).getWeight());
            assertEquals(50, monsters.get(1).getWeight());
            assertEquals(10, monsters.get(2).getWeight());
        }

        @Test
        @DisplayName("monsterEncounters 为 null 时安全返回")
        void nullSafe() {
            User user = createUser(mapId);
            MapNode mapNode = createMapNode();

            when(userStateService.getUser(userId)).thenReturn(user);
            when(mapNodeRepository.findById(mapId)).thenReturn(Optional.of(mapNode));

            MapInfoVO result = mapService.getCurrentMapInfo(userId);

            assertTrue(result.getMonsters().isEmpty());
        }
    }

    // ===================== getAllMaps =====================

    @Nested
    @DisplayName("getAllMaps")
    class GetAllMapsTests {

        @Test
        @DisplayName("返回所有地图")
        void shouldReturnAllMaps() {
            MapNode node1 = createMapNode();
            MapNode node2 = MapNode.create();
            node2.setId(2L);
            node2.setName("秘境");
            node2.setDescription("隐藏秘境");
            node2.setMapType(MapType.HIDDEN_ZONE);

            when(mapNodeRepository.findAll()).thenReturn(List.of(node1, node2));

            List<MapInfoVO> result = mapService.getAllMaps();

            assertEquals(2, result.size());
            assertEquals("测试地图", result.get(0).getName());
            assertEquals("秘境", result.get(1).getName());
        }

        @Test
        @DisplayName("无地图时返回空列表")
        void whenNoMaps_shouldReturnEmptyList() {
            when(mapNodeRepository.findAll()).thenReturn(List.of());

            List<MapInfoVO> result = mapService.getAllMaps();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("所有地图的怪物信息正确解析")
        void allMapsWithParsedData() {
            MapNode node1 = createMapNode();
            node1.setMonsterEncounters(List.of(encounter(1L, 80, 1, 2)));

            when(mapNodeRepository.findAll()).thenReturn(List.of(node1));
            when(monsterTemplateRepository.findByIds(anyList()))
                    .thenReturn(List.of(createTemplate(1L, "猛虎", 3)));

            List<MapInfoVO> result = mapService.getAllMaps();

            assertEquals(1, result.size());
            assertEquals(1, result.get(0).getMonsters().size());
            assertEquals("猛虎", result.get(0).getMonsters().get(0).getName());
            assertEquals(80, result.get(0).getMonsters().get(0).getWeight());
        }
    }

    // ===================== getMapName =====================

    @Nested
    @DisplayName("getMapName")
    class GetMapNameTests {

        @Test
        @DisplayName("地图存在时返回名称")
        void whenMapExists_shouldReturnName() {
            when(mapNodeRepository.findById(1L)).thenReturn(Optional.of(createMapNode()));

            assertEquals("测试地图", mapService.getMapName(1L));
        }

        @Test
        @DisplayName("地图不存在时返回「未知」")
        void whenMapNotFound_shouldReturnUnknown() {
            when(mapNodeRepository.findById(99L)).thenReturn(Optional.empty());

            assertEquals("未知", mapService.getMapName(99L));
        }

        @Test
        @DisplayName("mapId 为 null 时返回「未知」")
        void whenMapIdNull_shouldReturnUnknown() {
            assertEquals("未知", mapService.getMapName(null));
            verify(mapNodeRepository, never()).findById(any());
        }
    }
}
