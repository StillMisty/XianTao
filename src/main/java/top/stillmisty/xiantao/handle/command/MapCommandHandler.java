package top.stillmisty.xiantao.handle.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.bounty.BountyRewardItem;
import top.stillmisty.xiantao.domain.bounty.BountyRewardPool;
import top.stillmisty.xiantao.domain.bounty.vo.BountyRewardVO;
import top.stillmisty.xiantao.domain.bounty.vo.BountyStatusVO;
import top.stillmisty.xiantao.domain.bounty.vo.BountyVO;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.map.vo.MapInfoVO;
import top.stillmisty.xiantao.domain.map.vo.TrainingRewardVO;
import top.stillmisty.xiantao.domain.map.vo.TravelResultVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.infrastructure.util.FormatUtils;
import top.stillmisty.xiantao.service.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MapCommandHandler implements CommandGroup {

  private final MapService mapService;
  private final TravelService travelService;
  private final TrainingService trainingService;
  private final BountyService bountyService;
  private final ItemTemplateRepository itemTemplateRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;

  // ===================== 委托方法（默认 PLAIN） =====================

  public String handleGoTo(PlatformType platform, String openId, String mapName) {
    return handleGoTo(platform, openId, mapName, TextFormat.PLAIN);
  }

  public String handleTraining(PlatformType platform, String openId) {
    return handleTraining(platform, openId, TextFormat.PLAIN);
  }

  public String handleEndTraining(PlatformType platform, String openId) {
    return handleEndTraining(platform, openId, TextFormat.PLAIN);
  }

  public String handleMapList(PlatformType platform, String openId) {
    return handleMapList(platform, openId, TextFormat.PLAIN);
  }

  public String handleCurrentMap(PlatformType platform, String openId) {
    return handleCurrentMap(platform, openId, TextFormat.PLAIN);
  }

  public String handleBountyList(PlatformType platform, String openId) {
    return handleBountyList(platform, openId, TextFormat.PLAIN);
  }

  public String handleBountyStatus(PlatformType platform, String openId) {
    return handleBountyStatus(platform, openId, TextFormat.PLAIN);
  }

  public String handleStartBounty(PlatformType platform, String openId, String bountyId) {
    return handleStartBounty(platform, openId, bountyId, TextFormat.PLAIN);
  }

  public String handleCompleteBounty(PlatformType platform, String openId) {
    return handleCompleteBounty(platform, openId, TextFormat.PLAIN);
  }

  public String handleAbandonBounty(PlatformType platform, String openId) {
    return handleAbandonBounty(platform, openId, TextFormat.PLAIN);
  }

  // ===================== Markdown 委托方法 =====================

  public String handleGoToMarkdown(PlatformType platform, String openId, String mapName) {
    return handleGoTo(platform, openId, mapName, TextFormat.MARKDOWN);
  }

  public String handleTrainingMarkdown(PlatformType platform, String openId) {
    return handleTraining(platform, openId, TextFormat.MARKDOWN);
  }

  public String handleEndTrainingMarkdown(PlatformType platform, String openId) {
    return handleEndTraining(platform, openId, TextFormat.MARKDOWN);
  }

  public String handleMapListMarkdown(PlatformType platform, String openId) {
    return handleMapList(platform, openId, TextFormat.MARKDOWN);
  }

  public String handleCurrentMapMarkdown(PlatformType platform, String openId) {
    return handleCurrentMap(platform, openId, TextFormat.MARKDOWN);
  }

  public String handleBountyListMarkdown(PlatformType platform, String openId) {
    return handleBountyList(platform, openId, TextFormat.MARKDOWN);
  }

  public String handleBountyStatusMarkdown(PlatformType platform, String openId) {
    return handleBountyStatus(platform, openId, TextFormat.MARKDOWN);
  }

  public String handleStartBountyMarkdown(PlatformType platform, String openId, String bountyId) {
    return handleStartBounty(platform, openId, bountyId, TextFormat.MARKDOWN);
  }

  public String handleCompleteBountyMarkdown(PlatformType platform, String openId) {
    return handleCompleteBounty(platform, openId, TextFormat.MARKDOWN);
  }

  public String handleAbandonBountyMarkdown(PlatformType platform, String openId) {
    return handleAbandonBounty(platform, openId, TextFormat.MARKDOWN);
  }

  // ===================== 统一处理方法（含 TextFormat 参数） =====================

  public String handleGoTo(PlatformType platform, String openId, String mapName, TextFormat fmt) {
    log.debug("处理前往请求 - Platform: {}, OpenId: {}, MapName: {}", platform, openId, mapName);
    return switch (travelService.startTravel(platform, openId, mapName)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var vo) ->
          vo.isSuccess() ? formatTravelResult(vo, fmt) : vo.getMessage();
    };
  }

  public String handleTraining(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理历练请求 - Platform: {}, OpenId: {}", platform, openId);
    return switch (trainingService.startTraining(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var vo) ->
          vo.isSuccess()
              ? String.format("开始在%s历练，使用「历练结算」结算收益。", vo.getMapName())
              : vo.getMessage();
    };
  }

  public String handleEndTraining(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理结束历练请求 - Platform: {}, OpenId: {}", platform, openId);
    return switch (trainingService.endTraining(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var vo) -> formatTrainingReward(vo, fmt);
    };
  }

  public String handleMapList(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理地图列表查询 - Platform: {}, OpenId: {}", platform, openId);
    return switch (mapService.getAllMaps(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var vo) -> vo.isEmpty() ? "暂无可用地图" : formatMapList(vo, fmt);
    };
  }

  public String handleCurrentMap(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理当前地图查询 - Platform: {}, OpenId: {}", platform, openId);
    return switch (mapService.getCurrentMapInfo(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var vo) -> formatCurrentMap(vo, fmt);
    };
  }

  // ===================== 悬赏统一处理方法 =====================

  public String handleBountyList(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理悬赏列表 - Platform: {}, OpenId: {}", platform, openId);
    return switch (bountyService.listBounties(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var vo) -> formatBountyList(vo, fmt);
    };
  }

  public String handleBountyStatus(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理悬赏状态 - Platform: {}, OpenId: {}", platform, openId);
    return switch (bountyService.getBountyStatus(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var vo) -> formatBountyStatus(vo, fmt);
    };
  }

  public String handleStartBounty(
      PlatformType platform, String openId, String bountyId, TextFormat fmt) {
    log.debug("处理接取悬赏 - Platform: {}, OpenId: {}, BountyId: {}", platform, openId, bountyId);
    return switch (bountyService.startBounty(platform, openId, bountyId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var msg) -> msg;
    };
  }

  public String handleCompleteBounty(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理完成悬赏 - Platform: {}, OpenId: {}", platform, openId);
    return switch (bountyService.completeBounty(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var vo) -> formatBountyReward(vo, fmt);
    };
  }

  public String handleAbandonBounty(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理放弃悬赏 - Platform: {}, OpenId: {}", platform, openId);
    return switch (bountyService.abandonBounty(platform, openId)) {
      case ServiceResult.Failure(var code, var msg) -> "❌ " + msg;
      case ServiceResult.Success(var msg) -> msg;
    };
  }

  // ===================== 格式化方法 =====================

  private String formatTravelResult(TravelResultVO result, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(result.getMessage()).append("\n");
    if (result.getEstimatedArrivalTime() != null) {
      sb.append(
          fmt.listItem("预计到达时间: " + FormatUtils.formatDateTime(result.getEstimatedArrivalTime())));
    }
    return sb.toString();
  }

  private String formatTrainingReward(TrainingRewardVO rewards, TextFormat fmt) {
    if (rewards.getSummary() != null && rewards.getMapName() == null) {
      return rewards.getSummary();
    }
    StringBuilder sb = new StringBuilder();
    sb.append(fmt.heading("历练结算"));
    sb.append(rewards.getMapName());
    sb.append(" | ");
    sb.append(FormatUtils.formatMinutes(rewards.getDurationMinutes()));
    sb.append(" | 效率").append(String.format("%.1f", rewards.getEfficiencyMultiplier())).append("x");

    if (rewards.getLevelDecayMultiplier() != null && rewards.getLevelDecayMultiplier() < 1.0) {
      int decayPercent = (int) ((1.0 - rewards.getLevelDecayMultiplier()) * 100);
      sb.append(" | 衰减").append(decayPercent).append("%");
    }

    sb.append("\n");

    if (rewards.getSummary() != null) {
      sb.append(rewards.getSummary()).append("\n");
    }

    if (rewards.getExp() != null && rewards.getExp() > 0) {
      sb.append(fmt.listItem("经验+" + rewards.getExp()));
    }
    if (rewards.getItems() != null && !rewards.getItems().isEmpty()) {
      StringBuilder itemsStr = new StringBuilder();
      for (int i = 0; i < rewards.getItems().size(); i++) {
        Map<String, Object> item = rewards.getItems().get(i);
        itemsStr.append(item.get("name")).append("×").append(item.get("quantity"));
        if (i < rewards.getItems().size() - 1) itemsStr.append(" ");
      }
      sb.append(fmt.listItem("物品：" + itemsStr.toString()));
    }

    return sb.toString();
  }

  private record TemplateResolvers(
      Function<Long, String> itemResolver, Function<Long, String> equipResolver) {}

  private TemplateResolvers resolveTemplates(List<BountyVO> bounties) {
    var allRewards =
        bounties.stream()
            .flatMap(b -> b.rewards() != null ? b.rewards().stream() : Stream.of())
            .toList();

    Set<Long> itemIds = new HashSet<>();
    Set<Long> equipIds = new HashSet<>();
    for (BountyRewardPool reward : allRewards) {
      switch (reward) {
        case BountyRewardPool.RareItem(_, _, _, var tid) -> itemIds.add(tid);
        case BountyRewardPool.EquipmentReward(_, var tid) -> equipIds.add(tid);
        default -> {}
      }
    }

    Map<Long, String> itemNames = new ConcurrentHashMap<>();
    Map<Long, String> equipNames = new ConcurrentHashMap<>();

    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      Future<?> itemFuture =
          executor.submit(
              () -> {
                if (!itemIds.isEmpty()) {
                  itemTemplateRepository
                      .findByIds(new ArrayList<>(itemIds))
                      .forEach(t -> itemNames.put(t.getId(), t.getName()));
                }
              });
      Future<?> equipFuture =
          executor.submit(
              () -> {
                if (!equipIds.isEmpty()) {
                  equipmentTemplateRepository
                      .findByIds(new ArrayList<>(equipIds))
                      .forEach(t -> equipNames.put(t.getId(), t.getName()));
                }
              });
      try {
        itemFuture.get();
        equipFuture.get();
      } catch (Exception e) {
        log.warn("Failed to resolve template names", e);
      }
    }

    return new TemplateResolvers(
        id -> itemNames.getOrDefault(id, "稀有物品"), id -> equipNames.getOrDefault(id, "装备"));
  }

  private String formatBountyList(List<BountyVO> bounties, TextFormat fmt) {
    if (bounties == null || bounties.isEmpty()) {
      return "当前地图没有可接取的悬赏。";
    }
    var resolvers = resolveTemplates(bounties);
    StringBuilder sb = new StringBuilder();
    for (BountyVO b : bounties) {
      sb.append(
          String.format(
              "%s | %s（%s）\n",
              fmt.bold("ID: " + b.id()), b.name(), FormatUtils.formatMinutes(b.durationMinutes())));
      if (b.description() != null && !b.description().isEmpty()) {
        sb.append(String.format("  %s\n", b.description()));
      }
      sb.append(fmt.listItem("推荐等级: " + b.requireLevel()));
      if (b.rewards() != null && !b.rewards().isEmpty()) {
        sb.append(
            fmt.listItem(
                "奖励: "
                    + b.rewards().stream()
                        .map(
                            r -> r.displayText(resolvers.itemResolver(), resolvers.equipResolver()))
                        .collect(Collectors.joining("、"))));
      }
      sb.append(fmt.separator());
    }
    sb.append("「悬赏接取 [ID]」接取，完成后「悬赏结算」。");
    return sb.toString();
  }

  private String formatBountyStatus(BountyStatusVO status, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(fmt.heading("悬赏进行中"));
    sb.append(fmt.listItem("悬赏: " + status.bountyName()));
    if (status.description() != null && !status.description().isEmpty()) {
      sb.append(String.format("  %s\n", status.description()));
    }
    sb.append(
        fmt.listItem(
            String.format(
                    "%s / %s",
                    FormatUtils.formatMinutes(status.minutesElapsed()),
                    FormatUtils.formatMinutes(status.durationMinutes()))
                + (status.minutesRemaining() > 0
                    ? String.format("（剩余 %s）", FormatUtils.formatMinutes(status.minutesRemaining()))
                    : "（已可结算）")));

    if (status.rewards() != null && !status.rewards().isEmpty()) {
      sb.append("\n预计奖励:\n");
      for (BountyRewardItem item : status.rewards()) {
        switch (item) {
          case BountyRewardItem.ItemReward(_, var name, var quantity) ->
              sb.append(fmt.listItem(String.format("%s x%d", name, quantity)));
          case BountyRewardItem.SpiritStonesReward(var amount) ->
              sb.append(fmt.listItem(String.format("%d 灵石", amount)));
          case BountyRewardItem.BeastEggReward(_, var name) ->
              sb.append(fmt.listItem(name + "（兽卵）"));
          case BountyRewardItem.EquipmentRewardItem(_, var name) ->
              sb.append(fmt.listItem(name + "（装备）"));
        }
      }
    }

    sb.append("\n「悬赏结算」结算  「悬赏放弃」放弃");
    return sb.toString();
  }

  private String formatBountyReward(BountyRewardVO reward, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(fmt.heading("悬赏结算"));
    sb.append(fmt.listItem("悬赏: " + reward.bountyName()));
    sb.append(fmt.listItem("地点: " + reward.mapName()));
    sb.append(fmt.listItem("耗时: " + FormatUtils.formatMinutes(reward.durationMinutes())));

    if (reward.eventDescription() != null) {
      sb.append("\n").append(fmt.heading("途中事件"));
      sb.append(reward.eventDescription()).append("\n");
    }

    if (reward.rewardDescription() != null) {
      sb.append("\n").append(reward.rewardDescription()).append("\n");
    }

    if (reward.items() != null && !reward.items().isEmpty()) {
      sb.append("物品:\n");
      for (BountyRewardItem item : reward.items()) {
        switch (item) {
          case BountyRewardItem.ItemReward(_, var name, var quantity) ->
              sb.append(fmt.listItem(String.format("%s x%d", name, quantity)));
          case BountyRewardItem.BeastEggReward(_, var name) ->
              sb.append(fmt.listItem(name + " x1"));
          case BountyRewardItem.EquipmentRewardItem(_, var name) ->
              sb.append(fmt.listItem(name + " x1"));
          default -> {}
        }
      }
    }
    return sb.toString();
  }

  private String formatMapList(List<MapInfoVO> maps, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(fmt.heading("世界地图")).append("\n");
    for (MapInfoVO map : maps) {
      sb.append(
          String.format(
              "%s%s (等级: %d)\n",
              fmt.bold(map.getMapTypeName()), map.getName(), map.getLevelRequirement()));
      if (map.getDescription() != null && !map.getDescription().isEmpty()) {
        sb.append(fmt.listItem("描述: " + map.getDescription()));
      }
      if (map.getMonsters() != null && !map.getMonsters().isEmpty()) {
        sb.append(
            fmt.listItem(
                "怪物: "
                    + map.getMonsters().stream()
                        .map(
                            m ->
                                m.getName()
                                    + "{"
                                    + m.getTypeName()
                                    + " Lv"
                                    + m.getBaseLevel()
                                    + "}")
                        .collect(Collectors.joining("、"))));
      }
      if (map.getAdjacentMapNames() != null && !map.getAdjacentMapNames().isEmpty()) {
        sb.append(fmt.listItem("相邻: " + String.join(", ", map.getAdjacentMapNames())));
      }
      sb.append(fmt.separator());
    }
    sb.append("使用「前往 [地图名]」开始旅行。");
    return sb.toString();
  }

  private String formatCurrentMap(MapInfoVO map, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(
        String.format(
            "%s%s (推荐等级: %d)\n",
            fmt.bold(map.getMapTypeName()), map.getName(), map.getLevelRequirement()));
    if (map.getDescription() != null && !map.getDescription().isEmpty()) {
      sb.append(String.format("\n%s\n", map.getDescription()));
    }
    if (map.getMonsters() != null && !map.getMonsters().isEmpty()) {
      sb.append("\n").append(fmt.heading("遇怪列表"));
      for (MapInfoVO.MonsterInfoVO monster : map.getMonsters()) {
        String countRange =
            monster.getMinCount() == monster.getMaxCount()
                ? String.valueOf(monster.getMinCount())
                : monster.getMinCount() + "~" + monster.getMaxCount();
        sb.append(
            fmt.listItem(
                String.format(
                    "%s [%s] Lv%d  权重:%d  数量:%s",
                    monster.getName(),
                    monster.getTypeName(),
                    monster.getBaseLevel(),
                    monster.getWeight(),
                    countRange)));
      }
    }
    if (map.getAdjacentMapNames() != null && !map.getAdjacentMapNames().isEmpty()) {
      sb.append("\n").append(fmt.heading("相邻地图"));
      for (int i = 0; i < map.getAdjacentMapNames().size(); i++) {
        String adjName = map.getAdjacentMapNames().get(i);
        Integer travelTime =
            map.getNeighbors() != null && i < map.getNeighbors().size()
                ? map.getNeighbors().get(i).cost()
                : null;
        String timeStr =
            travelTime != null ? " (" + FormatUtils.formatMinutes(travelTime) + ")" : "";
        sb.append(fmt.listItem(adjName + timeStr));
      }
    }
    sb.append("\n使用「前往 [地图名]」开始旅行。");
    return sb.toString();
  }

  @Override
  public String groupName() {
    return "地图";
  }

  @Override
  public String groupDescription() {
    return "地图探索、历练、悬赏";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("地图", "查看当前地图信息", "地图"),
        new CommandEntry("地图列表", "查看所有地图", "地图列表"),
        new CommandEntry("前往 {{地图名}}", "前往相邻地图", "前往 青木林"),
        new CommandEntry("历练", "开始在地图历练", "历练"),
        new CommandEntry("历练结算", "结束历练并结算收益", "历练结算"),
        new CommandEntry("悬赏", "查看当前悬赏状态", "悬赏"),
        new CommandEntry("悬赏列表", "查看可接取悬赏", "悬赏列表"),
        new CommandEntry("悬赏接取 {{编号}}", "接取悬赏任务", "悬赏接取 1"),
        new CommandEntry("悬赏结算", "完成并结算悬赏", "悬赏结算"),
        new CommandEntry("悬赏放弃", "放弃当前悬赏", "悬赏放弃"));
  }
}
