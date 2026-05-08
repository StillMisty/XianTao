package top.stillmisty.xiantao.service;

import java.util.List;
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

  private final EquipmentRepository equipmentRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;
  private final SkillRepository skillRepository;
  private final PlayerSkillRepository playerSkillRepository;
  private final BeastRepository beastRepository;
  private final PlayerBuffRepository playerBuffRepository;
  private final CombatEngine combatEngine;

  public BattleResultVO simulate(Team teamA, Team teamB, int maxRounds) {
    Battle battle =
        Battle.of(teamA, teamB, BattleContext.BattleScene.TRAINING, maxRounds, combatEngine);
    return battle.execute();
  }

  public Team buildPlayerTeam(User user) {
    Team team = new Team(user.getId(), "Player");

    List<PlayerBuff> activeBuffs = playerBuffRepository.findActiveByUserId(user.getId());
    int attackBuff = 0, defenseBuff = 0, speedBuff = 0;
    for (PlayerBuff buff : activeBuffs) {
      switch (buff.getBuffType()) {
        case "attack" -> attackBuff += buff.getValue();
        case "defense" -> defenseBuff += buff.getValue();
        case "speed" -> speedBuff += buff.getValue();
      }
    }

    Equipment weapon =
        equipmentRepository.findEquippedByUserId(user.getId()).stream()
            .filter(e -> e.getSlot() == EquipmentSlot.WEAPON)
            .findFirst()
            .orElse(null);

    double attackSpeed = 1.0;
    if (weapon != null) {
      attackSpeed =
          equipmentTemplateRepository
              .findById(weapon.getTemplateId())
              .map(template -> template.getAttackSpeed() != null ? template.getAttackSpeed() : 1.0)
              .orElse(1.0);
    }

    List<Skill> playerSkills = loadEquippedSkills(user.getId(), weapon);

    team.addMember(
        new PlayerCombatant(user, weapon, attackSpeed, playerSkills)
            .withBuffs(attackBuff, defenseBuff, speedBuff));

    List<Beast> deployed = beastRepository.findDeployedByUserId(user.getId());
    for (int i = 0; i < Math.min(deployed.size(), 2); i++) {
      Beast beast = deployed.get(i);
      if (beast.canFight()) {
        List<Skill> beastSkills = List.of();
        if (beast.getSkills() != null && !beast.getSkills().isEmpty()) {
          beastSkills = skillRepository.findByIds(beast.getSkills());
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

  private boolean isSkillCompatibleWithWeapon(Skill skill, Equipment weapon) {
    BindingType bindingType = skill.getBindingType();
    if (bindingType == null || bindingType == BindingType.NONE) return true;

    if (weapon == null) return false;

    WeaponType weaponType = weapon.getWeaponType();
    if (weaponType == null) return false;

    return switch (bindingType) {
      case WEAPON_TYPE -> weaponType.getCode().equals(skill.getBindingValue());
      case WEAPON_CATEGORY -> weaponType.getCategory().equals(skill.getBindingValue());
      case ELEMENT, NONE -> true;
    };
  }
}
