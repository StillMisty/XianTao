package top.stillmisty.xiantao.domain.sect.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.sect.entity.Sect;

public interface SectRepository {
  Sect save(Sect sect);

  Optional<Sect> findById(Long id);

  Optional<Sect> findByName(String name);

  Optional<Sect> findByLeaderId(Long leaderId);

  List<Sect> findAll();

  void deleteById(Long id);
}
