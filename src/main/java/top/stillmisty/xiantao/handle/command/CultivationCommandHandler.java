package top.stillmisty.xiantao.handle.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.vo.AttributeChange;
import top.stillmisty.xiantao.domain.item.vo.CharacterStatusResult;
import top.stillmisty.xiantao.domain.item.vo.InventorySummaryVO;
import top.stillmisty.xiantao.domain.user.enums.AttributeType;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.vo.*;
import top.stillmisty.xiantao.service.*;

/**
 * 修仙命令处理器（纯 View 层）
 * 调用 Service 层获取结构化数据，格式化为纯文本返回
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CultivationCommandHandler {

    private final UserService userService;
    private final ItemService itemService;
    private final CultivationService cultivationService;
    private final StaminaService staminaService;

    /**
     * 注册（UserService.createUser 内部做重复注册检查）
     */
    public String handleRegister(PlatformType platform, String openId, String nickname) {
        log.debug("处理注册请求 - Platform: {}, OpenId: {}, Nickname: {}", platform, openId, nickname);
        var result = userService.createUser(platform, openId, nickname);
        if (!result.success()) {
            log.error("用户创建失败 - OpenId: {}, Nickname: {}", openId, nickname);
            return result.message() != null ? result.message() : "系统错误：用户创建失败，请联系管理员";
        }
        log.info("用户注册成功 - UserId: {}, Nickname: {}", result.userId(), result.nickname());
        return String.format("欢迎踏入仙途！您的道号为：%s\n输入「状态」查看您的角色信息", result.nickname());
    }

    public String handleStatus(PlatformType platform, String openId) {
        log.debug("处理状态查询 - Platform: {}, OpenId: {}", platform, openId);
        var status = itemService.getCharacterStatus(platform, openId);
        return switch (status) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> {
                var stamina = staminaService.getStaminaInfo(platform, openId);
                String staminaText = switch (stamina) {
                    case ServiceResult.Failure(var m) -> m;
                    case ServiceResult.Success(var svo) -> formatStaminaInfo(svo);
                };
                yield formatCharacterStatus(vo) + "\n\n" + staminaText;
            }
        };
    }

    public String handleStaminaQuery(PlatformType platform, String openId) {
        log.debug("处理体力查询 - Platform: {}, OpenId: {}", platform, openId);
        return switch (staminaService.getStaminaInfo(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatStaminaInfo(vo);
        };
    }

    public String handleInventory(PlatformType platform, String openId) {
        log.debug("处理背包查询 - Platform: {}, OpenId: {}", platform, openId);
        return switch (itemService.getInventorySummary(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatInventorySummary(vo);
        };
    }

    public String handleEquip(PlatformType platform, String openId, String itemName) {
        log.debug("处理装备穿戴 - Platform: {}, OpenId: {}, ItemName: {}", platform, openId, itemName);
        return switch (itemService.equipItem(platform, openId, itemName)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> vo.isSuccess() ? formatEquipResult(vo) : vo.getMessage();
        };
    }

    public String handleUnequip(PlatformType platform, String openId, String slotName) {
        log.debug("处理装备卸下 - Platform: {}, OpenId: {}, SlotName: {}", platform, openId, slotName);
        return switch (itemService.unequipItem(platform, openId, slotName)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> vo.isSuccess() ? formatUnequipResult(vo) : vo.getMessage();
        };
    }

    public String handleAllocatePoints(PlatformType platform, String openId, String attributeName, int points) {
        log.debug("处理属性加点 - Platform: {}, OpenId: {}, Attribute: {}, Points: {}", platform, openId, attributeName, points);
        AttributeType attributeType = parseAttributeType(attributeName);
        if (attributeType == null) {
            return "无效的属性名称，请使用：力量、体质、敏捷、智慧";
        }
        return switch (cultivationService.allocateAttributePoints(platform, openId, attributeType, points)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> vo.isSuccess() ? formatAttributeAllocationResult(vo) : vo.getMessage();
        };
    }

    public String handleResetPoints(PlatformType platform, String openId) {
        log.debug("处理洗点 - Platform: {}, OpenId: {}", platform, openId);
        return switch (cultivationService.resetStatPoints(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> vo.isSuccess() ? formatStatResetResult(vo) : vo.getMessage();
        };
    }

    public String handleBreakthrough(PlatformType platform, String openId) {
        log.debug("处理突破 - Platform: {}, OpenId: {}", platform, openId);
        return switch (cultivationService.attemptBreakthrough(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatBreakthroughResult(vo);
        };
    }

    public String handleEstablishProtection(PlatformType platform, String openId, String protegeNickname) {
        log.debug("处理护道 - Platform: {}, OpenId: {}, Protege: {}", platform, openId, protegeNickname);
        return switch (cultivationService.establishProtection(platform, openId, protegeNickname)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> vo.isSuccess() ? formatProtectionResult(vo) : vo.getMessage();
        };
    }

    public String handleRemoveProtection(PlatformType platform, String openId, String protegeNickname) {
        log.debug("处理护道解除 - Platform: {}, OpenId: {}, Protege: {}", platform, openId, protegeNickname);
        return switch (cultivationService.removeProtection(platform, openId, protegeNickname)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> vo.getMessage();
        };
    }

    public String handleQueryProtection(PlatformType platform, String openId) {
        log.debug("处理护道查询 - Platform: {}, OpenId: {}", platform, openId);
        return switch (cultivationService.queryProtectionInfo(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> vo.isSuccess() ? formatProtectionQueryResult(vo) : vo.getMessage();
        };
    }

    // ===================== 文本格式化方法 =====================

    private String formatStaminaInfo(StaminaInfoVO info) {
        StringBuilder sb = new StringBuilder();
        sb.append("💪 【体力状态】\n");
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("当前体力：").append(info.currentStamina()).append("/").append(info.maxStamina())
                .append(" (").append(info.percent()).append("%)\n");
        if (info.offlineRecovered() > 0) {
            sb.append("离线恢复：+").append(info.offlineRecovered()).append(" 体力\n");
        }
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("💡 体力影响探索和旅行，可通过打坐或使用丹药恢复");
        return sb.toString();
    }

    private String formatCharacterStatus(CharacterStatusResult status) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("【%s】的修仙状态\n", status.getNickname()));
        if (status.getStatus() != null && status.getStatusName() != null) {
            sb.append(String.format("状态：%s\n", status.getStatusName()));
        }
        sb.append(String.format("境界：第%d层 (%.1f%%)\n", status.getLevel(), status.getExpPercentage()));
        sb.append(String.format("HP：%d/%d (%.1f%%)\n", status.getHpCurrent(), status.getHpMax(), status.getHpPercentage()));
        sb.append("\n");
        sb.append("【基础属性】\n");
        sb.append(String.format("  力量：%d\n", status.getBaseStr()));
        sb.append(String.format("  体质：%d\n", status.getBaseCon()));
        sb.append(String.format("  敏捷：%d\n", status.getBaseAgi()));
        sb.append(String.format("  智慧：%d\n", status.getBaseWis()));
        if (status.getEquipStr() != 0 || status.getEquipCon() != 0 || status.getEquipAgi() != 0 || status.getEquipWis() != 0) {
            sb.append("\n【装备加成】\n");
            if (status.getEquipStr() != 0) sb.append(String.format("  力量：+%d\n", status.getEquipStr()));
            if (status.getEquipCon() != 0) sb.append(String.format("  体质：+%d\n", status.getEquipCon()));
            if (status.getEquipAgi() != 0) sb.append(String.format("  敏捷：+%d\n", status.getEquipAgi()));
            if (status.getEquipWis() != 0) sb.append(String.format("  智慧：+%d\n", status.getEquipWis()));
        }
        sb.append("\n【战斗属性】\n");
        sb.append(String.format("  攻击：%d\n", status.getAttack()));
        sb.append(String.format("  防御：%d\n", status.getDefense()));
        if (status.getFreeStatPoints() > 0) {
            sb.append(String.format("\n可用属性点：%d\n", status.getFreeStatPoints()));
        }
        if (status.getBreakthroughSuccessRate() != null) {
            sb.append("\n【突破信息】\n");
            sb.append(String.format("  突破成功率：%.1f%%\n", status.getBreakthroughSuccessRate()));
            if (status.getBreakthroughFailCount() != null && status.getBreakthroughFailCount() > 0) {
                sb.append(String.format("  失败次数：%d（已累积补偿）\n", status.getBreakthroughFailCount()));
            }
        }
        if (status.getProtectorCount() != null || (status.getProtectedByList() != null && !status.getProtectedByList().isEmpty())) {
            sb.append("\n【护道信息】\n");
            if (status.getProtectingList() != null && !status.getProtectingList().isEmpty()) {
                sb.append(String.format("  你正在为 %d/%d 位道友护道\n", status.getProtectorCount(), status.getMaxProtectorCount()));
                for (var info : status.getProtectingList()) {
                    String locationStatus = Boolean.TRUE.equals(info.getIsInSameLocation()) ? "[同地点]" : "[异地]";
                    sb.append(String.format("    %s（第%d层）- %s %s - 加成%.1f%%\n",
                            info.getUserName(), info.getUserLevel(), info.getLocationName(), locationStatus, info.getBonusPercentage()));
                }
            } else {
                sb.append(String.format("  你正在为 %d/%d 位道友护道\n",
                        status.getProtectorCount() != null ? status.getProtectorCount() : 0, status.getMaxProtectorCount() != null ? status.getMaxProtectorCount() : 3));
            }
            if (status.getProtectedByList() != null && !status.getProtectedByList().isEmpty()) {
                sb.append(String.format("  有 %d 位道友为你护道，总加成：%.1f%%\n",
                        status.getProtectedByList().size(), status.getTotalProtectionBonus() != null ? status.getTotalProtectionBonus() : 0.0));
                for (var info : status.getProtectedByList()) {
                    String locationStatus = Boolean.TRUE.equals(info.getIsInSameLocation()) ? "[同地点]" : "[异地]";
                    String bonusText = Boolean.TRUE.equals(info.getIsInSameLocation()) ? String.format("加成%.1f%%", info.getBonusPercentage()) : "无法提供加成";
                    sb.append(String.format("    %s（第%d层）- %s %s - %s\n",
                            info.getUserName(), info.getUserLevel(), info.getLocationName(), locationStatus, bonusText));
                }
            } else {
                sb.append("  无道友为你护道\n");
            }
        }
        sb.append(String.format("\n灵石：%d | 铜币：%d\n", status.getSpiritStones(), status.getCoins()));
        if (status.getEquipment() != null && !status.getEquipment().getItems().isEmpty()) {
            sb.append("\n【已穿戴装备】\n");
            status.getEquipment().getItems().forEach(item ->
                    sb.append(String.format("  %s：%s [%s]\n", item.getSlotName(), item.getName(), item.getRarityName())));
        }
        return sb.toString();
    }

    private String formatInventorySummary(InventorySummaryVO inventory) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("【背包】%d/%d\n", inventory.getUsedSlots(), inventory.getCapacity()));
        if (inventory.getEquipmentByQuality() != null && !inventory.getEquipmentByQuality().isEmpty()) {
            sb.append("\n【装备】\n");
            inventory.getEquipmentByQuality().forEach((quality, count) -> sb.append(String.format("  %s x%d\n", quality, count)));
        }
        if (inventory.getStackableItemCount() != null && !inventory.getStackableItemCount().isEmpty()) {
            sb.append("\n【物品】\n");
            inventory.getStackableItemCount().forEach((type, count) -> sb.append(String.format("  %s x%d种\n", type.getName(), count)));
        }
        sb.append(String.format("\n灵石：%d | 铜币：%d\n", inventory.getSpiritStones(), inventory.getCoins()));
        return sb.toString();
    }

    private String formatEquipResult(top.stillmisty.xiantao.domain.item.vo.EquipResult result) {
        return formatAttributeChangeResult(result.getMessage(), result.getAttributeChange());
    }

    private String formatUnequipResult(top.stillmisty.xiantao.domain.item.vo.UnequipResult result) {
        return formatAttributeChangeResult(result.getMessage(), result.getAttributeChange());
    }

    private String formatAttributeChangeResult(String message, AttributeChange change) {
        StringBuilder sb = new StringBuilder();
        sb.append(message).append("\n");
        if (change != null) {
            sb.append("\n【属性变化】\n");
            if (change.getStrChange() != 0) sb.append(formatAttrChange("力量", change.getStrChange())).append("\n");
            if (change.getConChange() != 0) sb.append(formatAttrChange("体质", change.getConChange())).append("\n");
            if (change.getAgiChange() != 0) sb.append(formatAttrChange("敏捷", change.getAgiChange())).append("\n");
            if (change.getWisChange() != 0) sb.append(formatAttrChange("智慧", change.getWisChange())).append("\n");
            if (change.getAttackChange() != 0) sb.append(formatAttrChange("攻击", change.getAttackChange())).append("\n");
            if (change.getDefenseChange() != 0) sb.append(formatAttrChange("防御", change.getDefenseChange())).append("\n");
            if (change.getMaxHpChange() != 0) sb.append(formatAttrChange("HP上限", change.getMaxHpChange()));
        }
        return sb.toString();
    }

    private String formatAttrChange(String attrName, int change) {
        String sign = change > 0 ? "+" : "";
        return String.format("  %s：%s%d", attrName, sign, change);
    }

    private AttributeType parseAttributeType(String attributeName) {
        if (attributeName == null || attributeName.isEmpty()) return null;
        return switch (attributeName.trim()) {
            case "力量", "str", "STR" -> AttributeType.STR;
            case "体质", "con", "CON" -> AttributeType.CON;
            case "敏捷", "agi", "AGI" -> AttributeType.AGI;
            case "智慧", "wis", "WIS" -> AttributeType.WIS;
            default -> null;
        };
    }

    private String formatAttributeAllocationResult(AttributeAllocationResult result) {
        return result.getMessage() + "\n" +
                String.format("当前%s：%d\n", result.getAttributeName(), result.getCurrentAttributeValue()) +
                String.format("剩余可用属性点：%d", result.getRemainingPoints());
    }

    private String formatStatResetResult(StatResetResult result) {
        return result.getMessage() + "\n\n" +
                "【重置详情】\n" +
                String.format("  力量：-%d\n", result.getResetStr()) +
                String.format("  体质：-%d\n", result.getResetCon()) +
                String.format("  敏捷：-%d\n", result.getResetAgi()) +
                String.format("  智慧：-%d\n", result.getResetWis()) +
                String.format("\n返还总属性点：%d\n", result.getTotalFreePoints()) +
                String.format("下次可洗点时间：%d小时后", result.getCooldownHoursRemaining());
    }

    private String formatBreakthroughResult(BreakthroughResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(result.getMessage()).append("\n\n");
        if (result.getSuccessRate() != null) sb.append(String.format("突破成功率：%.1f%%\n", result.getSuccessRate()));
        if (result.getNewLevel() != null) sb.append(String.format("当前境界：第%d层\n", result.getNewLevel()));
        if (result.getFreeStatPoints() != null) sb.append(String.format("可用属性点：%d\n", result.getFreeStatPoints()));
        if (result.getNextBreakthroughRate() != null) sb.append(String.format("下次突破成功率：%.1f%%", result.getNextBreakthroughRate()));
        return sb.toString();
    }

    private String formatProtectionResult(DaoProtectionResult result) {
        return result.getMessage() + "\n\n" +
                "【护道详情】\n" +
                String.format("  护道者：%s（第%d层）\n", result.getProtectorName(), result.getProtectorLevel()) +
                String.format("  被护道者：%s（第%d层）\n", result.getProtegeName(), result.getProtegeLevel()) +
                String.format("  单人加成：%.1f%%\n", result.getSingleProtectorBonus()) +
                String.format("  是否同地点：%s", result.getIsInSameLocation() ? "是" : "否");
    }

    private String formatProtectionQueryResult(DaoProtectionQueryResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(result.getMessage()).append("\n");
        if (result.getProtectingList() != null && !result.getProtectingList().isEmpty()) {
            sb.append(String.format("\n【你正在为以下道友护道】(%d/%d)\n", result.getProtectingCount(), result.getMaxProtectingCount()));
            for (var info : result.getProtectingList()) {
                String locationStatus = info.getIsInSameLocation() ? "[同地点]" : "[异地]";
                sb.append(String.format("  %s（第%d层）- %s %s - 加成%.1f%%\n",
                        info.getUserName(), info.getUserLevel(), info.getLocationName(), locationStatus, info.getBonusPercentage()));
            }
        } else {
            sb.append("\n【你正在为以下道友护道】无\n");
        }
        if (result.getProtectedByList() != null && !result.getProtectedByList().isEmpty()) {
            sb.append(String.format("\n【以下道友正在为你护道】总加成：%.1f%%\n", result.getTotalBonusPercentage()));
            for (var info : result.getProtectedByList()) {
                String locationStatus = info.getIsInSameLocation() ? "[同地点]" : "[异地]";
                String bonusText = info.getIsInSameLocation() ? String.format("加成%.1f%%", info.getBonusPercentage()) : "无法提供加成";
                sb.append(String.format("  %s（第%d层）- %s %s - %s\n",
                        info.getUserName(), info.getUserLevel(), info.getLocationName(), locationStatus, bonusText));
            }
        } else {
            sb.append("\n【以下道友正在为你护道】无\n");
        }
        return sb.toString();
    }
}
