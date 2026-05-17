package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.sect.entity.SectBuilding;
import top.stillmisty.xiantao.domain.sect.enums.SectBuildingType;
import top.stillmisty.xiantao.domain.sect.repository.SectBuildingRepository;
import top.stillmisty.xiantao.infrastructure.mapper.SectBuildingMapper;

@Repository
@RequiredArgsConstructor
public class SectBuildingRepositoryImpl implements SectBuildingRepository {

  private final SectBuildingMapper mapper;

  @Override
  public SectBuilding save(SectBuilding building) {
    mapper.insertOrUpdateSelective(building);
    return building;
  }

  @Override
  public Optional<SectBuilding> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  @Override
  public List<SectBuilding> findBySectId(Long sectId) {
    QueryWrapper query = new QueryWrapper().eq(SectBuilding::getSectId, sectId);
    return mapper.selectListByQuery(query);
  }

  @Override
  public Optional<SectBuilding> findBySectIdAndType(Long sectId, SectBuildingType buildingType) {
    QueryWrapper query =
        new QueryWrapper()
            .eq(SectBuilding::getSectId, sectId)
            .eq(SectBuilding::getBuildingType, buildingType);
    return Optional.ofNullable(mapper.selectOneByQuery(query));
  }

  @Override
  public void deleteBySectId(Long sectId) {
    QueryWrapper query = new QueryWrapper().eq(SectBuilding::getSectId, sectId);
    mapper.deleteByQuery(query);
  }
}
