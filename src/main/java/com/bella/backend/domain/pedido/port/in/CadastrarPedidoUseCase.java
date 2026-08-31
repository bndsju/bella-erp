package com.bella.backend.domain.pedido.port.in;

import com.bella.backend.domain.pedido.model.Pedido;

public interface CadastrarPedidoUseCase {

    Pedido cadastrar(ComandoPedido comando);
}
