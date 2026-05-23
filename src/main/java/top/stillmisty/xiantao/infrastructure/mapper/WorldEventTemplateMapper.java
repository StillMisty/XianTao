package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEventTemplate;

@Mapper
public interface WorldEventTemplateMapper extends BaseMapper<WorldEventTemplate> {

  @Select(
      "SELECT * FROM world_event_template WHERE category = #{category} ORDER BY selection_weight DESC")
  List<WorldEventTemplate> selectByCategory(@Param("category") String category);

  @Select(
      "SELECT * FROM world_event_template WHERE scope = #{scope} ORDER BY selection_weight DESC")
  List<WorldEventTemplate> selectByScope(@Param("scope") String scope);
}
