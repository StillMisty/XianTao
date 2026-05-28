package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.shop.entity.ShopProduct;
import top.stillmisty.xiantao.infrastructure.mapper.ShopProductMapper;

@Repository
@RequiredArgsConstructor
public class ShopProductRepository {

  private final ShopProductMapper shopProductMapper;

  public ShopProduct save(ShopProduct product) {
    shopProductMapper.insertOrUpdateSelective(product);
    return product;
  }

  public Optional<ShopProduct> findById(Long id) {
    return Optional.ofNullable(shopProductMapper.selectOneById(id));
  }

  public List<ShopProduct> findByShopNpcId(Long shopNpcId) {
    return shopProductMapper.selectListByQuery(
        new QueryWrapper().eq(ShopProduct::getShopNpcId, shopNpcId));
  }

  public Optional<ShopProduct> findByShopNpcIdAndTemplateId(Long shopNpcId, Long templateId) {
    return Optional.ofNullable(
        shopProductMapper.selectOneByQuery(
            new QueryWrapper()
                .eq(ShopProduct::getShopNpcId, shopNpcId)
                .eq(ShopProduct::getTemplateId, templateId)));
  }

  public void deleteById(Long id) {
    shopProductMapper.deleteById(id);
  }

  public int deductStockIfAvailable(Long id, int qty) {
    return shopProductMapper.deductStockIfAvailable(id, qty);
  }
}
