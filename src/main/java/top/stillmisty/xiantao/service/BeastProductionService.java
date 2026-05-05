package top.stillmisty.xiantao.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.fudi.entity.CellConfig;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.fudi.vo.CollectVO;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;

/** 灵兽产出收集、生产更新 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeastProductionService {

  private final FudiCellRepository fudiCellRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemService stackableItemService;
  private final FudiHelper fudiHelper;
  private final BeastDisplayHelper beastDisplayHelper;

  CollectVO collectBeastProduce(Fudi fudi, FudiCell cell, Integer cellId) {
    if (beastDisplayHelper.isIncubating(cell)) {
      throw new IllegalStateException("灵兽尚在孵化中");
    }

    updateBeastProduction(cell, fudi);

    List<CellConfig.ProductionItem> productionStored = getProductionStoredList(cell);

    if (productionStored.isEmpty()) {
      throw new IllegalStateException("暂无产出可收取");
    }

    String beastName = "灵兽";
    Beast beast = beastDisplayHelper.findBeastByCell(cell);
    if (beast != null) {
      beastName = beast.getBeastName();
    }

    int totalItems = 0;
    for (CellConfig.ProductionItem item : productionStored) {
      if (item.quantity() > 0) {
        stackableItemService.addStackableItem(
            fudi.getUserId(), item.templateId(), ItemType.HERB, item.name(), item.quantity());
        totalItems += item.quantity();
        log.info(
            "用户 {} 收取地块 {} 的灵兽产出: {} x{}", fudi.getUserId(), cellId, item.name(), item.quantity());
      }
    }

    cell.clearProductionStored();
    fudiCellRepository.save(cell);

    log.info("用户 {} 收取地块 {} 的灵兽产出 {} 件", fudi.getUserId(), cellId, totalItems);

    return new CollectVO(cellId, "pen", null, beastName, totalItems, totalItems);
  }

  void updateBeastProduction(FudiCell cell, Fudi fudi) {
    if (!(cell.getConfig() instanceof CellConfig.PenConfig pen)) return;

    LocalDateTime matureTime = pen.matureTime();
    if (matureTime == null) return;

    LocalDateTime now = LocalDateTime.now();
    if (now.isBefore(matureTime)) return;

    Beast beast = beastDisplayHelper.findBeastByCell(cell);
    if (beast == null) return;

    if (Boolean.TRUE.equals(beast.getIsDeployed())) return;

    LocalDateTime lastProduction = pen.lastProductionTime();
    if (lastProduction == null) lastProduction = matureTime;

    double intervalHours = getProductionIntervalHours(pen.templateId());
    long intervalSeconds = (long) (intervalHours * 3600);
    if (intervalSeconds <= 0) intervalSeconds = 14400;

    long elapsed = java.time.Duration.between(lastProduction, now).getSeconds();
    int cycles = (int) (elapsed / intervalSeconds);
    if (cycles <= 0) return;

    int tier = beast.getTier();
    double outputMultiplier = beast.getQualityMultiplier();
    int perCycle =
        (int)
            Math.round(
                tier
                    * outputMultiplier
                    * (1 + ThreadLocalRandom.current().nextInt(tier + 1))
                    / 2.0);
    int produced = Math.max(1, perCycle * cycles);

    List<String> mutationTraits = beast.getMutationTraits();
    if (mutationTraits != null && mutationTraits.contains("high_yield")) {
      produced = (int) (produced * 1.3);
    }

    List<ItemProperties.ProductionItem> productionItems = getProductionItems(cell);
    if (productionItems.isEmpty()) {
      int currentStored = pen.totalProductionQuantity();
      int maxStorage = tier * 20;
      int newStored = Math.min(maxStorage, currentStored + produced);
      int added = newStored - currentStored;
      if (added > 0) {
        pen.addProductionItem(1L, "灵草", added);
      }
    } else {
      int maxStorage = tier * 20;
      int currentTotal = pen.totalProductionQuantity();
      int availableSpace = maxStorage - currentTotal;
      if (availableSpace <= 0) {
        cell.setConfig(pen.withLastProductionTime(now));
        fudiCellRepository.save(cell);
        return;
      }
      int toProduce = Math.min(produced, availableSpace);
      for (int i = 0; i < toProduce; i++) {
        var selectedItem = selectRandomProductionItem(productionItems);
        if (selectedItem != null) {
          pen.addProductionItem(selectedItem.templateId(), selectedItem.name(), 1);
        }
      }
      if (mutationTraits != null && mutationTraits.contains("rare_produce")) {
        if (ThreadLocalRandom.current().nextInt(100) < 5) {
          var higherItem = selectHigherTierItem(productionItems);
          if (higherItem != null) {
            pen.addProductionItem(higherItem.templateId(), higherItem.name(), 1);
          }
        }
      }
    }

    cell.setConfig(pen.withLastProductionTime(now));
    fudiCellRepository.save(cell);
  }

  List<ItemProperties.ProductionItem> getProductionItems(FudiCell cell) {
    if (!(cell.getConfig() instanceof CellConfig.PenConfig pen) || pen.templateId() == null) {
      return List.of();
    }
    ItemTemplate template =
        itemTemplateRepository.findById(pen.templateId().longValue()).orElse(null);
    if (template == null) {
      return List.of();
    }
    var props = template.typedProperties();
    if (props instanceof ItemProperties.BeastEgg egg) {
      return egg.productionItems();
    }
    return List.of();
  }

  ItemProperties.ProductionItem selectRandomProductionItem(
      List<ItemProperties.ProductionItem> productionItems) {
    if (productionItems.isEmpty()) {
      return null;
    }
    int totalWeight = 0;
    for (var item : productionItems) {
      totalWeight += item.weight();
    }
    if (totalWeight <= 0) {
      return productionItems.getFirst();
    }
    int random = ThreadLocalRandom.current().nextInt(totalWeight);
    int current = 0;
    for (var item : productionItems) {
      current += item.weight();
      if (random < current) {
        return item;
      }
    }
    return productionItems.getFirst();
  }

  ItemProperties.ProductionItem selectHigherTierItem(
      List<ItemProperties.ProductionItem> productionItems) {
    if (productionItems.isEmpty()) return null;
    return productionItems.stream()
        .min(Comparator.comparingInt(ItemProperties.ProductionItem::weight))
        .orElse(null);
  }

  List<CellConfig.ProductionItem> getProductionStoredList(FudiCell cell) {
    if (cell.getConfig() instanceof CellConfig.PenConfig pen) {
      return pen.productionStored();
    }
    return List.of();
  }

  double getProductionIntervalHours(Integer templateId) {
    if (templateId == null) return 4.0;
    ItemTemplate template = itemTemplateRepository.findById(templateId.longValue()).orElse(null);
    if (template == null) return 4.0;
    double baseGrowthHours = template.getGrowTime() != null ? template.getGrowTime() : 72;
    int tier = fudiHelper.getCropTier((int) baseGrowthHours);
    double levelSpeed = fudiHelper.getLevelSpeedMultiplier(1, tier);
    return 4.0 / levelSpeed;
  }
}
