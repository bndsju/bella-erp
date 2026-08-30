package com.bella.backend.domain.cliente.port.in;

import com.bella.backend.domain.cliente.model.Cliente;
import com.bella.backend.domain.cliente.model.StatusCliente;

import java.util.UUID;

public interface AlterarStatusClienteUseCase {

    Cliente alterarStatus(UUID id, StatusCliente novoStatus);
}
