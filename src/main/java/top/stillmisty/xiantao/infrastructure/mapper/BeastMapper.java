package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.stillmisty.xiantao.domain.beast.entity.Beast;

@Mapper
public interface BeastMapper extends BaseMapper<Beast> {
}
