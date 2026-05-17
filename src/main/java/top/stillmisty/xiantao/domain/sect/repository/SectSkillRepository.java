package top.stillmisty.xiantao.domain.sect.repository;

import java.util.List;
import top.stillmisty.xiantao.domain.sect.entity.SectSkill;

/** 宗门功法仓储接口 */
public interface SectSkillRepository {

  SectSkill save(SectSkill skill);

  List<SectSkill> findBySectId(Long sectId);

  void deleteBySectId(Long sectId);
}
