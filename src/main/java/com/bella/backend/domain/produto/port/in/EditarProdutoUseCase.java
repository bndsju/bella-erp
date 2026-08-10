package com.bella.backend.domain.produto.port.in;

import com.bella.backend.domain.produto.model.Produto;

import java.math.BigDecimal;
import java.util.UUID;

public interface EditarProdutoUseCase {

    Produto editar(UUID id, Comando comando);

    record Comando(
            String codigoBarras,
            String nome,
            String descricao,
            UUID categoriaId,
            UUID unidadeMedidaId,
            BigDecimal precoCusto,
            BigDecimal precoVenda,
            BigDecimal estoqueMinimo
    ) {
    }
}
