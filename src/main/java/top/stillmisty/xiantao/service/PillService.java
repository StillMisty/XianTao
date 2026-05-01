package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.pill.entity.PlayerPillRecipe;
import top.stillmisty.xiantao.domain.pill.repository.PlayerPillRecipeRepository;
import top.stillmisty.xiantao.domain.pill.vo.PillRecipeVO;
import top.stillmisty.xiantao.domain.pill.vo.PillRefiningResultVO;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 炼丹服务
 * 处理：丹方学习、丹方查询、炼丹、服用丹药
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PillService {

    private final UserRepository userRepository;
    private final ItemTemplateRepository itemTemplateRepository;
    private final StackableItemRepository stackableItemRepository;
    private final PlayerPillRecipeRepository playerPillRecipeRepository;
    private final AuthenticationService authService;
    private final ItemService itemService;

    // ===================== 公开 API（含认证） =====================

    /**
     * 获取玩家已学丹方列表
     */
    public ServiceResult<List<PillRecipeVO>> getLearnedRecipes(PlatformType platform, String openId) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        return new ServiceResult.Success<>(getLearnedRecipes(auth.userId()));
    }

    /**
     * 获取丹方详情
     */
    public ServiceResult<PillRecipeVO> getRecipeDetail(PlatformType platform, String openId, String recipeName) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        return new ServiceResult.Success<>(getRecipeDetail(auth.userId(), recipeName));
    }

    /**
     * 学习丹方
     */
    public ServiceResult<PillRecipeVO> learnRecipe(PlatformType platform, String openId, String recipeName) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        return new ServiceResult.Success<>(learnRecipe(auth.userId(), recipeName));
    }

    /**
     * 自动炼丹
     */
    public ServiceResult<PillRefiningResultVO> refinePillAuto(PlatformType platform, String openId, String recipeName) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        return new ServiceResult.Success<>(refinePillAuto(auth.userId(), recipeName));
    }

    /**
     * 手动炼丹
     */
    public ServiceResult<PillRefiningResultVO> refinePillManual(PlatformType platform, String openId, List<String> herbInputs) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        return new ServiceResult.Success<>(refinePillManual(auth.userId(), herbInputs));
    }

    /**
     * 服用丹药
     */
    public ServiceResult<String> takePill(PlatformType platform, String openId, String pillName) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        return new ServiceResult.Success<>(takePill(auth.userId(), pillName));
    }

    // ===================== 内部 API（需预先完成认证） =====================

    /**
     * 获取玩家已学丹方列表
     */
    public List<PillRecipeVO> getLearnedRecipes(Long userId) {
        List<PlayerPillRecipe> recipes = playerPillRecipeRepository.findByUserId(userId);
        return recipes.stream()
                .map(recipe -> {
                    ItemTemplate recipeTemplate = itemTemplateRepository.findById(recipe.getRecipeTemplateId()).orElse(null);
                    ItemTemplate resultTemplate = itemTemplateRepository.findById(recipe.getResultItemId()).orElse(null);
                    if (recipeTemplate == null || resultTemplate == null) return null;
                    return convertToPillRecipeVO(recipe, recipeTemplate, resultTemplate);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 获取丹方详情
     */
    public PillRecipeVO getRecipeDetail(Long userId, String recipeName) {
        // 查找玩家已学丹方中匹配名称的丹方
        List<PlayerPillRecipe> recipes = playerPillRecipeRepository.findByUserId(userId);
        for (PlayerPillRecipe recipe : recipes) {
            ItemTemplate recipeTemplate = itemTemplateRepository.findById(recipe.getRecipeTemplateId()).orElse(null);
            if (recipeTemplate != null && recipeTemplate.getName().contains(recipeName)) {
                ItemTemplate resultTemplate = itemTemplateRepository.findById(recipe.getResultItemId()).orElse(null);
                if (resultTemplate != null) {
                    return convertToPillRecipeVO(recipe, recipeTemplate, resultTemplate);
                }
            }
        }
        return null;
    }

    /**
     * 学习丹方
     */
    public PillRecipeVO learnRecipe(Long userId, String recipeName) {
        // 查找背包中的丹方卷轴
        List<StackableItem> items = stackableItemRepository.findByUserId(userId);
        StackableItem recipeItem = null;
        for (StackableItem item : items) {
            if (item.getItemType() == ItemType.MATERIAL && item.getName().contains(recipeName)) {
                ItemTemplate template = itemTemplateRepository.findById(item.getTemplateId()).orElse(null);
                if (template != null && template.getRecipe() != null) {
                    recipeItem = item;
                    break;
                }
            }
        }

        if (recipeItem == null) {
            return null;
        }

        ItemTemplate recipeTemplate = itemTemplateRepository.findById(recipeItem.getTemplateId()).orElse(null);
        if (recipeTemplate == null) return null;

        // 检查是否已学
        if (playerPillRecipeRepository.existsByUserIdAndRecipeTemplateId(userId, recipeTemplate.getId())) {
            return null;
        }

        // 获取成品丹药模板ID
        Long resultItemId = recipeTemplate.getRecipeResultItemId();
        if (resultItemId == null) return null;

        // 创建玩家已学丹方
        PlayerPillRecipe recipe = PlayerPillRecipe.create(userId, recipeTemplate.getId(), resultItemId);
        playerPillRecipeRepository.save(recipe);

        // 消耗丹方卷轴
        itemService.reduceStackableItem(userId, recipeTemplate.getId(), 1);

        ItemTemplate resultTemplate = itemTemplateRepository.findById(resultItemId).orElse(null);
        if (resultTemplate == null) return null;

        return convertToPillRecipeVO(recipe, recipeTemplate, resultTemplate);
    }

    /**
     * 自动炼丹
     */
    public PillRefiningResultVO refinePillAuto(Long userId, String recipeName) {
        // 查找玩家已学丹方
        List<PlayerPillRecipe> recipes = playerPillRecipeRepository.findByUserId(userId);
        PlayerPillRecipe targetRecipe = null;
        ItemTemplate recipeTemplate = null;
        for (PlayerPillRecipe recipe : recipes) {
            ItemTemplate template = itemTemplateRepository.findById(recipe.getRecipeTemplateId()).orElse(null);
            if (template != null && template.getName().contains(recipeName)) {
                targetRecipe = recipe;
                recipeTemplate = template;
                break;
            }
        }

        if (targetRecipe == null || recipeTemplate == null) {
            return new PillRefiningResultVO(false, "未找到丹方：" + recipeName, null, null, 0, null, null, null);
        }

        // 获取丹方五行要求
        Map<String, Map<String, Integer>> requirements = recipeTemplate.getRecipeRequirements();
        if (requirements.isEmpty()) {
            return new PillRefiningResultVO(false, "丹方数据错误", null, null, 0, null, null, null);
        }

        // 获取玩家背包中的药材
        List<StackableItem> herbs = stackableItemRepository.findByUserId(userId).stream()
                .filter(item -> item.getItemType() == ItemType.HERB)
                .toList();

        if (herbs.isEmpty()) {
            return new PillRefiningResultVO(false, "背包中没有药材", null, null, 0, null, null, List.of("所有药材"));
        }

        // 自动配药算法
        return findBestCombination(userId, herbs, requirements, recipeTemplate);
    }

    /**
     * 手动炼丹
     */
    public PillRefiningResultVO refinePillManual(Long userId, List<String> herbInputs) {
        // 解析药材输入
        List<HerbInput> parsedInputs = parseHerbInputs(userId, herbInputs);
        if (parsedInputs.isEmpty()) {
            return new PillRefiningResultVO(false, "药材输入格式错误", null, null, 0, null, null, null);
        }

        // 计算五行累加值
        Map<String, Integer> elementTotals = new HashMap<>();
        Map<String, Integer> usedHerbs = new HashMap<>();
        for (HerbInput input : parsedInputs) {
            StackableItem herb = input.herb();
            int quantity = input.quantity();
            if (!herb.hasEnoughQuantity(quantity)) {
                return new PillRefiningResultVO(false, "药材数量不足：" + herb.getName(), null, null, 0, null, null, null);
            }
            // 累加五行值
            for (String element : List.of("metal", "wood", "water", "fire", "earth")) {
                int value = herb.getElementValue(element) * quantity;
                elementTotals.merge(element, value, Integer::sum);
            }
            usedHerbs.put(herb.getName(), quantity);
        }

        // 匹配丹方
        List<PlayerPillRecipe> recipes = playerPillRecipeRepository.findByUserId(userId);
        for (PlayerPillRecipe recipe : recipes) {
            ItemTemplate recipeTemplate = itemTemplateRepository.findById(recipe.getRecipeTemplateId()).orElse(null);
            if (recipeTemplate == null) continue;

            Map<String, Map<String, Integer>> requirements = recipeTemplate.getRecipeRequirements();
            if (matchesRequirements(elementTotals, requirements)) {
                // 成丹判定
                double qualityScore = calculateQualityScore(elementTotals, requirements);
                String quality = determineQuality(qualityScore);

                // 消耗药材
                for (HerbInput input : parsedInputs) {
                    itemService.reduceStackableItem(userId, input.herb().getTemplateId(), input.quantity());
                }

                // 生成丹药
                Long resultItemId = recipeTemplate.getRecipeResultItemId();
                int resultQuantity = recipeTemplate.getRecipeResultQuantity();
                ItemTemplate resultTemplate = itemTemplateRepository.findById(resultItemId).orElse(null);
                if (resultTemplate == null) continue;

                // 创建丹药实例（带properties）
                createPillItem(userId, resultTemplate, recipeTemplate.getRecipeGrade(), quality, resultQuantity);

                return new PillRefiningResultVO(true, "炼丹成功！", resultItemId, resultTemplate.getName(),
                        resultQuantity, quality, usedHerbs, null);
            }
        }

        return new PillRefiningResultVO(false, "药材五行不匹配任何丹方", null, null, 0, null, usedHerbs, null);
    }

    /**
     * 服用丹药
     */
    public String takePill(Long userId, String pillName) {
        // 查找背包中的丹药
        List<StackableItem> pills = stackableItemRepository.findByUserId(userId).stream()
                .filter(item -> item.getItemType() == ItemType.POTION && item.getName().contains(pillName))
                .toList();

        if (pills.isEmpty()) {
            return "背包中未找到丹药：" + pillName;
        }

        StackableItem pill = pills.get(0);
        ItemTemplate template = itemTemplateRepository.findById(pill.getTemplateId()).orElse(null);
        if (template == null) {
            return "丹药数据错误";
        }

        // 获取丹药效果
        Map<String, Object> properties = template.getProperties();
        if (properties == null) {
            return "丹药无效果";
        }

        // 获取成色倍率
        double qualityMultiplier = getQualityMultiplier(pill.getQuality());

        // 应用效果
        String effectResult = applyPillEffect(userId, properties, qualityMultiplier);
        if (effectResult == null) {
            return "丹药效果未知";
        }

        // 消耗丹药
        itemService.reduceStackableItem(userId, pill.getTemplateId(), 1);

        return effectResult;
    }

    // ===================== 辅助方法 =====================

    /**
     * 查找最优药材组合（自动炼丹）
     */
    private PillRefiningResultVO findBestCombination(Long userId, List<StackableItem> herbs,
            Map<String, Map<String, Integer>> requirements, ItemTemplate recipeTemplate) {
        // 简化实现：贪心算法
        Map<String, Integer> elementTotals = new HashMap<>();
        Map<String, Integer> usedHerbs = new HashMap<>();
        List<String> missingElements = new ArrayList<>();

        // 检查每个五行要求
        for (Map.Entry<String, Map<String, Integer>> entry : requirements.entrySet()) {
            String element = entry.getKey();
            int min = entry.getValue().getOrDefault("min", 0);
            int max = entry.getValue().getOrDefault("max", Integer.MAX_VALUE);

            // 查找包含该五行的药材
            int currentTotal = elementTotals.getOrDefault(element, 0);
            if (currentTotal < min) {
                // 需要补充
                boolean found = false;
                for (StackableItem herb : herbs) {
                    int herbValue = herb.getElementValue(element);
                    if (herbValue > 0 && !usedHerbs.containsKey(herb.getName())) {
                        // 计算需要多少个
                        int needed = (int) Math.ceil((double) (min - currentTotal) / herbValue);
                        int available = herb.getQuantity();
                        int toUse = Math.min(needed, available);
                        if (toUse > 0) {
                            elementTotals.merge(element, herbValue * toUse, Integer::sum);
                            usedHerbs.put(herb.getName(), toUse);
                            currentTotal = elementTotals.get(element);
                            if (currentTotal >= min) {
                                found = true;
                                break;
                            }
                        }
                    }
                }
                if (!found) {
                    missingElements.add(element);
                }
            }
        }

        if (!missingElements.isEmpty()) {
            return new PillRefiningResultVO(false, "缺少药材属性：" + String.join(", ", missingElements),
                    null, null, 0, null, null, missingElements);
        }

        // 检查是否超过最大值
        for (Map.Entry<String, Map<String, Integer>> entry : requirements.entrySet()) {
            String element = entry.getKey();
            int max = entry.getValue().getOrDefault("max", Integer.MAX_VALUE);
            int currentTotal = elementTotals.getOrDefault(element, 0);
            if (currentTotal > max) {
                return new PillRefiningResultVO(false, "药材属性超过上限：" + element,
                        null, null, 0, null, usedHerbs, null);
            }
        }

        // 成丹判定
        double qualityScore = calculateQualityScore(elementTotals, requirements);
        String quality = determineQuality(qualityScore);

        // 消耗药材
        for (Map.Entry<String, Integer> entry : usedHerbs.entrySet()) {
            String herbName = entry.getKey();
            int quantity = entry.getValue();
            for (StackableItem herb : herbs) {
                if (herb.getName().equals(herbName)) {
                    itemService.reduceStackableItem(userId, herb.getTemplateId(), quantity);
                    break;
                }
            }
        }

        // 生成丹药
        Long resultItemId = recipeTemplate.getRecipeResultItemId();
        int resultQuantity = recipeTemplate.getRecipeResultQuantity();
        ItemTemplate resultTemplate = itemTemplateRepository.findById(resultItemId).orElse(null);
        if (resultTemplate == null) {
            return new PillRefiningResultVO(false, "丹药模板不存在", null, null, 0, null, null, null);
        }

        // 创建丹药实例
        createPillItem(userId, resultTemplate, recipeTemplate.getRecipeGrade(), quality, resultQuantity);

        return new PillRefiningResultVO(true, "炼丹成功！", resultItemId, resultTemplate.getName(),
                resultQuantity, quality, usedHerbs, null);
    }

    /**
     * 检查五行是否匹配丹方要求
     */
    private boolean matchesRequirements(Map<String, Integer> elementTotals,
            Map<String, Map<String, Integer>> requirements) {
        for (Map.Entry<String, Map<String, Integer>> entry : requirements.entrySet()) {
            String element = entry.getKey();
            int min = entry.getValue().getOrDefault("min", 0);
            int max = entry.getValue().getOrDefault("max", Integer.MAX_VALUE);
            int current = elementTotals.getOrDefault(element, 0);
            if (current < min || current > max) {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算品质分数
     */
    private double calculateQualityScore(Map<String, Integer> elementTotals,
            Map<String, Map<String, Integer>> requirements) {
        double totalScore = 0;
        int count = 0;
        for (Map.Entry<String, Map<String, Integer>> entry : requirements.entrySet()) {
            String element = entry.getKey();
            int min = entry.getValue().getOrDefault("min", 0);
            int max = entry.getValue().getOrDefault("max", 0);
            int current = elementTotals.getOrDefault(element, 0);

            double center = (max + min) / 2.0;
            double halfWidth = (max - min) / 2.0;
            if (halfWidth == 0) {
                totalScore += 1.0;
            } else {
                double deviation = Math.abs(current - center);
                double score = 1 - deviation / halfWidth;
                totalScore += Math.max(0, score);
            }
            count++;
        }
        return count > 0 ? totalScore / count : 0;
    }

    /**
     * 根据品质分数确定成色
     */
    private String determineQuality(double score) {
        if (score >= 0.8) return "superior";
        if (score >= 0.5) return "normal";
        return "inferior";
    }

    /**
     * 获取成色倍率
     */
    private double getQualityMultiplier(String quality) {
        if (quality == null) return 1.0;
        return switch (quality) {
            case "superior" -> 1.5;
            case "normal" -> 1.0;
            case "inferior" -> 0.7;
            default -> 1.0;
        };
    }

    /**
     * 应用丹药效果
     */
    private String applyPillEffect(Long userId, Map<String, Object> properties, double qualityMultiplier) {
        String pillType = (String) properties.get("pill_type");
        if (pillType == null) return null;

        User user = userRepository.findById(userId).orElseThrow();

        return switch (pillType) {
            case "exp" -> {
                Object expObj = properties.get("exp_amount");
                if (expObj instanceof Number expAmount) {
                    long actualExp = (long) (expAmount.longValue() * qualityMultiplier);
                    user.addExp(actualExp);
                    userRepository.save(user);
                    yield String.format("服用丹药成功，获得 %d 经验值", actualExp);
                }
                yield null;
            }
            case "hp" -> {
                Object hpObj = properties.get("hp_percentage");
                if (hpObj instanceof Number hpPercentage) {
                    int maxHp = user.calculateMaxHp();
                    int healAmount = (int) (maxHp * hpPercentage.doubleValue() / 100 * qualityMultiplier);
                    int oldHp = user.getHpCurrent();
                    user.setHpCurrent(Math.min(maxHp, oldHp + healAmount));
                    userRepository.save(user);
                    yield String.format("服用丹药成功，恢复 %d 生命值", healAmount);
                }
                yield null;
            }
            case "breakthrough" -> {
                // 突破成功率加成（临时效果，需要额外实现）
                yield "服用丹药成功，下次突破成功率提升";
            }
            case "stat" -> {
                Object statObj = properties.get("stat_amount");
                if (statObj instanceof Number statAmount) {
                    int actualStat = (int) (statAmount.intValue() * qualityMultiplier);
                    // 永久属性点（需要额外实现）
                    yield String.format("服用丹药成功，获得 %d 属性点", actualStat);
                }
                yield null;
            }
            default -> null;
        };
    }

    /**
     * 创建丹药实例
     */
    private void createPillItem(Long userId, ItemTemplate resultTemplate, int grade, String quality, int quantity) {
        // 创建丹药properties
        Map<String, Object> properties = new HashMap<>();
        properties.put("grade", grade);
        properties.put("quality", quality);

        // 查找是否已有相同丹药
        Optional<StackableItem> existingItem = stackableItemRepository.findByUserIdAndTemplateId(userId, resultTemplate.getId());
        if (existingItem.isPresent()) {
            StackableItem item = existingItem.get();
            item.addQuantity(quantity);
            // 更新properties（如果有变化）
            item.setProperties(properties);
            stackableItemRepository.save(item);
        } else {
            StackableItem newItem = StackableItem.create(userId, resultTemplate.getId(),
                    resultTemplate.getType(), resultTemplate.getName(), quantity);
            newItem.setProperties(properties);
            stackableItemRepository.save(newItem);
        }
    }

    /**
     * 解析药材输入
     */
    private List<HerbInput> parseHerbInputs(Long userId, List<String> herbInputs) {
        List<HerbInput> result = new ArrayList<>();
        for (String input : herbInputs) {
            // 格式：药材名×数量 或 药材名x数量
            String[] parts = input.split("[×xX]");
            if (parts.length != 2) continue;

            String herbName = parts[0].trim();
            int quantity;
            try {
                quantity = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                continue;
            }

            if (quantity <= 0) continue;

            // 查找药材
            List<StackableItem> herbs = stackableItemRepository.findByUserId(userId).stream()
                    .filter(item -> item.getItemType() == ItemType.HERB && item.getName().contains(herbName))
                    .toList();

            if (!herbs.isEmpty()) {
                result.add(new HerbInput(herbs.get(0), quantity));
            }
        }
        return result;
    }

    /**
     * 转换为PillRecipeVO
     */
    private PillRecipeVO convertToPillRecipeVO(PlayerPillRecipe recipe, ItemTemplate recipeTemplate,
            ItemTemplate resultTemplate) {
        return new PillRecipeVO(
                recipe.getRecipeTemplateId(),
                recipeTemplate.getName(),
                recipeTemplate.getRecipeGrade(),
                recipe.getResultItemId(),
                resultTemplate.getName(),
                recipeTemplate.getRecipeResultQuantity(),
                recipeTemplate.getRecipeRequirements()
        );
    }

    /**
     * 药材输入记录
     */
    private record HerbInput(StackableItem herb, int quantity) {
    }
}