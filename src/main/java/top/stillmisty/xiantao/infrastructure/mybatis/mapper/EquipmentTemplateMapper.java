package top.stillmisty.xiantao.infrastructure.mybatis.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.stillmisty.xiantao.domain.item.entity.EquipmentTemplate;

@Mapper
public interface EquipmentTemplateMapper extends BaseMapper<EquipmentTemplate> {

  @Select("SELECT * FROM xt_equipment_template WHERE name = #{name}")
  EquipmentTemplate selectByName(@Param("name") String name);
}
