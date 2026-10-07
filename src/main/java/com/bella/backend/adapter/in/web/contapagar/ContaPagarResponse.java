package com.bella.backend.adapter.in.web.contapagar;

import com.bella.backend.domain.categoriadespesa.model.CategoriaDespesa;
import com.bella.backend.domain.contapagar.model.ContaPagar;
import com.bella.backend.domain.contapagar.model.OrigemContaPagar;
import com.bella.backend.domain.contapagar.model.StatusContaPagar;
import com.bella.backend.domain.fornecedor.model.Fornecedor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ContaPagarResponse(
        UUID id,
        String descricao,
        UUID fornecedorId,
        String fornecedorNome,
        UUID categoriaDespesaId,
        String categoriaDespesaNome,
        BigDecimal valorTotal,
        BigDecimal valorPago,
        BigDecimal saldoPendente,
        LocalDate dataLancamento,
        String observacoes,
        OrigemContaPagar origem,
        UUID origemReferenciaId,
        StatusContaPagar status,
        boolean vencida,
        List<ParcelaContaPagarResponse> parcelas,
        String motivoCancelamento,
        Instant canceladaEm,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static ContaPagarResponse de(ContaPagar conta, Fornecedor fornecedor, CategoriaDespesa categoria,
                                         LocalDate hoje) {
        return new ContaPagarResponse(
                conta.getId(),
                conta.getDescricao(),
                fornecedor == null ? null : fornecedor.getId(),
                fornecedor == null ? null : fornecedor.getNome(),
                categoria.getId(),
                categoria.getNome(),
                conta.getValorTotal(),
                conta.getValorPago(),
                conta.getSaldoPendente(),
                conta.getDataLancamento(),
                conta.getObservacoes(),
                conta.getOrigem(),
                conta.getOrigemReferenciaId(),
                conta.getStatus(),
                conta.estaVencida(hoje),
                conta.getParcelas().stream().map(parcela -> ParcelaContaPagarResponse.de(parcela, hoje)).toList(),
                conta.getMotivoCancelamento(),
                conta.getCanceladaEm(),
                conta.getCriadoEm(),
                conta.getAtualizadoEm());
    }
}
