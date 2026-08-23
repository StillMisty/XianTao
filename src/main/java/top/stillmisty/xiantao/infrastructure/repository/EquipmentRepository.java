package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.item.entity.table.EquipmentTableDef.EQUIPMENT;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.infrastructure.mapper.EquipmentMapper;

@Repository
@RequiredArgsConstructor
public class EquipmentRepository {

  private final EquipmentMapper equipmentMapper;

  public Equipment save(Equipment equipment) {
    equipmentMapper.insertOrUpdateSelective(equipment);
    return equipment;
  }

  public Optional<Equipment> findById(Long id) {
    return Optional.ofNullable(equipmentMapper.selectOneById(id));
  }

  /** 条件删除未穿戴的装备，返回受影响行数（0=装备已不存在或已被并发处理） */
  public int deleteUnequippedById(Long id, Long userId) {
    return equipmentMapper.deleteUnequippedById(id, userId);
  }

  public List<Equipment> findByUserId(Long userId) {
    QueryWrapper query = QueryWrapper.create().where(EQUIPMENT.USER_ID.eq(userId));
    return equipmentMapper.selectListByQuery(query);
  }

  public List<Equipment> findUnequippedByUserId(Long userId) {
    QueryWrapper query =
        QueryWrapper.create().where(EQUIPMENT.USER_ID.eq(userId)).and(EQUIPMENT.EQUIPPED.eq(false));
    return equipmentMapper.selectListByQuery(query);
  }

  public List<Equipment> findEquippedByUserId(Long userId) {
    QueryWrapper query =
        QueryWrapper.create().where(EQUIPMENT.USER_ID.eq(userId)).and(EQUIPMENT.EQUIPPED.eq(true));
    return equipmentMapper.selectListByQuery(query);
  }

  public Optional<Equipment> findEquippedByUserIdAndSlot(Long userId, EquipmentSlot slot) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(EQUIPMENT.USER_ID.eq(userId))
            .and(EQUIPMENT.SLOT.eq(slot))
            .and(EQUIPMENT.EQUIPPED.eq(true));
    return Optional.ofNullable(equipmentMapper.selectOneByQuery(query));
  }

  public Optional<Equipment> findEquippedByUserIdAndSlotForUpdate(Long userId, EquipmentSlot slot) {
    return Optional.ofNullable(
        equipmentMapper.selectEquippedByUserIdAndSlotForUpdate(userId, slot));
  }

  public void deleteById(Long id) {
    equipmentMapper.deleteById(id);
  }
}
