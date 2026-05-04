package top.stillmisty.xiantao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.beast.vo.ReleaseBeastVO;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.entity.CellConfig;
import top.stillmisty.xiantao.domain.fudi.enums.BeastQuality;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("BeastService 测试")
@ExtendWith(MockitoExtension.class)
class BeastServiceTest {

    @Mock
    private FudiCellRepository fudiCellRepository;
    @Mock
    private BeastRepository beastRepository;
    @Mock
    private ItemTemplateRepository itemTemplateRepository;
    @Mock
    private StackableItemRepository stackableItemRepository;
    @Mock
    private SpiritRepository spiritRepository;
    @Mock
    private StackableItemService stackableItemService;
    @Mock
    private ItemResolver itemResolver;
    @Mock
    private FudiHelper fudiHelper;

    @InjectMocks
    private BeastService beastService;

    // ===================== 静态公式测试 =====================

    @Test
    @DisplayName("calculateBeastAttack — 凡品1级攻击力")
    void calculateBeastAttack_mortalLevel1_shouldReturnCorrectValue() {
        int attack = BeastService.calculateBeastAttack(1, BeastQuality.MORTAL);
        assertEquals(8, attack);
    }

    @Test
    @DisplayName("calculateBeastAttack — 神品10级攻击力")
    void calculateBeastAttack_divineLevel10_shouldReturnCorrectValue() {
        int attack = BeastService.calculateBeastAttack(10, BeastQuality.DIVINE);
        assertEquals(128, attack);
    }

    @Test
    @DisplayName("calculateBeastAttack — 快速成长")
    void calculateBeastAttack_shouldScaleWithLevel() {
        int atk1 = BeastService.calculateBeastAttack(1, BeastQuality.SPIRIT);
        int atk5 = BeastService.calculateBeastAttack(5, BeastQuality.SPIRIT);
        assertTrue(atk5 > atk1);
    }

    @Test
    @DisplayName("calculateBeastAttack — 高品质更高")
    void calculateBeastAttack_divineShouldBeHigherThanMortal() {
        int mortal = BeastService.calculateBeastAttack(5, BeastQuality.MORTAL);
        int divine = BeastService.calculateBeastAttack(5, BeastQuality.DIVINE);
        assertTrue(divine > mortal);
    }

    @Test
    @DisplayName("calculateBeastDefense — 凡品1级防御")
    void calculateBeastDefense_mortalLevel1_shouldReturnCorrectValue() {
        int defense = BeastService.calculateBeastDefense(1, BeastQuality.MORTAL);
        assertEquals(6, defense);
    }

    @Test
    @DisplayName("calculateBeastDefense — 圣品5级防御")
    void calculateBeastDefense_saintLevel5_shouldReturnCorrectValue() {
        int defense = BeastService.calculateBeastDefense(5, BeastQuality.SAINT);
        assertEquals(33, defense);
    }

    // ===================== releaseBeast =====================

    @Test
    @DisplayName("releaseBeast — 地块不存在抛异常")
    void releaseBeast_whenCellNotFound_shouldThrow() {
        when(fudiHelper.parseCellId("1")).thenReturn(1);
        when(fudiHelper.getFudiByUserId(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> beastService.releaseBeast(1L, "1"));
    }

    @Test
    @DisplayName("releaseBeast — 非兽栏地块抛异常")
    void releaseBeast_whenNotPenCell_shouldThrow() {
        when(fudiHelper.parseCellId("1")).thenReturn(1);
        var fudi = mock(top.stillmisty.xiantao.domain.fudi.entity.Fudi.class);
        when(fudi.getId()).thenReturn(100L);
        when(fudiHelper.getFudiByUserId(1L)).thenReturn(Optional.of(fudi));

        FudiCell farmCell = new FudiCell();
        farmCell.setCellType(CellType.FARM);
        when(fudiCellRepository.findByFudiIdAndCellId(100L, 1)).thenReturn(Optional.of(farmCell));

        assertThrows(IllegalStateException.class,
                () -> beastService.releaseBeast(1L, "1"));
    }

    @Test
    @DisplayName("releaseBeast — 空兽栏释放返回 ReleaseVO")
    void releaseBeast_whenEmptyPen_shouldReturnReleaseVO() {
        when(fudiHelper.parseCellId("1")).thenReturn(1);
        var fudi = mock(top.stillmisty.xiantao.domain.fudi.entity.Fudi.class);
        when(fudi.getId()).thenReturn(100L);
        when(fudiHelper.getFudiByUserId(1L)).thenReturn(Optional.of(fudi));

        FudiCell penCell = new FudiCell();
        penCell.setCellType(CellType.PEN);
        penCell.setConfig(new CellConfig.EmptyConfig());
        when(fudiCellRepository.findByFudiIdAndCellId(100L, 1)).thenReturn(Optional.of(penCell));

        ReleaseBeastVO result = beastService.releaseBeast(1L, "1");

        assertNotNull(result);
        assertEquals("未知灵兽", result.beastName());
    }

    // ===================== 变异/品质辅助测试 =====================

    @Test
    @DisplayName("rollBeastQuality — 返回有效品质")
    void rollBeastQuality_shouldReturnValidQuality() {
        BeastQuality quality = beastService.rollBeastQuality();
        assertNotNull(quality);
    }

    @Test
    @DisplayName("rollRandomTrait — 返回有效变异特性编码")
    void rollRandomTrait_shouldReturnValidTraitCode() {
        String trait = beastService.rollRandomTrait();
        assertNotNull(trait);
        assertFalse(trait.isEmpty());
    }

    // ===================== isIncubating =====================

    @Test
    @DisplayName("isIncubating — 空配置返回 false")
    void isIncubating_whenEmptyConfig_shouldReturnFalse() {
        FudiCell cell = new FudiCell();
        cell.setCellType(CellType.PEN);
        cell.setConfig(new CellConfig.EmptyConfig());

        assertFalse(beastService.isIncubating(cell));
    }

    @Test
    @DisplayName("isIncubating — Farm配置返回 false")
    void isIncubating_whenFarmConfig_shouldReturnFalse() {
        FudiCell cell = new FudiCell();
        cell.setCellType(CellType.FARM);
        cell.setConfig(new CellConfig.FarmConfig(1, java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now().plusHours(1), 0));

        assertFalse(beastService.isIncubating(cell));
    }
}
