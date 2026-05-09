package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.service.annotation.Authenticated;

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

  // ===================== 公开 API（含 GM 认证） =====================

  @Authenticated
  @Transactional
  public ServiceResult<String> gmHelp(PlatformType platform, String openId) {
    Long gmUserId = UserContext.getCurrentUserId();
    if (!isGm(gmUserId)) return ServiceResult.businessFailure("你不是GM，无法执行GM指令");
    return new ServiceResult.Success<>(help(gmUserId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> giveSpiritStones(
      PlatformType platform, String openId, String targetNickname, long amount) {
    Long gmUserId = UserContext.getCurrentUserId();
    if (!isGm(gmUserId)) return ServiceResult.businessFailure("你不是GM，无法执行GM指令");
    return new ServiceResult.Success<>(giveSpiritStones(gmUserId, targetNickname, amount));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> giveExp(
      PlatformType platform, String openId, String targetNickname, long amount) {
    Long gmUserId = UserContext.getCurrentUserId();
    if (!isGm(gmUserId)) return ServiceResult.businessFailure("你不是GM，无法执行GM指令");
    return new ServiceResult.Success<>(giveExp(gmUserId, targetNickname, amount));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> healUser(
      PlatformType platform, String openId, String targetNickname) {
    Long gmUserId = UserContext.getCurrentUserId();
    if (!isGm(gmUserId)) return ServiceResult.businessFailure("你不是GM，无法执行GM指令");
    return new ServiceResult.Success<>(healUser(gmUserId, targetNickname));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> reviveUser(
      PlatformType platform, String openId, String targetNickname) {
    Long gmUserId = UserContext.getCurrentUserId();
    if (!isGm(gmUserId)) return ServiceResult.businessFailure("你不是GM，无法执行GM指令");
    return new ServiceResult.Success<>(reviveUser(gmUserId, targetNickname));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> setLevel(
      PlatformType platform, String openId, String targetNickname, int level) {
    Long gmUserId = UserContext.getCurrentUserId();
    if (!isGm(gmUserId)) return ServiceResult.businessFailure("你不是GM，无法执行GM指令");
    return new ServiceResult.Success<>(setLevel(gmUserId, targetNickname, level));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> setLocation(
      PlatformType platform, String openId, String targetNickname, String locationName) {
    Long gmUserId = UserContext.getCurrentUserId();
    if (!isGm(gmUserId)) return ServiceResult.businessFailure("你不是GM，无法执行GM指令");
    return new ServiceResult.Success<>(setLocation(gmUserId, targetNickname, locationName));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> giveItem(
      PlatformType platform, String openId, String targetNickname, String itemName, int quantity) {
    Long gmUserId = UserContext.getCurrentUserId();
    if (!isGm(gmUserId)) return ServiceResult.businessFailure("你不是GM，无法执行GM指令");
    return new ServiceResult.Success<>(giveItem(gmUserId, targetNickname, itemName, quantity));
  }

  // ===================== 内部 API（需预先完成认证） =====================

  private boolean isGm(Long userId) {
    return userRepository.findById(userId).map(u -> Boolean.TRUE.equals(u.getGm())).orElse(false);
  }

  String help(Long gmUserId) {
    return """
            【GM指令列表】
            GM帮助 - 查看此列表
            GM给灵石 [道号] [数量] - 给指定玩家灵石
            GM给修为 [道号] [数量] - 给指定玩家修为
            GM治疗 [道号] - 治疗指定玩家至满血
            GM复活 [道号] - 复活指定玩家
            GM设置等级 [道号] [等级] - 设置指定玩家等级
            GM设置所在地点 [道号] [地点名] - 传送到指定地图节点
            GM给物品和装备 [道号] [物品名] [数量] - 给指定玩家物品/装备（数量可选，默认1）""";
  }

  @Transactional
  String giveSpiritStones(Long gmUserId, String targetNickname, long amount) {
    if (amount <= 0) return "数量必须大于0";
    User target = getTargetUser(targetNickname);
    if (target == null) return "未找到玩家：" + targetNickname;
    target.setSpiritStones(target.getSpiritStones() + amount);
    userRepository.save(target);
    log.info(
        "GM {} 给 {} 添加灵石 {}（剩余：{}）", gmUserId, targetNickname, amount, target.getSpiritStones());
    return String.format("已给 %s 添加 %d 灵石（当前：%d）", targetNickname, amount, target.getSpiritStones());
  }

  @Transactional
  String giveExp(Long gmUserId, String targetNickname, long amount) {
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
  String healUser(Long gmUserId, String targetNickname) {
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
  String reviveUser(Long gmUserId, String targetNickname) {
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
  String setLevel(Long gmUserId, String targetNickname, int level) {
    if (level < 1) return "等级必须大于等于1";
    User target = getTargetUser(targetNickname);
    if (target == null) return "未找到玩家：" + targetNickname;
    int before = target.getLevel();
    target.setLevel(level);
    target.setExp(0L);
    userRepository.save(target);
    log.info("GM {} 设置 {} 等级（{} → {}）", gmUserId, targetNickname, before, level);
    return String.format("已将 %s 等级设置为 %d（原等级：%d，经验已清零）", targetNickname, level, before);
  }

  @Transactional
  String setLocation(Long gmUserId, String targetNickname, String locationName) {
    User target = getTargetUser(targetNickname);
    if (target == null) return "未找到玩家：" + targetNickname;
    var mapNode = mapNodeRepository.findByName(locationName);
    if (mapNode.isEmpty()) return "未找到地点：" + locationName;
    target.setLocationId(mapNode.get().getId());
    userRepository.save(target);
    log.info("GM {} 传送 {} 到 {}（{}）", gmUserId, targetNickname, locationName, mapNode.get().getId());
    return String.format("已将 %s 传送到「%s」", targetNickname, locationName);
  }

  @Transactional
  String giveItem(Long gmUserId, String targetNickname, String itemName, int quantity) {
    if (quantity <= 0) return "数量必须大于0";
    User target = getTargetUser(targetNickname);
    if (target == null) return "未找到玩家：" + targetNickname;

    // 尝试查找堆叠物品模板
    var itemTemplate = itemTemplateRepository.findByName(itemName);
    if (itemTemplate.isPresent()) {
      var template = itemTemplate.get();
      stackableItemService.addStackableItem(
          target.getId(), template.getId(), template.getType(), template.getName(), quantity);
      log.info("GM {} 给 {} 添加物品 {} x{}", gmUserId, targetNickname, itemName, quantity);
      return String.format("已给 %s 添加 %s x%d", targetNickname, itemName, quantity);
    }

    // 尝试查找装备模板
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

  private User getTargetUser(String nickname) {
    return userRepository.findByNickname(nickname).orElse(null);
  }
}
