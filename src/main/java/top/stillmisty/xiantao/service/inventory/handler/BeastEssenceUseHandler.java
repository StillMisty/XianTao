package top.stillmisty.xiantao.service.inventory.handler;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.fudi.entity.CellConfig;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiCell;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.infrastructure.repository.BeastRepository;
import top.stillmisty.xiantao.infrastructure.repository.FudiCellRepository;
import top.stillmisty.xiantao.infrastructure.repository.FudiRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.beast.BeastBreedingService;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

@Component
@RequiredArgsConstructor
public class BeastEssenceUseHandler implements ItemUseHandler {

  private final FudiRepository fudiRepository;
  private final FudiCellRepository fudiCellRepository;
  private final BeastRepository beastRepository;
  private final StackableItemService stackableItemService;

  @Override
  public ItemType getItemType() {
    return ItemType.BEAST_ESSENCE;
  }

  @Override
  public boolean consumesInternally() {
    return true;
  }

  @Override
  public String use(Long userId, StackableItem item, @Nullable ItemTemplate template, String args) {
    if (args == null || args.isBlank()) {
      throw new BusinessException(ErrorCode.PARAM_INVALID, "请指定数量和兽栏地块编号，如：使用 灵兽精华 3 5");
    }

    String[] parts = args.trim().split("\\s+", -1);
    if (parts.length < 2) {
      throw new BusinessException(ErrorCode.PARAM_INVALID, "请同时指定数量和兽栏地块编号，如：使用 灵兽精华 3 5");
    }

    int quantity;
    int cellId;
    try {
      quantity = Integer.parseInt(parts[0]);
      cellId = Integer.parseInt(parts[1]);
    } catch (NumberFormatException e) {
      throw new BusinessException(ErrorCode.PARAM_INVALID, "数量和地块编号必须为数字");
    }

    if (quantity <= 0) {
      throw new BusinessException(ErrorCode.PARAM_INVALID, "数量必须大于0");
    }
    if (quantity > item.getQuantity()) {
      throw new BusinessException(
          ErrorCode.ITEM_QUANTITY_INSUFFICIENT, quantity, item.getQuantity());
    }

    Fudi fudi =
        fudiRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FUDI_NOT_FOUND));

    FudiCell cell =
        fudiCellRepository
            .findByFudiIdAndCellId(fudi.getId(), cellId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CELL_NOT_FOUND, cellId));

    if (cell.getCellType() != CellType.PEN) {
      throw new BusinessException(ErrorCode.CELL_NOT_PEN, cellId);
    }

    if (!(cell.getConfig() instanceof CellConfig.PenConfig pen) || pen.beastId() == null) {
      throw new BusinessException(ErrorCode.BEAST_NOT_FOUND);
    }

    Beast beast =
        beastRepository
            .findById(pen.beastId())
            .orElseThrow(() -> new BusinessException(ErrorCode.BEAST_NOT_FOUND));

    if (beast.getLevel() >= beast.getLevelCap()) {
      throw new BusinessException(ErrorCode.CELL_MAX_LEVEL);
    }

    stackableItemService.reduceStackableItem(userId, item.getId(), quantity);

    int totalExp = quantity * BeastBreedingService.ESSENCE_EXP_PER_UNIT;
    long consumed = beast.addExp(totalExp);
    beastRepository.save(beast);

    return "喂养成功！消耗 %d 份灵兽精华，%s 获得 %d 修为。".formatted(quantity, beast.getBeastName(), consumed);
  }
}
