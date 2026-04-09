package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.entity.InventoryItem;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.item.vo.*;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 物品服务
 * 处理：状态查看、背包查看、装备穿戴、装备卸下
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final StackableItemRepository stackableItemRepository;
    private final ItemTemplateRepository itemTemplateRepository;

    /**
     * 查看角色状态（状态）
     * 包含：HP、属性、装扮（已穿戴装备）、境界进度（等级经验）、当前状态
     *
     * @param userId 用户 ID
     * @return 角色完整状态 VO
     */
    public CharacterStatusResult getCharacterStatus(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    // 获取已穿戴装备
                    List<Equipment> equippedItems = equipmentRepository.findEquippedByUserId(userId);

                    // 计算装备加成
                    int equipStr = 0, equipCon = 0, equipAgi = 0, equipWis = 0;
                    int equipAttack = 0, equipDefense = 0;

                    for (Equipment equipment : equippedItems) {
                        equipStr += equipment.getStrBonus();
                        equipCon += equipment.getConBonus();
                        equipAgi += equipment.getAgiBonus();
                        equipWis += equipment.getWisBonus();
                        equipAttack += equipment.getAttackBonus() != null ? equipment.getAttackBonus() : 0;
                        equipDefense += equipment.getDefenseBonus() != null ? equipment.getDefenseBonus() : 0;
                    }

                    // 计算最终属性
                    int totalStr = user.getStatStr() + equipStr;
                    int totalCon = user.getStatCon() + equipCon;
                    int totalAgi = user.getStatAgi() + equipAgi;
                    int totalWis = user.getStatWis() + equipWis;

                    // 计算战斗属性
                    int attack = totalStr * 2 + equipAttack;
                    int defense = totalCon + equipDefense;
                    int hpMax = 100 + totalCon * 20;

                    // 转换装备为VO
                    CharacterStatusResult.EquipmentSummary equipmentSummary = CharacterStatusResult.EquipmentSummary.builder()
                            .totalEquipped(equippedItems.size())
                            .items(equippedItems.stream()
                                    .map(this::convertToEquipmentSummaryItem)
                                    .toList())
                            .build();

                    return CharacterStatusResult.builder()
                            .success(true)
                            .message("")
                            .userId(user.getId())
                            .nickname(user.getNickname())
                            // 境界进度
                            .level(user.getLevel())
                            .exp(user.getExp())
                            .expToNextLevel(user.calculateExpToNextLevel())
                            .expPercentage(user.getExp() * 100.0 / user.calculateExpToNextLevel())
                            // 当前状态
                            .status(user.getStatus())
                            .statusName(user.getStatus().getName())
                            .locationId(user.getLocationId())
                            // HP
                            .hpCurrent(user.getHpCurrent())
                            .hpMax(hpMax)
                            .hpPercentage(user.getHpCurrent() * 100.0 / hpMax)
                            // 基础属性
                            .baseStr(user.getStatStr())
                            .baseCon(user.getStatCon())
                            .baseAgi(user.getStatAgi())
                            .baseWis(user.getStatWis())
                            // 装备加成
                            .equipStr(equipStr)
                            .equipCon(equipCon)
                            .equipAgi(equipAgi)
                            .equipWis(equipWis)
                            // 最终属性
                            .totalStr(totalStr)
                            .totalCon(totalCon)
                            .totalAgi(totalAgi)
                            .totalWis(totalWis)
                            // 战斗属性
                            .attack(attack)
                            .defense(defense)
                            // 自由属性点
                            .freeStatPoints(user.getFreeStatPoints())
                            // 货币
                            .coins(user.getCoins())
                            .spiritStones(user.getSpiritStones())
                            // 装扮
                            .equipment(equipmentSummary)
                            .build();
                })
                .orElse(CharacterStatusResult.builder()
                        .success(false)
                        .message("用户不存在")
                        .build());
    }

    /**
     * 查看背包（背包）
     * 包含：装备、材料、种子、灵蛋、消耗品、灵石/铜币数量
     *
     * @param userId 用户 ID
     * @return 背包内容 VO
     */
    public InventoryResult getInventory(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    // 获取并分类物品
                    List<Equipment> allEquipments = equipmentRepository.findByUserId(userId);
                    List<StackableItem> stackableItems = stackableItemRepository.findByUserId(userId);

                    // 过滤并按稀有度排序未穿戴装备
                    List<InventoryItem> equipments = allEquipments.stream()
                            .filter(e -> !e.getEquipped())
                            .sorted((a, b) -> b.getRarity().ordinal() - a.getRarity().ordinal())
                            .map(e -> InventoryItem.forEquipment(e.getId(), e.getName()))
                            .toList();

                    // 按类型分组堆叠物品
                    Map<ItemType, List<InventoryItem>> groupedItems = stackableItems.stream()
                            .collect(Collectors.groupingBy(
                                    StackableItem::getItemType,
                                    Collectors.mapping(
                                            item -> InventoryItem.forStackable(
                                                    item.getTemplateId(),
                                                    item.getItemType(),
                                                    item.getName(),
                                                    item.getQuantity()
                                            ),
                                            Collectors.toList()
                                    )
                            ));

                    // 计算总物品数量
                    int totalSize = equipments.size()
                            + groupedItems.getOrDefault(ItemType.MATERIAL, List.of()).size()
                            + groupedItems.getOrDefault(ItemType.SEED, List.of()).size()
                            + groupedItems.getOrDefault(ItemType.SPIRIT_EGG, List.of()).size()
                            + groupedItems.getOrDefault(ItemType.CONSUMABLE, List.of()).size();

                    return InventoryResult.builder()
                            .success(true)
                            .userId(userId)
                            .capacity(50)
                            .currentSize(totalSize)
                            .equipments(equipments)
                            .materials(groupedItems.getOrDefault(ItemType.MATERIAL, List.of()))
                            .seeds(groupedItems.getOrDefault(ItemType.SEED, List.of()))
                            .spiritEggs(groupedItems.getOrDefault(ItemType.SPIRIT_EGG, List.of()))
                            .consumables(groupedItems.getOrDefault(ItemType.CONSUMABLE, List.of()))
                            .coins(user.getCoins())
                            .spiritStones(user.getSpiritStones())
                            .build();
                })
                .orElse(InventoryResult.builder()
                        .success(false)
                        .message("用户不存在")
                        .build());
    }

    /**
     * 装备穿戴（装备 [物品名]）
     * 将背包中的武器或防具穿戴到身上，直接影响四维属性与攻防数值
     *
     * @param userId 用户 ID
     * @param itemName 物品名称
     * @return 装备穿戴结果
     */
    public EquipResult equipItem(UUID userId, String itemName) {
        return userRepository.findById(userId)
                .flatMap(user -> {
                    // 查找未穿戴且名称匹配的装备
                    List<Equipment> availableEquipments = equipmentRepository.findByUserId(userId).stream()
                            .filter(e -> !e.getEquipped() && e.getName().contains(itemName))
                            .toList();

                    if (availableEquipments.isEmpty()) {
                        return java.util.Optional.of(EquipResult.builder()
                                .success(false)
                                .message("背包中未找到名为 [" + itemName + "] 的装备")
                                .build());
                    }

                    if (availableEquipments.size() > 1) {
                        return java.util.Optional.of(EquipResult.builder()
                                .success(false)
                                .message("找到多个名为 [" + itemName + "] 的装备，请使用更精确的名称")
                                .build());
                    }

                    Equipment equipmentToEquip = availableEquipments.getFirst();
                    EquipmentSlot slot = equipmentToEquip.getSlot();

                    // 检查该部位是否已有装备
                    var currentEquipped = equipmentRepository.findEquippedByUserIdAndSlot(userId, slot);

                    UUID replacedEquipmentId = null;
                    String replacedEquipmentName = null;
                    Equipment replacedEquipment = null;

                    // 如果有装备，先卸下
                    if (currentEquipped.isPresent()) {
                        replacedEquipment = currentEquipped.get();
                        replacedEquipment.setEquipped(false);
                        equipmentRepository.save(replacedEquipment);
                        replacedEquipmentId = replacedEquipment.getId();
                        replacedEquipmentName = replacedEquipment.getName();
                    }

                    // 穿戴新装备
                    equipmentToEquip.setEquipped(true);
                    equipmentRepository.save(equipmentToEquip);

                    // 计算属性变化
                    EquipResult.AttributeChange attributeChange = calculateAttributeChange(
                            replacedEquipment, equipmentToEquip);

                    String message;
                    if (replacedEquipmentName != null) {
                        message = String.format("成功装备 [%s]，替换了 [%s]",
                                equipmentToEquip.getName(), replacedEquipmentName);
                    } else {
                        message = String.format("成功装备 [%s]", equipmentToEquip.getName());
                    }

                    return java.util.Optional.of(EquipResult.builder()
                            .success(true)
                            .message(message)
                            .equipmentId(equipmentToEquip.getId())
                            .equipmentName(equipmentToEquip.getName())
                            .slot(slot)
                            .slotName(slot.getName())
                            .replacedEquipmentId(replacedEquipmentId)
                            .replacedEquipmentName(replacedEquipmentName)
                            .attributeChange(attributeChange)
                            .build());
                })
                .orElse(EquipResult.builder()
                        .success(false)
                        .message("用户不存在")
                        .build());
    }

    /**
     * 装备卸下（卸下 [部位]）
     * 将身上的装备放回背包
     *
     * @param userId 用户 ID
     * @param slotName 部位名称（中文，如"武器"、"护甲"等）
     * @return 装备卸下结果
     */
    public UnequipResult unequipItem(UUID userId, String slotName) {
        // 查找对应的部位枚举
        EquipmentSlot slot = EquipmentSlot.fromChineseName(slotName);
        if (slot == null) {
            return UnequipResult.builder()
                    .success(false)
                    .message("无效的装备部位，可选：武器、护甲、头盔、鞋子、饰品")
                    .build();
        }

        return equipmentRepository.findEquippedByUserIdAndSlot(userId, slot)
                .map(equipment -> {
                    // 先获取装备属性用于计算变化
                    UnequipResult.AttributeChange attributeChange = UnequipResult.AttributeChange.builder()
                            .strChange(-equipment.getStrBonus())
                            .conChange(-equipment.getConBonus())
                            .agiChange(-equipment.getAgiBonus())
                            .wisChange(-equipment.getWisBonus())
                            .attackChange(-(equipment.getAttackBonus() != null ? equipment.getAttackBonus() : 0))
                            .defenseChange(-(equipment.getDefenseBonus() != null ? equipment.getDefenseBonus() : 0))
                            .maxHpChange(-equipment.getConBonus() * 20) // 体质影响HP
                            .build();

                    // 卸下装备
                    equipment.setEquipped(false);
                    equipmentRepository.save(equipment);

                    return UnequipResult.builder()
                            .success(true)
                            .message(String.format("成功卸下 [%s] 部位的 [%s]", slot.getName(), equipment.getName()))
                            .equipmentId(equipment.getId())
                            .equipmentName(equipment.getName())
                            .slot(slot)
                            .slotName(slot.getName())
                            .attributeChange(attributeChange)
                            .build();
                })
                .orElse(UnequipResult.builder()
                        .success(false)
                        .message("[" + slotName + "] 部位未穿戴任何装备")
                        .build());
    }

    /**
     * 计算属性变化（用于装备时显示）
     */
    private EquipResult.AttributeChange calculateAttributeChange(
            Equipment replacedEquipment, Equipment newEquipment) {

        int strChange = newEquipment.getStrBonus();
        int conChange = newEquipment.getConBonus();
        int agiChange = newEquipment.getAgiBonus();
        int wisChange = newEquipment.getWisBonus();
        int attackChange = newEquipment.getAttackBonus() != null ? newEquipment.getAttackBonus() : 0;
        int defenseChange = newEquipment.getDefenseBonus() != null ? newEquipment.getDefenseBonus() : 0;
        int maxHpChange = newEquipment.getConBonus() * 20;

        // 如果替换了装备，减去被替换装备的属性
        if (replacedEquipment != null) {
            strChange -= replacedEquipment.getStrBonus();
            conChange -= replacedEquipment.getConBonus();
            agiChange -= replacedEquipment.getAgiBonus();
            wisChange -= replacedEquipment.getWisBonus();
            attackChange -= (replacedEquipment.getAttackBonus() != null ? replacedEquipment.getAttackBonus() : 0);
            defenseChange -= (replacedEquipment.getDefenseBonus() != null ? replacedEquipment.getDefenseBonus() : 0);
            maxHpChange -= replacedEquipment.getConBonus() * 20;
        }

        return EquipResult.AttributeChange.builder()
                .strChange(strChange)
                .conChange(conChange)
                .agiChange(agiChange)
                .wisChange(wisChange)
                .attackChange(attackChange)
                .defenseChange(defenseChange)
                .maxHpChange(maxHpChange)
                .build();
    }

    /**
     * 转换为装备摘要项
     */
    private CharacterStatusResult.EquipmentSummaryItem convertToEquipmentSummaryItem(Equipment equipment) {
        return CharacterStatusResult.EquipmentSummaryItem.builder()
                .equipmentId(equipment.getId())
                .name(equipment.getName())
                .slot(equipment.getSlot())
                .slotName(equipment.getSlot().getName())
                .rarity(equipment.getRarity())
                .rarityName(equipment.getRarity().getName())
                .strBonus(equipment.getStrBonus())
                .conBonus(equipment.getConBonus())
                .agiBonus(equipment.getAgiBonus())
                .wisBonus(equipment.getWisBonus())
                .attackBonus(equipment.getAttackBonus())
                .defenseBonus(equipment.getDefenseBonus())
                .build();
    }

    // ===================== 堆叠物品辅助方法 =====================

    /**
     * 添加堆叠物品到背包
     *
     * @param userId 用户 ID
     * @param templateId 物品模板 ID
     * @param itemType 物品类型
     * @param name 物品名称
     * @param quantity 数量
     * @return 是否添加成功
     */
    public boolean addStackableItem(UUID userId, UUID templateId, ItemType itemType,
                                     String name, int quantity) {
        return userRepository.findById(userId)
                .map(user -> {
                    // 查找是否已存在该物品
                    var existingItem = stackableItemRepository.findByUserIdAndTemplateId(userId, templateId);

                    if (existingItem.isPresent()) {
                        // 增加数量
                        StackableItem item = existingItem.get();
                        item.addQuantity(quantity);
                        stackableItemRepository.save(item);
                        log.info("增加堆叠物品数量: userId={}, templateId={}, quantity={}", userId, templateId, quantity);
                    } else {
                        // 创建新物品
                        StackableItem newItem = StackableItem.create(userId, templateId, itemType, name, quantity);
                        stackableItemRepository.save(newItem);
                        log.info("添加新堆叠物品: userId={}, templateId={}, quantity={}", userId, templateId, quantity);
                    }

                    return true;
                })
                .orElse(false);
    }

    /**
     * 减少堆叠物品数量
     *
     * @param userId 用户 ID
     * @param templateId 物品模板 ID
     * @param quantity 减少的数量
     * @return 实际减少的数量，如果物品不足则返回 -1
     */
    public int reduceStackableItem(UUID userId, UUID templateId, int quantity) {
        var existingItem = stackableItemRepository.findByUserIdAndTemplateId(userId, templateId);

        if (existingItem.isEmpty()) {
            return -1;
        }

        StackableItem item = existingItem.get();

        if (!item.hasEnoughQuantity(quantity)) {
            return -1;
        }

        if (!item.reduceQuantity(quantity)) {
            // 数量减少到0，删除物品
            stackableItemRepository.deleteById(item.getId());
            log.info("删除堆叠物品（数量为0）: userId={}, templateId={}", userId, templateId);
        } else {
            stackableItemRepository.save(item);
            log.info("减少堆叠物品数量: userId={}, templateId={}, quantity={}", userId, templateId, quantity);
        }

        return quantity;
    }

    /**
     * 检查堆叠物品数量是否足够
     *
     * @param userId 用户 ID
     * @param templateId 物品模板 ID
     * @param quantity 需要的数量
     * @return 是否足够
     */
    public boolean hasEnoughStackableItem(UUID userId, UUID templateId, int quantity) {
        return stackableItemRepository.findByUserIdAndTemplateId(userId, templateId)
                .map(item -> item.hasEnoughQuantity(quantity))
                .orElse(false);
    }

    // ===================== 背包摘要方法（按品质折叠） =====================

    /**
     * 获取背包摘要（按品质折叠装备）
     * 用于 #背包 命令，防止刷屏
     *
     * @param userId 用户 ID
     * @return 背包摘要 VO
     */
    public InventorySummaryVO getInventorySummary(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    // 获取所有装备和堆叠物品
                    List<Equipment> allEquipments = equipmentRepository.findByUserId(userId);
                    List<StackableItem> stackableItems = stackableItemRepository.findByUserId(userId);

                    // 过滤未穿戴装备并按品质分组统计
                    Map<String, Integer> equipmentByQuality = allEquipments.stream()
                            .filter(e -> !e.getEquipped())
                            .collect(Collectors.groupingBy(
                                    e -> e.getRarity().getName(),
                                    Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                            ));

                    // 按类型统计堆叠物品
                    Map<ItemType, Integer> stackableItemCount = new HashMap<>();
                    for (StackableItem item : stackableItems) {
                        stackableItemCount.merge(item.getItemType(), 1, Integer::sum);
                    }

                    // 计算总容量使用
                    int usedSlots = (int) allEquipments.stream().filter(e -> !e.getEquipped()).count()
                            + stackableItemCount.values().stream().mapToInt(Integer::intValue).sum();

                    return InventorySummaryVO.builder()
                            .capacity(50)
                            .usedSlots(usedSlots)
                            .equipmentByQuality(equipmentByQuality)
                            .stackableItemCount(stackableItemCount)
                            .coins(user.getCoins())
                            .spiritStones(user.getSpiritStones())
                            .build();
                })
                .orElse(null);
    }

    /**
     * 获取装备列表（展开显示）
     * 用于 #背包 装备 命令
     *
     * @param userId 用户 ID
     * @return 装备列表 VO
     */
    public EquipmentListResult getEquipmentList(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    List<Equipment> allEquipments = equipmentRepository.findByUserId(userId);

                    List<EquipmentDetailVO> equipments = allEquipments.stream()
                            .filter(e -> !e.getEquipped())
                            .sorted((a, b) -> {
                                // 按品质排序：金>紫>蓝>绿>白
                                int rarityCompare = b.getRarity().ordinal() - a.getRarity().ordinal();
                                if (rarityCompare != 0) return rarityCompare;
                                // 品质相同按锻造等级排序
                                Integer aForge = a.getForgeLevel() != null ? a.getForgeLevel() : 0;
                                Integer bForge = b.getForgeLevel() != null ? b.getForgeLevel() : 0;
                                return bForge - aForge;
                            })
                            .map(this::convertToEquipmentDetailVO)
                            .toList();

                    return EquipmentListResult.builder()
                            .success(true)
                            .userId(userId)
                            .equipments(equipments)
                            .totalCount(equipments.size())
                            .build();
                })
                .orElse(EquipmentListResult.builder()
                        .success(false)
                        .message("用户不存在")
                        .build());
    }

    /**
     * 查看装备详细属性
     * 用于 #查看 [装备UUID] 命令
     *
     * @param userId    用户 ID
     * @param equipmentId 装备 UUID
     * @return 装备详情 VO
     */
    public EquipmentDetailVO getEquipmentDetail(UUID userId, UUID equipmentId) {
        return equipmentRepository.findById(equipmentId)
                .filter(e -> e.getUserId().equals(userId))
                .map(this::convertToEquipmentDetailVO)
                .orElse(null);
    }

    // ===================== 标签搜索方法（地灵AI联动） =====================

    /**
     * 按标签搜索堆叠物品
     * 用于地灵AI执行标签查询，如 ["seed", "fire"]
     *
     * @param userId 用户 ID
     * @param tags   要搜索的标签列表（AND关系，需包含所有标签）
     * @return 匹配的物品列表
     */
    public List<StackableItem> searchStackableItemsByTags(UUID userId, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }

        List<StackableItem> allItems = stackableItemRepository.findByUserId(userId);

        return allItems.stream()
                .filter(item -> item.hasAllTags(tags))
                .toList();
    }

    /**
     * 按标签搜索堆叠物品（OR关系，包含任一标签即可）
     *
     * @param userId 用户 ID
     * @param tags   要搜索的标签列表
     * @return 匹配的物品列表
     */
    public List<StackableItem> searchStackableItemsByAnyTag(UUID userId, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }

        List<StackableItem> allItems = stackableItemRepository.findByUserId(userId);

        return allItems.stream()
                .filter(item -> tags.stream().anyMatch(item::hasTag))
                .toList();
    }

    /**
     * 按物品类型搜索堆叠物品
     *
     * @param userId 用户 ID
     * @param type   物品类型
     * @return 匹配的物品列表
     */
    public List<StackableItem> searchStackableItemsByType(UUID userId, ItemType type) {
        return stackableItemRepository.findByUserId(userId).stream()
                .filter(item -> item.getItemType() == type)
                .toList();
    }

    // ===================== 辅助转换方法 =====================

    /**
     * 转换为装备详情 VO
     */
    private EquipmentDetailVO convertToEquipmentDetailVO(Equipment equipment) {
        // 生成词条描述列表
        List<String> affixDescriptions = new ArrayList<>();
        if (equipment.getAffixes() != null && !equipment.getAffixes().isEmpty()) {
            equipment.getAffixes().forEach((key, value) -> {
                String desc = switch (key.toUpperCase()) {
                    case "STR" -> String.format("力量 +%d", value);
                    case "CON" -> String.format("体质 +%d", value);
                    case "AGI" -> String.format("敏捷 +%d", value);
                    case "WIS" -> String.format("智慧 +%d", value);
                    case "LIFE_STEAL" -> String.format("吸血 +%d%%", value);
                    case "TREASURE_HUNT" -> String.format("寻宝 +%d%%", value);
                    default -> key + " +" + value;
                };
                affixDescriptions.add(desc);
            });
        }

        return EquipmentDetailVO.builder()
                .id(equipment.getId())
                .displayName(equipment.getName())
                .templateId(equipment.getTemplateId())
                .rarity(equipment.getRarity())
                .rarityName(equipment.getRarity().getName())
                .rarityEmoji(equipment.getRarity().getColor().getEmoji())
                .slot(equipment.getSlot())
                .slotName(equipment.getSlot().getName())
                .qualityMultiplier(equipment.getQualityMultiplier())
                .forgeLevel(equipment.getForgeLevel() != null ? equipment.getForgeLevel() : 0)
                .attack(equipment.getFinalAttack())
                .defense(equipment.getFinalDefense())
                .strBonus(equipment.getStrBonus())
                .conBonus(equipment.getConBonus())
                .agiBonus(equipment.getAgiBonus())
                .wisBonus(equipment.getWisBonus())
                .affixes(equipment.getAffixes())
                .affixDescriptions(affixDescriptions)
                .equipped(equipment.getEquipped())
                .build();
    }
}
