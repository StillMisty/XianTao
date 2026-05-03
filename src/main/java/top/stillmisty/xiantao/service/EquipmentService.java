package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.enums.AffixType;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.vo.*;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 装备服务
 * 负责：装备穿戴/卸下、装备生成、装备列表/详情查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentTemplateRepository equipmentTemplateRepository;
    private final ItemTemplateRepository itemTemplateRepository;
    private final ItemResolver itemResolver;
    // ===================== 公开 API（含认证） =====================

    public ServiceResult<EquipResult> equipItem(PlatformType platform, String openId, String itemName) {
        Long userId = UserContext.getCurrentUserId();
        return new ServiceResult.Success<>(equipItem(userId, itemName));
    }

    public ServiceResult<UnequipResult> unequipItem(PlatformType platform, String openId, String slotName) {
        Long userId = UserContext.getCurrentUserId();
        return new ServiceResult.Success<>(unequipItem(userId, slotName));
    }

    // ===================== 内部 API（需预先完成认证） =====================

    /**
     * 装备穿戴（装备 [物品名/编号]）
     */
    @Transactional
    public EquipResult equipItem(Long userId, String input) {
        userRepository.findById(userId).orElseThrow();

        var result = itemResolver.resolveEquipment(userId, input);
        if (result instanceof ItemResolver.NotFound<?> n) {
            return EquipResult.builder()
                    .success(false)
                    .message("背包中未找到 [" + n.input() + "] 相关的装备")
                    .build();
        }
        if (result instanceof ItemResolver.Ambiguous<?> a) {
            var sb = new StringBuilder("找到多个装备，请使用编号：\n");
            for (var e : a.candidates()) {
                sb.append(e.index()).append(". ").append(e.name()).append(" [").append(e.metadata()).append("]\n");
            }
            return EquipResult.builder()
                    .success(false)
                    .message(sb.toString().strip())
                    .build();
        }

        var found = (ItemResolver.Found<Equipment>) result;
        Equipment equipmentToEquip = found.item();
        EquipmentSlot slot = equipmentToEquip.getSlot();

        var currentEquipped = equipmentRepository.findEquippedByUserIdAndSlot(userId, slot);

        Long replacedEquipmentId = null;
        String replacedEquipmentName = null;
        Equipment replacedEquipment = null;

        if (currentEquipped.isPresent()) {
            replacedEquipment = currentEquipped.get();
            replacedEquipment.setEquipped(false);
            equipmentRepository.save(replacedEquipment);
            replacedEquipmentId = replacedEquipment.getId();
            replacedEquipmentName = replacedEquipment.getName();
        }

        equipmentToEquip.setEquipped(true);
        equipmentRepository.save(equipmentToEquip);

        AttributeChange attributeChange = calculateAttributeChange(
                replacedEquipment, equipmentToEquip);

        String message;
        if (replacedEquipmentName != null) {
            message = String.format(
                    "成功装备 [%s]，替换了 [%s]",
                    equipmentToEquip.getName(), replacedEquipmentName
            );
        } else {
            message = String.format("成功装备 [%s]", equipmentToEquip.getName());
        }

        return EquipResult.builder()
                .success(true)
                .message(message)
                .equipmentId(equipmentToEquip.getId())
                .equipmentName(equipmentToEquip.getName())
                .slot(slot)
                .slotName(slot.getName())
                .replacedEquipmentId(replacedEquipmentId)
                .replacedEquipmentName(replacedEquipmentName)
                .attributeChange(attributeChange)
                .build();
    }

    /**
     * 装备卸下（卸下 [部位]）
     */
    @Transactional
    public UnequipResult unequipItem(Long userId, String slotName) {
        EquipmentSlot slot = EquipmentSlot.fromChineseName(slotName);
        if (slot == null) {
            return UnequipResult.builder()
                    .success(false)
                    .message("无效的装备部位，可选：法器、护甲、饰品")
                    .build();
        }

        return equipmentRepository.findEquippedByUserIdAndSlot(userId, slot)
                .map(equipment -> {
                    AttributeChange attributeChange = AttributeChange.builder()
                            .strChange(-equipment.getStrBonus())
                            .conChange(-equipment.getConBonus())
                            .agiChange(-equipment.getAgiBonus())
                            .wisChange(-equipment.getWisBonus())
                            .attackChange(-equipment.getAttackBonus())
                            .defenseChange(-equipment.getDefenseBonus())
                            .maxHpChange(-equipment.getConBonus() * 20)
                            .build();

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
     * 创建装备实例（稀有度加权随机）
     */
    @Transactional
    public Equipment createEquipment(Long userId, Long templateId) {
        var equipTmpl = equipmentTemplateRepository.findByTemplateId(templateId).orElse(null);
        if (equipTmpl == null) return null;
        var itemTmpl = itemTemplateRepository.findById(templateId).orElse(null);
        if (itemTmpl == null) return null;

        Rarity rarity = Rarity.roll(equipTmpl.getDropWeight());
        double qm = rarity.randomQualityMultiplier();

        int affixCount = rarity.randomAffixCount();
        Map<String, Integer> affixes = new LinkedHashMap<>();
        List<AffixType> pool = new ArrayList<>(List.of(AffixType.getAttributeAffixes()));
        if (rarity == Rarity.LEGENDARY) {
            pool.addAll(List.of(AffixType.getSpecialAffixes()));
        }
        Collections.shuffle(pool, ThreadLocalRandom.current());
        for (int i = 0; i < affixCount && i < pool.size(); i++) {
            AffixType at = pool.get(i);
            int value = at.isSpecial() ? 5 : (1 + (int) (Math.random() * 4));
            if (at.getStatField() != null) {
                affixes.put(at.getStatField(), value);
            } else {
                affixes.put(at.name(), value);
            }
        }

        String name = rarity.randomPrefix() + itemTmpl.getName();

        Map<String, Integer> statBonus = Map.of(
                "str", equipTmpl.getBaseStr(),
                "con", equipTmpl.getBaseCon(),
                "agi", equipTmpl.getBaseAgi(),
                "wis", equipTmpl.getBaseWis()
        );

        Equipment equipment = Equipment.create(
                userId, templateId, name,
                equipTmpl.getSlot(), rarity, equipTmpl.getWeaponType(),
                qm, affixes, statBonus,
                equipTmpl.getBaseAttack(), equipTmpl.getBaseDefense()
        );
        equipmentRepository.save(equipment);
        return equipment;
    }

    /**
     * 获取装备列表（展开显示）
     */
    public EquipmentListResult getEquipmentList(Long userId) {
        userRepository.findById(userId).orElseThrow();

        List<Equipment> allEquipments = equipmentRepository.findByUserId(userId);

        List<EquipmentDetailVO> equipments = allEquipments.stream()
                .filter(e -> !e.getEquipped())
                .sorted((a, b) -> {
                    int rarityCompare = b.getRarity().ordinal() - a.getRarity().ordinal();
                    if (rarityCompare != 0) return rarityCompare;
                    int aForge = a.getForgeLevel();
                    int bForge = b.getForgeLevel();
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
    }

    /**
     * 查看装备详细属性
     */
    public EquipmentDetailVO getEquipmentDetail(Long userId, Long equipmentId) {
        return equipmentRepository.findById(equipmentId)
                .filter(e -> e.getUserId().equals(userId))
                .map(this::convertToEquipmentDetailVO)
                .orElse(null);
    }

    // ===================== 辅助方法 =====================

    private AttributeChange calculateAttributeChange(
            Equipment replacedEquipment, Equipment newEquipment) {

        int strChange = newEquipment.getStrBonus();
        int conChange = newEquipment.getConBonus();
        int agiChange = newEquipment.getAgiBonus();
        int wisChange = newEquipment.getWisBonus();
        int attackChange = newEquipment.getAttackBonus();
        int defenseChange = newEquipment.getDefenseBonus();
        int maxHpChange = newEquipment.getConBonus() * 20;

        if (replacedEquipment != null) {
            strChange -= replacedEquipment.getStrBonus();
            conChange -= replacedEquipment.getConBonus();
            agiChange -= replacedEquipment.getAgiBonus();
            wisChange -= replacedEquipment.getWisBonus();
            attackChange -= replacedEquipment.getAttackBonus();
            defenseChange -= replacedEquipment.getDefenseBonus();
            maxHpChange -= replacedEquipment.getConBonus() * 20;
        }

        return AttributeChange.builder()
                .strChange(strChange)
                .conChange(conChange)
                .agiChange(agiChange)
                .wisChange(wisChange)
                .attackChange(attackChange)
                .defenseChange(defenseChange)
                .maxHpChange(maxHpChange)
                .build();
    }

    private EquipmentDetailVO convertToEquipmentDetailVO(Equipment equipment) {
        List<String> affixDescriptions = new ArrayList<>();
        if (equipment.getAffixes() != null && !equipment.getAffixes().isEmpty()) {
            equipment.getAffixes().forEach((key, value) -> {
                var affixType = AffixType.fromKey(key);
                String desc = affixType != null
                        ? (affixType.isSpecial()
                            ? String.format("%s +%d%%", affixType.getDisplayName(), value)
                            : String.format("%s +%d", affixType.getDisplayName(), value))
                        : key + " +" + value;
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
                .weaponType(equipment.getWeaponType())
                .weaponTypeName(equipment.getWeaponType() != null ? equipment.getWeaponType().getName() : null)
                .qualityMultiplier(equipment.getQualityMultiplier())
                .forgeLevel(equipment.getForgeLevel())
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
