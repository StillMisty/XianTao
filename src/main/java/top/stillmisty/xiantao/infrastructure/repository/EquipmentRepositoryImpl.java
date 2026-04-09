package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.item.entity.Equipment;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.repository.EquipmentRepository;
import top.stillmisty.xiantao.infrastructure.mapper.EquipmentMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EquipmentRepositoryImpl implements EquipmentRepository {

    private final EquipmentMapper equipmentMapper;

    @Override
    public Equipment save(Equipment equipment) {
        equipmentMapper.insertOrUpdate(equipment);
        return equipment;
    }

    @Override
    public List<Equipment> saveAll(List<Equipment> equipments) {
        equipments.forEach(equipmentMapper::insertOrUpdate);
        return equipments;
    }

    @Override
    public Optional<Equipment> findById(UUID id) {
        return Optional.ofNullable(equipmentMapper.selectOneById(id));
    }

    @Override
    public List<Equipment> findByUserId(UUID userId) {
        QueryWrapper query = new QueryWrapper()
                .eq(Equipment::getUserId, userId);
        return equipmentMapper.selectListByQuery(query);
    }

    @Override
    public List<Equipment> findEquippedByUserId(UUID userId) {
        QueryWrapper query = new QueryWrapper()
                .eq(Equipment::getUserId, userId)
                .eq(Equipment::getEquipped, true);
        return equipmentMapper.selectListByQuery(query);
    }

    @Override
    public Optional<Equipment> findEquippedByUserIdAndSlot(UUID userId, EquipmentSlot slot) {
        QueryWrapper query = new QueryWrapper()
                .eq(Equipment::getUserId, userId)
                .eq(Equipment::getSlot, slot)
                .eq(Equipment::getEquipped, true);
        return Optional.ofNullable(equipmentMapper.selectOneByQuery(query));
    }

    @Override
    public List<Equipment> findByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        QueryWrapper query = new QueryWrapper()
                .in(Equipment::getId, ids);
        return equipmentMapper.selectListByQuery(query);
    }

    @Override
    public void deleteById(UUID id) {
        equipmentMapper.deleteById(id);
    }

    @Override
    public void deleteByIds(List<UUID> ids) {
        if (ids != null && !ids.isEmpty()) {
            ids.forEach(equipmentMapper::deleteById);
        }
    }
}
