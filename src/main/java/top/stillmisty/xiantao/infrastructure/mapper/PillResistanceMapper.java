package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.pill.entity.PillResistance;

@Mapper
public interface PillResistanceMapper extends BaseMapper<PillResistance> {

  @Select(
      "SELECT * FROM xt_pill_resistance WHERE user_id = #{userId} AND template_id = #{templateId}")
  Optional<PillResistance> selectByUserIdAndTemplateId(
      @Param("userId") Long userId, @Param("templateId") Long templateId);

  @Update(
      "INSERT INTO xt_pill_resistance (user_id, template_id, count, updated_at) "
          + "VALUES (#{userId}, #{templateId}, 1, NOW()) "
          + "ON CONFLICT (user_id, template_id) DO UPDATE SET count = xt_pill_resistance.count + 1, updated_at = NOW()")
  int upsertIncrementCount(@Param("userId") Long userId, @Param("templateId") Long templateId);
}
