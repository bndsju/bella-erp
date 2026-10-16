package com.bella.backend.adapter.in.web.contareceber;

import com.bella.backend.domain.contareceber.model.OrigemContaReceber;
import com.bella.backend.domain.contareceber.port.in.ComandoContaReceber;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Recebimento único: informe vencimento. Parcelado: informe parcelas (soma igual ao valor total).
 * A API sempre lança como MANUAL; a origem VENDA é reservada para a integração interna futura.
 */
public record ContaReceberRequest(
        @NotBlank(message = "Descrição é obrigatória") String descricao,
        UUID clienteId,
        @NotNull(message = "Valor da conta é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor da conta deve ser maior que zero") BigDecimal valorTotal,
        LocalDate vencimento,
        @Valid List<ParcelaContaReceberRequest> parcelas,
        String observacoes
) {

    public ComandoContaReceber paraComando() {
        return new ComandoContaReceber(
                descricao,
                clienteId,
                valorTotal,
                vencimento,
                parcelas == null ? List.of() : parcelas.stream().map(ParcelaContaReceberRequest::paraComando).toList(),
                observacoes,
                OrigemContaReceber.MANUAL,
                null);
    }
}
