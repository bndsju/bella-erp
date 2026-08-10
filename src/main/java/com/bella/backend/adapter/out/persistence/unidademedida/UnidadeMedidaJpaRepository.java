package com.bella.backend.adapter.out.persistence.unidademedida;

import com.bella.backend.domain.unidademedida.model.StatusUnidadeMedida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UnidadeMedidaJpaRepository extends JpaRepository<UnidadeMedidaJpaEntity, UUID> {

    List<UnidadeMedidaJpaEntity> findByStatusOrderByNome(StatusUnidadeMedida status);

    List<UnidadeMedidaJpaEntity> findAllByOrderByNome();
}
