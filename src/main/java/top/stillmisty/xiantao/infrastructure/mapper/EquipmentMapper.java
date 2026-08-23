package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;

@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {

  @Select(
      "SELECT * FROM equipment WHERE user_id = #{userId} AND slot = #{slot} AND equipped = true FOR UPDATE")
  Equipment selectEquippedByUserIdAndSlotForUpdate(
      @Param("userId") Long userId, @Param("slot") EquipmentSlot slot);

  /** 条件删除未穿戴的装备，返回受影响行数（0=装备已不存在或已被并发处理） */
  @Delete("DELETE FROM equipment WHERE id = #{id} AND user_id = #{userId} AND equipped = false")
  int deleteUnequippedById(@Param("id") Long id, @Param("userId") Long userId);
}
