package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.infrastructure.mapper.SpiritMapper;

import java.util.Optional;

import static top.stillmisty.xiantao.domain.fudi.entity.table.SpiritTableDef.SPIRIT;

@Repository
@RequiredArgsConstructor
public class SpiritRepositoryImpl implements SpiritRepository {

    private final SpiritMapper spiritMapper;

    @Override
    public Optional<Spirit> findById(Long id) {
        Spirit spirit = spiritMapper.selectOneById(id);
        return Optional.ofNullable(spirit);
    }

    @Override
    public Optional<Spirit> findByFudiId(Long fudiId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(SPIRIT.FUDI_ID.eq(fudiId));
        Spirit spirit = spiritMapper.selectOneByQuery(queryWrapper);
        return Optional.ofNullable(spirit);
    }

    @Override
    public Spirit save(Spirit spirit) {
        spiritMapper.insertOrUpdateSelective(spirit);
        return spirit;
    }

    @Override
    public void deleteById(Long id) {
        spiritMapper.deleteById(id);
    }
}
