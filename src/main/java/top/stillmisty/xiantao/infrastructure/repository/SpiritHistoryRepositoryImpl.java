package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.fudi.entity.SpiritHistory;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritHistoryRepository;
import top.stillmisty.xiantao.infrastructure.mapper.SpiritHistoryMapper;

import java.util.List;

import static top.stillmisty.xiantao.domain.fudi.entity.table.SpiritHistoryTableDef.SPIRIT_HISTORY;

@Repository
@RequiredArgsConstructor
public class SpiritHistoryRepositoryImpl implements SpiritHistoryRepository {

    private final SpiritHistoryMapper spiritHistoryMapper;

    @Override
    public SpiritHistory save(SpiritHistory history) {
        spiritHistoryMapper.insertOrUpdateSelective(history);
        return history;
    }

    @Override
    public List<SpiritHistory> findByFudiIdOrderByCreateTimeDesc(Long fudiId, int limit) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(SPIRIT_HISTORY.FUDI_ID.eq(fudiId))
                .orderBy(SPIRIT_HISTORY.CREATE_TIME.desc())
                .limit(limit);
        return spiritHistoryMapper.selectListByQuery(queryWrapper);
    }
}
