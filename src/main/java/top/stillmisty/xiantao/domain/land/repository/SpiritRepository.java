package top.stillmisty.xiantao.domain.land.repository;

import top.stillmisty.xiantao.domain.land.entity.Spirit;

import java.util.Optional;

public interface SpiritRepository {

    Optional<Spirit> findById(Long id);

    Optional<Spirit> findByFudiId(Long fudiId);

    Spirit save(Spirit spirit);

    void deleteById(Long id);
}
