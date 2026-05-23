package top.stillmisty.xiantao.service.ai.spirit;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import top.stillmisty.xiantao.domain.item.vo.ItemEntry;

/**
 * 背包物品查询结果。
 *
 * <p>按类别筛选后返回物品列表。若 {@code items} 为空，说明该类别下无物品， 提示主人先去获取相应物品。每次查询只返回一个类别。
 */
public record CheckPlayerBagResponse(
    @JsonPropertyDescription("查询的类别中文名（种子/装备/兽卵/锻材/丹药/药材/法决玉简/丹方卷轴/锻造图纸/灵兽精华）") String category,
    @JsonPropertyDescription("物品列表，每项含 id（复制编号用于后续操作）、name、quantity（数量）、description（物品描述）")
        List<ItemEntry> items) {}
