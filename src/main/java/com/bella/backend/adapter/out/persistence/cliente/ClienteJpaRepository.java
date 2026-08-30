package com.bella.backend.adapter.out.persistence.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClienteJpaRepository extends JpaRepository<ClienteJpaEntity, UUID> {

    @Query(value = """
            SELECT c.id FROM bella.clientes c
            LEFT JOIN bella.clientes_pf pf ON pf.cliente_id = c.id
            LEFT JOIN bella.clientes_pj pj ON pj.cliente_id = c.id
            WHERE (:status IS NULL OR c.status = :status)
            AND (:termo IS NULL
                 OR pf.nome_completo ILIKE CONCAT('%', :termo, '%')
                 OR pj.razao_social ILIKE CONCAT('%', :termo, '%'))
            ORDER BY COALESCE(pf.nome_completo, pj.razao_social)
            """, nativeQuery = true)
    List<UUID> buscarIdsPorFiltro(@Param("termo") String termo, @Param("status") String status);
}
