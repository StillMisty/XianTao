package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;

@Mapper
public interface DungeonInstanceMapper extends BaseMapper<DungeonInstance> {}
