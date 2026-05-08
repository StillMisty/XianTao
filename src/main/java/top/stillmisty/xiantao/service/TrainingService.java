package top.stillmisty.xiantao.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.entity.SpecialtyEntry;
import top.stillmisty.xiantao.domain.map.enums.MapType;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.map.vo.TrainingRewardVO;
import top.stillmisty.xiantao.domain.map.vo.TrainingStartResult;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.infrastructure.util.TypeUtils;
import top.stillmisty.xiantao.infrastructure.util.WeightedRandom;
import top.stillmisty.xiantao.service.annotation.Authenticated;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingService {

  private static final long BASE_EXP_PER_MINUTE = 2;
  private final UserStateService userStateService;
  private final MapNodeRepository mapNodeRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemService stackableItemService;
  private final TrainingCombatLogic trainingCombatLogic;

  @Authenticated
  @Transactional
  public ServiceResult<TrainingStartResult> startTraining(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(startTraining(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<TrainingRewardVO> endTraining(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(endTraining(userId));
  }

  // ===================== 内部 API =====================

  @Transactional
  public TrainingStartResult startTraining(Long userId) {
    User user = userStateService.loadUser(userId);

    if (user.getStatus() != UserStatus.IDLE) {
      throw new IllegalStateException(
          "您当前处于 " + user.getStatus().getName() + " 状态，无法开始历练（需要 空闲 状态）");
    }

    if (user.getLocationId() == null) {
      return TrainingStartResult.builder().success(false).message("当前位置无效，无法开始历练").build();
    }

    MapNode mapNode = mapNodeRepository.findById(user.getLocationId()).orElse(null);

    if (mapNode == null) {
      return TrainingStartResult.builder().success(false).message("当前地图不存在，无法开始历练").build();
    }

    if (mapNode.getMapType() != MapType.TRAINING_ZONE) {
      return TrainingStartResult.builder()
          .success(false)
          .message("当前地图不是历练区域（需要野外历练区），无法开始历练")
          .build();
    }

    String mapName = mapNode.getName();

    user.setTrainingStartTime(LocalDateTime.now());
    user.setStatus(UserStatus.EXERCISING);
    userStateService.save(user);

    log.info("用户 {} 开始在 {} 历练", userId, mapName);
    return TrainingStartResult.builder().success(true).mapName(mapName).build();
  }

  @Transactional
  public TrainingRewardVO endTraining(Long userId) {
    User user = userStateService.loadUser(userId);

    if (user.getStatus() != UserStatus.EXERCISING && user.getStatus() != UserStatus.DYING) {
      throw new IllegalStateException(
          "您当前处于 " + user.getStatus().getName() + " 状态，无法结束历练（需要 历练 状态）");
    }

    TrainingRewardVO earlyResult = checkEndTrainingEarlyExit(userId, user);
    if (earlyResult != null) return earlyResult;

    long minutesTraining =
        Duration.between(user.getTrainingStartTime(), LocalDateTime.now()).toMinutes();
    MapNode mapNode = mapNodeRepository.findById(user.getLocationId()).orElseThrow();

    return processNormalTrainingEnd(userId, user, minutesTraining, mapNode);
  }

  private TrainingRewardVO checkEndTrainingEarlyExit(Long userId, User user) {
    if (user.getStatus() == UserStatus.DYING) {
      return TrainingRewardVO.builder()
          .userId(userId)
          .mapId(user.getLocationId())
          .summary("你正处于重伤濒死中，请等待自动恢复（30 分钟后恢复至 20% HP）")
          .build();
    }

    if (user.getTrainingStartTime() == null) {
      user.setStatus(UserStatus.IDLE);
      userStateService.save(user);
      return TrainingRewardVO.builder()
          .userId(userId)
          .mapId(user.getLocationId())
          .summary("您当前没有在历练")
          .build();
    }

    long minutesTraining =
        Duration.between(user.getTrainingStartTime(), LocalDateTime.now()).toMinutes();
    if (minutesTraining <= 5) {
      user.setStatus(UserStatus.IDLE);
      user.setTrainingStartTime(null);
      userStateService.save(user);
      return TrainingRewardVO.builder()
          .userId(userId)
          .mapId(user.getLocationId())
          .summary("历练时间过短毫无收获")
          .build();
    }

    if (mapNodeRepository.findById(user.getLocationId()).isEmpty()) {
      user.setStatus(UserStatus.IDLE);
      user.setTrainingStartTime(null);
      userStateService.save(user);
      return TrainingRewardVO.builder().userId(userId).summary("当前地图不存在").build();
    }

    return null;
  }

  private TrainingRewardVO processNormalTrainingEnd(
      Long userId, User user, long minutesTraining, MapNode mapNode) {
    double efficiencyMultiplier = calculateEfficiencyMultiplier(user.getEffectiveStatAgi());
    double levelDecayMultiplier =
        calculateLevelDecayMultiplier(user.getLevel(), mapNode.getLevelRequirement());
    long baseExp =
        (long)
            (BASE_EXP_PER_MINUTE * minutesTraining * efficiencyMultiplier * levelDecayMultiplier);
    List<Map<String, Object>> trainingItems =
        calculateItemsReward(minutesTraining, efficiencyMultiplier, mapNode);

    BattleResultVO battleResult =
        trainingCombatLogic.simulateTraining(userId, user, (int) minutesTraining, mapNode);
    long combatExp = battleResult.expGained();
    long totalExp = baseExp + combatExp;

    user = userStateService.loadUser(userId);
    boolean diedInTraining = user.getStatus() == UserStatus.DYING;

    if (baseExp > 0) {
      user.addExp(baseExp);
    }
    addTrainingItemsToInventory(userId, trainingItems);

    if (!diedInTraining) {
      user.setStatus(UserStatus.IDLE);
    }
    user.setTrainingStartTime(null);
    userStateService.save(user);

    String summary =
        buildEndTrainingSummary(
            minutesTraining, totalExp, battleResult, trainingItems, diedInTraining);

    log.info("用户 {} 结束历练并应用奖励", userId);
    return TrainingRewardVO.builder()
        .userId(userId)
        .mapId(mapNode.getId())
        .mapName(mapNode.getName())
        .durationMinutes(minutesTraining)
        .efficiencyMultiplier(efficiencyMultiplier)
        .levelDecayMultiplier(levelDecayMultiplier)
        .exp(totalExp)
        .items(trainingItems)
        .summary(summary)
        .build();
  }

  private String buildEndTrainingSummary(
      long minutesTraining,
      long totalExp,
      BattleResultVO battleResult,
      List<Map<String, Object>> trainingItems,
      boolean diedInTraining) {
    StringBuilder summary = new StringBuilder();
    summary.append(String.format("历练时长: %d 分钟\n", minutesTraining));
    if (totalExp > 0) {
      summary.append(String.format("经验: +%d\n", totalExp));
    }
    if (battleResult.summary() != null) {
      summary.append(battleResult.summary()).append("\n");
    }
    if (!trainingItems.isEmpty()) {
      summary.append("物品:\n");
      for (Map<String, Object> item : trainingItems) {
        String name = (String) item.get("name");
        Integer qty = ((Number) item.getOrDefault("quantity", 1)).intValue();
        summary.append(String.format("  %s x%d\n", name, qty));
      }
    }
    if (diedInTraining) {
      summary.append("\n重伤濒死！30 分钟后自动恢复至 20% HP");
    }
    return summary.toString();
  }

  // ===================== 基础历练收益 =====================

  private double calculateEfficiencyMultiplier(int agility) {
    return 1.0 + (agility * 0.01);
  }

  private double calculateLevelDecayMultiplier(int playerLevel, int mapLevel) {
    int levelDiff = playerLevel - mapLevel - 15;
    if (levelDiff <= 0) {
      return 1.0;
    }
    double decay = levelDiff * 0.04;
    return Math.max(0.1, 1.0 - decay);
  }

  private List<Map<String, Object>> calculateItemsReward(
      long minutesTraining, double efficiencyMultiplier, MapNode mapNode) {
    List<Map<String, Object>> items = new ArrayList<>();
    var specialties = mapNode.getSpecialties();
    if (specialties == null || specialties.isEmpty()) return items;

    Map<Long, ItemTemplate> templateMap =
        itemTemplateRepository
            .findByIds(
                specialties.stream()
                    .map(top.stillmisty.xiantao.domain.map.entity.SpecialtyEntry::templateId)
                    .toList())
            .stream()
            .collect(Collectors.toMap(ItemTemplate::getId, t -> t));

    int dropChances = (int) (minutesTraining / 10.0 * efficiencyMultiplier);

    for (int i = 0; i < dropChances; i++) {
      SpecialtyEntry selectedEntry =
          WeightedRandom.select(specialties, SpecialtyEntry::weight, ThreadLocalRandom.current());
      if (selectedEntry == null) continue;
      Long selectedTemplateId = selectedEntry.templateId();
      if (selectedTemplateId == null) continue;

      int quantity = ThreadLocalRandom.current().nextInt(3) + 1;

      boolean exists = false;
      for (Map<String, Object> existing : items) {
        if (Objects.equals(existing.get("templateId"), selectedTemplateId)) {
          existing.put("quantity", (Integer) existing.get("quantity") + quantity);
          exists = true;
          break;
        }
      }
      if (!exists) {
        ItemTemplate template = templateMap.get(selectedTemplateId);
        String name = template != null ? template.getName() : "未知物品";
        Map<String, Object> item = new HashMap<>();
        item.put("templateId", selectedTemplateId);
        item.put("name", name);
        item.put("quantity", quantity);
        items.add(item);
      }
    }

    return items;
  }

  private void addTrainingItemsToInventory(Long userId, List<Map<String, Object>> items) {
    if (items == null || items.isEmpty()) return;
    for (Map<String, Object> item : items) {
      Long templateId = TypeUtils.toLong(item.get("templateId"));
      if (templateId == null) continue;
      String name = (String) item.get("name");
      int quantity = ((Number) item.getOrDefault("quantity", 1)).intValue();
      ItemType itemType =
          itemTemplateRepository
              .findById(templateId)
              .map(ItemTemplate::getType)
              .orElse(ItemType.MATERIAL);
      stackableItemService.addStackableItem(userId, templateId, itemType, name, quantity);
    }
  }
}
