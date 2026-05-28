package top.stillmisty.xiantao.service.beast;

import static top.stillmisty.xiantao.service.ErrorCode.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.enums.MutationEffectType;
import top.stillmisty.xiantao.domain.fudi.entity.CellConfig;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.vo.CollectVO;
import top.stillmisty.xiantao.domain.item.entity.ItemProperties;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.ProductionItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.infrastructure.repository.BeastTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.FudiCellRepository;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.fudi.FudiHelper;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

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
  private final MutationEffectResolver effectResolver;
  private final BeastTemplateRepository beastTemplateRepository;

  @Transactional
  public CollectVO collectBeastProduce(Fudi fudi, FudiCell cell, Integer cellId) {
    if (beastDisplayHelper.isIncubating(cell)) {
      throw new BusinessException(BEAST_HATCHING);
    }

    updateBeastProduction(cell, fudi);

    List<CellConfig.ProductionItem> productionStored = getProductionStoredList(cell);

    if (productionStored.isEmpty()) {
      throw new BusinessException(BEAST_PRODUCE_NOTHING);
    }

    String beastName = "灵兽";
    Beast beast = beastDisplayHelper.findBeastByCell(cell);
    if (beast != null) {
      beastName = beast.getBeastName();
    }

    int totalItems = 0;
    for (CellConfig.ProductionItem item : productionStored) {
      if (item.quantity() > 0) {
        ItemType itemType =
            itemTemplateRepository
                .findById(item.templateId())
                .map(ItemTemplate::getType)
                .orElse(ItemType.HERB);
        stackableItemService.addStackableItem(
            fudi.getUserId(), item.templateId(), itemType, item.name(), item.quantity());
        totalItems += item.quantity();
        log.debug(
            "玩家 {} 收取地块 {} 的灵兽产出: {} x{}", fudi.getUserId(), cellId, item.name(), item.quantity());
      }
    }

    cell.clearProductionStored();
    fudiCellRepository.save(cell);

    log.debug("玩家 {} 收取地块 {} 的灵兽产出 {} 件", fudi.getUserId(), cellId, totalItems);

    return new CollectVO(cellId, "PEN", null, beastName, totalItems, totalItems);
  }

  public void updateBeastProduction(FudiCell cell, Fudi fudi) {
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

    double intervalHours = getProductionIntervalHours(beast.getTier(), cell.getCellLevel());
    double intervalReduce =
        effectResolver.sumEffectValue(beast, MutationEffectType.OUTPUT_INTERVAL_REDUCE);
    intervalHours *= (1 - intervalReduce / 100);
    long intervalSeconds = (long) (intervalHours * 3600);
    if (intervalSeconds <= 0) intervalSeconds = 14400;

    long elapsed = java.time.Duration.between(lastProduction, now).getSeconds();
    if (elapsed < 0) {
      log.warn("检测到灵兽产出台账时间异常: lastProduction={}, now={}", lastProduction, now);
      return;
    }
    int cycles = (int) (elapsed / intervalSeconds);
    if (cycles <= 0) return;

    int produced = calculateProductionAmount(beast, cycles);
    applyProductionToCell(cell, pen, beast, produced, now);
  }

  private int calculateProductionAmount(Beast beast, int cycles) {
    int tier = beast.getTier();
    double outputMultiplier = beast.getQuality().getOutputMultiplier();
    int perCycle =
        (int)
            Math.round(
                tier
                    * outputMultiplier
                    * (1 + ThreadLocalRandom.current().nextInt(tier + 1))
                    / 2.0);
    int produced = Math.max(1, perCycle * cycles);

    double outputBonus = effectResolver.sumEffectValue(beast, MutationEffectType.OUTPUT_PERCENT);
    produced = (int) (produced * (1 + outputBonus / 100));
    return produced;
  }

  private void applyProductionToCell(
      FudiCell cell, CellConfig.PenConfig pen, Beast beast, int produced, LocalDateTime now) {
    int tier = beast.getTier();

    List<ProductionItem> productionItems = getProductionItems(cell);
    if (productionItems.isEmpty()) {
      log.warn("灵兽 {} (地块 {}) 兽卵模板未定义产出物品，跳过产出", beast.getBeastName(), cell.getCellId());
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
          pen.addProductionItem(selectedItem.templateId(), resolveName(selectedItem), 1);
        }
      }
      double rareChance = effectResolver.sumEffectValue(beast, MutationEffectType.RARE_ITEM_CHANCE);
      if (rareChance > 0) {
        if (ThreadLocalRandom.current().nextInt(100) < rareChance) {
          var higherItem = selectHigherTierItem(productionItems);
          if (higherItem != null) {
            pen.addProductionItem(higherItem.templateId(), resolveName(higherItem), 1);
          }
        }
      }
    }

    cell.setConfig(pen.withLastProductionTime(now));
    fudiCellRepository.save(cell);
  }

  List<ProductionItem> getProductionItems(FudiCell cell) {
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
      return beastTemplateRepository
          .findById(egg.beastTemplateId())
          .map(
              bt ->
                  bt.getProductionItems() != null
                      ? bt.getProductionItems()
                      : List.<ProductionItem>of())
          .orElse(List.of());
    }
    return List.of();
  }

  ProductionItem selectRandomProductionItem(List<ProductionItem> productionItems) {
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

  ProductionItem selectHigherTierItem(List<ProductionItem> productionItems) {
    if (productionItems.isEmpty()) return null;
    return productionItems.stream()
        .min(Comparator.comparingInt(ProductionItem::weight))
        .orElse(null);
  }

  private String resolveName(ProductionItem item) {
    return itemTemplateRepository
        .findById(item.templateId())
        .map(ItemTemplate::getName)
        .orElse("未知灵物");
  }

  public List<CellConfig.ProductionItem> getProductionStoredList(FudiCell cell) {
    if (cell.getConfig() instanceof CellConfig.PenConfig pen) {
      return pen.productionStored();
    }
    return List.of();
  }

  double getProductionIntervalHours(int beastTier, int cellLevel) {
    double levelSpeed = fudiHelper.getLevelSpeedMultiplier(cellLevel, beastTier);
    return 4.0 / levelSpeed;
  }
}
