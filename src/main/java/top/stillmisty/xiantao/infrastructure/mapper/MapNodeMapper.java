package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.stillmisty.xiantao.domain.map.entity.MapNode;

/**
 * 地图节点 Mapper
 */
@Mapper
public interface MapNodeMapper extends BaseMapper<MapNode> {
}
