package top.stillmisty.xiantao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.EquipmentTemplate;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;
import top.stillmisty.xiantao.domain.shop.vo.AppraisalResult;
import top.stillmisty.xiantao.domain.shop.vo.EquipmentListVO;
import top.stillmisty.xiantao.domain.shop.vo.EquipmentPurchaseResult;
import top.stillmisty.xiantao.domain.shop.vo.HaggleResult;
import top.stillmisty.xiantao.domain.shop.vo.ProductListVO;
import top.stillmisty.xiantao.domain.shop.vo.PurchaseResult;
import top.stillmisty.xiantao.domain.shop.vo.SellResult;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.service.ai.ShopTools;

@DisplayName("ShopTools 测试")
@ExtendWith(MockitoExtension.class)
class ShopToolsTest {

  @Mock private ShopService shopService;
  @Mock private ItemTemplateRepository itemTemplateRepository;
  @Mock private EquipmentTemplateRepository equipmentTemplateRepository;
  @Mock private EquipmentRepository equipmentRepository;
  @Mock private UserStateService userStateService;

  @InjectMocks private ShopTools shopTools;

  private final Long userId = 1L;

  private User createUser() {
    return User.create()
        .setId(userId)
        .setNickname("测试修士")
        .setLevel(10)
        .setSpiritStones(5000L)
        .setStatWis(30)
        .setStatus(UserStatus.IDLE)
        .setLocationId(1L);
  }

  private ShopNpc createNpc() {
    ShopNpc npc = new ShopNpc();
    npc.setId(1L);
    npc.setName("药老");
    return npc;
  }

  // ===================== appraiseItem =====================

  @Nested
  @DisplayName("appraiseItem")
  class AppraiseItemTests {

    @Test
    @DisplayName("按名称找到唯一装备可估价")
    void singleEquipmentAppraisal() throws Exception {
      ScopedValue.where(UserContext.CURRENT_USER, userId)
          .call(
              () -> {
                User user = createUser();
                ShopNpc npc = createNpc();
                when(userStateService.loadUser(userId)).thenReturn(user);
                when(shopService.findByLocation(1L)).thenReturn(npc);

                Equipment eq = new Equipment();
                eq.setId(1L);
                eq.setName("青冥剑");
                eq.setRarity(Rarity.RARE);
                when(shopService.findEquipmentByName(userId, "青冥剑")).thenReturn(List.of(eq));
                when(shopService.findStackableItemsByName(userId, "青冥剑")).thenReturn(List.of());

                AppraisalResult expected = new AppraisalResult(true, 200L, 160L, 240L, "青冥剑", "描述");
                when(shopService.appraiseEquipment(userId, npc, 1L)).thenReturn(expected);

                AppraisalResult result = shopTools.appraiseItem("青冥剑", null);

                assertTrue(result.tradable());
                assertEquals(200L, result.basePrice());
                return null;
              });
    }

    @Test
    @DisplayName("名称匹配多个装备时返回列表")
    void multipleEquipmentMatches() throws Exception {
      ScopedValue.where(UserContext.CURRENT_USER, userId)
          .call(
              () -> {
                when(userStateService.loadUser(userId)).thenReturn(createUser());
                when(shopService.findByLocation(1L)).thenReturn(createNpc());

                Equipment eq1 = new Equipment();
                eq1.setId(1L);
                eq1.setName("青冥剑");
                eq1.setRarity(Rarity.COMMON);
                Equipment eq2 = new Equipment();
                eq2.setId(2L);
                eq2.setName("青冥剑");
                eq2.setRarity(Rarity.RARE);
                when(shopService.findEquipmentByName(userId, "青冥剑")).thenReturn(List.of(eq1, eq2));
                when(shopService.findStackableItemsByName(userId, "青冥剑")).thenReturn(List.of());

                AppraisalResult result = shopTools.appraiseItem("青冥剑", null);

                assertFalse(result.tradable());
                assertTrue(result.description().contains("多个匹配"));
                return null;
              });
    }

