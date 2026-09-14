package com.bella.backend.domain.venda.port.out;

import com.bella.backend.domain.venda.model.Venda;
import com.bella.backend.domain.venda.port.in.ListarVendasUseCase.FiltroListagem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendaRepositoryPort {

    Venda salvar(Venda venda);

    Optional<Venda> buscarPorId(UUID id);

    List<Venda> listar(FiltroListagem filtro);

    boolean existePorPedidoId(UUID pedidoId);
}
