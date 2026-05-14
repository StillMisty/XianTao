package top.stillmisty.xiantao.domain.shop.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.shop.entity.ShopSpecialOrder;
import top.stillmisty.xiantao.domain.shop.enums.SpecialOrderStatus;

public interface ShopSpecialOrderRepository {

  ShopSpecialOrder save(ShopSpecialOrder order);

  Optional<ShopSpecialOrder> findById(Long id);

  List<ShopSpecialOrder> findByPlayerId(Long playerId);

  List<ShopSpecialOrder> findByPlayerIdAndStatus(Long playerId, SpecialOrderStatus status);

  void deleteById(Long id);
}
