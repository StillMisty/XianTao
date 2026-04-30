package top.stillmisty.xiantao.handle.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.bounty.vo.BountyRewardVO;
import top.stillmisty.xiantao.domain.bounty.vo.BountyVO;
import top.stillmisty.xiantao.domain.map.vo.MapInfoVO;
import top.stillmisty.xiantao.domain.map.vo.TrainingRewardVO;
import top.stillmisty.xiantao.domain.map.vo.TravelResultVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MapCommandHandler {

    private final MapService mapService;
    private final TravelService travelService;
    private final TrainingService trainingService;
    private final BountyService bountyService;

    public String handleGoTo(PlatformType platform, String openId, String mapName) {
        log.debug("处理前往请求 - Platform: {}, OpenId: {}, MapName: {}", platform, openId, mapName);
        return switch (travelService.startTravel(platform, openId, mapName)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> vo.isSuccess() ? formatTravelResult(vo) : vo.getMessage();
        };
    }

    public String handleTraining(PlatformType platform, String openId) {
        log.debug("处理历练请求 - Platform: {}, OpenId: {}", platform, openId);
        return switch (trainingService.startTraining(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) ->
                    vo.isSuccess() ? String.format("开始在%s历练，使用「历练结算」结算收益。", vo.getMapName()) : vo.getMessage();
        };
    }

    public String handleEndTraining(PlatformType platform, String openId) {
        log.debug("处理结束历练请求 - Platform: {}, OpenId: {}", platform, openId);
        return switch (trainingService.endTraining(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatTrainingReward(vo);
        };
    }

    public String handleMapList(PlatformType platform, String openId) {
        log.debug("处理地图列表查询 - Platform: {}, OpenId: {}", platform, openId);
        return switch (mapService.getAllMaps(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> vo.isEmpty() ? "暂无可用地图" : formatMapList(vo);
        };
    }

    // ===================== 悬赏命令 =====================

    public String handleBountyList(PlatformType platform, String openId) {
        log.debug("处理悬赏列表 - Platform: {}, OpenId: {}", platform, openId);
        return switch (bountyService.listBounties(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatBountyList(vo);
        };
    }

    public String handleStartBounty(PlatformType platform, String openId, String bountyId) {
        log.debug("处理接取悬赏 - Platform: {}, OpenId: {}, BountyId: {}", platform, openId, bountyId);
        try {
            Long id = Long.parseLong(bountyId);
            return switch (bountyService.startBounty(platform, openId, id)) {
                case ServiceResult.Failure(var msg) -> msg;
                case ServiceResult.Success(var msg) -> msg;
            };
        } catch (NumberFormatException e) {
            return "请输入有效的悬赏编号";
        }
    }

    public String handleCompleteBounty(PlatformType platform, String openId) {
        log.debug("处理完成悬赏 - Platform: {}, OpenId: {}", platform, openId);
        return switch (bountyService.completeBounty(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatBountyReward(vo);
        };
    }

    public String handleAbandonBounty(PlatformType platform, String openId) {
        log.debug("处理放弃悬赏 - Platform: {}, OpenId: {}", platform, openId);
        return switch (bountyService.abandonBounty(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
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
                sb.append(String.format("  骰点: %d/20\n", result.getD20Roll()));
                sb.append(String.format("  事件: %s\n", result.getEventType().getName()));
                if (result.getEventDescription() != null) {
                    sb.append(String.format("  描述: %s\n", result.getEventDescription()));
                }
            }
        } else {
            if (result.getEstimatedArrivalTime() != null) {
                sb.append(String.format("\n预计到达时间: %s", result.getEstimatedArrivalTime()));
            }
        }
        return sb.toString();
    }

    private String formatTrainingReward(TrainingRewardVO rewards) {
        if (rewards.getSummary() != null && rewards.getMapName() == null) {
            return rewards.getSummary();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("【历练结算】\n");
        sb.append(String.format("地图: %s\n", rewards.getMapName()));
        sb.append(String.format("历练时长: %d 分钟\n", rewards.getDurationMinutes()));
        sb.append(String.format("效率倍率: %.2fx\n\n", rewards.getEfficiencyMultiplier()));
        if (rewards.getExp() != null && rewards.getExp() > 0) {
            sb.append(String.format("经验: +%d\n", rewards.getExp()));
        }
        if (rewards.getItems() != null && !rewards.getItems().isEmpty()) {
            sb.append("物品:\n");
            for (Map<String, Object> item : rewards.getItems()) {
                sb.append(String.format("  %s x%d\n", item.get("name"), item.get("quantity")));
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
            sb.append(String.format("ID: %d | %s（%d 分钟）\n", b.id(), b.name(), b.durationMinutes()));
            if (b.description() != null && !b.description().isEmpty()) {
                sb.append(String.format("  %s\n", b.description()));
            }
            sb.append(String.format("  要求等级: %d\n", b.requireLevel()));
            sb.append("\n");
        }
        sb.append("使用「接悬赏 [ID]」接取，完成后「交悬赏」结算。");
        return sb.toString();
    }

    private String formatBountyReward(BountyRewardVO reward) {
        StringBuilder sb = new StringBuilder();
        sb.append("【悬赏完成】\n");
        sb.append(String.format("悬赏: %s\n", reward.bountyName()));
        sb.append(String.format("地点: %s\n", reward.mapName()));
        sb.append(String.format("耗时: %d 分钟\n", reward.durationMinutes()));

        if (reward.eventDescription() != null) {
            sb.append(String.format("\n【途中事件】%s\n", reward.eventDescription()));
        }

        if (reward.rewardDescription() != null) {
            sb.append("\n").append(reward.rewardDescription()).append("\n");
        }

        if (reward.items() != null && !reward.items().isEmpty()) {
            sb.append("物品:\n");
            for (Map<String, Object> item : reward.items()) {
                sb.append(String.format("  %s x%d\n", item.get("name"), item.get("quantity")));
            }
        }
        return sb.toString();
    }

    private String formatMapList(List<MapInfoVO> maps) {
        StringBuilder sb = new StringBuilder();
        sb.append("【世界地图】\n\n");
        for (MapInfoVO map : maps) {
            sb.append(String.format("【%s】%s (等级: %d)\n", map.getMapTypeName(), map.getName(), map.getLevelRequirement()));
            if (map.getDescription() != null && !map.getDescription().isEmpty()) {
                sb.append(String.format("  描述: %s\n", map.getDescription()));
            }
            if (map.getAdjacentMapNames() != null && !map.getAdjacentMapNames().isEmpty()) {
                sb.append("  相邻: ");
                sb.append(String.join(", ", map.getAdjacentMapNames()));
                sb.append("\n");
            }
            sb.append("\n");
        }
        sb.append("使用「前往 [地图名]」开始旅行。");
        return sb.toString();
    }
}