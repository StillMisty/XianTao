package top.stillmisty.xiantao.service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
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
import top.stillmisty.xiantao.domain.item.vo.*;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.annotation.Authenticated;

/** 装备服务 负责：装备穿戴/卸下、装备生成、装备列表/详情查询 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentService {

  private final UserStateService userStateService;
  private final EquipmentRepository equipmentRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;
  private final ItemResolver itemResolver;

  // ===================== 公开 API（含认证） =====================

  @Authenticated
  @Transactional
  public ServiceResult<EquipResult> equipItem(
      PlatformType platform, String openId, String itemName) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(equipItem(userId, itemName));
  }

  @Authenticated
  @Transactional
  public ServiceResult<UnequipResult> unequipItem(
      PlatformType platform, String openId, String slotName) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(unequipItem(userId, slotName));
  }

  // ===================== 内部 API（需预先完成认证） =====================

  /** 装备穿戴（装备 [物品名/编号]） */
  @Transactional
  public EquipResult equipItem(Long userId, String input) {
    userStateService.loadUser(userId);

    var result = itemResolver.resolveEquipment(userId, input);
    if (result instanceof ItemResolver.NotFound<?>(String input1)) {
      return new EquipResult(
          false, "背包中未找到 [" + input1 + "] 相关的装备", null, null, null, null, null, null, null);
    }
    if (result instanceof ItemResolver.Ambiguous<?> a) {
      return buildAmbiguousEquipResult(a);
    }

    var found = (ItemResolver.Found<Equipment>) result;
    Equipment equipmentToEquip = found.item();

    ReplacementResult replacement = handleSlotReplacement(userId, equipmentToEquip.getSlot());
    equipmentToEquip.setEquipped(true);
    equipmentRepository.save(equipmentToEquip);

    AttributeChange attributeChange =
        calculateAttributeChange(replacement.replacedEquipment, equipmentToEquip);

    String message = buildEquipMessage(equipmentToEquip, replacement);

    return new EquipResult(
        true,
        message,
        equipmentToEquip.getId(),
        equipmentToEquip.getName(),
        equipmentToEquip.getSlot(),
        equipmentToEquip.getSlot().getName(),
        replacement.replacedEquipmentId,
        replacement.replacedEquipmentName,
        attributeChange);
  }

  private EquipResult buildAmbiguousEquipResult(ItemResolver.Ambiguous<?> a) {
    var sb = new StringBuilder("找到多个装备，请使用编号：\n");
    for (var e : a.candidates()) {
      sb.append(e.index())
          .append(". ")
          .append(e.name())
          .append(" [")
          .append(e.metadata())
          .append("]\n");
    }
    return new EquipResult(false, sb.toString().strip(), null, null, null, null, null, null, null);
  }

  private record ReplacementResult(
      Long replacedEquipmentId, String replacedEquipmentName, Equipment replacedEquipment) {}

  private ReplacementResult handleSlotReplacement(Long userId, EquipmentSlot slot) {
    var currentEquipped = equipmentRepository.findEquippedByUserIdAndSlot(userId, slot);

    if (currentEquipped.isEmpty()) {
      return new ReplacementResult(null, null, null);
    }

    Equipment replacedEquipment = currentEquipped.get();
    replacedEquipment.setEquipped(false);
    equipmentRepository.save(replacedEquipment);
    return new ReplacementResult(
        replacedEquipment.getId(), replacedEquipment.getName(), replacedEquipment);
  }

  private String buildEquipMessage(Equipment equipmentToEquip, ReplacementResult replacement) {
    if (replacement.replacedEquipmentName != null) {
      return String.format(
          "成功装备 [%s]，替换了 [%s]", equipmentToEquip.getName(), replacement.replacedEquipmentName);
    }
    return String.format("成功装备 [%s]", equipmentToEquip.getName());
  }

  /** 装备卸下（卸下 [部位/物品名/编号]） */
  @Transactional
  public UnequipResult unequipItem(Long userId, String input) {
    EquipmentSlot slot = EquipmentSlot.fromChineseName(input);
    if (slot == null) {
      return unequipByItemInput(userId, input);
    }

    return equipmentRepository
        .findEquippedByUserIdAndSlot(userId, slot)
        .map(
            equipment -> {
              AttributeChange attributeChange = calculateAttributeChange(equipment, null);

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
        .orElse(
            UnequipResult.builder()
                .success(false)
                .message("[" + slot.getName() + "] 部位未穿戴任何装备")
                .build());
  }

  /** 通过物品名称/编号卸下装备 */
  @Transactional
  public UnequipResult unequipByItemInput(Long userId, String input) {
    var result = itemResolver.resolveEquipment(userId, input);
    if (result instanceof ItemResolver.NotFound<?>(String input1)) {
      return UnequipResult.builder()
          .success(false)
          .message("未找到装备【" + input1 + "】，可输入部位名（法器/护甲/饰品）或装备名称/编号")
          .build();
    }
    if (result instanceof ItemResolver.Ambiguous<?> a) {
      var sb = new StringBuilder("找到多个装备，请使用编号：\n");
      for (var e : a.candidates()) {
        sb.append(e.index())
            .append(". ")
            .append(e.name())
            .append(" [")
            .append(e.metadata())
            .append("]\n");
      }
      return UnequipResult.builder().success(false).message(sb.toString().strip()).build();
    }

    var found = (ItemResolver.Found<Equipment>) result;
    Equipment equipment = found.item();
    if (!equipment.getEquipped()) {
      return UnequipResult.builder()
          .success(false)
          .message("【" + equipment.getName() + "】未装备")
          .build();
    }

    AttributeChange attributeChange = calculateAttributeChange(equipment, null);

    equipment.setEquipped(false);
    equipmentRepository.save(equipment);

    return UnequipResult.builder()
        .success(true)
        .message("成功卸下【" + equipment.getName() + "】")
        .equipmentId(equipment.getId())
        .equipmentName(equipment.getName())
        .slot(equipment.getSlot())
        .slotName(equipment.getSlot().getName())
        .attributeChange(attributeChange)
        .build();
  }

  /** 创建装备实例（稀有度加权随机） */
  @Transactional
  public void createEquipment(Long userId, Long templateId) {
    var equipTmpl = equipmentTemplateRepository.findById(templateId).orElse(null);
    if (equipTmpl == null) return;

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

    String name = rarity.randomPrefix() + equipTmpl.getName();

    Map<String, Integer> statBonus =
        Map.of(
            "STR", equipTmpl.getBaseStr(),
            "CON", equipTmpl.getBaseCon(),
            "AGI", equipTmpl.getBaseAgi(),
            "WIS", equipTmpl.getBaseWis());

    Equipment equipment =
        Equipment.create(
            userId,
            templateId,
            name,
            equipTmpl.getSlot(),
            rarity,
            equipTmpl.getWeaponType(),
            qm,
            affixes,
            statBonus,
            equipTmpl.getBaseAttack(),
            equipTmpl.getBaseDefense());
    equipmentRepository.save(equipment);
  }

  /** 获取装备列表（展开显示） */
  public EquipmentListResult getEquipmentList(Long userId) {
    userStateService.loadUser(userId);

    List<Equipment> allEquipments = equipmentRepository.findByUserId(userId);

    List<EquipmentDetailVO> equipments =
        allEquipments.stream()
            .filter(e -> !e.getEquipped())
            .sorted(
                (a, b) -> {
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

  /** 查看装备详细属性 */
  public EquipmentDetailVO getEquipmentDetail(Long userId, Long equipmentId) {
    return equipmentRepository
        .findById(equipmentId)
        .filter(e -> e.getUserId().equals(userId))
        .map(this::convertToEquipmentDetailVO)
        .orElse(null);
  }

  // ===================== 辅助方法 =====================

  private AttributeChange calculateAttributeChange(
      Equipment replacedEquipment, Equipment newEquipment) {

    int strChange = newEquipment != null ? newEquipment.getStrBonus() : 0;
    int conChange = newEquipment != null ? newEquipment.getConBonus() : 0;
    int agiChange = newEquipment != null ? newEquipment.getAgiBonus() : 0;
    int wisChange = newEquipment != null ? newEquipment.getWisBonus() : 0;
    int attackChange = newEquipment != null ? newEquipment.getAttackBonus() : 0;
    int defenseChange = newEquipment != null ? newEquipment.getDefenseBonus() : 0;
    int maxHpChange = newEquipment != null ? newEquipment.getConBonus() * 20 : 0;

    if (replacedEquipment != null) {
      strChange -= replacedEquipment.getStrBonus();
      conChange -= replacedEquipment.getConBonus();
      agiChange -= replacedEquipment.getAgiBonus();
      wisChange -= replacedEquipment.getWisBonus();
      attackChange -= replacedEquipment.getAttackBonus();
      defenseChange -= replacedEquipment.getDefenseBonus();
      maxHpChange -= replacedEquipment.getConBonus() * 20;
    }

    return new AttributeChange(
        strChange, conChange, agiChange, wisChange, attackChange, defenseChange, maxHpChange);
  }

  private EquipmentDetailVO convertToEquipmentDetailVO(Equipment equipment) {
    List<String> affixDescriptions = new ArrayList<>();
    if (equipment.getAffixes() != null && !equipment.getAffixes().isEmpty()) {
      equipment
          .getAffixes()
          .forEach(
              (key, value) -> {
                AffixType affixType;
                try {
                  affixType = AffixType.fromKey(key);
                } catch (IllegalArgumentException ex) {
                  affixType = null;
                }
                String desc =
                    affixType != null
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
        .weaponTypeName(
            equipment.getWeaponType() != null ? equipment.getWeaponType().getName() : null)
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
