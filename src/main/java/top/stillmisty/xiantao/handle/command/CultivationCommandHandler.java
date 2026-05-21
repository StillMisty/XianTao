package top.stillmisty.xiantao.handle.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.item.entity.ElementRange;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.ProductionItem;
import top.stillmisty.xiantao.domain.item.entity.SeedProduct;
import top.stillmisty.xiantao.domain.item.enums.InventoryCategory;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.vo.AttributeChange;
import top.stillmisty.xiantao.domain.item.vo.CharacterStatusResult;
import top.stillmisty.xiantao.domain.item.vo.EquipmentDetailVO;
import top.stillmisty.xiantao.domain.item.vo.InventorySummaryVO;
import top.stillmisty.xiantao.domain.item.vo.ItemEntry;
import top.stillmisty.xiantao.domain.item.vo.StackableItemDetailVO;
import top.stillmisty.xiantao.domain.monster.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.domain.monster.vo.MonsterDetailVO;
import top.stillmisty.xiantao.domain.pill.enums.PillQuality;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.user.enums.CultivationRealm;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.vo.*;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.infrastructure.util.FormatUtils;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.cultivation.CultivationService;
import top.stillmisty.xiantao.service.inventory.DiscardService;
import top.stillmisty.xiantao.service.inventory.EquipmentService;
import top.stillmisty.xiantao.service.inventory.InventoryService;
import top.stillmisty.xiantao.service.player.CharacterStatusService;
import top.stillmisty.xiantao.service.player.PlayerViewService;
import top.stillmisty.xiantao.service.player.UserService;

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
  private final MonsterTemplateRepository monsterTemplateRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final SkillRepository skillRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;

  // ===================== 统一处理方法（含 TextFormat 参数） =====================

  public String handleRegister(
      PlatformType platform, String openId, String nickname, TextFormat fmt) {
    log.debug("处理注册请求 - Platform: {}, OpenId: {}, Nickname: {}", platform, openId, nickname);
    return CommandHandlerHelper.safeCall(
        () -> userService.createUser(platform, openId, nickname),
        fmt,
        result -> {
          if (!result.success()) {
            return result.message() != null ? result.message() : "系统错误：用户创建失败，请联系管理员";
          }
          log.info("玩家注册成功 - UserId: {}, Nickname: {}", result.userId(), result.nickname());
          return "欢迎踏入仙途！您的道号为：" + result.nickname() + "\n输入「状态」查看您的角色信息";
        });
  }

  public String handleStatus(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理状态查询 - Platform: {}, OpenId: {}", platform, openId);
    return CommandHandlerHelper.safeCall(
        () -> characterStatusService.getCharacterStatus(platform, openId),
        fmt,
        vo -> formatCharacterStatus(vo, fmt));
  }

  public String handleInventory(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理背包查询 - Platform: {}, OpenId: {}", platform, openId);
    return CommandHandlerHelper.safeCall(
        () -> inventoryService.getInventorySummary(platform, openId),
        fmt,
        vo -> formatInventorySummary(vo, fmt));
  }

  public String handleSeedInventory(PlatformType platform, String openId, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> inventoryService.getSeedInventory(platform, openId),
        fmt,
        entries -> formatItemList("种子", entries, fmt));
  }

  public String handleEquipmentInventory(PlatformType platform, String openId, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> inventoryService.getEquipmentInventory(platform, openId),
        fmt,
        entries -> formatItemList("装备", entries, fmt));
  }

  public String handleEggInventory(PlatformType platform, String openId, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> inventoryService.getEggInventory(platform, openId),
        fmt,
        entries -> formatItemList("兽卵", entries, fmt));
  }

  public String handleInventoryByCategory(
      PlatformType platform, String openId, String category, TextFormat fmt) {
    for (var cat : InventoryCategory.values()) {
      if (cat.getChineseName().equals(category)) {
        if (cat == InventoryCategory.EQUIPMENT) {
          return handleEquipmentInventory(platform, openId, fmt);
        }
        var type = cat.toItemType();
        if (type != null) {
          return handleItemTypeInventory(platform, openId, cat.getChineseName(), type, fmt);
        }
      }
    }
    var validCategories =
        Arrays.stream(InventoryCategory.values())
            .map(InventoryCategory::getChineseName)
            .collect(Collectors.joining("、"));
    return "未知分类，可选：" + validCategories;
  }

  private String handleItemTypeInventory(
      PlatformType platform, String openId, String categoryName, ItemType type, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> inventoryService.getItemsByType(platform, openId, type),
        fmt,
        entries -> formatItemList(categoryName, entries, fmt));
  }

  public String handleEquip(PlatformType platform, String openId, String itemName, TextFormat fmt) {
    log.debug("处理装备穿戴 - Platform: {}, OpenId: {}, ItemName: {}", platform, openId, itemName);
    return CommandHandlerHelper.safeCall(
        () -> equipmentService.equipItem(platform, openId, itemName),
        fmt,
        vo -> vo.success() ? formatEquipResult(vo, fmt) : vo.message());
  }

  public String handleUnequip(
      PlatformType platform, String openId, String slotName, TextFormat fmt) {
    log.debug("处理装备卸下 - Platform: {}, OpenId: {}, SlotName: {}", platform, openId, slotName);
    return CommandHandlerHelper.safeCall(
        () -> equipmentService.unequipItem(platform, openId, slotName),
        fmt,
        vo -> vo.isSuccess() ? formatUnequipResult(vo, fmt) : vo.getMessage());
  }

  public String handleBreakthrough(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理突破 - Platform: {}, OpenId: {}", platform, openId);
    return CommandHandlerHelper.safeCall(
        () -> cultivationService.attemptBreakthrough(platform, openId),
        fmt,
        vo -> formatBreakthroughResult(vo, fmt));
  }

  public String handleEstablishProtection(
      PlatformType platform, String openId, String protegeNickname, TextFormat fmt) {
    log.debug("处理护道 - Platform: {}, OpenId: {}, Protege: {}", platform, openId, protegeNickname);
    return CommandHandlerHelper.safeCall(
        () -> cultivationService.establishProtection(platform, openId, protegeNickname),
        fmt,
        vo -> vo.success() ? formatProtectionResult(vo, fmt) : vo.message());
  }

  public String handleRemoveProtection(
      PlatformType platform, String openId, String protegeNickname, TextFormat fmt) {
    log.debug("处理护道解除 - Platform: {}, OpenId: {}, Protege: {}", platform, openId, protegeNickname);
    return CommandHandlerHelper.safeCall(
        () -> cultivationService.removeProtection(platform, openId, protegeNickname),
        fmt,
        vo -> vo.message());
  }

  public String handleDiscard(
      PlatformType platform, String openId, String itemName, TextFormat fmt) {
    log.debug("处理丢弃 - Platform: {}, OpenId: {}, ItemName: {}", platform, openId, itemName);
    return CommandHandlerHelper.safeCall(
        () -> discardService.discardItem(platform, openId, itemName), fmt, msg -> msg);
  }

  public String handleChangeNickname(
      PlatformType platform, String openId, String newNickname, TextFormat fmt) {
    log.debug("处理改号 - Platform: {}, OpenId: {}, NewNickname: {}", platform, openId, newNickname);
    return CommandHandlerHelper.safeCall(
        () -> userService.changeNickname(platform, openId, newNickname), fmt, msg -> msg);
  }

  /** 统一「查看」命令 — 依次尝试装备、怪物、物品 */
  public String handleView(PlatformType platform, String openId, String target, TextFormat fmt) {
    log.debug("处理查看 - Platform: {}, OpenId: {}, Target: {}", platform, openId, target);

    // ① 尝试装备
    String result =
        tryResolve(
            () -> equipmentService.getEquipmentDetail(platform, openId, target),
            vo -> formatEquipmentDetail(vo, fmt));
    if (result != null) return result;

    // ② 尝试怪物
    MonsterDetailVO monster = resolveMonsterDetail(target);
    if (monster != null) return formatMonsterDetail(monster, fmt);

    // ③ 尝试堆叠物品
    result =
        tryResolve(
            () -> inventoryService.getItemDetail(platform, openId, target),
            vo -> formatStackableItemDetail(vo, fmt));
    if (result != null) return result;

    return "未找到 [" + target + "]，可输入装备名/编号、怪物名或物品名";
  }

  public String handleQueryProtection(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理护道查询 - Platform: {}, OpenId: {}", platform, openId);
    return CommandHandlerHelper.safeCall(
        () -> cultivationService.queryProtectionInfo(platform, openId),
        fmt,
        vo -> vo.isSuccess() ? formatProtectionQueryResult(vo, fmt) : vo.getMessage());
  }

  // ===================== 查看命令辅助方法 =====================

  /** 尝试解析请求，成功返回格式化文本，失败返回 null（继续尝试下一个解析器），歧义返回歧义消息 */
  private <T> String tryResolve(Supplier<ServiceResult<T>> call, Function<T, String> onSuccess) {
    try {
      return switch (call.get()) {
        case ServiceResult.Success<T> s -> onSuccess.apply(s.data());
        case ServiceResult.Failure<T> f -> {
          if ("ITEM_MULTIPLE_MATCH".equals(f.errorCode())) yield f.errorMessage();
          yield null;
        }
      };
    } catch (BusinessException e) {
      return null;
    }
  }

  private MonsterDetailVO resolveMonsterDetail(String target) {
    return monsterTemplateRepository
        .findByName(target)
        .map(
            t ->
                new MonsterDetailVO(
                    t.getId(),
                    t.getName(),
                    t.getDescription(),
                    t.getMonsterType() != null ? t.getMonsterType().getName() : "",
                    t.getBaseLevel() != null ? t.getBaseLevel() : 0,
                    t.getBaseHp() != null ? t.getBaseHp() : 0,
                    t.getBaseAttack() != null ? t.getBaseAttack() : 0,
                    t.getBaseDefense() != null ? t.getBaseDefense() : 0,
                    t.getBaseSpeed() != null ? t.getBaseSpeed() : 0,
                    t.getExpReward() != null ? t.getExpReward() : 0,
                    t.getTags()))
        .orElse(null);
  }

  // ===================== 统一格式化方法 =====================

  private String formatCharacterStatus(CharacterStatusResult status, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(fmt.subHeading(status.nickname()));

    if (status.locationName() != null) {
      sb.append(fmt.listItem("所在地：" + status.locationName()));
    }

    if (status.status() == UserStatus.TRAVELING && status.travelDestinationName() != null) {
      sb.append(
          fmt.listItem(
              "状态：赶路中 (" + status.locationName() + " → " + status.travelDestinationName() + ")"));
      if (status.travelTimeMinutes() != null
          && status.travelMinutesElapsed() != null
          && status.travelMinutesRemaining() != null) {
        String progress =
            "旅途进度："
                + FormatUtils.formatMinutes(status.travelMinutesElapsed())
                + "/"
                + FormatUtils.formatMinutes(status.travelTimeMinutes().longValue());
        if (status.travelMinutesRemaining() > 0) {
          progress += "（剩余 " + FormatUtils.formatMinutes(status.travelMinutesRemaining()) + "）";
        } else {
          progress += "（即将到达）";
        }
        sb.append(fmt.listItem(progress));
      } else if (status.estimatedArrivalTime() != null) {
        sb.append(
            fmt.listItem("预计到达：" + FormatUtils.formatDateTime(status.estimatedArrivalTime())));
      }
    } else if (status.status() != null && status.statusName() != null) {
      sb.append(fmt.listItem("状态：" + status.statusName()));
    }

    sb.append(
        fmt.listItem(
            String.format("境界：%s (%.1f%%)", status.realmDisplay(), status.expPercentage())));
    sb.append(
        fmt.listItem(
            String.format(
                "HP：%d/%d (%.1f%%)", status.hpCurrent(), status.hpMax(), status.hpPercentage())));
    sb.append("\n");
    sb.append(fmt.heading("基础属性"));
    sb.append(fmt.listItem("力道：" + formatAttrWithBonus(status.statStr(), status.equipStr())));
    sb.append(fmt.listItem("根骨：" + formatAttrWithBonus(status.statCon(), status.equipCon())));
    sb.append(fmt.listItem("身法：" + formatAttrWithBonus(status.statAgi(), status.equipAgi())));
    sb.append(fmt.listItem("悟性：" + formatAttrWithBonus(status.statWis(), status.equipWis())));
    sb.append("\n");
    sb.append(fmt.heading("战斗属性"));
    sb.append(fmt.listItem("攻击：" + status.attack()));
    sb.append(fmt.listItem("防御：" + status.defense()));
    if (status.breakthroughSuccessRate() != null) {
      sb.append("\n");
      sb.append(fmt.heading("突破信息"));
      sb.append(fmt.listItem(String.format("突破成功率：%.1f%%", status.breakthroughSuccessRate())));
      if (status.breakthroughFailCount() != null && status.breakthroughFailCount() > 0) {
        sb.append(fmt.listItem(String.format("失败次数：%d（已累积补偿）", status.breakthroughFailCount())));
      }
    }
    if (status.protectorCount() != null
        || (status.protectedByList() != null && !status.protectedByList().isEmpty())) {
      sb.append("\n");
      sb.append(fmt.heading("护道信息"));
      if (status.protectingList() != null && !status.protectingList().isEmpty()) {
        sb.append(
            fmt.listItem(
                String.format(
                    "你正在为 %d/%d 位道友护道", status.protectorCount(), status.maxProtectorCount())));
        for (var info : status.protectingList()) {
          String locationStatus = locationStatusText(info.isInSameLocation(), fmt);
          sb.append(
              fmt.subListItem(
                  String.format(
                      "%s（%s）- %s %s - 加成%.1f%%",
                      info.userName(),
                      CultivationRealm.realmDisplay(info.userLevel()),
                      info.locationName(),
                      locationStatus,
                      info.bonusPercentage())));
        }
      } else {
        sb.append(
            fmt.listItem(
                String.format(
                    "你正在为 %d/%d 位道友护道",
                    status.protectorCount() != null ? status.protectorCount() : 0,
                    status.maxProtectorCount() != null ? status.maxProtectorCount() : 3)));
      }
      if (status.protectedByList() != null && !status.protectedByList().isEmpty()) {
        sb.append(
            fmt.listItem(
                String.format(
                    "有 %d 位道友为你护道，总加成：%.1f%%",
                    status.protectedByList().size(),
                    status.totalProtectionBonus() != null ? status.totalProtectionBonus() : 0.0)));
        for (var info : status.protectedByList()) {
          String locationStatus = locationStatusText(info.isInSameLocation(), fmt);
          String bonusText =
              Boolean.TRUE.equals(info.isInSameLocation())
                  ? String.format("加成%.1f%%", info.bonusPercentage())
                  : "无法提供加成";
          sb.append(
              fmt.subListItem(
                  String.format(
                      "%s（%s）- %s %s - %s",
                      info.userName(),
                      CultivationRealm.realmDisplay(info.userLevel()),
                      info.locationName(),
                      locationStatus,
                      bonusText)));
        }
      } else {
        sb.append(fmt.listItem("无道友为你护道"));
      }
    }
    sb.append("\n").append(fmt.heading("灵石：" + status.spiritStones()));
    if (status.equipment() != null && !status.equipment().items().isEmpty()) {
      sb.append("\n");
      sb.append(fmt.heading("已穿戴装备"));
      status
          .equipment()
          .items()
          .forEach(
              item ->
                  sb.append(
                      fmt.listItem(
                          String.format(
                              "%s：%s [%s]", item.slotName(), item.name(), item.rarityName()))));
    }
    return sb.toString();
  }

  private String formatItemList(String title, List<ItemEntry> entries, TextFormat fmt) {
    if (entries.isEmpty()) {
      return fmt.heading(title + "列表（空）").trim();
    }
    var sb = new StringBuilder(fmt.heading(title + "列表"));
    for (var e : entries) {
      sb.append(e.index()).append(". ").append(e.name());
      if (e.quantity() > 1) sb.append(" x").append(e.quantity());
      if (!e.metadata().isBlank()) sb.append(" [").append(e.metadata()).append("]");
      sb.append("\n");
    }
    return sb.toString().strip();
  }

  private String formatInventorySummary(InventorySummaryVO inventory, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(fmt.heading("背包"));
    if (inventory.equipmentByQuality() != null && !inventory.equipmentByQuality().isEmpty()) {
      sb.append("\n");
      sb.append(fmt.heading("装备"));
      inventory
          .equipmentByQuality()
          .forEach((quality, count) -> sb.append(fmt.listItem(quality + " x" + count)));
    }
    if (inventory.stackableItemCount() != null && !inventory.stackableItemCount().isEmpty()) {
      sb.append("\n");
      sb.append(fmt.heading("物品"));
      inventory
          .stackableItemCount()
          .forEach((type, count) -> sb.append(fmt.listItem(type.getName() + " x" + count + "种")));
    }
    sb.append("\n").append(fmt.listItem("灵石：" + inventory.spiritStones()));
    return sb.toString();
  }

  private String formatEquipResult(
      top.stillmisty.xiantao.domain.item.vo.EquipResult result, TextFormat fmt) {
    return formatAttributeChangeResult(result.message(), result.attributeChange(), fmt);
  }

  private String formatUnequipResult(
      top.stillmisty.xiantao.domain.item.vo.UnequipResult result, TextFormat fmt) {
    return formatAttributeChangeResult(result.getMessage(), result.getAttributeChange(), fmt);
  }

  private String formatAttributeChangeResult(
      String message, AttributeChange change, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(message).append("\n");
    if (change != null) {
      sb.append("\n");
      sb.append(fmt.heading("属性变化"));
      if (change.strChange() != 0)
        sb.append(formatAttrChange("力道", change.strChange())).append("\n");
      if (change.conChange() != 0)
        sb.append(formatAttrChange("根骨", change.conChange())).append("\n");
      if (change.agiChange() != 0)
        sb.append(formatAttrChange("身法", change.agiChange())).append("\n");
      if (change.wisChange() != 0)
        sb.append(formatAttrChange("悟性", change.wisChange())).append("\n");
      if (change.attackChange() != 0)
        sb.append(formatAttrChange("攻击", change.attackChange())).append("\n");
      if (change.defenseChange() != 0)
        sb.append(formatAttrChange("防御", change.defenseChange())).append("\n");
      if (change.maxHpChange() != 0) sb.append(formatAttrChange("HP上限", change.maxHpChange()));
    }
    return sb.toString();
  }

  private String formatPlayerView(PlayerViewVO vo, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(fmt.subHeading(vo.nickname()));
    sb.append(fmt.listItem("境界：" + vo.realmDisplay()));
    sb.append(fmt.listItem("状态：" + vo.statusName()));
    sb.append(fmt.listItem("HP：" + vo.hpCurrent() + "/" + vo.hpMax()));
    sb.append(fmt.listItem("攻击：" + vo.attack() + " | 防御：" + vo.defense()));
    sb.append(
        fmt.listItem(
            "力道："
                + vo.statStr()
                + " | 根骨："
                + vo.statCon()
                + " | 身法："
                + vo.statAgi()
                + " | 悟性："
                + vo.statWis()));
    if (vo.locationName() != null) {
      sb.append(fmt.listItem("所在地：" + vo.locationName()));
    }
    if (vo.equippedItems() != null && !vo.equippedItems().isEmpty()) {
      sb.append("\n");
      sb.append(fmt.heading("已穿戴装备"));
      for (String item : vo.equippedItems()) {
        sb.append(fmt.listItem(item));
      }
    }
    return sb.toString();
  }

  private String formatBreakthroughResult(BreakthroughResult result, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(result.message()).append("\n\n");

    if (result.battleResult() != null) {
      // 战斗突破：展示雷劫类型 + 战斗摘要
      sb.append(fmt.heading("雷劫详情"));
      sb.append(fmt.listItem("雷劫类型：" + result.tribulationTypeName()));
      sb.append(fmt.listItem("战斗回合：" + result.battleResult().rounds()));
      sb.append(fmt.listItem("胜负：" + result.battleResult().winner()));
      if (result.newLevel() != null) sb.append(fmt.listItem("当前境界：" + result.realmDisplay()));

      if (result.battleResult().playerHpChange() != null) {
        result
            .battleResult()
            .playerHpChange()
            .forEach(
                (name, hp) ->
                    sb.append(
                        fmt.listItem(
                            String.format("%s HP：%d → %d", name, hp.before(), hp.after()))));
      }
    } else {
      // 小境界突破：现有格式
      if (result.successRate() != null)
        sb.append(fmt.listItem(String.format("突破成功率：%.1f%%", result.successRate())));
      if (result.newLevel() != null)
        sb.append(fmt.listItem(String.format("当前境界：%s", result.realmDisplay())));
      if (result.nextBreakthroughRate() != null)
        sb.append(fmt.listItem(String.format("下次突破成功率：%.1f%%", result.nextBreakthroughRate())));
    }
    return sb.toString();
  }

  private String formatProtectionResult(DaoProtectionResult result, TextFormat fmt) {
    return (result.message()
        + "\n\n"
        + fmt.heading("护道详情")
        + fmt.listItem(
            "护道者："
                + result.protectorName()
                + "（"
                + CultivationRealm.realmDisplay(result.protectorLevel())
                + "）")
        + fmt.listItem(
            "被护道者："
                + result.protegeName()
                + "（"
                + CultivationRealm.realmDisplay(result.protegeLevel())
                + "）")
        + fmt.listItem(String.format("单人加成：%.1f%%", result.singleProtectorBonus()))
        + fmt.listItem("是否同地点：" + (result.isInSameLocation() ? "是" : "否")));
  }

  private String formatProtectionQueryResult(DaoProtectionQueryResult result, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(result.getMessage()).append("\n");
    if (result.getProtectingList() != null && !result.getProtectingList().isEmpty()) {
      sb.append("\n");
      sb.append(
          fmt.heading(
              String.format(
                  "你正在为以下道友护道 (%d/%d)",
                  result.getProtectingCount(), result.getMaxProtectingCount())));
      for (var info : result.getProtectingList()) {
        String locationStatus = locationStatusText(info.getIsInSameLocation(), fmt);
        sb.append(
            fmt.subListItem(
                String.format(
                    "%s（%s）- %s %s - 加成%.1f%%",
                    info.getUserName(),
                    CultivationRealm.realmDisplay(info.getUserLevel()),
                    info.getLocationName(),
                    locationStatus,
                    info.getBonusPercentage())));
      }
    } else {
      sb.append("\n");
      sb.append(fmt.heading("你正在为以下道友护道"));
      sb.append("无\n");
    }
    if (result.getProtectedByList() != null && !result.getProtectedByList().isEmpty()) {
      sb.append("\n");
      sb.append(
          fmt.heading(String.format("以下道友正在为你护道（总加成：%.1f%%）", result.getTotalBonusPercentage())));
      for (var info : result.getProtectedByList()) {
        String locationStatus = locationStatusText(info.getIsInSameLocation(), fmt);
        String bonusText =
            info.getIsInSameLocation()
                ? String.format("加成%.1f%%", info.getBonusPercentage())
                : "无法提供加成";
        sb.append(
            fmt.subListItem(
                String.format(
                    "%s（%s）- %s %s - %s",
                    info.getUserName(),
                    CultivationRealm.realmDisplay(info.getUserLevel()),
                    info.getLocationName(),
                    locationStatus,
                    bonusText)));
      }
    } else {
      sb.append("\n");
      sb.append(fmt.heading("以下道友正在为你护道"));
      sb.append("无\n");
    }
    return sb.toString();
  }

  // ===================== 工具方法 =====================

  private String formatAttrWithBonus(int base, int bonus) {
    if (bonus == 0) {
      return String.valueOf(base);
    }
    String sign = bonus > 0 ? "+" : "";
    return String.format("%d（%s%d）", base, sign, bonus);
  }

  private String formatAttrChange(String attrName, int change) {
    String sign = change > 0 ? "+" : "";
    return String.format("  %s：%s%d", attrName, sign, change);
  }

  // ===================== 查看详情格式化方法 =====================

  private String formatEquipmentDetail(EquipmentDetailVO vo, TextFormat fmt) {
    var sb = new StringBuilder();
    sb.append(fmt.subHeading(vo.getRarityEmoji() + " " + vo.getDisplayName()));
    sb.append(fmt.listItem("部位：" + vo.getSlotName()));
    sb.append(fmt.listItem("稀有度：" + vo.getRarityName()));
    if (vo.getWeaponTypeName() != null) {
      sb.append(fmt.listItem("类型：" + vo.getWeaponTypeName()));
    }
    sb.append(fmt.listItem("攻击：" + vo.getAttack() + " | 防御：" + vo.getDefense()));
    sb.append(
        fmt.listItem(
            String.format(
                "力道 +%d | 根骨 +%d | 身法 +%d | 悟性 +%d",
                vo.getStrBonus(), vo.getConBonus(), vo.getAgiBonus(), vo.getWisBonus())));
    if (vo.getAffixDescriptions() != null && !vo.getAffixDescriptions().isEmpty()) {
      sb.append(fmt.listItem("词条：" + String.join("、", vo.getAffixDescriptions())));
    }
    sb.append(
        fmt.listItem(
            String.format(
                "锻造：+%d | 品质系数：%.2f",
                vo.getForgeLevel() != null ? vo.getForgeLevel() : 0,
                vo.getQualityMultiplier() != null ? vo.getQualityMultiplier() : 1.0)));
    if (Boolean.TRUE.equals(vo.getEquipped())) {
      sb.append(fmt.listItem("状态：已穿戴"));
    }
    return sb.toString().strip();
  }

  private String formatStackableItemDetail(StackableItemDetailVO vo, TextFormat fmt) {
    var sb = new StringBuilder();
    sb.append(fmt.subHeading(vo.name()));
    sb.append(fmt.listItem("类型：" + vo.typeName()));
    sb.append(fmt.listItem("数量：" + vo.quantity()));
    if (vo.description() != null && !vo.description().isEmpty()) {
      sb.append(fmt.listItem("描述：" + vo.description()));
    }
    appendTypeProperties(vo, sb, fmt);
    return sb.toString().strip();
  }

  private void appendTypeProperties(StackableItemDetailVO vo, StringBuilder sb, TextFormat fmt) {
    var template = itemTemplateRepository.findById(vo.templateId()).orElse(null);
    ItemProperties typed = template != null ? template.typedProperties() : null;

    switch (vo.typeName()) {
      case "丹药" -> {
        Object grade = vo.properties() != null ? vo.properties().get("grade") : null;
        if (grade instanceof Number n) {
          sb.append(fmt.listItem("品级：" + n.intValue() + "级"));
        }
        if (vo.quality() != null && !vo.quality().isEmpty()) {
          try {
            PillQuality pq = PillQuality.fromCode(vo.quality());
            sb.append(fmt.listItem("成色：" + pq.getChineseName()));
            sb.append(fmt.listItem("效果倍率：×" + pq.getMultiplier()));
          } catch (IllegalArgumentException e) {
            sb.append(fmt.listItem("成色：" + vo.quality()));
          }
        }
        if (typed instanceof ItemProperties.Potion p && !p.effects().isEmpty()) {
          sb.append(fmt.listItem("效果：" + p.effects().size() + "种"));
        }
      }
      case "药材" -> {
        if (typed instanceof ItemProperties.Herb h) {
          var parts = new ArrayList<String>();
          for (var entry : h.elements().entrySet()) {
            parts.add(entry.getKey() + "" + entry.getValue());
          }
          if (!parts.isEmpty()) {
            sb.append(fmt.listItem("五行：" + String.join(" ", parts)));
          }
        }
      }
      case "锻材" -> {
        if (typed instanceof ItemProperties.Material m) {
          sb.append(
              fmt.listItem(
                  String.format("刚硬：%d | 韧性：%d | 灵气：%d", m.rigidity(), m.toughness(), m.spirit())));
        }
      }
      case "种子" -> {
        if (typed instanceof ItemProperties.Growth g) {
          sb.append(fmt.listItem("成熟时间：" + g.growTime() + "小时"));
          if (g.maxHarvest() > 1) {
            sb.append(fmt.listItem("可收获：" + g.maxHarvest() + "次"));
          }
          appendSeedProducts(g.productionItems(), sb, fmt);
        }
      }
      case "兽卵" -> {
        if (typed instanceof ItemProperties.BeastEgg e) {
          sb.append(fmt.listItem("孵化时间：" + e.growTime() + "小时"));
          if (e.skillPool() != null) {
            if (e.skillPool().innateSkills() != null && !e.skillPool().innateSkills().isEmpty()) {
              sb.append(fmt.listItem("天生技能：" + e.skillPool().innateSkills().size() + "个"));
            }
            if (e.skillPool().awakeningSkills() != null
                && !e.skillPool().awakeningSkills().isEmpty()) {
              sb.append(fmt.listItem("觉醒技能：" + e.skillPool().awakeningSkills().size() + "个"));
            }
          }
          appendProductionItems(e.productionItems(), "产出", true, sb, fmt);
        }
      }
      case "法决玉简" -> {
        if (typed instanceof ItemProperties.SkillJade s) {
          skillRepository
              .findById(s.skillId())
              .ifPresent(
                  skill -> {
                    sb.append(fmt.listItem("法决：" + fmt.bold(skill.getName())));
                    sb.append(
                        fmt.listItem(
                            "类型："
                                + (skill.getSkillType() != null
                                    ? skill.getSkillType().getName()
                                    : "未知")));
                    if (skill.getEffects() != null && !skill.getEffects().isEmpty()) {
                      var effectText = new StringBuilder("效果：");
                      for (int i = 0; i < skill.getEffects().size(); i++) {
                        if (i > 0) effectText.append(" + ");
                        effectText.append(SkillFormatter.formatEffect(skill.getEffects().get(i)));
                      }
                      sb.append(fmt.listItem(effectText.toString()));
                    }
                    if (skill.getBindingType() != null) {
                      var bindingText =
                          new StringBuilder("绑定：").append(skill.getBindingType().getName());
                      if (skill.getBindingValue() != null && !skill.getBindingValue().isBlank()) {
                        bindingText.append("（").append(skill.getBindingValue()).append("）");
                      }
                      sb.append(fmt.listItem(bindingText.toString()));
                    }
                    sb.append(
                        fmt.listItem(
                            "调息："
                                + (skill.getCooldownSeconds() != null
                                    ? skill.getCooldownSeconds()
                                    : 0)
                                + "息 | 修为："
                                + CultivationRealm.realmDisplay(
                                    skill.getLevelRequirement() != null
                                        ? skill.getLevelRequirement()
                                        : 1)));
                    if (skill.getDescription() != null && !skill.getDescription().isEmpty()) {
                      sb.append(fmt.listItem(skill.getDescription()));
                    }
                  });
        }
      }
      case "丹方卷轴" -> {
        if (typed instanceof ItemProperties.Scroll s) {
          sb.append(fmt.listItem("品级：" + s.grade() + "级"));
          itemTemplateRepository
              .findById(s.resultItemId())
              .ifPresent(
                  resultTemplate ->
                      sb.append(
                          fmt.listItem(
                              "产出：" + resultTemplate.getName() + " x" + s.resultQuantity())));
          if (s.requirements() != null && !s.requirements().isEmpty()) {
            var reqParts = new ArrayList<String>();
            s.requirements()
                .forEach(
                    (elem, range) -> reqParts.add(elem + ":" + range.min() + "-" + range.max()));
            sb.append(fmt.listItem("需求五行：" + String.join(" ", reqParts)));
          }
        }
      }
      case "锻造图纸" -> {
        if (typed instanceof ItemProperties.ForgingBlueprint f) {
          sb.append(fmt.listItem("品级：" + f.grade() + "级"));
          equipmentTemplateRepository
              .findById(f.equipmentTemplateId())
              .ifPresent(eqt -> sb.append(fmt.listItem("产出：" + eqt.getName())));
          if (f.requirements() != null && !f.requirements().isEmpty()) {
            sb.append(
                fmt.listItem(
                    String.format(
                        "需求：刚硬%d-%d 韧性%d-%d 灵气%d-%d",
                        f.requirements().getOrDefault("RIGIDITY", new ElementRange(0, 0)).min(),
                        f.requirements().getOrDefault("RIGIDITY", new ElementRange(0, 0)).max(),
                        f.requirements().getOrDefault("TOUGHNESS", new ElementRange(0, 0)).min(),
                        f.requirements().getOrDefault("TOUGHNESS", new ElementRange(0, 0)).max(),
                        f.requirements().getOrDefault("SPIRIT", new ElementRange(0, 0)).min(),
                        f.requirements().getOrDefault("SPIRIT", new ElementRange(0, 0)).max())));
          }
        }
      }
      default -> {}
    }
  }

  private void appendProductionItems(
      List<ProductionItem> items,
      String label,
      boolean showWeight,
      StringBuilder sb,
      TextFormat fmt) {
    if (items == null || items.isEmpty()) return;
    int totalWeight = showWeight ? items.stream().mapToInt(ProductionItem::weight).sum() : 0;
    var names =
        items.stream()
            .map(
                pi -> {
                  String name =
                      itemTemplateRepository
                          .findById(pi.templateId())
                          .map(t -> t.getName())
                          .orElse("物品#" + pi.templateId());
                  if (showWeight && totalWeight > 0 && items.size() > 1) {
                    int pct = pi.weight() * 100 / totalWeight;
                    return name + "(" + pct + "%)";
                  }
                  return name;
                })
            .toList();
    sb.append(fmt.listItem(label + "：" + String.join("、", names)));
  }

  private void appendSeedProducts(List<SeedProduct> items, StringBuilder sb, TextFormat fmt) {
    if (items == null || items.isEmpty()) return;
    var names =
        items.stream()
            .map(
                sp ->
                    itemTemplateRepository
                        .findById(sp.templateId())
                        .map(t -> t.getName())
                        .orElse("物品#" + sp.templateId()))
            .toList();
    sb.append(fmt.listItem("产出：" + String.join("、", names)));
  }

  private String formatMonsterDetail(MonsterDetailVO vo, TextFormat fmt) {
    var sb = new StringBuilder();
    sb.append(fmt.subHeading(vo.name()));
    sb.append(fmt.listItem("类型：" + vo.typeName()));
    sb.append(fmt.listItem("等级：" + vo.baseLevel()));
    sb.append(fmt.listItem("HP：" + vo.baseHp()));
    sb.append(fmt.listItem("攻击：" + vo.baseAttack() + " | 防御：" + vo.baseDefense()));
    sb.append(fmt.listItem("速度：" + vo.baseSpeed()));
    sb.append(fmt.listItem("经验奖励：" + vo.expReward()));
    if (vo.description() != null && !vo.description().isEmpty()) {
      sb.append(fmt.listItem("描述：" + vo.description()));
    }
    return sb.toString().strip();
  }

  // ===================== 工具方法 =====================

  private String locationStatusText(Boolean isInSameLocation, TextFormat fmt) {
    boolean same = Boolean.TRUE.equals(isInSameLocation);
    return same ? fmt.emoji("📍", "") + "同地点" : fmt.emoji("📌", "") + "异地";
  }

  // ===================== CommandGroup 实现 =====================

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
        new CommandEntry("改号 「新道号」", "更改道号（道号不可与其他玩家重复）", "改号 李四"),
        new CommandEntry("我要修仙 「道号」", "注册新角色", "我要修仙 张三"),
        new CommandEntry("状态", "查看角色完整信息", "状态"),
        new CommandEntry("背包", "查看背包汇总", "背包"),
        new CommandEntry("背包 「分类」", "查看分类物品（种子/装备/兽卵/锻材/丹药/药材/法决玉简/丹方卷轴/锻造图纸/灵兽精华）", "背包 丹药"),
        new CommandEntry("装备 「物品」", "穿戴装备", "装备 玄铁剑"),
        new CommandEntry("卸下 「部位/物品」", "卸下装备（支持部位名或物品名/编号）", "卸下 武器"),
        new CommandEntry("丢弃 「物品」", "丢弃物品或装备", "丢弃 玄铁剑"),
        new CommandEntry("查看 「目标」", "查看装备/怪物/物品详情", "查看 铁剑"),
        new CommandEntry("突破", "尝试境界突破", "突破"),
        new CommandEntry("护道 「道号」", "建立护道关系", "护道 李四"),
        new CommandEntry("护道解除 「道号」", "解除护道关系", "护道解除 李四"),
        new CommandEntry("护道查询", "查看护道信息", "护道查询"));
  }
}
