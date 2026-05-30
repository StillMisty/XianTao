package top.stillmisty.xiantao.service.shop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.event.vo.FortuneVO;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.EquipmentTemplate;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.AffixType;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;
import top.stillmisty.xiantao.domain.shop.entity.ShopProduct;
import top.stillmisty.xiantao.domain.shop.enums.ProductType;
import top.stillmisty.xiantao.domain.shop.vo.AppraisalResult;
import top.stillmisty.xiantao.domain.shop.vo.EquipmentListVO;
import top.stillmisty.xiantao.domain.shop.vo.EquipmentPurchaseResult;
import top.stillmisty.xiantao.domain.shop.vo.HaggleResult;
import top.stillmisty.xiantao.domain.shop.vo.PlayerItemsVO;
import top.stillmisty.xiantao.domain.shop.vo.ProductListVO;
import top.stillmisty.xiantao.domain.shop.vo.PurchaseResult;
import top.stillmisty.xiantao.domain.shop.vo.SellResult;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentRepository;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.ShopNpcRepository;
import top.stillmisty.xiantao.infrastructure.repository.ShopProductRepository;
import top.stillmisty.xiantao.infrastructure.repository.StackableItemRepository;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.FortuneService;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.ai.ShopChatContext;
import top.stillmisty.xiantao.service.inventory.StackableItemService;
import top.stillmisty.xiantao.service.player.UserStateService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopService {

  private final ShopNpcRepository shopNpcRepository;
  private final ShopProductRepository shopProductRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;
  private final StackableItemRepository stackableItemRepository;
  private final EquipmentRepository equipmentRepository;
  private final UserRepository userRepository;
  private final UserStateService userStateService;
  private final PriceEngine priceEngine;
  private final StackableItemService stackableItemService;
  private final FortuneService fortuneService;
  private final ShopQueryService shopQueryService;

  // ===================== 公开 API =====================

  public ServiceResult<ProductListVO> listProducts(Long userId) {
    return shopQueryService.listProducts(userId);
  }

  @Transactional
  public ServiceResult<PurchaseResult> purchaseItem(Long userId, Long templateId, int quantity) {
    User user = userStateService.loadUser(userId);
    ShopNpc npc = findByLocation(user.getLocationId());
    return new ServiceResult.Success<>(purchaseItemInternal(userId, npc, templateId, quantity));
  }

  @Transactional
  public ServiceResult<EquipmentPurchaseResult> purchaseEquipment(Long userId, Long templateId) {
    User user = userStateService.loadUser(userId);
    ShopNpc npc = findByLocation(user.getLocationId());
    return new ServiceResult.Success<>(purchaseEquipmentInternal(userId, npc, templateId));
  }

  // ===================== 内部 API（供 Tools 调用） =====================

  @Cacheable(cacheNames = "shop_products", key = "#userId")
  public ProductListVO listProductsInternal(Long userId) {
    return shopQueryService.listProductsInternal(userId);
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(cacheNames = "shop_products", key = "#userId"),
        @CacheEvict(cacheNames = "shop_player_items", key = "'items:' + #userId")
      })
  public PurchaseResult purchaseItemInternal(
      Long userId, ShopNpc npc, Long templateId, int quantity) {
    if (quantity <= 0) {
      throw new BusinessException(ErrorCode.PARAM_INVALID, "数量必须大于0");
    }
    ShopProduct product =
        shopProductRepository
            .findByShopNpcIdAndTemplateId(npc.getId(), templateId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_PRODUCT_NOT_FOUND));
    if (product.getProductType() != ProductType.ITEM) {
      throw new BusinessException(ErrorCode.SHOP_PRODUCT_NOT_FOUND);
    }
    if (product.getCurrentStock() < quantity) {
      throw new BusinessException(ErrorCode.SHOP_PRODUCT_OUT_OF_STOCK);
    }

    ItemTemplate template =
        itemTemplateRepository
            .findById(templateId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_EXISTS));

    long unitPrice = product.getCurrentPrice();
    long totalPrice = unitPrice * quantity;

    int rows = userRepository.deductSpiritStonesIfEnough(userId, totalPrice);
    if (rows == 0) {
      User user = userStateService.loadUser(userId);
      throw new BusinessException(
          ErrorCode.SHOP_SPIRIT_STONES_INSUFFICIENT, totalPrice, user.getSpiritStones());
    }

    rows = shopProductRepository.deductStockIfAvailable(product.getId(), quantity);
    if (rows == 0) {
      throw new BusinessException(ErrorCode.SHOP_PRODUCT_OUT_OF_STOCK);
    }

    ShopProduct saleTouch = new ShopProduct();
    saleTouch.setId(product.getId());
    saleTouch.setLastSaleTime(TimeUtil.now());
    shopProductRepository.save(saleTouch);

    addStackableItem(userId, template, quantity);

    return new PurchaseResult(template.getName(), quantity, totalPrice);
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(cacheNames = "shop_products", key = "#userId"),
        @CacheEvict(cacheNames = "shop_player_items", key = "'equipment:' + #userId")
      })
  public EquipmentPurchaseResult purchaseEquipmentInternal(
      Long userId, ShopNpc npc, Long templateId) {
    ShopProduct product =
        shopProductRepository
            .findByShopNpcIdAndTemplateId(npc.getId(), templateId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_PRODUCT_NOT_FOUND));
    if (product.getProductType() != ProductType.EQUIPMENT) {
      throw new BusinessException(ErrorCode.SHOP_PRODUCT_NOT_FOUND);
    }
    if (product.getCurrentStock() <= 0) {
      throw new BusinessException(ErrorCode.SHOP_PRODUCT_OUT_OF_STOCK);
    }

    EquipmentTemplate template =
        equipmentTemplateRepository
            .findById(templateId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_EXISTS));

    long price = product.getCurrentPrice();

    int rows = userRepository.deductSpiritStonesIfEnough(userId, price);
    if (rows == 0) {
      User user = userStateService.loadUser(userId);
      throw new BusinessException(
          ErrorCode.SHOP_SPIRIT_STONES_INSUFFICIENT, price, user.getSpiritStones());
    }

    rows = shopProductRepository.deductStockIfAvailable(product.getId(), 1);
    if (rows == 0) {
      throw new BusinessException(ErrorCode.SHOP_PRODUCT_OUT_OF_STOCK);
    }

    Rarity rarity = Rarity.roll(template.getDropWeight());
    double qualityMultiplier = rarity.randomQualityMultiplier();
    int affixCount = rarity.randomAffixCount();
    Map<String, Integer> affixes = rollAffixes(rarity, affixCount);

    Equipment equipment =
        Equipment.create(
            userId,
            template.getId(),
            template.getName() + "-" + rarity.getName(),
            template.getSlot(),
            rarity,
            template.getWeaponType(),
            qualityMultiplier,
            affixes,
            buildStatBonus(template),
            template.getBaseAttack(),
            template.getBaseDefense());
    equipment.setTradable(true);
    equipment.setEquipped(false);
    equipmentRepository.save(equipment);

    ShopProduct saleTouch = new ShopProduct();
    saleTouch.setId(product.getId());
    saleTouch.setLastSaleTime(TimeUtil.now());
    shopProductRepository.save(saleTouch);

    String description = rarity.getName() + "品质，品质系数 " + String.format("%.2f", qualityMultiplier);
    if (affixCount > 0) {
      description += "，含 " + affixCount + " 条词缀";
    }

    return new EquipmentPurchaseResult(equipment.getName(), rarity.getCode(), price, description);
  }

  @CacheEvict(cacheNames = "shop_player_items", key = "'items:' + #userId")
  @Transactional
  public SellResult sellStackableItem(Long userId, ShopNpc npc, Long itemId, long confirmedPrice) {
    StackableItem item =
        stackableItemRepository
            .findById(itemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_EXISTS));
    if (!item.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.ITEM_OWNERSHIP_MISMATCH);
    }
    if (!item.getTradable()) {
      throw new BusinessException(ErrorCode.ITEM_NOT_TRADABLE);
    }
    if (item.getQuantity() <= 0) {
      throw new BusinessException(ErrorCode.ITEM_NOT_IN_BAG);
    }

    ItemTemplate template =
        itemTemplateRepository
            .findById(item.getTemplateId())
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_EXISTS));

    long basePrice = priceEngine.calculateBuybackPrice(template, npc);
    long minPrice = priceEngine.getMinPrice(basePrice);
    long maxPrice = priceEngine.getMaxPrice(basePrice);

    if (confirmedPrice < minPrice || confirmedPrice > maxPrice) {
      throw new BusinessException(ErrorCode.SELL_PRICE_MISMATCH);
    }

    String itemName = item.getName();
    double acceptanceRate = 1.0 - (confirmedPrice - minPrice) / (double) (maxPrice - minPrice);
    if (ThreadLocalRandom.current().nextDouble() > acceptanceRate) {
      throw new BusinessException(
          ErrorCode.SELL_PRICE_MISMATCH, "掌柜对你的报价不满意：" + itemName + " 未能售出，试着多降些价吧");
    }

    if (item.reduceQuantity(1)) {
      stackableItemRepository.deleteById(item.getId());
    } else {
      stackableItemRepository.save(item);
    }

    userRepository.addSpiritStonesAtomically(userId, confirmedPrice);

    return new SellResult(confirmedPrice, itemName);
  }

  @CacheEvict(cacheNames = "shop_player_items", key = "'equipment:' + #userId")
  @Transactional
  public SellResult sellEquipment(Long userId, ShopNpc npc, Long equipmentId, long confirmedPrice) {
    Equipment equipment =
        equipmentRepository
            .findById(equipmentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
    if (!equipment.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.EQUIPMENT_NOT_OWNED);
    }
    if (equipment.getEquipped()) {
      throw new BusinessException(ErrorCode.EQUIPMENT_ALREADY_EQUIPPED);
    }
    if (!equipment.getTradable()) {
      throw new BusinessException(ErrorCode.EQUIPMENT_NOT_TRADABLE);
    }

    ItemTemplate template =
        itemTemplateRepository
            .findById(equipment.getTemplateId())
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_EXISTS));

    long basePrice = priceEngine.calculateBuybackPrice(equipment, template, npc);
    long minPrice = priceEngine.getMinPrice(basePrice);
    long maxPrice = priceEngine.getMaxPrice(basePrice);

    if (confirmedPrice < minPrice || confirmedPrice > maxPrice) {
      throw new BusinessException(ErrorCode.SELL_PRICE_MISMATCH);
    }

    String equipmentName = equipment.getName();

    double acceptanceRate = 1.0 - (confirmedPrice - minPrice) / (double) (maxPrice - minPrice);
    if (ThreadLocalRandom.current().nextDouble() > acceptanceRate) {
      throw new BusinessException(
          ErrorCode.SELL_PRICE_MISMATCH, "掌柜对你的报价不满意：" + equipmentName + " 未能售出，试着多降些价吧");
    }

    userRepository.addSpiritStonesAtomically(userId, confirmedPrice);
    equipmentRepository.deleteById(equipment.getId());

    return new SellResult(confirmedPrice, equipmentName);
  }

  public AppraisalResult appraiseStackableItem(Long userId, ShopNpc npc, Long itemId) {
    StackableItem item =
        stackableItemRepository
            .findById(itemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_EXISTS));
    if (!item.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.ITEM_OWNERSHIP_MISMATCH);
    }

    ItemTemplate template =
        itemTemplateRepository
            .findById(item.getTemplateId())
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_EXISTS));

    boolean tradable = item.getTradable();
    if (!tradable) {
      return new AppraisalResult(false, 0, 0, 0, item.getName(), "此物伴有绑定印记，不可回收");
    }

    long basePrice = priceEngine.calculateBuybackPrice(template, npc);
    long minPrice = priceEngine.getMinPrice(basePrice);
    long maxPrice = priceEngine.getMaxPrice(basePrice);

    String desc = buildStackableAppraisalDesc(template, basePrice);
    return new AppraisalResult(true, basePrice, minPrice, maxPrice, item.getName(), desc);
  }

  public AppraisalResult appraiseEquipment(Long userId, ShopNpc npc, Long equipmentId) {
    Equipment equipment =
        equipmentRepository
            .findById(equipmentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
    if (!equipment.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.EQUIPMENT_NOT_OWNED);
    }
    if (equipment.getEquipped()) {
      return new AppraisalResult(false, 0, 0, 0, equipment.getName(), "客官还穿着呢，脱下来再说吧");
    }
    if (!equipment.getTradable()) {
      return new AppraisalResult(false, 0, 0, 0, equipment.getName(), "此物伴有神魂烙印，收不得");
    }

    ItemTemplate template =
        itemTemplateRepository
            .findById(equipment.getTemplateId())
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_EXISTS));

    long basePrice = priceEngine.calculateBuybackPrice(equipment, template, npc);
    long minPrice = priceEngine.getMinPrice(basePrice);
    long maxPrice = priceEngine.getMaxPrice(basePrice);

    String desc = buildEquipmentAppraisalDesc(equipment, basePrice);
    return new AppraisalResult(true, basePrice, minPrice, maxPrice, equipment.getName(), desc);
  }

  public HaggleResult haggleItem(
      Long userId, ShopNpc npc, long currentPrice, long basePrice, boolean isBuying) {
    User user;
    ShopChatContext chatCtx = ShopChatContext.current();
    if (chatCtx != null) {
      user = chatCtx.user();
    } else {
      user = userStateService.loadUser(userId);
    }
    double charmFactor = Math.clamp((user.getEffectiveStatWis() - 10) / 20.0, 0, 0.3);
    double difficulty = npc.getHaggleDifficulty();

    FortuneVO fortune = fortuneService.calculate(userId);
    double wealthMultiplier = fortuneService.getWealthMultiplier(fortune.wealth());

    double successRate = (0.3 + charmFactor * 0.3 - difficulty) * wealthMultiplier;
    successRate = Math.clamp(successRate, 0.05, 0.85);

    double roll = ThreadLocalRandom.current().nextDouble();
    boolean success = roll < successRate;

    if (!success) {
      String refusalMsg = isBuying ? "客官，这价已经是最低了，再降老朽要亏本了" : "客官，这价已经是最多了，再加老朽只能喝西北风了";
      return new HaggleResult(false, currentPrice, 0, refusalMsg);
    }

    double successMargin = successRate - roll;
    double amountRatio = (0.03 + successMargin * 0.12) * wealthMultiplier;
    amountRatio = Math.clamp(amountRatio, 0.01, 0.20);
    long amount = Math.max(1L, (long) Math.ceil(currentPrice * amountRatio));
    long newPrice = isBuying ? currentPrice - amount : currentPrice + amount;

    long minPrice = priceEngine.getMinPrice(basePrice);
    long maxPrice = priceEngine.getMaxPrice(basePrice);
    if (isBuying && newPrice < minPrice) {
      newPrice = minPrice;
      amount = currentPrice - newPrice;
    }
    if (!isBuying && newPrice > maxPrice) {
      newPrice = maxPrice;
      amount = newPrice - currentPrice;
    }
    if (amount <= 0) {
      return new HaggleResult(false, currentPrice, 0, "客官，这价已经是极限了！");
    }

    String msg =
        isBuying
            ? "罢了罢了，看你诚心，让利 " + amount + " 灵石，算你 " + newPrice + " 灵石吧"
            : "罢了罢了，看这品质确实不错，加 " + amount + " 灵石， " + newPrice + " 灵石收了";
    return new HaggleResult(true, newPrice, amount, msg);
  }

  @Cacheable(cacheNames = "shop_player_items", key = "'equipment:' + #userId")
  public EquipmentListVO queryPlayerEquipment(Long userId) {
    return shopQueryService.queryPlayerEquipment(userId);
  }

  @Cacheable(cacheNames = "shop_player_items", key = "'items:' + #userId")
  public PlayerItemsVO queryPlayerItems(Long userId) {
    return shopQueryService.queryPlayerItems(userId);
  }

  // ===================== 辅助方法 =====================

  public long getMinPrice(long basePrice) {
    return priceEngine.getMinPrice(basePrice);
  }

  public long getMaxPrice(long basePrice) {
    return priceEngine.getMaxPrice(basePrice);
  }

  @Cacheable(cacheNames = "shop_locations", key = "#locationId")
  public ShopNpc findByLocation(Long locationId) {
    return shopQueryService.findByLocation(locationId);
  }

  public boolean hasShopAtLocation(Long locationId) {
    return shopQueryService.hasShopAtLocation(locationId);
  }

  public List<StackableItem> findStackableItemsByName(Long userId, String name) {
    return shopQueryService.findStackableItemsByName(userId, name);
  }

  public List<Equipment> findEquipmentByName(Long userId, String name) {
    return shopQueryService.findEquipmentByName(userId, name);
  }

  private void addStackableItem(Long userId, ItemTemplate template, int quantity) {
    stackableItemService.addStackableItem(
        userId,
        template.getId(),
        template.getType(),
        template.getName(),
        quantity,
        template.getProperties());
  }

  private Map<String, Integer> rollAffixes(Rarity rarity, int affixCount) {
    if (affixCount <= 0) return Map.of();
    Map<String, Integer> affixes = new LinkedHashMap<>();
    List<AffixType> pool = new ArrayList<>(List.of(AffixType.getAttributeAffixes()));
    if (rarity == Rarity.LEGENDARY) {
      pool.addAll(List.of(AffixType.getSpecialAffixes()));
    }
    Collections.shuffle(pool, ThreadLocalRandom.current());
    for (int i = 0; i < affixCount && i < pool.size(); i++) {
      AffixType at = pool.get(i);
      int value = at.isSpecial() ? 5 : (1 + ThreadLocalRandom.current().nextInt(4));
      if (at.getStatField() != null) {
        affixes.put(at.getStatField(), value);
      } else {
        affixes.put(at.name(), value);
      }
    }
    return affixes;
  }

  private Map<String, Integer> buildStatBonus(EquipmentTemplate template) {
    Map<String, Integer> stats = new HashMap<>();
    if (template.getBaseStr() > 0) stats.put("STR", template.getBaseStr());
    if (template.getBaseCon() > 0) stats.put("CON", template.getBaseCon());
    if (template.getBaseAgi() > 0) stats.put("AGI", template.getBaseAgi());
    if (template.getBaseWis() > 0) stats.put("WIS", template.getBaseWis());
    return stats;
  }

  private String buildStackableAppraisalDesc(ItemTemplate template, long basePrice) {
    return template.getName() + "，基准估价 " + basePrice + " 灵石";
  }

  private String buildEquipmentAppraisalDesc(Equipment equipment, long basePrice) {
    StringBuilder sb = new StringBuilder();
    Rarity rarity = equipment.getRarity();
    sb.append(rarity.getName()).append("品质");
    if (equipment.getForgeLevel() > 0) {
      sb.append("，锻造+").append(equipment.getForgeLevel());
    }
    if (equipment.getAffixes() != null && !equipment.getAffixes().isEmpty()) {
      sb.append("，").append(equipment.getAffixes().size()).append("条词缀");
    }
    sb.append("，基准估价 ").append(basePrice).append(" 灵石");
    return sb.toString();
  }
}
