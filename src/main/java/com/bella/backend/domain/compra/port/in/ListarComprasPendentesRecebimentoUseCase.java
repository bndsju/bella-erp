package com.bella.backend.domain.compra.port.in;

import com.bella.backend.domain.compra.model.Compra;

import java.util.List;

/** Compras confirmadas que ainda não foram recebidas. */
public interface ListarComprasPendentesRecebimentoUseCase {

    List<Compra> listarPendentesDeRecebimento();
}
