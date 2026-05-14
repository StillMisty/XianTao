package top.stillmisty.xiantao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;
import top.stillmisty.xiantao.domain.shop.entity.ShopProduct;
import top.stillmisty.xiantao.domain.shop.entity.WorldEvent;
import top.stillmisty.xiantao.domain.shop.repository.WorldEventRepository;

@DisplayName("PriceEngine 测试")
@ExtendWith(MockitoExtension.class)
class PriceEngineTest {

  @Mock private WorldEventRepository worldEventRepository;

  @InjectMocks private PriceEngine priceEngine;

  private ShopNpc createNpc(double buyModifier, Map<String, BigDecimal> categoryMultiplier) {
    ShopNpc npc = new ShopNpc();
    npc.setId(1L);
    npc.setName("测试掌柜");
    npc.setBuyPriceModifier(BigDecimal.valueOf(buyModifier));
    npc.setCategoryMultiplier(categoryMultiplier);
    return npc;
  }

  private ItemTemplate createTemplate(long baseValue, Set<String> tags) {
    ItemTemplate template = new ItemTemplate();
    template.setId(1L);
    template.setName("聚灵丹");
    template.setType(ItemType.POTION);
    template.setBaseValue(baseValue);
    template.setTags(tags);
    return template;
  }

  private Equipment createEquipment(
      Rarity rarity, double qualityMultiplier, int forgeLevel, Map<String, Integer> affixes) {
    Equipment eq = new Equipment();
    eq.setId(1L);
    eq.setUserId(1L);
    eq.setName("测试剑");
    eq.setSlot(EquipmentSlot.WEAPON);
    eq.setRarity(rarity);
    eq.setQualityMultiplier(qualityMultiplier);
    eq.setForgeLevel(forgeLevel);
    eq.setAffixes(affixes);
    return eq;
  }

  // ===================== 堆叠物品收购价 =====================

  @Test
  @DisplayName("calculateBuybackPrice — 基础价 × 折扣 × 分类系数")
  void calculateBuybackPrice_basicFormula() {
    when(worldEventRepository.findActiveEvents()).thenReturn(List.of());

    ItemTemplate template = createTemplate(100L, Set.of("pill"));
    ShopNpc npc = createNpc(0.60, Map.of("POTION", BigDecimal.valueOf(0.85)));

    long price = priceEngine.calculateBuybackPrice(template, npc);

    assertEquals(51L, price);
  }

  @Test
  @DisplayName("calculateBuybackPrice — 默认分类系数为 1.0")
  void calculateBuybackPrice_defaultCategoryMultiplier() {
    when(worldEventRepository.findActiveEvents()).thenReturn(List.of());

    ItemTemplate template = createTemplate(100L, Set.of("ore"));
    ShopNpc npc = createNpc(0.50, null);

    long price = priceEngine.calculateBuybackPrice(template, npc);

    assertEquals(50L, price);
  }

  @Test
  @DisplayName("calculateBuybackPrice — 世界事件影响价格")
  void calculateBuybackPrice_worldEventMultiplier() {
    WorldEvent event = new WorldEvent();
    event.setGlobalMultiplier(BigDecimal.valueOf(1.20));
    event.setAffectedTags(Set.of("pill"));
    when(worldEventRepository.findActiveEvents()).thenReturn(List.of(event));

    ItemTemplate template = createTemplate(100L, Set.of("pill"));
    ShopNpc npc = createNpc(0.50, null);

    long price = priceEngine.calculateBuybackPrice(template, npc);

    assertEquals(60L, price);
  }

  @Test
  @DisplayName("calculateBuybackPrice — 无标签物品不受事件分类影响")
  void calculateBuybackPrice_noTags_noEventEffect() {
    WorldEvent event = new WorldEvent();
    event.setGlobalMultiplier(BigDecimal.valueOf(1.20));
    event.setAffectedTags(Set.of("herb"));
    when(worldEventRepository.findActiveEvents()).thenReturn(List.of(event));

    ItemTemplate template = createTemplate(100L, null);
    ShopNpc npc = createNpc(0.50, null);

    long price = priceEngine.calculateBuybackPrice(template, npc);

    assertEquals(60L, price);
  }

  @Test
  @DisplayName("calculateBuybackPrice — 最低返回 1 灵石")
  void calculateBuybackPrice_minimumOne() {
    when(worldEventRepository.findActiveEvents()).thenReturn(List.of());

    ItemTemplate template = createTemplate(1L, Set.of("trash"));
    ShopNpc npc = createNpc(0.10, null);

    long price = priceEngine.calculateBuybackPrice(template, npc);

    assertTrue(price >= 1);
  }

  // ===================== 装备收购价 =====================

  @Test
  @DisplayName("calculateBuybackPrice(Equipment) — 传说装备 ×15 倍")
  void calculateBuybackPrice_legendaryEquipment() {
    when(worldEventRepository.findActiveEvents()).thenReturn(List.of());

    ItemTemplate template = createTemplate(100L, Set.of("sword"));
    ShopNpc npc = createNpc(1.00, null);

    Equipment eq = createEquipment(Rarity.LEGENDARY, 1.0, 0, Map.of());
    long price = priceEngine.calculateBuybackPrice(eq, template, npc);

    assertEquals(1500L, price);
  }

