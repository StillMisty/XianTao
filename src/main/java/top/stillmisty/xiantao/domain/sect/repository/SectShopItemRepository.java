package top.stillmisty.xiantao.domain.sect.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.sect.entity.SectShopItem;

/** 宗门商店商品仓储接口 */
public interface SectShopItemRepository {

  SectShopItem save(SectShopItem item);

  Optional<SectShopItem> findById(Long id);

  List<SectShopItem> findBySectId(Long sectId);

  void deleteById(Long id);

  void deleteBySectId(Long sectId);
}
