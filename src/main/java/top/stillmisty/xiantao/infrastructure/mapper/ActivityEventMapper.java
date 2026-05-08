package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;

@Mapper
public interface ActivityEventMapper extends BaseMapper<ActivityEvent> {}
