package com.bella.backend.adapter.out.persistence.contapagar;

import com.bella.backend.domain.contapagar.model.OrigemContaPagar;
import com.bella.backend.domain.contapagar.model.StatusContaPagar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ContaPagarJpaRepository extends JpaRepository<ContaPagarJpaEntity, UUID> {

    boolean existsByOrigemAndOrigemReferenciaId(OrigemContaPagar origem, UUID origemReferenciaId);

    @Query("""
            SELECT c FROM ContaPagarJpaEntity c
            WHERE (:fornecedorId IS NULL OR c.fornecedorId = :fornecedorId)
            AND (:categoriaDespesaId IS NULL OR c.categoriaDespesaId = :categoriaDespesaId)
            AND (:status IS NULL OR c.status = :status)
            ORDER BY c.criadoEm DESC
            """)
    List<ContaPagarJpaEntity> buscarPorFiltro(@Param("fornecedorId") UUID fornecedorId,
                                               @Param("categoriaDespesaId") UUID categoriaDespesaId,
                                               @Param("status") StatusContaPagar status);
}
