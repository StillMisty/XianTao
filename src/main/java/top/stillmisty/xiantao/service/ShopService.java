package top.stillmisty.xiantao.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.EquipmentTemplate;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;
import top.stillmisty.xiantao.domain.shop.entity.ShopProduct;
import top.stillmisty.xiantao.domain.shop.enums.ProductType;
import top.stillmisty.xiantao.domain.shop.repository.ShopNpcRepository;
import top.stillmisty.xiantao.domain.shop.repository.ShopProductRepository;
import top.stillmisty.xiantao.domain.shop.vo.AppraisalResult;
import top.stillmisty.xiantao.domain.shop.vo.EquipmentListVO;
import top.stillmisty.xiantao.domain.shop.vo.EquipmentPurchaseResult;
import top.stillmisty.xiantao.domain.shop.vo.HaggleResult;
import top.stillmisty.xiantao.domain.shop.vo.ProductListVO;
import top.stillmisty.xiantao.domain.shop.vo.PurchaseResult;
import top.stillmisty.xiantao.domain.shop.vo.SellResult;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.service.annotation.Authenticated;

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

  // ===================== 公开 API（含认证） =====================

  @Authenticated
  public ServiceResult<ProductListVO> listProducts(
      top.stillmisty.xiantao.domain.user.enums.PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    User user = userStateService.loadUser(userId);
    ShopNpc npc = findByLocation(user.getLocationId());
    return new ServiceResult.Success<>(listProducts(npc));
  }

  @Authenticated
  @Transactional
  public ServiceResult<PurchaseResult> purchaseItem(
      top.stillmisty.xiantao.domain.user.enums.PlatformType platform,
      String openId,
      Long templateId,
      int quantity) {
    Long userId = UserContext.getCurrentUserId();
    User user = userStateService.loadUser(userId);
    ShopNpc npc = findByLocation(user.getLocationId());
    return new ServiceResult.Success<>(purchaseItem(userId, npc, templateId, quantity));
  }

  @Authenticated
  @Transactional
  public ServiceResult<EquipmentPurchaseResult> purchaseEquipment(
      top.stillmisty.xiantao.domain.user.enums.PlatformType platform,
      String openId,
      Long templateId) {
    Long userId = UserContext.getCurrentUserId();
    User user = userStateService.loadUser(userId);
    ShopNpc npc = findByLocation(user.getLocationId());
    return new ServiceResult.Success<>(purchaseEquipment(userId, npc, templateId));
  }

  // ===================== 内部 API（供 Tools 调用） =====================

  public ProductListVO listProducts(Long userId) {
    User user = userStateService.loadUser(userId);
    ShopNpc npc = findByLocation(user.getLocationId());
    return listProducts(npc);
  }

  @Transactional
  public PurchaseResult purchaseItem(Long userId, ShopNpc npc, Long templateId, int quantity) {
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

    int rows = userRepository.deductSpiritStonesIfEnough(userId, (int) totalPrice);
    if (rows == 0) {
      User user = userStateService.loadUser(userId);
      throw new BusinessException(
          ErrorCode.SHOP_SPIRIT_STONES_INSUFFICIENT, totalPrice, user.getSpiritStones());
    }

    product.setCurrentStock(product.getCurrentStock() - quantity);
    product.setLastSaleTime(LocalDateTime.now());
    shopProductRepository.save(product);

    addStackableItem(userId, template, quantity);

    return new PurchaseResult(
        true,
        template.getName(),
        quantity,
        totalPrice,
        "购买成功！获得 " + template.getName() + " x" + quantity + "，花费 " + totalPrice + " 灵石");
  }

  @Transactional
  public EquipmentPurchaseResult purchaseEquipment(Long userId, ShopNpc npc, Long templateId) {
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

    int rows = userRepository.deductSpiritStonesIfEnough(userId, (int) price);
    if (rows == 0) {
      User user = userStateService.loadUser(userId);
      throw new BusinessException(
          ErrorCode.SHOP_SPIRIT_STONES_INSUFFICIENT, price, user.getSpiritStones());
    }

    Rarity rarity = rollRarity();
    double qualityMultiplier = rarity.randomQualityMultiplier();
    int affixCount = rarity.randomAffixCount();
    Map<String, Integer> affixes = rollAffixes(template, affixCount);
    String prefix = rarity.randomPrefix();

    Equipment equipment =
        Equipment.create(
            userId,
            template.getId(),
            prefix + template.getName(),
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

    product.setCurrentStock(product.getCurrentStock() - 1);
    product.setLastSaleTime(LocalDateTime.now());
    shopProductRepository.save(product);

    String description = rarity.getName() + "品质，品质系数 " + String.format("%.2f", qualityMultiplier);
    if (affixCount > 0) {
      description += "，含 " + affixCount + " 条词缀";
    }

    return new EquipmentPurchaseResult(
        true, equipment.getName(), rarity.getCode(), price, description);
  }

  @Transactional
  public SellResult sellStackableItem(Long userId, ShopNpc npc, Long itemId, long confirmedPrice) {
    StackableItem item =
        stackableItemRepository
            .findById(itemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_EXISTS));
    if (!item.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.ITEM_OWNERSHIP_MISMATCH);
    }
    if (item.getTradable() != null && !item.getTradable()) {
      throw new BusinessException(ErrorCode.ITEM_NOT_TRADABLE);
    }
    if (item.getQuantity() == null || item.getQuantity() <= 0) {
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
    int quantity = item.getQuantity();

    if (item.reduceQuantity(1)) {
      stackableItemRepository.deleteById(item.getId());
    } else {
      stackableItemRepository.save(item);
    }

    User user = userStateService.loadUser(userId);
    user.setSpiritStones(user.getSpiritStones() + confirmedPrice);
    userRepository.save(user);

    return new SellResult(
        true,
        confirmedPrice,
        itemName,
        "出售成功！"
            + itemName
            + " 售出 "
            + confirmedPrice
            + " 灵石"
            + (quantity > 1 ? "（剩余 " + item.getQuantity() + " 个）" : ""));
  }

  @Transactional
  public SellResult sellEquipment(Long userId, ShopNpc npc, Long equipmentId, long confirmedPrice) {
    Equipment equipment =
        equipmentRepository
            .findById(equipmentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
    if (!equipment.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.EQUIPMENT_NOT_OWNED);
    }
    if (equipment.getEquipped() != null && equipment.getEquipped()) {
      throw new BusinessException(ErrorCode.EQUIPMENT_ALREADY_EQUIPPED);
    }
    if (equipment.getTradable() != null && !equipment.getTradable()) {
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
    equipmentRepository.deleteById(equipment.getId());

    User user = userStateService.loadUser(userId);
    user.setSpiritStones(user.getSpiritStones() + confirmedPrice);
    userRepository.save(user);

    return new SellResult(
        true,
        confirmedPrice,
        equipmentName,
        "出售成功！" + equipmentName + " 售出 " + confirmedPrice + " 灵石");
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

    boolean tradable = item.getTradable() == null || item.getTradable();
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
    if (equipment.getEquipped() != null && equipment.getEquipped()) {
      return new AppraisalResult(false, 0, 0, 0, equipment.getName(), "客官还穿着呢，脱下来再说吧");
    }
    if (equipment.getTradable() != null && !equipment.getTradable()) {
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

  public HaggleResult haggleItem(Long userId, ShopNpc npc, long currentPrice) {
    User user = userStateService.loadUser(userId);
    double charmFactor = Math.min(0.3, (user.getEffectiveStatWis() - 10) / 20.0);
    double personalityFactor = 0.1;
    if (npc.getPersonality() != null && npc.getPersonality().contains("T")) {
      personalityFactor = 0.2;
    }

    double successRate = 0.3 + charmFactor * 0.3 - personalityFactor;
    successRate = Math.max(0.1, Math.min(0.7, successRate));

    boolean success = ThreadLocalRandom.current().nextDouble() < successRate;

    if (!success) {
      return new HaggleResult(false, currentPrice, 0, "客官，这价已经是最低了，再降老朽要亏本了");
    }

    long discount = (long) Math.ceil(currentPrice * 0.05);
    long newPrice = currentPrice - discount;

    return new HaggleResult(
        true, newPrice, discount, "罢了罢了，看你诚心，让利 " + discount + " 灵石，算你 " + newPrice + " 灵石吧");
  }

  public EquipmentListVO queryPlayerEquipment(Long userId) {
    List<Equipment> unequipped = equipmentRepository.findUnequippedByUserId(userId);
    List<EquipmentListVO.EquipmentEntry> entries =
        unequipped.stream()
            .map(
                e ->
                    new EquipmentListVO.EquipmentEntry(
                        e.getId(),
                        e.getName(),
                        e.getRarity() != null ? e.getRarity().getName() : "未知",
                        e.getForgeLevel() != null ? e.getForgeLevel() : 0,
                        e.getAffixes() != null
                            ? e.getAffixes().keySet().stream()
                                .limit(3)
                                .collect(Collectors.joining("、"))
                            : "无"))
            .toList();
    return new EquipmentListVO(entries);
  }

  // ===================== 辅助方法 =====================

  public ShopNpc findByLocation(Long locationId) {
    return shopNpcRepository
        .findByMapNodeId(locationId)
        .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_NOT_FOUND));
  }

  public boolean hasShopAtLocation(Long locationId) {
    return shopNpcRepository.findByMapNodeId(locationId).isPresent();
  }

  public List<StackableItem> findStackableItemsByName(Long userId, String name) {
    return stackableItemRepository.findByUserIdAndNameContaining(userId, name);
  }

  public List<Equipment> findEquipmentByName(Long userId, String name) {
    return equipmentRepository.findUnequippedByUserId(userId).stream()
        .filter(e -> e.getName().contains(name))
        .toList();
  }

  private ProductListVO listProducts(ShopNpc npc) {
    List<ShopProduct> products = shopProductRepository.findByShopNpcId(npc.getId());
    List<ProductListVO.ProductEntry> entries = new ArrayList<>();
    for (ShopProduct product : products) {
      priceEngine.applyLazyRestock(product);
      String name = "";
      String extra = "";
      if (product.getProductType() == ProductType.ITEM) {
        var t = itemTemplateRepository.findById(product.getTemplateId());
        name = t.map(ItemTemplate::getName).orElse("未知物品");
      } else {
        var t = equipmentTemplateRepository.findById(product.getTemplateId());
        name = t.map(EquipmentTemplate::getName).orElse("未知装备");
        extra = t.map(et -> et.getSlot() != null ? et.getSlot().getName() : "").orElse("");
      }
      entries.add(
          new ProductListVO.ProductEntry(
              product.getId(),
              product.getProductType().getCode(),
              name,
              product.getCurrentPrice(),
              product.getCurrentStock(),
              extra));
    }
    return new ProductListVO(npc.getName(), entries);
  }

  private void addStackableItem(Long userId, ItemTemplate template, int quantity) {
    int hash = StackableItem.computeHash(template.getProperties());
    var existing =
        stackableItemRepository.findByUserIdAndTemplateIdAndPropertiesHash(
            userId, template.getId(), hash);
    if (existing.isPresent()) {
      StackableItem item = existing.get();
      item.addQuantity(quantity);
      stackableItemRepository.save(item);
    } else {
      StackableItem newItem =
          StackableItem.create(
              userId, template.getId(), template.getType(), template.getName(), quantity);
      newItem.setTags(template.getTags());
      newItem.setProperties(template.getProperties());
      newItem.setPropertiesHash(hash);
      newItem.setTradable(true);
      stackableItemRepository.save(newItem);
    }
  }

  private Rarity rollRarity() {
    return ThreadLocalRandom.current().nextDouble() < 0.05
        ? Rarity.EPIC
        : ThreadLocalRandom.current().nextDouble() < 0.30 ? Rarity.RARE : Rarity.COMMON;
  }

  private Map<String, Integer> rollAffixes(EquipmentTemplate template, int affixCount) {
    if (affixCount <= 0) return Map.of();
    Map<String, Integer> affixes = new HashMap<>();
    String[][] affixPool = {
      {"STR", "攻击"},
      {"CON", "防御"},
      {"AGI", "速度"},
      {"WIS", "灵气"},
      {"CRIT_RATE", "暴击率"},
      {"CRIT_DMG", "暴击伤害"},
      {"LIFE_STEAL", "吸血"},
      {"LUCK", "寻宝"},
      {"ELEMENT_DMG", "属性伤害"},
      {"HP_BONUS", "气血"}
    };
    for (int i = 0; i < affixCount; i++) {
      String[] affix = affixPool[ThreadLocalRandom.current().nextInt(affixPool.length)];
      affixes.put(affix[0], 1 + ThreadLocalRandom.current().nextInt(5));
    }
    return affixes;
  }

  private Map<String, Integer> buildStatBonus(EquipmentTemplate template) {
    Map<String, Integer> stats = new HashMap<>();
    if (template.getBaseStr() != null && template.getBaseStr() > 0)
      stats.put("STR", template.getBaseStr());
    if (template.getBaseCon() != null && template.getBaseCon() > 0)
      stats.put("CON", template.getBaseCon());
    if (template.getBaseAgi() != null && template.getBaseAgi() > 0)
      stats.put("AGI", template.getBaseAgi());
    if (template.getBaseWis() != null && template.getBaseWis() > 0)
      stats.put("WIS", template.getBaseWis());
    return stats;
  }

  private String buildStackableAppraisalDesc(ItemTemplate template, long basePrice) {
    return template.getName() + "，基准估价 " + basePrice + " 灵石";
  }

  private String buildEquipmentAppraisalDesc(Equipment equipment, long basePrice) {
    StringBuilder sb = new StringBuilder();
    Rarity rarity = equipment.getRarity();
    if (rarity != null) {
      sb.append(rarity.getName()).append("品质");
    }
    if (equipment.getForgeLevel() != null && equipment.getForgeLevel() > 0) {
      sb.append("，锻造+").append(equipment.getForgeLevel());
    }
    if (equipment.getAffixes() != null && !equipment.getAffixes().isEmpty()) {
      sb.append("，").append(equipment.getAffixes().size()).append("条词缀");
    }
    sb.append("，基准估价 ").append(basePrice).append(" 灵石");
    return sb.toString();
  }
}
