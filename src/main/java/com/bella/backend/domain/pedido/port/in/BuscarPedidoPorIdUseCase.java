package com.bella.backend.domain.pedido.port.in;

import com.bella.backend.domain.pedido.model.Pedido;

import java.util.UUID;

public interface BuscarPedidoPorIdUseCase {

    Pedido buscarPorId(UUID id);
}
