package top.stillmisty.xiantao.domain.item.repository;

import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 装备仓储接口
 */
public interface EquipmentRepository {

    /**
     * 保存装备
     */
    Equipment save(Equipment equipment);

    /**
     * 批量保存装备
     */
    List<Equipment> saveAll(List<Equipment> equipments);

    /**
     * 根据ID查找装备
     */
    Optional<Equipment> findById(UUID id);

    /**
     * 根据用户ID查找所有装备
     */
    List<Equipment> findByUserId(UUID userId);

    /**
     * 根据用户ID查找已穿戴的装备
     */
    List<Equipment> findEquippedByUserId(UUID userId);

    /**
     * 根据用户ID和部位查找已穿戴的装备
     */
    Optional<Equipment> findEquippedByUserIdAndSlot(UUID userId, EquipmentSlot slot);

    /**
     * 根据ID列表批量查找装备
     */
    List<Equipment> findByIds(List<UUID> ids);

    /**
     * 删除装备
     */
    void deleteById(UUID id);

    /**
     * 批量删除装备
     */
    void deleteByIds(List<UUID> ids);
}
