package top.stillmisty.xiantao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.handler.ItemUseHandler;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ItemUseService 测试")
@ExtendWith(MockitoExtension.class)
class ItemUseServiceTest {

    @Mock
    private StackableItemRepository stackableItemRepository;
    @Mock
    private ItemTemplateRepository itemTemplateRepository;
    @Mock
    private StackableItemService stackableItemService;
    @Mock
    private List<ItemUseHandler> handlers;

    @InjectMocks
    private ItemUseService itemUseService;

    private final Long userId = 1L;

    // ===================== useItem =====================

    @Test
    @DisplayName("useItem — 物品不存在抛异常")
    void useItem_whenItemNotFound_shouldThrow() {
        when(stackableItemRepository.findByUserIdAndNameContaining(userId, "丹药"))
                .thenReturn(List.of());

        assertThrows(IllegalStateException.class,
                () -> itemUseService.useItem(userId, "丹药", ""));
    }

    @Test
    @DisplayName("useItem — 无匹配处理器抛异常")
    void useItem_whenNoHandler_shouldThrow() {
        StackableItem item = new StackableItem();
        item.setId(1L);
        item.setName("未知丹");
        item.setTemplateId(10L);
        item.setItemType(ItemType.HERB);

        ItemTemplate template = new ItemTemplate();
        template.setId(10L);
        template.setName("未知丹");

        when(stackableItemRepository.findByUserIdAndNameContaining(userId, "未知丹"))
                .thenReturn(List.of(item));
        when(itemTemplateRepository.findById(10L)).thenReturn(Optional.of(template));
        when(handlers.stream()).thenReturn(List.<ItemUseHandler>of().stream());

        assertThrows(IllegalStateException.class,
                () -> itemUseService.useItem(userId, "未知丹", ""));
    }

    @Test
    @DisplayName("useItem — 匹配到处理器后执行使用")
    void useItem_withMatchingHandler_shouldExecuteUse() {
        StackableItem item = new StackableItem();
        item.setId(1L);
        item.setName("金疮药");
        item.setTemplateId(10L);
        item.setItemType(ItemType.HERB);
        item.setQuantity(3);

        ItemTemplate template = new ItemTemplate();
        template.setId(10L);
        template.setName("金疮药");
        template.setType(ItemType.HERB);

        ItemUseHandler mockHandler = mock(ItemUseHandler.class);
        when(mockHandler.supports(eq(ItemType.HERB), eq(template))).thenReturn(true);
        when(mockHandler.consumesInternally()).thenReturn(false);
        when(mockHandler.use(eq(userId), eq(item), eq(template), eq(""))).thenReturn("使用成功！");

        when(stackableItemRepository.findByUserIdAndNameContaining(userId, "金疮药"))
                .thenReturn(List.of(item));
        when(itemTemplateRepository.findById(10L)).thenReturn(Optional.of(template));
        when(handlers.stream()).thenReturn(List.of(mockHandler).stream());

        String result = itemUseService.useItem(userId, "金疮药", "");

        assertEquals("使用成功！", result);
        verify(mockHandler).use(userId, item, template, "");
    }

    @Test
    @DisplayName("useItem — consumesInternally=true 时不额外扣减")
    void useItem_whenHandlerConsumesInternally_shouldNotReduceItem() {
        StackableItem item = new StackableItem();
        item.setId(1L);
        item.setName("某物品");
        item.setTemplateId(10L);
        item.setItemType(ItemType.HERB);

        ItemTemplate template = new ItemTemplate();
        template.setId(10L);
        template.setName("某物品");

        ItemUseHandler mockHandler = mock(ItemUseHandler.class);
        when(mockHandler.supports(any(), any())).thenReturn(true);
        when(mockHandler.consumesInternally()).thenReturn(true);
        when(mockHandler.use(anyLong(), any(), any(), any())).thenReturn("OK");

        when(stackableItemRepository.findByUserIdAndNameContaining(userId, "某物品"))
                .thenReturn(List.of(item));
        when(itemTemplateRepository.findById(10L)).thenReturn(Optional.of(template));
        when(handlers.stream()).thenReturn(List.of(mockHandler).stream());

        itemUseService.useItem(userId, "某物品", "");

        verify(stackableItemService, never()).reduceStackableItem(anyLong(), anyLong(), anyInt());
    }
}
