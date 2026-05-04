package top.stillmisty.xiantao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.fudi.vo.FarmCellVO;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("FarmService 测试")
@ExtendWith(MockitoExtension.class)
class FarmServiceTest {

    @Mock
    private FudiCellRepository fudiCellRepository;
    @Mock
    private ItemTemplateRepository itemTemplateRepository;
    @Mock
    private StackableItemRepository stackableItemRepository;
    @Mock
    private StackableItemService stackableItemService;
    @Mock
    private ItemResolver itemResolver;
    @Mock
    private FudiHelper fudiHelper;

    @InjectMocks
    private FarmService farmService;

    private final Long userId = 1L;
    private final Long fudiId = 100L;

    private Fudi createTestFudi() {
        return Fudi.create()
                .setUserId(userId)
                .setTribulationStage(5)
                .setId(fudiId);
    }

    private ItemTemplate createSeedTemplate(Long id, String name, int growTime) {
        ItemTemplate template = new ItemTemplate();
        template.setId(id);
        template.setName(name);
        template.setType(ItemType.SEED);
        template.setProperties(Map.of("grow_time", growTime, "reharvest", 0));
        return template;
    }

    private StackableItem createStackableItem(Long templateId, int quantity) {
        StackableItem item = new StackableItem();
        item.setId(templateId);
        item.setTemplateId(templateId);
        item.setQuantity(quantity);
        return item;
    }

    // ===================== plantCropByName =====================

    @Test
    @DisplayName("plantCropByName — 成功种植返回 FarmCellVO")
    void plantCropByName_shouldReturnFarmCellVO() {
        when(fudiHelper.parseCellId("1")).thenReturn(1);
        when(fudiHelper.getFudiByUserId(userId)).thenReturn(Optional.of(createTestFudi()));
        when(fudiHelper.getCropTier(anyInt())).thenReturn(1);
        when(fudiHelper.getLevelSpeedMultiplier(anyInt(), anyInt())).thenReturn(1.0);
        doNothing().when(fudiHelper).checkSpiritStones(eq(userId), anyInt());
        doNothing().when(fudiHelper).deductSpiritStones(eq(userId), anyInt());

        ItemTemplate seed = createSeedTemplate(10L, "灵芝种子", 6);
        when(itemTemplateRepository.findByType(ItemType.SEED)).thenReturn(List.of(seed));
        when(stackableItemRepository.findByUserIdAndTemplateId(userId, 10L))
                .thenReturn(Optional.of(createStackableItem(10L, 5)));
        when(fudiCellRepository.findByFudiIdAndCellId(fudiId, 1)).thenReturn(Optional.empty());
        doNothing().when(stackableItemService).reduceStackableItem(eq(userId), eq(10L), eq(1));

        FarmCellVO result = farmService.plantCropByName(userId, "1", "灵芝种子");

        assertNotNull(result);
        assertEquals(1, result.getCellId());
        assertEquals("灵芝种子", result.getCropName());
        assertFalse(result.getIsMature());
    }

    @Test
    @DisplayName("plantCropByName — 种子不存在抛异常")
    void plantCropByName_whenSeedNotFound_shouldThrow() {
        when(fudiHelper.parseCellId("1")).thenReturn(1);
        when(itemTemplateRepository.findByType(ItemType.SEED)).thenReturn(List.of());

        assertThrows(IllegalStateException.class,
                () -> farmService.plantCropByName(userId, "1", "不存在种子"));
    }

    @Test
    @DisplayName("plantCropByName — 福地不存在抛异常")
    void plantCropByName_whenFudiNotFound_shouldThrow() {
        when(fudiHelper.parseCellId("1")).thenReturn(1);
        when(fudiHelper.getFudiByUserId(userId)).thenReturn(Optional.empty());

        ItemTemplate seed = createSeedTemplate(1L, "种子名", 6);
        when(itemTemplateRepository.findByType(ItemType.SEED)).thenReturn(List.of(seed));
        when(stackableItemRepository.findByUserIdAndTemplateId(userId, 1L))
                .thenReturn(Optional.of(createStackableItem(1L, 5)));

        assertThrows(IllegalStateException.class,
                () -> farmService.plantCropByName(userId, "1", "种子名"));
    }