  @Test
  @DisplayName("calculateBuybackPrice(Equipment) — 品质系数影响")
  void calculateBuybackPrice_qualityMultiplier() {
    when(worldEventRepository.findActiveEvents()).thenReturn(List.of());

    ItemTemplate template = createTemplate(100L, Set.of("sword"));
    ShopNpc npc = createNpc(1.00, null);

    Equipment eq = createEquipment(Rarity.COMMON, 1.50, 0, Map.of());
    long price = priceEngine.calculateBuybackPrice(eq, template, npc);

    assertEquals(150L, price);
  }

  @Test
  @DisplayName("calculateBuybackPrice(Equipment) — 锻造等级加成")
  void calculateBuybackPrice_forgeLevel() {
    when(worldEventRepository.findActiveEvents()).thenReturn(List.of());

    ItemTemplate template = createTemplate(100L, Set.of("sword"));
    ShopNpc npc = createNpc(1.00, null);

    Equipment eq = createEquipment(Rarity.COMMON, 1.0, 5, Map.of());
    long price = priceEngine.calculateBuybackPrice(eq, template, npc);

    assertEquals(175L, price);
  }

  @Test
  @DisplayName("calculateBuybackPrice(Equipment) — 词缀加成")
  void calculateBuybackPrice_affixBonus() {
    when(worldEventRepository.findActiveEvents()).thenReturn(List.of());

    ItemTemplate template = createTemplate(100L, Set.of("sword"));
    ShopNpc npc = createNpc(1.00, null);

    Equipment eq = createEquipment(Rarity.COMMON, 1.0, 0, Map.of("STR", 5, "CRIT_RATE", 5));
    long price = priceEngine.calculateBuybackPrice(eq, template, npc);

    assertEquals(110L, price);
  }

  @Test
  @DisplayName("calculateBuybackPrice(Equipment) — 破旧装备 ×0.3")
  void calculateBuybackPrice_brokenEquipment() {
    when(worldEventRepository.findActiveEvents()).thenReturn(List.of());

    ItemTemplate template = createTemplate(100L, Set.of("sword"));
    ShopNpc npc = createNpc(1.00, null);

    Equipment eq = createEquipment(Rarity.BROKEN, 1.0, 0, Map.of());
    long price = priceEngine.calculateBuybackPrice(eq, template, npc);

    assertEquals(30L, price);
  }

  // ===================== 价格边界 =====================

  @Test
  @DisplayName("getMinPrice — 基准价的 80%")
  void getMinPrice_80percent() {
    assertEquals(80L, priceEngine.getMinPrice(100L));
  }

  @Test
  @DisplayName("getMaxPrice — 基准价的 120%")
  void getMaxPrice_120percent() {
    assertEquals(120L, priceEngine.getMaxPrice(100L));
  }

  // ===================== 懒补货 & 懒调价 =====================

  @Test
  @DisplayName("applyLazyRestock — 无 lastSaleTime 不触发")
  void applyLazyRestock_noLastSaleTime() {
    ShopProduct product = new ShopProduct();
    product.setCurrentStock(0);
    product.setCurrentPrice(100L);
    product.setMinStock(0);
    product.setMaxStock(20);

    priceEngine.applyLazyRestock(product);

    assertEquals(0, product.getCurrentStock());
    assertEquals(100L, product.getCurrentPrice());
  }

  @Test
  @DisplayName("applyLazyRestock — 库存为空且超过 4 小时触发补货涨价")
  void applyLazyRestock_restockOnEmptyStock() {
    ShopProduct product = new ShopProduct();
    product.setCurrentStock(0);
    product.setCurrentPrice(100L);
    product.setMinStock(0);
    product.setMaxStock(20);
    product.setMinPrice(50L);
    product.setMaxPrice(200L);
    product.setLastSaleTime(LocalDateTime.now().minusHours(5));

    priceEngine.applyLazyRestock(product);

    assertEquals(5, product.getCurrentStock());
    assertEquals(115L, product.getCurrentPrice());
  }

  @Test
  @DisplayName("applyLazyRestock — 库存积压超过 2 天触发降价减仓")
  void applyLazyRestock_decayOnOverstock() {
    ShopProduct product = new ShopProduct();
    product.setCurrentStock(18);
    product.setCurrentPrice(100L);
    product.setMinStock(0);
    product.setMaxStock(20);
    product.setMinPrice(50L);
    product.setMaxPrice(200L);
    product.setLastSaleTime(LocalDateTime.now().minusDays(3));

    priceEngine.applyLazyRestock(product);

    assertTrue(product.getCurrentStock() < 18);
    assertEquals(92L, product.getCurrentPrice());
  }

  @Test
  @DisplayName("applyLazyRestock — 补货价不超 maxPrice")
  void applyLazyRestock_capAtMaxPrice() {
    ShopProduct product = new ShopProduct();
    product.setCurrentStock(0);
    product.setCurrentPrice(95L);
    product.setMinStock(0);
    product.setMaxStock(20);
    product.setMinPrice(50L);
    product.setMaxPrice(100L);
    product.setLastSaleTime(LocalDateTime.now().minusHours(5));

    priceEngine.applyLazyRestock(product);

    assertEquals(100L, product.getCurrentPrice());
  }
}
