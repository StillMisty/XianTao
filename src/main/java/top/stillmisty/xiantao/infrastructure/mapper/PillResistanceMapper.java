package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.stillmisty.xiantao.domain.pill.entity.PillResistance;

import java.util.Optional;

@Mapper
public interface PillResistanceMapper extends BaseMapper<PillResistance> {

    @Select("SELECT * FROM xt_pill_resistance WHERE user_id = #{userId} AND template_id = #{templateId}")
    Optional<PillResistance> selectByUserIdAndTemplateId(@Param("userId") Long userId, @Param("templateId") Long templateId);
}
