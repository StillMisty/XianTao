package top.stillmisty.xiantao.service;

import static top.stillmisty.xiantao.service.ErrorCode.*;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.beast.vo.BeastStatusVO;
import top.stillmisty.xiantao.domain.fudi.entity.CellConfig;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.fudi.vo.CellDetailVO;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;

/** 灵兽显示和地块操作的共享辅助方法 */
@Component
@RequiredArgsConstructor
public class BeastDisplayHelper {

  private final BeastRepository beastRepository;
  private final FudiCellRepository fudiCellRepository;
  private final FudiHelper fudiHelper;

  public record PenCellBeast(Fudi fudi, FudiCell cell, Integer cellId, Beast beast) {}

  PenCellBeast getBeastFromPenCell(Long userId, String position, boolean checkIncubating) {
    Integer cellId = fudiHelper.parseCellId(position);
    Fudi fudi =
        fudiHelper
            .findAndTouchFudi(userId)
            .orElseThrow(() -> new BusinessException(FUDI_NOT_FOUND));

    FudiCell cell =
        fudiCellRepository
            .findByFudiIdAndCellId(fudi.getId(), cellId)
            .orElseThrow(() -> new BusinessException(CELL_NOT_FOUND, cellId));

    if (cell.getCellType() != CellType.PEN) {
      throw new BusinessException(CELL_NOT_PEN, cellId);
    }
    if (checkIncubating && isIncubating(cell)) {
      throw new BusinessException(BEAST_HATCHING);
    }

    Beast beast = findBeastByCell(cell);
    if (beast == null) {
      throw new BusinessException(BEAST_NOT_FOUND);
    }
    return new PenCellBeast(fudi, cell, cellId, beast);
  }

  Beast findBeastByCell(FudiCell cell) {
    if (!(cell.getConfig() instanceof CellConfig.PenConfig pen) || pen.beastId() == null)
      return null;
    return beastRepository.findById(pen.beastId()).orElse(null);
  }

  BeastStatusVO convertToBeastStatusVO(Beast beast) {
    return BeastStatusVO.builder()
        .id(beast.getId())
        .beastName(beast.getBeastName())
        .quality(beast.getQuality().getCode())
        .isMutant(Boolean.TRUE.equals(beast.getIsMutant()))
        .mutationTraits(beast.getMutationTraits())
        .tier(beast.getTier())
        .level(beast.getLevel())
        .exp(beast.getExp())
        .attack(beast.getAttack())
        .defense(beast.getDefense())
        .maxHp(beast.getMaxHp())
        .hpCurrent(beast.getHpCurrent())
        .skills(beast.getSkills())
        .isDeployed(Boolean.TRUE.equals(beast.getIsDeployed()))
        .needsRecovery(beast.needsRecovery())
        .pennedCellId(beast.getPennedCellId() != null ? beast.getPennedCellId() : 0)
        .build();
  }

  PenCellVO buildPenCellVO(FudiCell cell) {
    Beast beast = findBeastByCell(cell);
    CellConfig.PenConfig pen = cell.getConfig() instanceof CellConfig.PenConfig p ? p : null;

    String beastName = beast != null ? beast.getBeastName() : "未知灵兽";
    int tier = beast != null ? beast.getTier() : 1;
    String qualityChinese = beast != null ? beast.getQuality().getChineseName() : "凡品";
    int qualityOrdinal = beast != null ? beast.getQuality().getOrder() : 1;
    boolean isMutant = beast != null && Boolean.TRUE.equals(beast.getIsMutant());
    List<String> mutationTraits =
        beast != null && beast.getMutationTraits() != null ? beast.getMutationTraits() : List.of();
    int powerScore = tier * 10;

    return PenCellVO.builder()
        .cellId(cell.getCellId())
        .cellLevel(cell.getCellLevel())
        .beastId(beast != null ? beast.getId() : null)
        .beastName(beastName)
        .tier(tier)
        .quality(qualityChinese)
        .qualityOrdinal(qualityOrdinal)
        .isMutant(isMutant)
        .mutationTraits(mutationTraits)
        .isIncubating(isIncubating(cell))
        .hatchTime(pen != null ? pen.hatchTime() : null)
        .matureTime(pen != null ? pen.matureTime() : null)
        .productionIntervalHours(pen != null ? 4.0 : 4.0)
        .productionStored(cell.getTotalProductionQuantity())
        .powerScore(powerScore)
        .birthTime(beast != null ? beast.getBirthTime() : null)
        .build();
  }

  void buildPenCellDetail(CellDetailVO.CellDetailVOBuilder builder, FudiCell cell) {
    Beast beast = findBeastByCell(cell);

    if (beast == null) {
      builder.name("空兽栏");
    } else {
      String qualityChinese = beast.getQuality().getChineseName();
      List<String> traits =
          beast.getMutationTraits() != null ? beast.getMutationTraits() : List.of();
      builder
          .name(beast.getBeastName())
          .level(beast.getTier())
          .quality(qualityChinese)
          .mutationTraits(traits)
          .productionStored(cell.getTotalProductionQuantity())
          .isIncubating(isIncubating(cell));
    }
  }

  @Transactional
  void clearBeastCell(FudiCell cell) {
    cell.setConfig(new CellConfig.EmptyConfig());
    fudiCellRepository.save(cell);
  }

  boolean isIncubating(FudiCell cell) {
    if (!(cell.getConfig() instanceof CellConfig.PenConfig pen)) return false;
    LocalDateTime matureTime = pen.matureTime();
    if (matureTime == null) return false;
    return LocalDateTime.now().isBefore(matureTime);
  }
}
