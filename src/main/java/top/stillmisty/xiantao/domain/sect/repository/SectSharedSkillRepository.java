package top.stillmisty.xiantao.domain.sect.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.sect.entity.SectSharedSkill;
import top.stillmisty.xiantao.domain.sect.enums.SectSharedSkillStatus;

public interface SectSharedSkillRepository {
  SectSharedSkill save(SectSharedSkill skill);

  Optional<SectSharedSkill> findById(Long id);

  List<SectSharedSkill> findBySectId(Long sectId);

  List<SectSharedSkill> findBySectIdAndStatus(Long sectId, SectSharedSkillStatus status);

  Optional<SectSharedSkill> findBySectIdAndSkillId(Long sectId, Long skillId);

  long countBySectIdAndStatus(Long sectId, SectSharedSkillStatus status);

  void deleteById(Long id);

  void deleteBySectId(Long sectId);
}
