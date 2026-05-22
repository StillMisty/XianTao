package top.stillmisty.xiantao.service.sect;

import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.sect.entity.Sect;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.domain.sect.entity.SectSharedSkill;
import top.stillmisty.xiantao.domain.sect.enums.SectSharedSkillStatus;
import top.stillmisty.xiantao.domain.sect.repository.SectMemberRepository;
import top.stillmisty.xiantao.domain.sect.repository.SectRepository;
import top.stillmisty.xiantao.domain.sect.repository.SectSharedSkillRepository;
import top.stillmisty.xiantao.domain.skill.entity.PlayerSkill;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.repository.PlayerSkillRepository;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
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

  // ===================== 公开 API =====================

  @Authenticated
  public ServiceResult<String> getSharedSkills(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getSharedSkills(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> learnSharedSkill(
      PlatformType platform, String openId, long sharedSkillId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(learnSharedSkill(userId, sharedSkillId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> submitSkillJade(
      PlatformType platform, String openId, String jadeName) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(submitSkillJade(userId, jadeName));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> removeSharedSkill(
      PlatformType platform, String openId, long sharedSkillId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(removeSharedSkill(userId, sharedSkillId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> listSharedSkill(
      PlatformType platform, String openId, long sharedSkillId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(listSharedSkill(userId, sharedSkillId));
  }

  // ===================== 内部 API =====================

  @Cacheable(cacheNames = "sect_shared_skills", key = "#userId")
  public String getSharedSkills(Long userId) {
    SectMember member = requireMember(userId);
    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    int maxSlots = sectBuildingService.getScriptureSlotCount(sect.getId());
    List<SectSharedSkill> listedSkills =
        sectSharedSkillRepository.findBySectIdAndStatus(
            member.getSectId(), SectSharedSkillStatus.LISTED);
    long listedCount = listedSkills.size();

    StringBuilder sb = new StringBuilder();
    sb.append("=== 宗门共享功法 ===\n");
    sb.append("功法位: ").append(listedCount).append("/").append(maxSlots);
    if (member.getPosition().canManageSkills()) {
      List<SectSharedSkill> pendingSkills =
          sectSharedSkillRepository.findBySectIdAndStatus(
              member.getSectId(), SectSharedSkillStatus.PENDING);
      if (!pendingSkills.isEmpty()) {
        sb.append(" (待上架: ").append(pendingSkills.size()).append(")");
      }
    }
    sb.append("\n我的贡献: ").append(member.getContribution()).append("\n");
    sb.append("（学习消耗: 功法等级 × 50 贡献）\n\n");

    if (listedSkills.isEmpty()) {
      sb.append("暂无可学的共享功法。");
      if (member.getPosition().canManageSkills()) {
        List<SectSharedSkill> pendingSkills =
            sectSharedSkillRepository.findBySectIdAndStatus(
                member.getSectId(), SectSharedSkillStatus.PENDING);
        if (!pendingSkills.isEmpty()) {
          sb.append("\n有 ").append(pendingSkills.size()).append(" 本待上架功法。");
        }
      }
      return sb.toString();
    }

    for (SectSharedSkill ss : listedSkills) {
      Skill skill = skillRepository.findById(ss.getSkillId()).orElse(null);
      String skillName = skill != null ? skill.getName() : "[未知]";
      int reqLevel =
          skill != null && skill.getLevelRequirement() != null ? skill.getLevelRequirement() : 0;
      int cost = reqLevel * 50;
      sb.append("  [#")
          .append(ss.getId())
          .append("] ")
          .append(skillName)
          .append(" (Lv")
          .append(reqLevel)
          .append("+ | ")
          .append(cost)
          .append("贡献)\n");
    }

    return sb.toString();
  }

  @Transactional
  public String learnSharedSkill(Long userId, long sharedSkillId) {
    SectMember member = requireMember(userId);

    SectSharedSkill sharedSkill =
        sectSharedSkillRepository
            .findById(sharedSkillId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_SHARED_SKILL_NOT_FOUND));

    if (!sharedSkill.getSectId().equals(member.getSectId())) {
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
    playerSkill.setSourceSectId(member.getSectId());
    playerSkillRepository.save(playerSkill);

    log.info("玩家 {} 从宗门 {} 学习共享功法 {}", userId, member.getSectId(), skill.getName());
    return "成功学会「"
        + skill.getName()
        + "」！消耗 "
        + cost
        + " 贡献，剩余贡献: "
        + member.getContribution()
        + "。";
  }

  @Transactional
  public String submitSkillJade(Long userId, String jadeName) {
    SectMember member = requireMember(userId);

    List<top.stillmisty.xiantao.domain.item.entity.StackableItem> jadeItems =
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

    if (sectSharedSkillRepository.findBySectIdAndSkillId(member.getSectId(), skillId).isPresent()) {
      throw new BusinessException(ErrorCode.SECT_SHARED_SKILL_ALREADY_EXISTS);
    }

    int maxSlots = sectBuildingService.getScriptureSlotCount(member.getSectId());
    long listedCount =
        sectSharedSkillRepository.countBySectIdAndStatus(
            member.getSectId(), SectSharedSkillStatus.LISTED);
    long pendingCount =
        sectSharedSkillRepository.countBySectIdAndStatus(
            member.getSectId(), SectSharedSkillStatus.PENDING);
    if (listedCount + pendingCount >= maxSlots) {
      throw new BusinessException(
          ErrorCode.SECT_SHARED_SKILL_LIMIT, listedCount + pendingCount, maxSlots);
    }

    stackableItemService.reduceStackableItem(userId, matchedJade.getId(), 1);

    SectSharedSkill sharedSkill =
        SectSharedSkill.create()
            .setSectId(member.getSectId())
            .setSkillId(skillId)
            .setSubmitterUserId(userId)
            .setStatus(SectSharedSkillStatus.PENDING);
    sectSharedSkillRepository.save(sharedSkill);

    member.setContribution(member.getContribution() + SKILL_SUBMIT_CONTRIBUTION);
    sectMemberRepository.save(member);

    log.info("玩家 {} 提交功法玉简 {} 到宗门 {}", userId, skill.getName(), member.getSectId());
    return "已提交「" + skill.getName() + "」到宗门，获得 " + SKILL_SUBMIT_CONTRIBUTION + " 贡献。需长老/宗主上架后方可学习。";
  }

  @Transactional
  public String removeSharedSkill(Long userId, long sharedSkillId) {
    SectMember member = requireMember(userId);
    SectSharedSkill sharedSkill = requireManageableSharedSkill(member, sharedSkillId);

    if (sharedSkill.getStatus() != SectSharedSkillStatus.LISTED) {
      return "该功法尚未上架。";
    }

    sharedSkill.setStatus(SectSharedSkillStatus.PENDING);
    sectSharedSkillRepository.save(sharedSkill);

    Skill skill = skillRepository.findById(sharedSkill.getSkillId()).orElse(null);
    String skillName = skill != null ? skill.getName() : "[" + sharedSkillId + "]";
    return "已下架「" + skillName + "」，已学习的成员不受影响。";
  }

  @Transactional
  public String listSharedSkill(Long userId, long sharedSkillId) {
    SectMember member = requireMember(userId);
    SectSharedSkill sharedSkill = requireManageableSharedSkill(member, sharedSkillId);

    if (sharedSkill.getStatus() != SectSharedSkillStatus.PENDING) {
      return "该功法已上架或状态异常。";
    }

    int maxSlots = sectBuildingService.getScriptureSlotCount(member.getSectId());
    long listedCount =
        sectSharedSkillRepository.countBySectIdAndStatus(
            member.getSectId(), SectSharedSkillStatus.LISTED);
    if (listedCount >= maxSlots) {
      throw new BusinessException(ErrorCode.SECT_SHARED_SKILL_LIMIT, listedCount, maxSlots);
    }

    sharedSkill.setStatus(SectSharedSkillStatus.LISTED);
    sectSharedSkillRepository.save(sharedSkill);

    Skill skill = skillRepository.findById(sharedSkill.getSkillId()).orElse(null);
    String skillName = skill != null ? skill.getName() : "[" + sharedSkillId + "]";
    return "已上架「" + skillName + "」，宗门成员可以学习了。";
  }

  private SectSharedSkill requireManageableSharedSkill(SectMember member, long sharedSkillId) {
    if (!member.getPosition().canManageSkills()) {
      throw new BusinessException(ErrorCode.SECT_NO_PERMISSION, "管理功法");
    }

    SectSharedSkill sharedSkill =
        sectSharedSkillRepository
            .findById(sharedSkillId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_SHARED_SKILL_NOT_FOUND));

    if (!sharedSkill.getSectId().equals(member.getSectId())) {
      throw new BusinessException(ErrorCode.SECT_SHARED_SKILL_NOT_FOUND);
    }
    return sharedSkill;
  }

  // ===================== 工具方法 =====================

  private SectMember requireMember(Long userId) {
    return sectMemberRepository
        .findByUserId(userId)
        .filter(m -> m.getSectId() != null)
        .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
  }

  private <T> T resolveByName(List<T> items, String input, Function<T, String> nameExtractor) {
    if (input == null || input.isBlank()) return null;
    for (var item : items) {
      if (nameExtractor.apply(item).equals(input)) return item;
    }
    var partial = items.stream().filter(item -> nameExtractor.apply(item).contains(input)).toList();
    if (partial.size() == 1) return partial.getFirst();
    return null;
  }
}
