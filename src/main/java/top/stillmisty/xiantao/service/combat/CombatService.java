package top.stillmisty.xiantao.service.combat;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.WeaponType;
import top.stillmisty.xiantao.domain.monster.Battle;
import top.stillmisty.xiantao.domain.monster.BattleContext;
import top.stillmisty.xiantao.domain.monster.CombatEngine;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.PlayerCombatant;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.enums.BindingType;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.repository.BeastRepository;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentRepository;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.infrastructure.repository.PlayerSkillRepository;
import top.stillmisty.xiantao.infrastructure.repository.SkillRepository;
import top.stillmisty.xiantao.service.beast.MutationEffectResolver;

@Slf4j
@Service
@RequiredArgsConstructor
public class CombatService {

  private static final int MAX_BEAST_DEPLOY_COUNT = 2;

  private final EquipmentRepository equipmentRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;
  private final SkillRepository skillRepository;
  private final MutationEffectResolver effectResolver;
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
    return buildPlayerTeam(user, Map.of(), "Player");
  }

  /** 构建玩家队伍，使用预加载的技能映射（避免N+1查询） */
  public CombatTeam buildPlayerTeam(User user, Map<Long, Skill> skillLookup) {
    return buildPlayerTeam(user, skillLookup, "Player");
  }

  /** 构建玩家队伍，可指定队伍名称（PvP时用于区分双方） */
  public CombatTeam buildPlayerTeam(User user, Map<Long, Skill> skillLookup, String teamName) {
    CombatTeam team = new CombatTeam(user.getId(), teamName);

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
        team.addMember(new BeastCombatant(beast, beastSkills, effectResolver));
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
        case BREAKTHROUGH, TRIBULATION_RESIST -> {}
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

  // ===================== 战斗后 HP 应用 =====================

  /** 计算队伍属性统计 */
  public record TeamStats(int totalMaxHp, int avgAttack, int avgDef, int avgSpeed) {}

  public TeamStats calculateTeamStats(CombatTeam team) {
    List<Combatant> members = team.members();
    int totalMaxHp = 0, totalAtk = 0, totalDef = 0, totalSpd = 0;
    int count = 0;
    for (Combatant c : members) {
      if (c.isAlive()) {
        totalMaxHp += c.getMaxHp();
        totalAtk += c.getAttack();
        totalDef += c.getDefense();
        totalSpd += c.getSpeed();
        count++;
      }
    }
    count = Math.max(1, count);
    return new TeamStats(totalMaxHp, totalAtk / count, totalDef / count, totalSpd / count);
  }
}
