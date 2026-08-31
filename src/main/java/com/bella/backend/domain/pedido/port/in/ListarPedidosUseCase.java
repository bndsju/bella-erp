package com.bella.backend.domain.pedido.port.in;

import com.bella.backend.domain.pedido.model.Pedido;
import com.bella.backend.domain.pedido.model.StatusPedido;

import java.util.List;
import java.util.UUID;

public interface ListarPedidosUseCase {

    List<Pedido> listar(FiltroListagem filtro);

    record FiltroListagem(UUID clienteId, StatusPedido status) {
    }
}
