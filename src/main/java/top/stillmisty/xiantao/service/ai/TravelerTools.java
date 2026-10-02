package top.stillmisty.xiantao.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.shop.vo.TravelerGoodsVO;
import top.stillmisty.xiantao.domain.shop.vo.TravelerPurchaseResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.shop.TravelerShopService;

/** 旅行商人对话工具 — 价格与库存全部由程序计算，LLM 只负责呈现与促成 */
@Service
@RequiredArgsConstructor
public class TravelerTools {

  private final ToolExecutor toolExecutor;
  private final TravelerShopService travelerShopService;

  /** 查看货摊：返回商人名号、摊位的剩余有效分钟数与全部货物 */
  @Tool(description = "查看旅行商人货摊上的全部货物：名称、类别、单价（灵石）、剩余库存，以及货摊还能存在多少分钟")
  public TravelerGoodsVO listTravelerGoods() {
    return toolExecutor.execute(
        "listTravelerGoods",
        () -> travelerShopService.listGoods(UserContext.requireCurrentUserId()));
  }

  /** 成交：原子扣灵石、入背包、扣摊存 */
  @Tool(description = "向旅行商人买货。goodsName 必须与 listTravelerGoods 返回的名称完全一致；数量不得超过剩余库存")
  @Transactional
  public TravelerPurchaseResult buyFromTraveler(
      @ToolParam(description = "货物名称") String goodsName,
      @ToolParam(description = "购买数量") int quantity) {
    return toolExecutor.execute(
        "buyFromTraveler",
        () -> travelerShopService.buy(UserContext.requireCurrentUserId(), goodsName, quantity));
  }
}
