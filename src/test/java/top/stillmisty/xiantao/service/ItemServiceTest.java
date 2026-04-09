package top.stillmisty.xiantao.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.item.vo.*;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ItemService 测试类
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private StackableItemRepository stackableItemRepository;

    private UUID testUserId;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = User.init();
        testUser.setNickname("测试道友");
        testUser.setLocationId("测试地点");
        testUser = userRepository.save(testUser);
        testUserId = testUser.getId();
    }

    @Nested
    @DisplayName("getCharacterStatus - 角色状态查看")
    class GetCharacterStatusTests {

        @Test
        @DisplayName("应该返回用户完整状态")
        void shouldReturnFullCharacterStatus() {
            // Arrange
            CharacterStatusResult result = itemService.getCharacterStatus(testUserId);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(testUserId, result.getUserId());
            assertEquals("测试道友", result.getNickname());
            assertEquals(1, result.getLevel());
            assertEquals(5, result.getBaseStr());
            assertEquals(5, result.getBaseCon());
            assertEquals(5, result.getBaseAgi());
            assertEquals(5, result.getBaseWis());
            assertEquals(0, result.getFreeStatPoints());
            assertEquals(100L, result.getCoins());
            assertEquals(0L, result.getSpiritStones());
        }

        @Test
        @DisplayName("用户不存在时应该返回失败")
        void shouldReturnFailureWhenUserNotFound() {
            // Arrange
            UUID nonExistentUserId = UUID.randomUUID();

            // Act
            CharacterStatusResult result = itemService.getCharacterStatus(nonExistentUserId);

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("用户不存在", result.getMessage());
        }

        @Test
        @DisplayName("应该正确计算装备加成")
        void shouldCalculateEquipmentBonus() {
            // Arrange - 创建并穿戴装备
            Equipment weapon = Equipment.create(testUserId, "test_sword", "测试铁剑",
                    EquipmentSlot.WEAPON, Rarity.COMMON,
                    Map.of("str", 3, "con", 0, "agi", 1, "wis", 0),
                    5, 0);
            weapon.setEquipped(true);
            equipmentRepository.save(weapon);

            // Act
            CharacterStatusResult result = itemService.getCharacterStatus(testUserId);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(3, result.getEquipStr()); // 装备力量加成
            assertEquals(1, result.getEquipAgi()); // 装备敏捷加成
            assertEquals(8, result.getTotalStr()); // 5(基础) + 3(装备)
            assertEquals(6, result.getTotalAgi()); // 5(基础) + 1(装备)
            assertEquals(13, result.getAttack()); // 8 * 2 + 5(装备攻击)
            assertEquals(1, result.getEquipment().getTotalEquipped());
        }
    }

    @Nested
    @DisplayName("getInventory - 背包查看")
    class GetInventoryTests {

        @Test
        @DisplayName("应该返回空背包")
        void shouldReturnEmptyInventory() {
            // Act
            InventoryResult result = itemService.getInventory(testUserId);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(testUserId, result.getUserId());
            assertEquals(50, result.getCapacity());
            assertEquals(0, result.getCurrentSize());
            assertTrue(result.getEquipments().isEmpty());
            assertTrue(result.getMaterials().isEmpty());
            assertTrue(result.getSeeds().isEmpty());
            assertTrue(result.getSpiritEggs().isEmpty());
            assertTrue(result.getConsumables().isEmpty());
            assertEquals(100L, result.getCoins());
            assertEquals(0L, result.getSpiritStones());
        }

        @Test
        @DisplayName("应该正确分类堆叠物品")
        void shouldCategorizeStackableItems() {
            // Arrange
            itemService.addStackableItem(testUserId, "material_iron", ItemType.MATERIAL, "精铁", 100);
            itemService.addStackableItem(testUserId, "seed_herb", ItemType.SEED, "灵草种子", 50);
            itemService.addStackableItem(testUserId, "egg_fire", ItemType.SPIRIT_EGG, "火灵蛋", 1);
            itemService.addStackableItem(testUserId, "pill_healing", ItemType.CONSUMABLE, "疗伤丹", 20);

            // Act
            InventoryResult result = itemService.getInventory(testUserId);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(4, result.getCurrentSize());
            assertEquals(1, result.getMaterials().size());
            assertEquals(1, result.getSeeds().size());
            assertEquals(1, result.getSpiritEggs().size());
            assertEquals(1, result.getConsumables().size());
            assertEquals("精铁", result.getMaterials().getFirst().getName());
            assertEquals(100, result.getMaterials().getFirst().getQuantity());
        }

        @Test
        @DisplayName("未穿戴装备应该出现在背包中")
        void shouldShowUnequippedItems() {
            // Arrange
            Equipment weapon = Equipment.create(testUserId, "test_sword", "测试铁剑",
                    EquipmentSlot.WEAPON, Rarity.COMMON,
                    Map.of("str", 3, "con", 0, "agi", 1, "wis", 0),
                    5, 0);
            weapon.setEquipped(false);
            equipmentRepository.save(weapon);

            // Act
            InventoryResult result = itemService.getInventory(testUserId);

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(1, result.getEquipments().size());
            assertEquals("测试铁剑", result.getEquipments().getFirst().getName());
        }

        @Test
        @DisplayName("已穿戴装备不应该出现在背包中")
        void shouldNotShowEquippedItems() {
            // Arrange
            Equipment weapon = Equipment.create(testUserId, "test_sword", "测试铁剑",
                    EquipmentSlot.WEAPON, Rarity.COMMON,
                    Map.of("str", 3, "con", 0, "agi", 1, "wis", 0),
                    5, 0);
            weapon.setEquipped(true);
            equipmentRepository.save(weapon);

            // Act
            InventoryResult result = itemService.getInventory(testUserId);

            // Assert
            assertTrue(result.isSuccess());
            assertTrue(result.getEquipments().isEmpty());
        }
    }

    @Nested
    @DisplayName("equipItem - 装备穿戴")
    class EquipItemTests {

        @Test
        @DisplayName("应该成功穿戴装备")
        void shouldEquipItemSuccessfully() {
            // Arrange
            Equipment weapon = Equipment.create(testUserId, "test_sword", "测试铁剑",
                    EquipmentSlot.WEAPON, Rarity.COMMON,
                    Map.of("str", 3, "con", 0, "agi", 1, "wis", 0),
                    5, 0);
            weapon.setEquipped(false);
            equipmentRepository.save(weapon);

            // Act
            EquipResult result = itemService.equipItem(testUserId, "测试铁剑");

            // Assert
            assertTrue(result.isSuccess());
            assertEquals("成功装备 [测试铁剑]", result.getMessage());
            assertEquals(EquipmentSlot.WEAPON, result.getSlot());
            assertEquals("武器", result.getSlotName());
            assertNotNull(result.getAttributeChange());
            assertEquals(3, result.getAttributeChange().getStrChange());
            assertEquals(1, result.getAttributeChange().getAgiChange());
        }

        @Test
        @DisplayName("应该替换同部位装备")
        void shouldReplaceExistingEquipment() {
            // Arrange
            Equipment oldWeapon = Equipment.create(testUserId, "old_sword", "旧铁剑",
                    EquipmentSlot.WEAPON, Rarity.COMMON,
                    Map.of("str", 2, "con", 0, "agi", 0, "wis", 0),
                    3, 0);
            oldWeapon.setEquipped(true);
            equipmentRepository.save(oldWeapon);

            Equipment newWeapon = Equipment.create(testUserId, "new_sword", "新铁剑",
                    EquipmentSlot.WEAPON, Rarity.UNCOMMON,
                    Map.of("str", 5, "con", 0, "agi", 1, "wis", 0),
                    8, 0);
            newWeapon.setEquipped(false);
            equipmentRepository.save(newWeapon);

            // Act
            EquipResult result = itemService.equipItem(testUserId, "新铁剑");

            // Assert
            assertTrue(result.isSuccess());
            assertEquals("成功装备 [新铁剑]，替换了 [旧铁剑]", result.getMessage());
            assertEquals(oldWeapon.getId(), result.getReplacedEquipmentId());
            assertEquals(3, result.getAttributeChange().getStrChange()); // 5 - 2
        }

        @Test
        @DisplayName("找不到装备时应该返回失败")
        void shouldFailWhenItemNotFound() {
            // Act
            EquipResult result = itemService.equipItem(testUserId, "不存在的装备");

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("背包中未找到名为 [不存在的装备] 的装备", result.getMessage());
        }

        @Test
        @DisplayName("找到多个匹配装备时应该返回失败")
        void shouldFailWhenMultipleItemsMatch() {
            // Arrange
            Equipment sword1 = Equipment.create(testUserId, "test_sword", "铁剑",
                    EquipmentSlot.WEAPON, Rarity.COMMON,
                    Map.of("str", 3, "con", 0, "agi", 1, "wis", 0),
                    5, 0);
            sword1.setEquipped(false);
            equipmentRepository.save(sword1);

            Equipment sword2 = Equipment.create(testUserId, "test_sword2", "铁剑",
                    EquipmentSlot.WEAPON, Rarity.COMMON,
                    Map.of("str", 3, "con", 0, "agi", 1, "wis", 0),
                    5, 0);
            sword2.setEquipped(false);
            equipmentRepository.save(sword2);

            // Act
            EquipResult result = itemService.equipItem(testUserId, "铁剑");

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("找到多个名为 [铁剑] 的装备，请使用更精确的名称", result.getMessage());
        }
    }

    @Nested
    @DisplayName("unequipItem - 装备卸下")
    class UnequipItemTests {

        @Test
        @DisplayName("应该成功卸下装备")
        void shouldUnequipItemSuccessfully() {
            // Arrange
            Equipment weapon = Equipment.create(testUserId, "test_sword", "测试铁剑",
                    EquipmentSlot.WEAPON, Rarity.COMMON,
                    Map.of("str", 3, "con", 0, "agi", 1, "wis", 0),
                    5, 0);
            weapon.setEquipped(true);
            equipmentRepository.save(weapon);

            // Act
            UnequipResult result = itemService.unequipItem(testUserId, "武器");

            // Assert
            assertTrue(result.isSuccess());
            assertEquals("成功卸下 [武器] 部位的 [测试铁剑]", result.getMessage());
            assertEquals(EquipmentSlot.WEAPON, result.getSlot());
            assertEquals(-3, result.getAttributeChange().getStrChange());
            assertEquals(-60, result.getAttributeChange().getMaxHpChange()); // -3 * 20

            // 验证装备已卸下
            Equipment updated = equipmentRepository.findById(weapon.getId()).orElse(null);
            assertNotNull(updated);
            assertFalse(updated.getEquipped());
        }

        @Test
        @DisplayName("无效部位名称应该返回失败")
        void shouldFailWithInvalidSlotName() {
            // Act
            UnequipResult result = itemService.unequipItem(testUserId, "不存在的部位");

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("无效的装备部位，可选：武器、护甲、头盔、鞋子、饰品", result.getMessage());
        }

        @Test
        @DisplayName("部位未穿戴装备时应该返回失败")
        void shouldFailWhenSlotIsEmpty() {
            // Act
            UnequipResult result = itemService.unequipItem(testUserId, "武器");

            // Assert
            assertFalse(result.isSuccess());
            assertEquals("[武器] 部位未穿戴任何装备", result.getMessage());
        }
    }

    @Nested
    @DisplayName("addStackableItem - 添加堆叠物品")
    class AddStackableItemTests {

        @Test
        @DisplayName("应该成功添加新物品")
        void shouldAddNewItem() {
            // Act
            boolean result = itemService.addStackableItem(testUserId, "material_iron",
                    ItemType.MATERIAL, "精铁", 100);

            // Assert
            assertTrue(result);
            var item = stackableItemRepository.findByUserIdAndTemplateId(testUserId, "material_iron");
            assertTrue(item.isPresent());
            assertEquals("精铁", item.get().getName());
            assertEquals(100, item.get().getQuantity());
        }

        @Test
        @DisplayName("应该增加已存在物品的数量")
        void shouldIncreaseQuantityForExistingItem() {
            // Arrange
            itemService.addStackableItem(testUserId, "material_iron", ItemType.MATERIAL, "精铁", 100);

            // Act
            boolean result = itemService.addStackableItem(testUserId, "material_iron",
                    ItemType.MATERIAL, "精铁", 50);

            // Assert
            assertTrue(result);
            var item = stackableItemRepository.findByUserIdAndTemplateId(testUserId, "material_iron");
            assertTrue(item.isPresent());
            assertEquals(150, item.get().getQuantity());
        }

        @Test
        @DisplayName("用户不存在时应该返回失败")
        void shouldFailWhenUserNotFound() {
            // Act
            boolean result = itemService.addStackableItem(UUID.randomUUID(), "material_iron",
                    ItemType.MATERIAL, "精铁", 100);

            // Assert
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("reduceStackableItem - 减少堆叠物品")
    class ReduceStackableItemTests {

        @Test
        @DisplayName("应该成功减少物品数量")
        void shouldReduceQuantitySuccessfully() {
            // Arrange
            itemService.addStackableItem(testUserId, "material_iron", ItemType.MATERIAL, "精铁", 100);

            // Act
            int result = itemService.reduceStackableItem(testUserId, "material_iron", 30);

            // Assert
            assertEquals(30, result);
            var item = stackableItemRepository.findByUserIdAndTemplateId(testUserId, "material_iron");
            assertTrue(item.isPresent());
            assertEquals(70, item.get().getQuantity());
        }

        @Test
        @DisplayName("数量减为0时应该删除物品")
        void shouldDeleteItemWhenQuantityReachesZero() {
            // Arrange
            itemService.addStackableItem(testUserId, "material_iron", ItemType.MATERIAL, "精铁", 50);

            // Act
            int result = itemService.reduceStackableItem(testUserId, "material_iron", 50);

            // Assert
            assertEquals(50, result);
            var item = stackableItemRepository.findByUserIdAndTemplateId(testUserId, "material_iron");
            assertTrue(item.isEmpty());
        }

        @Test
        @DisplayName("物品不足时应该返回-1")
        void shouldReturnNegativeOneWhenInsufficientQuantity() {
            // Arrange
            itemService.addStackableItem(testUserId, "material_iron", ItemType.MATERIAL, "精铁", 10);

            // Act
            int result = itemService.reduceStackableItem(testUserId, "material_iron", 20);

            // Assert
            assertEquals(-1, result);
            var item = stackableItemRepository.findByUserIdAndTemplateId(testUserId, "material_iron");
            assertTrue(item.isPresent());
            assertEquals(10, item.get().getQuantity()); // 数量不应该变化
        }

        @Test
        @DisplayName("物品不存在时应该返回-1")
        void shouldReturnNegativeOneWhenItemNotFound() {
            // Act
            int result = itemService.reduceStackableItem(testUserId, "non_existent", 10);

            // Assert
            assertEquals(-1, result);
        }
    }

    @Nested
    @DisplayName("hasEnoughStackableItem - 检查物品数量")
    class HasEnoughStackableItemTests {

        @Test
        @DisplayName("数量足够时应该返回true")
        void shouldReturnTrueWhenEnoughQuantity() {
            // Arrange
            itemService.addStackableItem(testUserId, "material_iron", ItemType.MATERIAL, "精铁", 100);

            // Act
            boolean result = itemService.hasEnoughStackableItem(testUserId, "material_iron", 50);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("数量不足时应该返回false")
        void shouldReturnFalseWhenInsufficientQuantity() {
            // Arrange
            itemService.addStackableItem(testUserId, "material_iron", ItemType.MATERIAL, "精铁", 10);

            // Act
            boolean result = itemService.hasEnoughStackableItem(testUserId, "material_iron", 50);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("物品不存在时应该返回false")
        void shouldReturnFalseWhenItemNotFound() {
            // Act
            boolean result = itemService.hasEnoughStackableItem(testUserId, "non_existent", 10);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("数量刚好相等时应该返回true")
        void shouldReturnTrueWhenQuantityExactlyEquals() {
            // Arrange
            itemService.addStackableItem(testUserId, "material_iron", ItemType.MATERIAL, "精铁", 50);

            // Act
            boolean result = itemService.hasEnoughStackableItem(testUserId, "material_iron", 50);

            // Assert
            assertTrue(result);
        }
    }
}
