package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.map.vo.ExplorationResultVO;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 探索服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExplorationService {

    private final UserRepository userRepository;
    private final MapNodeRepository mapNodeRepository;
    private final ExplorationDescriptionFunction explorationDescriptionFunction;

    // 探索配置
    private static final int EXPLORATION_TIME_COST_MINUTES = 10;
    private static final int EXPLORATION_STAMINA_COST = 20;

    /**
     * 探索当前区域
     *
     * @param userId 用户 ID
     * @return 探索结果
     */
    public ExplorationResultVO exploreCurrentArea(Long userId) {
        // 获取用户
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ExplorationResultVO.builder()
                    .userId(userId)
                    .description("用户不存在")
                    .build();
        }

        User user = userOpt.get();

        // 检查用户状态
        if (user.getStatus() != UserStatus.IDLE) {
            String statusName = user.getStatus() != null ? user.getStatus().getName() : "未知";
            return ExplorationResultVO.builder()
                    .userId(userId)
                    .description(String.format("您当前处于 %s 状态，无法探索", statusName))
                    .build();
        }

        // 获取当前地图
        Optional<MapNode> mapOpt = mapNodeRepository.findById(user.getLocationId());
        if (mapOpt.isEmpty()) {
            return ExplorationResultVO.builder()
                    .userId(userId)
                    .description("当前地图不存在")
                    .build();
        }

        MapNode mapNode = mapOpt.get();

        // 生成探索结果
        ExplorationResultVO result = generateExplorationResult(user, mapNode);

        // 添加经验值
        long expGained = calculateExpGained(user.getStatWis(), mapNode.getLevelRequirement());
        user.addExp(expGained);
        result.setExpGained(expGained);

        // TODO: 添加物品到背包
        if (result.getFoundItems() != null && !result.getFoundItems().isEmpty()) {
            for (Map<String, Object> item : result.getFoundItems()) {
                log.info("用户 {} 探索获得物品: {} x{}", userId, item.get("name"), item.get("quantity"));
            }
        }

        userRepository.save(user);

        log.info("用户 {} 在 {} 探索, WIS: {}, 获得经验: {}", userId, mapNode.getName(), user.getStatWis(), expGained);

        // 提取物品名称列表
        List<String> foundItemNames = null;
        if (result.getFoundItems() != null && !result.getFoundItems().isEmpty()) {
            foundItemNames = result.getFoundItems().stream()
                    .map(item -> (String) item.get("name"))
                    .toList();
        }

        // 调用 LLM 生成美化描述
        ExplorationDescriptionFunction.Request request = new ExplorationDescriptionFunction.Request(
                mapNode.getName(),
                result.getEventType(),
                foundItemNames
        );

        ExplorationDescriptionFunction.Response response = explorationDescriptionFunction.generateExplorationDescription().apply(request);

        if (response != null && response.description() != null) {
            result.setDescription(response.description());
        }

        return result;
    }

    /**
     * 生成探索结果
     */
    private ExplorationResultVO generateExplorationResult(User user, MapNode mapNode) {
        int wisdom = user.getStatWis();
        int mapLevel = mapNode.getLevelRequirement();

        // 计算有效智慧等级（考虑地图等级影响）
        // 公式: effectiveWisdom = wisdom - (mapLevel - 1) * 5
        // 地图等级每高1级，探索难度增加5点
        int effectiveWisdom = wisdom - (mapLevel - 1) * 5;
        effectiveWisdom = Math.max(1, effectiveWisdom); // 最低为1

        ExplorationResultVO.ExplorationResultVOBuilder builder = ExplorationResultVO.builder()
                .userId(user.getId())
                .mapId(mapNode.getId())
                .mapName(mapNode.getName())
                .wisdom(wisdom)
                .timeCostMinutes(EXPLORATION_TIME_COST_MINUTES)
                .staminaCost(EXPLORATION_STAMINA_COST);

        // 根据有效智慧等级确定探索结果
        if (effectiveWisdom >= 1 && effectiveWisdom <= 20) {
            // 基础材料
            builder.eventType("发现基础材料");
            builder.description(""); // 由 LLM 生成
            builder.foundItems(findBasicItems(mapNode, 1));
        } else if (effectiveWisdom >= 21 && effectiveWisdom <= 50) {
            // 稀有材料
            builder.eventType("发现稀有材料");
            builder.description(""); // 由 LLM 生成
            builder.foundItems(findRareItems(mapNode, 1));
        } else if (effectiveWisdom >= 51 && effectiveWisdom <= 80) {
            // 稀有材料 + 小概率配方
            builder.eventType("重大发现");
            builder.description(""); // 由 LLM 生成
            builder.foundItems(findRareItems(mapNode, 2));

            // 随机配方（暂时使用占位符）
            if (Math.random() < 0.3) {
                builder.recipeId(1L);
                builder.recipeName("初级炼药配方");
            }
        } else {
            // 稀有材料 + 配方 + 特殊灵蛋
            builder.eventType("惊人发现");
            builder.description(""); // 由 LLM 生成
            builder.foundItems(findRareItems(mapNode, 3));

            builder.recipeId(2L);
            builder.recipeName("高级炼药配方");

            // 随机灵蛋（暂时使用占位符）
            if (Math.random() < 0.2) {
                Map<String, Object> egg = new HashMap<>();
                egg.put("name", "火灵蛋");
                egg.put("templateId", 101);
                egg.put("quantity", 1);
                List<Map<String, Object>> items = builder.build().getFoundItems();
                if (items == null) {
                    items = new ArrayList<>();
                }
                items.add(egg);
                builder.foundItems(items);
            }
        }

        return builder.build();
    }

    /**
     * 查找基础物品
     */
    private List<Map<String, Object>> findBasicItems(MapNode mapNode, int count) {
        List<Map<String, Object>> items = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Map<String, Object> specialty = mapNode.getRandomSpecialty();
            if (specialty != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", specialty.get("name"));
                item.put("templateId", specialty.get("templateId"));
                item.put("quantity", (int) (Math.random() * 3) + 1); // 1-3
                items.add(item);
            }
        }

        return items;
    }

    /**
     * 查找稀有物品
     */
    private List<Map<String, Object>> findRareItems(MapNode mapNode, int count) {
        List<Map<String, Object>> items = new ArrayList<>();

        // 稀有物品列表（临时硬编码）
        List<String> rareItems = List.of("灵草", "精铁", "灵芝");

        for (int i = 0; i < count; i++) {
            String itemName = rareItems.get((int) (Math.random() * rareItems.size()));

            Map<String, Object> item = new HashMap<>();
            item.put("name", itemName);
            item.put("templateId", (long) (Math.random() * 1000)); // 临时使用随机 ID
            item.put("quantity", 1);
            items.add(item);
        }

        return items;
    }

    /**
     * 计算获得的经验值
     */
    private long calculateExpGained(int wisdom, int mapLevel) {
        // 基础经验 10，每点智慧增加 2 经验
        // 地图等级越高，获得的经验越多（每级增加 5 经验）
        return 10 + (wisdom * 2) + (mapLevel * 5);
    }
}
