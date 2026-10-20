package com.bella.backend.domain.fluxocaixa.port.in;

import com.bella.backend.domain.fluxocaixa.model.OrigemMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.SituacaoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.TipoMovimentacaoCaixa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Entrada usada pelos módulos de origem (Contas a Receber/Pagar) ao alimentar o fluxo de caixa. */
public record ComandoMovimentacaoAutomaticaCaixa(
        TipoMovimentacaoCaixa tipo,
        String descricao,
        UUID categoriaId,
        BigDecimal valor,
        SituacaoMovimentacaoCaixa situacao,
        LocalDate dataPrevista,
        LocalDate dataEfetiva,
        OrigemMovimentacaoCaixa origem,
        UUID origemReferenciaId,
        String observacoes
) {
}
