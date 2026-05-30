package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.MapNodeRepository;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.service.inventory.EquipmentService;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmService {

  private final UserRepository userRepository;
  private final StackableItemService stackableItemService;
  private final EquipmentService equipmentService;
  private final ItemTemplateRepository itemTemplateRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;
  private final MapNodeRepository mapNodeRepository;
  private final SpiritStoneService spiritStoneService;

  // ===================== 公开 API（GM认证由拦截器处理） =====================

  @Transactional
  public ServiceResult<String> gmHelp(Long userId) {
    return new ServiceResult.Success<>(helpInternal());
  }

  @Transactional
  public ServiceResult<String> giveSpiritStones(Long userId, String targetNickname, long amount) {
    String result = giveSpiritStonesInternal(userId, targetNickname, amount);
    if (result.startsWith("未找到") || result.startsWith("数量必须")) {
      return ServiceResult.businessFailure(result);
    }
    return new ServiceResult.Success<>(result);
  }

  @Transactional
  public ServiceResult<String> giveExp(Long userId, String targetNickname, long amount) {
    String result = giveExpInternal(userId, targetNickname, amount);
    if (result.startsWith("未找到") || result.startsWith("数量必须")) {
      return ServiceResult.businessFailure(result);
    }
    return new ServiceResult.Success<>(result);
  }

  @Transactional
  public ServiceResult<String> healUser(Long userId, String targetNickname) {
    String result = healUserInternal(userId, targetNickname);
    if (result.startsWith("未找到")) {
      return ServiceResult.businessFailure(result);
    }
    return new ServiceResult.Success<>(result);
  }

  @Transactional
  public ServiceResult<String> reviveUser(Long userId, String targetNickname) {
    String result = reviveUserInternal(userId, targetNickname);
    if (result.startsWith("未找到") || result.contains("未处于")) {
      return ServiceResult.businessFailure(result);
    }
    return new ServiceResult.Success<>(result);
  }

  @Transactional
  public ServiceResult<String> setLevel(Long userId, String targetNickname, int level) {
    String result = setLevelInternal(userId, targetNickname, level);
    if (result.startsWith("未找到") || result.startsWith("等级必须")) {
      return ServiceResult.businessFailure(result);
    }
    return new ServiceResult.Success<>(result);
  }

  @Transactional
  public ServiceResult<String> setLocation(
      Long userId, String targetNickname, String locationName) {
    String result = setLocationInternal(userId, targetNickname, locationName);
    if (result.startsWith("未找到")) {
      return ServiceResult.businessFailure(result);
    }
    return new ServiceResult.Success<>(result);
  }

  @Transactional
  public ServiceResult<String> giveItem(
      Long userId, String targetNickname, String itemName, int quantity) {
    String result = giveItemInternal(userId, targetNickname, itemName, quantity);
    if (result.startsWith("未找到") || result.startsWith("数量必须")) {
      return ServiceResult.businessFailure(result);
    }
    return new ServiceResult.Success<>(result);
  }

  // ===================== 内部 API（需预先完成认证） =====================

  String helpInternal() {
    return """
            【GM指令列表】
            GM帮助 - 查看此列表
            GM给灵石 [道号] [数量] - 给指定玩家灵石
            GM给修为 [道号] [数量] - 给指定玩家修为
            GM治疗 [道号] - 治疗指定玩家至满血
            GM复活 [道号] - 复活指定玩家
            GM等级 [道号] [等级] - 设置指定玩家等级
            GM传送 [道号] [地点名] - 传送到指定地图节点
            GM给物品 [道号] [物品名] [数量] - 给指定玩家物品/装备（数量可选，默认1）""";
  }

  @Transactional
  String giveSpiritStonesInternal(Long gmUserId, String targetNickname, long amount) {
    if (amount <= 0) return "数量必须大于0";
    User target = getTargetUser(targetNickname);
    if (target == null) return "未找到玩家：" + targetNickname;
    spiritStoneService.deposit(target.getId(), amount);
    long newBalance = spiritStoneService.getBalance(target.getId());
    log.info("GM {} 给 {} 添加灵石 {}（剩余：{}）", gmUserId, targetNickname, amount, newBalance);
    return String.format("已给 %s 添加 %d 灵石（当前：%d）", targetNickname, amount, newBalance);
  }

  @Transactional
  String giveExpInternal(Long gmUserId, String targetNickname, long amount) {
    if (amount <= 0) return "数量必须大于0";
    User target = getTargetUser(targetNickname);
    if (target == null) return "未找到玩家：" + targetNickname;
    long before = target.getExp();
    target.addExp(amount);
    long actualAdd = target.getExp() - before;
    userRepository.save(target);
    log.info(
        "GM {} 给 {} 添加修为 {}（实际：{}，当前：{}）",
        gmUserId,
        targetNickname,
        amount,
        actualAdd,
        target.getExp());
    if (actualAdd < amount) {
      return String.format(
          "已给 %s 添加 %d 修为（存储上限已满，实际获得 %d，当前：%d）",
          targetNickname, amount, actualAdd, target.getExp());
    }
    return String.format("已给 %s 添加 %d 修为（当前：%d）", targetNickname, amount, target.getExp());
  }

  @Transactional
  String healUserInternal(Long gmUserId, String targetNickname) {
    User target = getTargetUser(targetNickname);
    if (target == null) return "未找到玩家：" + targetNickname;
    int maxHp = target.calculateMaxHp();
    int before = target.getHpCurrent();
    target.setHpCurrent(maxHp);
    userRepository.save(target);
    log.info("GM {} 治疗 {}（{} → {}）", gmUserId, targetNickname, before, maxHp);
    return String.format("已治疗 %s（%d → %d）", targetNickname, before, maxHp);
  }

  @Transactional
  String reviveUserInternal(Long gmUserId, String targetNickname) {
    User target = getTargetUser(targetNickname);
    if (target == null) return "未找到玩家：" + targetNickname;
    if (target.getStatus() != UserStatus.DYING) return targetNickname + " 未处于濒死状态";
    int maxHp = target.calculateMaxHp();
    target.setHpCurrent(maxHp);
    target.setStatus(UserStatus.IDLE);
    target.clearActivity();
    target.setDyingStartTime(null);
    userRepository.save(target);
    log.info("GM {} 复活 {}（HP 恢复至 {}）", gmUserId, targetNickname, maxHp);
    return String.format("已复活 %s（HP：%d，状态：空闲）", targetNickname, maxHp);
  }

  @Transactional
  String setLevelInternal(Long gmUserId, String targetNickname, int level) {
    if (level < 1) return "等级必须大于等于1";
    User target = getTargetUser(targetNickname);
    if (target == null) return "未找到玩家：" + targetNickname;
    int before = target.getLevel();
    target.setLevel(level);
    target.setExp(0L);
    userRepository.save(target);
    log.info("GM {} 设置 {} 等级（{} → {}）", gmUserId, targetNickname, before, level);
    return String.format("已将 %s 等级设置为 %d（原等级：%d，修为已清零）", targetNickname, level, before);
  }

  @Transactional
  String setLocationInternal(Long gmUserId, String targetNickname, String locationName) {
    User target = getTargetUser(targetNickname);
    if (target == null) return "未找到玩家：" + targetNickname;
    var mapNode = mapNodeRepository.findByName(locationName);
    if (mapNode.isEmpty()) return "未找到地点：" + locationName;
    target.setLocationId(mapNode.get().getId());
    target.setStatus(UserStatus.IDLE);
    target.clearActivity();
    userRepository.save(target);
    log.info("GM {} 传送 {} 到 {}（{}）", gmUserId, targetNickname, locationName, mapNode.get().getId());
    return String.format("已将 %s 传送到「%s」", targetNickname, locationName);
  }

  @Transactional
  String giveItemInternal(Long gmUserId, String targetNickname, String itemName, int quantity) {
    if (quantity <= 0) return "数量必须大于0";
    User target = getTargetUser(targetNickname);
    if (target == null) return "未找到玩家：" + targetNickname;

    var itemTemplate = itemTemplateRepository.findByName(itemName);
    if (itemTemplate.isPresent()) {
      var template = itemTemplate.get();
      stackableItemService.addStackableItem(
          target.getId(), template.getId(), template.getType(), template.getName(), quantity);
      log.info("GM {} 给 {} 添加物品 {} x{}", gmUserId, targetNickname, itemName, quantity);
      return String.format("已给 %s 添加 %s x%d", targetNickname, itemName, quantity);
    }

    var equipTemplate = equipmentTemplateRepository.findByName(itemName);
    if (equipTemplate.isPresent()) {
      var template = equipTemplate.get();
      for (int i = 0; i < quantity; i++) {
        equipmentService.createEquipment(target.getId(), template.getId());
      }
      log.info("GM {} 给 {} 添加装备 {} x{}", gmUserId, targetNickname, itemName, quantity);
      return String.format("已给 %s 添加 %s x%d", targetNickname, itemName, quantity);
    }

    return "未找到物品/装备：" + itemName;
  }

  @Nullable
  private User getTargetUser(String nickname) {
    return userRepository.findByNickname(nickname).orElse(null);
  }
}
