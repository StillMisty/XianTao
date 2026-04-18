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
 * 修仙命令处理器
 * 集中处理跨平台的修仙相关命令逻辑
 */
@Slf4j
@Component
public class CultivationCommandHandler extends BaseCommandHandler {

    private final UserService userService;
    private final ItemService itemService;
    private final CultivationService cultivationService;
    private final StaminaService staminaService;

    public CultivationCommandHandler(UserAuthService userAuthService, UserService userService, 
                                    ItemService itemService, CultivationService cultivationService,
                                    StaminaService staminaService) {
        super(userAuthService,userService);
        this.userService = userService;
        this.itemService = itemService;
        this.cultivationService = cultivationService;
        this.staminaService = staminaService;
    }

    /**
     * 处理注册命令（我要修仙）
     *
     * @param platform 平台类型
     * @param openId 平台用户ID
     * @param nickname 道号
     * @return 注册结果消息
     */
    public String handleRegister(PlatformType platform, String openId, String nickname) {
        log.debug("处理注册请求 - Platform: {}, OpenId: {}, Nickname: {}", platform, openId, nickname);

        // 验证用户身份
        var authResult = authenticate(platform, openId);
        if (authResult.authenticated()) {
            return "您已经踏上仙途了哦~";
        }

        var result = userService.createUser(platform, openId, nickname);

        if (!result.success()) {
            log.error("用户创建失败 - OpenId: {}, Nickname: {}", openId, nickname);
            return "系统错误：用户创建失败，请联系管理员";
        }

        log.info("用户注册成功 - UserId: {}, Nickname: {}", result.userId(), result.nickname());

        return String.format(
                "欢迎踏入仙途！您的道号为：%s\n输入「状态」查看您的角色信息",
                result.nickname()
        );
    }

