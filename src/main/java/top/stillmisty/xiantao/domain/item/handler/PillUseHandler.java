package top.stillmisty.xiantao.domain.item.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.service.PillService;

/**
 * 丹药使用处理器
 */
@Component
@RequiredArgsConstructor
public class PillUseHandler implements ItemUseHandler {

    private final PillService pillService;

    @Override
    public boolean supports(ItemType type, ItemTemplate template) {
        return type == ItemType.POTION;
    }

    @Override
    public String use(Long userId, StackableItem item, ItemTemplate template, String args) {
        return pillService.takePill(userId, item.getName());
    }
}
