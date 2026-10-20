package com.bella.backend.adapter.out.persistence.fluxocaixa;

import com.bella.backend.domain.fluxocaixa.model.OrigemMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.SituacaoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.TipoMovimentacaoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MovimentacaoCaixaJpaRepository extends JpaRepository<MovimentacaoCaixaJpaEntity, UUID> {

    Optional<MovimentacaoCaixaJpaEntity> findByOrigemAndOrigemReferenciaId(OrigemMovimentacaoCaixa origem,
                                                                            UUID origemReferenciaId);

    @Query("""
            SELECT m FROM MovimentacaoCaixaJpaEntity m
            WHERE (:tipo IS NULL OR m.tipo = :tipo)
            AND (:categoriaId IS NULL OR m.categoriaId = :categoriaId)
            AND (:origem IS NULL OR m.origem = :origem)
            AND (:situacao IS NULL OR m.situacao = :situacao)
            AND (:incluirCanceladas = TRUE OR m.cancelada = FALSE)
            ORDER BY m.criadoEm DESC
            """)
    List<MovimentacaoCaixaJpaEntity> buscarPorFiltro(@Param("tipo") TipoMovimentacaoCaixa tipo,
                                                      @Param("categoriaId") UUID categoriaId,
                                                      @Param("origem") OrigemMovimentacaoCaixa origem,
                                                      @Param("situacao") SituacaoMovimentacaoCaixa situacao,
                                                      @Param("incluirCanceladas") boolean incluirCanceladas);
}
