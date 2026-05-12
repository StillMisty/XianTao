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
    when(stackableItemRepository.findByUserIdAndTemplateIdAndPropertiesHash(userId, templateId, 0))
        .thenReturn(Optional.empty());

    stackableItemService.addStackableItem(userId, templateId, ItemType.MATERIAL, "测试材料", 5);

    verify(stackableItemRepository).save(any(StackableItem.class));
  }

  @Test
  @DisplayName("addStackableItem — 已有物品增加数量")
  void addStackableItem_whenExisting_shouldAddQuantity() {
    var existing = createItem(1L, 3);
    when(stackableItemRepository.findByUserIdAndTemplateIdAndPropertiesHash(userId, templateId, 0))
        .thenReturn(Optional.of(existing));

    stackableItemService.addStackableItem(userId, templateId, ItemType.MATERIAL, "测试材料", 5);

    assertEquals(8, existing.getQuantity());
    verify(stackableItemRepository).save(existing);
  }

  @Test
  @DisplayName("addStackableItem — 带属性时按属性哈希查找")
  void addStackableItem_withProperties_shouldLookupByHash() {
    Map<String, Object> props = Map.of("grade", 3, "quality", "SUPERIOR");
    int expectedHash = StackableItem.computeHash(props);
    when(stackableItemRepository.findByUserIdAndTemplateIdAndPropertiesHash(
            userId, templateId, expectedHash))
        .thenReturn(Optional.empty());

    stackableItemService.addStackableItem(userId, templateId, ItemType.POTION, "筑基丹", 1, props);

    verify(stackableItemRepository).save(argThat(item -> item.getPropertiesHash() == expectedHash));
  }

  @Test
  @DisplayName("reduceStackableItem — 正常扣减")
  void reduceStackableItem_shouldReduceQuantity() {
    var item = createItem(1L, 5);
    when(stackableItemRepository.findById(1L)).thenReturn(Optional.of(item));

    stackableItemService.reduceStackableItem(userId, 1L, 3);

    assertEquals(2, item.getQuantity());
    verify(stackableItemRepository).save(item);
  }

  @Test
  @DisplayName("reduceStackableItem — 扣减到零时删除")
  void reduceStackableItem_whenDepleted_shouldDelete() {
    var item = createItem(1L, 3);
    when(stackableItemRepository.findById(1L)).thenReturn(Optional.of(item));

    stackableItemService.reduceStackableItem(userId, 1L, 3);

    verify(stackableItemRepository).deleteById(1L);
  }

  @Test
  @DisplayName("reduceStackableItem — 物品不存在抛异常")
  void reduceStackableItem_whenNotFound_shouldThrow() {
    when(stackableItemRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        BusinessException.class, () -> stackableItemService.reduceStackableItem(userId, 99L, 1));
  }

  @Test
  @DisplayName("reduceStackableItem — 所有权不匹配抛异常")
  void reduceStackableItem_whenOwnershipMismatch_shouldThrow() {
    var item = createItem(1L, 5);
    item.setUserId(2L);
    when(stackableItemRepository.findById(1L)).thenReturn(Optional.of(item));

    assertThrows(
        BusinessException.class, () -> stackableItemService.reduceStackableItem(userId, 1L, 1));
  }

  @Test
  @DisplayName("reduceStackableItem — 数量不足抛异常")
  void reduceStackableItem_whenInsufficientQuantity_shouldThrow() {
    var item = createItem(1L, 2);
    when(stackableItemRepository.findById(1L)).thenReturn(Optional.of(item));

    assertThrows(
        BusinessException.class, () -> stackableItemService.reduceStackableItem(userId, 1L, 5));
  }
}
