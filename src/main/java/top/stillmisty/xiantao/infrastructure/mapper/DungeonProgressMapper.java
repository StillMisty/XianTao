package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonProgress;

@Mapper
public interface DungeonProgressMapper extends BaseMapper<DungeonProgress> {}
