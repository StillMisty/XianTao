package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import top.stillmisty.xiantao.domain.fudi.entity.FudiEventTemplate;

@Mapper
public interface FudiEventTemplateMapper extends BaseMapper<FudiEventTemplate> {

  @Select("SELECT * FROM fudi_event_template WHERE enabled = TRUE ORDER BY selection_weight DESC")
  List<FudiEventTemplate> selectEnabled();
}
