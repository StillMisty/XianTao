package top.stillmisty.xiantao.domain.shop.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.shop.entity.ShopProduct;

public interface ShopProductRepository {

  ShopProduct save(ShopProduct product);

  Optional<ShopProduct> findById(Long id);

  List<ShopProduct> findByShopNpcId(Long shopNpcId);

  Optional<ShopProduct> findByShopNpcIdAndTemplateId(Long shopNpcId, Long templateId);

  void deleteById(Long id);
}
