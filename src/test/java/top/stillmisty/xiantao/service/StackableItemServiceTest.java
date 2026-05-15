package top.stillmisty.xiantao.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.inventory.StackableItemService;
import top.stillmisty.xiantao.service.player.UserStateService;

@DisplayName("StackableItemService 测试")
@ExtendWith(MockitoExtension.class)
class StackableItemServiceTest {

  @Mock private UserStateService userStateService;
  @Mock private StackableItemRepository stackableItemRepository;

  @InjectMocks private StackableItemService stackableItemService;

  private final Long userId = 1L;
  private final Long templateId = 100L;

  private StackableItem createItem(Long id, int quantity) {
    StackableItem item = new StackableItem();
    item.setId(id);
    item.setUserId(userId);
    item.setTemplateId(templateId);
    item.setItemType(ItemType.MATERIAL);
    item.setName("测试材料");
    item.setQuantity(quantity);
    return item;
  }

  @Test
  @DisplayName("addStackableItem — 新物品创建")
  void addStackableItem_whenNew_shouldCreate() {
    when(stackableItemRepository.upsertIncrementQuantity(any(StackableItem.class))).thenReturn(1);

    stackableItemService.addStackableItem(userId, templateId, ItemType.MATERIAL, "测试材料", 5);

    verify(stackableItemRepository).upsertIncrementQuantity(any(StackableItem.class));
  }

  @Test
  @DisplayName("addStackableItem — 已有物品增加数量")
  void addStackableItem_whenExisting_shouldAddQuantity() {
    when(stackableItemRepository.upsertIncrementQuantity(any(StackableItem.class))).thenReturn(2);

    stackableItemService.addStackableItem(userId, templateId, ItemType.MATERIAL, "测试材料", 5);

    verify(stackableItemRepository).upsertIncrementQuantity(any(StackableItem.class));
  }

  @Test
  @DisplayName("addStackableItem — 带属性时按属性哈希查找")
  void addStackableItem_withProperties_shouldLookupByHash() {
    Map<String, Object> props = Map.of("grade", 3, "quality", "SUPERIOR");
    int expectedHash = StackableItem.computeHash(props);
    when(stackableItemRepository.upsertIncrementQuantity(any(StackableItem.class))).thenReturn(1);

    stackableItemService.addStackableItem(userId, templateId, ItemType.POTION, "筑基丹", 1, props);

    verify(stackableItemRepository)
        .upsertIncrementQuantity(argThat(item -> item.getPropertiesHash() == expectedHash));
  }

  @Test
  @DisplayName("reduceStackableItem — 正常扣减")
  void reduceStackableItem_shouldReduceQuantity() {
    when(stackableItemRepository.reduceQuantityById(1L, userId, 3)).thenReturn(1);

    stackableItemService.reduceStackableItem(userId, 1L, 3);

    verify(stackableItemRepository).reduceQuantityById(1L, userId, 3);
  }

  @Test
  @DisplayName("reduceStackableItem — 扣减到零时删除")
  void reduceStackableItem_whenDepleted_shouldDelete() {
    when(stackableItemRepository.reduceQuantityById(1L, userId, 3)).thenReturn(1);

    stackableItemService.reduceStackableItem(userId, 1L, 3);

    verify(stackableItemRepository).reduceQuantityById(1L, userId, 3);
  }

  @Test
  @DisplayName("reduceStackableItem — 物品不存在抛异常")
  void reduceStackableItem_whenNotFound_shouldThrow() {
    when(stackableItemRepository.reduceQuantityById(99L, userId, 1)).thenReturn(0);
    when(stackableItemRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        BusinessException.class, () -> stackableItemService.reduceStackableItem(userId, 99L, 1));
  }

  @Test
  @DisplayName("reduceStackableItem — 所有权不匹配抛异常")
  void reduceStackableItem_whenOwnershipMismatch_shouldThrow() {
    var item = createItem(1L, 5);
    item.setUserId(2L);
    when(stackableItemRepository.reduceQuantityById(1L, userId, 1)).thenReturn(0);
    when(stackableItemRepository.findById(1L)).thenReturn(Optional.of(item));

    assertThrows(
        BusinessException.class, () -> stackableItemService.reduceStackableItem(userId, 1L, 1));
  }

  @Test
  @DisplayName("reduceStackableItem — 数量不足抛异常")
  void reduceStackableItem_whenInsufficientQuantity_shouldThrow() {
    var item = createItem(1L, 2);
    when(stackableItemRepository.reduceQuantityById(1L, userId, 5)).thenReturn(0);
    when(stackableItemRepository.findById(1L)).thenReturn(Optional.of(item));

    assertThrows(
        BusinessException.class, () -> stackableItemService.reduceStackableItem(userId, 1L, 5));
  }
}
