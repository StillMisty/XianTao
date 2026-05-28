package top.stillmisty.xiantao.handle.command;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.item.entity.ElementRange;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.ProductionItem;
import top.stillmisty.xiantao.domain.item.entity.SeedProduct;
import top.stillmisty.xiantao.domain.item.vo.EquipmentDetailVO;
import top.stillmisty.xiantao.domain.item.vo.StackableItemDetailVO;
import top.stillmisty.xiantao.domain.monster.vo.MonsterDetailVO;
import top.stillmisty.xiantao.domain.pill.enums.PillQuality;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.infrastructure.repository.BeastTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.SkillRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.inventory.EquipmentService;
import top.stillmisty.xiantao.service.inventory.InventoryService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCommandHandler implements CommandGroup {

  private final EquipmentService equipmentService;
  private final InventoryService inventoryService;
  private final MonsterTemplateRepository monsterTemplateRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final SkillRepository skillRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;
  private final BeastTemplateRepository beastTemplateRepository;

  /** 统一「查看」命令 — 依次尝试装备、怪物、物品 */
  public String handleView(PlatformType platform, String openId, String target, TextFormat fmt) {
    log.debug("处理查看 - Platform: {}, OpenId: {}, Target: {}", platform, openId, target);

    String result =
        tryResolve(
            () -> equipmentService.getEquipmentDetail(platform, openId, target),
            vo -> formatEquipmentDetail(vo, fmt));
    if (result != null) return result;

    MonsterDetailVO monster = resolveMonsterDetail(target);
    if (monster != null) return formatMonsterDetail(monster, fmt);

    result =
        tryResolve(
            () -> inventoryService.getItemDetail(platform, openId, target),
            vo -> formatStackableItemDetail(vo, fmt));
    if (result != null) return result;

    return "未找到 [" + target + "]，可输入装备名/编号、怪物名或物品名";
  }

  private <T> String tryResolve(Supplier<ServiceResult<T>> call, Function<T, String> onSuccess) {
    try {
      return switch (call.get()) {
        case ServiceResult.Success<T> s -> onSuccess.apply(s.data());
        case ServiceResult.Failure<T> f -> {
          if (f.errorCode() == ErrorCode.ITEM_MULTIPLE_MATCH) yield f.errorMessage();
          yield null;
        }
      };
    } catch (BusinessException e) {
      return null;
    }
  }

  private MonsterDetailVO resolveMonsterDetail(String target) {
    return monsterTemplateRepository
        .findByName(target)
        .map(
            t ->
                new MonsterDetailVO(
                    t.getId(),
                    t.getName(),
                    t.getDescription(),
                    t.getMonsterType() != null ? t.getMonsterType().getName() : "",
                    t.getBaseLevel() != null ? t.getBaseLevel() : 0,
                    t.getBaseHp() != null ? t.getBaseHp() : 0,
                    t.getBaseAttack() != null ? t.getBaseAttack() : 0,
                    t.getBaseDefense() != null ? t.getBaseDefense() : 0,
                    t.getBaseSpeed() != null ? t.getBaseSpeed() : 0,
                    t.getExpReward() != null ? t.getExpReward() : 0,
                    t.getTags()))
        .orElse(null);
  }

  private String formatEquipmentDetail(EquipmentDetailVO vo, TextFormat fmt) {
    var sb = new StringBuilder();
    sb.append(fmt.subHeading(vo.getRarityEmoji() + " " + vo.getDisplayName()));
    sb.append(fmt.listItem("部位：" + vo.getSlotName()));
    sb.append(fmt.listItem("稀有度：" + vo.getRarityName()));
    if (vo.getWeaponTypeName() != null) {
      sb.append(fmt.listItem("类型：" + vo.getWeaponTypeName()));
    }
    sb.append(fmt.listItem("攻击：" + vo.getAttack() + " | 防御：" + vo.getDefense()));
    sb.append(
        fmt.listItem(
            String.format(
                "力道 +%d | 根骨 +%d | 身法 +%d | 悟性 +%d",
                vo.getStrBonus(), vo.getConBonus(), vo.getAgiBonus(), vo.getWisBonus())));
    if (vo.getAffixDescriptions() != null && !vo.getAffixDescriptions().isEmpty()) {
      sb.append(fmt.listItem("词条：" + String.join("、", vo.getAffixDescriptions())));
    }
    sb.append(
        fmt.listItem(
            String.format(
                "锻造：+%d | 品质系数：%.2f",
                vo.getForgeLevel() != null ? vo.getForgeLevel() : 0,
                vo.getQualityMultiplier() != null ? vo.getQualityMultiplier() : 1.0)));
    if (Boolean.TRUE.equals(vo.getEquipped())) {
      sb.append(fmt.listItem("状态：已穿戴"));
    }
    return sb.toString().strip();
  }

  private String formatStackableItemDetail(StackableItemDetailVO vo, TextFormat fmt) {
    var sb = new StringBuilder();
    sb.append(fmt.subHeading(vo.name()));
    sb.append(fmt.listItem("类型：" + vo.typeName()));
    sb.append(fmt.listItem("数量：" + vo.quantity()));
    if (vo.description() != null && !vo.description().isEmpty()) {
      sb.append(fmt.listItem("描述：" + vo.description()));
    }
    appendTypeProperties(vo, sb, fmt);
    return sb.toString().strip();
  }

  private void appendTypeProperties(StackableItemDetailVO vo, StringBuilder sb, TextFormat fmt) {
    var template = itemTemplateRepository.findById(vo.templateId()).orElse(null);
    ItemProperties typed = template != null ? template.typedProperties() : null;

    switch (vo.typeName()) {
      case "丹药" -> {
        Object grade = vo.properties() != null ? vo.properties().get("grade") : null;
        if (grade instanceof Number n) {
          sb.append(fmt.listItem("品级：" + n.intValue() + "级"));
        }
        if (vo.quality() != null && !vo.quality().isEmpty()) {
          try {
            PillQuality pq = PillQuality.fromCode(vo.quality());
            sb.append(fmt.listItem("成色：" + pq.getChineseName()));
            sb.append(fmt.listItem("效果倍率：×" + pq.getMultiplier()));
          } catch (IllegalArgumentException e) {
            sb.append(fmt.listItem("成色：" + vo.quality()));
          }
        }
        if (typed instanceof ItemProperties.Potion p && !p.effects().isEmpty()) {
          sb.append(fmt.listItem("效果：" + p.effects().size() + "种"));
        }
      }
      case "药材" -> {
        if (typed instanceof ItemProperties.Herb h) {
          var parts = new ArrayList<String>();
          for (var entry : h.elements().entrySet()) {
            parts.add(entry.getKey() + "" + entry.getValue());
          }
          if (!parts.isEmpty()) {
            sb.append(fmt.listItem("五行：" + String.join(" ", parts)));
          }
        }
      }
      case "锻材" -> {
        if (typed instanceof ItemProperties.Material m) {
          sb.append(
              fmt.listItem(
                  String.format("刚硬：%d | 韧性：%d | 灵气：%d", m.rigidity(), m.toughness(), m.spirit())));
        }
      }
      case "种子" -> {
        if (typed instanceof ItemProperties.Growth g) {
          sb.append(fmt.listItem("成熟时间：" + g.growTime() + "小时"));
          if (g.maxHarvest() > 1) {
            sb.append(fmt.listItem("可收获：" + g.maxHarvest() + "次"));
          }
          appendSeedProducts(g.productionItems(), sb, fmt);
        }
      }
      case "兽卵" -> {
        if (typed instanceof ItemProperties.BeastEgg e) {
          var beastTemplate = beastTemplateRepository.findById(e.beastTemplateId()).orElse(null);
          if (beastTemplate != null) {
            sb.append(fmt.listItem("孵化时间：" + beastTemplate.getGrowTime() + "小时"));
            if (beastTemplate.getSkillPool() != null) {
              var pool = beastTemplate.getSkillPool();
              if (pool.innateSkills() != null && !pool.innateSkills().isEmpty()) {
                sb.append(fmt.listItem("天生技能：" + pool.innateSkills().size() + "个"));
              }
              if (pool.awakeningSkills() != null && !pool.awakeningSkills().isEmpty()) {
                sb.append(fmt.listItem("觉醒技能：" + pool.awakeningSkills().size() + "个"));
              }
            }
            appendProductionItems(beastTemplate.getProductionItems(), "产出", true, sb, fmt);
          }
        }
      }
      case "法决玉简" -> {
        if (typed instanceof ItemProperties.SkillJade s) {
          skillRepository
              .findById(s.skillId())
              .ifPresent(
                  skill -> {
                    sb.append(fmt.listItem("法决：" + fmt.bold(skill.getName())));
                    sb.append(
                        fmt.listItem(
                            "类型："
                                + (skill.getSkillType() != null
                                    ? skill.getSkillType().getName()
                                    : "未知")));
                    if (skill.getEffects() != null && !skill.getEffects().isEmpty()) {
                      var effectText = new StringBuilder("效果：");
                      for (int i = 0; i < skill.getEffects().size(); i++) {
                        if (i > 0) effectText.append(" + ");
                        effectText.append(SkillFormatter.formatEffect(skill.getEffects().get(i)));
                      }
                      sb.append(fmt.listItem(effectText.toString()));
                    }
                    if (skill.getBindingType() != null) {
                      var bindingText =
                          new StringBuilder("绑定：").append(skill.getBindingType().getName());
                      if (skill.getBindingValue() != null && !skill.getBindingValue().isBlank()) {
                        bindingText.append("（").append(skill.getBindingValue()).append("）");
                      }
                      sb.append(fmt.listItem(bindingText.toString()));
                    }
                    sb.append(
                        fmt.listItem(
                            "调息："
                                + (skill.getCooldownSeconds() != null
                                    ? skill.getCooldownSeconds()
                                    : 0)
                                + "息 | 修为："
                                + top.stillmisty.xiantao.domain.user.enums.CultivationRealm
                                    .realmDisplay(
                                        skill.getLevelRequirement() != null
                                            ? skill.getLevelRequirement()
                                            : 1)));
                    if (skill.getDescription() != null && !skill.getDescription().isEmpty()) {
                      sb.append(fmt.listItem(skill.getDescription()));
                    }
                  });
        }
      }
      case "丹方卷轴" -> {
        if (typed instanceof ItemProperties.Scroll s) {
          sb.append(fmt.listItem("品级：" + s.grade() + "级"));
          itemTemplateRepository
              .findById(s.resultItemId())
              .ifPresent(
                  resultTemplate ->
                      sb.append(
                          fmt.listItem(
                              "产出：" + resultTemplate.getName() + " x" + s.resultQuantity())));
          if (s.requirements() != null && !s.requirements().isEmpty()) {
            var reqParts = new ArrayList<String>();
            s.requirements()
                .forEach(
                    (elem, range) -> reqParts.add(elem + ":" + range.min() + "-" + range.max()));
            sb.append(fmt.listItem("需求五行：" + String.join(" ", reqParts)));
          }
        }
      }
      case "锻造图纸" -> {
        if (typed instanceof ItemProperties.ForgingBlueprint f) {
          sb.append(fmt.listItem("品级：" + f.grade() + "级"));
          equipmentTemplateRepository
              .findById(f.equipmentTemplateId())
              .ifPresent(eqt -> sb.append(fmt.listItem("产出：" + eqt.getName())));
          if (f.requirements() != null && !f.requirements().isEmpty()) {
            sb.append(
                fmt.listItem(
                    String.format(
                        "需求：刚硬%d-%d 韧性%d-%d 灵气%d-%d",
                        f.requirements().getOrDefault("RIGIDITY", new ElementRange(0, 0)).min(),
                        f.requirements().getOrDefault("RIGIDITY", new ElementRange(0, 0)).max(),
                        f.requirements().getOrDefault("TOUGHNESS", new ElementRange(0, 0)).min(),
                        f.requirements().getOrDefault("TOUGHNESS", new ElementRange(0, 0)).max(),
                        f.requirements().getOrDefault("SPIRIT", new ElementRange(0, 0)).min(),
                        f.requirements().getOrDefault("SPIRIT", new ElementRange(0, 0)).max())));
          }
        }
      }
      default -> {}
    }
  }

  private void appendProductionItems(
      List<ProductionItem> items,
      String label,
      boolean showWeight,
      StringBuilder sb,
      TextFormat fmt) {
    if (items == null || items.isEmpty()) return;
    int totalWeight = showWeight ? items.stream().mapToInt(ProductionItem::weight).sum() : 0;
    var names =
        items.stream()
            .map(
                pi -> {
                  String name =
                      itemTemplateRepository
                          .findById(pi.templateId())
                          .map(t -> t.getName())
                          .orElse("物品#" + pi.templateId());
                  if (showWeight && totalWeight > 0 && items.size() > 1) {
                    int pct = pi.weight() * 100 / totalWeight;
                    return name + "(" + pct + "%)";
                  }
                  return name;
                })
            .toList();
    sb.append(fmt.listItem(label + "：" + String.join("、", names)));
  }

  private void appendSeedProducts(List<SeedProduct> items, StringBuilder sb, TextFormat fmt) {
    if (items == null || items.isEmpty()) return;
    var names =
        items.stream()
            .map(
                sp ->
                    itemTemplateRepository
                        .findById(sp.templateId())
                        .map(t -> t.getName())
                        .orElse("物品#" + sp.templateId()))
            .toList();
    sb.append(fmt.listItem("产出：" + String.join("、", names)));
  }

  private String formatMonsterDetail(MonsterDetailVO vo, TextFormat fmt) {
    var sb = new StringBuilder();
    sb.append(fmt.subHeading(vo.name()));
    sb.append(fmt.listItem("类型：" + vo.typeName()));
    sb.append(fmt.listItem("等级：" + vo.baseLevel()));
    sb.append(fmt.listItem("HP：" + vo.baseHp()));
    sb.append(fmt.listItem("攻击：" + vo.baseAttack() + " | 防御：" + vo.baseDefense()));
    sb.append(fmt.listItem("速度：" + vo.baseSpeed()));
    sb.append(fmt.listItem("修为奖励：" + vo.expReward()));
    if (vo.description() != null && !vo.description().isEmpty()) {
      sb.append(fmt.listItem("描述：" + vo.description()));
    }
    return sb.toString().strip();
  }

  @Override
  public String groupName() {
    return "查看";
  }

  @Override
  public String groupDescription() {
    return "查看物品/装备/怪物详情";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(new CommandEntry("查看 「目标」", "查看装备/怪物/物品详情", "查看 铁剑"));
  }
}
