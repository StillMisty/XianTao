package top.stillmisty.xiantao.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;

@DisplayName("DiscardService 测试")
@ExtendWith(MockitoExtension.class)
class DiscardServiceTest {

  @Mock private UserStateService userStateService;
  @Mock private ItemResolver itemResolver;
  @Mock private EquipmentRepository equipmentRepository;
  @Mock private StackableItemRepository stackableItemRepository;
  @Mock private StackableItemService stackableItemService;

  @InjectMocks private DiscardService discardService;

  private final Long userId = 1L;

  @Test
  @DisplayName("discardItem — 丢弃未装备的装备")
  void discardItem_whenUnequippedEquipment_shouldDelete() {
    Equipment equipment = new Equipment();
    equipment.setId(10L);
    equipment.setName("玄铁剑");
    equipment.setEquipped(false);

    when(itemResolver.resolveEquipment(userId, "玄铁剑"))
        .thenReturn(new ItemResolver.Found<>(equipment, 1));

    String result = discardService.discardItem(userId, "玄铁剑");

    assertEquals("已丢弃装备【玄铁剑】", result);
    verify(equipmentRepository).deleteById(10L);
  }

  @Test
  @DisplayName("discardItem — 丢弃已装备的装备抛异常")
  void discardItem_whenEquippedEquipment_shouldThrow() {
    Equipment equipment = new Equipment();
    equipment.setId(10L);
    equipment.setName("玄铁剑");
    equipment.setEquipped(true);

    when(itemResolver.resolveEquipment(userId, "玄铁剑"))
        .thenReturn(new ItemResolver.Found<>(equipment, 1));

    assertThrows(IllegalStateException.class, () -> discardService.discardItem(userId, "玄铁剑"));
  }

  @Test
  @DisplayName("discardItem — 丢弃堆叠物品")
  void discardItem_whenStackableItem_shouldReduce() {
    when(itemResolver.resolveEquipment(userId, "铁矿"))
        .thenReturn(new ItemResolver.NotFound<Equipment>("铁矿"));

    StackableItem item = new StackableItem();
    item.setId(20L);
    item.setTemplateId(200L);
    item.setName("铁矿");
    item.setItemType(ItemType.MATERIAL);

    when(stackableItemRepository.findByUserIdAndNameContaining(userId, "铁矿"))
        .thenReturn(List.of(item));

    String result = discardService.discardItem(userId, "铁矿");

    assertEquals("已丢弃【铁矿】", result);
    verify(stackableItemService).reduceStackableItem(userId, 20L, 1);
  }

  @Test
  @DisplayName("discardItem — 物品不存在抛异常")
  void discardItem_whenNotFound_shouldThrow() {
    when(itemResolver.resolveEquipment(userId, "不存在"))
        .thenReturn(new ItemResolver.NotFound<Equipment>("不存在"));
    when(stackableItemRepository.findByUserIdAndNameContaining(userId, "不存在"))
        .thenReturn(List.of());

    assertThrows(IllegalStateException.class, () -> discardService.discardItem(userId, "不存在"));
  }
}
