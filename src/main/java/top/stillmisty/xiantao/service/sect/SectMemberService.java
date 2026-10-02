package top.stillmisty.xiantao.service.sect;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import top.stillmisty.xiantao.domain.sect.entity.Sect;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.domain.sect.enums.SectPosition;
import top.stillmisty.xiantao.domain.sect.vo.DonateResultVO;
import top.stillmisty.xiantao.domain.sect.vo.ExpandMembersResultVO;
import top.stillmisty.xiantao.domain.sect.vo.SectOverviewVO;
import top.stillmisty.xiantao.domain.sect.vo.UpgradeSectResultVO;
import top.stillmisty.xiantao.domain.user.entity.Player;
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
import top.stillmisty.xiantao.service.ai.PromptSanitizer;
import top.stillmisty.xiantao.service.ai.SectIdentityGenerator;
import top.stillmisty.xiantao.service.player.PlayerLoader;

@Slf4j
@Service
@RequiredArgsConstructor
public class SectMemberService {

  static final int SECT_CREATE_COST = 5000;
  static final int SECT_INITIAL_FUNDS = 2000;
  static final int SECT_COOLDOWN_HOURS = 24;
  static final double DONATE_RATE = 0.1;
  static final int SECT_DONATE_MIN = 1000;

  private final SectRepository sectRepository;
  private final SectMemberRepository sectMemberRepository;
  private final SectShopItemRepository sectShopItemRepository;
  private final SectSharedSkillRepository sectSharedSkillRepository;
  private final SectBuildingRepository sectBuildingRepository;
  private final UserRepository userRepository;
  private final PlayerLoader playerLoader;
  private final PlayerSkillRepository playerSkillRepository;
  private final SectIdentityGenerator sectIdentityGenerator;
  private final SpiritStoneService spiritStoneService;
  private final SectLedger sectLedger;
  private final SectEventGenerator sectEventGenerator;
  private final TransactionTemplate transactionTemplate;

  // ===================== 公开 API =====================

  public ServiceResult<SectOverviewVO> getSectOverview(Long userId) {
    return new ServiceResult.Success<>(getSectOverviewInternal(userId));
  }

  /** 建宗编排：校验与 LLM 身份生成在事务外执行（LLM 往返可达数十秒，事务内进行会长时间 占用连接并持有扣款行锁），仅落库+扣款在短事务内完成。 */
  public ServiceResult<String> createSect(Long userId, String name) {
    return new ServiceResult.Success<>(createSectFlow(userId, name, ""));
  }

