package top.stillmisty.xiantao.handle.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.map.vo.TrainingRewardVO;
import top.stillmisty.xiantao.domain.map.vo.ExplorationResultVO;
import top.stillmisty.xiantao.domain.map.vo.MapInfoVO;
import top.stillmisty.xiantao.domain.map.vo.TravelResultVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.service.*;

import java.util.List;
import java.util.Map;

/**
 * 地图命令处理器
 * 集中处理跨平台的地图相关命令逻辑
 */
@Slf4j
@Component
public class MapCommandHandler extends BaseCommandHandler {

    private final MapService mapService;
    private final TravelService travelService;
    private final TrainingService trainingService;
    private final ExplorationService explorationService;
    private final ItemService itemService;

    public MapCommandHandler(
            UserAuthService userAuthService, ItemService itemService, MapService mapService, TravelService travelService,
            TrainingService trainingService, ExplorationService explorationService,
            UserService userService
    ) {
        super(userAuthService, userService);
        this.itemService = itemService;
        this.mapService = mapService;
        this.travelService = travelService;
        this.trainingService = trainingService;
        this.explorationService = explorationService;
    }

    /**
     * 处理前往命令
     *
     * @param platform      平台类型
     * @param openId        平台用户 ID
     * @param mapName       目标地图名称
     * @param forceRealTime 是否强制使用真实时间模式
     * @return 旅行结果消息
     */
    public String handleGoTo(PlatformType platform, String openId, String mapName, boolean forceRealTime) {
        log.debug(
                "处理前往请求 - Platform: {}, OpenId: {}, MapName: {}, ForceRealTime: {}",
                platform, openId, mapName, forceRealTime
        );

        // 验证用户身份
        var authResult = authenticate(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        // 获取并验证用户状态
        var characterStatus = itemService.getCharacterStatus(authResult.userId());
        if (characterStatus == null || !characterStatus.isSuccess()) {
            return "获取用户状态失败";
        }

        String idleError = validateUserStatus(authResult.userId(), UserStatus.IDLE);
        if (idleError != null) {
            return idleError;
        }

        // 获取目标地图
        var targetMapOpt = mapService.getMapInfoByName(mapName);
        if (targetMapOpt.isEmpty()) {
            return String.format("未找到地图: %s", mapName);
        }

        MapInfoVO targetMap = targetMapOpt.get();
        Long currentMapId = characterStatus.getLocationId();

        // 验证旅行
        String validationError = mapService.validateTravel(currentMapId, targetMap.getId(), characterStatus.getLevel());
        if (validationError != null) {
            return validationError;
        }

        // 计算需要的体力
        int travelTime = targetMap.getTravelTimeMinutes();
        int staminaCost = travelTime * TravelService.STAMINA_COST_PER_MINUTE;

        // 判断是否使用体力模式
        // 优先使用体力，体力不足时才使用真实时间
        boolean useStamina = !forceRealTime && hasEnoughStamina(characterStatus, staminaCost);

        // 开始旅行
        TravelResultVO result = travelService.startTravel(authResult.userId(), targetMap.getId(), useStamina);

        if (!result.isSuccess()) {
            return result.getMessage();
        }

        return formatTravelResult(result);
    }

    /**
     * 检查体力是否足够
     * TODO: 实现体力系统后需要修改此方法
     */
    private boolean hasEnoughStamina(top.stillmisty.xiantao.domain.item.vo.CharacterStatusResult characterStatus, int staminaCost) {
        // 临时返回 true，需要实现体力系统后修改
        return true;
    }

    /**
     * 处理历练命令
     *
     * @param platform 平台类型
     * @param openId   平台用户 ID
     * @return 历练结果消息
     */
    public String handleTraining(PlatformType platform, String openId) {
        log.debug("处理历练请求 - Platform: {}, OpenId: {}", platform, openId);

        // 验证用户身份和状态
        var authResult = authenticateAndValidateStatus(platform, openId, UserStatus.IDLE);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        // TODO: 这里应该是开始历练，但目前代码是结算历练
        // 暂时保持原有逻辑，后续需要重构为开始历练+消耗体力
        
        // 计算历练奖励
        TrainingRewardVO rewards = trainingService.calculateTrainingRewards(authResult.userId());

        // 应用奖励
        if (rewards.getCoins() != null && rewards.getCoins() > 0 ||
                rewards.getSpiritStones() != null && rewards.getSpiritStones() > 0 ||
                rewards.getItems() != null && !rewards.getItems().isEmpty()) {

            boolean applied = trainingService.applyTrainingRewards(authResult.userId(), rewards);
            if (applied) {
                return formatTrainingReward(rewards);
            }
        }

        return rewards.getSummary();
    }

    /**
     * 处理结束历练命令
     *
     * @param platform 平台类型
     * @param openId   平台用户 ID
     * @return 历练结算结果消息
     */
    public String handleEndTraining(PlatformType platform, String openId) {
        log.debug("处理结束历练请求 - Platform: {}, OpenId: {}", platform, openId);

        // 验证用户身份
        var authResult = authenticate(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        // 结束历练并应用奖励
        TrainingRewardVO rewards = trainingService.endTraining(authResult.userId());

        return formatTrainingReward(rewards);
    }

    /**
     * 处理探索命令
     *
     * @param platform 平台类型
     * @param openId   平台用户 ID
     * @return 探索结果消息
     */
    public String handleExplore(PlatformType platform, String openId) {
        log.debug("处理探索请求 - Platform: {}, OpenId: {}", platform, openId);

        // 验证用户身份和状态
        var authResult = authenticateAndValidateStatus(platform, openId, UserStatus.IDLE);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        // 执行探索
        ExplorationResultVO result = explorationService.exploreCurrentArea(authResult.userId());

        return formatExplorationResult(result);
    }

    /**
     * 格式化地图列表
     */
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

    /**
     * 处理地图列表命令
     *
     * @param platform 平台类型
     * @param openId   平台用户 ID
     * @return 地图列表信息
     */
    public String handleMapList(PlatformType platform, String openId) {
        log.debug("处理地图列表查询 - Platform: {}, OpenId: {}", platform, openId);

        var authResult = authenticate(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        List<MapInfoVO> allMaps = mapService.getAllMaps();

        if (allMaps.isEmpty()) {
            return "暂无可用地图";
        }

        return formatMapList(allMaps);
    }

    // ===================== 响应格式化方法 =====================

    /**
     * 格式化旅行结果
     */
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

    /**
     * 格式化历练奖励
     */
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

    /**
     * 格式化探索结果
     */
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
}
