package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.land.entity.FudiCell;
import top.stillmisty.xiantao.domain.land.enums.CellType;
import top.stillmisty.xiantao.domain.land.repository.FudiCellRepository;
import top.stillmisty.xiantao.infrastructure.mapper.FudiCellMapper;

import java.util.List;
import java.util.Optional;

import static top.stillmisty.xiantao.domain.land.entity.table.FudiCellTableDef.FUDI_CELL;

@Repository
@RequiredArgsConstructor
public class FudiCellRepositoryImpl implements FudiCellRepository {

    private final FudiCellMapper fudiCellMapper;

    @Override
    public Optional<FudiCell> findById(Long id) {
        FudiCell cell = fudiCellMapper.selectOneById(id);
        return Optional.ofNullable(cell);
    }

    @Override
    public Optional<FudiCell> findByFudiIdAndCellId(Long fudiId, Integer cellId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(FUDI_CELL.FUDI_ID.eq(fudiId))
                .and(FUDI_CELL.CELL_ID.eq(cellId));
        FudiCell cell = fudiCellMapper.selectOneByQuery(queryWrapper);
        return Optional.ofNullable(cell);
    }

    @Override
    public List<FudiCell> findByFudiId(Long fudiId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(FUDI_CELL.FUDI_ID.eq(fudiId))
                .orderBy(FUDI_CELL.CELL_ID.asc());
        return fudiCellMapper.selectListByQuery(queryWrapper);
    }

    @Override
    public List<FudiCell> findByFudiIdAndCellType(Long fudiId, CellType cellType) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(FUDI_CELL.FUDI_ID.eq(fudiId))
                .and(FUDI_CELL.CELL_TYPE.eq(cellType.getCode()));
        return fudiCellMapper.selectListByQuery(queryWrapper);
    }

    @Override
    public FudiCell save(FudiCell cell) {
        fudiCellMapper.insertOrUpdateSelective(cell);
        return cell;
    }

    @Override
    public void deleteById(Long id) {
        fudiCellMapper.deleteById(id);
    }

    @Override
    public int countByFudiId(Long fudiId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(FUDI_CELL.FUDI_ID.eq(fudiId));
        return (int) fudiCellMapper.selectCountByQuery(queryWrapper);
    }
}
