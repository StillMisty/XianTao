package top.stillmisty.xiantao.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.StackableItemRepository;
import top.stillmisty.xiantao.domain.pill.entity.PillResistance;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.pill.enums.PillQuality;
import top.stillmisty.xiantao.domain.pill.repository.PillResistanceRepository;
import top.stillmisty.xiantao.domain.pill.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.AttributeType;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.service.annotation.Authenticated;

/** 丹药服用服务 处理：服用丹药、等级衰减、抗性衰减、效果应用 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PillConsumptionService {

  private static final double GRADE_DECAY_COEFFICIENT = 0.2;
  private final UserStateService userStateService;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemRepository stackableItemRepository;
  private final PillResistanceRepository pillResistanceRepository;
  private final PlayerBuffRepository playerBuffRepository;

  // ===================== 公开 API（含认证） =====================

  @Authenticated
  @Transactional
  public ServiceResult<String> takePill(PlatformType platform, String openId, String pillName) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(takePill(userId, pillName));
  }

  // ===================== 内部 API =====================

  /** 服用丹药（内部调用，物品扣减由 ItemUseService 统一处理） */
  @Transactional
  public String takePill(Long userId, String pillName) {
    StackableItem pill = findPill(userId, pillName);
    if (pill == null) return "背包中未找到丹药：" + pillName;

    ItemTemplate template = itemTemplateRepository.findById(pill.getTemplateId()).orElse(null);
    if (template == null) return "丹药数据错误";

    var props = template.typedProperties();
    if (!(props instanceof ItemProperties.Potion(List<ItemProperties.Effect> effects)))
      return "丹药没有效果";

    User user = userStateService.loadUser(userId);
    double qualityMultiplier = PillQuality.fromCode(pill.getQuality()).getMultiplier();
    int grade = getPillGrade(pill);
    var messages = new ArrayList<String>();

    for (var effect : effects) {
      String msg = applyEffect(user, effect, qualityMultiplier, grade, pill.getTemplateId());
      if (msg != null && !msg.isEmpty()) messages.add(msg);
    }

    userStateService.save(user);

    if (messages.isEmpty()) return "丹药效果未知";
    return "服用丹药成功：" + String.join("，", messages);
  }

  // ===================== 效果应用 =====================

  private String applyEffect(
      User user,
      ItemProperties.Effect effect,
      double qualityMultiplier,
      int grade,
      long templateId) {
    return switch (effect) {
      case ItemProperties.Effect.Exp e -> applyExp(user, e, qualityMultiplier, grade, templateId);
      case ItemProperties.Effect.Hp e -> applyHp(user, e, qualityMultiplier);
      case ItemProperties.Effect.Stat e -> applyStat(user, e, qualityMultiplier, grade, templateId);
      case ItemProperties.Effect.Breakthrough e ->
          applyBreakthrough(user, e, qualityMultiplier, grade);
      case ItemProperties.Effect.Buff e -> applyBuff(user, e, qualityMultiplier, grade);
      case ItemProperties.Effect.Cure e -> applyCure(user, e);
    };
  }

  private String applyExp(
      User user,
      ItemProperties.Effect.Exp e,
      double qualityMultiplier,
      int grade,
      long templateId) {
    double gradeDecay = calcGradeDecay(user.getLevel(), grade, true);
    double resistanceDecay = calcResistanceDecay(user.getId(), templateId);
    long actualExp = (long) (e.amount() * qualityMultiplier * gradeDecay * resistanceDecay);
    if (actualExp <= 0) return null;
    user.addExp(actualExp);
    pillResistanceRepository.incrementCount(user.getId(), templateId);
    return "获得 " + actualExp + " 经验值";
  }

  private String applyHp(User user, ItemProperties.Effect.Hp e, double qualityMultiplier) {
    if (user.getStatus() == UserStatus.DYING) {
      user.setHpCurrent(user.calculateMaxHp());
      user.setStatus(UserStatus.IDLE);
      return "复活并回满生命值";
    }

    int maxHp = user.calculateMaxHp();
    int healAmount;
    if (e.percentage() > 0) {
      healAmount = (int) (maxHp * e.percentage() / 100 * qualityMultiplier);
    } else {
      healAmount = (int) (e.amount() * qualityMultiplier);
    }
    int oldHp = user.getHpCurrent();
    user.setHpCurrent(Math.min(maxHp, oldHp + healAmount));
    return "恢复 " + healAmount + " 生命值";
  }

  private String applyStat(
      User user,
      ItemProperties.Effect.Stat e,
      double qualityMultiplier,
      int grade,
      long templateId) {
    double gradeDecay = calcGradeDecay(user.getLevel(), grade, false);
    double resistanceDecay = calcResistanceDecay(user.getId(), templateId);
    int actualStat = (int) (e.amount() * qualityMultiplier * gradeDecay * resistanceDecay);
    if (actualStat <= 0) return null;

    var attrType = AttributeType.fromCode(e.statAttr());
    if (attrType == null) return null;

    var statName =
        switch (attrType) {
          case STR -> {
            user.addStatStr(actualStat);
            yield "力道";
          }
          case CON -> {
            user.addStatCon(actualStat);
            yield "根骨";
          }
          case AGI -> {
            user.addStatAgi(actualStat);
            yield "身法";
          }
          case WIS -> {
            user.addStatWis(actualStat);
            yield "悟性";
          }
        };

    pillResistanceRepository.incrementCount(user.getId(), templateId);
    return statName + " +" + actualStat;
  }

  private String applyBreakthrough(
      User user, ItemProperties.Effect.Breakthrough e, double qualityMultiplier, int grade) {
    double gradeDecay = calcGradeDecay(user.getLevel(), grade, true);
    int bonusValue = (int) (e.rate() * 100 * qualityMultiplier * gradeDecay);
    if (bonusValue <= 0) return null;

    LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
    PlayerBuff buff = PlayerBuff.create(user.getId(), "breakthrough", bonusValue, expiresAt);
    playerBuffRepository.save(buff);

    return "突破成功率 +" + bonusValue + "%（1小时内有效）";
  }

  private String applyBuff(
      User user, ItemProperties.Effect.Buff e, double qualityMultiplier, int grade) {
    double gradeDecay = calcGradeDecay(user.getLevel(), grade, true);
    int actualValue = (int) (e.amount() * qualityMultiplier * gradeDecay);
    if (actualValue <= 0) return null;

    LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(e.durationSeconds());
    PlayerBuff buff = PlayerBuff.create(user.getId(), e.attribute(), actualValue, expiresAt);
    playerBuffRepository.save(buff);

    var buffName =
        switch (e.attribute()) {
          case "attack" -> "攻击";
          case "defense" -> "防御";
          case "speed" -> "速度";
          default -> e.attribute();
        };
    return "获得 " + buffName + " +" + actualValue + "（持续" + e.durationSeconds() + "秒）";
  }

  private String applyCure(User user, ItemProperties.Effect.Cure e) {
    if (user.getStatus() == UserStatus.DYING) {
      user.setHpCurrent(user.calculateMaxHp());
      user.setStatus(UserStatus.IDLE);
      return "驱散异常并回满生命值";
    }
    return "没有可驱散的异常状态";
  }

  // ===================== 衰减计算 =====================

  /**
   * 计算等级衰减
   *
   * @param withFloor 是否有保底（exp/breakthrough 有0.1保底，stat无保底）
   */
  private double calcGradeDecay(int playerLevel, int pillGrade, boolean withFloor) {
    double decay = Math.min(1.0, pillGrade / (playerLevel * GRADE_DECAY_COEFFICIENT));
    return withFloor ? Math.max(0.1, decay) : decay;
  }

  /** 计算抗性衰减 */
  private double calcResistanceDecay(Long userId, long templateId) {
    var opt = pillResistanceRepository.findByUserIdAndTemplateId(userId, templateId);
    int count = opt.map(PillResistance::getCount).orElse(0);
    return Math.max(0.1, 1.0 / (1 + count));
  }

  // ===================== 辅助方法 =====================

  private StackableItem findPill(Long userId, String pillName) {
    List<StackableItem> pills =
        stackableItemRepository.findByUserId(userId).stream()
            .filter(
                item ->
                    item.getItemType() == top.stillmisty.xiantao.domain.item.enums.ItemType.POTION
                        && item.getName().contains(pillName))
            .toList();
    return pills.isEmpty() ? null : pills.getFirst();
  }

  private int getPillGrade(StackableItem pill) {
    if (pill.getProperties() == null) return 1;
    Object gradeObj = pill.getProperties().get("grade");
    if (gradeObj instanceof Integer i) return i;
    if (gradeObj instanceof Long l) return l.intValue();
    if (gradeObj instanceof Number n) return n.intValue();
    return 1;
  }
}
