package com.bella.backend.adapter.out.persistence.contareceber;

import com.bella.backend.domain.contareceber.model.OrigemContaReceber;
import com.bella.backend.domain.contareceber.model.StatusContaReceber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ContaReceberJpaRepository extends JpaRepository<ContaReceberJpaEntity, UUID> {

    boolean existsByOrigemAndOrigemReferenciaId(OrigemContaReceber origem, UUID origemReferenciaId);

    @Query("""
            SELECT c FROM ContaReceberJpaEntity c
            WHERE (:clienteId IS NULL OR c.clienteId = :clienteId)
            AND (:status IS NULL OR c.status = :status)
            ORDER BY c.criadoEm DESC
            """)
    List<ContaReceberJpaEntity> buscarPorFiltro(@Param("clienteId") UUID clienteId,
                                               @Param("status") StatusContaReceber status);
}
