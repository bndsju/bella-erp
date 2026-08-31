package com.bella.backend.domain.pedido.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record ComandoItemPedido(UUID produtoId, BigDecimal quantidade, BigDecimal valorUnitario) {
}
