package top.stillmisty.xiantao.handle.command;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.bounty.BountyRewardItem;
import top.stillmisty.xiantao.domain.bounty.BountyRewardPool;
import top.stillmisty.xiantao.domain.bounty.vo.BountyRewardVO;
import top.stillmisty.xiantao.domain.bounty.vo.BountyStatusVO;
import top.stillmisty.xiantao.domain.bounty.vo.BountyVO;
import top.stillmisty.xiantao.domain.map.vo.MapInfoVO;
import top.stillmisty.xiantao.domain.map.vo.TrainingRewardVO;
import top.stillmisty.xiantao.domain.map.vo.TravelResultVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.infrastructure.util.FormatUtils;
import top.stillmisty.xiantao.service.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MapCommandHandler {

    private final MapService mapService;
    private final TravelService travelService;
    private final TrainingService trainingService;
    private final BountyService bountyService;

    public String handleGoTo(
        PlatformType platform,
        String openId,
        String mapName
    ) {
        log.debug(
            "处理前往请求 - Platform: {}, OpenId: {}, MapName: {}",
            platform,
            openId,
            mapName
        );
        return switch (travelService.startTravel(platform, openId, mapName)) {
            case ServiceResult.Failure(var code, var msg) -> msg;
            case ServiceResult.Success(var vo) -> vo.isSuccess()
                ? formatTravelResult(vo)
                : vo.getMessage();
        };
    }

    public String handleTraining(PlatformType platform, String openId) {
        log.debug("处理历练请求 - Platform: {}, OpenId: {}", platform, openId);
        return switch (trainingService.startTraining(platform, openId)) {
            case ServiceResult.Failure(var code, var msg) -> msg;
            case ServiceResult.Success(var vo) -> vo.isSuccess()
                ? String.format(
                      "开始在%s历练，使用「历练结算」结算收益。",
                      vo.getMapName()
                  )
                : vo.getMessage();
        };
    }

    public String handleEndTraining(PlatformType platform, String openId) {
        log.debug(
            "处理结束历练请求 - Platform: {}, OpenId: {}",
            platform,
            openId
        );
        return switch (trainingService.endTraining(platform, openId)) {
            case ServiceResult.Failure(var code, var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatTrainingReward(vo);
        };
    }

    public String handleMapList(PlatformType platform, String openId) {
        log.debug(
            "处理地图列表查询 - Platform: {}, OpenId: {}",
            platform,
            openId
        );
        return switch (mapService.getAllMaps(platform, openId)) {
            case ServiceResult.Failure(var code, var msg) -> msg;
            case ServiceResult.Success(var vo) -> vo.isEmpty()
                ? "暂无可用地图"
                : formatMapList(vo);
        };
    }

    public String handleCurrentMap(PlatformType platform, String openId) {
        log.debug(
            "处理当前地图查询 - Platform: {}, OpenId: {}",
            platform,
            openId
        );
        return switch (mapService.getCurrentMapInfo(platform, openId)) {
            case ServiceResult.Failure(var code, var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatCurrentMap(vo);
        };
    }

    // ===================== 悬赏命令 =====================

    public String handleBountyList(PlatformType platform, String openId) {
        log.debug("处理悬赏列表 - Platform: {}, OpenId: {}", platform, openId);
        return switch (bountyService.listBounties(platform, openId)) {
            case ServiceResult.Failure(var code, var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatBountyList(vo);
        };
    }

    public String handleBountyStatus(PlatformType platform, String openId) {
        log.debug("处理悬赏状态 - Platform: {}, OpenId: {}", platform, openId);
        return switch (bountyService.getBountyStatus(platform, openId)) {
            case ServiceResult.Failure(var code, var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatBountyStatus(vo);
        };
    }

    public String handleStartBounty(
        PlatformType platform,
        String openId,
        String bountyId
    ) {
        log.debug(
            "处理接取悬赏 - Platform: {}, OpenId: {}, BountyId: {}",
            platform,
            openId,
            bountyId
        );
        try {
            Long id = Long.parseLong(bountyId);
            return switch (bountyService.startBounty(platform, openId, id)) {
                case ServiceResult.Failure(var code, var msg) -> msg;
                case ServiceResult.Success(var msg) -> msg;
            };
        } catch (NumberFormatException e) {
            return "请输入有效的悬赏编号";
        }
    }

    public String handleCompleteBounty(PlatformType platform, String openId) {
        log.debug("处理完成悬赏 - Platform: {}, OpenId: {}", platform, openId);
        return switch (bountyService.completeBounty(platform, openId)) {
            case ServiceResult.Failure(var code, var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatBountyReward(vo);
        };
    }

    public String handleAbandonBounty(PlatformType platform, String openId) {
        log.debug("处理放弃悬赏 - Platform: {}, OpenId: {}", platform, openId);
        return switch (bountyService.abandonBounty(platform, openId)) {
            case ServiceResult.Failure(var code, var msg) -> msg;
            case ServiceResult.Success(var msg) -> msg;
        };
    }

    // ===================== 文本格式化 =====================

    private String formatTravelResult(TravelResultVO result) {
        StringBuilder sb = new StringBuilder();
        sb.append(result.getMessage()).append("\n");
        if (result.isArrived()) {
            if (result.getEventType() != null) {
                sb.append("\n【路途事件】\n");
                sb.append(
                    String.format("  骰点: %d/20\n", result.getD20Roll())
                );
                sb.append(
                    String.format(
                        "  事件: %s\n",
                        result.getEventType().getName()
                    )
                );
                if (result.getEventDescription() != null) {
                    sb.append(
                        String.format(
                            "  描述: %s\n",
                            result.getEventDescription()
                        )
                    );
                }
            }
        } else {
            if (result.getEstimatedArrivalTime() != null) {
                sb.append(
                    String.format(
                        "\n预计到达时间: %s",
                        FormatUtils.formatDateTime(result.getEstimatedArrivalTime())
                    )
                );
            }
        }
        return sb.toString();
    }

    private String formatTrainingReward(TrainingRewardVO rewards) {
        if (rewards.getSummary() != null && rewards.getMapName() == null) {
            return rewards.getSummary();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("【历练结算】");
        sb.append(rewards.getMapName());
        sb.append(" | ");
        sb.append(FormatUtils.formatMinutes(rewards.getDurationMinutes()));
        sb
            .append(" | 效率")
            .append(String.format("%.1f", rewards.getEfficiencyMultiplier()))
            .append("x");

        // 显示等级衰减（如果有）
        if (
            rewards.getLevelDecayMultiplier() != null &&
            rewards.getLevelDecayMultiplier() < 1.0
        ) {
            int decayPercent = (int) ((1.0 -
                    rewards.getLevelDecayMultiplier()) *
                100);
            sb.append(" | 衰减").append(decayPercent).append("%");
        }

        sb.append("\n");

        // 战斗统计（从summary中提取）
        if (rewards.getSummary() != null) {
            sb.append(rewards.getSummary()).append("\n");
        }

        // 经验和物品
        if (rewards.getExp() != null && rewards.getExp() > 0) {
            sb.append("经验+").append(rewards.getExp());
        }
        if (rewards.getItems() != null && !rewards.getItems().isEmpty()) {
            if (rewards.getExp() != null && rewards.getExp() > 0) {
                sb.append(" | ");
            }
            for (int i = 0; i < rewards.getItems().size(); i++) {
                Map<String, Object> item = rewards.getItems().get(i);
                sb
                    .append(item.get("name"))
                    .append("×")
                    .append(item.get("quantity"));
                if (i < rewards.getItems().size() - 1) sb.append(" ");
            }
        }

        return sb.toString();
    }

    private String formatBountyList(List<BountyVO> bounties) {
        if (bounties == null || bounties.isEmpty()) {
            return "当前地图没有可接取的悬赏。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("【悬赏列表】\n\n");
        for (BountyVO b : bounties) {
            sb.append(
                String.format(
                    "ID: %d | %s（%s）\n",
                    b.id(),
                    b.name(),
                    FormatUtils.formatMinutes(b.durationMinutes())
                )
            );
            if (b.description() != null && !b.description().isEmpty()) {
                sb.append(String.format("  %s\n", b.description()));
            }
            sb.append(String.format("  要求等级: %d\n", b.requireLevel()));
            if (b.rewards() != null && !b.rewards().isEmpty()) {
                sb.append("  奖励: ");
                sb.append(
                    b.rewards().stream()
                        .map(BountyRewardPool::displayText)
                        .collect(Collectors.joining("、"))
                );
                sb.append("\n");
            }
            sb.append("\n");
        }
        sb.append("「悬赏接取 [ID]」接取，完成后「悬赏结算」。");
        return sb.toString();
    }

    private String formatBountyStatus(BountyStatusVO status) {
        StringBuilder sb = new StringBuilder();
        sb.append("【悬赏进行中】\n");
        sb.append(String.format("悬赏: %s\n", status.bountyName()));
        if (status.description() != null && !status.description().isEmpty()) {
            sb.append(String.format("  %s\n", status.description()));
        }
        sb.append(
            String.format(
                "%s / %s",
                FormatUtils.formatMinutes(status.minutesElapsed()),
                FormatUtils.formatMinutes(status.durationMinutes())
            )
        );
        if (status.minutesRemaining() > 0) {
            sb.append(
                String.format("（剩余 %s）", FormatUtils.formatMinutes(status.minutesRemaining()))
            );
        } else {
            sb.append("（已可结算）");
        }
        sb.append("\n");

        if (status.rewards() != null && !status.rewards().isEmpty()) {
            sb.append("\n预计奖励:\n");
            for (BountyRewardItem item : status.rewards()) {
                switch (item) {
                    case BountyRewardItem.ItemReward(_, var name, var quantity) ->
                            sb.append(String.format("  %s x%d\n", name, quantity));
                    case BountyRewardItem.SpiritStonesReward(var amount) ->
                            sb.append(String.format("  %d 灵石\n", amount));
                    case BountyRewardItem.BeastEggReward(_, var name) ->
                            sb.append(String.format("  %s（灵兽卵）\n", name));
                }
            }
        }

        sb.append("\n「悬赏结算」结算  「悬赏放弃」放弃");
        return sb.toString();
    }

    private String formatBountyReward(BountyRewardVO reward) {
        StringBuilder sb = new StringBuilder();
        sb.append("【悬赏结算】\n");
        sb.append(String.format("悬赏: %s\n", reward.bountyName()));
        sb.append(String.format("地点: %s\n", reward.mapName()));
        sb.append(String.format("耗时: %s\n", FormatUtils.formatMinutes(reward.durationMinutes())));

        if (reward.eventDescription() != null) {
            sb.append(
                String.format("\n【途中事件】%s\n", reward.eventDescription())
            );
        }

        if (reward.rewardDescription() != null) {
            sb.append("\n").append(reward.rewardDescription()).append("\n");
        }

        if (reward.items() != null && !reward.items().isEmpty()) {
            sb.append("物品:\n");
            for (BountyRewardItem item : reward.items()) {
                switch (item) {
                    case BountyRewardItem.ItemReward(_, var name, var quantity) ->
                            sb.append(String.format("  %s x%d\n", name, quantity));
                    case BountyRewardItem.BeastEggReward(_, var name) ->
                            sb.append(String.format("  %s x1\n", name));
                    default -> {}
                }
            }
        }
        return sb.toString();
    }

    private String formatMapList(List<MapInfoVO> maps) {
        StringBuilder sb = new StringBuilder();
        sb.append("【世界地图】\n\n");
        for (MapInfoVO map : maps) {
            sb.append(
                String.format(
                    "【%s】%s (等级: %d)\n",
                    map.getMapTypeName(),
                    map.getName(),
                    map.getLevelRequirement()
                )
            );
            if (
                map.getDescription() != null && !map.getDescription().isEmpty()
            ) {
                sb.append(String.format("  描述: %s\n", map.getDescription()));
            }
            if (
                map.getMonsters() != null && !map.getMonsters().isEmpty()
            ) {
                sb.append("  怪物: ");
                sb.append(
                    map.getMonsters().stream()
                        .map(m -> m.getName() + "{" + m.getTypeName() + " Lv" + m.getBaseLevel() + "}")
                        .collect(Collectors.joining("、"))
                );
                sb.append("\n");
            }
            if (
                map.getAdjacentMapNames() != null &&
                !map.getAdjacentMapNames().isEmpty()
            ) {
                sb.append("  相邻: ");
                sb.append(String.join(", ", map.getAdjacentMapNames()));
                sb.append("\n");
            }
            sb.append("\n");
        }
        sb.append("使用「前往 [地图名]」开始旅行。");
        return sb.toString();
    }

    private String formatCurrentMap(MapInfoVO map) {
        StringBuilder sb = new StringBuilder();
        sb.append(
            String.format(
                "【%s】%s (要求等级: %d)\n",
                map.getMapTypeName(),
                map.getName(),
                map.getLevelRequirement()
            )
        );
        if (map.getDescription() != null && !map.getDescription().isEmpty()) {
            sb.append(String.format("\n%s\n", map.getDescription()));
        }
        if (
            map.getMonsters() != null && !map.getMonsters().isEmpty()
        ) {
            sb.append("\n【遇怪列表】\n");
            for (MapInfoVO.MonsterInfoVO monster : map.getMonsters()) {
                String countRange = monster.getMinCount() == monster.getMaxCount()
                    ? String.valueOf(monster.getMinCount())
                    : monster.getMinCount() + "~" + monster.getMaxCount();
                sb.append(
                    String.format(
                        "  %s [%s] Lv%d  权重:%d  数量:%s\n",
                        monster.getName(),
                        monster.getTypeName(),
                        monster.getBaseLevel(),
                        monster.getWeight(),
                        countRange
                    )
                );
            }
        }
        if (
            map.getAdjacentMapNames() != null &&
            !map.getAdjacentMapNames().isEmpty()
        ) {
            sb.append("\n【相邻地图】\n");
            for (int i = 0; i < map.getAdjacentMapNames().size(); i++) {
                String adjName = map.getAdjacentMapNames().get(i);
                Integer travelTime = map.getNeighbors() != null && i < map.getNeighbors().size()
                    ? map.getNeighbors().get(i).cost()
                    : null;
                String timeStr = travelTime != null
                    ? " (" + FormatUtils.formatMinutes(travelTime) + ")"
                    : "";
                sb.append(String.format("  %s%s\n", adjName, timeStr));
            }
        }
        sb.append("\n使用「前往 [地图名]」开始旅行。");
        return sb.toString();
    }
}