    @Test
    @DisplayName("物品不存在时返回失败")
    void itemNotFound() throws Exception {
      ScopedValue.where(UserContext.CURRENT_USER, userId)
          .call(
              () -> {
                when(userStateService.loadUser(userId)).thenReturn(createUser());
                when(shopService.findByLocation(1L)).thenReturn(createNpc());
                when(shopService.findEquipmentByName(userId, "不存在")).thenReturn(List.of());
                when(shopService.findStackableItemsByName(userId, "不存在")).thenReturn(List.of());

                AppraisalResult result = shopTools.appraiseItem("不存在", null);

                assertFalse(result.tradable());
                assertTrue(result.description().contains("未找到"));
                return null;
              });
    }
  }

  // ===================== haggle =====================

  @Nested
  @DisplayName("haggle")
  class HaggleItemTests {

    @Test
    @DisplayName("砍价调用 ShopService.haggleItem")
    void delegatesToShopService() throws Exception {
      ScopedValue.where(UserContext.CURRENT_USER, userId)
          .call(
              () -> {
                when(userStateService.loadUser(userId)).thenReturn(createUser());
                ShopNpc npc = createNpc();
                when(shopService.findByLocation(1L)).thenReturn(npc);
                HaggleResult expected = new HaggleResult(true, 95L, 5L, "砍价成功");
                when(shopService.haggleItem(userId, npc, 100L)).thenReturn(expected);

                HaggleResult result = shopTools.haggle("青冥剑", 100L);

                assertTrue(result.success());
                assertEquals(95L, result.currentPrice());
                return null;
              });
    }
  }

  // ===================== sellItem =====================

  @Nested
  @DisplayName("sellItem")
  class SellItemTests {

    @Test
    @DisplayName("出售装备成功")
    void sellEquipmentSuccess() throws Exception {
      ScopedValue.where(UserContext.CURRENT_USER, userId)
          .call(
              () -> {
                when(userStateService.loadUser(userId)).thenReturn(createUser());
                ShopNpc npc = createNpc();
                when(shopService.findByLocation(1L)).thenReturn(npc);

                SellResult expected = new SellResult(true, 200L, "青冥剑", "出售成功");
                when(shopService.sellEquipment(userId, npc, 1L, 200L)).thenReturn(expected);

                SellResult result = shopTools.sellItem("青冥剑", "1", 200L);

                assertTrue(result.success());
                assertEquals(200L, result.price());
                return null;
              });
    }

    @Test
    @DisplayName("itemId 为空时返回失败")
    void emptyItemId() throws Exception {
      ScopedValue.where(UserContext.CURRENT_USER, userId)
          .call(
              () -> {
                SellResult result = shopTools.sellItem("青冥剑", "", 200L);
                assertFalse(result.success());
                return null;
              });
    }
  }

  // ===================== listProducts =====================

  @Nested
  @DisplayName("listProducts")
  class ListProductsTests {

    @Test
    @DisplayName("列出商品")
    void listProductsSuccess() throws Exception {
      ScopedValue.where(UserContext.CURRENT_USER, userId)
          .call(
              () -> {
                ProductListVO expected = new ProductListVO("药老", List.of());
                when(shopService.listProducts(userId)).thenReturn(expected);

                ProductListVO result = shopTools.listProducts();

                assertNotNull(result);
                assertEquals("药老", result.shopName());
                return null;
              });
    }

    @Test
    @DisplayName("商铺不存在时返回空列表")
    void noShop() throws Exception {
      ScopedValue.where(UserContext.CURRENT_USER, userId)
          .call(
              () -> {
                when(shopService.listProducts(userId))
                    .thenThrow(new BusinessException(ErrorCode.SHOP_NOT_FOUND));

                ProductListVO result = shopTools.listProducts();

                assertEquals("未知", result.shopName());
                return null;
              });
    }
  }

  // ===================== purchaseItem =====================

  @Nested
  @DisplayName("purchaseItem")
  class PurchaseItemTests {

