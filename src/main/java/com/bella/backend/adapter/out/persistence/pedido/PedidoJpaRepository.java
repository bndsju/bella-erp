package com.bella.backend.adapter.out.persistence.pedido;

import com.bella.backend.domain.pedido.model.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PedidoJpaRepository extends JpaRepository<PedidoJpaEntity, UUID> {

    @Query("""
            SELECT p FROM PedidoJpaEntity p
            WHERE (:clienteId IS NULL OR p.clienteId = :clienteId)
            AND (:status IS NULL OR p.status = :status)
            ORDER BY p.criadoEm DESC
            """)
    List<PedidoJpaEntity> buscarPorFiltro(@Param("clienteId") UUID clienteId, @Param("status") StatusPedido status);
}
