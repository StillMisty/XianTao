package top.stillmisty.xiantao.service.sect;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.sect.entity.Sect;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.domain.sect.enums.SectPosition;
import top.stillmisty.xiantao.domain.sect.vo.DonateResultVO;
import top.stillmisty.xiantao.domain.sect.vo.ExpandMembersResultVO;
import top.stillmisty.xiantao.domain.sect.vo.SectOverviewVO;
import top.stillmisty.xiantao.domain.sect.vo.TasksQueryVO;
import top.stillmisty.xiantao.domain.sect.vo.UpgradeSectResultVO;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.CultivationRealm;
import top.stillmisty.xiantao.infrastructure.repository.PlayerSkillRepository;
import top.stillmisty.xiantao.infrastructure.repository.SectBuildingRepository;
import top.stillmisty.xiantao.infrastructure.repository.SectMemberRepository;
import top.stillmisty.xiantao.infrastructure.repository.SectRepository;
import top.stillmisty.xiantao.infrastructure.repository.SectSharedSkillRepository;
import top.stillmisty.xiantao.infrastructure.repository.SectShopItemRepository;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.masterapprentice.MasterApprenticeService;
import top.stillmisty.xiantao.service.player.UserStateService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SectMemberService {

  static final int SECT_CREATE_COST = 5000;
  static final int SECT_INITIAL_FUNDS = 2000;
  static final int SECT_COOLDOWN_HOURS = 24;
  static final double DONATE_RATE = 0.1;

  private final SectRepository sectRepository;
  private final SectMemberRepository sectMemberRepository;
  private final SectShopItemRepository sectShopItemRepository;
  private final SectSharedSkillRepository sectSharedSkillRepository;
  private final SectBuildingRepository sectBuildingRepository;
  private final UserRepository userRepository;
  private final UserStateService userStateService;
  private final PlayerSkillRepository playerSkillRepository;
  private final ChatClient npcChatClient;

  @Lazy private final MasterApprenticeService masterApprenticeService;

  private final SpiritStoneService spiritStoneService;

  // ===================== 公开 API =====================

  public ServiceResult<SectOverviewVO> getSectOverview(Long userId) {
    return new ServiceResult.Success<>(getSectOverviewInternal(userId));
  }

  @Transactional
  public ServiceResult<String> createSect(Long userId, String name) {
    return new ServiceResult.Success<>(createSectInternal(userId, name, ""));
  }

  @Transactional
  public ServiceResult<String> createSectWithEthos(Long userId, String name, String ethosDesc) {
    return new ServiceResult.Success<>(createSectInternal(userId, name, ethosDesc));
  }

  @Transactional
  public ServiceResult<String> inviteMember(Long userId, String targetNickname) {
    return new ServiceResult.Success<>(inviteMemberInternal(userId, targetNickname));
  }

  @Transactional
  public ServiceResult<String> kickMember(Long userId, String targetNickname) {
    return new ServiceResult.Success<>(kickMemberInternal(userId, targetNickname));
  }

  @Transactional
  public ServiceResult<String> leaveSect(Long userId) {
    return new ServiceResult.Success<>(leaveSectInternal(userId));
  }

  @Transactional
  public ServiceResult<String> appointMember(
      Long userId, String targetNickname, String positionCode) {
    return new ServiceResult.Success<>(appointMemberInternal(userId, targetNickname, positionCode));
  }

  @Transactional
  public ServiceResult<String> dismissSect(Long userId) {
    return new ServiceResult.Success<>(dismissSectInternal(userId));
  }

  @Transactional
  public ServiceResult<String> setNotice(Long userId, String content) {
    return new ServiceResult.Success<>(setNoticeInternal(userId, content));
  }

  @Transactional
  public ServiceResult<String> donateStones(Long userId, long amount) {
    DonateResultVO vo = donateStonesInternal(userId, amount);
    return new ServiceResult.Success<>(
        "捐献成功！消耗 " + amount + " 灵石，获得 " + vo.contributionGained() + " 贡献值，宗门资金 +" + amount + "。");
  }

  public ServiceResult<String> getTasks(Long userId) {
    TasksQueryVO vo = getTasksInternal(userId);
    return new ServiceResult.Success<>(
        vo.tasks().isEmpty() ? "暂无进行中的宗门事件任务。" : "有 " + vo.tasks().size() + " 个进行中的任务。");
  }

  @Transactional
  public ServiceResult<String> upgradeSect(Long userId) {
    UpgradeSectResultVO vo = upgradeSectInternal(userId);
    return new ServiceResult.Success<>(
        "宗门升级成功！当前等级 Lv."
            + vo.newLevel()
            + "，成员上限 "
            + vo.newMaxMembers()
            + "，消耗资金 "
            + vo.cost()
            + "（剩余 "
            + vo.remainingFunds()
            + "）。");
  }

  @Transactional
  public ServiceResult<String> expandMembers(Long userId) {
    ExpandMembersResultVO vo = expandMembersInternal(userId);
    return new ServiceResult.Success<>(
        "扩充成功！成员上限 +"
            + vo.addedSlots()
            + "（当前 "
            + vo.newMaxMembers()
            + "），消耗资金 "
            + vo.cost()
            + "（剩余 "
            + vo.remainingFunds()
            + "）。");
  }

  // ===================== 内部 API =====================

  public SectOverviewVO getSectOverviewInternal(Long userId) {
    userStateService.loadUserReadOnly(userId);
    SectMember member = requireMember(userId);
    Sect sect =
        sectRepository
            .findById(requireSectId(member))
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));
    User leader = userStateService.loadUserReadOnly(sect.getLeaderId());
    List<SectMember> members = sectMemberRepository.findBySectId(sect.getId());

    List<Long> memberUserIds = members.stream().map(SectMember::getUserId).distinct().toList();
    Map<Long, User> memberUserMap =
        memberUserIds.isEmpty()
            ? Map.of()
            : userRepository.findByIds(memberUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

    List<SectOverviewVO.MemberEntry> memberEntries =
        members.stream()
            .map(
                m -> {
                  User memberUser = memberUserMap.get(m.getUserId());
                  if (memberUser == null) return null;
                  return new SectOverviewVO.MemberEntry(
                      m.getPosition().getName(),
                      memberUser.getNickname(),
                      memberUser.getLevel(),
                      m.getUserId().equals(userId));
                })
            .filter(java.util.Objects::nonNull)
            .toList();

    return new SectOverviewVO(
        sect.getName(),
        sect.getVerse(),
        sect.getLevel(),
        leader.getNickname(),
        members.size(),
        sect.getMaxMembers(),
        sect.getFunds(),
        member.getContribution(),
        member.getPosition().getName(),
        sect.getDescription(),
        sect.getNotice(),
        sect.getLastEventText(),
        memberEntries);
  }

  @Transactional
  public String createSectInternal(Long userId, String name, String ethosDesc) {
    User user = userStateService.loadUser(userId);

    if (CultivationRealm.fromLevel(user.getLevel()).getRank()
        < CultivationRealm.GOLDEN_CORE.getRank()) {
      throw new BusinessException(ErrorCode.SECT_CREATE_LEVEL_INSUFFICIENT);
    }

    if (sectMemberRepository.findByUserId(userId).isPresent()) {
      throw new BusinessException(ErrorCode.SECT_ALREADY_IN, "已有宗门");
    }

    if (sectRepository.findByName(name).isPresent()) {
      throw new BusinessException(ErrorCode.SECT_NAME_TAKEN, name);
    }

    spiritStoneService.withdraw(userId, SECT_CREATE_COST);

    String[] llmResult = generateSectIdentity(name, ethosDesc);

    Sect sect =
        Sect.create()
            .setName(name)
            .setLeaderId(userId)
            .setLevel(1)
            .setFunds((long) SECT_INITIAL_FUNDS)
            .setMaxMembers(10)
            .setVerse(llmResult[0])
            .setEthos(llmResult[1])
            .setSpiritPersonality(llmResult[2])
            .setDescription(llmResult[1]);
    sectRepository.save(sect);

    SectMember member =
        SectMember.create()
            .setSectId(sect.getId())
            .setUserId(userId)
            .setPosition(SectPosition.LEADER)
            .setContribution(0);
    sectMemberRepository.save(member);

    log.info("玩家 {} 创建宗门 {} (id={})", userId, name, sect.getId());

    StringBuilder sb = new StringBuilder();
    sb.append("宗门【").append(name).append("】创建成功！你已成为宗主。\n");
    if (sect.getVerse() != null && !sect.getVerse().isBlank()) {
      sb.append("「").append(sect.getVerse()).append("」\n");
    }
    sb.append("初始资金: ").append(SECT_INITIAL_FUNDS).append(" 灵石。");
    return sb.toString();
  }

  private String[] generateSectIdentity(String name, String ethosDesc) {
    try {
      String prompt =
          """
                你是一位修仙世界的宗门命名师。请根据以下信息为宗门生成诗号、道统和宗灵人格。

                宗门名称：%s
                道统描述：%s

                请严格按以下格式回复（每行一项）：
                诗号：xxx
                道统：xxx
                宗灵人格：xxx

                要求：
                - 诗号：4-7言对仗句，体现宗门气质
                - 道统：100字以内的宗门修行理念简述
                - 宗灵人格：50字以内的宗灵人格种子描述
                """
              .formatted(
                  name,
                  ethosDesc != null && !ethosDesc.isBlank() ? ethosDesc : "无特殊描述，请根据宗门名称自由发挥");

      String response =
          npcChatClient.prompt().system("你是修仙世界的宗门命名师，擅长为宗门赋予灵性身份。").user(prompt).call().content();

      if (response == null || response.isBlank()) {
        return new String[] {
          "", "以" + name + "之名，问道长生。", "沉稳大气的宗门意志",
        };
      }

      String verse = "";
      String ethos = "";
      String personality = "";

      for (String line : response.lines().toList()) {
        String trimmed = line.trim();
        if (trimmed.startsWith("诗号：") || trimmed.startsWith("诗号:")) {
          verse = extractSuffix(trimmed);
        } else if (trimmed.startsWith("道统：") || trimmed.startsWith("道统:")) {
          ethos = extractSuffix(trimmed);
        } else if (trimmed.startsWith("宗灵人格：") || trimmed.startsWith("宗灵人格:")) {
          personality = extractSuffix(trimmed);
        }
      }

      if (verse.isBlank()) verse = "";
      if (ethos.isBlank()) ethos = "以" + name + "之名，问道长生。";
      if (personality.isBlank()) personality = "沉稳大气的宗门意志";

      if (verse.length() > 100) verse = verse.substring(0, 100);
      if (personality.length() > 100) personality = personality.substring(0, 100);

      return new String[] {verse, ethos, personality};
    } catch (Exception e) {
      log.warn("LLM 生成宗门身份失败，使用默认值", e);
      return new String[] {
        "", "以" + name + "之名，问道长生。", "沉稳大气的宗门意志",
      };
    }
  }

  private static String extractSuffix(String trimmed) {
    int fullWidthIndex = trimmed.indexOf('：');
    if (fullWidthIndex >= 0) {
      return trimmed.substring(fullWidthIndex + 1).trim();
    }
    int halfWidthIndex = trimmed.indexOf(':');
    if (halfWidthIndex >= 0) {
      return trimmed.substring(halfWidthIndex + 1).trim();
    }
    return trimmed;
  }

  @Transactional
  public String inviteMemberInternal(Long userId, String targetNickname) {
    SectMember inviterMember = requireMember(userId);
    if (!inviterMember.getPosition().canInvite()) {
      throw new BusinessException(ErrorCode.SECT_NO_PERMISSION, "邀请");
    }

    User target = userStateService.loadUserByNickname(targetNickname);
    if (target == null) {
      throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND, targetNickname);
    }

    if (sectMemberRepository.findByUserId(target.getId()).isPresent()) {
      throw new BusinessException(ErrorCode.SECT_ALREADY_IN, "已在他宗");
    }

    if (isOnCooldown(target.getId())) {
      throw new BusinessException(ErrorCode.SECT_COOLDOWN, SECT_COOLDOWN_HOURS);
    }

    Sect sect =
        sectRepository
            .findById(requireSectId(inviterMember))
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    long memberCount = sectMemberRepository.countBySectId(sect.getId());
    if (memberCount >= sect.getMaxMembers()) {
      throw new BusinessException(ErrorCode.SECT_FULL, sect.getMaxMembers());
    }

    SectMember newMember =
        SectMember.create()
            .setSectId(sect.getId())
            .setUserId(target.getId())
            .setPosition(SectPosition.MEMBER)
            .setContribution(0);
    sectMemberRepository.save(newMember);

    masterApprenticeService.syncSectForMaster(target.getId(), sect.getId());

    log.info("玩家 {} 被 {} 邀请加入宗门 {}", target.getId(), userId, sect.getId());
    return "【" + targetNickname + "】已加入宗门！";
  }

  @Transactional
  public String kickMemberInternal(Long userId, String targetNickname) {
    SectMember actorMember = requireMember(userId);

    User target = userStateService.loadUserByNickname(targetNickname);
    if (target == null) {
      throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND, targetNickname);
    }

    if (target.getId().equals(userId)) {
      throw new BusinessException(ErrorCode.SECT_CANNOT_KICK_SELF);
    }

    SectMember targetMember =
        sectMemberRepository
            .findByUserId(target.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_SAME));

    if (targetMember.getPosition() == SectPosition.LEADER) {
      throw new BusinessException(ErrorCode.SECT_CANNOT_KICK_LEADER);
    }

    if (!requireSectId(targetMember).equals(requireSectId(actorMember))) {
      throw new BusinessException(ErrorCode.SECT_NOT_SAME);
    }

    if (!actorMember.getPosition().canKick()
        || !actorMember.getPosition().isHigherThan(targetMember.getPosition())) {
      throw new BusinessException(ErrorCode.SECT_CANNOT_KICK_SAME_OR_HIGHER);
    }

    executeLeave(target.getId(), targetMember);

    log.info("玩家 {} 被 {} 踢出宗门 {}", target.getId(), userId, requireSectId(targetMember));
    return ("已将【" + targetNickname + "】踢出宗门，" + SECT_COOLDOWN_HOURS + " 小时内无法加入新宗门。");
  }

  @Transactional
  public String leaveSectInternal(Long userId) {
    SectMember member = requireMember(userId);

    if (member.getPosition() == SectPosition.LEADER) {
      long memberCount = sectMemberRepository.countBySectId(requireSectId(member));
      if (memberCount > 1) {
        throw new BusinessException(ErrorCode.SECT_DISSOLVE_HAS_MEMBERS, memberCount - 1);
      }
      sectRepository.deleteById(requireSectId(member));
    }

    executeLeave(userId, member);

    log.info("玩家 {} 退出宗门 {}", userId, requireSectId(member));
    return ("你已退出宗门，贡献值清零，已学共享功法已遗忘，" + SECT_COOLDOWN_HOURS + " 小时内无法加入新宗门。");
  }

  @Transactional
  public String appointMemberInternal(Long userId, String targetNickname, String positionCode) {
    SectMember actorMember = requireMember(userId);
    if (!actorMember.getPosition().canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    User target = userStateService.loadUserByNickname(targetNickname);
    if (target == null) {
      throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND, targetNickname);
    }

    SectMember targetMember =
        sectMemberRepository
            .findByUserId(target.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_SAME));

    if (!requireSectId(targetMember).equals(requireSectId(actorMember))) {
      throw new BusinessException(ErrorCode.SECT_NOT_SAME);
    }

    SectPosition newPosition;
    try {
      newPosition = SectPosition.fromCode(positionCode);
    } catch (IllegalArgumentException e) {
      throw new BusinessException(ErrorCode.SECT_POSITION_INVALID, positionCode);
    }

    if (newPosition == SectPosition.LEADER) {
      if (targetMember.getPosition() == SectPosition.LEADER) {
        return "【" + targetNickname + "】已经是宗主。";
      }
      Sect sect =
          sectRepository
              .findById(requireSectId(actorMember))
              .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));
      actorMember.setPosition(SectPosition.ELDER);
      sectMemberRepository.save(actorMember);
      targetMember.setPosition(SectPosition.LEADER);
      sectMemberRepository.save(targetMember);
      sect.setLeaderId(target.getId());
      sectRepository.save(sect);
      log.info("玩家 {} 将宗门 {} 的宗主之位传给 {}", userId, requireSectId(actorMember), target.getId());
      return "已将宗主之位传给【" + targetNickname + "】，你已成为长老。";
    }

    targetMember.setPosition(newPosition);
    sectMemberRepository.save(targetMember);

    log.info(
        "玩家 {} 被任命为宗门 {} 的 {}", target.getId(), requireSectId(actorMember), newPosition.getName());
    return ("已将【" + targetNickname + "】任命为" + newPosition.getName() + "。");
  }

  @Transactional
  public String dismissSectInternal(Long userId) {
    SectMember member = requireMember(userId);
    if (member.getPosition() != SectPosition.LEADER) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    Sect sect =
        sectRepository
            .findById(requireSectId(member))
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    sectShopItemRepository.deleteBySectId(sect.getId());
    sectSharedSkillRepository.deleteBySectId(sect.getId());
    sectBuildingRepository.deleteBySectId(sect.getId());

    List<SectMember> members = sectMemberRepository.findBySectId(sect.getId());
    for (SectMember m : members) {
      forgetSharedSkills(m.getUserId(), sect.getId());
      masterApprenticeService.handleMasterSectLeave(m.getUserId());
    }
    for (SectMember m : members) {
      sectMemberRepository.deleteById(m.getId());
    }

    sectRepository.deleteById(sect.getId());

    log.info("宗门 {} 被宗主 {} 解散", sect.getId(), userId);
    return "宗门【" + sect.getName() + "】已解散。";
  }

  @Transactional
  public String setNoticeInternal(Long userId, String content) {
    SectMember member = requireMember(userId);
    if (!member.getPosition().canPostNotice()) {
      throw new BusinessException(ErrorCode.SECT_NO_PERMISSION, "发布公告");
    }

    Sect sect =
        sectRepository
            .findById(requireSectId(member))
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));
    sect.setNotice(content);
    sectRepository.save(sect);

    return "宗门公告已更新。";
  }

  @Transactional
  public DonateResultVO donateStonesInternal(Long userId, long amount) {
    SectMember member = requireMember(userId);

    if (amount <= 0) {
      throw new BusinessException(ErrorCode.PARAM_INVALID, "捐献灵石必须大于0");
    }

    spiritStoneService.withdraw(userId, amount);

    Sect sect =
        sectRepository
            .findById(requireSectId(member))
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));
    sect.addFunds(amount);
    sectRepository.save(sect);

    int contributionGain = (int) (amount * DONATE_RATE);
    member.setContribution(member.getContribution() + contributionGain);
    sectMemberRepository.save(member);

    return new DonateResultVO(contributionGain);
  }

  public TasksQueryVO getTasksInternal(Long userId) {
    requireMember(userId);
    return new TasksQueryVO(java.util.List.of());
  }

  @Transactional
  public UpgradeSectResultVO upgradeSectInternal(Long userId) {
    SectMember member = requireMember(userId);
    if (member.getPosition().canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    Sect sect =
        sectRepository
            .findById(requireSectId(member))
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    if (sect.isMaxLevel()) {
      throw new BusinessException(ErrorCode.SECT_UPGRADE_MAX_LEVEL);
    }

    long cost =
        switch (sect.getLevel()) {
          case 1 -> 5000;
          case 2 -> 15000;
          case 3 -> 30000;
          case 4 -> 50000;
          default -> Long.MAX_VALUE;
        };

    sect.deductFundsOrThrow(cost);

    int oldLevel = sect.getLevel();
    sect.setLevel(oldLevel + 1);
    sect.setMaxMembers(sect.getMaxMembers() + 5);
    sectRepository.save(sect);

    log.info("宗门 {} 升级至 Lv.{}，消耗 {} 资金", sect.getId(), sect.getLevel(), cost);
    return new UpgradeSectResultVO(sect.getLevel(), sect.getMaxMembers(), cost, sect.getFunds());
  }

  @Transactional
  public ExpandMembersResultVO expandMembersInternal(Long userId) {
    SectMember member = requireMember(userId);
    if (member.getPosition().canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    Sect sect =
        sectRepository
            .findById(requireSectId(member))
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    int slots = 5;
    long cost = slots * 500L;

    sect.deductFundsOrThrow(cost);

    sect.setMaxMembers(sect.getMaxMembers() + slots);
    sectRepository.save(sect);

    log.info("宗门 {} 扩充成员上限至 {}", sect.getId(), sect.getMaxMembers());
    return new ExpandMembersResultVO(slots, sect.getMaxMembers(), cost, sect.getFunds());
  }

  // ===================== 跨服务接口 =====================

  @Transactional
  public void joinSectInternal(Long userId, Long sectId) {
    sectMemberRepository.deleteByUserId(userId);
    SectMember member =
        SectMember.create()
            .setSectId(sectId)
            .setUserId(userId)
            .setPosition(SectPosition.MEMBER)
            .setContribution(0);
    sectMemberRepository.save(member);
  }

  @Transactional
  public void removeFromSect(Long userId) {
    sectMemberRepository.deleteByUserId(userId);
  }

  // ===================== 包内工具方法 =====================

  public SectMember requireMember(Long userId) {
    return sectMemberRepository
        .findByUserId(userId)
        .filter(m -> m.getSectId() != null)
        .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
  }

  private Long requireSectId(SectMember member) {
    return member.requireSectId();
  }

  boolean isOnCooldown(Long userId) {
    return sectMemberRepository
        .findByUserId(userId)
        .filter(m -> m.getSectId() == null)
        .map(SectMember::isOnCooldown)
        .orElse(false);
  }

  @Transactional
  public void forgetSharedSkills(Long userId, Long sectId) {
    playerSkillRepository.deleteByUserIdAndSourceSectId(userId, sectId);
  }

  @Transactional
  void executeLeave(Long userId, SectMember member) {
    sectMemberRepository.deleteByUserId(userId);

    forgetSharedSkills(userId, requireSectId(member));

    sectMemberRepository.save(
        SectMember.create()
            .setUserId(userId)
            .setSectId(null)
            .setContribution(0)
            .setCooldownUntil(TimeUtil.now().plusHours(SECT_COOLDOWN_HOURS)));

    masterApprenticeService.handleMasterSectLeave(userId);
  }
}
