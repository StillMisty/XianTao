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
    public Optional<Equipment> findById(Long id) {
        return Optional.ofNullable(equipmentMapper.selectOneById(id));
    }

    @Override
    public List<Equipment> findByUserId(Long userId) {
        QueryWrapper query = new QueryWrapper()
                .eq(Equipment::getUserId, userId);
        return equipmentMapper.selectListByQuery(query);
    }

    @Override
    public List<Equipment> findUnequippedByUserId(Long userId) {
        QueryWrapper query = new QueryWrapper()
                .eq(Equipment::getUserId, userId)
                .eq(Equipment::getEquipped, false);
        return equipmentMapper.selectListByQuery(query);
    }

    @Override
    public List<Equipment> findEquippedByUserId(Long userId) {
        QueryWrapper query = new QueryWrapper()
                .eq(Equipment::getUserId, userId)
                .eq(Equipment::getEquipped, true);
        return equipmentMapper.selectListByQuery(query);
    }

    @Override
    public Optional<Equipment> findEquippedByUserIdAndSlot(Long userId, EquipmentSlot slot) {
        QueryWrapper query = new QueryWrapper()
                .eq(Equipment::getUserId, userId)
                .eq(Equipment::getSlot, slot)
                .eq(Equipment::getEquipped, true);
        return Optional.ofNullable(equipmentMapper.selectOneByQuery(query));
    }

    @Override
    public void deleteById(Long id) {
        equipmentMapper.deleteById(id);
    }
}