  public ServiceResult<String> createSectWithEthos(Long userId, String name, String ethosDesc) {
    return new ServiceResult.Success<>(createSectFlow(userId, name, ethosDesc));
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
    playerLoader.loadReadOnly(userId);
    SectMember member = requireMember(userId);
    Sect sect =
        sectRepository
            .findById(requireSectId(member))
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));
    sectEventGenerator.ensureEvent(sect);
    Player leader = playerLoader.loadReadOnly(sect.getLeaderId());
    List<SectMember> members = sectMemberRepository.findBySectId(sect.getId());

    List<Long> memberUserIds = members.stream().map(SectMember::getUserId).distinct().toList();
    Map<Long, Player> memberUserMap =
        memberUserIds.isEmpty()
            ? Map.of()
            : userRepository.findByIds(memberUserIds).stream()
                .collect(Collectors.toMap(Player::getId, u -> u));

    List<SectOverviewVO.MemberEntry> memberEntries =
        members.stream()
            .map(
                m -> {
                  Player memberUser = memberUserMap.get(m.getUserId());
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

  private String createSectFlow(Long userId, String name, String ethosDesc) {
    Player user = playerLoader.loadReadOnly(userId);

    if (CultivationRealm.fromLevel(user.getLevel()).getRank()
        < CultivationRealm.GOLDEN_CORE.getRank()) {
      throw new BusinessException(ErrorCode.SECT_CREATE_LEVEL_INSUFFICIENT);
    }
    if (findActiveMember(userId).isPresent()) {
      throw new BusinessException(ErrorCode.SECT_ALREADY_IN, "已有宗门");
    }
    if (sectRepository.findByName(name).isPresent()) {
      throw new BusinessException(ErrorCode.SECT_NAME_TAKEN, name);
    }

    // LLM 身份生成在事务外执行；玩家可控文本先净化，防 prompt 注入
    String safeEthos = PromptSanitizer.sanitize(ethosDesc == null ? "" : ethosDesc, 200);
    String[] llmResult = sectIdentityGenerator.generate(name, safeEthos);

    // 短事务：扣款 + 落库（宗门名唯一由 uq_sect_name 兜底并发）
    String result =
        transactionTemplate.execute(
            status -> {
              spiritStoneService.withdraw(userId, SECT_CREATE_COST);
              return persistNewSect(userId, name, llmResult);
            });
    return result != null ? result : "宗门创建失败，请稍后再试。";
  }

  private String persistNewSect(Long userId, String name, String[] llmResult) {
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

    // 复用可能存在的退宗冷却记录（user_id 唯一），避免插入冲突
    SectMember member = sectMemberRepository.findByUserId(userId).orElseGet(SectMember::create);
    member.setSectId(sect.getId());
    member.setUserId(userId);
    member.setPosition(SectPosition.LEADER);
    member.setContribution(0);
    member.setCooldownUntil(null);
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

  @Transactional
  public String inviteMemberInternal(Long userId, String targetNickname) {
    SectMember inviterMember = requireMember(userId);
    if (!inviterMember.getPosition().canInvite()) {
      throw new BusinessException(ErrorCode.SECT_NO_PERMISSION, "邀请");
    }

    Player target = playerLoader.findByNickname(targetNickname);
    if (target == null) {
      throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND, targetNickname);
    }

    if (findActiveMember(target.getId()).isPresent()) {
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
        sectMemberRepository.findByUserId(target.getId()).orElseGet(SectMember::create);
    newMember.setSectId(sect.getId());
    newMember.setUserId(target.getId());
    newMember.setPosition(SectPosition.MEMBER);
    newMember.setContribution(0);
    newMember.setCooldownUntil(null);
    sectMemberRepository.save(newMember);

    log.info("玩家 {} 被 {} 邀请加入宗门 {}", target.getId(), userId, sect.getId());
    return "【" + targetNickname + "】已加入宗门！";
  }

  @Transactional
  public String kickMemberInternal(Long userId, String targetNickname) {
    SectMember actorMember = requireMember(userId);

    Player target = playerLoader.findByNickname(targetNickname);
    if (target == null) {
      throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND, targetNickname);
    }

    if (target.getId().equals(userId)) {
      throw new BusinessException(ErrorCode.SECT_CANNOT_KICK_SELF);
    }

    SectMember targetMember =
        findActiveMember(target.getId())
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

    Player target = playerLoader.findByNickname(targetNickname);
    if (target == null) {
      throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND, targetNickname);
    }

    SectMember targetMember =
        findActiveMember(target.getId())
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
    }
    for (SectMember m : members) {
      sectMemberRepository.deleteById(m.getId());
    }

    sectRepository.deleteById(sect.getId());

    log.info("宗门 {} 被宗主 {} 解散", sect.getId(), userId);
    return "宗门【" + sect.getName() + "】已解散。";
  }

  /** 公告长度上限（公告会拼入宗灵 system prompt，过长或含控制字符存在注入与 token 风险） */
  private static final int NOTICE_MAX_LENGTH = 200;

  @Transactional
  public String setNoticeInternal(Long userId, String content) {
    SectMember member = requireMember(userId);
    if (!member.getPosition().canPostNotice()) {
      throw new BusinessException(ErrorCode.SECT_NO_PERMISSION, "发布公告");
    }
    if (content == null || content.isBlank()) {
      throw new BusinessException(ErrorCode.PARAM_INVALID, "公告内容不能为空");
    }

    Sect sect =
        sectRepository
            .findById(requireSectId(member))
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));
    sect.setNotice(PromptSanitizer.sanitize(content, NOTICE_MAX_LENGTH));
    sectRepository.save(sect);

    return "宗门公告已更新。";
  }

  @Transactional
  public DonateResultVO donateStonesInternal(Long userId, long amount) {
    SectMember member = requireMember(userId);

    if (amount < SECT_DONATE_MIN) {
      throw new BusinessException(ErrorCode.SECT_DONATE_TOO_LOW, SECT_DONATE_MIN);
    }

    spiritStoneService.withdraw(userId, amount);

    // 原子累加，防止并发捐献互相覆盖资金/贡献
    Sect sect =
        sectRepository
            .findById(requireSectId(member))
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));
    sectLedger.addFunds(sect.getId(), amount);

    int contributionGain = (int) (amount * DONATE_RATE);
    sectLedger.addContribution(member.getUserId(), contributionGain);

    return new DonateResultVO(contributionGain);
  }

  @Transactional
  public UpgradeSectResultVO upgradeSectInternal(Long userId) {
    SectMember member = requireMember(userId);
    if (!member.getPosition().canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    Sect sect =
        sectRepository
            .findById(requireSectId(member))
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    if (sect.isMaxLevel()) {
      throw new BusinessException(ErrorCode.SECT_UPGRADE_MAX_LEVEL);
    }

    long cost = SectLedger.upgradeCost(sect.getLevel());

    // 原子条件扣款，防止并发升级双花资金
    if (!sectLedger.deductFundsIfEnough(sect.getId(), cost)) {
      throw new BusinessException(ErrorCode.SECT_FUNDS_INSUFFICIENT, cost, sect.getFunds());
    }

    int oldLevel = sect.getLevel();
    sect.setLevel(oldLevel + 1);
    sect.setMaxMembers(sect.getMaxMembers() + 5);
    sectRepository.save(sect);

    log.info("宗门 {} 升级至 Lv.{}，消耗 {} 资金", sect.getId(), sect.getLevel(), cost);
    return new UpgradeSectResultVO(
        sect.getLevel(), sect.getMaxMembers(), cost, sect.getFunds() - cost);
  }

  @Transactional
  public ExpandMembersResultVO expandMembersInternal(Long userId) {
    SectMember member = requireMember(userId);
    if (!member.getPosition().canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    Sect sect =
        sectRepository
            .findById(requireSectId(member))
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    int slots = SectLedger.EXPAND_SLOTS;
    long cost = (long) slots * SectLedger.EXPAND_COST_PER_SLOT;

    if (!sectLedger.deductFundsIfEnough(sect.getId(), cost)) {
      throw new BusinessException(ErrorCode.SECT_FUNDS_INSUFFICIENT, cost, sect.getFunds());
    }

    sect.setMaxMembers(sect.getMaxMembers() + slots);
    sectRepository.save(sect);

    log.info("宗门 {} 扩充成员上限至 {}", sect.getId(), sect.getMaxMembers());
    return new ExpandMembersResultVO(slots, sect.getMaxMembers(), cost, sect.getFunds() - cost);
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
    return findActiveMember(userId).orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
  }

  /** 当前有效成员记录：退宗/被踢后保留的冷却记录 sect_id 为空，不计入成员 */
  private Optional<SectMember> findActiveMember(Long userId) {
    return sectMemberRepository.findByUserId(userId).filter(m -> m.getSectId() != null);
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
  }
}
