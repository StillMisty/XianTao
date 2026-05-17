package top.stillmisty.xiantao.service.sect;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.sect.entity.Sect;
import top.stillmisty.xiantao.domain.sect.entity.SectBuilding;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.domain.sect.entity.SectSharedSkill;
import top.stillmisty.xiantao.domain.sect.entity.SectShopItem;
import top.stillmisty.xiantao.domain.sect.enums.SectBuildingType;
import top.stillmisty.xiantao.domain.sect.enums.SectPosition;
import top.stillmisty.xiantao.domain.sect.enums.SectSharedSkillStatus;
import top.stillmisty.xiantao.domain.sect.repository.SectBuildingRepository;
import top.stillmisty.xiantao.domain.sect.repository.SectMemberRepository;
import top.stillmisty.xiantao.domain.sect.repository.SectRepository;
import top.stillmisty.xiantao.domain.sect.repository.SectSharedSkillRepository;
import top.stillmisty.xiantao.domain.sect.repository.SectShopItemRepository;
import top.stillmisty.xiantao.domain.skill.entity.PlayerSkill;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.repository.PlayerSkillRepository;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.CultivationRealm;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.inventory.StackableItemService;
import top.stillmisty.xiantao.service.masterapprentice.MasterApprenticeService;
import top.stillmisty.xiantao.service.player.UserStateService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SectService {

  static final int SECT_CREATE_COST = 5000;
  static final int SECT_INITIAL_FUNDS = 2000;
  static final int SECT_COOLDOWN_HOURS = 24;
  static final int SKILL_SUBMIT_CONTRIBUTION = 300;
  static final double DONATE_RATE = 0.1;
  static final int DEFAULT_SCRIPTURE_SLOTS = 3;
  static final int SCRIPTURE_SLOTS_PER_LEVEL = 3;

  private final SectRepository sectRepository;
  private final SectMemberRepository sectMemberRepository;
  private final SectSharedSkillRepository sectSharedSkillRepository;
  private final SectShopItemRepository sectShopItemRepository;
  private final SectBuildingRepository sectBuildingRepository;
  private final UserRepository userRepository;
  private final UserStateService userStateService;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemService stackableItemService;
  private final StackableItemRepository stackableItemRepository;
  private final SkillRepository skillRepository;
  private final PlayerSkillRepository playerSkillRepository;
  private final ChatClient npcChatClient;
  @Lazy private final MasterApprenticeService masterApprenticeService;

  // ===================== 公开 API =====================

  @Authenticated
  public ServiceResult<String> getSectOverview(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getSectOverview(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> createSect(PlatformType platform, String openId, String name) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(createSect(userId, name, ""));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> createSectWithEthos(
      PlatformType platform, String openId, String name, String ethosDesc) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(createSect(userId, name, ethosDesc));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> inviteMember(
      PlatformType platform, String openId, String targetNickname) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(inviteMember(userId, targetNickname));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> kickMember(
      PlatformType platform, String openId, String targetNickname) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(kickMember(userId, targetNickname));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> leaveSect(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(leaveSect(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> appointMember(
      PlatformType platform, String openId, String targetNickname, String positionCode) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(appointMember(userId, targetNickname, positionCode));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> dismissSect(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(dismissSect(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> setNotice(PlatformType platform, String openId, String content) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(setNotice(userId, content));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> donateStones(PlatformType platform, String openId, long amount) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(donateStones(userId, amount));
  }

  @Authenticated
  public ServiceResult<String> getShop(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getShop(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> exchangeShopItem(
      PlatformType platform, String openId, long shopItemId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(exchangeShopItem(userId, shopItemId));
  }

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

  @Authenticated
  public ServiceResult<String> getTasks(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getTasks(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> upgradeSect(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(upgradeSect(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> expandMembers(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(expandMembers(userId));
  }

  @Authenticated
  public ServiceResult<String> getBuildings(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getBuildings(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> buildStructure(
      PlatformType platform, String openId, String buildingTypeCode) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(buildStructure(userId, buildingTypeCode));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> upgradeBuilding(
      PlatformType platform, String openId, String buildingTypeCode) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(upgradeBuilding(userId, buildingTypeCode));
  }

  // ===================== 内部 API =====================

  public String getSectOverview(Long userId) {
    User user = userStateService.loadUser(userId);
    SectMember member = requireMember(userId);
    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));
    User leader = userStateService.loadUser(sect.getLeaderId());
    List<SectMember> members = sectMemberRepository.findBySectId(sect.getId());

    StringBuilder sb = new StringBuilder();
    sb.append("=== ").append(sect.getName()).append(" ===\n");
    if (sect.getVerse() != null && !sect.getVerse().isBlank()) {
      sb.append("「").append(sect.getVerse()).append("」\n");
    }
    sb.append("等级: Lv.").append(sect.getLevel()).append("\n");
    sb.append("宗主: ").append(leader.getNickname()).append("\n");
    sb.append("成员: ").append(members.size()).append("/").append(sect.getMaxMembers()).append("\n");
    sb.append("资金: ").append(sect.getFunds()).append(" 灵石\n");
    sb.append("我的贡献: ").append(member.getContribution()).append("\n");
    sb.append("我的职位: ").append(member.getPosition().getName()).append("\n");
    if (sect.getDescription() != null && !sect.getDescription().isBlank()) {
      sb.append("简介: ").append(sect.getDescription()).append("\n");
    }
    if (sect.getNotice() != null && !sect.getNotice().isBlank()) {
      sb.append("公告: ").append(sect.getNotice()).append("\n");
    }
    if (sect.getLastEventText() != null && !sect.getLastEventText().isBlank()) {
      sb.append("当前事件: ").append(sect.getLastEventText()).append("\n");
    }

    sb.append("\n成员列表:\n");
    for (SectMember m : members) {
      User memberUser = userStateService.loadUser(m.getUserId());
      sb.append("  ")
          .append(m.getPosition().getName())
          .append(" ")
          .append(memberUser.getNickname())
          .append(" (Lv.")
          .append(memberUser.getLevel())
          .append(")");
      if (m.getUserId().equals(userId)) {
        sb.append(" [我]");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  public String createSect(Long userId, String name, String ethosDesc) {
    User user = userStateService.loadUserForUpdate(userId);

    if (CultivationRealm.fromLevel(user.getLevel()).ordinal()
        < CultivationRealm.GOLDEN_CORE.ordinal()) {
      throw new BusinessException(ErrorCode.SECT_CREATE_LEVEL_INSUFFICIENT);
    }

    if (sectMemberRepository.findByUserId(userId).isPresent()) {
      throw new BusinessException(ErrorCode.SECT_ALREADY_IN, "已有宗门");
    }

    if (sectRepository.findByName(name).isPresent()) {
      throw new BusinessException(ErrorCode.SECT_NAME_TAKEN, name);
    }

    if (user.getSpiritStones() < SECT_CREATE_COST) {
      throw new BusinessException(ErrorCode.SECT_CREATE_FUNDS_INSUFFICIENT, SECT_CREATE_COST);
    }

    user.setSpiritStones(user.getSpiritStones() - SECT_CREATE_COST);
    userRepository.save(user);

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
    if (!sect.getVerse().isBlank()) {
      sb.append("「").append(sect.getVerse()).append("」\n");
    }
    sb.append("初始资金: ").append(SECT_INITIAL_FUNDS).append(" 灵石。");
    return sb.toString();
  }

  /** 使用 LLM 生成宗门身份 */
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
        return new String[] {"", "以" + name + "之名，问道长生。", "沉稳大气的宗门意志"};
      }

      String verse = "";
      String ethos = "";
      String personality = "";

      for (String line : response.split("\n")) {
        String trimmed = line.trim();
        if (trimmed.startsWith("诗号：") || trimmed.startsWith("诗号:")) {
          verse = trimmed.substring(trimmed.indexOf('：') + 1).trim();
          if (verse.isEmpty()) verse = trimmed.substring(trimmed.indexOf(':') + 1).trim();
        } else if (trimmed.startsWith("道统：") || trimmed.startsWith("道统:")) {
          ethos = trimmed.substring(trimmed.indexOf('：') + 1).trim();
          if (ethos.isEmpty()) ethos = trimmed.substring(trimmed.indexOf(':') + 1).trim();
        } else if (trimmed.startsWith("宗灵人格：") || trimmed.startsWith("宗灵人格:")) {
          personality = trimmed.substring(trimmed.indexOf('：') + 1).trim();
          if (personality.isEmpty())
            personality = trimmed.substring(trimmed.indexOf(':') + 1).trim();
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
      return new String[] {"", "以" + name + "之名，问道长生。", "沉稳大气的宗门意志"};
    }
  }

  public String inviteMember(Long userId, String targetNickname) {
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
            .findById(inviterMember.getSectId())
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

  public String kickMember(Long userId, String targetNickname) {
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

    if (!targetMember.getSectId().equals(actorMember.getSectId())) {
      throw new BusinessException(ErrorCode.SECT_NOT_SAME);
    }

    if (!actorMember.getPosition().canKick()
        || !actorMember.getPosition().isHigherThan(targetMember.getPosition())) {
      throw new BusinessException(ErrorCode.SECT_CANNOT_KICK_SAME_OR_HIGHER);
    }

    executeLeave(target.getId(), targetMember);

    log.info("玩家 {} 被 {} 踢出宗门 {}", target.getId(), userId, targetMember.getSectId());
    return "已将【" + targetNickname + "】踢出宗门，" + SECT_COOLDOWN_HOURS + " 小时内无法加入新宗门。";
  }

  public String leaveSect(Long userId) {
    SectMember member = requireMember(userId);

    if (member.getPosition() == SectPosition.LEADER) {
      long memberCount = sectMemberRepository.countBySectId(member.getSectId());
      if (memberCount > 1) {
        throw new BusinessException(ErrorCode.SECT_DISSOLVE_HAS_MEMBERS, memberCount - 1);
      }
      sectRepository.deleteById(member.getSectId());
    }

    executeLeave(userId, member);

    log.info("玩家 {} 退出宗门 {}", userId, member.getSectId());
    return "你已退出宗门，贡献值清零，已学共享功法已遗忘，" + SECT_COOLDOWN_HOURS + " 小时内无法加入新宗门。";
  }

  public String appointMember(Long userId, String targetNickname, String positionCode) {
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

    if (!targetMember.getSectId().equals(actorMember.getSectId())) {
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
              .findById(actorMember.getSectId())
              .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));
      actorMember.setPosition(SectPosition.ELDER);
      sectMemberRepository.save(actorMember);
      targetMember.setPosition(SectPosition.LEADER);
      sectMemberRepository.save(targetMember);
      sect.setLeaderId(target.getId());
      sectRepository.save(sect);
      log.info("玩家 {} 将宗门 {} 的宗主之位传给 {}", userId, actorMember.getSectId(), target.getId());
      return "已将宗主之位传给【" + targetNickname + "】，你已成为长老。";
    }

    targetMember.setPosition(newPosition);
    sectMemberRepository.save(targetMember);

    log.info(
        "玩家 {} 被任命为宗门 {} 的 {}", target.getId(), actorMember.getSectId(), newPosition.getName());
    return "已将【" + targetNickname + "】任命为" + newPosition.getName() + "。";
  }

  public String dismissSect(Long userId) {
    SectMember member = requireMember(userId);
    if (!member.getPosition().canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    sectShopItemRepository.deleteBySectId(sect.getId());
    sectSharedSkillRepository.deleteBySectId(sect.getId());
    sectBuildingRepository.deleteBySectId(sect.getId());

    List<SectMember> members = sectMemberRepository.findBySectId(sect.getId());
    for (SectMember m : members) {
      forgetSharedSkills(m.getUserId());
      masterApprenticeService.handleMasterSectLeave(m.getUserId());
    }
    for (SectMember m : members) {
      sectMemberRepository.deleteById(m.getId());
    }

    sectRepository.deleteById(sect.getId());

    log.info("宗门 {} 被宗主 {} 解散", sect.getId(), userId);
    return "宗门【" + sect.getName() + "】已解散。";
  }

  public String setNotice(Long userId, String content) {
    SectMember member = requireMember(userId);
    if (!member.getPosition().canPostNotice()) {
      throw new BusinessException(ErrorCode.SECT_NO_PERMISSION, "发布公告");
    }

    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));
    sect.setNotice(content);
    sectRepository.save(sect);

    return "宗门公告已更新。";
  }

  public String donateStones(Long userId, long amount) {
    SectMember member = requireMember(userId);

    if (amount <= 0) {
      throw new BusinessException(ErrorCode.SPIRIT_STONES_INSUFFICIENT, amount, 0);
    }

    User user = userStateService.loadUserForUpdate(userId);
    if (user.getSpiritStones() < amount) {
      throw new BusinessException(
          ErrorCode.SPIRIT_STONES_INSUFFICIENT, amount, user.getSpiritStones());
    }

    user.setSpiritStones(user.getSpiritStones() - amount);
    userRepository.save(user);

    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));
    sect.addFunds(amount);
    sectRepository.save(sect);

    int contributionGain = (int) (amount * DONATE_RATE);
    member.setContribution(member.getContribution() + contributionGain);
    sectMemberRepository.save(member);

    return "捐献成功！消耗 " + amount + " 灵石，获得 " + contributionGain + " 贡献值，宗门资金 +" + amount + "。";
  }

  public String getShop(Long userId) {
    SectMember member = requireMember(userId);
    List<SectShopItem> items = sectShopItemRepository.findBySectId(member.getSectId());

    if (items.isEmpty()) {
      return "宗门贡献商店暂无商品。";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("=== 宗门贡献商店 ===\n");
    sb.append("我的贡献: ").append(member.getContribution()).append("\n\n");

    for (SectShopItem item : items) {
      ItemTemplate template =
          itemTemplateRepository.findById(item.getItemTemplateId()).orElse(null);
      String itemName = template != null ? template.getName() : "[未知]";
      sb.append("  [#").append(item.getId()).append("] ").append(itemName);
      sb.append(" | 贡献: ").append(item.getPriceContribution());
      if (item.getStock() == -1) {
        sb.append(" (无限)");
      } else if (item.getStock() == 0) {
        sb.append(" (售罄)");
      } else {
        sb.append(" (库存: ").append(item.getStock()).append(")");
      }
      sb.append("\n");
    }

    return sb.toString();
  }

  public String exchangeShopItem(Long userId, long shopItemId) {
    SectMember member = requireMember(userId);

    SectShopItem shopItem =
        sectShopItemRepository
            .findById(shopItemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_EXISTS));

    if (!shopItem.getSectId().equals(member.getSectId())) {
      throw new BusinessException(ErrorCode.ITEM_NOT_EXISTS);
    }

    if (!shopItem.isInStock()) {
      throw new BusinessException(ErrorCode.SHOP_PRODUCT_OUT_OF_STOCK);
    }

    if (member.getContribution() < shopItem.getPriceContribution()) {
      throw new BusinessException(
          ErrorCode.SECT_SHOP_ITEM_INSUFFICIENT_CONTRIBUTION,
          shopItem.getPriceContribution(),
          member.getContribution());
    }

    ItemTemplate template =
        itemTemplateRepository
            .findById(shopItem.getItemTemplateId())
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_EXISTS));

    member.setContribution(member.getContribution() - shopItem.getPriceContribution());
    sectMemberRepository.save(member);

    if (!shopItem.deductStock(1)) {
      throw new BusinessException(ErrorCode.SHOP_PRODUCT_OUT_OF_STOCK);
    }
    sectShopItemRepository.save(shopItem);

    stackableItemService.addStackableItem(
        userId, template.getId(), template.getType(), template.getName(), 1);

    return "兑换成功！获得 " + template.getName() + "，剩余贡献: " + member.getContribution() + "。";
  }

  public String getSharedSkills(Long userId) {
    SectMember member = requireMember(userId);
    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    int maxSlots = getScriptureSlotCount(sect.getId());
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

    User user = userStateService.loadUser(userId);
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

  public String submitSkillJade(Long userId, String jadeName) {
    SectMember member = requireMember(userId);

    List<top.stillmisty.xiantao.domain.item.entity.StackableItem> jadeItems =
        stackableItemRepository.findByUserId(userId).stream()
            .filter(si -> si.getItemType() == ItemType.SKILL_JADE)
            .toList();

    if (jadeItems.isEmpty()) {
      throw new BusinessException(ErrorCode.ITEM_NOT_FOUND, "功法玉简");
    }

    var matchedJade = resolveByName(jadeItems, jadeName, si -> si.getName());
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

    int maxSlots = getScriptureSlotCount(member.getSectId());
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

  public String removeSharedSkill(Long userId, long sharedSkillId) {
    SectMember member = requireMember(userId);
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

    if (sharedSkill.getStatus() != SectSharedSkillStatus.LISTED) {
      return "该功法尚未上架。";
    }

    sharedSkill.setStatus(SectSharedSkillStatus.PENDING);
    sectSharedSkillRepository.save(sharedSkill);

    Skill skill = skillRepository.findById(sharedSkill.getSkillId()).orElse(null);
    String skillName = skill != null ? skill.getName() : "[" + sharedSkillId + "]";
    return "已下架「" + skillName + "」，已学习的成员不受影响。";
  }

  public String listSharedSkill(Long userId, long sharedSkillId) {
    SectMember member = requireMember(userId);
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

    if (sharedSkill.getStatus() != SectSharedSkillStatus.PENDING) {
      return "该功法已上架或状态异常。";
    }

    int maxSlots = getScriptureSlotCount(member.getSectId());
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

  public String getTasks(Long userId) {
    SectMember member = requireMember(userId);
    // 宗门事件任务暂未实现完整逻辑
    return "暂无进行中的宗门事件任务。";
  }

  public String upgradeSect(Long userId) {
    SectMember member = requireMember(userId);
    if (!member.getPosition().canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    if (sect.isMaxLevel()) {
      return "宗门已达最高等级 Lv.5。";
    }

    long cost =
        switch (sect.getLevel()) {
          case 1 -> 5000;
          case 2 -> 15000;
          case 3 -> 30000;
          case 4 -> 50000;
          default -> Long.MAX_VALUE;
        };

    if (!sect.deductFunds(cost)) {
      throw new BusinessException(ErrorCode.SECT_FUNDS_INSUFFICIENT, cost, sect.getFunds());
    }

    sect.setLevel(sect.getLevel() + 1);
    sect.setMaxMembers(sect.getMaxMembers() + 5);
    sectRepository.save(sect);

    log.info("宗门 {} 升级至 Lv.{}，消耗 {} 资金", sect.getId(), sect.getLevel(), cost);
    return "宗门升级成功！当前等级 Lv."
        + sect.getLevel()
        + "，成员上限 +5（"
        + sect.getMaxMembers()
        + "），消耗资金 "
        + cost
        + "（剩余 "
        + sect.getFunds()
        + "）。";
  }

  public String expandMembers(Long userId) {
    SectMember member = requireMember(userId);
    if (!member.getPosition().canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    int slots = 5;
    long cost = slots * 500L;

    if (!sect.deductFunds(cost)) {
      throw new BusinessException(ErrorCode.SECT_FUNDS_INSUFFICIENT, cost, sect.getFunds());
    }

    sect.setMaxMembers(sect.getMaxMembers() + slots);
    sectRepository.save(sect);

    log.info("宗门 {} 扩充成员上限至 {}", sect.getId(), sect.getMaxMembers());
    return "扩充成功！成员上限 +" + slots + "（当前 " + sect.getMaxMembers() + "），消耗资金 " + cost + "。";
  }

  public String getBuildings(Long userId) {
    SectMember member = requireMember(userId);
    List<SectBuilding> buildings = sectBuildingRepository.findBySectId(member.getSectId());

    StringBuilder sb = new StringBuilder();
    sb.append("=== 宗门建筑 ===\n");

    if (buildings.isEmpty()) {
      sb.append("暂无建筑，宗主可消耗宗门资金建造。\n\n");
    } else {
      for (SectBuilding b : buildings) {
        sb.append("  ")
            .append(b.getBuildingType().getName())
            .append(" Lv.")
            .append(b.getLevel())
            .append("/")
            .append(b.getBuildingType().getMaxLevel())
            .append(" | 升级: ")
            .append(b.getBuildingType().upgradeCost())
            .append(" 灵石\n");
      }
      sb.append("\n");
    }

    sb.append("可建造建筑：\n");
    for (SectBuildingType type : SectBuildingType.values()) {
      boolean built = buildings.stream().anyMatch(b -> b.getBuildingType() == type);
      if (!built) {
        sb.append("  ")
            .append(type.getName())
            .append(" | 建造: ")
            .append(type.getBuildCost())
            .append(" 灵石\n");
      }
    }

    return sb.toString();
  }

  public String buildStructure(Long userId, String buildingTypeCode) {
    SectMember member = requireMember(userId);
    if (!member.getPosition().canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    SectBuildingType type;
    try {
      type = SectBuildingType.fromCode(buildingTypeCode);
    } catch (IllegalArgumentException e) {
      throw new BusinessException(ErrorCode.SECT_BUILDING_NOT_FOUND);
    }

    if (sectBuildingRepository.findBySectIdAndType(member.getSectId(), type).isPresent()) {
      throw new BusinessException(ErrorCode.SECT_BUILDING_ALREADY_EXISTS);
    }

    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    if (!sect.deductFunds(type.getBuildCost())) {
      throw new BusinessException(
          ErrorCode.SECT_FUNDS_INSUFFICIENT, type.getBuildCost(), sect.getFunds());
    }
    sectRepository.save(sect);

    SectBuilding building =
        SectBuilding.create().setSectId(member.getSectId()).setBuildingType(type).setLevel(1);
    sectBuildingRepository.save(building);

    log.info("宗门 {} 建造建筑 {} Lv.1", sect.getId(), type.getName());
    return "已建造" + type.getName() + " Lv.1，消耗资金 " + type.getBuildCost() + "。";
  }

  public String upgradeBuilding(Long userId, String buildingTypeCode) {
    SectMember member = requireMember(userId);
    if (!member.getPosition().canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    SectBuildingType type;
    try {
      type = SectBuildingType.fromCode(buildingTypeCode);
    } catch (IllegalArgumentException e) {
      throw new BusinessException(ErrorCode.SECT_BUILDING_NOT_FOUND);
    }

    SectBuilding building =
        sectBuildingRepository
            .findBySectIdAndType(member.getSectId(), type)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_BUILDING_NOT_FOUND));

    if (building.isMaxLevel()) {
      throw new BusinessException(ErrorCode.SECT_BUILDING_MAX_LEVEL, type.getMaxLevel());
    }

    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    long cost = type.upgradeCost();
    if (!sect.deductFunds(cost)) {
      throw new BusinessException(ErrorCode.SECT_FUNDS_INSUFFICIENT, cost, sect.getFunds());
    }
    sectRepository.save(sect);

    int oldLevel = building.getLevel();
    building.setLevel(oldLevel + 1);
    sectBuildingRepository.save(building);

    log.info(
        "宗门 {} 升级建筑 {} Lv.{} -> Lv.{}",
        sect.getId(),
        type.getName(),
        oldLevel,
        building.getLevel());
    return "已将"
        + type.getName()
        + "从 Lv."
        + oldLevel
        + " 升级至 Lv."
        + building.getLevel()
        + "，消耗资金 "
        + cost
        + "。";
  }

  // ===================== 跨服务接口 =====================

  public boolean isInSect(Long userId) {
    Optional<SectMember> member = sectMemberRepository.findByUserId(userId);
    return member.isPresent() && member.get().getSectId() != null;
  }

  public Long getSectId(Long userId) {
    return sectMemberRepository.findByUserId(userId).map(SectMember::getSectId).orElse(null);
  }

  public boolean isInSameSect(Long userIdA, Long userIdB) {
    Optional<SectMember> memberA = sectMemberRepository.findByUserId(userIdA);
    Optional<SectMember> memberB = sectMemberRepository.findByUserId(userIdB);
    if (memberA.isEmpty() && memberB.isEmpty()) {
      return true;
    }
    if (memberA.isEmpty() || memberB.isEmpty()) {
      return false;
    }
    Long sectA = memberA.get().getSectId();
    Long sectB = memberB.get().getSectId();
    return sectA != null && sectA.equals(sectB);
  }

  public boolean areBothRogue(Long userIdA, Long userIdB) {
    return sectMemberRepository.findByUserId(userIdA).isEmpty()
        && sectMemberRepository.findByUserId(userIdB).isEmpty();
  }

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
  public void leaveSectInternal(Long userId) {
    sectMemberRepository.deleteByUserId(userId);
  }

  /** 退宗时遗忘共享功法 */
  @Transactional
  public void forgetSharedSkills(Long userId) {
    Optional<SectMember> memberOpt = sectMemberRepository.findByUserId(userId);
    if (memberOpt.isEmpty() || memberOpt.get().getSectId() == null) return;
    Long sectId = memberOpt.get().getSectId();
    playerSkillRepository.deleteByUserIdAndSourceSectId(userId, sectId);
  }

  /** 执行退出操作（清贡献、设冷却、遗忘功法、通知师徒） */
  private void executeLeave(Long userId, SectMember member) {
    long sectId = member.getSectId();
    sectMemberRepository.deleteByUserId(userId);

    forgetSharedSkills(userId);

    sectMemberRepository.save(
        SectMember.create()
            .setUserId(userId)
            .setSectId(null)
            .setContribution(0)
            .setCooldownUntil(LocalDateTime.now().plusHours(SECT_COOLDOWN_HOURS)));

    masterApprenticeService.handleMasterSectLeave(userId);
  }

  // ===================== 建筑加成查询 =====================

  /** 获取宗门某建筑等级（0=未建造） */
  public int getBuildingLevel(Long sectId, SectBuildingType type) {
    return sectBuildingRepository
        .findBySectIdAndType(sectId, type)
        .map(SectBuilding::getLevel)
        .orElse(0);
  }

  /** 练功房修炼经验加成（每级+3%） */
  public double getTrainingBonus(Long sectId) {
    int level = getBuildingLevel(sectId, SectBuildingType.TRAINING_ROOM);
    return 1.0 + level * 0.03;
  }

  /** 炼丹房品质评分加成（每级+5%） */
  public double getAlchemyBonus(Long sectId) {
    int level = getBuildingLevel(sectId, SectBuildingType.ALCHEMY_CHAMBER);
    return 1.0 + level * 0.05;
  }

  /** 锻造坊强化费用折扣（每级-5%） */
  public double getForgeDiscount(Long sectId) {
    int level = getBuildingLevel(sectId, SectBuildingType.FORGE_WORKSHOP);
    return 1.0 - level * 0.05;
  }

  /** 护阵受伤减免（每级-3%） */
  public double getGuardDamageReduction(Long sectId) {
    int level = getBuildingLevel(sectId, SectBuildingType.GUARD_ARRAY);
    return 1.0 - level * 0.03;
  }

  /** 获取藏经阁共享功法位上限 */
  public int getScriptureSlotCount(Long sectId) {
    int level = getBuildingLevel(sectId, SectBuildingType.SCRIPTURE_PAVILION);
    return DEFAULT_SCRIPTURE_SLOTS + level * SCRIPTURE_SLOTS_PER_LEVEL;
  }

  /** 灵脉惰性结算 */
  @Transactional
  public void settleSpiritVein(Long sectId) {
    SectBuilding vein =
        sectBuildingRepository
            .findBySectIdAndType(sectId, SectBuildingType.SPIRIT_VEIN)
            .orElse(null);
    if (vein == null) return;

    Sect sect =
        sectRepository
            .findById(sectId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime lastPayout = sect.getLastVeinPayout();
    if (lastPayout == null) {
      lastPayout = sect.getCreatedAt();
    }

    long hoursSinceLast = ChronoUnit.HOURS.between(lastPayout, now);
    if (hoursSinceLast <= 0) return;

    long income = hoursSinceLast * vein.getLevel() * 100 / 24;
    if (income > 0) {
      sect.addFunds(income);
      sect.setLastVeinPayout(now);
      sectRepository.save(sect);
    }
  }

  // ===================== 工具方法 =====================

  private SectMember requireMember(Long userId) {
    return sectMemberRepository
        .findByUserId(userId)
        .filter(m -> m.getSectId() != null)
        .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
  }

  private boolean isOnCooldown(Long userId) {
    Optional<SectMember> member = sectMemberRepository.findByUserId(userId);
    return member.isPresent() && member.get().getSectId() == null && member.get().isOnCooldown();
  }

  @SuppressWarnings("unchecked")
  private <T> T resolveByName(
      List<T> items, String input, java.util.function.Function<T, String> nameExtractor) {
    if (input == null || input.isBlank()) return null;
    for (var item : items) {
      if (nameExtractor.apply(item).equals(input)) return item;
    }
    var partial = items.stream().filter(item -> nameExtractor.apply(item).contains(input)).toList();
    if (partial.size() == 1) return partial.getFirst();
    return null;
  }
}
