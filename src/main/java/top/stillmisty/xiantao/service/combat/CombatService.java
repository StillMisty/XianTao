package top.stillmisty.xiantao.service.combat;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.WeaponType;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.monster.*;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.pill.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.enums.BindingType;
import top.stillmisty.xiantao.domain.skill.repository.PlayerSkillRepository;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.user.entity.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class CombatService {

  private static final int MAX_BEAST_DEPLOY_COUNT = 2;

  private final EquipmentRepository equipmentRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;
  private final SkillRepository skillRepository;
  private final PlayerSkillRepository playerSkillRepository;
  private final BeastRepository beastRepository;
  private final PlayerBuffRepository playerBuffRepository;
  private final CombatEngine combatEngine;

  public BattleResultVO simulate(CombatTeam teamA, CombatTeam teamB, int maxRounds) {
    Battle battle =
        Battle.of(teamA, teamB, BattleContext.BattleScene.TRAINING, maxRounds, combatEngine);
    return battle.execute();
  }

  public CombatTeam buildPlayerTeam(User user) {
    return buildPlayerTeam(user, Map.of());
  }

  /** 构建玩家队伍，使用预加载的技能映射（避免N+1查询） */
  public CombatTeam buildPlayerTeam(User user, Map<Long, Skill> skillLookup) {
    CombatTeam team = new CombatTeam(user.getId(), "Player");

    BuffValues buffs = loadActiveBuffs(user.getId());

    Equipment weapon = findWeapon(user.getId());
    double attackSpeed = getWeaponAttackSpeed(user.getId(), weapon);

    List<Skill> playerSkills = loadEquippedSkillsFromLookup(user.getId(), weapon, skillLookup);

    team.addMember(
        new PlayerCombatant(user, weapon, attackSpeed, playerSkills)
            .withBuffs(buffs.attack, buffs.defense, buffs.speed));

    List<Beast> deployed = beastRepository.findDeployedByUserId(user.getId());
    for (int i = 0; i < Math.min(deployed.size(), MAX_BEAST_DEPLOY_COUNT); i++) {
      Beast beast = deployed.get(i);
      if (beast.canFight()) {
        List<Skill> beastSkills = List.of();
        if (beast.getSkills() != null && !beast.getSkills().isEmpty()) {
          beastSkills =
              beast.getSkills().stream()
                  .map(skillLookup::get)
                  .filter(java.util.Objects::nonNull)
                  .toList();
        }
        team.addMember(new BeastCombatant(beast, beastSkills));
      }
    }
    return team;
  }

  private List<Skill> loadEquippedSkills(Long userId, Equipment weapon) {
    List<Long> equippedSkillIds =
        playerSkillRepository.findEquippedByUserId(userId).stream()
            .map(top.stillmisty.xiantao.domain.skill.entity.PlayerSkill::getSkillId)
            .toList();

    if (equippedSkillIds.isEmpty()) return List.of();

    List<Skill> allSkills = skillRepository.findByIds(equippedSkillIds);
    return allSkills.stream().filter(skill -> isSkillCompatibleWithWeapon(skill, weapon)).toList();
  }

  private List<Skill> loadEquippedSkillsFromLookup(
      Long userId, Equipment weapon, Map<Long, Skill> skillLookup) {
    List<Long> equippedSkillIds =
        playerSkillRepository.findEquippedByUserId(userId).stream()
            .map(top.stillmisty.xiantao.domain.skill.entity.PlayerSkill::getSkillId)
            .toList();

    if (equippedSkillIds.isEmpty()) return List.of();

    if (skillLookup.isEmpty()) {
      return loadEquippedSkills(userId, weapon);
    }

    return equippedSkillIds.stream()
        .map(skillLookup::get)
        .filter(java.util.Objects::nonNull)
        .filter(skill -> isSkillCompatibleWithWeapon(skill, weapon))
        .toList();
  }

  private boolean isSkillCompatibleWithWeapon(Skill skill, Equipment weapon) {
    BindingType bindingType = skill.getBindingType();
    if (bindingType == null || bindingType == BindingType.NONE) return true;

    if (weapon == null) return false;

    WeaponType weaponType = weapon.getWeaponType();
    if (weaponType == null) return false;

    return switch (bindingType) {
      case WEAPON_TYPE -> weaponType.getCode().equals(skill.getBindingValue());
      case WEAPON_CATEGORY -> weaponType.getCategory().equals(skill.getBindingValue());
      case ELEMENT -> true;
      default -> true;
    };
  }

  // ===================== 辅助方法 =====================

  private record BuffValues(int attack, int defense, int speed) {}

  private BuffValues loadActiveBuffs(Long userId) {
    List<PlayerBuff> activeBuffs = playerBuffRepository.findActiveByUserId(userId);
    int attackBuff = 0, defenseBuff = 0, speedBuff = 0;
    for (PlayerBuff buff : activeBuffs) {
      switch (buff.getBuffType()) {
        case ATTACK -> attackBuff += buff.getValue();
        case DEFENSE -> defenseBuff += buff.getValue();
        case SPEED -> speedBuff += buff.getValue();
        case BREAKTHROUGH -> {}
      }
    }
    return new BuffValues(attackBuff, defenseBuff, speedBuff);
  }

  private Equipment findWeapon(Long userId) {
    return equipmentRepository.findEquippedByUserId(userId).stream()
        .filter(e -> e.getSlot() == EquipmentSlot.WEAPON)
        .findFirst()
        .orElse(null);
  }

  private double getWeaponAttackSpeed(Long userId, Equipment weapon) {
    if (weapon == null) return 1.0;
    return equipmentTemplateRepository
        .findById(weapon.getTemplateId())
        .map(template -> template.getAttackSpeed() != null ? template.getAttackSpeed() : 1.0)
        .orElse(1.0);
  }
}
