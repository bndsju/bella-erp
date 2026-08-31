package com.bella.backend.adapter.out.persistence.venda;

import com.bella.backend.domain.venda.model.StatusVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VendaJpaRepository extends JpaRepository<VendaJpaEntity, UUID> {

    boolean existsByPedidoId(UUID pedidoId);

    @Query("""
            SELECT v FROM VendaJpaEntity v
            WHERE (:clienteId IS NULL OR v.clienteId = :clienteId)
            AND (:status IS NULL OR v.status = :status)
            ORDER BY v.criadoEm DESC
            """)
    List<VendaJpaEntity> buscarPorFiltro(@Param("clienteId") UUID clienteId, @Param("status") StatusVenda status);
}
