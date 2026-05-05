package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.item.vo.AttributeChange;
import top.stillmisty.xiantao.domain.item.vo.CharacterStatusResult;
import top.stillmisty.xiantao.domain.item.vo.InventorySummaryVO;
import top.stillmisty.xiantao.domain.item.vo.ItemEntry;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.vo.*;
import top.stillmisty.xiantao.domain.user.vo.PlayerViewVO;
import top.stillmisty.xiantao.infrastructure.util.FormatUtils;
import top.stillmisty.xiantao.service.CharacterStatusService;
import top.stillmisty.xiantao.service.CultivationService;
import top.stillmisty.xiantao.service.DiscardService;
import top.stillmisty.xiantao.service.EquipmentService;
import top.stillmisty.xiantao.service.InventoryService;
import top.stillmisty.xiantao.service.PlayerViewService;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserService;

/** 修仙命令处理器（纯 View 层） 调用 Service 层获取结构化数据，格式化为纯文本返回 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CultivationCommandHandler implements CommandGroup {

  private final UserService userService;
  private final CharacterStatusService characterStatusService;
  private final InventoryService inventoryService;
  private final EquipmentService equipmentService;
  private final CultivationService cultivationService;
  private final DiscardService discardService;
  private final PlayerViewService playerViewService;

  /** 注册（UserService.createUser 内部做重复注册检查） */
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
    var status = characterStatusService.getCharacterStatus(platform, openId);
    return switch (status) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var vo) -> formatCharacterStatus(vo);
    };
  }

  public String handleInventory(PlatformType platform, String openId) {
    log.debug("处理背包查询 - Platform: {}, OpenId: {}", platform, openId);
    return switch (inventoryService.getInventorySummary(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var vo) -> formatInventorySummary(vo);
    };
  }

  public String handleSeedInventory(PlatformType platform, String openId) {
    return switch (inventoryService.getSeedInventory(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var entries) -> formatItemList("种子", entries);
    };
  }

  public String handleEquipmentInventory(PlatformType platform, String openId) {
    return switch (inventoryService.getEquipmentInventory(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var entries) -> formatItemList("装备", entries);
    };
  }

  public String handleEggInventory(PlatformType platform, String openId) {
    return switch (inventoryService.getEggInventory(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var entries) -> formatItemList("兽卵", entries);
    };
  }

  public String handleInventoryByCategory(PlatformType platform, String openId, String category) {
    return switch (category) {
      case "种子" -> handleSeedInventory(platform, openId);
      case "装备" -> handleEquipmentInventory(platform, openId);
      case "兽卵" -> handleEggInventory(platform, openId);
      default -> "未知分类，可选：种子、装备、兽卵";
    };
  }

  public String handleEquip(PlatformType platform, String openId, String itemName) {
    log.debug("处理装备穿戴 - Platform: {}, OpenId: {}, ItemName: {}", platform, openId, itemName);
    return switch (equipmentService.equipItem(platform, openId, itemName)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var vo) ->
          vo.isSuccess() ? formatEquipResult(vo) : vo.getMessage();
    };
  }

  public String handleUnequip(PlatformType platform, String openId, String slotName) {
    log.debug("处理装备卸下 - Platform: {}, OpenId: {}, SlotName: {}", platform, openId, slotName);
    return switch (equipmentService.unequipItem(platform, openId, slotName)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var vo) ->
          vo.isSuccess() ? formatUnequipResult(vo) : vo.getMessage();
    };
  }

  public String handleBreakthrough(PlatformType platform, String openId) {
    log.debug("处理突破 - Platform: {}, OpenId: {}", platform, openId);
    return switch (cultivationService.attemptBreakthrough(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var vo) -> formatBreakthroughResult(vo);
    };
  }

  public String handleEstablishProtection(
      PlatformType platform, String openId, String protegeNickname) {
    log.debug("处理护道 - Platform: {}, OpenId: {}, Protege: {}", platform, openId, protegeNickname);
    return switch (cultivationService.establishProtection(platform, openId, protegeNickname)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var vo) ->
          vo.isSuccess() ? formatProtectionResult(vo) : vo.getMessage();
    };
  }

  public String handleRemoveProtection(
      PlatformType platform, String openId, String protegeNickname) {
    log.debug("处理护道解除 - Platform: {}, OpenId: {}, Protege: {}", platform, openId, protegeNickname);
    return switch (cultivationService.removeProtection(platform, openId, protegeNickname)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var vo) -> vo.getMessage();
    };
  }

  public String handleDiscard(PlatformType platform, String openId, String itemName) {
    log.debug("处理丢弃 - Platform: {}, OpenId: {}, ItemName: {}", platform, openId, itemName);
    return switch (discardService.discardItem(platform, openId, itemName)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var msg) -> msg;
    };
  }

  public String handleViewPlayer(PlatformType platform, String openId, String targetNickname) {
    log.debug("处理查看 - Platform: {}, OpenId: {}, Target: {}", platform, openId, targetNickname);
    return switch (playerViewService.viewPlayer(platform, openId, targetNickname)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var vo) -> formatPlayerView(vo);
    };
  }

  public String handleQueryProtection(PlatformType platform, String openId) {
    log.debug("处理护道查询 - Platform: {}, OpenId: {}", platform, openId);
    return switch (cultivationService.queryProtectionInfo(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> msg;
      case ServiceResult.Success(var vo) ->
          vo.isSuccess() ? formatProtectionQueryResult(vo) : vo.getMessage();
    };
  }

  // ===================== 文本格式化方法 =====================

  private String formatCharacterStatus(CharacterStatusResult status) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("【%s】的修仙状态\n", status.getNickname()));

    // 位置信息
    if (status.getLocationName() != null) {
      sb.append(String.format("所在地：%s\n", status.getLocationName()));
    }

    // 状态：赶路时显示优化信息，其余显示普通状态名
    if (status.getStatus() == UserStatus.RUNNING && status.getTravelDestinationName() != null) {
      sb.append(
          String.format(
              "状态：赶路中 (%s → %s)\n", status.getLocationName(), status.getTravelDestinationName()));
      if (status.getTravelTimeMinutes() != null
          && status.getTravelMinutesElapsed() != null
          && status.getTravelMinutesRemaining() != null) {
        sb.append(
            String.format(
                "旅途进度：%s/%s",
                FormatUtils.formatMinutes(status.getTravelMinutesElapsed()),
                FormatUtils.formatMinutes(status.getTravelTimeMinutes().longValue())));
        if (status.getTravelMinutesRemaining() > 0) {
          sb.append(
              String.format(
                  "（剩余 %s）", FormatUtils.formatMinutes(status.getTravelMinutesRemaining())));
        } else {
          sb.append("（即将到达）");
        }
        sb.append("\n");
      } else if (status.getEstimatedArrivalTime() != null) {
        sb.append(
            String.format(
                "预计到达：%s\n", FormatUtils.formatDateTime(status.getEstimatedArrivalTime())));
      }
    } else if (status.getStatus() != null && status.getStatusName() != null) {
      sb.append(String.format("状态：%s\n", status.getStatusName()));
    }

    sb.append(String.format("境界：第%d层 (%.1f%%)\n", status.getLevel(), status.getExpPercentage()));
    sb.append(
        String.format(
            "HP：%d/%d (%.1f%%)\n",
            status.getHpCurrent(), status.getHpMax(), status.getHpPercentage()));
    sb.append("\n");
    sb.append("【基础属性】\n");
    sb.append(String.format("  力道：%d\n", status.getStatStr()));
    sb.append(String.format("  根骨：%d\n", status.getStatCon()));
    sb.append(String.format("  身法：%d\n", status.getStatAgi()));
    sb.append(String.format("  悟性：%d\n", status.getStatWis()));
    if (status.getEquipStr() != 0
        || status.getEquipCon() != 0
        || status.getEquipAgi() != 0
        || status.getEquipWis() != 0) {
      sb.append("\n【装备加成】\n");
      if (status.getEquipStr() != 0) sb.append(String.format("  力道：+%d\n", status.getEquipStr()));
      if (status.getEquipCon() != 0) sb.append(String.format("  根骨：+%d\n", status.getEquipCon()));
      if (status.getEquipAgi() != 0) sb.append(String.format("  身法：+%d\n", status.getEquipAgi()));
      if (status.getEquipWis() != 0) sb.append(String.format("  悟性：+%d\n", status.getEquipWis()));
    }
    sb.append("\n【战斗属性】\n");
    sb.append(String.format("  攻击：%d\n", status.getAttack()));
    sb.append(String.format("  防御：%d\n", status.getDefense()));
    if (status.getBreakthroughSuccessRate() != null) {
      sb.append("\n【突破信息】\n");
      sb.append(String.format("  突破成功率：%.1f%%\n", status.getBreakthroughSuccessRate()));
      if (status.getBreakthroughFailCount() != null && status.getBreakthroughFailCount() > 0) {
        sb.append(String.format("  失败次数：%d（已累积补偿）\n", status.getBreakthroughFailCount()));
      }
    }
    if (status.getProtectorCount() != null
        || (status.getProtectedByList() != null && !status.getProtectedByList().isEmpty())) {
      sb.append("\n【护道信息】\n");
      if (status.getProtectingList() != null && !status.getProtectingList().isEmpty()) {
        sb.append(
            String.format(
                "  你正在为 %d/%d 位道友护道\n", status.getProtectorCount(), status.getMaxProtectorCount()));
        for (var info : status.getProtectingList()) {
          String locationStatus =
              Boolean.TRUE.equals(info.getIsInSameLocation()) ? "[同地点]" : "[异地]";
          sb.append(
              String.format(
                  "    %s（第%d层）- %s %s - 加成%.1f%%\n",
                  info.getUserName(),
                  info.getUserLevel(),
                  info.getLocationName(),
                  locationStatus,
                  info.getBonusPercentage()));
        }
      } else {
        sb.append(
            String.format(
                "  你正在为 %d/%d 位道友护道\n",
                status.getProtectorCount() != null ? status.getProtectorCount() : 0,
                status.getMaxProtectorCount() != null ? status.getMaxProtectorCount() : 3));
      }
      if (status.getProtectedByList() != null && !status.getProtectedByList().isEmpty()) {
        sb.append(
            String.format(
                "  有 %d 位道友为你护道，总加成：%.1f%%\n",
                status.getProtectedByList().size(),
                status.getTotalProtectionBonus() != null ? status.getTotalProtectionBonus() : 0.0));
        for (var info : status.getProtectedByList()) {
          String locationStatus =
              Boolean.TRUE.equals(info.getIsInSameLocation()) ? "[同地点]" : "[异地]";
          String bonusText =
              Boolean.TRUE.equals(info.getIsInSameLocation())
                  ? String.format("加成%.1f%%", info.getBonusPercentage())
                  : "无法提供加成";
          sb.append(
              String.format(
                  "    %s（第%d层）- %s %s - %s\n",
                  info.getUserName(),
                  info.getUserLevel(),
                  info.getLocationName(),
                  locationStatus,
                  bonusText));
        }
      } else {
        sb.append("  无道友为你护道\n");
      }
    }
    sb.append(String.format("\n灵石：%d\n", status.getSpiritStones()));
    if (status.getEquipment() != null && !status.getEquipment().getItems().isEmpty()) {
      sb.append("\n【已穿戴装备】\n");
      status
          .getEquipment()
          .getItems()
          .forEach(
              item ->
                  sb.append(
                      String.format(
                          "  %s：%s [%s]\n",
                          item.getSlotName(), item.getName(), item.getRarityName())));
    }
    return sb.toString();
  }

  private String formatItemList(String title, List<ItemEntry> entries) {
    if (entries.isEmpty()) {
      return "【" + title + "列表】（空）";
    }
    var sb = new StringBuilder("【" + title + "列表】\n");
    for (var e : entries) {
      sb.append(e.index()).append(". ").append(e.name());
      if (e.quantity() > 1) sb.append(" x").append(e.quantity());
      if (!e.metadata().isBlank()) sb.append(" [").append(e.metadata()).append("]");
      sb.append("\n");
    }
    return sb.toString().strip();
  }

  private String formatInventorySummary(InventorySummaryVO inventory) {
    StringBuilder sb = new StringBuilder();
    sb.append("【背包】\n");
    if (inventory.getEquipmentByQuality() != null && !inventory.getEquipmentByQuality().isEmpty()) {
      sb.append("\n【装备】\n");
      inventory
          .getEquipmentByQuality()
          .forEach((quality, count) -> sb.append(String.format("  %s x%d\n", quality, count)));
    }
    if (inventory.getStackableItemCount() != null && !inventory.getStackableItemCount().isEmpty()) {
      sb.append("\n【物品】\n");
      inventory
          .getStackableItemCount()
          .forEach((type, count) -> sb.append(String.format("  %s x%d种\n", type.getName(), count)));
    }
    sb.append(String.format("\n灵石：%d\n", inventory.getSpiritStones()));
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
      if (change.getStrChange() != 0)
        sb.append(formatAttrChange("力道", change.getStrChange())).append("\n");
      if (change.getConChange() != 0)
        sb.append(formatAttrChange("根骨", change.getConChange())).append("\n");
      if (change.getAgiChange() != 0)
        sb.append(formatAttrChange("身法", change.getAgiChange())).append("\n");
      if (change.getWisChange() != 0)
        sb.append(formatAttrChange("悟性", change.getWisChange())).append("\n");
      if (change.getAttackChange() != 0)
        sb.append(formatAttrChange("攻击", change.getAttackChange())).append("\n");
      if (change.getDefenseChange() != 0)
        sb.append(formatAttrChange("防御", change.getDefenseChange())).append("\n");
      if (change.getMaxHpChange() != 0)
        sb.append(formatAttrChange("HP上限", change.getMaxHpChange()));
    }
    return sb.toString();
  }

  private String formatAttrChange(String attrName, int change) {
    String sign = change > 0 ? "+" : "";
    return String.format("  %s：%s%d", attrName, sign, change);
  }

  private String formatPlayerView(PlayerViewVO vo) {
    StringBuilder sb = new StringBuilder();
    sb.append("【").append(vo.nickname()).append("】\n");
    sb.append("境界：第").append(vo.level()).append("层\n");
    sb.append("状态：").append(vo.statusName()).append("\n");
    sb.append("HP：").append(vo.hpCurrent()).append("/").append(vo.hpMax()).append("\n");
    sb.append("攻击：").append(vo.attack()).append(" | 防御：").append(vo.defense()).append("\n");
    sb.append("力道：").append(vo.statStr()).append(" | 根骨：").append(vo.statCon());
    sb.append(" | 身法：").append(vo.statAgi()).append(" | 悟性：").append(vo.statWis()).append("\n");
    if (vo.locationName() != null) {
      sb.append("所在地：").append(vo.locationName()).append("\n");
    }
    if (vo.equippedItems() != null && !vo.equippedItems().isEmpty()) {
      sb.append("\n【已穿戴装备】\n");
      for (String item : vo.equippedItems()) {
        sb.append("  ").append(item).append("\n");
      }
    }
    return sb.toString();
  }

  @Override
  public String groupName() {
    return "角色";
  }

  @Override
  public String groupDescription() {
    return "角色注册、状态查看、背包、装备、突破、护道";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("我要修仙 {{道号}}", "注册新角色", "我要修仙 张三"),
        new CommandEntry("状态", "查看角色完整信息", "状态"),
        new CommandEntry("背包", "查看背包汇总", "背包"),
        new CommandEntry("背包 {{分类}}", "查看分类物品（种子/装备/兽卵）", "背包 种子"),
        new CommandEntry("装备 {{物品}}", "穿戴装备", "装备 玄铁剑"),
        new CommandEntry("卸下 {{部位/物品}}", "卸下装备（支持部位名或物品名/编号）", "卸下 武器"),
        new CommandEntry("丢弃 {{物品}}", "丢弃物品或装备", "丢弃 玄铁剑"),
        new CommandEntry("查看 {{道号}}", "查看其他玩家的信息", "查看 张三"),
        new CommandEntry("突破", "尝试境界突破", "突破"),
        new CommandEntry("护道 {{道号}}", "建立护道关系", "护道 李四"),
        new CommandEntry("护道解除 {{道号}}", "解除护道关系", "护道解除 李四"),
        new CommandEntry("护道查询", "查看护道信息", "护道查询"));
  }

  private String formatBreakthroughResult(BreakthroughResult result) {
    StringBuilder sb = new StringBuilder();
    sb.append(result.getMessage()).append("\n\n");
    if (result.getSuccessRate() != null)
      sb.append(String.format("突破成功率：%.1f%%\n", result.getSuccessRate()));
    if (result.getNewLevel() != null) sb.append(String.format("当前境界：第%d层\n", result.getNewLevel()));
    if (result.getNextBreakthroughRate() != null)
      sb.append(String.format("下次突破成功率：%.1f%%", result.getNextBreakthroughRate()));
    return sb.toString();
  }

  private String formatProtectionResult(DaoProtectionResult result) {
    return result.getMessage()
        + "\n\n"
        + "【护道详情】\n"
        + String.format("  护道者：%s（第%d层）\n", result.getProtectorName(), result.getProtectorLevel())
        + String.format("  被护道者：%s（第%d层）\n", result.getProtegeName(), result.getProtegeLevel())
        + String.format("  单人加成：%.1f%%\n", result.getSingleProtectorBonus())
        + String.format("  是否同地点：%s", result.getIsInSameLocation() ? "是" : "否");
  }

  private String formatProtectionQueryResult(DaoProtectionQueryResult result) {
    StringBuilder sb = new StringBuilder();
    sb.append(result.getMessage()).append("\n");
    if (result.getProtectingList() != null && !result.getProtectingList().isEmpty()) {
      sb.append(
          String.format(
              "\n【你正在为以下道友护道】(%d/%d)\n",
              result.getProtectingCount(), result.getMaxProtectingCount()));
      for (var info : result.getProtectingList()) {
        String locationStatus = info.getIsInSameLocation() ? "[同地点]" : "[异地]";
        sb.append(
            String.format(
                "  %s（第%d层）- %s %s - 加成%.1f%%\n",
                info.getUserName(),
                info.getUserLevel(),
                info.getLocationName(),
                locationStatus,
                info.getBonusPercentage()));
      }
    } else {
      sb.append("\n【你正在为以下道友护道】无\n");
    }
    if (result.getProtectedByList() != null && !result.getProtectedByList().isEmpty()) {
      sb.append(String.format("\n【以下道友正在为你护道】总加成：%.1f%%\n", result.getTotalBonusPercentage()));
      for (var info : result.getProtectedByList()) {
        String locationStatus = info.getIsInSameLocation() ? "[同地点]" : "[异地]";
        String bonusText =
            info.getIsInSameLocation()
                ? String.format("加成%.1f%%", info.getBonusPercentage())
                : "无法提供加成";
        sb.append(
            String.format(
                "  %s（第%d层）- %s %s - %s\n",
                info.getUserName(),
                info.getUserLevel(),
                info.getLocationName(),
                locationStatus,
                bonusText));
      }
    } else {
      sb.append("\n【以下道友正在为你护道】无\n");
    }
    return sb.toString();
  }
}
