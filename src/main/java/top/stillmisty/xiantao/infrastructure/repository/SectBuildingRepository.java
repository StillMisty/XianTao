package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.sect.entity.table.SectBuildingTableDef.SECT_BUILDING;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.sect.entity.SectBuilding;
import top.stillmisty.xiantao.domain.sect.enums.SectBuildingType;
import top.stillmisty.xiantao.infrastructure.mapper.SectBuildingMapper;

@Repository
@RequiredArgsConstructor
public class SectBuildingRepository {

  private final SectBuildingMapper mapper;

  public SectBuilding save(SectBuilding building) {
    mapper.insertOrUpdateSelective(building);
    return building;
  }

  public Optional<SectBuilding> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  public List<SectBuilding> findBySectId(Long sectId) {
    QueryWrapper query = QueryWrapper.create().where(SECT_BUILDING.SECT_ID.eq(sectId));
    return mapper.selectListByQuery(query);
  }

  public Optional<SectBuilding> findBySectIdAndType(Long sectId, SectBuildingType buildingType) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(SECT_BUILDING.SECT_ID.eq(sectId))
            .and(SECT_BUILDING.BUILDING_TYPE.eq(buildingType));
    return Optional.ofNullable(mapper.selectOneByQuery(query));
  }

  public void deleteBySectId(Long sectId) {
    QueryWrapper query = QueryWrapper.create().where(SECT_BUILDING.SECT_ID.eq(sectId));
    mapper.deleteByQuery(query);
  }
}
