package top.stillmisty.xiantao.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.item.vo.InventorySummaryVO;
import top.stillmisty.xiantao.domain.item.vo.ItemEntry;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;

@DisplayName("InventoryService 测试")
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

  @Mock private UserStateService userStateService;
  @Mock private EquipmentRepository equipmentRepository;
  @Mock private StackableItemRepository stackableItemRepository;
  @Mock private ItemResolver itemResolver;

  @InjectMocks private InventoryService inventoryService;

  private final Long userId = 1L;

  private User createTestUser() {
    return User.create()
        .setId(userId)
        .setNickname("测试修士")
        .setLevel(10)
        .setSpiritStones(500L)
        .setStatus(UserStatus.IDLE);
  }

  // ===================== getInventorySummary =====================

  @Test
  @DisplayName("getInventorySummary — 背包为空时返回零值摘要")
  void getInventorySummary_whenEmptyInventory_shouldReturnEmptySummary() {
    when(userStateService.loadUser(userId)).thenReturn(createTestUser());
    when(equipmentRepository.findUnequippedByUserId(userId)).thenReturn(List.of());
    when(stackableItemRepository.findByUserId(userId)).thenReturn(List.of());

    InventorySummaryVO result = inventoryService.getInventorySummary(userId);

    assertNotNull(result);
    assertEquals(500L, result.spiritStones());
    assertTrue(result.equipmentByQuality().isEmpty());
    assertTrue(result.stackableItemCount().isEmpty());
  }

  @Test
  @DisplayName("getInventorySummary — 按品质聚合装备数量")
  void getInventorySummary_withEquipments_shouldGroupByQuality() {
    when(userStateService.loadUser(userId)).thenReturn(createTestUser());
    when(stackableItemRepository.findByUserId(userId)).thenReturn(List.of());

    Equipment eq1 = new Equipment();
    eq1.setRarity(Rarity.COMMON);
    Equipment eq2 = new Equipment();
    eq2.setRarity(Rarity.COMMON);
    Equipment eq3 = new Equipment();
    eq3.setRarity(Rarity.EPIC);

    when(equipmentRepository.findUnequippedByUserId(userId)).thenReturn(List.of(eq1, eq2, eq3));

    InventorySummaryVO result = inventoryService.getInventorySummary(userId);

    assertEquals(2, result.equipmentByQuality().get("普通"));
    assertEquals(1, result.equipmentByQuality().get("史诗"));
  }

  @Test
  @DisplayName("getInventorySummary — 统计可堆叠物品类型数量")
  void getInventorySummary_withStackableItems_shouldCountByType() {
    when(userStateService.loadUser(userId)).thenReturn(createTestUser());
    when(equipmentRepository.findUnequippedByUserId(userId)).thenReturn(List.of());

    StackableItem seed1 = new StackableItem();
    seed1.setItemType(ItemType.SEED);
    StackableItem seed2 = new StackableItem();
    seed2.setItemType(ItemType.SEED);
    StackableItem herb = new StackableItem();
    herb.setItemType(ItemType.HERB);

    when(stackableItemRepository.findByUserId(userId)).thenReturn(List.of(seed1, seed2, herb));

    InventorySummaryVO result = inventoryService.getInventorySummary(userId);

    assertEquals(2, result.stackableItemCount().get(ItemType.SEED));
    assertEquals(1, result.stackableItemCount().get(ItemType.HERB));
  }

  @Test
  @DisplayName("getInventorySummary — 用户不存在抛异常")
  void getInventorySummary_whenUserNotFound_shouldThrow() {
    when(userStateService.loadUser(userId)).thenThrow(new RuntimeException("用户不存在"));

    assertThrows(Exception.class, () -> inventoryService.getInventorySummary(userId));
  }

  // ===================== getSeedInventory =====================

  @Test
  @DisplayName("getSeedInventory — 委托给 ItemResolver")
  void getSeedInventory_shouldDelegateToItemResolver() {
    List<ItemEntry> expected =
        List.of(new ItemEntry(1, 10L, "灵芝种子", 3, "T1"), new ItemEntry(2, 11L, "人参种子", 5, "T2"));
    when(itemResolver.listSeeds(userId)).thenReturn(expected);

    List<ItemEntry> result = inventoryService.getSeedInventory(userId);

    assertEquals(2, result.size());
    assertEquals("灵芝种子", result.get(0).name());
    assertEquals(3, result.get(0).quantity());
  }

  @Test
  @DisplayName("getSeedInventory — 无种子时返回空列表")
  void getSeedInventory_whenNoSeeds_shouldReturnEmptyList() {
    when(itemResolver.listSeeds(userId)).thenReturn(List.of());

    List<ItemEntry> result = inventoryService.getSeedInventory(userId);

    assertTrue(result.isEmpty());
  }

  // ===================== getEquipmentInventory =====================

  @Test
  @DisplayName("getEquipmentInventory — 委托给 ItemResolver")
  void getEquipmentInventory_shouldDelegateToItemResolver() {
    List<ItemEntry> expected = List.of(new ItemEntry(1, 10L, "铁剑", 1, "普通"));
    when(itemResolver.listEquipment(userId)).thenReturn(expected);

    List<ItemEntry> result = inventoryService.getEquipmentInventory(userId);

    assertEquals(1, result.size());
    assertEquals("铁剑", result.getFirst().name());
  }

  // ===================== getEggInventory =====================

  @Test
  @DisplayName("getEggInventory — 委托给 ItemResolver")
  void getEggInventory_shouldDelegateToItemResolver() {
    List<ItemEntry> expected = List.of(new ItemEntry(1, 10L, "火凤兽卵", 2, "T2"));
    when(itemResolver.listEggs(userId)).thenReturn(expected);

    List<ItemEntry> result = inventoryService.getEggInventory(userId);

    assertEquals(1, result.size());
    assertEquals("火凤兽卵", result.getFirst().name());
  }
}
