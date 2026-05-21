package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;

@Mapper
public interface MonsterTemplateMapper extends BaseMapper<MonsterTemplate> {

  @Select("SELECT * FROM xt_monster_template WHERE name = #{name}")
  MonsterTemplate selectByName(@Param("name") String name);
}
