package top.stillmisty.xiantao.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.fudi.entity.CellConfig;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.fudi.vo.CollectAllVO;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class FudiCollectService {

  private final FudiHelper fudiHelper;
  private final FudiCellRepository fudiCellRepository;
  private final FarmService farmService;
  private final BeastService beastService;
  private final StackableItemService stackableItemService;
  private final ItemTemplateRepository itemTemplateRepository;

  @Transactional
  public CollectAllVO collectAll(Long userId) {
    Fudi fudi =
        fudiHelper
            .findAndTouchFudi(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FUDI_NOT_FOUND));
    List<FudiCell> cells = fudiCellRepository.findByFudiId(fudi.getId());
    if (cells.isEmpty()) {
      return new CollectAllVO(0, 0, 0);
    }

    FarmCollectResult farmResult = collectAllFarmCells(fudi, cells);
    PenCollectResult penResult = collectAllPenCells(fudi, cells);

    return new CollectAllVO(
        farmResult.harvestCount,
        penResult.collectedCount,
        farmResult.totalItems + penResult.totalItems);
  }

  private record FarmCollectResult(int harvestCount, int totalItems) {}

  private record PenCollectResult(int collectedCount, int totalItems) {}

  private FarmCollectResult collectAllFarmCells(Fudi fudi, List<FudiCell> cells) {
    int harvestCount = 0;
    int totalItems = 0;
    List<FudiCell> toReset = new ArrayList<>();

    for (FudiCell cell : cells) {
      if (cell.getCellType() != CellType.FARM) continue;
      if (!(cell.getConfig() instanceof CellConfig.FarmConfig farm)) continue;

      farmService.updateGrowthProgress(cell);
      Double progress = farmService.calculateGrowthProgress(cell);
      if (progress == null || progress < 1.0) continue;

      boolean isPerennial = farm.harvestCount() < farmService.getMaxHarvest(farm.cropId());
      if (!isPerennial && progress > 1.0 && farmService.isWilted(cell)) {
        toReset.add(cell);
        continue;
      }

      int yield = farmService.calculateYield(farm.cropId(), fudi.getTribulationStage());
      farmService.grantHarvestItems(fudi.getUserId(), farm.cropId(), yield);
      int hCount = farm.harvestCount() + 1;
      int maxHarvest = farmService.getMaxHarvest(farm.cropId());

      if (isPerennial && hCount < maxHarvest) {
        cell.setConfig(farm.withHarvestCount(hCount));
        farmService.replantAfterHarvest(cell);
      } else {
        toReset.add(cell);
      }

      totalItems += yield;
      harvestCount++;
    }

    for (FudiCell cell : toReset) {
      cell.setCellType(CellType.EMPTY);
      cell.clearConfig();
      fudiCellRepository.save(cell);
    }

    return new FarmCollectResult(harvestCount, totalItems);
  }

  private PenCollectResult collectAllPenCells(Fudi fudi, List<FudiCell> cells) {
    int collectedCount = 0;
    int totalItems = 0;

    for (FudiCell cell : cells) {
      if (cell.getCellType() != CellType.PEN) continue;
      if (beastService.isIncubating(cell)) continue;

      beastService.updateBeastProduction(cell, fudi);

      List<CellConfig.ProductionItem> productionStored = beastService.getProductionStoredList(cell);
      if (productionStored.isEmpty()) continue;

      int cellTotalItems = 0;
      for (CellConfig.ProductionItem item : productionStored) {
        if (item.quantity() > 0) {
          ItemType itemType =
              itemTemplateRepository
                  .findById(item.templateId())
                  .map(ItemTemplate::getType)
                  .orElse(ItemType.HERB);
          stackableItemService.addStackableItem(
              fudi.getUserId(), item.templateId(), itemType, item.name(), item.quantity());
          cellTotalItems += item.quantity();
        }
      }

      cell.clearProductionStored();
      fudiCellRepository.save(cell);

      totalItems += cellTotalItems;
      collectedCount++;
      log.info("用户 {} 收取地块 {} 的灵兽产出 {} 件", fudi.getUserId(), cell.getCellId(), cellTotalItems);
    }

    return new PenCollectResult(collectedCount, totalItems);
  }
}
