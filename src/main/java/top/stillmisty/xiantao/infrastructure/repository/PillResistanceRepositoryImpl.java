package top.stillmisty.xiantao.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.pill.entity.PillResistance;
import top.stillmisty.xiantao.domain.pill.repository.PillResistanceRepository;
import top.stillmisty.xiantao.infrastructure.mapper.PillResistanceMapper;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PillResistanceRepositoryImpl implements PillResistanceRepository {

    private final PillResistanceMapper mapper;

    @Override
    public Optional<PillResistance> findByUserIdAndTemplateId(Long userId, Long templateId) {
        return mapper.selectByUserIdAndTemplateId(userId, templateId);
    }

    @Override
    public int incrementCount(Long userId, Long templateId) {
        var existing = mapper.selectByUserIdAndTemplateId(userId, templateId);
        if (existing.isPresent()) {
            PillResistance pr = existing.get();
            pr.setCount(pr.getCount() + 1);
            mapper.update(pr);
            return pr.getCount();
        } else {
            PillResistance pr = PillResistance.create(userId, templateId);
            mapper.insert(pr);
            return 1;
        }
    }
}
