package top.stillmisty.xiantao.domain.item.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.item.entity.EquipmentTemplate;

/** 装备模板仓储接口 */
public interface EquipmentTemplateRepository {

  Optional<EquipmentTemplate> findById(Long id);

  Optional<EquipmentTemplate> findByName(String name);

  List<EquipmentTemplate> findByIds(List<Long> ids);

  List<EquipmentTemplate> findAll();
}
