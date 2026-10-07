package com.bella.backend.adapter.out.persistence.compra;

import com.bella.backend.domain.compra.model.StatusCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CompraJpaRepository extends JpaRepository<CompraJpaEntity, UUID> {

    @Query("""
            SELECT c FROM CompraJpaEntity c
            WHERE (:fornecedorId IS NULL OR c.fornecedorId = :fornecedorId)
            AND (:status IS NULL OR c.status = :status)
            AND (:dataInicio IS NULL OR c.dataCompra >= :dataInicio)
            AND (:dataFim IS NULL OR c.dataCompra <= :dataFim)
            ORDER BY c.criadoEm DESC
            """)
    List<CompraJpaEntity> buscarPorFiltro(@Param("fornecedorId") UUID fornecedorId,
                                           @Param("status") StatusCompra status,
                                           @Param("dataInicio") LocalDate dataInicio,
                                           @Param("dataFim") LocalDate dataFim);
}
