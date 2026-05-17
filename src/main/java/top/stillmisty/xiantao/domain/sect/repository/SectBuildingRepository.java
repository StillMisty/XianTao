package top.stillmisty.xiantao.domain.sect.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.sect.entity.SectBuilding;
import top.stillmisty.xiantao.domain.sect.enums.SectBuildingType;

public interface SectBuildingRepository {
  SectBuilding save(SectBuilding building);

  Optional<SectBuilding> findById(Long id);

  List<SectBuilding> findBySectId(Long sectId);

  Optional<SectBuilding> findBySectIdAndType(Long sectId, SectBuildingType buildingType);

  void deleteBySectId(Long sectId);
}
