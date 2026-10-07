package com.bella.backend.domain.compra.port.in;

import com.bella.backend.domain.compra.model.Compra;
import com.bella.backend.domain.compra.model.StatusCompra;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ListarComprasUseCase {

    List<Compra> listar(FiltroListagem filtro);

    /** O período filtra pela data da compra, com limites inclusivos. */
    record FiltroListagem(UUID fornecedorId, StatusCompra status, LocalDate dataInicio, LocalDate dataFim) {
    }
}
