package top.stillmisty.xiantao.domain.fudi.vo;

import java.util.List;

public record CellStatusVO(int totalCells, int occupiedCount, int emptyCount,
                            List<Integer> emptyCellIds,
                            List<CellDetailVO> occupiedCells) {
}
