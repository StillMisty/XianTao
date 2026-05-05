package top.stillmisty.xiantao.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.vo.BeastSkillPoolVO;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;

/** 灵兽技能：技能池、先天技解锁、后天悟觉醒 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeastSkillService {

  private final ItemTemplateRepository itemTemplateRepository;

  BeastSkillPoolVO getBeastSkillPool(Integer templateId) {
    if (templateId == null) {
      return null;
    }
    ItemTemplate template = itemTemplateRepository.findById(templateId.longValue()).orElse(null);
    if (template == null) {
      return null;
    }
    var props = template.typedProperties();
    if (!(props instanceof ItemProperties.BeastEgg egg)) {
      return null;
    }
    var pool = egg.skillPool();
    if (pool == null) {
      return null;
    }
    var innateSkills =
        pool.innateSkills().stream()
            .map(is -> new BeastSkillPoolVO.InnateSkill(is.skillId(), is.unlock()))
            .toList();
    var awakeningSkills =
        pool.awakeningSkills().stream()
            .map(as -> new BeastSkillPoolVO.AwakeningSkill(as.skillId(), as.weight()))
            .toList();
    return new BeastSkillPoolVO(innateSkills, awakeningSkills);
  }

  void unlockInnateSkills(Beast beast, String unlockCondition) {
    BeastSkillPoolVO skillPool = getBeastSkillPool(beast.getTemplateId().intValue());
    if (skillPool == null) {
      return;
    }
    List<Long> currentSkills = beast.getSkills();
    if (currentSkills == null) {
      currentSkills = new ArrayList<>();
    }
    for (BeastSkillPoolVO.InnateSkill innateSkill : skillPool.innateSkills()) {
      if (innateSkill.unlock().equals(unlockCondition)) {
        if (!currentSkills.contains(innateSkill.skillId())) {
          currentSkills.add(innateSkill.skillId());
          log.info("灵兽 {} 解锁先天技: {}", beast.getBeastName(), innateSkill.skillId());
        }
      }
    }
    beast.setSkills(currentSkills);
  }

  public void tryAwakeningSkill(Beast beast) {
    BeastSkillPoolVO skillPool = getBeastSkillPool(beast.getTemplateId().intValue());
    if (skillPool == null || skillPool.awakeningSkills().isEmpty()) {
      return;
    }
    List<Long> currentSkills = beast.getSkills();
    if (currentSkills == null) {
      currentSkills = new ArrayList<>();
    }
    if (currentSkills.size() >= 4) {
      return;
    }
    if (ThreadLocalRandom.current().nextInt(100) >= 15) {
      return;
    }
    int totalWeight = 0;
    for (BeastSkillPoolVO.AwakeningSkill awakeningSkill : skillPool.awakeningSkills()) {
      totalWeight += awakeningSkill.weight();
    }
    if (totalWeight <= 0) {
      return;
    }
    int random = ThreadLocalRandom.current().nextInt(totalWeight);
    int current = 0;
    for (BeastSkillPoolVO.AwakeningSkill awakeningSkill : skillPool.awakeningSkills()) {
      current += awakeningSkill.weight();
      if (random < current) {
        if (!currentSkills.contains(awakeningSkill.skillId())) {
          currentSkills.add(awakeningSkill.skillId());
          beast.setSkills(currentSkills);
          log.info("灵兽 {} 觉醒后天悟: {}", beast.getBeastName(), awakeningSkill.skillId());
          return;
        }
        break;
      }
    }
  }
}
