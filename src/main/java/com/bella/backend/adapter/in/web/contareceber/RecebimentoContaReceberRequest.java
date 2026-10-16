package com.bella.backend.adapter.in.web.contareceber;

import com.bella.backend.domain.contareceber.port.in.ComandoRecebimentoContaReceber;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecebimentoContaReceberRequest(
        @NotNull(message = "Parcela é obrigatória") UUID parcelaId,
        @NotNull(message = "Valor recebido é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor recebido deve ser maior que zero") BigDecimal valorRecebido,
        LocalDate dataRecebimento,
        @DecimalMin(value = "0", message = "Juros não podem ser negativos") BigDecimal juros,
        @DecimalMin(value = "0", message = "Multa não pode ser negativa") BigDecimal multa,
        @DecimalMin(value = "0", message = "Desconto não pode ser negativo") BigDecimal desconto,
        String observacao
) {

    public ComandoRecebimentoContaReceber paraComando() {
        return new ComandoRecebimentoContaReceber(parcelaId, valorRecebido, dataRecebimento, juros, multa, desconto,
                observacao);
    }
}
