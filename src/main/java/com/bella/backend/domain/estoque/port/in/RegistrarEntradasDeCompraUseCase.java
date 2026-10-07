package com.bella.backend.domain.estoque.port.in;

import java.util.List;
import java.util.UUID;

/**
 * Gera uma entrada de estoque por item de uma compra recebida. Idempotente: se a compra já
 * tiver entradas registradas, a chamada não tem efeito, evitando estoque duplicado.
 */
public interface RegistrarEntradasDeCompraUseCase {

    void registrarEntradasDeCompra(UUID compraId, List<ItemEntradaCompra> itens);
}
