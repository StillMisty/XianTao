package top.stillmisty.xiantao.service.sect;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.domain.sect.entity.SectShopItem;
import top.stillmisty.xiantao.domain.sect.repository.SectMemberRepository;
import top.stillmisty.xiantao.domain.sect.repository.SectShopItemRepository;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

@Service
@RequiredArgsConstructor
public class SectShopService {

  private final SectMemberRepository sectMemberRepository;
  private final SectShopItemRepository sectShopItemRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemService stackableItemService;

  // ===================== 公开 API =====================

  @Authenticated
  public ServiceResult<String> getShop(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getShop(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> exchangeShopItem(
      PlatformType platform, String openId, long shopItemId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(exchangeShopItem(userId, shopItemId));
  }

  // ===================== 内部 API =====================

  public String getShop(Long userId) {
    SectMember member = requireMember(userId);
    List<SectShopItem> items = sectShopItemRepository.findBySectId(member.getSectId());

    if (items.isEmpty()) {
      return "宗门贡献商店暂无商品。";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("=== 宗门贡献商店 ===\n");
    sb.append("我的贡献: ").append(member.getContribution()).append("\n\n");

    for (SectShopItem item : items) {
      ItemTemplate template =
          itemTemplateRepository.findById(item.getItemTemplateId()).orElse(null);
      String itemName = template != null ? template.getName() : "[未知]";
      sb.append("  [#").append(item.getId()).append("] ").append(itemName);
      sb.append(" | 贡献: ").append(item.getPriceContribution());
      if (item.getStock() == -1) {
        sb.append(" (无限)");
      } else if (item.getStock() == 0) {
        sb.append(" (售罄)");
      } else {
        sb.append(" (库存: ").append(item.getStock()).append(")");
      }
      sb.append("\n");
    }

    return sb.toString();
  }

  @Transactional
  public String exchangeShopItem(Long userId, long shopItemId) {
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

    return "兑换成功！获得 " + template.getName() + "，剩余贡献: " + member.getContribution() + "。";
  }

  // ===================== 工具方法 =====================

  private SectMember requireMember(Long userId) {
    return sectMemberRepository
        .findByUserId(userId)
        .filter(m -> m.getSectId() != null)
        .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
  }
}
