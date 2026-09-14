package com.bella.backend.domain.venda.port.in;

import com.bella.backend.domain.venda.model.Venda;

import java.util.UUID;

public interface BuscarVendaPorIdUseCase {

    Venda buscarPorId(UUID id);
}
