package com.bella.backend.adapter.in.web.fluxocaixa;

import com.bella.backend.domain.categoriafluxocaixa.model.CategoriaFluxoCaixa;
import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.OrigemMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.SituacaoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.TipoMovimentacaoCaixa;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MovimentacaoCaixaResponse(
        UUID id,
        TipoMovimentacaoCaixa tipo,
        String descricao,
        UUID categoriaId,
        String categoriaNome,
        BigDecimal valor,
        SituacaoMovimentacaoCaixa situacao,
        LocalDate dataPrevista,
        LocalDate dataEfetiva,
        OrigemMovimentacaoCaixa origem,
        UUID origemReferenciaId,
        String observacoes,
        boolean cancelada,
        String motivoCancelamento,
        Instant canceladaEm,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static MovimentacaoCaixaResponse de(MovimentacaoCaixa movimentacao, CategoriaFluxoCaixa categoria) {
        return new MovimentacaoCaixaResponse(
                movimentacao.getId(),
                movimentacao.getTipo(),
                movimentacao.getDescricao(),
                categoria == null ? null : categoria.getId(),
                categoria == null ? null : categoria.getNome(),
                movimentacao.getValor(),
                movimentacao.getSituacao(),
                movimentacao.getDataPrevista(),
                movimentacao.getDataEfetiva(),
                movimentacao.getOrigem(),
                movimentacao.getOrigemReferenciaId(),
                movimentacao.getObservacoes(),
                movimentacao.isCancelada(),
                movimentacao.getMotivoCancelamento(),
                movimentacao.getCanceladaEm(),
                movimentacao.getCriadoEm(),
                movimentacao.getAtualizadoEm());
    }
}