    @Test
    @DisplayName("购买物品成功")
    void purchaseItemSuccess() throws Exception {
      ScopedValue.where(UserContext.CURRENT_USER, userId)
          .call(
              () -> {
                when(userStateService.loadUser(userId)).thenReturn(createUser());
                ShopNpc npc = createNpc();
                when(shopService.findByLocation(1L)).thenReturn(npc);

                ItemTemplate template = new ItemTemplate();
                template.setId(1L);
                template.setName("聚灵丹");
                template.setType(ItemType.POTION);
                when(itemTemplateRepository.findByName("聚灵丹")).thenReturn(Optional.of(template));

                PurchaseResult expected = new PurchaseResult(true, "聚灵丹", 2, 160L, "购买成功");
                when(shopService.purchaseItem(userId, npc, 1L, 2)).thenReturn(expected);

                PurchaseResult result = shopTools.purchaseItem("聚灵丹", 2);

                assertTrue(result.success());
                assertEquals("聚灵丹", result.itemName());
                return null;
              });
    }

    @Test
    @DisplayName("物品模板不存在时返回失败")
    void templateNotFound() throws Exception {
      ScopedValue.where(UserContext.CURRENT_USER, userId)
          .call(
              () -> {
                when(userStateService.loadUser(userId)).thenReturn(createUser());
                when(shopService.findByLocation(1L)).thenReturn(createNpc());
                when(itemTemplateRepository.findByName("不存在")).thenReturn(Optional.empty());

                PurchaseResult result = shopTools.purchaseItem("不存在", 1);

                assertFalse(result.success());
                return null;
              });
    }
  }

  // ===================== purchaseEquipment =====================

  @Nested
  @DisplayName("purchaseEquipment")
  class PurchaseEquipmentTests {

    @Test
    @DisplayName("购买装备成功")
    void purchaseEquipmentSuccess() throws Exception {
      ScopedValue.where(UserContext.CURRENT_USER, userId)
          .call(
              () -> {
                when(userStateService.loadUser(userId)).thenReturn(createUser());
                ShopNpc npc = createNpc();
                when(shopService.findByLocation(1L)).thenReturn(npc);

                EquipmentTemplate template = new EquipmentTemplate();
                template.setId(1L);
                template.setName("青冥剑");
                when(equipmentTemplateRepository.findByName("青冥剑"))
                    .thenReturn(Optional.of(template));

                EquipmentPurchaseResult expected =
                    new EquipmentPurchaseResult(true, "精工青冥剑", "RARE", 200L, "购买成功");
                when(shopService.purchaseEquipment(userId, npc, 1L)).thenReturn(expected);

                EquipmentPurchaseResult result = shopTools.purchaseEquipment("青冥剑");

                assertTrue(result.success());
                assertEquals("RARE", result.rarity());
                return null;
              });
    }

    @Test
    @DisplayName("装备模板不存在时返回失败")
    void templateNotFound() throws Exception {
      ScopedValue.where(UserContext.CURRENT_USER, userId)
          .call(
              () -> {
                when(userStateService.loadUser(userId)).thenReturn(createUser());
                when(shopService.findByLocation(1L)).thenReturn(createNpc());
                when(equipmentTemplateRepository.findByName("不存在")).thenReturn(Optional.empty());

                EquipmentPurchaseResult result = shopTools.purchaseEquipment("不存在");

                assertFalse(result.success());
                assertTrue(result.description().contains("未找到"));
                return null;
              });
    }
  }

  // ===================== queryPlayerEquipment =====================

  @Nested
  @DisplayName("queryPlayerEquipment")
  class QueryPlayerEquipmentTests {

    @Test
    @DisplayName("查询装备列表为空时返回空列表")
    void emptyEquipmentList() throws Exception {
      ScopedValue.where(UserContext.CURRENT_USER, userId)
          .call(
              () -> {
                when(shopService.queryPlayerEquipment(userId))
                    .thenReturn(new EquipmentListVO(List.of()));

                EquipmentListVO result = shopTools.queryPlayerEquipment();

                assertNotNull(result);
                assertTrue(result.equipments().isEmpty());
                return null;
              });
    }
  }
}
