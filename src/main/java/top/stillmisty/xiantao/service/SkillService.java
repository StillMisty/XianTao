package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.skill.entity.PlayerSkill;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.repository.PlayerSkillRepository;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.skill.vo.SkillSlotResult;
import top.stillmisty.xiantao.domain.skill.vo.SkillVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final PlayerSkillRepository playerSkillRepository;
    private final StackableItemRepository stackableItemRepository;
    private final ItemTemplateRepository itemTemplateRepository;

    // ===================== 公开 API（含认证） =====================

    @Transactional
    public ServiceResult<SkillSlotResult> learnFromJade(PlatformType platform, String openId, String jadeInput) {
        Long userId = UserContext.getCurrentUserId();
        return new ServiceResult.Success<>(learnFromJade(userId, jadeInput));
    }

    public ServiceResult<List<SkillVO>> getLearnedSkills(PlatformType platform, String openId) {
        Long userId = UserContext.getCurrentUserId();
        return new ServiceResult.Success<>(getLearnedSkills(userId));
    }

    public ServiceResult<List<SkillVO>> getEquippedSkills(PlatformType platform, String openId) {
        Long userId = UserContext.getCurrentUserId();
        return new ServiceResult.Success<>(getEquippedSkills(userId));
    }

    @Transactional
    public ServiceResult<SkillSlotResult> equipSkill(PlatformType platform, String openId, String skillInput) {
        Long userId = UserContext.getCurrentUserId();
        return new ServiceResult.Success<>(equipSkill(userId, skillInput));
    }

    @Transactional
    public ServiceResult<SkillSlotResult> unequipSkill(PlatformType platform, String openId, String skillInput) {
        Long userId = UserContext.getCurrentUserId();
        return new ServiceResult.Success<>(unequipSkill(userId, skillInput));
    }

    // ===================== 内部 API（需预先完成认证） =====================

    @Transactional
    public SkillSlotResult learnFromJade(Long userId, String jadeInput) {
        // 1. 查找用户背包中的法决玉简
        var jadeItems = stackableItemRepository.findByUserId(userId).stream()
                .filter(si -> si.getItemType() == ItemType.SKILL_JADE)
                .toList();

        if (jadeItems.isEmpty()) {
            return SkillSlotResult.builder()
                    .success(false)
                    .message("你没有法决玉简")
                    .build();
        }

        // 2. 解析玉简（支持编号或名称匹配）
        StackableItem matchedJade = resolveJade(jadeItems, jadeInput);
        if (matchedJade == null) {
            var candidates = new ArrayList<String>();
            for (int i = 0; i < jadeItems.size(); i++) {
                candidates.add((i + 1) + ". " + jadeItems.get(i).getName() + " x" + jadeItems.get(i).getQuantity());
            }
            return SkillSlotResult.builder()
                    .success(false)
                    .message("找不到匹配的法决玉简「" + jadeInput + "」，你的玉简有：\n" + String.join("\n", candidates))
                    .build();
        }

        // 3. 获取玉简对应的法决
        var template = itemTemplateRepository.findById(matchedJade.getTemplateId()).orElse(null);
        if (template == null) {
            return SkillSlotResult.builder()
                    .success(false)
                    .message("玉简数据异常")
                    .build();
        }

        var props = template.typedProperties();
        if (!(props instanceof ItemProperties.SkillJade(long skillId))) {
            return SkillSlotResult.builder()
                    .success(false)
                    .message("玉简未包含法决信息")
                    .build();
        }

        var skill = skillRepository.findById(skillId).orElse(null);
        if (skill == null) {
            return SkillSlotResult.builder()
                    .success(false)
                    .message("玉简对应的法决不存在")
                    .build();
        }

        // 4. 检查是否已学习
        if (playerSkillRepository.findByUserIdAndSkillId(userId, skillId).isPresent()) {
            return SkillSlotResult.builder()
                    .success(false)
                    .message("你已经学会「" + skill.getName() + "」了")
                    .build();
        }

        // 5. 检查等级要求
        var user = userRepository.findById(userId).orElseThrow();
        if (user.getLevel() < skill.getLevelRequirement()) {
            return SkillSlotResult.builder()
                    .success(false)
                    .message(String.format(
                            "你的境界不足，需要第%d层才能学习「%s」（当前第%d层）",
                            skill.getLevelRequirement(), skill.getName(), user.getLevel()
                    ))
                    .build();
        }

        // 6. 消耗玉简
        if (matchedJade.reduceQuantity(1)) {
            stackableItemRepository.deleteById(matchedJade.getId());
        } else {
            stackableItemRepository.save(matchedJade);
        }
        log.info("消耗法决玉简: userId={}, templateId={}, skillId={}", userId, matchedJade.getTemplateId(), skillId);

        // 7. 学习法决
        PlayerSkill playerSkill = new PlayerSkill();
        playerSkill.setUserId(userId);
        playerSkill.setSkillId(skillId);
        playerSkill.setIsEquipped(false);
        playerSkillRepository.save(playerSkill);

        log.info("学习法决成功: userId={}, skillId={}, skillName={}", userId, skillId, skill.getName());

        return SkillSlotResult.builder()
                .success(true)
                .message("你成功学会了「" + skill.getName() + "」！")
                .skill(toSkillVO(playerSkill, skill))
                .build();
    }

    List<SkillVO> getLearnedSkills(Long userId) {
        return toSkillVOList(playerSkillRepository.findByUserId(userId));
    }

    List<SkillVO> getEquippedSkills(Long userId) {
        return toSkillVOList(playerSkillRepository.findEquippedByUserId(userId));
    }

    private List<SkillVO> toSkillVOList(List<PlayerSkill> playerSkills) {
        if (playerSkills.isEmpty()) return List.of();

        var skillIds = playerSkills.stream()
                .map(PlayerSkill::getSkillId)
                .toList();
        var skillMap = skillRepository.findByIds(skillIds).stream()
                .collect(Collectors.toMap(Skill::getId, s -> s));

        return playerSkills.stream()
                .map(ps -> toSkillVO(ps, skillMap.get(ps.getSkillId())))
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional
    SkillSlotResult equipSkill(Long userId, String skillInput) {
        // 1. 获取已学法决
        var playerSkills = playerSkillRepository.findByUserId(userId);
        if (playerSkills.isEmpty()) {
            return SkillSlotResult.builder()
                    .success(false)
                    .message("你还没有学会任何法决")
                    .build();
        }

        // 2. 解析要去装载的法决
        var matched = resolvePlayerSkill(playerSkills, skillInput);
        if (matched == null) {
            return SkillSlotResult.builder()
                    .success(false)
                    .message("找不到匹配的法决「" + skillInput + "」，请使用「法决列表」查看")
                    .build();
        }

        // 3. 检查是否已装载
        if (matched.getIsEquipped()) {
            var skill = skillRepository.findById(matched.getSkillId()).orElse(null);
            return SkillSlotResult.builder()
                    .success(false)
                    .message("「" + (skill != null ? skill.getName() : matched.getSkillId()) + "」已经在槽位中")
                    .build();
        }

        // 4. 检查槽位
        var user = userRepository.findById(userId).orElseThrow();
        int maxSlots = calculateMaxSlots(user.getLevel());
        long equippedCount = playerSkills.stream().filter(PlayerSkill::getIsEquipped).count();
        if (equippedCount >= maxSlots) {
            return SkillSlotResult.builder()
                    .success(false)
                    .message(String.format("法决槽位已满（%d/%d），请先卸下不需要的法决", equippedCount, maxSlots))
                    .equippedCount((int) equippedCount)
                    .maxSlots(maxSlots)
                    .build();
        }

        // 5. 装载
        matched.setIsEquipped(true);
        playerSkillRepository.save(matched);

        var skill = skillRepository.findById(matched.getSkillId()).orElse(null);
        if (skill == null) {
            return SkillSlotResult.builder()
                    .success(false)
                    .message("法决数据异常")
                    .build();
        }

        log.info("装载法决: userId={}, skillId={}, skillName={}", userId, skill.getId(), skill.getName());

        return SkillSlotResult.builder()
                .success(true)
                .message("已装载「" + skill.getName() + "」")
                .skill(toSkillVO(matched, skill))
                .equippedCount((int) equippedCount + 1)
                .maxSlots(maxSlots)
                .build();
    }

    @Transactional
    SkillSlotResult unequipSkill(Long userId, String skillInput) {
        // 1. 获取已装载法决
        var equippedSkills = playerSkillRepository.findEquippedByUserId(userId);
        if (equippedSkills.isEmpty()) {
            return SkillSlotResult.builder()
                    .success(false)
                    .message("你当前没有装载任何法决")
                    .build();
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
        matched.setIsEquipped(false);
        playerSkillRepository.save(matched);

        var user = userRepository.findById(userId).orElseThrow();
        int maxSlots = calculateMaxSlots(user.getLevel());

        var skill = skillRepository.findById(matched.getSkillId()).orElse(null);
        String skillName = skill != null ? skill.getName() : String.valueOf(matched.getSkillId());

        log.info("卸下法决: userId={}, skillId={}, skillName={}", userId, matched.getSkillId(), skillName);

        return SkillSlotResult.builder()
                .success(true)
                .message("已卸下「" + skillName + "」")
                .skill(skill != null ? toSkillVO(matched, skill) : null)
                .equippedCount(equippedSkills.size() - 1)
                .maxSlots(maxSlots)
                .build();
    }

    // ===================== 工具方法 =====================

    private SkillVO toSkillVO(PlayerSkill ps, Skill skill) {
        if (skill == null) return null;
        return new SkillVO(
                ps.getId(),
                skill.getId(),
                skill.getName(),
                skill.getDescription(),
                skill.getEffects(),
                skill.getBindingType() != null ? skill.getBindingType().getCode() : "NONE",
                skill.getBindingType() != null ? skill.getBindingType().getName() : "无",
                skill.getBindingValue(),
                skill.getCooldownSeconds(),
                skill.getLevelRequirement(),
                Boolean.TRUE.equals(ps.getIsEquipped())
        );
    }

    private int calculateMaxSlots(int level) {
        if (level >= 80) return 5;
        if (level >= 60) return 4;
        if (level >= 40) return 3;
        if (level >= 20) return 2;
        return 1;
    }

    private StackableItem resolveJade(List<StackableItem> jadeItems, String input) {
        if (input == null || input.isEmpty()) return null;
        // 按编号匹配
        if (input.matches("\\d+")) {
            int idx = Integer.parseInt(input);
            if (idx >= 1 && idx <= jadeItems.size()) {
                return jadeItems.get(idx - 1);
            }
        }
        // 按名称精确匹配
        for (var item : jadeItems) {
            if (item.getName().equals(input)) return item;
        }
        // 按名称模糊匹配
        var partial = jadeItems.stream()
                .filter(item -> item.getName().contains(input))
                .toList();
        if (partial.size() == 1) return partial.getFirst();
        return null;
    }

    private PlayerSkill resolvePlayerSkill(List<PlayerSkill> playerSkills, String input) {
        if (input == null || input.isEmpty()) return null;

        // 获取所有法决信息用于名称匹配
        var skillIds = playerSkills.stream().map(PlayerSkill::getSkillId).toList();
        var skillMap = skillRepository.findByIds(skillIds).stream()
                .collect(Collectors.toMap(Skill::getId, s -> s));

        // 按编号匹配
        if (input.matches("\\d+")) {
            int idx = Integer.parseInt(input);
            if (idx >= 1 && idx <= playerSkills.size()) {
                return playerSkills.get(idx - 1);
            }
        }

        // 按技能名称精确匹配
        for (var ps : playerSkills) {
            var skill = skillMap.get(ps.getSkillId());
            if (skill != null && skill.getName().equals(input)) return ps;
        }

        // 按技能名称模糊匹配
        var partial = playerSkills.stream()
                .filter(ps -> {
                    var skill = skillMap.get(ps.getSkillId());
                    return skill != null && skill.getName().contains(input);
                })
                .toList();
        if (partial.size() == 1) return partial.getFirst();

        return null;
    }
}
