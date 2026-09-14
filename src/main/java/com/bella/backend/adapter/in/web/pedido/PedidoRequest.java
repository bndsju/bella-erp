package com.bella.backend.adapter.in.web.pedido;

import com.bella.backend.domain.pedido.port.in.ComandoPedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PedidoRequest(
        @NotNull(message = "Cliente é obrigatório") UUID clienteId,
        UUID transportadoraId,
        @NotEmpty(message = "Pedido precisa ter ao menos um item") @Valid List<PedidoItemRequest> itens,
        @NotNull(message = "Percentual de desconto é obrigatório")
        @DecimalMin(value = "0", message = "Percentual de desconto deve estar entre 0 e 100")
        @DecimalMax(value = "100", message = "Percentual de desconto deve estar entre 0 e 100") BigDecimal percentualDesconto,
        @NotNull(message = "Valor do frete é obrigatório")
        @DecimalMin(value = "0", message = "Valor do frete não pode ser negativo") BigDecimal valorFrete,
        @NotBlank(message = "Condição de pagamento é obrigatória") String condicaoPagamento
) {

    public ComandoPedido paraComando() {
        return new ComandoPedido(
                clienteId,
                transportadoraId,
                itens.stream().map(PedidoItemRequest::paraComando).toList(),
                percentualDesconto,
                valorFrete,
                condicaoPagamento);
    }
}
