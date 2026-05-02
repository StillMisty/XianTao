package top.stillmisty.xiantao.domain.fudi.repository;

import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;

import java.util.List;
import java.util.Optional;

public interface FudiCellRepository {

    Optional<FudiCell> findById(Long id);

    Optional<FudiCell> findByFudiIdAndCellId(Long fudiId, Integer cellId);

    List<FudiCell> findByFudiId(Long fudiId);

    List<FudiCell> findByFudiIdAndCellType(Long fudiId, CellType cellType);

    FudiCell save(FudiCell cell);

    void deleteById(Long id);

    int countByFudiId(Long fudiId);
}
