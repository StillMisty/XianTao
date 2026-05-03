package top.stillmisty.xiantao.domain.fudi.repository;

import top.stillmisty.xiantao.domain.fudi.entity.SpiritForm;

import java.util.List;
import java.util.Optional;

public interface SpiritFormRepository {

    Optional<SpiritForm> findById(Long id);

    List<SpiritForm> findAll();
}
