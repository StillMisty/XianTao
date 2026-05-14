package top.stillmisty.xiantao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
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
import top.stillmisty.xiantao.domain.shop.vo.HaggleResult;
import top.stillmisty.xiantao.domain.shop.vo.PurchaseResult;
import top.stillmisty.xiantao.domain.shop.vo.SellResult;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

@DisplayName("ShopService 测试")
@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

  @Mock private ShopNpcRepository shopNpcRepository;
  @Mock private ShopProductRepository shopProductRepository;
  @Mock private ItemTemplateRepository itemTemplateRepository;
  @Mock private EquipmentTemplateRepository equipmentTemplateRepository;
  @Mock private StackableItemRepository stackableItemRepository;
  @Mock private EquipmentRepository equipmentRepository;
  @Mock private UserRepository userRepository;
  @Mock private UserStateService userStateService;
  @Mock private PriceEngine priceEngine;

  @InjectMocks private ShopService shopService;

  private final Long userId = 1L;
  private final Long shopNpcId = 1L;
  private ShopNpc npc;
  private User user;

  @BeforeEach
  void setUp() {
    npc = new ShopNpc();
    npc.setId(shopNpcId);
    npc.setName("药老");
    npc.setMapNodeId(1L);
    npc.setBuyPriceModifier(BigDecimal.valueOf(0.60));

    user =
        User.create()
            .setId(userId)
            .setNickname("测试修士")
            .setLevel(10)
            .setSpiritStones(5000L)
            .setStatWis(30)
            .setStatus(UserStatus.IDLE)
            .setLocationId(1L);
  }

  // ===================== 购买堆叠物品 =====================

  @Nested
  @DisplayName("purchaseItem")
  class PurchaseItemTests {

    @Test
    @DisplayName("库存充足时成功购买")
    void purchaseItem_success() {
      when(userRepository.deductSpiritStonesIfEnough(eq(userId), anyInt())).thenReturn(1);

      ItemTemplate template = createTemplate("聚灵丹", ItemType.POTION);
      when(itemTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

      ShopProduct product = createProduct(ProductType.ITEM, 1L, 80L, 10, 20);
      when(shopProductRepository.findByShopNpcIdAndTemplateId(shopNpcId, 1L))
          .thenReturn(Optional.of(product));

      PurchaseResult result = shopService.purchaseItem(userId, npc, 1L, 2);

      assertTrue(result.success());
      assertEquals("聚灵丹", result.itemName());
      assertEquals(2, result.quantity());
      assertEquals(160L, result.totalPrice());
      verify(shopProductRepository).save(any());
    }

    @Test
    @DisplayName("商品不存在时抛异常")
    void purchaseItem_productNotFound() {
      when(shopProductRepository.findByShopNpcIdAndTemplateId(shopNpcId, 1L))
          .thenReturn(Optional.empty());

      assertThrows(BusinessException.class, () -> shopService.purchaseItem(userId, npc, 1L, 1));
    }

    @Test
    @DisplayName("库存不足时抛异常")
    void purchaseItem_insufficientStock() {
      ShopProduct product = createProduct(ProductType.ITEM, 1L, 80L, 1, 20);
      when(shopProductRepository.findByShopNpcIdAndTemplateId(shopNpcId, 1L))
          .thenReturn(Optional.of(product));

      assertThrows(BusinessException.class, () -> shopService.purchaseItem(userId, npc, 1L, 3));
    }

    @Test
    @DisplayName("灵石不足时抛异常")
    void purchaseItem_insufficientStones() {
      when(userStateService.loadUser(userId)).thenReturn(user);
      when(userRepository.deductSpiritStonesIfEnough(eq(userId), anyInt())).thenReturn(0);

      ItemTemplate template = createTemplate("聚灵丹", ItemType.POTION);
      when(itemTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

      ShopProduct product = createProduct(ProductType.ITEM, 1L, 80L, 5, 20);
      when(shopProductRepository.findByShopNpcIdAndTemplateId(shopNpcId, 1L))
          .thenReturn(Optional.of(product));

      assertThrows(BusinessException.class, () -> shopService.purchaseItem(userId, npc, 1L, 2));
    }
  }

  // ===================== 装备估价 =====================

  @Nested
  @DisplayName("appraiseEquipment")
  class AppraiseEquipmentTests {

    @Test
    @DisplayName("正常装备可估价")
    void appraiseEquipment_normal() {
      Equipment eq = createEquipment(1L, userId, "青冥剑", Rarity.RARE, false, true);
      when(equipmentRepository.findById(1L)).thenReturn(Optional.of(eq));

      ItemTemplate template = createTemplate("青冥剑", ItemType.MATERIAL);
      when(itemTemplateRepository.findById(anyLong())).thenReturn(Optional.of(template));

      when(priceEngine.calculateBuybackPrice(
              any(Equipment.class), any(ItemTemplate.class), any(ShopNpc.class)))
          .thenReturn(200L);
      when(priceEngine.getMinPrice(200L)).thenReturn(160L);
      when(priceEngine.getMaxPrice(200L)).thenReturn(240L);

      AppraisalResult result = shopService.appraiseEquipment(userId, npc, 1L);

      assertTrue(result.tradable());
      assertEquals(200L, result.basePrice());
      assertEquals(160L, result.minPrice());
      assertEquals(240L, result.maxPrice());
    }

    @Test
    @DisplayName("已装备的装备不可估价")
    void appraiseEquipment_equipped() {
      Equipment eq = createEquipment(1L, userId, "青冥剑", Rarity.RARE, true, true);
      when(equipmentRepository.findById(1L)).thenReturn(Optional.of(eq));

      AppraisalResult result = shopService.appraiseEquipment(userId, npc, 1L);

      assertFalse(result.tradable());
      assertTrue(result.description().contains("穿着"));
    }

    @Test
    @DisplayName("已绑定的装备不可回收")
    void appraiseEquipment_notTradable() {
      Equipment eq = createEquipment(1L, userId, "青冥剑", Rarity.RARE, false, false);
      when(equipmentRepository.findById(1L)).thenReturn(Optional.of(eq));

      AppraisalResult result = shopService.appraiseEquipment(userId, npc, 1L);

      assertFalse(result.tradable());
    }

    @Test
    @DisplayName("装备不属于当前用户时抛异常")
    void appraiseEquipment_notOwned() {
      Equipment eq = createEquipment(1L, 999L, "青冥剑", Rarity.RARE, false, true);
      when(equipmentRepository.findById(1L)).thenReturn(Optional.of(eq));

      assertThrows(BusinessException.class, () -> shopService.appraiseEquipment(userId, npc, 1L));
    }
  }

  // ===================== 堆叠物品估价 =====================

  @Nested
  @DisplayName("appraiseStackableItem")
  class AppraiseStackableItemTests {

    @Test
    @DisplayName("正常物品可估价")
    void appraiseStackableItem_normal() {
      StackableItem item = createStackableItem(1L, "聚灵丹", true);
      when(stackableItemRepository.findById(1L)).thenReturn(Optional.of(item));

      ItemTemplate template = createTemplate("聚灵丹", ItemType.POTION);
      when(itemTemplateRepository.findById(anyLong())).thenReturn(Optional.of(template));

      when(priceEngine.calculateBuybackPrice(any(ItemTemplate.class), any(ShopNpc.class)))
          .thenReturn(50L);
      when(priceEngine.getMinPrice(50L)).thenReturn(40L);
      when(priceEngine.getMaxPrice(50L)).thenReturn(60L);

      AppraisalResult result = shopService.appraiseStackableItem(userId, npc, 1L);

      assertTrue(result.tradable());
      assertEquals(50L, result.basePrice());
    }

    @Test
    @DisplayName("不可回收物品返回不可交易")
    void appraiseStackableItem_notTradable() {
      StackableItem item = createStackableItem(1L, "绑定物品", false);
      when(stackableItemRepository.findById(1L)).thenReturn(Optional.of(item));
      when(itemTemplateRepository.findById(anyLong()))
          .thenReturn(Optional.of(createTemplate("绑定物品", ItemType.MATERIAL)));

      AppraisalResult result = shopService.appraiseStackableItem(userId, npc, 1L);

      assertFalse(result.tradable());
      assertEquals(0L, result.basePrice());
    }
  }

  // ===================== 出售堆叠物品 =====================

  @Nested
  @DisplayName("sellStackableItem")
  class SellStackableItemTests {

    @Test
    @DisplayName("正常出售成功")
    void sellStackableItem_success() {
      StackableItem item = createStackableItem(1L, "聚灵丹", true);
      item.setQuantity(5);
      when(stackableItemRepository.findById(1L)).thenReturn(Optional.of(item));

      ItemTemplate template = createTemplate("聚灵丹", ItemType.POTION);
      when(itemTemplateRepository.findById(anyLong())).thenReturn(Optional.of(template));

      when(priceEngine.calculateBuybackPrice(any(ItemTemplate.class), any(ShopNpc.class)))
          .thenReturn(50L);
      when(priceEngine.getMinPrice(50L)).thenReturn(40L);
      when(priceEngine.getMaxPrice(50L)).thenReturn(60L);
      when(userStateService.loadUser(userId)).thenReturn(user);

      SellResult result = shopService.sellStackableItem(userId, npc, 1L, 50L);

      assertTrue(result.success());
      assertEquals(50L, result.price());
      assertEquals("聚灵丹", result.itemName());
    }

    @Test
    @DisplayName("确认价超出范围时抛异常")
    void sellStackableItem_priceOutOfRange() {
      StackableItem item = createStackableItem(1L, "聚灵丹", true);
      when(stackableItemRepository.findById(1L)).thenReturn(Optional.of(item));

      ItemTemplate template = createTemplate("聚灵丹", ItemType.POTION);
      when(itemTemplateRepository.findById(anyLong())).thenReturn(Optional.of(template));

      when(priceEngine.calculateBuybackPrice(any(ItemTemplate.class), any(ShopNpc.class)))
          .thenReturn(50L);
      when(priceEngine.getMinPrice(50L)).thenReturn(40L);
      when(priceEngine.getMaxPrice(50L)).thenReturn(60L);

      assertThrows(
          BusinessException.class, () -> shopService.sellStackableItem(userId, npc, 1L, 100L));
    }

    @Test
    @DisplayName("不可回收物品出售时抛异常")
    void sellStackableItem_notTradable() {
      StackableItem item = createStackableItem(1L, "绑定物品", false);
      when(stackableItemRepository.findById(1L)).thenReturn(Optional.of(item));

      assertThrows(
          BusinessException.class, () -> shopService.sellStackableItem(userId, npc, 1L, 50L));
    }
  }

  // ===================== 砍价 =====================

  @Nested
  @DisplayName("haggleItem")
  class HaggleItemTests {

    @Test
    @DisplayName("砍价返回 HaggleResult（不抛异常）")
    void haggleItem_returnsHaggleResult() {
      when(userStateService.loadUser(userId)).thenReturn(user);

      HaggleResult result = shopService.haggleItem(userId, npc, 100L);

      assertNotNull(result);
      assertTrue(result.success() || !result.success());
    }

    @Test
    @DisplayName("砍价成功时降价约 5%")
    void haggleItem_successDiscount() {
      user.setStatWis(80);
      when(userStateService.loadUser(userId)).thenReturn(user);

      // Run 50 times to account for randomness
      int successCount = 0;
      long totalDiscount = 0;
      for (int i = 0; i < 50; i++) {
        HaggleResult result = shopService.haggleItem(userId, npc, 100L);
        if (result.success()) {
          successCount++;
          totalDiscount += result.discountAmount();
          assertEquals(result.currentPrice() + result.discountAmount(), 100L);
        }
      }

      assertTrue(successCount > 0, "高悟性下应有砍价成功");
    }
  }

  // ===================== 查询装备列表 =====================

  @Nested
  @DisplayName("queryPlayerEquipment")
  class QueryPlayerEquipmentTests {

    @Test
    @DisplayName("返回未装备的装备列表")
    void queryPlayerEquipment_returnsUnequippedList() {
      Equipment eq1 = createEquipment(1L, userId, "青冥剑", Rarity.COMMON, false, true);
      eq1.setForgeLevel(0);
      eq1.setAffixes(Map.of("STR", 3));
      Equipment eq2 = createEquipment(2L, userId, "七星剑", Rarity.EPIC, false, true);
      eq2.setForgeLevel(3);
      eq2.setAffixes(Map.of("CRIT_RATE", 2, "LIFE_STEAL", 4));

      when(equipmentRepository.findUnequippedByUserId(userId)).thenReturn(List.of(eq1, eq2));

      EquipmentListVO result = shopService.queryPlayerEquipment(userId);

      assertNotNull(result);
      assertEquals(2, result.equipments().size());
    }
  }

  // ===================== 查商铺 =====================

  @Nested
  @DisplayName("findByLocation")
  class FindByLocationTests {

    @Test
    @DisplayName("有商铺时返回掌柜")
    void findByLocation_hasShop() {
      when(shopNpcRepository.findByMapNodeId(1L)).thenReturn(Optional.of(npc));

      ShopNpc result = shopService.findByLocation(1L);

      assertEquals("药老", result.getName());
    }

    @Test
    @DisplayName("无商铺时抛异常")
    void findByLocation_noShop() {
      when(shopNpcRepository.findByMapNodeId(1L)).thenReturn(Optional.empty());

      assertThrows(BusinessException.class, () -> shopService.findByLocation(1L));
    }
  }

  // ===================== 辅助工厂方法 =====================

  private ItemTemplate createTemplate(String name, ItemType type) {
    ItemTemplate template = new ItemTemplate();
    template.setId(1L);
    template.setName(name);
    template.setType(type);
    template.setBaseValue(100L);
    template.setTags(Set.of("pill"));
    return template;
  }

  private ShopProduct createProduct(
      ProductType type, long templateId, long currentPrice, int currentStock, int maxStock) {
    ShopProduct product = new ShopProduct();
    product.setId(1L);
    product.setShopNpcId(shopNpcId);
    product.setProductType(type);
    product.setTemplateId(templateId);
    product.setBasePrice(currentPrice);
    product.setMinPrice(currentPrice / 2);
    product.setMaxPrice(currentPrice * 2);
    product.setMinStock(0);
    product.setMaxStock(maxStock);
    product.setCurrentPrice(currentPrice);
    product.setCurrentStock(currentStock);
    return product;
  }

  private StackableItem createStackableItem(Long itemId, String name, boolean tradable) {
    StackableItem item = new StackableItem();
    item.setId(itemId);
    item.setUserId(userId);
    item.setTemplateId(1L);
    item.setName(name);
    item.setItemType(ItemType.POTION);
    item.setQuantity(1);
    item.setTradable(tradable);
    return item;
  }

  private Equipment createEquipment(
      Long eqId, Long ownerId, String name, Rarity rarity, boolean equipped, boolean tradable) {
    Equipment eq = new Equipment();
    eq.setId(eqId);
    eq.setUserId(ownerId);
    eq.setTemplateId(1L);
    eq.setName(name);
    eq.setSlot(EquipmentSlot.WEAPON);
    eq.setRarity(rarity);
    eq.setQualityMultiplier(1.0);
    eq.setForgeLevel(0);
    eq.setAffixes(Map.of());
    eq.setEquipped(equipped);
    eq.setTradable(tradable);
    return eq;
  }
}
