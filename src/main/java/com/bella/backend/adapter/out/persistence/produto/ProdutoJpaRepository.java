package com.bella.backend.adapter.out.persistence.produto;

import com.bella.backend.domain.produto.model.StatusProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProdutoJpaRepository extends JpaRepository<ProdutoJpaEntity, UUID> {

    boolean existsByCodigoInterno(String codigoInterno);

    boolean existsByCodigoInternoAndIdNot(String codigoInterno, UUID id);

    boolean existsByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarrasAndIdNot(String codigoBarras, UUID id);

    @Query("""
            SELECT p FROM ProdutoJpaEntity p
            WHERE (:status IS NULL OR p.status = :status)
            AND (:categoriaId IS NULL OR p.categoriaId = :categoriaId)
            AND (:termo IS NULL
                 OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%'))
                 OR LOWER(p.codigoInterno) LIKE LOWER(CONCAT('%', :termo, '%')))
            ORDER BY p.nome
            """)
    List<ProdutoJpaEntity> buscarPorFiltro(@Param("termo") String termo,
                                            @Param("status") StatusProduto status,
                                            @Param("categoriaId") UUID categoriaId);
}
