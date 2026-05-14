package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.shop.entity.ShopSpecialOrder;
import top.stillmisty.xiantao.domain.shop.enums.SpecialOrderStatus;
import top.stillmisty.xiantao.domain.shop.repository.ShopSpecialOrderRepository;
import top.stillmisty.xiantao.infrastructure.mapper.ShopSpecialOrderMapper;

@Repository
@RequiredArgsConstructor
public class ShopSpecialOrderRepositoryImpl implements ShopSpecialOrderRepository {

  private final ShopSpecialOrderMapper shopSpecialOrderMapper;

  @Override
  public ShopSpecialOrder save(ShopSpecialOrder order) {
    shopSpecialOrderMapper.insertOrUpdateSelective(order);
    return order;
  }

  @Override
  public Optional<ShopSpecialOrder> findById(Long id) {
    return Optional.ofNullable(shopSpecialOrderMapper.selectOneById(id));
  }

  @Override
  public List<ShopSpecialOrder> findByPlayerId(Long playerId) {
    return shopSpecialOrderMapper.selectListByQuery(
        new QueryWrapper().eq(ShopSpecialOrder::getPlayerId, playerId));
  }

  @Override
  public List<ShopSpecialOrder> findByPlayerIdAndStatus(Long playerId, SpecialOrderStatus status) {
    return shopSpecialOrderMapper.selectListByQuery(
        new QueryWrapper()
            .eq(ShopSpecialOrder::getPlayerId, playerId)
            .eq(ShopSpecialOrder::getStatus, status));
  }

  @Override
  public void deleteById(Long id) {
    shopSpecialOrderMapper.deleteById(id);
  }
}
