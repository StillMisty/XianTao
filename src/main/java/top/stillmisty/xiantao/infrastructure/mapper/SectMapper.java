package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.stillmisty.xiantao.domain.sect.entity.Sect;

@Mapper
public interface SectMapper extends BaseMapper<Sect> {}
