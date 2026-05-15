package top.stillmisty.xiantao.domain.pill.repository;

import java.util.Optional;
import top.stillmisty.xiantao.domain.pill.entity.PillResistance;

/** 丹药抗性仓储接口 */
public interface PillResistanceRepository {

  /** 根据用户ID、丹药模板ID和品质查找抗性记录 */
  Optional<PillResistance> findByUserIdAndTemplateIdAndQuality(
      Long userId, Long templateId, String quality);

  /** 增加服用次数（首次创建，已存在则+1） */
  int incrementCount(Long userId, Long templateId, String quality);
}
