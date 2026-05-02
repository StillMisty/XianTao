package top.stillmisty.xiantao.domain.pill.repository;

import top.stillmisty.xiantao.domain.pill.entity.PillResistance;

import java.util.Optional;

/**
 * 丹药抗性仓储接口
 */
public interface PillResistanceRepository {

    /**
     * 根据用户ID和丹药模板ID查找抗性记录
     */
    Optional<PillResistance> findByUserIdAndTemplateId(Long userId, Long templateId);

    /**
     * 增加服用次数（首次创建，已存在则+1）
     */
    int incrementCount(Long userId, Long templateId);
}
