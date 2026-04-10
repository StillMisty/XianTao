package top.stillmisty.xiantao.handle.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.vo.AttributeChange;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.ItemService;
import top.stillmisty.xiantao.service.UserAuthService;
import top.stillmisty.xiantao.service.UserService;

/**
 * 修仙命令处理器
 * 集中处理跨平台的修仙相关命令逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CultivationCommandHandler {
    
    private final UserAuthService userAuthService;
    private final UserService userService;
    private final ItemService itemService;
    
    /**
     * 处理注册命令（我要修仙）
     * 
     * @param platform 平台类型
     * @param openId 平台用户ID
     * @param nickname 道号
     * @return 注册结果消息
     */
    public String handleRegister(PlatformType platform, String openId, String nickname) {
        log.info("处理注册请求 - Platform: {}, OpenId: {}, Nickname: {}", platform, openId, nickname);
        
        // 验证道号
        if (nickname == null || nickname.isBlank()) {
            return "道号不能为空哦~";
        }
        
        nickname = nickname.strip();
        
        if (nickname.length() < 4 || nickname.length() > 8) {
            return "道号长度需在4到8个字符之间~";
        }
        
        // 检查是否已注册
        var existingAuth = userAuthService.findUserIdByOpenId(platform, openId);
        if (existingAuth.isPresent()) {
            return "您已入仙途了哦~";
        }
        
        // 创建用户
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
        
        var userAuth = userAuthService.findUserIdByOpenId(platform, openId);
        
        if (userAuth.isEmpty()) {
            return "输入「我要修仙 [道号]」进入仙途吧！";
        }
        
        var characterStatus = itemService.getCharacterStatus(userAuth.get().getUserId());
        
        if (!characterStatus.isSuccess()) {
            return characterStatus.getMessage();
        }
        
        // 格式化状态显示
        return formatCharacterStatus(characterStatus);
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
        
        var userAuth = userAuthService.findUserIdByOpenId(platform, openId);
        
        if (userAuth.isEmpty()) {
            return "输入「我要修仙 [道号]」进入仙途吧！";
        }
        
        var inventory = itemService.getInventorySummary(userAuth.get().getUserId());
        
        if (inventory == null) {
            return "查询背包失败，请稍后重试";
        }
        
        return formatInventorySummary(inventory);
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
        
        var userAuth = userAuthService.findUserIdByOpenId(platform, openId);
        
        if (userAuth.isEmpty()) {
            return "输入「我要修仙 [道号]」进入仙途吧！";
        }
        
        var result = itemService.equipItem(userAuth.get().getUserId(), itemName);
        
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
        
        var userAuth = userAuthService.findUserIdByOpenId(platform, openId);
        
        if (userAuth.isEmpty()) {
            return "输入「我要修仙 [道号]」进入仙途吧！";
        }
        
        var result = itemService.unequipItem(userAuth.get().getUserId(), slotName);
        
        if (!result.isSuccess()) {
            return result.getMessage();
        }
        
        return formatUnequipResult(result);
    }
    
    // ===================== 响应格式化方法 =====================
    
    /**
     * 格式化角色状态显示
     */
    private String formatCharacterStatus(top.stillmisty.xiantao.domain.item.vo.CharacterStatusResult status) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(String.format("【%s】的修仙状态\n", status.getNickname()));
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
        
        sb.append(String.format("\n灵石：%d | 铜币：%d\n", status.getSpiritStones(), status.getCoins()));
        
        // 已穿戴装备
        if (status.getEquipment() != null && !status.getEquipment().getItems().isEmpty()) {
            sb.append("\n【已穿戴装备】\n");
            status.getEquipment().getItems().forEach(item -> {
                sb.append(String.format("  %s：%s [%s]\n", 
                    item.getSlotName(), item.getName(), item.getRarityName()));
            });
        }
        
        return sb.toString();
    }
    
    /**
     * 格式化背包摘要显示
     */
    private String formatInventorySummary(top.stillmisty.xiantao.domain.item.vo.InventorySummaryVO inventory) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(String.format("【背包】%d/%d\n", inventory.getUsedSlots(), inventory.getCapacity()));
        
        // 装备按品质统计
        if (!inventory.getEquipmentByQuality().isEmpty()) {
            sb.append("\n【装备】\n");
            inventory.getEquipmentByQuality().forEach((quality, count) -> {
                sb.append(String.format("  %s x%d\n", quality, count));
            });
        }
        
        // 堆叠物品统计
        if (!inventory.getStackableItemCount().isEmpty()) {
            sb.append("\n【物品】\n");
            inventory.getStackableItemCount().forEach((type, count) -> {
                sb.append(String.format("  %s x%d种\n", type.getName(), count));
            });
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
}
