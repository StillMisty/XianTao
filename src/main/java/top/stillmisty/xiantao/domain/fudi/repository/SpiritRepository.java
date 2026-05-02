package top.stillmisty.xiantao.domain.fudi.repository;

import top.stillmisty.xiantao.domain.fudi.entity.Spirit;

import java.util.Optional;

public interface SpiritRepository {

    Optional<Spirit> findById(Long id);

    Optional<Spirit> findByFudiId(Long fudiId);

    Spirit save(Spirit spirit);

    void deleteById(Long id);
}
