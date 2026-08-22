package top.stillmisty.xiantao.service.sect;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

  /** 长老上架/改价商品（同物品重复上架时更新定价与库存） */
  @Transactional
  @CacheEvict(cacheNames = "sect_shop", key = "#userId")
  public ShopListingVO listShopItemInternal(
      Long userId, String itemName, int priceContribution, int stock) {
    if (priceContribution <= 0) {
      throw new BusinessException(ErrorCode.SECT_SHOP_INVALID_PRICE);
    }
    if (stock < -1 || stock == 0) {
      throw new BusinessException(ErrorCode.SECT_SHOP_INVALID_STOCK);
    }

    SectMember member = requireMember(userId);
    if (!member.getPosition().canManage()) {
      throw new BusinessException(ErrorCode.SECT_NO_PERMISSION, "长老");
    }

    ItemTemplate template =
        itemTemplateRepository
            .findByName(itemName)
            .orElseThrow(() -> new BusinessException(ErrorCode.APPRAISE_ITEM_NOT_FOUND, itemName));

    SectShopItem item =
        sectShopItemRepository
            .findBySectIdAndItemTemplateId(member.requireSectId(), template.getId())
            .orElseGet(
                () ->
                    SectShopItem.create()
                        .setSectId(member.requireSectId())
                        .setItemTemplateId(template.getId()));
    item.setPriceContribution(priceContribution);
    item.setStock(stock);
    sectShopItemRepository.save(item);

    log.info(
        "宗门 {} 上架商品 {}（{} 贡献/份，库存 {}）", member.getSectId(), itemName, priceContribution, stock);
    return new ShopListingVO(template.getName(), priceContribution, stock);
  }

  /** 药园建造/升级时同步上架对应品阶灵药并补满库存。 灵药为宗门商店专属供应渠道，等级越高品类越多。 */
  @Transactional
  @CacheEvict(cacheNames = "sect_shop", allEntries = true)
  public void syncHerbGardenStock(Long sectId, int gardenLevel) {
    for (int tier = 1; tier <= Math.min(gardenLevel, HERB_GARDEN_TIERS.size()); tier++) {
      List<HerbStock> herbs = HERB_GARDEN_TIERS.get(tier);
      if (herbs == null) {
        continue;
      }
      for (HerbStock herb : herbs) {
        var templateOpt = itemTemplateRepository.findByName(herb.name());
        if (templateOpt.isEmpty()) {
          log.warn("药园灵药模板不存在: {}", herb.name());
          continue;
        }
        ItemTemplate template = templateOpt.get();
        SectShopItem item =
            sectShopItemRepository
                .findBySectIdAndItemTemplateId(sectId, template.getId())
                .orElseGet(
                    () ->
                        SectShopItem.create()
                            .setSectId(sectId)
                            .setItemTemplateId(template.getId()));
        item.setPriceContribution(herb.price());
        item.setStock(HERB_RESTOCK_QTY);
        sectShopItemRepository.save(item);
      }
    }
    log.info("宗门 {} 药园 Lv.{} 灵药已上架补货", sectId, gardenLevel);
  }

  /** 药园各品阶自动上架的灵药：tier -> 清单 */
  private static final Map<Integer, List<HerbStock>> HERB_GARDEN_TIERS =
      Map.of(
          1,
          List.of(new HerbStock("灵芝", 15), new HerbStock("茯苓", 15)),
          2,
          List.of(new HerbStock("血参", 40), new HerbStock("金银花", 40)),
          3,
          List.of(new HerbStock("雪莲", 60), new HerbStock("紫丹参", 60)));

  private static final int HERB_RESTOCK_QTY = 10;

  private record HerbStock(String name, int price) {}

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

  /** 上架结果 */
  public record ShopListingVO(String itemName, int priceContribution, int stock) {}
}
