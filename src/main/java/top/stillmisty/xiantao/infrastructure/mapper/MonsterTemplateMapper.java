package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;

@Mapper
public interface MonsterTemplateMapper extends BaseMapper<MonsterTemplate> {
}
