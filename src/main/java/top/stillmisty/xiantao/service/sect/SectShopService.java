package top.stillmisty.xiantao.service.sect;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.domain.sect.entity.SectShopItem;
import top.stillmisty.xiantao.domain.sect.vo.ExchangeResultVO;
import top.stillmisty.xiantao.domain.sect.vo.SectShopItemVO;
import top.stillmisty.xiantao.domain.sect.vo.ShopQueryVO;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.SectMemberRepository;
import top.stillmisty.xiantao.infrastructure.repository.SectShopItemRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

@Service
@RequiredArgsConstructor
public class SectShopService {

  private final SectMemberRepository sectMemberRepository;
  private final SectShopItemRepository sectShopItemRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemService stackableItemService;
  private final SectMemberService sectMemberService;

  // ===================== 公开 API =====================

  public ServiceResult<String> getShop(Long userId) {
    ShopQueryVO vo = getShopInternal(userId);
    return new ServiceResult.Success<>(formatShopText(vo));
  }

  @Transactional
  public ServiceResult<String> exchangeShopItem(Long userId, long shopItemId) {
    ExchangeResultVO vo = exchangeShopItemInternal(userId, shopItemId);
    return new ServiceResult.Success<>(
        "兑换成功！获得 " + vo.itemName() + "，剩余贡献: " + vo.remainingContribution() + "。");
  }

  // ===================== 内部 API =====================

  @Cacheable(cacheNames = "sect_shop", key = "#userId")
  public ShopQueryVO getShopInternal(Long userId) {
    SectMember member = requireMember(userId);
    List<SectShopItem> items = sectShopItemRepository.findBySectId(member.requireSectId());

    List<SectShopItemVO> itemVOs =
        items.stream()
            .map(
                item -> {
                  ItemTemplate template =
                      itemTemplateRepository.findById(item.getItemTemplateId()).orElse(null);
                  return new SectShopItemVO(
                      item.getId(),
                      template != null ? template.getName() : "[未知]",
                      item.getPriceContribution(),
                      item.getStock());
                })
            .toList();

    return new ShopQueryVO(member.getContribution(), itemVOs);
  }

  @Transactional
  @CacheEvict(cacheNames = "sect_shop", key = "#userId")
  public ExchangeResultVO exchangeShopItemInternal(Long userId, long shopItemId) {
    SectMember member = requireMember(userId);

    SectShopItem shopItem =
        sectShopItemRepository
            .findById(shopItemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_EXISTS));

    if (!shopItem.getSectId().equals(member.getSectId())) {
      throw new BusinessException(ErrorCode.ITEM_NOT_EXISTS);
    }

    if (!shopItem.isInStock()) {
      throw new BusinessException(ErrorCode.SHOP_PRODUCT_OUT_OF_STOCK);
    }

    if (member.getContribution() < shopItem.getPriceContribution()) {
      throw new BusinessException(
          ErrorCode.SECT_SHOP_ITEM_INSUFFICIENT_CONTRIBUTION,
          shopItem.getPriceContribution(),
          member.getContribution());
    }

    ItemTemplate template =
        itemTemplateRepository
            .findById(shopItem.getItemTemplateId())
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_EXISTS));

    member.setContribution(member.getContribution() - shopItem.getPriceContribution());
    sectMemberRepository.save(member);

    if (!shopItem.deductStock(1)) {
      throw new BusinessException(ErrorCode.SHOP_PRODUCT_OUT_OF_STOCK);
    }
    sectShopItemRepository.save(shopItem);

    stackableItemService.addStackableItem(
        userId, template.getId(), template.getType(), template.getName(), 1);

    return new ExchangeResultVO(template.getName(), member.getContribution());
  }

  // ===================== 工具方法 =====================

  private static String formatShopText(ShopQueryVO vo) {
    List<SectShopItemVO> items = vo.items();
    if (items.isEmpty()) {
      return "宗门贡献商店暂无商品。";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("=== 宗门贡献商店 ===\n");
    sb.append("我的贡献: ").append(vo.myContribution()).append("\n\n");

    for (SectShopItemVO item : items) {
      sb.append("  [#").append(item.id()).append("] ").append(item.itemName());
      sb.append(" | 贡献: ").append(item.priceContribution());
      int stock = item.stock();
      if (stock == -1) {
        sb.append(" (无限)");
      } else if (stock == 0) {
        sb.append(" (售罄)");
      } else {
        sb.append(" (库存: ").append(stock).append(")");
      }
      sb.append("\n");
    }

    return sb.toString();
  }

  private SectMember requireMember(Long userId) {
    return sectMemberService.requireMember(userId);
  }
}
