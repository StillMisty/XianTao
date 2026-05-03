package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.bounty.entity.Bounty;
import top.stillmisty.xiantao.domain.bounty.entity.UserBounty;
import top.stillmisty.xiantao.domain.bounty.repository.BountyRepository;
import top.stillmisty.xiantao.domain.bounty.repository.UserBountyRepository;
import top.stillmisty.xiantao.domain.bounty.vo.BountyRewardVO;
import top.stillmisty.xiantao.domain.bounty.vo.BountyVO;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.enums.TravelEventType;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.service.ai.ExplorationDescriptionFunction;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 悬赏服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BountyService {

    private final UserRepository userRepository;
    private final MapNodeRepository mapNodeRepository;
    private final BountyRepository bountyRepository;
    private final UserBountyRepository userBountyRepository;
    private final ItemTemplateRepository itemTemplateRepository;
    private final ExplorationDescriptionFunction explorationDescriptionFunction;
    private final StackableItemService stackableItemService;

    // ===================== 公开 API（含认证） =====================

    public ServiceResult<List<BountyVO>> listBounties(PlatformType platform, String openId) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(listBounties(userId));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    public ServiceResult<String> startBounty(PlatformType platform, String openId, Long bountyId) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(startBounty(userId, bountyId));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    public ServiceResult<BountyRewardVO> completeBounty(PlatformType platform, String openId) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(completeBounty(userId));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    public ServiceResult<String> abandonBounty(PlatformType platform, String openId) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return new ServiceResult.Success<>(abandonBounty(userId));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ServiceResult.businessFailure(e.getMessage());
        }
    }

    // ===================== 内部 API =====================

    public List<BountyVO> listBounties(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getStatus() != UserStatus.IDLE) {
            throw new IllegalStateException("您当前处于 " + user.getStatus().getName() + " 状态，无法查看悬赏（需要 空闲 状态）");
        }
        MapNode mapNode = mapNodeRepository.findById(user.getLocationId()).orElseThrow();

        return bountyRepository.findByMapId(mapNode.getId()).stream()
                .filter(b -> user.getLevel() >= b.getRequireLevel())
                .map(b -> new BountyVO(
                        b.getId(), b.getName(), b.getDescription(),
                        b.getDurationMinutes(), b.getRewards(),
                        b.getRequireLevel(), b.getEventWeight()
                ))
                .toList();
    }

    public String startBounty(Long userId, Long bountyId) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getStatus() != UserStatus.IDLE) {
            throw new IllegalStateException("您当前处于 " + user.getStatus().getName() + " 状态，无法接取悬赏（需要 空闲 状态）");
        }

        Bounty bounty = bountyRepository.findById(bountyId)
                .orElseThrow(() -> new IllegalArgumentException("悬赏不存在"));

        MapNode mapNode = mapNodeRepository.findById(user.getLocationId()).orElseThrow();
        if (!bounty.getMapId().equals(mapNode.getId())) {
            throw new IllegalArgumentException("该悬赏不属于当前地图");
        }
        if (user.getLevel() < bounty.getRequireLevel()) {
            throw new IllegalArgumentException("等级不足，无法接取该悬赏");
        }

        // 种子随机预确定奖励物品（userId + 当日日期为种子）
        long seed = userId * 31 + LocalDate.now().toEpochDay();
        Random rng = new Random(seed);
        List<Map<String, Object>> predeterminedRewards = determineRewards(bounty, mapNode, rng);

        UserBounty record = new UserBounty();
        record.setUserId(userId);
        record.setBountyId(bountyId);
        record.setBountyName(bounty.getName());
        record.setStartTime(LocalDateTime.now());
        record.setDurationMinutes(bounty.getDurationMinutes());
        record.setRewards(predeterminedRewards);
        record.setStatus("active");
        userBountyRepository.save(record);

        user.setStatus(UserStatus.BOUNTY);
        userRepository.save(user);

        log.info(
                "用户 {} 接取悬赏: {} (ID={}, 耗时{}分, 预存物品数={})",
                userId, bounty.getName(), bountyId, bounty.getDurationMinutes(), predeterminedRewards.size()
        );

        return String.format("已接取悬赏「%s」，预计 %d 分钟后完成。", bounty.getName(), bounty.getDurationMinutes());
    }

    @Transactional
    public BountyRewardVO completeBounty(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getStatus() != UserStatus.BOUNTY) {
            throw new IllegalStateException("您当前处于 " + user.getStatus().getName() + " 状态，无法完成悬赏（需要 悬赏 状态）");
        }
        UserBounty record = userBountyRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("当前没有进行中的悬赏"));

        // 检查时间
        long minutesElapsed = Duration.between(record.getStartTime(), LocalDateTime.now()).toMinutes();
        if (minutesElapsed < record.getDurationMinutes()) {
            long remaining = record.getDurationMinutes() - minutesElapsed;
            throw new IllegalArgumentException(
                    String.format("悬赏「%s」还需 %d 分钟（共需 %d 分）", record.getBountyName(), remaining, record.getDurationMinutes()));
        }

        MapNode mapNode = mapNodeRepository.findById(user.getLocationId()).orElseThrow();
        Bounty bounty = bountyRepository.findById(record.getBountyId()).orElseThrow();

        // 从预存记录中读取奖励物品
        List<Map<String, Object>> items = record.getRewards() != null ? new ArrayList<>(record.getRewards()) : List.of();
        long spiritStones = 0;
        boolean hasBeastEgg = false;

        // 统计奖励类型
        for (Map<String, Object> item : items) {
            String type = (String) item.get("_rewardType");
            if ("spirit_stones".equals(type)) {
                spiritStones = ((Number) item.getOrDefault("amount", 0)).longValue();
            } else if ("beast_egg".equals(type)) {
                hasBeastEgg = true;
            }
        }

        // 去除内部标记字段
        items = items.stream()
                .filter(i -> !"spirit_stones".equals(i.get("_rewardType")))
                .collect(Collectors.toCollection(ArrayList::new));

        // 发放物品
        addRewardsToInventory(userId, items);

        // 发放灵石
        if (spiritStones > 0) {
            user.setSpiritStones(user.getSpiritStones() + spiritStones);
        }

        // 旅行事件
        String eventDescription = resolveEvent(user, mapNode);

        // 奖励描述
        String rewardDescription = buildRewardDescription(spiritStones, items, hasBeastEgg);

        // LLM 美化
        String beautified = beautifyBountyCompletion(mapNode, record.getBountyName(), rewardDescription, eventDescription, items);

        // 标记完成
        record.setStatus("completed");
        userBountyRepository.update(record);

        user.setStatus(UserStatus.IDLE);
        userRepository.save(user);

        log.info(
                "用户 {} 完成悬赏: {} (耗时{}分, 物品数={}, 灵石={})",
                userId, record.getBountyName(), minutesElapsed, items.size(), spiritStones
        );

        return new BountyRewardVO(
                userId, record.getBountyId(), record.getBountyName(), mapNode.getName(),
                minutesElapsed,
                beautified != null ? beautified : rewardDescription,
                eventDescription, items, spiritStones, hasBeastEgg
        );
    }

    public String abandonBounty(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getStatus() != UserStatus.BOUNTY) {
            throw new IllegalStateException("您当前处于 " + user.getStatus().getName() + " 状态，无法放弃悬赏（需要 悬赏 状态）");
        }
        UserBounty record = userBountyRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("当前没有进行中的悬赏"));

        record.setStatus("abandoned");
        userBountyRepository.update(record);

        user.setStatus(UserStatus.IDLE);
        userRepository.save(user);

        log.info("用户 {} 放弃悬赏: {}", userId, record.getBountyName());
        return String.format("已放弃悬赏「%s」，无任何产出。", record.getBountyName());
    }

    // ===================== 奖励预计算 =====================

    /**
     * 接取时预确定奖励（种子随机）
     */
    private List<Map<String, Object>> determineRewards(Bounty bounty, MapNode mapNode, Random rng) {
        if (bounty.getRewards() == null || bounty.getRewards().isEmpty()) return List.of();

        int totalWeight = bounty.getRewards().stream()
                .mapToInt(r -> ((Number) r.getOrDefault("weight", 1)).intValue())
                .sum();
        int roll = rng.nextInt(totalWeight);
        int cumulative = 0;
        Map<String, Object> selected = null;
        for (Map<String, Object> reward : bounty.getRewards()) {
            cumulative += ((Number) reward.getOrDefault("weight", 1)).intValue();
            if (roll < cumulative) {
                selected = reward;
                break;
            }
        }

        if (selected == null) return List.of();

        String type = (String) selected.get("type");
        return switch (type) {
            case "rare_item" -> {
                int count = ((Number) selected.getOrDefault("count", 1)).intValue();
                yield findRareItems(mapNode, count, rng);
            }
            case "spirit_stones" -> {
                long amount = ((Number) selected.getOrDefault("amount", 0)).longValue();
                Map<String, Object> meta = new HashMap<>();
                meta.put("_rewardType", "spirit_stones");
                meta.put("amount", amount);
                yield List.of(meta);
            }
            case "beast_egg" -> {
                List<ItemTemplate> eggs = itemTemplateRepository.findByType(ItemType.BEAST_EGG);
                if (!eggs.isEmpty()) {
                    ItemTemplate egg = eggs.get(rng.nextInt(eggs.size()));
                    Map<String, Object> eggItem = new HashMap<>();
                    eggItem.put("_rewardType", "beast_egg");
                    eggItem.put("name", egg.getName());
                    eggItem.put("templateId", egg.getId());
                    eggItem.put("quantity", 1);
                    yield List.of(eggItem);
                }
                yield List.of();
            }
            default -> List.of();
        };
    }

    private List<Map<String, Object>> findRareItems(MapNode mapNode, int count, Random rng) {
        var specialties = mapNode.getSpecialties();
        if (specialties == null || specialties.isEmpty()) return List.of();

        // Batch lookup item templates
        Map<Long, ItemTemplate> templateMap = itemTemplateRepository.findByIds(new ArrayList<>(specialties.keySet()))
                .stream()
                .collect(Collectors.toMap(ItemTemplate::getId, t -> t));

        int totalWeight = specialties.values().stream().mapToInt(Integer::intValue).sum();

        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int roll = rng.nextInt(Math.max(1, totalWeight));
            int cum = 0;
            for (Map.Entry<Long, Integer> entry : specialties.entrySet()) {
                cum += entry.getValue();
                if (roll < cum) {
                    Long templateId = entry.getKey();
                    ItemTemplate template = templateMap.get(templateId);
                    String name = template != null ? template.getName() : "未知物品";
                    Map<String, Object> item = new HashMap<>();
                    item.put("templateId", templateId);
                    item.put("name", name);
                    item.put("quantity", 1);
                    items.add(item);
                    break;
                }
            }
        }
        return items;
    }

    // ===================== 奖励发放 =====================

    private void addRewardsToInventory(Long userId, List<Map<String, Object>> items) {
        if (items.isEmpty()) return;

        Set<Long> templateIds = items.stream()
                .map(item -> toLong(item.get("templateId")))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (templateIds.isEmpty()) return;

        Map<Long, ItemType> typeMap = itemTemplateRepository.findByIds(new ArrayList<>(templateIds))
                .stream()
                .collect(Collectors.toMap(ItemTemplate::getId, ItemTemplate::getType));

        for (Map<String, Object> item : items) {
            Object templateIdObj = item.get("templateId");
            if (templateIdObj == null) continue;
            String name = (String) item.get("name");
            Long templateId = toLong(templateIdObj);
            int quantity = ((Number) item.get("quantity")).intValue();
            ItemType itemType = typeMap.getOrDefault(templateId, ItemType.MATERIAL);
            stackableItemService.addStackableItem(userId, templateId, itemType, name, quantity);
        }
    }

    private String buildRewardDescription(long spiritStones, List<Map<String, Object>> items, boolean hasBeastEgg) {
        StringBuilder sb = new StringBuilder();
        if (spiritStones > 0) {
            sb.append("获得 ").append(spiritStones).append(" 灵石。");
        }
        if (!items.isEmpty()) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append("获得物品：");
            sb.append(items.stream()
                              .map(i -> String.format("%s x%d", i.get("name"), i.get("quantity")))
                              .collect(Collectors.joining("、")));
            sb.append("。");
        }
        if (hasBeastEgg) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append("获得了一颗灵兽卵！");
        }
        if (sb.isEmpty()) {
            sb.append("完成悬赏，但未获得额外奖励。");
        }
        return sb.toString();
    }

    // ===================== 事件 =====================

    private String resolveEvent(User user, MapNode mapNode) {
        if (mapNode.getTravelEvents() == null || mapNode.getTravelEvents().isEmpty()) return null;

        int d20Roll = (int) (Math.random() * 20) + 1;
        if (d20Roll > 10) return null;

        TravelEventType eventType = TravelEventType.randomEvent(mapNode.getTravelEvents());
        if (eventType == TravelEventType.SAFE_PASSAGE) return null;

        String description = processEvent(user, eventType);
        log.info("用户 {} 悬赏途中触发事件: {}", user.getId(), description);
        return description;
    }

    private String processEvent(User user, TravelEventType eventType) {
        return switch (eventType) {
            case AMBUSH -> {
                int damage = 10 + (int) (Math.random() * 20);
                user.takeDamage(damage);
                yield String.format("途中遭遇敌人袭击，受到 %d 点伤害（剩余 HP: %d）", damage, user.getHpCurrent());
            }
            case FIND_TREASURE -> {
                long reward = 5 + (long) (Math.random() * 15);
                user.setSpiritStones(user.getSpiritStones() + reward);
                yield String.format("途中发现了一个隐藏宝箱，获得 %d 灵石", reward);
            }
            case WEATHER -> "遭遇毒雾天气，艰难穿过才得以继续前行";
            case SAFE_PASSAGE -> null;
        };
    }

    // ===================== LLM 美化 =====================

    private String beautifyBountyCompletion(
            MapNode mapNode, String bountyName,
            String rewardDescription, String eventDescription,
            List<Map<String, Object>> items
    ) {
        List<String> itemNames = null;
        if (items != null && !items.isEmpty()) {
            itemNames = items.stream().map(i -> (String) i.get("name")).toList();
        }

        var request = new ExplorationDescriptionFunction.Request(
                mapNode.getName(),
                mapNode.getDescription(),
                "完成悬赏「" + bountyName + "」",
                itemNames,
                null,
                null,
                eventDescription
        );

        try {
            var response = explorationDescriptionFunction.beautify(request);
            return response != null ? response.description() : null;
        } catch (Exception e) {
            log.warn("LLM 美化悬赏描述失败", e);
            return null;
        }
    }

    private Long toLong(Object value) {
        if (value instanceof Long longVal) return longVal;
        if (value instanceof Number number) return number.longValue();
        throw new IllegalArgumentException("无法转换为 Long: " + value);
    }
}