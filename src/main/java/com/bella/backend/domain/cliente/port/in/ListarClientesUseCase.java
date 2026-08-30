package com.bella.backend.domain.cliente.port.in;

import com.bella.backend.domain.cliente.model.Cliente;
import com.bella.backend.domain.cliente.model.StatusCliente;

import java.util.List;

public interface ListarClientesUseCase {

    List<Cliente> listar(FiltroListagem filtro);

    record FiltroListagem(String termoBusca, StatusCliente status) {
    }
}
