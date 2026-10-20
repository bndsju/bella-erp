package com.bella.backend.adapter.in.web.fluxocaixa;

import com.bella.backend.domain.fluxocaixa.model.SituacaoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.TipoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.port.in.ComandoLancamentoManualCaixa;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LancamentoManualCaixaRequest(
        @NotNull(message = "Tipo é obrigatório") TipoMovimentacaoCaixa tipo,
        @NotBlank(message = "Descrição é obrigatória") String descricao,
        @NotNull(message = "Categoria é obrigatória") UUID categoriaId,
        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero") BigDecimal valor,
        @NotNull(message = "Situação é obrigatória") SituacaoMovimentacaoCaixa situacao,
        @NotNull(message = "Data prevista é obrigatória") LocalDate dataPrevista,
        LocalDate dataEfetiva,
        String observacoes
) {

    public ComandoLancamentoManualCaixa paraComando() {
        return new ComandoLancamentoManualCaixa(tipo, descricao, categoriaId, valor, situacao, dataPrevista,
                dataEfetiva, observacoes);
    }
}
