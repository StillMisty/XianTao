package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.vo.CharacterStatusResult;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.user.entity.DaoProtection;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.DaoProtectionRepository;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 角色状态服务
 * 负责：角色详情查询（HP、属性、装备、突破进度、护道信息）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterStatusService {

    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final DaoProtectionRepository daoProtectionRepository;
    private final MapNodeRepository mapNodeRepository;
    private final AuthenticationService authService;

    // ===================== 公开 API（含认证） =====================

    public ServiceResult<CharacterStatusResult> getCharacterStatus(PlatformType platform, String openId) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        return new ServiceResult.Success<>(getCharacterStatus(auth.userId()));
    }

    // ===================== 内部 API（需预先完成认证） =====================

    /**
     * 查看角色状态（状态）
     * 包含：HP、属性、装扮（已穿戴装备）、境界进度（等级经验）、当前状态
     */
    public CharacterStatusResult getCharacterStatus(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

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

        // 获取突破相关信息
        double breakthroughSuccessRate = user.calculateBreakthroughSuccessRate();
        Integer breakthroughFailCount = user.getBreakthroughFailCount() != null ? user.getBreakthroughFailCount() : 0;

        // 获取护道相关信息
        List<DaoProtection> protectingList = daoProtectionRepository.findByProtectorId(userId);
        List<DaoProtection> protectedByList = daoProtectionRepository.findByProtegeId(userId);

        // 转换为 VO
        List<CharacterStatusResult.ProtectionInfoVO> protectingVOList = convertToProtectionInfoVO(protectingList, user);
        List<CharacterStatusResult.ProtectionInfoVO> protectedByVOList = convertToProtectionInfoVOWithBonus(protectedByList, user);

        // 计算总护道加成
        double totalProtectionBonus = protectedByVOList.stream()
                .filter(info -> Boolean.TRUE.equals(info.getIsInSameLocation()))
                .mapToDouble(CharacterStatusResult.ProtectionInfoVO::getBonusPercentage)
                .sum();
        totalProtectionBonus = Math.min(20.0, totalProtectionBonus); // 上限 20%

        return CharacterStatusResult.builder()
                .success(true)
                .message("")
                .userId(user.getId())
                .nickname(user.getNickname())
                .level(user.getLevel())
                .exp(user.getExp())
                .expToNextLevel(user.calculateExpToNextLevel())
                .expPercentage(user.getExp() * 100.0 / user.calculateExpToNextLevel())
                .status(user.getStatus())
                .statusName(user.getStatus() != null ? user.getStatus().getName() : "未知")
                .locationId(user.getLocationId())
                .hpCurrent(user.getHpCurrent())
                .hpMax(hpMax)
                .hpPercentage(user.getHpCurrent() * 100.0 / hpMax)
                .statStr(user.getStatStr())
                .statCon(user.getStatCon())
                .statAgi(user.getStatAgi())
                .statWis(user.getStatWis())
                .equipStr(equipStr)
                .equipCon(equipCon)
                .equipAgi(equipAgi)
                .equipWis(equipWis)
                .totalStr(totalStr)
                .totalCon(totalCon)
                .totalAgi(totalAgi)
                .totalWis(totalWis)
                .attack(attack)
                .defense(defense)
                .spiritStones(user.getSpiritStones())
                .breakthroughSuccessRate(breakthroughSuccessRate)
                .breakthroughFailCount(breakthroughFailCount)
                .protectorCount(protectingList.size())
                .maxProtectorCount(3)
                .protectingList(protectingVOList)
                .protectedByList(protectedByVOList)
                .totalProtectionBonus(totalProtectionBonus)
                .equipment(equipmentSummary)
                .build();
    }

    // ===================== 辅助转换方法 =====================

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

    private List<CharacterStatusResult.ProtectionInfoVO> convertToProtectionInfoVO(
            List<DaoProtection> protections, User currentUser) {
        if (protections == null || protections.isEmpty()) {
            return List.of();
        }

        return protections.stream()
                .map(protection -> {
                    Optional<User> targetUserOpt = userRepository.findById(protection.getProtegeId());
                    if (targetUserOpt.isEmpty()) {
                        return null;
                    }

                    User targetUser = targetUserOpt.get();
                    boolean inSameLocation = isInSameLocation(currentUser, targetUser);
                    double bonus = calculateSingleProtectorBonus(currentUser, targetUser);

                    return CharacterStatusResult.ProtectionInfoVO.builder()
                            .userId(targetUser.getId())
                            .userName(targetUser.getNickname())
                            .userLevel(targetUser.getLevel())
                            .locationId(targetUser.getLocationId())
                            .locationName(getMapName(targetUser.getLocationId()))
                            .isInSameLocation(inSameLocation)
                            .bonusPercentage(bonus)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private List<CharacterStatusResult.ProtectionInfoVO> convertToProtectionInfoVOWithBonus(
            List<DaoProtection> protections, User currentUser) {
        if (protections == null || protections.isEmpty()) {
            return List.of();
        }

        return protections.stream()
                .map(protection -> {
                    Optional<User> protectorOpt = userRepository.findById(protection.getProtectorId());
                    if (protectorOpt.isEmpty()) {
                        return null;
                    }

                    User protector = protectorOpt.get();
                    boolean inSameLocation = isInSameLocation(currentUser, protector);

                    double bonus = 0.0;
                    if (inSameLocation) {
                        bonus = calculateSingleProtectorBonus(protector, currentUser);
                    }

                    return CharacterStatusResult.ProtectionInfoVO.builder()
                            .userId(protector.getId())
                            .userName(protector.getNickname())
                            .userLevel(protector.getLevel())
                            .locationId(protector.getLocationId())
                            .locationName(getMapName(protector.getLocationId()))
                            .isInSameLocation(inSameLocation)
                            .bonusPercentage(bonus)
                            .build();
                })
                .filter(vo -> vo != null)
                .toList();
    }

    private boolean isInSameLocation(User user1, User user2) {
        if (user1.getLocationId() == null || user2.getLocationId() == null) {
            return false;
        }
        return user1.getLocationId().equals(user2.getLocationId());
    }

    private double calculateSingleProtectorBonus(User protector, User protege) {
        int levelDiff = protector.getLevel() - protege.getLevel();
        return 5.0 + (levelDiff * 1.0);
    }

    private String getMapName(Long mapId) {
        if (mapId == null) {
            return "未知";
        }
        Optional<MapNode> mapOpt = mapNodeRepository.findById(mapId);
        return mapOpt.map(MapNode::getName).orElse("未知");
    }
}
