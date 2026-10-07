package com.bella.backend.domain.compra.port.out;

import com.bella.backend.domain.compra.model.Compra;
import com.bella.backend.domain.compra.port.in.ListarComprasUseCase.FiltroListagem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompraRepositoryPort {

    Compra salvar(Compra compra);

    Optional<Compra> buscarPorId(UUID id);

    List<Compra> listar(FiltroListagem filtro);
}
