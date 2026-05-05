package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;

@Mapper
public interface StackableItemMapper extends BaseMapper<StackableItem> {}
