package top.stillmisty.xiantao.domain.item.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;

/** 装备仓储接口 */
public interface EquipmentRepository {

  /** 保存装备 */
  Equipment save(Equipment equipment);

  /** 根据ID查找装备 */
  Optional<Equipment> findById(Long id);

  /** 根据用户ID查找所有装备 */
  List<Equipment> findByUserId(Long userId);

  /** 根据用户ID查找未穿戴的装备 */
  List<Equipment> findUnequippedByUserId(Long userId);

  /** 根据用户ID查找已穿戴的装备 */
  List<Equipment> findEquippedByUserId(Long userId);

  /** 根据用户ID和部位查找已穿戴的装备 */
  Optional<Equipment> findEquippedByUserIdAndSlot(Long userId, EquipmentSlot slot);

  /** 删除装备 */
  void deleteById(Long id);
}
