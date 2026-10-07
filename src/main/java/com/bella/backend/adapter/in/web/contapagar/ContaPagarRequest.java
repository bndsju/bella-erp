package com.bella.backend.adapter.in.web.contapagar;

import com.bella.backend.domain.contapagar.model.OrigemContaPagar;
import com.bella.backend.domain.contapagar.port.in.ComandoContaPagar;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Pagamento único: informe vencimento. Parcelado: informe parcelas (soma igual ao valor total).
 * A API sempre lança como MANUAL; a origem COMPRA é reservada para a integração interna futura.
 */
public record ContaPagarRequest(
        @NotBlank(message = "Descrição é obrigatória") String descricao,
        UUID fornecedorId,
        @NotNull(message = "Categoria de despesa é obrigatória") UUID categoriaDespesaId,
        @NotNull(message = "Valor da conta é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor da conta deve ser maior que zero") BigDecimal valorTotal,
        LocalDate vencimento,
        @Valid List<ParcelaContaPagarRequest> parcelas,
        String observacoes
) {

    public ComandoContaPagar paraComando() {
        return new ComandoContaPagar(
                descricao,
                fornecedorId,
                categoriaDespesaId,
                valorTotal,
                vencimento,
                parcelas == null ? List.of() : parcelas.stream().map(ParcelaContaPagarRequest::paraComando).toList(),
                observacoes,
                OrigemContaPagar.MANUAL,
                null);
    }
}
