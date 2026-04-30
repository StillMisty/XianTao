package top.stillmisty.xiantao.infrastructure.mybatis.repository;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.item.entity.EquipmentTemplate;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.infrastructure.mybatis.mapper.EquipmentTemplateMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EquipmentTemplateRepositoryImpl implements EquipmentTemplateRepository {

    private final EquipmentTemplateMapper mapper;

    @Override
    public Optional<EquipmentTemplate> findById(Long id) {
        return Optional.ofNullable(mapper.selectOneById(id));
    }

    @Override
    public Optional<EquipmentTemplate> findByTemplateId(Long templateId) {
        return Optional.ofNullable(
                mapper.selectOneByQuery(QueryWrapper.create()
                        .eq("template_id", templateId))
        );
    }

    @Override
    public List<EquipmentTemplate> findAll() {
        return mapper.selectAll();
    }
}
