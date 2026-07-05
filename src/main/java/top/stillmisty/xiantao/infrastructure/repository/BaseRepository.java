package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.BaseMapper;
import java.util.List;
import java.util.Optional;

/**
 * 通用基础仓储，提供标准 CRUD 方法。
 *
 * <p>子类只需通过构造函数注入对应的 Mapper，无需重复编写 findById/save/findAll。
 * 自定义查询方法直接在子类中添加。
 *
 * @param <T> 实体类型
 */
public abstract class BaseRepository<T> {

  protected final BaseMapper<T> mapper;

  protected BaseRepository(BaseMapper<T> mapper) {
    this.mapper = mapper;
  }

  public Optional<T> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  public T save(T entity) {
    mapper.insertOrUpdateSelective(entity);
    return entity;
  }

  public List<T> findAll() {
    return mapper.selectAll();
  }

  public boolean deleteById(Long id) {
    return mapper.deleteById(id) > 0;
  }
}
