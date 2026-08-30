package com.bella.backend.adapter.out.persistence.orcamento;

import com.bella.backend.domain.orcamento.model.StatusOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrcamentoJpaRepository extends JpaRepository<OrcamentoJpaEntity, UUID> {

    @Query("""
            SELECT o FROM OrcamentoJpaEntity o
            WHERE (:clienteId IS NULL OR o.clienteId = :clienteId)
            AND (:status IS NULL OR o.status = :status)
            ORDER BY o.criadoEm DESC
            """)
    List<OrcamentoJpaEntity> buscarPorFiltro(@Param("clienteId") UUID clienteId,
                                              @Param("status") StatusOrcamento status);
}
