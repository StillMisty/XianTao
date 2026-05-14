package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.stillmisty.xiantao.domain.shop.entity.ShopProduct;

@Mapper
public interface ShopProductMapper extends BaseMapper<ShopProduct> {}
