package top.stillmisty.xiantao.service.sect;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
import top.stillmisty.xiantao.domain.sect.entity.Sect;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.domain.sect.entity.SectSharedSkill;
import top.stillmisty.xiantao.domain.sect.enums.SectSharedSkillStatus;
import top.stillmisty.xiantao.domain.sect.vo.LearnSkillResultVO;
import top.stillmisty.xiantao.domain.sect.vo.SectSharedSkillVO;
import top.stillmisty.xiantao.domain.sect.vo.SharedSkillsQueryVO;
import top.stillmisty.xiantao.domain.sect.vo.SkillOperationResultVO;
import top.stillmisty.xiantao.domain.sect.vo.SubmitJadeResultVO;
import top.stillmisty.xiantao.domain.skill.entity.PlayerSkill;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.PlayerSkillRepository;
import top.stillmisty.xiantao.infrastructure.repository.SectMemberRepository;
import top.stillmisty.xiantao.infrastructure.repository.SectRepository;
import top.stillmisty.xiantao.infrastructure.repository.SectSharedSkillRepository;
import top.stillmisty.xiantao.infrastructure.repository.SkillRepository;
import top.stillmisty.xiantao.infrastructure.repository.StackableItemRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.inventory.StackableItemService;
import top.stillmisty.xiantao.service.player.UserStateService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SectSharedSkillService {

  static final int SKILL_SUBMIT_CONTRIBUTION = 300;

  private final SectRepository sectRepository;
  private final SectMemberRepository sectMemberRepository;
  private final SectSharedSkillRepository sectSharedSkillRepository;
  private final SkillRepository skillRepository;
  private final PlayerSkillRepository playerSkillRepository;
  private final UserStateService userStateService;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemService stackableItemService;
  private final StackableItemRepository stackableItemRepository;
  private final SectBuildingService sectBuildingService;
  private final SectMemberService sectMemberService;

  // ===================== 公开 API =====================

  public ServiceResult<String> getSharedSkills(Long userId) {
    SharedSkillsQueryVO vo = getSharedSkillsInternal(userId);
    return new ServiceResult.Success<>(formatSharedSkillsText(vo));
  }

  @Transactional
  public ServiceResult<String> learnSharedSkill(Long userId, long sharedSkillId) {
    LearnSkillResultVO vo = learnSharedSkillInternal(userId, sharedSkillId);
    return new ServiceResult.Success<>(
        "成功学会「"
            + vo.skillName()
            + "」！消耗 "
            + vo.cost()
            + " 贡献，剩余贡献: "
            + vo.remainingContribution()
            + "。");
  }

  @Transactional
  public ServiceResult<String> submitSkillJade(Long userId, String jadeName) {
    SubmitJadeResultVO vo = submitSkillJadeInternal(userId, jadeName);
    return new ServiceResult.Success<>(
        "已提交「" + vo.skillName() + "」到宗门，获得 " + vo.contributionGained() + " 贡献。需长老/宗主上架后方可学习。");
  }

  @Transactional
  public ServiceResult<String> removeSharedSkill(Long userId, long sharedSkillId) {
    SkillOperationResultVO vo = removeSharedSkillInternal(userId, sharedSkillId);
    return new ServiceResult.Success<>("已下架「" + vo.skillName() + "」，已学习的成员不受影响。");
  }

  @Transactional
  public ServiceResult<String> listSharedSkill(Long userId, long sharedSkillId) {
    SkillOperationResultVO vo = listSharedSkillInternal(userId, sharedSkillId);
    return new ServiceResult.Success<>("已上架「" + vo.skillName() + "」，宗门成员可以学习了。");
  }

  // ===================== 内部 API =====================

  @Cacheable(cacheNames = "sect_shared_skills", key = "#userId")
  public SharedSkillsQueryVO getSharedSkillsInternal(Long userId) {
    SectMember member = requireMember(userId);
    Sect sect =
        sectRepository
            .findById(requireSectId(member))
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    int maxSlots = sectBuildingService.getScriptureSlotCount(sect.getId());
    List<SectSharedSkill> listedSkills =
        sectSharedSkillRepository.findBySectIdAndStatus(
            requireSectId(member), SectSharedSkillStatus.LISTED);

    Map<Long, Skill> skillCache =
        listedSkills.isEmpty()
            ? Map.of()
            : skillRepository
                .findByIds(
                    listedSkills.stream().map(SectSharedSkill::getSkillId).distinct().toList())
                .stream()
                .collect(java.util.stream.Collectors.toMap(Skill::getId, s -> s));

    List<SectSharedSkillVO> skillVOs =
        listedSkills.stream()
            .map(
                ss -> {
                  Skill skill = skillCache.get(ss.getSkillId());
                  String skillName = skill != null ? skill.getName() : "[未知]";
                  int reqLevel =
                      skill != null && skill.getLevelRequirement() != null
                          ? skill.getLevelRequirement()
                          : 0;
                  int cost = reqLevel * 50;
                  return new SectSharedSkillVO(
                      ss.getId(),
                      ss.getSkillId(),
                      skillName,
                      null,
                      reqLevel,
                      cost,
                      ss.getStatus(),
                      null);
                })
            .toList();

    int pendingCount = 0;
    if (member.getPosition().canManageSkills()) {
      pendingCount =
          (int)
              sectSharedSkillRepository.countBySectIdAndStatus(
                  requireSectId(member), SectSharedSkillStatus.PENDING);
    }

    return new SharedSkillsQueryVO(
        member.getContribution(), listedSkills.size(), maxSlots, skillVOs, pendingCount);
  }

  @Transactional
  @CacheEvict(cacheNames = "sect_shared_skills", key = "#userId")
  public LearnSkillResultVO learnSharedSkillInternal(Long userId, long sharedSkillId) {
    SectMember member = requireMember(userId);

    SectSharedSkill sharedSkill =
        sectSharedSkillRepository
            .findById(sharedSkillId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_SHARED_SKILL_NOT_FOUND));

    if (!sharedSkill.getSectId().equals(requireSectId(member))) {
      throw new BusinessException(ErrorCode.SECT_SHARED_SKILL_NOT_FOUND);
    }

    if (sharedSkill.getStatus() != SectSharedSkillStatus.LISTED) {
      throw new BusinessException(ErrorCode.SECT_SHARED_SKILL_NOT_LISTED);
    }

    Skill skill =
        skillRepository
            .findById(sharedSkill.getSkillId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_SHARED_SKILL_NOT_FOUND));

    if (playerSkillRepository.findByUserIdAndSkillId(userId, skill.getId()).isPresent()) {
      throw new BusinessException(ErrorCode.SECT_SKILL_ALREADY_LEARNED, skill.getName());
    }

    var user = userStateService.loadUser(userId);
    if (skill.getLevelRequirement() != null && user.getLevel() < skill.getLevelRequirement()) {
      throw new BusinessException(ErrorCode.SECT_CREATE_LEVEL_INSUFFICIENT);
    }

    int cost = skill.getLevelRequirement() != null ? skill.getLevelRequirement() * 50 : 50;
    if (member.getContribution() < cost) {
      throw new BusinessException(
          ErrorCode.SECT_SHOP_ITEM_INSUFFICIENT_CONTRIBUTION, cost, member.getContribution());
    }

    member.setContribution(member.getContribution() - cost);
    sectMemberRepository.save(member);

    PlayerSkill playerSkill = PlayerSkill.create(userId, skill.getId(), false);
    playerSkill.setSourceSectId(requireSectId(member));
    playerSkillRepository.save(playerSkill);

    log.info("玩家 {} 从宗门 {} 学习共享功法 {}", userId, requireSectId(member), skill.getName());
    return new LearnSkillResultVO(skill.getName(), cost, member.getContribution());
  }

  @Transactional
  @CacheEvict(cacheNames = "sect_shared_skills", key = "#userId")
  public SubmitJadeResultVO submitSkillJadeInternal(Long userId, String jadeName) {
    SectMember member = requireMember(userId);

    List<StackableItem> jadeItems =
        stackableItemRepository.findByUserId(userId).stream()
            .filter(si -> si.getItemType() == ItemType.SKILL_JADE)
            .toList();

    if (jadeItems.isEmpty()) {
      throw new BusinessException(ErrorCode.ITEM_NOT_FOUND, "功法玉简");
    }

    var matchedJade = resolveByName(jadeItems, jadeName, StackableItem::getName);
    if (matchedJade == null) {
      throw new BusinessException(ErrorCode.ITEM_NOT_FOUND, jadeName);
    }

    var template = itemTemplateRepository.findById(matchedJade.getTemplateId()).orElse(null);
    if (template == null
        || !(template.typedProperties() instanceof ItemProperties.SkillJade(long skillId))) {
      throw new BusinessException(ErrorCode.ITEM_CANNOT_USE);
    }

    Skill skill = skillRepository.findById(skillId).orElse(null);
    if (skill == null) {
      throw new BusinessException(ErrorCode.SECT_SHARED_SKILL_NOT_FOUND);
    }

    if (sectSharedSkillRepository
        .findBySectIdAndSkillId(requireSectId(member), skillId)
        .isPresent()) {
      throw new BusinessException(ErrorCode.SECT_SHARED_SKILL_ALREADY_EXISTS);
    }

    int maxSlots = sectBuildingService.getScriptureSlotCount(requireSectId(member));
    long listedCount =
        sectSharedSkillRepository.countBySectIdAndStatus(
            requireSectId(member), SectSharedSkillStatus.LISTED);
    long pendingCount =
        sectSharedSkillRepository.countBySectIdAndStatus(
            requireSectId(member), SectSharedSkillStatus.PENDING);
    if (listedCount + pendingCount >= maxSlots) {
      throw new BusinessException(
          ErrorCode.SECT_SHARED_SKILL_LIMIT, listedCount + pendingCount, maxSlots);
    }

    stackableItemService.reduceStackableItem(userId, matchedJade.getId(), 1);

    SectSharedSkill sharedSkill =
        SectSharedSkill.create()
            .setSectId(requireSectId(member))
            .setSkillId(skillId)
            .setSubmitterUserId(userId)
            .setStatus(SectSharedSkillStatus.PENDING);
    sectSharedSkillRepository.save(sharedSkill);

    member.setContribution(member.getContribution() + SKILL_SUBMIT_CONTRIBUTION);
    sectMemberRepository.save(member);

    log.info("玩家 {} 提交功法玉简 {} 到宗门 {}", userId, skill.getName(), requireSectId(member));
    return new SubmitJadeResultVO(skill.getName(), SKILL_SUBMIT_CONTRIBUTION);
  }

  @Transactional
  @CacheEvict(cacheNames = "sect_shared_skills", key = "#userId")
  public SkillOperationResultVO removeSharedSkillInternal(Long userId, long sharedSkillId) {
    SectMember member = requireMember(userId);
    SectSharedSkill sharedSkill = requireManageableSharedSkill(member, sharedSkillId);

    if (sharedSkill.getStatus() != SectSharedSkillStatus.LISTED) {
      throw new BusinessException(ErrorCode.SECT_SHARED_SKILL_NOT_LISTED);
    }

    sharedSkill.setStatus(SectSharedSkillStatus.PENDING);
    sectSharedSkillRepository.save(sharedSkill);

    Skill skill = skillRepository.findById(sharedSkill.getSkillId()).orElse(null);
    String skillName = skill != null ? skill.getName() : "[" + sharedSkillId + "]";
    return new SkillOperationResultVO(skillName);
  }

  @Transactional
  @CacheEvict(cacheNames = "sect_shared_skills", key = "#userId")
  public SkillOperationResultVO listSharedSkillInternal(Long userId, long sharedSkillId) {
    SectMember member = requireMember(userId);
    SectSharedSkill sharedSkill = requireManageableSharedSkill(member, sharedSkillId);

    if (sharedSkill.getStatus() != SectSharedSkillStatus.PENDING) {
      throw new BusinessException(ErrorCode.SECT_SHARED_SKILL_ALREADY_LISTED);
    }

    int maxSlots = sectBuildingService.getScriptureSlotCount(requireSectId(member));
    long listedCount =
        sectSharedSkillRepository.countBySectIdAndStatus(
            requireSectId(member), SectSharedSkillStatus.LISTED);
    if (listedCount >= maxSlots) {
      throw new BusinessException(ErrorCode.SECT_SHARED_SKILL_LIMIT, listedCount, maxSlots);
    }

    sharedSkill.setStatus(SectSharedSkillStatus.LISTED);
    sectSharedSkillRepository.save(sharedSkill);

    Skill skill = skillRepository.findById(sharedSkill.getSkillId()).orElse(null);
    String skillName = skill != null ? skill.getName() : "[" + sharedSkillId + "]";
    return new SkillOperationResultVO(skillName);
  }

  // ===================== 格式化 =====================

  private static String formatSharedSkillsText(SharedSkillsQueryVO vo) {
    StringBuilder sb = new StringBuilder();
    sb.append("=== 宗门共享功法 ===\n");
    sb.append("功法位: ").append(vo.usedSlots()).append("/").append(vo.maxSlots());
    if (vo.pendingCount() > 0) {
      sb.append(" (待上架: ").append(vo.pendingCount()).append(")");
    }
    sb.append("\n我的贡献: ").append(vo.myContribution()).append("\n");
    sb.append("（学习消耗: 功法等级 × 50 贡献）\n\n");

    if (vo.skills().isEmpty()) {
      sb.append("暂无可学的共享功法。");
      if (vo.pendingCount() > 0) {
        sb.append("\n有 ").append(vo.pendingCount()).append(" 本待上架功法。");
      }
    } else {
      for (var s : vo.skills()) {
        sb.append("  [#")
            .append(s.sharedSkillId())
            .append("] ")
            .append(s.skillName())
            .append(" (Lv")
            .append(s.levelRequirement())
            .append("+ | ")
            .append(s.contributionCost())
            .append("贡献)\n");
      }
    }

    return sb.toString();
  }

  private SectSharedSkill requireManageableSharedSkill(SectMember member, long sharedSkillId) {
    if (!member.getPosition().canManageSkills()) {
      throw new BusinessException(ErrorCode.SECT_NO_PERMISSION, "管理功法");
    }

    SectSharedSkill sharedSkill =
        sectSharedSkillRepository
            .findById(sharedSkillId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_SHARED_SKILL_NOT_FOUND));

    if (!sharedSkill.getSectId().equals(requireSectId(member))) {
      throw new BusinessException(ErrorCode.SECT_SHARED_SKILL_NOT_FOUND);
    }
    return sharedSkill;
  }

  private SectMember requireMember(Long userId) {
    return sectMemberService.requireMember(userId);
  }

  private Long requireSectId(SectMember member) {
    return member.requireSectId();
  }

  private <T> @Nullable T resolveByName(
      List<T> items, String input, Function<T, String> nameExtractor) {
    if (input == null || input.isBlank()) return null;
    for (var item : items) {
      if (nameExtractor.apply(item).equals(input)) return item;
    }
    var partial = items.stream().filter(item -> nameExtractor.apply(item).contains(input)).toList();
    if (partial.size() == 1) return partial.getFirst();
    return null;
  }
}
