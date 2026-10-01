package top.stillmisty.xiantao.service.combat;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.WeaponType;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.PlayerCombatant;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.skill.entity.PlayerSkill;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.enums.BindingType;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.BeastRepository;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentRepository;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.infrastructure.repository.PlayerSkillRepository;
import top.stillmisty.xiantao.infrastructure.repository.SkillRepository;
import top.stillmisty.xiantao.service.beast.MutationEffectResolver;

@Component
@RequiredArgsConstructor
public class DefaultTeamBuilder implements TeamBuilder {

  private static final int MAX_BEAST_DEPLOY_COUNT = 2;

  private final EquipmentRepository equipmentRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;
  private final SkillRepository skillRepository;
  private final MutationEffectResolver effectResolver;
  private final PlayerSkillRepository playerSkillRepository;
  private final BeastRepository beastRepository;
  private final PlayerBuffRepository playerBuffRepository;

  @Override
  public CombatTeam buildPlayerTeam(Player user) {
    return buildPlayerTeam(user, BuildOptions.DEFAULT);
  }

  @Override
  public CombatTeam buildPlayerTeam(Player user, BuildOptions options) {
    CombatTeam team = new CombatTeam(user.getId(), options.teamName());

    BuffValues buffs = loadActiveBuffs(user.getId());
    Equipment weapon = findWeapon(user.getId());
    double attackSpeed = getWeaponAttackSpeed(user.getId(), weapon);
    List<Skill> playerSkills = loadEquippedSkills(user.getId(), weapon, options.skillLookup());

    team.addMember(
        new PlayerCombatant(user, weapon, attackSpeed, playerSkills)
            .withBuffs(buffs.attack, buffs.defense, buffs.speed));

    List<Beast> beasts =
        options.deployedBeasts() != null
            ? options.deployedBeasts()
            : beastRepository.findDeployedByUserId(user.getId());
    for (int i = 0; i < Math.min(beasts.size(), MAX_BEAST_DEPLOY_COUNT); i++) {
      Beast beast = beasts.get(i);
      if (beast.canFight()) {
        List<Skill> beastSkills = List.of();
        if (beast.getSkills() != null && !beast.getSkills().isEmpty()) {
          beastSkills =
              beast.getSkills().stream()
                  .map(options.skillLookup()::get)
                  .filter(Objects::nonNull)
                  .toList();
        }
        team.addMember(new BeastCombatant(beast, beastSkills, effectResolver));
      }
    }
    return team;
  }

  // ===================== 步骤方法 =====================

  private List<Skill> loadEquippedSkills(
      Long userId, @Nullable Equipment weapon, Map<Long, Skill> skillLookup) {
    List<Long> equippedSkillIds =
        playerSkillRepository.findEquippedByUserId(userId).stream()
            .map(PlayerSkill::getSkillId)
            .toList();
    if (equippedSkillIds.isEmpty()) return List.of();

    List<Skill> skills;
    if (skillLookup.isEmpty()) {
      skills = skillRepository.findByIds(equippedSkillIds);
    } else {
      skills = equippedSkillIds.stream().map(skillLookup::get).filter(Objects::nonNull).toList();
    }

    return skills.stream().filter(skill -> isSkillCompatibleWithWeapon(skill, weapon)).toList();
  }

  private boolean isSkillCompatibleWithWeapon(Skill skill, @Nullable Equipment weapon) {
    BindingType bindingType = skill.getBindingType();
    if (bindingType == null || bindingType == BindingType.NONE) return true;
    if (weapon == null) return false;

    WeaponType weaponType = weapon.getWeaponType();
    if (weaponType == null) return false;

    return switch (bindingType) {
      case WEAPON_TYPE -> weaponType.getCode().equals(skill.getBindingValue());
      case WEAPON_CATEGORY -> weaponType.categoryCode().equalsIgnoreCase(skill.getBindingValue());
      case ELEMENT -> true;
      default -> true;
    };
  }

  private @Nullable Equipment findWeapon(Long userId) {
    return equipmentRepository.findEquippedByUserId(userId).stream()
        .filter(e -> e.getSlot() == EquipmentSlot.WEAPON)
        .findFirst()
        .orElse(null);
  }

  private double getWeaponAttackSpeed(Long userId, @Nullable Equipment weapon) {
    if (weapon == null) return 1.0;
    return equipmentTemplateRepository
        .findById(weapon.getTemplateId())
        .map(template -> template.getAttackSpeed() != null ? template.getAttackSpeed() : 1.0)
        .orElse(1.0);
  }

  private record BuffValues(int attack, int defense, int speed) {}

  private BuffValues loadActiveBuffs(Long userId) {
    List<PlayerBuff> activeBuffs = playerBuffRepository.findActiveByUserId(userId);
    int attackBuff = 0, defenseBuff = 0, speedBuff = 0;
    for (PlayerBuff buff : activeBuffs) {
      switch (buff.getBuffType()) {
        case ATTACK -> attackBuff += buff.getValue();
        case DEFENSE -> defenseBuff += buff.getValue();
        case SPEED -> speedBuff += buff.getValue();
        case BREAKTHROUGH, TRIBULATION_RESIST -> {}
      }
    }
    return new BuffValues(attackBuff, defenseBuff, speedBuff);
  }
}
