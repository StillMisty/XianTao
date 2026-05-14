package top.stillmisty.xiantao.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;
import top.stillmisty.xiantao.domain.shop.repository.ShopNpcRepository;
import top.stillmisty.xiantao.infrastructure.mapper.ShopNpcMapper;

@Repository
@RequiredArgsConstructor
public class ShopNpcRepositoryImpl implements ShopNpcRepository {

  private final ShopNpcMapper shopNpcMapper;

  @Override
  public ShopNpc save(ShopNpc shopNpc) {
    shopNpcMapper.insertOrUpdateSelective(shopNpc);
    return shopNpc;
  }

  @Override
  public Optional<ShopNpc> findById(Long id) {
    return Optional.ofNullable(shopNpcMapper.selectOneById(id));
  }

  @Override
  public Optional<ShopNpc> findByMapNodeId(Long mapNodeId) {
    return Optional.ofNullable(shopNpcMapper.selectByMapNodeId(mapNodeId));
  }

  @Override
  public List<ShopNpc> findAll() {
    return shopNpcMapper.selectAll();
  }
}
