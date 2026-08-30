package com.bella.backend.domain.cliente.port.in;

import com.bella.backend.domain.cliente.model.Cliente;

import java.util.UUID;

public interface BuscarClientePorIdUseCase {

    Cliente buscarPorId(UUID id);
}
