package top.stillmisty.xiantao.service.sect;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.sect.entity.Sect;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.domain.sect.entity.SectShopItem;
import top.stillmisty.xiantao.domain.sect.enums.SectPosition;
import top.stillmisty.xiantao.domain.sect.repository.SectMemberRepository;
import top.stillmisty.xiantao.domain.sect.repository.SectRepository;
import top.stillmisty.xiantao.domain.sect.repository.SectShopItemRepository;
import top.stillmisty.xiantao.domain.sect.repository.SectSkillRepository;
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

  private static final int SECT_CREATE_COST = 5000;
  private static final int SECT_INITIAL_FUNDS = 2000;
  private static final int SECT_CREATE_MIN_MEMBERS = 3;
  private static final int SECT_EXPAND_MEMBER_COST_PER = 500;
  private static final int SECT_COOLDOWN_HOURS = 24;

  private final SectRepository sectRepository;
  private final SectMemberRepository sectMemberRepository;
  private final SectSkillRepository sectSkillRepository;
  private final SectShopItemRepository sectShopItemRepository;
  private final UserRepository userRepository;
  private final UserStateService userStateService;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemService stackableItemService;
  @Lazy private final MasterApprenticeService masterApprenticeService;

  // ===================== 公开 API =====================

  @Authenticated
  @Transactional
  public ServiceResult<String> getSectOverview(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getSectOverview(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> createSect(PlatformType platform, String openId, String name) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(createSect(userId, name));
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
  public ServiceResult<String> applyJoin(PlatformType platform, String openId, String sectName) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(applyJoin(userId, sectName));
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
  public ServiceResult<String> getSkills(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getSkills(userId));
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
  @Transactional
  public ServiceResult<String> refreshShop(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(refreshShop(userId));
  }

  // ===================== 内部 API =====================

  public String getSectOverview(Long userId) {
    User user = userStateService.loadUser(userId);
    Optional<SectMember> memberOpt = sectMemberRepository.findByUserId(userId);
    if (memberOpt.isEmpty()) {
      throw new BusinessException(ErrorCode.SECT_NOT_IN);
    }

    SectMember member = memberOpt.get();
    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));
    User leader = userStateService.loadUser(sect.getLeaderId());
    List<SectMember> members = sectMemberRepository.findBySectId(sect.getId());

    StringBuilder sb = new StringBuilder();
    sb.append("=== ").append(sect.getName()).append(" ===\n");
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

  public String createSect(Long userId, String name) {
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

    Sect sect =
        Sect.create()
            .setName(name)
            .setLeaderId(userId)
            .setLevel(1)
            .setFunds((long) SECT_INITIAL_FUNDS)
            .setMaxMembers(10);
    sectRepository.save(sect);

    SectMember member =
        SectMember.create()
            .setSectId(sect.getId())
            .setUserId(userId)
            .setPosition(SectPosition.LEADER)
            .setContribution(0);
    sectMemberRepository.save(member);

    log.info("玩家 {} 创建宗门 {} (id={})", userId, name, sect.getId());
    return "宗门【" + name + "】创建成功！你已成为宗主。初始资金: " + SECT_INITIAL_FUNDS + " 灵石。";
  }

  public String inviteMember(Long userId, String targetNickname) {
    User inviter = userStateService.loadUser(userId);
    SectMember inviterMember =
        sectMemberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
    if (!inviterMember.canInvite()) {
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

  public String applyJoin(Long userId, String sectName) {
    User user = userStateService.loadUserForUpdate(userId);

    if (sectMemberRepository.findByUserId(userId).isPresent()) {
      throw new BusinessException(ErrorCode.SECT_ALREADY_IN, "已有宗门");
    }

    if (isOnCooldown(userId)) {
      throw new BusinessException(ErrorCode.SECT_COOLDOWN, SECT_COOLDOWN_HOURS);
    }

    Sect sect =
        sectRepository
            .findByName(sectName)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    long memberCount = sectMemberRepository.countBySectId(sect.getId());
    if (memberCount >= sect.getMaxMembers()) {
      throw new BusinessException(ErrorCode.SECT_FULL, sect.getMaxMembers());
    }

    SectMember member =
        SectMember.create()
            .setSectId(sect.getId())
            .setUserId(userId)
            .setPosition(SectPosition.MEMBER)
            .setContribution(0);
    sectMemberRepository.save(member);

    masterApprenticeService.syncSectForMaster(userId, sect.getId());

    log.info("玩家 {} 申请加入宗门 {}", userId, sect.getId());
    return "你已加入宗门【" + sectName + "】！";
  }

  public String kickMember(Long userId, String targetNickname) {
    SectMember actorMember =
        sectMemberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
    if (!actorMember.canKick()) {
      throw new BusinessException(ErrorCode.SECT_NO_PERMISSION, "踢出");
    }

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

    sectMemberRepository.deleteByUserId(target.getId());

    sectMemberRepository.save(
        SectMember.create()
            .setUserId(target.getId())
            .setSectId(null)
            .setContribution(0)
            .setCooldownUntil(LocalDateTime.now().plusHours(SECT_COOLDOWN_HOURS)));

    masterApprenticeService.handleMasterSectLeave(target.getId());

    log.info("玩家 {} 被 {} 踢出宗门 {}", target.getId(), userId, targetMember.getSectId());
    return "已将【" + targetNickname + "】踢出宗门，" + SECT_COOLDOWN_HOURS + " 小时内无法加入新宗门。";
  }

  public String leaveSect(Long userId) {
    User user = userStateService.loadUser(userId);
    SectMember member =
        sectMemberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));

    if (member.getPosition() == SectPosition.LEADER) {
      long memberCount = sectMemberRepository.countBySectId(member.getSectId());
      if (memberCount > 1) {
        throw new BusinessException(ErrorCode.SECT_DISSOLVE_HAS_MEMBERS, memberCount - 1);
      }
      sectRepository.deleteById(member.getSectId());
    }

    sectMemberRepository.deleteByUserId(userId);

    sectMemberRepository.save(
        SectMember.create()
            .setUserId(userId)
            .setSectId(null)
            .setContribution(0)
            .setCooldownUntil(LocalDateTime.now().plusHours(SECT_COOLDOWN_HOURS)));

    masterApprenticeService.handleMasterSectLeave(userId);

    log.info("玩家 {} 退出宗门 {}", userId, member.getSectId());
    return "你已退出宗门，贡献值清零，" + SECT_COOLDOWN_HOURS + " 小时内无法加入新宗门。";
  }

  public String appointMember(Long userId, String targetNickname, String positionCode) {
    SectMember actorMember =
        sectMemberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
    if (!actorMember.canManage()) {
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
      actorMember.setPosition(SectPosition.VICE_LEADER);
      sectMemberRepository.save(actorMember);
      targetMember.setPosition(SectPosition.LEADER);
      sectMemberRepository.save(targetMember);
      sect.setLeaderId(target.getId());
      sectRepository.save(sect);
      log.info("玩家 {} 将宗门 {} 的宗主之位传给 {}", userId, actorMember.getSectId(), target.getId());
      return "已将宗主之位传给【" + targetNickname + "】，你已成为副宗主。";
    }

    if (newPosition == SectPosition.VICE_LEADER) {
      long viceLeaders =
          sectMemberRepository.findBySectId(actorMember.getSectId()).stream()
              .filter(m -> m.getPosition() == SectPosition.VICE_LEADER)
              .count();
      if (viceLeaders >= 2 && targetMember.getPosition() != SectPosition.VICE_LEADER) {
        throw new BusinessException(ErrorCode.SECT_POSITION_LIMIT, 2);
      }
    }

    if (newPosition == SectPosition.ELDER) {
      long elders =
          sectMemberRepository.findBySectId(actorMember.getSectId()).stream()
              .filter(m -> m.getPosition() == SectPosition.ELDER)
              .count();
      if (elders >= 5 && targetMember.getPosition() != SectPosition.ELDER) {
        throw new BusinessException(ErrorCode.SECT_POSITION_LIMIT, 5);
      }
    }

    targetMember.setPosition(newPosition);
    sectMemberRepository.save(targetMember);

    log.info(
        "玩家 {} 被任命为宗门 {} 的 {}", target.getId(), actorMember.getSectId(), newPosition.getName());
    return "已将【" + targetNickname + "】任命为" + newPosition.getName() + "。";
  }

  public String dismissSect(Long userId) {
    SectMember member =
        sectMemberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
    if (!member.canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    sectShopItemRepository.deleteBySectId(sect.getId());
    sectSkillRepository.deleteBySectId(sect.getId());

    List<SectMember> members = sectMemberRepository.findBySectId(sect.getId());
    for (SectMember m : members) {
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
    SectMember member =
        sectMemberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
    if (!member.canPostNotice()) {
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
    SectMember member =
        sectMemberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));

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

    int contributionGain = (int) (amount * 0.1);
    member.setContribution(member.getContribution() + contributionGain);
    sectMemberRepository.save(member);

    return "捐献成功！消耗 " + amount + " 灵石，获得 " + contributionGain + " 贡献值。";
  }

  public String getShop(Long userId) {
    SectMember member =
        sectMemberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
    List<SectShopItem> items = sectShopItemRepository.findBySectId(member.getSectId());

    if (items.isEmpty()) {
      return "宗门商店暂无商品。";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("=== 宗门贡献商店 ===\n");
    sb.append("我的贡献: ").append(member.getContribution()).append("\n\n");

    for (SectShopItem item : items) {
      sb.append("  [#").append(item.getId()).append("] ");
      sb.append("[ID:").append(item.getItemTemplateId()).append("] ");
      sb.append("贡献: ").append(item.getPriceContribution());
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
    SectMember member =
        sectMemberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));

    SectShopItem shopItem =
        sectShopItemRepository
            .findById(shopItemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_EXISTS));

    if (!shopItem.getSectId().equals(member.getSectId())) {
      throw new BusinessException(ErrorCode.ITEM_NOT_EXISTS);
    }

    if (!shopItem.isInStock()) {
      return "该商品已售罄。";
    }

    if (member.getContribution() < shopItem.getPriceContribution()) {
      return "贡献值不足（需要 "
          + shopItem.getPriceContribution()
          + "，当前 "
          + member.getContribution()
          + "）。";
    }

    ItemTemplate template =
        itemTemplateRepository
            .findById(shopItem.getItemTemplateId())
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_EXISTS));

    member.setContribution(member.getContribution() - shopItem.getPriceContribution());
    sectMemberRepository.save(member);

    if (!shopItem.deductStock(1)) {
      return "库存不足。";
    }
    sectShopItemRepository.save(shopItem);

    stackableItemService.addStackableItem(
        userId, template.getId(), template.getType(), template.getName(), 1);

    return "兑换成功！获得 " + template.getName() + "，剩余贡献: " + member.getContribution() + "。";
  }

  public String getSkills(Long userId) {
    SectMember member =
        sectMemberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    StringBuilder sb = new StringBuilder();
    sb.append("=== 宗门功法 ===\n");
    sb.append("宗门等级: Lv.").append(sect.getLevel()).append("\n\n");

    String[] skillNames = {"凝心诀", "聚灵诀", "通脉诀", "护体诀", "天罡诀"};
    String[] skillEffects = {"修炼经验 +5%", "历练效率 +5%", "突破成功率 +3%", "战斗受伤 -5%", "全属性 +5"};

    for (int i = 0; i < skillNames.length; i++) {
      boolean unlocked = sect.getLevel() >= (i + 1);
      sb.append("  ");
      if (unlocked) {
        sb.append("✅ ");
      } else {
        sb.append("🔒 ");
      }
      sb.append(skillNames[i]).append(": ").append(skillEffects[i]);
      if (!unlocked) {
        sb.append(" (需要宗门 Lv.").append(i + 1).append(")");
      }
      sb.append("\n");
    }

    return sb.toString();
  }

  public String upgradeSect(Long userId) {
    SectMember member =
        sectMemberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
    if (!member.canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    int currentLevel = sect.getLevel();
    if (currentLevel >= 5) {
      return "宗门已达最高等级 Lv.5。";
    }

    long cost;
    switch (currentLevel) {
      case 1 -> cost = 5000;
      case 2 -> cost = 15000;
      case 3 -> cost = 30000;
      case 4 -> cost = 50000;
      default -> {
        return "宗门已达最高等级。";
      }
    }

    if (!sect.deductFunds(cost)) {
      return "宗门资金不足（需要 " + cost + "，当前 " + sect.getFunds() + "）。";
    }

    sect.setLevel(currentLevel + 1);
    sect.setMaxMembers(sect.getMaxMembers() + 5);
    sectRepository.save(sect);

    log.info("宗门 {} 升级至 Lv.{}，消耗 {} 资金", sect.getId(), sect.getLevel(), cost);
    return "宗门升级成功！当前等级 Lv."
        + sect.getLevel()
        + "，成员上限 "
        + sect.getMaxMembers()
        + "，消耗资金 "
        + cost
        + "（剩余 "
        + sect.getFunds()
        + "）。";
  }

  public String expandMembers(Long userId) {
    SectMember member =
        sectMemberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
    if (!member.canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    Sect sect =
        sectRepository
            .findById(member.getSectId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_FOUND));

    int slots = 5;
    long cost = (long) slots * SECT_EXPAND_MEMBER_COST_PER;

    if (!sect.deductFunds(cost)) {
      return "宗门资金不足（需要 " + cost + "，当前 " + sect.getFunds() + "）。";
    }

    sect.setMaxMembers(sect.getMaxMembers() + slots);
    sectRepository.save(sect);

    log.info("宗门 {} 扩充成员上限至 {}", sect.getId(), sect.getMaxMembers());
    return "扩充成功！成员上限增加 " + slots + "（当前 " + sect.getMaxMembers() + "），消耗资金 " + cost + "。";
  }

  public String refreshShop(Long userId) {
    SectMember member =
        sectMemberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SECT_NOT_IN));
    if (!member.canManage()) {
      throw new BusinessException(ErrorCode.SECT_NOT_LEADER);
    }

    return "宗门商店刷新功能即将开放，敬请期待。";
  }

  // ===================== 跨服务接口 (包私有) =====================

  public boolean isInSect(Long userId) {
    return sectMemberRepository.findByUserId(userId).isPresent();
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
    return memberA.get().getSectId().equals(memberB.get().getSectId());
  }

  public boolean areBothRogue(Long userIdA, Long userIdB) {
    return sectMemberRepository.findByUserId(userIdA).isEmpty()
        && sectMemberRepository.findByUserId(userIdB).isEmpty();
  }

  /** 玩家加入宗门（内部调用，不校验冷却） */
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

  /** 玩家退出宗门（内部调用，不校验权限） */
  @Transactional
  public void leaveSectInternal(Long userId) {
    sectMemberRepository.deleteByUserId(userId);
  }

  private boolean isOnCooldown(Long userId) {
    Optional<SectMember> member = sectMemberRepository.findByUserId(userId);
    return member.isPresent() && member.get().isOnCooldown();
  }
}
