package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;

@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {

  @Select(
      "SELECT * FROM xt_equipment WHERE user_id = #{userId} AND slot = #{slot} AND equipped = true FOR UPDATE")
  Equipment selectEquippedByUserIdAndSlotForUpdate(
      @Param("userId") Long userId, @Param("slot") EquipmentSlot slot);
}
