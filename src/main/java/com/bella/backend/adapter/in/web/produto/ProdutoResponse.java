package com.bella.backend.adapter.in.web.produto;

import com.bella.backend.adapter.in.web.categoria.CategoriaResponse;
import com.bella.backend.adapter.in.web.unidademedida.UnidadeMedidaResponse;
import com.bella.backend.domain.produto.model.Produto;
import com.bella.backend.domain.produto.model.StatusProduto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProdutoResponse(
        UUID id,
        String codigoInterno,
        String codigoBarras,
        String nome,
        String descricao,
        CategoriaResponse categoria,
        UnidadeMedidaResponse unidadeMedida,
        BigDecimal precoCusto,
        BigDecimal precoVenda,
        BigDecimal estoqueMinimo,
        StatusProduto status,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static ProdutoResponse de(Produto produto, CategoriaResponse categoria,
                                      UnidadeMedidaResponse unidadeMedida) {
        return new ProdutoResponse(produto.getId(), produto.getCodigoInterno(), produto.getCodigoBarras(),
                produto.getNome(), produto.getDescricao(), categoria, unidadeMedida, produto.getPrecoCusto(),
                produto.getPrecoVenda(), produto.getEstoqueMinimo(), produto.getStatus(), produto.getCriadoEm(),
                produto.getAtualizadoEm());
    }
}
