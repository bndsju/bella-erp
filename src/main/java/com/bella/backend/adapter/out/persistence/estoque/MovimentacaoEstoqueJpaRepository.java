package com.bella.backend.adapter.out.persistence.estoque;

import com.bella.backend.domain.estoque.model.TipoMovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MovimentacaoEstoqueJpaRepository extends JpaRepository<MovimentacaoEstoqueJpaEntity, UUID> {

    List<MovimentacaoEstoqueJpaEntity> findByOrigemOperacaoIdOrderByDataHoraAsc(UUID origemOperacaoId);

    boolean existsByOrigemOperacaoIdAndTipo(UUID origemOperacaoId, TipoMovimentacaoEstoque tipo);

    @Query("""
            SELECT m FROM MovimentacaoEstoqueJpaEntity m
            WHERE (:produtoId IS NULL OR m.produtoId = :produtoId)
            AND (:tipo IS NULL OR m.tipo = :tipo)
            AND (:dataInicio IS NULL OR m.dataHora >= :dataInicio)
            AND (:dataFim IS NULL OR m.dataHora <= :dataFim)
            ORDER BY m.dataHora DESC
            """)
    List<MovimentacaoEstoqueJpaEntity> buscarPorFiltro(@Param("produtoId") UUID produtoId,
                                                         @Param("tipo") TipoMovimentacaoEstoque tipo,
                                                         @Param("dataInicio") Instant dataInicio,
                                                         @Param("dataFim") Instant dataFim);
}
