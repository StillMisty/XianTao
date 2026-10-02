package top.stillmisty.xiantao.service.skill;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.skill.entity.PlayerSkill;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.enums.SkillType;
import top.stillmisty.xiantao.domain.skill.vo.SkillSlotResult;
import top.stillmisty.xiantao.domain.skill.vo.SkillVO;
import top.stillmisty.xiantao.domain.user.enums.CultivationRealm;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.PlayerSkillRepository;
import top.stillmisty.xiantao.infrastructure.repository.SkillRepository;
import top.stillmisty.xiantao.infrastructure.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.inventory.StackableItemService;
import top.stillmisty.xiantao.service.player.PlayerLoader;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillService {

  private final PlayerLoader playerLoader;
  private final SkillRepository skillRepository;
  private final PlayerSkillRepository playerSkillRepository;
  private final StackableItemRepository stackableItemRepository;
  private final StackableItemService stackableItemService;
  private final ItemTemplateRepository itemTemplateRepository;

  // ===================== 内部 API（需预先完成认证） =====================

  @Transactional
  @CacheEvict(cacheNames = "player_skills", key = "#userId")
  public SkillSlotResult learnFromJade(Long userId, String jadeInput) {
    var jadeItems =
        stackableItemRepository.findByUserId(userId).stream()
            .filter(si -> si.getItemType() == ItemType.SKILL_JADE)
            .toList();

    if (jadeItems.isEmpty()) {
      return SkillSlotResult.builder().success(false).message("你没有法决玉简").build();
    }

    StackableItem matchedJade = resolveJade(jadeItems, jadeInput);
    if (matchedJade == null) {
      return buildJadeNotFoundResult(jadeItems, jadeInput);
    }

    Skill skill = resolveSkillFromJade(matchedJade);
    if (skill == null)
      return SkillSlotResult.builder().success(false).message("玉简对应的法决不存在").build();

    SkillSlotResult validationError = validateLearningEligibility(userId, skill);
    if (validationError != null) return validationError;

    consumeJade(matchedJade, userId, skill.getId());

    PlayerSkill playerSkill = new PlayerSkill();
    playerSkill.setUserId(userId);
    playerSkill.setSkillId(skill.getId());
    playerSkill.unequip();
    playerSkillRepository.save(playerSkill);

    log.info("学习法决成功: userId={}, skillId={}, skillName={}", userId, skill.getId(), skill.getName());

    String passiveNote = skill.isPassive() ? "（被动法决，习得即生效，不占法决槽位）" : "";
    return SkillSlotResult.builder()
        .success(true)
        .message("你成功学会了「" + skill.getName() + "」！" + passiveNote)
        .skill(toSkillVO(playerSkill, skill))
        .build();
  }

  private SkillSlotResult buildJadeNotFoundResult(List<StackableItem> jadeItems, String jadeInput) {
    var candidates = new ArrayList<String>();
    for (int i = 0; i < jadeItems.size(); i++) {
      candidates.add(
          (i + 1) + ". " + jadeItems.get(i).getName() + " x" + jadeItems.get(i).getQuantity());
    }
    return SkillSlotResult.builder()
        .success(false)
        .message("找不到匹配的法决玉简「" + jadeInput + "」，你的玉简有：\n" + String.join("\n", candidates))
        .build();
  }

  @Nullable
  private Skill resolveSkillFromJade(StackableItem matchedJade) {
    var template = itemTemplateRepository.findById(matchedJade.getTemplateId()).orElse(null);
    if (template == null) return null;

    var props = template.typedProperties();
    if (!(props instanceof ItemProperties.SkillJade(long skillId))) return null;

    return skillRepository.findById(skillId).orElse(null);
  }

  @Nullable
  private SkillSlotResult validateLearningEligibility(Long userId, Skill skill) {
    if (playerSkillRepository.findByUserIdAndSkillId(userId, skill.getId()).isPresent()) {
      return SkillSlotResult.builder()
          .success(false)
          .message("你已经学会「" + skill.getName() + "」了")
          .build();
    }

    var user = playerLoader.load(userId);
    if (user.getLevel() < skill.getLevelRequirement()) {
      return SkillSlotResult.builder()
          .success(false)
          .message(
              String.format(
                  "你的境界不足，需要 %s 才能学习「%s」（当前 %s）",
                  CultivationRealm.realmDisplay(skill.getLevelRequirement()),
                  skill.getName(),
                  CultivationRealm.realmDisplay(user.getLevel())))
          .build();
    }

    return validatePrecursor(userId, skill);
  }

  /** 法决树前置：未修习前置法决时拒绝学习，并提示前置法决名。 */
  @Nullable
  private SkillSlotResult validatePrecursor(Long userId, Skill skill) {
    Long precursorId = skill.getRequireSkillId();
    if (precursorId == null) return null;
    if (playerSkillRepository.findByUserIdAndSkillId(userId, precursorId).isPresent()) return null;

    String precursorName =
        skillRepository
            .findById(precursorId)
            .map(Skill::getName)
            .orElse(String.valueOf(precursorId));
    String requirement = ErrorCode.SKILL_REQUIREMENT_NOT_MET.format(precursorName);
    return SkillSlotResult.builder()
        .success(false)
        .message("学习「" + skill.getName() + "」前，" + requirement)
        .build();
  }

  private void consumeJade(StackableItem matchedJade, Long userId, Long skillId) {
    stackableItemService.reduceStackableItem(userId, matchedJade.getId(), 1);
    log.info(
        "消耗法决玉简: userId={}, templateId={}, skillId={}",
        userId,
        matchedJade.getTemplateId(),
        skillId);
  }

  @Cacheable(cacheNames = "player_skills", key = "'learned:' + #userId")
  public List<SkillVO> getLearnedSkills(Long userId) {
    return toSkillVOList(playerSkillRepository.findByUserId(userId));
  }

  @Cacheable(cacheNames = "player_skills", key = "'equipped:' + #userId")
  public List<SkillVO> getEquippedSkills(Long userId) {
    // 被动法决习得即生效、不占槽位，不属于装载列表
    return toSkillVOList(playerSkillRepository.findEquippedByUserId(userId)).stream()
        .filter(skill -> !skill.isPassive())
        .toList();
  }

  private List<SkillVO> toSkillVOList(List<PlayerSkill> playerSkills) {
    if (playerSkills.isEmpty()) return List.of();

    var skillMap = loadSkillMap(playerSkills);
    return playerSkills.stream()
        .map(ps -> toSkillVO(ps, skillMap.get(ps.getSkillId())))
        .filter(Objects::nonNull)
        .toList();
  }

  @Transactional
  @CacheEvict(cacheNames = "player_skills", key = "#userId")
  public SkillSlotResult equipSkill(Long userId, String skillInput) {
    // 1. 获取已学法决
    var playerSkills = playerSkillRepository.findByUserId(userId);
    if (playerSkills.isEmpty()) {
      return SkillSlotResult.builder().success(false).message("你还没有学会任何法决").build();
    }

    // 2. 解析要去装载的法决
    var skillMap = loadSkillMap(playerSkills);
    var matched = resolvePlayerSkill(playerSkills, skillInput, skillMap);
    if (matched == null) {
      return SkillSlotResult.builder()
          .success(false)
          .message("找不到匹配的法决「" + skillInput + "」，请使用「法决」查看")
          .build();
    }

    var skill = skillMap.get(matched.getSkillId());
    if (skill == null) {
      return SkillSlotResult.builder().success(false).message("法决数据异常").build();
    }

    // 3. 被动法决习得即生效，不进入槽位
    if (skill.isPassive()) {
      return SkillSlotResult.builder()
          .success(false)
          .message("「" + skill.getName() + "」为被动法决，习得即生效，无需装载")
          .build();
    }

    // 4. 检查是否已装载
    if (matched.isEquipped()) {
      return SkillSlotResult.builder()
          .success(false)
          .message("「" + skill.getName() + "」已经在槽位中")
          .build();
    }

    // 5. 检查槽位（被动法决不占槽位，仅统计主动法决）
    var user = playerLoader.load(userId);
    int maxSlots = calculateMaxSlots(user.getLevel());
    long equippedCount =
        playerSkills.stream()
            .filter(PlayerSkill::isEquipped)
            .filter(ps -> isActiveSkill(ps, skillMap))
            .count();
    if (equippedCount >= maxSlots) {
      return SkillSlotResult.builder()
          .success(false)
          .message(String.format("法决槽位已满（%d/%d），请先卸下不需要的法决", equippedCount, maxSlots))
          .equippedCount((int) equippedCount)
          .maxSlots(maxSlots)
          .build();
    }

    // 6. 原子条件装载，防止并发装载超额
    if (playerSkillRepository.equipIfSlotAvailable(matched.getId(), userId, maxSlots) == 0) {
      return SkillSlotResult.builder()
          .success(false)
          .message(String.format("法决槽位已满（%d/%d），请先卸下不需要的法决", maxSlots, maxSlots))
          .equippedCount(maxSlots)
          .maxSlots(maxSlots)
          .build();
    }
    matched.equip();

    log.debug("装载法决: userId={}, skillId={}, skillName={}", userId, skill.getId(), skill.getName());

    return SkillSlotResult.builder()
        .success(true)
        .message("已装载「" + skill.getName() + "」")
        .skill(toSkillVO(matched, skill))
        .equippedCount((int) equippedCount + 1)
        .maxSlots(maxSlots)
        .build();
  }

  private boolean isActiveSkill(PlayerSkill playerSkill, Map<Long, Skill> skillMap) {
    Skill skill = skillMap.get(playerSkill.getSkillId());
    return skill == null || !skill.isPassive();
  }

  @Transactional
  @CacheEvict(cacheNames = "player_skills", key = "#userId")
  public SkillSlotResult unequipSkill(Long userId, String skillInput) {
    // 1. 获取已装载法决
    var equippedSkills = playerSkillRepository.findEquippedByUserId(userId);
    if (equippedSkills.isEmpty()) {
      return SkillSlotResult.builder().success(false).message("你当前没有装载任何法决").build();
    }

    // 2. 解析要卸下的法决
    var matched = resolvePlayerSkill(equippedSkills, skillInput);
    if (matched == null) {
      return SkillSlotResult.builder()
          .success(false)
          .message("找不到匹配的已装载法决「" + skillInput + "」，请使用「法决」查看当前装载")
          .build();
    }

    // 3. 卸下
    matched.unequip();
    playerSkillRepository.save(matched);

    var user = playerLoader.load(userId);
    int maxSlots = calculateMaxSlots(user.getLevel());

    var skill = skillRepository.findById(matched.getSkillId()).orElse(null);
    String skillName = skill != null ? skill.getName() : String.valueOf(matched.getSkillId());

    log.debug("卸下法决: userId={}, skillId={}, skillName={}", userId, matched.getSkillId(), skillName);

    return SkillSlotResult.builder()
        .success(true)
        .message("已卸下「" + skillName + "」")
        .skill(skill != null ? toSkillVO(matched, skill) : null)
        .equippedCount(equippedSkills.size() - 1)
        .maxSlots(maxSlots)
        .build();
  }

  // ===================== 工具方法 =====================

  @Nullable
  private SkillVO toSkillVO(PlayerSkill ps, @Nullable Skill skill) {
    if (skill == null) return null;
    return new SkillVO(
        ps.getId(),
        skill.getId(),
        skill.getName(),
        skill.getDescription() != null ? skill.getDescription() : "",
        skill.getEffects(),
        skill.getSkillType() != null ? skill.getSkillType().getCode() : SkillType.ACTIVE.getCode(),
        skill.getSkillType() != null ? skill.getSkillType().getName() : SkillType.ACTIVE.getName(),
        skill.getBindingType() != null ? skill.getBindingType().getCode() : "NONE",
        skill.getBindingType() != null ? skill.getBindingType().getName() : "无",
        skill.getBindingValue() != null ? skill.getBindingValue() : "",
        skill.getCooldownSeconds(),
        skill.getLevelRequirement(),
        Boolean.TRUE.equals(ps.getIsEquipped()));
  }

  private int calculateMaxSlots(int level) {
    if (level >= 80) return 5;
    if (level >= 60) return 4;
    if (level >= 40) return 3;
    if (level >= 20) return 2;
    return 1;
  }

  @Nullable
  private StackableItem resolveJade(List<StackableItem> jadeItems, String input) {
    return resolveByIndexOrName(jadeItems, input, StackableItem::getName);
  }

  @Nullable
  private PlayerSkill resolvePlayerSkill(List<PlayerSkill> playerSkills, String input) {
    return resolvePlayerSkill(playerSkills, input, loadSkillMap(playerSkills));
  }

  @Nullable
  private PlayerSkill resolvePlayerSkill(
      List<PlayerSkill> playerSkills, String input, Map<Long, Skill> skillMap) {
    return resolveByIndexOrName(
        playerSkills,
        input,
        ps -> {
          var skill = skillMap.get(ps.getSkillId());
          return skill != null ? skill.getName() : "";
        });
  }

  private Map<Long, Skill> loadSkillMap(List<PlayerSkill> playerSkills) {
    var skillIds = playerSkills.stream().map(PlayerSkill::getSkillId).distinct().toList();
    return skillRepository.findByIds(skillIds).stream()
        .collect(Collectors.toMap(Skill::getId, s -> s));
  }

  /** 按编号→精确名称→模糊名称三级解析 */
  @Nullable
  private <T> T resolveByIndexOrName(
      List<T> items, String input, java.util.function.Function<T, String> nameExtractor) {
    if (input == null || input.isEmpty()) return null;
    if (input.matches("\\d+")) {
      int idx = Integer.parseInt(input);
      if (idx >= 1 && idx <= items.size()) {
        return items.get(idx - 1);
      }
    }
    for (var item : items) {
      if (nameExtractor.apply(item).equals(input)) return item;
    }
    var partial = items.stream().filter(item -> nameExtractor.apply(item).contains(input)).toList();
    if (partial.size() == 1) return partial.getFirst();
    return null;
  }
}
