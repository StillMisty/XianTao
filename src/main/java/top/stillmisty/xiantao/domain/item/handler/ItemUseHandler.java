package top.stillmisty.xiantao.domain.item.handler;

import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;

/**
 * 物品使用处理器接口
 * 实现此接口的处理器可以处理对应类型物品的使用逻辑
 */
public interface ItemUseHandler {

    /**
     * 是否支持该物品类型
     */
    boolean supports(ItemType type, ItemTemplate template);

    /**
     * 使用物品
     *
     * @param userId 用户ID
     * @param item   物品实例
     * @param template 物品模板
     * @param args   额外参数（如进化石需要位置）
     * @return 使用结果消息
     */
    String use(Long userId, StackableItem item, ItemTemplate template, String args);

    /**
     * 是否自行管理物品消耗（默认 false，由 ItemUseService 统一扣减）
     */
    default boolean consumesInternally() {
        return false;
    }
}