    /**
     * 处理状态查询命令
     *
     * @param platform 平台类型
     * @param openId 平台用户ID
     * @return 状态信息或提示消息
     */
    public String handleStatus(PlatformType platform, String openId) {
        log.debug("处理状态查询 - Platform: {}, OpenId: {}", platform, openId);

        var authResult = authenticate(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        var characterStatus = itemService.getCharacterStatus(authResult.userId());

        if (!characterStatus.isSuccess()) {
            return "查询状态失败";
        }

        // 格式化状态显示
        StringBuilder sb = new StringBuilder();
        sb.append(formatCharacterStatus(characterStatus));
        sb.append("\n\n");
        
        // 添加体力信息
        sb.append(staminaService.getStaminaInfo(authResult.userId()));
        
        return sb.toString();
    }

    /**
     * 处理体力查询命令
     *
     * @param platform 平台类型
     * @param openId 平台用户ID
     * @return 体力信息
     */
    public String handleStaminaQuery(PlatformType platform, String openId) {
        log.debug("处理体力查询 - Platform: {}, OpenId: {}", platform, openId);

        var authResult = authenticate(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        return staminaService.getStaminaInfo(authResult.userId());
    }

    /**
     * 处理背包查询命令
     *
     * @param platform 平台类型
     * @param openId 平台用户ID
     * @return 背包信息或提示消息
     */
    public String handleInventory(PlatformType platform, String openId) {
        log.debug("处理背包查询 - Platform: {}, OpenId: {}", platform, openId);

        var authResult = authenticate(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        var inventorySummary = itemService.getInventorySummary(authResult.userId());

        if (inventorySummary == null) {
            return "查询背包失败：用户不存在";
        }

        return formatInventorySummary(inventorySummary);
    }

    /**
     * 处理装备穿戴命令
     *
     * @param platform 平台类型
     * @param openId 平台用户ID
     * @param itemName 物品名称
     * @return 装备结果消息
     */
    public String handleEquip(PlatformType platform, String openId, String itemName) {
        log.debug("处理装备穿戴 - Platform: {}, OpenId: {}, ItemName: {}", platform, openId, itemName);

        var authResult = authenticate(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        var result = itemService.equipItem(authResult.userId(), itemName);

        if (!result.isSuccess()) {
            return result.getMessage();
        }

        return formatEquipResult(result);
    }

    /**
     * 处理装备卸下命令
     *
     * @param platform 平台类型
     * @param openId 平台用户ID
     * @param slotName 部位名称
     * @return 卸下结果消息
     */
    public String handleUnequip(PlatformType platform, String openId, String slotName) {
        log.debug("处理装备卸下 - Platform: {}, OpenId: {}, SlotName: {}", platform, openId, slotName);

        var authResult = authenticate(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        var result = itemService.unequipItem(authResult.userId(), slotName);

        if (!result.isSuccess()) {
            return result.getMessage();
        }

        return formatUnequipResult(result);
    }

    /**
     * 处理属性加点命令
     *
     * @param platform 平台类型
     * @param openId 平台用户ID
     * @param attributeName 属性名称（力量/体质/敏捷/智慧）
     * @param points 点数
     * @return 加点结果消息
     */
    public String handleAllocatePoints(PlatformType platform, String openId, String attributeName, int points) {
        log.debug("处理属性加点 - Platform: {}, OpenId: {}, Attribute: {}, Points: {}", platform, openId, attributeName, points);

        var authResult = authenticate(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        // 解析属性类型
        AttributeType attributeType = parseAttributeType(attributeName);
        if (attributeType == null) {
            return "无效的属性名称，请使用：力量、体质、敏捷、智慧";
        }

        var result = cultivationService.allocateAttributePoints(authResult.userId(), attributeType, points);

        if (!result.isSuccess()) {
            return result.getMessage();
        }

        return formatAttributeAllocationResult(result);
    }

    /**
     * 处理洗点命令
     *
     * @param platform 平台类型
     * @param openId 平台用户ID
     * @return 洗点结果消息
     */
    public String handleResetPoints(PlatformType platform, String openId) {
        log.debug("处理洗点 - Platform: {}, OpenId: {}", platform, openId);

        var authResult = authenticate(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        var result = cultivationService.resetStatPoints(authResult.userId());

        if (!result.isSuccess()) {
            return result.getMessage();
        }

        return formatStatResetResult(result);
    }

    /**
     * 处理突破命令
     *
     * @param platform 平台类型
     * @param openId 平台用户ID
     * @return 突破结果消息
     */
    public String handleBreakthrough(PlatformType platform, String openId) {
        log.debug("处理突破 - Platform: {}, OpenId: {}", platform, openId);

        var authResult = authenticate(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        var result = cultivationService.attemptBreakthrough(authResult.userId());

        return formatBreakthroughResult(result);
    }

    /**
     * 处理护道命令
     *
     * @param platform 平台类型
     * @param openId 平台用户ID
     * @param protegeNickname 被护道者的道号
     * @return 护道结果消息
     */
    public String handleEstablishProtection(PlatformType platform, String openId, String protegeNickname) {
        log.debug("处理护道 - Platform: {}, OpenId: {}, Protege: {}", platform, openId, protegeNickname);

        var authResult = authenticate(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        var result = cultivationService.establishProtection(authResult.userId(), protegeNickname);

        if (!result.isSuccess()) {
            return result.getMessage();
        }

        return formatProtectionResult(result);
    }

    /**
     * 处理护道解除命令
     *
     * @param platform 平台类型
     * @param openId 平台用户ID
     * @param protegeNickname 被护道者的道号
     * @return 解除结果消息
     */
    public String handleRemoveProtection(PlatformType platform, String openId, String protegeNickname) {
        log.debug("处理护道解除 - Platform: {}, OpenId: {}, Protege: {}", platform, openId, protegeNickname);

        var authResult = authenticate(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        var result = cultivationService.removeProtection(authResult.userId(), protegeNickname);

        if (!result.isSuccess()) {
            return result.getMessage();
        }

        return result.getMessage();
    }

    /**
     * 处理护道查询命令
     *
     * @param platform 平台类型
     * @param openId 平台用户ID
     * @return 护道查询结果消息
     */
    public String handleQueryProtection(PlatformType platform, String openId) {
        log.debug("处理护道查询 - Platform: {}, OpenId: {}", platform, openId);

        var authResult = authenticate(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        var result = cultivationService.queryProtectionInfo(authResult.userId());

        if (!result.isSuccess()) {
            return result.getMessage();
        }

        return formatProtectionQueryResult(result);
    }

    // ===================== 响应格式化方法 =====================

    /**
     * 格式化角色状态显示
     */
    private String formatCharacterStatus(CharacterStatusResult status) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("【%s】的修仙状态\n", status.getNickname()));

        // 自身状态
        if (status.getStatus() != null && status.getStatusName() != null) {
            sb.append(String.format("状态：%s\n", status.getStatusName()));
        }

        sb.append(String.format("境界：第%d层 (%.1f%%)\n", status.getLevel(), status.getExpPercentage()));
        sb.append(String.format("HP：%d/%d (%.1f%%)\n", status.getHpCurrent(), status.getHpMax(), status.getHpPercentage()));
        sb.append("\n");

        // 属性
        sb.append("【基础属性】\n");
        sb.append(String.format("  力量：%d\n", status.getBaseStr()));
        sb.append(String.format("  体质：%d\n", status.getBaseCon()));
        sb.append(String.format("  敏捷：%d\n", status.getBaseAgi()));
        sb.append(String.format("  智慧：%d\n", status.getBaseWis()));

        if (status.getEquipStr() != 0 || status.getEquipCon() != 0 ||
                status.getEquipAgi() != 0 || status.getEquipWis() != 0) {
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

        // 突破相关信息
        if (status.getBreakthroughSuccessRate() != null) {
            sb.append(String.format("\n【突破信息】\n"));
            sb.append(String.format("  突破成功率：%.1f%%\n", status.getBreakthroughSuccessRate()));
            if (status.getBreakthroughFailCount() != null && status.getBreakthroughFailCount() > 0) {
                sb.append(String.format("  失败次数：%d（已累积补偿）\n", status.getBreakthroughFailCount()));
            }
        }

        // 护道相关信息
        if (status.getProtectorCount() != null || (status.getProtectedByList() != null && !status.getProtectedByList().isEmpty())) {
            sb.append("\n【护道信息】\n");
            
            // 正在为谁护道
            if (status.getProtectingList() != null && !status.getProtectingList().isEmpty()) {
                sb.append(String.format("  你正在为 %d/%d 位道友护道\n", 
                        status.getProtectorCount(), status.getMaxProtectorCount()));
                for (var info : status.getProtectingList()) {
                    String locationStatus = Boolean.TRUE.equals(info.getIsInSameLocation()) ? "[同地点]" : "[异地]";
                    sb.append(String.format("    %s（第%d层）- %s %s - 加成%.1f%%\n",
                            info.getUserName(), info.getUserLevel(),
                            info.getLocationName(), locationStatus, info.getBonusPercentage()));
                }
            } else {
                sb.append(String.format("  你正在为 %d/%d 位道友护道\n", 
                        status.getProtectorCount() != null ? status.getProtectorCount() : 0,
                        status.getMaxProtectorCount() != null ? status.getMaxProtectorCount() : 3));
            }
            
            // 有谁在为自己护道
            if (status.getProtectedByList() != null && !status.getProtectedByList().isEmpty()) {
                sb.append(String.format("  有 %d 位道友为你护道，总加成：%.1f%%\n",
                        status.getProtectedByList().size(),
                        status.getTotalProtectionBonus() != null ? status.getTotalProtectionBonus() : 0.0));
                for (var info : status.getProtectedByList()) {
                    String locationStatus = Boolean.TRUE.equals(info.getIsInSameLocation()) ? "[同地点]" : "[异地]";
                    String bonusText = Boolean.TRUE.equals(info.getIsInSameLocation()) ?
                            String.format("加成%.1f%%", info.getBonusPercentage()) : "无法提供加成";
                    sb.append(String.format("    %s（第%d层）- %s %s - %s\n",
                            info.getUserName(), info.getUserLevel(),
                            info.getLocationName(), locationStatus, bonusText));
                }
            } else {
                sb.append("  无道友为你护道\n");
            }
        }

        sb.append(String.format("\n灵石：%d | 铜币：%d\n", status.getSpiritStones(), status.getCoins()));

        // 已穿戴装备
        if (status.getEquipment() != null && !status.getEquipment().getItems().isEmpty()) {
            sb.append("\n【已穿戴装备】\n");
            status.getEquipment().getItems().forEach(item ->
                    sb.append(String.format("  %s：%s [%s]\n", item.getSlotName(), item.getName(), item.getRarityName()))
            );
        }

        return sb.toString();
    }

    /**
     * 格式化背包摘要显示
     */
    private String formatInventorySummary(InventorySummaryVO inventory) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("【背包】%d/%d\n", inventory.getUsedSlots(), inventory.getCapacity()));

        // 装备按品质统计
        if (inventory.getEquipmentByQuality() != null && !inventory.getEquipmentByQuality().isEmpty()) {
            sb.append("\n【装备】\n");
            inventory.getEquipmentByQuality().forEach((quality, count) ->
                    sb.append(String.format("  %s x%d\n", quality, count))
            );
        }

        // 堆叠物品统计
        if (inventory.getStackableItemCount() != null && !inventory.getStackableItemCount().isEmpty()) {
            sb.append("\n【物品】\n");
            inventory.getStackableItemCount().forEach((type, count) ->
                    sb.append(String.format("  %s x%d种\n", type.getName(), count))
            );
        }

        sb.append(String.format("\n灵石：%d | 铜币：%d\n", inventory.getSpiritStones(), inventory.getCoins()));

        return sb.toString();
    }

    /**
     * 格式化装备穿戴结果
     */
    private String formatEquipResult(top.stillmisty.xiantao.domain.item.vo.EquipResult result) {
        return formatAttributeChangeResult(result.getMessage(), result.getAttributeChange());
    }

    /**
     * 格式化装备卸下结果
     */
    private String formatUnequipResult(top.stillmisty.xiantao.domain.item.vo.UnequipResult result) {
        return formatAttributeChangeResult(result.getMessage(), result.getAttributeChange());
    }

    /**
     * 通用属性变化结果格式化
     */
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

    /**
     * 格式化属性变化
     */
    private String formatAttrChange(String attrName, int change) {
        String sign = change > 0 ? "+" : "";
        return String.format("  %s：%s%d", attrName, sign, change);
    }

    // ===================== 新增格式化方法 =====================

    /**
     * 解析属性类型
     */
    private AttributeType parseAttributeType(String attributeName) {
        if (attributeName == null || attributeName.isEmpty()) {
            return null;
        }
        
        return switch (attributeName.trim()) {
            case "力量", "str", "STR" -> AttributeType.STR;
            case "体质", "con", "CON" -> AttributeType.CON;
            case "敏捷", "agi", "AGI" -> AttributeType.AGI;
            case "智慧", "wis", "WIS" -> AttributeType.WIS;
            default -> null;
        };
    }

    /**
     * 格式化属性分配结果
     */
    private String formatAttributeAllocationResult(AttributeAllocationResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(result.getMessage()).append("\n");
        sb.append(String.format("当前%s：%d\n", result.getAttributeName(), result.getCurrentAttributeValue()));
        sb.append(String.format("剩余可用属性点：%d", result.getRemainingPoints()));
        return sb.toString();
    }

    /**
     * 格式化洗点结果
     */
    private String formatStatResetResult(StatResetResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(result.getMessage()).append("\n\n");
        sb.append("【重置详情】\n");
        sb.append(String.format("  力量：-%d\n", result.getResetStr()));
        sb.append(String.format("  体质：-%d\n", result.getResetCon()));
        sb.append(String.format("  敏捷：-%d\n", result.getResetAgi()));
        sb.append(String.format("  智慧：-%d\n", result.getResetWis()));
        sb.append(String.format("\n返还总属性点：%d\n", result.getTotalFreePoints()));
        sb.append(String.format("下次可洗点时间：%d小时后", result.getCooldownHoursRemaining()));
        return sb.toString();
    }

    /**
     * 格式化突破结果
     */
    private String formatBreakthroughResult(BreakthroughResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(result.getMessage()).append("\n\n");
        
        if (result.getSuccessRate() != null) {
            sb.append(String.format("突破成功率：%.1f%%\n", result.getSuccessRate()));
        }
        
        if (result.getNewLevel() != null) {
            sb.append(String.format("当前境界：第%d层\n", result.getNewLevel()));
        }
        
        if (result.getFreeStatPoints() != null) {
            sb.append(String.format("可用属性点：%d\n", result.getFreeStatPoints()));
        }
        
        if (result.getNextBreakthroughRate() != null) {
            sb.append(String.format("下次突破成功率：%.1f%%", result.getNextBreakthroughRate()));
        }
        
        return sb.toString();
    }

    /**
     * 格式化护道结果
     */
    private String formatProtectionResult(DaoProtectionResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(result.getMessage()).append("\n\n");
        sb.append("【护道详情】\n");
        sb.append(String.format("  护道者：%s（第%d层）\n", result.getProtectorName(), result.getProtectorLevel()));
        sb.append(String.format("  被护道者：%s（第%d层）\n", result.getProtegeName(), result.getProtegeLevel()));
        sb.append(String.format("  单人加成：%.1f%%\n", result.getSingleProtectorBonus()));
        sb.append(String.format("  是否同地点：%s", result.getIsInSameLocation() ? "是" : "否"));
        return sb.toString();
    }

    /**
     * 格式化护道查询结果
     */
    private String formatProtectionQueryResult(DaoProtectionQueryResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(result.getMessage()).append("\n");

        // 显示正在为谁护道
        if (result.getProtectingList() != null && !result.getProtectingList().isEmpty()) {
            sb.append(String.format("\n【你正在为以下道友护道】(%d/%d)\n", 
                    result.getProtectingCount(), result.getMaxProtectingCount()));
            for (var info : result.getProtectingList()) {
                String locationStatus = info.getIsInSameLocation() ? "[同地点]" : "[异地]";
                sb.append(String.format("  %s（第%d层）- %s %s - 加成%.1f%%\n",
                        info.getUserName(), info.getUserLevel(),
                        info.getLocationName(), locationStatus, info.getBonusPercentage()));
            }
        } else {
            sb.append("\n【你正在为以下道友护道】无\n");
        }

        // 显示有谁在为自己护道
        if (result.getProtectedByList() != null && !result.getProtectedByList().isEmpty()) {
            sb.append(String.format("\n【以下道友正在为你护道】总加成：%.1f%%\n", 
                    result.getTotalBonusPercentage()));
            for (var info : result.getProtectedByList()) {
                String locationStatus = info.getIsInSameLocation() ? "[同地点]" : "[异地]";
                String bonusText = info.getIsInSameLocation() ? 
                        String.format("加成%.1f%%", info.getBonusPercentage()) : "无法提供加成";
                sb.append(String.format("  %s（第%d层）- %s %s - %s\n",
                        info.getUserName(), info.getUserLevel(),
                        info.getLocationName(), locationStatus, bonusText));
            }
        } else {
            sb.append("\n【以下道友正在为你护道】无\n");
        }

        return sb.toString();
    }
}
