package com.bella.backend.domain.compra.port.in;

import com.bella.backend.domain.compra.model.Compra;

public interface CadastrarCompraUseCase {

    Compra cadastrar(ComandoCompra comando);
}
