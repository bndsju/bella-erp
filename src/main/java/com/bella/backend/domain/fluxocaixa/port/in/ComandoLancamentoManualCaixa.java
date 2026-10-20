package com.bella.backend.domain.fluxocaixa.port.in;

import com.bella.backend.domain.fluxocaixa.model.SituacaoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.TipoMovimentacaoCaixa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Comando compartilhado entre lançamento e edição de lançamento manual. Na edição, situacao e
 * dataEfetiva são ignoradas: só lançamentos previstos podem ser editados, e a realização tem
 * operação própria.
 */
public record ComandoLancamentoManualCaixa(
        TipoMovimentacaoCaixa tipo,
        String descricao,
        UUID categoriaId,
        BigDecimal valor,
        SituacaoMovimentacaoCaixa situacao,
        LocalDate dataPrevista,
        LocalDate dataEfetiva,
        String observacoes
) {
}
