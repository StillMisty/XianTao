package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.beast.entity.Beast;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.infrastructure.mapper.BeastMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static top.stillmisty.xiantao.domain.beast.entity.table.BeastTableDef.BEAST;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BeastRepositoryImpl implements BeastRepository {

    private final BeastMapper mapper;

    @Override
    public Optional<Beast> findById(Long id) {
        return Optional.ofNullable(mapper.selectOneById(id));
    }

    @Override
    public List<Beast> findByUserId(Long userId) {
        return mapper.selectListByQuery(
                QueryWrapper.create()
                        .select()
                        .from(BEAST)
                        .where(BEAST.USER_ID.eq(userId))
        );
    }

    @Override
    public List<Beast> findByFudiId(Long fudiId) {
        return mapper.selectListByQuery(
                QueryWrapper.create()
                        .select()
                        .from(BEAST)
                        .where(BEAST.FUDI_ID.eq(fudiId))
        );
    }

    @Override
    public List<Beast> findDeployedByUserId(Long userId) {
        return mapper.selectListByQuery(
                QueryWrapper.create()
                        .select()
                        .from(BEAST)
                        .where(BEAST.USER_ID.eq(userId))
                        .and(BEAST.IS_DEPLOYED.eq(true))
                        .and(BEAST.HP_CURRENT.gt(0))
                        .and(BEAST.RECOVERY_UNTIL.lt(LocalDateTime.now()).or(BEAST.RECOVERY_UNTIL.isNull()))
        );
    }

    @Override
    public Beast save(Beast beast) {
        if (beast.getId() == null) {
            mapper.insert(beast);
        } else {
            mapper.update(beast);
        }
        return beast;
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public long countByUserId(Long userId) {
        return mapper.selectCountByQuery(
                QueryWrapper.create()
                        .select()
                        .from(BEAST)
                        .where(BEAST.USER_ID.eq(userId))
        );
    }
}
