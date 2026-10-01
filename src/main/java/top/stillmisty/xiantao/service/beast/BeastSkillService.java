package top.stillmisty.xiantao.service.beast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.enums.SkillUnlock;
import top.stillmisty.xiantao.domain.beast.vo.BeastSkillPoolVO;
import top.stillmisty.xiantao.infrastructure.repository.BeastTemplateRepository;

/** 灵兽技能：技能池、先天技解锁、后天悟觉醒 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeastSkillService {

  private final BeastTemplateRepository beastTemplateRepository;

  /** 按灵兽模板 ID 读取技能池（beast.template_id 指向 beast_template.id） */
  @Nullable BeastSkillPoolVO getBeastSkillPool(Long beastTemplateId) {
    if (beastTemplateId == null) {
      return null;
    }
    var beastTemplate = beastTemplateRepository.findById(beastTemplateId).orElse(null);
    if (beastTemplate == null) {
      return null;
    }
    var pool = beastTemplate.getSkillPool();
    if (pool == null) {
      return null;
    }
    var innateSkills =
        pool.innateSkills().stream()
            .map(is -> new BeastSkillPoolVO.InnateSkill(is.skillId(), is.unlock().getCode()))
            .toList();
    var awakeningSkills =
        pool.awakeningSkills().stream()
            .map(as -> new BeastSkillPoolVO.AwakeningSkill(as.skillId(), as.weight()))
            .toList();
    return new BeastSkillPoolVO(innateSkills, awakeningSkills);
  }

  void unlockInnateSkills(Beast beast, SkillUnlock unlockCondition) {
    BeastSkillPoolVO skillPool = getBeastSkillPool(beast.getTemplateId());
    if (skillPool == null) {
      return;
    }
    // 技能列表可能是不可变 List.of()，解锁前复制为可变列表
    List<Long> currentSkills = beast.getSkills();
    currentSkills = currentSkills == null ? new ArrayList<>() : new ArrayList<>(currentSkills);
    for (BeastSkillPoolVO.InnateSkill innateSkill : skillPool.innateSkills()) {
      // 技能池数据与枚举 code 均为大写（BIRTH / TIER_N）
      if (unlockCondition.getCode().equals(innateSkill.unlock())) {
        if (!currentSkills.contains(innateSkill.skillId())) {
          currentSkills.add(innateSkill.skillId());
          log.debug("灵兽 {} 解锁先天技: {}", beast.getBeastName(), innateSkill.skillId());
        }
      }
    }
    beast.setSkills(currentSkills);
  }

  public void tryAwakeningSkill(Beast beast) {
    BeastSkillPoolVO skillPool = getBeastSkillPool(beast.getTemplateId());
    if (skillPool == null || skillPool.awakeningSkills().isEmpty()) {
      return;
    }
    List<Long> currentSkills = beast.getSkills();
    currentSkills = currentSkills == null ? new ArrayList<>() : new ArrayList<>(currentSkills);
    final List<Long> skills = currentSkills;
    if (skills.size() >= 4) {
      return;
    }
    if (ThreadLocalRandom.current().nextInt(100) >= 15) {
      return;
    }
    List<BeastSkillPoolVO.AwakeningSkill> unlearned =
        skillPool.awakeningSkills().stream().filter(as -> !skills.contains(as.skillId())).toList();
    if (unlearned.isEmpty()) {
      return;
    }
    int totalWeight = 0;
    for (BeastSkillPoolVO.AwakeningSkill awakeningSkill : unlearned) {
      totalWeight += awakeningSkill.weight();
    }
    if (totalWeight <= 0) {
      return;
    }
    int random = ThreadLocalRandom.current().nextInt(totalWeight);
    int current = 0;
    for (BeastSkillPoolVO.AwakeningSkill awakeningSkill : unlearned) {
      current += awakeningSkill.weight();
      if (random < current) {
        currentSkills.add(awakeningSkill.skillId());
        beast.setSkills(currentSkills);
        log.debug("灵兽 {} 觉醒后天悟: {}", beast.getBeastName(), awakeningSkill.skillId());
        return;
      }
    }
  }
}
