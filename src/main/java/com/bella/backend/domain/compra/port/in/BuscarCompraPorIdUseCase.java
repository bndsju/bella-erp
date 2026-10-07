package com.bella.backend.domain.compra.port.in;

import com.bella.backend.domain.compra.model.Compra;

import java.util.UUID;

public interface BuscarCompraPorIdUseCase {

    Compra buscarPorId(UUID id);
}
