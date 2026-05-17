package top.stillmisty.xiantao.domain.sect.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;

public interface SectMemberRepository {
  SectMember save(SectMember member);

  Optional<SectMember> findById(Long id);

  Optional<SectMember> findByUserId(Long userId);

  List<SectMember> findBySectId(Long sectId);

  long countBySectId(Long sectId);

  void deleteById(Long id);

  void deleteByUserId(Long userId);
}
