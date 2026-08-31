package com.bella.backend.domain.pedido.port.out;

import com.bella.backend.domain.pedido.model.Pedido;
import com.bella.backend.domain.pedido.port.in.ListarPedidosUseCase.FiltroListagem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoRepositoryPort {

    Pedido salvar(Pedido pedido);

    Optional<Pedido> buscarPorId(UUID id);

    List<Pedido> listar(FiltroListagem filtro);

    boolean existePorId(UUID id);
}
