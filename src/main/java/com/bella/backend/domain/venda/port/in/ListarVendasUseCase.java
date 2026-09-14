package com.bella.backend.domain.venda.port.in;

import com.bella.backend.domain.venda.model.StatusVenda;
import com.bella.backend.domain.venda.model.Venda;

import java.util.List;
import java.util.UUID;

public interface ListarVendasUseCase {

    List<Venda> listar(FiltroListagem filtro);

    record FiltroListagem(UUID clienteId, StatusVenda status) {
    }
}
