package com.bella.backend.domain.cliente.port.in;

import com.bella.backend.domain.cliente.model.Cliente;

public interface CadastrarClienteUseCase {

    Cliente cadastrar(ComandoCliente comando);
}
