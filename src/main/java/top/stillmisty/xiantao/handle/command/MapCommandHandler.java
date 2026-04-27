package top.stillmisty.xiantao.handle.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.map.vo.ExplorationResultVO;
import top.stillmisty.xiantao.domain.map.vo.MapInfoVO;
import top.stillmisty.xiantao.domain.map.vo.TrainingRewardVO;
import top.stillmisty.xiantao.domain.map.vo.TravelResultVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.*;

import java.util.List;
import java.util.Map;

/**
 * 地图命令处理器（纯 View 层）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MapCommandHandler {

    private final MapService mapService;
    private final TravelService travelService;
    private final TrainingService trainingService;
    private final ExplorationService explorationService;

    public String handleGoTo(PlatformType platform, String openId, String mapName, boolean forceRealTime) {
        log.debug("处理前往请求 - Platform: {}, OpenId: {}, MapName: {}", platform, openId, mapName);
        return switch (travelService.startTravel(platform, openId, mapName, forceRealTime)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> vo.isSuccess() ? formatTravelResult(vo) : vo.getMessage();
        };
    }

    public String handleTraining(PlatformType platform, String openId) {
        log.debug("处理历练请求 - Platform: {}, OpenId: {}", platform, openId);
        return switch (trainingService.startTraining(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) ->
                    vo.isSuccess() ? String.format("开始在%s历练，使用「历练结束」结算收益。", vo.getMapName()) : vo.getMessage();
        };
    }

    public String handleEndTraining(PlatformType platform, String openId) {
        log.debug("处理结束历练请求 - Platform: {}, OpenId: {}", platform, openId);
        return switch (trainingService.endTraining(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatTrainingReward(vo);
        };
    }

    public String handleExplore(PlatformType platform, String openId) {
        log.debug("处理探索请求 - Platform: {}, OpenId: {}", platform, openId);
        return switch (explorationService.exploreCurrentArea(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatExplorationResult(vo);
        };
    }

    public String handleMapList(PlatformType platform, String openId) {
        log.debug("处理地图列表查询 - Platform: {}, OpenId: {}", platform, openId);
        return switch (mapService.getAllMaps(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> vo.isEmpty() ? "暂无可用地图" : formatMapList(vo);
        };
    }

    // ===================== 文本格式化方法 =====================

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
        StringBuilder sb = new StringBuilder();
        sb.append("【历练结算】\n");
        sb.append(String.format("地图: %s\n", rewards.getMapName()));
        sb.append(String.format("历练时长: %d 分钟\n", rewards.getDurationMinutes()));
        sb.append(String.format("效率倍率: %.2fx\n\n", rewards.getEfficiencyMultiplier()));
        if (rewards.getCoins() != null && rewards.getCoins() > 0) {
            sb.append(String.format("铜币: +%d\n", rewards.getCoins()));
        }
        if (rewards.getSpiritStones() != null && rewards.getSpiritStones() > 0) {
            sb.append(String.format("灵石: +%d\n", rewards.getSpiritStones()));
        }
        if (rewards.getItems() != null && !rewards.getItems().isEmpty()) {
            sb.append("物品:\n");
            for (Map<String, Object> item : rewards.getItems()) {
                sb.append(String.format("  %s x%d\n", item.get("name"), item.get("quantity")));
            }
        }
        return sb.toString();
    }

    private String formatExplorationResult(ExplorationResultVO result) {
        StringBuilder sb = new StringBuilder();
        sb.append("【探索结果】\n");
        sb.append(String.format("地图: %s\n", result.getMapName()));
        sb.append(String.format("智慧值: %d\n", result.getWisdom()));
        sb.append(String.format("消耗: %d 分钟 / %d 体力\n\n", result.getTimeCostMinutes(), result.getStaminaCost()));
        if (result.getEventType() != null) {
            sb.append(String.format("【%s】\n", result.getEventType()));
        }
        if (result.getDescription() != null) {
            sb.append(result.getDescription()).append("\n\n");
        }
        if (result.getFoundItems() != null && !result.getFoundItems().isEmpty()) {
            sb.append("【发现物品】\n");
            for (Map<String, Object> item : result.getFoundItems()) {
                sb.append(String.format("  %s x%d\n", item.get("name"), item.get("quantity")));
            }
            sb.append("\n");
        }
        if (result.getRecipeName() != null) {
            sb.append(String.format("【发现配方】%s\n\n", result.getRecipeName()));
        }
        if (result.getExpGained() != null && result.getExpGained() > 0) {
            sb.append(String.format("【获得经验】+%d\n", result.getExpGained()));
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
        sb.append("使用「前往 [地图名]」开始旅行，或使用「探索」探索当前区域。");
        return sb.toString();
    }
}