    @Test
    @DisplayName("plantCropByName — 地块已有非灵田类型建筑抛异常")
    void plantCropByName_whenCellOccupiedByNonFarm_shouldThrow() {
        when(fudiHelper.parseCellId("5")).thenReturn(5);
        when(fudiHelper.getFudiByUserId(userId)).thenReturn(Optional.of(createTestFudi()));
        when(fudiHelper.getCropTier(anyInt())).thenReturn(1);

        ItemTemplate seed = createSeedTemplate(10L, "灵芝种子", 6);
        when(itemTemplateRepository.findByType(ItemType.SEED)).thenReturn(List.of(seed));
        when(stackableItemRepository.findByUserIdAndTemplateId(userId, 10L))
                .thenReturn(Optional.of(createStackableItem(10L, 5)));

        FudiCell occupiedCell = new FudiCell();
        occupiedCell.setFudiId(fudiId);
        occupiedCell.setCellId(5);
        occupiedCell.setCellType(CellType.PEN);
        when(fudiCellRepository.findByFudiIdAndCellId(fudiId, 5)).thenReturn(Optional.of(occupiedCell));

        assertThrows(IllegalStateException.class,
                () -> farmService.plantCropByName(userId, "5", "灵芝种子"));
    }

    // ===================== 辅助方法 =====================

    @Test
    @DisplayName("calculateYield — 产出包含随机基础值和天劫加成")
    void calculateYield_shouldIncludeBaseAndTribulationBonus() {
        int yield = farmService.calculateYield(1, 10);
        assertTrue(yield >= 1 + 10 / 5);
        assertTrue(yield <= 3 + 10 / 5);
    }

    @Test
    @DisplayName("getBaseGrowthHours — 无生长时间默认返回5.0")
    void getBaseGrowthHours_whenNoGrowTime_shouldReturnDefault() {
        when(itemTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        double hours = farmService.getBaseGrowthHours(99);

        assertEquals(5.0, hours);
    }

    @Test
    @DisplayName("getBaseGrowthHours — 返回物品的生长时间")
    void getBaseGrowthHours_shouldReturnTemplateGrowTime() {
        ItemTemplate template = new ItemTemplate();
        template.setId(1L);
        template.setType(ItemType.SEED);
        template.setProperties(Map.of("grow_time", 24, "reharvest", 0));
        when(itemTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

        double hours = farmService.getBaseGrowthHours(1);

        assertEquals(24.0, hours);
    }

    @Test
    @DisplayName("getCropName — 物品存在返回名称")
    void getCropName_shouldReturnTemplateName() {
        ItemTemplate template = new ItemTemplate();
        template.setId(5L);
        template.setName("灵芝");
        when(itemTemplateRepository.findById(5L)).thenReturn(Optional.of(template));

        String name = farmService.getCropName(5);

        assertEquals("灵芝", name);
    }

    @Test
    @DisplayName("getCropName — 物品不存在返回默认值")
    void getCropName_whenNotFound_shouldReturnDefault() {
        when(itemTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        String name = farmService.getCropName(999);

        assertEquals("未知灵草", name);
    }

    @Test
    @DisplayName("findSeedTemplateByName — 精确匹配返回模板")
    void findSeedTemplateByName_shouldMatchExactly() {
        ItemTemplate seed = createSeedTemplate(1L, "灵芝种子", 6);
        when(itemTemplateRepository.findByType(ItemType.SEED)).thenReturn(List.of(seed));

        ItemTemplate result = farmService.findSeedTemplateByName("灵芝种子");

        assertNotNull(result);
        assertEquals("灵芝种子", result.getName());
    }

    @Test
    @DisplayName("findSeedTemplateByName — 找不到抛异常")
    void findSeedTemplateByName_whenNotFound_shouldThrow() {
        when(itemTemplateRepository.findByType(ItemType.SEED)).thenReturn(List.of());

        assertThrows(IllegalStateException.class,
                () -> farmService.findSeedTemplateByName("不存在"));
    }
}
