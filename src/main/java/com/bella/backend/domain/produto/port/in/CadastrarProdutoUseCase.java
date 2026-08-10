package com.bella.backend.domain.produto.port.in;

import com.bella.backend.domain.produto.model.Produto;

import java.math.BigDecimal;
import java.util.UUID;

public interface CadastrarProdutoUseCase {

    Produto cadastrar(Comando comando);

    record Comando(
            String codigoInterno,
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
