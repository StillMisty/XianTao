package top.stillmisty.xiantao.domain.item.repository;

import top.stillmisty.xiantao.domain.item.entity.EquipmentTemplate;

import java.util.List;
import java.util.Optional;

/**
 * 装备模板仓储接口
 */
public interface EquipmentTemplateRepository {

    Optional<EquipmentTemplate> findById(Long id);

    Optional<EquipmentTemplate> findByTemplateId(Long templateId);

    List<EquipmentTemplate> findAll();
}
