package top.stillmisty.xiantao.domain.sect.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.sect.entity.SectShopItem;

public interface SectShopItemRepository {
  SectShopItem save(SectShopItem item);

  Optional<SectShopItem> findById(Long id);

  List<SectShopItem> findBySectId(Long sectId);

  void deleteById(Long id);

  void deleteBySectId(Long sectId);
}
