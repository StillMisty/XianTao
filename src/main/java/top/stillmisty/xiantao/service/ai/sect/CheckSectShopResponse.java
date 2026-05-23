package top.stillmisty.xiantao.service.ai.sect;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import top.stillmisty.xiantao.domain.sect.vo.SectShopItemVO;

/**
 * 宗门贡献商店查询结果。
 *
 * <p>返回当前弟子所见的贡献商店货品。{@code myContribution} 是弟子的可用贡献值余额。 兑换前请确保贡献值充足（物品消耗 >= myContribution 则无法兑换）。
 */
public record CheckSectShopResponse(
    @JsonPropertyDescription("当前弟子的可用贡献值") int myContribution,
    @JsonPropertyDescription("商品列表，每项含编号(id)、名称、价格、库存") List<SectShopItemVO> items) {}
