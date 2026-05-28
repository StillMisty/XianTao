package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.sect.entity.SectShopItem;
import top.stillmisty.xiantao.infrastructure.mapper.SectShopItemMapper;

@Repository
@RequiredArgsConstructor
public class SectShopItemRepository {

  private final SectShopItemMapper sectShopItemMapper;

  public SectShopItem save(SectShopItem item) {
    sectShopItemMapper.insertOrUpdateSelective(item);
    return item;
  }

  public Optional<SectShopItem> findById(Long id) {
    return Optional.ofNullable(sectShopItemMapper.selectOneById(id));
  }

  public List<SectShopItem> findBySectId(Long sectId) {
    QueryWrapper query = new QueryWrapper().eq(SectShopItem::getSectId, sectId);
    return sectShopItemMapper.selectListByQuery(query);
  }

  public void deleteById(Long id) {
    sectShopItemMapper.deleteById(id);
  }

  public void deleteBySectId(Long sectId) {
    QueryWrapper query = new QueryWrapper().eq(SectShopItem::getSectId, sectId);
    sectShopItemMapper.deleteByQuery(query);
  }
}
