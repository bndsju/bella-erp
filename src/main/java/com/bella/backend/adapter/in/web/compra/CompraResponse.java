package com.bella.backend.adapter.in.web.compra;

import com.bella.backend.domain.compra.model.Compra;
import com.bella.backend.domain.compra.model.StatusCompra;
import com.bella.backend.domain.fornecedor.model.Fornecedor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CompraResponse(
        UUID id,
        UUID fornecedorId,
        String fornecedorNome,
        LocalDate dataCompra,
        LocalDate previsaoRecebimento,
        LocalDate dataRecebimento,
        List<CompraItemResponse> itens,
        BigDecimal valorProdutos,
        BigDecimal descontoGeral,
        BigDecimal descontoTotal,
        BigDecimal frete,
        BigDecimal outrasDespesas,
        BigDecimal valorTotal,
        String condicaoPagamento,
        String observacoes,
        StatusCompra status,
        String motivoCancelamento,
        Instant confirmadaEm,
        Instant canceladaEm,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static CompraResponse de(Compra compra, Fornecedor fornecedor, List<CompraItemResponse> itens) {
        return new CompraResponse(
                compra.getId(),
                fornecedor.getId(),
                fornecedor.getNome(),
                compra.getDataCompra(),
                compra.getPrevisaoRecebimento(),
                compra.getDataRecebimento(),
                itens,
                compra.getValorProdutos(),
                compra.getDescontoGeral(),
                compra.getDescontoTotal(),
                compra.getFrete(),
                compra.getOutrasDespesas(),
                compra.getValorTotal(),
                compra.getCondicaoPagamento(),
                compra.getObservacoes(),
                compra.getStatus(),
                compra.getMotivoCancelamento(),
                compra.getConfirmadaEm(),
                compra.getCanceladaEm(),
                compra.getCriadoEm(),
                compra.getAtualizadoEm());
    }
}
