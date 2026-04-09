package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.stillmisty.xiantao.domain.item.entity.Equipment;

/**
 * 装备 Mapper 接口
 */
@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {
}
