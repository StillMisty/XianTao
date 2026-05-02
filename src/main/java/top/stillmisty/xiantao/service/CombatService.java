package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.map.entity.MonsterSpawn;
import top.stillmisty.xiantao.domain.monster.*;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;
import top.stillmisty.xiantao.domain.pill.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.user.entity.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class CombatService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentTemplateRepository equipmentTemplateRepository;
    private final SkillRepository skillRepository;
    private final BeastRepository beastRepository;
    private final PlayerBuffRepository playerBuffRepository;
    private final CombatEngine combatEngine;

    public BattleResultVO simulate(Team teamA, Team teamB, int maxRounds) {
        Battle battle = Battle.of(teamA, teamB, BattleContext.BattleScene.TRAINING, maxRounds, combatEngine);
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

        team.addMember(new PlayerCombatant(user, equipmentRepository, equipmentTemplateRepository)
                .withBuffs(attackBuff, defenseBuff, speedBuff));

        List<Beast> deployed = beastRepository.findDeployedByUserId(user.getId());
        int beastLimit = Math.min(3, user.getLevel() / 5 + 1);
        for (int i = 0; i < Math.min(deployed.size(), beastLimit); i++) {
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

    public Team buildMonsterTeam(MonsterTemplate tmpl, int count) {
        Team team = new Team(0L, "Monsters");
        for (int j = 0; j < count; j++) {
            List<Skill> monsterSkills = tmpl.getSkills() != null && !tmpl.getSkills().isEmpty()
                    ? skillRepository.findByIds(tmpl.getSkills())
                    : List.of();
            int monsterLevel = tmpl.getBaseLevel() + ThreadLocalRandom.current().nextInt(-2, 3);
            monsterLevel = Math.max(1, monsterLevel);
            team.addMember(new Monster(tmpl, monsterLevel, monsterSkills));
        }
        return team;
    }
}
