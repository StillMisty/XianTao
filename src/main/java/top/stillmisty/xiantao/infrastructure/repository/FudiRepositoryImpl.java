package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.land.entity.Fudi;
import top.stillmisty.xiantao.domain.land.repository.FudiRepository;
import top.stillmisty.xiantao.infrastructure.mapper.FudiMapper;

import java.util.Optional;

import static top.stillmisty.xiantao.domain.land.entity.table.FudiTableDef.FUDI;

/**
 * 福地 Repository 实现
 */
@Repository
@RequiredArgsConstructor
public class FudiRepositoryImpl implements FudiRepository {

    private final FudiMapper fudiMapper;

    @Override
    public Optional<Fudi> findById(Long id) {
        Fudi fudi = fudiMapper.selectOneById(id);
        return Optional.ofNullable(fudi);
    }

    @Override
    public Optional<Fudi> findByUserId(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(FUDI.USER_ID.eq(userId));
        Fudi fudi = fudiMapper.selectOneByQuery(queryWrapper);
        return Optional.ofNullable(fudi);
    }

    @Override
    public Fudi save(Fudi fudi) {
        fudiMapper.insertOrUpdateSelective(fudi);
        return fudi;
    }

    @Override
    public boolean existsByUserId(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(FUDI.USER_ID.eq(userId));
        return fudiMapper.selectCountByQuery(queryWrapper) > 0;
    }

    @Override
    public void deleteById(Long id) {
        fudiMapper.deleteById(id);
    }
}
