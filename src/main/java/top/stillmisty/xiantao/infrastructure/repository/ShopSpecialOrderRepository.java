package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.shop.entity.table.ShopSpecialOrderTableDef.SHOP_SPECIAL_ORDER;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.shop.entity.ShopSpecialOrder;
import top.stillmisty.xiantao.domain.shop.enums.SpecialOrderStatus;
import top.stillmisty.xiantao.infrastructure.mapper.ShopSpecialOrderMapper;

@Repository
@RequiredArgsConstructor
public class ShopSpecialOrderRepository {

  private final ShopSpecialOrderMapper shopSpecialOrderMapper;

  public ShopSpecialOrder save(ShopSpecialOrder order) {
    shopSpecialOrderMapper.insertOrUpdateSelective(order);
    return order;
  }

  public Optional<ShopSpecialOrder> findById(Long id) {
    return Optional.ofNullable(shopSpecialOrderMapper.selectOneById(id));
  }

  public List<ShopSpecialOrder> findByPlayerId(Long playerId) {
    return shopSpecialOrderMapper.selectListByQuery(
        QueryWrapper.create().where(SHOP_SPECIAL_ORDER.PLAYER_ID.eq(playerId)));
  }

  public List<ShopSpecialOrder> findByPlayerIdAndStatus(Long playerId, SpecialOrderStatus status) {
    return shopSpecialOrderMapper.selectListByQuery(
        QueryWrapper.create()
            .where(SHOP_SPECIAL_ORDER.PLAYER_ID.eq(playerId))
            .and(SHOP_SPECIAL_ORDER.STATUS.eq(status)));
  }

  public void deleteById(Long id) {
    shopSpecialOrderMapper.deleteById(id);
  }
}
