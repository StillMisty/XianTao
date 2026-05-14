package top.stillmisty.xiantao.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;
import top.stillmisty.xiantao.domain.shop.entity.ShopProduct;
import top.stillmisty.xiantao.domain.shop.entity.WorldEvent;
import top.stillmisty.xiantao.domain.shop.repository.WorldEventRepository;

@Component
@RequiredArgsConstructor
public class PriceEngine {

  private final WorldEventRepository worldEventRepository;

  private static final double RARITY_MULTIPLIER_BROKEN = 0.3;
  private static final double RARITY_MULTIPLIER_COMMON = 1.0;
  private static final double RARITY_MULTIPLIER_RARE = 2.0;
  private static final double RARITY_MULTIPLIER_EPIC = 5.0;
  private static final double RARITY_MULTIPLIER_LEGENDARY = 15.0;
  private static final double FORGE_MULTIPLIER_PER_LEVEL = 0.15;

  /** 计算堆叠物品收购价 */
  public long calculateBuybackPrice(ItemTemplate template, ShopNpc npc) {
    double baseValue = template.getBaseValue() != null ? template.getBaseValue().doubleValue() : 0;
    return calculateBuybackPrice(baseValue, template.getTags(), npc);
  }

  /** 计算装备收购价 */
  public long calculateBuybackPrice(Equipment equipment, ItemTemplate template, ShopNpc npc) {
    double baseValue = template.getBaseValue() != null ? template.getBaseValue().doubleValue() : 0;
    double equipmentMultiplier = calculateEquipmentMultiplier(equipment);
    double rawValue = baseValue * equipmentMultiplier;
    return calculateBuybackPrice(rawValue, template.getTags(), npc);
  }

  /** 计算商铺商品当前售价（含世界事件和懒调价） */
  public long calculateSellPrice(ShopProduct product, ShopNpc npc) {
    applyLazyRestock(product);
    double basePrice = product.getCurrentPrice().doubleValue();
    double worldMultiplier = getWorldEventMultiplier(null);
    double categoryMultiplier = 1.0;
    return (long) Math.max(1, basePrice * worldMultiplier * categoryMultiplier);
  }

  private long calculateBuybackPrice(double baseValue, Set<String> tags, ShopNpc npc) {
    double modifier = npc.getBuyPriceModifierDouble();
    double worldMultiplier =
        tags != null ? getWorldEventMultiplier(tags) : getWorldEventMultiplier(null);
    String typeCode = tags != null && !tags.isEmpty() ? inferCategoryFromTags(tags) : "MATERIAL";
    double categoryMultiplier = npc.getCategoryMultiplier(typeCode);

    double finalPrice = baseValue * modifier * worldMultiplier * categoryMultiplier;
    return Math.max(1, (long) finalPrice);
  }

  public long getMinPrice(long basePrice) {
    return (long) (basePrice * 0.8);
  }

  public long getMaxPrice(long basePrice) {
    return (long) (basePrice * 1.2);
  }

  private double calculateEquipmentMultiplier(Equipment equipment) {
    double multiplier = 1.0;

    if (equipment.getRarity() != null) {
      multiplier *= getRarityMultiplier(equipment.getRarity());
    }

    if (equipment.getQualityMultiplier() != null) {
      multiplier *= equipment.getQualityMultiplier();
    }

    if (equipment.getForgeLevel() != null && equipment.getForgeLevel() > 0) {
      multiplier *= (1.0 + equipment.getForgeLevel() * FORGE_MULTIPLIER_PER_LEVEL);
    }

    if (equipment.getAffixes() != null && !equipment.getAffixes().isEmpty()) {
      double affixPower =
          equipment.getAffixes().values().stream().mapToInt(Integer::intValue).sum();
      multiplier *= (1.0 + affixPower / 100.0);
    }

    return multiplier;
  }

  private double getRarityMultiplier(Rarity rarity) {
    return switch (rarity) {
      case BROKEN -> RARITY_MULTIPLIER_BROKEN;
      case COMMON -> RARITY_MULTIPLIER_COMMON;
      case RARE -> RARITY_MULTIPLIER_RARE;
      case EPIC -> RARITY_MULTIPLIER_EPIC;
      case LEGENDARY -> RARITY_MULTIPLIER_LEGENDARY;
    };
  }

  private double getWorldEventMultiplier(Set<String> tags) {
    List<WorldEvent> activeEvents = worldEventRepository.findActiveEvents();
    double multiplier = 1.0;
    for (WorldEvent event : activeEvents) {
      if (tags == null || event.affectsAnyTag(tags)) {
        multiplier *= event.getGlobalMultiplierDouble();
      }
    }
    return multiplier;
  }

  private String inferCategoryFromTags(Set<String> tags) {
    if (tags == null || tags.isEmpty()) return "MATERIAL";
    for (String tag : tags) {
      String upper = tag.toUpperCase();
      if (upper.contains("HERB") || upper.contains("MEDICINE")) return "HERB";
      if (upper.contains("ORE") || upper.contains("METAL") || upper.contains("FORGE")) return "ORE";
      if (upper.contains("POTION") || upper.contains("PILL")) return "POTION";
      if (upper.contains("SEED")) return "SEED";
      if (upper.contains("BEAST") || upper.contains("EGG")) return "BEAST_EGG";
    }
    return "MATERIAL";
  }

  /** 懒补货 & 懒调价：打开商铺或交易时触发 */
  public void applyLazyRestock(ShopProduct product) {
    if (product.getLastSaleTime() == null) return;
    LocalDateTime now = LocalDateTime.now();

    int currentStock = product.getCurrentStock();
    long currentPrice = product.getCurrentPrice();

    // 缺货涨价补货
    if (currentStock == 0) {
      long hoursSinceLastSale = Duration.between(product.getLastSaleTime(), now).toHours();
      if (hoursSinceLastSale > 4) {
        int restockQty = Math.min(5, product.getMaxStock());
        int newStock = Math.min(restockQty, product.getMaxStock());
        long newPrice = Math.min((long) Math.ceil(currentPrice * 1.15), product.getMaxPrice());
        product.setCurrentStock(newStock);
        product.setCurrentPrice(newPrice);
        product.setLastSaleTime(now);
        return;
      }
    }

    // 滞销降价减仓
    if (currentStock > product.getMaxStock() / 2) {
      long daysSinceLastSale = Duration.between(product.getLastSaleTime(), now).toDays();
      if (daysSinceLastSale > 2) {
        int decayQty = Math.max(1, currentStock / 5);
        int newStock = Math.max(currentStock - decayQty, product.getMinStock());
        long newPrice = Math.max((long) Math.ceil(currentPrice * 0.92), product.getMinPrice());
        product.setCurrentStock(newStock);
        product.setCurrentPrice(newPrice);
        product.setLastSaleTime(now);
      }
    }
  }
}
