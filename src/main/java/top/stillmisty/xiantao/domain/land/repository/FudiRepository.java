package top.stillmisty.xiantao.domain.land.repository;

import top.stillmisty.xiantao.domain.land.entity.Fudi;

import java.util.Optional;

/**
 * 福地 Repository 接口
 */
public interface FudiRepository {

    /**
     * 根据ID查找福地
     */
    Optional<Fudi> findById(Long id);

    /**
     * 根据用户ID查找福地
     */
    Optional<Fudi> findByUserId(Long userId);

    /**
     * 保存或更新福地
     */
    Fudi save(Fudi fudi);

    /**
     * 检查用户是否已有福地
     */
    boolean existsByUserId(Long userId);

    /**
     * 删除福地
     */
    void deleteById(Long id);
}
