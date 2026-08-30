package com.bella.backend.adapter.in.web.orcamento;

import com.bella.backend.domain.cliente.model.Cliente;
import com.bella.backend.domain.orcamento.model.Orcamento;
import com.bella.backend.domain.orcamento.model.StatusOrcamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OrcamentoResponse(
        UUID id,
        UUID clienteId,
        String clienteNome,
        List<OrcamentoItemResponse> itens,
        BigDecimal percentualDesconto,
        BigDecimal valorFrete,
        BigDecimal subtotal,
        BigDecimal valorDesconto,
        BigDecimal valorTotal,
        LocalDate prazoValidade,
        String condicaoPagamento,
        String observacoes,
        StatusOrcamento status,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static OrcamentoResponse de(Orcamento orcamento, Cliente cliente, List<OrcamentoItemResponse> itens) {
        return new OrcamentoResponse(
                orcamento.getId(),
                cliente.getId(),
                cliente.getNomeExibicao(),
                itens,
                orcamento.getPercentualDesconto(),
                orcamento.getValorFrete(),
                orcamento.getSubtotal(),
                orcamento.getValorDesconto(),
                orcamento.getValorTotal(),
                orcamento.getPrazoValidade(),
                orcamento.getCondicaoPagamento(),
                orcamento.getObservacoes(),
                orcamento.getStatus(),
                orcamento.getCriadoEm(),
                orcamento.getAtualizadoEm());
    }
}
