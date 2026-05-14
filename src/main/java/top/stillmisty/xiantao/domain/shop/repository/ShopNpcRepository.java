package top.stillmisty.xiantao.domain.shop.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;

public interface ShopNpcRepository {

  ShopNpc save(ShopNpc shopNpc);

  Optional<ShopNpc> findById(Long id);

  Optional<ShopNpc> findByMapNodeId(Long mapNodeId);

  List<ShopNpc> findAll();
}
