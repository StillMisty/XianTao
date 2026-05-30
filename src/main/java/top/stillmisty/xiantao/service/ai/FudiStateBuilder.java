package top.stillmisty.xiantao.service.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.fudi.entity.CellConfig;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.infrastructure.repository.BeastRepository;
import top.stillmisty.xiantao.infrastructure.repository.FudiCellRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.fudi.FarmService;

/**
 * 福地状态文本构建器 — 为 LLM prompt 生成福地地块详情
 *
 * <p>设计决策：从 SpiritChatService 提取，将福地状态文本构建与对话逻辑分离。 SpiritChatService 负责对话流程，此类负责将福地状态序列化为 LLM
 * 可读的文本。
 */
@Component
@RequiredArgsConstructor
public class FudiStateBuilder {

  private final FudiCellRepository fudiCellRepository;
  private final BeastRepository beastRepository;
  private final FarmService farmService;

  /** 构建福地地块详情文本（供 LLM prompt 使用） */
  public String buildCellDetailForLLM(Fudi fudi) {
    List<FudiCell> cells = fudiCellRepository.findByFudiId(fudi.getId());
    if (cells.isEmpty()) {
      return "福地尚未开辟任何地块。";
    }

    int totalCells = cells.size();
    List<FudiCell> emptyCells = new ArrayList<>();
    List<FudiCell> farmCells = new ArrayList<>();
    List<FudiCell> penCells = new ArrayList<>();

    for (FudiCell cell : cells) {
      switch (cell.getCellType()) {
        case EMPTY -> emptyCells.add(cell);
        case FARM -> farmCells.add(cell);
        case PEN -> penCells.add(cell);
      }
    }

    StringBuilder sb = new StringBuilder();
    sb.append("福地状态（共").append(totalCells).append("个地块）：\n");

    List<String> typeSummary = new ArrayList<>();
    if (!farmCells.isEmpty()) typeSummary.add("灵田×" + farmCells.size());
    if (!penCells.isEmpty()) typeSummary.add("兽栏×" + penCells.size());
    if (!typeSummary.isEmpty()) {
      sb.append("地块组成：").append(String.join("、", typeSummary)).append("\n");
    }

    if (emptyCells.isEmpty()) {
      sb.append("所有地块已使用。如需调整布局可先拆除部分地块。\n");
    } else {
      sb.append("可用空地块编号：");
      sb.append(emptyCells.stream().map(c -> String.valueOf(c.getCellId())).toList());
      sb.append("\n");
    }

    sb.append("【已占地块详情】\n");
    Map<Long, Beast> beastCache =
        beastRepository.findByFudiId(fudi.getId()).stream()
            .collect(Collectors.toMap(Beast::getId, b -> b));
    for (FudiCell cell : farmCells) {
      sb.append("- [").append(cell.getCellId()).append("] FARM");
      if (cell.getConfig() instanceof CellConfig.FarmConfig farm) {
        sb.append(" 种植:").append(farmService.getCropName(farm.cropId()));
        Double progress = farmService.calculateGrowthProgress(cell);
        if (progress != null) {
          if (progress >= 1.0) {
            sb.append(" 可收获✅");
          } else {
            sb.append(String.format(" (%.0f%%)", progress * 100));
          }
        }
      }
      sb.append("\n");
    }

    for (FudiCell cell : penCells) {
      sb.append("- [").append(cell.getCellId()).append("] PEN");
      if (cell.getConfig() instanceof CellConfig.PenConfig pen) {
        Beast beast = beastCache.get(pen.beastId());
        if (beast != null) {
          sb.append(" 饲养:").append(beast.getBeastName());
          if (beast.getGender() != null) {
            sb.append("(")
                .append(beast.getGender().getSymbol())
                .append(beast.getGender().getChineseName())
                .append(")");
          }
          if (beast.getBreedingCooldownUntil() != null
              && beast.getBreedingCooldownUntil().isAfter(TimeUtil.now())) {
            sb.append(" 🔥繁育冷却中");
          }
        }
      }
      sb.append("\n");
    }

    long matureFarmCount =
        farmCells.stream()
            .filter(
                c -> {
                  if (c.getConfig() instanceof CellConfig.FarmConfig) {
                    Double progress = farmService.calculateGrowthProgress(c);
                    return progress != null && progress >= 1.0;
                  }
                  return false;
                })
            .count();
    if (matureFarmCount > 0) {
      sb.append("有 ").append(matureFarmCount).append(" 块灵田可收获。\n");
    }

    return sb.toString();
  }
}
